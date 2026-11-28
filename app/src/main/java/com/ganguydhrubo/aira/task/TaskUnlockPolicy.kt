package com.ganguydhrubo.aira.task

import com.ganguydhrubo.aira.core.run.ChannelPeer
import com.ganguydhrubo.aira.core.run.RunOrigin

/**
 * Unlock is offered to scheduled tasks, or to on-phone tasks while the local switch is on; it is re-read at unlock time.
 * Every other source (MCP, child tasks, channels) never gets unlock permission.
 */
internal fun taskUnlockAuthorization(
    origin: RunOrigin,
    peer: ChannelPeer?,
    localUnlockEnabled: () -> Boolean = { false },
): (() -> Boolean)? = when {
    origin == RunOrigin.Trigger -> ({ true })
    origin == RunOrigin.Ui && localUnlockEnabled() -> ({ localUnlockEnabled() })
    else -> null
}
