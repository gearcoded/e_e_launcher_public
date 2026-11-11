# e_e launcher releases

This document records the first public release and provides a checklist for
future releases. The public source repository is
[`gearcoded/e_e_launcher_public`](https://github.com/gearcoded/e_e_launcher_public).
The default branch is `master`. The public repository has its own clean Git
history, separate from the maintainer's private development repository.

## Published release: 0.50.0

**e_e launcher 0.50.0 (versionCode 184) is the first public stable release.**
The version was finalized on 2026-11-10 and published on GitHub on 2026-11-11.
It is based on the tested 0.48.10 mainline.

- **Package:** `org.collegiumdesideriorum.e_e_launcher`
- **Minimum Android version:** Android 14 (API 34)
- **Git tag:** [`v0.50.0`](https://github.com/gearcoded/e_e_launcher_public/releases/tag/v0.50.0)
- **Tagged source commit:** `7034382` (`Initial public release 0.50.0`)
- **Release APK:** `e_e_launcher-v0.50.0.apk`
- **License archive:** `e_e_launcher-v0.50.0-licenses.zip`
- **Checksums:** `SHA256SUMS.txt`, attached to the GitHub Release

The official release APK is signed with the maintainer's release key. The APK
signature, package name, version information, and production build flags were
verified with `tools/verify_release_apk.py`:

- Package: `org.collegiumdesideriorum.e_e_launcher`
- `versionName`: `0.50.0`
- `versionCode`: `184`
- Signature verification: passed
- `android:debuggable` and `android:testOnly`: disabled

### Published artifact checksums

SHA-256 of `e_e_launcher-v0.50.0.apk`:

```text
38488e0541bf1d9cc9d357da5c34b7948d3434bdeaf8246531e0d0670de330d8
```

SHA-256 of `e_e_launcher-v0.50.0-licenses.zip`:

```text
9aaeb428fdfed4cb3f3c774c272d4370276e9db3184a299b44e6d6e8f33647f7
```

SHA-256 of the **signing certificate** (not of the APK file):

```text
726c7bdfc833bc31e877991be5a054087588c3bfe067cbe22218cf49cd21a33f
```

These checksums describe the exact published files. Rebuilding or resigning an
APK can change its file checksum even when the source code and signing key are
the same. The GitHub-generated `Source code (zip)` and `Source code (tar.gz)`
archives correspond to the tagged source commit; they are not signed APKs.

### Validation and device testing

- The 0.48.10 baseline passed repeated-orientation stress tests on Motorola
  Edge 60 (**102 orientation changes**) and Tecno Pova 6 Pro (**154 changes**),
  with no new launcher ANRs during the recorded sessions. These counts refer to
  **0.48.10**, not to a separate 0.50.0 stress-test run.
- The baseline was also tested for JSON backup/restore and invalid-import
  handling, large 8000 x 8000 JPEG backgrounds, normal widget operation and
  resizing, and the app-drawer image-picker layout.
- The signed 0.50.0 release was installed and smoke-tested on Motorola Edge 60.
- On Tecno Pova 6 Pro, **0.48.10 -> 0.50.0** was installed through Obtainium
  using the project's GitHub link. Existing launcher settings and workspace
  layouts were preserved. Obtainium correctly detected the installed and
  available 0.50.0 versions.

**Known deferred issue:** On Tecno Pova 6 Pro, repeated cyclic rotations through
left landscape, portrait, and right landscape can produce a rare visual gap in
the app-drawer background. This is a visual defect, not the previously fixed
rotation ANR.

**Testing limits:** Note-widget ID rebinding, injected write-failure rollback,
and every OEM-specific cloud-backup/device-to-device transfer scenario were not
separately verified. Android backup behavior can vary by device and firmware.
See [`TESTED_DEVICES.md`](TESTED_DEVICES.md) and
[`CHANGELOG.md`](CHANGELOG.md) for more detail.

## Checklist for future releases

The following steps apply to **new versions**. They are not outstanding tasks
for 0.50.0. Do not retag, rebuild, or replace the published 0.50.0 artifacts
merely to update documentation on `master`.

### 1. Prepare and review public sources

- Complete implementation and testing in the private development project.
- Transfer only intended, reviewed changes into the separate public repository.
  Preserve its existing `.git` directory, `master` branch, and independent
  history; **do not run `git init` again** or import the private repository's
  old commits.
- Include Android sources and resources, the Gradle wrapper, `docs/media/`,
  `LICENSE`, `PRIVACY.md`, `ASSET_PROVENANCE.md`, `THIRD_PARTY_LICENSES.md`, and
  the `licenses/` directory as applicable.
- Exclude signing keys, passwords, `local.properties`, `.env` files, APK build
  outputs, caches, device logs, diagnostics, and internal developer material.
  `.gitignore` is not sufficient protection against files added with `git add -f`.
- Review `git status`, `git diff`, and staged filenames before committing.
  Keep `CHANGELOG.md` as the single source-tree changelog; do not introduce
  `RELEASE_NOTES_*` files.
- Update version information, documentation, known issues, and the tested-device
  matrix for the new release.

### 2. Verify the Gradle wrapper and source integrity

The **published 0.50.0 source** contains the verified official Gradle 8.13
wrapper JAR. Its SHA-256 is:

```text
81a82aaea5abcc8ff68b3dfcb58b3c3c429378efd98e7433460610fecd7ae45f
```

For the same Gradle wrapper version, verify it with:

```bash
sha256sum gradle/wrapper/gradle-wrapper.jar
```

If the project changes Gradle versions, verify the new wrapper against the
corresponding official release checksum instead of reusing this value.
Never copy an unverified wrapper JAR from an older developer archive over the
published one. The `gradlew` script should retain its executable Git mode
(`100755`). Official references:
[Gradle release checksums](https://gradle.org/release-checksums/) and
[Gradle Wrapper documentation](https://docs.gradle.org/current/userguide/gradle_wrapper.html).

Run the public source checks from the project root:

```bash
python3 tools/check_localization.py
python3 tools/check_release_integrity.py
python3 tools/check_reset_defaults.py
./gradlew :app:assembleDebug :app:assembleRelease
```

A build of the **public checkout** may produce an **unsigned** release APK if
private signing credentials are not configured. This is acceptable for source
build validation, but that unsigned APK must **not** be uploaded as an official
release. The 0.50.0 public source checks and independent debug/release Gradle
build completed successfully before initial publication.

### 3. Produce and verify the official signed APK

Build the release from the corresponding reviewed sources with the maintainer's
existing, privately stored release-signing configuration. The project uses
`EE_KEYSTORE_FILE`, `EE_KEYSTORE_PASSWORD`, and `EE_KEY_PASSWORD` through Gradle
properties and/or environment variables. Do not commit or publish these values
or the private keystore.

```bash
./gradlew :app:clean :app:assembleRelease
```

Check the actual output path before verification. For example, if the signed
APK is at `app/build/outputs/apk/release/app-release.apk`:

```bash
APK_PATH="app/build/outputs/apk/release/app-release.apk"
python3 tools/verify_release_apk.py "$APK_PATH" \
  --expected-cert-sha256 726c7bdfc833bc31e877991be5a054087588c3bfe067cbe22218cf49cd21a33f
sha256sum "$APK_PATH"
```

The verifier requires Android SDK `aapt` and `apksigner` and checks the
signature, package, version, and production flags. The certificate fingerprint
above is the identity used for 0.50.0; preserve it for subsequent releases so
that users can update without uninstalling the app. Do not publish a debug,
`testOnly`, or unsigned APK.

### 4. Test installation and updates

Test the **same signed APK** intended for publication, including installation
over an existing version without clearing launcher data. With a known device
serial:

```bash
adb devices
adb -s DEVICE_SERIAL install -r "$APK_PATH"
```

Here `APK_PATH` must point to the actual tested file and `DEVICE_SERIAL` must
be replaced with the chosen device's serial number. **Never uninstall the app
just to bypass a signature mismatch**; investigate the certificate instead.
Do not use `adb install -t` for the public release.

Check the desktop, folders and docks, widget interaction and resizing,
settings persistence, backup/restore, rotation behavior, and any new features.
An optional accessibility service may require re-enabling after updates on some
OEM firmware. Test updates through Obtainium when practical.

### 5. Tag the reviewed public commit and publish

- Verify that the public `master` commit contains the exact source version
  corresponding to the tested APK. Confirm a clean intended working tree and
  review staged changes (`git diff --cached --check`).
- Create a **new annotated version tag** on the correct public commit, then push
  `master` and the tag to `origin`. Do not move existing published tags.
- Create a GitHub Release for the new tag and attach the signed APK, an updated
  license/attribution ZIP, and `SHA256SUMS.txt` containing the checksums of the
  exact uploaded APK and license ZIP.
- Include release notes, minimum supported Android version, known limitations,
  source and license links, and tested devices. GitHub automatically provides
  source ZIP and tar.gz archives for the tag.
- Verify the uploaded assets' SHA-256 values against the local files. Finally,
  check the Release page and downloads without a GitHub login.
- Keep the published APK signing certificate consistent across GitHub, Obtainium,
  and any third-party store distributing the same package ID. An APK signed by a
  different certificate cannot update an existing installation in place.

## Licenses and distribution

The project's own source code and functional resources are distributed under
`GPL-3.0-or-later`, except where otherwise specified. Applicable third-party
licenses and attributions are documented in [`LICENSE`](LICENSE),
[`THIRD_PARTY_LICENSES.md`](THIRD_PARTY_LICENSES.md),
[`ASSET_PROVENANCE.md`](ASSET_PROVENANCE.md), and [`licenses/`](licenses/).
The published 0.50.0 license bundle accompanies the APK; it does not replace
the corresponding complete source code.

The original local dependency/license review was completed before publication.
Its internal evidence-generation scripts and reports are **not** part of the
public source tree; do not refer public readers to commands or files that are
not included in this repository. Recheck license and notice requirements when
adding or upgrading dependencies or third-party assets.

GitHub Releases is the primary distribution source. Obtainium can track the
GitHub releases and install updates from them; an installation/update from
0.48.10 to 0.50.0 was tested on Tecno Pova 6 Pro. Other app stores may have
separate submission, build, or signing requirements.
