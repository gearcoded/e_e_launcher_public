# Third-party software and assets

This file records third-party components intentionally used by **e_e launcher**.
Unless a section below says otherwise, the project's own source code and functional
resources are licensed under `GPL-3.0-or-later`; a third-party component keeps its
own upstream license.

## Apache License 2.0 software

The following component families are primarily distributed under Apache License
2.0. A copy of that license is included at `licenses/Apache-2.0.txt`.
Some bundled Kotlin standard-library code is covered by BSD-3-Clause and
Boost-1.0; its upstream license records also credit code derived from GWT and
Guava under Apache-2.0. See the notices immediately below.

| Component | Version / source | Use in e_e launcher |
| --- | --- | --- |
| Android Gradle Plugin | 8.13.0 | Android build plugin |
| Kotlin Android Gradle Plugin | 2.3.21 | Kotlin Android build plugin |
| Kotlin Compose Compiler plugin | 2.3.21 | Compose compiler integration |
| Kotlin standard library | supplied by Kotlin 2.3.21 | Kotlin runtime support |
| AndroidX Core KTX | 1.15.0 | Android framework extensions |
| AndroidX Activity Compose | 1.10.1 | Compose activity integration |
| kotlinx-coroutines-android | 1.10.2 | Coroutine support |
| Jetpack Compose BOM | 2025.06.01 | Compose version alignment |
| Jetpack Compose UI / UI Graphics / Foundation / Material 3 / tooling modules | versions selected by the BOM | User interface and debug tooling |
| Gradle Wrapper | Gradle 8.13; official wrapper JAR SHA-256 verified | Build bootstrap; the wrapper JAR included in this repository was verified against the official Gradle 8.13 distribution. |
| JetBrains Java Annotations (`org.jetbrains:annotations`) | 23.0.0 (transitive) | Java annotation metadata |
| JSpecify (`org.jspecify:jspecify`) | 1.0.0 (transitive) | Java nullability annotations |
| Guava ListenableFuture (`com.google.guava:listenablefuture`) | 1.0 (transitive) | Small standalone ListenableFuture API artifact (not full Guava) |

## Kotlin standard-library third-party code (BSD 3-Clause)

Kotlin **2.3.21** identifies a ThreeTenBP-derived section of
`libraries/stdlib/src/kotlin/time` as third-party code licensed under a
**3-clause BSD license**, with copyright attributed to **Stephen Colebourne and
Michael Nascimento Santos (2007–present)**. This applies to the relevant
third-party-derived material, not to the whole standard library.

The full upstream notice is reproduced in
`licenses/BSD-3-Clause-ThreeTenBP.txt` and must accompany any binary
redistribution that contains the relevant code. Upstream source documentation:
https://github.com/JetBrains/kotlin/blob/v2.3.21/license/README.md and
https://github.com/JetBrains/kotlin/blob/v2.3.21/license/third_party/threetenbp_license.txt.

## Kotlin standard-library third-party code (Boost Software License 1.0)

For the pinned Kotlin version **2.3.21**, JetBrains' third-party inventory also
identifies `libraries/stdlib/jvm/src/kotlin/util/MathJVM.kt` as containing code
derived from Boost special math functions under **Boost Software License 1.0**.
The upstream credit is **Eric Ford and Hubert Holin (2001)**. The legal text is
included at `licenses/Boost-1.0.txt` and is reproduced from:
https://github.com/JetBrains/kotlin/blob/v2.3.21/license/third_party/boost_LICENSE.txt.

This attribution concerns JVM standard-library source and is distinct from
Kotlin compiler or non-Android JavaScript/native/wasm components. Some
conditions of the Boost license have an object-code exception; we include
the license and credit in the source and release materials conservatively.

## Kotlin standard-library Apache-2.0 origin credits

JetBrains' pinned Kotlin source inventory records the following origin credits
within the JVM/common standard-library code. Their license family remains
Apache-2.0, whose text is already included at `licenses/Apache-2.0.txt`:

- `libraries/stdlib/src/kotlin/collections`: derived from Google Web Toolkit
  (GWT), **Copyright (C) 2007–2008 Google Inc.**
- `libraries/stdlib/jvm/src/kotlin/util/UnsignedJVM.kt`: derived from Guava's
  `UnsignedLongs`, **Copyright (C) 2011 The Guava Authors**.

See the pinned upstream inventory:
https://github.com/JetBrains/kotlin/blob/v2.3.21/license/README.md.
These attributions do not imply that the full GWT or Guava libraries are bundled.
The resolved `com.google.guava:listenablefuture:1.0` artifact is a separate
small dependency and is already listed in the Apache-2.0 table above.

The AndroidX, Kotlin, kotlinx.coroutines and Gradle projects contain their own
copyright notices. Redistribution must not remove notices that are supplied by
those projects.

## Google Material Icons and related adapted pictograms

Some vector drawables in `app/src/main/res/drawable/` are Google Material Icons,
modified versions of these icons, or familiar Material-style pictograms.
The project's **conservative licensing policy** is to preserve the upstream
Apache-2.0 attribution for the potentially Material-derived items named below,
not to relicense their icon shapes as GPL or CC BY-SA.

| Resource file (under `app/src/main/res/drawable/`) | Origin / treatment |
| --- | --- |
| `ic_edit.xml` | Material edit glyph; recolored for the app |
| `ic_folder.xml` | Material folder glyph; recolored for the app |
| `ic_phone.xml` | Material phone glyph; recolored for the app |
| `ic_settings.xml` | Material settings glyph; recolored for the app |
| `ic_sort.xml` | Material sort glyph; recolored for the app |
| `ic_close.xml` | Material close-style glyph; path adapted |
| `ic_delete.xml` | Material delete-style glyph; simplified path |
| `ic_drag.xml` | Material drag-style glyph; adapted grid |
| `ic_info.xml` | Material information-style glyph; adapted path |
| `ic_map.xml` | Material map-style glyph; adapted path |
| `ic_message.xml` | Material message-style glyph; adapted path |
| `ic_search.xml` | Material search-style glyph; simplified path |
| `ic_undo_rounded.xml` | Material undo-style glyph; adapted path |
| `ic_apps.xml` | Material apps-style grid; simplified geometry |

The exact provenance of each adapted/simplified shape cannot be established
from its path geometry alone. Such items are conservatively treated as
potentially Material-derived pending original source records.

Source project: https://github.com/google/material-design-icons
License: Apache License, Version 2.0 (`licenses/Apache-2.0.txt`).
See `ASSET_PROVENANCE.md` for the remaining project-specific drawable files
and project-created launcher artwork.

## Transitive dependencies and release graph

**Validated against 0.48.10 on 2026-11-09.** The maintainer ran
`./gradlew :app:dependencies --configuration releaseRuntimeClasspath` after
installing and verifying the official Gradle 8.13 Wrapper. The dependency
resolution task completed successfully. The graph contains the expected direct
libraries and transitive families, with the following selected versions:

| Family | Selected versions in the release graph |
| --- | --- |
| Kotlin standard library | 2.3.21 |
| Kotlin coroutines | 1.10.2 |
| AndroidX Core / Core KTX | 1.15.0 |
| AndroidX Activity / Activity Compose | 1.10.1 |
| AndroidX Lifecycle | 2.8.7 |
| AndroidX Collection | 1.5.0 |
| AndroidX Compose UI, Foundation, Runtime, Animation | 1.8.3 |
| AndroidX Compose Material 3 | 1.3.2 |
| AndroidX Compose material-icons-core | 1.7.8 |
| AndroidX graphics-path | 1.0.1 |
| AndroidX emoji2 | 1.4.0 |
| JetBrains Java annotations | 23.0.0 |
| JSpecify | 1.0.0 |
| Guava ListenableFuture only | 1.0 |

The Compose BOM (`2025.06.01`) and Kotlin coroutines BOM are dependency
**constraints**, not additional executable library binaries. Android Gradle and
Kotlin build plugins are build-time tools, not application runtime libraries.
The graph also contains Android/KMP variant coordinates; the line count must
not be interpreted as the number of JARs packaged in the release APK.

**License assessment:** identified runtime library families are accounted
for in the Apache-2.0 attributions, Kotlin BSD-3-Clause notice and Boost-1.0 notice above. In particular, the Maven
metadata for JSpecify 1.0.0, Guava ListenableFuture 1.0 and JetBrains Java
annotations identifies Apache-2.0. AndroidX publishes Apache-2.0 in its Maven
metadata; Kotlin is also Apache-2.0. There is no previously unlisted direct library family or identified
license conflict in this resolved graph. This is not a complete file-by-file
review of third-party notices in every binary dependency.

**Offline artifact-level license/NOTICE scan completed (2026-11-10):** the
maintainer scanned 83 dependency coordinates and 130 locally cached JAR/AAR
archives, obtaining 69 legal-text files (68 copies of Apache-2.0 and one GPL v2
with Classpath Exception in the JSpecify **Javadoc**, not its runtime JAR).
**No separate `NOTICE` file was found in the scanned archives.** JSpecify
1.0.0 itself remains Apache-2.0 under its Maven Central POM; the Javadoc
legal text does not make the Android runtime GPL v2 licensed. Twenty-seven
missing Gradle cache coordinates were principally BOMs, metadata aliases or
multiplatform variant selectors, not 27 independently confirmed missing
runtime binaries. Local Maven POM files were not cached, so the scan did not
provide POM declarations; version-specific Maven metadata was checked
separately for JSpecify. The scan does **not** prove that all upstream source
repositories and transformed native payloads are NOTICE-free, or reveal all
classes in the final signed APK. No additional license or NOTICE document is
indicated by this local evidence. The local audit workpapers are not included
in the public source repository. The conclusions above are limited to the
artifacts actually inspected.

Source references: https://github.com/JetBrains/java-annotations,
https://github.com/jspecify/jspecify,
https://github.com/google/guava.

## Components not bundled with the application

Installed Android icon packs may be read at runtime through their public Android
resources. Their APKs, artwork and source code are not included in e_e launcher and
remain subject to the icon-pack authors' own licenses.

## NOTICE scope and source-only dependencies

Apache License 2.0 section 4(d) requires carrying forward the applicable
attribution text of an **upstream NOTICE file if that work includes one**.
This conditional requirement is not a requirement to invent a separate NOTICE
for every Apache-licensed dependency. A version-specific scan of locally
available JAR/AAR artifacts was completed on 2026-11-10; no separate NOTICE
file was found in the scanned archives. The one-off audit script and working
reports are not part of this public repository. This is **not** an
upstream-source-wide NOTICE certification, nor does it establish exhaustive
inspection of the final APK.

Gradle and compiler plugin licenses are relevant to the **source/build toolchain**;
the plugins are not automatically bundled into the APK. Likewise JavaScript,
Wasm, compiler-only and Kotlin test-data notices in upstream source inventories
must not be presented as mandatory notices for this Android/JVM application
unless the corresponding material actually ships. The additional Boost-1.0,
BSD-3-Clause, Guava-origin and GWT-origin records above were identified in
Kotlin's common/JVM standard-library source.
