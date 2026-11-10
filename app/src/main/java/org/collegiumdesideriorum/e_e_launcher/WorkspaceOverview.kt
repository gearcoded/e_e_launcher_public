package org.collegiumdesideriorum.e_e_launcher

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.hypot
import kotlin.math.roundToInt

internal enum class WorkspaceOverviewItemType {
    SHORTCUT,
    FOLDER,
    WIDGET
}

internal data class WorkspaceOverviewItem(
    val id: String,
    val type: WorkspaceOverviewItemType,
    val label: String,
    val page: Int,
    val xFraction: Float,
    val yFraction: Float,
    val widthFraction: Float = 0.16f,
    val heightFraction: Float = 0.18f,
    val icon: ImageBitmap? = null
)

private data class OverviewItemDrag(
    val item: WorkspaceOverviewItem,
    val startRoot: Offset,
    val width: Dp,
    val height: Dp,
    val offset: Offset = Offset.Zero
)

private data class OverviewPageDrag(
    val page: Int,
    val startRoot: Offset,
    val offset: Offset = Offset.Zero
)

@Composable
internal fun WorkspaceOverviewScreen(
    pageCount: Int,
    currentPage: Int,
    defaultPage: Int,
    items: List<WorkspaceOverviewItem>,
    editingEnabled: Boolean,
    onClose: () -> Unit,
    onOpenPage: (Int) -> Unit,
    onSetDefaultPage: (Int) -> Unit,
    onAddPage: () -> Unit,
    onDeletePage: (page: Int, moveContentsTo: Int?) -> Unit,
    onReorderPage: (fromPage: Int, toPage: Int) -> Unit,
    onMoveItem: (item: WorkspaceOverviewItem, page: Int, xFraction: Float, yFraction: Float) -> Unit
) {
    val density = LocalDensity.current
    val pageBounds = remember { mutableStateMapOf<Int, Rect>() }
    var itemDrag by remember { mutableStateOf<OverviewItemDrag?>(null) }
    var pageDrag by remember { mutableStateOf<OverviewPageDrag?>(null) }
    var deletePage by remember { mutableStateOf<Int?>(null) }

    fun targetPageAt(root: Offset): Int? = pageBounds.entries
        .firstOrNull { (_, bounds) -> bounds.contains(root) }
        ?.key

    Surface(Modifier.fillMaxSize(), color = Color(0xF2181B23), contentColor = Color(0xFFF2F4FA)) {
        Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().padding(12.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        l10n(R.string.ui_page_overview),
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color(0xFFF6F7FC),
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        if (editingEnabled) l10n(R.string.ui_overview_editing_hint)
                        else l10n(R.string.ui_overview_navigation_hint),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFB9C0D1)
                    )
                }
                TextButton(onClick = onClose) {
                    Text(l10n(R.string.ui_close), color = Color(0xFFCBD6FF))
                }
            }

            LazyVerticalGrid(
                columns = GridCells.Adaptive(154.dp),
                modifier = Modifier.weight(1f).padding(top = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                itemsIndexed((0 until pageCount).toList(), key = { _, page -> page }) { _, page ->
                    val dragged = pageDrag?.page == page
                    WorkspaceOverviewPageCard(
                        page = page,
                        current = page == currentPage,
                        isDefault = page == defaultPage,
                        items = items.filter { it.page == page },
                        editingEnabled = editingEnabled,
                        dragged = dragged,
                        dragOffset = if (dragged) pageDrag?.offset ?: Offset.Zero else Offset.Zero,
                        onBounds = { pageBounds[page] = it },
                        onOpen = { onOpenPage(page) },
                        onSetDefault = { onSetDefaultPage(page) },
                        onDelete = { deletePage = page },
                        onPageDragStart = { root -> pageDrag = OverviewPageDrag(page, root) },
                        onPageDrag = { amount ->
                            pageDrag = pageDrag?.takeIf { it.page == page }?.let {
                                it.copy(offset = it.offset + amount)
                            }
                        },
                        onPageDragEnd = {
                            pageDrag?.takeIf { it.page == page }?.let { drag ->
                                targetPageAt(drag.startRoot + drag.offset)
                                    ?.takeIf { it != page }
                                    ?.let { onReorderPage(page, it) }
                            }
                            pageDrag = null
                        },
                        onPageDragCancel = { pageDrag = null },
                        onItemDragStart = { item, root, width, height ->
                            itemDrag = OverviewItemDrag(item, root, width, height)
                        },
                        onItemDrag = { amount ->
                            itemDrag = itemDrag?.let { it.copy(offset = it.offset + amount) }
                        },
                        onItemDragEnd = {
                            itemDrag?.let { drag ->
                                val root = drag.startRoot + drag.offset
                                val targetPage = targetPageAt(root)
                                val bounds = targetPage?.let(pageBounds::get)
                                if (targetPage != null && bounds != null) {
                                    val x = ((root.x - bounds.left) / bounds.width).coerceIn(0.05f, 0.95f)
                                    val y = ((root.y - bounds.top) / bounds.height).coerceIn(0.08f, 0.92f)
                                    onMoveItem(drag.item, targetPage, x, y)
                                }
                            }
                            itemDrag = null
                        },
                        onItemDragCancel = { itemDrag = null }
                    )
                }

                if (editingEnabled && pageCount < 9) {
                    item(key = "add-page") {
                        OutlinedButton(
                            onClick = onAddPage,
                            modifier = Modifier.fillMaxWidth().height(224.dp)
                        ) {
                            Text("+  ${l10n(R.string.ui_add_page)}")
                        }
                    }
                }
            }
        }

        itemDrag?.let { drag ->
            Box(Modifier.fillMaxSize()) {
                OverviewDragGhost(
                    item = drag.item,
                    width = drag.width,
                    height = drag.height,
                    modifier = Modifier.align(Alignment.TopStart).offset {
                        IntOffset(
                            (drag.startRoot.x + drag.offset.x - with(density) { drag.width.toPx() / 2f }).roundToInt(),
                            (drag.startRoot.y + drag.offset.y - with(density) { drag.height.toPx() / 2f }).roundToInt()
                        )
                    }
                )
            }
        }
    }

    deletePage?.let { page ->
        val hasContents = items.any { it.page == page }
        val target = when {
            pageCount <= 1 -> null
            page < pageCount - 1 -> page + 1
            else -> page - 1
        }
        AlertDialog(
            onDismissRequest = { deletePage = null },
            title = { Text(l10n(R.string.ui_delete_page_number, page + 1)) },
            text = {
                Text(
                    when {
                        pageCount <= 1 -> l10n(R.string.ui_at_least_one_page_required)
                        hasContents -> l10n(R.string.ui_page_contents_will_move, (target ?: 0) + 1)
                        else -> l10n(R.string.ui_delete_empty_page_confirmation)
                    }
                )
            },
            confirmButton = {
                if (pageCount > 1) {
                    TextButton(onClick = {
                        onDeletePage(page, if (hasContents) target else null)
                        deletePage = null
                    }) {
                        Text(
                            if (hasContents) l10n(R.string.ui_move_and_delete)
                            else l10n(R.string.ui_delete),
                            color = Color(0xFFFF7A83)
                        )
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { deletePage = null }) { Text(l10n(R.string.ui_cancel)) }
            }
        )
    }
}

@Composable
private fun WorkspaceOverviewPageCard(
    page: Int,
    current: Boolean,
    isDefault: Boolean,
    items: List<WorkspaceOverviewItem>,
    editingEnabled: Boolean,
    dragged: Boolean,
    dragOffset: Offset,
    onBounds: (Rect) -> Unit,
    onOpen: () -> Unit,
    onSetDefault: () -> Unit,
    onDelete: () -> Unit,
    onPageDragStart: (Offset) -> Unit,
    onPageDrag: (Offset) -> Unit,
    onPageDragEnd: () -> Unit,
    onPageDragCancel: () -> Unit,
    onItemDragStart: (WorkspaceOverviewItem, Offset, Dp, Dp) -> Unit,
    onItemDrag: (Offset) -> Unit,
    onItemDragEnd: () -> Unit,
    onItemDragCancel: () -> Unit
) {
    var rootPosition by remember(page) { mutableStateOf(Offset.Zero) }
    val defaultPageActionLabel = l10n(R.string.ui_default_page)
    val deletePageActionLabel = l10n(R.string.ui_delete)
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(224.dp)
            .onGloballyPositioned {
                rootPosition = it.positionInRoot()
                onBounds(it.boundsInRoot())
            }
            .graphicsLayer {
                translationX = if (dragged) dragOffset.x else 0f
                translationY = if (dragged) dragOffset.y else 0f
                scaleX = if (dragged) 1.04f else 1f
                scaleY = if (dragged) 1.04f else 1f
                shadowElevation = if (dragged) 22f else 0f
            },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (current) Color(0xFF39455D) else Color(0xFF2D3442)
        )
    ) {
        Column(Modifier.fillMaxSize().padding(8.dp)) {
            Row(
                Modifier.fillMaxWidth().then(
                    if (editingEnabled) Modifier.pointerInput(page) {
                        detectDragGesturesAfterLongPress(
                            onDragStart = { onPageDragStart(rootPosition + it) },
                            onDrag = { change, amount -> change.consume(); onPageDrag(amount) },
                            onDragEnd = onPageDragEnd,
                            onDragCancel = onPageDragCancel
                        )
                    } else Modifier
                ),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    l10n(R.string.ui_page_number, page + 1),
                    modifier = Modifier.weight(1f),
                    fontWeight = if (current) FontWeight.Bold else FontWeight.Medium,
                    color = Color(0xFFF4F6FF),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (editingEnabled) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // A single home control doubles as the default-page indicator.
                        // The earlier separate golden house is intentionally removed.
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .clickable(onClick = onSetDefault)
                                .semantics { contentDescription = defaultPageActionLabel },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "⌂",
                                color = if (isDefault) Color(0xFFFFD56A) else Color(0xFFDDE3F0),
                                fontSize = 30.sp,
                                fontWeight = FontWeight.SemiBold,
                                lineHeight = 30.sp
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .clickable(onClick = onDelete)
                                .semantics { contentDescription = deletePageActionLabel },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "×",
                                color = Color(0xFFFF8A91),
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Medium,
                                lineHeight = 32.sp
                            )
                        }
                    }
                }
            }
            BoxWithConstraints(
                Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF252D3D))
                    .border(1.dp, Color.White.copy(alpha = 0.23f), RoundedCornerShape(12.dp))
                    .clickable(onClick = onOpen)
            ) {
                items.forEach { item ->
                    val width = (maxWidth * item.widthFraction.coerceIn(0.10f, 0.55f)).coerceAtLeast(20.dp)
                    val height = (maxHeight * item.heightFraction.coerceIn(0.10f, 0.55f)).coerceAtLeast(20.dp)
                    var itemBounds by remember(item.id) { mutableStateOf(Rect.Zero) }
                    OverviewMiniItem(
                        item = item,
                        editingEnabled = editingEnabled,
                        modifier = Modifier
                            .offset(
                                x = (maxWidth - width) * item.xFraction.coerceIn(0f, 1f),
                                y = (maxHeight - height) * item.yFraction.coerceIn(0f, 1f)
                            )
                            .requiredSize(width, height),
                        onBounds = { itemBounds = it },
                        onDragStart = { local ->
                            onItemDragStart(item, itemBounds.topLeft + local, width, height)
                        },
                        onDrag = onItemDrag,
                        onDragEnd = onItemDragEnd,
                        onDragCancel = onItemDragCancel
                    )
                }
            }
        }
    }
}

@Composable
private fun OverviewMiniItem(
    item: WorkspaceOverviewItem,
    editingEnabled: Boolean,
    modifier: Modifier,
    onBounds: (Rect) -> Unit,
    onDragStart: (Offset) -> Unit,
    onDrag: (Offset) -> Unit,
    onDragEnd: () -> Unit,
    onDragCancel: () -> Unit
) {
    Box(
        modifier = modifier
            .onGloballyPositioned { onBounds(it.boundsInRoot()) }
            .clip(RoundedCornerShape(7.dp))
            .background(
                when (item.type) {
                    WorkspaceOverviewItemType.SHORTCUT -> Color(0xFF4E5B86)
                    WorkspaceOverviewItemType.FOLDER -> Color(0xFF59647D)
                    WorkspaceOverviewItemType.WIDGET -> Color(0xFF2F635C)
                }
            )
            .then(
                if (editingEnabled) Modifier.pointerInput(item.id) {
                    detectDragGesturesAfterLongPress(
                        onDragStart = onDragStart,
                        onDrag = { change, amount -> change.consume(); onDrag(amount) },
                        onDragEnd = onDragEnd,
                        onDragCancel = onDragCancel
                    )
                } else Modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        if (item.icon != null && item.type == WorkspaceOverviewItemType.SHORTCUT) {
            Image(item.icon, item.label, Modifier.fillMaxSize().padding(2.dp))
        } else {
            Text(
                when (item.type) {
                    WorkspaceOverviewItemType.SHORTCUT -> "●"
                    WorkspaceOverviewItemType.FOLDER -> "▦"
                    WorkspaceOverviewItemType.WIDGET -> item.label.take(4)
                },
                fontSize = if (item.type == WorkspaceOverviewItemType.WIDGET) 7.sp else 13.sp,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun OverviewDragGhost(item: WorkspaceOverviewItem, width: Dp, height: Dp, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.requiredSize(width, height).alpha(0.92f),
        shape = RoundedCornerShape(7.dp),
        colors = CardDefaults.cardColors(containerColor = when (item.type) {
            WorkspaceOverviewItemType.SHORTCUT -> Color(0xFF4E5B86)
            WorkspaceOverviewItemType.FOLDER -> Color(0xFF59647D)
            WorkspaceOverviewItemType.WIDGET -> Color(0xFF2F635C)
        })
    ) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            if (item.icon != null && item.type == WorkspaceOverviewItemType.SHORTCUT)
                Image(item.icon, item.label, Modifier.fillMaxSize().padding(2.dp))
            else Text(
                if (item.type == WorkspaceOverviewItemType.FOLDER) "▦" else if (item.type == WorkspaceOverviewItemType.WIDGET) item.label.take(4) else "●",
                fontSize = if (item.type == WorkspaceOverviewItemType.WIDGET) 7.sp else 13.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

internal fun Modifier.workspaceOverviewPinch(
    enabled: Boolean,
    onPinch: () -> Unit
): Modifier = if (!enabled) this else pointerInput(onPinch) {
    awaitEachGesture {
        awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
        var initialDistance: Float? = null
        var opened = false
        while (!opened) {
            val event = awaitPointerEvent(pass = PointerEventPass.Initial)
            val pressed = event.changes.filter { it.pressed }
            if (pressed.size < 2) {
                if (pressed.isEmpty()) break
                continue
            }
            val first = pressed[0].position
            val second = pressed[1].position
            val distance = hypot((first.x - second.x).toDouble(), (first.y - second.y).toDouble()).toFloat()
            val start = initialDistance ?: distance.also { initialDistance = it }
            if (start > 0f && distance / start <= 0.70f) {
                event.changes.forEach { it.consume() }
                opened = true
                onPinch()
            }
        }
    }
}
