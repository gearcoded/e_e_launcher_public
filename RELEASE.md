# Preparing the first public e_e launcher release

This checklist follows the **0.50.0 / versionCode 184** first-public-release
candidate, based on the tested **0.48.10** mainline. The version was assigned
for release preparation, but **publication is not yet approved**. The exact
0.50.0 signed APK must pass the remaining release gates.
The repeated-orientation ANR issue was tested successfully on **Motorola Edge 60
(102 changes)** and **Tecno Pova 6 Pro (154 changes)**. Bottom-shelf rendering
was confirmed on device in 0.48.9. JSON export/import rejection, large JPEG backgrounds, and normal widget
interaction have since passed targeted release QA. Note-widget ID rebinding,
OEM backup policies and injected write-failure rollback were not separately
tested. A repeatable but non-crashing Tecno app-drawer background gap
after multiple cyclic rotations is recorded as deferred visual QA.

**The 0.50.0 candidate is not approved for publication yet.** The maintainer has since verified and installed the official
Gradle 8.13 Wrapper JAR in the **local working tree**, and the source integrity
script now reports `SOURCE_RELEASE_CHECKS_OK`. Earlier developer ZIPs still
contain the mismatched JAR; they must not overwrite the verified local copy.

## 1. Review the repository before pushing

- The GitHub source tree should include `LICENSE`, `PRIVACY.md`,
  `ASSET_PROVENANCE.md`, `THIRD_PARTY_LICENSES.md`, the `licenses/` directory,
  Gradle wrapper and all Android source/resources.
- Developer delivery ZIPs intentionally omit the unchanged, large
  `docs/media/` directory. **The public Git repository retains those media files**;
  do not delete them when applying a ZIP update. Video and preview links in
  `README.md` are meant to resolve in GitHub, not in the compact ZIP.
- The public source distribution intentionally excludes developer `.idea/`
  settings and the maintainer's personal `commit_changes` helper. Neither is
  needed to build the app. Keep personal Git shortcuts outside the repository.
- Make sure private signing keys, passwords, `local.properties`, `.env` files,
  generated APKs, logs, and device diagnostics are **not** staged in Git.
  `.gitignore` reduces accidents, but files force-added with `git add -f` can
  still be committed. Check `git status` and `git diff --cached --name-only`.
- Use a private backup of the signing key. Losing the key prevents replacing an
  existing installed package with a future update using the same application ID.

### Mandatory Gradle Wrapper verification before publication

The `gradle/wrapper/gradle-wrapper.jar` contained in the **previously
delivered** developer ZIP has a SHA-256 that does **not** match the official
Gradle 8.13 reference. **Do not use that old JAR to build a published artifact.**
The local working-tree copy has been corrected and verified by the maintainer. Gradle publishes a
`gradle-8.13-wrapper.jar.sha256` file at its distribution server, but **does not
provide the standalone JAR at the corresponding URL** (that URL returns 404).

Use the project's verification/replacement script instead, from the project root:

```bash
bash tools/verify_replace_gradle_wrapper_8_13.sh .
```

This script retrieves `gradle/wrapper/gradle-wrapper.jar` from the official
`gradle/gradle` GitHub repository at **tag `v8.13.0`**, verifies it against the
published Gradle 8.13 SHA-256
`81a82aaea5abcc8ff68b3dfcb58b3c3c429378efd98e7433460610fecd7ae45f`,
and only then installs it. If download or verification fails, it leaves the
existing JAR untouched and exits with an error. You may instead generate the
wrapper using an independently trusted local installation of Gradle 8.13
(`gradle wrapper --gradle-version 8.13`) and verify the same checksum. Neither
method requires upgrading the project's `gradle-8.13-bin.zip` distribution.

Source ZIPs should retain executable permission on `gradlew` (Git index mode
`100755`). The maintainer's **local** integrity check has already passed after
replacing the JAR; verify that any newly packaged full-source archive and public
Git checkout also contain the corrected JAR. The older ZIP cannot be used for
a public release build without replacement.

Official references: https://gradle.org/release-checksums/ and
https://docs.gradle.org/current/userguide/gradle_wrapper.html.

For a new public checkout, retain the Git-tracked `docs/media/` directory
alongside source files. **Do not reconstruct the public repository solely from
a compact developer ZIP**, which intentionally excludes its unchanged videos.
Run source checks after replacing the wrapper and reviewing repository changes:

```bash
python3 tools/check_localization.py
python3 tools/check_release_integrity.py
python3 tools/check_reset_defaults.py

git init -b main
git add .
git diff --cached --name-only
git status --short
# Review the staged files before committing or pushing.
```

Once the files have been reviewed, commit and connect the intended GitHub remote.
Do not execute unreviewed automatic `git push` scripts.

## 2. Build the actual release APK locally

Requirements: Android SDK platform 35/build-tools, a compatible installed JDK,
Android Gradle Plugin <= 8.13.1, and the project's existing signing key.
The source uses AGP 8.13.0 and JVM target 17.

The existing Gradle file reads `EE_KEYSTORE_FILE` from a Gradle property and
`EE_KEYSTORE_PASSWORD` and `EE_KEY_PASSWORD` from Gradle properties or
environment variables. Preserve the maintainer's existing **local password
retrieval and keystore path configuration**. Do not put passwords in the
repository, scripts or screenshots.

```bash
./gradlew :app:clean :app:assembleRelease
```

If release credentials are unavailable, Gradle is intentionally allowed to
produce an *unsigned* APK. **Do not publish that file.** Do not use Android
Studio's Run/Apply Changes action or a debug/test-only artifact as the public APK.

Expected signed output is usually `app/build/outputs/apk/release/app-release.apk`.
Check the actual filename under that directory.

## 3. Inspect signature, manifest and version

The included verifier requires Android SDK `aapt` and `apksigner`. It rejects
missing/invalid signatures, a debug or test-only APK, and package/version values
that do not match `app/build.gradle.kts`. It also prints the signing certificate
fingerprint. Supply the **known SHA-256 fingerprint of the existing signing
certificate** to protect against accidentally signing with a different key:

```bash
python3 tools/verify_release_apk.py \
  app/build/outputs/apk/release/app-release.apk \
  --expected-cert-sha256 726c7bdfc833bc31e877991be5a054087588c3bfe067cbe22218cf49cd21a33f
```

Without `--expected-cert-sha256` the script still checks the APK signature and
manifest but warns that signing *identity* was not independently verified.

For comparison, the Android SDK tools also support:

```bash
apksigner verify --verbose --print-certs app/build/outputs/apk/release/app-release.apk
aapt dump badging app/build/outputs/apk/release/app-release.apk
```

Verify APK does not contain `android:testOnly=true` or `android:debuggable=true`.

## 4. Verify update installation without losing settings

Confirm the intended device serial first, since multiple phones may be
connected at once:

```bash
adb devices
adb -s DEVICE_SERIAL install -r app/build/outputs/apk/release/app-release.apk
```

**Do not use `-t`** for the public release. `-r` updates the existing installed
application without clearing launcher data. Check the main home screen,
widget-resize controls, dock locks, and settings persistence with this **exact
APK file**. If Android reports `INSTALL_FAILED_UPDATE_INCOMPATIBLE`, stop and
check the signing certificate. **Do not uninstall or wipe app data** to bypass a
signature mismatch. The special accessibility service may require re-enabling
on some OEM firmware after an APK replacement.

The 0.48.10 rotation fix passed stress testing on Motorola Edge 60 and Tecno
Pova 6 Pro without new ANRs or crashes during the recorded tests. The 0.48.10 tests have already covered normal JSON restore, rejection of
foreign and malformed JSON without losing settings, and 8000×8000 JPEG
background selection and persistence. The more unusual cases — built-in note
text/localization after widget-ID rebind, injected write failures during
restore, and OEM cloud-backup / D2D transport — remain untested. On Tecno, a visual background gap in the app drawer can
appear after several left-landscape → portrait → right-landscape cycles;
track this as deferred visual QA, not a reoccurrence of the rotation ANR.
After choosing 0.50.0, verify the **exact** signed APK to be uploaded, including
update installation without data loss. System backup/device transfer behavior
may differ across OEMs.

## 5. Publish GitHub Release

After all prior checks pass:

1. Review the source tree, license notices, dependency graph and permissions one
   final time. Preserve upstream Apache-2.0 attributions for Material-derived
   icons and dependencies.
2. Confirm `versionName = "0.50.0"` and `versionCode = 184` in the exact
   tested sources, then create the annotated tag `v0.50.0` **only after** final QA.
3. Create a GitHub Release for that tag and attach the **verified signed 0.50.0 APK**.
   Provide `e_e_launcher_0.50.0_licenses_and_attributions.zip` alongside the APK,
   and clearly link to the corresponding source tag/archive.
   GitHub supplies separate source archives from the tag.
4. Add release notes: Android 14+ (`minSdk 34`); package ID
   `org.collegiumdesideriorum.e_e_launcher`; tested Motorola Edge 60 and
   Tecno Pova 6 Pro (Android 14);
   optional Accessibility Service and Usage Access; the system backup/D2D policy;
   links to `PRIVACY.md` and license documentation.
5. Save the APK SHA-256 and signing certificate SHA-256 in release records. Use the relevant version section of [`CHANGELOG.md`](CHANGELOG.md) as the basis for the GitHub Release description; do not create separate release-notes files in the source tree.

The release license bundle includes `LICENSE`, `THIRD_PARTY_LICENSES.md`,
`ASSET_PROVENANCE.md`, `licenses/Apache-2.0.txt`,
`licenses/BSD-3-Clause-ThreeTenBP.txt`, `licenses/Boost-1.0.txt`
and `licenses/CC-BY-SA-4.0.txt`. It is
not a substitute for the complete corresponding sources. From the project root, collect runtime license evidence with:

```bash
python3 tools/collect_runtime_license_evidence.py \
  --deps release_dependencies.txt \
  --out runtime_license_evidence_0.50.0.zip
```

The 2026-11-10 local Gradle-cache license/NOTICE scan has been reviewed:
83 dependency coordinates, 130 JAR/AAR archives, 69 legal entries and **no
separate `NOTICE` files** extracted. A GPL v2/ClassPath Exception text was
found only inside a JSpecify Javadoc archive, not the runtime JAR;
JSpecify itself is Apache-2.0. The scan is evidence-limited, not a complete
certification of external repository notices. See
`docs/release-license-review-0.50.0.md`.

The remaining priority is the **Git history and index privacy audit**, then
final signed 0.50.0 APK SHA-256/inventory checks. Do not include private
`internal/` material in the public release tree if it is intended to remain
private; merely adding it to `.gitignore` would not erase Git history.

This document targets GitHub Releases only. No Google Play or IzzyOnDroid
publication is planned.
