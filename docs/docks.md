# Side and bottom docks

e_e launcher provides two separate areas for commonly used items: a vertical
side dock and a paged horizontal bottom dock.

## Bottom dock

Open **Launcher settings → Bottom dock**.

- Configure from one to five pages (three by default).
- Dock pages do not loop by default.
- Drag a shortcut from the workspace into the dock, or drag an item upward
  into the workspace to create a workspace shortcut and remove the dock entry.
- Organize applications into dock folders, including moving a whole folder
  between the dock and workspace where the target allows editing.
- Configure the background and the optional Cairo-Dock-inspired decorative
  shelf. Each page owns its shelf so the shelf and icons scroll together.
- Remove a dock entry using the available drag-to-delete target. Removing an
  entry does **not** uninstall the corresponding Android app.

## Side dock

The vertical side dock is a separate collection of shortcuts and folders.
Its appearance, transparency, and editing protection can be managed
independently of the bottom dock.

## Editing locks

The workspace, side dock, and bottom dock have **independent editing locks**.
Locking one does not automatically lock the other two. Attempting to move
items inside a locked dock or drop new items there shows a short notice, while
normal tap-to-launch continues to work.

Folder contents can be reordered inside an open locked folder, but adding or
extracting items is restricted by the relevant editing lock.

These locks protect *layout editing*; they are not a system security boundary
or an application access-control mechanism.
