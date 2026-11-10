# Page transitions and Environment Effects

e_e launcher includes visual effects for workspace navigation, app drawer
pages, icon interactions, and the surrounding environment.

## Page transitions

- Choose an effect in launcher settings and use the built-in preview before
  applying it.
- Effects can be previewed both in settings and during workspace navigation.
- Different effects transform page geometry in different ways, including
  three-dimensional-looking page motion and reveal-style transitions.
- Optional looping allows the last workspace page to transition back to the
  first, and vice versa.

Some transitions use Android Mesh rendering introduced in API 34. Therefore
the current mainline release requires **Android 14 or newer**. Future versions
for older Android releases may use different implementations and might not
produce identical effects.

The Pager review through 0.44.6 removed redundant Fade, Stack, Depth and classic Cube choices, and reworked the graphics-layer Plane/Card flip. Older saved effect IDs are migrated to surviving transitions. Two stationary zoom effects are available: Zoom in and Zoom out. The Pager effects previously called Wave, Rotate and Flip are now displayed in English as Diagonal page swipe, Slanted page swipe and Rotating page swipe; their underlying persisted IDs and animation algorithms are unchanged. The independent icon Wave animation retains its original name. Canvas and Mesh reviews are separate future steps.

The page-curl/scroll effect has known cosmetic differences from its intended
appearance; these are tracked as future improvements and do not prevent
ordinary launcher use.

## Environment Effects

Environment Effects add animated elements to the workspace independently of
page transitions. Existing types include rain, snow, leaves, clouds, fire,
sun rays, fish, and moving distortion variants.

Effects may use more GPU time and energy than a static wallpaper, especially
when several visual features are enabled simultaneously. To troubleshoot
graphics problems, see [Safe Mode](safe-mode.md).
