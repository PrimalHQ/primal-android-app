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
    <img src="https://primal.net/assets/logo_fire-409917ad.svg" alt="Logo" width="80" height="80">
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

<table>
  <tr>
    <td width="80" align="center">
      <a href="https://appollo41.com">
        <img src="https://appollo41.com/favicon.svg" alt="Appollo41" width="64" height="64">
      </a>
    </td>
    <td>
      <strong><a href="https://appollo41.com">Appollo41</a></strong> — Nostr and Bitcoin apps.
      Shaped with you. Built to last.<br>
      We build Nostr &amp; Bitcoin/Lightning products and multiplatform Android &amp; iOS apps
      (Kotlin/KMP), and take teams from product discovery to a shipped MVP.
    </td>
  </tr>
</table>

- 🌐 Website — [appollo41.com](https://appollo41.com)
- ✉️ Email — [hello@appollo41.com](mailto:hello@appollo41.com)
- 💻 GitHub — [@Appollo41](https://github.com/Appollo41)
- 📅 Book a discovery call — [calendly.com/appollo41](https://calendly.com/appollo41/getting-to-know)

**Maintainer:** Alex — [@alex](https://primal.net/profile/npub1ky9s6hjl46wxcj9gcalhuk4ag2nea9yqufdyp9q9r496fns5g44sw0alex)

Project Link: [https://github.com/PrimalHQ/primal-android-app](https://github.com/PrimalHQ/primal-android-app)

<p align="right">(<a href="#readme-top">back to top</a>)</p>



<!-- ACKNOWLEDGMENTS -->
## Acknowledgments

* [Quartz](https://github.com/vitorpamplona/quartz) — Nostr event & NIP-04/NIP-44 crypto
* [Breez SDK](https://breez.technology) — Spark self-custodial Lightning wallet
* [NostrPostr](https://github.com/Giszmo/NostrPostr)

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
