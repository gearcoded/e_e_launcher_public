# Changelog

## 0.50.0 (versionCode 184) — 2026-11-10 (first public stable release)

- **First public release:** published on GitHub on 2026-11-11 from the tested 0.48.10 mainline, with release versionName `0.50.0` and versionCode `184`. The version bump did not change launcher runtime logic, effects, backup serialization, widget implementations, or Android manifest behavior.
- **Validation of the 0.48.10 baseline:** completed source checks, debug/release compilation, signed APK verification and same-version reinstall, release smoke tests, JSON backup/error handling, 8000×8000 JPEG background tests, widget functional tests (6/6), and rotation stress sessions on Motorola Edge 60 (102 changes) and Tecno Pova 6 Pro (154 changes). The app-drawer picture-selection button layout was confirmed on device after its fix. The rotation counts are for 0.48.10, not a separate 0.50.0 stress test.
- **Verification of the 0.50.0 release:** the official APK passed package/version, digital-signature, certificate fingerprint, non-debuggable, and non-testOnly verification. The signed release was installed and smoke-tested on Motorola Edge 60. On Tecno Pova 6 Pro, an update from 0.48.10 to 0.50.0 through Obtainium succeeded without losing existing settings or workspace layouts.
- **Licenses and source distribution:** documented the Kotlin 2.3.21 `kotlin.time` ThreeTenBP-derived material and its BSD 3-clause notice alongside other project and third-party licenses. The first public source snapshot includes the verified official Gradle 8.13 Wrapper JAR. The GitHub Release for tag `v0.50.0` includes a signed APK, the license/attribution ZIP, and `SHA256SUMS.txt`.
- **Known deferred issue:** on Tecno Pova 6 Pro, repeated cyclic screen rotations can produce a rare app-drawer background gap. This is a visual issue, distinct from the resolved rotation ANR.

## 0.48.10 (versionCode 183) — 2026-11-09 (rotation ANR candidate)

- **App-drawer background settings (2026-11-10, confirmed on device):** moved the existing Choose/Replace picture button directly below the Color / Gradient / Picture mode list when Picture is selected. The image picker action, image URI persistence, background rendering, opacity, color controls and other settings are unchanged. The maintainer confirmed on a device that the button now appears in the intended location and only in Picture mode.
- **Repeated screen rotations:** Android no longer recreates `MainActivity` when the orientation or dimensions of the normal application window change. The existing Compose screen resizes in place, and `AppWidgetHost` and widget views are not recreated on every rotation. Locale, display-density and other configuration changes not listed in `configChanges` continue to use the standard Android handling.
- **Workspace protection:** the expensive `HorizontalPager` is not composed while the available workspace area is temporarily too small during rotation. The cyclic pager's virtual page range was reduced from 2,000 to 24 cycles; the existing invisible recentering mechanism still supports continuous looping without an enormous virtual item range.
- **Current page:** `rememberSaveable` preserves the selected workspace page index if the Activity must be recreated. The default home page and Home button behavior are unchanged.
- **Diagnostics:** added `activity configuration changed` logging to confirm that a rotation does not trigger another `onCreate`.
- **Evidence:** logs recorded on November 8–9, 2026 captured at least six ANRs, two `Too many receivers, total of 1000` crashes originating in `TextClock` within widget `AndroidView`s, and extensive repeated `AppWidgetHostView` creation during rotations. The 0.48.9 bottom-dock fix had already been confirmed on device and was not modified.
- **Device QA (2026-11-09):** Motorola Edge 60 completed **102** orientation changes and Tecno Pova 6 Pro completed **154**; neither session recorded new launcher ANRs/crashes or Activity recreation, and disappearing/misplaced shortcuts and widgets did not recur. The app drawer now stays open across rotation; this behavior was approved. A separate Tecno app-drawer background gap during repeated cyclic rotations was recorded as deferred visual QA; no causal relationship to the resolved ANR has been established.
- **Release documentation (no APK version change):** refreshed the 0.48.10 status, the planned 0.50.0 release, and the policy of excluding `docs/media/` from compact developer ZIPs. Fixed the Gradle Wrapper retrieval script to use Gradle's official GitHub `v8.13.0` tag and verify its SHA-256. The maintainer subsequently confirmed that the official Gradle 8.13 Wrapper JAR was installed locally and `SOURCE_RELEASE_CHECKS_OK` passed. Previously generated source ZIPs still contain the old JAR and must not overwrite the locally verified copy.
- User data, JSON backup format, shortcut positions, dock settings, effect implementations and widget implementations were not changed.

## 0.48.9 (versionCode 182) — 2026-11-08

- **Build fix:** within the bottom-dock shadow mask (`BottomDockShelf.kt`), `drawContent()` is now invoked inside `clipPath` using the explicit receiver `this@drawWithContent`. This resolves the Kotlin `drawContent() cannot be called in this context with an implicit receiver` error introduced in 0.48.8.
- Shelf geometry and opacity, the shadow mask, scrolling and shortcut interaction remain unchanged.
- **Verification at the time of 0.48.9:** an isolated Kotlin test confirmed the explicit receiver syntax; a complete Android build and on-device visual test were still required. The Gradle Wrapper JAR mismatch was not yet resolved at that point.

## 0.48.8 (versionCode 181) — 2026-11-08

- **Bottom dock / translucent shelf:** removed the inner `graphicsLayer` shadow showing through the shelf. At 1–99% transparency it appeared as a second lighter/darker silhouette (a 'mini shelf') inside the PLATFORM shelf; at 0% the shadow was disabled, and at 100% the opaque fill covered it.
- **Rendering:** the shadow now excludes the interior of each visible shelf silhouette while preserving its outer falloff and side diffusion. The mask travels with the dock pages. The actual shelf's opacity, shape, size, position, shortcuts and drag-and-drop behavior were not changed. The correction applies to all shaped shelves, not the no-shelf mode.
- **Verification at the time of 0.48.8:** static source checks passed; an Android build and on-device confirmation, especially at medium transparency and during dock swipes, were still required. Gradle Wrapper verification was also outstanding at that point. The subsequent 0.48.9 on-device tests confirmed the corrected shelf appearance.

## 0.48.7 (versionCode 180) — 2026-11-08

- **Bottom dock / shelf:** corrected the mismatch between the PLATFORM shelf silhouette and its former rectangular shadow. Both fill and shadow now use the same PLATFORM outline; other shelf styles retain their own outlines.
- **Side shadow diffusion:** the shadow is rendered in an extended-width parent dock layer outside `HorizontalPager`'s horizontal clipping. The shelf still belongs to its respective dock page and moves with it. Shadow positioning tracks page offsets without recalculating every shortcut on each frame.
- **Compatibility:** shelf dimensions, offset, color, opacity, existing settings, dock contents and drag-and-drop paths are unchanged. The 0.48.6 backup and background-image fixes are retained.
- **Verification at the time of 0.48.7:** static source and ZIP checks passed; visual checks for bands and shadow clipping in portrait/landscape against light wallpapers were still needed. No Android build was performed in that environment.

## 0.48.6 (versionCode 179) — 2026-11-08

- **Safe JSON restore:** all primary and supplemental settings, value types and widget-restoration data are validated before any write. Invalid versions, types or structures are rejected without changing current data. On a write failure, the app attempts to roll back three SharedPreferences files. Existing valid backup files need no format changes (versions 1–3 remain readable).
- **Notes:** widget-ID remapping and rebinding preserve the `customized` flag, allowing default hints to follow the selected locale without overwriting user-entered text.
- **Memory usage:** user-selected workspace, interface and app-drawer backgrounds are decoded with a maximum long edge of 2,560 pixels; dialog backgrounds are limited to 1,200 pixels. The full-resolution source Bitmap is no longer allocated before downsampling.
- **Android backup:** added explicit Android 12+ `dataExtractionRules`. Cloud backup remains governed by the user's setting through `LauncherBackupAgent`; automatic direct device-to-device (D2D) transfer is disabled for privacy. Manual JSON export/import remains available. Existing system backups are not automatically deleted.
- **Pre-release infrastructure:** updated the validator, instructions, audit and documentation; `gradlew` was prepared to retain executable (`+x`) permissions in ZIP archives. At that time the unverified `gradle-wrapper.jar` was deliberately left in place pending the original Gradle 8.13 JAR and SHA-256 verification. The releaseRuntimeClasspath dependencies and final signed APK certificate had not yet been checked.
- **Status at the time of 0.48.6:** a candidate for targeted testing; the 0.48.5 page effects, gestures, docks and page overview were unchanged. The full Android build and new device scenarios were not yet tested.

## 0.48.5 (versionCode 178) — 2026-11-08

- **Bottom dock:** removed a duplicate horizontal line while dragging a workspace folder toward the dock. Only the standard vertical bottom-dock insertion marker remains, as with a regular shortcut. The side-dock insertion marker was not changed.
- **Workspace and app-drawer effect previews:** in landscape, preview height is limited to 28% of screen height and at most 190 dp, leaving room to browse the effect list. Demo-page content adapts to the reduced height. Portrait preview height remains 190 dp.
- All other 0.48.4 behavior remained unchanged. The technical-audit fixes were postponed until stabilization. A full Android build and device test were still required.

## 0.48.4 (177) — side-dock closed-folder swipe and open-folder drag restoration
- Restored the standard long-press drag/extraction behavior in an OPEN side-dock folder, as in 0.48.0. It uses the actual finger release position on the workspace; no special vertical swipe handling in FolderDialog.
- Implemented the missing CLOSED side-dock folder swipe gesture: vertically swiping the folder icon without opening it pulls a trail of its icons after the finger, matching the existing desktop and bottom-dock folder fan.
- Side-dock scrolling yields to the closed-folder swipe at touch slop, while a long press still drags the folder itself. Folder fan is dismissed on Home, Back, or a tap on empty space.
- All other approved features, effects and settings unchanged. Predelivery Android build and device QA remain necessary.

## 0.48.3 (versionCode 176) — 2026-11-08

- **Side dock / folders:** resolved competition between the shortcut swipe handler and vertical list scrolling. For shortcuts inside a side-dock folder, movement is detected during `PointerEventPass.Initial` before `verticalScroll` can consume the gesture; an ordinary tap is not intercepted before the movement threshold is exceeded. Finishing a vertical swipe continues to extract the shortcut to the workspace.
- **Side dock:** disabled panel scrolling while a side-dock folder is open. Scrolling resumes after the folder closes; drag behavior and gestures for other folders are unchanged.
- Other 0.48.2 fixes, including folder insertion markers, the vertical **Sphere**, and maple leaves, are unchanged. The pre-release technical audit remained postponed. Android build and device testing had not yet been performed.

## 0.48.2 (versionCode 175) — 2026-11-08

- **Folders and side dock:** moving a workspace folder now uses the actual `SideDockPanel` insertion marker with the same coordinates, dimensions and colors as ordinary shortcuts; the old approximate workspace line was removed. Folder movement passes the current coordinates to the side dock, and the insertion position is retained until dropping is complete.
- **Extracting a side-dock folder shortcut:** swiping vertically on the shortcut begins dragging immediately without a long press, with list scrolling suspended during the drag. Other folders retain long-press behavior. The intentional-extraction threshold was adjusted.
- **Vertical-drawer Mesh Sphere:** calculates radii separately using the screen's actual pixel aspect ratio, preventing an overly stretched or ring-shaped silhouette. Horizontal **Sphere** is unchanged.
- **Maple leaves:** restored a simple, distinctive three-lobed outline with a long central lobe, two broad side lobes, small lower protrusions and veins, rather than many equally prominent teeth.
- Previously confirmed 0.48.1 fixes were left unchanged. Technical-audit corrections remained deferred. No Android build or visual device test was performed; Kotlin PSI and XML were checked statically. User-provided demonstration videos are omitted from ZIP archives.

## 0.48.1 (versionCode 174) — 2026-11-08

- **App-drawer icon effect menu:** expansion state of the **Simple** and **Experimental** groups persists across visits and launcher restarts.
- **Effect compatibility:** selecting an experimental icon effect displays a persistent warning in the app-drawer page-effect settings that Pager/Canvas transitions will be suppressed; the warning also appears within the relevant groups. The Mesh group does not display this warning.
- **Workspace / docks:** dragging a folder over a side- or bottom-dock insertion area displays the corresponding marker. Dragging a shortcut from the side dock to the workspace now uses coordinates relative to the actual workspace area rather than the full screen. Inside a side-dock folder, a sufficiently long directed upward/downward swipe extracts a shortcut even if the finger remains inside the dialog.
- **Vertical transitions:** Mesh **Slicing** now produces independent vertical strips and moves them along the vertical axis; Mesh **Laser burn** uses that axis for both the glowing line and its particles. Pager **Diagonal page swipe** retains the normal vertical movement and adds a horizontal component, without scaling that would expose a neighboring page.
- **Mesh Sphere:** enhanced latitude curvature, perspective depth and light/shadow contrast to make it more visually distinct from Cylinder.
- **Environment effects:** refined the serrated five-lobed silhouette of maple leaves and added veins.
- User-confirmed 0.48.0 fixes (page overview, icon performance, page layout after rotation, settings and weather effects) were not revised. Full Android build and device QA had not yet been performed; pre-release audit corrections were deferred. User-supplied videos are excluded from ZIP archives.

## 0.48.0 (versionCode 173) — 2026-11-07

- **Page overview:** added a bottom system inset and scrolling allowance so the last card is not clipped on smaller screens.
- **Workspace and docks:** removed folder scaling during drag, enabled dragging workspace folders to the side dock (including styling), and corrected reachability of the bottom-dock drop zone. Extracting a shortcut from a side-dock folder now completes the move to the workspace. Side-dock shortcut drags use finger coordinates without grid snapping.
- **Vertical app-drawer mode:** selected Pager (**Diagonal page swipe**), Mesh (cascade twists, page curls, Sphere, Slicing, Laser burn, page-turning transitions) and Canvas (Space shards, checkerboard diagonal, Laser burn, page-turning transitions) effects now have vertical geometry. The app-drawer preview also uses a vertical Pager. Other transition differences in vertical navigation were not reviewed at this stage.
- **App-drawer icon effects:** added collapsible **Simple effects** and **Experimental effects** groups with a red performance warning. Heavy variants use 16 fragments instead of 25 and capture icon contents once per swipe; performance gains still require device measurements.
- **Environment effects:** clouds exit the screen completely before reappearing from the opposite side; precipitation and bubbles get independent random parameters and minor individual trajectory deviations to avoid uniform rows. Refined the green-leaf, maple-leaf and flame silhouettes.
- **Settings:** removed a nonfunctional preview-mode toggle, set activation/animation slider steps to 10, made the entire security-toggle row clickable, and ensured the side dock is displayed when opening its settings. The green-checkmark position settings now show a sample. Icon-pack loading was improved so distinct apps using the same drawable are not discarded.
- **Localization:** default workspace notes follow the selected language until first edited by the user; `RemoteViews` refresh when the selected language changes.
- **Page indicator:** increased tap priority above live wallpapers and visual overlays.
- Existing effect IDs were retained. Corrections from the earlier technical audit were intentionally deferred. User-provided videos are excluded from source ZIPs.
- Full Android build and device testing were pending at this stage: vertical Mesh/Canvas effects and new drag paths required visual verification.

## 0.47.2 (versionCode 172) — 2026-11-07

- **Page overview:** enlarged the home-page selection and delete buttons. Removed the separate gold home-page marker; the home-shaped button itself is now highlighted in gold on the selected page, remaining light-colored elsewhere. Added button action descriptions and single-line page-title width constraints to prevent large icons from covering titles.
- Other settings, app-drawer page recalculation, the workspace and effects were unchanged.
- User-provided videos are not included in ZIPs. Android builds and phone testing had not yet been performed.

## 0.47.1 (versionCode 171) — 2026-11-07

- **Page overview:** restored a more neutral dark palette for cards and thumbnails while keeping the contrast improvements from 0.47.0. The **Page overview** title and close button now have explicit light colors rather than relying on implicit `Surface` content colors.
- **App drawer during orientation changes:** the user-verified page recalculation introduced in 0.47.0 was not modified.
- Page effects and drag-and-drop logic were unchanged. User-provided videos are excluded from ZIPs.
- Android device testing and a full build of this revision had not yet been performed.

## 0.47.0 (versionCode 170) — 2026-11-07

- **Workspace overview:** lightened cards and page thumbnails, and increased contrast for page titles and their borders. Editing, dragging and page management are unchanged.
- **App drawer during rotation:** page capacity now accounts for actual available height and cell size. When the configured row count does not fit, only the effective row count is reduced and more pages are created automatically. Saved row and column settings remain unchanged; portrait pagination is recalculated when returning to that orientation.
- Page/icon effects from 0.46.11, workspace shortcut positions and the adaptive grid are unchanged. User videos are omitted from the source archives.
- Full Android build and device testing were not performed for this revision.

## 0.46.11 (versionCode 169) — 2026-11-06

- **Particle disintegration:** 25 fragments now fade smoothly at 1.5× speed instead of 2×, balancing the behavior between 0.46.9 and 0.46.10. Alpha reaches zero at two-thirds of the transition; trajectories and movement speed are unchanged.
- **Big Bang:** expanded the outgoing whole-icon virtual flight scale from 400–600% to 600–800%, retaining 1.5× acceleration. Incoming-icon assembly and other effects are unchanged.
- Source archives omit user-supplied videos. Android builds and on-device visual tests were not performed.

## 0.46.10 (versionCode 168) — 2026-11-06

- **Particle disintegration:** doubled the smooth fade-out speed of scattered fragments: alpha reaches zero at 50% rather than 100% of transition progress. Fragment directions and travel speed are unchanged.
- **Big Bang:** outgoing whole icons fly 1.5 times faster (capped at the existing final distance). Incoming icon assembly from particles was not changed.
- **Spiral** and **Black hole** are unchanged. Effect IDs and saved settings are unchanged. Videos are excluded from source archives.
- Android Studio / device QA was not performed.

## 0.46.9 (versionCode 167) — 2026-11-06

- **Particle disintegration:** retained the trajectories of all 25 fragments, but their opacity now decreases smoothly from the start of their flight instead of dropping abruptly at the shortcut boundary.
- **Black hole:** retained the approved implementation from 0.46.8 without changes.
- **Big Bang:** retained the enlarged virtual area for outgoing whole icons introduced in 0.46.8; restored incoming icon assembly from 25 fragments flying from the common page center, as in 0.46.7.
- **Spiral:** stabilized incoming icon coordinates relative to the visible page even during cyclic and consecutive swipes. Outgoing icons follow expanding spirals around the center without collapsing into a translucent lump. Incoming icons travel from the center on shorter spirals, turning mainly at the beginning before settling into their target cells without extra rotations. The icons themselves do not rotate.
- Updated descriptions in all eight locales. Internal effect IDs, saved settings, page geometry and previously approved workspace effects are unchanged. Videos are excluded from ZIPs.
- Android Studio / device QA was not performed; forward, reverse and consecutive swipes still needed testing.

## 0.46.8
- Refined advanced app-drawer icon effects.
- Spiral: outgoing icons fly to page centre; incoming icons spiral outward without self-rotation.
- Particle disintegration: fragments fade sharply after leaving the original icon contour.
- Black hole: outgoing fragments converge to the page centre with distance-dependent speed; incoming icons fly in from well outside the page.
- Big Bang: outgoing icons fly outward beyond the page as whole icons; incoming icons fade in.

## 0.46.7 (versionCode 165) — 2026-11-06

- **Black hole (app drawer icons):** keep all 25 fragments of every outgoing icon at their original size while moving toward the common centre of the drawer page. Removed the old fragment scaling; incoming icons still fade in, and the page stays stationary.
- **Big Bang:** incoming fragments retain a constant size while flying *from the drawer page centre* to their final cells. Removed the previous 0.28→1.0 fragment scaling and the incoming icon's layer-level alpha fade that caused off-cell fragments to be clipped. Normalize incoming icon coordinates to the visible Pager page if they were initially measured on an off-screen neighbor. Outgoing icons still fade quickly; page remains stationary.
- **Spiral:** widened the initial orbit about each icon's original position from `0.32 × cell size` to `0.80 × cell size` (2.5×). Icons remain upright and use the approved outward spiral curve.
- Renamed the app drawer icon effect **Fireworks → Particle disintegration** (localized in all eight languages), keeping the stable internal `FIREWORK` ID and its already-approved fragment motion unchanged.
- Full and incremental source archives exclude user-recorded videos. Visual QA / Android compilation still required.

## 0.46.6 (versionCode 164) — 2026-11-06

- **Spiral (app drawer icons):** icons remain upright (no `rotationZ` and no axis scaling) while their positions trace a smoother widening spiral around their original centres. The radius grows gently during the early orbit before extending to just beyond the page boundary. Incoming icons follow the reverse trajectory.
- **Fireworks:** kept the existing outgoing 25-fragment scatter to the measured page edge; incoming icons now **only fade in gradually** in their stationary places, without reverse fragmentation.
- **Black hole:** outgoing icons scatter into 25 pieces that converge to the common centre of the visible drawer page (instead of individual icon centres). Incoming icons only fade in. The page itself remains stationary.
- Added **Big Bang** as a distinct app-drawer icon effect: outgoing icons fade quickly, while incoming icons assemble from 25 fragments flying from the centre of the visible page into their individual icon positions. The icon transitions remain Compose graphicsLayer/Canvas-based, without Android Mesh.
- Each advanced effect now distinguishes the outgoing page from the incoming page using the transition-source page, rather than interpreting the absolute Pager offset as an identical animation on both pages. Preserve the screen-stationary page behavior and avoid per-fragment `drawContent()` repetition.
- Added new effect name and updated descriptions in all eight supported languages. Retained existing enum identifiers, settings, backup formats, and all previous page effects. Archives exclude user-recorded video files. Android build/device QA still required.

## 0.46.5 (versionCode 163) — 2026-11-06

- Removed the explanatory parenthetical from the zero setting of both minimum-effect-duration sliders: show simply **0 ms** in all eight languages; nonzero settings and behavior are unchanged.
- Redesigned advanced Compose app-drawer icon effects to keep both drawer pages stationary during icon animations. Counteracts Pager's displacement at the page surface, once, without an additional icon-level compensation. For these three effects the simultaneous page transition is deliberately suppressed (they remain unavailable with Mesh page effects as before).
- **Spiral:** two locally centred revolutions, with a slow-growing radius for the first turn; subsequent travel expands to just beyond the screen boundary. Icons no longer immediately fly off as soon as the swipe begins. Content remains live.
- **Fireworks:** each of the 25 deterministic icon fragments travels to the nearest measured drawer page boundary along its own direction plus a short margin. The page is stationary; fragments do not become transparent prematurely or jump several screen diagonals outside the viewport. Arrival plays backward.
- **Black hole:** each icon's 25 fragments collapse into that particular icon's centre, not the centre of the whole screen. The page is stationary and individual pieces contract continuously as the swipe advances.
- Kept existing effect IDs, stored settings, and the 0.46.4 app drawer drag fix. No user-recorded video files in deliverable ZIP archives. Static checks performed; Android Studio compilation and visual/device QA still required.

## 0.46.4 (versionCode 162) — 2026-11-06

- Fixed app-drawer → desktop placement so it uses the exact measured workspace bounds and the same `workspaceFolderDropFractions` coordinate mapping as a folder drag. Drops do not snap to an application grid; a new folder is created only with a clear centre-on-centre overlap of the dragged app and an existing desktop shortcut/folder. Nearby shortcuts can be placed freely. Dragging from a folder and automatic vacancy search for menu-based additions remain unchanged.
- Corrected **Horizontal strips** in Compose Canvas and Android Mesh to reveal from **right to left on a left swipe** and **left to right on a right swipe**. The previous inverse was caused by confusing the sign of Pager offset; incoming and outgoing masks share a consistent swipe direction. Vertical-strip variants and other effects are unchanged.
- Moved the two independent 0–1000 ms minimum-duration controls **after all three page-effect groups**, to the very bottom of their respective desktop/app-drawer effect settings. Values, 10 ms steps and persistence remain unchanged.
- App drawer icon effects: renamed **Wave → Sway**, **Deformation → Stretching** and the two legacy cube variants to **Turn left / Turn right** while preserving their enum names and saved settings. Added **Turn up / Turn down** based on the same `graphicsLayer` perspective/hinge logic. Picker displays the four turns consecutively without changing existing enum IDs.
- Added three Compose-only app-drawer icon effects: **Spiral** (icons fly out/in on expanding spirals), **Fireworks** (25 deterministic outward-moving fragments per icon, reversed for incoming icons), and **Black hole** (fragments drawn toward the screen centre). Fragment animations record each live icon only once into a reusable offscreen `GraphicsLayer` and replay clipped pieces through Canvas rather than re-rendering the icon 25 times. Mesh rendering is not introduced; existing restrictions while using a Mesh page transition still apply.
- Added effect titles/descriptions in eight languages. User-recorded video clips remain excluded from both source ZIPs. Static checks completed; Android Studio compilation, performance and device visual checks still required.

## 0.46.3 (versionCode 161) — 2026-11-06

- Renamed five page-wipe effects as a consistent **Page turning:** family (with corresponding localized prefixes): Angle, Arc, Diagonal, Wave, and Intersecting waves. Applied to Canvas and Mesh variants in all eight supported languages; effect enum IDs and descriptions are unchanged.
- Moved Canvas **Page turning: Angle** directly above **Page turning: Arc**; Mesh already had the intended order.
- Made ordinary horizontal-strip reveals follow the swipe direction in both Canvas and Mesh: a left swipe reveals right-to-left and vice versa. Preserved strip order, timings, and all vertical-strip behaviors.
- Fixed dragged desktop shortcuts jumping away from the user's chosen coordinates. App drawer drags, placement-overlay drags, and extraction from workspace folders now use the dropped fractions directly (clamped only to the valid workspace range), just like existing-icon moves. Automatic additions through the add-icons/settings menus and the widget picker still use `freeWorkspacePosition`/`vacantWorkspacePosition` to avoid overlaps when no exact placement was selected.
- Preserved all existing effect IDs, saved settings and backup formats. Excluded user-recorded videos from both ZIP deliverables; remaining project/media resources retained. Full Android Studio compilation and device QA pending.

## 0.46.2 (versionCode 160) — 2026-11-06

- Fixed inherited scrolling in the launcher settings dialog: each settings page now has an independent `ScrollState` for the lifetime of the dialog, instead of sharing one `defaultSettingsScrollState` across all non-effect screens.
- Opening a child settings page starts at its own position (initially the top); returning to the parent restores exactly the position where the user left it. Visiting an unrelated page never changes the parent's position. Reopening the launcher settings dialog starts the root page at the top.
- Preserved the deliberate scroll-to-selected-item behavior when opening either page-effect picker and the scroll-to-folder-name behavior when opening a folder. No changes to their animations, the app drawer's remembered page, or user-controlled scrolling.
- Existing settings values, layout, effect identifiers and backup/restore formats remain unchanged. Release history is recorded only in `CHANGELOG.md`.
- Static source checks performed; full Android Studio compilation and physical-device testing remain pending.

## 0.46.1 (versionCode 159) — 2026-11-05

- Fixed long setting labels becoming one character per line on narrow screens: all numeric settings using `IntegerSliderSetting` and the independent workspace/drawer minimum-effect-duration sliders now use a shared full-width, word-wrapping label header. The current value is displayed on its own, right-aligned line **directly above the slider**, rather than competing for horizontal space with its label.
- Applied the same narrow-dialog safety to gesture-action settings and radio/checkbox labels: long captions can wrap without being compressed by adjacent controls. The selected gesture action appears beneath its caption.
- Slider ranges, values, callbacks, settings persistence and effect implementations remain unchanged. This release only adjusts responsive settings layout; no new Release Notes or Canvas QA files.
- Static layout/source checks performed; full Android Studio compilation and device-level layout testing remain pending.

## 0.46.0 (versionCode 158) — 2026-11-05

- Compose Canvas and Android Mesh: renamed **Thread** to **Wave** while preserving stored effect identifiers. Canvas **Intersecting threads** renamed to **Intersecting waves**; localized in all eight supported languages.
- Removed the two experimental Mesh Random bars variants from the effect picker. Their legacy enum identifiers remain readable for backward compatibility; Canvas random bars and Mesh progressive horizontal/vertical strips are unchanged.
- Effect pickers for desktop and app drawer now scroll to the effect selected when opening the respective settings screen, **only on entry**. Selecting another effect does not automatically reposition the list; collapsed selected groups are expanded on entry.
- Added independent, persistent **minimum page-effect settling duration** sliders for desktop and app drawer: 0–1000 ms in 10 ms increments. 0 preserves the standard Compose Pager fling; non-zero values apply a bounded tween to the completion phase after finger release. The drag still follows the user's finger and programmatic page moves are unaffected.
- Added the two duration preferences to the appropriate area resets and automatic JSON preferences backup/restore.
- No release notes or Canvas QA documents created.

## 0.45.7 — Consistent orientation order and Canvas Cube performance

- Compile fix: use `androidx.compose.ui.graphics.layer.CompositingStrategy` for `GraphicsLayer.compositingStrategy`; the former `androidx.compose.ui.graphics.CompositingStrategy` import caused an assignment type mismatch. No behavior or version change.

- Reordered all horizontal/vertical effect pairs across Pager, Compose Canvas and Android Mesh to show **horizontal first, vertical second**, including the Mesh tile-flip pair whose legacy enum IDs differ from their visible orientation. Changed only the picker order, not identifiers, translations, or animation implementations.
- Optimized the experimental **Compose Canvas Cube** without changing its 32-strip perspective geometry: capture the current live page's draw commands in a reusable Compose `GraphicsLayer` once per frame, then replay that layer for each projected strip instead of running the entire page's `drawContent()` 32 times per draw.
- Precompute the cube's per-frame trigonometric and perspective constants once for all strip points. Source page content remains live while swiping; the transition does not require asynchronous bitmap snapshots.
- The approved Mesh and Pager renderers, curl configuration, all page-effect IDs and JSON backup values are unchanged. Full Android build and on-device performance/visual validation are still needed.
- All release history remains in `CHANGELOG.md`; no Release Notes or Canvas QA were added.

## 0.45.6 — Curled page refinements, sorted picker, Canvas cube

- Applied the user-tested Mesh curl configuration to both **Page curl (inward)** and **Page curl (outward)**: the translucent carrier uses `Color.argb(50, 180, 180, 180)`, and the outgoing geometry and incoming reveal both use `softness = 1.0f`.
- Removed the misleading rear-face texture mirroring in `pageCurl()` (`texMirror = mirroredU`). The lighting test `back = cos(angle) < 0f` remains; the rest of the curl algorithm is unchanged.
- Renamed the two Mesh-only interlaced blinds **Tile flip (vertical/horizontal)**. No animations, Pager or Canvas labels were renamed.
- Grouped all three effect picker sections by their visual behavior using a separate UI-only ordering list. Existing enum declarations, effect IDs and JSON backup values are untouched. Canvas cross-circle/oval variants follow Angle; Canvas Laser burn precedes Blinds; Mesh Laser burn follows Thread; both Mesh curls precede Cascade twist.
- Added an experimental **Cube** under Compose Canvas immediately after **Dissolve**. The Canvas renderer uses narrow projected strips based on the native Mesh shared-cube face/camera geometry, with no `android.graphics.Mesh` and no change to the established Mesh Cube or Pager shutters. Android Studio and device-level visual/performance QA are still needed for the approximation.
- No Release Notes or Canvas QA files were created. All changes are tracked in `CHANGELOG.md`.

## 0.45.5 — Translucent carrier for Mesh page curls

- Added a **translucent page sheet** to both Android Mesh curls, **Page curl (inward)** and **Page curl (outward)**. The outgoing page's existing full-page snapshot (shortcuts, folders, notes, and cached widget contents) is rendered onto a light, semi-transparent carrier, making the sheet visible even on sparse workspace pages.
- The carrier uses **exactly the same curl Mesh**, animation math, shading, and layer order as 0.45.4; neither fold geometry nor the progressive reveal of the incoming page is changed. Only the outgoing snapshot texture is decorated, once per captured bitmap, and only during the transition.
- The sheet disappears with the Mesh transition. Cancelled swipes return directly to normal live page content; the next page and all non-curl effects use their unmodified textures.
- All existing effect identifiers, page layouts, widgets, Compose Canvas and Pager behaviors are preserved. No separate release-notes or QA files.
- Static source and archive checks performed. Full Android Studio build and device verification are still required.

## 0.45.4 — Restore Mesh progressive strips

- Restored **Horizontal strips** (`RANDOM_BARS`) and **Vertical strips** (`STRIPS_VERTICAL_MESH`) to their original **0.45.2** flat Mesh rendering, removing the angular/wavy borders added in 0.45.3. Preserved 30 horizontal and 20 vertical strips and the original staggered reveal timing.
- **Random strips (horizontal/vertical)** still use the 0.45.3 angular strips and instant random-order replacement. **Page curl (outward)** and all other Mesh/Canvas/Pager effects remain unchanged.
- Existing effect IDs, localized names, persistent settings and JSON backups remain compatible. Changes recorded only in `CHANGELOG.md`.
- Android Studio compilation and on-device verification remain pending.

## 0.45.3 — Mesh angular strips and outward curl (device test candidate)

- Mesh-only: changed both **Random bars** variants and both progressive **Horizontal/Vertical strips** variants to use gently faceted, shared triangular-wave band boundaries (22% of stripe thickness). The page textures remain stationary and undistorted; random strips still replace whole bands immediately in the same 96-band order, and progressive strips retain their original per-band timing and reveal direction.
- **Page curl (outward)** now folds its rolled flap **back over the still-visible current page**, rather than merely reversing the perspective depth of the inward roll. Added a darker crease, a distinct illuminated raised flap, and reversed triangle draw order for the opposite swipe direction so the flap stays in front. The next page is exposed only beyond the screen-space outgoing silhouette; **Page curl (inward)** is unchanged.
- The approved Mesh cube, interlaced blinds, Triangle/Diamond, all Compose Canvas effects and all Pager effects remain unchanged. Existing effect IDs and preferences are preserved.
- All changes recorded only here; no release notes or Canvas QA files. Full Android/physical-device testing required.

## 0.45.2 — Android Mesh topology and scroll/cube revisions (device test candidate)

- Compile fix: restored the `-> true` result in `PageTransitionEffect.isMeshCube()` after `CUBE_IN_VERTICAL`; resolves Kotlin errors at `MeshPageEffects.kt:153` and `:158` without changing any effects.

- Android Mesh only; approved Compose Canvas and Pager effects have not been modified or renamed.
- Rebuilt Mesh **Checkerboard (vertical/horizontal)**, **Triangle**, **Diamond**, and **Random strips (horizontal/vertical)** with the same regular, triangulated Mesh lattice used by the established Mesh transitions. Removed all random displacement of mesh vertices; the reveal boundary now follows unshifted triangular facets. Even/odd checker phases, grow-from-centre shapes, and stable 96-band shuffle remain.
- Corrected the horizontal-labelled interlaced Mesh blinds order: left swipes reveal columns from the right, right swipes from the left. Canvas and Pager blinds are unchanged.
- Renamed the approved soft curl to **Page curl (inward)** (`PAGE_CURL_SOFT`). Added a mirror-depth **Page curl (outward)** (`PAGE_CURL_OUTWARD`) based on exactly the same soft geometry and occlusion calculation, with opposite depth/shading. Removed the older `PAGE_CURL` variant from the picker/enum; saved settings or JSON backups referencing `PAGE_CURL` or `PAGE_CURL_GOOGLE` now migrate to the inward curl. Existing `PAGE_CURL_SOFT` and `PAGE_CURL_APPLE` remain compatible.
- Rebuilt **Cube (inside)** on the actual shared Mesh **Cube** faces, reversing the wall depth and using a slight approach instead of the outer cube's pullback. Removed the earlier unrelated Pager-shutter geometry and its extra page fade. It remains directly below **Cube** in Mesh.
- Version 0.45.2 / code 152; updates recorded exclusively in this CHANGELOG. No separate QA or release notes.
- Static checks are not substitutes for compiling in Android Studio or visual testing on a device.

## 0.45.1 — Angular Android Mesh refinements (device test candidate)

- Android Mesh ONLY. Existing Compose Canvas and Pager renderers, effect identifiers and resource labels remain unchanged.
- Native Mesh checkerboards now reveal small skewed triangular fragments with a shared deterministic irregular grid, instead of rectangular clip masks. The two-phase even/odd checker logic and individual top-to-bottom / left-to-right reveal remain intact.
- Native Mesh triangle/diamond now use the same irregular triangular facets along their growing outline instead of clean exact polygons. Native Mesh random horizontal/vertical strips preserve instantaneous per-strip replacement and stable random order but now have angular rather than straight strip boundaries; both snapshots share identical facet coordinates to avoid gaps.
- Native Mesh progressive **Horizontal strips** increased from 20 to 30 thin horizontal bands; vertical variant unchanged.
- Native Mesh horizontal interlaced blinds reverse their panel turn direction and horizontal traversal to agree with the swipe. Their current labels remain unchanged pending choice of more descriptive names.
- Native Mesh page curls now reveal the next page only beyond the actual projected screen-space envelope of the rolling outgoing sheet (the previous UV-space fold edge revealed it too soon); soft/regular variants use their corresponding bend parameters.
- Mesh **Cube (inside)** now uses real, unaccelerated Pager offsets and hinge-relative perspective, with correct per-page depth order and no extra shader darkening. Moved directly below the ordinary shared **Cube** in the Mesh section.
- Ordered Mesh outward **Cascade twist** immediately above its inward **(reverse)** counterpart.
- Renamed only the Mesh flying-rectangles effect to **Slicing** with dedicated translations in all eight locales. The effect animation and persistent ID are unchanged.
- All changes consolidated here; no Release Notes or Canvas QA files added. Full Android compilation and physical-device comparison still required.

## 0.45.0 — Android Mesh revisions (device test candidate)

- Mesh-only: preserve all Pager/Compose Canvas effect identities, algorithms and localized names. Keep legacy Mesh identifiers intact for saved preferences and JSON backups.
- Rename Mesh checkerboard to **Checkerboard diagonal**, add exact rectangular Mesh geometry for **Checkerboard (vertical reveal)** and **(horizontal reveal)**: even checker cells reveal first, odd ones second, each cell independently top-to-bottom or left-to-right.
- Rename old half-diamond Mesh **Triangle** to **Angle**. Add genuine center-growing Mesh **Triangle** and **Diamond**, clipped to the screen; the old page remains behind the revealed region.
- Add Mesh **Random strips (horizontal/vertical)**: 96 complete, instantly appearing sections in the same stable pseudorandom order as Canvas. Rename old 20 delayed Mesh bars **Horizontal strips**, add its vertical analogue **Vertical strips**. Unlike the new random effects, the two legacy strip effects gradually wipe individual stripes.
- Rename Mesh Glitter to **Dissolution**; swap only Mesh Interlaced Blinds display titles to match perceived motion orientation (enum identities and motion unchanged).
- Mesh page curls: incoming page is now revealed only behind the progressively rolled-away portion of the old page, instead of appearing fully at the start. Both existing scroll variants still share the original cylindrical deformation; the soft variant uses 78% angular intensity and therefore a larger curl radius.
- Add **Cube (inside)**, an independent Mesh rendition of Pager's horizontal inward shutters; the Pager itself is unchanged.
- Rename outward cascade `DNA_OUT` to **Cascade twist**, and inward `DNA` to **Cascade twist (reverse)**. Existing animation math unchanged.
- Kept the Mesh 12-row flying rectangles effect named as before pending a user choice of replacement name (candidate descriptions supplied separately).
- No new Release Notes or Canvas QA; full Android build and physical-device visual testing required.

## 0.44.13 — Compose Canvas: Angle, Triangle, Diamond, Space shards (test candidate)

- Renamed the previous Mesh-geometry Canvas **Triangle** effect (`TRIANGLE_WIPE_CANVAS`) to **Angle** and moved it immediately before the Canvas **Arc**. Its existing animation and persisted ID remain unchanged; the original Mesh **Triangle** remains unchanged.
- Added independent centre-growing Canvas **Triangle** (`TRIANGLE_REVEAL_CANVAS`) and **Diamond** (`DIAMOND_REVEAL_CANVAS`) near Circle/Oval. Their polygon clipping is shared by incoming/outgoing pages, mirrors the current Circle/Oval reveal semantics and covers all screen corners at the end of the swipe.
- Reordered the existing Canvas **Space shards** pair: stationary (`RETRO_FLY_TILES_STATIC`) now appears first as **Space shards**, and moving (`RETRO_FLY_TILES_OREO`) second as **Space shards with scrolling**. Animations and persisted IDs unchanged.
- Removed all `CANVAS_QA_*.md` files. All changes are recorded only in `CHANGELOG.md`, with no new release notes or QA documents.
- Added localized names/descriptions for all eight languages. No Mesh or Pager renderer/label changes. Static validations performed; Android Studio build and device test remain pending.

## 0.44.12 — Canvas Triangle (test candidate)

- Ported Android Mesh **Triangle** as a separate **Compose Canvas** effect (`TRIANGLE_WIPE_CANVAS`). The existing Mesh `TRIANGLE_WIPE` and its rendering are not changed.
- Reproduced the Mesh half-diamond boundary exactly in normalized coordinates: `edgeDistance(u, direction) + abs(v - 0.5) * 0.88 <= progress * 1.44`. The entering page reveals from the side edge, with the leading corner at vertical centre. Reverse swipes mirror its direction.
- Both pages are clipped using a single, complementary Canvas Path to avoid transparent seams; cancelling a swipe restores the outgoing page. The effect uses the existing localized Triangle title and description (all eight languages), without changing labels for Mesh or Pager.
- All previous effect IDs and settings are preserved. `CHANGELOG.md` remains the only version history; no Release Notes are created.
- Static geometry and resource checks performed. Android Studio compilation and physical-device evaluation still required.

## 0.44.11 — Canvas horizontal/vertical variants (test candidate)

- Keep the existing 96-strip, instantaneous, deterministic random reveal as **Random strips (horizontal)** (`RANDOM_BARS_OREO`); only its Canvas-specific label changes. Add a new **Random strips (vertical)** (`RANDOM_BARS_VERTICAL_CANVAS`) with the same shuffled order but narrow full-height vertical columns. Each rectangle switches completely to the incoming page without sliding, stretching or fading.
- Preserve **Checkerboard (vertical reveal)** (`CHECKERBOARD_VERTICAL_CANVAS`) and add **Checkerboard (horizontal reveal)** (`CHECKERBOARD_HORIZONTAL_CANVAS`) beside it. Both use the same square-cell grid: even squares reveal during the first half, then odd squares; in the new version each *individual* square reveals from left to right rather than top to bottom.
- Incoming/outgoing pages use exact complementary masks during forward, backward and cancelled swipes. Original effect identifiers and settings remain compatible; both new variants have separate enum IDs. Names and descriptions are localized into all eight existing language catalogs.
- Scope is **Compose Canvas only**: no effect implementation, resource label or renderer for Android Mesh or Pager has changed.
- Consolidated version history in `CHANGELOG.md`; removed 18 legacy `RELEASE_NOTES_*.md` files and updated the release guide and historical test reference. No separate release notes are generated from this version onward.
- Static structure, locale, geometry and archive checks pass; full Android Studio compilation and on-device visual testing remain necessary.

## 0.44.10 (Canvas checkerboard and strip effects)
- Details: the existing 96 thin strips replace entire rows instantly in deterministic Fisher–Yates order, and the checkerboard reveals each even cell independently over progress 0–50%, then each odd cell over 50–100%. The Canvas-only names are localized, without changing the names of corresponding Mesh effects.
- Canvas-only checkerboard diagonal rename, new vertically revealing even/odd square checkerboard.
- Canvas random bars are now thin horizontal full-width stripes appearing in a deterministic random order with no motion.
- Horizontal Canvas interlaced strips made comparable in thickness to vertical; both renamed to strips only in Canvas.
- Mesh and Pager titles and rendering untouched. Existing effect IDs retained.

## 0.44.9 (Canvas names and crossed threads)
- Named the Mesh and Canvas fire effects **Laser burn**.
- Reordered Canvas circle/oval and crossed variants.
- Added Crossed threads as a separate legacy-style Canvas effect without two vertical white lines; retained original Thread.

## 0.44.8 — Compose Canvas effects (test candidate)

- Rename the two Canvas flying-rectangle variants **Space shards**; mirror the Canvas fire effect and rename it **Burn away**.
- Move the previous random-strip Canvas implementation into vertical interlaced blinds; add horizontal transposition with shared complementary page masks. Port Mesh random bars (20 delayed strips) into the existing Canvas random-bars ID.
- Rename the old Canvas checkerboard to **Dissolve** and add a distinct Mesh-timed Canvas **Checkerboard**.
- Add genuine expanding **Circle** and **Oval** reveals using complementary masks, before the preserved **Cross circle** and **Cross oval** variants.
- Remove the two visible strips from Canvas **Wave** (formerly Thread); replace Canvas **Page turning: Diagonal** with Mesh-derived geometry; add Canvas **Page turning: Arc**.
- Hide Canvas **Glitter** in the picker, retaining its old enum for saved settings. Keep all other existing enum IDs unchanged, and localize new labels/descriptions in all eight languages.
- No Android Studio/GPU device build or visual validation was performed in the authoring environment.

## 0.44.7 — Separate Pager and Mesh effect labels (test candidate)

- Device feedback: all Pager effects were tested in 0.44.6 and work as expected. The remaining request is display names only.
- Rename the Pager/graphicsLayer **CARD_OREO** effect **Plane / card → Compression**; keep the separate Android Mesh **CARD** name unchanged.
- Rename Android Mesh **VORTEX** **Wheel → Vortex**, restoring the original Mesh name; leave Pager/graphicsLayer **VORTEX_OREO** as **Wheel**.
- Use distinct string resource IDs per rendering technology in all eight locales. Preserve enum IDs, stored choices, renderers, effects, previews and gestures. No settings migration required.
- Android release compilation and on-device label verification remain pending.

## 0.44.6 — Pager effect names (test candidate)

- Device QA of 0.44.5 **passed 6/6**: after Zoom out, workspace shortcuts, folders and widgets respond to taps, including reverse and cancelled swipes. Zoom in remains unchanged.
- Updated the localized display names of three Pager effects: **Wave → Diagonal page swipe**, **Rotate → Slanted page swipe**, **Flip → Rotating page swipe**. Updated labels in all eight locales without changing the `WAVE`, `ROTATE` or `FLIP` enum identifiers, animations, settings storage or previews.
- Separated the Pager Wave label (`ui_diagonal_page_swipe`) from the icon animation Wave label (`ui_wave`). Icon animation retains its original name.
- Canvas/Mesh renderers and all other effects are unchanged. Needs Android Studio compilation and on-device title verification.

## 0.44.5 — Fix Zoom out blocking desktop taps (test candidate)

- Fix **Zoom out** leaving a fully transparent, 400%-scaled neighbouring page above the active page when the Pager is idle. Even with `alpha = 0`, the elevated layer could intercept taps on shortcuts, folders and widgets.
- Only apply the incoming-page zIndex elevation **while a swipe/settling animation is active**. After it ends, restore the caller's interactive-page zIndex immediately. Preserve both Zoom out's scale/opacity math and **Zoom in** behaviour unchanged.
- Applies to desktop, paged app drawer and previews that share `MeshPageSurface`. Requires Android Studio compilation and a device recheck of tapping after complete/aborted swipes.

## 0.44.4 — Pager Zoom in / Zoom out (test candidate)

- Renamed the existing stationary simultaneous **Scale** effect to **Zoom in** (including its Russian localization). The persisted enum identifier `SCALE` and animation behaviour are unchanged, so older saved settings and JSON backups remain valid.
- Added a separate Pager graphics-layer effect, **Zoom out** (ID `ZOOM_OUT`, localized in all eight languages). The incoming page is initially at **400% scale and 0% opacity**, then decreases to **100% scale and 100% opacity** across the entire swipe; the outgoing page stays at 100% scale and fades from 100% to 0% opacity simultaneously.
- Both pages remain centred by cancelling Pager's native displacement. The incoming page draws above the outgoing page to avoid incorrect stacking; preview, horizontal workspace, and horizontal/vertical paged app drawer use the same implementation.
- Added localized names and descriptions for all eight languages; no Canvas/Mesh implementation changes. Based on 0.44.3 (user confirmed its simultaneous scale worked correctly). Android Gradle compilation and on-device testing of this new candidate are pending.

## 0.44.3 — Simultaneous stationary Pager Scale (test candidate)

- **Scale** now shrinks the outgoing page from 100% to 0% while growing the incoming page from 0% to 100% **concurrently** throughout the gesture, instead of splitting the movement into two phases.
- Cancel the native Pager displacement for both pages, to keep them stationary in the centre. Place the growing incoming page above the shrinking outgoing page; only the participating pages render and there is no opacity fade.
- Behaviour is shared between desktop, app drawer and effect preview, in both directions. No other effects changed.
- Based on 0.44.2 and the working 0.44.0 compilefix v3. Android build and device testing remain necessary.

## 0.44.2 — Fix stationary Pager Scale (test candidate)

- **Scale** now runs as two exclusive zero-to-full zoom phases with a true 0% minimum size: outgoing page 100%→0% before mid-swipe, incoming page 0%→100% afterward. Never show background neighbours during the transition.
- Retain Pager position compensation to keep the visible page stationary (horizontal workspace and horizontal/vertical app drawer). Replace partial opacity with a binary hidden/visible mask.
- No other page effects or saved settings change; based on 0.44.1 + compilefix v3.
- Device/build QA required.

## 0.44.1 — Pager cleanup, stage 2 (test candidate)

- **Scale** now zooms both pages in place without Pager's horizontal/vertical slide. The 100%-to-30% scale range remains unchanged for workspace and app drawer; the alpha transition is retained.
- Removed the obsolete **classic Cube** from the Pager picker/enum, leaving Mesh Cube variants untouched. Persisted `CUBE` values from older profiles/JSON imports map to **Horizontal outward shutters** in both workspace and app drawer.
- Renamed **Vortex** to **Wheel** in all eight translations (including the Russian localization); this is a display-only rename and the Mesh renderer remains unchanged.
- **Flip**, **Rotate** and **Wave** names remain unchanged pending the naming decision: the proposed localized names are Axial flip, Tilt and Sway.
- Based on user QA of 0.44.0 compilefix v3: eight Pager checks passed, and the three duplicate-comparison checks were skipped. The new scale animation requires device testing. This candidate has not been fully built with Android Gradle in the authoring environment.

## 0.44.0 — Pager transitions review, stage 1 (test candidate)

- Removed **Fade**, **Stack** and **Depth** from the selectable effects and enum. Existing stored values and older JSON profiles map to Crossfade, Default Pager and Scale respectively, for workspace and app drawer.
- **Scale** now shrinks a departing page from 100% to about 30% (previously 78%), retaining its alpha profile.
- Reworked the non-Mesh **Plane / Card** effect with paired 3D graphics-layer rotations (outgoing face up to 90°, incoming face from 90°), centered on the screen with Pager's built-in slide cancelled. The faces meet at the halfway point instead of moving apart and leaving a long blank interval. For vertical Pager, the rotation uses the X axis.
- Source comparison confirmed that **classic Cube** and **Horizontal outward shutters (Pager)** both rotate around an outer vertical edge with different angles and fading, while **Flip** rotates around the page center. All three are temporarily retained for visual testing; classic Cube is the strongest redundancy candidate.
- Canvas and Mesh effects remain unchanged. Android Studio compilation and on-device visual/gesture tests are required before publication.

## 0.43.7 — Folder drop coordinates and workspace reset behavior (test candidate)

- Permanent extraction of an icon from an open folder now uses the touch release point in **screen coordinates** rather than normalizing dialog-root coordinates against display dimensions. The target is mapped through the measured workspace bounds before the existing collision-aware placement algorithm runs. Temporary folder-fan swipes are not changed.
- **Security → Reset launcher settings → Workspace** now restores the initial five-page workspace, starter actions/folder and two native note widgets (when the system grants self-widget binding); other domains remain untouched.
- Added a **separate** red **Clear workspace items** action under **Workspace settings**, with an explicit confirmation. It removes shortcuts, folders and all hosted widgets (including note texts), while preserving the page count, chosen page, gestures, transitions, docks and app drawer.
- Test results on 0.43.6 before these changes: workspace placement 9/10 (only permanent folder extraction failed); backup/domain reset 14/14. Regression tests and an Android Studio build of this candidate are required before release.
- Effects are intentionally unchanged pending the separate staged review: Pager → Canvas (rename distinct existing effects) → Canvas approximations of Mesh shapes → final names/categories audit.

## 0.43.6 — First-run copy and launcher action labels (test candidate)

- Revised the home-page welcome note: swiping up or down **from the folder** lets you retrieve icons without opening it. Localized in all eight supported languages.
- Renamed the two launcher actions to concise labels: **Side dock** and **App drawer**, reusing the existing localized interface names.
- Kept the original two-note arrangement: one native note widget on the home page, one on the adjacent page. No changes to widget placement, gestures or stored data.
- Confirmed by user: the five repeated widget dragging and resizing scenarios passed on-device with 0.43.5.
- Existing widgets keep their user-editable text; the revised welcome text is seeded only on a new profile.

## 0.43.5 — Repeated widget drag uses the latest position (test candidate)

- Fix widget and note-widget jumping back to their original coordinate on the second drag while edit mode stays open.
- Refresh the gesture callbacks' position, bounds and commit action after each update without restarting the active pointer input detector.
- Applies to the standard widget editor overlay and the ordinary long-press widget drag path. No settings or widget data migrations.

## 0.43.4 — Native e_e launcher text widget (test candidate)
- The real note widget stores up to 4000 characters; backup/restore tracks widget text through stable references and remaps Android widget IDs when restored. The old `workspace_notes` Compose-card data is not migrated.

- Implement a normal `AppWidgetProvider` for notes and route them through the existing hosted-widget move, resize and removal UX.
- Provide a tap-to-edit dialog, localized empty note hint, initial welcome widget seeding and text persistence through widget ID remapping in JSON backups.
- Retire the separate Compose note-card interaction path without migrating test-stage notes.
- Android build and device verification remain required.

## 0.43.3 — Correct repeated note dragging and vertical resizing (test candidate)
- Note pointer hit testing uses the widget-safe workspace height and excludes the bottom shelf only once.

- Use the same pixel-anchored move and resize algorithm as the launcher’s hosted widgets; stable gesture detectors no longer restart when note dimensions change.
- Fix note jumps on later drags and top/bottom resize gestures; all six handles use the same cumulative drag delta and fixed opposite-edge anchoring as widgets.
- Keep note coordinates, sizes, content and backup JSON schema unchanged. No external AppWidgetProvider migration or new permissions.
- Static source checks and standalone note geometry tests passed; **Android Gradle build and real device testing are still required**.

## 0.43.2 — Free placement, resizable notes and welcome actions (test candidate)
- The collision search keeps a minimum 3 dp clearance, follows the original long-press point through asynchronous widget binding, and initializes old Compose notes at 286 × 134 dp when dimensions were absent. Existing profiles are not overwritten by new layout defaults.

- Precise dp-based collision search for shortcut and widget placement, preserving long-press anchor even through widget binding.
- Six resize handles and persistent dimensions for built-in notes; clearer welcome note text.
- Fresh-profile two-row layout adjusted to leave space above folder and for extraction.
- Welcome folder now contains Side dock, App drawer, Gestures, App drawer effect and Wallpaper, with short capitalized labels.
- New launcher actions for Gestures and Wallpaper; Wallpaper added to the workspace long-press menu.

## 0.43.1 — Pager shutters, lower shortcuts and desktop notes (test candidate)
- Welcome notes are initially placed on the home screen and its right neighbour, follow overview page reorder/delete, and are stored in private preferences and manual JSON backups.

- Default horizontal inward shutters now select **Pager graphics-layer** rather than Mesh; already saved effect preferences are unchanged.
- First-use icons and folder moved toward the middle/bottom; Launcher settings comes last in the first row.
- The welcome folder uses launcher-action names without “Open” and shows action icons in the folder and preview; extracted actions receive proper names.
- New local, editable note cards, including two welcome notes on a clean profile; store and restore via existing launcher SharedPreferences / JSON backups, Workspace/All reset and overview page operations.
- No new permissions and no network access. This is a **launcher-local note**, not an Android OS AppWidget. Android compile and on-device QA still required.

## 0.43.0 — First-run experience and adaptive drawer (test candidate)

- New workspaces start with five launcher-action shortcuts and a folder containing the same five functional actions, on the default home page. Existing user profiles and deliberately emptied workspaces are not repopulated on update.
- Launcher actions now work inside the initial folder and can be extracted to the desktop as action shortcuts.
- New workspace effect default: **Shutters inward — horizontal**. App drawer effect: **Crossfade**; drawer icon effect: **2D rotation**; icon activation: **Enlarge**. Previously saved user choices remain unchanged.
- Page overview defaults to **Editing** for new profiles; existing preferences retain their selection.
- New desktop shortcuts, action shortcuts and settings shortcuts prefer the position of the long press that opened the Add dialog. If that position is occupied, the launcher searches for a nearby free place. Without a remembered press position, it searches from the center of the page.
- Rearranged the desktop Add menu: Application icon, Widget, Settings shortcut, Launcher action. The redundant direct effects choices are removed from that menu, but available as launcher actions. The action picker has shorter labels and Launcher settings last.
- Added automatic app-drawer grid defaults based on usable screen dimensions in dp; column and row overrides remain manual, orientation changes recalculate only non-overridden dimensions. Tested the pure grid and placement logic separately with the local Kotlin compiler.
- **Deferred:** Android 8/12 app-drawer swipe latency and visual reimplementation/audit of Mesh transitions (triangle/arc/interlaced blinds). Those need separate branch comparison and reference tests.
- Android Gradle build and device QA have **not** been completed: the Gradle distribution is unavailable in the current environment. Do not publish this candidate before on-device verification.

## 0.42.0 — Defaults and guarded launcher resets (test candidate)

- Updated fresh-install defaults for folder fan physics, dialog opacity, app drawer frames/background, text shadows, and Environment Effects (sun rays, rain, bubbles, clouds, tumbleweed, fish and snow).
- The Security page now contains the HOME role, workspace/side-dock/bottom-dock locks, Safe Mode, and icon packs directly; the crimson Reset launcher settings actions are on a nested Security page, not in root settings.
- Added independently confirmed resets for workspace, app drawer, side dock, bottom dock and interface settings.
- Each reset explains affected data and requests confirmation; resetting all launcher settings requires a second explicit confirmation.
- On workspace/full reset the launcher releases Android widget host bindings; installed apps and user-selected external files are not deleted.
- **Does not overwrite existing preferences during normal update.** New defaults apply to fresh installation or explicit domain resets.
- **Not yet built or tested on Android devices:** verify domain isolation, widget lifecycle and complete-reset flow before public release.

## 0.41.0 — README gallery, demonstration media and publication documentation

- Prepared four silent H.264 video demonstrations and clickable static previews for the README: workspace transitions, environment effects, app drawer transitions, and docks/folders.
- Updated `docs/media/README.md` with playback, GitHub attachment and media-provenance guidance; documented the media gallery in the main README.
- Updated publication candidate metadata to `versionName = "0.41.0"` / `versionCode = 126`; **no changes to launcher Kotlin code, runtime resources or default settings**.
- This version remains an **unpublished candidate**. The updated signed APK has not yet been built or installed as an update; further default-settings refinements may follow before publication.

## 0.40.0 — First public release candidate

- Prepared the first public GitHub release from the functionally tested 0.37.30 source; only the version metadata and publication/checklist documentation were updated, with no changes to launcher Kotlin code or Android resources.
- Set `versionName = "0.40.0"` and `versionCode = 125`, preserving the existing package ID and signing configuration so Android can install it as an update.
- Confirmed the preceding signed 0.37.30 release APK passed signature, package/version, non-debuggable and non-testOnly checks, and installed over the existing Tecno launcher without loss of settings, folders or widget placements.
- Release 0.40.0 itself still requires a new signed APK build, verification and update-install smoke test before GitHub publication.
- Known non-blocking limitation: the page-curl/scroll effect retains its earlier visual behavior and will be improved in a later version.

## 0.37.30 — Restore correct left/top widget resize anchors

- Fix regression exposed in 0.37.29: resizing from the left, top or top-left corner now preserves the opposite (right/bottom) edge instead of appearing to resize from the other side.
- Base all drag geometry on the widget's dimensions and pixel position captured at drag start; do not recalculate the fixed edge from live dimensions during recomposition.
- Constrain expansion against the workspace left/top boundary without moving the fixed opposite edge.
- Preserve 0.37.29 anchored right/bottom/bottom-right resizing and consistent Canvas resize icons. No changes to widget storage, permissions or backup formats.
- Static checks only; a Gradle build and on-device verification are still required.

## 0.37.29 — Consistent resize controls and anchored bottom/right resizing

- Draw all widget resize arrows (horizontal, vertical and both diagonal corners) directly with Compose Canvas, using one stroke, head size and color on every supported device. No OEM font symbols are used.
- Keep the original top-left widget position fixed when resizing with the right, bottom or bottom-right controls. Previously recalculating position from relative workspace fractions as size changed caused widgets to drift or jump.
- Limit growth at the right and bottom boundaries rather than allowing the widget to expand beyond the page and shift it back on release.
- Ensure long-lived drag detectors use the most recent resize callbacks after recompositions and successive resize operations.
- Resize from the top and left and all widget data/backup formats remain unchanged.
- Static source checks passed; APK build and device verification remain pending.

## 0.37.28 — Device-independent vertical widget resize handles

- Render the upper and lower widget resize arrows with Compose Canvas instead of relying on the Unicode bidirectional arrow glyph. This addresses incorrect font fallback observed on Tecno Pova 6 Pro (Android 14).
- Horizontal and diagonal resize handles, resizing gestures, and widget configuration/persistence remain unchanged.
- Static source checks passed; rendering on Tecno and APK compilation still require on-device verification.

## 0.37.27 — Separate dock protection and locked-drag feedback

- Added independent editing locks for the side dock and bottom dock in Advanced Features. The previous shared lock is mapped to both locks when loading settings from 0.37.26 and earlier (including old JSON backups), preserving previous protection.
- Moving, adding and removing dock items now checks the appropriate dock's lock independently; moving between workspace and a dock still respects the workspace lock.
- Long-pressing and attempting to drag an icon inside a locked bottom or side dock displays a short locked-editing notification without changing its position. Normal tap-to-launch continues to work.
- Prevented entering the side dock's deletion mode while its editing lock is active.


## 0.37.26

- `versionCode = 120`, `versionName = "0.37.26"`.
- Show the dock-lock notification when a desktop shortcut is dropped on a locked bottom or side dock; leave the source shortcut in place. The target uses the visible dock bounds, not insertion markers.
- Show the existing top Delete drop target while dragging an entry from the bottom dock. Dropping there removes only that dock entry; protected system dock actions continue to request confirmation.


## 0.37.25

- `versionCode = 119`, `versionName = "0.37.25"`.
- Recognize app-drawer drops onto locked bottom dock using its measured bounds in root coordinates; the edit remains blocked but displays the existing localized dock-lock notification.
- Preserve normal desktop/drop behavior and independent desktop/dock lock settings; no changes to dock layout or data formats.


## 0.37.24

- `versionCode = 118`, `versionName = "0.37.24"`.
- Added localized feedback when locked desktop or dock changes are attempted, including widget long-press gestures and app-drawer drops.
- Reordering existing icons inside an open folder remains possible while its desktop/dock is locked; dragging an icon outside a locked folder now returns it to its place, keeps the folder open, and displays a message.
- Kept dock and desktop locks independent; folder membership changes remain blocked.


## 0.37.23

- `versionCode = 117`, `versionName = "0.37.23"`.
- Fixed repeated app-drawer page overview after opening it once: the one-time request is now consumed by the parent instead of replaying after every drawer recreation.
- Prevented creation of new workspace shortcuts, including app-drawer drops, while desktop editing is locked.
- Prevented widgets from entering a transient drag state while desktop editing is locked; widget interactions remain available.
- These fixes do not change page-transition effects or saved launcher settings.

## 0.37.22

- `versionCode = 116`, `versionName = "0.37.22"`.
- Added `PRIVACY.md` documenting local processing, optional Android backup,
  permissions, accessibility service and widget-provider boundaries.
- Expanded the bundled third-party attribution inventory with per-file
  Material-derived drawable information and resolved transitive Apache-2.0 components.
- Catalogued all 18 app vector drawables and launcher icon resource groups in
  `ASSET_PROVENANCE.md`.
- Renamed the two internal page-curl effect enum values to neutral names,
  `PAGE_CURL` and `PAGE_CURL_SOFT`; preserved the old serialized names when
  loading existing workspace/drawer settings or restored settings backups.
- Reworded historical Microsoft Plus! Science references in shader comments;
  distortion calculations, effect labels and animation rendering are unchanged.

## 0.37.21

- `versionCode = 115`, `versionName = "0.37.21"`.
- Removed the exported `start_safe_mode` activity extra; Safe Mode can still be enabled manually from settings or the welcome screen.
- Extended automatic Safe Mode recovery with `ApplicationExitInfo`, including native process crashes, while ignoring ordinary Android process termination.
- Added a recovery-policy v2 baseline so historical crashes from before the update do not trigger Safe Mode after installing 0.37.21.
- Removed the obsolete pending-start/process-ID recovery keys from active logic and clean them up during startup.
- Preserved manually selected Safe Mode across crash recovery; only an automatically entered recovery mode is cleared after a healthy launch.
- Release signing now reads the private keystore path from `EE_KEYSTORE_FILE`; the repository no longer contains a machine-specific absolute key path.

## 0.37.20

- `versionCode = 114`, `versionName = "0.37.20"`.
- Release-build cleanup: fixed Android string-format warnings for literal percent signs.
- Replaced deprecated `quadraticBezierTo()` calls with `quadraticTo()` without changing path geometry.
- Cleaned redundant/nullability warnings in app customization, widget editing, distortion, and mesh-retention code.
- Switched the uninstall request back to `Intent.ACTION_DELETE`; `REQUEST_DELETE_PACKAGES` remains declared so the system confirmation flow continues to work.
- Kept current status-bar and generic AppWidget-options behavior while locally suppressing only their known API deprecation warnings.
- Removed the old attribution suffix from the visible Spiral and Inside Out effect names in every locale.

## 0.37.19

- `versionCode = 113`, `versionName = "0.37.19"`.
- App drawer workspace edge swipes now perform a real interactive workspace scroll instead of triggering a page switch after a fixed distance.
- The workspace follows the finger while dragging from the physical left/right edge and settles to the nearest page on release; fast flings preserve the swipe direction.
- The existing edge-swipe option remains disabled by default.

## 0.37.18

- `versionCode = 112`, `versionName = "0.37.18"`.
- Removed the redundant top-level **App drawer** entry from the configurable app-drawer quick-action picker.
- Added an opt-in **workspace edge swipe from app drawer** interaction, disabled by default.
- When enabled, an inward swipe starting at the extreme left edge moves to the previous workspace page; an inward swipe from the right edge moves to the next workspace page while the app drawer stays open.
- The edge gesture is handled explicitly by the app drawer instead of relying on accidental gesture pass-through to the workspace pager.

## 0.37.17

- `versionCode = 111`, `versionName = "0.37.17"`.
- Added a configurable fifth app-drawer bottom-bar button. Its default action is **Page overview**.
- The quick-action button can instead open launcher settings, any app-drawer settings subsection, or one of the supported Android system-settings shortcuts.
- The selected quick action is stored in launcher preferences and is included automatically in manual settings backups.
- Page overview can now be opened directly from the bottom bar and its page cards are tappable for immediate navigation.
- System-settings quick actions use the same whitelist as widget action chains, so unsupported imported actions fall back safely.
- Finished removal of the obsolete vertical/horizontal page-overview layout modes; only **Fill available space** remains.

## 0.37.16

- `versionCode = 110`, `versionName = "0.37.16"`.
- Fixed app uninstall from the app-drawer drag action. The launcher now declares `REQUEST_DELETE_PACKAGES`, which Android requires for apps targeting API 28+ to request package removal through the system UI.
- Replaced the generic `ACTION_DELETE` intent with the explicit package-uninstall action and verify that a system handler exists before launching it.
- The actual deletion is still performed by Android and always uses the system confirmation screen.

## 0.37.15

- `versionCode = 109`, `versionName = "0.37.15"`.
- Unified app-drawer dragging: the separate **Move icons** mode/button is removed.
- Every normal long-press drag can now reorder icons, move between app-drawer pages, use Delete/Edit/Info actions, or move the app to the workspace.
- Cross-page edge zones are shown during ordinary dragging.
- Page overview now shows a clockwise countdown ring on the hovered page; it reflects the existing hover timeout without controlling it.
- Removed the **Vertical scroll** and **Horizontal scroll** page-overview layouts. Saved instances of those old modes safely fall back to **Fill available space**.
- Renamed the interaction setting from reorder hold time to drag hold time.

## 0.37.14

- `versionCode = 108`, `versionName = "0.37.14"`.
- App drawer cross-page reordering now tracks the drag pointer on the stable full-screen drawer container rather than in the source icon cell.
- Programmatic pager switches no longer inject a page-width/page-height offset into the drag coordinates, so the dragged icon should continue following both X and Y after moving to another page.
- Paged app-drawer cells now own only drag start/end; movement is supplied by the parent drawer coordinate space. Vertical-list dragging keeps its existing cell-local tracking.

## 0.37.13

- `versionCode = 107`, `versionName = "0.37.13"`.
- Fixed paged app-drawer reordering after an automatic page switch. The dragged icon now resolves the current pointer directly into launcher-root coordinates on every event instead of accumulating transformed deltas from the source page.
- Horizontal and vertical pointer movement therefore remain independent after dragging from one app-drawer page to another, so the dragged icon can follow the finger across every grid column and row before the final reorder.

## 0.37.12

- `versionCode = 106`, `versionName = "0.37.12"`.
- Switched the Gradle Wrapper from the old `9.0-milestone-1` preview to stable **Gradle 8.13** and pinned the official distribution SHA-256.
- Completed all release locale catalogs: German, French, Spanish, Brazilian Portuguese, Japanese, Simplified Chinese and Russian now contain the full base string set.
- Moved remaining welcome/protection UI text from hardcoded English into localized resources.
- Widget action-chain imports now whitelist executable Android Settings actions; unknown `INTENT_ACTION` values are discarded on load/save and blocked again before execution.
- Added a user-controlled **System backup** switch. Android Auto Backup remains available at manifest level, while a custom `BackupAgent` either delegates to normal file backup or writes no launcher data according to the setting. Existing installations default to enabled to preserve previous behaviour.

## 0.37.11

- `versionCode = 105`, `versionName = "0.37.11"`.
- Settings backup filenames now automatically include the device's current local date and time: `e_e_launcher_settings_YYYY-MM-DD_HH-mm-ss.json`.
- Completed the one-release Sun rays migration: the project-owned effect now uses the `SUN_RAYS` enum directly and the temporary `INDEPENDENT_RAYS` compatibility code has been removed.

## 0.37.10

- `versionCode = 104`, `versionName = "0.37.10"`.
- Transitional Sun rays settings migration: existing `INDEPENDENT_RAYS` preferences are copied to `SUN_RAYS` on first load.
- The migrated `INDEPENDENT_RAYS` block deliberately overwrites any stale legacy `SUN_RAYS` settings from the removed third-party shader, so the currently tested project-owned ray settings win.
- `INDEPENDENT_RAYS` is removed from the persisted environment-effects JSON after migration and is no longer written on save. The enum name remains only internally for this release and can be removed/renamed in the next version.

## 0.37.9

- `versionCode = 103`, `versionName = "0.37.9"`.
- Removed the legacy third-party **Sun rays** shader, its old renderer branch, controls and licensing hold. The tested project-owned AGSL ray renderer is now the only Sun rays effect.
- Added per-widget **action chains** in Widget settings. Existing placed widgets are scanned and can be assigned individually or in groups to one shared chain.
- A chain can intercept the whole widget or a custom rectangular click area configured by X/Y/width/height percentages. Outside a custom area the widget keeps its normal provider interaction.
- Chain actions can open applications or run launcher actions, multiple actions can be ordered and are executed sequentially.
- Removed the former global widget fallback tap handler so custom tap behaviour now applies only to explicitly selected widget instances.
- Widget action chains use the launcher's stable `WorkspaceWidget.id`, so they survive Android `appWidgetId` rebinding after widget restore.

## 0.37.8

- `versionCode = 102`, `versionName = "0.37.8"`.
- **Sun rays (independent)**: appearance/fade animation is about four times faster than before.
- Individual rays now use a full 0–100% visibility envelope instead of a small brightness pulse, so they can actually fade out and reappear.
- Ray position, angle, thickness and the GIMP-reference transverse profile are unchanged.

## 0.37.7

- `versionCode = 101`, `versionName = "0.37.7"`.
- Reworked **Sun rays (independent)** after comparing the launcher output with supplied thin/wide GIMP ray references.
- Removed the 16 discrete transverse colour bands. Each ray is now rendered by a continuous AGSL distance-to-axis profile.
- The complete colour and opacity profile is normalized by the selected thickness in pixels, so increasing thickness scales the whole beam proportionally. The falloff curve was tuned against the supplied GIMP thin/wide references, whose transverse profiles remain almost identical after width normalization.
- Wide rays no longer become flat one-colour translucent polygons; centre, colour transition, edge fade and opacity profile all expand together.
- Longitudinal fade remains proportional to ray length, avoiding hard clipping at the configured height.

## 0.37.6

- `versionCode = 100`, `versionName = "0.37.6"`.
- **Sun rays (independent)**: corrected the two-colour cross-section for thick rays.
- The colour transition is now normalized to the ray width, so increasing thickness scales the complete tube-like colour profile proportionally instead of expanding a single-colour centre.
- Primary colour forms the centre and transitions symmetrically to the secondary colour at both sides.
- Blur remains a separate post-effect and is not driven by ray thickness.

## 0.37.5

- `versionCode = 99`, `versionName = "0.37.5"`.
- **Sun rays (independent)**: **Ray thickness** is now measured in real pixels instead of percent.
- Increasing thickness now widens the actual visible body of each ray. It no longer works like extra blur.
- Blur remains a separate setting and only affects softness/feathering of the beam edges.
- The beam renderer now draws a solid core plus a narrow blur-dependent halo, so thick rays look wider rather than simply more diffuse.

## 0.37.4

- Reworked **Sun rays (independent)** so ray count and beam thickness are fully independent controls.
- Replaced the old 4–80 density range for the independent effect with an explicit **Ray count** range of 1–20; the selected value is now exactly the total number of visible beams.
- Beam width is now based on a fixed fraction of the display width and no longer uses `screenWidth / rayCount`, so changing ray count cannot make every beam thinner or thicker.
- Replaced the former 14 horizontal trapezoid slices with continuous full-length gradient beams. This removes the translucent horizontal/stepped rectangles that appeared when density and thickness interacted.
- **Ray thickness** now scales each individual beam directly while retaining soft side gradients and end fading.
- Fresh independent-ray settings default to 12 rays; existing saved values are preserved and clamped to the new 1–20 UI range.
- `versionCode = 98`, `versionName = "0.37.4"`.

## 0.37.3

- Added visible **Ray thickness** control to **Sun rays (independent)**, using the
  existing persisted thickness parameter with a 25–400% range.
- Added **Maximum deviation angle** (0–75°) for independent sun rays. `0°` keeps
  the beams vertical; larger values widen the allowed cone symmetrically.
- Reworked independent-ray direction generation so animation changes a bounded
  angle rather than an unrestricted target X position, reducing the laser-show
  appearance while preserving motion.
- `versionCode = 97`, `versionName = "0.37.3"`.

## 0.37.2

- Repaired saved application references after package updates. If an Android update
  changes a launcher Activity or activity-alias, workspace, dock and folder entries
  are reconnected to the current launcher component of the same package.
- Added extra delayed application-list refreshes for `PACKAGE_ADDED`,
  `PACKAGE_REPLACED`, package changes and matching `LauncherApps` callbacks, avoiding
  stale empty folder icons during in-place application updates.
- Added **Keep shortcuts after app removal** (enabled by default) under App drawer
  settings. When enabled, missing-app placeholders are preserved and become functional
  again after reinstall. When disabled, a real uninstall removes that package from
  workspace shortcuts, side/bottom docks and folder contents. Package replacement is
  explicitly excluded from removal handling.
- Migrated hidden-app, custom-order and per-app customization component references when
  a package update changes its launcher component.
- Settings backup format v3 now keys saved App Widget options by the launcher's stable
  `WorkspaceWidget.id`, not only by Android's temporary `appWidgetId`. After restore,
  missing widget bindings receive one automatic rebind attempt with the original page,
  position and size preserved; provider-specific widget options are applied to the new ID
  before a configuration activity is opened.
- Added handling for Android's `ACTION_APPWIDGET_HOST_RESTORED` old-ID → new-ID mapping,
  and conservative cleanup of genuinely orphaned IDs still owned by `AppWidgetHost`.
- Added `*.key` and `keystore.properties` to `.gitignore` alongside the existing keystore
  patterns; signing secrets and keys remain outside the source tree.
- `versionCode = 96`, `versionName = "0.37.2"`.

## 0.37.1

- Fixed the icon-activation settings crash on Android 26+ by loading the actual
  installed launcher icon through `PackageManager` instead of passing an adaptive
  icon XML to Compose `painterResource`.
- Enlarged the pinned activation preview so the 400% wireframe animation can be
  inspected without being clipped by the preview card.
- Made the wireframe visibly reach 400%, hold that size, and fade only afterward;
  removed unnecessary ancestor clipping around several launcher icon containers.
- Extended launch activation timing to side-dock and bottom-dock apps, folders,
  settings shortcuts, launcher actions, and non-application workspace shortcuts;
  workspace folder icons now animate when opened as well.
- Reworked cross-page app-drawer drag deltas by converting current and previous
  pointer positions through the same current `LayoutCoordinates` transform, so
  page transforms no longer turn vertical motion into horizontal drift.
- Softened **Sun rays (independent)** by fading nested beam edges and fading the
  beam toward its configured endpoint instead of cutting it off sharply.
- Made private release signing optional during Gradle configuration. Dependency
  reports can now run without `EE_KEYSTORE_PASSWORD` / `EE_KEY_PASSWORD`; when the
  local keystore and credentials are available, development/release builds retain
  the private project signature.
- `versionCode = 95`, `versionName = "0.37.1"`.

## 0.37.0

- Added repository licensing/provenance documentation for the planned
  `GPL-3.0-or-later` code/resources and `CC BY-SA 4.0` documentation/artwork model,
  including Apache-2.0 third-party notices and explicit AI-assisted asset provenance.
- Kept the legacy Sun rays shader unchanged for testing, but marked its upstream
  license as unresolved and added a separately designed **Sun rays (independent)**
  renderer using the same visible ray controls.
- Reworked **Icon, then wireframe** so the second phase extracts image edges from
  rendered icon content instead of drawing a generic rounded icon frame.
- Added a pinned, tappable launch-animation preview to the icon-activation settings.
- Fixed paged app-drawer dragging after an automatic page switch by accumulating
  real pointer deltas after discarding the one coordinate jump caused by the page move.
- Application discovery now retries up to ten times, waiting 200 ms after each
  unsuccessful attempt, before showing the manual retry/error UI.
- `versionCode = 94`, `versionName = "0.37.0"`.

## 0.36.1

- Added a configurable new-app cutoff date, with automatic defaults for launcher installation, launcher updates, and older backups without a saved cutoff.
- Added widget tap action settings: the widget handles the tap first, then a configurable delay can open the provider application only when no response is observed.
- Split icon activation into launch and drag profiles, linked launch delay to animation duration by default, and added the 200%-icon/400%-wireframe effect.
- Added a legacy edge-switch option for app-drawer reordering, kept the drag overlay in finger coordinates across page changes, and added simple or miniature page previews.
- Preserved bound widget option bundles during settings backup and restore.
- Added a configurable all-pages overview for app-drawer reordering that keeps the dragged icon under the finger.
- Added configurable new-app emblems, including an animated flame, geometric shapes, gradients, custom images and icon-pack assets.
- Added configurable icon-animation duration and an optional launch delay that is ignored when animation is disabled.
- Re-applied edge-to-edge mode after launcher restart and configuration restore.
- Fixed accumulated workspace-icon drag lag and kept the app drawer hidden after transferring an icon out of it.
- Increased configurable bottom-dock capacity to 1–10 icons (default 7), with full-dock folder drops and guidance.
- Added side-dock insertion markers, scrolling, folders, and the existing icon context menu for stationary dock drags.
- Added configurable two- and three-finger directional gestures and made the workspace-overview pinch action configurable.
- Corrected launcher-background and behind-environment-effect layering and limited slider dialog transparency to app-drawer icon settings.
- `versionCode = 93`, `versionName = "0.36.1"`.

## 0.35.32

- Moved the Mesh idle-preview switch from the effect picker to the parent Desktops settings page, leaving more vertical space for the effect list on compact screens.
- `versionCode = 91`, `versionName = "0.35.32"`.

## 0.35.31

- Added a default idle-time Mesh snapshot mode: the active desktop page is captured after one second without interaction, so a fast swipe can begin immediately with a prepared texture.
- Added a setting that restores the previous behaviour and captures the outgoing page when the swipe starts.
- App-drawer icon effects are now suspended while an Android Mesh page effect is active, preventing mirrored icons on the incoming page.
- Added a pinned red compatibility warning to the app-drawer icon-effect settings when Android Mesh is selected.
- `versionCode = 90`, `versionName = "0.35.31"`.

## 0.35.30

- Captured visible widget frames synchronously at the first horizontal workspace swipe movement,
  before the outgoing page is replaced by its transition texture.
- Kept the subsequent page-texture capture after the widget's newly published frame.
- Handled multiple active virtual views of a widget on looping pages.
- `versionCode = 89`, `versionName = "0.35.30"`.

## 0.35.29

- Refresh outgoing workspace page and widget textures after the first rendered swipe frame,
  avoiding a retained snapshot of the previous widget state during transitions.
- Force overview drag ghosts and miniature cells to retain thumbnail dimensions.
- Ignore desktop swipes starting on a widget by default, while keeping the setting adjustable.
- `versionCode = 88`, `versionName = "0.35.29"`.

## 0.35.28

- Kept overview shortcut and widget drag previews the same size as their page thumbnails.
- Added a horizontal insertion marker for vertical reordering in the side dock.
- Kept the side dock open while a workspace icon drag begins outside it; taps outside still close it.
- Added a saved setting to ignore workspace vertical gestures that begin on a widget.
- Captured transition textures when swiping starts and removed them from the cache when swiping ends.
- Stopped drawing retained widget snapshots behind live translucent widgets.
- `versionCode = 87`, `versionName = "0.35.28"`.

## 0.35.27

- Expanded workspace-to-bottom-dock dropping to the full dock page and based
  acceptance on the real finger position instead of the dragged card centre.
- Added a live vertical insertion marker while dragging a workspace shortcut
  into the bottom dock; the marker and final page-local insertion share the
  same slot calculation.
- Added a configurable 300–2000 ms bottom-dock folder-creation delay (800 ms
  by default). Ordinary reordering remains active until the delay completes.
- Folder activation now requires the dragged icon to remain within an 8 dp
  stationary area. Moving outside it cancels the pending/ready folder action
  and starts a fresh delay over the next eligible target.
- Added a semi-transparent folder preview at the target position once folder
  creation is armed; the insertion line is hidden while this preview is active.
- Added all new settings text to every supported locale.
- `versionCode = 86`, `versionName = "0.35.27"`.

## 0.35.26

- Changed the preset `Messages` action from `ACTION_SENDTO` with an empty
  `smsto:` address, which opened the new-message composer, to the launcher main
  activity of the current default SMS application.
- Added the Android messaging-category selector as a fallback when the default
  SMS package does not expose a conventional launcher activity.
- Applied the same main-screen behaviour to `Messages` in the bottom and side
  docks. Tapping the preset now opens the conversation list instead of starting
  a new message.
- `versionCode = 85`, `versionName = "0.35.26"`.

## 0.35.25

- Removed the dragged bottom-dock cell from insertion-slot calculation before
  determining both the marker position and the final page-local index.
- Eliminated the phantom source slot that skipped a gap during right-to-left
  moves and drew the marker one position to the right of the actual result.
- Every gap in the remaining row can now be selected independently. In a
  five-item dock, moving the fourth item can target both the gap between items
  1–2 and the gap between items 2–3, with the marker matching the final order.
- Kept page-local rendering from 0.35.24 and all existing folder, workspace,
  and page-transfer drop actions.
- `versionCode = 84`, `versionName = "0.35.25"`.

## 0.35.24

- Fixed the bottom-dock insertion marker being drawn in the outer dock
  container while its slot was measured in the pager page coordinate space.
- The vertical marker is now rendered inside the same measured page as the dock
  icons. Its visible gap therefore matches the page-local insertion index used
  when the icon is released, including moves from the fourth slot into the gap
  between the first and second icons.
- Kept the 0.35.23 slot calculation, folder-hover handling, and edge/page
  transfers unchanged.
- `versionCode = 83`, `versionName = "0.35.24"`.

## 0.35.23

- Added a vertical insertion line while reordering icons and folders on the
  current bottom-dock page. It supports the slot before the first item, every
  gap between items, and the slot after the last item.
- Based the dock indicator on the measured on-screen bounds of the visible dock
  cells, so it remains accurate with left, centre, and right alignment.
- Made the final drop use the exact page-local insertion index shown by the
  line, including drops into the empty space between dock items.
- Kept edge page transfer and upward workspace transfer unchanged. The line is
  hidden when either transfer is active and when a held app target is ready to
  create or accept a folder.
- Reused the existing insertion-line colour and thickness settings so folder
  and dock reorder indicators have one consistent appearance.
- `versionCode = 82`, `versionName = "0.35.23"`.

## 0.35.22

- Replaced event-count-based folder-fan slowing with distance-based motion, so
  touch sampling frequency can no longer change the chain speed.
- Kept the configured initial speed unchanged until the last icon has enough
  trajectory behind it to leave the folder; retention now starts after that
  point and slows the whole chain together.
- Placed every icon at a fixed arc-length interval on one shared pointer path.
  Icons can no longer accelerate to catch the pointer, overtake each other, or
  collapse together after the full chain has been extracted.
- Removed the fixed 960-point visible-trail limit that made the ends of large
  folders share one position. The measured pointer path now retains the full
  trajectory and uses cumulative distances with binary-search sampling.
- Applied the same motion model to workspace and bottom-dock folders.
- `versionCode = 81`, `versionName = "0.35.22"`.

## 0.35.21

- Replaced the folder reorder calculation based on fixed `210 dp` and `64 dp`
  offsets with the measured on-screen bounds of every folder item.
- Added a horizontal insertion line that follows the pointer and shows the exact
  slot where the dragged icon will be placed, including before the first and
  after the last item.
- Made the insertion line work in both workspace folders and bottom-dock folders
  and account for folder-dialog scrolling and variable content height.
- Added insertion-line colour and thickness settings to the `Folders` settings
  page. Defaults are `#9BB2FF` and `3 dp`; thickness can be set from 1 to 12 dp.
- `versionCode = 80`, `versionName = "0.35.21"`.

## 0.35.20

- Kept workspace shortcuts in stable composition order while dragging and keyed
  each shortcut by its persistent ID. Long-pressing an item that was not last on
  the page no longer replaces its pointer-input node and cancels the gesture.
- Applied the same stable-key fix to workspace folders, which used the same
  drag-time reordering pattern.
- Active shortcuts and folders still rise above other items through their
  existing elevated `zIndex`; list reordering is no longer required.
- `versionCode = 79`, `versionName = "0.35.20"`.

## 0.35.19

- Renamed the four inside/outside face-rotation effects to `Shutters inward` and
  `Shutters outward`, keeping their horizontal and vertical variants and saved
  enum values unchanged.
- Tracked whether `AppWidgetHostView` is displaying Android's default/error
  fallback instead of provider content. A retained good widget frame now covers
  that grey fallback until valid `RemoteViews` return.
- Prevented the grey disabled widget fallback from being captured and replacing
  the last known good widget snapshot.
- Added fallback-state and skipped-capture entries to `EELauncherDiag` for the
  next Motorola widget test.
- Added a `Restart launcher` button to the root settings screen. It recreates the
  launcher Activity and all hosted widget views without resetting settings.
- `versionCode = 78`, `versionName = "0.35.19"`.

## 0.35.18

- Made each `MeshRenderView` strongly retain the complete `Mesh`, `Paint`, and
  `BitmapShader` referenced by its current display list. Replaced meshes remain
  in a one-second process-wide tail, while the final mesh of a detached view is
  retained for ten seconds for delayed RenderThread work on affected Android 14
  HWUI builds.
- Expanded `EELauncherDiag` Mesh entries with view ID, Mesh ID, transition
  progress, retention reason, queue size, and hold duration.
- Removed the high- and low-density diagnostic Sphere entries. The remaining
  `Sphere` now uses the tested 32×36 grid; saved diagnostic Sphere choices migrate
  to it automatically.
- Removed the Android Mesh `Slide reveal` effect; saved selections migrate to the
  default workspace transition or Crossfade in the app drawer.
- Kept the last 6% of the four Mesh face-rotation effects for Pager settling after
  finger release, preventing their old page from freezing before it disappears.
- `versionCode = 77`, `versionName = "0.35.18"`.

## 0.35.17

- Added a diagnostic process-wide retainer for Android Mesh draw objects. Each
  `Mesh`, together with its `Paint` and `BitmapShader`, remains strongly reachable
  for 1 second after `Canvas.drawMesh()` records it, including when Compose disposes
  the originating `AndroidView`.
- Added throttled `EELauncherDiag` logcat entries with the active Mesh effect, the
  number of retained draw objects, and the configured retention duration. This
  allows a later native crash to be associated with the effect that was rendering.
- Kept all effect geometry and the three diagnostic Sphere densities unchanged so
  the Tecno test isolates object lifetime from mesh complexity.
- `versionCode = 76`, `versionName = "0.35.17"`.

## 0.35.16

- Replaced the `(2D)` / `(3D)` effect-name prefixes and the visible `(Oreo)`
  suffix with three renderer-based sections: Pager / Compose graphicsLayer,
  Compose Canvas, and Android Mesh.
- Made every renderer section independently collapsible and persisted its expanded
  state for both the workspace and app-drawer effect pickers.
- Kept search useful across collapsed sections: matching sections temporarily
  expand and renderer names are included in the search.
- Added three clearly marked diagnostic Sphere variants using 64×72, 32×36, and
  24×27 segment grids. All three keep the same Sphere geometry and differ only in
  Mesh density.
- `versionCode = 75`, `versionName = "0.35.16"`.

## 0.35.15

- Fixed the first-null widget update path: when no retained RemoteViews or snapshot
  exists, `AppWidgetHostView` now inflates the provider's declared initial layout
  instead of remaining completely empty.
- Kept the last good widget frame across later transient null updates, while using
  Android's normal error view when no retained frame exists so failures remain
  visible and diagnosable.
- Removed manual `ACTION_APPWIDGET_UPDATE` broadcasts. System logs confirmed that
  this protected broadcast was rejected and could not wake restricted providers.
- Consumed Back on the bare HOME screen. It no longer finishes and immediately
  recreates the launcher Activity and all hosted widgets.
- Added general widget-provider background-restriction guidance to `README.md` and
  detailed confirmed Motorola Edge 60 and Tecno Pova 6 Pro results to
  `TESTED_DEVICES.md`.
- Added `tools/install_preserving_accessibility.sh` for ADB test installs that
  preserve the existing e_e launcher accessibility-service state without replacing
  other enabled accessibility services.
- Extended the diagnostics collector with an opt-in `--full-logcat` mode for OEM
  provider kills that do not appear under the launcher's focused log tag.
- Kept all eight distortion effects unchanged after their successful device test.
- `versionCode = 74`, `versionName = "0.35.15"`.

## 0.35.14

- Replaced the invisible Tecno `RuntimeShaderEffect` overlay for Fish eye, Minify,
  Wobble, Water, Black hole, Spiral, and Inside Out with an explicit captured
  bitmap supplied to AGSL as a `BitmapShader`.
- Kept Magnifier on its verified Compose clipped-layer renderer and kept the
  ordinary launcher independently visible under every distortion mode.
- Removed the old RenderEffect overlay that could leave a narrow artifact below
  the status bar when its OEM child shader was only partially valid.
- Changed automatic safe-mode recovery to require a recorded uncaught crash.
  Ordinary early process termination no longer enables safe mode, and automatic
  recovery is limited to one launch. The first 0.35.14 start clears a flag left by
  the old pending-start recovery; later settings and ADB safe mode remain persistent.
- Let a newly created widget host inflate the provider's declared initial layout
  while waiting for the first RemoteViews update; retained widgets still suppress
  the transient disabled grey fallback after a provider restart.
- Added focused `EELauncherDiag` logging for distortion and widget stages plus
  `tools/collect_launcher_diagnostics.sh` for logcat, exit-info, AppWidget state,
  graphics frame statistics, device properties, and optional bugreport capture.
- `versionCode = 73`, `versionName = "0.35.14"`.

## 0.35.13

- Enabled Fish eye, Minify, Wobble, Water, Black hole, Spiral, and Inside Out in
  the Distortion settings alongside Magnifier.
- Kept Magnifier on the verified Compose clipped-layer path.
- Applied the other distortion shaders only to a separately recorded overlay
  while the normal workspace remains independently visible underneath.
- Changed the AGSL output outside moving shapes to transparent, so the overlay
  replaces pixels only inside the selected distortion regions.
- Preserved the chosen distortion type when saving settings and when a renderer
  failure automatically disables the effect.
- Added full safe-mode startup, automatic recovery, multi-device ADB, and exit
  instructions to `README.md`.
- `versionCode = 72`, `versionName = "0.35.13"`.

## 0.35.12

- Fixed the Magnifier source compilation errors by avoiding a local `clipPath`
  name collision and importing the Compose `clipPath` and `scale` draw helpers.
- `versionCode = 71`, `versionName = "0.35.12"`.

## 0.35.11

- Replaced the full-surface RuntimeShader path for Magnifier after a Tecno device
  accepted the shader but supplied a transparent RenderEffect input, leaving only
  the live wallpaper visible.
- The normal launcher is now always drawn directly. Magnifier records a second
  copy through Compose `rememberGraphicsLayer` and redraws only the clipped,
  enlarged regions over the visible desktop.
- Kept one to five elements, all folder shapes, all motion directions, size,
  strength, speed, and bounce as the default direction.
- Kept the complete AGSL implementations of Fish eye, Minify, Wobble, Water,
  Black hole, Spiral, and Inside Out in source while their settings remain disabled.
- Added renderer failure fallback around layer capture and lens overlay drawing;
  a failure leaves the directly rendered desktop visible and disables Distortion.
- `versionCode = 70`, `versionName = "0.35.11"`.

## 0.35.10

- Re-enabled the `Distortion` environment effect in a limited test stage with
  Magnifier as the only selectable and executable distortion type.
- Kept Fish eye, Minify, Wobble, Water, Black hole, Spiral and Inside Out in the
  source and settings list, but disabled their controls until they are validated.
- Replaced the unsupported AGSL remainder operators that caused the original
  RuntimeShader compilation crash.
- Added guarded RuntimeShader, RenderEffect and uniform setup. If any of these
  stages fails, the launcher keeps rendering normally and automatically disables
  Distortion instead of entering a startup crash loop.
- Normalized old or malformed saved distortion types to Magnifier while this test
  limitation is active.
- `versionCode = 69`, `versionName = "0.35.10"`.

## 0.35.9

- Fixed procedural Sun rays on tall portrait displays by normalizing shader
  coordinates independently against screen width and height instead of using the
  width as the complete distance limit.
- Corrected premultiplied-alpha output so ray strength and the configured opacity
  are no longer applied twice during composition.
- Added startup health tracking. If the launcher fails during its first four
  seconds, the following launch enters persistent safe mode before Compose effects
  are initialized.
- Added the `start_safe_mode` activity extra for an explicit safe-mode launch through
  ADB or another trusted launcher entry point.
- Made safe mode disable every Environment Effect in addition to page and icon
  animations.
- `versionCode = 68`, `versionName = "0.35.9"`.

## 0.35.8

- Temporarily disabled the `Distortion` environment effect after it was found to
  crash the launcher.
- Hidden `Distortion` from environment-effect settings while retaining its enum
  and saved option fields for future compatibility.
- Force saved `Distortion` selections to disabled during loading and saving so an
  installation over 0.35.7 can start without clearing launcher data.
- Added a renderer-level safety gate that bypasses RuntimeShader creation even if
  an old or malformed configuration still marks `Distortion` as enabled.
- `versionCode = 67`, `versionName = "0.35.8"`.

## 0.35.7

- Added `Lock screen` as an assignable desktop gesture action. It uses the
  launcher's existing system-gesture accessibility service and requests access
  when that service is not yet enabled.
- Added the `Distortion` environment effect. One to five moving pixel-distortion
  regions can use every folder shape, every environment direction, configurable
  size/strength/speed, and bounce from screen edges by default.
- Added Magnifier, Fish eye, Minify, Wobble, Water, Black hole, Spiral and Inside
  Out distortion modes. Spiral and Inside Out reproduce the lens idea from the
  Microsoft Plus! Science screen saver.
- Applied distortion as an Android 14 RuntimeShader/RenderEffect over the rendered
  launcher surface, so wallpaper, shortcuts, folders, widgets and docks are sampled
  and genuinely deformed beneath each moving element.
- Replaced the line-based Sun rays renderer with an AGSL port of the selected
  procedural Light Rays shader: two off-screen sources, cosine-shaped animated
  bands, horizontal source movement and depth attenuation.
- Kept only the Sun rays controls that match the procedural algorithm: speed,
  vertical reach, density, two colours, opacity and blur.
- Expanded the accessibility-service explanation to cover both notification-shade
  and screen-lock actions without requesting screen-content access.
- `versionCode = 66`, `versionName = "0.35.7"`.

## 0.35.6

- Changed every Sun rays source to emit one original ray. Additional rays are
  parallel copies with the same direction, angle, length, colour, and thickness,
  placed alternately to the left and right of the original.
- Added a Sun rays copy-width control that defines the complete horizontal range
  occupied by the parallel copies as a percentage of the screen width.
- Assigned every copied ray a stable independent opacity while keeping the global
  opacity control as a common multiplier for the complete group.
- Allowed one ray per source as the minimum rays-per-source value.
- `versionCode = 65`, `versionName = "0.35.6"`.

## 0.35.5

- Added Sun rays controls for vertical reach, source count, rays per source, and
  ray thickness.
- Gave every Sun rays source an independent fade phase and cycle duration so the
  sources no longer appear and disappear together.
- Positioned the app-drawer `To desktop` drop field above the system navigation
  area and matched hit testing to that inset.
- Restored the compact 48 dp height of the app-drawer Delete, Edit, and Information
  drag targets.
- `versionCode = 64`, `versionName = "0.35.5"`.

## 0.35.4

- Rebuilt Sun rays as five independently moving sources positioned above the visible
  screen. Only their lower 270-degree sector (plus or minus 30 degrees) is drawn, so
  the source points and all unused rays remain outside the visible composition. The
  source count is fixed at five and is reported as such in the effect summary.
- Increased the default Sun rays blur from 4 dp to 6 dp.
- A tapped fish now receives a new random 4x-10x speed multiplier and keeps its
  direction until it leaves the screen, then returns to its normal random speed.
- Doubled the app-drawer `To desktop` drop field height and attached its lower edge
  directly to the bottom screen edge; hit testing uses the same 96 dp region.
- Limited the folder's automatic scroll to one completed action per opening.
- `versionCode = 63`, `versionName = "0.35.4"`.

## 0.35.3

- Fixed the density type collision in the fish renderer that caused compilation errors in `EnvironmentEffects.kt`.
- `versionCode = 62`, `versionName = "0.35.3"`.

## 0.35.2

- Kept the app-drawer controls above the shared app-drawer background by assigning
  the bottom control row an explicit layer above the drawer content.
- Made fish taps observable without intercepting launcher gestures, including when
  environment effects are configured behind launcher elements.
- A tapped fish now keeps its current direction at double speed until it has fully
  left the screen, then returns to its original random speed and normal turning.
- Replaced the broad triangular sun shafts with a moving supernova-style source made
  from many fine radial rays with independently varying lengths and brightness.
- `versionCode = 61`, `versionName = "0.35.2"`.

## 0.35.1

- Enabled `BuildConfig` generation so the current app version can be displayed in launcher settings.
- `versionCode = 60`, `versionName = "0.35.1"`.

## 0.35.0

- App-drawer page effects now use the same fixed preview, compact search and undo controls as desktop effects.
- App-drawer effects apply immediately; undo restores the effect active when the page was opened without leaving settings.
- The app-drawer icon area now reserves the measured bottom-control height instead of a device-dependent fixed value.
- The app-drawer background extends under the bottom controls; the separate rounded dock-style control card was removed.
- The current application version is always rendered after all root launcher-settings items.
- Added the `Scroll wallpaper` setting for scrollable system and live wallpapers.
- Added the environment effects `Sun rays` and `Fish`.
- Fish have independent random speeds, turn on their own, can leave the screen and double their speed when tapped.
- Up to ten custom fish pictures can be selected and are included in settings backup through the existing preferences backup.
- `versionCode = 59`, `versionName = "0.35.0"`.

## 0.34.2

- Shared captured Mesh textures between virtual copies of the same logical page,
  preventing cyclic workspace and app-drawer transitions from losing an incoming
  effect or falling back to a partial fade at the first/last-page boundary.
- Kept the outgoing texture role stable for the complete pager gesture.
- Made the inline workspace/app-drawer effect preview cyclic in both directions.
- Changed the workspace effect undo control to restore the effect active when the
  screen was opened without navigating to the parent settings page or clearing search.
- `versionCode = 58`, `versionName = "0.34.2"`.

## 0.34.1

- Reduced the fixed workspace-effect search control from the standard 56 dp height
  to a compact 28 dp row.
- Moved reversible cancellation beside the search field and replaced its text with
  a localized rounded undo-arrow control. It still restores the effect that was
  active when the effect screen was opened.
- Added a localized `Preview` label inside the lower-left corner of the workspace
  effect preview.
- Reversed only the new shared flat-faced Mesh cube's gesture direction so its
  rotation follows left and right swipes correctly.
- `versionCode = 57`, `versionName = "0.34.1"`.

## 0.34.0

- Moved the interactive workspace-effect preview above the scrollable effect list.
- Removed the workspace preview hint and added a localized effect search field directly
  below the preview.
- Workspace effects now apply immediately when selected. The centered Cancel button
  restores the effect that was active when the effect screen was opened.
- Added a new shared Mesh cube whose pages remain flat cube faces without cylindrical
  or spherical distortion. It is listed immediately before Cylinder.
- Renamed the original compatible cube to `Cube (obsolete)` without changing its
  persisted enum identifier, so existing settings remain valid.
- `versionCode = 56`, `versionName = "0.34.0"`.

## 0.33.1

- Fixed the app-drawer system-height toggle call so its change handler is passed
  to `onCheckedChange` instead of being interpreted as the `enabled` argument.
- `versionCode = 55`, `versionName = "0.33.1"`.

## 0.33.0

- Added independent vertical offsets from -32 to +32 dp for bottom-dock icons and
  each dock page's shelf.
- Positioned the workspace page indicator above whichever dock element is higher,
  with a configurable 0-32 dp gap and an option to hide the indicator completely.
- Added an option to hide bottom-dock labels while preserving their layout space.
- Made the app drawer more vertically compact and adjusted its grid and drag areas
  to use the released space.
- Added automatic app-drawer card sizing, enabled by default, so the selected grid
  fills all available horizontal and vertical space.
- Added an optional manual `screenHeightDp` override for app-drawer grid calculations;
  the Android-reported value remains the default.
- Added `(2D)` and `(3D)` prefixes and grouped all page effects by dimension.
- Added stable Oreo implementations as separately named `(Oreo)` alternatives,
  including the stationary-page flying rectangles, flat burning, blinds, reveal,
  cube, card, and vortex variants.
- Kept the failed experimental `drawBitmapMesh()` restoration set out of the main
  branch.
- Kept the workspace effect preview and its Apply control permanently visible while
  the effect list scrolls independently.
- `versionCode = 54`, `versionName = "0.33.0"`.

## 0.32.0

- Added a full-screen overview for workspace pages, opened from the workspace menu,
  workspace settings, or a two-finger pinch.
- Added navigation-only and editing overview modes. Editing mode supports page
  reordering, default-page selection, page creation/deletion, and moving shortcuts,
  folders, and widgets between page thumbnails.
- Non-empty page deletion moves its contents to an adjacent page after confirmation.
- Added a shared interactive page-effect preview that uses the launcher's real effect
  engine for both workspace and app-drawer page effects.
- Added an optional live preview on the real workspace or app-drawer pages, with
  explicit Apply and Cancel controls and no persistent change before confirmation.
- Added bottom-dock folders with the same content, appearance, icon, renaming,
  reordering, extraction, and automatic single-icon dissolution behavior as workspace
  folders.
- Added whole-folder movement between the workspace and bottom dock, folder creation
  by holding one dock app over another, insertion into an existing dock folder, and
  movement between dock pages.
- Extended the existing bottom-dock JSON format without invalidating older settings or
  backup files, including an explicit folder marker so even an empty transferred folder
  remains a folder.
- `versionCode = 53`, `versionName = "0.32.0"`.

## 0.31.1

- Removed the incompatible standalone Compose `weight` import.
- Restored widget dragging through the edit-mode input shield while keeping widget
  content inactive.
- Allowed widgets to reach the top edge beneath the edit controls.
- `versionCode = 52`, `versionName = "0.31.1"`.

## 0.31.0

- Added the Android HOME role request through `RoleManager`.
- Added a skippable first-run welcome screen and safe-mode entry.
- Added desktop editing and dock modification locks.
- Added a launcher-wide icon pack choice with system-icon fallback.
- Included advanced launcher preferences in settings backup and restore.

## 0.30.0

- Updated fresh-install defaults: system wallpaper, left side dock, five workspace pages with page 3 selected, horizontal app-drawer pages, 80% dialog opacity with shadow, 70% side-dock background, and a 40 dp curved bottom shelf at 20% opacity with shadow.
- Excluded the search row from horizontal app-drawer grid-size calculations.
- Widgets now start at the provider-proposed size after correct px-to-dp conversion.
- Widget content cannot activate while the green move/resize frame is visible.
- Added JSON settings backup and restore through Android's document picker.
- Folder dialogs open scrolled to the name and contained icons.
- A folder containing one remaining icon is replaced by that icon at the folder position.
- `versionCode = 49`, `versionName = "0.30.0"`.

## 0.29.4

- Fixed the folder reorder callback to use item indices consistently.
- Fixed the bottom-dock live reorder callback to use stable entry IDs consistently.
- Restored the missing bottom-dock root coordinate state used by the drag overlay.
- `versionCode = 48`, `versionName = "0.29.4"`.

## 0.29.3

- Added a universal globe symbol beside the Language entry so it remains identifiable after an accidental language change.
- Removed the bottom dock's visual clipping during drag and made the dragged icon follow the finger instead of remaining in its original cell.
- Added live bottom-dock reordering: neighboring icons immediately close the source gap and open the prospective destination slot.
- Removed the fixed 56 dp upper workspace inset so widgets can be positioned at the top edge of the desktop.
- Replaced the wide Delete widget button with a compact circular trash icon.
- Changed widget drag-to-delete hit testing to require the finger itself to enter the trash target; overlap by a large widget no longer deletes it.
- `versionCode = 47`, `versionName = "0.29.3"`.

## 0.29.2

- Added complete German, French, Spanish, Brazilian Portuguese, Japanese, and Simplified Chinese resource catalogs alongside the existing English and Russian interfaces.
- Added an in-launcher language page with System language, English, Russian, German, French, Spanish, Brazilian Portuguese, Japanese, and Simplified Chinese choices.
- Applied language changes through Android's per-application `LocaleManager`, so the selected language is independent of the phone locale and persists through process restarts.
- Kept only the requested release locales while retaining `en-XA` and `ar-XB` for debug-only layout testing.
- Localized the remaining hard-coded Environment effects settings title.
- Extended `tools/check_localization.py` to validate every release catalog, resource key, numbered format placeholder, and unfinished translation marker.
- Corrected the release archive identity from the obsolete `AnimLauncher` filename to `e_e_launcher`.
- `versionCode = 46`, `versionName = "0.29.2"`.

## 0.29.1

- Enabled Android's accented `en-XA` and bidirectional `ar-XB` pseudo-locales in debug builds and automatic per-app locale configuration with English declared as the unqualified resource locale.
- Replaced process-cached translated enum/model labels with resource IDs that resolve against the current locale whenever displayed.
- Replaced the concatenated Accessibility explanation with one complete translatable sentence.
- Made app-drawer drag targets share available width and permit two-line labels for expanded pseudo-localized text.
- Moved the widget dimension text and the remaining Squircle label into string resources.
- Added `tools/check_localization.py` to verify resource parity, format placeholders, resource references, Kotlin Cyrillic, sentence concatenation, stale enum translations, and pseudo-locale configuration without an Android SDK.
- `versionCode = 45`, `versionName = "0.29.1"`.

## 0.29.0

- Extracted 514 Russian user-interface texts from Kotlin into Android string resources without enlarging the existing composable functions.
- Added a complete English default resource catalog and a matching Russian `values-ru` catalog with numbered format placeholders for locale-specific word order.
- Added application-level resource access for non-composable UI models and registered the application class in the manifest.
- Kept the application name non-translatable and localized the accessibility-service label and description.
- `versionCode = 44`, `versionName = "0.29.0"`.

## 0.28.7

- Changed the bottom-dock `+` button to appear only on an empty current dock page; it is never inserted beside existing entries.
- Added a persistent bottom-dock setting that can disable the `+` button completely. Existing installations default to enabled.
- Added an always-available `Add item to current page` action in bottom-dock settings, so entries can still be added when `+` is hidden or disabled.
- Removed bottom-dock uniqueness checks for apps, launcher actions, settings shortcuts, preset icons, and workspace-to-dock moves. Every addition receives a new UUID and duplicate entries are allowed on the same page or different pages.
- Kept the existing five-entry capacity per bottom-dock page and added explicit full-page feedback to every picker path.
- Moved the new add-button settings UI into `BottomDockAddControls.kt` instead of enlarging the main settings composable.
- `versionCode = 43`, `versionName = "0.28.7"`.

## 0.28.6

- Restored the pager-offset compensation used by `Arc` for circle and oval reveals. Both page textures now remain stationary while only the centre-out mask changes.
- Reset retained radial texture roles for every precomposed page whenever the pager is at rest, so a previously visited page correctly becomes the destination when revisited.
- `versionCode = 42`, `versionName = "0.28.6"`.

## 0.28.5

- Moved the circle and oval source/destination Mesh textures into one stationary screen plane instead of applying the pager translation to each texture.
- The single expanding radial mask now removes the old page and exposes the new page at the same coordinates simultaneously; there is no second shrinking reveal phase.
- `versionCode = 41`, `versionName = "0.28.5"`.

## 0.28.4

- Retained circle/oval source and destination texture roles until live pager content returns, preventing the centre-out reveal from reversing near the settle boundary.
- Changed the oval reveal from horizontal to vertical by making its physical vertical radius larger than its horizontal radius.
- `versionCode = 40`, `versionName = "0.28.4"`.

## 0.28.3

- Preserved the existing cascade twist as `Cascade twist (inward)` and added a complementary `Cascade twist (outward)` variant whose visible turn follows the opposite screen-space direction.
- Added non-rotating circular and oval centre-out Mesh reveals next to `Arc` in the effect list.
- Both new reveals use complementary physically culled source/destination geometry, so they do not reintroduce translucent full-page overlap.
- `versionCode = 39`, `versionName = "0.28.3"`.

## 0.28.2

- Replaced alpha-only Mesh masking with physical triangle culling. Hidden source or destination regions are no longer submitted to `android.graphics.Mesh`, eliminating full-page translucent ghosts even when captured page backgrounds contain transparency.
- Applied the same hard visibility rule to independent interlaced-blind tiles and the phased cylinder/sphere surfaces.
- Made cascade direction visible in screen-space geometry with a direction-aware turn offset and strip curl, in addition to depth rotation.
- Increased burning-particle size in density-independent terms so particles remain visibly large on high-density displays.
- Prevented `SnapshotAppWidgetHostView` from passing transient null `RemoteViews` to the platform default view.
- Replaced platform widget default/error overlays with transparent fallbacks while retaining the last valid captured widget frame.
- Added an in-process widget snapshot cache so a temporarily unavailable widget does not cover its previous working image after page recomposition.
- `versionCode = 38`, `versionName = "0.28.2"`.

## 0.28.1

- Split cylinder and sphere transitions into three visibility phases: outgoing-page wrapping, shared-surface rotation, and incoming-page unwrapping. Flat pages no longer separate or converge side by side.
- Corrected the cascade-twisting direction while retaining one shared direction for both page textures.
- Increased every 3D-burning particle radius by exactly four times.
- Hidden uncaptured incoming live pages during Mesh transitions and captured missing textures immediately, preventing complete source and destination pages from appearing together.
- Added bounded capture retries before the required crossfade error fallback.
- Added a throttled, provider-grouped `ACTION_APPWIDGET_UPDATE` request when the launcher starts or receives Home again. Stored provider data is used when Android temporarily returns no provider metadata.
- `versionCode = 37`, `versionName = "0.28.1"`.

## 0.28.0

- Unified the visual direction used by both textures in every paired Mesh transition.
- Corrected the incoming-page direction for cylinder and sphere transitions without changing the accepted outgoing-page motion.
- Corrected cascade twisting and flying-rectangle motion so both follow the finger direction.
- Synchronized the 3D-burning reveal mask with its visible fire boundary.
- Removed the near-black edge shading from flying rectangles; tiles now preserve the captured page colors.
- Made all presentation-style reveals use exactly complementary binary source/destination masks, preventing translucent page overlap.
- Increased deterministic fire particles from 96 to 512 and kept them tied to the single progress-driven boundary.
- Locked cached Mesh textures through the release boundary and stopped transient capture failures from selecting crossfade permanently, addressing fast page-curl flashes.
- Decoupled widget host views from application-list refreshes and launcher resume events.
- Removed the widget snapshot feedback loop, debounced frame capture, retained the last valid `RemoteViews`, and retained provider metadata through temporary package/service unavailability.
- `versionCode = 36`, `versionName = "0.28.0"`.

## 0.27.2

- Fixed the remaining `LauncherDialogStyle.kt` compilation error: Android `Paint` does not expose `setRenderEffect`.
- Dialog image backgrounds are now decoded as software bitmaps and blurred off the UI thread with an architecture-independent Kotlin implementation.
- Kept the blur scoped to the dialog background so dialog text and controls remain sharp.
- `versionCode = 35`, `versionName = "0.27.2"`.

## 0.27.1

- Corrected the Material 3 `LocalTextStyle` import used by launcher dialogs.
- Replaced the unsupported Kotlin synthetic `Paint.renderEffect` assignment with the official `setRenderEffect` call.
- Added the Compose `nativeCanvas` extension import required for drawing dialog background bitmaps.
- Removed the inaccessible standalone `weight` import; the modifier now resolves correctly through `RowScope`.
- `versionCode = 34`, `versionName = "0.27.1"`.

## 0.27.0

- Unified the 3D-burning Mesh mask, hot edge, and ember particles around one progress-driven boundary. The fire pass can no longer outrun the destination texture or restart near the final edge.
- Added ninety-six deterministic, tiny glowing ember particles inspired by the shader-based Compiz-style fire family. They follow gesture progress and never run as an independent repeating animation.
- Corrected paired-page direction for fire, cylinder, sphere, cascade twisting, and flying rectangles. Source and destination geometry now occupy the same transition coordinate system.
- Changed cylinder and sphere page spacing to exact adjacent 90-degree sectors and expanded each page surface to the complete sector, eliminating the unused angular gap.
- Removed translucent cascade overlap by giving outgoing and incoming strips identical geometry with a binary texture handoff.
- Removed the `Retro rectangles` effect. Existing stored selections migrate safely to `Flying rectangles`.
- Rebuilt `Flying rectangles` with independent four-vertex Mesh tiles, contiguous page coverage, and no artificial gutters or between-page gap.
- Added vertical blinds, horizontal blinds, checkerboard, triangle, arc, random bars, thread, slide, diagonal, glitter, vortex, vertical interlaced blinds, and horizontal interlaced blinds.
- Interlaced blinds use independent rectangular Mesh tiles: all one-based odd tiles turn sequentially before the even tiles, and each completed tile switches to the destination texture.
- Captured Mesh textures after the next frame instead of a fixed startup delay. Missing initial captures remain live rather than falsely triggering the crossfade error fallback.
- Increased horizontal Mesh resolution so the fire mask and visible boundary remain closely aligned.
- Added independent launcher-dialog styling: color, gradient, image, opacity, image blur, text color, and configurable text shadow.
- Routed every launcher `AlertDialog`, including widget, folder, icon-pack, dock, confirmation, and settings dialogs, through the shared dialog appearance layer.
- `versionCode = 33`, `versionName = "0.27.0"`.

## 0.26.0

- Replaced live `AndroidView` widget content during Mesh transitions with retained Compose-drawable widget frames. Page-curl, cascade, and retro textures now stay stable when a widget updates during a fast release.
- Preserved the exact stored widget width and height across launcher restarts and upgrades instead of clamping them to the provider's nominal maximum size.
- Kept unavailable widget records on the workspace and added explicit reconnect and remove actions. Reconnecting preserves the original page, position, and custom size.
- Recreated hosted widget views after launcher resume or package changes and resumed `AppWidgetHost` listening so providers can recover after extended device idle.
- Added a late-settle workspace hit layer that routes a quick tap to the destination shortcut or folder while the pager is finishing its final visual frame.
- Removed the extra pager translation from shared sphere and cylinder surfaces, preventing an empty page gap between adjacent workspaces.
- Made cascade and retro texture ownership binary so unrelated translucent source or destination pages cannot bleed through.
- Added bordered retro rectangles with empty gutters and outer margins.
- Added `Flying rectangles`, a sequential strip transition whose sections leave and arrive from the swipe direction without flipping.
- Made `3D burning` use one progress-driven fire pass with a slower eased reveal and a stationary source and destination.
- Effect-choice rows bring the currently selected effect into view when the picker opens.
- Added launcher-interface background settings for solid color, gradient, image, opacity, and blur, plus workspace text color and the existing text-shadow controls.
- Side-dock dismissal now starts on the initial empty-workspace pointer event rather than waiting for the completed tap gesture.
- `versionCode = 32`, `versionName = "0.26.0"`.

## 0.25.0

- Removed fast-settle page-curl flashes by fading the final folded source and switching the settled target directly to live Compose content.
- Made completed Mesh transitions release their captured preview immediately, so the destination page can be used as soon as it settles.
- Limited paired Mesh rendering to the outgoing page and the one active incoming neighbour. This removes idle neighbour overlays from cube-in and translucent extra pages from cascade twisting.
- Reversed the shared cylinder and sphere rotation direction to follow the finger.
- Added `Retro rectangles`, a deliberately stepped twelve-strip sequential flip inspired by 1980s and 1990s computer transitions.
- Added `3D burning`, which keeps both pages stationary while a full-height animated fire boundary reveals the destination.
- Empty-workspace taps now close the side dock.
- Added an optional page-indicator action that shows or hides the bottom dock; the indicator moves down while the dock is hidden.
- `versionCode = 31`, `versionName = "0.25.0"`.

## 0.24.0

- Kept the corrected Compiz-style cube-in and cube-out geometry unchanged.
- Decoupled pager placement from Mesh deformation progress. A page curl started near the centre now begins at the edge and rapidly catches up to the finger without moving the underlying pages differently.
- Removed the post-release Mesh progress boost; the remaining transition continues with the same progress mapping.
- Rebuilt cylinder and sphere as shared surfaces that zoom out, rotate the attached workspaces, and zoom back in.
- Made the card effect two-sided: the outgoing page disappears at the edge-on midpoint and the incoming page occupies the reverse face.
- Replaced the two-half DNA implementation with a full-height cascade of horizontal strips. Every strip turns once, stops, and switches independently from the outgoing texture to the incoming texture.
- Isolated behind-mode Environment Effects and the wallpaper in a dedicated background layer below all launcher content.
- The bottom-dock `+` button is now shown only on a completely empty page.
- Added confirmation before removing a workspace shortcut, a folder, or an app icon from a folder through removal controls.
- Added immediate and animated fly-back modes for closing the persistent folder fan by tapping empty space.
- Removed the package-migration instruction from the project.
- `versionCode = 30`, `versionName = "0.24.0"`.

## 0.23.0

- Bottom-dock entries now retain their own page. The `+` action adds to the visible page, including partially filled pages.
- Preset Phone, Files, Apps, Maps, and Messages icons can be removed and restored from the new collapsed Preset icons category in either dock.
- Each bottom-dock page continues to render its shelf and icons as one moving pager page, including looping mode.
- The widget picker groups providers by application in collapsed, expandable application sections.
- Folder-shape choices are collapsed into a preview picker by default.
- Folder extraction accepts only upward or downward gestures within 30 degrees of vertical; horizontal gestures are ignored.
- Bottom-dock dragging suppresses the workspace swipe-up action for the entire drag.
- Environment Effects can render behind launcher content or above all launcher content.
- Mesh cube-in and cube-out transitions now use opposite depth directions.
- Non-cube Mesh transitions reveal the incoming page only after the midpoint and only below vacated outgoing geometry.
- DNA Mesh geometry twists the top half first, reaches a swapped top edge and fixed middle/bottom at the midpoint, then engages the lower half.
- Mesh progress is scaled by the gesture starting position; release accelerates the remaining transition.
- Consolidated version notes into this changelog and all build corrections into `FIXES.md`.
- `versionCode = 29`, `versionName = "0.23.0"`.

## 0.22.1

- Corrected Compose and dialog compilation issues while preserving Android 14 Mesh rendering.
- Added normalization for legacy launcher action identifiers.

## 0.22.0

- Renamed the app to e_e launcher and changed the package to `org.collegiumdesideriorum.e_e_launcher`.
- Retained Russian interface strings while converting developer comments and supporting documents to English.

## 0.21.0

- Added Android 14 `Mesh`/`MeshSpecification` page curl, cube, cylinder, sphere, card, and DNA transitions with captured Compose page textures and crossfade fallback.
- Added workspace-effect and app-drawer-effect launcher actions.
- Moved app-drawer drag coordinates into the root overlay and retained the original gesture owner across page changes.

## 0.20.0

- Expanded Environment Effects movement and rotation modes.
- Added folder-to-workspace extraction and cross-page app-drawer drag corrections.
- Added icon activation effects.

## 0.19.0

- Added local shortcut/folder appearance editing, image and icon-pack support, and launcher Environment Effects actions.
- Added configurable Environment Effects speed, count, fill, and gradient modes.

## 0.18.0–0.18.1

- Added icon-pack discovery and selection.
- Reworked manual app-drawer sorting to preserve the source gesture across pages and require edge-zone re-entry.
- Added simultaneous Environment Effects and corrected their Compose animation imports.

## 0.17.0–0.17.1

- Made bottom-dock shelves page backgrounds, added dock alignment, app customization, text shadows, and robust drawer edge paging.
- Split the launcher UI into non-inline sections to avoid `MethodTooLargeException`.

## 0.16.0

- Corrected drawer-to-workspace dragging, reorder previews, page switching, and package refresh handling.

## 0.15.0–0.15.1

- Added the multi-page bottom dock, dock shelves, workspace transfer, drawer backgrounds, visual color settings, and dense grids.
- Corrected build ordering and Compose imports.

## 0.14.0–0.14.1

- Added app-drawer drag targets, manual ordering, search controls, and workspace/side-dock transfer.
- Corrected the tap callback signature.

## 0.13.0

- Added widget editing handles and removal, side-dock dragging/removal mode, default workspace selection, vertical app-drawer pages, and grid sliders.

## 0.12.0

- Added drawer grids and card appearance, three widget-picker presentation modes, and stable widget resizing.

## 0.11.0

- Added editable folders, the temporary folder fan, side dock, and independent workspace gestures.

## 0.10.0

- Added folder movement and the temporary pointer-following folder fan.

## 0.9.0

- Added nested settings, widget editing, drawer sorting, Usage Access integration, and folder creation.

## 0.8.0

- Added looping workspace/drawer pages, the empty-workspace add menu, AppWidgetHost integration, binding, configuration, and persistence.

## 0.7.0

- Added independent app-drawer page and icon effects.

## 0.6.0

- Corrected shortcut dragging, notification-shade gesture arbitration, and added paged app-drawer mode.

## 0.5.0

- Added shortcut context actions and the accessibility-backed notification-shade gesture.

## 0.4.0

- Added movable workspace shortcuts, removal target, page effects, and workspace-count settings.
