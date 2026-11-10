package org.collegiumdesideriorum.e_e_launcher

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput

/** Relays a tap while the pager is finishing its final, already-completed visual frame. */
@Composable
internal fun <Shortcut, Folder> WorkspaceSettlingHitLayer(
    enabled: Boolean,
    pointerKey: Any?,
    shortcuts: List<Shortcut>,
    folders: List<Folder>,
    shortcutAt: (Shortcut, Offset) -> Boolean,
    folderAt: (Folder, Offset) -> Boolean,
    onShortcut: (Shortcut) -> Unit,
    onFolder: (Folder) -> Unit,
    onEmpty: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (!enabled) return
    Box(
        modifier.pointerInput(pointerKey, shortcuts, folders) {
            awaitEachGesture {
                val down = awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
                val start = down.position
                var end = start
                var moved = false
                while (true) {
                    val event = awaitPointerEvent(pass = PointerEventPass.Initial)
                    val change = event.changes.firstOrNull { it.id == down.id } ?: break
                    end = change.position
                    if ((end - start).getDistance() > viewConfiguration.touchSlop) moved = true
                    if (!change.pressed) break
                }
                if (!moved) {
                    val shortcut = shortcuts.lastOrNull { shortcutAt(it, end) }
                    val folder = folders.lastOrNull { folderAt(it, end) }
                    when {
                        shortcut != null -> onShortcut(shortcut)
                        folder != null -> onFolder(folder)
                        else -> onEmpty()
                    }
                }
            }
        }
    )
}
