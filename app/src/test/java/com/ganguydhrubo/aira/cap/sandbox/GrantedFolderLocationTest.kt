package com.ganguydhrubo.aira.cap.sandbox

import softassert.assertEquals
import softassert.assertNull
import org.junit.Test

class GrantedFolderLocationTest {
    private val external = "com.android.externalstorage.documents"
    private val downloads = "com.android.providers.downloads.documents"
    private val volumes = mapOf("primary" to "/storage/emulated/10", "ABCD-1234" to "/storage/ABCD-1234")
    private fun location(authority: String, id: String) = grantedFolderLocation(authority, id, volumes::get)

    @Test fun sameNameInDifferentParentsAndVolumesHasDistinctLocations() {
        assertEquals("/storage/emulated/10/Download/Aira", location(external, "primary:Download/Aira"))
        assertEquals("/storage/emulated/10/Documents/Aira", location(external, "primary:Documents/Aira"))
        assertEquals("/storage/ABCD-1234/Download/Aira", location(external, "ABCD-1234:Download/Aira"))
        assertEquals("/storage/emulated/10/Download/学习 %2F", location(downloads, "raw:/storage/emulated/10/Download/学习 %2F"))
    }

    @Test fun opaqueIdentifiersAndMalformedPathsDoNotBecomeLocations() {
        assertNull(location("example.cloud", "primary:Download/Aira"))
        assertNull(location(downloads, "msd:123"))
        assertNull(location(external, "unmounted:Download/Aira"))
        assertNull(location(external, "primary:/Download/Aira"))
        assertNull(location(external, "primary:Download/../Aira"))
        assertNull(location(downloads, "raw:Download/Aira"))
    }
}
