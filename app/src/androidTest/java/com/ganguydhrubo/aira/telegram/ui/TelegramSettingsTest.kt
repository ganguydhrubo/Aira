package com.ganguydhrubo.aira.telegram.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.platform.app.InstrumentationRegistry
import com.ganguydhrubo.aira.EnglishUi
import com.ganguydhrubo.aira.core.cap.telegram.TelegramAccount
import com.ganguydhrubo.aira.settings.KeyCipher
import com.ganguydhrubo.aira.ui.theme.AiraTheme
import com.ganguydhrubo.aira.telegram.TelegramRuntime
import com.ganguydhrubo.aira.telegram.TelegramStore
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.io.File
import java.util.UUID
import com.ganguydhrubo.aira.ui.clickInDialog

class TelegramSettingsTest {
    @get:Rule(order = Int.MIN_VALUE) val chinese = EnglishUi()
    @get:Rule val compose = createComposeRule()
    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private val dir = File(context.cacheDir, "telegram-settings-ui-${UUID.randomUUID()}")
    private val cipher = object : KeyCipher {
        override fun encrypt(plain: ByteArray): ByteArray = plain
        override fun decrypt(data: ByteArray): ByteArray = data
    }
    private val store = TelegramStore(dir, cipher)
    private val account = TelegramAccount("test-token", "https://example.test", "bot", "owner")
    private var runtime: TelegramRuntime? = null

    @After fun cleanup() {
        runtime?.stop()
        dir.deleteRecursively()
    }

    private fun page() {
        val value = TelegramRuntime(context, store).also { runtime = it }
        compose.setContent { AiraTheme(dark = false, reduceMotion = true) {
            TelegramSettingsScreen(value, {})
        } }
    }

    @Test fun unlockPermissionIsExplicitAndRebindingUpdatesTheOpenPage() {
        store.saveAccount(account)
        page()
        compose.onNodeWithTag("telegram-allow-unlock").performScrollTo().assertIsOff().performClick()
        compose.waitUntil(5_000) { store.unlockAllowed() }
        compose.onNodeWithTag("telegram-allow-unlock").assertIsOn()
        assertFalse(store.enabled())
        assertTrue(TelegramStore(dir, cipher).unlockAllowed())

        // 重绑并不一定改变连接状态；已打开的页面仍必须观察到授权被清除。
        compose.runOnIdle { store.saveAccount(account.copy(token = "new-test-token")) }
        compose.onNodeWithTag("telegram-allow-unlock").assertIsOff()
    }

    @Test fun unbindingRemovesTheUnlockControlAndRevokesPermission() {
        store.saveAccount(account)
        store.setUnlockAllowed(true)
        page()
        compose.onNodeWithTag("telegram-allow-unlock").performScrollTo().assertIsOn()
        compose.onNodeWithText("解除绑定").performScrollTo().performClick()
        compose.clickInDialog("解除绑定")
        compose.waitUntil(5_000) { store.account() == null }
        compose.onNodeWithTag("telegram-allow-unlock").assertDoesNotExist()
        assertFalse(store.unlockAllowed())
        assertFalse(TelegramStore(dir, cipher).unlockAllowed())
    }

    @Test fun failedSaveShowsAnErrorWithoutGrantingUnlock() {
        store.saveAccount(account)
        page()
        // 模拟授权文件写入失败，核对页面没有显示尚未保存的授权。
        assertTrue(File(dir, "unlock.json.tmp").mkdir())
        compose.onNodeWithTag("telegram-allow-unlock").performScrollTo().performClick()
        compose.waitUntil(5_000) {
            compose.onAllNodesWithText("设置未能保存，请重试。").fetchSemanticsNodes().isNotEmpty()
        }
        compose.onNodeWithText("设置未能保存，请重试。").performScrollTo().assertIsDisplayed()
        compose.onNodeWithTag("telegram-allow-unlock").performScrollTo().assertIsOff().assertIsEnabled()
        assertFalse(store.unlockAllowed())
        assertFalse(TelegramStore(dir, cipher).unlockAllowed())
    }
}
