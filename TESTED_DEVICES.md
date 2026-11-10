# Tested devices and known device-specific behaviour

This file records observed behaviour, confirmed causes, and practical workarounds.
It is not a list of universal requirements: do not apply an OEM workaround to
another device unless that device shows the same symptom.

## Motorola Edge 60 (XT2505-4, China firmware, Android 15)

### App widgets

Observed symptoms:

- some widgets are initially empty after a reboot;
- VLC and Device Info can become inactive after the launcher is stopped or the
  screen remains locked;
- opening the provider app or adding its widget to Motorola Launcher can make the
  already-bound e_e launcher widget appear.

Diagnostics showed that the e_e launcher AppWidget host, IDs, and provider binding
were valid. `com.motorola.deviceguard` repeatedly stopped the VLC and Device Info
provider processes within tens of milliseconds of their background broadcast.
The providers therefore could not always publish their first `RemoteViews` update.

The effective workaround on this firmware was to enable **Allow running in the
background** for the widget-provider applications. After reboot, leave the launcher
idle for roughly ten seconds before judging the initial widget state. Device Info
and the other tested widgets then appeared without first adding copies to Motorola
Launcher.

The exact menu name can vary by firmware. Also check the provider application's
battery/background page for **Unrestricted**, **Allow background activity**,
**Autostart**, or an equivalent OEM option. If present, disable **Pause app activity
if unused** / **Stop app when unused** for providers that must update widgets.

Version 0.35.15 also fixes two launcher-side issues exposed during this test:

- an initial null update now inflates the provider's declared initial layout
  instead of leaving an empty host;
- Back on an otherwise empty HOME screen is consumed instead of finishing and
  recreating the launcher Activity and all widget hosts.

### System-gesture accessibility service

The firmware may disable or temporarily fail to reconnect an accessibility service
when a development APK is replaced. The app cannot enable this protected setting
by itself. `tools/install_preserving_accessibility.sh` preserves the service state
during ADB test installations without overwriting other enabled accessibility
services.

## Tecno Pova 6 Pro (Android 14)

### App widgets

The tested widgets work normally without the Motorola-specific host or provider
workarounds. Do not enable device-specific widget code merely because Motorola
requires background exceptions.

### Widget resize-handle glyphs and anchoring (0.37.30)

In 0.37.27 the top and bottom handles showed incorrect Unicode arrow glyphs on
Tecno Pova 6 Pro, while Motorola Edge 60 rendered them as expected. OEM font
fallback was a likely cause, but was not independently confirmed in system logs.

Versions 0.37.28–0.37.29 replaced the handle glyphs with Compose Canvas arrows
and addressed right/bottom resize anchoring. Version 0.37.30 corrected a temporary
regression in left/top/top-left anchoring introduced by those changes.

**Confirmed on Tecno Pova 6 Pro:** the user installed 0.37.30 and passed all five
widget-resize tests covering left/top/top-left anchors, right/bottom/bottom-right
anchors, correct handle appearance, and saved widget dimensions/positions.

### Distortion effects

The earlier `RuntimeShaderEffect` child-input path could become transparent or
destabilize rendering on this device. In 0.35.14, Magnifier uses the Compose
layer-copy path and the other seven distortion modes use an explicit bitmap input.
All eight modes passed the reported functional test, and the normal workspace stays
independently visible below the effect layer.

## Adding another device

Record the exact model, firmware region, Android version, symptom, diagnostic
evidence, workaround, and launcher version. Keep confirmed findings separate from
suspected causes so a workaround for one OEM does not become a global code path.


## Recent feature QA (0.43.5–0.43.6; device not identified in report)

The user confirmed all five tests of repeated normal/note widget dragging, resize sequences and persistence in 0.43.5. In 0.43.6, workspace placement passed 9/10 scenarios: adding apps, actions and widgets near a long press, avoiding occupied regions and edges, and persistence worked. **Permanent extraction from an open folder** was the one failure: the icon landed nearer the page center instead of the drag-release position. The separate **temporary folder-fan swipe to launch** works and must remain unchanged.

The JSON backup/import and domain reset suite passed **14/14** on 0.43.6, including launcher notes' text and sizes, third-party widgets, cross-domain isolation and confirmation cancellation. A design ambiguity was resolved for the next version: Reset Workspace re-creates the welcome layout, while Clear workspace items intentionally removes placed items. These 0.43.7 changes still require their own Android Studio build and device verification.

## Pending 0.44.0 effect QA

Pager effect cleanup (Fade/Stack/Depth), stronger Scale, and paired graphics-layer Plane/Card flip have passed source-level checks only. **No device test of 0.44.0 has been reported**. Also carry forward the 0.43.7 folder-drop/reset/clear items for on-device validation.


## 0.48.5 multi-device acceptance — 2026-11-08

The maintainer reported broadly successful launcher use and first launch on several
physical devices. The last two 0.48.5 changes were confirmed: folder insertion
into both docks shows only the standard divider, and effects are selectable in
landscape despite the small display area (one visible list item at a time).
Earlier fixes to side-dock folder fan gestures, extraction from an open folder,
page overview and app drawer effects were also accepted. This is maintainer
QA, not automated SDK instrumentation.

**At the time of the November 8 tests**, the 0.48.6 robustness changes had
not yet been revalidated. Subsequent tests on 0.48.10 confirmed ordinary JSON
backup/import and safe rejection of unsupported/malformed JSON, large custom
image backgrounds, and widget interactions. Built-in note-widget ID rebinding
and OEM cloud/D2D system-backup behavior remain untested.

## 0.48.9–0.48.10 on-device QA — 2026-11-09

**Bottom-dock shelf (0.48.9):** The maintainer confirmed the intended appearance
for all tested shelf background types and combinations of transparency, height
and position. Earlier inner-shadow/false-shelf artifacts were no longer seen.

**Rotation reliability (0.48.10):**

| Device | Reported test | Result |
| --- | --- | --- |
| Motorola Edge 60 | 102 portrait/landscape configuration changes in one process | **PASS:** no new launcher ANRs/crashes in test log; desktop icons/widgets remained in place; rotation was faster. |
| Tecno Pova 6 Pro, Android 14 | 154 portrait/landscape changes in one process | **PASS:** prior rotation hang/layout corruption not reproduced; no new launcher ANRs/crashes in test log. |

The app drawer now remains open while adapting to orientation, rather than
closing on Activity recreation. This is accepted as the preferred behavior.

**Deferred Tecno-only visual defect:** After multiple cyclic movements through
left landscape → portrait → right landscape with the app drawer open, a gap can
appear in its background, exposing the workspace beneath. It was not reproduced
with a single rotation. Tecno's delayed rotation animation may be relevant, but
no root cause has been established and no workaround/code fix has been approved.
This should remain distinct from the resolved ANR.

These are maintainer-led device tests, not full automated instrumentation.
The separate 0.48.10 JSON and image tests are listed below; untested backup
transport and forced restore rollback remain open items.


## 0.48.10 release-readiness QA — 2026-11-09–10

- **Signed Release APK:** valid APK v2 signature (certificate SHA-256
  `726c7bdfc833bc31e877991be5a054087588c3bfe067cbe22218cf49cd21a33f`),
  installed over the same version using `adb install -r`, with 5/5 smoke checks
  passing (Home role, shortcuts/folders/docks, widgets, settings persistence,
  and restart layout).
- **JSON backup:** exporting and reimporting after an effect change and adding
  a temporary shortcut restored the original configuration; an unsupported
  Wakeup Alarm backup and a syntactically truncated JSON file were rejected
  without changing existing settings (3/3 checked scenarios).
- **Large background images:** a user-created 8000 × 8000 JPEG was selected
  for app-drawer and dialog backgrounds without noticeable lag, worked after
  several orientation changes, and persisted after launcher restart.
- **Widgets:** 6/6 tests passed (horizontal/vertical resize, move to another
  workspace page, interaction, rotation, preservation on restart, and moving
  the widget back).
- **App drawer background dialog:** the Choose/Replace picture control was
  moved directly below the Color / Gradient / Picture selector; the maintainer
  confirmed its location and Picture-only visibility on a device.

**Unverified by these scenarios:** mid-write JSON rollback, rebind of the
built-in note widget ID with locale/customized flags, OEM cloud/D2D transport,
and upgrading an installed historical 0.40.0 release. These do not negate
successful execution of the scenarios listed above.
