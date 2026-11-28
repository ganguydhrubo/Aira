package com.ganguydhrubo.aira.selfcheck

import com.ganguydhrubo.aira.core.screen.uistate.ScreenSnapshot
import com.ganguydhrubo.aira.core.screen.uistate.UiElement
import com.ganguydhrubo.aira.core.screen.uistate.UiTreeNode
import com.ganguydhrubo.aira.core.screen.uistate.UiWindowTree
import softassert.assertEquals
import softassert.assertFalse
import softassert.assertNull
import softassert.assertTrue
import org.junit.Test

/** 自检读屏按窗口归属认控件,不把合并节点和最上层窗口的包名混为一谈。 */
class SelfCheckWindowTest {
    @Test
    fun systemWindowAboveCompleteAiraContentIsReady() {
        val aira = window(1, AIRA, layer = 1, controls = EXPECTED.map { "aira-$it" to it })
        val system = window(2, SYSTEM_UI, layer = 2, controls = listOf("system-shade" to "notification_shade"))

        val result = inspect(snapshot(SYSTEM_UI, system, aira), topTaskPackage = AIRA)

        assertTrue(result.ready)
        assertFalse(result.wrongAppOnTop)
        assertEquals(EXPECTED, result.content?.elements?.map { it.resourceId })
        assertEquals(AIRA, result.content?.packageName)
    }

    @Test
    fun fullSystemApplicationAboveCompleteAiraContentStillFails() {
        val aira = window(1, AIRA, layer = 1, controls = EXPECTED.map { "aira-$it" to it })
        val settings = window(2, SETTINGS, layer = 2, controls = listOf("settings-content" to "settings_content"))

        val result = inspect(snapshot(SETTINGS, settings, aira), topTaskPackage = SETTINGS)

        assertFalse(result.ready)
        assertTrue(result.wrongAppOnTop)
        assertTrue(result.missing.isEmpty())
    }

    @Test
    fun systemOverlayWithUnknownTaskOwnershipIsNotAssumedSafe() {
        val aira = window(1, AIRA, layer = 1, controls = EXPECTED.map { "aira-$it" to it })
        val system = window(2, SYSTEM_UI, layer = 2, controls = listOf("system-shade" to "notification_shade"))

        val result = inspect(snapshot(SYSTEM_UI, system, aira), topTaskPackage = null)

        assertFalse(result.ready)
        assertTrue(result.wrongAppOnTop)
    }

    @Test
    fun systemWindowDoesNotHideMissingAiraControls() {
        val present = EXPECTED - BUTTON
        val aira = window(1, AIRA, layer = 1, controls = present.map { "aira-$it" to it })
        val system = window(2, SYSTEM_UI, layer = 2, controls = listOf("system-button" to BUTTON))

        val result = inspect(snapshot(SYSTEM_UI, system, aira), topTaskPackage = AIRA)

        assertFalse(result.ready)
        assertEquals(listOf(BUTTON), result.missing)
        assertFalse(result.wrongAppOnTop)
        assertEquals(present, result.content?.elements?.map { it.resourceId })
    }

    @Test
    fun controlsInAnotherAppsWindowCannotCompleteAiraPage() {
        val aira = window(1, AIRA, layer = 1, controls = listOf("aira-$TITLE" to TITLE))
        val other = window(2, OTHER_APP, layer = 2, controls = EXPECTED.map { "other-$it" to it })

        val result = inspect(snapshot(OTHER_APP, other, aira))

        assertFalse(result.ready)
        assertTrue(result.wrongAppOnTop)
        assertEquals(EXPECTED - TITLE, result.missing)
        assertEquals(listOf(TITLE), result.content?.elements?.map { it.resourceId })
    }

    @Test
    fun completeAiraContentBelowAnotherAppStillFails() {
        val aira = window(1, AIRA, layer = 1, controls = EXPECTED.map { "aira-$it" to it })
        val other = window(2, OTHER_APP, layer = 2, controls = listOf("other-content" to "other_content"))

        val result = inspect(snapshot(OTHER_APP, other, aira))

        assertFalse(result.ready)
        assertTrue(result.wrongAppOnTop)
        assertTrue(result.missing.isEmpty())
    }

    @Test
    fun expectedControlsOnlyInAnotherWindowDoNotCountAsAiraContent() {
        val other = window(1, OTHER_APP, layer = 1, controls = EXPECTED.map { "other-$it" to it })

        val result = inspect(snapshot(OTHER_APP, other))

        assertFalse(result.ready)
        assertNull(result.content)
        assertEquals(EXPECTED, result.missing)
        assertTrue(result.wrongAppOnTop)
    }

    private fun inspect(screen: ScreenSnapshot, topTaskPackage: String? = screen.packageName): SelfCheckWindowRead = readSelfCheckWindow(
        screen, AIRA, topTaskPackage, EXPECTED, isSystemPackage = { it == SYSTEM_UI || it == SETTINGS },
    ) { key -> elements.firstOrNull { it.resourceId == key } }

    private fun snapshot(topPackage: String, vararg windows: UiWindowTree): ScreenSnapshot = ScreenSnapshot(
        snapshotId = "self-check",
        packageName = topPackage,
        elements = windows.flatMap { window -> window.nodes.flatMap(::elements) },
        width = 1080,
        height = 2400,
        windows = windows.toList(),
    )

    private fun elements(root: UiTreeNode): List<UiElement> = buildList {
        fun visit(node: UiTreeNode) {
            val ref = node.ref
            if (ref != null) add(UiElement(
                ref = ref,
                role = node.role,
                name = node.resourceId.orEmpty(),
                centerX = 100,
                centerY = 100,
                bounds = node.bounds,
                resourceId = node.resourceId,
                clickable = false,
                editable = false,
                scrollable = false,
            ))
            node.children.forEach(::visit)
        }
        visit(root)
    }

    private fun window(
        id: Int,
        packageName: String,
        layer: Int,
        controls: List<Pair<String, String>>,
    ): UiWindowTree = UiWindowTree(
        id = id,
        type = "application",
        layer = layer,
        title = null,
        packageName = packageName,
        active = true,
        focused = layer == 2,
        nodes = listOf(UiTreeNode(
            role = "FrameLayout",
            bounds = BOUNDS,
            children = controls.map { (ref, resourceId) -> UiTreeNode(
                role = "View",
                bounds = BOUNDS,
                ref = ref,
                resourceId = resourceId,
            ) },
        )),
    )

    private companion object {
        const val AIRA = "com.ganguydhrubo.aira"
        const val SYSTEM_UI = "com.android.systemui"
        const val SETTINGS = "com.android.settings"
        const val OTHER_APP = "com.example.other"
        const val TITLE = "selfcheck_title"
        const val BUTTON = "selfcheck_button"
        val EXPECTED = listOf(TITLE, "selfcheck_count", BUTTON, "selfcheck_input", "selfcheck_tick", "selfcheck_list")
        val BOUNDS = listOf(0, 0, 999, 999)
    }
}
