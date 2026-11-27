package com.ganguydhrubo.aira.core.device.shell

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AccessibilityListTest {
    private val aira = PermissionCommands.accessibilityService("com.ganguydhrubo.aira")
    private val airaLong = "com.ganguydhrubo.aira/com.ganguydhrubo.aira.a11y.AiraAccessibilityService"
    private val other = "com.example.reader/.ReaderService"
    private val other2 = "org.sample.keys/org.sample.keys.KeyService"

    @Test
    fun emptyOrUnsetValues() {
        listOf(null, "", "   ", "null", "null\n", ":", " : ").forEach { raw ->
            assertEquals("raw=$raw", emptyList<String>(), AccessibilityList.entries(raw))
            assertFalse(AccessibilityList.contains(raw, aira))
            assertEquals(aira, AccessibilityList.with(raw, aira))
            assertEquals("", AccessibilityList.without(raw, aira))
        }
    }

    @Test
    fun appendKeepsOtherServicesInOrder() {
        assertEquals("$other:$aira", AccessibilityList.with(other, aira))
        assertEquals("$other:$other2:$aira", AccessibilityList.with("$other:$other2\n", aira))
    }

    @Test
    fun alreadyPresentIsNotDuplicated() {
        assertEquals("$other:$aira", AccessibilityList.with("$other:$aira", aira))
        // 系统里存的是完整类名写法,也算已经有了
        assertEquals("$airaLong:$other", AccessibilityList.with("$airaLong:$other", aira))
        assertTrue(AccessibilityList.contains(airaLong, aira))
    }

    @Test
    fun removeKeepsOthersAndHandlesBothSpellings() {
        assertEquals("$other:$other2", AccessibilityList.without("$other:$aira:$other2", aira))
        assertEquals(other, AccessibilityList.without("$airaLong:$other", aira))
        assertEquals(other, AccessibilityList.without(other, aira))
        assertEquals("", AccessibilityList.without(aira, aira))
    }

    @Test
    fun strayColonsAndSpacesAreCleanedUp() {
        assertEquals("$other:$other2:$aira", AccessibilityList.with(":$other:: $other2 :", aira))
        assertEquals("$other:$other2", AccessibilityList.without("$other::$aira:$other2:", aira))
    }

    @Test
    fun differentServiceOfSamePackageIsNotConfused() {
        val sibling = "com.ganguydhrubo.aira/.a11y.OtherService"
        assertFalse(AccessibilityList.contains(sibling, aira))
        assertEquals(sibling, AccessibilityList.without("$sibling:$aira", aira))
    }
}
