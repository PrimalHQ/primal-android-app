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
    Featuring easy onboarding, fast & snappy UI, ability to explore Nostr, and create & manage custom feeds
    <br />
    <a href="https://github.com/PrimalHQ/primal-android-app"><strong>Explore the docs »</strong></a>
    <br />
    <br />
    <a href="https://github.com/PrimalHQ/primal-android-app">View Demo</a>
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
        <li><a href="#debug builds">Debug builds</a></li>
        <li><a href="#release builds">Release builds</a></li>
      </ul>
    </li>
    <li><a href="#contributing">Contributing</a></li>
    <li><a href="#license">License</a></li>
    <li><a href="#contact">Contact</a></li>
    <li><a href="#acknowledgments">Acknowledgments</a></li>
  </ol>
</details>



<!-- ABOUT THE PROJECT -->
## About The Project

<div align="center" style="display: flex; justify-content: center; gap: 10px;">
    <img src="https://github.com/PrimalHQ/primal-android-app/blob/main/docs/screenshots/home_promo.png" alt="Primal Home Screenshot" style="max-width: 100px; height: auto;">
    <img src="https://github.com/PrimalHQ/primal-android-app/blob/main/docs/screenshots/wallet_promo.png" alt="Primal Wallet Screenshot" style="max-width: 100px; height: auto;">
    <img src="https://github.com/PrimalHQ/primal-android-app/blob/main/docs/screenshots/reads_promo.png" alt="Primal Reads Screenshot" style="max-width: 100px; height: auto;">
</div>

<p align="right">(<a href="#readme-top">back to top</a>)</p>



### Built With

[![Kotlin][Kotlin]][Kotlin-url]

[![AndroidStudio][AndroidStudio]][AndroidStudio-url]

<p align="right">(<a href="#readme-top">back to top</a>)</p>



<!-- GETTING STARTED -->
## Getting Started

### Prerequisites

This project requires the following pre-requisites:
- Java 17;
- Android SDK;
- Android Studio (Hedgehog 2023.1.1 and AGP 8.2);
- Android 8.0+ device or emulator;

## Building
### Debug
To build debug builds no extra configuration required, just execute `./gradlew assembleDebug` or run in Android Studio.

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
To install debug build execute `./gradlew installDebug` or run `debug` build variant in AndroidStudio.
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

Alex - [@alex](https://primal.net/profile/npub1ky9s6hjl46wxcj9gcalhuk4ag2nea9yqufdyp9q9r496fns5g44sw0alex)

Project Link: [https://github.com/PrimalHQ/primal-android-app](https://github.com/PrimalHQ/primal-android-app)

<p align="right">(<a href="#readme-top">back to top</a>)</p>



<!-- ACKNOWLEDGMENTS -->
## Acknowledgments

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
[license-url]: https://github.com/PrimalHQ/primal-android-app/blob/master/LICENSE.txt
[Kotlin]: https://img.shields.io/badge/kotlin-000000?style=for-the-badge&logo=kotlin&logoColor=white
[Kotlin-url]: https://kotlinlang.org
[AndroidStudio]: https://img.shields.io/badge/androidstudio-000000?style=for-the-badge&logo=androidstudio&logoColor=white
[AndroidStudio-url]: https://developer.android.com/studio
