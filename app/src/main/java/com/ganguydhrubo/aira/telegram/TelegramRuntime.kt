package com.ganguydhrubo.aira.telegram

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Network
import android.util.Log
import com.ganguydhrubo.aira.BuildConfig
import com.ganguydhrubo.aira.AiraApp
import com.ganguydhrubo.aira.R
import com.ganguydhrubo.aira.i18n.tr
import com.ganguydhrubo.aira.core.action.ConfirmChoice
import com.ganguydhrubo.aira.core.net.NetProxy
import com.ganguydhrubo.aira.core.run.ChannelGateway
import com.ganguydhrubo.aira.core.run.ChannelPeer
import com.ganguydhrubo.aira.core.run.Delivery
import com.ganguydhrubo.aira.core.run.MemoryOwnerIdentity
import com.ganguydhrubo.aira.core.run.RunId
import com.ganguydhrubo.aira.core.cap.telegram.TelegramAccount
import com.ganguydhrubo.aira.core.cap.telegram.TelegramApi
import com.ganguydhrubo.aira.core.cap.telegram.TelegramHttpException
import com.ganguydhrubo.aira.core.cap.telegram.TelegramUser
import com.ganguydhrubo.aira.settings.FeatureFlags
import com.ganguydhrubo.aira.task.TaskService
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit
import kotlin.math.min

sealed interface TelegramConnectionState {
    data object Off : TelegramConnectionState
    data object NeedsBinding : TelegramConnectionState
    data object Connecting : TelegramConnectionState
    data object Connected : TelegramConnectionState
    data class Failed(val message: String) : TelegramConnectionState
}

/** One owner, private text and images over a Telegram bot. A foreground service keeps polling alive; tasks still go through TaskService. */
class TelegramRuntime(
    private val context: Context,
    val store: TelegramStore,
    client: OkHttpClient? = null,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val http = client ?: NetProxy.shared.client(OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS).writeTimeout(20, TimeUnit.SECONDS)
        .readTimeout(45, TimeUnit.SECONDS))
    private val api = TelegramApi(http, BuildConfig.VERSION_NAME)
    private val owners = MemoryOwnerIdentity()
    private val app get() = context.applicationContext as AiraApp
    private val gateway = ChannelGateway(owners,
        { threadId -> app.taskState.run(threadId)?.activeAsk },
        object : ChannelGateway.Route {
            override suspend fun newTask(peer: ChannelPeer, messageId: String, text: String) =
                TaskService.startChannel(context, peer, messageId, text)

            override suspend fun paused(peer: ChannelPeer, messageId: String) =
                com.ganguydhrubo.aira.task.replyPaused(app.outbox, channel, peer, messageId)

            override suspend fun answer(threadId: String, askId: String, text: String): Boolean =
                app.taskState.run(threadId)?.answerFromChannel(text, askId) == true

            override suspend fun confirm(threadId: String, askId: String, choice: ConfirmChoice) = false
            override suspend fun stop(runId: RunId) = TaskService.cancel(context, runId.value)
            override suspend fun cancelAsk(threadId: String) {
                val run = app.taskState.run(threadId) ?: return
                run.activeAsk?.askId?.let(run::cancelAsk)
            }
        }, com.ganguydhrubo.aira.settings.AiraPause.get(context))

    val channel = TelegramChannel(store, api,
        activeAsk = { threadId -> app.taskState.run(threadId)?.activeAsk },
        imageFiles = androidTelegramImages(context, api) { store.account() },
        imageFailureText = { tr(R.string.telegram_image_receive_failed) },
        reportImageFailure = ::reportImageFailure)

    private suspend fun reportImageFailure(peer: ChannelPeer, notice: Delivery.Notice) {
        app.outbox.deliver(channel, peer, notice)
    }
    private var registration: AutoCloseable? = null
    private var monitor: Job? = null
    private val network = TelegramNetwork(context) { restartMonitor() }
    private val _state = MutableStateFlow<TelegramConnectionState>(
        if (store.account() == null) TelegramConnectionState.NeedsBinding else TelegramConnectionState.Off)
    val state: StateFlow<TelegramConnectionState> = _state.asStateFlow()
    val allowUnlock: StateFlow<Boolean> = store.allowUnlock

    fun channelFor(id: String) = channel.takeIf { id == TelegramChannel.ID }
    fun acknowledge(messageId: String) = store.acknowledge(messageId)

    @Synchronized fun sync(): Boolean {
        if (FeatureFlags.get(context).quitRequested()) return false
        val account = store.account()
        if (!store.enabled() || account == null) {
            _state.value = TelegramConnectionState.NeedsBinding.takeIf { account == null } ?: TelegramConnectionState.Off
            return false
        }
        if (registration == null) {
            owners.bind(ChannelPeer(TelegramChannel.ID, account.ownerId, ""))
            registration = gateway.start(channel)
        }
        network.start()
        if (monitor?.isActive != true) {
            _state.value = TelegramConnectionState.Connecting
            monitor = scope.launch { monitorLoop() }
        }
        return true
    }

    @Synchronized fun stop() {
        monitor?.cancel()
        monitor = null
        registration?.close()
        registration = null
        network.stop()
        _state.value = if (store.account() == null) TelegramConnectionState.NeedsBinding else TelegramConnectionState.Off
    }

    fun ensureRunning() {
        if (FeatureFlags.get(context).quitRequested() || !store.enabled() || store.account() == null) return
        _state.value = TelegramConnectionState.Connecting
        runCatching { context.startForegroundService(Intent(context, TelegramService::class.java)) }
            .onFailure { _state.value = TelegramConnectionState.Failed(it.message ?: it::class.simpleName.orEmpty()) }
    }

    fun setEnabled(enabled: Boolean) {
        if (enabled) check(store.account() != null) { "Link Telegram before enabling it" }
        store.setEnabled(enabled)
        if (enabled) ensureRunning() else {
            context.stopService(Intent(context, TelegramService::class.java))
            stop()
        }
    }

    fun setUnlockAllowed(allowed: Boolean) = store.setUnlockAllowed(allowed)

    /** Checks a pasted bot token and removes any webhook, since long polling and webhooks exclude each other. */
    suspend fun checkToken(token: String): TelegramUser = withContext(Dispatchers.IO) {
        val clean = token.trim()
        require(TelegramApi.isTokenShaped(clean)) { "Invalid bot token" }
        val bot = api.getMe(clean)
        api.deleteWebhook(clean)
        bot
    }

    /**
     * Waits until someone sends [code] to the bot in a private chat; that person becomes the owner.
     * Cancelling only stops this wait; an existing link keeps working. Returns null after [PAIRING_TIMEOUT_MS].
     */
    suspend fun awaitPairing(token: String, bot: TelegramUser, code: String): TelegramAccount? {
        val clean = token.trim()
        var cursor = ""
        val deadline = System.currentTimeMillis() + PAIRING_TIMEOUT_MS
        while (currentCoroutineContext().isActive && System.currentTimeMillis() < deadline) {
            val list = api.rawUpdates(clean, cursor)
            for (update in list) {
                val message = update.message ?: continue
                val from = message.from ?: continue
                if (message.chat.type != "private" || from.isBot || !TelegramApi.matchesPairing(message.text, code)) continue
                val account = TelegramAccount(clean, bot.id.toString(), bot.username.orEmpty(), from.id.toString())
                bind(account, (update.updateId + 1).toString(), message.chat.id.toString())
                runCatching {
                    withContext(Dispatchers.IO) {
                        api.sendText(account, message.chat.id.toString(), "pairing", tr(R.string.telegram_paired_message), null)
                    }
                }
                return account
            }
            list.maxOfOrNull { it.updateId }?.let { cursor = (it + 1).toString() }
        }
        return null
    }

    private fun bind(account: TelegramAccount, cursor: String, chatId: String) {
        store.account()?.let { old -> owners.unbind(ChannelPeer(TelegramChannel.ID, old.ownerId, "")) }
        store.saveAccount(account)
        store.setEnabled(true)
        store.saveCursor(cursor, account)
        store.messagePeer(account, chatId)
        owners.bind(ChannelPeer(TelegramChannel.ID, account.ownerId, ""))
        // Any poll for the old link is cancelled here; the new one starts from the saved offset.
        restartMonitor()
        ensureRunning()
    }

    fun unbind() {
        context.stopService(Intent(context, TelegramService::class.java))
        store.account()?.let { old -> owners.unbind(ChannelPeer(TelegramChannel.ID, old.ownerId, "")) }
        stop()
        store.clear()
        _state.value = TelegramConnectionState.NeedsBinding
    }

    @Synchronized private fun restartMonitor() {
        if (registration == null || !store.enabled()) return
        monitor?.cancel()
        monitor = scope.launch { monitorLoop() }
    }

    private suspend fun monitorLoop() {
        var failures = 0
        while (currentCoroutineContext().isActive && store.enabled()) {
            try {
                channel.pollOnce()
                failures = 0
                _state.value = TelegramConnectionState.Connected
            } catch (e: CancellationException) {
                throw e
            } catch (e: TelegramHttpException) {
                if (e.httpCode == 401 || e.httpCode == 403) {
                    store.setEnabled(false)
                    _state.value = TelegramConnectionState.NeedsBinding
                    context.stopService(Intent(context, TelegramService::class.java))
                    return
                }
                failures++
                _state.value = TelegramConnectionState.Failed(e.message.orEmpty())
                delay(backoff(failures))
            } catch (e: Exception) {
                failures++
                Log.w(TAG, "Telegram polling failed", e)
                _state.value = TelegramConnectionState.Failed(e.message ?: e::class.simpleName.orEmpty())
                delay(backoff(failures))
            }
        }
    }

    private fun backoff(failures: Int) = min(30_000L, 1_000L * (1L shl (failures - 1).coerceAtMost(5)))

    companion object {
        private const val TAG = "AiraTelegram"
        const val PAIRING_TIMEOUT_MS = 15 * 60_000L
    }
}

private class TelegramNetwork(context: Context, private val changed: () -> Unit) {
    private val manager = context.getSystemService(ConnectivityManager::class.java)
    private var started = false
    private val callback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) = changed()
        override fun onLost(network: Network) = changed()
    }

    @Synchronized fun start() {
        if (started) return
        runCatching { manager.registerDefaultNetworkCallback(callback) }.onSuccess { started = true }
    }

    @Synchronized fun stop() {
        if (!started) return
        runCatching { manager.unregisterNetworkCallback(callback) }
        started = false
    }
}
