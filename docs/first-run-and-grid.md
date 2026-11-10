# First-run workspace and app drawer grid (0.43.1 test candidate)

## First-run workspace

A genuinely new profile (before the Welcome screen has been completed and without saved desktop items) receives three standalone launcher actions and an accompanying folder on the default third page. The standalone actions are: Environment effects, Workspace effect and Launcher settings. The shortcuts and accompanying folder are placed in the middle/lower part of the page. The folder contains five independent action entries with the same functions, which can be launched or dragged out to desktop shortcuts.

Two editable launcher-local welcome notes are placed above the shortcuts on the home page and on the page to its right. See [notes](notes.md).

Updating a configured launcher does not regenerate the demo content or override its current shortcuts/folders.

## Drawer grid

When no manually saved drawer column/row count exists, the launcher derives the dimensions from the screen width/height in dp with per-cell size limits. Orientation changes recompute dimensions that are not manually overridden. Columns are clamped to 4–10; rows to 4–10. The default first-install target for a typical portrait phone is 5 columns. The app drawer’s normal cell-size and fill-available controls still apply.

Changing a column or row count manually persists that dimension; the untouched dimension remains automatic. This means users can, for example, fix columns at 7 but retain automatic rows.

## Press-to-add

On a long press in an empty portion of the workspace, the launcher records where the gesture occurred. Adding an app, settings shortcut or launcher action via that menu targets that location. A free-position search avoids collision with other shortcuts, folders or widgets on that page. If no long-press location exists, the search begins near the page center. The candidate uses approximate screen-space occupancy to keep additions clear of other items; confirm on devices with very different insets and bottom docks.

## Test priorities

- Fresh install vs upgrade with saved custom values
- Long press near screen edges and near dock, short and long screens
- App picker selection after long press; repeated additions to occupied position
- Drag one of the five initial folder actions onto the desktop, and open it
- Folder icons and effect icons after changing language
- Portrait/landscape tablet, manual override of only columns or only rows
- Settings restore and domain reset without unexpected re-creation of shortcuts

## Workspace maintenance (0.43.7)

The Security **Reset Workspace** action now re-creates the initial starter shortcuts and folder, plus the two native note widgets if Android allows binding. To leave an empty desktop without resetting page count or effects, use the separate **Clear workspace items** action in Workspaces settings. Each operation requires explicit confirmation; keep a JSON backup.

Permanent dragging from an open folder uses the actual point of release (mapped from the dialog window to the workspace); this is independent of the temporary swipe-to-launch gesture on a closed folder.
