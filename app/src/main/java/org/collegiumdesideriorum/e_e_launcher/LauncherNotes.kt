package org.collegiumdesideriorum.e_e_launcher

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.style.TextOverflow
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID
import kotlin.math.roundToInt

/** A local, editable launcher card — intentionally not a system AppWidgetProvider. */
internal data class WorkspaceNote(
    val id: String = UUID.randomUUID().toString(),
    val page: Int,
    val xFraction: Float,
    val yFraction: Float,
    val text: String,
    val widthDp: Int = 286,
    val heightDp: Int = 134
)

internal object LauncherNotesStore {
    private const val PREFS_NAME = "e_e_launcher_workspace"
    internal const val KEY_NOTES = "workspace_notes"

    fun hasNotes(context: Context): Boolean =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).contains(KEY_NOTES)

    fun load(context: Context): List<WorkspaceNote> {
        val raw = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_NOTES, null) ?: return emptyList()
        return runCatching {
            val array = JSONArray(raw)
            buildList {
                for (index in 0 until array.length()) {
                    val item = array.getJSONObject(index)
                    add(WorkspaceNote(
                        id = item.getString("id"),
                        page = item.optInt("page", 0).coerceAtLeast(0),
                        xFraction = item.optDouble("x", 0.15).toFloat().coerceIn(0f, 1f),
                        yFraction = item.optDouble("y", 0.25).toFloat().coerceIn(0f, 1f),
                        text = item.optString("text", ""),
                        widthDp = item.optInt("widthDp", 286).coerceIn(120, 900),
                        heightDp = item.optInt("heightDp", 134).coerceIn(72, 900)
                    ))
                }
            }
        }.getOrDefault(emptyList())
    }

    fun save(context: Context, notes: List<WorkspaceNote>) {
        val array = JSONArray()
        notes.forEach { note ->
            array.put(JSONObject().apply {
                put("id", note.id)
                put("page", note.page)
                put("x", note.xFraction.toDouble())
                put("y", note.yFraction.toDouble())
                put("text", note.text)
                put("widthDp", note.widthDp)
                put("heightDp", note.heightDp)
            })
        }
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit().putString(KEY_NOTES, array.toString()).apply()
    }

    fun seedWelcome(context: Context, homePage: Int, pageCount: Int) {
        if (hasNotes(context)) return
        save(context, listOf(
            WorkspaceNote(page = homePage, xFraction = 0.13f, yFraction = 0.22f,
                text = context.getString(R.string.ui_note_welcome_home)),
            WorkspaceNote(page = (homePage + 1).coerceAtMost(pageCount - 1),
                xFraction = 0.13f, yFraction = 0.26f,
                text = context.getString(R.string.ui_note_welcome_next))
        ))
    }
}

/**
 * Launcher-local note with the same absolute-pixel placement and anchored resize
 * semantics as WorkspaceWidgetItem. The data stays in workspace_notes: no AppWidgetIds
 * to migrate, and older JSON backups remain readable.
 *
 * IMPORTANT: containerSize is the widget-safe workspace size (bottom shelf excluded),
 * exactly as for WorkspaceWidgetItem; do NOT subtract the dock inset twice.
 */
@Composable
internal fun WorkspaceNoteCard(
    note: WorkspaceNote,
    containerSize: IntSize,
    editingLocked: Boolean,
    onEditingLocked: () -> Unit,
    onEdit: () -> Unit,
    onMove: (Float, Float) -> Unit,
    onResize: (Int, Int, Float, Float) -> Unit,
    onDelete: () -> Unit,
    onDragging: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val maxWidthDp = with(density) { containerSize.width.toDp().value.roundToInt() }.coerceAtLeast(1)
    val maxHeightDp = with(density) { containerSize.height.toDp().value.roundToInt() }.coerceAtLeast(1)
    val minWidthDp = minOf(120, maxWidthDp)
    val minHeightDp = minOf(72, maxHeightDp)

    var temporaryPosition by remember(note.id) { mutableStateOf<Offset?>(null) }
    var temporaryWidthDp by remember(note.id) { mutableStateOf<Int?>(null) }
    var temporaryHeightDp by remember(note.id) { mutableStateOf<Int?>(null) }
    var resizeStartPosition by remember(note.id) { mutableStateOf(Offset.Zero) }
    var resizeStartWidthDp by remember(note.id) { mutableStateOf(note.widthDp) }
    var resizeStartHeightDp by remember(note.id) { mutableStateOf(note.heightDp) }
    var selected by remember(note.id) { mutableStateOf(false) }
    var confirmDelete by remember(note.id) { mutableStateOf(false) }

    val widthDp = (temporaryWidthDp ?: note.widthDp).coerceIn(minWidthDp, maxWidthDp)
    val heightDp = (temporaryHeightDp ?: note.heightDp).coerceIn(minHeightDp, maxHeightDp)
    val widthPx = with(density) { widthDp.dp.toPx() }
    val heightPx = with(density) { heightDp.dp.toPx() }
    val maxX = (containerSize.width - widthPx).coerceAtLeast(0f)
    val maxY = (containerSize.height - heightPx).coerceAtLeast(0f)
    val savedPosition = Offset(
        LauncherNoteGeometry.absolute(note.xFraction, containerSize.width.toFloat(), widthPx),
        LauncherNoteGeometry.absolute(note.yFraction, containerSize.height.toFloat(), heightPx)
    )
    val position = temporaryPosition ?: savedPosition

    // Never release the visual pixel anchor before the parent has published the
    // updated note. Clearing the preview in onDragEnd previously made it jump back
    // to its old saved fraction, especially on subsequent drag operations.
    LaunchedEffect(note.xFraction, note.yFraction, note.widthDp, note.heightDp) {
        temporaryPosition = null
        temporaryWidthDp = null
        temporaryHeightDp = null
    }

    fun commit(anchor: Offset, requestedWidthDp: Int = widthDp, requestedHeightDp: Int = heightDp) {
        val safeWidth = requestedWidthDp.coerceIn(minWidthDp, maxWidthDp)
        val safeHeight = requestedHeightDp.coerceIn(minHeightDp, maxHeightDp)
        val x = LauncherNoteGeometry.fraction(
            anchor.x, containerSize.width.toFloat(), with(density) { safeWidth.dp.toPx() }
        )
        val y = LauncherNoteGeometry.fraction(
            anchor.y, containerSize.height.toFloat(), with(density) { safeHeight.dp.toPx() }
        )
        if (safeWidth != note.widthDp || safeHeight != note.heightDp) {
            onResize(safeWidth, safeHeight, x, y)
        } else {
            onMove(x, y)
        }
    }

    // The gesture detector must survive all recompositions and temporary dimension
    // changes. Keep callbacks current without using maxX/maxY/width/height as keys.
    val currentPosition by rememberUpdatedState(position)
    val currentMaxX by rememberUpdatedState(maxX)
    val currentMaxY by rememberUpdatedState(maxY)
    val currentWidthDp by rememberUpdatedState(widthDp)
    val currentHeightDp by rememberUpdatedState(heightDp)
    val currentCommit by rememberUpdatedState<(Offset, Int, Int) -> Unit>({ anchor, w, h ->
        commit(anchor, w, h)
    })
    val currentOnEditingLocked by rememberUpdatedState(onEditingLocked)
    val currentOnDragging by rememberUpdatedState(onDragging)

    fun startResize() {
        resizeStartPosition = position
        resizeStartWidthDp = widthDp
        resizeStartHeightDp = heightDp
        temporaryPosition = position
        currentOnDragging(true)
    }

    fun applyResize(total: Offset, fromLeft: Boolean = false, fromTop: Boolean = false,
        fromRight: Boolean = false, fromBottom: Boolean = false) {
        val initialWidthPx = with(density) { resizeStartWidthDp.dp.toPx() }
        val initialHeightPx = with(density) { resizeStartHeightDp.dp.toPx() }
        val initialRight = resizeStartPosition.x + initialWidthPx
        val initialBottom = resizeStartPosition.y + initialHeightPx

        val proposedW = when {
            fromLeft -> resizeStartWidthDp - with(density) { total.x.toDp().value }.roundToInt()
            fromRight -> resizeStartWidthDp + with(density) { total.x.toDp().value }.roundToInt()
            else -> resizeStartWidthDp
        }
        val proposedH = when {
            fromTop -> resizeStartHeightDp - with(density) { total.y.toDp().value }.roundToInt()
            fromBottom -> resizeStartHeightDp + with(density) { total.y.toDp().value }.roundToInt()
            else -> resizeStartHeightDp
        }
        val horizontalRoom = if (fromLeft) initialRight else containerSize.width.toFloat() - resizeStartPosition.x
        val verticalRoom = if (fromTop) initialBottom else containerSize.height.toFloat() - resizeStartPosition.y
        val maxAtAnchorW = with(density) { horizontalRoom.coerceAtLeast(0f).toDp().value.toInt() }
            .coerceIn(minWidthDp, maxWidthDp)
        val maxAtAnchorH = with(density) { verticalRoom.coerceAtLeast(0f).toDp().value.toInt() }
            .coerceIn(minHeightDp, maxHeightDp)
        val newW = proposedW.coerceIn(minWidthDp, maxAtAnchorW)
        val newH = proposedH.coerceIn(minHeightDp, maxAtAnchorH)
        val newWidthPx = with(density) { newW.dp.toPx() }
        val newHeightPx = with(density) { newH.dp.toPx() }
        val left = if (fromLeft) initialRight - newWidthPx else resizeStartPosition.x
        val top = if (fromTop) initialBottom - newHeightPx else resizeStartPosition.y
        temporaryWidthDp = newW
        temporaryHeightDp = newH
        temporaryPosition = Offset(left.coerceAtLeast(0f), top.coerceAtLeast(0f))
    }

    fun finishResize() {
        currentOnDragging(false)
        currentCommit(temporaryPosition ?: position,
            temporaryWidthDp ?: widthDp, temporaryHeightDp ?: heightDp)
        // Leave the temporary bounds visible until the saved model is updated.
    }

    Box(Modifier.fillMaxSize()) {
        Box(
            modifier = modifier.offset { IntOffset(position.x.roundToInt(), position.y.roundToInt()) }
                .size(widthDp.dp, heightDp.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxSize()
                    .then(if (selected && !editingLocked) Modifier.border(
                        2.dp, Color(0xFF72E28A), RoundedCornerShape(17.dp)) else Modifier)
                    .pointerInput(note.id, editingLocked) {
                        detectDragGesturesAfterLongPress(
                            onDragStart = {
                                if (editingLocked) {
                                    currentOnEditingLocked()
                                } else {
                                    selected = true
                                    temporaryPosition = currentPosition
                                    currentOnDragging(true)
                                }
                            },
                            onDrag = { change, amount ->
                                if (!editingLocked) {
                                    change.consume()
                                    val from = temporaryPosition ?: currentPosition
                                    temporaryPosition = Offset(
                                        (from.x + amount.x).coerceIn(0f, currentMaxX),
                                        (from.y + amount.y).coerceIn(0f, currentMaxY)
                                    )
                                }
                            },
                            onDragEnd = {
                                if (!editingLocked) {
                                    currentCommit(temporaryPosition ?: currentPosition, currentWidthDp, currentHeightDp)
                                    currentOnDragging(false)
                                }
                            },
                            onDragCancel = {
                                temporaryPosition = null
                                currentOnDragging(false)
                            }
                        )
                    }
                    .clickable { if (editingLocked) onEditingLocked() else onEdit() },
                shape = RoundedCornerShape(17.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xE52B3040)),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(Modifier.fillMaxSize().padding(14.dp)) {
                    Text(note.text, color = Color.White, fontSize = 16.sp,
                        lineHeight = 21.sp, overflow = TextOverflow.Ellipsis)
                }
            }
            if (selected && !editingLocked) {
                // Exactly the ResizeHandle gesture component used by hosted widgets.
                // It reports TOTAL drag delta, including for the top/bottom handles.
                ResizeHandle("↔", Modifier.align(Alignment.CenterStart),
                    onDragStart = ::startResize,
                    onDrag = { applyResize(it, fromLeft = true) }, onDragEnd = ::finishResize)
                ResizeHandle("↕", Modifier.align(Alignment.TopCenter),
                    onDragStart = ::startResize,
                    onDrag = { applyResize(it, fromTop = true) }, onDragEnd = ::finishResize)
                ResizeHandle("↖", Modifier.align(Alignment.TopStart),
                    onDragStart = ::startResize,
                    onDrag = { applyResize(it, fromLeft = true, fromTop = true) }, onDragEnd = ::finishResize)
                ResizeHandle("↔", Modifier.align(Alignment.CenterEnd),
                    onDragStart = ::startResize,
                    onDrag = { applyResize(it, fromRight = true) }, onDragEnd = ::finishResize)
                ResizeHandle("↕", Modifier.align(Alignment.BottomCenter),
                    onDragStart = ::startResize,
                    onDrag = { applyResize(it, fromBottom = true) }, onDragEnd = ::finishResize)
                ResizeHandle("↘", Modifier.align(Alignment.BottomEnd),
                    onDragStart = ::startResize,
                    onDrag = { applyResize(it, fromRight = true, fromBottom = true) }, onDragEnd = ::finishResize)
                Text("×", color = Color.White, fontSize = 24.sp,
                    modifier = Modifier.align(Alignment.TopEnd)
                        .background(Color(0xBBBD2637), RoundedCornerShape(12.dp))
                        .clickable { confirmDelete = true }
                        .padding(horizontal = 12.dp, vertical = 3.dp))
                Text("✓", color = Color.White, fontSize = 20.sp,
                    modifier = Modifier.align(Alignment.TopStart).padding(start = 44.dp)
                        .background(Color(0xBB268542), RoundedCornerShape(12.dp))
                        .clickable { selected = false }
                        .padding(horizontal = 10.dp, vertical = 4.dp))
            }
        }
    }
    if (confirmDelete) {
        AlertDialog(onDismissRequest = { confirmDelete = false },
            title = { Text(l10n(R.string.ui_delete)) },
            text = { Text(l10n(R.string.ui_note_delete_confirm)) },
            confirmButton = { TextButton(onClick = { confirmDelete = false; onDelete() }) {
                Text(l10n(R.string.ui_delete))
            } },
            dismissButton = { TextButton(onClick = { confirmDelete = false }) {
                Text(l10n(R.string.ui_cancel))
            } })
    }
}

@Composable
internal fun WorkspaceNoteEditor(
    note: WorkspaceNote,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit,
    onDelete: () -> Unit
) {
    var draft by remember(note.id) { mutableStateOf(note.text) }
    var confirmDelete by remember(note.id) { mutableStateOf(false) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(l10n(R.string.ui_note_title)) },
        text = {
            OutlinedTextField(
                value = draft,
                onValueChange = { draft = it.take(4000) },
                modifier = Modifier.width(320.dp),
                minLines = 4,
                maxLines = 10
            )
        },
        confirmButton = { TextButton(onClick = { onSave(draft) }) { Text(l10n(R.string.ui_save)) } },
        dismissButton = {
            TextButton(onClick = { confirmDelete = true }) { Text(l10n(R.string.ui_delete)) }
            TextButton(onClick = onDismiss) { Text(l10n(R.string.ui_cancel)) }
        }
    )
    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text(l10n(R.string.ui_delete)) },
            text = { Text(l10n(R.string.ui_note_delete_confirm)) },
            confirmButton = { TextButton(onClick = onDelete) { Text(l10n(R.string.ui_delete)) } },
            dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text(l10n(R.string.ui_cancel)) } }
        )
    }
}
