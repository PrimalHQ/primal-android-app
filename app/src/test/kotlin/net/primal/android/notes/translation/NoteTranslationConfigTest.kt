package net.primal.android.notes.translation

import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import net.primal.android.notes.translation.remote.toDeepLLanguage
import net.primal.android.user.domain.NoteTranslationProvider
import net.primal.android.user.domain.NoteTranslationSettings
import org.junit.Test

class NoteTranslationConfigTest {

    @Test
    fun `empty libretranslate url is fail-closed and never invents a host`() {
        NoteTranslationConfig.normalizeLibreTranslateBaseUrl("") shouldBe null
        NoteTranslationConfig.normalizeLibreTranslateBaseUrl("   ") shouldBe null
        NoteTranslationConfig.normalizeLibreTranslateBaseUrl("/translate") shouldBe null
    }

    @Test
    fun `invalid schemes and hosts are rejected`() {
        NoteTranslationConfig.normalizeLibreTranslateBaseUrl("ftp://translate.local") shouldBe null
        NoteTranslationConfig.normalizeLibreTranslateBaseUrl("not a url") shouldBe null
        NoteTranslationConfig.normalizeLibreTranslateBaseUrl("https://") shouldBe null
        NoteTranslationConfig.normalizeLibreTranslateBaseUrl("javascript:alert(1)") shouldBe null
    }

    @Test
    fun `normalizes base url and full translate path without doubling`() {
        NoteTranslationConfig.normalizeLibreTranslateBaseUrl("https://lt.example/") shouldBe "https://lt.example"
        NoteTranslationConfig.normalizeLibreTranslateBaseUrl(
            "https://lt.example/translate",
        ) shouldBe "https://lt.example"
        NoteTranslationConfig.normalizeLibreTranslateBaseUrl(
            "http://192.168.1.10:5000/translate/",
        ) shouldBe "http://192.168.1.10:5000"
    }

    @Test
    fun `prefer on-device stays available without a network provider`() {
        val plan = NoteTranslationConfig.resolve(NoteTranslationSettings())
        plan.shouldBeInstanceOf<TranslationPlan.OnDeviceThenNetwork>()
        (plan as TranslationPlan.OnDeviceThenNetwork).network shouldBe null
    }

    @Test
    fun `libretranslate without url is unavailable`() {
        val settings = NoteTranslationSettings(provider = NoteTranslationProvider.LibreTranslate)
        NoteTranslationConfig.resolve(settings) shouldBe TranslationPlan.Unavailable
        NoteTranslationConfig.isReady(settings) shouldBe false
    }

    @Test
    fun `deepl and google without keys are unavailable`() {
        NoteTranslationConfig.resolve(
            NoteTranslationSettings(provider = NoteTranslationProvider.DeepL),
        ) shouldBe TranslationPlan.Unavailable
        NoteTranslationConfig.resolve(
            NoteTranslationSettings(provider = NoteTranslationProvider.Google),
        ) shouldBe TranslationPlan.Unavailable
    }

    @Test
    fun `configured providers resolve to network-only routes`() {
        val libre = NoteTranslationConfig.resolve(
            NoteTranslationSettings(
                provider = NoteTranslationProvider.LibreTranslate,
                libreTranslateUrl = "https://lt.home/translate",
                apiKey = "k",
            ),
        )
        libre.shouldBeInstanceOf<TranslationPlan.NetworkOnly>()
        val libreRoute = (libre as TranslationPlan.NetworkOnly).network
        libreRoute.shouldBeInstanceOf<NetworkTranslationRoute.LibreTranslate>()
        (libreRoute as NetworkTranslationRoute.LibreTranslate).endpoint shouldBe "https://lt.home"

        val deepl = NoteTranslationConfig.resolveNetworkRoute(
            NoteTranslationSettings(provider = NoteTranslationProvider.DeepL, apiKey = "abc:fx"),
        )
        deepl.shouldBeInstanceOf<NetworkTranslationRoute.DeepL>()
        (deepl as NetworkTranslationRoute.DeepL).endpoint shouldBe NoteTranslationConfig.DEEPL_FREE_ENDPOINT

        val google = NoteTranslationConfig.resolveNetworkRoute(
            NoteTranslationSettings(provider = NoteTranslationProvider.Google, apiKey = "gcp"),
        )
        google.shouldBeInstanceOf<NetworkTranslationRoute.Google>()
    }

    @Test
    fun `deepl pro keys use the pro endpoint`() {
        NoteTranslationConfig.deeplEndpoint("keep-secret") shouldBe NoteTranslationConfig.DEEPL_PRO_ENDPOINT
    }

    @Test
    fun `prefer on-device uses libretranslate only as an explicit fallback`() {
        val plan = NoteTranslationConfig.resolve(
            NoteTranslationSettings(libreTranslateUrl = "https://lt.home"),
        )
        plan.shouldBeInstanceOf<TranslationPlan.OnDeviceThenNetwork>()
        (plan as TranslationPlan.OnDeviceThenNetwork).network
            .shouldBeInstanceOf<NetworkTranslationRoute.LibreTranslate>()
    }

    @Test
    fun `missing fallback url on prefer-on-device does not invent a public host`() {
        NoteTranslationConfig.resolveNetworkRoute(NoteTranslationSettings()).shouldBeNull()
    }

    @Test
    fun `deepL language tags map regional variants`() {
        toDeepLLanguage("zh") shouldBe "ZH-HANS"
        toDeepLLanguage("zh-TW") shouldBe "ZH-HANT"
        toDeepLLanguage("pt") shouldBe "PT-BR"
        toDeepLLanguage("en") shouldBe "EN"
        toDeepLLanguage("de") shouldBe "DE"
    }
}
