# Reset launcher settings

Open **Launcher settings → Security → Reset launcher settings**. The Security page also contains the HOME role, editing locks, Safe Mode, and icon-pack selection. Only the nested reset page shows destructive reset actions, highlighted in crimson. Before resetting, use **Create backup** on the main settings screen to save your current layout to a JSON file.

Each choice shows a confirmation dialog with the specific data affected:

| Choice | What is reset | What is retained |
| --- | --- | --- |
| **Workspace** | Pages, shortcuts, workspace folders, widgets and their Android bindings, gesture mappings and page transition preferences; then the starter actions, folder and two note widgets are recreated | Both docks, app drawer and interface appearance |
| **App drawer** | Layout, ordering, icon frames, backgrounds, hidden-app list, and app-drawer preferences | Workspace, both docks and interface appearance |
| **Side dock** | Side-dock contents, display settings and editing lock | Bottom dock, workspace and app drawer |
| **Bottom dock** | Bottom-dock contents, pages, display settings and editing lock | Side dock, workspace and app drawer |
| **Interface settings** | Interface and dialog appearance, wallpaper-mode settings, shadows, environment effects, folder fan animation settings, icon launch animation and global icon pack | Workspace contents, dock contents and app drawer layout |
| **All launcher settings** | Every launcher preference, saved page/dock/widget layout, visual setting, editing lock, and preferred launcher language | Installed applications, Android HOME role, Android permissions already granted by the system, and externally saved backups |

The final option requires **two confirmations**: first a list of the data to be cleared, then an explicit final warning. After reset, the launcher restarts its activity to reload the fresh defaults. The complete reset can also display the welcome flow again.

Resetting launcher wallpaper **configuration** does not erase the phone's system wallpaper or the source image files. A selective reset does not remove installed applications or files stored outside e_e launcher. A backup cannot be restored unless it has been saved beforehand.

**Before resetting:** Save a JSON backup of any layout you want to keep. The reset implementation has been through targeted testing, but destructive reset paths and widget restoration can vary across devices; test unfamiliar reset options on a nonessential profile first.

## Clearing the desktop without resetting preferences

Open **Launcher settings → Workspaces → Workspace cleanup → Clear workspace items**. This is intentionally **not** the same as **Security → Reset launcher settings → Workspace**. It removes all placed shortcuts, folders and widgets, including launcher-note text, but keeps the number of pages, current default page, gestures, transition effects, and side/bottom dock configurations. A confirmation explains exactly what will be removed. Save a JSON backup first.

**Reset Workspace** restores the original five-page demo profile; the two built-in notes require Android to permit the launcher's own widget binding. **Clear workspace items** instead leaves the pages empty and does not automatically recreate the starter profile after an ordinary restart.
