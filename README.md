# Primal Android client
Repository of the Primal Android client for Nostr.

## Requirements
This project requires the following pre-requisites:
- Java 17;
- Android SDK;
- Android Studio (Giraffe 2022.3.1 and AGP 8.1);
- Android 8.0+ device or emulator;

## Building
### Debug
To build debug builds no extra configuration required, just execute `./gradlew assembleDebug` or run in Android Studio.

### Release
To build release builds you will need to create `config.properties` file in the project root directory.
Following properties in `config.properties` are **MANDATORY** for any release build:
```properties
# Encryption
localStorage.keyAlias={KeystoreAliasForEncryption}
```
If you want to build the release build with your own certificate you can use `releasePlayStore` build variant
which will read the certificate details from following properties in `config.properties`:
```properties
# Google Play Upload Certificate
playStore.storeFile={PathToYourCertificate}
playStore.storePassword={CertificatePassword}
playStore.keyAlias={YourAlias}
playStore.keyPassword={AliasPassword}
```

## Installing
Attach your device to the computer or start your emulator and install debug or release build as described below.

### Debug builds
To install debug build execute `./gradlew installDebug` or run `debug` build variant in AndroidStudio.
Please note that debug builds do not use encryption when storing sensitive information and that the performance is
significantly slower compared to release builds.

### Release builds
To install release build execute `./gradlew installReleasePlayStore` or run `releasePlayStore` build variant in AndroidStudio.
Please note that `releasePlayStore` build variant requires `config.properties` configured with mandatory properties.
If no certificate properties are provided it fallbacks to `debug` certificate.

## Development
The app is still under the development and changes are frequent. Breaking changes can and will happen in the future.
