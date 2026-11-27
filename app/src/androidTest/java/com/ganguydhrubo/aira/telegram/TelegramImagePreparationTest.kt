package com.ganguydhrubo.aira.telegram

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import com.ganguydhrubo.aira.core.cap.telegram.TelegramDownloadedImage
import com.ganguydhrubo.aira.core.cap.telegram.TelegramImages
import org.junit.Assert.*
import org.junit.Test
import java.io.ByteArrayOutputStream
import java.io.IOException

class TelegramImagePreparationTest {
    private fun png(width: Int, height: Int): ByteArray {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        return try {
            bitmap.eraseColor(Color.BLUE)
            ByteArrayOutputStream().also { assertTrue(bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)) }.toByteArray()
        } finally { bitmap.recycle() }
    }

    @Test fun smallPngRemainsByteForByteUnchanged() {
        val bytes = png(24, 24)
        val prepared = prepareTelegramImage(TelegramImages.validate(bytes))
        assertEquals("png", prepared.extension)
        assertArrayEquals(bytes, prepared.bytes)
    }

    @Test fun largeDimensionsAreSampledBeforeDecodeAndRemainReadable() {
        val prepared = prepareTelegramImage(TelegramImages.validate(png(8193, 64)))
        val bitmap = BitmapFactory.decodeByteArray(prepared.bytes, 0, prepared.bytes.size)
        assertNotNull(bitmap)
        try {
            assertTrue(bitmap.width <= 4096)
            assertTrue(prepared.bytes.size <= 5 * 1024 * 1024)
            assertEquals(Color.BLUE, bitmap.getPixel(0, 0))
        } finally { bitmap.recycle() }
    }

    @Test fun forgedImageHeadersAreRejected() {
        val fake = TelegramDownloadedImage(byteArrayOf(0x89.toByte(), 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a), "image/png", "png")
        assertTrue(runCatching { prepareTelegramImage(fake) }.exceptionOrNull() is IOException)
    }
}
