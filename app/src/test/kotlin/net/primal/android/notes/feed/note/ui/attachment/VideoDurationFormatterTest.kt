package net.primal.android.notes.feed.note.ui.attachment

import io.kotest.matchers.shouldBe
import org.junit.Test

class VideoDurationFormatterTest {

    @Test
    fun `formats zero as colon zero-zero`() {
        formatVideoDuration(0.0) shouldBe "0:00"
    }

    @Test
    fun `formats sub-minute seconds with leading zero minute`() {
        formatVideoDuration(48.0) shouldBe "0:48"
        formatVideoDuration(3.0) shouldBe "0:03"
    }

    @Test
    fun `floors fractional seconds toward zero`() {
        formatVideoDuration(47.9) shouldBe "0:47"
        formatVideoDuration(0.9) shouldBe "0:00"
    }

    @Test
    fun `formats multi-minute values under one hour as M colon SS`() {
        formatVideoDuration(65.0) shouldBe "1:05"
        formatVideoDuration(3599.0) shouldBe "59:59"
    }

    @Test
    fun `formats one hour and above as H colon MM colon SS`() {
        formatVideoDuration(3600.0) shouldBe "1:00:00"
        formatVideoDuration(3923.0) shouldBe "1:05:23"
        formatVideoDuration(45296.0) shouldBe "12:34:56"
    }

    @Test
    fun `clamps negative values to zero`() {
        formatVideoDuration(-5.0) shouldBe "0:00"
    }
}
