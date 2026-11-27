package net.primal.android.notes.translation

import io.kotest.matchers.shouldBe
import org.junit.Test

class LanguageDetectorTest {

    @Test
    fun `detects spanish prose`() {
        LanguageDetector.detectLanguage(
            "Este es un mensaje de prueba para todos los amigos que están aquí con nosotros",
        ) shouldBe "es"
    }

    @Test
    fun `detects english prose`() {
        LanguageDetector.detectLanguage(
            "This is the house that we built for all of the people in the town",
        ) shouldBe "en"
    }

    @Test
    fun `detects indonesian prose`() {
        LanguageDetector.detectLanguage(
            "Ini adalah pesan yang saya kirim untuk semua orang yang ada di sini",
        ) shouldBe "id"
    }

    @Test
    fun `detects japanese by kana`() {
        LanguageDetector.detectLanguage("これはテストメッセージです。よろしくお願いします") shouldBe "ja"
    }

    @Test
    fun `detects korean by hangul`() {
        LanguageDetector.detectLanguage("이것은 테스트 메시지입니다 감사합니다") shouldBe "ko"
    }

    @Test
    fun `detects chinese by han without kana`() {
        LanguageDetector.detectLanguage("这是一个测试消息 谢谢大家的关注与支持") shouldBe "zh"
    }

    @Test
    fun `detects ukrainian by distinctive cyrillic letters`() {
        LanguageDetector.detectLanguage("Це повідомлення для всіх моїх друзів їжак") shouldBe "uk"
    }

    @Test
    fun `returns null for very short text`() {
        LanguageDetector.detectLanguage("hola") shouldBe null
        LanguageDetector.detectLanguage("") shouldBe null
    }

    @Test
    fun `primaryLanguageCode strips region and case`() {
        LanguageDetector.primaryLanguageCode("en-US") shouldBe "en"
        LanguageDetector.primaryLanguageCode("PT_br") shouldBe "pt"
        LanguageDetector.primaryLanguageCode("") shouldBe null
        LanguageDetector.primaryLanguageCode(null) shouldBe null
    }
}
