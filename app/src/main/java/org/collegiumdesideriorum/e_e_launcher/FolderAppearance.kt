package org.collegiumdesideriorum.e_e_launcher

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.clip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin

internal enum class FolderShape(override val titleRes: Int) : LocalizedTitle {
    DEFAULT(R.string.ui_default_folder),
    NONE(R.string.ui_no_background),
    ROUNDED_RECTANGLE(R.string.ui_rounded_rectangle),
    RECTANGLE(R.string.ui_rectangle),
    CIRCLE(R.string.ui_circle_oval),
    PILL(R.string.ui_capsule),
    HEXAGON(R.string.ui_hexagon),
    OCTAGON(R.string.ui_octagon),
    DIAMOND(R.string.ui_diamond),
    TRIANGLE(R.string.ui_triangle),
    TRAPEZOID(R.string.ui_trapezoid),
    PARALLELOGRAM(R.string.ui_parallelogram),
    STAR(R.string.ui_star),
    ROUNDED_STAR(R.string.ui_rounded_star),
    SHIELD(R.string.ui_shield),
    DROP(R.string.ui_drop),
    HEART(R.string.ui_heart),
    CLOUD(R.string.ui_cloud),
    FLOWER(R.string.ui_flower),
    LEAF(R.string.ui_leaf),
    CRESCENT(R.string.ui_crescent),
    CROSS(R.string.ui_cross),
    TICKET(R.string.ui_ticket),
    TAG(R.string.ui_tag),
    SQUIRCLE(R.string.ui_squircle),
    SUPERELLIPSE(R.string.ui_superellipse),
    FOLDER_TAB(R.string.ui_tabbed_folder)
}

internal fun folderShape(shape: FolderShape): Shape = when (shape) {
    FolderShape.DEFAULT -> RoundedCornerShape(17.dp)
    FolderShape.NONE -> androidx.compose.ui.graphics.RectangleShape
    FolderShape.ROUNDED_RECTANGLE -> RoundedCornerShape(22.dp)
    FolderShape.RECTANGLE -> RoundedCornerShape(2.dp)
    FolderShape.CIRCLE -> CircleShape
    FolderShape.PILL -> RoundedCornerShape(50)
    FolderShape.HEXAGON -> polygonShape(6, -30f)
    FolderShape.OCTAGON -> polygonShape(8, -22.5f)
    FolderShape.DIAMOND -> polygonShape(4, -90f)
    FolderShape.TRIANGLE -> polygonShape(3, -90f)
    FolderShape.TRAPEZOID -> GenericShape { size, _ ->
        moveTo(size.width * 0.20f, 0f); lineTo(size.width * 0.80f, 0f)
        lineTo(size.width, size.height); lineTo(0f, size.height); close()
    }
    FolderShape.PARALLELOGRAM -> GenericShape { size, _ ->
        moveTo(size.width * 0.22f, 0f); lineTo(size.width, 0f)
        lineTo(size.width * 0.78f, size.height); lineTo(0f, size.height); close()
    }
    FolderShape.STAR -> starShape(5, 0.45f)
    FolderShape.ROUNDED_STAR -> starShape(5, 0.58f)
    FolderShape.SHIELD -> GenericShape { size, _ ->
        moveTo(size.width * 0.08f, size.height * 0.10f)
        quadraticTo(size.width * 0.5f, 0f, size.width * 0.92f, size.height * 0.10f)
        lineTo(size.width * 0.84f, size.height * 0.68f)
        quadraticTo(size.width * 0.72f, size.height * 0.88f, size.width * 0.5f, size.height)
        quadraticTo(size.width * 0.28f, size.height * 0.88f, size.width * 0.16f, size.height * 0.68f)
        close()
    }
    FolderShape.DROP -> GenericShape { size, _ ->
        moveTo(size.width * 0.5f, 0f)
        cubicTo(size.width * 0.92f, size.height * 0.32f, size.width, size.height * 0.62f, size.width * 0.5f, size.height)
        cubicTo(0f, size.height * 0.62f, size.width * 0.08f, size.height * 0.32f, size.width * 0.5f, 0f)
        close()
    }
    FolderShape.HEART -> GenericShape { size, _ ->
        moveTo(size.width * 0.5f, size.height)
        cubicTo(size.width * 0.10f, size.height * 0.70f, -size.width * 0.05f, size.height * 0.30f, size.width * 0.22f, size.height * 0.12f)
        cubicTo(size.width * 0.38f, 0f, size.width * 0.5f, size.height * 0.12f, size.width * 0.5f, size.height * 0.25f)
        cubicTo(size.width * 0.5f, size.height * 0.12f, size.width * 0.62f, 0f, size.width * 0.78f, size.height * 0.12f)
        cubicTo(size.width * 1.05f, size.height * 0.30f, size.width * 0.90f, size.height * 0.70f, size.width * 0.5f, size.height)
        close()
    }
    FolderShape.CLOUD -> GenericShape { size, _ ->
        moveTo(size.width * 0.16f, size.height * 0.78f)
        cubicTo(-size.width * 0.02f, size.height * 0.72f, 0f, size.height * 0.42f, size.width * 0.22f, size.height * 0.42f)
        cubicTo(size.width * 0.24f, size.height * 0.12f, size.width * 0.62f, size.height * 0.08f, size.width * 0.70f, size.height * 0.34f)
        cubicTo(size.width, size.height * 0.28f, size.width * 1.08f, size.height * 0.70f, size.width * 0.82f, size.height * 0.80f)
        close()
    }
    FolderShape.FLOWER -> radialShape(8, 0.26f)
    FolderShape.LEAF -> GenericShape { size, _ ->
        moveTo(size.width * 0.08f, size.height * 0.82f)
        cubicTo(size.width * 0.12f, size.height * 0.22f, size.width * 0.55f, -size.height * 0.05f, size.width * 0.94f, size.height * 0.08f)
        cubicTo(size.width * 0.98f, size.height * 0.58f, size.width * 0.58f, size.height, size.width * 0.08f, size.height * 0.82f)
        close()
    }
    FolderShape.CRESCENT -> GenericShape { size, _ ->
        fillType = PathFillType.EvenOdd
        addOval(Rect(0f, 0f, size.width, size.height))
        addOval(Rect(size.width * 0.30f, -size.height * 0.04f, size.width * 1.04f, size.height * 0.78f))
    }
    FolderShape.CROSS -> GenericShape { size, _ ->
        val a = 0.30f; val b = 0.70f
        moveTo(size.width * a, 0f); lineTo(size.width * b, 0f)
        lineTo(size.width * b, size.height * a); lineTo(size.width, size.height * a)
        lineTo(size.width, size.height * b); lineTo(size.width * b, size.height * b)
        lineTo(size.width * b, size.height); lineTo(size.width * a, size.height)
        lineTo(size.width * a, size.height * b); lineTo(0f, size.height * b)
        lineTo(0f, size.height * a); lineTo(size.width * a, size.height * a); close()
    }
    FolderShape.TICKET -> GenericShape { size, _ ->
        moveTo(size.width * 0.10f, 0f); lineTo(size.width * 0.90f, 0f)
        quadraticTo(size.width * 0.86f, size.height * 0.25f, size.width, size.height * 0.30f)
        lineTo(size.width, size.height * 0.70f)
        quadraticTo(size.width * 0.86f, size.height * 0.75f, size.width * 0.90f, size.height)
        lineTo(size.width * 0.10f, size.height)
        quadraticTo(size.width * 0.14f, size.height * 0.75f, 0f, size.height * 0.70f)
        lineTo(0f, size.height * 0.30f)
        quadraticTo(size.width * 0.14f, size.height * 0.25f, size.width * 0.10f, 0f); close()
    }
    FolderShape.TAG -> GenericShape { size, _ ->
        moveTo(0f, size.height * 0.18f); lineTo(size.width * 0.72f, size.height * 0.18f)
        lineTo(size.width, size.height * 0.5f); lineTo(size.width * 0.72f, size.height * 0.82f)
        lineTo(0f, size.height * 0.82f); close()
    }
    FolderShape.SQUIRCLE -> superellipseShape(4.2f)
    FolderShape.SUPERELLIPSE -> superellipseShape(2.8f)
    FolderShape.FOLDER_TAB -> GenericShape { size, _ ->
        moveTo(0f, size.height * 0.18f); lineTo(size.width * 0.34f, size.height * 0.18f)
        lineTo(size.width * 0.44f, 0f); lineTo(size.width * 0.72f, 0f)
        lineTo(size.width * 0.82f, size.height * 0.18f); lineTo(size.width, size.height * 0.18f)
        lineTo(size.width, size.height); lineTo(0f, size.height); close()
    }
}

private fun polygonShape(sides: Int, rotationDegrees: Float) = GenericShape { size, _ ->
    val radiusX = size.width * 0.5f
    val radiusY = size.height * 0.5f
    repeat(sides) { index ->
        val angle = (rotationDegrees + index * 360f / sides) * PI.toFloat() / 180f
        val x = size.width * 0.5f + cos(angle) * radiusX
        val y = size.height * 0.5f + sin(angle) * radiusY
        if (index == 0) moveTo(x, y) else lineTo(x, y)
    }
    close()
}

private fun starShape(points: Int, innerRadius: Float) = GenericShape { size, _ ->
    repeat(points * 2) { index ->
        val radius = if (index % 2 == 0) 0.5f else innerRadius * 0.5f
        val angle = (-90f + index * 180f / points) * PI.toFloat() / 180f
        val x = size.width * 0.5f + cos(angle) * size.width * radius
        val y = size.height * 0.5f + sin(angle) * size.height * radius
        if (index == 0) moveTo(x, y) else lineTo(x, y)
    }
    close()
}

private fun radialShape(petals: Int, depth: Float) = GenericShape { size, _ ->
    repeat(petals * 2) { index ->
        val radius = if (index % 2 == 0) 0.5f else 0.5f - depth
        val angle = (-90f + index * 180f / petals) * PI.toFloat() / 180f
        val x = size.width * 0.5f + cos(angle) * size.width * radius
        val y = size.height * 0.5f + sin(angle) * size.height * radius
        if (index == 0) moveTo(x, y) else lineTo(x, y)
    }
    close()
}

private fun superellipseShape(power: Float) = GenericShape { size, _ ->
    val steps = 64
    repeat(steps) { index ->
        val angle = index * 2f * PI.toFloat() / steps
        val c = cos(angle)
        val s = sin(angle)
        val x = size.width * 0.5f + size.width * 0.5f * kotlin.math.sign(c) * kotlin.math.abs(c).pow(2f / power)
        val y = size.height * 0.5f + size.height * 0.5f * kotlin.math.sign(s) * kotlin.math.abs(s).pow(2f / power)
        if (index == 0) moveTo(x, y) else lineTo(x, y)
    }
    close()
}

@Composable
internal fun FolderAppearanceEditor(
    shape: FolderShape,
    colorHex: String,
    opacityPercent: Int,
    onShapeChange: (FolderShape) -> Unit,
    onColorChange: (String) -> Unit,
    onOpacityChange: (Int) -> Unit
) {
    var shapePickerOpen by remember { mutableStateOf(false) }
    Column(Modifier.padding(top = 4.dp)) {
        SettingsSectionTitle(l10n(R.string.ui_closed_folder_shape))
        Card(
            modifier = Modifier.fillMaxWidth().clickable { shapePickerOpen = true },
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.06f))
        ) {
            FolderShapePreviewRow(shape = shape, selected = true)
        }
        Spacer(Modifier.height(6.dp))
        VisualColorSetting(l10n(R.string.ui_folder_colour), colorHex, onColorChange)
        IntegerSliderSetting(l10n(R.string.ui_folder_opacity), opacityPercent, 0, 100, onValueChange = onOpacityChange)
    }
    if (shapePickerOpen) {
        AlertDialog(
            onDismissRequest = { shapePickerOpen = false },
            title = { Text(l10n(R.string.ui_closed_folder_shape)) },
            text = {
                LazyColumn {
                    items(FolderShape.values().toList(), key = { it.name }) { option ->
                        Row(
                            Modifier.fillMaxWidth().clickable {
                                onShapeChange(option)
                                shapePickerOpen = false
                            }
                        ) {
                            FolderShapePreviewRow(option, option == shape)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { shapePickerOpen = false }) { Text(l10n(R.string.ui_close)) }
            }
        )
    }
}

@Composable
private fun FolderShapePreviewRow(shape: FolderShape, selected: Boolean) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier.size(38.dp).clip(folderShape(shape)).background(
                if (shape == FolderShape.NONE) Color.Transparent
                else if (selected) Color(0xFF8BA8FF) else Color.White.copy(alpha = 0.30f)
            )
        )
        Spacer(Modifier.width(12.dp))
        Text(shape.title, modifier = Modifier.weight(1f))
        Text(if (selected) "✓" else "›", color = Color.White.copy(alpha = 0.66f))
    }
}
