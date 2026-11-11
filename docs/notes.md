# Native text note widget

Notes are now **real Android AppWidgets** provided by e_e launcher itself (`LauncherNoteWidgetProvider`), rendered with `RemoteViews` and hosted by the **same `AppWidgetHost`** and `WorkspaceWidgetItem` as third-party widgets. The older Compose note-card gesture controls are disabled and their `workspace_notes` records are not migrated; the user agreed to a clean replacement during testing.

- **First launch:** on a fresh, empty profile, the launcher attempts to bind two note widgets (on home and the page right of home). The home note introduces swipes and dragging icons out of the folder; the right note introduces the long-press menu. All text is localized. If Android rejects silent self-widget binding, the launcher does not create broken entries; add a note via the normal widget picker instead.
- **Add:** long-press the desktop → Add → Note. This binds the launcher's own widget via the normal host system, just like selecting it from the grouped Android widget picker (under e_e launcher). The normal Android widget permission dialog is used if binding is not permitted automatically.
- **Edit:** tap the note once when it is not in edit/resize mode. A text dialog opens, supporting up to 4000 characters; Save or Cancel. The note re-renders through `AppWidgetManager.updateAppWidget()`.
- **Move:** exactly like a hosted widget: long press → drag, free position, no snap to icon grid. Drag behavior is handled by the existing `WorkspaceWidgetItem` code, not a separate note gesture detector.
- **Resize:** same green widget edit frame and six handles used for regular Android widgets; top/bottom and corner resizing are provided by the launcher host, not the note implementation. `onAppWidgetOptionsChanged()` refreshes `RemoteViews` after resizing.
- **Delete:** same existing widget delete target/confirmation. The host deletes the Android `appWidgetId`; the note provider/host clears per-widget text data.
- **Persistence:** widgets use the normal `widgets` preference, and note text uses `launcher_note_widget_text_<appWidgetId>` in `e_e_launcher_workspace`. Manual JSON backup stores text additionally under the widget's stable workspace ID in widgetOptions so text is copied to newly bound Android widget IDs. Workspace/All reset deletes note widgets and text.

## Known testing boundaries

- The first-run seed depends on Android granting this launcher permission to bind its own AppWidget provider without a prompt. The app does not silently bypass system widget binding rules. Verify on Motorola and Tecno; if system binding is denied, add notes through the widget picker to trigger the normal permission flow.
- This iteration intentionally does not migrate the old local Compose-card notes. The user reported having no note content to preserve.
- Earlier device tests confirmed repeated widget/note dragging and resizing in 0.43.5 and JSON note backup/restore in 0.43.6. Ordinary widget operation was also tested in the 0.48.10 baseline. Note-widget ID rebinding and Reset Workspace reseeding were not separately verified in every device scenario; see [tested devices](../TESTED_DEVICES.md).
