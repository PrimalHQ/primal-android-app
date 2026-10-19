<!-- Improved compatibility of back to top link: See: https://github.com/othneildrew/Best-README-Template/pull/73 -->
<a name="readme-top"></a>
<!--
*** Thanks for checking out the Best-README-Template. If you have a suggestion
*** that would make this better, please fork the repo and create a pull request
*** or simply open an issue with the tag "enhancement".
*** Don't forget to give the project a star!
*** Thanks again! Now go create something AMAZING! :D
-->



<!-- PROJECT SHIELDS -->
<!--
*** I'm using markdown "reference style" links for readability.
*** Reference links are enclosed in brackets [ ] instead of parentheses ( ).
*** See the bottom of this document for the declaration of the reference variables
*** for contributors-url, forks-url, etc. This is an optional, concise syntax you may use.
*** https://www.markdownguide.org/basic-syntax/#reference-style-links
-->
[![Contributors][contributors-shield]][contributors-url]
[![Forks][forks-shield]][forks-url]
[![Stargazers][stars-shield]][stars-url]
[![Issues][issues-shield]][issues-url]
[![MIT License][license-shield]][license-url]



<!-- PROJECT LOGO -->
<!--suppress ALL -->

<br />
<div align="center">
  <a href="https://github.com/PrimalHQ/primal-android-app">
    <img src="https://primal.net/assets/logo-d368885b.svg" alt="Logo" width="80" height="80">
  </a>

<h3 align="center">Primal</h3>

  <p align="center">
    A Nostr social client with a built-in Bitcoin Lightning wallet. Featuring easy onboarding, a fast & snappy
    UI, long-form Reads, custom feeds, advanced search, and zaps.
    <br />
    <a href="https://primal.net"><strong>Explore Primal »</strong></a>
    <br />
    <br />
    <a href="https://play.google.com/store/apps/details?id=net.primal.android">Get it on Google Play</a>
    ·
    <a href="https://github.com/PrimalHQ/primal-android-app/issues">Report Bug</a>
    ·
    <a href="https://github.com/PrimalHQ/primal-android-app/issues">Request Feature</a>
  </p>
</div>



<!-- TABLE OF CONTENTS -->
<details>
  <summary>Table of Contents</summary>
  <ol>
    <li>
      <a href="#about-the-project">About The Project</a>
      <ul>
        <li><a href="#built-with">Built With</a></li>
      </ul>
    </li>
    <li>
      <a href="#getting-started">Getting Started</a>
      <ul>
        <li><a href="#prerequisites">Prerequisites</a></li>
      </ul>
        <li><a href="#building">Building</a></li>
        <ul>
            <li><a href="#debug">Debug</a></li>
            <li><a href="#release">Release</a></li>
        </ul>
    </li>
    <li>
      <a href="#installing">Installing</a>
      <ul>
        <li><a href="#debug-builds">Debug builds</a></li>
        <li><a href="#release-builds">Release builds</a></li>
      </ul>
    </li>
    <li><a href="#note-translation">Note translation</a></li>
    <li><a href="#development">Development</a></li>
    <li><a href="#contributing">Contributing</a></li>
    <li><a href="#license">License</a></li>
    <li><a href="#contact">Contact</a></li>
    <li><a href="#acknowledgments">Acknowledgments</a></li>
  </ol>
</details>



<!-- ABOUT THE PROJECT -->
## About The Project

<div align="center" style="display: flex; justify-content: center; gap: 21px;">
    <img src="https://raw.githubusercontent.com/PrimalHQ/primal-android-app/main/docs/screenshots/feeds.png" alt="Primal Feeds Screenshot" width="200px">
    <img src="https://raw.githubusercontent.com/PrimalHQ/primal-android-app/main/docs/screenshots/reads.png" alt="Primal Reads Screenshot" width="200px">
    <img src="https://raw.githubusercontent.com/PrimalHQ/primal-android-app/main/docs/screenshots/wallet.png" alt="Primal Wallet Screenshot" width="200px">
    <img src="https://raw.githubusercontent.com/PrimalHQ/primal-android-app/main/docs/screenshots/explore.png" alt="Primal Explore Screenshot" width="200px">
</div>

<p align="right">(<a href="#readme-top">back to top</a>)</p>



### Built With

[![Kotlin][Kotlin]][Kotlin-url]
[![Jetpack Compose][Compose]][Compose-url]
[![Kotlin Multiplatform][KMP]][KMP-url]
[![Android Studio][AndroidStudio]][AndroidStudio-url]

Multi-module app built with **Kotlin Multiplatform**, **Jetpack Compose**, **Room** (SQLCipher), and **Hilt**,
following an MVI + Clean Architecture approach.

<p align="right">(<a href="#readme-top">back to top</a>)</p>



<!-- GETTING STARTED -->
## Getting Started

### Prerequisites

This project requires the following pre-requisites:
- Java 21;
- Android SDK (compileSdk 36);
- Android Studio (latest stable, AGP 8.13+, Kotlin 2.2.x);
- Android 8.0+ (API 26) device or emulator;

The app ships in two product flavors (`aosp` and `google`), each with `debug`, `altRelease`, and `playRelease`
build types — giving variants such as `aospDebug`, `aospAltRelease`, and `googlePlayRelease`.

## Building
### Debug
To build debug builds no extra configuration is required, just execute `./gradlew :app:assembleAospDebug`
(or `:app:assembleGoogleDebug`) or run in Android Studio.

### Release
To build release builds you will need to create `config.properties` file in the project root directory.
Following properties in `config.properties` are **MANDATORY** for any release build:
```properties
localStorage.keyAlias={KeystoreAliasForEncryption}
```
If you want to build the release build with your own certificate you can use `googlePlayRelease` or `aospAltRelease`
build variant which will read the certificate details from following properties in `config.properties`:
```properties
{signingConfigName}.storeFile={PathToYourCertificate}
{signingConfigName}.storePassword={CertificatePassword}
{signingConfigName}.keyAlias={YourAlias}
{signingConfigName}.keyPassword={AliasPassword}
```
`{signingConfigName}` should be replaced with `playStore` or `alternative`.

## Installing
Attach your device to the computer or start your emulator and install debug or release build as described below.

### Debug builds
To install debug build execute `./gradlew :app:installAospDebug` or run a `debug` build variant in Android Studio.
Please note that debug builds do not use encryption when storing sensitive information and that the performance is
significantly slower compared to release builds.

### Release builds
To install release build execute `./gradlew installAospAltRelease` or `./gradlew installGooglePlayRelease`, or
run in AndroidStudio. Please note that `googlePlayRelease` and `aospAltRelease` build variants require
`config.properties` configured with mandatory properties. If no certificate properties are provided it fallbacks
to `debug` certificate.

## Note translation

Open **Settings → Content Display → Note translation** to enable or disable
**Show Translate on notes**, choose a target language, and select a provider.
The default target is the device language.

| Provider | Setup and behavior |
| --- | --- |
| Prefer on-device, then your provider | Tries Android's translation service on Android 12 / API 31 or newer. Availability depends on the device service and language support. Falls back only to the LibreTranslate URL and optional key you configure. |
| LibreTranslate (your URL) | Enter your instance's base URL or full `/translate` URL, plus an API key if the instance requires one. |
| DeepL (your API key) | Enter your own DeepL API key. The client selects the Free or Pro endpoint from the key format. |
| Google Cloud Translation (your API key) | Enter your own Google Cloud Translation API key. |

Tap **Translate** below a note to request a translation, then **Show original**
to return to the original text. Short notes and notes containing only protected
tokens do not offer the control. Translation is requested per note; there is no
feed-wide automatic translation.

No public LibreTranslate endpoint or shared API key is supplied. An empty or
invalid LibreTranslate URL, or a missing DeepL/Google key, does not select a
default network service. If on-device translation is unavailable and no valid
network provider is configured, the note displays a setup hint. Network
translation sends the masked note text to the configured service.

Changing provider or editing the LibreTranslate URL clears the saved API key;
enter the key for the new destination. Stored credentials are bound to their
provider and LibreTranslate URL. LibreTranslate URLs must not contain query
parameters, fragments, or embedded usernames/passwords. Device-only settings
show that availability is checked per note, rather than claiming the service
is ready before a device translation succeeds.

URLs, Nostr references, Lightning/Bitcoin identifiers, Cashu tokens, mentions,
hashtags, and emoji shortcodes are masked before either translation path and
restored afterward. Successful results are cached in memory for the session.

### Translation verification

With the development prerequisites above available, run the focused unit tests:

```sh
./gradlew :app:testAospDebugUnitTest --tests 'net.primal.android.notes.translation.*'
```

Device checks should cover an unavailable on-device service with no configured
network fallback, a supported on-device language pair, a configured network
provider, preserved identifiers, **Show original**, and short/token-only notes.
The repository's **Check PR** workflow additionally runs formatting, static
analysis/lint, the full AOSP and KMP test tasks, and AOSP/Google compilation.

## Development
The app is still under the development and changes are frequent. Breaking changes can and will happen in the future.

<!-- CONTRIBUTING -->
## Contributing

Read more about contributions in [CONTRIBUTING.md](CONTRIBUTING.md).

<p align="right">(<a href="#readme-top">back to top</a>)</p>

<!-- LICENSE -->
## License

Distributed under the MIT License. See [LICENSE](LICENSE) for more information.

<p align="right">(<a href="#readme-top">back to top</a>)</p>

<!-- CONTACT -->
## Contact

For help with Primal:

- 🌐 Website — [primal.net](https://primal.net)
- ✉️ Email — [support@primal.net](mailto:support@primal.net)
- 🐛 Report a bug — [GitHub Issues](https://github.com/PrimalHQ/primal-android-app/issues)
- 💡 Request a feature — [GitHub Issues](https://github.com/PrimalHQ/primal-android-app/issues)

Developed by [Appollo41](https://appollo41.com), a software development studio.

<p align="right">(<a href="#readme-top">back to top</a>)</p>



<!-- ACKNOWLEDGMENTS -->
## Acknowledgments

* [Quartz](https://github.com/vitorpamplona/quartz) — Nostr event & NIP-04/NIP-44 crypto
* [Breez SDK](https://breez.technology) — Spark self-custodial Lightning wallet
* [NostrPostr](https://github.com/Giszmo/NostrPostr)
* [Acinq](https://acinq.co)

<p align="right">(<a href="#readme-top">back to top</a>)</p>



<!-- MARKDOWN LINKS & IMAGES -->
<!-- https://www.markdownguide.org/basic-syntax/#reference-style-links -->
[contributors-shield]: https://img.shields.io/github/contributors/PrimalHQ/primal-android-app.svg?style=for-the-badge
[contributors-url]: https://github.com/PrimalHQ/primal-android-app/graphs/contributors
[forks-shield]: https://img.shields.io/github/forks/PrimalHQ/primal-android-app.svg?style=for-the-badge
[forks-url]: https://github.com/PrimalHQ/primal-android-app/network/members
[stars-shield]: https://img.shields.io/github/stars/PrimalHQ/primal-android-app.svg?style=for-the-badge
[stars-url]: https://github.com/PrimalHQ/primal-android-app/stargazers
[issues-shield]: https://img.shields.io/github/issues/PrimalHQ/primal-android-app.svg?style=for-the-badge
[issues-url]: https://github.com/PrimalHQ/primal-android-app/issues
[license-shield]: https://img.shields.io/github/license/PrimalHQ/primal-android-app.svg?style=for-the-badge
[license-url]: https://github.com/PrimalHQ/primal-android-app/blob/main/LICENSE
[Kotlin]: https://img.shields.io/badge/kotlin-000000?style=for-the-badge&logo=kotlin&logoColor=white
[Kotlin-url]: https://kotlinlang.org
[Compose]: https://img.shields.io/badge/jetpack%20compose-000000?style=for-the-badge&logo=jetpackcompose&logoColor=white
[Compose-url]: https://developer.android.com/jetpack/compose
[KMP]: https://img.shields.io/badge/kotlin%20multiplatform-000000?style=for-the-badge&logo=kotlin&logoColor=white
[KMP-url]: https://kotlinlang.org/docs/multiplatform.html
[AndroidStudio]: https://img.shields.io/badge/androidstudio-000000?style=for-the-badge&logo=androidstudio&logoColor=white
[AndroidStudio-url]: https://developer.android.com/studio
