# Building e_e launcher from source

The first public stable release is **0.50.0 (versionCode 184)**, based on the
device-tested 0.48.10 mainline. The 0.48.10 rotation fix passed stress tests
on Motorola Edge 60 (102 orientation changes) and Tecno Pova 6 Pro (154
changes); these counts do not represent a separate 0.50.0 rotation stress test.
The official 0.50.0 APK was signed and signature-verified, smoke-tested on
Motorola Edge 60, and installed as an update from 0.48.10 on Tecno Pova 6 Pro
through Obtainium with settings and workspace layouts preserved.

## Toolchain and target

- Android namespace/application ID: `org.collegiumdesideriorum.e_e_launcher`
- Minimum Android API: **34** (Android 14)
- Compile SDK: **35**
- Target SDK: **35**
- Kotlin/Java JVM target: **17**
- Android Gradle Plugin: **8.13.0** in the current project configuration
- No project-owned NDK code; AndroidX/Compose dependencies may contain
  transitive native libraries

The public Git repository includes the verified official Gradle 8.13 Wrapper
JAR. Its SHA-256 is:

```text
81a82aaea5abcc8ff68b3dfcb58b3c3c429378efd98e7433460610fecd7ae45f
```

To check the wrapper included in a public source checkout, run:

```bash
sha256sum gradle/wrapper/gradle-wrapper.jar
```

A fresh clone of the public repository does **not** require a wrapper
replacement. Some older, privately distributed developer ZIPs contained an
incorrect wrapper JAR; do not use them in place of the public source tree.
See [`RELEASE.md`](../RELEASE.md) for the release checklist and wrapper
verification guidance.

## Release builds and signing

The Gradle configuration can read the local values `EE_KEYSTORE_FILE`,
`EE_KEYSTORE_PASSWORD`, and `EE_KEY_PASSWORD`. Signing material must remain
outside the publicly tracked repository. Debug and release variants use the
configured key when those values are available; otherwise debug uses the
regular Android debug key and a release build may be **unsigned**.

To build a release candidate:

```bash
./gradlew :app:clean :app:assembleRelease
```

Only a **signed, non-debuggable, non-testOnly** APK with the intended package,
version, and certificate should be distributed as an official release.
The publication procedure and validation commands are in [`RELEASE.md`](../RELEASE.md).

## Static checks

The following checks can run without a full Android SDK build:

```bash
python3 tools/check_localization.py
python3 tools/check_release_integrity.py
python3 tools/check_reset_defaults.py
```

Release UI languages are English, Russian, German, French, Spanish, Brazilian
Portuguese, Japanese, and Simplified Chinese. Debug builds also expose
Android pseudo-locales `en-XA` and `ar-XB` to help inspect longer strings and
right-to-left layout behavior.

See [`CHANGELOG.md`](../CHANGELOG.md) for version history and release notes.

## Source archives and demonstration media

The public Git repository and GitHub's source archives generated from release
tags include `docs/media/`, with the project's demonstration videos and poster
images. Compact archives exchanged during private development may omit these
media files; use the public Git checkout for a complete published source tree.
