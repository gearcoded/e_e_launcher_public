# Building e_e launcher from source

The current mainline first-public-release candidate is **0.50.0
(versionCode 184)**, based on the device-tested 0.48.10 code. The orientation
fix passed stress tests on Motorola Edge 60 (102 orientation changes) and Tecno
Pova 6 Pro (154 changes). The exact 0.50.0 signed APK still requires local
build, signature verification and on-device update QA.

## Toolchain and target

- Android namespace/application ID: `org.collegiumdesideriorum.e_e_launcher`
- Minimum Android API: **34** (Android 14)
- Compile SDK: **35**
- Target SDK: **35**
- Kotlin/Java JVM target: **17**
- Android Gradle Plugin: **8.13.0** in the current project configuration
- No project-owned NDK code; AndroidX/Compose dependencies may contain
  transitive native libraries

**For old developer ZIPs, before running the bundled wrapper**, replace the
mismatched JAR with the official Gradle 8.13 artifact and verify its checksum.
The maintainer has already done this successfully in the current local working
tree, with `SOURCE_RELEASE_CHECKS_OK` after replacement. Preserve that verified
copy when applying documentation-only updates. The previous URL
`https://services.gradle.org/distributions/gradle-8.13-wrapper.jar`
returns 404; the verified artifact is available from Gradle's `v8.13.0`
source tag on GitHub, as used by the included script:

```bash
bash tools/verify_replace_gradle_wrapper_8_13.sh .
python3 tools/check_release_integrity.py
./gradlew :app:assembleDebug
```

The script refuses to replace the JAR when its SHA-256 does not match the
official 8.13 checksum. A fresh clone from a Git repository with an already
verified wrapper needs no replacement.

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

## Compact developer source ZIPs

`docs/media/` is preserved in the Git repository but deliberately left out of
transferred development ZIPs. Do not remove Git-tracked media when applying a
ZIP update, and do not interpret README media links as missing repository files.
