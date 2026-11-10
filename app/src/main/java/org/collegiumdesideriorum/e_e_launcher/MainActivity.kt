package org.collegiumdesideriorum.e_e_launcher

import android.app.Activity
import android.app.AppOpsManager
import android.app.DatePickerDialog
import android.app.usage.UsageStatsManager
import android.app.WallpaperManager
import android.appwidget.AppWidgetHost
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProviderInfo
import android.content.ActivityNotFoundException
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.IntentFilter
import android.content.Intent
import android.content.pm.LauncherApps
import android.content.pm.PackageManager
import android.net.Uri
import android.graphics.ImageDecoder
import android.os.Bundle
import android.os.Process
import android.os.UserHandle
import android.provider.Settings
import android.provider.Telephony
import android.util.SizeF
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.border
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items as lazyItems
import androidx.compose.foundation.lazy.itemsIndexed as lazyItemsIndexed
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.withFrameNanos
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.layout.positionOnScreen
import androidx.compose.ui.zIndex
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.core.graphics.drawable.toBitmap
import androidx.core.view.WindowCompat
import androidx.activity.result.contract.ActivityResultContracts.OpenDocument
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import java.util.Calendar
import java.util.Date
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID
import kotlin.math.absoluteValue
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.hypot
import kotlin.math.ln
import kotlin.math.roundToInt
import kotlin.math.sin

class MainActivity : ComponentActivity() {
    companion object {
        internal const val APP_WIDGET_HOST_ID = 7319
        private const val INVALID_WIDGET_ID = AppWidgetManager.INVALID_APPWIDGET_ID
        private const val STARTUP_HEALTH_CHECK_DELAY_MS = 4_000L
    }

    lateinit var launcherAppWidgetHost: AppWidgetHost
        private set
    lateinit var launcherAppWidgetManager: AppWidgetManager
        private set

    var onWidgetAdded: ((appWidgetId: Int, info: AppWidgetProviderInfo, page: Int, replacementId: String?) -> Unit)? = null
    var onHomeInvoked: (() -> Unit)? = null
    var onLauncherResumed: (() -> Unit)? = null

    private var pendingWidgetId: Int = INVALID_WIDGET_ID
    private var pendingWidgetPage: Int = 0
    private var pendingWidgetReplacementId: String? = null
    private var widgetHostListening = false

    private val widgetConfigureLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            completeWidgetAddition()
        } else {
            cancelPendingWidget()
        }
    }

    private val widgetBindLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            continueWidgetAdditionAfterBinding()
        } else {
            cancelPendingWidget()
        }
    }

    private val homeRoleLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { }

    internal fun resetLauncherArea(area: LauncherResetArea): Boolean {
        if (!LauncherReset.apply(applicationContext, area)) return false
        // Release hosted widgets only after preference changes have succeeded.
        if ((area == LauncherResetArea.WORKSPACE || area == LauncherResetArea.ALL) &&
            ::launcherAppWidgetHost.isInitialized
        ) {
            launcherAppWidgetHost.appWidgetIds.forEach(::deleteHostedWidget)
        }
        if (area == LauncherResetArea.WORKSPACE) {
            // Reset means restore the original five-page demo layout, not an empty desktop.
            LauncherStorage.seedWelcomeWorkspace(applicationContext)
            seedWelcomeNoteWidgets()
        }
        return true
    }

    /** Clear only placed items. Keep page count, default page, effects and gestures. */
    internal fun clearWorkspaceItems(): Boolean {
        if (!LauncherReset.clearWorkspaceItems(applicationContext)) return false
        if (::launcherAppWidgetHost.isInitialized) {
            launcherAppWidgetHost.appWidgetIds.forEach(::deleteHostedWidget)
        }
        return true
    }

    /** Seed two regular host widgets on a new launcher profile. */
    fun seedWelcomeNoteWidgets() {
        val context = applicationContext
        val provider = ComponentName(context, LauncherNoteWidgetProvider::class.java)
        val existing = LauncherStorage.loadWidgets(context).toMutableList()
        if (existing.isNotEmpty()) return
        val home = LauncherStorage.loadDefaultWorkspacePage(context)
        val pageCount = LauncherStorage.loadPageCount(context)
        val initial = listOf(
            Triple(home, R.string.ui_note_welcome_home, 0.17f),
            Triple((home + 1).coerceAtMost(pageCount - 1), R.string.ui_note_welcome_next, 0.21f)
        )
        initial.forEach { (page, textRes, y) ->
            val id = launcherAppWidgetHost.allocateAppWidgetId()
            val bound = runCatching {
                launcherAppWidgetManager.bindAppWidgetIdIfAllowed(id, provider)
            }.getOrDefault(false)
            if (!bound) {
                runCatching { launcherAppWidgetHost.deleteAppWidgetId(id) }
                LauncherDiagnostics.info("welcome note widget binding refused for page=$page")
                return@forEach
            }
            runCatching {
                launcherAppWidgetManager.updateAppWidgetOptions(id, Bundle().apply {
                    putInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, 286)
                    putInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH, 286)
                    putInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, 134)
                    putInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT, 134)
                })
            }
            LauncherNoteWidgetStore.set(context, id, context.getString(textRes), customized = false)
            existing.add(WorkspaceWidget(
                id = UUID.randomUUID().toString(),
                appWidgetId = id,
                provider = provider.flattenToString(),
                page = page, xFraction = 0.13f, yFraction = y,
                widthDp = 286, heightDp = 134
            ))
        }
        if (existing.isNotEmpty()) LauncherStorage.saveWidgets(context, existing)
    }

    fun requestHomeRole() = requestHomeRole(homeRoleLauncher)

    @Suppress("DEPRECATION")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Activity recreation (including backup restore) must not retain a
        // stale status-bar inset from the previous window.
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        LauncherDiagnostics.logDeviceOnce(applicationContext)
        AdvancedLauncherStorage.installCrashHandler(applicationContext)
        val enteredRecoveryMode = AdvancedLauncherStorage.beginStartup(this)
        launcherAppWidgetManager = AppWidgetManager.getInstance(this)
        launcherAppWidgetHost = SnapshotAppWidgetHost(this, APP_WIDGET_HOST_ID)
        LauncherDiagnostics.info("widget host created hostId=$APP_WIDGET_HOST_ID")
        setContent {
            EeLauncherApp()
        }
        window.decorView.postDelayed(
            { AdvancedLauncherStorage.markStartupSuccessful(applicationContext) },
            STARTUP_HEALTH_CHECK_DELAY_MS
        )
        if (enteredRecoveryMode) {
            Toast.makeText(this, R.string.ui_safe_mode_started, Toast.LENGTH_LONG).show()
        }
    }

    override fun onConfigurationChanged(newConfig: android.content.res.Configuration) {
        super.onConfigurationChanged(newConfig)
        LauncherDiagnostics.info(
            "activity configuration changed orientation=${newConfig.orientation} " +
                "size=${newConfig.screenWidthDp}x${newConfig.screenHeightDp}dp"
        )
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        onHomeInvoked?.invoke()
    }

    override fun onStart() {
        super.onStart()
        if (!widgetHostListening) {
            runCatching { launcherAppWidgetHost.startListening() }
                .onSuccess {
                    widgetHostListening = true
                    LauncherDiagnostics.info(
                        "widget host listening ids=${launcherAppWidgetHost.appWidgetIds.joinToString()}"
                    )
                }
                .onFailure { LauncherDiagnostics.error("widget host startListening failed", it) }
        }
    }

    @Suppress("DEPRECATION")
    override fun onResume() {
        super.onResume()
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        onLauncherResumed?.invoke()
    }

    override fun onStop() {
        if (widgetHostListening) {
            runCatching { launcherAppWidgetHost.stopListening() }
                .onFailure { LauncherDiagnostics.warning("widget host stopListening failed", it) }
            widgetHostListening = false
            LauncherDiagnostics.info("widget host stopped")
        }
        super.onStop()
    }

    fun hasPendingWidgetRequest(): Boolean = pendingWidgetId != INVALID_WIDGET_ID

    fun requestBindWidget(provider: ComponentName, page: Int, replacementId: String? = null) {
        cancelPendingWidget()
        pendingWidgetPage = page.coerceAtLeast(0)
        pendingWidgetReplacementId = replacementId
        pendingWidgetId = launcherAppWidgetHost.allocateAppWidgetId()
        LauncherDiagnostics.info(
            "widget bind begin id=$pendingWidgetId provider=${provider.flattenToShortString()} " +
                "replacement=${replacementId != null}"
        )

        val boundImmediately = runCatching {
            launcherAppWidgetManager.bindAppWidgetIdIfAllowed(pendingWidgetId, provider)
        }.getOrDefault(false)

        if (boundImmediately) {
            LauncherDiagnostics.info("widget bind allowed immediately id=$pendingWidgetId")
            continueWidgetAdditionAfterBinding()
            return
        }
        LauncherDiagnostics.info("widget bind permission activity required id=$pendingWidgetId")

        val bindIntent = Intent(AppWidgetManager.ACTION_APPWIDGET_BIND).apply {
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, pendingWidgetId)
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_PROVIDER, provider)
        }
        runCatching { widgetBindLauncher.launch(bindIntent) }
            .onFailure {
                cancelPendingWidget()
                Toast.makeText(this, l10n(R.string.ui_failed_to_request_widget_permission), Toast.LENGTH_SHORT).show()
            }
    }

    private fun continueWidgetAdditionAfterBinding() {
        val id = pendingWidgetId
        if (id == INVALID_WIDGET_ID) return
        val info = launcherAppWidgetManager.getAppWidgetInfo(id)
        if (info == null) {
            LauncherDiagnostics.error("widget provider info missing after bind id=$id")
            cancelPendingWidget()
            Toast.makeText(this, l10n(R.string.ui_failed_to_obtain_widget_data), Toast.LENGTH_SHORT).show()
            return
        }

        // A restored widget may receive a completely new system appWidgetId.
        // Reapply provider-specific options (for example a provider's own stable
        // profile ID) before its configuration Activity is opened.
        pendingWidgetReplacementId?.let { replacementId ->
            LauncherSettingsBackup.applyPendingWidgetOptions(
                applicationContext,
                replacementId,
                id
            )
        }

        val configureComponent = info.configure
        if (configureComponent != null) {
            LauncherDiagnostics.info(
                "widget configuration begin id=$id component=${configureComponent.flattenToShortString()}"
            )
            val configureIntent = Intent(AppWidgetManager.ACTION_APPWIDGET_CONFIGURE).apply {
                component = configureComponent
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, id)
            }
            runCatching { widgetConfigureLauncher.launch(configureIntent) }
                .onFailure {
                    cancelPendingWidget()
                    Toast.makeText(this, l10n(R.string.ui_failed_to_open_widget_configuration), Toast.LENGTH_SHORT).show()
                }
        } else {
            LauncherDiagnostics.info("widget has no configuration id=$id")
            completeWidgetAddition()
        }
    }

    fun deleteHostedWidget(appWidgetId: Int) {
        LauncherNoteWidgetStore.remove(this, appWidgetId)
        HostedWidgetSnapshotCache.remove(appWidgetId)
        runCatching { launcherAppWidgetHost.deleteAppWidgetId(appWidgetId) }
    }

    fun cleanupOrphanedHostedWidgets(referencedIds: Set<Int>) {
        if (!::launcherAppWidgetHost.isInitialized || !::launcherAppWidgetManager.isInitialized) return
        val orphanIds = launcherAppWidgetHost.appWidgetIds
            .filter { it != pendingWidgetId && it !in referencedIds }
        if (orphanIds.isEmpty()) return

        // During Android's own backup restore the host can receive new IDs just
        // before ACTION_APPWIDGET_HOST_RESTORED translates the persisted old IDs.
        // If that pattern is visible, defer cleanup rather than deleting a valid
        // freshly restored binding as an apparent orphan.
        val hasBrokenReferencedId = referencedIds.any { referencedId ->
            runCatching { launcherAppWidgetManager.getAppWidgetInfo(referencedId) }.getOrNull() == null
        }
        val hasValidOrphanId = orphanIds.any { orphanId ->
            runCatching { launcherAppWidgetManager.getAppWidgetInfo(orphanId) }.getOrNull() != null
        }
        if (hasBrokenReferencedId && hasValidOrphanId) {
            LauncherDiagnostics.info("orphan widget cleanup deferred while restore mapping may be pending")
            return
        }

        orphanIds.forEach { orphanId ->
            LauncherDiagnostics.info("deleting orphaned hosted widget id=$orphanId")
            deleteHostedWidget(orphanId)
        }
    }

    private fun completeWidgetAddition() {
        val id = pendingWidgetId
        if (id == INVALID_WIDGET_ID) return
        val info = launcherAppWidgetManager.getAppWidgetInfo(id)
        if (info == null) {
            LauncherDiagnostics.error("widget completion missing provider info id=$id")
            cancelPendingWidget()
            return
        }
        val page = pendingWidgetPage
        val replacementId = pendingWidgetReplacementId
        pendingWidgetId = INVALID_WIDGET_ID
        pendingWidgetReplacementId = null
        LauncherDiagnostics.info(
            "widget addition complete id=$id provider=${info.provider.flattenToShortString()} page=$page"
        )
        onWidgetAdded?.invoke(id, info, page, replacementId)
    }

    private fun cancelPendingWidget() {
        val id = pendingWidgetId
        pendingWidgetId = INVALID_WIDGET_ID
        pendingWidgetReplacementId = null
        if (id != INVALID_WIDGET_ID && ::launcherAppWidgetHost.isInitialized) {
            runCatching { launcherAppWidgetHost.deleteAppWidgetId(id) }
            LauncherDiagnostics.info("widget pending id deleted id=$id")
        }
    }
}

data class AppItem(
    val label: String,
    val packageName: String,
    val componentName: ComponentName,
    val user: UserHandle,
    val icon: ImageBitmap?,
    val installTimeMillis: Long = 0L,
    val lastUsedMillis: Long = 0L,
    val originalLabel: String = label,
    val customIconUri: String? = null
)

private data class AppCustomization(
    val label: String? = null,
    val iconUri: String? = null,
    val iconPackPackage: String? = null,
    val iconPackDrawable: String? = null
)

private fun componentPackageName(componentKey: String?): String? {
    if (componentKey.isNullOrBlank() || launcherActionOptionForComponent(componentKey) != null) return null
    return ComponentName.unflattenFromString(componentKey)?.packageName
        ?: componentKey.substringBefore('/').takeIf { it.isNotBlank() }
}

private fun componentBelongsToPackage(componentKey: String?, packageName: String): Boolean =
    componentPackageName(componentKey) == packageName

private fun isPackageInstalled(packageManager: PackageManager, packageName: String): Boolean =
    runCatching { packageManager.getPackageInfo(packageName, 0) }.isSuccess

private fun repairedComponentKey(
    storedComponent: String,
    appsByComponent: Map<String, AppItem>,
    appsByPackage: Map<String, List<AppItem>>,
    packageManager: PackageManager
): String {
    if (storedComponent in appsByComponent) return storedComponent
    val packageName = componentPackageName(storedComponent) ?: return storedComponent
    val candidates = appsByPackage[packageName].orEmpty()
    if (candidates.isEmpty()) return storedComponent

    // Package updates may replace the launcher Activity or an activity-alias while
    // preserving the package. Prefer Android's current default launch component,
    // then fall back to the only/first launcher activity exposed by LauncherApps.
    val currentLaunchComponent = runCatching {
        packageManager.getLaunchIntentForPackage(packageName)?.component?.flattenToString()
    }.getOrNull()
    if (currentLaunchComponent != null && currentLaunchComponent in appsByComponent) {
        return currentLaunchComponent
    }
    return candidates.first().componentName.flattenToString()
}

private data class TextShadowSettings(
    val enabled: Boolean = true,
    val offsetDp: Int = 2,
    val blurDp: Int = 4,
    val colorHex: String = "#000000",
    val opacityPercent: Int = 65
)

private data class WorkspaceShortcut(
    val id: String,
    val label: String,
    val componentName: String,
    val page: Int,
    val xFraction: Float,
    val yFraction: Float,
    val intentAction: String? = null,
    val customIconUri: String? = null,
    val customIconPackPackage: String? = null,
    val customIconPackDrawable: String? = null
)

/** Finds the closest free rectangle in physical dp, not on a fixed grid.
 * Stored fractions refer to the movable range of each item's own size.
 * Keeping that distinction is critical for detecting collisions with widgets. */
private fun vacantWorkspacePosition(
    page: Int,
    preferredX: Float,
    preferredY: Float,
    shortcuts: List<WorkspaceShortcut>,
    folders: List<WorkspaceFolder>,
    widgets: List<WorkspaceWidget>,
    screenWidthDp: Int,
    screenHeightDp: Int,
    notes: List<WorkspaceNote> = emptyList(),
    elementWidthDp: Int = 78,
    elementHeightDp: Int = 96
): Pair<Float, Float> {
    val canvasWidth = screenWidthDp.coerceAtLeast(1).toFloat()
    // The workspace is laid out above the bottom dock/indicator region.
    val canvasHeight = (screenHeightDp - 108).coerceAtLeast(1).toFloat()
    val width = elementWidthDp.toFloat().coerceIn(1f, canvasWidth)
    val height = elementHeightDp.toFloat().coerceIn(1f, canvasHeight)
    val maxX = (canvasWidth - width).coerceAtLeast(0f)
    val maxY = (canvasHeight - height).coerceAtLeast(0f)
    val iconMaxX = (canvasWidth - 78f).coerceAtLeast(0f)
    val iconMaxY = (canvasHeight - 96f).coerceAtLeast(0f)
    // The anchor from a long press is centred on an icon. Centre larger items
    // under that same finger position as closely as the screen permits.
    val targetX = (preferredX.coerceIn(0f, 1f) * iconMaxX + 39f - width / 2f).coerceIn(0f, maxX)
    val targetY = (preferredY.coerceIn(0f, 1f) * iconMaxY + 48f - height / 2f).coerceIn(0f, maxY)
    val occupied = buildList<FloatArray> {
        shortcuts.filter { it.page == page }.forEach {
            add(floatArrayOf(it.xFraction.coerceIn(0f, 1f) * iconMaxX,
                it.yFraction.coerceIn(0f, 1f) * iconMaxY, 78f, 96f))
        }
        folders.filter { it.page == page }.forEach {
            add(floatArrayOf(it.xFraction.coerceIn(0f, 1f) * iconMaxX,
                it.yFraction.coerceIn(0f, 1f) * iconMaxY, 78f, 96f))
        }
        widgets.filter { it.page == page }.forEach {
            val w = it.widthDp.toFloat().coerceIn(1f, canvasWidth)
            val h = it.heightDp.toFloat().coerceIn(1f, canvasHeight)
            add(floatArrayOf(it.xFraction.coerceIn(0f, 1f) * (canvasWidth - w),
                it.yFraction.coerceIn(0f, 1f) * (canvasHeight - h), w, h))
        }
        notes.filter { it.page == page }.forEach {
            val w = it.widthDp.toFloat().coerceIn(1f, canvasWidth)
            val h = it.heightDp.toFloat().coerceIn(1f, canvasHeight)
            add(floatArrayOf(it.xFraction.coerceIn(0f, 1f) * (canvasWidth - w),
                it.yFraction.coerceIn(0f, 1f) * (canvasHeight - h), w, h))
        }
    }
    val gap = 3f // Only a few dp/pixels of separation are needed.
    fun intersects(x: Float, y: Float, rect: FloatArray): Boolean =
        x < rect[0] + rect[2] + gap && x + width + gap > rect[0] &&
            y < rect[1] + rect[3] + gap && y + height + gap > rect[1]
    if (occupied.none { intersects(targetX, targetY, it) }) {
        return (if (maxX > 0f) targetX / maxX else 0f) to
            (if (maxY > 0f) targetY / maxY else 0f)
    }
    // The closest free position occurs at a requested coordinate, screen
    // boundary or obstacle edge. Build those candidate edges directly;
    // unlike a coarse lattice this does not waste a nearly empty gap.
    val xs = buildList {
        add(targetX); add(0f); add(maxX)
        occupied.forEach { rect ->
            add((rect[0] - width - gap).coerceIn(0f, maxX))
            add((rect[0] + rect[2] + gap).coerceIn(0f, maxX))
        }
    }.distinct()
    val ys = buildList {
        add(targetY); add(0f); add(maxY)
        occupied.forEach { rect ->
            add((rect[1] - height - gap).coerceIn(0f, maxY))
            add((rect[1] + rect[3] + gap).coerceIn(0f, maxY))
        }
    }.distinct()
    var bestX = targetX
    var bestY = targetY
    var distance = Float.POSITIVE_INFINITY
    for (x in xs) for (y in ys) {
        val dx = x - targetX
        val dy = y - targetY
        val score = dx * dx + dy * dy
        if (score >= distance) continue
        if (occupied.none { intersects(x, y, it) }) {
            bestX = x; bestY = y; distance = score
        }
    }
    return (if (maxX > 0f) bestX / maxX else 0f) to
        (if (maxY > 0f) bestY / maxY else 0f)
}

/** Convert a drop from an Android Dialog's separate window to the workspace's
 * actual screen-space content. The stored fraction describes the top-left
 * position of a 78 x 96 dp icon, not a fraction of the whole display. */
internal fun workspaceFolderDropFractions(
    screenX: Float, screenY: Float,
    workspaceLeft: Float, workspaceTop: Float,
    workspaceWidthPx: Float, workspaceHeightPx: Float,
    density: Float
): Pair<Float, Float> {
    val scale = density.coerceAtLeast(0.01f)
    val iconWidth = 78f * scale
    val iconHeight = 96f * scale
    val dockReserved = 108f * scale
    val movableX = (workspaceWidthPx - iconWidth).coerceAtLeast(1f)
    val movableY = (workspaceHeightPx - dockReserved - iconHeight).coerceAtLeast(1f)
    return ((screenX - workspaceLeft - iconWidth / 2f) / movableX).safeWorkspaceFraction() to
        ((screenY - workspaceTop - iconHeight / 2f) / movableY).safeWorkspaceFraction()
}

private data class WorkspaceWidget(
    val id: String,
    val appWidgetId: Int,
    val provider: String,
    val page: Int,
    val xFraction: Float,
    val yFraction: Float,
    val widthDp: Int,
    val heightDp: Int
)

/**
 * Builds the lightweight widget list used by the action-chain settings page.
 *
 * Keep this outside LauncherHome's settings content lambda: that lambda already
 * captures most launcher state and is close to the JVM 64 KiB method limit.
 * Resolving providers/labels inline there can make Compose generate an oversized
 * LauncherHome$lambda method.
 */
private fun buildWidgetActionWidgetDescriptors(
    activity: MainActivity,
    widgets: List<WorkspaceWidget>
): List<WidgetActionWidgetDescriptor> = widgets.map { widget ->
    val info = runCatching {
        activity.launcherAppWidgetManager.getAppWidgetInfo(widget.appWidgetId)
    }.getOrNull()
    val provider = ComponentName.unflattenFromString(widget.provider)
    val title = runCatching { info?.loadLabel(activity.packageManager) }
        .getOrNull()
        ?.takeIf { it.isNotBlank() }
        ?: provider?.className?.substringAfterLast('.')
        ?: l10n(R.string.ui_widget)
    WidgetActionWidgetDescriptor(
        id = widget.id,
        title = title,
        subtitle = l10n(
            R.string.ui_widget_page_provider,
            widget.page + 1,
            provider?.packageName ?: widget.provider
        )
    )
}

private data class WorkspaceFolder(
    val id: String,
    val label: String,
    val page: Int,
    val xFraction: Float,
    val yFraction: Float,
    val componentNames: List<String>,
    val shape: FolderShape = FolderShape.DEFAULT,
    val colorHex: String = "#4F5872",
    val opacityPercent: Int = 67,
    val customIconUri: String? = null,
    val customIconPackPackage: String? = null,
    val customIconPackDrawable: String? = null
)

private data class SideDockEntry(
    val id: String,
    val label: String,
    val componentName: String? = null,
    val intentAction: String? = null,
    val componentNames: List<String> = emptyList(),
    val folder: Boolean = false,
    val folderShape: FolderShape = FolderShape.DEFAULT,
    val folderColorHex: String = "#4F5872",
    val folderOpacityPercent: Int = 67,
    val customIconUri: String? = null,
    val customIconPackPackage: String? = null,
    val customIconPackDrawable: String? = null
) {
    val isFolder: Boolean get() = folder || componentNames.isNotEmpty()
}

private data class BottomDockEntry(
    val id: String,
    val label: String,
    val componentName: String? = null,
    val intentAction: String? = null,
    val page: Int = 0,
    val componentNames: List<String> = emptyList(),
    val folder: Boolean = false,
    val folderShape: FolderShape = FolderShape.DEFAULT,
    val folderColorHex: String = "#4F5872",
    val folderOpacityPercent: Int = 67,
    val customIconUri: String? = null,
    val customIconPackPackage: String? = null,
    val customIconPackDrawable: String? = null
) {
    val isFolder: Boolean get() = folder || componentNames.isNotEmpty()
}

private class LauncherSettingsExtensions(
    val appLaunchDelay: Int,
    val appLaunchAnimationDuration: Int,
    val appDragAnimationEffect: AppLaunchAnimationEffect,
    val appDragAnimationDuration: Int,
    val linkLaunchDelayToAnimation: Boolean,
    val appDrawerDragOverviewMode: AppDrawerDragOverviewMode,
    val appDrawerCrossPageDragMode: AppDrawerCrossPageDragMode,
    val appDrawerPagePreviewMode: AppDrawerPagePreviewMode,
    val appDrawerDragPageHover: Int,
    val appDrawerQuickAction: AppDrawerQuickAction,
    val appDrawerWorkspaceEdgeSwipeEnabled: Boolean,
    val newAppCutoffMillis: Long,
    val newAppEmblemShape: NewAppEmblemShape,
    val newAppEmblemSize: Int,
    val newAppEmblemColor: String,
    val newAppEmblemColorB: String,
    val newAppEmblemGradient: Boolean,
    val newAppEmblemX: Int,
    val newAppEmblemY: Int,
    val newAppEmblemImageUri: String?,
    val pinchAction: DesktopGestureAction,
    val multiSwipeActions: Map<Pair<Int, SwipeDirection>, DesktopGestureAction>,
    val bottomDockCapacity: Int,
    val keepUninstalledAppShortcuts: Boolean,
    val onAppLaunchDelayChange: (Int) -> Unit,
    val onAppLaunchAnimationDurationChange: (Int) -> Unit,
    val onAppDragAnimationEffectChange: (AppLaunchAnimationEffect) -> Unit,
    val onAppDragAnimationDurationChange: (Int) -> Unit,
    val onLinkLaunchDelayToAnimationChange: (Boolean) -> Unit,
    val onAppDrawerDragOverviewModeChange: (AppDrawerDragOverviewMode) -> Unit,
    val onAppDrawerCrossPageDragModeChange: (AppDrawerCrossPageDragMode) -> Unit,
    val onAppDrawerPagePreviewModeChange: (AppDrawerPagePreviewMode) -> Unit,
    val onAppDrawerDragPageHoverChange: (Int) -> Unit,
    val onAppDrawerQuickActionChange: (AppDrawerQuickAction) -> Unit,
    val onAppDrawerWorkspaceEdgeSwipeEnabledChange: (Boolean) -> Unit,
    val onNewAppCutoffChange: (Long) -> Unit,
    val onNewAppEmblemShapeChange: (NewAppEmblemShape) -> Unit,
    val onNewAppEmblemSizeChange: (Int) -> Unit,
    val onNewAppEmblemColorChange: (String) -> Unit,
    val onNewAppEmblemColorBChange: (String) -> Unit,
    val onNewAppEmblemGradientChange: (Boolean) -> Unit,
    val onNewAppEmblemXChange: (Int) -> Unit,
    val onNewAppEmblemYChange: (Int) -> Unit,
    val onChooseNewAppEmblemImage: () -> Unit,
    val onClearNewAppEmblemImage: () -> Unit,
    val onChooseNewAppEmblemIconPack: () -> Unit,
    val onClearNewAppEmblemIconPack: () -> Unit,
    val onPinchActionChange: (DesktopGestureAction) -> Unit,
    val onMultiSwipeActionChange: (Int, SwipeDirection, DesktopGestureAction) -> Unit,
    val onBottomDockCapacityChange: (Int) -> Unit,
    val onKeepUninstalledAppShortcutsChange: (Boolean) -> Unit
)

private class LauncherInteractionExtras(context: Context) {
    private val appContext = context.applicationContext

    var dragAnimationEffect by mutableStateOf(LauncherStorage.loadAppDragAnimation(appContext))
        private set
    var dragAnimationDuration by mutableIntStateOf(LauncherStorage.loadAppDragAnimationDuration(appContext))
        private set
    var linkLaunchDelayToAnimation by mutableStateOf(LauncherStorage.loadLinkLaunchDelayToAnimation(appContext))
        private set
    var crossPageDragMode by mutableStateOf(LauncherStorage.loadAppDrawerCrossPageDragMode(appContext))
        private set
    var pagePreviewMode by mutableStateOf(LauncherStorage.loadAppDrawerPagePreviewMode(appContext))
        private set
    var workspaceEdgeSwipeEnabled by mutableStateOf(LauncherStorage.loadAppDrawerWorkspaceEdgeSwipeEnabled(appContext))
        private set
    var newAppCutoffMillis by mutableStateOf(LauncherStorage.ensureNewAppCutoff(appContext))
        private set

    fun updateDragAnimationEffect(value: AppLaunchAnimationEffect) {
        dragAnimationEffect = value
        LauncherStorage.saveAppDragAnimation(appContext, value)
    }

    fun updateDragAnimationDuration(value: Int) {
        dragAnimationDuration = value.coerceIn(100, 1000)
        LauncherStorage.saveAppDragAnimationDuration(appContext, dragAnimationDuration)
    }

    fun updateLinkLaunchDelayToAnimation(value: Boolean) {
        linkLaunchDelayToAnimation = value
        LauncherStorage.saveLinkLaunchDelayToAnimation(appContext, value)
    }

    fun updateCrossPageDragMode(value: AppDrawerCrossPageDragMode) {
        crossPageDragMode = value
        LauncherStorage.saveAppDrawerCrossPageDragMode(appContext, value)
    }

    fun updatePagePreviewMode(value: AppDrawerPagePreviewMode) {
        pagePreviewMode = value
        LauncherStorage.saveAppDrawerPagePreviewMode(appContext, value)
    }

    fun updateWorkspaceEdgeSwipeEnabled(value: Boolean) {
        workspaceEdgeSwipeEnabled = value
        LauncherStorage.saveAppDrawerWorkspaceEdgeSwipeEnabled(appContext, value)
    }

    fun setNewAppCutoff(value: Long) {
        newAppCutoffMillis = value
        LauncherStorage.saveNewAppCutoff(appContext, value, manual = true)
    }

}

internal const val DOCK_ACTION_PREFIX = "org.collegiumdesideriorum.e_e_launcher.dock."
private const val LEGACY_DOCK_ACTION_PREFIX = "animlauncher.dock."
internal const val DOCK_ACTION_PHONE = "org.collegiumdesideriorum.e_e_launcher.dock.PHONE"
internal const val DOCK_ACTION_FILES = "org.collegiumdesideriorum.e_e_launcher.dock.FILES"
internal const val DOCK_ACTION_APPS = "org.collegiumdesideriorum.e_e_launcher.dock.APPS"
internal const val DOCK_ACTION_MAPS = "org.collegiumdesideriorum.e_e_launcher.dock.MAPS"
internal const val DOCK_ACTION_MESSAGES = "org.collegiumdesideriorum.e_e_launcher.dock.MESSAGES"

private const val LAUNCHER_ACTION_APP_DRAWER = "org.collegiumdesideriorum.e_e_launcher.action.APP_DRAWER"
private const val LAUNCHER_ACTION_SIDE_DOCK = "org.collegiumdesideriorum.e_e_launcher.action.SIDE_DOCK"
private const val LAUNCHER_ACTION_SETTINGS = "org.collegiumdesideriorum.e_e_launcher.action.SETTINGS"
private const val LAUNCHER_ACTION_ENVIRONMENT_EFFECTS = "org.collegiumdesideriorum.e_e_launcher.action.ENVIRONMENT_EFFECTS"
private const val LAUNCHER_ACTION_WORKSPACE_EFFECTS = "org.collegiumdesideriorum.e_e_launcher.action.WORKSPACE_EFFECTS"
private const val LAUNCHER_ACTION_APP_DRAWER_EFFECTS = "org.collegiumdesideriorum.e_e_launcher.action.APP_DRAWER_EFFECTS"
private const val LAUNCHER_ACTION_GESTURES = "org.collegiumdesideriorum.e_e_launcher.action.GESTURES"
private const val LAUNCHER_ACTION_WALLPAPER = "org.collegiumdesideriorum.e_e_launcher.action.WALLPAPER"
private const val LEGACY_LAUNCHER_ACTION_PREFIX = "animlauncher.action."

private fun canonicalLauncherAction(action: String?): String? = when {
    action == null -> null
    action.startsWith(LEGACY_DOCK_ACTION_PREFIX) ->
        DOCK_ACTION_PREFIX + action.removePrefix(LEGACY_DOCK_ACTION_PREFIX)
    action.startsWith(LEGACY_LAUNCHER_ACTION_PREFIX) ->
        "org.collegiumdesideriorum.e_e_launcher.action." + action.removePrefix(LEGACY_LAUNCHER_ACTION_PREFIX)
    else -> action
}

private fun isSystemDockAction(action: String?): Boolean =
    canonicalLauncherAction(action)?.startsWith(DOCK_ACTION_PREFIX) == true

private fun mainMessagingIntent(context: Context): Intent {
    val defaultPackage = Telephony.Sms.getDefaultSmsPackage(context)
    return defaultPackage
        ?.let { context.packageManager.getLaunchIntentForPackage(it) }
        ?: Intent.makeMainSelectorActivity(Intent.ACTION_MAIN, Intent.CATEGORY_APP_MESSAGING)
}

private fun bottomDockDataPage(selectedPage: Int, pageCount: Int): Int {
    val safePages = pageCount.coerceIn(1, 5)
    return Math.floorMod(selectedPage - safePages / 2, safePages)
}

private fun bottomDockPageIndicatorBottomPaddingDp(
    dockVisible: Boolean,
    iconVerticalOffsetDp: Int,
    shelfVerticalOffsetDp: Int,
    shelfHeightDp: Int,
    shelfShape: DockShelfShape,
    indicatorGapDp: Int
): Int {
    if (!dockVisible) return 12
    val iconTopDp = 6 + iconVerticalOffsetDp.coerceIn(-32, 32)
    val shelfTopDp = if (shelfShape != DockShelfShape.NONE && shelfHeightDp > 0) {
        6 + (92 - shelfHeightDp.coerceIn(1, 92)) + shelfVerticalOffsetDp.coerceIn(-32, 32)
    } else {
        Int.MAX_VALUE
    }
    val highestElementTopDp = minOf(iconTopDp, shelfTopDp)
    return (104 - highestElementTopDp + indicatorGapDp.coerceIn(0, 32)).coerceIn(12, 172)
}

/** Automatic first-use drawer grid: adapt to available dp, never override a stored manual value. */
private fun adaptiveDrawerGrid(screenWidthDp: Int, screenHeightDp: Int): Pair<Int, Int> {
    val width = (screenWidthDp - 44).coerceAtLeast(240)
    val height = (screenHeightDp - 168).coerceAtLeast(300)
    val targetCellWidth = if (screenWidthDp >= 600) 86f else 74f
    val columns = (width / targetCellWidth).roundToInt().coerceIn(4, 10)
    val rows = (height / 102f).roundToInt().coerceIn(4, 10)
    return columns to rows
}

private fun appDrawerHorizontalSpacingDp(columns: Int): Int = when {
    columns >= 9 -> 2
    columns >= 7 -> 4
    else -> 8
}

private fun appDrawerVerticalSpacingDp(rows: Int): Int = when {
    rows >= 16 -> 1
    rows >= 11 -> 2
    rows >= 8 -> 4
    else -> 8
}

private fun appDrawerMaxItemWidth(screenWidthDp: Int, columns: Int): Int {
    val availableWidth = (screenWidthDp - 52).coerceAtLeast(120)
    return ((availableWidth - (columns - 1) * appDrawerHorizontalSpacingDp(columns)) / columns)
        .coerceAtLeast(12)
}

private fun appDrawerMaxItemHeight(
    screenHeightDp: Int,
    rows: Int,
    bottomBarHeightDp: Int = 84
): Int {
    val availableHeight = (screenHeightDp - 56 - bottomBarHeightDp).coerceAtLeast(160)
    return ((availableHeight - (rows - 1) * appDrawerVerticalSpacingDp(rows)) / rows)
        .coerceAtLeast(12)
}

private data class LauncherActionOption(
    @param:StringRes private val titleRes: Int,
    val action: String
) {
    val title: String get() = l10n(titleRes)
}

private val launcherActionOptions = listOf(
    LauncherActionOption(R.string.ui_app_drawer, LAUNCHER_ACTION_APP_DRAWER),
    LauncherActionOption(R.string.ui_side_dock, LAUNCHER_ACTION_SIDE_DOCK),
    LauncherActionOption(R.string.ui_desktop_effect, LAUNCHER_ACTION_WORKSPACE_EFFECTS),
    LauncherActionOption(R.string.ui_app_drawer_effect, LAUNCHER_ACTION_APP_DRAWER_EFFECTS),
    LauncherActionOption(R.string.ui_open_environment_effects, LAUNCHER_ACTION_ENVIRONMENT_EFFECTS),
    LauncherActionOption(R.string.ui_wallpaper, LAUNCHER_ACTION_WALLPAPER),
    LauncherActionOption(R.string.ui_gestures, LAUNCHER_ACTION_GESTURES),
    LauncherActionOption(R.string.ui_open_launcher_settings, LAUNCHER_ACTION_SETTINGS)
)

private fun launcherActionOptionForComponent(component: String): LauncherActionOption? =
    launcherActionOptions.firstOrNull { it.action == canonicalLauncherAction(component) }

private fun shortLauncherActionName(title: String): String {
    val prefix = l10n(R.string.ui_open_prefix).trim()
    val clean = if (prefix.isNotEmpty() && title.startsWith(prefix, ignoreCase = true)) {
        title.substring(prefix.length).trimStart()
    } else title.trim()
    return clean.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
}

private fun launcherActionDisplayName(component: String): String? =
    launcherActionOptionForComponent(component)?.title?.let(::shortLauncherActionName)

private fun launcherActionIconRes(action: String?): Int = when (canonicalLauncherAction(action)) {
    LAUNCHER_ACTION_APP_DRAWER -> R.drawable.ic_apps
    LAUNCHER_ACTION_SIDE_DOCK -> R.drawable.ic_drag
    LAUNCHER_ACTION_SETTINGS -> R.drawable.ic_settings
    LAUNCHER_ACTION_WORKSPACE_EFFECTS -> R.drawable.ic_workspace_effect
    LAUNCHER_ACTION_APP_DRAWER_EFFECTS -> R.drawable.ic_app_drawer_effect
    LAUNCHER_ACTION_ENVIRONMENT_EFFECTS -> R.drawable.ic_environment
    LAUNCHER_ACTION_GESTURES -> R.drawable.ic_drag
    LAUNCHER_ACTION_WALLPAPER -> R.drawable.ic_workspace_effect
    else -> R.drawable.ic_settings
}

private enum class AppDrawerBackgroundMode(
    override val titleRes: Int,
    override val descriptionRes: Int
) : LocalizedTitle, LocalizedDescription {
    SOLID(R.string.ui_colour, R.string.ui_solid_background),
    GRADIENT(R.string.ui_gradient, R.string.ui_smooth_transition_between_two_colours),
    IMAGE(R.string.ui_picture, R.string.ui_selected_image)
}

private enum class BottomDockAlignment(override val titleRes: Int) : LocalizedTitle {
    LEFT(R.string.ui_left_0385),
    CENTER(R.string.ui_centre),
    RIGHT(R.string.ui_right_0411)
}

internal enum class DockShelfShape(override val titleRes: Int) : LocalizedTitle {
    NONE(R.string.ui_no_shelf),
    PLATFORM(R.string.ui_platform_with_curved_edges),
    ROUNDED(R.string.ui_rounded),
    PILL(R.string.ui_capsule),
    RECTANGLE(R.string.ui_rectangular)
}

private data class SettingsShortcutOption(
    val title: String,
    val action: String
)

private fun availableSettingsShortcutOptions(): List<SettingsShortcutOption> = listOf(
    SettingsShortcutOption(l10n(R.string.ui_wifi), Settings.ACTION_WIFI_SETTINGS),
    SettingsShortcutOption(l10n(R.string.ui_bluetooth), Settings.ACTION_BLUETOOTH_SETTINGS),
    SettingsShortcutOption(l10n(R.string.ui_display), Settings.ACTION_DISPLAY_SETTINGS),
    SettingsShortcutOption(l10n(R.string.ui_sound), Settings.ACTION_SOUND_SETTINGS),
    SettingsShortcutOption(l10n(R.string.ui_applications), Settings.ACTION_APPLICATION_SETTINGS),
    SettingsShortcutOption(l10n(R.string.ui_accessibility), Settings.ACTION_ACCESSIBILITY_SETTINGS),
    SettingsShortcutOption(l10n(R.string.ui_battery_saver), Settings.ACTION_BATTERY_SAVER_SETTINGS),
    SettingsShortcutOption(l10n(R.string.ui_all_system_settings), Settings.ACTION_SETTINGS)
)

private enum class LauncherSettingsPage {
    ROOT,
    SECURITY,
    SECURITY_RESET,
    LANGUAGE,
    INTERFACE,
    DIALOGS,
    WORKSPACES,
    WORKSPACE_PAGES,
    WORKSPACE_EFFECTS,
    GESTURES,
    GESTURE_SWIPE_DOWN,
    GESTURE_SWIPE_UP,
    GESTURE_DOUBLE_TAP,
    SIDE_DOCK,
    BOTTOM_DOCK,
    BOTTOM_DOCK_APPEARANCE,
    APP_DRAWER,
    APP_DRAWER_LAYOUT,
    APP_DRAWER_GRID,
    APP_DRAWER_ITEM_STYLE,
    APP_DRAWER_PAGE_EFFECTS,
    APP_DRAWER_ICON_EFFECTS,
    APP_DRAWER_INTERACTION,
    APP_DRAWER_NEW_APPS,
    APP_DRAWER_BACKGROUND,
    APP_DRAWER_HIDDEN_APPS,
    APP_DRAWER_QUICK_ACTION,
    APP_LAUNCH_ANIMATION,
    ENVIRONMENT_EFFECTS,
    WALLPAPER,
    WALLPAPER_ANIMATED,
    FOLDERS,
    WIDGETS,
    WIDGET_EDITING,
    WIDGET_PICKER,
    WIDGET_ACTIONS
}

private enum class AppDrawerQuickActionKind {
    PAGE_OVERVIEW,
    LAUNCHER_SETTINGS,
    APP_DRAWER_SETTINGS,
    SYSTEM_SETTINGS
}

private data class AppDrawerQuickAction(
    val kind: AppDrawerQuickActionKind = AppDrawerQuickActionKind.PAGE_OVERVIEW,
    val value: String = ""
)

private val appDrawerQuickSettingsPages = listOf(
    LauncherSettingsPage.APP_DRAWER_LAYOUT,
    LauncherSettingsPage.APP_DRAWER_GRID,
    LauncherSettingsPage.APP_DRAWER_ITEM_STYLE,
    LauncherSettingsPage.APP_DRAWER_PAGE_EFFECTS,
    LauncherSettingsPage.APP_DRAWER_ICON_EFFECTS,
    LauncherSettingsPage.APP_DRAWER_INTERACTION,
    LauncherSettingsPage.APP_DRAWER_NEW_APPS,
    LauncherSettingsPage.APP_DRAWER_BACKGROUND,
    LauncherSettingsPage.APP_DRAWER_HIDDEN_APPS
)

private fun normalizeAppDrawerQuickAction(action: AppDrawerQuickAction): AppDrawerQuickAction = when (action.kind) {
    AppDrawerQuickActionKind.PAGE_OVERVIEW -> AppDrawerQuickAction()
    AppDrawerQuickActionKind.LAUNCHER_SETTINGS -> AppDrawerQuickAction(AppDrawerQuickActionKind.LAUNCHER_SETTINGS)
    AppDrawerQuickActionKind.APP_DRAWER_SETTINGS -> {
        val page = runCatching { LauncherSettingsPage.valueOf(action.value) }.getOrNull()
        if (page in appDrawerQuickSettingsPages) action
        else AppDrawerQuickAction()
    }
    AppDrawerQuickActionKind.SYSTEM_SETTINGS -> {
        if (isSupportedWidgetIntentAction(action.value)) action else AppDrawerQuickAction()
    }
}

private fun appDrawerSettingsPageTitle(page: LauncherSettingsPage): String = when (page) {
    LauncherSettingsPage.APP_DRAWER -> l10n(R.string.ui_app_drawer_settings)
    LauncherSettingsPage.APP_DRAWER_LAYOUT -> l10n(R.string.ui_layout_and_looping)
    LauncherSettingsPage.APP_DRAWER_GRID -> l10n(R.string.ui_icon_grid_and_size)
    LauncherSettingsPage.APP_DRAWER_ITEM_STYLE -> l10n(R.string.ui_icon_frame)
    LauncherSettingsPage.APP_DRAWER_PAGE_EFFECTS -> l10n(R.string.ui_app_drawer_page_effect)
    LauncherSettingsPage.APP_DRAWER_ICON_EFFECTS -> l10n(R.string.ui_app_drawer_icon_effect)
    LauncherSettingsPage.APP_DRAWER_INTERACTION -> l10n(R.string.ui_app_drawer_dragging)
    LauncherSettingsPage.APP_DRAWER_NEW_APPS -> l10n(R.string.ui_new_app_emblem)
    LauncherSettingsPage.APP_DRAWER_BACKGROUND -> l10n(R.string.ui_app_drawer_background_0463)
    LauncherSettingsPage.APP_DRAWER_HIDDEN_APPS -> l10n(R.string.ui_hidden_applications)
    else -> l10n(R.string.ui_app_drawer_settings)
}

private fun systemSettingsActionTitle(action: String): String = when (action) {
    Settings.ACTION_WIFI_SETTINGS -> l10n(R.string.ui_wifi)
    Settings.ACTION_BLUETOOTH_SETTINGS -> l10n(R.string.ui_bluetooth)
    Settings.ACTION_DISPLAY_SETTINGS -> l10n(R.string.ui_display)
    Settings.ACTION_SOUND_SETTINGS -> l10n(R.string.ui_sound)
    Settings.ACTION_APPLICATION_SETTINGS -> l10n(R.string.ui_applications)
    Settings.ACTION_ACCESSIBILITY_SETTINGS -> l10n(R.string.ui_accessibility)
    Settings.ACTION_BATTERY_SAVER_SETTINGS -> l10n(R.string.ui_battery_saver)
    Settings.ACTION_SETTINGS -> l10n(R.string.ui_all_system_settings)
    else -> l10n(R.string.ui_system_settings)
}

private fun appDrawerQuickActionTitle(action: AppDrawerQuickAction): String = when (action.kind) {
    AppDrawerQuickActionKind.PAGE_OVERVIEW -> l10n(R.string.ui_page_overview)
    AppDrawerQuickActionKind.LAUNCHER_SETTINGS -> l10n(R.string.ui_launcher_settings)
    AppDrawerQuickActionKind.APP_DRAWER_SETTINGS -> {
        val page = runCatching { LauncherSettingsPage.valueOf(action.value) }.getOrNull()
        page?.let(::appDrawerSettingsPageTitle) ?: l10n(R.string.ui_app_drawer_settings)
    }
    AppDrawerQuickActionKind.SYSTEM_SETTINGS -> systemSettingsActionTitle(action.value)
}

private fun appDrawerQuickActionIconRes(action: AppDrawerQuickAction): Int = when (action.kind) {
    AppDrawerQuickActionKind.PAGE_OVERVIEW -> R.drawable.ic_apps
    AppDrawerQuickActionKind.APP_DRAWER_SETTINGS -> when (action.value) {
        LauncherSettingsPage.APP_DRAWER_PAGE_EFFECTS.name -> R.drawable.ic_app_drawer_effect
        else -> R.drawable.ic_settings
    }
    AppDrawerQuickActionKind.LAUNCHER_SETTINGS,
    AppDrawerQuickActionKind.SYSTEM_SETTINGS -> R.drawable.ic_settings
}

private enum class DesktopGestureAction(override val titleRes: Int) : LocalizedTitle {
    NONE(R.string.ui_nothing),
    NOTIFICATION_SHADE(R.string.ui_open_notification_shade_0258),
    SIDE_DOCK(R.string.ui_side_dock),
    APP_DRAWER(R.string.ui_app_drawer),
    LAUNCHER_SETTINGS(R.string.ui_launcher_settings),
    SYSTEM_SETTINGS(R.string.ui_system_settings),
    LOCK_SCREEN(R.string.ui_lock_screen),
    WORKSPACE_OVERVIEW(R.string.ui_page_overview)
}

private enum class SideDockPosition(override val titleRes: Int) : LocalizedTitle {
    LEFT(R.string.ui_left_0385),
    RIGHT(R.string.ui_right_0411)
}

private enum class WidgetEditBarPosition(override val titleRes: Int) : LocalizedTitle {
    CHECK_LEFT(R.string.ui_checkmark_on_the_left),
    CHECK_RIGHT(R.string.ui_checkmark_on_the_right)
}

private enum class AppSortMode(
    override val titleRes: Int,
    override val descriptionRes: Int
) : LocalizedTitle, LocalizedDescription {
    NAME(R.string.ui_by_name, R.string.ui_alphabetical_order),
    INSTALL_DATE(R.string.ui_by_installation_date, R.string.ui_recently_installed_first),
    LAST_USED(R.string.ui_by_last_used, R.string.ui_recently_opened_first),
    CUSTOM(R.string.ui_custom_order, R.string.ui_order_set_by_dragging)
}

private enum class LauncherWallpaperMode(
    override val titleRes: Int,
    override val descriptionRes: Int
) : LocalizedTitle, LocalizedDescription {
    ANIMATED(R.string.ui_launcher_animated_background, R.string.ui_built_in_background_with_moving_circles),
    SYSTEM(R.string.ui_system_wallpaper, R.string.ui_android_static_or_live_wallpaper)
}

private enum class AppDrawerMode(
    override val titleRes: Int,
    override val descriptionRes: Int
) : LocalizedTitle, LocalizedDescription {
    VERTICAL_LIST(R.string.ui_vertical_list, R.string.ui_one_continuous_list_that_scrolls_up_and_down),
    HORIZONTAL_PAGES(R.string.ui_horizontal_pages, R.string.ui_icons_are_split_into_separate_screens_that_swipe_left),
    VERTICAL_PAGES(R.string.ui_vertical_pages, R.string.ui_icons_are_split_into_separate_screens_that_swipe_up)
}

private enum class AppDrawerDragOverviewMode {
    FILL
}

private enum class AppDrawerCrossPageDragMode {
    PAGE_OVERVIEW,
    EDGE_SWITCH
}

private enum class AppDrawerPagePreviewMode {
    SIMPLE,
    MIPMAP
}

private enum class NewAppEmblemShape {
    NONE,
    FIRE,
    CIRCLE,
    OVAL,
    STAR,
    DIAMOND,
    ROUNDED_RECTANGLE
}

private enum class SwipeDirection { UP, DOWN, LEFT, RIGHT }

private enum class AppDrawerIconEffect(
    override val titleRes: Int,
    override val descriptionRes: Int
) : LocalizedTitle, LocalizedDescription {
    NONE(R.string.ui_no_effect, R.string.ui_icons_remain_stationary_within_the_page),
    SPIN_2D(R.string.ui_text_2d_rotation, R.string.ui_icons_rotate_around_their_centres),
    FLIP_Y(R.string.ui_text_3d_rotation_around_y, R.string.ui_icons_flip_left_and_right),
    FLIP_X(R.string.ui_text_3d_rotation_around_x, R.string.ui_icons_flip_up_and_down),
    SHRINK(R.string.ui_shrink, R.string.ui_icons_shrink_as_the_page_leaves),
    TILT(R.string.ui_tilt, R.string.ui_icons_tilt_and_shift_slightly),
    CUBE_IN(R.string.ui_drawer_icon_turn_left, R.string.ui_drawer_icon_turn_left_desc),
    CUBE_OUT(R.string.ui_drawer_icon_turn_right, R.string.ui_drawer_icon_turn_right_desc),
    WAVE(R.string.ui_drawer_icon_sway, R.string.ui_icons_move_in_a_delayed_wave_across_the_grid),
    SQUEEZE(R.string.ui_drawer_icon_stretch, R.string.ui_icons_contract_horizontally_and_stretch_vertically),
    TURN_UP(R.string.ui_drawer_icon_turn_up, R.string.ui_drawer_icon_turn_up_desc),
    TURN_DOWN(R.string.ui_drawer_icon_turn_down, R.string.ui_drawer_icon_turn_down_desc),
    SPIRAL(R.string.ui_drawer_icon_spiral, R.string.ui_drawer_icon_spiral_desc),
    FIREWORK(R.string.ui_drawer_icon_firework, R.string.ui_drawer_icon_firework_desc),
    BLACK_HOLE(R.string.ui_drawer_icon_black_hole, R.string.ui_drawer_icon_black_hole_desc),
    BIG_BANG(R.string.ui_drawer_icon_big_bang, R.string.ui_drawer_icon_big_bang_desc)
}

internal enum class AppLaunchAnimationEffect(
    override val titleRes: Int,
    override val descriptionRes: Int
) : LocalizedTitle, LocalizedDescription {
    NONE(R.string.ui_no_animation, R.string.ui_the_icon_launches_without_an_additional_effect),
    FRAME(R.string.ui_frame, R.string.ui_the_icon_outline_expands_and_gradually_disappears),
    ICON_EXPAND(R.string.ui_enlarge, R.string.ui_a_copy_of_the_icon_enlarges_and_fades_away),
    ICON_THEN_WIREFRAME(R.string.ui_icon_then_wireframe, R.string.ui_icon_then_wireframe_description),
    BOUNCE(R.string.ui_bounce, R.string.ui_the_icon_bounces_twice_and_returns_to_its_position),
    BLINK(R.string.ui_blink, R.string.ui_the_icon_flashes_quickly_several_times),
    ROTATE_2D_CW(R.string.ui_clockwise_2d_rotation, R.string.ui_full_clockwise_rotation),
    ROTATE_2D_CCW(R.string.ui_counterclockwise_2d_rotation, R.string.ui_full_counterclockwise_rotation),
    ROTATE_3D_RIGHT(R.string.ui_text_3d_rotation_right, R.string.ui_the_icon_rotates_right_around_its_vertical_axis),
    ROTATE_3D_LEFT(R.string.ui_text_3d_rotation_left, R.string.ui_the_icon_rotates_left_around_its_vertical_axis),
    ROTATE_3D_DIAGONAL(R.string.ui_text_3d_diagonal_rotation, R.string.ui_combined_rotation_around_two_axes),
    ROTATE_RANDOM(R.string.ui_random_rotation, R.string.ui_a_rotation_variant_is_selected_on_every_launch)
}

// Stable picker order: do not rearrange enum declarations, which may be read
// by older saved configuration or external tooling.
private val appDrawerIconEffectPickerOrder = listOf(
    AppDrawerIconEffect.NONE,
    AppDrawerIconEffect.SPIN_2D,
    AppDrawerIconEffect.FLIP_Y,
    AppDrawerIconEffect.FLIP_X,
    AppDrawerIconEffect.SHRINK,
    AppDrawerIconEffect.TILT,
    AppDrawerIconEffect.CUBE_IN,
    AppDrawerIconEffect.CUBE_OUT,
    AppDrawerIconEffect.TURN_UP,
    AppDrawerIconEffect.TURN_DOWN,
    AppDrawerIconEffect.WAVE,
    AppDrawerIconEffect.SQUEEZE,
    AppDrawerIconEffect.SPIRAL,
    AppDrawerIconEffect.FIREWORK,
    AppDrawerIconEffect.BLACK_HOLE,
    AppDrawerIconEffect.BIG_BANG
)

private enum class AppItemFrameShape(override val titleRes: Int) : LocalizedTitle {
    NONE(R.string.ui_no_frame),
    ROUNDED(R.string.ui_rounded_rectangle),
    RECTANGLE(R.string.ui_rectangle),
    CIRCLE(R.string.ui_circle_oval),
    PILL(R.string.ui_capsule)
}

internal enum class WidgetPickerDisplayMode(
    override val titleRes: Int,
    override val descriptionRes: Int
) : LocalizedTitle, LocalizedDescription {
    LIST(R.string.ui_list, R.string.ui_compact_rows_with_a_title_and_small_icon),
    ICON(R.string.ui_icons, R.string.ui_large_widget_provider_icons),
    PREVIEW(R.string.ui_preview, R.string.ui_widget_preview_when_provided_otherwise_the_icon_is_used)
}

internal fun parseAppFrameColor(value: String): Color {
    val clean = value.trim().removePrefix("#").takeLast(6)
    return runCatching {
        Color(0xFF000000L or clean.toLong(16))
    }.getOrDefault(Color(0xFF242936))
}

private fun textShadow(settings: TextShadowSettings, density: Float): Shadow? {
    if (!settings.enabled) return null
    return Shadow(
        color = parseAppFrameColor(settings.colorHex).copy(
            alpha = settings.opacityPercent.coerceIn(0, 100) / 100f
        ),
        offset = Offset(settings.offsetDp * density, settings.offsetDp * density),
        blurRadius = settings.blurDp * density
    )
}

private fun normalizedColorHex(value: String): String? {
    val clean = value.trim().removePrefix("#")
    if (clean.length != 6 || clean.any { it !in "0123456789abcdefABCDEF" }) return null
    return "#${clean.uppercase()}"
}

private fun appItemFrameShape(shape: AppItemFrameShape): Shape = when (shape) {
    AppItemFrameShape.NONE -> RectangleShape
    AppItemFrameShape.ROUNDED -> RoundedCornerShape(22.dp)
    AppItemFrameShape.RECTANGLE -> RoundedCornerShape(2.dp)
    AppItemFrameShape.CIRCLE -> CircleShape
    AppItemFrameShape.PILL -> RoundedCornerShape(50)
}


private fun Modifier.appDrawerIconTransform(
    effect: AppDrawerIconEffect,
    signedPageOffset: Float,
    itemIndex: Int,
    density: Float,
    verticalPager: Boolean,
    pageBoundsInRoot: Rect? = null,
    isOutgoingPage: Boolean = true
): Modifier {
    if (effect == AppDrawerIconEffect.SPIRAL ||
        effect == AppDrawerIconEffect.FIREWORK ||
        effect == AppDrawerIconEffect.BLACK_HOLE ||
        effect == AppDrawerIconEffect.BIG_BANG
    ) {
        val kind = when (effect) {
            AppDrawerIconEffect.SPIRAL -> AdvancedDrawerIconMotion.SPIRAL
            AppDrawerIconEffect.FIREWORK -> AdvancedDrawerIconMotion.FIREWORK
            AppDrawerIconEffect.BLACK_HOLE -> AdvancedDrawerIconMotion.BLACK_HOLE
            AppDrawerIconEffect.BIG_BANG -> AdvancedDrawerIconMotion.BIG_BANG
            else -> error("Not an advanced app-drawer icon effect")
        }
        return advancedAppDrawerIconTransform(kind, signedPageOffset, itemIndex, density, verticalPager, pageBoundsInRoot, isOutgoingPage)
    }
    val offset = signedPageOffset.coerceIn(-1f, 1f)
    val distance = offset.absoluteValue
    val stagger = (itemIndex.coerceIn(0, 19) / 19f)
    val alternatingDirection = if (itemIndex % 2 == 0) 1f else -1f
    val wave = sin((stagger * 1.8f + distance * 1.2f) * Math.PI).toFloat()

    return graphicsLayer {
        when (effect) {
            AppDrawerIconEffect.NONE -> Unit

            AppDrawerIconEffect.SPIN_2D -> {
                rotationZ = 320f * offset * (0.72f + stagger * 0.28f)
                val scale = 1f - distance * 0.12f
                scaleX = scale
                scaleY = scale
            }

            AppDrawerIconEffect.FLIP_Y -> {
                rotationY = -180f * offset
                cameraDistance = 28f * density
                alpha = (1f - distance * 0.48f).coerceAtLeast(0.35f)
            }

            AppDrawerIconEffect.FLIP_X -> {
                rotationX = 180f * offset * alternatingDirection
                cameraDistance = 28f * density
                alpha = (1f - distance * 0.45f).coerceAtLeast(0.38f)
            }

            AppDrawerIconEffect.SHRINK -> {
                val scale = (1f - distance * 0.78f).coerceAtLeast(0.22f)
                scaleX = scale
                scaleY = scale
                alpha = (1f - distance * 0.62f).coerceAtLeast(0.25f)
            }

            AppDrawerIconEffect.TILT -> {
                rotationZ = 28f * offset * alternatingDirection
                rotationY = -38f * offset
                translationY = distance * (10f + stagger * 24f) * density
                val scale = 1f - distance * 0.16f
                scaleX = scale
                scaleY = scale
            }

            AppDrawerIconEffect.CUBE_IN -> {
                rotationY = -88f * offset
                transformOrigin = if (offset < 0f) {
                    TransformOrigin(0f, 0.5f)
                } else {
                    TransformOrigin(1f, 0.5f)
                }
                cameraDistance = 30f * density
                val scale = 1f - distance * 0.10f
                scaleX = scale
                scaleY = scale
                alpha = (1f - distance * 0.28f).coerceAtLeast(0.55f)
            }

            AppDrawerIconEffect.CUBE_OUT -> {
                rotationY = 88f * offset
                transformOrigin = if (offset < 0f) {
                    TransformOrigin(1f, 0.5f)
                } else {
                    TransformOrigin(0f, 0.5f)
                }
                cameraDistance = 30f * density
                translationX = offset * 10f * density
                alpha = (1f - distance * 0.28f).coerceAtLeast(0.55f)
            }

            AppDrawerIconEffect.TURN_UP -> {
                rotationX = 88f * offset
                transformOrigin = if (offset < 0f) TransformOrigin(0.5f, 1f) else TransformOrigin(0.5f, 0f)
                cameraDistance = 30f * density
                scaleY = 1f - distance * 0.10f
                alpha = (1f - distance * 0.28f).coerceAtLeast(0.55f)
            }

            AppDrawerIconEffect.TURN_DOWN -> {
                rotationX = -88f * offset
                transformOrigin = if (offset < 0f) TransformOrigin(0.5f, 0f) else TransformOrigin(0.5f, 1f)
                cameraDistance = 30f * density
                translationY = offset * 10f * density
                alpha = (1f - distance * 0.28f).coerceAtLeast(0.55f)
            }

            // Advanced effects return a dedicated composited Canvas modifier above.
            AppDrawerIconEffect.SPIRAL,
            AppDrawerIconEffect.FIREWORK,
            AppDrawerIconEffect.BLACK_HOLE,
            AppDrawerIconEffect.BIG_BANG -> Unit

            AppDrawerIconEffect.WAVE -> {
                translationY = wave * 26f * density * distance
                rotationZ = wave * 22f * distance
                rotationY = -42f * offset
                val scale = 1f - distance * 0.14f
                scaleX = scale
                scaleY = scale
            }

            AppDrawerIconEffect.SQUEEZE -> {
                scaleX = (1f - distance * 0.72f).coerceAtLeast(0.25f)
                scaleY = 1f + distance * 0.24f
                rotationZ = 12f * offset * alternatingDirection
                translationX = offset * stagger * 10f * density
                alpha = (1f - distance * 0.24f).coerceAtLeast(0.60f)
            }
        }
    }
}

private data class ShortcutDragState(
    val shortcutId: String,
    val position: Offset,
    val overTrash: Boolean,
    val gestureOffset: Offset,
    val maxDistanceFromStart: Float,
    val pointerStartInRoot: Offset,
    val iconStartPosition: Offset
)

private data class FolderDragState(
    val folderId: String,
    val position: Offset,
    val overTrash: Boolean = false
)

private data class AppDrawerDragState(
    val app: AppItem,
    val position: Offset,
    val sourceIndex: Int,
    val overDelete: Boolean = false,
    val overEdit: Boolean = false,
    val overInfo: Boolean = false,
    val overWorkspace: Boolean = false,
    val hoverTargetIndex: Int? = null,
    val previewTargetIndex: Int? = null
)

private fun appDrawerTargetIndex(
    position: Offset,
    page: Int,
    columns: Int,
    rows: Int,
    screenWidthPx: Float,
    screenHeightPx: Float,
    bottomBarHeightDp: Int,
    density: Float,
    appCount: Int
): Int? {
    if (appCount <= 0 || screenWidthPx <= 0f || screenHeightPx <= 0f) return null
    val safeColumns = columns.coerceAtLeast(1)
    val safeRows = rows.coerceAtLeast(1)
    val itemsPerPage = safeColumns * safeRows
    val contentTop = 42f * density
    val contentBottom = screenHeightPx - (bottomBarHeightDp + 34f) * density
    val col = ((position.x / screenWidthPx) * safeColumns).toInt().coerceIn(0, safeColumns - 1)
    val row = (((position.y - contentTop) / (contentBottom - contentTop).coerceAtLeast(1f)) * safeRows)
        .toInt().coerceIn(0, safeRows - 1)
    return (page.coerceAtLeast(0) * itemsPerPage + row * safeColumns + col)
        .coerceIn(0, appCount - 1)
}

private data class FolderFanPathPoint(
    val position: Offset,
    val distancePx: Float
)

private data class FolderFanDisplay(
    val folderId: String,
    val page: Int,
    val fingerPosition: Offset,
    val rawFingerPosition: Offset,
    val trail: List<FolderFanPathPoint>,
    val headDistancePx: Float,
    val dragging: Boolean,
    val speedMultiplier: Float = 1f
)

private enum class FolderFanCloseMode(
    override val titleRes: Int,
    override val descriptionRes: Int
) : LocalizedTitle, LocalizedDescription {
    IMMEDIATE(R.string.ui_hide_immediately, R.string.ui_icons_disappear_immediately_after_tapping_an_empty_area),
    FLY_BACK(R.string.ui_return_to_folder, R.string.ui_icons_quickly_fly_back_into_the_folder)
}

private fun Float.safeWorkspaceFraction(defaultValue: Float = 0.5f): Float =
    if (isFinite()) coerceIn(0f, 1f) else defaultValue.coerceIn(0f, 1f)

private fun pointOnFolderFanTrail(
    trail: List<FolderFanPathPoint>,
    distanceFromStartPx: Float
): Offset {
    if (trail.isEmpty()) return Offset.Zero
    if (trail.size == 1 || distanceFromStartPx <= trail.first().distancePx) {
        return trail.first().position
    }
    if (distanceFromStartPx >= trail.last().distancePx) return trail.last().position

    var low = 1
    var high = trail.lastIndex
    while (low < high) {
        val middle = (low + high) ushr 1
        if (trail[middle].distancePx < distanceFromStartPx) low = middle + 1
        else high = middle
    }
    val end = trail[low]
    val start = trail[low - 1]
    val segmentLength = end.distancePx - start.distancePx
    if (segmentLength <= 0.001f) return end.position
    val t = ((distanceFromStartPx - start.distancePx) / segmentLength).coerceIn(0f, 1f)
    return Offset(
        x = start.position.x + (end.position.x - start.position.x) * t,
        y = start.position.y + (end.position.y - start.position.y) * t
    )
}

private data class FolderFanAdvance(
    val head: Offset,
    val trail: List<FolderFanPathPoint>,
    val headDistancePx: Float,
    val speedMultiplier: Float
)

/**
 * Moves the whole fan along one measured pointer path. Every icon uses the same
 * path progress and a fixed distance behind the head, so chain members cannot
 * catch or overtake each other. Deceleration starts only after the last icon has
 * enough path behind the head to leave the folder.
 */
private fun advanceFolderFan(
    rawStart: Offset,
    rawEnd: Offset,
    trail: List<FolderFanPathPoint>,
    headDistancePx: Float,
    initialSpeedRatio: Float,
    speedMultiplier: Float,
    fullExtractionDistancePx: Float,
    retentionRatio: Float,
    decayStepPx: Float
): FolderFanAdvance {
    val rawDistance = (rawEnd - rawStart).getDistance()
    val updatedTrail = if (rawDistance <= 0.001f) {
        trail
    } else {
        val previousDistance = trail.lastOrNull()?.distancePx ?: 0f
        trail + FolderFanPathPoint(rawEnd, previousDistance + rawDistance)
    }
    if (updatedTrail.isEmpty()) {
        return FolderFanAdvance(rawEnd, updatedTrail, 0f, 1f)
    }

    val baseSpeed = initialSpeedRatio.coerceIn(0.15f, 1f)
    val extractionDistance = fullExtractionDistancePx.coerceAtLeast(0f)
    var nextHeadDistance = headDistancePx.coerceIn(0f, updatedTrail.last().distancePx)
    var remainingRawDistance = rawDistance

    // Keep the configured initial speed unchanged until every icon is separated.
    if (nextHeadDistance < extractionDistance && remainingRawDistance > 0f) {
        val distanceToExtraction = extractionDistance - nextHeadDistance
        val rawDistanceToExtraction = distanceToExtraction / baseSpeed
        val constantSpeedRawDistance = remainingRawDistance.coerceAtMost(rawDistanceToExtraction)
        nextHeadDistance += constantSpeedRawDistance * baseSpeed
        remainingRawDistance -= constantSpeedRawDistance
    }

    var nextSpeedMultiplier = speedMultiplier.coerceIn(0.004f, 1f)
    if (remainingRawDistance > 0f) {
        val retention = retentionRatio.coerceIn(0f, 1f)
        val retentionPerStep = 0.86f + 0.14f * retention
        val safeDecayStep = decayStepPx.coerceAtLeast(1f)
        if (retentionPerStep >= 0.999999f) {
            nextHeadDistance += remainingRawDistance * baseSpeed * nextSpeedMultiplier
        } else {
            val minimumMultiplier = 0.004f
            val decayRate = ln(retentionPerStep.toDouble()) / safeDecayStep.toDouble()
            val rawDistanceToMinimum = if (nextSpeedMultiplier <= minimumMultiplier) {
                0.0
            } else {
                ln((minimumMultiplier / nextSpeedMultiplier).toDouble()) / decayRate
            }
            val decayingRawDistance = remainingRawDistance.toDouble()
                .coerceAtMost(rawDistanceToMinimum)
                .coerceAtLeast(0.0)
            if (decayingRawDistance > 0.0) {
                val startMultiplier = nextSpeedMultiplier.toDouble()
                val endMultiplier = startMultiplier * exp(decayRate * decayingRawDistance)
                val decayingAdvance = baseSpeed.toDouble() *
                    startMultiplier * (exp(decayRate * decayingRawDistance) - 1.0) / decayRate
                nextHeadDistance += decayingAdvance.toFloat()
                nextSpeedMultiplier = endMultiplier.toFloat().coerceAtLeast(minimumMultiplier)
            }
            val constantMinimumDistance = remainingRawDistance - decayingRawDistance.toFloat()
            if (constantMinimumDistance > 0f) {
                nextHeadDistance += constantMinimumDistance * baseSpeed * minimumMultiplier
                nextSpeedMultiplier = minimumMultiplier
            }
        }
    }

    nextHeadDistance = nextHeadDistance.coerceAtMost(updatedTrail.last().distancePx)
    return FolderFanAdvance(
        head = pointOnFolderFanTrail(updatedTrail, nextHeadDistance),
        trail = updatedTrail,
        headDistancePx = nextHeadDistance,
        speedMultiplier = nextSpeedMultiplier
    )
}

// A cyclic pager recentres itself long before the boundary. Keeping thousands
// of virtual pages is unnecessary and can cause unbounded subcomposition when
// a rotation briefly produces an invalid/near-zero viewport size.
private const val LOOP_PAGE_MULTIPLIER = 24

private fun logicalPage(virtualPage: Int, realPageCount: Int): Int =
    if (realPageCount <= 0) 0 else Math.floorMod(virtualPage, realPageCount)

private fun virtualPageCount(realPageCount: Int, loopEnabled: Boolean): Int =
    if (loopEnabled && realPageCount > 1) realPageCount * LOOP_PAGE_MULTIPLIER else realPageCount

private fun initialVirtualPage(
    realPageCount: Int,
    logicalPage: Int,
    loopEnabled: Boolean
): Int {
    if (!loopEnabled || realPageCount <= 1) return logicalPage.coerceIn(0, (realPageCount - 1).coerceAtLeast(0))
    val middle = virtualPageCount(realPageCount, true) / 2
    val alignedMiddle = middle - Math.floorMod(middle, realPageCount)
    return alignedMiddle + logicalPage.coerceIn(0, realPageCount - 1)
}

private fun nearestVirtualPage(currentVirtualPage: Int, targetLogicalPage: Int, realPageCount: Int): Int {
    if (realPageCount <= 1) return 0
    val currentLogical = logicalPage(currentVirtualPage, realPageCount)
    val forward = Math.floorMod(targetLogicalPage - currentLogical, realPageCount)
    val backward = forward - realPageCount
    return currentVirtualPage + if (kotlin.math.abs(backward) < kotlin.math.abs(forward)) backward else forward
}

private class WorkspaceEdgeSwipeController(
    private val scope: kotlinx.coroutines.CoroutineScope,
    private val pagerState: androidx.compose.foundation.pager.PagerState
) {
    private var deltaChannel: Channel<Float>? = null
    private var scrollJob: Job? = null
    private var startVirtualPage: Int = 0

    fun start() {
        deltaChannel?.close()
        scrollJob?.cancel()
        startVirtualPage = pagerState.currentPage
        val channel = Channel<Float>(Channel.UNLIMITED)
        deltaChannel = channel
        scrollJob = scope.launch {
            pagerState.scroll {
                for (fingerDeltaX in channel) {
                    // Pager scroll direction is opposite to the physical finger.
                    scrollBy(-fingerDeltaX)
                }
            }
        }
    }

    fun dragBy(fingerDeltaX: Float) {
        if (fingerDeltaX != 0f) deltaChannel?.trySend(fingerDeltaX)
    }

    fun finish(
        velocityXDpPerSecond: Float,
        workspaceCount: Int,
        loopEnabled: Boolean
    ) {
        val channel = deltaChannel
        val job = scrollJob
        deltaChannel = null
        scrollJob = null
        channel?.close()
        scope.launch {
            job?.join()
            if (workspaceCount <= 1) {
                pagerState.animateScrollToPage(pagerState.currentPage)
                return@launch
            }

            // Slow releases settle to the page currently closest to the viewport.
            // A fast fling keeps normal pager-like behaviour even when the finger
            // travelled less than half a page.
            val fastFling = kotlin.math.abs(velocityXDpPerSecond) >= 900f
            val targetVirtualPage = if (fastFling) {
                val direction = if (velocityXDpPerSecond < 0f) 1 else -1
                (startVirtualPage + direction).coerceIn(0, pagerState.pageCount - 1)
            } else {
                pagerState.currentPage
            }

            val target = if (loopEnabled) {
                targetVirtualPage
            } else {
                targetVirtualPage.coerceIn(0, workspaceCount - 1)
            }
            pagerState.animateScrollToPage(target)
        }
    }
}


private class AppsRepository(private val context: Context) {
    private val launcherApps: LauncherApps =
        context.getSystemService(LauncherApps::class.java)

    fun loadApps(customizations: Map<String, AppCustomization> = emptyMap()): List<AppItem> {
        val users = runCatching { launcherApps.profiles }
            .getOrElse { listOf(Process.myUserHandle()) }
        val usageByPackage = loadLastUsageByPackage()
        val globalIconPack = AdvancedLauncherStorage.load(context).globalIconPackPackage
        val globalIconEntries = globalIconPack?.let { IconPackRepository.loadEntries(context, it) }.orEmpty()

        return users
            .flatMap { user ->
                runCatching { launcherApps.getActivityList(null, user) }
                    .getOrDefault(emptyList())
                    .map { info ->
                        val packageName = info.applicationInfo.packageName
                        val icon = runCatching {
                            info.getIcon(context.resources.displayMetrics.densityDpi)
                                .toBitmap(width = 96, height = 96)
                                .asImageBitmap()
                        }.getOrNull()
                        val installTime = runCatching {
                            context.packageManager.getPackageInfo(packageName, 0).firstInstallTime
                        }.getOrDefault(0L)

                        val originalLabel = info.label?.toString().orEmpty().ifBlank { packageName }
                        val componentKey = info.componentName.flattenToString()
                        val customization = customizations[componentKey]
                        val customIconUri = customization?.iconUri
                        val customIconPackPackage = customization?.iconPackPackage
                        val customIconPackDrawable = customization?.iconPackDrawable
                        val customIcon = when {
                            !customIconUri.isNullOrBlank() -> runCatching {
                                val source = ImageDecoder.createSource(context.contentResolver, Uri.parse(customIconUri))
                                ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
                                    decoder.setTargetSize(192, 192)
                                    decoder.isMutableRequired = false
                                }.asImageBitmap()
                            }.getOrNull()
                            !customIconPackPackage.isNullOrBlank() && !customIconPackDrawable.isNullOrBlank() -> {
                                IconPackRepository.loadIcon(
                                    context,
                                    customIconPackPackage,
                                    customIconPackDrawable
                                )
                            }
                            !globalIconPack.isNullOrBlank() -> {
                                IconPackRepository.matchingEntry(globalIconEntries, info.componentName)?.let { entry ->
                                    IconPackRepository.loadIcon(context, globalIconPack, entry.drawableName)
                                }
                            }
                            else -> null
                        }
                        AppItem(
                            label = customization?.label?.takeIf { it.isNotBlank() } ?: originalLabel,
                            packageName = packageName,
                            componentName = info.componentName,
                            user = user,
                            icon = customIcon ?: icon,
                            installTimeMillis = installTime,
                            lastUsedMillis = usageByPackage[packageName] ?: 0L,
                            originalLabel = originalLabel,
                            customIconUri = customization?.iconUri
                        )
                    }
            }
            .filterNot { it.packageName == context.packageName }
            .distinctBy { it.componentName.flattenToShortString() + "#" + it.user.hashCode() }
    }

    private fun loadLastUsageByPackage(): Map<String, Long> {
        if (!hasUsageAccess(context)) return emptyMap()
        val manager = context.getSystemService(UsageStatsManager::class.java)
        val end = System.currentTimeMillis()
        val start = end - 1000L * 60L * 60L * 24L * 90L
        return runCatching {
            manager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, start, end)
                .groupBy { it.packageName }
                .mapValues { (_, values) -> values.maxOfOrNull { it.lastTimeUsed } ?: 0L }
        }.getOrDefault(emptyMap())
    }

    fun launch(app: AppItem) {
        launcherApps.startMainActivity(app.componentName, app.user, null, null)
    }

    fun launch(componentName: ComponentName) {
        launcherApps.startMainActivity(componentName, Process.myUserHandle(), null, null)
    }

    fun registerCallback(callback: LauncherApps.Callback) {
        launcherApps.registerCallback(callback)
    }

    fun unregisterCallback(callback: LauncherApps.Callback) {
        launcherApps.unregisterCallback(callback)
    }
}

private fun hasUsageAccess(context: Context): Boolean {
    val appOps = context.getSystemService(AppOpsManager::class.java)
    val mode = appOps.unsafeCheckOpNoThrow(
        AppOpsManager.OPSTR_GET_USAGE_STATS,
        android.os.Process.myUid(),
        context.packageName
    )
    return mode == AppOpsManager.MODE_ALLOWED
}

private object LauncherStorage {
    private const val PREFS_NAME = "e_e_launcher_workspace"
    private const val KEY_PAGE_COUNT = "page_count"
    private const val KEY_SHORTCUTS = "shortcuts"
    private const val KEY_PAGE_TRANSITION = "page_transition"
    private const val KEY_MESH_SNAPSHOT_AFTER_IDLE = "mesh_snapshot_after_idle"
    private const val KEY_EFFECT_GROUP_EXPANDED_PREFIX = "effect_group_expanded_"
    private const val KEY_APP_DRAWER_MODE = "app_drawer_mode"
    private const val KEY_APP_DRAWER_PAGE = "app_drawer_page"
    private const val KEY_APP_DRAWER_PAGE_TRANSITION = "app_drawer_page_transition"
    private const val KEY_APP_DRAWER_ICON_EFFECT = "app_drawer_icon_effect"
    private const val KEY_APP_LAUNCH_ANIMATION = "app_launch_animation"
    private const val KEY_APP_LAUNCH_DELAY = "app_launch_delay"
    private const val KEY_APP_LAUNCH_ANIMATION_DURATION = "app_launch_animation_duration"
    private const val KEY_APP_DRAG_ANIMATION = "app_drag_animation"
    private const val KEY_APP_DRAG_ANIMATION_DURATION = "app_drag_animation_duration"
    private const val KEY_LINK_LAUNCH_DELAY_TO_ANIMATION = "link_launch_delay_to_animation"
    private const val KEY_APP_DRAWER_DRAG_OVERVIEW_MODE = "app_drawer_drag_overview_mode"
    private const val KEY_APP_DRAWER_CROSS_PAGE_DRAG_MODE = "app_drawer_cross_page_drag_mode"
    private const val KEY_APP_DRAWER_PAGE_PREVIEW_MODE = "app_drawer_page_preview_mode"
    private const val KEY_APP_DRAWER_DRAG_PAGE_HOVER = "app_drawer_drag_page_hover"
    private const val KEY_APP_DRAWER_QUICK_ACTION_KIND = "app_drawer_quick_action_kind"
    private const val KEY_APP_DRAWER_QUICK_ACTION_VALUE = "app_drawer_quick_action_value"
    private const val KEY_APP_DRAWER_WORKSPACE_EDGE_SWIPE = "app_drawer_workspace_edge_swipe"
    private const val KEY_NEW_APP_CUTOFF_MILLIS = "new_app_cutoff_millis"
    private const val KEY_NEW_APP_CUTOFF_MANUAL = "new_app_cutoff_manual"
    private const val KEY_NEW_APP_CUTOFF_LAUNCHER_UPDATE = "new_app_cutoff_launcher_update"
    private const val KEY_NEW_APP_EMBLEM_SHAPE = "new_app_emblem_shape"
    private const val KEY_NEW_APP_EMBLEM_SIZE = "new_app_emblem_size"
    private const val KEY_NEW_APP_EMBLEM_COLOR = "new_app_emblem_color"
    private const val KEY_NEW_APP_EMBLEM_COLOR_B = "new_app_emblem_color_b"
    private const val KEY_NEW_APP_EMBLEM_GRADIENT = "new_app_emblem_gradient"
    private const val KEY_NEW_APP_EMBLEM_X = "new_app_emblem_x"
    private const val KEY_NEW_APP_EMBLEM_Y = "new_app_emblem_y"
    private const val KEY_NEW_APP_EMBLEM_IMAGE_URI = "new_app_emblem_image_uri"
    private const val KEY_NEW_APP_EMBLEM_PACK_PACKAGE = "new_app_emblem_pack_package"
    private const val KEY_NEW_APP_EMBLEM_PACK_DRAWABLE = "new_app_emblem_pack_drawable"
    private const val KEY_OPENED_APPS = "opened_apps"
    private const val KEY_WORKSPACE_LOOP = "workspace_loop"
    private const val KEY_APP_DRAWER_LOOP = "app_drawer_loop"
    private const val KEY_WIDGETS = "widgets"
    private const val KEY_FOLDERS = "folders"
    private const val KEY_SIDE_DOCK_POSITION = "side_dock_position"
    private const val KEY_SIDE_DOCK_OPACITY = "side_dock_opacity"
    private const val KEY_SIDE_DOCK_ITEMS = "side_dock_items"
    private const val KEY_DEFAULT_WORKSPACE_PAGE = "default_workspace_page"
    private const val KEY_SIDE_DOCK_GESTURE_MIGRATED = "side_dock_gesture_migrated_v11"
    private const val KEY_SWIPE_DOWN_ACTION = "swipe_down_action"
    private const val KEY_SWIPE_UP_ACTION = "swipe_up_action"
    private const val KEY_IGNORE_GESTURES_ON_WIDGETS = "ignore_gestures_on_widgets"
    private const val KEY_DOUBLE_TAP_ACTION = "double_tap_action"
    private const val KEY_PINCH_ACTION = "pinch_action"
    private const val KEY_MULTI_SWIPE_PREFIX = "multi_swipe_"
    private const val KEY_WIDGET_EDIT_BAR_POSITION = "widget_edit_bar_position"
    private const val KEY_APP_SORT_MODE = "app_sort_mode"
    private const val KEY_APP_DRAWER_COLUMNS = "app_drawer_columns"
    private const val KEY_APP_DRAWER_ROWS = "app_drawer_rows"
    private const val KEY_APP_DRAWER_ITEM_WIDTH = "app_drawer_item_width"
    private const val KEY_APP_DRAWER_ITEM_HEIGHT = "app_drawer_item_height"
    private const val KEY_APP_DRAWER_INDEPENDENT_SIZE = "app_drawer_independent_size"
    private const val KEY_APP_DRAWER_FILL_AVAILABLE = "app_drawer_fill_available"
    private const val KEY_APP_DRAWER_SCREEN_HEIGHT_DP = "app_drawer_screen_height_dp"
    private const val KEY_APP_DRAWER_FRAME_SHAPE = "app_drawer_frame_shape"
    private const val KEY_APP_DRAWER_FRAME_COLOR = "app_drawer_frame_color"
    private const val KEY_APP_DRAWER_FRAME_OPACITY = "app_drawer_frame_opacity"
    private const val KEY_WIDGET_PICKER_MODE = "widget_picker_mode"
    private const val KEY_APP_DRAWER_CUSTOM_ORDER = "app_drawer_custom_order"
    private const val KEY_APP_DRAWER_REORDER_HOLD = "app_drawer_reorder_hold"
    private const val KEY_FOLDER_FAN_SPEED = "folder_fan_speed"
    private const val KEY_FOLDER_FAN_DECAY = "folder_fan_decay"
    private const val KEY_FOLDER_FAN_CLOSE_MODE = "folder_fan_close_mode"
    private const val KEY_FOLDER_FAN_ICON_SIZE = "folder_fan_icon_size"
    private const val KEY_FOLDER_FAN_SPACING = "folder_fan_spacing"
    private const val KEY_FOLDER_REORDER_LINE_COLOR = "folder_reorder_line_color"
    private const val KEY_FOLDER_REORDER_LINE_THICKNESS = "folder_reorder_line_thickness"
    private const val KEY_WALLPAPER_MODE = "wallpaper_mode"
    private const val KEY_WALLPAPER_SCROLLING = "wallpaper_scrolling"
    private const val KEY_WALLPAPER_DYNAMIC = "wallpaper_dynamic"
    private const val KEY_WALLPAPER_BACKGROUND = "wallpaper_background"
    private const val KEY_WALLPAPER_CIRCLE_A = "wallpaper_circle_a"
    private const val KEY_WALLPAPER_CIRCLE_B = "wallpaper_circle_b"
    private const val KEY_WALLPAPER_CIRCLE_COUNT = "wallpaper_circle_count"
    private const val KEY_APP_DRAWER_EDGE_SWITCH_DP = "app_drawer_edge_switch_dp"
    private const val KEY_APP_DRAWER_BACKGROUND_MODE = "app_drawer_background_mode"
    private const val KEY_APP_DRAWER_BACKGROUND_OPACITY = "app_drawer_background_opacity"
    private const val KEY_APP_DRAWER_BACKGROUND_COLOR_A = "app_drawer_background_color_a"
    private const val KEY_APP_DRAWER_BACKGROUND_COLOR_B = "app_drawer_background_color_b"
    private const val KEY_APP_DRAWER_BACKGROUND_TEXT_COLOR = "app_drawer_background_text_color"
    private const val KEY_APP_DRAWER_BACKGROUND_IMAGE_URI = "app_drawer_background_image_uri"
    private const val KEY_APP_DRAWER_FRAME_TEXT_COLOR = "app_drawer_frame_text_color"
    private const val KEY_SIDE_DOCK_TEXT_COLOR = "side_dock_text_color"
    private const val KEY_BOTTOM_DOCK_ITEMS = "bottom_dock_items"
    private const val KEY_BOTTOM_DOCK_PAGE_COUNT = "bottom_dock_page_count"
    private const val KEY_BOTTOM_DOCK_LOOP = "bottom_dock_loop"
    private const val KEY_BOTTOM_DOCK_INDICATOR_TOGGLE = "bottom_dock_indicator_toggle"
    private const val KEY_BOTTOM_DOCK_ADD_BUTTON_ENABLED = "bottom_dock_add_button_enabled"
    private const val KEY_BOTTOM_DOCK_BACKGROUND_COLOR = "bottom_dock_background_color"
    private const val KEY_BOTTOM_DOCK_BACKGROUND_OPACITY = "bottom_dock_background_opacity"
    private const val KEY_BOTTOM_DOCK_TEXT_COLOR = "bottom_dock_text_color"
    private const val KEY_BOTTOM_DOCK_SHELF_HEIGHT = "bottom_dock_shelf_height"
    private const val KEY_BOTTOM_DOCK_ICON_VERTICAL_OFFSET = "bottom_dock_icon_vertical_offset"
    private const val KEY_BOTTOM_DOCK_SHELF_VERTICAL_OFFSET = "bottom_dock_shelf_vertical_offset"
    private const val KEY_BOTTOM_DOCK_PAGE_INDICATOR_VISIBLE = "bottom_dock_page_indicator_visible"
    private const val KEY_BOTTOM_DOCK_PAGE_INDICATOR_GAP = "bottom_dock_page_indicator_gap"
    private const val KEY_BOTTOM_DOCK_LABELS_VISIBLE = "bottom_dock_labels_visible"
    private const val KEY_BOTTOM_DOCK_SHELF_SHAPE = "bottom_dock_shelf_shape"
    private const val KEY_BOTTOM_DOCK_SHELF_COLOR = "bottom_dock_shelf_color"
    private const val KEY_BOTTOM_DOCK_SHELF_OPACITY = "bottom_dock_shelf_opacity"
    private const val KEY_BOTTOM_DOCK_ALIGNMENT = "bottom_dock_alignment"
    private const val KEY_BOTTOM_DOCK_FOLDER_CREATION_DELAY = "bottom_dock_folder_creation_delay"
    private const val KEY_BOTTOM_DOCK_CAPACITY = "bottom_dock_capacity"
    private const val KEY_HIDDEN_APPS = "hidden_apps"
    private const val KEY_KEEP_UNINSTALLED_APP_SHORTCUTS = "keep_uninstalled_app_shortcuts"
    private const val KEY_APP_CUSTOMIZATIONS = "app_customizations"
    private const val KEY_ENVIRONMENT_EFFECTS = "environment_effects"
    private const val KEY_LAUNCHER_INTERFACE_STYLE = "launcher_interface_style"
    private const val KEY_TEXT_SHADOW_ENABLED = "text_shadow_enabled"
    private const val KEY_TEXT_SHADOW_OFFSET = "text_shadow_offset"
    private const val KEY_TEXT_SHADOW_BLUR = "text_shadow_blur"
    private const val KEY_TEXT_SHADOW_COLOR = "text_shadow_color"
    private const val KEY_TEXT_SHADOW_OPACITY = "text_shadow_opacity"
    private const val KEY_V12_DEFAULTS_MIGRATED = "v12_defaults_migrated"
    private const val KEY_V13_DEFAULTS_MIGRATED = "v13_defaults_migrated"
    private const val DEFAULT_PAGE_COUNT = 5

    fun migrateV12Defaults(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        if (prefs.getBoolean(KEY_V12_DEFAULTS_MIGRATED, false)) return
        val editor = prefs.edit().putBoolean(KEY_V12_DEFAULTS_MIGRATED, true)
        val oldPageEffect = prefs.getString(KEY_APP_DRAWER_PAGE_TRANSITION, null)
        if (oldPageEffect == null || oldPageEffect == PageTransitionEffect.DEFAULT.name) {
            editor.putString(KEY_APP_DRAWER_PAGE_TRANSITION, PageTransitionEffect.CROSSFADE.name)
        }
        editor.apply()
    }

    fun migrateV13Defaults(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        if (prefs.getBoolean(KEY_V13_DEFAULTS_MIGRATED, false)) return
        val editor = prefs.edit().putBoolean(KEY_V13_DEFAULTS_MIGRATED, true)
                val oldWidth = prefs.getInt(KEY_APP_DRAWER_ITEM_WIDTH, 72)
        val oldHeight = prefs.getInt(KEY_APP_DRAWER_ITEM_HEIGHT, 72)
        if (!prefs.contains(KEY_APP_DRAWER_ITEM_WIDTH) || oldWidth == 72) editor.putInt(KEY_APP_DRAWER_ITEM_WIDTH, 80)
        if (!prefs.contains(KEY_APP_DRAWER_ITEM_HEIGHT) || oldHeight == 72) editor.putInt(KEY_APP_DRAWER_ITEM_HEIGHT, 90)
        editor.apply()
    }

    fun loadPageCount(context: Context): Int =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getInt(KEY_PAGE_COUNT, DEFAULT_PAGE_COUNT)
            .coerceIn(1, 9)

    fun savePageCount(context: Context, count: Int) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putInt(KEY_PAGE_COUNT, count.coerceIn(1, 9))
            .apply()
    }

    fun loadDefaultWorkspacePage(context: Context): Int =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getInt(KEY_DEFAULT_WORKSPACE_PAGE, 2).coerceIn(0, 8)

    fun saveDefaultWorkspacePage(context: Context, page: Int) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit().putInt(KEY_DEFAULT_WORKSPACE_PAGE, page.coerceIn(0, 8)).apply()
    }

    fun loadWorkspaceLoop(context: Context): Boolean =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_WORKSPACE_LOOP, true)

    fun saveWorkspaceLoop(context: Context, enabled: Boolean) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_WORKSPACE_LOOP, enabled)
            .apply()
    }

    fun loadAppDrawerLoop(context: Context): Boolean =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_APP_DRAWER_LOOP, true)

    fun saveAppDrawerLoop(context: Context, enabled: Boolean) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_APP_DRAWER_LOOP, enabled)
            .apply()
    }

    fun loadPageTransition(context: Context): PageTransitionEffect {
        val stored = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_PAGE_TRANSITION, PageTransitionEffect.CUBE_IN_HORIZONTAL_OREO.name)

        if (stored == "RETRO_TILES") return PageTransitionEffect.RETRO_FLY_TILES
        if (stored == "SPHERE_DIAGNOSTIC_MEDIUM" || stored == "SPHERE_DIAGNOSTIC_LOW") {
            return PageTransitionEffect.SPHERE
        }
        if (stored == "SLIDE_REVEAL") return PageTransitionEffect.DEFAULT
        // Removed Pager variants still appear in old profiles and JSON backups.
        if (stored == "FADE") return PageTransitionEffect.CROSSFADE
        if (stored == "STACK") return PageTransitionEffect.DEFAULT
        if (stored == "DEPTH") return PageTransitionEffect.SCALE
        if (stored == "CUBE") return PageTransitionEffect.CUBE_OUT_HORIZONTAL_OREO
        // Compatibility with effect names persisted by versions before 0.37.22,
        // including names restored from older JSON settings backups.
        if (stored == "PAGE_CURL_GOOGLE" || stored == "PAGE_CURL") return PageTransitionEffect.PAGE_CURL_SOFT
        if (stored == "PAGE_CURL_APPLE") return PageTransitionEffect.PAGE_CURL_SOFT
        return runCatching {
            PageTransitionEffect.valueOf(stored ?: PageTransitionEffect.CROSSFADE.name)
        }.getOrDefault(PageTransitionEffect.CROSSFADE)
    }

    fun savePageTransition(context: Context, effect: PageTransitionEffect) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_PAGE_TRANSITION, effect.name)
            .apply()
    }

    fun loadMeshSnapshotAfterIdle(context: Context): Boolean =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_MESH_SNAPSHOT_AFTER_IDLE, true)

    fun saveMeshSnapshotAfterIdle(context: Context, enabled: Boolean) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_MESH_SNAPSHOT_AFTER_IDLE, enabled)
            .apply()
    }

    fun loadDrawerIconGroupExpanded(context: Context, experimental: Boolean): Boolean =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean(if (experimental) "drawer_icon_group_experimental_expanded" else "drawer_icon_group_simple_expanded", !experimental)

    fun saveDrawerIconGroupExpanded(context: Context, experimental: Boolean, expanded: Boolean) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
            .putBoolean(if (experimental) "drawer_icon_group_experimental_expanded" else "drawer_icon_group_simple_expanded", expanded)
            .apply()
    }

    fun loadEffectGroupExpanded(context: Context, group: PageEffectTechnologyGroup): Boolean =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_EFFECT_GROUP_EXPANDED_PREFIX + group.name, true)

    fun saveEffectGroupExpanded(
        context: Context,
        group: PageEffectTechnologyGroup,
        expanded: Boolean
    ) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_EFFECT_GROUP_EXPANDED_PREFIX + group.name, expanded)
            .apply()
    }

    fun loadAppDrawerMode(context: Context): AppDrawerMode {
        val stored = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_APP_DRAWER_MODE, AppDrawerMode.HORIZONTAL_PAGES.name)

        return runCatching {
            AppDrawerMode.valueOf(stored ?: AppDrawerMode.HORIZONTAL_PAGES.name)
        }.getOrDefault(AppDrawerMode.HORIZONTAL_PAGES)
    }

    fun saveAppDrawerMode(context: Context, mode: AppDrawerMode) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_APP_DRAWER_MODE, mode.name)
            .apply()
    }

    fun loadAppDrawerPageTransition(context: Context): PageTransitionEffect {
        val stored = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_APP_DRAWER_PAGE_TRANSITION, PageTransitionEffect.CROSSFADE.name)

        if (stored == "RETRO_TILES") return PageTransitionEffect.RETRO_FLY_TILES
        if (stored == "SPHERE_DIAGNOSTIC_MEDIUM" || stored == "SPHERE_DIAGNOSTIC_LOW") {
            return PageTransitionEffect.SPHERE
        }
        if (stored == "SLIDE_REVEAL") return PageTransitionEffect.CROSSFADE
        if (stored == "FADE") return PageTransitionEffect.CROSSFADE
        if (stored == "STACK") return PageTransitionEffect.DEFAULT
        if (stored == "DEPTH") return PageTransitionEffect.SCALE
        if (stored == "CUBE") return PageTransitionEffect.CUBE_OUT_HORIZONTAL_OREO
        // Read older stored values (also used by JSON settings restore) safely.
        if (stored == "PAGE_CURL_GOOGLE" || stored == "PAGE_CURL") return PageTransitionEffect.PAGE_CURL_SOFT
        if (stored == "PAGE_CURL_APPLE") return PageTransitionEffect.PAGE_CURL_SOFT
        return runCatching {
            PageTransitionEffect.valueOf(stored ?: PageTransitionEffect.CROSSFADE.name)
        }.getOrDefault(PageTransitionEffect.CROSSFADE)
    }

    fun saveAppDrawerPageTransition(context: Context, effect: PageTransitionEffect) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_APP_DRAWER_PAGE_TRANSITION, effect.name)
            .apply()
    }

    fun loadAppDrawerIconEffect(context: Context): AppDrawerIconEffect {
        val stored = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_APP_DRAWER_ICON_EFFECT, AppDrawerIconEffect.SPIN_2D.name)

        return runCatching {
            AppDrawerIconEffect.valueOf(stored ?: AppDrawerIconEffect.SPIN_2D.name)
        }.getOrDefault(AppDrawerIconEffect.SPIN_2D)
    }

    fun saveAppDrawerIconEffect(context: Context, effect: AppDrawerIconEffect) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_APP_DRAWER_ICON_EFFECT, effect.name)
            .apply()
    }

    fun loadAppLaunchAnimation(context: Context): AppLaunchAnimationEffect {
        val stored = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_APP_LAUNCH_ANIMATION, AppLaunchAnimationEffect.ICON_EXPAND.name)
        return runCatching { AppLaunchAnimationEffect.valueOf(stored ?: AppLaunchAnimationEffect.ICON_EXPAND.name) }
            .getOrDefault(AppLaunchAnimationEffect.ICON_EXPAND)
    }

    fun saveAppLaunchAnimation(context: Context, effect: AppLaunchAnimationEffect) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit().putString(KEY_APP_LAUNCH_ANIMATION, effect.name).apply()
    }

    fun loadAppLaunchDelay(context: Context): Int =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getInt(KEY_APP_LAUNCH_DELAY, 480).coerceIn(100, 1000)

    fun saveAppLaunchDelay(context: Context, value: Int) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
            .putInt(KEY_APP_LAUNCH_DELAY, value.coerceIn(100, 1000)).apply()

    fun loadAppLaunchAnimationDuration(context: Context): Int =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getInt(KEY_APP_LAUNCH_ANIMATION_DURATION, 480).coerceIn(100, 1000)

    fun saveAppLaunchAnimationDuration(context: Context, value: Int) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
            .putInt(KEY_APP_LAUNCH_ANIMATION_DURATION, value.coerceIn(100, 1000)).apply()

    fun loadAppDragAnimation(context: Context): AppLaunchAnimationEffect =
        loadEnum(context, KEY_APP_DRAG_ANIMATION, AppLaunchAnimationEffect.FRAME)

    fun saveAppDragAnimation(context: Context, value: AppLaunchAnimationEffect) =
        saveEnum(context, KEY_APP_DRAG_ANIMATION, value)

    fun loadAppDragAnimationDuration(context: Context): Int =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getInt(KEY_APP_DRAG_ANIMATION_DURATION, 360).coerceIn(100, 1000)

    fun saveAppDragAnimationDuration(context: Context, value: Int) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
            .putInt(KEY_APP_DRAG_ANIMATION_DURATION, value.coerceIn(100, 1000)).apply()

    fun loadLinkLaunchDelayToAnimation(context: Context): Boolean =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_LINK_LAUNCH_DELAY_TO_ANIMATION, true)

    fun saveLinkLaunchDelayToAnimation(context: Context, value: Boolean) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
            .putBoolean(KEY_LINK_LAUNCH_DELAY_TO_ANIMATION, value).apply()

    fun loadAppDrawerDragOverviewMode(context: Context): AppDrawerDragOverviewMode =
        loadEnum(context, KEY_APP_DRAWER_DRAG_OVERVIEW_MODE, AppDrawerDragOverviewMode.FILL)

    fun saveAppDrawerDragOverviewMode(context: Context, value: AppDrawerDragOverviewMode) =
        saveEnum(context, KEY_APP_DRAWER_DRAG_OVERVIEW_MODE, value)

    fun loadAppDrawerCrossPageDragMode(context: Context): AppDrawerCrossPageDragMode =
        loadEnum(context, KEY_APP_DRAWER_CROSS_PAGE_DRAG_MODE, AppDrawerCrossPageDragMode.PAGE_OVERVIEW)

    fun saveAppDrawerCrossPageDragMode(context: Context, value: AppDrawerCrossPageDragMode) =
        saveEnum(context, KEY_APP_DRAWER_CROSS_PAGE_DRAG_MODE, value)

    fun loadAppDrawerPagePreviewMode(context: Context): AppDrawerPagePreviewMode =
        loadEnum(context, KEY_APP_DRAWER_PAGE_PREVIEW_MODE, AppDrawerPagePreviewMode.SIMPLE)

    fun saveAppDrawerPagePreviewMode(context: Context, value: AppDrawerPagePreviewMode) =
        saveEnum(context, KEY_APP_DRAWER_PAGE_PREVIEW_MODE, value)

    fun loadAppDrawerDragPageHover(context: Context): Int =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getInt(KEY_APP_DRAWER_DRAG_PAGE_HOVER, 650).coerceIn(250, 2000)

    fun saveAppDrawerDragPageHover(context: Context, value: Int) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
            .putInt(KEY_APP_DRAWER_DRAG_PAGE_HOVER, value.coerceIn(250, 2000)).apply()

    fun loadAppDrawerQuickAction(context: Context): AppDrawerQuickAction {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val kind = runCatching {
            AppDrawerQuickActionKind.valueOf(
                prefs.getString(KEY_APP_DRAWER_QUICK_ACTION_KIND, null).orEmpty()
            )
        }.getOrDefault(AppDrawerQuickActionKind.PAGE_OVERVIEW)
        val value = prefs.getString(KEY_APP_DRAWER_QUICK_ACTION_VALUE, "").orEmpty()
        return normalizeAppDrawerQuickAction(AppDrawerQuickAction(kind, value))
    }

    fun saveAppDrawerQuickAction(context: Context, value: AppDrawerQuickAction) {
        val normalized = normalizeAppDrawerQuickAction(value)
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
            .putString(KEY_APP_DRAWER_QUICK_ACTION_KIND, normalized.kind.name)
            .putString(KEY_APP_DRAWER_QUICK_ACTION_VALUE, normalized.value)
            .apply()
    }

    fun loadAppDrawerWorkspaceEdgeSwipeEnabled(context: Context): Boolean =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_APP_DRAWER_WORKSPACE_EDGE_SWIPE, false)

    fun saveAppDrawerWorkspaceEdgeSwipeEnabled(context: Context, enabled: Boolean) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
            .putBoolean(KEY_APP_DRAWER_WORKSPACE_EDGE_SWIPE, enabled)
            .apply()
    }

    fun ensureNewAppCutoff(context: Context): Long {
        val appContext = context.applicationContext
        val prefs = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val packageInfo = runCatching {
            appContext.packageManager.getPackageInfo(appContext.packageName, 0)
        }.getOrNull()
        val launcherUpdate = packageInfo?.lastUpdateTime?.takeIf { it > 0L }
            ?: packageInfo?.firstInstallTime?.takeIf { it > 0L }
            ?: System.currentTimeMillis()
        val observedUpdate = prefs.getLong(KEY_NEW_APP_CUTOFF_LAUNCHER_UPDATE, 0L)
        val manuallySelected = prefs.getBoolean(KEY_NEW_APP_CUTOFF_MANUAL, false)
        val storedCutoff = prefs.getLong(KEY_NEW_APP_CUTOFF_MILLIS, 0L)
        val cutoff = when {
            storedCutoff <= 0L -> launcherUpdate
            !manuallySelected && launcherUpdate > observedUpdate -> launcherUpdate
            else -> storedCutoff
        }
        if (cutoff != storedCutoff || observedUpdate != launcherUpdate) {
            prefs.edit()
                .putLong(KEY_NEW_APP_CUTOFF_MILLIS, cutoff)
                .putLong(KEY_NEW_APP_CUTOFF_LAUNCHER_UPDATE, launcherUpdate)
                .apply()
        }
        return cutoff
    }

    fun saveNewAppCutoff(context: Context, value: Long, manual: Boolean = true) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
            .putLong(KEY_NEW_APP_CUTOFF_MILLIS, value.coerceAtLeast(0L))
            .putBoolean(KEY_NEW_APP_CUTOFF_MANUAL, manual)
            .apply()
    }

    fun loadNewAppEmblemShape(context: Context): NewAppEmblemShape =
        loadEnum(context, KEY_NEW_APP_EMBLEM_SHAPE, NewAppEmblemShape.FIRE)

    fun saveNewAppEmblemShape(context: Context, value: NewAppEmblemShape) =
        saveEnum(context, KEY_NEW_APP_EMBLEM_SHAPE, value)

    fun loadNewAppEmblemSize(context: Context): Int =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getInt(KEY_NEW_APP_EMBLEM_SIZE, 10).coerceIn(4, 25)

    fun saveNewAppEmblemSize(context: Context, value: Int) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
            .putInt(KEY_NEW_APP_EMBLEM_SIZE, value.coerceIn(4, 25)).apply()

    fun loadNewAppEmblemColor(context: Context): String =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_NEW_APP_EMBLEM_COLOR, "#FF7A18") ?: "#FF7A18"

    fun saveNewAppEmblemColor(context: Context, value: String) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
            .putString(KEY_NEW_APP_EMBLEM_COLOR, value).apply()

    fun loadNewAppEmblemColorB(context: Context): String =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_NEW_APP_EMBLEM_COLOR_B, "#FFE066") ?: "#FFE066"

    fun saveNewAppEmblemColorB(context: Context, value: String) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
            .putString(KEY_NEW_APP_EMBLEM_COLOR_B, value).apply()

    fun loadNewAppEmblemGradient(context: Context): Boolean =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_NEW_APP_EMBLEM_GRADIENT, true)

    fun saveNewAppEmblemGradient(context: Context, value: Boolean) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
            .putBoolean(KEY_NEW_APP_EMBLEM_GRADIENT, value).apply()

    fun loadNewAppEmblemX(context: Context): Int =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getInt(KEY_NEW_APP_EMBLEM_X, 90).coerceIn(0, 100)

    fun saveNewAppEmblemX(context: Context, value: Int) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
            .putInt(KEY_NEW_APP_EMBLEM_X, value.coerceIn(0, 100)).apply()

    fun loadNewAppEmblemY(context: Context): Int =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getInt(KEY_NEW_APP_EMBLEM_Y, 10).coerceIn(0, 100)

    fun saveNewAppEmblemY(context: Context, value: Int) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
            .putInt(KEY_NEW_APP_EMBLEM_Y, value.coerceIn(0, 100)).apply()

    fun loadNewAppEmblemImageUri(context: Context): String? =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_NEW_APP_EMBLEM_IMAGE_URI, null)

    fun saveNewAppEmblemImageUri(context: Context, value: String?) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
            .putString(KEY_NEW_APP_EMBLEM_IMAGE_URI, value).apply()

    fun loadNewAppEmblemPackPackage(context: Context): String? =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getString(KEY_NEW_APP_EMBLEM_PACK_PACKAGE, null)

    fun loadNewAppEmblemPackDrawable(context: Context): String? =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getString(KEY_NEW_APP_EMBLEM_PACK_DRAWABLE, null)

    fun saveNewAppEmblemPackIcon(context: Context, packPackage: String?, drawable: String?) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
            .putString(KEY_NEW_APP_EMBLEM_PACK_PACKAGE, packPackage)
            .putString(KEY_NEW_APP_EMBLEM_PACK_DRAWABLE, drawable)
            .apply()

    fun loadOpenedApps(context: Context): Set<String> =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getStringSet(KEY_OPENED_APPS, emptySet())?.toSet().orEmpty()

    fun markAppOpened(context: Context, component: String) {
        val updated = loadOpenedApps(context) + component
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
            .putStringSet(KEY_OPENED_APPS, updated).apply()
    }

    fun loadAppDrawerColumns(context: Context): Int =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getInt(KEY_APP_DRAWER_COLUMNS, adaptiveDrawerGrid(context.resources.configuration.screenWidthDp,
                context.resources.configuration.screenHeightDp).first).coerceIn(3, 10)

    fun hasManualAppDrawerColumns(context: Context): Boolean =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).contains(KEY_APP_DRAWER_COLUMNS)

    fun saveAppDrawerColumns(context: Context, value: Int) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
            .putInt(KEY_APP_DRAWER_COLUMNS, value.coerceIn(3, 10)).apply()

    fun loadAppDrawerRows(context: Context): Int =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getInt(KEY_APP_DRAWER_ROWS, adaptiveDrawerGrid(context.resources.configuration.screenWidthDp,
                context.resources.configuration.screenHeightDp).second).coerceIn(3, 20)

    fun hasManualAppDrawerRows(context: Context): Boolean =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).contains(KEY_APP_DRAWER_ROWS)

    fun saveAppDrawerRows(context: Context, value: Int) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
            .putInt(KEY_APP_DRAWER_ROWS, value.coerceIn(3, 20)).apply()

    fun loadAppDrawerItemWidth(context: Context): Int =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getInt(KEY_APP_DRAWER_ITEM_WIDTH, 80).coerceIn(12, 180)

    fun saveAppDrawerItemWidth(context: Context, value: Int) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
            .putInt(KEY_APP_DRAWER_ITEM_WIDTH, value.coerceIn(12, 180)).apply()

    fun loadAppDrawerItemHeight(context: Context): Int =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getInt(KEY_APP_DRAWER_ITEM_HEIGHT, 90).coerceIn(12, 220)

    fun saveAppDrawerItemHeight(context: Context, value: Int) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
            .putInt(KEY_APP_DRAWER_ITEM_HEIGHT, value.coerceIn(12, 220)).apply()

    fun loadAppDrawerIndependentSize(context: Context): Boolean =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_APP_DRAWER_INDEPENDENT_SIZE, false)

    fun saveAppDrawerIndependentSize(context: Context, value: Boolean) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
            .putBoolean(KEY_APP_DRAWER_INDEPENDENT_SIZE, value).apply()

    fun loadAppDrawerFillAvailable(context: Context): Boolean =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_APP_DRAWER_FILL_AVAILABLE, true)

    fun saveAppDrawerFillAvailable(context: Context, value: Boolean) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
            .putBoolean(KEY_APP_DRAWER_FILL_AVAILABLE, value).apply()

    fun loadAppDrawerScreenHeightDp(context: Context): Int =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getInt(KEY_APP_DRAWER_SCREEN_HEIGHT_DP, 0).coerceIn(0, 2000)

    fun saveAppDrawerScreenHeightDp(context: Context, value: Int) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
            .putInt(KEY_APP_DRAWER_SCREEN_HEIGHT_DP, value.coerceIn(0, 2000)).apply()

    fun loadAppDrawerFrameShape(context: Context): AppItemFrameShape =
        loadEnum(context, KEY_APP_DRAWER_FRAME_SHAPE, AppItemFrameShape.NONE)

    fun saveAppDrawerFrameShape(context: Context, value: AppItemFrameShape) =
        saveEnum(context, KEY_APP_DRAWER_FRAME_SHAPE, value)

    fun loadAppDrawerFrameColor(context: Context): String =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_APP_DRAWER_FRAME_COLOR, "#242936") ?: "#242936"

    fun saveAppDrawerFrameColor(context: Context, value: String) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
            .putString(KEY_APP_DRAWER_FRAME_COLOR, value).apply()

    fun loadAppDrawerFrameOpacity(context: Context): Int =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getInt(KEY_APP_DRAWER_FRAME_OPACITY, 36).coerceIn(0, 100)

    fun saveAppDrawerFrameOpacity(context: Context, value: Int) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
            .putInt(KEY_APP_DRAWER_FRAME_OPACITY, value.coerceIn(0, 100)).apply()

    fun loadWidgetPickerMode(context: Context): WidgetPickerDisplayMode =
        loadEnum(context, KEY_WIDGET_PICKER_MODE, WidgetPickerDisplayMode.PREVIEW)

    fun saveWidgetPickerMode(context: Context, value: WidgetPickerDisplayMode) =
        saveEnum(context, KEY_WIDGET_PICKER_MODE, value)

    fun loadAppDrawerCustomOrder(context: Context): List<String> {
        val raw = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_APP_DRAWER_CUSTOM_ORDER, null) ?: return emptyList()
        return runCatching {
            val array = JSONArray(raw)
            buildList { for (i in 0 until array.length()) add(array.getString(i)) }
        }.getOrDefault(emptyList())
    }

    fun saveAppDrawerCustomOrder(context: Context, order: List<String>) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
            .putString(KEY_APP_DRAWER_CUSTOM_ORDER, JSONArray(order.distinct()).toString()).apply()
    }

    fun loadAppDrawerReorderHold(context: Context): Int =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getInt(KEY_APP_DRAWER_REORDER_HOLD, 220).coerceIn(100, 900)

    fun saveAppDrawerReorderHold(context: Context, value: Int) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
            .putInt(KEY_APP_DRAWER_REORDER_HOLD, value.coerceIn(100, 900)).apply()

    fun loadFolderFanSpeed(context: Context): Int =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getInt(KEY_FOLDER_FAN_SPEED, 90).coerceIn(15, 100)

    fun saveFolderFanSpeed(context: Context, value: Int) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
            .putInt(KEY_FOLDER_FAN_SPEED, value.coerceIn(15, 100)).apply()

    fun loadFolderFanDecay(context: Context): Int =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getInt(KEY_FOLDER_FAN_DECAY, 95).coerceIn(0, 100)

    fun saveFolderFanDecay(context: Context, value: Int) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
            .putInt(KEY_FOLDER_FAN_DECAY, value.coerceIn(0, 100)).apply()

    fun loadFolderFanCloseMode(context: Context): FolderFanCloseMode =
        loadEnum(context, KEY_FOLDER_FAN_CLOSE_MODE, FolderFanCloseMode.FLY_BACK)

    fun saveFolderFanCloseMode(context: Context, value: FolderFanCloseMode) =
        saveEnum(context, KEY_FOLDER_FAN_CLOSE_MODE, value)

    fun loadFolderFanIconSize(context: Context): Int =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getInt(KEY_FOLDER_FAN_ICON_SIZE, 54).coerceIn(24, 96)

    fun saveFolderFanIconSize(context: Context, value: Int) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
            .putInt(KEY_FOLDER_FAN_ICON_SIZE, value.coerceIn(24, 96)).apply()

    fun loadFolderFanSpacing(context: Context): Int =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getInt(KEY_FOLDER_FAN_SPACING, 150).coerceIn(48, 260)

    fun saveFolderFanSpacing(context: Context, value: Int) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
            .putInt(KEY_FOLDER_FAN_SPACING, value.coerceIn(48, 260)).apply()

    fun loadFolderReorderLineColor(context: Context): String =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_FOLDER_REORDER_LINE_COLOR, "#9BB2FF")
            ?.let(::normalizedColorHex) ?: "#9BB2FF"

    fun saveFolderReorderLineColor(context: Context, value: String) =
        normalizedColorHex(value)?.let { normalized ->
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
                .putString(KEY_FOLDER_REORDER_LINE_COLOR, normalized).apply()
        }

    fun loadFolderReorderLineThickness(context: Context): Int =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getInt(KEY_FOLDER_REORDER_LINE_THICKNESS, 3).coerceIn(1, 12)

    fun saveFolderReorderLineThickness(context: Context, value: Int) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
            .putInt(KEY_FOLDER_REORDER_LINE_THICKNESS, value.coerceIn(1, 12)).apply()
    fun loadWallpaperMode(context: Context): LauncherWallpaperMode =
        loadEnum(context, KEY_WALLPAPER_MODE, LauncherWallpaperMode.SYSTEM)

    fun saveWallpaperMode(context: Context, value: LauncherWallpaperMode) =
        saveEnum(context, KEY_WALLPAPER_MODE, value)

    fun loadWallpaperScrolling(context: Context): Boolean =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_WALLPAPER_SCROLLING, true)

    fun saveWallpaperScrolling(context: Context, value: Boolean) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
            .putBoolean(KEY_WALLPAPER_SCROLLING, value).apply()

    fun loadWallpaperDynamic(context: Context): Boolean =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_WALLPAPER_DYNAMIC, false)

    fun saveWallpaperDynamic(context: Context, value: Boolean) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
            .putBoolean(KEY_WALLPAPER_DYNAMIC, value).apply()

    fun loadWallpaperBackground(context: Context): String =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_WALLPAPER_BACKGROUND, "#101014") ?: "#101014"

    fun saveWallpaperBackground(context: Context, value: String) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
            .putString(KEY_WALLPAPER_BACKGROUND, value).apply()

    fun loadWallpaperCircleA(context: Context): String =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_WALLPAPER_CIRCLE_A, "#273456") ?: "#273456"

    fun saveWallpaperCircleA(context: Context, value: String) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
            .putString(KEY_WALLPAPER_CIRCLE_A, value).apply()

    fun loadWallpaperCircleB(context: Context): String =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_WALLPAPER_CIRCLE_B, "#49315F") ?: "#49315F"

    fun saveWallpaperCircleB(context: Context, value: String) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
            .putString(KEY_WALLPAPER_CIRCLE_B, value).apply()

    fun loadWallpaperCircleCount(context: Context): Int =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getInt(KEY_WALLPAPER_CIRCLE_COUNT, 2).coerceIn(0, 12)

    fun saveWallpaperCircleCount(context: Context, value: Int) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
            .putInt(KEY_WALLPAPER_CIRCLE_COUNT, value.coerceIn(0, 12)).apply()

    fun loadAppDrawerPage(context: Context): Int =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getInt(KEY_APP_DRAWER_PAGE, 0)
            .coerceAtLeast(0)

    fun saveAppDrawerPage(context: Context, page: Int) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putInt(KEY_APP_DRAWER_PAGE, page.coerceAtLeast(0))
            .apply()
    }

    fun loadSwipeDownAction(context: Context): DesktopGestureAction =
        loadEnum(context, KEY_SWIPE_DOWN_ACTION, DesktopGestureAction.NOTIFICATION_SHADE)

    fun saveSwipeDownAction(context: Context, action: DesktopGestureAction) =
        saveEnum(context, KEY_SWIPE_DOWN_ACTION, action)

    fun loadSwipeUpAction(context: Context): DesktopGestureAction {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        if (!prefs.getBoolean(KEY_SIDE_DOCK_GESTURE_MIGRATED, false)) {
            prefs.edit()
                .putBoolean(KEY_SIDE_DOCK_GESTURE_MIGRATED, true)
                .putString(KEY_SWIPE_UP_ACTION, DesktopGestureAction.SIDE_DOCK.name)
                .apply()
            return DesktopGestureAction.SIDE_DOCK
        }
        return loadEnum(context, KEY_SWIPE_UP_ACTION, DesktopGestureAction.SIDE_DOCK)
    }

    fun saveSwipeUpAction(context: Context, action: DesktopGestureAction) =
        saveEnum(context, KEY_SWIPE_UP_ACTION, action)

    fun loadDoubleTapAction(context: Context): DesktopGestureAction =
        loadEnum(context, KEY_DOUBLE_TAP_ACTION, DesktopGestureAction.NONE)

    fun saveDoubleTapAction(context: Context, action: DesktopGestureAction) =
        saveEnum(context, KEY_DOUBLE_TAP_ACTION, action)

    fun loadPinchAction(context: Context): DesktopGestureAction =
        loadEnum(context, KEY_PINCH_ACTION, DesktopGestureAction.WORKSPACE_OVERVIEW)

    fun savePinchAction(context: Context, action: DesktopGestureAction) =
        saveEnum(context, KEY_PINCH_ACTION, action)

    fun loadMultiSwipeAction(context: Context, fingers: Int, direction: SwipeDirection): DesktopGestureAction =
        loadEnum(context, "$KEY_MULTI_SWIPE_PREFIX${fingers}_${direction.name}", DesktopGestureAction.NONE)

    fun saveMultiSwipeAction(context: Context, fingers: Int, direction: SwipeDirection, action: DesktopGestureAction) =
        saveEnum(context, "$KEY_MULTI_SWIPE_PREFIX${fingers}_${direction.name}", action)

    fun loadIgnoreGesturesOnWidgets(context: Context): Boolean =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_IGNORE_GESTURES_ON_WIDGETS, true)

    fun saveIgnoreGesturesOnWidgets(context: Context, enabled: Boolean) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit().putBoolean(KEY_IGNORE_GESTURES_ON_WIDGETS, enabled).apply()

    fun loadSideDockPosition(context: Context): SideDockPosition =
        loadEnum(context, KEY_SIDE_DOCK_POSITION, SideDockPosition.LEFT)

    fun saveSideDockPosition(context: Context, position: SideDockPosition) =
        saveEnum(context, KEY_SIDE_DOCK_POSITION, position)

    fun loadSideDockOpacity(context: Context): Int =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getInt(KEY_SIDE_DOCK_OPACITY, 70).coerceIn(0, 100)

    fun saveSideDockOpacity(context: Context, opacity: Int) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit().putInt(KEY_SIDE_DOCK_OPACITY, opacity.coerceIn(0, 100)).apply()

    fun loadSideDockEntries(context: Context): List<SideDockEntry> {
        val raw = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_SIDE_DOCK_ITEMS, null) ?: return emptyList()
        return runCatching {
            val array = JSONArray(raw)
            buildList {
                for (index in 0 until array.length()) {
                    val item = array.getJSONObject(index)
                    add(
                        SideDockEntry(
                            id = item.getString("id"),
                            label = item.optString("label", l10n(R.string.ui_item)),
                            componentName = item.optString("component", "").takeIf { it.isNotBlank() },
                            intentAction = canonicalLauncherAction(item.optString("action", "").takeIf { it.isNotBlank() }),
                            componentNames = item.optJSONArray("components")?.let { components ->
                                buildList { for (i in 0 until components.length()) add(components.getString(i)) }
                            }.orEmpty(),
                            folder = item.optBoolean("folder", false),
                            folderShape = runCatching { FolderShape.valueOf(item.optString("folderShape", FolderShape.DEFAULT.name)) }.getOrDefault(FolderShape.DEFAULT),
                            folderColorHex = item.optString("folderColorHex", "#4F5872"),
                            folderOpacityPercent = item.optInt("folderOpacityPercent", 67).coerceIn(0, 100),
                            customIconUri = item.optString("customIconUri", "").takeIf(String::isNotBlank),
                            customIconPackPackage = item.optString("customIconPackPackage", "").takeIf(String::isNotBlank),
                            customIconPackDrawable = item.optString("customIconPackDrawable", "").takeIf(String::isNotBlank)
                        )
                    )
                }
            }
        }.getOrDefault(emptyList())
    }

    fun saveSideDockEntries(context: Context, entries: List<SideDockEntry>) {
        val array = JSONArray()
        entries.forEach { entry ->
            array.put(JSONObject().apply {
                put("id", entry.id)
                put("label", entry.label)
                entry.componentName?.let { put("component", it) }
                entry.intentAction?.let { put("action", it) }
                if (entry.componentNames.isNotEmpty()) put("components", JSONArray(entry.componentNames))
                put("folder", entry.isFolder)
                put("folderShape", entry.folderShape.name)
                put("folderColorHex", entry.folderColorHex)
                put("folderOpacityPercent", entry.folderOpacityPercent)
                entry.customIconUri?.let { put("customIconUri", it) }
                entry.customIconPackPackage?.let { put("customIconPackPackage", it) }
                entry.customIconPackDrawable?.let { put("customIconPackDrawable", it) }
            })
        }
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit().putString(KEY_SIDE_DOCK_ITEMS, array.toString()).apply()
    }

    fun loadWidgetEditBarPosition(context: Context): WidgetEditBarPosition =
        loadEnum(context, KEY_WIDGET_EDIT_BAR_POSITION, WidgetEditBarPosition.CHECK_RIGHT)

    fun saveWidgetEditBarPosition(context: Context, position: WidgetEditBarPosition) =
        saveEnum(context, KEY_WIDGET_EDIT_BAR_POSITION, position)

    fun loadAppSortMode(context: Context): AppSortMode =
        loadEnum(context, KEY_APP_SORT_MODE, AppSortMode.NAME)

    fun saveAppSortMode(context: Context, mode: AppSortMode) =
        saveEnum(context, KEY_APP_SORT_MODE, mode)

    private inline fun <reified T : Enum<T>> loadEnum(
        context: Context,
        key: String,
        default: T
    ): T {
        val raw = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(key, default.name)
        return enumValues<T>().firstOrNull { it.name == raw } ?: default
    }

    private fun saveEnum(context: Context, key: String, value: Enum<*>) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit().putString(key, value.name).apply()
    }

    fun loadAppDrawerEdgeSwitchDp(context: Context): Int =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getInt(KEY_APP_DRAWER_EDGE_SWITCH_DP, 52).coerceIn(16, 140)

    fun saveAppDrawerEdgeSwitchDp(context: Context, value: Int) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
            .putInt(KEY_APP_DRAWER_EDGE_SWITCH_DP, value.coerceIn(16, 140)).apply()

    fun loadAppDrawerBackgroundMode(context: Context): AppDrawerBackgroundMode =
        loadEnum(context, KEY_APP_DRAWER_BACKGROUND_MODE, AppDrawerBackgroundMode.SOLID)

    fun saveAppDrawerBackgroundMode(context: Context, value: AppDrawerBackgroundMode) =
        saveEnum(context, KEY_APP_DRAWER_BACKGROUND_MODE, value)

    fun loadAppDrawerBackgroundOpacity(context: Context): Int =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getInt(KEY_APP_DRAWER_BACKGROUND_OPACITY, 65).coerceIn(0, 100)

    fun saveAppDrawerBackgroundOpacity(context: Context, value: Int) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
            .putInt(KEY_APP_DRAWER_BACKGROUND_OPACITY, value.coerceIn(0, 100)).apply()

    fun loadAppDrawerBackgroundColorA(context: Context): String =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_APP_DRAWER_BACKGROUND_COLOR_A, "#1A1A22") ?: "#1A1A22"

    fun saveAppDrawerBackgroundColorA(context: Context, value: String) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
            .putString(KEY_APP_DRAWER_BACKGROUND_COLOR_A, value).apply()

    fun loadAppDrawerBackgroundColorB(context: Context): String =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_APP_DRAWER_BACKGROUND_COLOR_B, "#28334F") ?: "#28334F"

    fun saveAppDrawerBackgroundColorB(context: Context, value: String) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
            .putString(KEY_APP_DRAWER_BACKGROUND_COLOR_B, value).apply()

    fun loadAppDrawerBackgroundTextColor(context: Context): String =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_APP_DRAWER_BACKGROUND_TEXT_COLOR, "#FFFFFF") ?: "#FFFFFF"

    fun saveAppDrawerBackgroundTextColor(context: Context, value: String) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
            .putString(KEY_APP_DRAWER_BACKGROUND_TEXT_COLOR, value).apply()

    fun loadAppDrawerBackgroundImageUri(context: Context): String? =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_APP_DRAWER_BACKGROUND_IMAGE_URI, null)

    fun saveAppDrawerBackgroundImageUri(context: Context, value: String?) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
            .apply { if (value == null) remove(KEY_APP_DRAWER_BACKGROUND_IMAGE_URI) else putString(KEY_APP_DRAWER_BACKGROUND_IMAGE_URI, value) }.apply()

    fun loadAppDrawerFrameTextColor(context: Context): String =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_APP_DRAWER_FRAME_TEXT_COLOR, "#FFFFFF") ?: "#FFFFFF"

    fun saveAppDrawerFrameTextColor(context: Context, value: String) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
            .putString(KEY_APP_DRAWER_FRAME_TEXT_COLOR, value).apply()

    fun loadSideDockTextColor(context: Context): String =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_SIDE_DOCK_TEXT_COLOR, "#FFFFFF") ?: "#FFFFFF"

    fun saveSideDockTextColor(context: Context, value: String) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
            .putString(KEY_SIDE_DOCK_TEXT_COLOR, value).apply()

    fun loadBottomDockPageCount(context: Context): Int =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getInt(KEY_BOTTOM_DOCK_PAGE_COUNT, 3).coerceIn(1, 5)

    fun saveBottomDockPageCount(context: Context, value: Int) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
            .putInt(KEY_BOTTOM_DOCK_PAGE_COUNT, value.coerceIn(1, 5)).apply()

    fun loadBottomDockLoop(context: Context): Boolean =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_BOTTOM_DOCK_LOOP, false)

    fun saveBottomDockLoop(context: Context, value: Boolean) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
            .putBoolean(KEY_BOTTOM_DOCK_LOOP, value).apply()

    fun loadBottomDockIndicatorToggle(context: Context): Boolean =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_BOTTOM_DOCK_INDICATOR_TOGGLE, false)

    fun saveBottomDockIndicatorToggle(context: Context, value: Boolean) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
            .putBoolean(KEY_BOTTOM_DOCK_INDICATOR_TOGGLE, value).apply()

    fun loadBottomDockAddButtonEnabled(context: Context): Boolean =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_BOTTOM_DOCK_ADD_BUTTON_ENABLED, true)

    fun saveBottomDockAddButtonEnabled(context: Context, value: Boolean) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
            .putBoolean(KEY_BOTTOM_DOCK_ADD_BUTTON_ENABLED, value).apply()

    fun loadBottomDockBackgroundColor(context: Context): String =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_BOTTOM_DOCK_BACKGROUND_COLOR, "#1B1B23") ?: "#1B1B23"

    fun saveBottomDockBackgroundColor(context: Context, value: String) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
            .putString(KEY_BOTTOM_DOCK_BACKGROUND_COLOR, value).apply()

    fun loadBottomDockBackgroundOpacity(context: Context): Int =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getInt(KEY_BOTTOM_DOCK_BACKGROUND_OPACITY, 0).coerceIn(0, 100)

    fun saveBottomDockBackgroundOpacity(context: Context, value: Int) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
            .putInt(KEY_BOTTOM_DOCK_BACKGROUND_OPACITY, value.coerceIn(0, 100)).apply()

    fun loadBottomDockTextColor(context: Context): String =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_BOTTOM_DOCK_TEXT_COLOR, "#FFFFFF") ?: "#FFFFFF"

    fun saveBottomDockTextColor(context: Context, value: String) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
            .putString(KEY_BOTTOM_DOCK_TEXT_COLOR, value).apply()

    fun loadBottomDockShelfHeight(context: Context): Int =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getInt(KEY_BOTTOM_DOCK_SHELF_HEIGHT, 40).coerceIn(0, 72)

    fun saveBottomDockShelfHeight(context: Context, value: Int) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
            .putInt(KEY_BOTTOM_DOCK_SHELF_HEIGHT, value.coerceIn(0, 72)).apply()

    fun loadBottomDockIconVerticalOffset(context: Context): Int =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getInt(KEY_BOTTOM_DOCK_ICON_VERTICAL_OFFSET, 0).coerceIn(-32, 32)

    fun saveBottomDockIconVerticalOffset(context: Context, value: Int) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
            .putInt(KEY_BOTTOM_DOCK_ICON_VERTICAL_OFFSET, value.coerceIn(-32, 32)).apply()

    fun loadBottomDockShelfVerticalOffset(context: Context): Int =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getInt(KEY_BOTTOM_DOCK_SHELF_VERTICAL_OFFSET, 0).coerceIn(-32, 32)

    fun saveBottomDockShelfVerticalOffset(context: Context, value: Int) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
            .putInt(KEY_BOTTOM_DOCK_SHELF_VERTICAL_OFFSET, value.coerceIn(-32, 32)).apply()

    fun loadBottomDockPageIndicatorVisible(context: Context): Boolean =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_BOTTOM_DOCK_PAGE_INDICATOR_VISIBLE, true)

    fun saveBottomDockPageIndicatorVisible(context: Context, value: Boolean) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
            .putBoolean(KEY_BOTTOM_DOCK_PAGE_INDICATOR_VISIBLE, value).apply()

    fun loadBottomDockPageIndicatorGap(context: Context): Int =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getInt(KEY_BOTTOM_DOCK_PAGE_INDICATOR_GAP, 4).coerceIn(0, 32)

    fun saveBottomDockPageIndicatorGap(context: Context, value: Int) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
            .putInt(KEY_BOTTOM_DOCK_PAGE_INDICATOR_GAP, value.coerceIn(0, 32)).apply()

    fun loadBottomDockLabelsVisible(context: Context): Boolean =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_BOTTOM_DOCK_LABELS_VISIBLE, true)

    fun saveBottomDockLabelsVisible(context: Context, value: Boolean) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
            .putBoolean(KEY_BOTTOM_DOCK_LABELS_VISIBLE, value).apply()

    fun loadBottomDockShelfShape(context: Context): DockShelfShape =
        loadEnum(context, KEY_BOTTOM_DOCK_SHELF_SHAPE, DockShelfShape.PLATFORM)

    fun saveBottomDockShelfShape(context: Context, value: DockShelfShape) =
        saveEnum(context, KEY_BOTTOM_DOCK_SHELF_SHAPE, value)

    fun loadBottomDockShelfColor(context: Context): String =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_BOTTOM_DOCK_SHELF_COLOR, "#FFFFFF") ?: "#FFFFFF"

    fun saveBottomDockShelfColor(context: Context, value: String) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
            .putString(KEY_BOTTOM_DOCK_SHELF_COLOR, value).apply()

    fun loadBottomDockShelfOpacity(context: Context): Int =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getInt(KEY_BOTTOM_DOCK_SHELF_OPACITY, 20).coerceIn(0, 100)

    fun saveBottomDockShelfOpacity(context: Context, value: Int) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
            .putInt(KEY_BOTTOM_DOCK_SHELF_OPACITY, value.coerceIn(0, 100)).apply()

    fun loadBottomDockAlignment(context: Context): BottomDockAlignment =
        loadEnum(context, KEY_BOTTOM_DOCK_ALIGNMENT, BottomDockAlignment.CENTER)

    fun saveBottomDockAlignment(context: Context, value: BottomDockAlignment) =
        saveEnum(context, KEY_BOTTOM_DOCK_ALIGNMENT, value)

    fun loadBottomDockFolderCreationDelay(context: Context): Int =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getInt(KEY_BOTTOM_DOCK_FOLDER_CREATION_DELAY, 800).coerceIn(300, 2000)

    fun saveBottomDockFolderCreationDelay(context: Context, value: Int) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
            .putInt(KEY_BOTTOM_DOCK_FOLDER_CREATION_DELAY, value.coerceIn(300, 2000)).apply()

    fun loadBottomDockCapacity(context: Context): Int =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getInt(KEY_BOTTOM_DOCK_CAPACITY, 7).coerceIn(1, 10)

    fun saveBottomDockCapacity(context: Context, value: Int) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
            .putInt(KEY_BOTTOM_DOCK_CAPACITY, value.coerceIn(1, 10)).apply()

    fun loadHiddenApps(context: Context): Set<String> =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getStringSet(KEY_HIDDEN_APPS, emptySet())?.toSet() ?: emptySet()

    fun saveHiddenApps(context: Context, components: Set<String>) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
            .putStringSet(KEY_HIDDEN_APPS, components.toSet()).apply()

    fun loadKeepUninstalledAppShortcuts(context: Context): Boolean =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_KEEP_UNINSTALLED_APP_SHORTCUTS, true)

    fun saveKeepUninstalledAppShortcuts(context: Context, value: Boolean) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
            .putBoolean(KEY_KEEP_UNINSTALLED_APP_SHORTCUTS, value).apply()

    fun loadAppCustomizations(context: Context): Map<String, AppCustomization> {
        val raw = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_APP_CUSTOMIZATIONS, null) ?: return emptyMap()
        return runCatching {
            val root = JSONObject(raw)
            buildMap {
                root.keys().forEach { component ->
                    val item = root.getJSONObject(component)
                    put(component, AppCustomization(
                        label = item.optString("label", "").takeIf { it.isNotBlank() },
                        iconUri = item.optString("iconUri", "").takeIf { it.isNotBlank() },
                        iconPackPackage = item.optString("iconPackPackage", "").takeIf { it.isNotBlank() },
                        iconPackDrawable = item.optString("iconPackDrawable", "").takeIf { it.isNotBlank() }
                    ))
                }
            }
        }.getOrDefault(emptyMap())
    }

    fun saveAppCustomizations(context: Context, values: Map<String, AppCustomization>) {
        val root = JSONObject()
        values.forEach { (component, value) ->
            root.put(component, JSONObject().apply {
                value.label?.takeIf { it.isNotBlank() }?.let { put("label", it) }
                value.iconUri?.takeIf { it.isNotBlank() }?.let { put("iconUri", it) }
                value.iconPackPackage?.takeIf { it.isNotBlank() }?.let { put("iconPackPackage", it) }
                value.iconPackDrawable?.takeIf { it.isNotBlank() }?.let { put("iconPackDrawable", it) }
            })
        }
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
            .putString(KEY_APP_CUSTOMIZATIONS, root.toString()).apply()
    }

    fun loadEnvironmentEffects(context: Context): EnvironmentEffectsConfig {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val raw = prefs.getString(KEY_ENVIRONMENT_EFFECTS, null) ?: return EnvironmentEffectsConfig()
        return runCatching {
            val root = JSONObject(raw)

            val values = EnvironmentEffectType.values().associateWith { type ->
                val item = root.optJSONObject(type.name)
                if (item == null) {
                    EnvironmentEffectsConfig().options(type)
                } else {
                    val fallback = EnvironmentEffectsConfig().options(type)
                    EnvironmentEffectOptions(
                        enabled = item.optBoolean("enabled", fallback.enabled) &&
                            (type != EnvironmentEffectType.DISTORTION || DISTORTION_EFFECT_AVAILABLE),
                        direction = runCatching {
                            EnvironmentDirection.valueOf(item.optString("direction", fallback.direction.name))
                        }.getOrDefault(fallback.direction),
                        rotation = runCatching {
                            EnvironmentRotation.valueOf(item.optString("rotation", fallback.rotation.name))
                        }.getOrDefault(fallback.rotation),
                        colorHex = item.optString("color", fallback.colorHex),
                        secondaryColorHex = item.optString("secondaryColor", fallback.secondaryColorHex),
                        fillMode = runCatching {
                            EnvironmentFillMode.valueOf(item.optString("fillMode", fallback.fillMode.name))
                        }.getOrDefault(fallback.fillMode),
                        gradientType = runCatching {
                            EnvironmentGradientType.valueOf(item.optString("gradientType", fallback.gradientType.name))
                        }.getOrDefault(fallback.gradientType),
                        imageUri = item.optString("imageUri", "").takeIf { it.isNotBlank() },
                        imageUris = item.optJSONArray("imageUris")?.let { images ->
                            buildList {
                                for (imageIndex in 0 until minOf(images.length(), 10)) {
                                    images.optString(imageIndex).takeIf { it.isNotBlank() }
                                        ?.let { image -> add(image) }
                                }
                            }
                        }.orEmpty(),
                        iconPackPackage = item.optString("iconPackPackage", "").takeIf { it.isNotBlank() },
                        iconPackDrawable = item.optString("iconPackDrawable", "").takeIf { it.isNotBlank() },
                        opacityPercent = item.optInt("opacity", fallback.opacityPercent).coerceIn(0, 100),
                        blurDp = item.optInt("blur", fallback.blurDp).coerceIn(0, 18),
                        speedPercent = item.optInt("speed", fallback.speedPercent).coerceIn(0, 300),
                        count = item.optInt("count", fallback.count).coerceIn(1, 220),
                        sunRayHeightPercent = item.optInt(
                            "sunRayHeightPercent",
                            fallback.sunRayHeightPercent
                        ).coerceIn(10, 100),
                        sunRaysPerSource = item.optInt(
                            "sunRaysPerSource",
                            fallback.sunRaysPerSource
                        ).coerceIn(1, 80),
                        sunRayThicknessPx = (
                            if (item.has("sunRayThicknessPx")) {
                                item.optInt("sunRayThicknessPx", fallback.sunRayThicknessPx)
                            } else {
                                val legacyPercent = item.optInt("sunRayThicknessPercent", 100)
                                ((legacyPercent.coerceIn(25, 400) / 100f) * 24f).roundToInt()
                            }
                        ).coerceIn(2, 160),
                        sunRayMaxAngleDegrees = item.optInt(
                            "sunRayMaxAngleDegrees",
                            fallback.sunRayMaxAngleDegrees
                        ).coerceIn(0, 75),
                        distortionType = runCatching {
                            EnvironmentDistortionType.valueOf(
                                item.optString("distortionType", fallback.distortionType.name)
                            )
                        }.getOrDefault(fallback.distortionType),
                        distortionShape = runCatching {
                            FolderShape.valueOf(
                                item.optString("distortionShape", fallback.distortionShape.name)
                            )
                        }.getOrDefault(fallback.distortionShape),
                        distortionSizePercent = item.optInt(
                            "distortionSizePercent",
                            fallback.distortionSizePercent
                        ).coerceIn(8, 60),
                        distortionStrengthPercent = item.optInt(
                            "distortionStrengthPercent",
                            fallback.distortionStrengthPercent
                        ).coerceIn(10, 250)
                    )
                }
            }
            val layerMode = runCatching {
                EnvironmentEffectsLayerMode.valueOf(
                    root.optString("layerMode", EnvironmentEffectsLayerMode.BEHIND.name)
                )
            }.getOrDefault(EnvironmentEffectsLayerMode.BEHIND)
            EnvironmentEffectsConfig(values, layerMode)
        }.getOrDefault(EnvironmentEffectsConfig())
    }

    fun saveEnvironmentEffects(context: Context, config: EnvironmentEffectsConfig) {
        val root = JSONObject().put("layerMode", config.layerMode.name)
        EnvironmentEffectType.values().forEach { type ->
            val value = config.options(type)
            root.put(type.name, JSONObject().apply {
                put(
                    "enabled",
                    value.enabled &&
                        (type != EnvironmentEffectType.DISTORTION || DISTORTION_EFFECT_AVAILABLE)
                )
                put("direction", value.direction.name)
                put("rotation", value.rotation.name)
                put("color", value.colorHex)
                put("secondaryColor", value.secondaryColorHex)
                put("fillMode", value.fillMode.name)
                put("gradientType", value.gradientType.name)
                value.imageUri?.let { put("imageUri", it) }
                if (value.imageUris.isNotEmpty()) put("imageUris", JSONArray(value.imageUris.take(10)))
                value.iconPackPackage?.let { put("iconPackPackage", it) }
                value.iconPackDrawable?.let { put("iconPackDrawable", it) }
                put("opacity", value.opacityPercent)
                put("blur", value.blurDp)
                put("speed", value.speedPercent)
                put("count", value.count)
                put("sunRayHeightPercent", value.sunRayHeightPercent)
                put("sunRaysPerSource", value.sunRaysPerSource)
                put("sunRayThicknessPx", value.sunRayThicknessPx)
                put("sunRayThicknessPercent", (value.sunRayThicknessPx * 100f / 24f).roundToInt())
                put("sunRayMaxAngleDegrees", value.sunRayMaxAngleDegrees)
                put("distortionType", value.distortionType.name)
                put("distortionShape", value.distortionShape.name)
                put("distortionSizePercent", value.distortionSizePercent)
                put("distortionStrengthPercent", value.distortionStrengthPercent)
            })
        }
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
            .putString(KEY_ENVIRONMENT_EFFECTS, root.toString()).apply()
    }

    fun loadLauncherInterfaceStyle(context: Context): LauncherInterfaceStyle {
        val raw = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_LAUNCHER_INTERFACE_STYLE, null) ?: return LauncherInterfaceStyle()
        return runCatching {
            val json = JSONObject(raw)
            LauncherInterfaceStyle(
                backgroundMode = runCatching {
                    LauncherInterfaceBackgroundMode.valueOf(json.optString("backgroundMode"))
                }.getOrDefault(LauncherInterfaceBackgroundMode.NONE),
                colorAHex = json.optString("colorAHex", "#15151C"),
                colorBHex = json.optString("colorBHex", "#34345A"),
                imageUri = json.optString("imageUri").takeIf { it.isNotBlank() },
                opacityPercent = json.optInt("opacityPercent", 0).coerceIn(0, 100),
                blurDp = json.optInt("blurDp", 0).coerceIn(0, 48),
                textColorHex = json.optString("textColorHex", "#FFFFFF")
            )
        }.getOrDefault(LauncherInterfaceStyle())
    }

    fun saveLauncherInterfaceStyle(context: Context, style: LauncherInterfaceStyle) {
        val json = JSONObject().apply {
            put("backgroundMode", style.backgroundMode.name)
            put("colorAHex", style.colorAHex)
            put("colorBHex", style.colorBHex)
            style.imageUri?.let { put("imageUri", it) }
            put("opacityPercent", style.opacityPercent.coerceIn(0, 100))
            put("blurDp", style.blurDp.coerceIn(0, 48))
            put("textColorHex", style.textColorHex)
        }
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
            .putString(KEY_LAUNCHER_INTERFACE_STYLE, json.toString()).apply()
    }


    fun loadTextShadowSettings(context: Context): TextShadowSettings {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return TextShadowSettings(
            enabled = prefs.getBoolean(KEY_TEXT_SHADOW_ENABLED, true),
            offsetDp = prefs.getInt(KEY_TEXT_SHADOW_OFFSET, 2).coerceIn(0, 12),
            blurDp = prefs.getInt(KEY_TEXT_SHADOW_BLUR, 4).coerceIn(0, 24),
            colorHex = prefs.getString(KEY_TEXT_SHADOW_COLOR, "#000000") ?: "#000000",
            opacityPercent = prefs.getInt(KEY_TEXT_SHADOW_OPACITY, 65).coerceIn(0, 100)
        )
    }

    fun saveTextShadowSettings(context: Context, value: TextShadowSettings) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
            .putBoolean(KEY_TEXT_SHADOW_ENABLED, value.enabled)
            .putInt(KEY_TEXT_SHADOW_OFFSET, value.offsetDp.coerceIn(0, 12))
            .putInt(KEY_TEXT_SHADOW_BLUR, value.blurDp.coerceIn(0, 24))
            .putString(KEY_TEXT_SHADOW_COLOR, value.colorHex)
            .putInt(KEY_TEXT_SHADOW_OPACITY, value.opacityPercent.coerceIn(0, 100))
            .apply()

    fun loadBottomDockEntries(context: Context): List<BottomDockEntry> {
        val raw = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_BOTTOM_DOCK_ITEMS, null)
        if (raw.isNullOrBlank()) return listOf(
            BottomDockEntry(UUID.randomUUID().toString(), l10n(R.string.ui_phone), intentAction = DOCK_ACTION_PHONE),
            BottomDockEntry(UUID.randomUUID().toString(), l10n(R.string.ui_files), intentAction = DOCK_ACTION_FILES),
            BottomDockEntry(UUID.randomUUID().toString(), l10n(R.string.ui_applications), intentAction = DOCK_ACTION_APPS),
            BottomDockEntry(UUID.randomUUID().toString(), l10n(R.string.ui_maps), intentAction = DOCK_ACTION_MAPS),
            BottomDockEntry(UUID.randomUUID().toString(), l10n(R.string.ui_messages), intentAction = DOCK_ACTION_MESSAGES)
        ).also { saveBottomDockEntries(context, it) }
        return runCatching {
            val array = JSONArray(raw)
            buildList {
                for (index in 0 until array.length()) {
                    val item = array.getJSONObject(index)
                    val componentsJson = item.optJSONArray("components") ?: JSONArray()
                    val components = buildList {
                        for (componentIndex in 0 until componentsJson.length()) {
                            add(componentsJson.getString(componentIndex))
                        }
                    }
                    add(BottomDockEntry(
                        id = item.getString("id"),
                        label = item.optString("label", l10n(R.string.ui_item)),
                        componentName = item.optString("component", "").takeIf { it.isNotBlank() },
                        intentAction = canonicalLauncherAction(item.optString("action", "").takeIf { it.isNotBlank() }),
                        page = item.optInt("page", index / 5).coerceIn(0, 4),
                        componentNames = components.distinct(),
                        folder = item.optBoolean("isFolder", components.isNotEmpty()),
                        folderShape = runCatching {
                            FolderShape.valueOf(item.optString("folderShape", FolderShape.DEFAULT.name))
                        }.getOrDefault(FolderShape.DEFAULT),
                        folderColorHex = item.optString("folderColorHex", "#4F5872"),
                        folderOpacityPercent = item.optInt("folderOpacityPercent", 67).coerceIn(0, 100),
                        customIconUri = item.optString("customIconUri", "").takeIf { it.isNotBlank() },
                        customIconPackPackage = item.optString("customIconPackPackage", "").takeIf { it.isNotBlank() },
                        customIconPackDrawable = item.optString("customIconPackDrawable", "").takeIf { it.isNotBlank() }
                    ))
                }
            }
        }.getOrDefault(emptyList())
    }

    fun saveBottomDockEntries(context: Context, entries: List<BottomDockEntry>) {
        val array = JSONArray()
        entries.forEach { entry ->
            array.put(JSONObject().apply {
                put("id", entry.id)
                put("label", entry.label)
                entry.componentName?.let { put("component", it) }
                entry.intentAction?.let { put("action", it) }
                put("page", entry.page.coerceIn(0, 4))
                if (entry.isFolder) {
                    put("isFolder", true)
                    put("components", JSONArray(entry.componentNames))
                    put("folderShape", entry.folderShape.name)
                    put("folderColorHex", entry.folderColorHex)
                    put("folderOpacityPercent", entry.folderOpacityPercent.coerceIn(0, 100))
                    entry.customIconUri?.let { put("customIconUri", it) }
                    entry.customIconPackPackage?.let { put("customIconPackPackage", it) }
                    entry.customIconPackDrawable?.let { put("customIconPackDrawable", it) }
                }
            })
        }
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
            .putString(KEY_BOTTOM_DOCK_ITEMS, array.toString()).apply()
    }

    /** Only called on a fresh profile before the first welcome is dismissed. */
    fun seedWelcomeWorkspace(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        if (prefs.contains(KEY_SHORTCUTS) || prefs.contains(KEY_FOLDERS) ||
            prefs.contains(KEY_WIDGETS)) return
        val page = loadDefaultWorkspacePage(context)
        // Keep the frequently used controls within comfortable thumb reach.
        // Settings comes last in the upper row, while the folder is lower-right.
        val standalone = listOf(
            Triple(LAUNCHER_ACTION_ENVIRONMENT_EFFECTS, 0.39f, 0.63f),
            Triple(LAUNCHER_ACTION_WORKSPACE_EFFECTS, 0.70f, 0.63f),
            Triple(LAUNCHER_ACTION_SETTINGS, 0.70f, 0.84f)
        )
        saveShortcuts(context, standalone.map { (action, x, y) ->
            WorkspaceShortcut(
                id = UUID.randomUUID().toString(),
                label = launcherActionDisplayName(action) ?: action,
                componentName = "", page = page,
                xFraction = x, yFraction = y,
                intentAction = action
            )
        })
        saveFolders(context, listOf(
            WorkspaceFolder(
                id = UUID.randomUUID().toString(),
                label = context.getString(R.string.ui_folder),
                page = page, xFraction = 0.05f, yFraction = 0.84f,
                componentNames = listOf(
                    LAUNCHER_ACTION_SIDE_DOCK, LAUNCHER_ACTION_APP_DRAWER,
                    LAUNCHER_ACTION_GESTURES, LAUNCHER_ACTION_APP_DRAWER_EFFECTS,
                    LAUNCHER_ACTION_WALLPAPER
                )
            )
        ))
        // Welcome notes are seeded as real hosted app widgets in EeLauncherApp.
    }

    fun loadShortcuts(context: Context): List<WorkspaceShortcut> {
        val raw = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_SHORTCUTS, null)
            ?: return emptyList()

        return runCatching {
            val array = JSONArray(raw)
            buildList {
                for (index in 0 until array.length()) {
                    val item = array.getJSONObject(index)
                    add(
                        WorkspaceShortcut(
                            id = item.getString("id"),
                            label = item.getString("label"),
                            componentName = item.getString("component"),
                            page = item.getInt("page"),
                            xFraction = item.optDouble("x", 0.5).toFloat().safeWorkspaceFraction(),
                            yFraction = item.optDouble("y", 0.5).toFloat().safeWorkspaceFraction(),
                            intentAction = canonicalLauncherAction(item.optString("action", "").takeIf { it.isNotBlank() }),
                            customIconUri = item.optString("customIconUri", "").takeIf { it.isNotBlank() },
                            customIconPackPackage = item.optString("customIconPackPackage", "").takeIf { it.isNotBlank() },
                            customIconPackDrawable = item.optString("customIconPackDrawable", "").takeIf { it.isNotBlank() }
                        )
                    )
                }
            }
        }.getOrDefault(emptyList())
    }

    fun saveShortcuts(context: Context, shortcuts: List<WorkspaceShortcut>) {
        val array = JSONArray()
        shortcuts.forEach { shortcut ->
            array.put(
                JSONObject().apply {
                    put("id", shortcut.id)
                    put("label", shortcut.label)
                    put("component", shortcut.componentName)
                    put("page", shortcut.page)
                    put("x", shortcut.xFraction.toDouble())
                    put("y", shortcut.yFraction.toDouble())
                    shortcut.intentAction?.let { put("action", it) }
                    shortcut.customIconUri?.let { put("customIconUri", it) }
                    shortcut.customIconPackPackage?.let { put("customIconPackPackage", it) }
                    shortcut.customIconPackDrawable?.let { put("customIconPackDrawable", it) }
                }
            )
        }

        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_SHORTCUTS, array.toString())
            .apply()
    }

    fun loadWidgets(context: Context): List<WorkspaceWidget> {
        val raw = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_WIDGETS, null)
            ?: return emptyList()

        return runCatching {
            val array = JSONArray(raw)
            buildList {
                for (index in 0 until array.length()) {
                    val item = array.getJSONObject(index)
                    add(
                        WorkspaceWidget(
                            id = item.getString("id"),
                            appWidgetId = item.getInt("appWidgetId"),
                            provider = item.optString("provider", ""),
                            page = item.getInt("page"),
                            xFraction = item.optDouble("x", 0.5).toFloat().safeWorkspaceFraction(),
                            yFraction = item.optDouble("y", 0.25).toFloat().safeWorkspaceFraction(),
                            widthDp = item.optInt("widthDp", 180).coerceIn(48, 2000),
                            heightDp = item.optInt("heightDp", 120).coerceIn(48, 2000)
                        )
                    )
                }
            }
        }.getOrDefault(emptyList())
    }

    fun saveWidgets(context: Context, widgets: List<WorkspaceWidget>) {
        val array = JSONArray()
        widgets.forEach { widget ->
            array.put(
                JSONObject().apply {
                    put("id", widget.id)
                    put("appWidgetId", widget.appWidgetId)
                    put("provider", widget.provider)
                    put("page", widget.page)
                    put("x", widget.xFraction.toDouble())
                    put("y", widget.yFraction.toDouble())
                    put("widthDp", widget.widthDp)
                    put("heightDp", widget.heightDp)
                }
            )
        }

        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_WIDGETS, array.toString())
            .apply()
    }

    fun loadFolders(context: Context): List<WorkspaceFolder> {
        val raw = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_FOLDERS, null) ?: return emptyList()
        return runCatching {
            val array = JSONArray(raw)
            buildList {
                for (index in 0 until array.length()) {
                    val item = array.getJSONObject(index)
                    val componentsJson = item.optJSONArray("components") ?: JSONArray()
                    val components = buildList {
                        for (i in 0 until componentsJson.length()) add(componentsJson.getString(i))
                    }
                    add(
                        WorkspaceFolder(
                            id = item.getString("id"),
                            label = item.optString("label", l10n(R.string.ui_folder)),
                            page = item.optInt("page", 0),
                            xFraction = item.optDouble("x", 0.5).toFloat().safeWorkspaceFraction(),
                            yFraction = item.optDouble("y", 0.5).toFloat().safeWorkspaceFraction(),
                            componentNames = components.distinct(),
                            shape = runCatching {
                                FolderShape.valueOf(item.optString("shape", FolderShape.DEFAULT.name))
                            }.getOrDefault(FolderShape.DEFAULT),
                            colorHex = item.optString("colorHex", "#4F5872"),
                            opacityPercent = item.optInt("opacityPercent", 67).coerceIn(0, 100),
                            customIconUri = item.optString("customIconUri", "").takeIf { it.isNotBlank() },
                            customIconPackPackage = item.optString("customIconPackPackage", "").takeIf { it.isNotBlank() },
                            customIconPackDrawable = item.optString("customIconPackDrawable", "").takeIf { it.isNotBlank() }
                        )
                    )
                }
            }
        }.getOrDefault(emptyList())
    }

    fun saveFolders(context: Context, folders: List<WorkspaceFolder>) {
        val array = JSONArray()
        folders.forEach { folder ->
            array.put(JSONObject().apply {
                put("id", folder.id)
                put("label", folder.label)
                put("page", folder.page)
                put("x", folder.xFraction.toDouble())
                put("y", folder.yFraction.toDouble())
                put("components", JSONArray(folder.componentNames))
                put("shape", folder.shape.name)
                put("colorHex", folder.colorHex)
                put("opacityPercent", folder.opacityPercent.coerceIn(0, 100))
                folder.customIconUri?.let { put("customIconUri", it) }
                folder.customIconPackPackage?.let { put("customIconPackPackage", it) }
                folder.customIconPackDrawable?.let { put("customIconPackDrawable", it) }
            })
        }
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit().putString(KEY_FOLDERS, array.toString()).apply()
    }
}

internal enum class IconActivationKind { LAUNCH, DRAG }

internal fun loadConfiguredIconActivation(
    context: Context,
    kind: IconActivationKind = IconActivationKind.LAUNCH
): AppLaunchAnimationEffect = when (kind) {
    IconActivationKind.LAUNCH -> LauncherStorage.loadAppLaunchAnimation(context)
    IconActivationKind.DRAG -> LauncherStorage.loadAppDragAnimation(context)
}

internal fun loadConfiguredIconActivationDuration(
    context: Context,
    kind: IconActivationKind = IconActivationKind.LAUNCH
): Int = when (kind) {
    IconActivationKind.LAUNCH -> LauncherStorage.loadAppLaunchAnimationDuration(context)
    IconActivationKind.DRAG -> LauncherStorage.loadAppDragAnimationDuration(context)
}


private data class DockItem(
    val label: String,
    val iconRes: Int,
    val onClick: () -> Unit,
    val selected: Boolean = false,
    val prominent: Boolean = false
)

@Composable
fun EeLauncherApp() {
    val context = LocalContext.current
    val activity = context as MainActivity
    val welcomeVisibleOnStart = remember(context.applicationContext) {
        val isNew = !AdvancedLauncherStorage.isWelcomeDone(context)
        if (isNew) {
            LauncherStorage.seedWelcomeWorkspace(context.applicationContext)
            activity.seedWelcomeNoteWidgets()
        }
        isNew
    }
    var welcomeVisible by remember { mutableStateOf(welcomeVisibleOnStart) }
    val colorScheme = darkColorScheme(
        background = Color(0xFF101014),
        surface = Color(0xFF17171E),
        primary = Color(0xFF9BB2FF),
        onPrimary = Color(0xFF141827),
        onBackground = Color.White,
        onSurface = Color.White
    )

    MaterialTheme(colorScheme = colorScheme) {
        Surface(modifier = Modifier.fillMaxSize(), color = Color.Transparent) {
            if (welcomeVisible) {
                WelcomeScreen(
                    onContinue = {
                        AdvancedLauncherStorage.finishWelcome(context)
                        welcomeVisible = false
                        activity.requestHomeRole()
                    },
                    onSafeMode = {
                        AdvancedLauncherStorage.save(
                            context,
                            AdvancedLauncherStorage.load(context).copy(safeMode = true)
                        )
                        AdvancedLauncherStorage.finishWelcome(context)
                        welcomeVisible = false
                    },
                    onSkip = {
                        AdvancedLauncherStorage.finishWelcome(context)
                        welcomeVisible = false
                    }
                )
            } else LauncherHome()
        }
    }
}

/** Persistent launcher options, retained in one Compose snapshot-state holder. */
private class LauncherHomePreferencesState(
    appContext: Context,
    safeMode: Boolean,
    workspaceCount: Int
) {
    val pageTransitionEffect = mutableStateOf(if (safeMode) PageTransitionEffect.DEFAULT else LauncherStorage.loadPageTransition(appContext))
    val meshSnapshotAfterIdle = mutableStateOf(LauncherStorage.loadMeshSnapshotAfterIdle(appContext))
    val appDrawerMode = mutableStateOf(LauncherStorage.loadAppDrawerMode(appContext))
    val appDrawerPageTransitionEffect = mutableStateOf(if (safeMode) PageTransitionEffect.DEFAULT else LauncherStorage.loadAppDrawerPageTransition(appContext))
    val appDrawerIconEffect = mutableStateOf(if (safeMode) AppDrawerIconEffect.NONE else LauncherStorage.loadAppDrawerIconEffect(appContext))
    val appLaunchAnimationEffect = mutableStateOf(if (safeMode) AppLaunchAnimationEffect.NONE else LauncherStorage.loadAppLaunchAnimation(appContext))
    val appLaunchDelay = mutableIntStateOf(LauncherStorage.loadAppLaunchDelay(appContext))
    val appLaunchAnimationDuration = mutableIntStateOf(LauncherStorage.loadAppLaunchAnimationDuration(appContext))
    val appDrawerDragOverviewMode = mutableStateOf(LauncherStorage.loadAppDrawerDragOverviewMode(appContext))
    val appDrawerDragPageHover = mutableIntStateOf(LauncherStorage.loadAppDrawerDragPageHover(appContext))
    val appDrawerQuickAction = mutableStateOf(LauncherStorage.loadAppDrawerQuickAction(appContext))
    val newAppEmblemShape = mutableStateOf(LauncherStorage.loadNewAppEmblemShape(appContext))
    val newAppEmblemSize = mutableIntStateOf(LauncherStorage.loadNewAppEmblemSize(appContext))
    val newAppEmblemColor = mutableStateOf(LauncherStorage.loadNewAppEmblemColor(appContext))
    val newAppEmblemColorB = mutableStateOf(LauncherStorage.loadNewAppEmblemColorB(appContext))
    val newAppEmblemGradient = mutableStateOf(LauncherStorage.loadNewAppEmblemGradient(appContext))
    val newAppEmblemX = mutableIntStateOf(LauncherStorage.loadNewAppEmblemX(appContext))
    val newAppEmblemY = mutableIntStateOf(LauncherStorage.loadNewAppEmblemY(appContext))
    val newAppEmblemImageUri = mutableStateOf(LauncherStorage.loadNewAppEmblemImageUri(appContext))
    val openedApps = mutableStateOf(LauncherStorage.loadOpenedApps(appContext))
    val appDrawerPage = mutableStateOf(LauncherStorage.loadAppDrawerPage(appContext))
    val appDrawerColumns = mutableStateOf(LauncherStorage.loadAppDrawerColumns(appContext))
    val appDrawerRows = mutableStateOf(LauncherStorage.loadAppDrawerRows(appContext))
    val appDrawerItemWidth = mutableStateOf(LauncherStorage.loadAppDrawerItemWidth(appContext))
    val appDrawerItemHeight = mutableStateOf(LauncherStorage.loadAppDrawerItemHeight(appContext))
    val appDrawerIndependentSize = mutableStateOf(LauncherStorage.loadAppDrawerIndependentSize(appContext))
    val appDrawerFillAvailable = mutableStateOf(LauncherStorage.loadAppDrawerFillAvailable(appContext))
    val appDrawerScreenHeightDp = mutableIntStateOf(LauncherStorage.loadAppDrawerScreenHeightDp(appContext))
    val appDrawerFrameShape = mutableStateOf(LauncherStorage.loadAppDrawerFrameShape(appContext))
    val appDrawerFrameColor = mutableStateOf(LauncherStorage.loadAppDrawerFrameColor(appContext))
    val appDrawerFrameOpacity = mutableStateOf(LauncherStorage.loadAppDrawerFrameOpacity(appContext))
    val appDrawerFrameTextColor = mutableStateOf(LauncherStorage.loadAppDrawerFrameTextColor(appContext))
    val appDrawerEdgeSwitchDp = mutableStateOf(LauncherStorage.loadAppDrawerEdgeSwitchDp(appContext))
    val appDrawerBackgroundMode = mutableStateOf(LauncherStorage.loadAppDrawerBackgroundMode(appContext))
    val appDrawerBackgroundOpacity = mutableStateOf(LauncherStorage.loadAppDrawerBackgroundOpacity(appContext))
    val appDrawerBackgroundColorA = mutableStateOf(LauncherStorage.loadAppDrawerBackgroundColorA(appContext))
    val appDrawerBackgroundColorB = mutableStateOf(LauncherStorage.loadAppDrawerBackgroundColorB(appContext))
    val appDrawerBackgroundTextColor = mutableStateOf(LauncherStorage.loadAppDrawerBackgroundTextColor(appContext))
    val appDrawerBackgroundImageUri = mutableStateOf(LauncherStorage.loadAppDrawerBackgroundImageUri(appContext))
    val widgetPickerMode = mutableStateOf(LauncherStorage.loadWidgetPickerMode(appContext))
    val swipeDownAction = mutableStateOf(LauncherStorage.loadSwipeDownAction(appContext))
    val swipeUpAction = mutableStateOf(LauncherStorage.loadSwipeUpAction(appContext))
    val doubleTapAction = mutableStateOf(LauncherStorage.loadDoubleTapAction(appContext))
    val pinchAction = mutableStateOf(LauncherStorage.loadPinchAction(appContext))
    val multiSwipeActions = mutableStateOf(buildMap {
            for (fingers in 2..3) for (direction in SwipeDirection.values()) {
                put(fingers to direction, LauncherStorage.loadMultiSwipeAction(appContext, fingers, direction))
            }
        })
    val widgetEditBarPosition = mutableStateOf(LauncherStorage.loadWidgetEditBarPosition(appContext))
    val appSortMode = mutableStateOf(LauncherStorage.loadAppSortMode(appContext))
    val sideDockPosition = mutableStateOf(LauncherStorage.loadSideDockPosition(appContext))
    val sideDockOpacity = mutableStateOf(LauncherStorage.loadSideDockOpacity(appContext))
    val sideDockTextColor = mutableStateOf(LauncherStorage.loadSideDockTextColor(appContext))
    val bottomDockEntries = mutableStateOf(LauncherStorage.loadBottomDockEntries(appContext))
    val bottomDockPageCount = mutableStateOf(LauncherStorage.loadBottomDockPageCount(appContext))
    val bottomDockLoop = mutableStateOf(LauncherStorage.loadBottomDockLoop(appContext))
    val bottomDockIndicatorToggle = mutableStateOf(LauncherStorage.loadBottomDockIndicatorToggle(appContext))
    val bottomDockAddButtonEnabled = mutableStateOf(LauncherStorage.loadBottomDockAddButtonEnabled(appContext))
    val bottomDockAlignment = mutableStateOf(LauncherStorage.loadBottomDockAlignment(appContext))
    val bottomDockSelectedPage = mutableIntStateOf((LauncherStorage.loadBottomDockPageCount(appContext) / 2).coerceAtLeast(0))
    val bottomDockBackgroundColor = mutableStateOf(LauncherStorage.loadBottomDockBackgroundColor(appContext))
    val bottomDockBackgroundOpacity = mutableStateOf(LauncherStorage.loadBottomDockBackgroundOpacity(appContext))
    val bottomDockTextColor = mutableStateOf(LauncherStorage.loadBottomDockTextColor(appContext))
    val bottomDockShelfHeight = mutableStateOf(LauncherStorage.loadBottomDockShelfHeight(appContext))
    val bottomDockIconVerticalOffset = mutableStateOf(LauncherStorage.loadBottomDockIconVerticalOffset(appContext))
    val bottomDockShelfVerticalOffset = mutableStateOf(LauncherStorage.loadBottomDockShelfVerticalOffset(appContext))
    val bottomDockPageIndicatorVisible = mutableStateOf(LauncherStorage.loadBottomDockPageIndicatorVisible(appContext))
    val bottomDockPageIndicatorGap = mutableStateOf(LauncherStorage.loadBottomDockPageIndicatorGap(appContext))
    val bottomDockLabelsVisible = mutableStateOf(LauncherStorage.loadBottomDockLabelsVisible(appContext))
    val bottomDockShelfShape = mutableStateOf(LauncherStorage.loadBottomDockShelfShape(appContext))
    val bottomDockShelfColor = mutableStateOf(LauncherStorage.loadBottomDockShelfColor(appContext))
    val bottomDockShelfOpacity = mutableStateOf(LauncherStorage.loadBottomDockShelfOpacity(appContext))
    val bottomDockFolderCreationDelay = mutableStateOf(LauncherStorage.loadBottomDockFolderCreationDelay(appContext))
    val bottomDockCapacity = mutableIntStateOf(LauncherStorage.loadBottomDockCapacity(appContext))
    val defaultWorkspacePage = mutableStateOf(LauncherStorage.loadDefaultWorkspacePage(appContext).coerceIn(0, workspaceCount - 1))
    val sideDockEntries = mutableStateOf(LauncherStorage.loadSideDockEntries(appContext))
    val appDrawerCustomOrder = mutableStateOf(LauncherStorage.loadAppDrawerCustomOrder(appContext))
    val appDrawerReorderHold = mutableStateOf(LauncherStorage.loadAppDrawerReorderHold(appContext))
    val folderFanSpeed = mutableStateOf(LauncherStorage.loadFolderFanSpeed(appContext))
    val folderFanDecay = mutableStateOf(LauncherStorage.loadFolderFanDecay(appContext))
    val folderFanCloseMode = mutableStateOf(LauncherStorage.loadFolderFanCloseMode(appContext))
    val folderFanIconSize = mutableStateOf(LauncherStorage.loadFolderFanIconSize(appContext))
    val folderFanSpacing = mutableStateOf(LauncherStorage.loadFolderFanSpacing(appContext))
    val folderReorderLineColor = mutableStateOf(LauncherStorage.loadFolderReorderLineColor(appContext))
    val folderReorderLineThickness = mutableStateOf(LauncherStorage.loadFolderReorderLineThickness(appContext))
    val wallpaperMode = mutableStateOf(LauncherStorage.loadWallpaperMode(appContext))
    val wallpaperScrolling = mutableStateOf(LauncherStorage.loadWallpaperScrolling(appContext))
    val wallpaperDynamic = mutableStateOf(LauncherStorage.loadWallpaperDynamic(appContext))
    val wallpaperBackground = mutableStateOf(LauncherStorage.loadWallpaperBackground(appContext))
    val wallpaperCircleA = mutableStateOf(LauncherStorage.loadWallpaperCircleA(appContext))
    val wallpaperCircleB = mutableStateOf(LauncherStorage.loadWallpaperCircleB(appContext))
    val wallpaperCircleCount = mutableStateOf(LauncherStorage.loadWallpaperCircleCount(appContext))
    val textShadowSettings = mutableStateOf(LauncherStorage.loadTextShadowSettings(appContext))
    val environmentEffects = mutableStateOf(LauncherStorage.loadEnvironmentEffects(appContext))
    val launcherInterfaceStyle = mutableStateOf(LauncherStorage.loadLauncherInterfaceStyle(appContext))
}

/**
 * Short-lived UI state used by [LauncherHome]. Keeping independent MutableState
 * objects in one remembered holder avoids generating hundreds of `remember`
 * groups in a single Compose method (JVM 64 KiB bytecode limit).
 *
 * Fields remain observable Compose snapshot states. The holder is created once
 * per LauncherHome composition, like the original individual remember blocks.
 */
private class LauncherHomeUiState {
    val drawerOpen = mutableStateOf(false)
    val appPickerMode = mutableStateOf(false)
    val sideDockAppPickerMode = mutableStateOf(false)
    val bottomDockAppPickerMode = mutableStateOf(false)
    val query = mutableStateOf("")
    val isLoading = mutableStateOf(true)
    val loadError = mutableStateOf<String?>(null)
    val reloadKey = mutableStateOf(0)
    val editingApp = mutableStateOf<AppItem?>(null)
    val iconPackPickerApp = mutableStateOf<AppItem?>(null)
    val newAppEmblemIconPackPickerVisible = mutableStateOf(false)
    val pendingCustomIconComponent = mutableStateOf<String?>(null)
    val editingShortcut = mutableStateOf<WorkspaceShortcut?>(null)
    val shortcutIconPackPickerId = mutableStateOf<String?>(null)
    val pendingShortcutIconId = mutableStateOf<String?>(null)
    val folderIconPackPickerId = mutableStateOf<String?>(null)
    val pendingFolderIconId = mutableStateOf<String?>(null)
    val appDrawerEdgePreviewVisible = mutableStateOf(false)
    val appDrawerBottomBarHeightDp = mutableIntStateOf(84)
    val showWorkspaceSettings = mutableStateOf(false)
    val settingsStartPage = mutableStateOf(LauncherSettingsPage.ROOT)
    val pendingPlacement = mutableStateOf<AppItem?>(null)
    val shortcutDragActive = mutableStateOf(false)
    val workspaceFolderDragActive = mutableStateOf(false)
    val bottomDockDragActive = mutableStateOf(false)
    val bottomDockDeleteDragActive = mutableStateOf(false)
    val bottomDockDeleteDragOver = mutableStateOf(false)
    val bottomDockDeleteBoundsInRoot = mutableStateOf(Rect.Zero)
    val desktopRootOrigin = mutableStateOf(Offset.Zero)
    val workspaceShortcutDragPointerInRoot = mutableStateOf<Offset?>(null)
    val workspaceBottomDockInsertionIndex = mutableStateOf<Int?>(null)
    val workspaceBottomDockHoverTargetId = mutableStateOf<String?>(null)
    val bottomDockBoundsInRoot = mutableStateOf(Rect.Zero)
    val workspaceBoundsOnScreen = mutableStateOf(Rect.Zero)
    val sideDockBoundsInRoot = mutableStateOf(Rect.Zero)
    val workspaceSideDockInsertionIndex = mutableStateOf<Int?>(null)
    val workspaceSideDockHoverTargetId = mutableStateOf<String?>(null)
    val shortcutMenuTarget = mutableStateOf<WorkspaceShortcut?>(null)
    val showAccessibilityPrompt = mutableStateOf(false)
    val workspaceMenuPage = mutableStateOf<Int?>(null)
    val workspaceInsertionAnchor = mutableStateOf<Triple<Int, Float, Float>?>(null)
    val addToWorkspacePage = mutableStateOf<Int?>(null)
    val settingsShortcutPage = mutableStateOf<Int?>(null)
    val widgetPickerPage = mutableStateOf<Int?>(null)
    val widgetEditingId = mutableStateOf<String?>(null)
    val folderDialogTarget = mutableStateOf<WorkspaceFolder?>(null)
    val bottomDockFolderDialogTarget = mutableStateOf<BottomDockEntry?>(null)
    val bottomDockFolderFanDisplay = mutableStateOf<FolderFanDisplay?>(null)
    val sideDockFolderFanDisplay = mutableStateOf<FolderFanDisplay?>(null)
    val showWorkspaceOverview = mutableStateOf(false)
    val showSortDialog = mutableStateOf(false)
    val folderFanDisplay = mutableStateOf<FolderFanDisplay?>(null)
    val sideDockOpen = mutableStateOf(false)
    val showSideDockAddDialog = mutableStateOf(false)
    val showSideDockSettingsPicker = mutableStateOf(false)
    val showSideDockLauncherActionPicker = mutableStateOf(false)
    val showBottomDockAddDialog = mutableStateOf(false)
    val showBottomDockSettingsPicker = mutableStateOf(false)
    val showBottomDockLauncherActionPicker = mutableStateOf(false)
    val launcherActionWorkspacePage = mutableStateOf<Int?>(null)
    val sideDockMenuTarget = mutableStateOf<SideDockEntry?>(null)
    val sideDockFolderDialogTarget = mutableStateOf<SideDockEntry?>(null)
    val bottomDockMenuTarget = mutableStateOf<BottomDockEntry?>(null)
    val sideDockDeleteMode = mutableStateOf(false)
    val appDrawerSearchVisible = mutableStateOf(false)
    val appDrawerDragState = mutableStateOf<AppDrawerDragState?>(null)
    val drawerVisuallyHidden = mutableStateOf(false)
    val pendingUninstallApp = mutableStateOf<AppItem?>(null)
    val reorderHoverSerial = mutableIntStateOf(0)
    val liveWorkspaceEffectOriginal = mutableStateOf<PageTransitionEffect?>(null)
    val liveAppDrawerEffectOriginal = mutableStateOf<PageTransitionEffect?>(null)
    val appDrawerOverviewRequestSerial = mutableIntStateOf(0)
    val bottomDockVisible = mutableStateOf(true)
    val shadeSwipeInProgress = mutableStateOf(false)
    val appDrawerBackgroundImage = mutableStateOf<ImageBitmap?>(null)
}

@Composable
private fun LauncherHome() {
    val uiState = remember { LauncherHomeUiState() }
    val context = LocalContext.current
    val pageEffectTiming = remember(context.applicationContext) { PageEffectTimingSettings(context.applicationContext) }
    val activity = context as MainActivity
    val launcherView = LocalView.current
    val repository = remember { AppsRepository(context.applicationContext) }
    remember(context.applicationContext) {
        LauncherStorage.migrateV12Defaults(context.applicationContext)
        LauncherStorage.migrateV13Defaults(context.applicationContext)
        true
    }
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current
    val configuration = LocalConfiguration.current

    var drawerOpen by uiState.drawerOpen
    var appPickerMode by uiState.appPickerMode
    var sideDockAppPickerMode by uiState.sideDockAppPickerMode
    var bottomDockAppPickerMode by uiState.bottomDockAppPickerMode
    var apps by remember { mutableStateOf<List<AppItem>>(emptyList()) }
    var query by uiState.query
    var isLoading by uiState.isLoading
    var loadError by uiState.loadError
    var reloadKey by uiState.reloadKey
    var hiddenApps by remember {
        mutableStateOf(LauncherStorage.loadHiddenApps(context.applicationContext))
    }
    var keepUninstalledAppShortcuts by remember {
        mutableStateOf(LauncherStorage.loadKeepUninstalledAppShortcuts(context.applicationContext))
    }
    var appCustomizations by remember {
        mutableStateOf(LauncherStorage.loadAppCustomizations(context.applicationContext))
    }
    var editingApp by uiState.editingApp
    var iconPackPickerApp by uiState.iconPackPickerApp
    var newAppEmblemIconPackPickerVisible by uiState.newAppEmblemIconPackPickerVisible
    var pendingCustomIconComponent by uiState.pendingCustomIconComponent
    var editingShortcut by uiState.editingShortcut
    var shortcutIconPackPickerId by uiState.shortcutIconPackPickerId
    var pendingShortcutIconId by uiState.pendingShortcutIconId
    var folderIconPackPickerId by uiState.folderIconPackPickerId
    var pendingFolderIconId by uiState.pendingFolderIconId
    var appDrawerEdgePreviewVisible by uiState.appDrawerEdgePreviewVisible
    var appDrawerBottomBarHeightDp by uiState.appDrawerBottomBarHeightDp

    var workspaceCount by remember {
        mutableStateOf(LauncherStorage.loadPageCount(context.applicationContext))
    }
    var workspaceLoop by remember {
        mutableStateOf(LauncherStorage.loadWorkspaceLoop(context.applicationContext))
    }
    var appDrawerLoop by remember {
        mutableStateOf(LauncherStorage.loadAppDrawerLoop(context.applicationContext))
    }
    var shortcuts by remember {
        mutableStateOf(
            LauncherStorage.loadShortcuts(context.applicationContext).map { shortcut ->
                shortcut.copy(page = shortcut.page.coerceIn(0, workspaceCount - 1))
            }
        )
    }
    var notes by remember {
        mutableStateOf(emptyList<WorkspaceNote>())
    }
    var widgets by remember {
        mutableStateOf(
            LauncherStorage.loadWidgets(context.applicationContext)
                .map { widget -> widget.copy(page = widget.page.coerceIn(0, workspaceCount - 1)) }
        )
    }
    var widgetActionChains by remember {
        mutableStateOf(WidgetActionChainStorage.load(context.applicationContext))
    }
    var folders by remember {
        mutableStateOf(
            LauncherStorage.loadFolders(context.applicationContext)
                .map { folder -> folder.copy(page = folder.page.coerceIn(0, workspaceCount - 1)) }
        )
    }

    LaunchedEffect(Unit) {
        // Settings restore can replace the persisted widget list while AppWidgetHost
        // still owns IDs from the previous layout.  Those IDs no longer have a
        // visible launcher widget and must be explicitly released.
        delay(250L)
        activity.cleanupOrphanedHostedWidgets(widgets.map { it.appWidgetId }.toSet())
    }

    var showWorkspaceSettings by uiState.showWorkspaceSettings
    var settingsStartPage by uiState.settingsStartPage
    var pendingPlacement by uiState.pendingPlacement
    var shortcutDragActive by uiState.shortcutDragActive
    var workspaceFolderDragActive by uiState.workspaceFolderDragActive
    var bottomDockDragActive by uiState.bottomDockDragActive
    var bottomDockDeleteDragActive by uiState.bottomDockDeleteDragActive
    var bottomDockDeleteDragOver by uiState.bottomDockDeleteDragOver
    var bottomDockDeleteBoundsInRoot by uiState.bottomDockDeleteBoundsInRoot
    var ignoreGesturesOnWidgets by remember { mutableStateOf(LauncherStorage.loadIgnoreGesturesOnWidgets(context.applicationContext)) }
    val workspaceWidgetBounds = remember { mutableStateMapOf<String, Rect>() }
    var desktopRootOrigin by uiState.desktopRootOrigin
    var workspaceShortcutDragPointerInRoot by uiState.workspaceShortcutDragPointerInRoot
    var workspaceBottomDockInsertionIndex by uiState.workspaceBottomDockInsertionIndex
    var workspaceBottomDockHoverTargetId by uiState.workspaceBottomDockHoverTargetId
    var bottomDockBoundsInRoot by uiState.bottomDockBoundsInRoot
    var workspaceBoundsOnScreen by uiState.workspaceBoundsOnScreen
    var sideDockBoundsInRoot by uiState.sideDockBoundsInRoot
    var workspaceSideDockInsertionIndex by uiState.workspaceSideDockInsertionIndex
    var workspaceSideDockHoverTargetId by uiState.workspaceSideDockHoverTargetId
    var shortcutMenuTarget by uiState.shortcutMenuTarget
    var showAccessibilityPrompt by uiState.showAccessibilityPrompt
    var workspaceMenuPage by uiState.workspaceMenuPage
    var workspaceInsertionAnchor by uiState.workspaceInsertionAnchor
    var addToWorkspacePage by uiState.addToWorkspacePage
    var settingsShortcutPage by uiState.settingsShortcutPage
    var widgetPickerPage by uiState.widgetPickerPage
    var widgetEditingId by uiState.widgetEditingId
    var folderDialogTarget by uiState.folderDialogTarget
    var bottomDockFolderDialogTarget by uiState.bottomDockFolderDialogTarget
    var bottomDockFolderFanDisplay by uiState.bottomDockFolderFanDisplay
    var sideDockFolderFanDisplay by uiState.sideDockFolderFanDisplay
    var showWorkspaceOverview by uiState.showWorkspaceOverview
    var showSortDialog by uiState.showSortDialog
    var folderFanDisplay by uiState.folderFanDisplay
    var sideDockOpen by uiState.sideDockOpen
    var settingsSideDockPreview by remember { mutableStateOf(false) }
    var settingsWidgetCheckmarkPreview by remember { mutableStateOf(false) }
    var showSideDockAddDialog by uiState.showSideDockAddDialog
    var showSideDockSettingsPicker by uiState.showSideDockSettingsPicker
    var showSideDockLauncherActionPicker by uiState.showSideDockLauncherActionPicker
    var showBottomDockAddDialog by uiState.showBottomDockAddDialog
    var showBottomDockSettingsPicker by uiState.showBottomDockSettingsPicker
    var showBottomDockLauncherActionPicker by uiState.showBottomDockLauncherActionPicker
    var launcherActionWorkspacePage by uiState.launcherActionWorkspacePage
    var sideDockMenuTarget by uiState.sideDockMenuTarget
    var sideDockFolderDialogTarget by uiState.sideDockFolderDialogTarget
    var bottomDockMenuTarget by uiState.bottomDockMenuTarget
    var sideDockDeleteMode by uiState.sideDockDeleteMode
    var appDrawerSearchVisible by uiState.appDrawerSearchVisible
    var appDrawerDragState by uiState.appDrawerDragState
    var drawerVisuallyHidden by uiState.drawerVisuallyHidden
    var pendingUninstallApp by uiState.pendingUninstallApp
    var reorderHoverSerial by uiState.reorderHoverSerial
    var advancedPreferences by remember {
        mutableStateOf(AdvancedLauncherStorage.load(context.applicationContext))
    }
    val safeMode = advancedPreferences.safeMode
    val launcherPrefs = remember {
        LauncherHomePreferencesState(context.applicationContext, safeMode, workspaceCount)
    }

    LaunchedEffect(drawerOpen) {
        if (drawerOpen && appDrawerDragState == null) drawerVisuallyHidden = false
    }
    val workspaceEditingLocked = advancedPreferences.workspaceLocked
    val sideDockEditingLocked = advancedPreferences.sideDockLocked
    val bottomDockEditingLocked = advancedPreferences.bottomDockLocked
    fun notifyDesktopEditingLocked() {
        Toast.makeText(context, l10n(R.string.ui_desktop_editing_is_locked), Toast.LENGTH_SHORT).show()
    }
    fun notifyDockEditingLocked() {
        Toast.makeText(context, l10n(R.string.ui_dock_editing_is_locked), Toast.LENGTH_SHORT).show()
    }
    var liveWorkspaceEffectOriginal by uiState.liveWorkspaceEffectOriginal
    var liveAppDrawerEffectOriginal by uiState.liveAppDrawerEffectOriginal

    var pageTransitionEffect by launcherPrefs.pageTransitionEffect
    var meshSnapshotAfterIdle by launcherPrefs.meshSnapshotAfterIdle
    var appDrawerMode by launcherPrefs.appDrawerMode
    var appDrawerPageTransitionEffect by launcherPrefs.appDrawerPageTransitionEffect
    var appDrawerIconEffect by launcherPrefs.appDrawerIconEffect
    var appLaunchAnimationEffect by launcherPrefs.appLaunchAnimationEffect
    var appLaunchDelay by launcherPrefs.appLaunchDelay
    var appLaunchAnimationDuration by launcherPrefs.appLaunchAnimationDuration
    val interactionExtras = remember(context.applicationContext) {
        LauncherInteractionExtras(context.applicationContext)
    }
    var appDrawerDragOverviewMode by launcherPrefs.appDrawerDragOverviewMode
    var appDrawerDragPageHover by launcherPrefs.appDrawerDragPageHover
    var appDrawerQuickAction by launcherPrefs.appDrawerQuickAction
    var appDrawerOverviewRequestSerial by uiState.appDrawerOverviewRequestSerial
    var newAppEmblemShape by launcherPrefs.newAppEmblemShape
    var newAppEmblemSize by launcherPrefs.newAppEmblemSize
    var newAppEmblemColor by launcherPrefs.newAppEmblemColor
    var newAppEmblemColorB by launcherPrefs.newAppEmblemColorB
    var newAppEmblemGradient by launcherPrefs.newAppEmblemGradient
    var newAppEmblemX by launcherPrefs.newAppEmblemX
    var newAppEmblemY by launcherPrefs.newAppEmblemY
    var newAppEmblemImageUri by launcherPrefs.newAppEmblemImageUri
    var openedApps by launcherPrefs.openedApps
    var appDrawerPage by launcherPrefs.appDrawerPage
    var appDrawerColumns by launcherPrefs.appDrawerColumns
    var appDrawerRows by launcherPrefs.appDrawerRows
    LaunchedEffect(configuration.screenWidthDp, configuration.screenHeightDp) {
        val (autoColumns, autoRows) = adaptiveDrawerGrid(configuration.screenWidthDp, configuration.screenHeightDp)
        if (!LauncherStorage.hasManualAppDrawerColumns(context.applicationContext)) appDrawerColumns = autoColumns
        if (!LauncherStorage.hasManualAppDrawerRows(context.applicationContext)) appDrawerRows = autoRows
    }
    var appDrawerItemWidth by launcherPrefs.appDrawerItemWidth
    var appDrawerItemHeight by launcherPrefs.appDrawerItemHeight
    var appDrawerIndependentSize by launcherPrefs.appDrawerIndependentSize
    var appDrawerFillAvailable by launcherPrefs.appDrawerFillAvailable
    var appDrawerScreenHeightDp by launcherPrefs.appDrawerScreenHeightDp
    var appDrawerFrameShape by launcherPrefs.appDrawerFrameShape
    var appDrawerFrameColor by launcherPrefs.appDrawerFrameColor
    var appDrawerFrameOpacity by launcherPrefs.appDrawerFrameOpacity
    var appDrawerFrameTextColor by launcherPrefs.appDrawerFrameTextColor
    var appDrawerEdgeSwitchDp by launcherPrefs.appDrawerEdgeSwitchDp
    var appDrawerBackgroundMode by launcherPrefs.appDrawerBackgroundMode
    var appDrawerBackgroundOpacity by launcherPrefs.appDrawerBackgroundOpacity
    var appDrawerBackgroundColorA by launcherPrefs.appDrawerBackgroundColorA
    var appDrawerBackgroundColorB by launcherPrefs.appDrawerBackgroundColorB
    var appDrawerBackgroundTextColor by launcherPrefs.appDrawerBackgroundTextColor
    var appDrawerBackgroundImageUri by launcherPrefs.appDrawerBackgroundImageUri
    var widgetPickerMode by launcherPrefs.widgetPickerMode
    var swipeDownAction by launcherPrefs.swipeDownAction
    var swipeUpAction by launcherPrefs.swipeUpAction
    var doubleTapAction by launcherPrefs.doubleTapAction
    var pinchAction by launcherPrefs.pinchAction
    var multiSwipeActions by launcherPrefs.multiSwipeActions
    var widgetEditBarPosition by launcherPrefs.widgetEditBarPosition
    var appSortMode by launcherPrefs.appSortMode
    var sideDockPosition by launcherPrefs.sideDockPosition
    var sideDockOpacity by launcherPrefs.sideDockOpacity
    var sideDockTextColor by launcherPrefs.sideDockTextColor
    var bottomDockEntries by launcherPrefs.bottomDockEntries
    var bottomDockPageCount by launcherPrefs.bottomDockPageCount
    var bottomDockLoop by launcherPrefs.bottomDockLoop
    var bottomDockIndicatorToggle by launcherPrefs.bottomDockIndicatorToggle
    var bottomDockAddButtonEnabled by launcherPrefs.bottomDockAddButtonEnabled
    var bottomDockVisible by uiState.bottomDockVisible
    var bottomDockAlignment by launcherPrefs.bottomDockAlignment
    var bottomDockSelectedPage by launcherPrefs.bottomDockSelectedPage
    var bottomDockBackgroundColor by launcherPrefs.bottomDockBackgroundColor
    var bottomDockBackgroundOpacity by launcherPrefs.bottomDockBackgroundOpacity
    var bottomDockTextColor by launcherPrefs.bottomDockTextColor
    var bottomDockShelfHeight by launcherPrefs.bottomDockShelfHeight
    var bottomDockIconVerticalOffset by launcherPrefs.bottomDockIconVerticalOffset
    var bottomDockShelfVerticalOffset by launcherPrefs.bottomDockShelfVerticalOffset
    var bottomDockPageIndicatorVisible by launcherPrefs.bottomDockPageIndicatorVisible
    var bottomDockPageIndicatorGap by launcherPrefs.bottomDockPageIndicatorGap
    var bottomDockLabelsVisible by launcherPrefs.bottomDockLabelsVisible
    var bottomDockShelfShape by launcherPrefs.bottomDockShelfShape
    var bottomDockShelfColor by launcherPrefs.bottomDockShelfColor
    var bottomDockShelfOpacity by launcherPrefs.bottomDockShelfOpacity
    var bottomDockFolderCreationDelay by launcherPrefs.bottomDockFolderCreationDelay
    var bottomDockCapacity by launcherPrefs.bottomDockCapacity
    var defaultWorkspacePage by launcherPrefs.defaultWorkspacePage
    var sideDockEntries by launcherPrefs.sideDockEntries
    var appDrawerCustomOrder by launcherPrefs.appDrawerCustomOrder
    var appDrawerReorderHold by launcherPrefs.appDrawerReorderHold
    var folderFanSpeed by launcherPrefs.folderFanSpeed
    var folderFanDecay by launcherPrefs.folderFanDecay
    var folderFanCloseMode by launcherPrefs.folderFanCloseMode
    var folderFanIconSize by launcherPrefs.folderFanIconSize
    var folderFanSpacing by launcherPrefs.folderFanSpacing
    var folderReorderLineColor by launcherPrefs.folderReorderLineColor
    var folderReorderLineThickness by launcherPrefs.folderReorderLineThickness
    var wallpaperMode by launcherPrefs.wallpaperMode
    var wallpaperScrolling by launcherPrefs.wallpaperScrolling
    var wallpaperDynamic by launcherPrefs.wallpaperDynamic
    var wallpaperBackground by launcherPrefs.wallpaperBackground
    var wallpaperCircleA by launcherPrefs.wallpaperCircleA
    var wallpaperCircleB by launcherPrefs.wallpaperCircleB
    var wallpaperCircleCount by launcherPrefs.wallpaperCircleCount
    var textShadowSettings by launcherPrefs.textShadowSettings
    var environmentEffects by launcherPrefs.environmentEffects
    val activeEnvironmentEffects = remember(environmentEffects, safeMode) {
        if (safeMode) {
            environmentEffects.copy(
                effects = environmentEffects.effects.mapValues { (_, value) ->
                    value.copy(enabled = false)
                }
            )
        } else {
            environmentEffects
        }
    }
    val fishTapEvents = remember { MutableSharedFlow<Offset>(extraBufferCapacity = 1) }
    var launcherInterfaceStyle by launcherPrefs.launcherInterfaceStyle
    val launcherDialogStyleBinding = rememberLauncherDialogStyleBinding()
    val launcherDialogStyle = launcherDialogStyleBinding.value
    var shadeSwipeInProgress by uiState.shadeSwipeInProgress
    // Preserve the selected workspace if Android must recreate the activity for a
    // configuration change we do not handle ourselves (for example, density).
    var lastWorkspaceLogicalPage by rememberSaveable { mutableIntStateOf(defaultWorkspacePage) }

    var appDrawerBackgroundImage by uiState.appDrawerBackgroundImage
    val createSettingsBackup = rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) runCatching {
            LauncherSettingsBackup.write(context.applicationContext, uri)
        }.onSuccess {
            Toast.makeText(context, l10n(R.string.ui_done), Toast.LENGTH_SHORT).show()
        }.onFailure {
            Toast.makeText(context, it.message ?: l10n(R.string.ui_unknown_error), Toast.LENGTH_LONG).show()
        }
    }
    val restoreSettingsBackup = rememberLauncherForActivityResult(OpenDocument()) { uri ->
        if (uri != null) runCatching {
            LauncherSettingsBackup.restore(context.applicationContext, uri)
        }.onSuccess {
            activity.recreate()
        }.onFailure {
            Toast.makeText(context, it.message ?: l10n(R.string.ui_unknown_error), Toast.LENGTH_LONG).show()
        }
    }
    val drawerImagePicker = rememberLauncherForActivityResult(OpenDocument()) { uri ->
        if (uri != null) {
            runCatching {
                context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            appDrawerBackgroundImageUri = uri.toString()
            LauncherStorage.saveAppDrawerBackgroundImageUri(context.applicationContext, uri.toString())
            appDrawerBackgroundMode = AppDrawerBackgroundMode.IMAGE
            LauncherStorage.saveAppDrawerBackgroundMode(context.applicationContext, AppDrawerBackgroundMode.IMAGE)
        }
    }
    val newAppEmblemImagePicker = rememberLauncherForActivityResult(OpenDocument()) { uri ->
        if (uri != null) {
            runCatching {
                context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            newAppEmblemImageUri = uri.toString()
            LauncherStorage.saveNewAppEmblemImageUri(context.applicationContext, uri.toString())
            LauncherStorage.saveNewAppEmblemPackIcon(context.applicationContext, null, null)
        }
    }
    val interfaceImagePicker = rememberLauncherForActivityResult(OpenDocument()) { uri ->
        if (uri != null) {
            runCatching {
                context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            launcherInterfaceStyle = launcherInterfaceStyle.copy(
                backgroundMode = LauncherInterfaceBackgroundMode.IMAGE,
                imageUri = uri.toString(),
                opacityPercent = launcherInterfaceStyle.opacityPercent.coerceAtLeast(35)
            )
            LauncherStorage.saveLauncherInterfaceStyle(context.applicationContext, launcherInterfaceStyle)
        }
    }
    val customAppIconPicker = rememberLauncherForActivityResult(OpenDocument()) { uri ->
        val component = pendingCustomIconComponent
        pendingCustomIconComponent = null
        if (uri != null && component != null) {
            runCatching {
                context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            val old = appCustomizations[component] ?: AppCustomization()
            appCustomizations = appCustomizations + (component to old.copy(
                iconUri = uri.toString(),
                iconPackPackage = null,
                iconPackDrawable = null
            ))
            LauncherStorage.saveAppCustomizations(context.applicationContext, appCustomizations)
            reloadKey += 1
        }
    }

    val shortcutIconPicker = rememberLauncherForActivityResult(OpenDocument()) { uri ->
        val shortcutId = pendingShortcutIconId
        pendingShortcutIconId = null
        if (uri != null && shortcutId != null) {
            runCatching { context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION) }
            shortcuts = shortcuts.map { shortcut ->
                if (shortcut.id == shortcutId) shortcut.copy(
                    customIconUri = uri.toString(),
                    customIconPackPackage = null,
                    customIconPackDrawable = null
                ) else shortcut
            }
            LauncherStorage.saveShortcuts(context.applicationContext, shortcuts)
            editingShortcut = shortcuts.firstOrNull { it.id == shortcutId }
        }
    }

    val folderIconPicker = rememberLauncherForActivityResult(OpenDocument()) { uri ->
        val folderId = pendingFolderIconId
        pendingFolderIconId = null
        if (uri != null && folderId != null) {
            runCatching { context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION) }
            folders = folders.map { folder ->
                if (folder.id == folderId) folder.copy(
                    customIconUri = uri.toString(),
                    customIconPackPackage = null,
                    customIconPackDrawable = null
                ) else folder
            }
            LauncherStorage.saveFolders(context.applicationContext, folders)
            folderDialogTarget = folders.firstOrNull { it.id == folderId }
            bottomDockEntries = bottomDockEntries.map { entry ->
                if (entry.id == folderId && entry.isFolder) entry.copy(
                    customIconUri = uri.toString(),
                    customIconPackPackage = null,
                    customIconPackDrawable = null
                ) else entry
            }
            LauncherStorage.saveBottomDockEntries(context.applicationContext, bottomDockEntries)
            bottomDockFolderDialogTarget = bottomDockEntries.firstOrNull { it.id == folderId && it.isFolder }
        }
    }

    LaunchedEffect(appDrawerBackgroundImageUri) {
        appDrawerBackgroundImage = withContext(Dispatchers.IO) {
            appDrawerBackgroundImageUri?.let { raw ->
                runCatching {
                    decodeLauncherBackground(context, raw).asImageBitmap()
                }.getOrNull()
            }
        }
    }

    val workspaceVirtualCount = virtualPageCount(workspaceCount, workspaceLoop)
    val pagerState = key(workspaceCount, workspaceLoop) {
        rememberPagerState(
            initialPage = initialVirtualPage(
                realPageCount = workspaceCount,
                logicalPage = lastWorkspaceLogicalPage,
                loopEnabled = workspaceLoop
            ),
            pageCount = { workspaceVirtualCount }
        )
    }
    val currentWorkspacePage = logicalPage(pagerState.currentPage, workspaceCount)
    val currentWidgets by rememberUpdatedState(widgets)
    val currentShortcuts by rememberUpdatedState(shortcuts)
    val currentFolders by rememberUpdatedState(folders)
    val currentNotes by rememberUpdatedState(notes)
    val currentInsertionAnchor by rememberUpdatedState(workspaceInsertionAnchor)

    LaunchedEffect(
        wallpaperMode,
        wallpaperScrolling,
        workspaceCount,
        pagerState,
        launcherView
    ) {
        if (wallpaperMode != LauncherWallpaperMode.SYSTEM || !wallpaperScrolling) {
            return@LaunchedEffect
        }
        val wallpaperManager = WallpaperManager.getInstance(context.applicationContext)
        val horizontalStep = if (workspaceCount > 1) 1f / (workspaceCount - 1) else 1f
        runCatching { wallpaperManager.setWallpaperOffsetSteps(horizontalStep, 1f) }
        snapshotFlow { pagerState.currentPage to pagerState.currentPageOffsetFraction }
            .collect { (virtualPage, pageFraction) ->
                val token = launcherView.windowToken ?: return@collect
                val basePage = logicalPage(virtualPage, workspaceCount)
                val targetPage = when {
                    pageFraction > 0f -> (basePage + 1) % workspaceCount
                    pageFraction < 0f -> Math.floorMod(basePage - 1, workspaceCount)
                    else -> basePage
                }
                val baseOffset = if (workspaceCount > 1) {
                    basePage / (workspaceCount - 1f)
                } else 0.5f
                val targetOffset = if (workspaceCount > 1) {
                    targetPage / (workspaceCount - 1f)
                } else 0.5f
                val progress = abs(pageFraction).coerceIn(0f, 1f)
                val horizontalOffset = baseOffset + (targetOffset - baseOffset) * progress
                runCatching {
                    wallpaperManager.setWallpaperOffsets(
                        token,
                        horizontalOffset.coerceIn(0f, 1f),
                        0.5f
                    )
                }
            }
    }

    DisposableEffect(activity, workspaceCount, workspaceLoop, defaultWorkspacePage, pagerState) {
        activity.onWidgetAdded = { appWidgetId, info, requestedPage, replacementId ->
            val density = context.resources.displayMetrics.density.coerceAtLeast(1f)
            val screenWidthDp = context.resources.configuration.screenWidthDp.coerceAtLeast(120)
            val screenHeightDp = context.resources.configuration.screenHeightDp.coerceAtLeast(120)
            // AppWidgetProviderInfo dimensions are pixels. Preserve the provider's proposed
            // size instead of treating it as dp and accidentally enlarging it on dense screens.
            val providerWidthDp = info.minWidth.takeIf { it > 0 }
                ?.let { (it / density).roundToInt() }
                ?.coerceIn(48, screenWidthDp) ?: 180
            val providerHeightDp = info.minHeight.takeIf { it > 0 }
                ?.let { (it / density).roundToInt() }
                ?.coerceIn(48, screenHeightDp) ?: 120
            val replaced = replacementId?.let { id -> currentWidgets.firstOrNull { it.id == id } }
            val targetWidthDp = replaced?.widthDp ?: providerWidthDp
            val targetHeightDp = replaced?.heightDp ?: providerHeightDp
            runCatching {
                activity.launcherAppWidgetManager.updateAppWidgetOptions(
                    appWidgetId,
                    Bundle().apply {
                        putInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, targetWidthDp)
                        putInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH, targetWidthDp)
                        putInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, targetHeightDp)
                        putInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT, targetHeightDp)
                    }
                )
            }
            val targetPage = (replaced?.page ?: requestedPage).coerceIn(0, workspaceCount - 1)
            val insertion = currentInsertionAnchor?.takeIf { it.first == targetPage }
            val freePosition = if (replaced != null) {
                replaced.xFraction to replaced.yFraction
            } else {
                vacantWorkspacePosition(
                    page = targetPage,
                    preferredX = insertion?.second ?: 0.5f,
                    preferredY = insertion?.third ?: 0.42f,
                    shortcuts = currentShortcuts,
                    folders = currentFolders,
                    widgets = currentWidgets,
                    screenWidthDp = screenWidthDp,
                    screenHeightDp = screenHeightDp,
                    notes = currentNotes,
                    elementWidthDp = targetWidthDp,
                    elementHeightDp = targetHeightDp
                )
            }
            val widget = WorkspaceWidget(
                id = replaced?.id ?: UUID.randomUUID().toString(),
                appWidgetId = appWidgetId,
                provider = info.provider.flattenToString(),
                page = targetPage,
                xFraction = freePosition.first,
                yFraction = freePosition.second,
                widthDp = targetWidthDp,
                heightDp = targetHeightDp
            )
            val updatedWidgets = if (replaced == null) currentWidgets + widget else {
                activity.deleteHostedWidget(replaced.appWidgetId)
                currentWidgets.map { if (it.id == replaced.id) widget else it }
            }
            widgets = updatedWidgets
            workspaceInsertionAnchor = null
            LauncherStorage.saveWidgets(context.applicationContext, updatedWidgets)
            LauncherSettingsBackup.completePendingWidgetRestore(context.applicationContext, widget.id)
            activity.cleanupOrphanedHostedWidgets(updatedWidgets.map { it.appWidgetId }.toSet())
            Toast.makeText(context, l10n(R.string.ui_widget_added_to_the_desktop), Toast.LENGTH_SHORT).show()
        }
        activity.onLauncherResumed = { reloadKey += 1 }
        activity.onHomeInvoked = {
            folderFanDisplay = null
            sideDockOpen = false
            sideDockDeleteMode = false
            showSideDockAddDialog = false
            showSideDockSettingsPicker = false
            showSideDockLauncherActionPicker = false
            showBottomDockAddDialog = false
            showBottomDockSettingsPicker = false
            showBottomDockLauncherActionPicker = false
            showWorkspaceOverview = false
            bottomDockFolderDialogTarget = null
            bottomDockFolderFanDisplay = null
            sideDockFolderFanDisplay = null
            liveWorkspaceEffectOriginal?.let { pageTransitionEffect = it }
            liveAppDrawerEffectOriginal?.let { appDrawerPageTransitionEffect = it }
            liveWorkspaceEffectOriginal = null
            liveAppDrawerEffectOriginal = null
            launcherActionWorkspacePage = null
            widgetEditingId = null
            drawerOpen = false
            appPickerMode = false
            sideDockAppPickerMode = false
            bottomDockAppPickerMode = false
            appDrawerSearchVisible = false
            appDrawerDragState = null
            editingApp = null
            editingShortcut = null
            shortcutIconPackPickerId = null
            folderIconPackPickerId = null
            drawerVisuallyHidden = false
            query = ""
            val target = if (workspaceLoop) {
                nearestVirtualPage(pagerState.currentPage, defaultWorkspacePage, workspaceCount)
            } else defaultWorkspacePage
            scope.launch { pagerState.animateScrollToPage(target.coerceIn(0, workspaceVirtualCount - 1)) }
        }
        onDispose {
            activity.onWidgetAdded = null
            activity.onHomeInvoked = null
            activity.onLauncherResumed = null
        }
    }

    LaunchedEffect(widgets, reloadKey, safeMode) {
        if (safeMode || activity.hasPendingWidgetRequest()) return@LaunchedEffect
        delay(300L)

        val pendingIds = LauncherSettingsBackup.pendingAutomaticWidgetRestoreIds(
            context.applicationContext
        )
        if (pendingIds.isEmpty()) return@LaunchedEffect

        val manager = activity.launcherAppWidgetManager
        // A same-install backup restore can occasionally leave a binding valid.
        // Remove such records instead of allocating an unnecessary second widget ID.
        widgets.filter { it.id in pendingIds }.forEach { widget ->
            val info = runCatching { manager.getAppWidgetInfo(widget.appWidgetId) }.getOrNull()
            if (info != null && (
                    widget.provider.isBlank() || info.provider.flattenToString() == widget.provider
                )
            ) {
                LauncherSettingsBackup.completePendingWidgetRestore(
                    context.applicationContext,
                    widget.id
                )
            }
        }

        val remainingIds = LauncherSettingsBackup.pendingAutomaticWidgetRestoreIds(
            context.applicationContext
        )
        val widget = widgets.firstOrNull { candidate ->
            candidate.id in remainingIds && run {
                val info = runCatching { manager.getAppWidgetInfo(candidate.appWidgetId) }.getOrNull()
                info == null || (
                    candidate.provider.isNotBlank() &&
                        info.provider.flattenToString() != candidate.provider
                )
            }
        } ?: return@LaunchedEffect

        val storedProvider = ComponentName.unflattenFromString(widget.provider)
            ?: return@LaunchedEffect
        val installed = runCatching { manager.installedProviders }.getOrDefault(emptyList())
        val exactProvider = installed.firstOrNull { it.provider == storedProvider }?.provider
        val samePackageProviders = installed
            .map { it.provider }
            .filter { it.packageName == storedProvider.packageName }
            .distinct()
        // If a provider class was renamed, only guess when the package exposes a
        // single widget provider.  With multiple providers a manual Restore remains
        // safer than silently binding a different widget.
        val provider = exactProvider ?: samePackageProviders.singleOrNull()
            ?: return@LaunchedEffect

        LauncherSettingsBackup.markWidgetRestoreAutoAttempted(
            context.applicationContext,
            widget.id
        )
        activity.requestBindWidget(provider, widget.page, widget.id)
    }

    // A HOME activity must consume Back even when no panel is open. Otherwise
    // Android finishes it and immediately creates another HOME instance, which
    // looks like a launcher reload and unnecessarily rebuilds every widget host.
    BackHandler(enabled = true) {
        when {
            liveWorkspaceEffectOriginal != null -> {
                pageTransitionEffect = liveWorkspaceEffectOriginal ?: pageTransitionEffect
                liveWorkspaceEffectOriginal = null
            }
            liveAppDrawerEffectOriginal != null -> {
                appDrawerPageTransitionEffect = liveAppDrawerEffectOriginal ?: appDrawerPageTransitionEffect
                liveAppDrawerEffectOriginal = null
                drawerOpen = false
            }
            bottomDockFolderFanDisplay != null -> bottomDockFolderFanDisplay = null
            sideDockFolderFanDisplay != null -> sideDockFolderFanDisplay = null
            showWorkspaceOverview -> showWorkspaceOverview = false
            appDrawerDragState != null -> { appDrawerDragState = null; drawerVisuallyHidden = false }
            editingApp != null -> editingApp = null
            editingShortcut != null -> editingShortcut = null
            shortcutIconPackPickerId != null -> shortcutIconPackPickerId = null
            folderIconPackPickerId != null -> folderIconPackPickerId = null
            pendingUninstallApp != null -> pendingUninstallApp = null
            folderFanDisplay != null -> folderFanDisplay = null
            showBottomDockLauncherActionPicker -> showBottomDockLauncherActionPicker = false
            showBottomDockSettingsPicker -> showBottomDockSettingsPicker = false
            showBottomDockAddDialog -> showBottomDockAddDialog = false
            showSideDockLauncherActionPicker -> showSideDockLauncherActionPicker = false
            showSideDockSettingsPicker -> showSideDockSettingsPicker = false
            showSideDockAddDialog -> showSideDockAddDialog = false
            launcherActionWorkspacePage != null -> launcherActionWorkspacePage = null
            sideDockMenuTarget != null -> sideDockMenuTarget = null
            sideDockOpen -> {
                sideDockOpen = false
                sideDockDeleteMode = false
            }
            showSortDialog -> showSortDialog = false
            bottomDockFolderDialogTarget != null -> bottomDockFolderDialogTarget = null
            folderDialogTarget != null -> folderDialogTarget = null
            widgetEditingId != null -> widgetEditingId = null
            widgetPickerPage != null -> widgetPickerPage = null
            settingsShortcutPage != null -> settingsShortcutPage = null
            addToWorkspacePage != null -> addToWorkspacePage = null
            workspaceMenuPage != null -> workspaceMenuPage = null
            showAccessibilityPrompt -> showAccessibilityPrompt = false
            shortcutMenuTarget != null -> shortcutMenuTarget = null
            showWorkspaceSettings -> {
                appDrawerEdgePreviewVisible = false
                showWorkspaceSettings = false
            }
            pendingPlacement != null -> pendingPlacement = null
            drawerOpen -> {
                drawerOpen = false
                appPickerMode = false
                sideDockAppPickerMode = false
                bottomDockAppPickerMode = false
                    appDrawerSearchVisible = false
                appDrawerDragState = null
                drawerVisuallyHidden = false
                query = ""
            }
        }
    }

    fun removePackageReferences(packageName: String) {
        val updatedShortcuts = shortcuts.filterNot { shortcut ->
            shortcut.intentAction == null && componentBelongsToPackage(shortcut.componentName, packageName)
        }
        if (updatedShortcuts != shortcuts) {
            shortcuts = updatedShortcuts
            LauncherStorage.saveShortcuts(context.applicationContext, updatedShortcuts)
        }

        val updatedFolders = folders.map { folder ->
            folder.copy(componentNames = folder.componentNames.filterNot {
                componentBelongsToPackage(it, packageName)
            })
        }
        if (updatedFolders != folders) {
            folders = updatedFolders
            LauncherStorage.saveFolders(context.applicationContext, updatedFolders)
            folderDialogTarget = folderDialogTarget?.let { current ->
                updatedFolders.firstOrNull { it.id == current.id }
            }
        }

        val updatedSideDock = sideDockEntries.mapNotNull { entry ->
            if (entry.isFolder) {
                entry.copy(componentNames = entry.componentNames.filterNot {
                    componentBelongsToPackage(it, packageName)
                })
            } else if (entry.intentAction == null && componentBelongsToPackage(entry.componentName, packageName)) {
                null
            } else entry
        }
        if (updatedSideDock != sideDockEntries) {
            sideDockEntries = updatedSideDock
            LauncherStorage.saveSideDockEntries(context.applicationContext, updatedSideDock)
            sideDockFolderDialogTarget = sideDockFolderDialogTarget?.let { current ->
                updatedSideDock.firstOrNull { it.id == current.id && it.isFolder }
            }
        }

        val updatedBottomDock = bottomDockEntries.mapNotNull { entry ->
            if (entry.isFolder) {
                entry.copy(componentNames = entry.componentNames.filterNot {
                    componentBelongsToPackage(it, packageName)
                })
            } else if (entry.intentAction == null && componentBelongsToPackage(entry.componentName, packageName)) {
                null
            } else entry
        }
        if (updatedBottomDock != bottomDockEntries) {
            bottomDockEntries = updatedBottomDock
            LauncherStorage.saveBottomDockEntries(context.applicationContext, updatedBottomDock)
            bottomDockFolderDialogTarget = bottomDockFolderDialogTarget?.let { current ->
                updatedBottomDock.firstOrNull { it.id == current.id && it.isFolder }
            }
        }

        if (editingApp?.packageName == packageName) editingApp = null
        if (pendingUninstallApp?.packageName == packageName) pendingUninstallApp = null
        if (pendingPlacement?.packageName == packageName) pendingPlacement = null
    }

    DisposableEffect(context.applicationContext, keepUninstalledAppShortcuts) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(receiverContext: Context?, intent: Intent?) {
                val action = intent?.action
                val packageName = intent?.data?.schemeSpecificPart
                val replacing = intent?.getBooleanExtra(Intent.EXTRA_REPLACING, false) == true

                if (action == Intent.ACTION_PACKAGE_REMOVED &&
                    !replacing &&
                    !keepUninstalledAppShortcuts &&
                    !packageName.isNullOrBlank()
                ) {
                    removePackageReferences(packageName)
                }

                reloadKey += 1

                // During an in-place update LauncherApps can briefly expose the old
                // list without the replaced package. Refresh again after Android has
                // finished publishing the new launcher activity/alias.
                if (action == Intent.ACTION_PACKAGE_ADDED ||
                    action == Intent.ACTION_PACKAGE_REPLACED ||
                    action == Intent.ACTION_PACKAGE_CHANGED
                ) {
                    scope.launch {
                        delay(250L)
                        reloadKey += 1
                        delay(750L)
                        reloadKey += 1
                    }
                }
            }
        }
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_PACKAGE_ADDED)
            addAction(Intent.ACTION_PACKAGE_REMOVED)
            addAction(Intent.ACTION_PACKAGE_CHANGED)
            addAction(Intent.ACTION_PACKAGE_REPLACED)
            addDataScheme("package")
        }
        context.applicationContext.registerReceiver(receiver, filter)
        onDispose { runCatching { context.applicationContext.unregisterReceiver(receiver) } }
    }

    DisposableEffect(repository) {
        fun refreshAgainAfterPackageSettles() {
            scope.launch {
                delay(350L)
                reloadKey += 1
            }
        }
        val callback = object : LauncherApps.Callback() {
            override fun onPackageRemoved(packageName: String, user: UserHandle) { reloadKey += 1 }
            override fun onPackageAdded(packageName: String, user: UserHandle) {
                reloadKey += 1
                refreshAgainAfterPackageSettles()
            }
            override fun onPackageChanged(packageName: String, user: UserHandle) {
                reloadKey += 1
                refreshAgainAfterPackageSettles()
            }
            override fun onPackagesAvailable(packageNames: Array<out String>, user: UserHandle, replacing: Boolean) {
                reloadKey += 1
                if (replacing) refreshAgainAfterPackageSettles()
            }
            override fun onPackagesUnavailable(packageNames: Array<out String>, user: UserHandle, replacing: Boolean) { reloadKey += 1 }
        }
        runCatching { repository.registerCallback(callback) }
        onDispose { runCatching { repository.unregisterCallback(callback) } }
    }

    LaunchedEffect(reloadKey, appCustomizations) {
        isLoading = true
        loadError = null
        var loadedApps: List<AppItem>? = null
        var lastFailure: Throwable? = null
        for (attempt in 0 until 10) {
            val result = runCatching {
                withContext(Dispatchers.Default) { repository.loadApps(appCustomizations) }
            }
            val candidate = result.getOrNull()
            if (!candidate.isNullOrEmpty()) {
                loadedApps = candidate
                break
            }
            lastFailure = result.exceptionOrNull()
            if (attempt < 9) delay(200L)
        }
        val successful = loadedApps
        if (successful != null) {
            apps = successful

            val currentByComponent = successful.associateBy { it.componentName.flattenToString() }
            val currentByPackage = successful.groupBy { it.packageName }
            fun repair(component: String): String = repairedComponentKey(
                storedComponent = component,
                appsByComponent = currentByComponent,
                appsByPackage = currentByPackage,
                packageManager = context.packageManager
            )
            // Keep persisted launcher locations stable across package replacement.
            // If an update changes the launcher Activity/activity-alias, reconnect
            // old references to the current launcher component of the same package.
            val repairedShortcuts = shortcuts.map { shortcut ->
                if (shortcut.intentAction != null) return@map shortcut
                val component = repair(shortcut.componentName)
                if (component == shortcut.componentName) shortcut else shortcut.copy(componentName = component)
            }
            if (repairedShortcuts != shortcuts) {
                shortcuts = repairedShortcuts
                LauncherStorage.saveShortcuts(context.applicationContext, repairedShortcuts)
            }

            val repairedFolders = folders.map { folder ->
                val components = folder.componentNames.map(::repair)
                if (components == folder.componentNames) folder else folder.copy(componentNames = components)
            }
            if (repairedFolders != folders) {
                folders = repairedFolders
                LauncherStorage.saveFolders(context.applicationContext, repairedFolders)
                folderDialogTarget = folderDialogTarget?.let { current ->
                    repairedFolders.firstOrNull { it.id == current.id }
                }
            }

            val repairedSideDock = sideDockEntries.map { entry ->
                if (entry.isFolder) {
                    val components = entry.componentNames.map(::repair)
                    if (components == entry.componentNames) entry else entry.copy(componentNames = components)
                } else {
                    val original = entry.componentName ?: return@map entry
                    val component = repair(original)
                    if (component == original) entry else entry.copy(componentName = component)
                }
            }
            if (repairedSideDock != sideDockEntries) {
                sideDockEntries = repairedSideDock
                LauncherStorage.saveSideDockEntries(context.applicationContext, repairedSideDock)
                sideDockFolderDialogTarget = sideDockFolderDialogTarget?.let { current ->
                    repairedSideDock.firstOrNull { it.id == current.id && it.isFolder }
                }
            }

            val repairedBottomDock = bottomDockEntries.map { entry ->
                if (entry.isFolder) {
                    val components = entry.componentNames.map(::repair)
                    if (components == entry.componentNames) entry else entry.copy(componentNames = components)
                } else {
                    val original = entry.componentName ?: return@map entry
                    val component = repair(original)
                    if (component == original) entry else entry.copy(componentName = component)
                }
            }
            if (repairedBottomDock != bottomDockEntries) {
                bottomDockEntries = repairedBottomDock
                LauncherStorage.saveBottomDockEntries(context.applicationContext, repairedBottomDock)
                bottomDockFolderDialogTarget = bottomDockFolderDialogTarget?.let { current ->
                    repairedBottomDock.firstOrNull { it.id == current.id && it.isFolder }
                }
            }

            val repairedHiddenApps = hiddenApps.map(::repair).toSet()
            if (repairedHiddenApps != hiddenApps) {
                hiddenApps = repairedHiddenApps
                LauncherStorage.saveHiddenApps(context.applicationContext, repairedHiddenApps)
            }

            val repairedOrder = appDrawerCustomOrder.map(::repair).distinct()
            if (repairedOrder != appDrawerCustomOrder) {
                appDrawerCustomOrder = repairedOrder
                LauncherStorage.saveAppDrawerCustomOrder(context.applicationContext, repairedOrder)
            }

            val repairedCustomizations = buildMap {
                appCustomizations.forEach { (component, customization) ->
                    put(repair(component), customization)
                }
            }
            if (repairedCustomizations != appCustomizations) {
                appCustomizations = repairedCustomizations
                LauncherStorage.saveAppCustomizations(context.applicationContext, repairedCustomizations)
            }
        } else {
            apps = emptyList()
            loadError = lastFailure?.message?.takeIf { it.isNotBlank() }
                ?: l10n(R.string.ui_the_system_returned_no_available_applications_total_found, 0)
        }
        isLoading = false
    }

    LaunchedEffect(pagerState.currentPage, workspaceCount) {
        lastWorkspaceLogicalPage = logicalPage(pagerState.currentPage, workspaceCount)
    }

    LaunchedEffect(currentWorkspacePage) {
        if (folderFanDisplay?.page != currentWorkspacePage) folderFanDisplay = null
        sideDockDeleteMode = false
        if (widgetEditingId != null && widgets.none { it.id == widgetEditingId && it.page == currentWorkspacePage }) {
            widgetEditingId = null
        }
    }

    LaunchedEffect(sideDockOpen) {
        if (!sideDockOpen) sideDockDeleteMode = false
    }

    LaunchedEffect(pagerState.isScrollInProgress, workspaceLoop, workspaceCount) {
        if (!pagerState.isScrollInProgress && workspaceLoop && workspaceCount > 1) {
            val edge = workspaceCount * 3
            if (pagerState.currentPage < edge || pagerState.currentPage > workspaceVirtualCount - edge) {
                pagerState.scrollToPage(
                    initialVirtualPage(workspaceCount, currentWorkspacePage, true)
                )
            }
        }
    }

    val visibleApps = remember(apps, hiddenApps) {
        apps.filterNot { it.componentName.flattenToString() in hiddenApps }
    }
    val sortedApps = remember(visibleApps, appSortMode, appDrawerCustomOrder) {
        when (appSortMode) {
            AppSortMode.NAME -> visibleApps.sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.label })
            AppSortMode.INSTALL_DATE -> visibleApps.sortedWith(
                compareByDescending<AppItem> { it.installTimeMillis }
                    .thenBy(String.CASE_INSENSITIVE_ORDER) { it.label }
            )
            AppSortMode.LAST_USED -> visibleApps.sortedWith(
                compareByDescending<AppItem> { it.lastUsedMillis }
                    .thenBy(String.CASE_INSENSITIVE_ORDER) { it.label }
            )
            AppSortMode.CUSTOM -> {
                val order = appDrawerCustomOrder.withIndex().associate { it.value to it.index }
                visibleApps.sortedWith(compareBy<AppItem> { order[it.componentName.flattenToString()] ?: Int.MAX_VALUE }
                    .thenBy(String.CASE_INSENSITIVE_ORDER) { it.label })
            }
        }
    }
    val filteredApps = remember(sortedApps, query) {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) sortedApps else sortedApps.filter {
            it.label.contains(trimmed, ignoreCase = true) ||
                it.packageName.contains(trimmed, ignoreCase = true)
        }
    }
    val appsByComponent = remember(apps, context.applicationContext) {
        buildMap {
            putAll(apps.associateBy { it.componentName.flattenToString() })
            launcherActionOptions.forEach { option ->
                put(option.action, AppItem(
                    label = option.title,
                    packageName = context.packageName,
                    componentName = ComponentName(context.packageName, MainActivity::class.java.name),
                    user = android.os.Process.myUserHandle(),
                    icon = runCatching {
                        context.getDrawable(launcherActionIconRes(option.action))?.toBitmap(96, 96)?.asImageBitmap()
                    }.getOrNull()
                ))
            }
        }
    }
    val workspaceOverviewItems = remember(
        shortcuts,
        widgets,
        notes,
        folders,
        appsByComponent,
        configuration.screenWidthDp,
        configuration.screenHeightDp
    ) {
        val screenWidth = configuration.screenWidthDp.coerceAtLeast(1).toFloat()
        val screenHeight = (configuration.screenHeightDp - 150).coerceAtLeast(1).toFloat()
        buildList {
            shortcuts.forEach { shortcut ->
                add(
                    WorkspaceOverviewItem(
                        id = shortcut.id,
                        type = WorkspaceOverviewItemType.SHORTCUT,
                        label = shortcut.label,
                        page = shortcut.page,
                        xFraction = shortcut.xFraction,
                        yFraction = shortcut.yFraction,
                        icon = appsByComponent[shortcut.componentName]?.icon
                    )
                )
            }
            folders.forEach { folder ->
                add(
                    WorkspaceOverviewItem(
                        id = folder.id,
                        type = WorkspaceOverviewItemType.FOLDER,
                        label = folder.label,
                        page = folder.page,
                        xFraction = folder.xFraction,
                        yFraction = folder.yFraction
                    )
                )
            }
            notes.forEach { note ->
                add(WorkspaceOverviewItem(
                    id = note.id,
                    type = WorkspaceOverviewItemType.WIDGET,
                    label = note.text.take(48).ifBlank { l10n(R.string.ui_note_title) },
                    page = note.page,
                    xFraction = note.xFraction,
                    yFraction = note.yFraction,
                    widthFraction = (note.widthDp / screenWidth).coerceIn(0.18f, 0.85f),
                    heightFraction = (note.heightDp / screenHeight).coerceIn(0.12f, 0.85f)
                ))
            }
            widgets.forEach { widget ->
                add(
                    WorkspaceOverviewItem(
                        id = widget.id,
                        type = WorkspaceOverviewItemType.WIDGET,
                        label = ComponentName.unflattenFromString(widget.provider)
                            ?.className?.substringAfterLast('.') ?: l10n(R.string.ui_widget),
                        page = widget.page,
                        xFraction = widget.xFraction,
                        yFraction = widget.yFraction,
                        widthFraction = (widget.widthDp / screenWidth).coerceIn(0.18f, 0.70f),
                        heightFraction = (widget.heightDp / screenHeight).coerceIn(0.16f, 0.65f)
                    )
                )
            }
        }
    }

    fun performLauncherAction(action: String): Boolean {
        return when (canonicalLauncherAction(action)) {
            LAUNCHER_ACTION_APP_DRAWER -> {
                pendingPlacement = null
                appPickerMode = false
                sideDockAppPickerMode = false
                bottomDockAppPickerMode = false
                sideDockOpen = false
                drawerOpen = true
                true
            }
            LAUNCHER_ACTION_SIDE_DOCK -> {
                drawerOpen = false
                sideDockOpen = true
                true
            }
            LAUNCHER_ACTION_SETTINGS -> {
                settingsStartPage = LauncherSettingsPage.ROOT
                showWorkspaceSettings = true
                true
            }
            LAUNCHER_ACTION_ENVIRONMENT_EFFECTS -> {
                settingsStartPage = LauncherSettingsPage.ENVIRONMENT_EFFECTS
                showWorkspaceSettings = true
                true
            }
            LAUNCHER_ACTION_WORKSPACE_EFFECTS -> {
                settingsStartPage = LauncherSettingsPage.WORKSPACE_EFFECTS
                showWorkspaceSettings = true
                true
            }
            LAUNCHER_ACTION_APP_DRAWER_EFFECTS -> {
                settingsStartPage = LauncherSettingsPage.APP_DRAWER_PAGE_EFFECTS
                showWorkspaceSettings = true
                true
            }
            LAUNCHER_ACTION_GESTURES -> {
                settingsStartPage = LauncherSettingsPage.GESTURES
                showWorkspaceSettings = true
                true
            }
            LAUNCHER_ACTION_WALLPAPER -> {
                settingsStartPage = LauncherSettingsPage.WALLPAPER
                showWorkspaceSettings = true
                true
            }
            else -> false
        }
    }

    fun launchActivationDelayMillis(): Long {
        val configuredDelay = if (interactionExtras.linkLaunchDelayToAnimation) {
            appLaunchAnimationDuration
        } else {
            appLaunchDelay
        }
        return if (appLaunchAnimationEffect == AppLaunchAnimationEffect.NONE) 0L
        else configuredDelay.coerceAtLeast(0).toLong()
    }

    fun runAfterLaunchActivation(action: () -> Unit) {
        val delayMs = launchActivationDelayMillis()
        if (delayMs <= 0L) action() else scope.launch {
            delay(delayMs)
            action()
        }
    }

    fun launchAppNow(app: AppItem) {
        val component = app.componentName.flattenToString()
        LauncherStorage.markAppOpened(context.applicationContext, component)
        openedApps = openedApps + component
        runCatching { repository.launch(app) }
            .onFailure {
                Toast.makeText(context, l10n(R.string.ui_failed_to_launch, app.label), Toast.LENGTH_SHORT).show()
            }
    }

    fun performWidgetActionChain(chain: WidgetActionChain) {
        if (chain.actions.isEmpty()) return
        scope.launch {
            chain.actions.forEachIndexed { index, step ->
                when (step.type) {
                    WidgetActionStepType.APP -> {
                        val exact = appsByComponent[step.value]
                        val requestedPackage = componentPackageName(step.value)
                        val app = exact ?: requestedPackage?.let { packageName ->
                            apps.firstOrNull { it.packageName == packageName }
                        }
                        if (app != null) {
                            launchAppNow(app)
                        } else {
                            val component = ComponentName.unflattenFromString(step.value)
                            val launched = component?.let { value ->
                                runCatching { repository.launch(value) }.isSuccess
                            } == true
                            if (!launched && requestedPackage != null) {
                                context.packageManager.getLaunchIntentForPackage(requestedPackage)?.let { intent ->
                                    runCatching {
                                        context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                                    }
                                }
                            }
                        }
                    }
                    WidgetActionStepType.LAUNCHER_ACTION -> performLauncherAction(step.value)
                    WidgetActionStepType.INTENT_ACTION -> {
                        if (isSupportedWidgetIntentAction(step.value)) {
                            runCatching {
                                context.startActivity(
                                    Intent(step.value).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                )
                            }.onFailure {
                                Toast.makeText(
                                    context,
                                    l10n(R.string.ui_this_settings_item_is_unavailable),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        } else {
                            LauncherDiagnostics.warning(
                                "blocked unsupported widget action intent: ${step.value}"
                            )
                        }
                    }
                }
                if (index < chain.actions.lastIndex) delay(180L)
            }
        }
    }

    fun launchApp(app: AppItem) {
        runAfterLaunchActivation { launchAppNow(app) }
    }

    fun launchShortcut(shortcut: WorkspaceShortcut) {
        shortcut.intentAction?.let { action ->
            runAfterLaunchActivation {
                if (performLauncherAction(action)) return@runAfterLaunchActivation
                runCatching {
                    context.startActivity(Intent(action).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                }.onFailure {
                    Toast.makeText(context, l10n(R.string.ui_this_settings_item_is_unavailable), Toast.LENGTH_SHORT).show()
                }
            }
            return
        }

        val installedApp = appsByComponent[shortcut.componentName]
        if (installedApp != null) {
            launchApp(installedApp)
            return
        }

        val component = ComponentName.unflattenFromString(shortcut.componentName)
        if (component == null) {
            Toast.makeText(context, l10n(R.string.ui_application_not_found), Toast.LENGTH_SHORT).show()
            return
        }
        runAfterLaunchActivation {
            runCatching { repository.launch(component) }
                .onFailure {
                    Toast.makeText(context, l10n(R.string.ui_application_not_found), Toast.LENGTH_SHORT).show()
                }
        }
    }

    fun updateWorkspaceCount(newCount: Int) {
        val safeCount = newCount.coerceIn(1, 9)
        workspaceCount = safeCount
        lastWorkspaceLogicalPage = lastWorkspaceLogicalPage.coerceIn(0, safeCount - 1)
        defaultWorkspacePage = defaultWorkspacePage.coerceIn(0, safeCount - 1)
        LauncherStorage.savePageCount(context.applicationContext, safeCount)
        LauncherStorage.saveDefaultWorkspacePage(context.applicationContext, defaultWorkspacePage)

        shortcuts = shortcuts.map { shortcut ->
            if (shortcut.page >= safeCount) shortcut.copy(page = safeCount - 1) else shortcut
        }
        widgets = widgets.map { widget ->
            if (widget.page >= safeCount) widget.copy(page = safeCount - 1) else widget
        }
        notes = notes.map { if (it.page >= safeCount) it.copy(page = safeCount - 1) else it }
        LauncherNotesStore.save(context.applicationContext, notes)
        folders = folders.map { folder ->
            if (folder.page >= safeCount) folder.copy(page = safeCount - 1) else folder
        }
        LauncherStorage.saveShortcuts(context.applicationContext, shortcuts)
        LauncherStorage.saveWidgets(context.applicationContext, widgets)
        LauncherStorage.saveFolders(context.applicationContext, folders)
    }

    fun reorderWorkspacePage(fromPage: Int, toPage: Int) {
        if (workspaceEditingLocked || fromPage == toPage ||
            fromPage !in 0 until workspaceCount || toPage !in 0 until workspaceCount
        ) return
        val order = (0 until workspaceCount).toMutableList()
        val moved = order.removeAt(fromPage)
        order.add(toPage, moved)
        val newPageByOldPage = order.withIndex().associate { (newPage, oldPage) -> oldPage to newPage }
        shortcuts = shortcuts.map { it.copy(page = newPageByOldPage[it.page] ?: it.page) }
        widgets = widgets.map { it.copy(page = newPageByOldPage[it.page] ?: it.page) }
        notes = notes.map { it.copy(page = newPageByOldPage[it.page] ?: it.page) }
        LauncherNotesStore.save(context.applicationContext, notes)
        folders = folders.map { it.copy(page = newPageByOldPage[it.page] ?: it.page) }
        defaultWorkspacePage = newPageByOldPage[defaultWorkspacePage] ?: defaultWorkspacePage
        val reorderedCurrentPage = newPageByOldPage[currentWorkspacePage] ?: currentWorkspacePage
        lastWorkspaceLogicalPage = reorderedCurrentPage
        LauncherStorage.saveShortcuts(context.applicationContext, shortcuts)
        LauncherStorage.saveWidgets(context.applicationContext, widgets)
        LauncherStorage.saveFolders(context.applicationContext, folders)
        LauncherStorage.saveDefaultWorkspacePage(context.applicationContext, defaultWorkspacePage)
        val target = if (workspaceLoop) {
            nearestVirtualPage(pagerState.currentPage, reorderedCurrentPage, workspaceCount)
        } else reorderedCurrentPage
        scope.launch { pagerState.scrollToPage(target.coerceIn(0, workspaceVirtualCount - 1)) }
    }

    fun addWorkspacePageFromOverview() {
        if (workspaceEditingLocked || workspaceCount >= 9) return
        updateWorkspaceCount(workspaceCount + 1)
    }

    fun deleteWorkspacePage(page: Int, moveContentsTo: Int?) {
        if (workspaceEditingLocked || workspaceCount <= 1 || page !in 0 until workspaceCount) return
        val targetBeforeDelete = moveContentsTo?.takeIf { it in 0 until workspaceCount && it != page }
        val pageAfterDelete: (Int) -> Int = { oldPage ->
            val movedPage = if (oldPage == page) targetBeforeDelete ?: page.coerceAtMost(workspaceCount - 2)
                else oldPage
            if (movedPage > page) movedPage - 1 else movedPage
        }
        shortcuts = shortcuts.map { it.copy(page = pageAfterDelete(it.page)) }
        widgets = widgets.map { it.copy(page = pageAfterDelete(it.page)) }
        notes = notes.map { it.copy(page = pageAfterDelete(it.page)) }
        LauncherNotesStore.save(context.applicationContext, notes)
        folders = folders.map { it.copy(page = pageAfterDelete(it.page)) }
        defaultWorkspacePage = pageAfterDelete(defaultWorkspacePage).coerceIn(0, workspaceCount - 2)
        lastWorkspaceLogicalPage = pageAfterDelete(currentWorkspacePage).coerceIn(0, workspaceCount - 2)
        workspaceCount -= 1
        LauncherStorage.savePageCount(context.applicationContext, workspaceCount)
        LauncherStorage.saveDefaultWorkspacePage(context.applicationContext, defaultWorkspacePage)
        LauncherStorage.saveShortcuts(context.applicationContext, shortcuts)
        LauncherStorage.saveWidgets(context.applicationContext, widgets)
        LauncherStorage.saveFolders(context.applicationContext, folders)
    }

    fun moveOverviewItem(
        item: WorkspaceOverviewItem,
        page: Int,
        xFraction: Float,
        yFraction: Float
    ) {
        if (workspaceEditingLocked) { notifyDesktopEditingLocked(); return }
        val targetPage = page.coerceIn(0, workspaceCount - 1)
        when (item.type) {
            WorkspaceOverviewItemType.SHORTCUT -> {
                shortcuts = shortcuts.map { shortcut ->
                    if (shortcut.id == item.id) shortcut.copy(
                        page = targetPage,
                        xFraction = xFraction.safeWorkspaceFraction(shortcut.xFraction),
                        yFraction = yFraction.safeWorkspaceFraction(shortcut.yFraction)
                    ) else shortcut
                }
                LauncherStorage.saveShortcuts(context.applicationContext, shortcuts)
            }
            WorkspaceOverviewItemType.FOLDER -> {
                folders = folders.map { folder ->
                    if (folder.id == item.id) folder.copy(
                        page = targetPage,
                        xFraction = xFraction.safeWorkspaceFraction(folder.xFraction),
                        yFraction = yFraction.safeWorkspaceFraction(folder.yFraction)
                    ) else folder
                }
                LauncherStorage.saveFolders(context.applicationContext, folders)
            }
            WorkspaceOverviewItemType.WIDGET -> {
                if (notes.any { it.id == item.id }) {
                    notes = notes.map { note ->
                        if (note.id == item.id) note.copy(
                            page = targetPage,
                            xFraction = xFraction.safeWorkspaceFraction(note.xFraction),
                            yFraction = yFraction.safeWorkspaceFraction(note.yFraction)
                        ) else note
                    }
                    LauncherNotesStore.save(context.applicationContext, notes)
                } else {
                    widgets = widgets.map { widget ->
                        if (widget.id == item.id) widget.copy(
                            page = targetPage,
                            xFraction = xFraction.safeWorkspaceFraction(widget.xFraction),
                            yFraction = yFraction.safeWorkspaceFraction(widget.yFraction)
                        ) else widget
                    }
                    LauncherStorage.saveWidgets(context.applicationContext, widgets)
                }
            }
        }
    }

    fun openWorkspacePageFromOverview(page: Int) {
        showWorkspaceOverview = false
        val target = if (workspaceLoop) {
            nearestVirtualPage(pagerState.currentPage, page, workspaceCount)
        } else page
        scope.launch { pagerState.animateScrollToPage(target.coerceIn(0, workspaceVirtualCount - 1)) }
    }

    fun freeWorkspacePosition(page: Int, requestedX: Float = 0.5f, requestedY: Float = 0.42f,
        elementWidthDp: Int = 78, elementHeightDp: Int = 96): Pair<Float, Float> {
        val anchor = workspaceInsertionAnchor?.takeIf { it.first == page }
        return vacantWorkspacePosition(
            page, anchor?.second ?: requestedX, anchor?.third ?: requestedY,
            shortcuts, folders, widgets,
            configuration.screenWidthDp, configuration.screenHeightDp, notes,
            elementWidthDp, elementHeightDp
        )
    }

    fun addShortcut(app: AppItem, page: Int, xFraction: Float, yFraction: Float) {
        if (workspaceEditingLocked) {
            pendingPlacement = null
            notifyDesktopEditingLocked()
            return
        }
        // All callers supply either the user's exact drop point or a position
        // already calculated by freeWorkspacePosition() for automatic adding.
        // Never rerun vacantWorkspacePosition() here: it can shift a manually
        // dropped icon away from a valid spot merely because of nearby items.
        val shortcut = WorkspaceShortcut(
            id = UUID.randomUUID().toString(),
            label = app.label,
            componentName = app.componentName.flattenToString(),
            page = page.coerceIn(0, workspaceCount - 1),
            xFraction = xFraction.safeWorkspaceFraction(),
            yFraction = yFraction.safeWorkspaceFraction()
        )
        shortcuts = shortcuts + shortcut
        LauncherStorage.saveShortcuts(context.applicationContext, shortcuts)
        pendingPlacement = null
        workspaceInsertionAnchor = null
    }

    fun addSettingsShortcut(option: SettingsShortcutOption, page: Int) {
        if (workspaceEditingLocked) { notifyDesktopEditingLocked(); return }
        val position = freeWorkspacePosition(page)
        val shortcut = WorkspaceShortcut(
            id = UUID.randomUUID().toString(),
            label = option.title,
            componentName = "",
            page = page.coerceIn(0, workspaceCount - 1),
            xFraction = position.first,
            yFraction = position.second,
            intentAction = option.action
        )
        shortcuts = shortcuts + shortcut
        LauncherStorage.saveShortcuts(context.applicationContext, shortcuts)
        workspaceInsertionAnchor = null
        Toast.makeText(context, l10n(R.string.ui_shortcut_added, option.title), Toast.LENGTH_SHORT).show()
    }

    fun addLauncherActionShortcut(option: LauncherActionOption, page: Int) {
        if (workspaceEditingLocked) { notifyDesktopEditingLocked(); return }
        val position = freeWorkspacePosition(page)
        val shortcut = WorkspaceShortcut(
            id = UUID.randomUUID().toString(),
            label = shortLauncherActionName(option.title),
            componentName = "",
            page = page.coerceIn(0, workspaceCount - 1),
            xFraction = position.first,
            yFraction = position.second,
            intentAction = option.action
        )
        shortcuts = shortcuts + shortcut
        LauncherStorage.saveShortcuts(context.applicationContext, shortcuts)
        workspaceInsertionAnchor = null
        Toast.makeText(context, l10n(R.string.ui_action_added, shortcut.label), Toast.LENGTH_SHORT).show()
    }

    fun moveShortcut(id: String, page: Int, xFraction: Float, yFraction: Float) {
        if (workspaceEditingLocked) { notifyDesktopEditingLocked(); return }
        shortcuts = shortcuts.map { shortcut ->
            if (shortcut.id == id) {
                shortcut.copy(
                    page = page.coerceIn(0, workspaceCount - 1),
                    xFraction = xFraction.safeWorkspaceFraction(shortcut.xFraction),
                    yFraction = yFraction.safeWorkspaceFraction(shortcut.yFraction)
                )
            } else shortcut
        }
        LauncherStorage.saveShortcuts(context.applicationContext, shortcuts)
    }

    fun deleteShortcut(id: String) {
        if (workspaceEditingLocked) { notifyDesktopEditingLocked(); return }
        val deleted = shortcuts.firstOrNull { it.id == id }
        shortcuts = shortcuts.filterNot { it.id == id }
        LauncherStorage.saveShortcuts(context.applicationContext, shortcuts)
        deleted?.let {
            Toast.makeText(context, l10n(R.string.ui_removed_from_the_desktop, it.label), Toast.LENGTH_SHORT).show()
        }
    }

    fun updateShortcutVisual(
        id: String,
        label: String,
        iconUri: String?,
        iconPackPackage: String?,
        iconPackDrawable: String?
    ) {
        val clean = label.trim().take(48).ifBlank {
            shortcuts.firstOrNull { it.id == id }?.label ?: l10n(R.string.ui_shortcut)
        }
        shortcuts = shortcuts.map { shortcut ->
            if (shortcut.id == id) shortcut.copy(
                label = clean,
                customIconUri = iconUri,
                customIconPackPackage = iconPackPackage,
                customIconPackDrawable = iconPackDrawable
            ) else shortcut
        }
        LauncherStorage.saveShortcuts(context.applicationContext, shortcuts)
        editingShortcut = shortcuts.firstOrNull { it.id == id }
        shortcutMenuTarget = null
    }

    fun createEmptyFolder(page: Int) {
        if (workspaceEditingLocked) { notifyDesktopEditingLocked(); return }
        val position = freeWorkspacePosition(page)
        val folder = WorkspaceFolder(
            id = UUID.randomUUID().toString(),
            label = l10n(R.string.ui_folder),
            page = page.coerceIn(0, workspaceCount - 1),
            xFraction = position.first,
            yFraction = position.second,
            componentNames = emptyList()
        )
        folders = folders + folder
        LauncherStorage.saveFolders(context.applicationContext, folders)
        workspaceInsertionAnchor = null
        Toast.makeText(context, l10n(R.string.ui_folder_created), Toast.LENGTH_SHORT).show()
    }

    fun moveFolder(id: String, page: Int, xFraction: Float, yFraction: Float) {
        if (workspaceEditingLocked) { notifyDesktopEditingLocked(); return }
        folders = folders.map { folder ->
            if (folder.id == id) {
                folder.copy(
                    page = page.coerceIn(0, workspaceCount - 1),
                    xFraction = xFraction.safeWorkspaceFraction(folder.xFraction),
                    yFraction = yFraction.safeWorkspaceFraction(folder.yFraction)
                )
            } else folder
        }
        LauncherStorage.saveFolders(context.applicationContext, folders)
    }

    fun deleteFolder(id: String) {
        if (workspaceEditingLocked) { notifyDesktopEditingLocked(); return }
        val label = folders.firstOrNull { it.id == id }?.label ?: l10n(R.string.ui_folder)
        folders = folders.filterNot { it.id == id }
        LauncherStorage.saveFolders(context.applicationContext, folders)
        folderDialogTarget = null
        folderFanDisplay = null
        Toast.makeText(context, l10n(R.string.ui_removed, label), Toast.LENGTH_SHORT).show()
    }

    fun renameFolder(id: String, newLabel: String) {
        if (workspaceEditingLocked) { notifyDesktopEditingLocked(); return }
        val clean = newLabel.trim().take(40).ifBlank { l10n(R.string.ui_folder) }
        folders = folders.map { folder -> if (folder.id == id) folder.copy(label = clean) else folder }
        LauncherStorage.saveFolders(context.applicationContext, folders)
        folderDialogTarget = folders.firstOrNull { it.id == id }
    }

    fun updateFolderVisual(
        id: String,
        label: String,
        iconUri: String?,
        iconPackPackage: String?,
        iconPackDrawable: String?
    ) {
        if (workspaceEditingLocked) { notifyDesktopEditingLocked(); return }
        val clean = label.trim().take(40).ifBlank { l10n(R.string.ui_folder) }
        folders = folders.map { folder ->
            if (folder.id == id) folder.copy(
                label = clean,
                customIconUri = iconUri,
                customIconPackPackage = iconPackPackage,
                customIconPackDrawable = iconPackDrawable
            ) else folder
        }
        LauncherStorage.saveFolders(context.applicationContext, folders)
        folderDialogTarget = folders.firstOrNull { it.id == id }
    }

    fun updateFolderAppearance(
        id: String,
        shape: FolderShape,
        colorHex: String,
        opacityPercent: Int
    ) {
        if (workspaceEditingLocked) { notifyDesktopEditingLocked(); return }
        folders = folders.map { folder ->
            if (folder.id == id) folder.copy(
                shape = shape,
                colorHex = normalizedColorHex(colorHex) ?: folder.colorHex,
                opacityPercent = opacityPercent.coerceIn(0, 100)
            ) else folder
        }
        LauncherStorage.saveFolders(context.applicationContext, folders)
        folderDialogTarget = folders.firstOrNull { it.id == id }
    }

    fun addSideDockApp(app: AppItem) {
        if (sideDockEditingLocked) { notifyDockEditingLocked(); return }
        val component = app.componentName.flattenToString()
        if (sideDockEntries.any { it.componentName == component }) {
            Toast.makeText(context, l10n(R.string.ui_this_application_is_already_in_the_side_dock), Toast.LENGTH_SHORT).show()
            return
        }
        sideDockEntries = sideDockEntries + SideDockEntry(
            id = UUID.randomUUID().toString(),
            label = app.label,
            componentName = component
        )
        LauncherStorage.saveSideDockEntries(context.applicationContext, sideDockEntries)
    }

    fun addSideDockSettings(option: SettingsShortcutOption) {
        if (sideDockEditingLocked) { notifyDockEditingLocked(); return }
        sideDockEntries = sideDockEntries + SideDockEntry(
            id = UUID.randomUUID().toString(),
            label = option.title,
            intentAction = option.action
        )
        LauncherStorage.saveSideDockEntries(context.applicationContext, sideDockEntries)
    }

    fun addSideDockLauncherAction(option: LauncherActionOption) {
        if (sideDockEditingLocked) { notifyDockEditingLocked(); return }
        if (sideDockEntries.any { it.intentAction == option.action }) return
        sideDockEntries = sideDockEntries + SideDockEntry(
            id = UUID.randomUUID().toString(),
            label = option.title,
            intentAction = option.action
        )
        LauncherStorage.saveSideDockEntries(context.applicationContext, sideDockEntries)
    }

    fun addSideDockPreset(preset: PresetDockIcon) {
        if (sideDockEditingLocked) { notifyDockEditingLocked(); return }
        if (sideDockEntries.any { canonicalLauncherAction(it.intentAction) == preset.action }) return
        sideDockEntries = sideDockEntries + SideDockEntry(
            id = UUID.randomUUID().toString(),
            label = preset.label,
            intentAction = preset.action
        )
        LauncherStorage.saveSideDockEntries(context.applicationContext, sideDockEntries)
    }

    fun removeSideDockEntry(id: String) {
        if (sideDockEditingLocked) { notifyDockEditingLocked(); return }
        sideDockEntries = sideDockEntries.filterNot { it.id == id }
        LauncherStorage.saveSideDockEntries(context.applicationContext, sideDockEntries)
    }

    fun reorderSideDockEntry(fromIndex: Int, toIndex: Int) {
        if (sideDockEditingLocked) { notifyDockEditingLocked(); return }
        if (fromIndex !in sideDockEntries.indices || toIndex !in sideDockEntries.indices || fromIndex == toIndex) return
        val mutable = sideDockEntries.toMutableList()
        val item = mutable.removeAt(fromIndex)
        mutable.add(toIndex, item)
        sideDockEntries = mutable
        LauncherStorage.saveSideDockEntries(context.applicationContext, sideDockEntries)
    }

    fun updateSideDockFolder(folderId: String, transform: (SideDockEntry) -> SideDockEntry) {
        sideDockEntries = sideDockEntries.map { entry ->
            if (entry.id == folderId && entry.isFolder) transform(entry) else entry
        }
        LauncherStorage.saveSideDockEntries(context.applicationContext, sideDockEntries)
        sideDockFolderDialogTarget = sideDockEntries.firstOrNull { it.id == folderId && it.isFolder }
    }

    fun removeAppFromSideDockFolder(folderId: String, component: String) {
        val folder = sideDockEntries.firstOrNull { it.id == folderId && it.isFolder } ?: return
        val remaining = folder.componentNames.filterNot { it == component }
        sideDockEntries = if (remaining.size >= 2) sideDockEntries.map {
            if (it.id == folderId) it.copy(componentNames = remaining) else it
        } else sideDockEntries.mapNotNull {
            if (it.id != folderId) it else remaining.singleOrNull()?.let { single ->
                SideDockEntry(folder.id, appsByComponent[single]?.label ?: l10n(R.string.ui_application), componentName = single)
            }
        }
        LauncherStorage.saveSideDockEntries(context.applicationContext, sideDockEntries)
        sideDockFolderDialogTarget = sideDockEntries.firstOrNull { it.id == folderId && it.isFolder }
    }

    fun reorderSideDockFolderApp(folderId: String, from: Int, to: Int) {
        updateSideDockFolder(folderId) { entry ->
            val list = entry.componentNames.toMutableList()
            if (from in list.indices && to in list.indices && from != to) list.add(to, list.removeAt(from))
            entry.copy(componentNames = list)
        }
    }

    fun createSideDockFolder(sourceId: String, targetId: String) {
        val source = sideDockEntries.firstOrNull { it.id == sourceId } ?: return
        val target = sideDockEntries.firstOrNull { it.id == targetId } ?: return
        val sourceComponent = source.componentName ?: return
        val targetComponent = target.componentName ?: return
        if (source.intentAction != null || target.intentAction != null) return
        sideDockEntries = sideDockEntries.mapNotNull { entry ->
            when (entry.id) {
                sourceId -> null
                targetId -> entry.copy(
                    label = l10n(R.string.ui_folder), componentName = null,
                    componentNames = listOf(targetComponent, sourceComponent).distinct(), folder = true
                )
                else -> entry
            }
        }
        LauncherStorage.saveSideDockEntries(context.applicationContext, sideDockEntries)
    }

    fun addSideDockEntryToFolder(sourceId: String, targetId: String) {
        val source = sideDockEntries.firstOrNull { it.id == sourceId } ?: return
        val component = source.componentName ?: return
        sideDockEntries = sideDockEntries.mapNotNull { entry ->
            when (entry.id) {
                sourceId -> null
                targetId -> if (entry.isFolder) entry.copy(componentNames = (entry.componentNames + component).distinct()) else entry
                else -> entry
            }
        }
        LauncherStorage.saveSideDockEntries(context.applicationContext, sideDockEntries)
    }

    fun moveSideDockFolderAppToWorkspace(folderId: String, component: String, x: Float, y: Float) {
        val app = appsByComponent[component]
        shortcuts = shortcuts + WorkspaceShortcut(
            UUID.randomUUID().toString(), app?.label ?: l10n(R.string.ui_application), component,
            currentWorkspacePage, x.safeWorkspaceFraction(), y.safeWorkspaceFraction()
        )
        LauncherStorage.saveShortcuts(context.applicationContext, shortcuts)
        removeAppFromSideDockFolder(folderId, component)
        sideDockFolderDialogTarget = null
        sideDockOpen = false
    }

    fun moveWorkspaceShortcutToSideDock(shortcut: WorkspaceShortcut): Boolean {
        if (workspaceEditingLocked || sideDockEditingLocked) return false
        val hoverTarget = workspaceSideDockHoverTargetId
            ?.let { id -> sideDockEntries.firstOrNull { it.id == id } }
        if (hoverTarget != null && shortcut.intentAction == null) {
            sideDockEntries = when {
                hoverTarget.isFolder -> sideDockEntries.map { entry ->
                    if (entry.id == hoverTarget.id) entry.copy(
                        componentNames = (entry.componentNames + shortcut.componentName).distinct()
                    ) else entry
                }
                hoverTarget.componentName != null && hoverTarget.intentAction == null -> sideDockEntries.map { entry ->
                    if (entry.id == hoverTarget.id) entry.copy(
                        label = l10n(R.string.ui_folder), componentName = null,
                        componentNames = listOf(hoverTarget.componentName, shortcut.componentName).distinct(), folder = true
                    ) else entry
                }
                else -> return false
            }
        } else {
            val newEntry = if (shortcut.intentAction != null) {
                if (sideDockEntries.any { it.intentAction == shortcut.intentAction }) return false
                SideDockEntry(UUID.randomUUID().toString(), shortcut.label, intentAction = shortcut.intentAction)
            } else {
                if (sideDockEntries.any { it.componentName == shortcut.componentName }) return false
                SideDockEntry(UUID.randomUUID().toString(), shortcut.label, componentName = shortcut.componentName)
            }
            val index = workspaceSideDockInsertionIndex?.coerceIn(0, sideDockEntries.size)
                ?: sideDockEntries.size
            sideDockEntries = sideDockEntries.toMutableList().apply { add(index, newEntry) }
        }
        LauncherStorage.saveSideDockEntries(context.applicationContext, sideDockEntries)
        shortcuts = shortcuts.filterNot { it.id == shortcut.id }
        LauncherStorage.saveShortcuts(context.applicationContext, shortcuts)
        return true
    }

    fun moveSideDockEntryToWorkspace(entry: SideDockEntry, xFraction: Float, yFraction: Float) {
        if (workspaceEditingLocked || sideDockEditingLocked) return
        if (entry.isFolder) {
            folders = folders + WorkspaceFolder(
                id = entry.id, label = entry.label, page = currentWorkspacePage,
                xFraction = xFraction.safeWorkspaceFraction(0.5f),
                yFraction = yFraction.safeWorkspaceFraction(0.45f),
                componentNames = entry.componentNames,
                shape = entry.folderShape, colorHex = entry.folderColorHex,
                opacityPercent = entry.folderOpacityPercent,
                customIconUri = entry.customIconUri,
                customIconPackPackage = entry.customIconPackPackage,
                customIconPackDrawable = entry.customIconPackDrawable
            )
            LauncherStorage.saveFolders(context.applicationContext, folders)
            removeSideDockEntry(entry.id)
            sideDockOpen = false
            return
        }
        val shortcut = WorkspaceShortcut(
            id = UUID.randomUUID().toString(),
            label = entry.label,
            componentName = entry.componentName.orEmpty(),
            page = currentWorkspacePage,
            xFraction = xFraction.safeWorkspaceFraction(0.5f),
            yFraction = yFraction.safeWorkspaceFraction(0.45f),
            intentAction = entry.intentAction
        )
        shortcuts = shortcuts + shortcut
        LauncherStorage.saveShortcuts(context.applicationContext, shortcuts)
        removeSideDockEntry(entry.id)
        sideDockOpen = false
        sideDockDeleteMode = false
    }

    fun launchSideDockEntry(entry: SideDockEntry) {
        runAfterLaunchActivation {
            if (entry.isFolder) {
                sideDockFolderDialogTarget = entry
                return@runAfterLaunchActivation
            }
            entry.intentAction?.let { action ->
                if (performLauncherAction(action)) {
                    if (action != LAUNCHER_ACTION_SIDE_DOCK) sideDockOpen = false
                    return@runAfterLaunchActivation
                }
                if (canonicalLauncherAction(action) == DOCK_ACTION_MESSAGES) {
                    runCatching {
                        context.startActivity(
                            mainMessagingIntent(context).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        )
                    }.onFailure {
                        Toast.makeText(
                            context,
                            l10n(R.string.ui_messaging_application_not_found),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    sideDockOpen = false
                    return@runAfterLaunchActivation
                }
                runCatching { context.startActivity(Intent(action).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
                    .onFailure { Toast.makeText(context, l10n(R.string.ui_this_settings_item_is_unavailable), Toast.LENGTH_SHORT).show() }
                sideDockOpen = false
                return@runAfterLaunchActivation
            }
            val component = entry.componentName?.let(ComponentName::unflattenFromString)
            val app = entry.componentName?.let { appsByComponent[it] }
            when {
                app != null -> launchAppNow(app)
                component != null -> runCatching { repository.launch(component) }
                    .onFailure { Toast.makeText(context, l10n(R.string.ui_application_not_found), Toast.LENGTH_SHORT).show() }
                else -> Toast.makeText(context, l10n(R.string.ui_item_unavailable), Toast.LENGTH_SHORT).show()
            }
            sideDockOpen = false
            sideDockDeleteMode = false
        }
    }

    fun addBottomDockApp(app: AppItem) {
        if (bottomDockEditingLocked) { notifyDockEditingLocked(); return }
        val component = app.componentName.flattenToString()
        val targetPage = bottomDockDataPage(bottomDockSelectedPage, bottomDockPageCount)
        if (bottomDockEntries.count { it.page == targetPage } >= bottomDockCapacity) {
            Toast.makeText(context, l10n(R.string.ui_the_current_bottom_dock_page_is_full), Toast.LENGTH_SHORT).show()
            return
        }
        bottomDockEntries = bottomDockEntries + BottomDockEntry(
            id = UUID.randomUUID().toString(),
            label = app.label,
            componentName = component,
            page = targetPage
        )
        LauncherStorage.saveBottomDockEntries(context.applicationContext, bottomDockEntries)
    }

    fun addBottomDockSettings(option: SettingsShortcutOption) {
        if (bottomDockEditingLocked) { notifyDockEditingLocked(); return }
        val targetPage = bottomDockDataPage(bottomDockSelectedPage, bottomDockPageCount)
        if (bottomDockEntries.count { it.page == targetPage } >= bottomDockCapacity) {
            Toast.makeText(context, l10n(R.string.ui_the_current_bottom_dock_page_is_full), Toast.LENGTH_SHORT).show()
            return
        }
        bottomDockEntries = bottomDockEntries + BottomDockEntry(
            id = UUID.randomUUID().toString(),
            label = option.title,
            intentAction = option.action,
            page = targetPage
        )
        LauncherStorage.saveBottomDockEntries(context.applicationContext, bottomDockEntries)
    }

    fun addBottomDockLauncherAction(option: LauncherActionOption) {
        if (bottomDockEditingLocked) { notifyDockEditingLocked(); return }
        val targetPage = bottomDockDataPage(bottomDockSelectedPage, bottomDockPageCount)
        if (bottomDockEntries.count { it.page == targetPage } >= bottomDockCapacity) {
            Toast.makeText(context, l10n(R.string.ui_the_current_bottom_dock_page_is_full), Toast.LENGTH_SHORT).show()
            return
        }
        bottomDockEntries = bottomDockEntries + BottomDockEntry(
            id = UUID.randomUUID().toString(),
            label = option.title,
            intentAction = option.action,
            page = targetPage
        )
        LauncherStorage.saveBottomDockEntries(context.applicationContext, bottomDockEntries)
    }

    fun addBottomDockPreset(preset: PresetDockIcon) {
        if (bottomDockEditingLocked) { notifyDockEditingLocked(); return }
        val targetPage = bottomDockDataPage(bottomDockSelectedPage, bottomDockPageCount)
        if (bottomDockEntries.count { it.page == targetPage } >= bottomDockCapacity) {
            Toast.makeText(context, l10n(R.string.ui_the_current_bottom_dock_page_is_full), Toast.LENGTH_SHORT).show()
            return
        }
        bottomDockEntries = bottomDockEntries + BottomDockEntry(
            id = UUID.randomUUID().toString(),
            label = preset.label,
            intentAction = preset.action,
            page = targetPage
        )
        LauncherStorage.saveBottomDockEntries(context.applicationContext, bottomDockEntries)
    }

    fun removeBottomDockEntry(id: String) {
        if (bottomDockEditingLocked) { notifyDockEditingLocked(); return }
        bottomDockEntries = bottomDockEntries.filterNot { it.id == id }
        LauncherStorage.saveBottomDockEntries(context.applicationContext, bottomDockEntries)
    }

    fun reorderBottomDockEntryOnPage(sourceId: String, targetIndex: Int) {
        if (bottomDockEditingLocked) { notifyDockEditingLocked(); return }
        val source = bottomDockEntries.firstOrNull { it.id == sourceId } ?: return
        val pageEntries = bottomDockEntries.filter { it.page == source.page }
        val fromIndex = pageEntries.indexOfFirst { it.id == sourceId }
        if (fromIndex < 0 || pageEntries.isEmpty()) return
        val safeTargetIndex = targetIndex.coerceIn(0, pageEntries.lastIndex)
        if (fromIndex == safeTargetIndex) return

        val reorderedPage = pageEntries.toMutableList()
        val moved = reorderedPage.removeAt(fromIndex)
        reorderedPage.add(safeTargetIndex, moved)
        var pageIndex = 0
        bottomDockEntries = bottomDockEntries.map { entry ->
            if (entry.page == source.page) reorderedPage[pageIndex++] else entry
        }
        LauncherStorage.saveBottomDockEntries(context.applicationContext, bottomDockEntries)
    }

    fun moveWorkspaceShortcutToBottomDock(shortcut: WorkspaceShortcut): Boolean {
        if (workspaceEditingLocked || bottomDockEditingLocked) return false
        val targetPage = bottomDockDataPage(bottomDockSelectedPage, bottomDockPageCount)
        val pageEntries = bottomDockEntries.filter { it.page == targetPage }.take(bottomDockCapacity)
        val hoverTarget = workspaceBottomDockHoverTargetId
            ?.let { id -> pageEntries.firstOrNull { it.id == id } }
        if (hoverTarget != null && shortcut.intentAction == null) {
            if (hoverTarget.isFolder) {
                bottomDockEntries = bottomDockEntries.map { entry ->
                    if (entry.id == hoverTarget.id) entry.copy(
                        componentNames = (entry.componentNames + shortcut.componentName).distinct()
                    ) else entry
                }
            } else if (hoverTarget.componentName != null && hoverTarget.intentAction == null) {
                bottomDockEntries = bottomDockEntries.map { entry ->
                    if (entry.id == hoverTarget.id) entry.copy(
                        label = l10n(R.string.ui_folder),
                        componentName = null,
                        componentNames = listOf(hoverTarget.componentName, shortcut.componentName).distinct(),
                        folder = true
                    ) else entry
                }
            } else return false
            LauncherStorage.saveBottomDockEntries(context.applicationContext, bottomDockEntries)
            shortcuts = shortcuts.filterNot { it.id == shortcut.id }
            LauncherStorage.saveShortcuts(context.applicationContext, shortcuts)
            return true
        }
        val targetIndex = workspaceBottomDockInsertionIndex
            ?.coerceIn(0, pageEntries.size) ?: return false
        if (pageEntries.size >= bottomDockCapacity) {
            Toast.makeText(context, l10n(R.string.ui_bottom_dock_full_folder_hint), Toast.LENGTH_LONG).show()
            return true
        }
        val newEntry = if (shortcut.intentAction != null) {
            BottomDockEntry(UUID.randomUUID().toString(), shortcut.label, intentAction = shortcut.intentAction, page = targetPage)
        } else {
            BottomDockEntry(UUID.randomUUID().toString(), shortcut.label, componentName = shortcut.componentName, page = targetPage)
        }
        val globalInsertionIndex = when {
            targetIndex < pageEntries.size -> bottomDockEntries.indexOfFirst {
                it.id == pageEntries[targetIndex].id
            }.coerceAtLeast(0)
            pageEntries.isNotEmpty() -> bottomDockEntries.indexOfLast {
                it.id == pageEntries.last().id
            } + 1
            else -> bottomDockEntries.size
        }
        bottomDockEntries = bottomDockEntries.toMutableList().apply {
            add(globalInsertionIndex.coerceIn(0, size), newEntry)
        }
        LauncherStorage.saveBottomDockEntries(context.applicationContext, bottomDockEntries)
        shortcuts = shortcuts.filterNot { it.id == shortcut.id }
        LauncherStorage.saveShortcuts(context.applicationContext, shortcuts)
        return true
    }

    fun addDraggedAppToBottomDock(app: AppItem): Boolean {
        if (bottomDockEditingLocked) { notifyDockEditingLocked(); return false }
        val targetPage = bottomDockDataPage(bottomDockSelectedPage, bottomDockPageCount)
        val pageEntries = bottomDockEntries.filter { it.page == targetPage }.take(bottomDockCapacity)
        val component = app.componentName.flattenToString()
        val hoverTarget = workspaceBottomDockHoverTargetId?.let { id -> pageEntries.firstOrNull { it.id == id } }
        if (hoverTarget != null) {
            bottomDockEntries = when {
                hoverTarget.isFolder -> bottomDockEntries.map { entry ->
                    if (entry.id == hoverTarget.id) entry.copy(componentNames = (entry.componentNames + component).distinct()) else entry
                }
                hoverTarget.componentName != null && hoverTarget.intentAction == null -> bottomDockEntries.map { entry ->
                    if (entry.id == hoverTarget.id) entry.copy(
                        label = l10n(R.string.ui_folder), componentName = null,
                        componentNames = listOf(hoverTarget.componentName, component).distinct(), folder = true
                    ) else entry
                }
                else -> return false
            }
            LauncherStorage.saveBottomDockEntries(context.applicationContext, bottomDockEntries)
            return true
        }
        val targetIndex = workspaceBottomDockInsertionIndex?.coerceIn(0, pageEntries.size) ?: return false
        if (pageEntries.size >= bottomDockCapacity) {
            Toast.makeText(context, l10n(R.string.ui_bottom_dock_full_folder_hint), Toast.LENGTH_LONG).show()
            return true
        }
        val newEntry = BottomDockEntry(UUID.randomUUID().toString(), app.label, componentName = component, page = targetPage)
        val globalIndex = if (targetIndex < pageEntries.size) bottomDockEntries.indexOfFirst { it.id == pageEntries[targetIndex].id }
            else if (pageEntries.isNotEmpty()) bottomDockEntries.indexOfLast { it.id == pageEntries.last().id } + 1
            else bottomDockEntries.size
        bottomDockEntries = bottomDockEntries.toMutableList().apply { add(globalIndex.coerceIn(0, size), newEntry) }
        LauncherStorage.saveBottomDockEntries(context.applicationContext, bottomDockEntries)
        return true
    }

    fun moveBottomDockEntryToWorkspace(entry: BottomDockEntry, xFraction: Float, yFraction: Float) {
        if (bottomDockEditingLocked || workspaceEditingLocked) return
        if (entry.isFolder) {
            folders = folders + WorkspaceFolder(
                id = entry.id,
                label = entry.label,
                page = currentWorkspacePage,
                xFraction = xFraction.safeWorkspaceFraction(0.5f),
                yFraction = yFraction.safeWorkspaceFraction(0.72f),
                componentNames = entry.componentNames,
                shape = entry.folderShape,
                colorHex = entry.folderColorHex,
                opacityPercent = entry.folderOpacityPercent,
                customIconUri = entry.customIconUri,
                customIconPackPackage = entry.customIconPackPackage,
                customIconPackDrawable = entry.customIconPackDrawable
            )
            LauncherStorage.saveFolders(context.applicationContext, folders)
            removeBottomDockEntry(entry.id)
            return
        }
        val shortcut = WorkspaceShortcut(
            id = UUID.randomUUID().toString(),
            label = entry.label,
            componentName = entry.componentName.orEmpty(),
            page = currentWorkspacePage,
            xFraction = xFraction.safeWorkspaceFraction(0.5f),
            yFraction = yFraction.safeWorkspaceFraction(0.72f),
            intentAction = canonicalLauncherAction(entry.intentAction)?.takeUnless(::isSystemDockAction)
        )
        if (isSystemDockAction(entry.intentAction) && entry.componentName == null) {
            Toast.makeText(context, l10n(R.string.ui_a_system_dock_button_cannot_be_moved_as_a), Toast.LENGTH_SHORT).show()
            return
        }
        shortcuts = shortcuts + shortcut
        LauncherStorage.saveShortcuts(context.applicationContext, shortcuts)
        removeBottomDockEntry(entry.id)
    }

    fun createBottomDockFolder(sourceId: String, targetId: String) {
        if (bottomDockEditingLocked || sourceId == targetId) return
        val source = bottomDockEntries.firstOrNull { it.id == sourceId } ?: return
        val target = bottomDockEntries.firstOrNull { it.id == targetId } ?: return
        if (source.componentName == null || source.intentAction != null ||
            target.componentName == null || target.intentAction != null || source.page != target.page
        ) return
        val sourceIndex = bottomDockEntries.indexOf(source)
        val targetIndex = bottomDockEntries.indexOf(target)
        val folder = BottomDockEntry(
            id = target.id,
            label = l10n(R.string.ui_folder),
            page = target.page,
            componentNames = listOf(target.componentName, source.componentName).distinct(),
            folder = true
        )
        val updated = bottomDockEntries.filterNot { it.id == sourceId || it.id == targetId }.toMutableList()
        val insertionIndex = targetIndex - if (sourceIndex in 0 until targetIndex) 1 else 0
        updated.add(insertionIndex.coerceIn(0, updated.size), folder)
        bottomDockEntries = updated
        LauncherStorage.saveBottomDockEntries(context.applicationContext, bottomDockEntries)
    }

    fun addBottomDockEntryToFolder(sourceId: String, folderId: String) {
        if (bottomDockEditingLocked || sourceId == folderId) return
        val source = bottomDockEntries.firstOrNull { it.id == sourceId } ?: return
        val folder = bottomDockEntries.firstOrNull { it.id == folderId && it.isFolder } ?: return
        val component = source.componentName ?: return
        if (source.intentAction != null || source.page != folder.page) return
        bottomDockEntries = bottomDockEntries.mapNotNull { entry ->
            when (entry.id) {
                sourceId -> null
                folderId -> entry.copy(componentNames = (entry.componentNames + component).distinct())
                else -> entry
            }
        }
        LauncherStorage.saveBottomDockEntries(context.applicationContext, bottomDockEntries)
        bottomDockFolderDialogTarget = bottomDockEntries.firstOrNull { it.id == folderId }
    }

    fun moveBottomDockEntryToPage(entryId: String, page: Int) {
        if (bottomDockEditingLocked) { notifyDockEditingLocked(); return }
        val targetPage = page.coerceIn(0, bottomDockPageCount - 1)
        if (bottomDockEntries.count { it.page == targetPage && it.id != entryId } >= bottomDockCapacity) return
        bottomDockEntries = bottomDockEntries.map {
            if (it.id == entryId) it.copy(page = targetPage) else it
        }
        LauncherStorage.saveBottomDockEntries(context.applicationContext, bottomDockEntries)
    }

    fun moveWorkspaceFolderToSideDock(folder: WorkspaceFolder): Boolean {
        if (workspaceEditingLocked || sideDockEditingLocked) return false
        if (sideDockEntries.any { it.id == folder.id }) return false
        val insertion = workspaceSideDockInsertionIndex?.coerceIn(0, sideDockEntries.size)
            ?: sideDockEntries.size
        val entry = SideDockEntry(
            id = folder.id,
            label = folder.label,
            componentNames = folder.componentNames,
            folder = true,
            folderShape = folder.shape,
            folderColorHex = folder.colorHex,
            folderOpacityPercent = folder.opacityPercent,
            customIconUri = folder.customIconUri,
            customIconPackPackage = folder.customIconPackPackage,
            customIconPackDrawable = folder.customIconPackDrawable
        )
        sideDockEntries = sideDockEntries.toMutableList().apply { add(insertion, entry) }
        folders = folders.filterNot { it.id == folder.id }
        LauncherStorage.saveSideDockEntries(context.applicationContext, sideDockEntries)
        LauncherStorage.saveFolders(context.applicationContext, folders)
        folderDialogTarget = null
        return true
    }

    fun moveWorkspaceFolderToBottomDock(folder: WorkspaceFolder): Boolean {
        if (workspaceEditingLocked || bottomDockEditingLocked) return false
        val targetPage = bottomDockDataPage(bottomDockSelectedPage, bottomDockPageCount)
        if (bottomDockEntries.count { it.page == targetPage } >= bottomDockCapacity) return false
        bottomDockEntries = bottomDockEntries + BottomDockEntry(
            id = folder.id,
            label = folder.label,
            page = targetPage,
            componentNames = folder.componentNames,
            folder = true,
            folderShape = folder.shape,
            folderColorHex = folder.colorHex,
            folderOpacityPercent = folder.opacityPercent,
            customIconUri = folder.customIconUri,
            customIconPackPackage = folder.customIconPackPackage,
            customIconPackDrawable = folder.customIconPackDrawable
        )
        folders = folders.filterNot { it.id == folder.id }
        LauncherStorage.saveBottomDockEntries(context.applicationContext, bottomDockEntries)
        LauncherStorage.saveFolders(context.applicationContext, folders)
        folderDialogTarget = null
        return true
    }

    fun renameBottomDockFolder(id: String, newLabel: String) {
        if (bottomDockEditingLocked) { notifyDockEditingLocked(); return }
        val clean = newLabel.trim().take(40).ifBlank { l10n(R.string.ui_folder) }
        bottomDockEntries = bottomDockEntries.map {
            if (it.id == id && it.isFolder) it.copy(label = clean) else it
        }
        LauncherStorage.saveBottomDockEntries(context.applicationContext, bottomDockEntries)
        bottomDockFolderDialogTarget = bottomDockEntries.firstOrNull { it.id == id }
    }

    fun updateBottomDockFolderAppearance(id: String, shape: FolderShape, color: String, opacity: Int) {
        if (bottomDockEditingLocked) { notifyDockEditingLocked(); return }
        bottomDockEntries = bottomDockEntries.map {
            if (it.id == id && it.isFolder) it.copy(
                folderShape = shape,
                folderColorHex = normalizedColorHex(color) ?: it.folderColorHex,
                folderOpacityPercent = opacity.coerceIn(0, 100)
            ) else it
        }
        LauncherStorage.saveBottomDockEntries(context.applicationContext, bottomDockEntries)
        bottomDockFolderDialogTarget = bottomDockEntries.firstOrNull { it.id == id }
    }

    fun updateBottomDockFolderVisual(
        id: String,
        label: String,
        iconUri: String?,
        packPackage: String?,
        packDrawable: String?
    ) {
        if (bottomDockEditingLocked) { notifyDockEditingLocked(); return }
        bottomDockEntries = bottomDockEntries.map {
            if (it.id == id && it.isFolder) it.copy(
                label = label.trim().take(40).ifBlank { l10n(R.string.ui_folder) },
                customIconUri = iconUri,
                customIconPackPackage = packPackage,
                customIconPackDrawable = packDrawable
            ) else it
        }
        LauncherStorage.saveBottomDockEntries(context.applicationContext, bottomDockEntries)
        bottomDockFolderDialogTarget = bottomDockEntries.firstOrNull { it.id == id }
    }

    fun dissolveBottomDockFolderIfNeeded(entry: BottomDockEntry): BottomDockEntry? {
        if (entry.componentNames.size > 1) return entry
        val component = entry.componentNames.singleOrNull() ?: return null
        return BottomDockEntry(
            id = entry.id,
            label = appsByComponent[component]?.label ?: l10n(R.string.ui_application),
            componentName = component,
            page = entry.page
        )
    }

    fun removeAppFromBottomDockFolder(folderId: String, componentName: String) {
        if (bottomDockEditingLocked) { notifyDockEditingLocked(); return }
        bottomDockEntries = bottomDockEntries.mapNotNull { entry ->
            if (entry.id != folderId || !entry.isFolder) entry
            else dissolveBottomDockFolderIfNeeded(
                entry.copy(componentNames = entry.componentNames.filterNot { it == componentName })
            )
        }
        LauncherStorage.saveBottomDockEntries(context.applicationContext, bottomDockEntries)
        bottomDockFolderDialogTarget = bottomDockEntries.firstOrNull { it.id == folderId && it.isFolder }
    }

    fun reorderBottomDockFolderApp(folderId: String, fromIndex: Int, toIndex: Int) {
        bottomDockEntries = bottomDockEntries.map { entry ->
            if (entry.id != folderId || !entry.isFolder) entry else {
                val components = entry.componentNames.toMutableList()
                if (fromIndex !in components.indices || toIndex !in components.indices || fromIndex == toIndex) entry
                else entry.copy(componentNames = components.apply { add(toIndex, removeAt(fromIndex)) })
            }
        }
        LauncherStorage.saveBottomDockEntries(context.applicationContext, bottomDockEntries)
        bottomDockFolderDialogTarget = bottomDockEntries.firstOrNull { it.id == folderId }
    }

    fun moveBottomDockFolderAppToWorkspace(
        folderId: String,
        componentName: String,
        xFraction: Float,
        yFraction: Float
    ) {
        if (bottomDockEditingLocked || workspaceEditingLocked) return
        shortcuts = shortcuts + WorkspaceShortcut(
            id = UUID.randomUUID().toString(),
            label = appsByComponent[componentName]?.label ?: l10n(R.string.ui_application),
            componentName = componentName,
            page = currentWorkspacePage,
            xFraction = xFraction.safeWorkspaceFraction(),
            yFraction = yFraction.safeWorkspaceFraction()
        )
        LauncherStorage.saveShortcuts(context.applicationContext, shortcuts)
        removeAppFromBottomDockFolder(folderId, componentName)
        bottomDockFolderDialogTarget = null
    }

    fun launchExternal(primary: Intent, fallback: Intent? = null, errorMessage: String) {
        try {
            context.startActivity(primary.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        } catch (_: ActivityNotFoundException) {
            if (fallback != null) {
                try {
                    context.startActivity(fallback.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                    return
                } catch (_: ActivityNotFoundException) {
                    // Show the common error below.
                }
            }
            Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
        }
    }

    fun launchBottomDockEntry(entry: BottomDockEntry) {
        runAfterLaunchActivation {
            if (entry.isFolder) {
                bottomDockFolderDialogTarget = entry
                return@runAfterLaunchActivation
            }
            val action = canonicalLauncherAction(entry.intentAction)
            action?.let {
                if (performLauncherAction(it)) return@runAfterLaunchActivation
            }
            when (action) {
                DOCK_ACTION_PHONE -> launchExternal(Intent(Intent.ACTION_DIAL, Uri.parse("tel:")), errorMessage = l10n(R.string.ui_phone_application_not_found))
                DOCK_ACTION_FILES -> launchExternal(
                    Intent.makeMainSelectorActivity(Intent.ACTION_MAIN, Intent.CATEGORY_APP_FILES),
                    Intent(Intent.ACTION_OPEN_DOCUMENT).apply { addCategory(Intent.CATEGORY_OPENABLE); type = "*/*" },
                    l10n(R.string.ui_file_manager_not_found)
                )
                DOCK_ACTION_APPS -> {
                    pendingPlacement = null
                    appPickerMode = false
                    sideDockAppPickerMode = false
                    bottomDockAppPickerMode = false
                    sideDockOpen = false
                    drawerOpen = true
                }
                DOCK_ACTION_MAPS -> launchExternal(Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=")), errorMessage = l10n(R.string.ui_maps_application_not_found))
                DOCK_ACTION_MESSAGES -> launchExternal(
                    mainMessagingIntent(context),
                    errorMessage = l10n(R.string.ui_messaging_application_not_found)
                )
                else -> {
                    action?.let {
                        runCatching { context.startActivity(Intent(it).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
                            .onFailure { Toast.makeText(context, l10n(R.string.ui_item_unavailable), Toast.LENGTH_SHORT).show() }
                        return@runAfterLaunchActivation
                    }
                    val app = entry.componentName?.let { appsByComponent[it] }
                    val component = entry.componentName?.let(ComponentName::unflattenFromString)
                    when {
                        app != null -> launchAppNow(app)
                        component != null -> runCatching { repository.launch(component) }
                        else -> Toast.makeText(context, l10n(R.string.ui_application_not_found), Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    fun startBottomDockFolderFan(entry: BottomDockEntry, origin: Offset) {
        if (!entry.isFolder) return
        bottomDockFolderFanDisplay = FolderFanDisplay(
            folderId = entry.id,
            page = entry.page,
            fingerPosition = origin,
            rawFingerPosition = origin,
            trail = listOf(FolderFanPathPoint(origin, 0f)),
            headDistancePx = 0f,
            dragging = true
        )
    }

    fun dragBottomDockFolderFan(entry: BottomDockEntry, amount: Offset) {
        val current = bottomDockFolderFanDisplay?.takeIf { it.folderId == entry.id } ?: return
        val baseFollow = folderFanSpeed.coerceIn(15, 100) / 100f
        val retention = folderFanDecay.coerceIn(0, 100) / 100f
        val screenWidth = with(density) { configuration.screenWidthDp.dp.toPx() }
        val screenHeight = with(density) { configuration.screenHeightDp.dp.toPx() }
        val rawNext = Offset(
            (current.rawFingerPosition.x + amount.x).coerceIn(0f, screenWidth),
            (current.rawFingerPosition.y + amount.y).coerceIn(0f, screenHeight)
        )
        val iconSpacingPx = with(density) { folderFanSpacing.coerceIn(48, 260).dp.toPx() }
        val fullExtractionDistancePx =
            (entry.componentNames.size - 1).coerceAtLeast(0) * iconSpacingPx
        val advance = advanceFolderFan(
            rawStart = current.rawFingerPosition,
            rawEnd = rawNext,
            trail = current.trail,
            headDistancePx = current.headDistancePx,
            initialSpeedRatio = baseFollow,
            speedMultiplier = current.speedMultiplier,
            fullExtractionDistancePx = fullExtractionDistancePx,
            retentionRatio = retention,
            decayStepPx = with(density) { 16.dp.toPx() }
        )
        bottomDockFolderFanDisplay = current.copy(
            fingerPosition = advance.head,
            rawFingerPosition = rawNext,
            trail = advance.trail,
            headDistancePx = advance.headDistancePx,
            speedMultiplier = advance.speedMultiplier
        )
    }

    fun finishBottomDockFolderFan(entry: BottomDockEntry) {
        bottomDockFolderFanDisplay = bottomDockFolderFanDisplay
            ?.takeIf { it.folderId == entry.id }
            ?.copy(dragging = false)
    }

    // A swipe on a CLOSED folder in the side dock pulls its icons into the
    // existing FolderFanOverlay. Dragging icons out of an OPEN FolderDialog is
    // an unrelated long-press gesture, implemented above unchanged.
    fun startSideDockFolderFan(entry: SideDockEntry, origin: Offset) {
        if (!entry.isFolder) return
        bottomDockFolderFanDisplay = null
        folderFanDisplay = null
        sideDockFolderFanDisplay = FolderFanDisplay(
            folderId = entry.id,
            page = currentWorkspacePage,
            fingerPosition = origin,
            rawFingerPosition = origin,
            trail = listOf(FolderFanPathPoint(origin, 0f)),
            headDistancePx = 0f,
            dragging = true
        )
    }

    fun dragSideDockFolderFan(entry: SideDockEntry, amount: Offset) {
        val current = sideDockFolderFanDisplay?.takeIf { it.folderId == entry.id } ?: return
        val baseFollow = folderFanSpeed.coerceIn(15, 100) / 100f
        val retention = folderFanDecay.coerceIn(0, 100) / 100f
        val screenWidth = with(density) { configuration.screenWidthDp.dp.toPx() }
        val screenHeight = with(density) { configuration.screenHeightDp.dp.toPx() }
        val rawNext = Offset(
            (current.rawFingerPosition.x + amount.x).coerceIn(0f, screenWidth),
            (current.rawFingerPosition.y + amount.y).coerceIn(0f, screenHeight)
        )
        val iconSpacingPx = with(density) { folderFanSpacing.coerceIn(48, 260).dp.toPx() }
        val fullExtractionDistancePx = (entry.componentNames.size - 1).coerceAtLeast(0) * iconSpacingPx
        val advance = advanceFolderFan(
            rawStart = current.rawFingerPosition,
            rawEnd = rawNext,
            trail = current.trail,
            headDistancePx = current.headDistancePx,
            initialSpeedRatio = baseFollow,
            speedMultiplier = current.speedMultiplier,
            fullExtractionDistancePx = fullExtractionDistancePx,
            retentionRatio = retention,
            decayStepPx = with(density) { 16.dp.toPx() }
        )
        sideDockFolderFanDisplay = current.copy(
            fingerPosition = advance.head,
            rawFingerPosition = rawNext,
            trail = advance.trail,
            headDistancePx = advance.headDistancePx,
            speedMultiplier = advance.speedMultiplier
        )
    }

    fun finishSideDockFolderFan(entry: SideDockEntry) {
        sideDockFolderFanDisplay = sideDockFolderFanDisplay
            ?.takeIf { it.folderId == entry.id }
            ?.copy(dragging = false)
    }

    fun createFolderFromShortcuts(sourceId: String, targetId: String, page: Int) {
        if (workspaceEditingLocked) { notifyDesktopEditingLocked(); return }
        val source = shortcuts.firstOrNull { it.id == sourceId } ?: return
        val target = shortcuts.firstOrNull { it.id == targetId } ?: return
        val folder = WorkspaceFolder(
            id = UUID.randomUUID().toString(),
            label = l10n(R.string.ui_folder),
            page = page.coerceIn(0, workspaceCount - 1),
            xFraction = target.xFraction,
            yFraction = target.yFraction,
            componentNames = listOf(target.intentAction ?: target.componentName, source.intentAction ?: source.componentName).distinct()
        )
        shortcuts = shortcuts.filterNot { it.id == sourceId || it.id == targetId }
        folders = folders + folder
        LauncherStorage.saveShortcuts(context.applicationContext, shortcuts)
        LauncherStorage.saveFolders(context.applicationContext, folders)
    }

    fun createFolderFromAppAndShortcut(app: AppItem, targetId: String, page: Int) {
        if (workspaceEditingLocked) { notifyDesktopEditingLocked(); return }
        val target = shortcuts.firstOrNull { it.id == targetId } ?: return
        if (target.intentAction != null) return
        val component = app.componentName.flattenToString()
        val folder = WorkspaceFolder(
            id = UUID.randomUUID().toString(),
            label = l10n(R.string.ui_folder),
            page = page.coerceIn(0, workspaceCount - 1),
            xFraction = target.xFraction,
            yFraction = target.yFraction,
            componentNames = listOf(target.componentName, component).distinct()
        )
        shortcuts = shortcuts.filterNot { it.id == targetId }
        folders = folders + folder
        LauncherStorage.saveShortcuts(context.applicationContext, shortcuts)
        LauncherStorage.saveFolders(context.applicationContext, folders)
    }

    fun moveShortcutIntoFolder(shortcutId: String, folderId: String) {
        if (workspaceEditingLocked) { notifyDesktopEditingLocked(); return }
        val shortcut = shortcuts.firstOrNull { it.id == shortcutId } ?: return
        folders = folders.map { folder ->
            if (folder.id == folderId) {
                folder.copy(componentNames = (folder.componentNames + (shortcut.intentAction ?: shortcut.componentName)).distinct())
            } else folder
        }
        shortcuts = shortcuts.filterNot { it.id == shortcutId }
        LauncherStorage.saveShortcuts(context.applicationContext, shortcuts)
        LauncherStorage.saveFolders(context.applicationContext, folders)
    }

    fun addAppToFolder(folderId: String, app: AppItem) {
        if (workspaceEditingLocked) { notifyDesktopEditingLocked(); return }
        val component = app.componentName.flattenToString()
        folders = folders.map { folder ->
            if (folder.id == folderId) folder.copy(
                componentNames = (folder.componentNames + component).distinct()
            ) else folder
        }
        LauncherStorage.saveFolders(context.applicationContext, folders)
        Toast.makeText(context, l10n(R.string.ui_added_to_the_folder, app.label), Toast.LENGTH_SHORT).show()
    }

    fun removeAppFromFolder(folderId: String, componentName: String) {
        if (workspaceEditingLocked) { notifyDesktopEditingLocked(); return }
        var releasedShortcut: WorkspaceShortcut? = null
        folders = folders.mapNotNull { folder ->
            if (folder.id != folderId) folder else {
                val remaining = folder.componentNames.filterNot { it == componentName }
                if (remaining.size <= 1) {
                    remaining.singleOrNull()?.let { remainingComponent ->
                        releasedShortcut = WorkspaceShortcut(
                            id = UUID.randomUUID().toString(),
                            label = appsByComponent[remainingComponent]?.label ?: launcherActionDisplayName(remainingComponent) ?: l10n(R.string.ui_application),
                            componentName = if (launcherActionOptionForComponent(remainingComponent) != null) "" else remainingComponent,
                            page = folder.page,
                            xFraction = folder.xFraction,
                            yFraction = folder.yFraction,
                            intentAction = launcherActionOptionForComponent(remainingComponent)?.action
                        )
                    }
                    null
                } else folder.copy(componentNames = remaining)
            }
        }
        releasedShortcut?.let { shortcuts = shortcuts + it }
        LauncherStorage.saveShortcuts(context.applicationContext, shortcuts)
        LauncherStorage.saveFolders(context.applicationContext, folders)
        folderDialogTarget = folders.firstOrNull { it.id == folderId }
    }

    fun reorderFolderApp(folderId: String, fromIndex: Int, toIndex: Int) {
        folders = folders.map { folder ->
            if (folder.id != folderId) folder else {
                val items = folder.componentNames.toMutableList()
                if (fromIndex !in items.indices || toIndex !in items.indices || fromIndex == toIndex) folder
                else {
                    val moved = items.removeAt(fromIndex)
                    items.add(toIndex, moved)
                    folder.copy(componentNames = items)
                }
            }
        }
        LauncherStorage.saveFolders(context.applicationContext, folders)
        folderDialogTarget = folders.firstOrNull { it.id == folderId }
    }

    fun workspaceDropFractions(screenPosition: Offset): Pair<Float, Float> {
        val bounds = workspaceBoundsOnScreen
        val densityValue = density.density
        val measured = bounds.width > 1f && bounds.height > 1f
        return workspaceFolderDropFractions(
            screenPosition.x, screenPosition.y,
            if (measured) bounds.left else 0f,
            if (measured) bounds.top else 0f,
            if (measured) bounds.width else configuration.screenWidthDp * densityValue,
            if (measured) bounds.height else configuration.screenHeightDp * densityValue,
            densityValue
        )
    }

    fun moveFolderAppToWorkspace(folderId: String, componentName: String, xFraction: Float, yFraction: Float) {
        if (workspaceEditingLocked) { notifyDesktopEditingLocked(); return }
        val folder = folders.firstOrNull { it.id == folderId } ?: return
        val app = appsByComponent[componentName]
        // Extracting an icon from a folder is a manual drag with a known
        // screen position; do not snap it to a different vacant rectangle.
        val shortcut = WorkspaceShortcut(
            id = UUID.randomUUID().toString(),
            label = app?.label ?: launcherActionDisplayName(componentName) ?: l10n(R.string.ui_application),
            componentName = if (launcherActionOptionForComponent(componentName) != null) "" else componentName,
            page = folder.page.coerceIn(0, workspaceCount - 1),
            xFraction = xFraction.safeWorkspaceFraction(),
            yFraction = yFraction.safeWorkspaceFraction(),
            intentAction = launcherActionOptionForComponent(componentName)?.action
        )
        var releasedShortcut: WorkspaceShortcut? = null
        shortcuts = shortcuts + shortcut
        folders = folders.mapNotNull { item ->
            if (item.id != folderId) item else {
                val remaining = item.componentNames.filterNot { it == componentName }
                if (remaining.size <= 1) {
                    remaining.singleOrNull()?.let { remainingComponent ->
                        releasedShortcut = WorkspaceShortcut(
                            id = UUID.randomUUID().toString(),
                            label = appsByComponent[remainingComponent]?.label ?: launcherActionDisplayName(remainingComponent) ?: l10n(R.string.ui_application),
                            componentName = if (launcherActionOptionForComponent(remainingComponent) != null) "" else remainingComponent,
                            page = item.page,
                            xFraction = item.xFraction,
                            yFraction = item.yFraction,
                            intentAction = launcherActionOptionForComponent(remainingComponent)?.action
                        )
                    }
                    null
                } else item.copy(componentNames = remaining)
            }
        }
        releasedShortcut?.let { shortcuts = shortcuts + it }
        LauncherStorage.saveShortcuts(context.applicationContext, shortcuts)
        LauncherStorage.saveFolders(context.applicationContext, folders)
        folderDialogTarget = null
    }

    fun addNote(page: Int) {
        if (workspaceEditingLocked) { notifyDesktopEditingLocked(); return }
        activity.requestBindWidget(
            ComponentName(context, LauncherNoteWidgetProvider::class.java),
            page.coerceIn(0, workspaceCount - 1)
        )
    }

    fun resizeNote(id: String, widthDp: Int, heightDp: Int, x: Float, y: Float) {
        if (workspaceEditingLocked) { notifyDesktopEditingLocked(); return }
        notes = notes.map { if (it.id == id) it.copy(
            widthDp = widthDp.coerceIn(120, 900),
            heightDp = heightDp.coerceIn(72, 900),
            xFraction = x.coerceIn(0f, 1f),
            yFraction = y.coerceIn(0f, 1f)
        ) else it }
        LauncherNotesStore.save(context.applicationContext, notes)
    }

    fun moveNote(id: String, x: Float, y: Float) {
        if (workspaceEditingLocked) { notifyDesktopEditingLocked(); return }
        notes = notes.map { if (it.id == id) it.copy(
            xFraction = x.coerceIn(0f, 1f), yFraction = y.coerceIn(0f, 1f)) else it }
        LauncherNotesStore.save(context.applicationContext, notes)
    }

    fun saveNote(id: String, text: String) {
        notes = notes.map { if (it.id == id) it.copy(text = text.take(4000)) else it }
        LauncherNotesStore.save(context.applicationContext, notes)
    }

    fun deleteNote(id: String) {
        notes = notes.filterNot { it.id == id }
        LauncherNotesStore.save(context.applicationContext, notes)
    }

    fun updateWidget(updated: WorkspaceWidget) {
        if (workspaceEditingLocked) { notifyDesktopEditingLocked(); return }
        widgets = widgets.map { if (it.id == updated.id) updated else it }
        LauncherStorage.saveWidgets(context.applicationContext, widgets)
        runCatching {
            activity.launcherAppWidgetManager.updateAppWidgetOptions(
                updated.appWidgetId,
                Bundle().apply {
                    putInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, updated.widthDp)
                    putInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH, updated.widthDp)
                    putInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, updated.heightDp)
                    putInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT, updated.heightDp)
                }
            )
        }
    }

    fun deleteWidget(widgetId: String) {
        val widget = widgets.firstOrNull { it.id == widgetId } ?: return
        widgets = widgets.filterNot { it.id == widgetId }
        LauncherStorage.saveWidgets(context.applicationContext, widgets)
        activity.deleteHostedWidget(widget.appWidgetId)
        LauncherSettingsBackup.completePendingWidgetRestore(context.applicationContext, widget.id)
        widgetActionChains = widgetActionChains.mapNotNull { chain ->
            val remaining = chain.widgetIds - widget.id
            chain.copy(widgetIds = remaining).takeIf { remaining.isNotEmpty() }
        }
        WidgetActionChainStorage.save(context.applicationContext, widgetActionChains)
        widgetEditingId = null
    }

    fun saveWidgetActionChain(chain: WidgetActionChain) {
        val validWidgetIds = widgets.mapTo(mutableSetOf()) { it.id }
        val selectedIds = chain.widgetIds.filterTo(mutableSetOf()) { it in validWidgetIds }
        val updatedChain = chain.copy(
            name = chain.name.trim().ifBlank { l10n(R.string.ui_widget_action_chain) },
            widgetIds = selectedIds,
            region = chain.region.normalized()
        )
        val others = widgetActionChains
            .filterNot { it.id == updatedChain.id }
            .mapNotNull { existing ->
                val remaining = existing.widgetIds - selectedIds
                existing.copy(widgetIds = remaining).takeIf { remaining.isNotEmpty() }
            }
        widgetActionChains = others + updatedChain
        WidgetActionChainStorage.save(context.applicationContext, widgetActionChains)
    }

    fun deleteWidgetActionChain(chainId: String) {
        widgetActionChains = widgetActionChains.filterNot { it.id == chainId }
        WidgetActionChainStorage.save(context.applicationContext, widgetActionChains)
    }

    fun updatePageTransition(effect: PageTransitionEffect) {
        pageTransitionEffect = effect
        LauncherStorage.savePageTransition(context.applicationContext, effect)
    }

    fun updateMeshSnapshotAfterIdle(enabled: Boolean) {
        meshSnapshotAfterIdle = enabled
        LauncherStorage.saveMeshSnapshotAfterIdle(context.applicationContext, enabled)
    }

    fun updateWorkspaceLoop(enabled: Boolean) {
        workspaceLoop = enabled
        LauncherStorage.saveWorkspaceLoop(context.applicationContext, enabled)
    }

    fun updateAppDrawerLoop(enabled: Boolean) {
        appDrawerLoop = enabled
        LauncherStorage.saveAppDrawerLoop(context.applicationContext, enabled)
    }

    fun updateAppDrawerMode(mode: AppDrawerMode) {
        appDrawerMode = mode
        LauncherStorage.saveAppDrawerMode(context.applicationContext, mode)
    }

    fun updateAppDrawerPageTransition(effect: PageTransitionEffect) {
        appDrawerPageTransitionEffect = effect
        LauncherStorage.saveAppDrawerPageTransition(context.applicationContext, effect)
    }

    fun updateAppDrawerIconEffect(effect: AppDrawerIconEffect) {
        appDrawerIconEffect = effect
        LauncherStorage.saveAppDrawerIconEffect(context.applicationContext, effect)
    }

    fun updateAppLaunchAnimation(effect: AppLaunchAnimationEffect) {
        appLaunchAnimationEffect = effect
        LauncherStorage.saveAppLaunchAnimation(context.applicationContext, effect)
    }

    fun updateAppLaunchDelay(value: Int) {
        appLaunchDelay = value.coerceIn(100, 1000)
        LauncherStorage.saveAppLaunchDelay(context.applicationContext, appLaunchDelay)
    }

    fun updateAppLaunchAnimationDuration(value: Int) {
        appLaunchAnimationDuration = value.coerceIn(100, 1000)
        LauncherStorage.saveAppLaunchAnimationDuration(context.applicationContext, appLaunchAnimationDuration)
        if (interactionExtras.linkLaunchDelayToAnimation) updateAppLaunchDelay(appLaunchAnimationDuration)
    }

    fun updateAppDrawerDragOverviewMode(value: AppDrawerDragOverviewMode) {
        appDrawerDragOverviewMode = value
        LauncherStorage.saveAppDrawerDragOverviewMode(context.applicationContext, value)
    }

    fun updateAppDrawerDragPageHover(value: Int) {
        appDrawerDragPageHover = value.coerceIn(250, 2000)
        LauncherStorage.saveAppDrawerDragPageHover(context.applicationContext, appDrawerDragPageHover)
    }

    fun updateAppDrawerQuickAction(value: AppDrawerQuickAction) {
        appDrawerQuickAction = normalizeAppDrawerQuickAction(value)
        LauncherStorage.saveAppDrawerQuickAction(context.applicationContext, appDrawerQuickAction)
    }

    fun updateBottomDockCapacity(value: Int) {
        bottomDockCapacity = value.coerceIn(1, 10)
        LauncherStorage.saveBottomDockCapacity(context.applicationContext, bottomDockCapacity)
    }

    fun updateAppDrawerPage(page: Int) {
        appDrawerPage = page.coerceAtLeast(0)
        LauncherStorage.saveAppDrawerPage(context.applicationContext, appDrawerPage)
    }

    fun updateAppDrawerColumns(value: Int) {
        appDrawerColumns = value.coerceIn(3, 10)
        LauncherStorage.saveAppDrawerColumns(context.applicationContext, appDrawerColumns)
    }

    fun updateAppDrawerRows(value: Int) {
        appDrawerRows = value.coerceIn(3, 20)
        LauncherStorage.saveAppDrawerRows(context.applicationContext, appDrawerRows)
    }

    fun updateAppDrawerItemWidth(value: Int) {
        appDrawerItemWidth = value.coerceIn(12, 180)
        LauncherStorage.saveAppDrawerItemWidth(context.applicationContext, appDrawerItemWidth)
    }

    fun updateAppDrawerItemHeight(value: Int) {
        appDrawerItemHeight = value.coerceIn(12, 220)
        LauncherStorage.saveAppDrawerItemHeight(context.applicationContext, appDrawerItemHeight)
    }

    fun updateAppDrawerIndependentSize(value: Boolean) {
        appDrawerIndependentSize = value
        LauncherStorage.saveAppDrawerIndependentSize(context.applicationContext, value)
    }

    fun updateAppDrawerFillAvailable(value: Boolean) {
        appDrawerFillAvailable = value
        LauncherStorage.saveAppDrawerFillAvailable(context.applicationContext, value)
    }

    fun updateAppDrawerScreenHeightDp(value: Int) {
        appDrawerScreenHeightDp = value.coerceIn(0, 2000)
        LauncherStorage.saveAppDrawerScreenHeightDp(context.applicationContext, appDrawerScreenHeightDp)
    }

    fun updateAppDrawerFrameShape(value: AppItemFrameShape) {
        appDrawerFrameShape = value
        LauncherStorage.saveAppDrawerFrameShape(context.applicationContext, value)
    }

    fun updateAppDrawerFrameColor(value: String) {
        appDrawerFrameColor = value
        LauncherStorage.saveAppDrawerFrameColor(context.applicationContext, value)
    }

    fun updateAppDrawerFrameOpacity(value: Int) {
        appDrawerFrameOpacity = value.coerceIn(0, 100)
        LauncherStorage.saveAppDrawerFrameOpacity(context.applicationContext, appDrawerFrameOpacity)
    }

    fun updateAppDrawerFrameTextColor(value: String) {
        appDrawerFrameTextColor = value
        appDrawerBackgroundTextColor = value
        LauncherStorage.saveAppDrawerFrameTextColor(context.applicationContext, value)
        LauncherStorage.saveAppDrawerBackgroundTextColor(context.applicationContext, value)
    }

    fun updateAppDrawerEdgeSwitchDp(value: Int) {
        appDrawerEdgeSwitchDp = value.coerceIn(16, 140)
        LauncherStorage.saveAppDrawerEdgeSwitchDp(context.applicationContext, appDrawerEdgeSwitchDp)
    }

    fun updateAppDrawerBackgroundMode(value: AppDrawerBackgroundMode) {
        appDrawerBackgroundMode = value
        LauncherStorage.saveAppDrawerBackgroundMode(context.applicationContext, value)
    }

    fun updateAppDrawerBackgroundOpacity(value: Int) {
        appDrawerBackgroundOpacity = value.coerceIn(0, 100)
        LauncherStorage.saveAppDrawerBackgroundOpacity(context.applicationContext, appDrawerBackgroundOpacity)
    }

    fun updateAppDrawerBackgroundColorA(value: String) {
        appDrawerBackgroundColorA = value
        LauncherStorage.saveAppDrawerBackgroundColorA(context.applicationContext, value)
    }

    fun updateAppDrawerBackgroundColorB(value: String) {
        appDrawerBackgroundColorB = value
        LauncherStorage.saveAppDrawerBackgroundColorB(context.applicationContext, value)
    }

    fun updateAppDrawerBackgroundTextColor(value: String) {
        appDrawerBackgroundTextColor = value
        appDrawerFrameTextColor = value
        LauncherStorage.saveAppDrawerBackgroundTextColor(context.applicationContext, value)
        LauncherStorage.saveAppDrawerFrameTextColor(context.applicationContext, value)
    }

    fun updateSideDockTextColor(value: String) {
        sideDockTextColor = value
        LauncherStorage.saveSideDockTextColor(context.applicationContext, value)
    }

    fun updateBottomDockPageCount(value: Int) {
        bottomDockPageCount = value.coerceIn(1, 5)
        LauncherStorage.saveBottomDockPageCount(context.applicationContext, bottomDockPageCount)
        if (bottomDockEntries.any { it.page >= bottomDockPageCount }) {
            bottomDockEntries = bottomDockEntries.filter { it.page < bottomDockPageCount }
            LauncherStorage.saveBottomDockEntries(context.applicationContext, bottomDockEntries)
        }
        bottomDockSelectedPage = bottomDockSelectedPage.coerceIn(0, bottomDockPageCount - 1)
    }

    fun updateBottomDockLoop(value: Boolean) {
        bottomDockLoop = value
        LauncherStorage.saveBottomDockLoop(context.applicationContext, value)
    }

    fun updateBottomDockIndicatorToggle(value: Boolean) {
        bottomDockIndicatorToggle = value
        LauncherStorage.saveBottomDockIndicatorToggle(context.applicationContext, value)
        if (!value) bottomDockVisible = true
    }

    fun updateBottomDockAddButtonEnabled(value: Boolean) {
        bottomDockAddButtonEnabled = value
        LauncherStorage.saveBottomDockAddButtonEnabled(context.applicationContext, value)
    }

    fun updateBottomDockAlignment(value: BottomDockAlignment) {
        bottomDockAlignment = value
        LauncherStorage.saveBottomDockAlignment(context.applicationContext, value)
    }

    fun updateBottomDockBackgroundColor(value: String) {
        bottomDockBackgroundColor = value
        LauncherStorage.saveBottomDockBackgroundColor(context.applicationContext, value)
    }

    fun updateBottomDockBackgroundOpacity(value: Int) {
        bottomDockBackgroundOpacity = value.coerceIn(0, 100)
        LauncherStorage.saveBottomDockBackgroundOpacity(context.applicationContext, bottomDockBackgroundOpacity)
    }

    fun updateBottomDockTextColor(value: String) {
        bottomDockTextColor = value
        LauncherStorage.saveBottomDockTextColor(context.applicationContext, value)
    }

    fun updateBottomDockShelfHeight(value: Int) {
        bottomDockShelfHeight = value.coerceIn(0, 72)
        LauncherStorage.saveBottomDockShelfHeight(context.applicationContext, bottomDockShelfHeight)
    }

    fun updateBottomDockIconVerticalOffset(value: Int) {
        bottomDockIconVerticalOffset = value.coerceIn(-32, 32)
        LauncherStorage.saveBottomDockIconVerticalOffset(context.applicationContext, bottomDockIconVerticalOffset)
    }

    fun updateBottomDockShelfVerticalOffset(value: Int) {
        bottomDockShelfVerticalOffset = value.coerceIn(-32, 32)
        LauncherStorage.saveBottomDockShelfVerticalOffset(context.applicationContext, bottomDockShelfVerticalOffset)
    }

    fun updateBottomDockPageIndicatorVisible(value: Boolean) {
        bottomDockPageIndicatorVisible = value
        LauncherStorage.saveBottomDockPageIndicatorVisible(context.applicationContext, value)
    }

    fun updateBottomDockPageIndicatorGap(value: Int) {
        bottomDockPageIndicatorGap = value.coerceIn(0, 32)
        LauncherStorage.saveBottomDockPageIndicatorGap(context.applicationContext, bottomDockPageIndicatorGap)
    }

    fun updateBottomDockLabelsVisible(value: Boolean) {
        bottomDockLabelsVisible = value
        LauncherStorage.saveBottomDockLabelsVisible(context.applicationContext, value)
    }

    fun updateBottomDockShelfShape(value: DockShelfShape) {
        bottomDockShelfShape = value
        LauncherStorage.saveBottomDockShelfShape(context.applicationContext, value)
        if (value != DockShelfShape.NONE && bottomDockShelfHeight == 0) {
            bottomDockShelfHeight = 32
            LauncherStorage.saveBottomDockShelfHeight(context.applicationContext, bottomDockShelfHeight)
        }
    }

    fun updateBottomDockShelfColor(value: String) {
        bottomDockShelfColor = value
        LauncherStorage.saveBottomDockShelfColor(context.applicationContext, value)
    }

    fun updateBottomDockShelfOpacity(value: Int) {
        bottomDockShelfOpacity = value.coerceIn(0, 100)
        LauncherStorage.saveBottomDockShelfOpacity(context.applicationContext, bottomDockShelfOpacity)
    }

    fun updateBottomDockFolderCreationDelay(value: Int) {
        bottomDockFolderCreationDelay = value.coerceIn(300, 2000)
        LauncherStorage.saveBottomDockFolderCreationDelay(
            context.applicationContext,
            bottomDockFolderCreationDelay
        )
    }

    fun updateWidgetPickerMode(value: WidgetPickerDisplayMode) {
        widgetPickerMode = value
        LauncherStorage.saveWidgetPickerMode(context.applicationContext, value)
    }

    fun updateIgnoreGesturesOnWidgets(enabled: Boolean) {
        ignoreGesturesOnWidgets = enabled
        LauncherStorage.saveIgnoreGesturesOnWidgets(context.applicationContext, enabled)
    }

    fun updateSwipeDownAction(action: DesktopGestureAction) {
        swipeDownAction = action
        LauncherStorage.saveSwipeDownAction(context.applicationContext, action)
    }

    fun updateSwipeUpAction(action: DesktopGestureAction) {
        swipeUpAction = action
        LauncherStorage.saveSwipeUpAction(context.applicationContext, action)
    }

    fun updateDoubleTapAction(action: DesktopGestureAction) {
        doubleTapAction = action
        LauncherStorage.saveDoubleTapAction(context.applicationContext, action)
    }

    fun updateSideDockPosition(position: SideDockPosition) {
        sideDockPosition = position
        LauncherStorage.saveSideDockPosition(context.applicationContext, position)
    }

    fun updateSideDockOpacity(opacity: Int) {
        sideDockOpacity = opacity.coerceIn(0, 100)
        LauncherStorage.saveSideDockOpacity(context.applicationContext, sideDockOpacity)
    }

    fun updateDefaultWorkspacePage(page: Int) {
        defaultWorkspacePage = page.coerceIn(0, workspaceCount - 1)
        LauncherStorage.saveDefaultWorkspacePage(context.applicationContext, defaultWorkspacePage)
    }

    fun updateWidgetEditBarPosition(position: WidgetEditBarPosition) {
        widgetEditBarPosition = position
        LauncherStorage.saveWidgetEditBarPosition(context.applicationContext, position)
    }

    fun updateAppSortMode(mode: AppSortMode) {
        appSortMode = mode
        LauncherStorage.saveAppSortMode(context.applicationContext, mode)
        if (mode == AppSortMode.LAST_USED && !hasUsageAccess(context)) {
            Toast.makeText(context, l10n(R.string.ui_usage_access_is_required), Toast.LENGTH_LONG).show()
        }
    }

    fun updateAppDrawerReorderHold(value: Int) {
        appDrawerReorderHold = value.coerceIn(100, 900)
        LauncherStorage.saveAppDrawerReorderHold(context.applicationContext, appDrawerReorderHold)
    }

    fun updateFolderFanSpeed(value: Int) {
        folderFanSpeed = value.coerceIn(15, 100)
        LauncherStorage.saveFolderFanSpeed(context.applicationContext, folderFanSpeed)
    }

    fun updateFolderFanDecay(value: Int) {
        folderFanDecay = value.coerceIn(0, 100)
        LauncherStorage.saveFolderFanDecay(context.applicationContext, folderFanDecay)
    }

    fun updateFolderFanCloseMode(value: FolderFanCloseMode) {
        folderFanCloseMode = value
        LauncherStorage.saveFolderFanCloseMode(context.applicationContext, value)
    }

    fun updateFolderFanIconSize(value: Int) {
        folderFanIconSize = value.coerceIn(24, 96)
        LauncherStorage.saveFolderFanIconSize(context.applicationContext, folderFanIconSize)
    }

    fun updateFolderFanSpacing(value: Int) {
        folderFanSpacing = value.coerceIn(48, 260)
        LauncherStorage.saveFolderFanSpacing(context.applicationContext, folderFanSpacing)
    }

    fun updateFolderReorderLineColor(value: String) {
        folderReorderLineColor = normalizedColorHex(value) ?: folderReorderLineColor
        LauncherStorage.saveFolderReorderLineColor(context.applicationContext, folderReorderLineColor)
    }

    fun updateFolderReorderLineThickness(value: Int) {
        folderReorderLineThickness = value.coerceIn(1, 12)
        LauncherStorage.saveFolderReorderLineThickness(
            context.applicationContext,
            folderReorderLineThickness
        )
    }

    fun updateWallpaperMode(value: LauncherWallpaperMode) {
        wallpaperMode = value
        LauncherStorage.saveWallpaperMode(context.applicationContext, value)
    }

    fun updateWallpaperScrolling(value: Boolean) {
        wallpaperScrolling = value
        LauncherStorage.saveWallpaperScrolling(context.applicationContext, value)
    }

    fun updateWallpaperDynamic(value: Boolean) {
        wallpaperDynamic = value
        LauncherStorage.saveWallpaperDynamic(context.applicationContext, value)
    }

    fun updateWallpaperBackground(value: String) {
        wallpaperBackground = value
        LauncherStorage.saveWallpaperBackground(context.applicationContext, value)
    }

    fun updateWallpaperCircleA(value: String) {
        wallpaperCircleA = value
        LauncherStorage.saveWallpaperCircleA(context.applicationContext, value)
    }

    fun updateWallpaperCircleB(value: String) {
        wallpaperCircleB = value
        LauncherStorage.saveWallpaperCircleB(context.applicationContext, value)
    }

    fun toggleHiddenApp(component: String, hidden: Boolean) {
        hiddenApps = if (hidden) hiddenApps + component else hiddenApps - component
        LauncherStorage.saveHiddenApps(context.applicationContext, hiddenApps)
    }

    fun saveAppCustomization(component: String, value: AppCustomization) {
        val normalized = value.copy(label = value.label?.trim()?.takeIf { it.isNotBlank() })
        val isEmpty = normalized.label == null && normalized.iconUri == null &&
            normalized.iconPackPackage == null && normalized.iconPackDrawable == null
        appCustomizations = if (isEmpty) appCustomizations - component
        else appCustomizations + (component to normalized)
        LauncherStorage.saveAppCustomizations(context.applicationContext, appCustomizations)
        reloadKey += 1
    }

    fun updateEnvironmentEffects(value: EnvironmentEffectsConfig) {
        environmentEffects = value
        LauncherStorage.saveEnvironmentEffects(context.applicationContext, value)
    }

    fun updateLauncherInterfaceStyle(value: LauncherInterfaceStyle) {
        launcherInterfaceStyle = value.copy(
            opacityPercent = value.opacityPercent.coerceIn(0, 100),
            blurDp = value.blurDp.coerceIn(0, 48)
        )
        LauncherStorage.saveLauncherInterfaceStyle(context.applicationContext, launcherInterfaceStyle)
    }

    fun updateTextShadow(value: TextShadowSettings) {
        textShadowSettings = value
        LauncherStorage.saveTextShadowSettings(context.applicationContext, value)
    }

    fun updateWallpaperCircleCount(value: Int) {
        wallpaperCircleCount = value.coerceIn(0, 12)
        LauncherStorage.saveWallpaperCircleCount(context.applicationContext, wallpaperCircleCount)
    }

    fun reorderDrawerApp(sourceIndex: Int, targetIndex: Int) {
        val current = sortedApps.map { it.componentName.flattenToString() }.toMutableList()
        if (sourceIndex !in current.indices || targetIndex !in current.indices || sourceIndex == targetIndex) return
        val item = current.removeAt(sourceIndex)
        current.add(targetIndex, item)
        appDrawerCustomOrder = current
        LauncherStorage.saveAppDrawerCustomOrder(context.applicationContext, current)
        updateAppSortMode(AppSortMode.CUSTOM)
    }

    fun openAppInfo(app: AppItem) {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:${app.packageName}"))
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        runCatching { context.startActivity(intent) }
            .onFailure { Toast.makeText(context, l10n(R.string.ui_failed_to_open_application_information), Toast.LENGTH_SHORT).show() }
    }

    fun openAppInfo(shortcut: WorkspaceShortcut) {
        if (shortcut.intentAction != null) {
            launchShortcut(shortcut)
            return
        }
        val component = ComponentName.unflattenFromString(shortcut.componentName)
        val packageName = component?.packageName
        if (packageName.isNullOrBlank()) {
            Toast.makeText(context, l10n(R.string.ui_failed_to_identify_application), Toast.LENGTH_SHORT).show()
            return
        }
        val intent = Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.parse("package:$packageName")
        ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        runCatching { context.startActivity(intent) }
            .onFailure {
                Toast.makeText(context, l10n(R.string.ui_failed_to_open_application_information), Toast.LENGTH_SHORT).show()
            }
    }

    fun requestNotificationShade() {
        if (!LauncherAccessibilityService.openNotificationShade()) {
            showAccessibilityPrompt = true
        }
    }

    fun performDesktopGesture(action: DesktopGestureAction) {
        when (action) {
            DesktopGestureAction.NONE -> Unit
            DesktopGestureAction.NOTIFICATION_SHADE -> requestNotificationShade()
            DesktopGestureAction.SIDE_DOCK -> {
                sideDockDeleteMode = false
                sideDockOpen = true
            }
            DesktopGestureAction.APP_DRAWER -> {
                pendingPlacement = null
                appPickerMode = false
                sideDockAppPickerMode = false
                sideDockOpen = false
                drawerOpen = true
            }
            DesktopGestureAction.LAUNCHER_SETTINGS -> {
                settingsStartPage = LauncherSettingsPage.ROOT
                showWorkspaceSettings = true
            }
            DesktopGestureAction.SYSTEM_SETTINGS -> runCatching {
                context.startActivity(Intent(Settings.ACTION_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            }
            DesktopGestureAction.LOCK_SCREEN -> {
                if (!LauncherAccessibilityService.lockScreen()) {
                    showAccessibilityPrompt = true
                }
            }
            DesktopGestureAction.WORKSPACE_OVERVIEW -> showWorkspaceOverview = true
        }
    }

    fun openStaticWallpaperPicker() {
        updateWallpaperMode(LauncherWallpaperMode.SYSTEM)
        val intent = Intent(Intent.ACTION_SET_WALLPAPER).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        runCatching { context.startActivity(intent) }
            .onFailure { Toast.makeText(context, l10n(R.string.ui_failed_to_open_wallpaper_picker), Toast.LENGTH_SHORT).show() }
    }

    fun openLiveWallpaperPicker() {
        updateWallpaperMode(LauncherWallpaperMode.SYSTEM)
        val intent = Intent(WallpaperManager.ACTION_LIVE_WALLPAPER_CHOOSER)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        runCatching { context.startActivity(intent) }
            .onFailure { Toast.makeText(context, l10n(R.string.ui_live_wallpapers_are_unavailable), Toast.LENGTH_SHORT).show() }
    }

    val phoneIntent = remember { Intent(Intent.ACTION_DIAL, Uri.parse("tel:")) }
    val filesIntent = remember {
        Intent.makeMainSelectorActivity(Intent.ACTION_MAIN, Intent.CATEGORY_APP_FILES)
    }
    val filesFallbackIntent = remember {
        Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
        }
    }
    val mapsIntent = remember { Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=")) }
    val messagesIntent = remember { mainMessagingIntent(context) }

    val anyDialogOpen = showWorkspaceSettings || shortcutMenuTarget != null ||
        showAccessibilityPrompt || workspaceMenuPage != null || addToWorkspacePage != null ||
        settingsShortcutPage != null || widgetPickerPage != null || folderDialogTarget != null || sideDockFolderDialogTarget != null ||
        bottomDockFolderDialogTarget != null || showWorkspaceOverview ||
        showSortDialog || widgetEditingId != null || showSideDockAddDialog ||
        showSideDockSettingsPicker || showSideDockLauncherActionPicker ||
        showBottomDockAddDialog || showBottomDockSettingsPicker || showBottomDockLauncherActionPicker ||
        launcherActionWorkspacePage != null || sideDockMenuTarget != null || bottomDockMenuTarget != null || editingApp != null ||
        iconPackPickerApp != null || newAppEmblemIconPackPickerVisible || pendingUninstallApp != null
    val shadeGestureEnabled = !drawerOpen && !sideDockOpen && pendingPlacement == null &&
        !shortcutDragActive && !bottomDockDragActive && !anyDialogOpen

    // JVM bytecode size: keep extension construction out of LauncherHome itself.
    val settingsExtensions = object {
        fun build(): LauncherSettingsExtensions = LauncherSettingsExtensions(
        appLaunchDelay = if (interactionExtras.linkLaunchDelayToAnimation) appLaunchAnimationDuration else appLaunchDelay,
        appLaunchAnimationDuration = appLaunchAnimationDuration,
        appDragAnimationEffect = interactionExtras.dragAnimationEffect,
        appDragAnimationDuration = interactionExtras.dragAnimationDuration,
        linkLaunchDelayToAnimation = interactionExtras.linkLaunchDelayToAnimation,
        appDrawerDragOverviewMode = appDrawerDragOverviewMode,
        appDrawerCrossPageDragMode = interactionExtras.crossPageDragMode,
        appDrawerPagePreviewMode = interactionExtras.pagePreviewMode,
        appDrawerDragPageHover = appDrawerDragPageHover,
        appDrawerQuickAction = appDrawerQuickAction,
        appDrawerWorkspaceEdgeSwipeEnabled = interactionExtras.workspaceEdgeSwipeEnabled,
        newAppCutoffMillis = interactionExtras.newAppCutoffMillis,
        newAppEmblemShape = newAppEmblemShape,
        newAppEmblemSize = newAppEmblemSize,
        newAppEmblemColor = newAppEmblemColor,
        newAppEmblemColorB = newAppEmblemColorB,
        newAppEmblemGradient = newAppEmblemGradient,
        newAppEmblemX = newAppEmblemX,
        newAppEmblemY = newAppEmblemY,
        newAppEmblemImageUri = newAppEmblemImageUri,
        pinchAction = pinchAction,
        multiSwipeActions = multiSwipeActions,
        bottomDockCapacity = bottomDockCapacity,
        keepUninstalledAppShortcuts = keepUninstalledAppShortcuts,
        onAppLaunchDelayChange = ::updateAppLaunchDelay,
        onAppLaunchAnimationDurationChange = ::updateAppLaunchAnimationDuration,
        onAppDragAnimationEffectChange = interactionExtras::updateDragAnimationEffect,
        onAppDragAnimationDurationChange = interactionExtras::updateDragAnimationDuration,
        onLinkLaunchDelayToAnimationChange = { value ->
            interactionExtras.updateLinkLaunchDelayToAnimation(value)
            if (value) updateAppLaunchDelay(appLaunchAnimationDuration)
        },
        onAppDrawerDragOverviewModeChange = ::updateAppDrawerDragOverviewMode,
        onAppDrawerCrossPageDragModeChange = interactionExtras::updateCrossPageDragMode,
        onAppDrawerPagePreviewModeChange = interactionExtras::updatePagePreviewMode,
        onAppDrawerDragPageHoverChange = ::updateAppDrawerDragPageHover,
        onAppDrawerQuickActionChange = ::updateAppDrawerQuickAction,
        onAppDrawerWorkspaceEdgeSwipeEnabledChange = interactionExtras::updateWorkspaceEdgeSwipeEnabled,
        onNewAppCutoffChange = interactionExtras::setNewAppCutoff,
        onNewAppEmblemShapeChange = { value ->
            newAppEmblemShape = value
            LauncherStorage.saveNewAppEmblemShape(context.applicationContext, value)
        },
        onNewAppEmblemSizeChange = { value ->
            newAppEmblemSize = value.coerceIn(4, 25)
            LauncherStorage.saveNewAppEmblemSize(context.applicationContext, value)
        },
        onNewAppEmblemColorChange = { value ->
            newAppEmblemColor = value
            LauncherStorage.saveNewAppEmblemColor(context.applicationContext, value)
        },
        onNewAppEmblemColorBChange = { value ->
            newAppEmblemColorB = value
            LauncherStorage.saveNewAppEmblemColorB(context.applicationContext, value)
        },
        onNewAppEmblemGradientChange = { value ->
            newAppEmblemGradient = value
            LauncherStorage.saveNewAppEmblemGradient(context.applicationContext, value)
        },
        onNewAppEmblemXChange = { value ->
            newAppEmblemX = value.coerceIn(0, 100)
            LauncherStorage.saveNewAppEmblemX(context.applicationContext, value)
        },
        onNewAppEmblemYChange = { value ->
            newAppEmblemY = value.coerceIn(0, 100)
            LauncherStorage.saveNewAppEmblemY(context.applicationContext, value)
        },
        onChooseNewAppEmblemImage = { newAppEmblemImagePicker.launch(arrayOf("image/*")) },
        onClearNewAppEmblemImage = {
            newAppEmblemImageUri = null
            LauncherStorage.saveNewAppEmblemImageUri(context.applicationContext, null)
        },
        onChooseNewAppEmblemIconPack = {
            showWorkspaceSettings = false
            newAppEmblemIconPackPickerVisible = true
        },
        onClearNewAppEmblemIconPack = {
            LauncherStorage.saveNewAppEmblemPackIcon(context.applicationContext, null, null)
        },
        onPinchActionChange = { action ->
            pinchAction = action
            LauncherStorage.savePinchAction(context.applicationContext, action)
        },
        onMultiSwipeActionChange = { fingers, direction, action ->
            multiSwipeActions = multiSwipeActions + ((fingers to direction) to action)
            LauncherStorage.saveMultiSwipeAction(context.applicationContext, fingers, direction, action)
        },
        onBottomDockCapacityChange = ::updateBottomDockCapacity,
        onKeepUninstalledAppShortcutsChange = { value ->
            keepUninstalledAppShortcuts = value
            LauncherStorage.saveKeepUninstalledAppShortcuts(context.applicationContext, value)
            if (!value) {
                // Also honour the new policy for placeholders that already existed
                // before the option was changed. Check package installation itself
                // rather than the transient LauncherApps list, so an icon is not
                // removed merely because its launcher Activity is being replaced.
                val referencedPackages = buildSet {
                    shortcuts.filter { it.intentAction == null }.forEach {
                        componentPackageName(it.componentName)?.let(::add)
                    }
                    folders.flatMap { it.componentNames }.forEach {
                        componentPackageName(it)?.let(::add)
                    }
                    sideDockEntries.forEach { entry ->
                        if (entry.isFolder) entry.componentNames.forEach { componentPackageName(it)?.let(::add) }
                        else if (entry.intentAction == null) componentPackageName(entry.componentName)?.let(::add)
                    }
                    bottomDockEntries.forEach { entry ->
                        if (entry.isFolder) entry.componentNames.forEach { componentPackageName(it)?.let(::add) }
                        else if (entry.intentAction == null) componentPackageName(entry.componentName)?.let(::add)
                    }
                }
                referencedPackages
                    .filterNot { isPackageInstalled(context.packageManager, it) }
                    .forEach(::removePackageReferences)
            }
        }
        )
    }.build()

    LauncherHomeSection(launcherDialogStyle) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .onGloballyPositioned { desktopRootOrigin = it.positionInRoot() }
            .workspaceOverviewPinch(
                enabled = !drawerOpen && !sideDockOpen && !anyDialogOpen &&
                    pendingPlacement == null && widgetEditingId == null,
                onPinch = {
                    if (pinchAction != DesktopGestureAction.NONE) performDesktopGesture(pinchAction)
                }
            )
            .pointerInput(activeEnvironmentEffects.options(EnvironmentEffectType.FISH).enabled) {
                if (!activeEnvironmentEffects.options(EnvironmentEffectType.FISH).enabled) {
                    return@pointerInput
                }
                val touchSlop = viewConfiguration.touchSlop
                awaitEachGesture {
                    val down = awaitFirstDown(
                        requireUnconsumed = false,
                        pass = PointerEventPass.Initial
                    )
                    val start = down.position
                    var latest = start
                    var moved = false
                    var pressed = true
                    var completedTap = false
                    do {
                        val event = awaitPointerEvent(pass = PointerEventPass.Final)
                        val change = event.changes.firstOrNull { it.id == down.id } ?: break
                        latest = change.position
                        if ((latest - start).getDistance() > touchSlop) moved = true
                        pressed = change.pressed
                        if (!pressed) completedTap = true
                    } while (pressed)
                    if (completedTap && !moved) fishTapEvents.tryEmit(latest)
                }
            }
            .pointerInput(sideDockOpen, sideDockPosition) {
                if (!sideDockOpen) return@pointerInput
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
                    val dockWidth = 116.dp.toPx()
                    val insideDock = if (sideDockPosition == SideDockPosition.LEFT) {
                        down.position.x <= dockWidth
                    } else {
                        down.position.x >= size.width - dockWidth
                    }
                    if (!insideDock) {
                        // A down outside the dock can still be the start of a
                        // workspace icon drag into the dock. Close it only on a tap.
                        var moved = false
                        var released = false
                        while (!released) {
                            val event = awaitPointerEvent(pass = PointerEventPass.Initial)
                            val change = event.changes.firstOrNull { it.id == down.id } ?: break
                            if ((change.position - down.position).getDistance() > viewConfiguration.touchSlop)
                                moved = true
                            released = !change.pressed
                        }
                        if (released && !moved && !shortcutDragActive) sideDockOpen = false
                    }
                }
            }
            .pointerInput(shadeGestureEnabled, swipeDownAction, swipeUpAction, multiSwipeActions, ignoreGesturesOnWidgets, currentWorkspacePage) {
                if (!shadeGestureEnabled) return@pointerInput
                val minimumSwipeDistance = 88.dp.toPx()
                val touchSlop = viewConfiguration.touchSlop

                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
                    val start = down.position
                    if (ignoreGesturesOnWidgets && widgets.any { widget ->
                        widget.page == currentWorkspacePage &&
                            workspaceWidgetBounds[widget.id]?.contains(start + desktopRootOrigin) == true
                    }) return@awaitEachGesture
                    // Dock-originated upward drags belong to the dock from the first
                    // pointer event, before its long-press drag state is established.
                    if (start.y >= size.height - 132.dp.toPx()) return@awaitEachGesture
                    var end = start
                    var verticalSwipeLocked = false
                    var rejectedAsHorizontal = false
                    var direction = 0
                    val starts = mutableMapOf(down.id.value to start)
                    val ends = mutableMapOf(down.id.value to start)
                    var maxFingerCount = 1
                    var initialPinchDistance: Float? = null
                    var pinchDetected = false
                    try {
                        while (true) {
                            val event = awaitPointerEvent(pass = PointerEventPass.Initial)
                            val change = event.changes.firstOrNull { it.id == down.id } ?: break
                            event.changes.forEach { sample ->
                                starts.putIfAbsent(sample.id.value, sample.position)
                                ends[sample.id.value] = sample.position
                            }
                            val active = event.changes.filter { it.pressed }
                            maxFingerCount = maxOf(maxFingerCount, active.size.coerceAtMost(3))
                            if (active.size >= 2) {
                                verticalSwipeLocked = false
                                shadeSwipeInProgress = false
                                val distance = (active[0].position - active[1].position).getDistance()
                                val initial = initialPinchDistance ?: distance.also { initialPinchDistance = it }
                                if (initial > 1f && abs(distance / initial - 1f) > 0.14f) pinchDetected = true
                            }
                            end = change.position
                            val deltaX = end.x - start.x
                            val deltaY = end.y - start.y
                            if (maxFingerCount == 1 && !verticalSwipeLocked && !rejectedAsHorizontal &&
                                (abs(deltaX) >= touchSlop || abs(deltaY) >= touchSlop)
                            ) {
                                if (abs(deltaY) > abs(deltaX) * 1.15f) {
                                    direction = if (deltaY > 0f) 1 else -1
                                    val action = if (direction > 0) swipeDownAction else swipeUpAction
                                    if (action != DesktopGestureAction.NONE) {
                                        verticalSwipeLocked = true
                                        shadeSwipeInProgress = true
                                    } else rejectedAsHorizontal = true
                                } else if (abs(deltaX) > abs(deltaY)) {
                                    rejectedAsHorizontal = true
                                }
                            }
                            if (verticalSwipeLocked || (maxFingerCount > 1 && !pinchDetected)) event.changes.forEach { it.consume() }
                            if (event.changes.none { it.pressed }) break
                        }
                        if (maxFingerCount in 2..3 && !pinchDetected) {
                            val participating = starts.keys.take(maxFingerCount)
                            val startCentroid = participating.mapNotNull(starts::get).reduce { a, b -> a + b } / participating.size.toFloat()
                            val endCentroid = participating.mapNotNull(ends::get).reduce { a, b -> a + b } / participating.size.toFloat()
                            val delta = endCentroid - startCentroid
                            if (delta.getDistance() >= minimumSwipeDistance) {
                                val swipeDirection = if (abs(delta.x) > abs(delta.y)) {
                                    if (delta.x > 0f) SwipeDirection.RIGHT else SwipeDirection.LEFT
                                } else {
                                    if (delta.y > 0f) SwipeDirection.DOWN else SwipeDirection.UP
                                }
                                multiSwipeActions[maxFingerCount to swipeDirection]
                                    ?.takeIf { it != DesktopGestureAction.NONE }
                                    ?.let(::performDesktopGesture)
                            }
                        } else if (maxFingerCount == 1 && verticalSwipeLocked) {
                            val deltaX = end.x - start.x
                            val deltaY = end.y - start.y
                            if (abs(deltaY) >= minimumSwipeDistance && abs(deltaY) > abs(deltaX) * 1.15f) {
                                performDesktopGesture(if (direction > 0) swipeDownAction else swipeUpAction)
                            }
                        }
                    } finally {
                        shadeSwipeInProgress = false
                    }
                }
            }
    ) {
        EnvironmentDistortionSurface(
            config = activeEnvironmentEffects,
            modifier = Modifier.fillMaxSize(),
            onShaderUnavailable = {
                val current = environmentEffects.options(EnvironmentEffectType.DISTORTION)
                if (current.enabled) {
                    updateEnvironmentEffects(
                        environmentEffects.update(
                            EnvironmentEffectType.DISTORTION,
                            current.copy(
                                enabled = false
                            )
                        )
                    )
                    Toast.makeText(
                        context,
                        l10n(R.string.ui_distortion_shader_unavailable),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        ) {
        LauncherHomeBoxSection {
        Box(Modifier.fillMaxSize().zIndex(0f)) {
            LauncherInterfaceBackground(
                style = launcherInterfaceStyle,
                modifier = Modifier.fillMaxSize().zIndex(1f)
            )
            if (wallpaperMode == LauncherWallpaperMode.ANIMATED) {
                AnimatedLauncherBackground(
                    dynamicBackground = wallpaperDynamic,
                    backgroundColorHex = wallpaperBackground,
                    circleColorAHex = wallpaperCircleA,
                    circleColorBHex = wallpaperCircleB,
                    circleCount = wallpaperCircleCount,
                    modifier = Modifier.fillMaxSize().zIndex(0f)
                )
            }
            if (activeEnvironmentEffects.layerMode == EnvironmentEffectsLayerMode.BEHIND) {
                EnvironmentEffectsLayer(
                    config = activeEnvironmentEffects,
                    fishTapEvents = fishTapEvents,
                    modifier = Modifier.fillMaxSize().zIndex(2f)
                )
            }
        }

        DesktopPager(
            modifier = Modifier.zIndex(if (shortcutDragActive) 55f else 10f),
            workspaceCount = workspaceCount,
            loopEnabled = workspaceLoop,
            pagerState = pagerState,
            shortcuts = shortcuts,
            widgets = widgets,
            notes = notes,
            onEditNote = { },
            onMoveNote = ::moveNote,
            onResizeNote = ::resizeNote,
            onDeleteNote = ::deleteNote,
            widgetActionChainsByWidgetId = widgetActionChains
                .flatMap { chain -> chain.widgetIds.map { widgetId -> widgetId to chain } }
                .toMap(),
            folders = folders,
            widgetInfoRefreshKey = reloadKey,
            appWidgetHost = activity.launcherAppWidgetHost,
            appWidgetManager = activity.launcherAppWidgetManager,
            appsByComponent = appsByComponent,
            transitionEffect = pageTransitionEffect,
            meshSnapshotAfterIdle = meshSnapshotAfterIdle,
            minimumEffectDurationMs = pageEffectTiming.workspaceMs,
            editingLocked = workspaceEditingLocked,
            onEditingLocked = ::notifyDesktopEditingLocked,
            placementActive = pendingPlacement != null || widgetEditingId != null,
            shortcutDragActive = shortcutDragActive,
            widgetEditingId = widgetEditingId,
            widgetEditBarPosition = widgetEditBarPosition,
            shadeSwipeInProgress = shadeSwipeInProgress,
            sideDockOpen = sideDockOpen,
            sideDockPosition = sideDockPosition,
            folderFanDisplay = folderFanDisplay,
            folderFanSpeedPercent = folderFanSpeed,
            folderFanDecayPercent = folderFanDecay,
            folderFanCloseMode = folderFanCloseMode,
            folderFanIconSizeDp = folderFanIconSize,
            folderFanSpacingDp = folderFanSpacing,
            fanItemWidthDp = appDrawerItemWidth,
            fanItemHeightDp = appDrawerItemHeight,
            fanFrameShape = appDrawerFrameShape,
            fanFrameColorHex = appDrawerFrameColor,
            fanFrameOpacityPercent = appDrawerFrameOpacity,
            fanTextColorHex = appDrawerFrameTextColor,
            textShadowSettings = textShadowSettings,
            workspaceTextColor = parseInterfaceColor(launcherInterfaceStyle.textColorHex),
            onFolderFanDisplayChange = { folderFanDisplay = it },
            onWidgetBoundsChange = { id, bounds -> workspaceWidgetBounds[id] = bounds },
            onShortcutDragStateChange = { active ->
                shortcutDragActive = active
                if (!active) {
                    workspaceShortcutDragPointerInRoot = null
                    workspaceBottomDockInsertionIndex = null
                    workspaceBottomDockHoverTargetId = null
                    workspaceSideDockInsertionIndex = null
                    workspaceSideDockHoverTargetId = null
                }
            },
            onShortcutDragPositionChange = { pointer ->
                workspaceShortcutDragPointerInRoot = pointer
                if (pointer == null) {
                    workspaceBottomDockInsertionIndex = null
                    workspaceBottomDockHoverTargetId = null
                    workspaceSideDockInsertionIndex = null
                    workspaceSideDockHoverTargetId = null
                }
            },
            onFolderDragStateChange = { workspaceFolderDragActive = it },
            onLaunchShortcut = ::launchShortcut,
            onMoveShortcut = ::moveShortcut,
            onDeleteShortcut = ::deleteShortcut,
            onShowShortcutMenu = { shortcutMenuTarget = it },
            onCreateFolderFromShortcuts = ::createFolderFromShortcuts,
            onMoveShortcutIntoFolder = ::moveShortcutIntoFolder,
            onMoveShortcutToSideDock = ::moveWorkspaceShortcutToSideDock,
            onMoveShortcutToBottomDock = ::moveWorkspaceShortcutToBottomDock,
            onLockedDockDrop = { pointerInRoot ->
                val overBottomDock = bottomDockVisible &&
                    bottomDockBoundsInRoot.width > 0f && bottomDockBoundsInRoot.height > 0f &&
                    bottomDockBoundsInRoot.contains(pointerInRoot)
                val overSideDock = sideDockOpen && !drawerOpen &&
                    sideDockBoundsInRoot.width > 0f && sideDockBoundsInRoot.height > 0f &&
                    sideDockBoundsInRoot.contains(pointerInRoot)
                if ((bottomDockEditingLocked && overBottomDock) || (sideDockEditingLocked && overSideDock)) {
                    notifyDockEditingLocked()
                    true
                } else false
            },
            onMoveFolder = ::moveFolder,
            onMoveFolderToBottomDock = ::moveWorkspaceFolderToBottomDock,
            onMoveFolderToSideDock = ::moveWorkspaceFolderToSideDock,
            onDeleteFolder = ::deleteFolder,
            onOpenFolder = { folder -> runAfterLaunchActivation { folderDialogTarget = folder } },
            onWidgetEditStart = { widgetEditingId = it },
            onWidgetUpdate = ::updateWidget,
            onWidgetDelete = ::deleteWidget,
            onWidgetRepair = { widget ->
                ComponentName.unflattenFromString(widget.provider)?.let { provider ->
                    activity.requestBindWidget(provider, widget.page, widget.id)
                }
            },
            onWidgetEditDone = { widgetEditingId = null },
            onWidgetActionChain = ::performWidgetActionChain,
            onDoubleTapEmpty = {
                if (doubleTapAction != DesktopGestureAction.NONE) performDesktopGesture(doubleTapAction)
            },
            onTapEmpty = {
                folderFanDisplay = null
                sideDockDeleteMode = false
                sideDockOpen = false
                widgetEditingId = null
            },
            onLongPressEmpty = { page, x, y ->
                if (widgetEditingId == null) {
                    workspaceInsertionAnchor = Triple(page, x, y)
                    workspaceMenuPage = page
                }
            },
            onWorkspaceBoundsOnScreen = { bounds ->
                if (workspaceBoundsOnScreen != bounds) workspaceBoundsOnScreen = bounds
            }
        )

        if (bottomDockPageIndicatorVisible) {
            val indicatorBottomPaddingDp = bottomDockPageIndicatorBottomPaddingDp(
                dockVisible = bottomDockVisible,
                iconVerticalOffsetDp = bottomDockIconVerticalOffset,
                shelfVerticalOffsetDp = bottomDockShelfVerticalOffset,
                shelfHeightDp = bottomDockShelfHeight,
                shelfShape = bottomDockShelfShape,
                indicatorGapDp = bottomDockPageIndicatorGap
            )
            PageIndicator(
                pageCount = workspaceCount,
                currentPage = currentWorkspacePage,
                indicatorColor = parseInterfaceColor(launcherInterfaceStyle.textColorHex),
                onClick = if (bottomDockIndicatorToggle) ({ bottomDockVisible = !bottomDockVisible }) else null,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .zIndex(140f)
                    .navigationBarsPadding()
                    .padding(bottom = indicatorBottomPaddingDp.dp)
            )
        }

        if (showWorkspaceSettings && settingsWidgetCheckmarkPreview) {
            Box(
                modifier = Modifier
                    .align(if (widgetEditBarPosition == WidgetEditBarPosition.CHECK_LEFT)
                        Alignment.TopStart else Alignment.TopEnd)
                    .statusBarsPadding()
                    .padding(top = 22.dp, start = 12.dp, end = 12.dp)
                    .zIndex(135f)
            ) {
                // Settings-only preview: do not start widget edit mode.
                WidgetDoneButton(onDone = {})
            }
        }
        pendingPlacement?.takeUnless { workspaceEditingLocked }?.let { app ->
            ShortcutPlacementOverlay(
                app = app,
                currentPage = currentWorkspacePage,
                pageCount = workspaceCount,
                onCancel = { pendingPlacement = null },
                onDrop = { x, y -> addShortcut(app, currentWorkspacePage, x, y) }
            )
        }
        }

        LauncherHomeBoxSection {
        AnimatedVisibility(
            visible = drawerOpen,
            enter = fadeIn(tween(180)) + slideInVertically(
                animationSpec = spring(dampingRatio = 0.86f, stiffness = 380f),
                initialOffsetY = { it / 5 }
            ),
            exit = fadeOut(tween(140)) + slideOutVertically(
                animationSpec = tween(180),
                targetOffsetY = { it / 5 }
            ),
            modifier = Modifier.zIndex(20f)
        ) {
            val appDrawerLayoutHeightDp = appDrawerScreenHeightDp
                .takeIf { it > 0 }
                ?: configuration.screenHeightDp
            val effectiveAppDrawerItemWidth = if (appDrawerFillAvailable) {
                appDrawerMaxItemWidth(configuration.screenWidthDp, appDrawerColumns)
            } else {
                appDrawerItemWidth
            }
            val effectiveAppDrawerItemHeight = if (appDrawerFillAvailable) {
                appDrawerMaxItemHeight(
                    appDrawerLayoutHeightDp,
                    appDrawerRows,
                    appDrawerBottomBarHeightDp
                )
            } else {
                appDrawerItemHeight
            }
            val workspaceEdgeSwipeController = remember(scope, pagerState) {
                WorkspaceEdgeSwipeController(scope, pagerState)
            }
            AppDrawer(
                apps = filteredApps,
                totalAppCount = apps.size,
                query = query,
                searchVisible = appDrawerSearchVisible,
                visuallyHidden = drawerVisuallyHidden,
                reorderHoldMillis = appDrawerReorderHold,
                onQueryChange = { query = it },
                isLoading = isLoading,
                loadError = loadError,
                onRetry = { reloadKey += 1 },
                onLaunchApp = { app ->
                    if (bottomDockAppPickerMode) {
                        addBottomDockApp(app)
                        drawerOpen = false
                        bottomDockAppPickerMode = false
                        query = ""
                    } else if (sideDockAppPickerMode) {
                        addSideDockApp(app)
                        drawerOpen = false
                        sideDockAppPickerMode = false
                        query = ""
                        sideDockOpen = true
                    } else if (appPickerMode) {
                        val chosenPage = workspaceInsertionAnchor?.first ?: currentWorkspacePage
                        val position = freeWorkspacePosition(chosenPage)
                        addShortcut(app, chosenPage, position.first, position.second)
                        drawerOpen = false
                        appPickerMode = false
                        sideDockAppPickerMode = false
                        bottomDockAppPickerMode = false
                        query = ""
                    } else launchApp(app)
                },
                onStartPlacement = { app ->
                    if (bottomDockAppPickerMode) {
                        addBottomDockApp(app)
                        drawerOpen = false
                        bottomDockAppPickerMode = false
                        query = ""
                    } else if (sideDockAppPickerMode) {
                        addSideDockApp(app)
                        drawerOpen = false
                        sideDockAppPickerMode = false
                        query = ""
                        sideDockOpen = true
                    } else if (!workspaceEditingLocked) {
                        pendingPlacement = app
                        drawerOpen = false
                        appPickerMode = false
                        sideDockAppPickerMode = false
                        bottomDockAppPickerMode = false
                        query = ""
                    }
                },
                onDragStart = { app, position, sourceIndex ->
                    reorderHoverSerial += 1
                    appDrawerDragState = AppDrawerDragState(app, position, sourceIndex)
                },
                onDragMove = { position ->
                    val state = appDrawerDragState
                    if (state != null) {
                        val densityValue = density.density
                        val screenWidthPx = configuration.screenWidthDp * densityValue
                        val screenHeightPx = configuration.screenHeightDp * densityValue
                        val topHeight = 110f * densityValue
                        val viewHeightPx = launcherView.height.toFloat().takeIf { it > 0f }
                            ?: screenHeightPx
                        val navigationBarHeightPx = launcherView.rootWindowInsets
                            ?.getInsets(android.view.WindowInsets.Type.navigationBars())
                            ?.bottom
                            ?.toFloat()
                            ?: 0f
                        val workspaceTargetNow = position.y >
                            viewHeightPx - navigationBarHeightPx - 96f * densityValue
                        val workspaceTransferActive = state.overWorkspace || workspaceTargetNow
                        val third = screenWidthPx / 3f
                        val overDelete = !workspaceTransferActive && position.y < topHeight && position.x < third
                        val overEdit = !workspaceTransferActive && position.y < topHeight && position.x in third..(third * 2f)
                        val overInfo = !workspaceTransferActive && position.y < topHeight && position.x >= third * 2f
                        val pageEdgePx = appDrawerEdgeSwitchDp.coerceIn(16, 140) * densityValue
                        val inPageEdgeZone = appDrawerMode != AppDrawerMode.VERTICAL_LIST &&
                            position.y >= topHeight &&
                            position.y <= viewHeightPx - navigationBarHeightPx - 104f * densityValue &&
                            (position.x < pageEdgePx || position.x > screenWidthPx - pageEdgePx)
                        val overSpecialTarget = overDelete || overEdit || overInfo || workspaceTransferActive || inPageEdgeZone

                        var hoverTarget = state.hoverTargetIndex
                        var previewTarget = state.previewTargetIndex
                        var shouldSchedulePreview = false
                        if (!overSpecialTarget && appDrawerMode != AppDrawerMode.VERTICAL_LIST && sortedApps.isNotEmpty()) {
                            val target = appDrawerTargetIndex(
                                position = position,
                                page = appDrawerPage,
                                columns = appDrawerColumns,
                                rows = appDrawerRows,
                                screenWidthPx = screenWidthPx,
                                screenHeightPx = screenHeightPx,
                                bottomBarHeightDp = appDrawerBottomBarHeightDp,
                                density = densityValue,
                                appCount = sortedApps.size
                            )
                            val movementSinceLastEvent = (position - state.position).getDistance()
                            shouldSchedulePreview = target != null && target != state.sourceIndex &&
                                (target != state.hoverTargetIndex || movementSinceLastEvent > 1.5f)
                            hoverTarget = target
                        } else if (overSpecialTarget) {
                            hoverTarget = null
                            previewTarget = null
                            reorderHoverSerial += 1
                        }

                        appDrawerDragState = state.copy(
                            position = position,
                            overDelete = overDelete,
                            overEdit = overEdit,
                            overInfo = overInfo,
                            overWorkspace = workspaceTransferActive,
                            hoverTargetIndex = hoverTarget,
                            previewTargetIndex = previewTarget
                        )
                        if (workspaceTransferActive) drawerVisuallyHidden = true

                        if (shouldSchedulePreview && hoverTarget != null) {
                            reorderHoverSerial += 1
                            val serial = reorderHoverSerial
                            val target = hoverTarget
                            val anchorPosition = position
                            scope.launch {
                                delay(260)
                                val current = appDrawerDragState
                                if (serial == reorderHoverSerial &&
                                    current != null &&
                                    !current.overDelete && !current.overEdit && !current.overInfo && !current.overWorkspace &&
                                    current.hoverTargetIndex == target &&
                                    (current.position - anchorPosition).getDistance() < with(density) { 14.dp.toPx() }
                                ) {
                                    appDrawerDragState = current.copy(previewTargetIndex = target)
                                }
                            }
                        }
                    }
                },
                onDragEnd = { position ->
                    reorderHoverSerial += 1
                    val state = appDrawerDragState
                    appDrawerDragState = null
                    if (state != null) {
                        // Locked docks deliberately do not offer an insertion preview.
                        // Use the real dock bounds to recognize a drop anyway.
                        val measuredDock = bottomDockBoundsInRoot
                        val bottomDockHit = bottomDockVisible && (
                            if (measuredDock.width > 0f && measuredDock.height > 0f) {
                                measuredDock.contains(position)
                            } else {
                                // First drag may end before AnimatedVisibility measured the dock.
                                val rootHeight = launcherView.height.toFloat().takeIf { it > 0f }
                                    ?: configuration.screenHeightDp * density.density
                                val navigationInset = launcherView.rootWindowInsets
                                    ?.getInsets(android.view.WindowInsets.Type.navigationBars())
                                    ?.bottom?.toFloat() ?: 0f
                                position.y >= rootHeight - navigationInset - with(density) { 122.dp.toPx() }
                            }
                        )
                        when {
                            state.overDelete -> {
                                drawerVisuallyHidden = false
                                pendingUninstallApp = state.app
                            }
                            state.overEdit -> {
                                drawerVisuallyHidden = false
                                editingApp = state.app
                            }
                            state.overInfo -> {
                                drawerVisuallyHidden = false
                                openAppInfo(state.app)
                            }
                            state.overWorkspace || (drawerVisuallyHidden && bottomDockEditingLocked && bottomDockHit) -> {
                                // The app drawer pointer is in launcher-root coordinates.
                                // Match WorkspacePage's real 78x96 dp movable rectangle,
                                // not the drawer grid or an approximate screen-height formula.
                                val densityValue = density.density
                                val bounds = workspaceBoundsOnScreen
                                val rootToScreen = IntArray(2).also { launcherView.getLocationOnScreen(it) }
                                val screenPosition = Offset(
                                    position.x + rootToScreen[0],
                                    position.y + rootToScreen[1]
                                )
                                val (x, y) = workspaceFolderDropFractions(
                                    screenPosition.x, screenPosition.y,
                                    if (bounds.width > 1f) bounds.left else rootToScreen[0].toFloat(),
                                    if (bounds.height > 1f) bounds.top else rootToScreen[1].toFloat(),
                                    if (bounds.width > 1f) bounds.width else configuration.screenWidthDp * densityValue,
                                    if (bounds.height > 1f) bounds.height else configuration.screenHeightDp * densityValue,
                                    densityValue
                                )
                                val bottomDockZone = bottomDockHit
                                val bottomDockDrop = bottomDockZone &&
                                    (workspaceBottomDockInsertionIndex != null || workspaceBottomDockHoverTargetId != null)
                                // Create a folder ONLY when the dragged icon's centre is
                                // actually inside an existing icon's real desktop bounds.
                                // A broad 11%/13% tolerance incorrectly interpreted free
                                // space between nearby icons as a folder drop.
                                val iconWidth = 78f * densityValue
                                val iconHeight = 96f * densityValue
                                val maxX = ((if (bounds.width > 1f) bounds.width else configuration.screenWidthDp * densityValue) - iconWidth).coerceAtLeast(0f)
                                val maxY = ((if (bounds.height > 1f) bounds.height else configuration.screenHeightDp * densityValue) - 108f * densityValue - iconHeight).coerceAtLeast(0f)
                                val pointerX = x * maxX + iconWidth * 0.5f
                                val pointerY = y * maxY + iconHeight * 0.5f
                                // Require clear centre-on-centre overlap for folder creation.
                                // Nearby or partially touching icons can still be freely placed.
                                fun overlapsActualIcon(targetX: Float, targetY: Float): Boolean =
                                    kotlin.math.abs(pointerX - (targetX * maxX + iconWidth * 0.5f)) <= iconWidth * 0.32f &&
                                        kotlin.math.abs(pointerY - (targetY * maxY + iconHeight * 0.5f)) <= iconHeight * 0.32f
                                val folderTarget = folders.firstOrNull { folder ->
                                    folder.page == currentWorkspacePage &&
                                        overlapsActualIcon(folder.xFraction, folder.yFraction)
                                }
                                val shortcutTarget = if (folderTarget == null) {
                                    shortcuts.firstOrNull { shortcut ->
                                        shortcut.page == currentWorkspacePage &&
                                            shortcut.intentAction == null &&
                                            overlapsActualIcon(shortcut.xFraction, shortcut.yFraction)
                                    }
                                } else null
                                when {
                                    bottomDockZone && bottomDockEditingLocked -> notifyDockEditingLocked()
                                    bottomDockDrop && addDraggedAppToBottomDock(state.app) -> Unit
                                    folderTarget != null -> addAppToFolder(folderTarget.id, state.app)
                                    shortcutTarget != null -> createFolderFromAppAndShortcut(state.app, shortcutTarget.id, currentWorkspacePage)
                                    else -> addShortcut(state.app, currentWorkspacePage, x, y)
                                }
                                drawerOpen = false
                                appPickerMode = false
                                sideDockAppPickerMode = false
                                bottomDockAppPickerMode = false
                                appDrawerSearchVisible = false
                                query = ""
                            }
                            appDrawerMode != AppDrawerMode.VERTICAL_LIST -> {
                                val target = state.hoverTargetIndex ?: state.previewTargetIndex
                                if (target != null && target != state.sourceIndex && sortedApps.isNotEmpty()) {
                                    reorderDrawerApp(
                                        state.sourceIndex,
                                        target.coerceIn(0, sortedApps.lastIndex.coerceAtLeast(0))
                                    )
                                }
                                drawerVisuallyHidden = false
                            }
                            else -> drawerVisuallyHidden = false
                        }
                    } else {
                        drawerVisuallyHidden = false
                    }
                },
                mode = appDrawerMode,
                pickForPlacement = appPickerMode,
                pickForSideDock = sideDockAppPickerMode,
                pickForBottomDock = bottomDockAppPickerMode,
                savedPage = appDrawerPage,
                loopEnabled = appDrawerLoop,
                pageTransitionEffect = appDrawerPageTransitionEffect,
                minimumEffectDurationMs = pageEffectTiming.drawerMs,
                iconEffect = appDrawerIconEffect,
                gridColumns = appDrawerColumns,
                gridRows = appDrawerRows,
                itemWidthDp = effectiveAppDrawerItemWidth,
                itemHeightDp = effectiveAppDrawerItemHeight,
                frameShape = appDrawerFrameShape,
                frameColorHex = appDrawerFrameColor,
                frameOpacityPercent = appDrawerFrameOpacity,
                frameTextColorHex = appDrawerFrameTextColor,
                backgroundMode = appDrawerBackgroundMode,
                backgroundOpacityPercent = appDrawerBackgroundOpacity,
                backgroundColorAHex = appDrawerBackgroundColorA,
                backgroundColorBHex = appDrawerBackgroundColorB,
                backgroundTextColorHex = appDrawerBackgroundTextColor,
                backgroundImage = appDrawerBackgroundImage,
                activeDragPosition = appDrawerDragState?.position,
                activeDragSourceIndex = appDrawerDragState?.sourceIndex,
                activeDragPreviewIndex = appDrawerDragState?.previewTargetIndex,
                edgeSwitchDp = appDrawerEdgeSwitchDp,
                dragOverviewMode = appDrawerDragOverviewMode,
                crossPageDragMode = interactionExtras.crossPageDragMode,
                pagePreviewMode = interactionExtras.pagePreviewMode,
                dragPageHoverMillis = appDrawerDragPageHover,
                edgePreviewVisible = appDrawerEdgePreviewVisible,
                textShadowSettings = textShadowSettings,
                bottomBarHeightDp = appDrawerBottomBarHeightDp,
                overviewRequestSerial = appDrawerOverviewRequestSerial,
                onOverviewRequestConsumed = { appDrawerOverviewRequestSerial = 0 },
                workspaceEdgeSwipeEnabled = interactionExtras.workspaceEdgeSwipeEnabled,
                onWorkspaceEdgeSwipeStart = {
                    workspaceEdgeSwipeController.start()
                },
                onWorkspaceEdgeSwipeDrag = { fingerDeltaX ->
                    workspaceEdgeSwipeController.dragBy(fingerDeltaX)
                },
                onWorkspaceEdgeSwipeEnd = { velocityXDpPerSecond ->
                    workspaceEdgeSwipeController.finish(
                        velocityXDpPerSecond = velocityXDpPerSecond,
                        workspaceCount = workspaceCount,
                        loopEnabled = workspaceLoop
                    )
                },
                onPageChanged = { page ->
                    updateAppDrawerPage(page)
                    val state = appDrawerDragState
                    if (state != null && appDrawerMode != AppDrawerMode.VERTICAL_LIST &&
                        !state.overDelete && !state.overEdit && !state.overInfo && !state.overWorkspace
                    ) {
                        val densityValue = density.density
                        val target = appDrawerTargetIndex(
                            position = state.position,
                            page = page,
                            columns = appDrawerColumns,
                            rows = appDrawerRows,
                            screenWidthPx = configuration.screenWidthDp * densityValue,
                            screenHeightPx = configuration.screenHeightDp * densityValue,
                            bottomBarHeightDp = appDrawerBottomBarHeightDp,
                            density = densityValue,
                            appCount = sortedApps.size
                        )
                        appDrawerDragState = state.copy(
                            hoverTargetIndex = target,
                            previewTargetIndex = null
                        )
                    }
                }
            )
        }

        AnimatedVisibility(
            visible = (!drawerOpen || drawerVisuallyHidden) && bottomDockVisible,
            enter = fadeIn(tween(140)) + slideInVertically(initialOffsetY = { it }),
            exit = fadeOut(tween(100)) + slideOutVertically(targetOffsetY = { it }),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .zIndex(35f)
                .onGloballyPositioned { bottomDockBoundsInRoot = it.boundsInRoot() }
        ) {
            PagedBottomDock(
                entries = bottomDockEntries,
                capacity = bottomDockCapacity,
                pageCount = bottomDockPageCount,
                loopEnabled = bottomDockLoop,
                selectedPage = bottomDockSelectedPage,
                showAddButton = bottomDockAddButtonEnabled,
                alignment = bottomDockAlignment,
                appsByComponent = appsByComponent,
                backgroundColorHex = bottomDockBackgroundColor,
                backgroundOpacityPercent = bottomDockBackgroundOpacity,
                textColorHex = bottomDockTextColor,
                shelfHeightDp = bottomDockShelfHeight,
                iconVerticalOffsetDp = bottomDockIconVerticalOffset,
                shelfVerticalOffsetDp = bottomDockShelfVerticalOffset,
                showLabels = bottomDockLabelsVisible,
                shelfShape = bottomDockShelfShape,
                shelfColorHex = bottomDockShelfColor,
                shelfOpacityPercent = bottomDockShelfOpacity,
                textShadowSettings = textShadowSettings,
                reorderLineColor = folderReorderLineColor,
                reorderLineThicknessDp = folderReorderLineThickness,
                folderCreationDelayMillis = bottomDockFolderCreationDelay,
                externalDragPointerInRoot = workspaceShortcutDragPointerInRoot
                    ?: appDrawerDragState?.takeIf { it.overWorkspace }?.position,
                editingLocked = bottomDockEditingLocked,
                onLockedDrag = ::notifyDockEditingLocked,
                onPageChanged = { bottomDockSelectedPage = it },
                onLaunch = ::launchBottomDockEntry,
                onAddApp = {
                    showBottomDockAddDialog = true
                },
                onRemove = ::removeBottomDockEntry,
                onReorder = ::reorderBottomDockEntryOnPage,
                onCreateFolder = ::createBottomDockFolder,
                onAddToFolder = ::addBottomDockEntryToFolder,
                onMoveToPage = ::moveBottomDockEntryToPage,
                onMoveToWorkspace = ::moveBottomDockEntryToWorkspace,
                onContextMenu = { bottomDockMenuTarget = it },
                onFolderFanStart = ::startBottomDockFolderFan,
                onFolderFanDrag = ::dragBottomDockFolderFan,
                onFolderFanEnd = ::finishBottomDockFolderFan,
                onExternalInsertionTargetChange = { workspaceBottomDockInsertionIndex = it },
                onExternalHoverTargetChange = { workspaceBottomDockHoverTargetId = it },
                onInteractionActiveChange = { bottomDockDragActive = it },
                isOverDeleteTarget = { point ->
                    val bounds = bottomDockDeleteBoundsInRoot
                    bounds.width > 0f && bounds.height > 0f && bounds.contains(point)
                },
                onDeleteDragStateChange = { active, over ->
                    bottomDockDeleteDragActive = active
                    bottomDockDeleteDragOver = over
                }
            )
        }

        if (bottomDockDeleteDragActive) {
            Box(
                modifier = Modifier.fillMaxSize().zIndex(95f),
                contentAlignment = Alignment.TopCenter
            ) {
                DeleteTarget(
                    overTrash = bottomDockDeleteDragOver,
                    modifier = Modifier.onGloballyPositioned {
                        bottomDockDeleteBoundsInRoot = it.boundsInRoot()
                    }
                )
            }
        }

        AnimatedVisibility(
            visible = drawerOpen && !drawerVisuallyHidden,
            enter = fadeIn(tween(140)) + slideInVertically(initialOffsetY = { it }),
            exit = fadeOut(tween(100)) + slideOutVertically(targetOffsetY = { it }),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .zIndex(30f)
        ) {
            AppDrawerBottomBar(
                sortMode = appSortMode,
                searchActive = appDrawerSearchVisible,
                onSort = { showSortDialog = true },
                onSearch = {
                    appDrawerSearchVisible = !appDrawerSearchVisible
                    if (!appDrawerSearchVisible) query = ""
                },
                onClose = {
                    drawerOpen = false
                    appPickerMode = false
                    sideDockAppPickerMode = false
                    bottomDockAppPickerMode = false
                    appDrawerSearchVisible = false
                    appDrawerDragState = null
                    drawerVisuallyHidden = false
                    query = ""
                },
                quickAction = appDrawerQuickAction,
                onQuickAction = {
                    when (appDrawerQuickAction.kind) {
                        AppDrawerQuickActionKind.PAGE_OVERVIEW -> {
                            if (appDrawerMode == AppDrawerMode.VERTICAL_LIST) {
                                Toast.makeText(
                                    context,
                                    l10n(R.string.ui_page_overview_available_in_paged_modes),
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                appDrawerOverviewRequestSerial += 1
                            }
                        }
                        AppDrawerQuickActionKind.LAUNCHER_SETTINGS -> {
                            settingsStartPage = LauncherSettingsPage.ROOT
                            showWorkspaceSettings = true
                        }
                        AppDrawerQuickActionKind.APP_DRAWER_SETTINGS -> {
                            settingsStartPage = runCatching {
                                LauncherSettingsPage.valueOf(appDrawerQuickAction.value)
                            }.getOrDefault(LauncherSettingsPage.APP_DRAWER)
                            showWorkspaceSettings = true
                        }
                        AppDrawerQuickActionKind.SYSTEM_SETTINGS -> {
                            runCatching {
                                context.startActivity(
                                    Intent(appDrawerQuickAction.value).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                )
                            }.onFailure {
                                Toast.makeText(
                                    context,
                                    l10n(R.string.ui_unable_to_open_selected_settings),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                },
                onSettings = {
                    settingsStartPage = LauncherSettingsPage.APP_DRAWER
                    showWorkspaceSettings = true
                },
                onHeightChanged = { heightPx ->
                    appDrawerBottomBarHeightDp =
                        (heightPx / density.density).roundToInt().coerceAtLeast(58)
                }
            )
        }

        appDrawerDragState?.let { drag ->
            AppDrawerDragOverlay(
                state = drag,
                showTargets = !drag.overWorkspace,
                modifier = Modifier.fillMaxSize().zIndex(90f)
            )
        }
        if (activeEnvironmentEffects.layerMode == EnvironmentEffectsLayerMode.ABOVE) {
            EnvironmentEffectsLayer(
                config = activeEnvironmentEffects,
                fishTapEvents = fishTapEvents,
                modifier = Modifier.fillMaxSize().zIndex(120f)
            )
        }
        }

        LauncherHomeBoxSection {
        AnimatedVisibility(
            visible = (sideDockOpen || settingsSideDockPreview) && !drawerOpen,
            enter = fadeIn(tween(120)) + slideInHorizontally(
                initialOffsetX = { if (sideDockPosition == SideDockPosition.LEFT) -it else it }
            ),
            exit = fadeOut(tween(100)) + slideOutHorizontally(
                targetOffsetX = { if (sideDockPosition == SideDockPosition.LEFT) -it else it }
            ),
            modifier = Modifier
                .align(if (sideDockPosition == SideDockPosition.LEFT) Alignment.CenterStart else Alignment.CenterEnd)
                 .zIndex(if (workspaceFolderDragActive) 60f else 40f)
                .onGloballyPositioned { sideDockBoundsInRoot = it.boundsInRoot() }
        ) {
            SideDockPanel(
                position = sideDockPosition,
                opacityPercent = sideDockOpacity,
                textColorHex = sideDockTextColor,
                textShadowSettings = textShadowSettings,
                entries = sideDockEntries,
                appsByComponent = appsByComponent,
                deleteMode = sideDockDeleteMode && !sideDockEditingLocked,
                editingLocked = sideDockEditingLocked,
                onLockedDrag = ::notifyDockEditingLocked,
                externalDragPointerInRoot = workspaceShortcutDragPointerInRoot,
                externalDragIsFolder = workspaceFolderDragActive,
                onFolderFanStart = ::startSideDockFolderFan,
                onFolderFanDrag = ::dragSideDockFolderFan,
                onFolderFanEnd = ::finishSideDockFolderFan,
                folderCreationDelayMillis = bottomDockFolderCreationDelay,
                onAdd = { showSideDockAddDialog = true },
                onToggleDeleteMode = { if (sideDockEditingLocked) notifyDockEditingLocked() else sideDockDeleteMode = !sideDockDeleteMode },
                onLaunch = ::launchSideDockEntry,
                onRemove = ::removeSideDockEntry,
                onReorder = ::reorderSideDockEntry,
                onCreateFolder = ::createSideDockFolder,
                onAddToFolder = ::addSideDockEntryToFolder,
                onMoveToWorkspace = { entry, point ->
                    val (x, y) = workspaceDropFractions(point)
                    moveSideDockEntryToWorkspace(entry, x, y)
                },
                onContextMenu = { sideDockMenuTarget = it },
                onExternalInsertionTargetChange = { workspaceSideDockInsertionIndex = it },
                onExternalHoverTargetChange = { workspaceSideDockHoverTargetId = it },
                onEmptyTap = { sideDockDeleteMode = false }
            )
        }
        }
        }
        sideDockFolderFanDisplay?.let { fanState ->
            val entry = sideDockEntries.firstOrNull { it.id == fanState.folderId && it.isFolder }
            if (entry != null) {
                val folder = WorkspaceFolder(
                    id = entry.id,
                    label = entry.label,
                    page = currentWorkspacePage,
                    xFraction = 0.5f,
                    yFraction = 0.45f,
                    componentNames = entry.componentNames,
                    shape = entry.folderShape,
                    colorHex = entry.folderColorHex,
                    opacityPercent = entry.folderOpacityPercent,
                    customIconUri = entry.customIconUri,
                    customIconPackPackage = entry.customIconPackPackage,
                    customIconPackDrawable = entry.customIconPackDrawable
                )
                FolderFanOverlay(
                    folder = folder,
                    appsByComponent = appsByComponent,
                    trail = fanState.trail,
                    headDistancePx = fanState.headDistancePx,
                    itemWidthDp = appDrawerItemWidth,
                    itemHeightDp = appDrawerItemHeight,
                    frameShape = appDrawerFrameShape,
                    frameColorHex = appDrawerFrameColor,
                    frameOpacityPercent = appDrawerFrameOpacity,
                    textColorHex = appDrawerFrameTextColor,
                    textShadowSettings = textShadowSettings,
                    closeMode = folderFanCloseMode,
                    iconSizeDp = folderFanIconSize,
                    iconSpacingDp = folderFanSpacing,
                    onDismiss = { sideDockFolderFanDisplay = null },
                    onLaunch = { component ->
                        if (launcherActionOptionForComponent(component) != null) {
                            performLauncherAction(component)
                        } else {
                            appsByComponent[component]?.let(::launchApp)
                        }
                    },
                    modifier = Modifier.fillMaxSize().zIndex(190f)
                )
            }
        }
        bottomDockFolderFanDisplay?.let { fanState ->
            val entry = bottomDockEntries.firstOrNull { it.id == fanState.folderId && it.isFolder }
            if (entry != null) {
                val folder = WorkspaceFolder(
                    id = entry.id,
                    label = entry.label,
                    page = currentWorkspacePage,
                    xFraction = 0.5f,
                    yFraction = 0.72f,
                    componentNames = entry.componentNames,
                    shape = entry.folderShape,
                    colorHex = entry.folderColorHex,
                    opacityPercent = entry.folderOpacityPercent,
                    customIconUri = entry.customIconUri,
                    customIconPackPackage = entry.customIconPackPackage,
                    customIconPackDrawable = entry.customIconPackDrawable
                )
                FolderFanOverlay(
                    folder = folder,
                    appsByComponent = appsByComponent,
                    trail = fanState.trail,
                    headDistancePx = fanState.headDistancePx,
                    itemWidthDp = appDrawerItemWidth,
                    itemHeightDp = appDrawerItemHeight,
                    frameShape = appDrawerFrameShape,
                    frameColorHex = appDrawerFrameColor,
                    frameOpacityPercent = appDrawerFrameOpacity,
                    textColorHex = appDrawerFrameTextColor,
                    textShadowSettings = textShadowSettings,
                    closeMode = folderFanCloseMode,
                    iconSizeDp = folderFanIconSize,
                    iconSpacingDp = folderFanSpacing,
                    onDismiss = { bottomDockFolderFanDisplay = null },
                    onLaunch = { component ->
                if (launcherActionOptionForComponent(component) != null) performLauncherAction(component)
                else appsByComponent[component]?.let(::launchApp)
            },
                    modifier = Modifier.fillMaxSize().zIndex(190f)
                )
            }
        }
        if (showWorkspaceOverview) {
            Box(Modifier.fillMaxSize().zIndex(200f)) {
                WorkspaceOverviewScreen(
                    pageCount = workspaceCount,
                    currentPage = currentWorkspacePage,
                    defaultPage = defaultWorkspacePage,
                    items = workspaceOverviewItems,
                    editingEnabled = !workspaceEditingLocked &&
                        advancedPreferences.workspaceOverviewMode == WorkspaceOverviewMode.EDITING,
                    onClose = { showWorkspaceOverview = false },
                    onOpenPage = ::openWorkspacePageFromOverview,
                    onSetDefaultPage = ::updateDefaultWorkspacePage,
                    onAddPage = ::addWorkspacePageFromOverview,
                    onDeletePage = ::deleteWorkspacePage,
                    onReorderPage = ::reorderWorkspacePage,
                    onMoveItem = ::moveOverviewItem
                )
            }
        }
        if (liveWorkspaceEffectOriginal != null || liveAppDrawerEffectOriginal != null) {
            Card(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
                    .padding(10.dp)
                    .zIndex(210f),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xF01B1F2B))
            ) {
                Row(
                    Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        l10n(R.string.ui_effect_preview),
                        modifier = Modifier.weight(1f),
                        fontWeight = FontWeight.Bold
                    )
                    TextButton(onClick = {
                        liveWorkspaceEffectOriginal?.let { pageTransitionEffect = it }
                        liveAppDrawerEffectOriginal?.let { appDrawerPageTransitionEffect = it }
                        val menuPreview = liveAppDrawerEffectOriginal != null
                        liveWorkspaceEffectOriginal = null
                        liveAppDrawerEffectOriginal = null
                        drawerOpen = false
                        settingsStartPage = if (menuPreview) LauncherSettingsPage.APP_DRAWER_PAGE_EFFECTS
                            else LauncherSettingsPage.WORKSPACE_EFFECTS
                        showWorkspaceSettings = true
                    }) { Text(l10n(R.string.ui_cancel)) }
                    Button(onClick = {
                        val menuPreview = liveAppDrawerEffectOriginal != null
                        if (menuPreview) updateAppDrawerPageTransition(appDrawerPageTransitionEffect)
                        else updatePageTransition(pageTransitionEffect)
                        liveWorkspaceEffectOriginal = null
                        liveAppDrawerEffectOriginal = null
                        drawerOpen = false
                        settingsStartPage = if (menuPreview) LauncherSettingsPage.APP_DRAWER_PAGE_EFFECTS
                            else LauncherSettingsPage.WORKSPACE_EFFECTS
                        showWorkspaceSettings = true
                    }) { Text(l10n(R.string.ui_apply)) }
                }
            }
        }
    }
    }

    val widgetActionWidgetDescriptors = buildWidgetActionWidgetDescriptors(activity, widgets)

    // Keep the large settings argument/callback list out of a captured Compose lambda.
    // Capturing every LauncherHome MutableState in one lambda exceeds the JVM 64 KB
    // method limit (LauncherHome$lambda$789 / MethodTooLargeException).
    // Keep the large settings UI/callback generation in its own JVM method.
    // An anonymous object avoids a single Compose lambda capturing hundreds of states.
    val settingsDialogRenderer = object {
        @Composable
        fun Render() {
            if (showWorkspaceSettings) {
                WorkspaceSettingsDialog(
                    dialogStyle = launcherDialogStyle,
                    initialPage = settingsStartPage,
                    allApps = apps,
                    hiddenApps = hiddenApps,
                    workspaceCount = workspaceCount,
                    workspaceLoop = workspaceLoop,
                    defaultWorkspacePage = defaultWorkspacePage,
                    transitionEffect = pageTransitionEffect,
                    meshSnapshotAfterIdle = meshSnapshotAfterIdle,
                    pageEffectTiming = pageEffectTiming,
                    workspaceOverviewMode = advancedPreferences.workspaceOverviewMode,
                    effectPreviewMode = advancedPreferences.effectPreviewMode,
                    appDrawerMode = appDrawerMode,
                    appDrawerLoop = appDrawerLoop,
                    appDrawerPageTransitionEffect = appDrawerPageTransitionEffect,
                    appDrawerIconEffect = appDrawerIconEffect,
                    appLaunchAnimationEffect = appLaunchAnimationEffect,
                    extensions = settingsExtensions,
                    appDrawerColumns = appDrawerColumns,
                    appDrawerRows = appDrawerRows,
                    appDrawerItemWidth = appDrawerItemWidth,
                    appDrawerItemHeight = appDrawerItemHeight,
                    appDrawerIndependentSize = appDrawerIndependentSize,
                    appDrawerFillAvailable = appDrawerFillAvailable,
                    appDrawerScreenHeightDp = appDrawerScreenHeightDp,
                    appDrawerFrameShape = appDrawerFrameShape,
                    appDrawerFrameColor = appDrawerFrameColor,
                    appDrawerFrameOpacity = appDrawerFrameOpacity,
                    appDrawerFrameTextColor = appDrawerFrameTextColor,
                    appDrawerEdgeSwitchDp = appDrawerEdgeSwitchDp,
                    appDrawerBackgroundMode = appDrawerBackgroundMode,
                    appDrawerBackgroundOpacity = appDrawerBackgroundOpacity,
                    appDrawerBackgroundColorA = appDrawerBackgroundColorA,
                    appDrawerBackgroundColorB = appDrawerBackgroundColorB,
                    appDrawerBackgroundTextColor = appDrawerBackgroundTextColor,
                    appDrawerBackgroundImageUri = appDrawerBackgroundImageUri,
                    widgetPickerMode = widgetPickerMode,
                    widgetActionWidgets = widgetActionWidgetDescriptors,
                    widgetActionChains = widgetActionChains,
                    ignoreGesturesOnWidgets = ignoreGesturesOnWidgets,
                    swipeDownAction = swipeDownAction,
                    swipeUpAction = swipeUpAction,
                    doubleTapAction = doubleTapAction,
                    widgetEditBarPosition = widgetEditBarPosition,
                    sideDockPosition = sideDockPosition,
                    sideDockOpacity = sideDockOpacity,
                    sideDockTextColor = sideDockTextColor,
                    bottomDockPageCount = bottomDockPageCount,
                    bottomDockLoop = bottomDockLoop,
                    bottomDockIndicatorToggle = bottomDockIndicatorToggle,
                    bottomDockAddButtonEnabled = bottomDockAddButtonEnabled,
                    bottomDockAlignment = bottomDockAlignment,
                    bottomDockBackgroundColor = bottomDockBackgroundColor,
                    bottomDockBackgroundOpacity = bottomDockBackgroundOpacity,
                    bottomDockTextColor = bottomDockTextColor,
                    bottomDockShelfHeight = bottomDockShelfHeight,
                    bottomDockIconVerticalOffset = bottomDockIconVerticalOffset,
                    bottomDockShelfVerticalOffset = bottomDockShelfVerticalOffset,
                    bottomDockPageIndicatorVisible = bottomDockPageIndicatorVisible,
                    bottomDockPageIndicatorGap = bottomDockPageIndicatorGap,
                    bottomDockLabelsVisible = bottomDockLabelsVisible,
                    bottomDockShelfShape = bottomDockShelfShape,
                    bottomDockShelfColor = bottomDockShelfColor,
                    bottomDockShelfOpacity = bottomDockShelfOpacity,
                    bottomDockFolderCreationDelay = bottomDockFolderCreationDelay,
                    appDrawerReorderHold = appDrawerReorderHold,
                    folderFanSpeed = folderFanSpeed,
                    folderFanDecay = folderFanDecay,
                    folderFanCloseMode = folderFanCloseMode,
                    folderFanIconSize = folderFanIconSize,
                    folderFanSpacing = folderFanSpacing,
                    folderReorderLineColor = folderReorderLineColor,
                    folderReorderLineThickness = folderReorderLineThickness,
                    wallpaperMode = wallpaperMode,
                    wallpaperScrolling = wallpaperScrolling,
                    wallpaperDynamic = wallpaperDynamic,
                    wallpaperBackground = wallpaperBackground,
                    wallpaperCircleA = wallpaperCircleA,
                    wallpaperCircleB = wallpaperCircleB,
                    wallpaperCircleCount = wallpaperCircleCount,
                    textShadowSettings = textShadowSettings,
                    environmentEffects = environmentEffects,
                    launcherInterfaceStyle = launcherInterfaceStyle,
                    launcherDialogStyle = launcherDialogStyle,
                    onWorkspaceCountChange = ::updateWorkspaceCount,
                    onWorkspaceLoopChange = ::updateWorkspaceLoop,
                    onDefaultWorkspacePageChange = ::updateDefaultWorkspacePage,
                    onTransitionEffectChange = ::updatePageTransition,
                    onMeshSnapshotAfterIdleChange = ::updateMeshSnapshotAfterIdle,
                    onWorkspaceOverviewModeChange = { mode ->
                        advancedPreferences = advancedPreferences.copy(workspaceOverviewMode = mode)
                        AdvancedLauncherStorage.save(context.applicationContext, advancedPreferences)
                    },
                    onEffectPreviewModeChange = { mode ->
                        advancedPreferences = advancedPreferences.copy(effectPreviewMode = mode)
                        AdvancedLauncherStorage.save(context.applicationContext, advancedPreferences)
                    },
                    onOpenWorkspaceOverview = {
                        showWorkspaceSettings = false
                        showWorkspaceOverview = true
                    },
                    onPreviewWorkspaceEffectLive = { effect ->
                        liveWorkspaceEffectOriginal = pageTransitionEffect
                        pageTransitionEffect = effect
                        showWorkspaceSettings = false
                    },
                    onPreviewAppDrawerEffectLive = { effect ->
                        liveAppDrawerEffectOriginal = appDrawerPageTransitionEffect
                        appDrawerPageTransitionEffect = effect
                        showWorkspaceSettings = false
                        drawerOpen = true
                        appDrawerSearchVisible = false
                        query = ""
                    },
                    onAppDrawerModeChange = ::updateAppDrawerMode,
                    onAppDrawerLoopChange = ::updateAppDrawerLoop,
                    onAppDrawerPageTransitionChange = ::updateAppDrawerPageTransition,
                    onAppDrawerIconEffectChange = ::updateAppDrawerIconEffect,
                    onAppLaunchAnimationChange = ::updateAppLaunchAnimation,
                    onAppDrawerColumnsChange = ::updateAppDrawerColumns,
                    onAppDrawerRowsChange = ::updateAppDrawerRows,
                    onAppDrawerItemWidthChange = ::updateAppDrawerItemWidth,
                    onAppDrawerItemHeightChange = ::updateAppDrawerItemHeight,
                    onAppDrawerIndependentSizeChange = ::updateAppDrawerIndependentSize,
                    onAppDrawerFillAvailableChange = ::updateAppDrawerFillAvailable,
                    onAppDrawerScreenHeightDpChange = ::updateAppDrawerScreenHeightDp,
                    onAppDrawerFrameShapeChange = ::updateAppDrawerFrameShape,
                    onAppDrawerFrameColorChange = ::updateAppDrawerFrameColor,
                    onAppDrawerFrameOpacityChange = ::updateAppDrawerFrameOpacity,
                    onAppDrawerFrameTextColorChange = ::updateAppDrawerFrameTextColor,
                    onAppDrawerEdgeSwitchDpChange = ::updateAppDrawerEdgeSwitchDp,
                    onAppDrawerBackgroundModeChange = ::updateAppDrawerBackgroundMode,
                    onAppDrawerBackgroundOpacityChange = ::updateAppDrawerBackgroundOpacity,
                    onAppDrawerBackgroundColorAChange = ::updateAppDrawerBackgroundColorA,
                    onAppDrawerBackgroundColorBChange = ::updateAppDrawerBackgroundColorB,
                    onAppDrawerBackgroundTextColorChange = ::updateAppDrawerBackgroundTextColor,
                    onChooseAppDrawerImage = { drawerImagePicker.launch(arrayOf("image/*")) },
                    onWidgetPickerModeChange = ::updateWidgetPickerMode,
                    onWidgetActionChainSave = ::saveWidgetActionChain,
                    onWidgetActionChainDelete = ::deleteWidgetActionChain,
                    onIgnoreGesturesOnWidgetsChange = ::updateIgnoreGesturesOnWidgets,
                    onSwipeDownActionChange = ::updateSwipeDownAction,
                    onSwipeUpActionChange = ::updateSwipeUpAction,
                    onDoubleTapActionChange = ::updateDoubleTapAction,
                    onWidgetEditBarPositionChange = ::updateWidgetEditBarPosition,
                    onSideDockPositionChange = ::updateSideDockPosition,
                    onSideDockOpacityChange = ::updateSideDockOpacity,
                    onSideDockTextColorChange = ::updateSideDockTextColor,
                    onBottomDockPageCountChange = ::updateBottomDockPageCount,
                    onBottomDockLoopChange = ::updateBottomDockLoop,
                    onBottomDockIndicatorToggleChange = ::updateBottomDockIndicatorToggle,
                    onBottomDockAddButtonEnabledChange = ::updateBottomDockAddButtonEnabled,
                    onOpenBottomDockAdd = {
                        showWorkspaceSettings = false
                        showBottomDockAddDialog = true
                    },
                    onBottomDockAlignmentChange = ::updateBottomDockAlignment,
                    onBottomDockBackgroundColorChange = ::updateBottomDockBackgroundColor,
                    onBottomDockBackgroundOpacityChange = ::updateBottomDockBackgroundOpacity,
                    onBottomDockTextColorChange = ::updateBottomDockTextColor,
                    onBottomDockShelfHeightChange = ::updateBottomDockShelfHeight,
                    onBottomDockIconVerticalOffsetChange = ::updateBottomDockIconVerticalOffset,
                    onBottomDockShelfVerticalOffsetChange = ::updateBottomDockShelfVerticalOffset,
                    onBottomDockPageIndicatorVisibleChange = ::updateBottomDockPageIndicatorVisible,
                    onBottomDockPageIndicatorGapChange = ::updateBottomDockPageIndicatorGap,
                    onBottomDockLabelsVisibleChange = ::updateBottomDockLabelsVisible,
                    onBottomDockShelfShapeChange = ::updateBottomDockShelfShape,
                    onBottomDockShelfColorChange = ::updateBottomDockShelfColor,
                    onBottomDockShelfOpacityChange = ::updateBottomDockShelfOpacity,
                    onBottomDockFolderCreationDelayChange = ::updateBottomDockFolderCreationDelay,
                    onAppDrawerReorderHoldChange = ::updateAppDrawerReorderHold,
                    onHiddenAppChange = ::toggleHiddenApp,
                    onAppDrawerEdgePreviewChange = { appDrawerEdgePreviewVisible = it },
                    onTextShadowSettingsChange = ::updateTextShadow,
                    onEnvironmentEffectsChange = ::updateEnvironmentEffects,
                    onLauncherInterfaceStyleChange = ::updateLauncherInterfaceStyle,
                    onChooseLauncherInterfaceImage = { interfaceImagePicker.launch(arrayOf("image/*")) },
                    onLauncherDialogStyleChange = launcherDialogStyleBinding.onValueChange,
                    onChooseLauncherDialogImage = launcherDialogStyleBinding.onChooseImage,
                    onFolderFanSpeedChange = ::updateFolderFanSpeed,
                    onFolderFanDecayChange = ::updateFolderFanDecay,
                    onFolderFanCloseModeChange = ::updateFolderFanCloseMode,
                    onFolderFanIconSizeChange = ::updateFolderFanIconSize,
                    onFolderFanSpacingChange = ::updateFolderFanSpacing,
                    onFolderReorderLineColorChange = ::updateFolderReorderLineColor,
                    onFolderReorderLineThicknessChange = ::updateFolderReorderLineThickness,
                    onWallpaperModeChange = ::updateWallpaperMode,
                    onWallpaperScrollingChange = ::updateWallpaperScrolling,
                    onWallpaperDynamicChange = ::updateWallpaperDynamic,
                    onWallpaperBackgroundChange = ::updateWallpaperBackground,
                    onWallpaperCircleAChange = ::updateWallpaperCircleA,
                    onWallpaperCircleBChange = ::updateWallpaperCircleB,
                    onWallpaperCircleCountChange = ::updateWallpaperCircleCount,
                    onChooseStaticWallpaper = ::openStaticWallpaperPicker,
                    onChooseLiveWallpaper = ::openLiveWallpaperPicker,
                    onRestartLauncher = {
                        showWorkspaceSettings = false
                        LauncherDiagnostics.info("launcher restart requested from settings")
                        activity.recreate()
                    },
                    onClearWorkspaceItems = {
                        val ok = activity.clearWorkspaceItems()
                        if (ok) {
                            showWorkspaceSettings = false
                            activity.recreate()
                        } else {
                            Toast.makeText(context, l10n(R.string.ui_reset_failed), Toast.LENGTH_LONG).show()
                        }
                    },
                    onResetLauncherArea = { area ->
                        val ok = activity.resetLauncherArea(area)
                        if (ok) {
                            showWorkspaceSettings = false
                            activity.recreate()
                        } else {
                            Toast.makeText(context, l10n(R.string.ui_reset_failed), Toast.LENGTH_LONG).show()
                        }
                    },
                    onCreateBackup = { createSettingsBackup.launch(LauncherSettingsBackup.defaultFileName()) },
                    onRestoreBackup = { restoreSettingsBackup.launch(arrayOf("application/json", "text/json", "text/plain")) },
                    onSideDockSettingsPreviewChange = { settingsSideDockPreview = it },
                    onWidgetCheckmarkPreviewChange = { settingsWidgetCheckmarkPreview = it },
                    onDismiss = {
                        appDrawerEdgePreviewVisible = false
                        settingsSideDockPreview = false
                        settingsWidgetCheckmarkPreview = false
                        showWorkspaceSettings = false
                    }
                )
            }
        }
    }
    settingsDialogRenderer.Render()

    LauncherHomeSection(launcherDialogStyle) {
    shortcutMenuTarget?.let { shortcut ->
        ShortcutContextDialog(
            shortcut = shortcut,
            app = appsByComponent[shortcut.componentName],
            onDismiss = { shortcutMenuTarget = null },
            onRemove = {
                shortcutMenuTarget = null
                deleteShortcut(shortcut.id)
            },
            onEdit = {
                shortcutMenuTarget = null
                editingShortcut = shortcut
            },
            onShowInfo = {
                shortcutMenuTarget = null
                openAppInfo(shortcut)
            }
        )
    }

    folderDialogTarget?.let { folder ->
        FolderDialog(
            folder = folder,
            appsByComponent = appsByComponent,
            editingEnabled = !workspaceEditingLocked,
            extractionAllowed = !workspaceEditingLocked,
            onExtractionLocked = ::notifyDesktopEditingLocked,
            reorderLineColor = folderReorderLineColor,
            reorderLineThicknessDp = folderReorderLineThickness,
            onDismiss = { folderDialogTarget = null },
            onRename = { name -> renameFolder(folder.id, name) },
            onChooseIcon = {
                pendingFolderIconId = folder.id
                folderIconPicker.launch(arrayOf("image/*"))
            },
            onChooseIconPack = {
                folderDialogTarget = null
                folderIconPackPickerId = folder.id
            },
            onClearIcon = {
                updateFolderVisual(folder.id, folder.label, null, null, null)
                updateFolderAppearance(folder.id, FolderShape.DEFAULT, "#4F5872", 67)
            },
            onAppearanceChange = { shape, color, opacity ->
                updateFolderAppearance(folder.id, shape, color, opacity)
            },
            onLaunch = { component ->
                if (launcherActionOptionForComponent(component) != null) performLauncherAction(component)
                else appsByComponent[component]?.let(::launchApp)
            },
            onRemove = { component -> removeAppFromFolder(folder.id, component) },
            onReorder = { from, to -> reorderFolderApp(folder.id, from, to) },
            onMoveToWorkspace = { component, screenPosition ->
                val (x, y) = workspaceDropFractions(screenPosition)
                moveFolderAppToWorkspace(folder.id, component, x, y)
            },
            onDeleteFolder = { deleteFolder(folder.id) },
            deleteMessage = l10n(R.string.ui_delete_folder_from_desktop)
        )
    }

    bottomDockFolderDialogTarget?.takeIf { it.isFolder }?.let { entry ->
        val folder = WorkspaceFolder(
            id = entry.id,
            label = entry.label,
            page = currentWorkspacePage,
            xFraction = 0.5f,
            yFraction = 0.72f,
            componentNames = entry.componentNames,
            shape = entry.folderShape,
            colorHex = entry.folderColorHex,
            opacityPercent = entry.folderOpacityPercent,
            customIconUri = entry.customIconUri,
            customIconPackPackage = entry.customIconPackPackage,
            customIconPackDrawable = entry.customIconPackDrawable
        )
        FolderDialog(
            folder = folder,
            appsByComponent = appsByComponent,
            editingEnabled = !bottomDockEditingLocked,
            extractionAllowed = !bottomDockEditingLocked && !workspaceEditingLocked,
            onExtractionLocked = {
                if (bottomDockEditingLocked) notifyDockEditingLocked() else notifyDesktopEditingLocked()
            },
            reorderLineColor = folderReorderLineColor,
            reorderLineThicknessDp = folderReorderLineThickness,
            onDismiss = { bottomDockFolderDialogTarget = null },
            onRename = { name -> renameBottomDockFolder(entry.id, name) },
            onChooseIcon = {
                pendingFolderIconId = entry.id
                folderIconPicker.launch(arrayOf("image/*"))
            },
            onChooseIconPack = {
                bottomDockFolderDialogTarget = null
                folderIconPackPickerId = entry.id
            },
            onClearIcon = {
                updateBottomDockFolderVisual(entry.id, entry.label, null, null, null)
                updateBottomDockFolderAppearance(entry.id, FolderShape.DEFAULT, "#4F5872", 67)
            },
            onAppearanceChange = { shape, color, opacity ->
                updateBottomDockFolderAppearance(entry.id, shape, color, opacity)
            },
            onLaunch = { component ->
                if (launcherActionOptionForComponent(component) != null) performLauncherAction(component)
                else appsByComponent[component]?.let(::launchApp)
            },
            onRemove = { component -> removeAppFromBottomDockFolder(entry.id, component) },
            onReorder = { from, to -> reorderBottomDockFolderApp(entry.id, from, to) },
            onMoveToWorkspace = { component, screenPosition ->
                val (x, y) = workspaceDropFractions(screenPosition)
                moveBottomDockFolderAppToWorkspace(entry.id, component, x, y)
            },
            onDeleteFolder = {
                removeBottomDockEntry(entry.id)
                bottomDockFolderDialogTarget = null
            },
            deleteMessage = l10n(R.string.ui_delete_folder_from_bottom_dock)
        )
    }

    sideDockFolderDialogTarget?.takeIf { it.isFolder }?.let { entry ->
        val folder = WorkspaceFolder(
            id = entry.id, label = entry.label, page = currentWorkspacePage,
            xFraction = 0.5f, yFraction = 0.45f, componentNames = entry.componentNames,
            shape = entry.folderShape, colorHex = entry.folderColorHex,
            opacityPercent = entry.folderOpacityPercent,
            customIconUri = entry.customIconUri,
            customIconPackPackage = entry.customIconPackPackage,
            customIconPackDrawable = entry.customIconPackDrawable
        )
        FolderDialog(
            folder = folder,
            appsByComponent = appsByComponent,
            editingEnabled = !sideDockEditingLocked,
            extractionAllowed = !sideDockEditingLocked && !workspaceEditingLocked,
            onExtractionLocked = {
                if (sideDockEditingLocked) notifyDockEditingLocked() else notifyDesktopEditingLocked()
            },
            reorderLineColor = folderReorderLineColor,
            reorderLineThicknessDp = folderReorderLineThickness,
            onDismiss = { sideDockFolderDialogTarget = null },
            onRename = { name -> updateSideDockFolder(entry.id) { it.copy(label = name) } },
            onChooseIcon = {},
            onChooseIconPack = {},
            onClearIcon = {},
            onAppearanceChange = { shape, color, opacity ->
                updateSideDockFolder(entry.id) { it.copy(folderShape = shape, folderColorHex = color, folderOpacityPercent = opacity) }
            },
            onLaunch = { component ->
                if (launcherActionOptionForComponent(component) != null) performLauncherAction(component)
                else appsByComponent[component]?.let(::launchApp)
            },
            onRemove = { component -> removeAppFromSideDockFolder(entry.id, component) },
            onReorder = { from, to -> reorderSideDockFolderApp(entry.id, from, to) },
            onMoveToWorkspace = { component, screenPosition ->
                val (x, y) = workspaceDropFractions(screenPosition)
                moveSideDockFolderAppToWorkspace(entry.id, component, x, y)
            },
            onDeleteFolder = { sideDockFolderDialogTarget = null; removeSideDockEntry(entry.id) },
            deleteMessage = l10n(R.string.ui_delete_this_icon_from_the_side_dock)
        )
    }

    editingShortcut?.let { shortcut ->
        ShortcutEditDialog(
            shortcut = shortcut,
            app = appsByComponent[shortcut.componentName],
            onDismiss = { editingShortcut = null },
            onChooseIcon = {
                pendingShortcutIconId = shortcut.id
                shortcutIconPicker.launch(arrayOf("image/*"))
            },
            onChooseIconPack = {
                editingShortcut = null
                shortcutIconPackPickerId = shortcut.id
            },
            onClearIcon = {
                updateShortcutVisual(shortcut.id, shortcut.label, null, null, null)
            },
            onSave = { label ->
                updateShortcutVisual(
                    shortcut.id,
                    label,
                    shortcut.customIconUri,
                    shortcut.customIconPackPackage,
                    shortcut.customIconPackDrawable
                )
                editingShortcut = null
            }
        )
    }

    shortcutIconPackPickerId?.let { shortcutId ->
        IconPackAssetPickerDialog(
            title = l10n(R.string.ui_shortcut_icon),
            onDismiss = { shortcutIconPackPickerId = null },
            onSelect = { packPackage, drawableName ->
                val shortcut = shortcuts.firstOrNull { it.id == shortcutId }
                if (shortcut != null) {
                    updateShortcutVisual(shortcut.id, shortcut.label, null, packPackage, drawableName)
                }
                shortcutIconPackPickerId = null
            }
        )
    }

    folderIconPackPickerId?.let { folderId ->
        IconPackAssetPickerDialog(
            title = l10n(R.string.ui_folder_icon),
            onDismiss = { folderIconPackPickerId = null },
            onSelect = { packPackage, drawableName ->
                val folder = folders.firstOrNull { it.id == folderId }
                if (folder != null) {
                    updateFolderVisual(folder.id, folder.label, null, packPackage, drawableName)
                }
                val dockFolder = bottomDockEntries.firstOrNull { it.id == folderId && it.isFolder }
                if (dockFolder != null) {
                    updateBottomDockFolderVisual(
                        dockFolder.id,
                        dockFolder.label,
                        null,
                        packPackage,
                        drawableName
                    )
                }
                folderIconPackPickerId = null
            }
        )
    }

    if (showSortDialog) {
        AppSortDialog(
            selected = appSortMode,
            usageAccessGranted = hasUsageAccess(context),
            onSelect = { mode ->
                updateAppSortMode(mode)
                if (mode != AppSortMode.LAST_USED || hasUsageAccess(context)) {
                    showSortDialog = false
                }
            },
            onOpenUsageAccess = {
                runCatching {
                    context.startActivity(
                        Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    )
                }
            },
            onRefresh = { reloadKey += 1 },
            onDismiss = { showSortDialog = false }
        )
    }

    workspaceMenuPage?.let { page ->
        WorkspaceLongPressDialog(
            page = page,
            editingLocked = workspaceEditingLocked,
            onDismiss = { workspaceMenuPage = null },
            onOpenSystemSettings = {
                workspaceMenuPage = null
                runCatching {
                    context.startActivity(Intent(Settings.ACTION_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                }
            },
            onOpenLauncherSettings = {
                workspaceMenuPage = null
                settingsStartPage = LauncherSettingsPage.ROOT
                showWorkspaceSettings = true
            },
            onOpenOverview = {
                workspaceMenuPage = null
                showWorkspaceOverview = true
            },
            onOpenWallpaper = {
                workspaceMenuPage = null
                settingsStartPage = LauncherSettingsPage.WALLPAPER
                showWorkspaceSettings = true
            },
            onAddToWorkspace = {
                workspaceMenuPage = null
                addToWorkspacePage = page
            },
            onCreateFolder = {
                workspaceMenuPage = null
                createEmptyFolder(page)
            }
        )
    }

    addToWorkspacePage?.let { page ->
        AddToWorkspaceDialog(
            page = page,
            onDismiss = { addToWorkspacePage = null },
            onAddApp = {
                addToWorkspacePage = null
                appPickerMode = true
                drawerOpen = true
            },
            onAddSettingsShortcut = {
                addToWorkspacePage = null
                settingsShortcutPage = page
            },
            onAddLauncherAction = {
                addToWorkspacePage = null
                launcherActionWorkspacePage = page
            },
            onAddWidget = {
                addToWorkspacePage = null
                widgetPickerPage = page
            },
            onAddNote = {
                addToWorkspacePage = null
                addNote(page)
            }
        )
    }

    settingsShortcutPage?.let { page ->
        SettingsShortcutPickerDialog(
            onDismiss = { settingsShortcutPage = null },
            onPick = { option ->
                settingsShortcutPage = null
                addSettingsShortcut(option, page)
            }
        )
    }

    launcherActionWorkspacePage?.let { page ->
        LauncherActionPickerDialog(
            onDismiss = { launcherActionWorkspacePage = null },
            onPick = { option ->
                launcherActionWorkspacePage = null
                addLauncherActionShortcut(option, page)
            }
        )
    }

    widgetPickerPage?.let { page ->
        AppWidgetPickerDialog(
            providers = activity.launcherAppWidgetManager.installedProviders,
            displayMode = widgetPickerMode,
            onDismiss = { widgetPickerPage = null },
            onPick = { info ->
                widgetPickerPage = null
                activity.requestBindWidget(info.provider, page)
            }
        )
    }

    if (showSideDockAddDialog) {
        SideDockAddDialog(
            onDismiss = { showSideDockAddDialog = false },
            availablePresets = presetDockIcons.filterNot { preset ->
                sideDockEntries.any { canonicalLauncherAction(it.intentAction) == preset.action }
            },
            onAddPreset = { preset ->
                showSideDockAddDialog = false
                addSideDockPreset(preset)
                sideDockOpen = true
            },
            onAddApp = {
                showSideDockAddDialog = false
                sideDockOpen = false
                sideDockAppPickerMode = true
                appPickerMode = false
                drawerOpen = true
            },
            onAddSettings = {
                showSideDockAddDialog = false
                showSideDockSettingsPicker = true
            },
            onAddLauncherAction = {
                showSideDockAddDialog = false
                showSideDockLauncherActionPicker = true
            },
            onAddWorkspaceEffect = {
                showSideDockAddDialog = false
                addSideDockLauncherAction(launcherActionOptions.first { it.action == LAUNCHER_ACTION_WORKSPACE_EFFECTS })
            },
            onAddMenuEffect = {
                showSideDockAddDialog = false
                addSideDockLauncherAction(launcherActionOptions.first { it.action == LAUNCHER_ACTION_APP_DRAWER_EFFECTS })
            }
        )
    }

    if (showSideDockSettingsPicker) {
        SettingsShortcutPickerDialog(
            onDismiss = { showSideDockSettingsPicker = false },
            onPick = { option ->
                showSideDockSettingsPicker = false
                addSideDockSettings(option)
                sideDockOpen = true
            }
        )
    }

    if (showSideDockLauncherActionPicker) {
        LauncherActionPickerDialog(
            title = l10n(R.string.ui_side_dock_action),
            onDismiss = { showSideDockLauncherActionPicker = false },
            onPick = { option ->
                showSideDockLauncherActionPicker = false
                addSideDockLauncherAction(option)
                sideDockOpen = true
            }
        )
    }

    if (showBottomDockAddDialog) {
        BottomDockAddDialog(
            onDismiss = { showBottomDockAddDialog = false },
            availablePresets = presetDockIcons,
            onAddPreset = { preset ->
                showBottomDockAddDialog = false
                addBottomDockPreset(preset)
            },
            onAddApp = {
                showBottomDockAddDialog = false
                bottomDockAppPickerMode = true
                sideDockAppPickerMode = false
                appPickerMode = false
                drawerOpen = true
            },
            onAddSettings = {
                showBottomDockAddDialog = false
                showBottomDockSettingsPicker = true
            },
            onAddLauncherAction = {
                showBottomDockAddDialog = false
                showBottomDockLauncherActionPicker = true
            },
            onAddWorkspaceEffect = {
                showBottomDockAddDialog = false
                addBottomDockLauncherAction(launcherActionOptions.first { it.action == LAUNCHER_ACTION_WORKSPACE_EFFECTS })
            },
            onAddMenuEffect = {
                showBottomDockAddDialog = false
                addBottomDockLauncherAction(launcherActionOptions.first { it.action == LAUNCHER_ACTION_APP_DRAWER_EFFECTS })
            }
        )
    }

    if (showBottomDockSettingsPicker) {
        SettingsShortcutPickerDialog(
            onDismiss = { showBottomDockSettingsPicker = false },
            onPick = { option ->
                showBottomDockSettingsPicker = false
                addBottomDockSettings(option)
            }
        )
    }

    if (showBottomDockLauncherActionPicker) {
        LauncherActionPickerDialog(
            title = l10n(R.string.ui_bottom_dock_action),
            onDismiss = { showBottomDockLauncherActionPicker = false },
            onPick = { option ->
                showBottomDockLauncherActionPicker = false
                addBottomDockLauncherAction(option)
            }
        )
    }

    sideDockMenuTarget?.let { entry ->
        val app = entry.componentName?.let { appsByComponent[it] }
        ShortcutContextDialog(
            shortcut = WorkspaceShortcut(entry.id, entry.label, entry.componentName.orEmpty(), currentWorkspacePage, 0.5f, 0.5f, entry.intentAction),
            app = app,
            onDismiss = { sideDockMenuTarget = null },
            onRemove = {
                sideDockMenuTarget = null
                removeSideDockEntry(entry.id)
            },
            onEdit = { sideDockMenuTarget = null; if (app != null) editingApp = app },
            onShowInfo = { sideDockMenuTarget = null; if (app != null) openAppInfo(app) else launchSideDockEntry(entry) }
        )
    }

    bottomDockMenuTarget?.let { entry ->
        val app = entry.componentName?.let { appsByComponent[it] }
        ShortcutContextDialog(
            shortcut = WorkspaceShortcut(entry.id, entry.label, entry.componentName.orEmpty(), currentWorkspacePage, 0.5f, 0.5f, entry.intentAction),
            app = app,
            onDismiss = { bottomDockMenuTarget = null },
            onRemove = { bottomDockMenuTarget = null; removeBottomDockEntry(entry.id) },
            onEdit = { bottomDockMenuTarget = null; if (app != null) editingApp = app },
            onShowInfo = { bottomDockMenuTarget = null; if (app != null) openAppInfo(app) else launchBottomDockEntry(entry) }
        )
    }

    editingApp?.let { app ->
        val component = app.componentName.flattenToString()
        val currentApp = appsByComponent[component] ?: app
        AppCustomizationDialog(
            app = currentApp,
            customization = appCustomizations[component],
            onDismiss = { editingApp = null },
            onChooseIcon = {
                pendingCustomIconComponent = component
                customAppIconPicker.launch(arrayOf("image/*"))
            },
            onChooseIconPack = {
                editingApp = null
                iconPackPickerApp = currentApp
            },
            onRemoveIcon = {
                val old = appCustomizations[component] ?: AppCustomization()
                saveAppCustomization(component, old.copy(
                    iconUri = null,
                    iconPackPackage = null,
                    iconPackDrawable = null
                ))
            },
            onSave = { label ->
                val old = appCustomizations[component] ?: AppCustomization()
                saveAppCustomization(component, old.copy(label = label))
                editingApp = null
            },
            onReset = {
                saveAppCustomization(component, AppCustomization())
                editingApp = null
            }
        )
    }

    iconPackPickerApp?.let { app ->
        val component = app.componentName.flattenToString()
        IconPackPickerDialog(
            app = app,
            onDismiss = { iconPackPickerApp = null },
            onSelect = { packPackage, drawableName ->
                val old = appCustomizations[component] ?: AppCustomization()
                saveAppCustomization(component, old.copy(
                    iconUri = null,
                    iconPackPackage = packPackage,
                    iconPackDrawable = drawableName
                ))
                iconPackPickerApp = null
            }
        )
    }

    if (newAppEmblemIconPackPickerVisible) {
        val referenceApp = apps.firstOrNull()
        if (referenceApp != null) {
            IconPackPickerDialog(
                app = referenceApp,
                onDismiss = { newAppEmblemIconPackPickerVisible = false },
                onSelect = { packPackage, drawableName ->
                    LauncherStorage.saveNewAppEmblemPackIcon(context.applicationContext, packPackage, drawableName)
                    newAppEmblemImageUri = null
                    LauncherStorage.saveNewAppEmblemImageUri(context.applicationContext, null)
                    newAppEmblemIconPackPickerVisible = false
                }
            )
        } else {
            LaunchedEffect(Unit) { newAppEmblemIconPackPickerVisible = false }
        }
    }

    pendingUninstallApp?.let { app ->
        AlertDialog(
            onDismissRequest = { pendingUninstallApp = null },
            title = { Text(l10n(R.string.ui_uninstall_application)) },
            text = { Text(l10n(R.string.ui_android_will_show_system_confirmation_to_uninstall, app.label)) },
            confirmButton = {
                TextButton(onClick = {
                    pendingUninstallApp = null
                    val intent = Intent(
                        Intent.ACTION_DELETE,
                        Uri.parse("package:${app.packageName}")
                    ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    runCatching {
                        if (intent.resolveActivity(context.packageManager) == null) {
                            error("No activity can handle ACTION_DELETE")
                        }
                        context.startActivity(intent)
                    }.onFailure {
                        Toast.makeText(
                            context,
                            l10n(R.string.ui_failed_to_open_application_uninstall_screen),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }) { Text(l10n(R.string.ui_continue), color = Color(0xFFFF7A83)) }
            },
            dismissButton = { TextButton(onClick = { pendingUninstallApp = null }) { Text(l10n(R.string.ui_cancel)) } }
        )
    }

    if (showAccessibilityPrompt) {
        AccessibilityPromptDialog(
            onDismiss = { showAccessibilityPrompt = false },
            onOpenSettings = {
                showAccessibilityPrompt = false
                val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                runCatching { context.startActivity(intent) }
                    .onFailure {
                        Toast.makeText(
                            context,
                            l10n(R.string.ui_failed_to_open_accessibility_settings),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            }
        )
    }
    }
}

@Composable
private fun LauncherHomeSection(
    dialogStyle: LauncherDialogStyle,
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(LocalLauncherDialogStyle provides dialogStyle) { content() }
}

@Composable
private fun androidx.compose.foundation.layout.BoxScope.LauncherHomeBoxSection(
    content: @Composable androidx.compose.foundation.layout.BoxScope.() -> Unit
) {
    content()
}

@Composable
private fun DesktopPager(
    modifier: Modifier = Modifier,
    workspaceCount: Int,
    loopEnabled: Boolean,
    pagerState: androidx.compose.foundation.pager.PagerState,
    shortcuts: List<WorkspaceShortcut>,
    widgets: List<WorkspaceWidget>,
    notes: List<WorkspaceNote>,
    onEditNote: (String) -> Unit,
    onMoveNote: (String, Float, Float) -> Unit,
    onResizeNote: (String, Int, Int, Float, Float) -> Unit,
    onDeleteNote: (String) -> Unit,
    widgetActionChainsByWidgetId: Map<String, WidgetActionChain>,
    folders: List<WorkspaceFolder>,
    widgetInfoRefreshKey: Int,
    appWidgetHost: AppWidgetHost,
    appWidgetManager: AppWidgetManager,
    appsByComponent: Map<String, AppItem>,
    transitionEffect: PageTransitionEffect,
    meshSnapshotAfterIdle: Boolean,
    minimumEffectDurationMs: Int,
    editingLocked: Boolean,
    onEditingLocked: () -> Unit,
    placementActive: Boolean,
    shortcutDragActive: Boolean,
    widgetEditingId: String?,
    widgetEditBarPosition: WidgetEditBarPosition,
    shadeSwipeInProgress: Boolean,
    sideDockOpen: Boolean,
    sideDockPosition: SideDockPosition,
    folderFanDisplay: FolderFanDisplay?,
    folderFanSpeedPercent: Int,
    folderFanDecayPercent: Int,
    folderFanCloseMode: FolderFanCloseMode,
    folderFanIconSizeDp: Int,
    folderFanSpacingDp: Int,
    fanItemWidthDp: Int,
    fanItemHeightDp: Int,
    fanFrameShape: AppItemFrameShape,
    fanFrameColorHex: String,
    fanFrameOpacityPercent: Int,
    fanTextColorHex: String,
    textShadowSettings: TextShadowSettings,
    workspaceTextColor: Color,
    onFolderFanDisplayChange: (FolderFanDisplay?) -> Unit,
    onWidgetBoundsChange: (String, Rect) -> Unit,
    onShortcutDragStateChange: (Boolean) -> Unit,
    onShortcutDragPositionChange: (Offset?) -> Unit,
    onFolderDragStateChange: (Boolean) -> Unit,
    onLaunchShortcut: (WorkspaceShortcut) -> Unit,
    onMoveShortcut: (id: String, page: Int, xFraction: Float, yFraction: Float) -> Unit,
    onDeleteShortcut: (id: String) -> Unit,
    onShowShortcutMenu: (WorkspaceShortcut) -> Unit,
    onCreateFolderFromShortcuts: (sourceId: String, targetId: String, page: Int) -> Unit,
    onMoveShortcutIntoFolder: (shortcutId: String, folderId: String) -> Unit,
    onMoveShortcutToSideDock: (WorkspaceShortcut) -> Boolean,
    onMoveShortcutToBottomDock: (WorkspaceShortcut) -> Boolean,
    onLockedDockDrop: (Offset) -> Boolean,
    onMoveFolder: (id: String, page: Int, xFraction: Float, yFraction: Float) -> Unit,
    onMoveFolderToBottomDock: (WorkspaceFolder) -> Boolean,
    onMoveFolderToSideDock: (WorkspaceFolder) -> Boolean,
    onDeleteFolder: (String) -> Unit,
    onOpenFolder: (WorkspaceFolder) -> Unit,
    onWidgetEditStart: (String) -> Unit,
    onWidgetUpdate: (WorkspaceWidget) -> Unit,
    onWidgetDelete: (String) -> Unit,
    onWidgetRepair: (WorkspaceWidget) -> Unit,
    onWidgetEditDone: () -> Unit,
    onWidgetActionChain: (WidgetActionChain) -> Unit,
    onDoubleTapEmpty: (page: Int) -> Unit,
    onTapEmpty: (page: Int) -> Unit,
    onLongPressEmpty: (page: Int, xFraction: Float, yFraction: Float) -> Unit,
    onWorkspaceBoundsOnScreen: (Rect) -> Unit
) {
    val effectFling = if (minimumEffectDurationMs == 0) {
        PagerDefaults.flingBehavior(pagerState)
    } else {
        PagerDefaults.flingBehavior(
            pagerState,
            snapAnimationSpec = tween(durationMillis = maxOf(300, minimumEffectDurationMs))
        )
    }
    val gestureTuning = rememberPageGestureTuningState()
    val meshSnapshotCache = rememberMeshPageSnapshotCache(
        maxEntries = workspaceCount.coerceIn(4, 10)
    )
    val swipeSnapshotCallback = rememberUpdatedState<() -> Unit> {
        if (transitionEffect.mesh && !meshSnapshotAfterIdle &&
            !placementActive && !shortcutDragActive &&
            !shadeSwipeInProgress && widgetEditingId == null) {
            val page = logicalPage(pagerState.currentPage, workspaceCount)
            (appWidgetHost as? SnapshotAppWidgetHost)?.captureWidgetsNow(
                widgets.filter { it.page == page }.map { it.appWidgetId }
            )
        }
    }
    var transitionSourcePage by remember(pagerState) {
        mutableIntStateOf(pagerState.currentPage)
    }
    var idleCaptureSerial by remember { mutableIntStateOf(0) }
    var idleCaptureRequest by remember {
        mutableStateOf<Pair<Int, Int>?>(null)
    }
    LaunchedEffect(
        pagerState.isScrollInProgress,
        pagerState.currentPage,
        transitionEffect,
        meshSnapshotAfterIdle,
        widgets,
        shortcuts,
        folders,
        placementActive,
        shortcutDragActive,
        shadeSwipeInProgress,
        widgetEditingId
    ) {
        if (!pagerState.isScrollInProgress) {
            transitionSourcePage = pagerState.currentPage
            delay(48)
            gestureTuning.reset()
            if (transitionEffect.mesh && meshSnapshotAfterIdle &&
                !placementActive && !shortcutDragActive &&
                !shadeSwipeInProgress && widgetEditingId == null) {
                // A new swipe cancels this coroutine, so preparing the texture
                // during idle time never blocks the beginning of a fast gesture.
                delay(1_000L)
                val logicalCurrentPage = logicalPage(pagerState.currentPage, workspaceCount)
                (appWidgetHost as? SnapshotAppWidgetHost)?.captureWidgetsNow(
                    widgets.filter { it.page == logicalCurrentPage }.map { it.appWidgetId }
                )
                idleCaptureSerial += 1
                idleCaptureRequest = pagerState.currentPage to idleCaptureSerial
            }
        }
    }
    // Rotation can temporarily measure the workspace at 0/near-0 size. Do not
    // compose pages (and inflate Android AppWidgetHostViews) in that interval.
    BoxWithConstraints(Modifier.fillMaxSize()) {
        if (maxWidth >= 120.dp && maxHeight >= 120.dp) {
    HorizontalPager(
        state = pagerState,
        flingBehavior = effectFling,
        modifier = Modifier
            .fillMaxSize()
            .onGloballyPositioned { coords ->
                val topLeft = coords.positionOnScreen()
                onWorkspaceBoundsOnScreen(Rect(topLeft, Size(coords.size.width.toFloat(), coords.size.height.toFloat())))
            }
            .trackPageGestureTuning(gestureTuning, vertical = false,
                onSwipeStart = { swipeSnapshotCallback.value() }),
        userScrollEnabled = !placementActive && !shortcutDragActive && !shadeSwipeInProgress && widgetEditingId == null,
        beyondViewportPageCount = 1,
        key = { virtualPage -> virtualPage }
    ) { virtualPage ->
        val page = if (loopEnabled) logicalPage(virtualPage, workspaceCount) else virtualPage
        val signedPageOffset = (
            (pagerState.currentPage - virtualPage) + pagerState.currentPageOffsetFraction
        ).coerceIn(-1f, 1f)
        val isOutgoingPage = virtualPage == transitionSourcePage
        val transitionProgress = (
            (if (isOutgoingPage) signedPageOffset.absoluteValue else 1f - signedPageOffset.absoluteValue) /
                gestureTuning.travelFraction.coerceIn(0.48f, 1f)
            ).coerceIn(0f, 1f)
        val pageInteractive = virtualPage == pagerState.currentPage &&
            (!pagerState.isScrollInProgress || transitionProgress >= 0.98f)
        val pageLive = virtualPage == pagerState.currentPage && !pagerState.isScrollInProgress
        MeshPageSurface(
            effect = transitionEffect,
            signedPageOffset = signedPageOffset,
            verticalPager = false,
            captureKey = listOf(
                page,
                shortcuts.filter { it.page == page }.hashCode(),
                widgets.filter { it.page == page }.hashCode(),
                notes.filter { it.page == page }.hashCode(),
                folders.filter { it.page == page }.hashCode()
            ),
            snapshotCache = meshSnapshotCache,
            meshDeform = isOutgoingPage || transitionEffect.usesPairedMeshTextures(),
            isOutgoingPage = isOutgoingPage,
            liveContentInteractive = pageLive,
            transitionInProgress = pagerState.isScrollInProgress,
            refreshOutgoingOnSwipe = !meshSnapshotAfterIdle,
            deferSettledRefresh = meshSnapshotAfterIdle,
            captureRequestId = idleCaptureRequest
                ?.takeIf { it.first == virtualPage }
                ?.second,
            outgoingPageHasWidgets = widgets.any { it.page == page },
            gestureTravelFraction = gestureTuning.travelFraction,
            gestureReleased = gestureTuning.released,
            modifier = Modifier.zIndex(
                if (pageInteractive) 3f
                else if (transitionEffect.isMeshCube()) (1f - signedPageOffset.absoluteValue).coerceIn(0f, 1f)
                else if (isOutgoingPage) 2f else 1f
            )
        ) {
            WorkspacePage(
                page = page,
                pageCount = workspaceCount,
                pageInteractive = pageInteractive,
                pageSettling = pagerState.isScrollInProgress,
                shortcuts = shortcuts.filter { it.page == page },
                widgets = widgets.filter { it.page == page },
                notes = notes.filter { it.page == page },
                onEditNote = onEditNote,
                onMoveNote = onMoveNote,
                onResizeNote = onResizeNote,
                onDeleteNote = onDeleteNote,
                widgetActionChainsByWidgetId = widgetActionChainsByWidgetId,
                folders = folders.filter { it.page == page },
                widgetInfoRefreshKey = widgetInfoRefreshKey,
                appWidgetHost = appWidgetHost,
                appWidgetManager = appWidgetManager,
                appsByComponent = appsByComponent,
                folderFanDisplay = folderFanDisplay?.takeIf { it.page == page },
                folderFanSpeedPercent = folderFanSpeedPercent,
                folderFanDecayPercent = folderFanDecayPercent,
                folderFanCloseMode = folderFanCloseMode,
                folderFanIconSizeDp = folderFanIconSizeDp,
                folderFanSpacingDp = folderFanSpacingDp,
                fanItemWidthDp = fanItemWidthDp,
                fanItemHeightDp = fanItemHeightDp,
                fanFrameShape = fanFrameShape,
                fanFrameColorHex = fanFrameColorHex,
                fanFrameOpacityPercent = fanFrameOpacityPercent,
                fanTextColorHex = fanTextColorHex,
                textShadowSettings = textShadowSettings,
                workspaceTextColor = workspaceTextColor,
                editingLocked = editingLocked,
                onEditingLocked = onEditingLocked,
                onFolderFanDisplayChange = onFolderFanDisplayChange,
                onWidgetBoundsChange = onWidgetBoundsChange,
                onShortcutDragStateChange = onShortcutDragStateChange,
                onShortcutDragPositionChange = onShortcutDragPositionChange,
                onFolderDragStateChange = onFolderDragStateChange,
                onLaunchShortcut = onLaunchShortcut,
                onMoveShortcut = onMoveShortcut,
                onDeleteShortcut = onDeleteShortcut,
                onShowShortcutMenu = onShowShortcutMenu,
                onCreateFolderFromShortcuts = onCreateFolderFromShortcuts,
                onMoveShortcutIntoFolder = onMoveShortcutIntoFolder,
                sideDockOpen = sideDockOpen,
                sideDockPosition = sideDockPosition,
                onMoveShortcutToSideDock = onMoveShortcutToSideDock,
                onMoveShortcutToBottomDock = onMoveShortcutToBottomDock,
                onLockedDockDrop = onLockedDockDrop,
                onMoveFolder = onMoveFolder,
                onMoveFolderToBottomDock = onMoveFolderToBottomDock,
                onMoveFolderToSideDock = onMoveFolderToSideDock,
                onDeleteFolder = onDeleteFolder,
                onOpenFolder = onOpenFolder,
                widgetEditingId = widgetEditingId,
                widgetEditBarPosition = widgetEditBarPosition,
                onWidgetEditStart = onWidgetEditStart,
                onWidgetUpdate = onWidgetUpdate,
                onWidgetDelete = onWidgetDelete,
                onWidgetRepair = onWidgetRepair,
                onWidgetEditDone = onWidgetEditDone,
                onWidgetActionChain = onWidgetActionChain,
                onDoubleTapEmpty = onDoubleTapEmpty,
                onTapEmpty = onTapEmpty,
                onLongPressEmpty = onLongPressEmpty
            )
        }
    }
        }
    }
}

@Composable
private fun WorkspacePage(
    page: Int,
    pageCount: Int,
    pageInteractive: Boolean,
    pageSettling: Boolean,
    shortcuts: List<WorkspaceShortcut>,
    widgets: List<WorkspaceWidget>,
    notes: List<WorkspaceNote>,
    onEditNote: (String) -> Unit,
    onMoveNote: (String, Float, Float) -> Unit,
    onResizeNote: (String, Int, Int, Float, Float) -> Unit,
    onDeleteNote: (String) -> Unit,
    widgetActionChainsByWidgetId: Map<String, WidgetActionChain>,
    folders: List<WorkspaceFolder>,
    widgetInfoRefreshKey: Int,
    appWidgetHost: AppWidgetHost,
    appWidgetManager: AppWidgetManager,
    appsByComponent: Map<String, AppItem>,
    folderFanDisplay: FolderFanDisplay?,
    folderFanSpeedPercent: Int,
    folderFanDecayPercent: Int,
    folderFanCloseMode: FolderFanCloseMode,
    folderFanIconSizeDp: Int,
    folderFanSpacingDp: Int,
    fanItemWidthDp: Int,
    fanItemHeightDp: Int,
    fanFrameShape: AppItemFrameShape,
    fanFrameColorHex: String,
    fanFrameOpacityPercent: Int,
    fanTextColorHex: String,
    textShadowSettings: TextShadowSettings,
    workspaceTextColor: Color,
    editingLocked: Boolean,
    onEditingLocked: () -> Unit,
    onFolderFanDisplayChange: (FolderFanDisplay?) -> Unit,
    onWidgetBoundsChange: (String, Rect) -> Unit,
    onShortcutDragStateChange: (Boolean) -> Unit,
    onShortcutDragPositionChange: (Offset?) -> Unit,
    onFolderDragStateChange: (Boolean) -> Unit,
    onLaunchShortcut: (WorkspaceShortcut) -> Unit,
    onMoveShortcut: (id: String, page: Int, xFraction: Float, yFraction: Float) -> Unit,
    onDeleteShortcut: (id: String) -> Unit,
    onShowShortcutMenu: (WorkspaceShortcut) -> Unit,
    onCreateFolderFromShortcuts: (sourceId: String, targetId: String, page: Int) -> Unit,
    onMoveShortcutIntoFolder: (shortcutId: String, folderId: String) -> Unit,
    sideDockOpen: Boolean,
    sideDockPosition: SideDockPosition,
    onMoveShortcutToSideDock: (WorkspaceShortcut) -> Boolean,
    onMoveShortcutToBottomDock: (WorkspaceShortcut) -> Boolean,
    onLockedDockDrop: (Offset) -> Boolean,
    onMoveFolder: (id: String, page: Int, xFraction: Float, yFraction: Float) -> Unit,
    onMoveFolderToBottomDock: (WorkspaceFolder) -> Boolean,
    onMoveFolderToSideDock: (WorkspaceFolder) -> Boolean,
    onDeleteFolder: (String) -> Unit,
    onOpenFolder: (WorkspaceFolder) -> Unit,
    widgetEditingId: String?,
    widgetEditBarPosition: WidgetEditBarPosition,
    onWidgetEditStart: (String) -> Unit,
    onWidgetUpdate: (WorkspaceWidget) -> Unit,
    onWidgetDelete: (String) -> Unit,
    onWidgetRepair: (WorkspaceWidget) -> Unit,
    onWidgetEditDone: () -> Unit,
    onWidgetActionChain: (WidgetActionChain) -> Unit,
    onDoubleTapEmpty: (page: Int) -> Unit,
    onTapEmpty: (page: Int) -> Unit,
    onLongPressEmpty: (page: Int, xFraction: Float, yFraction: Float) -> Unit
) {
    var containerSize by remember { mutableStateOf(IntSize.Zero) }
    var dragState by remember(page) { mutableStateOf<ShortcutDragState?>(null) }
    var folderDragState by remember(page) { mutableStateOf<FolderDragState?>(null) }
    var pageOriginInRoot by remember(page) { mutableStateOf(Offset.Zero) }
    var localFanDisplay by remember(page) { mutableStateOf(folderFanDisplay) }
    var widgetOverDelete by remember(page) { mutableStateOf(false) }

    LaunchedEffect(folderFanDisplay) {
        localFanDisplay = folderFanDisplay
    }

    val density = LocalDensity.current
    val shortcutWidthPx = with(density) { 78.dp.roundToPx() }
    val shortcutHeightPx = with(density) { 96.dp.roundToPx() }
    val trashWidthPx = with(density) { 154.dp.roundToPx() }
    val trashHeightPx = with(density) { 68.dp.roundToPx() }
    val widgetActionWidthPx = with(density) { 56.dp.roundToPx() }
    val widgetDeleteHeightPx = with(density) { 56.dp.roundToPx() }
    val widgetActionMarginPx = with(density) { 8.dp.roundToPx() }
    val contextMenuMovementThresholdPx = with(density) { 20.dp.toPx() }
    val workspaceBottomInsetPx = with(density) { 108.dp.roundToPx() }
    val maxX = (containerSize.width - shortcutWidthPx).coerceAtLeast(0)
    val maxY = (containerSize.height - workspaceBottomInsetPx - shortcutHeightPx).coerceAtLeast(0)
    val dragMaxY = (containerSize.height - shortcutHeightPx).coerceAtLeast(0)
    val widgetContainerSize = IntSize(
        containerSize.width,
        (containerSize.height - workspaceBottomInsetPx).coerceAtLeast(0)
    )
    val trashLeft = ((containerSize.width - trashWidthPx) / 2f).coerceAtLeast(0f)
    val trashRight = trashLeft + trashWidthPx
    val trashBottom = trashHeightPx.toFloat()

    fun shortcutPosition(shortcut: WorkspaceShortcut): Offset = Offset(
        (shortcut.xFraction.safeWorkspaceFraction() * maxX).coerceIn(0f, maxX.toFloat()),
        (shortcut.yFraction.safeWorkspaceFraction() * maxY).coerceIn(0f, maxY.toFloat())
    )

    fun folderPosition(folder: WorkspaceFolder): Offset = Offset(
        (folder.xFraction.safeWorkspaceFraction() * maxX).coerceIn(0f, maxX.toFloat()),
        (folder.yFraction.safeWorkspaceFraction() * maxY).coerceIn(0f, maxY.toFloat())
    )

    fun isOverTrash(position: Offset): Boolean {
        val centerX = position.x + shortcutWidthPx / 2f
        val centerY = position.y + shortcutHeightPx / 2f
        return centerX in trashLeft..trashRight && centerY in 0f..trashBottom
    }

    fun folderUnder(position: Offset): WorkspaceFolder? {
        val centerX = position.x + shortcutWidthPx / 2f
        val centerY = position.y + shortcutHeightPx / 2f
        return folders.firstOrNull { folder ->
            val target = folderPosition(folder)
            centerX in target.x..(target.x + shortcutWidthPx) &&
                centerY in target.y..(target.y + shortcutHeightPx)
        }
    }

    fun shortcutUnder(position: Offset, sourceId: String): WorkspaceShortcut? {
        val centerX = position.x + shortcutWidthPx / 2f
        val centerY = position.y + shortcutHeightPx / 2f
        return shortcuts.firstOrNull { target ->
            target.id != sourceId && target.intentAction == null &&
                shortcutPosition(target).let { targetPosition ->
                    centerX in targetPosition.x..(targetPosition.x + shortcutWidthPx) &&
                        centerY in targetPosition.y..(targetPosition.y + shortcutHeightPx)
                }
        }
    }

    val deleteOnLeft = widgetEditBarPosition == WidgetEditBarPosition.CHECK_RIGHT
    val widgetDeleteLeft = if (deleteOnLeft) widgetActionMarginPx.toFloat() else {
        (containerSize.width - widgetActionWidthPx - widgetActionMarginPx).coerceAtLeast(0).toFloat()
    }
    val widgetDeleteRight = widgetDeleteLeft + widgetActionWidthPx

    fun isWidgetOverDelete(pointerPosition: Offset): Boolean {
        return pointerPosition.x in widgetDeleteLeft..widgetDeleteRight &&
            pointerPosition.y in 0f..widgetDeleteHeightPx.toFloat()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .onSizeChanged { containerSize = it }
            .onGloballyPositioned { pageOriginInRoot = it.positionInRoot() }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .zIndex(0f)
                .pointerInput(page, widgetEditingId, pageInteractive) {
                    if (!pageInteractive) return@pointerInput
                    if (widgetEditingId == null) {
                        detectTapGestures(
                            onTap = { onTapEmpty(page) },
                            onDoubleTap = { onDoubleTapEmpty(page) },
                            onLongPress = { point ->
                                val availableWidth = (containerSize.width - shortcutWidthPx).coerceAtLeast(1).toFloat()
                                val availableHeight = (containerSize.height - workspaceBottomInsetPx - shortcutHeightPx).coerceAtLeast(1).toFloat()
                                onLongPressEmpty(
                                    page,
                                    ((point.x - shortcutWidthPx / 2f) / availableWidth).safeWorkspaceFraction(),
                                    ((point.y - shortcutHeightPx / 2f) / availableHeight).safeWorkspaceFraction()
                                )
                            }
                        )
                    } else {
                        detectTapGestures(onTap = {
                            onWidgetEditDone()
                            onTapEmpty(page)
                        })
                    }
                }
        )

        widgets.sortedBy { if (it.id == widgetEditingId) 1 else 0 }.forEach { widget ->
            key(widget.id) {
                val info = rememberRetainedAppWidgetInfo(
                    manager = appWidgetManager,
                    appWidgetId = widget.appWidgetId,
                    refreshKey = widgetInfoRefreshKey
                )
                if (info != null) {
                    WorkspaceWidgetItem(
                        widget = widget,
                        info = info,
                        appWidgetHost = appWidgetHost,
                        containerSize = widgetContainerSize,
                        editing = widget.id == widgetEditingId && !editingLocked,
                        editingAllowed = !editingLocked,
                        onEditingLocked = onEditingLocked,
                        isOverDelete = ::isWidgetOverDelete,
                        onOverDeleteChange = { widgetOverDelete = it },
                        onInteractionActiveChange = onShortcutDragStateChange,
                        onStartEdit = { if (!editingLocked) onWidgetEditStart(widget.id) },
                        actionChain = widgetActionChainsByWidgetId[widget.id],
                        onActionChainTap = onWidgetActionChain,
                        onBounds = { onWidgetBoundsChange(widget.id, it) },
                        onUpdate = onWidgetUpdate,
                        onDelete = { onWidgetDelete(widget.id) },
                        modifier = Modifier.zIndex(if (widget.id == widgetEditingId) 14f else 1f)
                    )
                } else {
                    MissingWidgetItem(
                        widget = widget,
                        containerSize = widgetContainerSize,
                        cachedSnapshot = HostedWidgetSnapshotCache.get(widget.appWidgetId)?.asImageBitmap(),
                        onRepair = { onWidgetRepair(widget) },
                        onDelete = { onWidgetDelete(widget.id) },
                        modifier = Modifier.zIndex(1f)
                    )
                }
            }
        }

        folders.forEach { folder ->
            key(folder.id) {
                val savedPosition = folderPosition(folder)
                val activeDrag = folderDragState?.takeIf { it.folderId == folder.id }
                val position = activeDrag?.position ?: savedPosition

                WorkspaceFolderIcon(
                    folder = folder,
                    appsByComponent = appsByComponent,
                    isMoving = activeDrag != null,
                    textColor = workspaceTextColor,
                    textShadowSettings = textShadowSettings,
                    onClick = { onOpenFolder(folder) },
                    onMoveStart = {
                        if (!editingLocked) {
                            folderDragState = FolderDragState(folder.id, savedPosition, false)
                            onFolderDragStateChange(true)
                            onShortcutDragPositionChange(pageOriginInRoot + savedPosition +
                                Offset(shortcutWidthPx / 2f, shortcutHeightPx / 2f))
                            onShortcutDragStateChange(true)
                        } else onEditingLocked()
                    },
                    onMove = { dragAmount ->
                        val current = folderDragState?.takeIf { it.folderId == folder.id }
                        if (current != null) {
                            val nextPosition = Offset(
                                x = (current.position.x + dragAmount.x).coerceIn(0f, maxX.toFloat()),
                                y = (current.position.y + dragAmount.y).coerceIn(0f, dragMaxY.toFloat())
                            )
                            folderDragState = current.copy(
                                position = nextPosition,
                                overTrash = isOverTrash(nextPosition)
                            )
                            onShortcutDragPositionChange(pageOriginInRoot + nextPosition +
                                Offset(shortcutWidthPx / 2f, shortcutHeightPx / 2f))
                        }
                    },
                    onMoveEnd = {
                        val finished = folderDragState?.takeIf { it.folderId == folder.id }
                        folderDragState = null
                        if (finished != null) {
                            val centreX = finished.position.x + shortcutWidthPx / 2f
                            val bottomDockDrop = (finished.position.y + shortcutHeightPx / 2f) >=
                                (containerSize.height - with(density) { 118.dp.toPx() })
                            val sideDockDrop = sideDockOpen && (
                                if (sideDockPosition == SideDockPosition.LEFT)
                                    centreX <= with(density) { 90.dp.toPx() }
                                else centreX >= containerSize.width - with(density) { 90.dp.toPx() }
                            )
                            if (finished.overTrash) {
                                onDeleteFolder(folder.id)
                            } else if (sideDockDrop && onMoveFolderToSideDock(folder)) {
                                Unit
                            } else if (bottomDockDrop && onMoveFolderToBottomDock(folder)) {
                                Unit
                            } else {
                            val xFraction = if (maxX == 0) folder.xFraction else {
                                (finished.position.x / maxX).safeWorkspaceFraction(folder.xFraction)
                            }
                            val yFraction = if (maxY == 0) folder.yFraction else {
                                (finished.position.y / maxY).safeWorkspaceFraction(folder.yFraction)
                            }
                            onMoveFolder(folder.id, page, xFraction, yFraction)
                            }
                        }
                        onFolderDragStateChange(false)
                        onShortcutDragPositionChange(null)
                        onShortcutDragStateChange(false)
                    },
                    onMoveCancel = {
                        folderDragState = null
                        onFolderDragStateChange(false)
                        onShortcutDragPositionChange(null)
                        onShortcutDragStateChange(false)
                    },
                    onFanStart = {
                        folderDragState = null
                        val origin = savedPosition + Offset(shortcutWidthPx / 2f, shortcutWidthPx / 2f)
                        val started = FolderFanDisplay(
                            folderId = folder.id,
                            page = page,
                            fingerPosition = origin,
                            rawFingerPosition = origin,
                            trail = listOf(FolderFanPathPoint(origin, 0f)),
                            headDistancePx = 0f,
                            dragging = true,
                            speedMultiplier = 1f
                        )
                        localFanDisplay = started
                        onFolderFanDisplayChange(started)
                        onShortcutDragStateChange(true)
                    },
                    onFanDrag = { dragAmount ->
                        val current = localFanDisplay?.takeIf { it.folderId == folder.id && it.page == page }
                        if (current != null) {
                            val baseFollow = folderFanSpeedPercent.coerceIn(15, 100) / 100f
                            val retention = folderFanDecayPercent.coerceIn(0, 100) / 100f
                            val rawNext = Offset(
                                x = (current.rawFingerPosition.x + dragAmount.x)
                                    .coerceIn(0f, containerSize.width.toFloat()),
                                y = (current.rawFingerPosition.y + dragAmount.y)
                                    .coerceIn(0f, containerSize.height.toFloat())
                            )
                            val iconSpacingPx = with(density) {
                                folderFanSpacingDp.coerceIn(48, 260).dp.toPx()
                            }
                            val fullExtractionDistancePx =
                                (folder.componentNames.size - 1).coerceAtLeast(0) * iconSpacingPx
                            val advance = advanceFolderFan(
                                rawStart = current.rawFingerPosition,
                                rawEnd = rawNext,
                                trail = current.trail,
                                headDistancePx = current.headDistancePx,
                                initialSpeedRatio = baseFollow,
                                speedMultiplier = current.speedMultiplier,
                                fullExtractionDistancePx = fullExtractionDistancePx,
                                retentionRatio = retention,
                                decayStepPx = with(density) { 16.dp.toPx() }
                            )
                            val updated = current.copy(
                                fingerPosition = advance.head,
                                rawFingerPosition = rawNext,
                                trail = advance.trail,
                                headDistancePx = advance.headDistancePx,
                                dragging = true,
                                speedMultiplier = advance.speedMultiplier
                            )
                            localFanDisplay = updated
                            onFolderFanDisplayChange(updated)
                        }
                    },
                    onFanEnd = {
                        localFanDisplay?.takeIf { it.folderId == folder.id && it.page == page }?.let {
                            val finished = it.copy(dragging = false)
                            localFanDisplay = finished
                            onFolderFanDisplayChange(finished)
                        }
                        onShortcutDragStateChange(false)
                    },
                    modifier = Modifier
                        .offset { IntOffset(position.x.roundToInt(), position.y.roundToInt()) }
                        .zIndex(if (activeDrag != null) 16f else 2f)
                )
            }
        }

        shortcuts.forEach { shortcut ->
            key(shortcut.id) {
                val savedPosition = shortcutPosition(shortcut)
                val activeState = dragState?.takeIf { it.shortcutId == shortcut.id }
                val position = activeState?.position ?: savedPosition
                val app = appsByComponent[shortcut.componentName]

                WorkspaceShortcutIcon(
                    shortcut = shortcut,
                    app = app,
                    isDragging = activeState != null,
                    textColor = workspaceTextColor,
                    textShadowSettings = textShadowSettings,
                    onClick = { onLaunchShortcut(shortcut) },
                    onDragStart = { pointerInRoot ->
                        if (!editingLocked) {
                            dragState = ShortcutDragState(
                                shortcutId = shortcut.id,
                                position = savedPosition,
                                overTrash = false,
                                gestureOffset = Offset.Zero,
                                maxDistanceFromStart = 0f,
                                pointerStartInRoot = pointerInRoot,
                                iconStartPosition = savedPosition
                            )
                            onShortcutDragStateChange(true)
                            onShortcutDragPositionChange(pointerInRoot)
                        } else onEditingLocked()
                    },
                    onDrag = { dragAmount, pointerInRoot ->
                        val current = dragState?.takeIf { it.shortcutId == shortcut.id }
                        if (current != null) {
                            // Derive the icon from the absolute pointer sample instead of
                            // accumulating local deltas from a moving composable.  The latter
                            // feeds the icon's layout motion back into pointer coordinates and
                            // makes a long drag visibly fall behind the finger.
                            val gestureOffset = pointerInRoot - current.pointerStartInRoot
                            val newPosition = Offset(
                                x = (current.iconStartPosition.x + gestureOffset.x).coerceIn(0f, maxX.toFloat()),
                                y = (current.iconStartPosition.y + gestureOffset.y).coerceIn(0f, dragMaxY.toFloat())
                            )
                            val distanceFromStart = hypot(
                                gestureOffset.x.toDouble(), gestureOffset.y.toDouble()
                            ).toFloat()
                            val maxDistanceFromStart = maxOf(current.maxDistanceFromStart, distanceFromStart)
                            dragState = current.copy(
                                position = newPosition,
                                overTrash = maxDistanceFromStart >= contextMenuMovementThresholdPx &&
                                    isOverTrash(newPosition),
                                gestureOffset = gestureOffset,
                                maxDistanceFromStart = maxDistanceFromStart
                            )
                            onShortcutDragPositionChange(pointerInRoot)
                        }
                    },
                    onDragEnd = { pointerInRoot ->
                        val finished = dragState?.takeIf { it.shortcutId == shortcut.id }
                        dragState = null
                        onShortcutDragPositionChange(pointerInRoot)
                        if (finished != null) {
                            val centerX = finished.position.x + shortcutWidthPx / 2f
                            val dockDrop = sideDockOpen && (
                                (sideDockPosition == SideDockPosition.LEFT && centerX <= with(density) { 110.dp.toPx() }) ||
                                    (sideDockPosition == SideDockPosition.RIGHT && centerX >= containerSize.width - with(density) { 110.dp.toPx() })
                                )
                            val folderTarget = if (shortcut.intentAction == null) folderUnder(finished.position) else null
                            val shortcutTarget = if (shortcut.intentAction == null) {
                                shortcutUnder(finished.position, shortcut.id)
                            } else null
                            when {
                                finished.maxDistanceFromStart < contextMenuMovementThresholdPx -> {
                                    onShowShortcutMenu(shortcut)
                                }
                                finished.overTrash -> onDeleteShortcut(shortcut.id)
                                // A locked dock offers no insertion marker, so test the actual
                                // drop position before falling back to moving on the desktop.
                                onLockedDockDrop(pointerInRoot) -> Unit
                                dockDrop && onMoveShortcutToSideDock(shortcut) -> Unit
                                onMoveShortcutToBottomDock(shortcut) -> Unit
                                folderTarget != null -> onMoveShortcutIntoFolder(shortcut.id, folderTarget.id)
                                shortcutTarget != null -> {
                                    onCreateFolderFromShortcuts(shortcut.id, shortcutTarget.id, page)
                                }
                                else -> {
                                    val xFraction = if (maxX == 0) shortcut.xFraction else {
                                        (finished.position.x / maxX).safeWorkspaceFraction(shortcut.xFraction)
                                    }
                                    val yFraction = if (maxY == 0) shortcut.yFraction else {
                                        (finished.position.y / maxY).safeWorkspaceFraction(shortcut.yFraction)
                                    }
                                    onMoveShortcut(shortcut.id, page, xFraction, yFraction)
                                }
                            }
                        }
                        onShortcutDragPositionChange(null)
                        onShortcutDragStateChange(false)
                    },
                    onDragCancel = {
                        dragState = null
                        onShortcutDragPositionChange(null)
                        onShortcutDragStateChange(false)
                    },
                    modifier = Modifier
                        .offset { IntOffset(position.x.roundToInt(), position.y.roundToInt()) }
                        .zIndex(if (activeState != null) 15f else 3f)
                )
            }
        }

        localFanDisplay?.let { fanState ->
            val folder = folders.firstOrNull { it.id == fanState.folderId }
            if (folder != null) {
                FolderFanOverlay(
                    folder = folder,
                    appsByComponent = appsByComponent,
                    trail = fanState.trail,
                    headDistancePx = fanState.headDistancePx,
                    itemWidthDp = fanItemWidthDp,
                    itemHeightDp = fanItemHeightDp,
                    frameShape = fanFrameShape,
                    frameColorHex = fanFrameColorHex,
                    frameOpacityPercent = fanFrameOpacityPercent,
                    textColorHex = fanTextColorHex,
                    textShadowSettings = textShadowSettings,
                    closeMode = folderFanCloseMode,
                    iconSizeDp = folderFanIconSizeDp,
                    iconSpacingDp = folderFanSpacingDp,
                    onDismiss = { onFolderFanDisplayChange(null) },
                    onLaunch = { componentName ->
                        appsByComponent[componentName]?.let {
                            onLaunchShortcut(
                                WorkspaceShortcut(
                                    id = "fan-$componentName",
                                    label = appsByComponent[componentName]?.label ?: l10n(R.string.ui_application),
                                    componentName = if (launcherActionOptionForComponent(componentName) != null) "" else componentName,
                                    page = page,
                                    xFraction = 0f,
                                    yFraction = 0f,
                                    intentAction = launcherActionOptionForComponent(componentName)?.action
                                )
                            )
                        }
                    },
                    modifier = Modifier.fillMaxSize().zIndex(18f)
                )
            }
        }

        WorkspaceSettlingHitLayer(
            enabled = pageInteractive && pageSettling && widgetEditingId == null,
            pointerKey = page,
            shortcuts = shortcuts,
            folders = folders,
            shortcutAt = { item, point ->
                val origin = shortcutPosition(item)
                point.x in origin.x..(origin.x + shortcutWidthPx) &&
                    point.y in origin.y..(origin.y + shortcutHeightPx)
            },
            folderAt = { item, point ->
                val origin = folderPosition(item)
                point.x in origin.x..(origin.x + shortcutWidthPx) &&
                    point.y in origin.y..(origin.y + shortcutHeightPx)
            },
            onShortcut = onLaunchShortcut,
            onFolder = onOpenFolder,
            onEmpty = { onTapEmpty(page) },
            modifier = Modifier.fillMaxSize().zIndex(90f)
        )

        // The side and bottom dock panels render their own insertion markers for
        // externally dragged folders, just as for shortcuts. Do not draw an
        // additional workspace-level marker: it caused a horizontal bar across
        // the bottom dock alongside its correct vertical insertion divider.

        AnimatedVisibility(
            visible = dragState != null || folderDragState != null,
            enter = fadeIn(tween(120)) + slideInVertically(initialOffsetY = { -it }),
            exit = fadeOut(tween(100)) + slideOutVertically(targetOffsetY = { -it }),
            modifier = Modifier.align(Alignment.TopCenter).zIndex(20f)
        ) {
            DeleteTarget(overTrash = dragState?.overTrash == true || folderDragState?.overTrash == true)
        }

        val activeWidgetEditingId = widgetEditingId
        if (activeWidgetEditingId != null && widgets.any { it.id == activeWidgetEditingId }) {
            WidgetEditToolbar(
                position = widgetEditBarPosition,
                overDelete = widgetOverDelete,
                onDelete = { onWidgetDelete(activeWidgetEditingId) },
                onDone = {
                    widgetOverDelete = false
                    onWidgetEditDone()
                },
                modifier = Modifier.align(Alignment.TopCenter).zIndex(30f)
            )
        }

        if (shortcuts.isEmpty() && widgets.isEmpty() && folders.isEmpty() && notes.isEmpty()) {
            Text(
                text = l10n(R.string.ui_desktop_of, page + 1, pageCount),
                modifier = Modifier.align(Alignment.Center).alpha(0.18f),
                style = MaterialTheme.typography.titleMedium,
                color = workspaceTextColor
            )
        }
    }
}

@Composable
private fun WorkspaceWidgetItem(
    widget: WorkspaceWidget,
    info: AppWidgetProviderInfo,
    appWidgetHost: AppWidgetHost,
    containerSize: IntSize,
    editing: Boolean,
    editingAllowed: Boolean,
    onEditingLocked: () -> Unit,
    isOverDelete: (Offset) -> Boolean,
    onOverDeleteChange: (Boolean) -> Unit,
    onInteractionActiveChange: (Boolean) -> Unit,
    onStartEdit: () -> Unit,
    actionChain: WidgetActionChain?,
    onActionChainTap: (WidgetActionChain) -> Unit,
    onBounds: (Rect) -> Unit,
    onUpdate: (WorkspaceWidget) -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val currentOnStartEdit by rememberUpdatedState(onStartEdit)
    val currentOnEditingLocked by rememberUpdatedState(onEditingLocked)
    val currentOnUpdate by rememberUpdatedState(onUpdate)
    val currentOnDelete by rememberUpdatedState(onDelete)
    val currentOnInteractionActiveChange by rememberUpdatedState(onInteractionActiveChange)
    val currentOnOverDeleteChange by rememberUpdatedState(onOverDeleteChange)
    var temporaryPosition by remember(widget.id) { mutableStateOf<Offset?>(null) }
    var temporaryWidthDp by remember(widget.id) { mutableStateOf<Int?>(null) }
    var temporaryHeightDp by remember(widget.id) { mutableStateOf<Int?>(null) }
    var resizeStartWidthDp by remember(widget.id) { mutableIntStateOf(widget.widthDp) }
    var resizeStartHeightDp by remember(widget.id) { mutableIntStateOf(widget.heightDp) }
    var resizeStartPosition by remember(widget.id) { mutableStateOf(Offset.Zero) }
    var dragPointerPosition by remember(widget.id) { mutableStateOf<Offset?>(null) }

    val containerWidthDp = with(density) { containerSize.width.toDp().value.roundToInt() }.coerceAtLeast(72)
    val containerHeightDp = with(density) { containerSize.height.toDp().value.roundToInt() }.coerceAtLeast(72)
    val providerMinWidth = (info.minResizeWidth.takeIf { it > 0 } ?: info.minWidth).let { (it / density.density).roundToInt() }.coerceAtLeast(48)
    val providerMinHeight = (info.minResizeHeight.takeIf { it > 0 } ?: info.minHeight).let { (it / density.density).roundToInt() }.coerceAtLeast(48)
    val minWidthDp = providerMinWidth.coerceAtMost(containerWidthDp)
    val minHeightDp = providerMinHeight.coerceAtMost(containerHeightDp)
    val maxWidthDp = containerWidthDp.coerceAtLeast(minWidthDp)
    val maxHeightDp = containerHeightDp.coerceAtLeast(minHeightDp)
    // The host can resize its surface even when an older provider does not advertise resize flags.
    // updateAppWidgetSize still informs providers that support responsive layouts.
    val canResizeHorizontal = true
    val canResizeVertical = true

    val widthDp = (temporaryWidthDp ?: widget.widthDp).coerceIn(minWidthDp, maxWidthDp)
    val heightDp = (temporaryHeightDp ?: widget.heightDp).coerceIn(minHeightDp, maxHeightDp)
    val widthPx = with(density) { widthDp.dp.toPx() }
    val heightPx = with(density) { heightDp.dp.toPx() }
    val maxX = (containerSize.width - widthPx).coerceAtLeast(0f)
    val maxY = (containerSize.height - heightPx).coerceAtLeast(0f)
    val savedPosition = Offset(widget.xFraction.safeWorkspaceFraction() * maxX, widget.yFraction.safeWorkspaceFraction() * maxY)
    val position = temporaryPosition ?: savedPosition

    // Resize from the right/bottom keeps the opposite (top-left) corner fixed.
    // This must be an absolute pixel anchor for the duration of a gesture:
    // savedPosition is calculated from xFraction/yFraction * available space,
    // and available space shrinks when a widget grows.
    // A drag from the left/top keeps the opposite edge fixed throughout the
    // gesture. Use the dimensions recorded at drag START, never the current
    // widthDp/heightDp (which change on every pointer update). Using the live
    // dimension makes a left/top gesture appear to grow the right/bottom edge.
    fun leftResizeWidth(totalDragX: Float): Int {
        val initialRightPx = resizeStartPosition.x + with(density) { resizeStartWidthDp.dp.toPx() }
        val maxAtAnchor = minOf(maxWidthDp, with(density) {
            initialRightPx.coerceAtLeast(0f).toDp().value.toInt()
        }).coerceAtLeast(minWidthDp)
        return (resizeStartWidthDp - with(density) { totalDragX.toDp().value }.roundToInt())
            .coerceIn(minWidthDp, maxAtAnchor)
    }

    fun topResizeHeight(totalDragY: Float): Int {
        val initialBottomPx = resizeStartPosition.y + with(density) { resizeStartHeightDp.dp.toPx() }
        val maxAtAnchor = minOf(maxHeightDp, with(density) {
            initialBottomPx.coerceAtLeast(0f).toDp().value.toInt()
        }).coerceAtLeast(minHeightDp)
        return (resizeStartHeightDp - with(density) { totalDragY.toDp().value }.roundToInt())
            .coerceIn(minHeightDp, maxAtAnchor)
    }

    fun leftEdgeForWidth(newWidthDp: Int): Float =
        (resizeStartPosition.x + with(density) { resizeStartWidthDp.dp.toPx() } -
            with(density) { newWidthDp.dp.toPx() }).coerceAtLeast(0f)

    fun topEdgeForHeight(newHeightDp: Int): Float =
        (resizeStartPosition.y + with(density) { resizeStartHeightDp.dp.toPx() } -
            with(density) { newHeightDp.dp.toPx() }).coerceAtLeast(0f)

    fun rightResizeWidth(totalDragX: Float): Int {
        val roomDp = with(density) {
            (containerSize.width.toFloat() - resizeStartPosition.x)
                .coerceAtLeast(0f).toDp().value.toInt()
        }
        val maxAtAnchor = maxOf(minWidthDp, minOf(maxWidthDp, roomDp))
        return (resizeStartWidthDp + with(density) { totalDragX.toDp().value }.roundToInt())
            .coerceIn(minWidthDp, maxAtAnchor)
    }

    fun bottomResizeHeight(totalDragY: Float): Int {
        val roomDp = with(density) {
            (containerSize.height.toFloat() - resizeStartPosition.y)
                .coerceAtLeast(0f).toDp().value.toInt()
        }
        val maxAtAnchor = maxOf(minHeightDp, minOf(maxHeightDp, roomDp))
        return (resizeStartHeightDp + with(density) { totalDragY.toDp().value }.roundToInt())
            .coerceIn(minHeightDp, maxAtAnchor)
    }

    fun commit(positionToSave: Offset = position, widthToSave: Int = widthDp, heightToSave: Int = heightDp) {
        val safeWidth = widthToSave.coerceIn(minWidthDp, maxWidthDp)
        val safeHeight = heightToSave.coerceIn(minHeightDp, maxHeightDp)
        val newWidthPx = with(density) { safeWidth.dp.toPx() }
        val newHeightPx = with(density) { safeHeight.dp.toPx() }
        val newMaxX = (containerSize.width - newWidthPx).coerceAtLeast(0f)
        val newMaxY = (containerSize.height - newHeightPx).coerceAtLeast(0f)
        val clamped = Offset(positionToSave.x.coerceIn(0f, newMaxX), positionToSave.y.coerceIn(0f, newMaxY))
        currentOnUpdate(widget.copy(
            xFraction = if (newMaxX == 0f) 0f else (clamped.x / newMaxX).safeWorkspaceFraction(widget.xFraction),
            yFraction = if (newMaxY == 0f) 0f else (clamped.y / newMaxY).safeWorkspaceFraction(widget.yFraction),
            widthDp = safeWidth,
            heightDp = safeHeight
        ))
        temporaryPosition = null; temporaryWidthDp = null; temporaryHeightDp = null
    }

    // The pointerInput coroutines below survive recomposition. If their callbacks
    // capture `position` from their first composition, the second drag starts at
    // the widget's ORIGINAL coordinates (until the edit frame is closed/reopened).
    // Read these updated values on every gesture instead. Do not key pointerInput
    // by `position`: that would cancel the gesture as soon as the widget moves.
    val latestGesturePosition by rememberUpdatedState(position)
    val latestGestureMaxX by rememberUpdatedState(maxX)
    val latestGestureMaxY by rememberUpdatedState(maxY)
    val latestGestureIsOverDelete by rememberUpdatedState(isOverDelete)
    val latestGestureCommit by rememberUpdatedState<(Offset) -> Unit>({ target ->
        commit(positionToSave = target)
    })

    Box(Modifier.fillMaxSize()) {
        Box(
            modifier = modifier.offset { IntOffset(position.x.roundToInt(), position.y.roundToInt()) }
                .size(widthDp.dp, heightDp.dp)
                .onGloballyPositioned { onBounds(it.boundsInRoot()) }
                .then(if (editing) Modifier.border(2.dp, Color(0xFF72E28A), RoundedCornerShape(18.dp)) else Modifier)
                .pointerInput(widget.id, editing, editingAllowed, containerSize, widthDp, heightDp) {
                    // A locked desktop must not animate widget movement, trigger delete
                    // hover state or report a drag that can never be committed.
                    if (!editingAllowed) {
                        detectDragGesturesAfterLongPress(
                            onDragStart = { currentOnEditingLocked() },
                            onDrag = { change, _ -> change.consume() }
                        )
                        return@pointerInput
                    }
                    if (editing) detectDragGestures(
                        onDragStart = { local ->
                            temporaryPosition = latestGesturePosition
                            dragPointerPosition = latestGesturePosition + local
                            currentOnInteractionActiveChange(true)
                        },
                        onDragCancel = {
                            dragPointerPosition = null
                            currentOnOverDeleteChange(false)
                            currentOnInteractionActiveChange(false)
                        },
                        onDragEnd = {
                            currentOnInteractionActiveChange(false)
                            val finalPosition = temporaryPosition ?: latestGesturePosition
                            val overDelete = dragPointerPosition?.let(latestGestureIsOverDelete) == true
                            dragPointerPosition = null
                            currentOnOverDeleteChange(false)
                            if (overDelete) currentOnDelete() else latestGestureCommit(finalPosition)
                        },
                        onDrag = { change, amount ->
                            change.consume()
                            val current = temporaryPosition ?: latestGesturePosition
                            val next = Offset((current.x + amount.x).coerceIn(0f, latestGestureMaxX), (current.y + amount.y).coerceIn(0f, latestGestureMaxY))
                            val pointer = next + change.position
                            temporaryPosition = next
                            dragPointerPosition = pointer
                            currentOnOverDeleteChange(latestGestureIsOverDelete(pointer))
                        }
                    ) else detectDragGesturesAfterLongPress(
                        onDragStart = { local ->
                            currentOnStartEdit()
                            temporaryPosition = latestGesturePosition
                            dragPointerPosition = latestGesturePosition + local
                            currentOnInteractionActiveChange(true)
                        },
                        onDragCancel = {
                            dragPointerPosition = null
                            currentOnOverDeleteChange(false)
                            currentOnInteractionActiveChange(false)
                        },
                        onDragEnd = {
                            currentOnInteractionActiveChange(false)
                            val finalPosition = temporaryPosition ?: latestGesturePosition
                            val overDelete = dragPointerPosition?.let(latestGestureIsOverDelete) == true
                            dragPointerPosition = null
                            currentOnOverDeleteChange(false)
                            if (overDelete) currentOnDelete() else latestGestureCommit(finalPosition)
                        },
                        onDrag = { change, amount ->
                            change.consume()
                            val current = temporaryPosition ?: latestGesturePosition
                            val next = Offset((current.x + amount.x).coerceIn(0f, latestGestureMaxX), (current.y + amount.y).coerceIn(0f, latestGestureMaxY))
                            val pointer = next + change.position
                            temporaryPosition = next
                            dragPointerPosition = pointer
                            currentOnOverDeleteChange(latestGestureIsOverDelete(pointer))
                        }
                    )
                }
        ) {
            HostedWidget(
                widget.copy(widthDp = widthDp, heightDp = heightDp),
                info,
                appWidgetHost,
                Modifier.fillMaxSize()
            )
            val chain = actionChain?.takeIf { it.actions.isNotEmpty() }
            if (!editing && chain != null) {
                val region = if (chain.regionMode == WidgetActionRegionMode.FULL) {
                    WidgetActionRegion()
                } else chain.region.normalized()
                Box(
                    Modifier
                        .offset(
                            x = (widthDp * region.leftPercent / 100f).dp,
                            y = (heightDp * region.topPercent / 100f).dp
                        )
                        .size(
                            width = (widthDp * region.widthPercent / 100f).dp,
                            height = (heightDp * region.heightPercent / 100f).dp
                        )
                        .zIndex(25f)
                        .pointerInput(chain.id, region) {
                            detectTapGestures(
                                onTap = { onActionChainTap(chain) },
                                onLongPress = { currentOnStartEdit() }
                            )
                        }
                )
            }
        }

        if (editing) {
            Box(
                modifier = Modifier.offset { IntOffset(position.x.roundToInt(), position.y.roundToInt()) }
                    .size(widthDp.dp, heightDp.dp).zIndex(30f)
            ) {
                // While the green edit frame is visible, keep AppWidgetHostView inactive but
                // handle dragging here so the protective layer does not disable moving.
                Box(
                    Modifier.fillMaxSize().zIndex(30f).pointerInput(
                        widget.id,
                        containerSize,
                        widthDp,
                        heightDp
                    ) {
                        detectDragGestures(
                            onDragStart = { local ->
                                temporaryPosition = latestGesturePosition
                                dragPointerPosition = latestGesturePosition + local
                                currentOnInteractionActiveChange(true)
                            },
                            onDragCancel = {
                                dragPointerPosition = null
                                currentOnOverDeleteChange(false)
                                currentOnInteractionActiveChange(false)
                            },
                            onDragEnd = {
                                currentOnInteractionActiveChange(false)
                                val finalPosition = temporaryPosition ?: latestGesturePosition
                                val overDelete = dragPointerPosition?.let(latestGestureIsOverDelete) == true
                                dragPointerPosition = null
                                currentOnOverDeleteChange(false)
                                if (overDelete) currentOnDelete() else latestGestureCommit(finalPosition)
                            },
                            onDrag = { change, amount ->
                                change.consume()
                                val current = temporaryPosition ?: latestGesturePosition
                                val next = Offset(
                                    (current.x + amount.x).coerceIn(0f, latestGestureMaxX),
                                    (current.y + amount.y).coerceIn(0f, latestGestureMaxY)
                                )
                                val pointer = next + change.position
                                temporaryPosition = next
                                dragPointerPosition = pointer
                                currentOnOverDeleteChange(latestGestureIsOverDelete(pointer))
                            }
                        )
                    }
                )
                if (canResizeHorizontal) ResizeHandle("↔", Modifier.align(Alignment.CenterStart).zIndex(31f),
                    onDragStart = {
                        resizeStartWidthDp = widthDp
                        resizeStartPosition = position
                        temporaryPosition = position
                        currentOnInteractionActiveChange(true)
                    },
                    onDrag = { total ->
                        val newWidth = leftResizeWidth(total.x)
                        temporaryWidthDp = newWidth
                        temporaryPosition = Offset(leftEdgeForWidth(newWidth), resizeStartPosition.y)
                    },
                    onDragEnd = {
                        currentOnInteractionActiveChange(false)
                        commit(positionToSave = temporaryPosition ?: position, widthToSave = temporaryWidthDp ?: widthDp)
                    })
                if (canResizeVertical) ResizeHandle("↕", Modifier.align(Alignment.TopCenter).zIndex(31f),
                    onDragStart = {
                        resizeStartHeightDp = heightDp
                        resizeStartPosition = position
                        temporaryPosition = position
                        currentOnInteractionActiveChange(true)
                    },
                    onDrag = { total ->
                        val newHeight = topResizeHeight(total.y)
                        temporaryHeightDp = newHeight
                        temporaryPosition = Offset(resizeStartPosition.x, topEdgeForHeight(newHeight))
                    },
                    onDragEnd = {
                        currentOnInteractionActiveChange(false)
                        commit(positionToSave = temporaryPosition ?: position, heightToSave = temporaryHeightDp ?: heightDp)
                    })
                if (canResizeHorizontal && canResizeVertical) ResizeHandle("↖", Modifier.align(Alignment.TopStart).zIndex(32f),
                    onDragStart = {
                        resizeStartWidthDp = widthDp
                        resizeStartHeightDp = heightDp
                        resizeStartPosition = position
                        temporaryPosition = position
                        currentOnInteractionActiveChange(true)
                    },
                    onDrag = { total ->
                        val newWidth = leftResizeWidth(total.x)
                        val newHeight = topResizeHeight(total.y)
                        temporaryWidthDp = newWidth
                        temporaryHeightDp = newHeight
                        temporaryPosition = Offset(leftEdgeForWidth(newWidth), topEdgeForHeight(newHeight))
                    },
                    onDragEnd = {
                        currentOnInteractionActiveChange(false)
                        commit(
                            positionToSave = temporaryPosition ?: position,
                            widthToSave = temporaryWidthDp ?: widthDp,
                            heightToSave = temporaryHeightDp ?: heightDp
                        )
                    })
                if (canResizeHorizontal) ResizeHandle("↔", Modifier.align(Alignment.CenterEnd).zIndex(31f),
                    onDragStart = {
                        resizeStartWidthDp = widthDp
                        resizeStartPosition = position
                        temporaryPosition = position
                        currentOnInteractionActiveChange(true)
                    },
                    onDrag = { total ->
                        temporaryWidthDp = rightResizeWidth(total.x)
                    },
                    onDragEnd = {
                        currentOnInteractionActiveChange(false)
                        commit(positionToSave = resizeStartPosition, widthToSave = temporaryWidthDp ?: widthDp)
                    })
                if (canResizeVertical) ResizeHandle("↕", Modifier.align(Alignment.BottomCenter).zIndex(31f),
                    onDragStart = {
                        resizeStartHeightDp = heightDp
                        resizeStartPosition = position
                        temporaryPosition = position
                        currentOnInteractionActiveChange(true)
                    },
                    onDrag = { total ->
                        temporaryHeightDp = bottomResizeHeight(total.y)
                    },
                    onDragEnd = {
                        currentOnInteractionActiveChange(false)
                        commit(positionToSave = resizeStartPosition, heightToSave = temporaryHeightDp ?: heightDp)
                    })
                if (canResizeHorizontal && canResizeVertical) ResizeHandle("↘", Modifier.align(Alignment.BottomEnd).zIndex(32f),
                    onDragStart = {
                        resizeStartWidthDp = widthDp
                        resizeStartHeightDp = heightDp
                        resizeStartPosition = position
                        temporaryPosition = position
                        currentOnInteractionActiveChange(true)
                    },
                    onDrag = { total ->
                        temporaryWidthDp = rightResizeWidth(total.x)
                        temporaryHeightDp = bottomResizeHeight(total.y)
                    },
                    onDragEnd = {
                        currentOnInteractionActiveChange(false)
                        commit(
                            positionToSave = resizeStartPosition,
                            widthToSave = temporaryWidthDp ?: widthDp,
                            heightToSave = temporaryHeightDp ?: heightDp
                        )
                    })
            }
        }
    }
}

@Composable
internal fun ResizeHandle(
    label: String,
    modifier: Modifier,
    onDragStart: () -> Unit,
    onDrag: (Offset) -> Unit,
    onDragEnd: () -> Unit
) {
    // The callbacks change after a resize/recomposition. Keep the pointer
    // detector stable while always calling the current lambdas.
    val currentOnDragStart by rememberUpdatedState(onDragStart)
    val currentOnDrag by rememberUpdatedState(onDrag)
    val currentOnDragEnd by rememberUpdatedState(onDragEnd)
    Box(
        modifier = modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(Color(0xFF72E28A))
            .pointerInput(label) {
                var totalDrag = Offset.Zero
                detectDragGestures(
                    onDragStart = {
                        totalDrag = Offset.Zero
                        currentOnDragStart()
                    },
                    onDragCancel = { currentOnDragEnd() },
                    onDragEnd = { currentOnDragEnd() },
                    onDrag = { change, amount ->
                        change.consume()
                        totalDrag += amount
                        currentOnDrag(totalDrag)
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        // Draw every handle ourselves, avoiding OEM-dependent Unicode arrow
        // glyphs. Horizontal/vertical have heads at both ends; corners have
        // a single arrowhead facing the dragged corner.
        Canvas(modifier = Modifier.size(24.dp)) {
            val ink = Color(0xFF102016)
            val inset = 3.dp.toPx()
            val centerX = size.width / 2f
            val centerY = size.height / 2f
            val (start, end) = when (label) {
                "↔" -> Offset(inset, centerY) to Offset(size.width - inset, centerY)
                "↕" -> Offset(centerX, inset) to Offset(centerX, size.height - inset)
                "↖" -> Offset(size.width - inset, size.height - inset) to Offset(inset, inset)
                "↘" -> Offset(inset, inset) to Offset(size.width - inset, size.height - inset)
                else -> Offset(centerX, inset) to Offset(centerX, size.height - inset)
            }
            val stroke = 2.5.dp.toPx()
            val length = hypot(end.x - start.x, end.y - start.y).coerceAtLeast(1f)
            val forward = Offset((end.x - start.x) / length, (end.y - start.y) / length)

            fun segment(from: Offset, to: Offset) {
                drawLine(color = ink, start = from, end = to,
                    strokeWidth = stroke, cap = StrokeCap.Round)
            }

            fun arrowhead(tip: Offset, inward: Offset) {
                val base = tip + inward * 5.dp.toPx()
                val perpendicular = Offset(-inward.y, inward.x) * 3.5.dp.toPx()
                segment(tip, base + perpendicular)
                segment(tip, base - perpendicular)
            }

            segment(start, end)
            if (label == "↔" || label == "↕") arrowhead(start, forward)
            arrowhead(end, Offset(-forward.x, -forward.y))
        }
    }
}

@Composable
private fun WidgetEditToolbar(
    position: WidgetEditBarPosition,
    overDelete: Boolean,
    onDelete: () -> Unit,
    onDone: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (position == WidgetEditBarPosition.CHECK_LEFT) {
            WidgetDoneButton(onDone)
            WidgetDeleteButton(overDelete, onDelete)
        } else {
            WidgetDeleteButton(overDelete, onDelete)
            WidgetDoneButton(onDone)
        }
    }
}

@Composable
private fun WidgetDeleteButton(overDelete: Boolean, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.size(56.dp).clickable(onClick = onDelete),
        shape = CircleShape,
        colors = CardDefaults.cardColors(
            containerColor = if (overDelete) Color(0xFFE34B55) else Color(0xE66A2B33)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (overDelete) 16.dp else 8.dp)
    ) {
        Image(
            painter = painterResource(R.drawable.ic_delete),
            contentDescription = l10n(R.string.ui_delete_widget),
            modifier = Modifier.fillMaxSize().padding(14.dp),
            colorFilter = ColorFilter.tint(Color.White)
        )
    }
}

@Composable
private fun WidgetDoneButton(onDone: () -> Unit) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(Color(0xFF72E28A))
            .clickable(onClick = onDone),
        contentAlignment = Alignment.Center
    ) {
        Text("✓", color = Color(0xFF102016), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun WorkspaceFolderIcon(
    folder: WorkspaceFolder,
    appsByComponent: Map<String, AppItem>,
    isMoving: Boolean,
    textColor: Color,
    textShadowSettings: TextShadowSettings,
    onClick: () -> Unit,
    onMoveStart: () -> Unit,
    onMove: (Offset) -> Unit,
    onMoveEnd: () -> Unit,
    onMoveCancel: () -> Unit,
    onFanStart: () -> Unit,
    onFanDrag: (Offset) -> Unit,
    onFanEnd: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val densityValue = LocalDensity.current.density
    val customFolderIcon = rememberLauncherCustomIcon(
        folder.customIconUri,
        folder.customIconPackPackage,
        folder.customIconPackDrawable
    )
    val currentOnClick by rememberUpdatedState(onClick)
    val currentOnMoveStart by rememberUpdatedState(onMoveStart)
    val currentOnMove by rememberUpdatedState(onMove)
    val currentOnMoveEnd by rememberUpdatedState(onMoveEnd)
    val currentOnMoveCancel by rememberUpdatedState(onMoveCancel)
    val currentOnFanStart by rememberUpdatedState(onFanStart)
    val currentOnFanDrag by rememberUpdatedState(onFanDrag)
    val currentOnFanEnd by rememberUpdatedState(onFanEnd)
    var activationSerial by remember(folder.id) { mutableIntStateOf(0) }
    // During a finger drag keep the icon anchored without a spring trailing
    // behind the pointer. Animate only the settled/non-dragged state.
    val closedFolderShape = folderShape(folder.shape)
    val closedFolderColor = parseAppFrameColor(folder.colorHex).copy(
        alpha = if (folder.shape == FolderShape.NONE) 0f
            else folder.opacityPercent.coerceIn(0, 100) / 100f
    )

    Column(
        modifier = modifier
            .width(78.dp)
            .iconActivationEffect(
                activationSerial,
                RoundedCornerShape(20.dp),
                IconActivationKind.LAUNCH,
                excludedEffect = AppLaunchAnimationEffect.ICON_THEN_WIREFRAME
            )
            .pointerInput(folder.id) {
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    val start = down.position
                    var releasedBeforeLongPress = false
                    var fanStarted = false
                    var initialFanDelta = Offset.Zero

                    val completedBeforeLongPress = withTimeoutOrNull(
                        viewConfiguration.longPressTimeoutMillis
                    ) {
                        while (true) {
                            val event = awaitPointerEvent()
                            val change = event.changes.firstOrNull { it.id == down.id }
                                ?: return@withTimeoutOrNull true
                            if (!change.pressed) {
                                releasedBeforeLongPress = true
                                return@withTimeoutOrNull true
                            }
                            val totalDelta = change.position - start
                            val distance = hypot(
                                totalDelta.x.toDouble(), totalDelta.y.toDouble()
                            ).toFloat()
                            if (distance > viewConfiguration.touchSlop) {
                                val withinVerticalCone = abs(totalDelta.x) <=
                                    abs(totalDelta.y) * 0.5773503f
                                if (withinVerticalCone) {
                                    fanStarted = true
                                    initialFanDelta = totalDelta
                                    change.consume()
                                }
                                return@withTimeoutOrNull true
                            }
                        }
                    }

                    when {
                        completedBeforeLongPress == null -> {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            currentOnMoveStart()
                            var endedNormally = false
                            try {
                                while (true) {
                                    val event = awaitPointerEvent()
                                    val change = event.changes.firstOrNull { it.id == down.id } ?: break
                                    if (!change.pressed) {
                                        endedNormally = true
                                        break
                                    }
                                    val delta = change.position - change.previousPosition
                                    if (delta != Offset.Zero) {
                                        change.consume()
                                        currentOnMove(delta)
                                    }
                                }
                            } finally {
                                if (endedNormally) currentOnMoveEnd() else currentOnMoveCancel()
                            }
                        }

                        fanStarted -> {
                            currentOnFanStart()
                            if (initialFanDelta != Offset.Zero) currentOnFanDrag(initialFanDelta)
                            try {
                                while (true) {
                                    val event = awaitPointerEvent()
                                    val change = event.changes.firstOrNull { it.id == down.id } ?: break
                                    if (!change.pressed) break
                                    val delta = change.position - change.previousPosition
                                    if (delta != Offset.Zero) {
                                        change.consume()
                                        currentOnFanDrag(delta)
                                    }
                                }
                            } finally {
                                currentOnFanEnd()
                            }
                        }

                        releasedBeforeLongPress -> {
                            activationSerial += 1
                            currentOnClick()
                        }
                    }
                }
            }
            .padding(horizontal = 4.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .iconActivationEffect(
                    activationSerial,
                    closedFolderShape,
                    IconActivationKind.LAUNCH,
                    onlyEffect = AppLaunchAnimationEffect.ICON_THEN_WIREFRAME
                ),
            contentAlignment = Alignment.Center
        ) {
            if (customFolderIcon != null) {
                AppIconImage(folder.label, customFolderIcon, Modifier.fillMaxSize().clip(closedFolderShape))
            } else {
                Card(
                    modifier = Modifier.fillMaxSize(),
                    shape = closedFolderShape,
                    colors = CardDefaults.cardColors(containerColor = closedFolderColor)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(5.dp),
                        verticalArrangement = Arrangement.SpaceEvenly
                    ) {
                        folder.componentNames.take(4).chunked(2).forEach { rowApps ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                rowApps.forEach { component ->
                                    val app = appsByComponent[component]
                                    val actionLabel = launcherActionDisplayName(component)
                                    if (actionLabel != null) Image(
                                        painterResource(launcherActionIconRes(component)),
                                        contentDescription = actionLabel,
                                        modifier = Modifier.size(20.dp)
                                    ) else AppIconImage(
                                        label = app?.label ?: l10n(R.string.ui_application),
                                        icon = app?.icon,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(5.dp))
        Text(
            folder.label,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.labelSmall.copy(
                shadow = textShadow(textShadowSettings, densityValue)
            ),
            color = textColor.copy(alpha = 0.92f)
        )
    }
}

@Composable
private fun FolderFanOverlay(
    folder: WorkspaceFolder,
    appsByComponent: Map<String, AppItem>,
    trail: List<FolderFanPathPoint>,
    headDistancePx: Float,
    itemWidthDp: Int,
    itemHeightDp: Int,
    frameShape: AppItemFrameShape,
    frameColorHex: String,
    frameOpacityPercent: Int,
    textColorHex: String,
    textShadowSettings: TextShadowSettings,
    closeMode: FolderFanCloseMode,
    iconSizeDp: Int,
    iconSpacingDp: Int,
    onDismiss: () -> Unit,
    onLaunch: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val densityValue = density.density
    val safeIconSizeDp = iconSizeDp.coerceIn(24, 96)
    val cardWidth = maxOf(itemWidthDp.coerceIn(64, 112), safeIconSizeDp + 12).dp
    val cardHeight = maxOf(itemHeightDp.coerceIn(78, 126), safeIconSizeDp + 32).dp
    val cardWidthPx = with(density) { cardWidth.toPx() }
    val cardHeightPx = with(density) { cardHeight.toPx() }
    val iconSpacingPx = with(density) { iconSpacingDp.coerceIn(48, 260).dp.toPx() }
    val shape = appItemFrameShape(frameShape)
    val textColor = parseAppFrameColor(textColorHex)
    val background = parseAppFrameColor(frameColorHex).copy(
        alpha = if (frameShape == AppItemFrameShape.NONE) 0f else frameOpacityPercent.coerceIn(0, 100) / 100f
    )
    val returnProgress = remember(folder.id) { Animatable(0f) }
    val scope = rememberCoroutineScope()
    val emptyTapInteraction = remember { MutableInteractionSource() }

    fun dismissFan() {
        if (closeMode == FolderFanCloseMode.IMMEDIATE) {
            onDismiss()
        } else if (!returnProgress.isRunning) {
            scope.launch {
                returnProgress.animateTo(1f, tween(durationMillis = 260))
                onDismiss()
            }
        }
    }

    Box(modifier = modifier) {
        Box(
            Modifier.fillMaxSize().clickable(
                interactionSource = emptyTapInteraction,
                indication = null,
                onClick = ::dismissFan
            )
        )
        folder.componentNames.forEachIndexed { index, componentName ->
            val app = appsByComponent[componentName]
            val itemDistancePx = (headDistancePx - index * iconSpacingPx).coerceAtLeast(0f)
            val trailPoint = pointOnFolderFanTrail(trail, itemDistancePx)
            val origin = trail.firstOrNull()?.position ?: trailPoint
            val point = Offset(
                x = trailPoint.x + (origin.x - trailPoint.x) * returnProgress.value,
                y = trailPoint.y + (origin.y - trailPoint.y) * returnProgress.value
            )
            var activationSerial by remember(componentName) { mutableIntStateOf(0) }
            val activationInteraction = remember(componentName) { MutableInteractionSource() }
            val activationPressed by activationInteraction.collectIsPressedAsState()
            LaunchedEffect(activationPressed) {
                if (activationPressed) activationSerial += 1
            }
            Column(
                modifier = Modifier
                    .offset {
                        IntOffset(
                            (point.x - cardWidthPx / 2f).roundToInt(),
                            (point.y - cardHeightPx / 2f).roundToInt()
                        )
                    }
                    .width(cardWidth)
                    .height(cardHeight)
                    .zIndex((folder.componentNames.size - index).toFloat())
                    .then(
                        if (frameShape == AppItemFrameShape.NONE) Modifier
                        else Modifier.background(background, shape)
                    )
                    .clickable(
                        interactionSource = activationInteraction,
                        indication = null
                    ) { onLaunch(componentName) }
                    .padding(horizontal = 5.dp, vertical = 5.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                val iconSize = safeIconSizeDp.dp
                AppIconImage(
                    label = app?.label ?: l10n(R.string.ui_application),
                    icon = app?.icon,
                    modifier = Modifier.size(iconSize)
                        .iconActivationEffect(activationSerial, RoundedCornerShape(14.dp))
                )
                Spacer(Modifier.height(3.dp))
                Text(
                    text = app?.label ?: l10n(R.string.ui_application),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                    fontSize = 10.sp,
                    lineHeight = 11.sp,
                    color = textColor,
                    style = MaterialTheme.typography.labelSmall.copy(
                        shadow = textShadow(textShadowSettings, densityValue)
                    )
                )
            }
        }
    }
}

@Composable
private fun MissingWidgetItem(
    widget: WorkspaceWidget,
    containerSize: IntSize,
    cachedSnapshot: ImageBitmap?,
    onRepair: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val containerWidthDp = with(density) { containerSize.width.toDp().value }.coerceAtLeast(48f)
    val containerHeightDp = with(density) { containerSize.height.toDp().value }.coerceAtLeast(48f)
    val widthDp = widget.widthDp.coerceAtMost(containerWidthDp.roundToInt())
    val heightDp = widget.heightDp.coerceAtMost(containerHeightDp.roundToInt())
    val widthPx = with(density) { widthDp.dp.toPx() }
    val heightPx = with(density) { heightDp.dp.toPx() }
    val maxX = (containerSize.width - widthPx).coerceAtLeast(0f)
    val maxY = (containerSize.height - heightPx).coerceAtLeast(0f)
    val position = Offset(widget.xFraction.safeWorkspaceFraction() * maxX, widget.yFraction.safeWorkspaceFraction() * maxY)

    Card(
        modifier = modifier.offset { IntOffset(position.x.roundToInt(), position.y.roundToInt()) }
            .size(widthDp.dp, heightDp.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (cachedSnapshot == null) Color(0xCC34343C) else Color.Transparent
        )
    ) {
        if (cachedSnapshot != null) {
            Image(
                bitmap = cachedSnapshot,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.FillBounds
            )
        } else {
            Column(
                Modifier.fillMaxSize().padding(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(l10n(R.string.ui_widget_temporarily_unavailable), textAlign = TextAlign.Center)
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = onRepair) { Text(l10n(R.string.ui_restore)) }
                    TextButton(onClick = onDelete) { Text(l10n(R.string.ui_delete)) }
                }
            }
        }
    }
}

@Composable
private fun HostedWidget(
    widget: WorkspaceWidget,
    info: AppWidgetProviderInfo,
    appWidgetHost: AppWidgetHost,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val liveContentVisible = LocalMeshLiveContentVisible.current
    val captureWidgetSnapshot = LocalMeshCaptureWidgetSnapshot.current
    val invalidateMeshSnapshot = LocalMeshSnapshotInvalidator.current
    val currentInvalidateMeshSnapshot by rememberUpdatedState(invalidateMeshSnapshot)
    val hostView = remember(widget.appWidgetId, info.provider) {
        appWidgetHost.createView(context, widget.appWidgetId, info)
    }
    var snapshot by remember(widget.appWidgetId) {
        mutableStateOf((hostView as? SnapshotAppWidgetHostView)?.latestSnapshot?.asImageBitmap())
    }
    var fallbackActive by remember(hostView) {
        mutableStateOf((hostView as? SnapshotAppWidgetHostView)?.isFallbackActive == true)
    }
    DisposableEffect(hostView, info.provider) {
        val snapshotView = hostView as? SnapshotAppWidgetHostView
        snapshotView?.onSnapshotReady = { bitmap ->
            snapshot = bitmap.asImageBitmap()
            currentInvalidateMeshSnapshot()
        }
        snapshotView?.onFallbackStateChanged = { active ->
            fallbackActive = active
            currentInvalidateMeshSnapshot()
        }
        fallbackActive = snapshotView?.isFallbackActive == true
        snapshotView?.requestSnapshot(120L)
        onDispose {
            snapshotView?.onSnapshotReady = null
            snapshotView?.onFallbackStateChanged = null
        }
    }
    LaunchedEffect(hostView, widget.widthDp, widget.heightDp) {
        runCatching {
            hostView.updateAppWidgetSize(
                Bundle(),
                listOf(SizeF(widget.widthDp.toFloat(), widget.heightDp.toFloat()))
            )
        }
        (hostView as? SnapshotAppWidgetHostView)?.requestSnapshot(120L)
    }
    LaunchedEffect(hostView, captureWidgetSnapshot) {
        if (captureWidgetSnapshot) (hostView as? SnapshotAppWidgetHostView)?.requestSnapshot(0L)
    }
    Card(modifier = modifier, shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = Color.Transparent)) {
        Box(Modifier.fillMaxSize()) {
            // The retained frame is only needed while Mesh draws a texture or
            // the widget provider has replaced its view with an error fallback.
            // Drawing it beneath a live translucent widget doubles its content.
            snapshot?.takeIf { !liveContentVisible || fallbackActive || captureWidgetSnapshot }?.let { image ->
                Image(image, null, Modifier.fillMaxSize(), contentScale = ContentScale.FillBounds)
            }
            AndroidView(
                factory = { hostView },
                modifier = Modifier.fillMaxSize(),
                update = { view ->
                    // When Android substitutes its grey default/error child,
                    // reveal the retained good frame underneath. A widget with
                    // no retained frame stays visible for diagnosis and repair.
                    val retainedFrameCoversFallback = fallbackActive && snapshot != null
                    view.alpha = if (liveContentVisible && !retainedFrameCoversFallback &&
                        !(captureWidgetSnapshot && snapshot != null)) 1f else 0f
                }
            )
        }
    }
}

@Composable
private fun DeleteTarget(overTrash: Boolean, modifier: Modifier = Modifier) {
    val scale by animateFloatAsState(
        targetValue = if (overTrash) 1.12f else 1f,
        animationSpec = spring(dampingRatio = 0.62f, stiffness = 520f),
        label = "trash_scale"
    )

    Card(
        modifier = Modifier
            .padding(top = 4.dp)
            .then(modifier)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (overTrash) Color(0xFFE34B55) else Color(0xCC5B252C)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (overTrash) 16.dp else 8.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(9.dp)
        ) {
            Image(
                painter = painterResource(R.drawable.ic_delete),
                contentDescription = l10n(R.string.ui_delete_shortcut),
                modifier = Modifier.size(26.dp),
                colorFilter = ColorFilter.tint(Color.White)
            )
            Text(
                text = if (overTrash) l10n(R.string.ui_release_to_delete) else l10n(R.string.ui_delete),
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

@Composable
private fun WorkspaceShortcutIcon(
    shortcut: WorkspaceShortcut,
    app: AppItem?,
    isDragging: Boolean,
    textColor: Color,
    textShadowSettings: TextShadowSettings,
    onClick: () -> Unit,
    onDragStart: (Offset) -> Unit,
    onDrag: (Offset, Offset) -> Unit,
    onDragEnd: (Offset) -> Unit,
    onDragCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    val densityValue = LocalDensity.current.density
    var launchSerial by remember(shortcut.id) { mutableIntStateOf(0) }
    var dragSerial by remember(shortcut.id) { mutableIntStateOf(0) }
    val customIcon = rememberLauncherCustomIcon(
        shortcut.customIconUri,
        shortcut.customIconPackPackage,
        shortcut.customIconPackDrawable
    )
    val displayLabel = shortcut.label.ifBlank { app?.label ?: l10n(R.string.ui_shortcut) }
    val currentOnDragStart by rememberUpdatedState(onDragStart)
    val currentOnDrag by rememberUpdatedState(onDrag)
    val currentOnDragEnd by rememberUpdatedState(onDragEnd)
    val currentOnDragCancel by rememberUpdatedState(onDragCancel)
    var layoutCoordinates by remember(shortcut.id) {
        mutableStateOf<androidx.compose.ui.layout.LayoutCoordinates?>(null)
    }
    var latestPointerInRoot by remember(shortcut.id) { mutableStateOf(Offset.Zero) }
    val scale by animateFloatAsState(
        targetValue = if (isDragging) 1.14f else 1f,
        animationSpec = spring(dampingRatio = 0.58f, stiffness = 520f),
        label = "workspace_shortcut_scale"
    )
    Column(
        modifier = modifier
            .width(78.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                shadowElevation = if (isDragging) 22f else 0f
            }
            .iconActivationEffect(
                launchSerial,
                RoundedCornerShape(20.dp),
                IconActivationKind.LAUNCH,
                excludedEffect = AppLaunchAnimationEffect.ICON_THEN_WIREFRAME
            )
            .iconActivationEffect(
                dragSerial,
                RoundedCornerShape(20.dp),
                IconActivationKind.DRAG,
                excludedEffect = AppLaunchAnimationEffect.ICON_THEN_WIREFRAME
            )
            .onGloballyPositioned { layoutCoordinates = it }
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = !isDragging,
                onClick = {
                    launchSerial += 1
                    onClick()
                }
            )
            .pointerInput(shortcut.id) {
                detectDragGesturesAfterLongPress(
                    onDragStart = { localPosition ->
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        dragSerial += 1
                        latestPointerInRoot = layoutCoordinates?.localToRoot(localPosition)
                            ?: localPosition
                        currentOnDragStart(latestPointerInRoot)
                    },
                    onDragEnd = { currentOnDragEnd(latestPointerInRoot) },
                    onDragCancel = { currentOnDragCancel() },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        latestPointerInRoot = layoutCoordinates?.localToRoot(change.position)
                            ?: latestPointerInRoot + dragAmount
                        currentOnDrag(dragAmount, latestPointerInRoot)
                    }
                )
            }
            .background(
                color = if (isDragging) {
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.20f)
                } else {
                    Color.Transparent
                },
                shape = RoundedCornerShape(20.dp)
            )
            .padding(horizontal = 4.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(54.dp)
                .iconActivationEffect(
                    launchSerial,
                    RoundedCornerShape(16.dp),
                    IconActivationKind.LAUNCH,
                    onlyEffect = AppLaunchAnimationEffect.ICON_THEN_WIREFRAME
                )
                .iconActivationEffect(
                    dragSerial,
                    RoundedCornerShape(16.dp),
                    IconActivationKind.DRAG,
                    onlyEffect = AppLaunchAnimationEffect.ICON_THEN_WIREFRAME
                ),
            contentAlignment = Alignment.Center
        ) {
            if (customIcon != null) {
                AppIconImage(displayLabel, customIcon, Modifier.fillMaxSize())
            } else if (shortcut.intentAction != null) {
                Image(
                    painter = painterResource(launcherActionIconRes(shortcut.intentAction)),
                    contentDescription = displayLabel,
                    modifier = Modifier.fillMaxSize(),
                    colorFilter = ColorFilter.tint(Color.White)
                )
            } else {
                AppIconImage(
                    label = displayLabel,
                    icon = app?.icon,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
        Spacer(modifier = Modifier.height(5.dp))
        Text(
            text = displayLabel,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.labelSmall.copy(
                shadow = textShadow(textShadowSettings, densityValue)
            ),
            color = textColor.copy(alpha = 0.92f)
        )
    }
}

@Composable
private fun AppCustomizationDialog(
    app: AppItem,
    customization: AppCustomization?,
    onDismiss: () -> Unit,
    onChooseIcon: () -> Unit,
    onChooseIconPack: () -> Unit,
    onRemoveIcon: () -> Unit,
    onSave: (String?) -> Unit,
    onReset: () -> Unit
) {
    var label by remember(app.componentName, customization?.label) {
        mutableStateOf(customization?.label ?: app.label)
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(l10n(R.string.ui_edit_application)) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AppIconImage(app.label, app.icon, Modifier.size(76.dp))
                OutlinedTextField(
                    value = label,
                    onValueChange = { label = it.take(48) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    label = { Text(l10n(R.string.ui_name_in_launcher)) },
                    supportingText = { Text(l10n(R.string.ui_system_name, app.originalLabel)) }
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(onClick = onChooseIcon, modifier = Modifier.weight(1f)) {
                        Text(l10n(R.string.ui_from_file))
                    }
                    OutlinedButton(onClick = onChooseIconPack, modifier = Modifier.weight(1f)) {
                        Text(l10n(R.string.ui_icon_pack))
                    }
                }
                OutlinedButton(
                    onClick = onRemoveIcon,
                    enabled = customization?.iconUri != null ||
                        customization?.iconPackPackage != null,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(l10n(R.string.ui_restore_system_icon))
                }
                Text(
                    l10n(R.string.ui_changes_apply_only_inside_e_e_launcher),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.62f),
                    textAlign = TextAlign.Center
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(label.trim().takeIf { it.isNotBlank() }) }) {
                Text(l10n(R.string.ui_save))
            }
        },
        dismissButton = {
            Row {
                TextButton(onClick = onReset) { Text(l10n(R.string.ui_reset_all)) }
                TextButton(onClick = onDismiss) { Text(l10n(R.string.ui_cancel)) }
            }
        }
    )
}

@Composable
private fun ShortcutContextDialog(
    shortcut: WorkspaceShortcut,
    app: AppItem?,
    onDismiss: () -> Unit,
    onRemove: () -> Unit,
    onEdit: () -> Unit,
    onShowInfo: () -> Unit
) {
    var confirmRemoval by remember(shortcut.id) { mutableStateOf(false) }
    val customIcon = rememberLauncherCustomIcon(
        shortcut.customIconUri,
        shortcut.customIconPackPackage,
        shortcut.customIconPackDrawable
    )
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(shortcut.label) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (customIcon != null) {
                    AppIconImage(shortcut.label, customIcon, Modifier.size(64.dp))
                } else if (shortcut.intentAction != null) {
                    Image(
                        painterResource(launcherActionIconRes(shortcut.intentAction)),
                        shortcut.label,
                        Modifier.size(64.dp),
                        colorFilter = ColorFilter.tint(Color.White)
                    )
                } else {
                    AppIconImage(shortcut.label, app?.icon, Modifier.size(64.dp))
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = l10n(R.string.ui_the_shortcut_was_not_moved_choose_an_action),
                    textAlign = TextAlign.Center,
                    color = Color.White.copy(alpha = 0.72f)
                )
            }
        },
        confirmButton = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = { confirmRemoval = true }) {
                    Text(l10n(R.string.ui_remove), color = Color(0xFFFF7A83))
                }
                Row {
                    TextButton(onClick = onEdit) { Text(l10n(R.string.ui_edit)) }
                    TextButton(onClick = onShowInfo) {
                        Text(if (shortcut.intentAction != null) l10n(R.string.ui_open) else l10n(R.string.ui_information))
                    }
                }
                TextButton(onClick = onDismiss) { Text(l10n(R.string.ui_cancel)) }
            }
        },
        dismissButton = {}
    )
    if (confirmRemoval) {
        DeleteConfirmationDialog(
            title = shortcut.label,
            message = l10n(R.string.ui_delete_this_icon_from_the_desktop),
            onDismiss = { confirmRemoval = false },
            onConfirm = {
                confirmRemoval = false
                onRemove()
            }
        )
    }
}

@Composable
private fun ShortcutEditDialog(
    shortcut: WorkspaceShortcut,
    app: AppItem?,
    onDismiss: () -> Unit,
    onChooseIcon: () -> Unit,
    onChooseIconPack: () -> Unit,
    onClearIcon: () -> Unit,
    onSave: (String) -> Unit
) {
    var label by remember(shortcut.id, shortcut.label) { mutableStateOf(shortcut.label) }
    val customIcon = rememberLauncherCustomIcon(
        shortcut.customIconUri,
        shortcut.customIconPackPackage,
        shortcut.customIconPackDrawable
    )
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(l10n(R.string.ui_edit_shortcut)) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (customIcon != null) AppIconImage(label, customIcon, Modifier.size(76.dp))
                else if (shortcut.intentAction != null) Image(
                    painterResource(launcherActionIconRes(shortcut.intentAction)), label,
                    Modifier.size(76.dp), colorFilter = ColorFilter.tint(Color.White)
                ) else AppIconImage(label, app?.icon, Modifier.size(76.dp))
                OutlinedTextField(
                    value = label,
                    onValueChange = { label = it.take(48) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    label = { Text(l10n(R.string.ui_shortcut_name)) }
                )
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = onChooseIcon, modifier = Modifier.weight(1f)) { Text(l10n(R.string.ui_from_file)) }
                    OutlinedButton(onClick = onChooseIconPack, modifier = Modifier.weight(1f)) { Text(l10n(R.string.ui_icon_pack)) }
                }
                OutlinedButton(
                    onClick = onClearIcon,
                    enabled = shortcut.customIconUri != null || shortcut.customIconPackPackage != null,
                    modifier = Modifier.fillMaxWidth()
                ) { Text(l10n(R.string.ui_restore_default_icon)) }
                Text(
                    l10n(R.string.ui_only_this_shortcut_is_changed_inside_the_launcher),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.62f)
                )
            }
        },
        confirmButton = { TextButton(onClick = { onSave(label) }) { Text(l10n(R.string.ui_save)) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(l10n(R.string.ui_cancel)) } }
    )
}

private data class FolderInsertionPreview(
    val targetIndex: Int,
    val lineYInRoot: Float
)

private fun calculateFolderInsertionPreview(
    pointerYInRoot: Float,
    draggedIndex: Int,
    orderedBounds: List<Rect>
): FolderInsertionPreview? {
    if (draggedIndex !in orderedBounds.indices || orderedBounds.isEmpty()) return null
    val rawSlot = orderedBounds.indexOfFirst { pointerYInRoot < it.center.y }
        .let { if (it < 0) orderedBounds.size else it }
    val targetIndex = (rawSlot - if (rawSlot > draggedIndex) 1 else 0)
        .coerceIn(0, orderedBounds.lastIndex)
    val lineY = when (rawSlot) {
        0 -> orderedBounds.first().top
        orderedBounds.size -> orderedBounds.last().bottom
        else -> (orderedBounds[rawSlot - 1].bottom + orderedBounds[rawSlot].top) / 2f
    }
    return FolderInsertionPreview(targetIndex, lineY)
}

@Composable
private fun FolderDialog(
    folder: WorkspaceFolder,
    appsByComponent: Map<String, AppItem>,
    editingEnabled: Boolean,
    extractionAllowed: Boolean,
    onExtractionLocked: () -> Unit,
    reorderLineColor: String,
    reorderLineThicknessDp: Int,
    onDismiss: () -> Unit,
    onRename: (String) -> Unit,
    onChooseIcon: () -> Unit,
    onChooseIconPack: () -> Unit,
    onClearIcon: () -> Unit,
    onAppearanceChange: (FolderShape, String, Int) -> Unit,
    onLaunch: (String) -> Unit,
    onRemove: (String) -> Unit,
    onReorder: (Int, Int) -> Unit,
    onMoveToWorkspace: (String, Offset) -> Unit,
    onDeleteFolder: () -> Unit,
    deleteMessage: String
) {
    var draftName by remember(folder.id, folder.label) { mutableStateOf(folder.label) }
    val customIcon = rememberLauncherCustomIcon(
        folder.customIconUri,
        folder.customIconPackPackage,
        folder.customIconPackDrawable
    )
    val haptic = LocalHapticFeedback.current
    val density = LocalDensity.current
    val configuration = LocalConfiguration.current
    var dialogOrigin by remember { mutableStateOf(Offset.Zero) }
    var dialogSize by remember { mutableStateOf(IntSize.Zero) }
    var draggedComponent by remember { mutableStateOf<String?>(null) }
    var dragOffset by remember { mutableStateOf(Offset.Zero) }
    var dragStartRoot by remember { mutableStateOf(Offset.Zero) }
    var itemBounds by remember(folder.id) { mutableStateOf<Map<Int, Rect>>(emptyMap()) }
    var insertionPreview by remember(folder.id) { mutableStateOf<FolderInsertionPreview?>(null) }
    var confirmFolderDelete by remember(folder.id) { mutableStateOf(false) }
    var confirmComponentRemoval by remember(folder.id) { mutableStateOf<String?>(null) }
    val folderScrollState = rememberScrollState()
    var folderNameContentOffset by remember(folder.id) { mutableIntStateOf(-1) }
    var initialFolderScrollCompleted by remember(folder.id) { mutableStateOf(false) }
    val currentItemBounds by rememberUpdatedState(itemBounds)
    val currentDialogOrigin by rememberUpdatedState(dialogOrigin)
    val currentDialogSize by rememberUpdatedState(dialogSize)
    val currentOnExtractionLocked by rememberUpdatedState(onExtractionLocked)

    fun isInsideDialog(position: Offset): Boolean =
        position.x in currentDialogOrigin.x..(currentDialogOrigin.x + currentDialogSize.width) &&
            position.y in currentDialogOrigin.y..(currentDialogOrigin.y + currentDialogSize.height)

    fun insertionPreviewAt(position: Offset, sourceIndex: Int): FolderInsertionPreview? {
        if (!isInsideDialog(position)) return null
        val orderedBounds = folder.componentNames.indices.map { currentItemBounds[it] ?: return null }
        return calculateFolderInsertionPreview(position.y, sourceIndex, orderedBounds)
    }

    LaunchedEffect(folder.id, folderNameContentOffset, folderScrollState.maxValue) {
        if (!initialFolderScrollCompleted && folderNameContentOffset >= 0 && folderScrollState.maxValue > 0) {
            // Consume the one automatic move before changing scroll state. Layout
            // updates caused by that move must not trigger another automatic scroll.
            initialFolderScrollCompleted = true
            // Put the folder-name field exactly at the top whenever the remaining
            // content is tall enough; otherwise use the furthest valid position.
            folderScrollState.scrollTo(folderNameContentOffset.coerceAtMost(folderScrollState.maxValue))
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(l10n(R.string.ui_folder))
                if (editingEnabled) {
                    Box(
                        Modifier.size(34.dp).clip(CircleShape).background(Color(0x55FF5C68))
                            .clickable { confirmFolderDelete = true },
                        contentAlignment = Alignment.Center
                    ) { Text("×", color = Color(0xFFFF8A91), fontSize = 24.sp, fontWeight = FontWeight.Bold) }
                }
            }
        },
        text = {
            Box(
                modifier = Modifier.fillMaxWidth().heightIn(max = 500.dp)
                    .onGloballyPositioned { coordinates ->
                        dialogOrigin = coordinates.positionOnScreen()
                        dialogSize = coordinates.size
                    }
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().verticalScroll(folderScrollState),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                if (customIcon != null) AppIconImage(
                    folder.label,
                    customIcon,
                    Modifier.size(72.dp).clip(folderShape(folder.shape))
                )
                else WorkspaceFolderPreview(folder, appsByComponent)
                if (editingEnabled) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = onChooseIcon, modifier = Modifier.weight(1f)) { Text(l10n(R.string.ui_picture)) }
                        OutlinedButton(onClick = onChooseIconPack, modifier = Modifier.weight(1f)) { Text(l10n(R.string.ui_icon_pack)) }
                    }
                    OutlinedButton(
                        onClick = onClearIcon,
                        enabled = folder.customIconUri != null || folder.customIconPackPackage != null ||
                            folder.shape != FolderShape.DEFAULT || folder.colorHex != "#4F5872" || folder.opacityPercent != 67,
                        modifier = Modifier.fillMaxWidth()
                    ) { Text(l10n(R.string.ui_restore_default_appearance)) }
                    FolderAppearanceEditor(
                        shape = folder.shape,
                        colorHex = folder.colorHex,
                        opacityPercent = folder.opacityPercent,
                        onShapeChange = { onAppearanceChange(it, folder.colorHex, folder.opacityPercent) },
                        onColorChange = { onAppearanceChange(folder.shape, it, folder.opacityPercent) },
                        onOpacityChange = { onAppearanceChange(folder.shape, folder.colorHex, it) }
                    )
                    OutlinedTextField(
                        value = draftName,
                        onValueChange = { draftName = it.take(40) },
                        modifier = Modifier.fillMaxWidth().onGloballyPositioned { coordinates ->
                            folderNameContentOffset = (
                                coordinates.positionInParent().y + folderScrollState.value
                            ).roundToInt().coerceAtLeast(0)
                        },
                        singleLine = true,
                        label = { Text(l10n(R.string.ui_folder_name)) }
                    )
                } else {
                    Text(
                        folder.label,
                        modifier = Modifier.fillMaxWidth().onGloballyPositioned { coordinates ->
                            folderNameContentOffset = (
                                coordinates.positionInParent().y + folderScrollState.value
                            ).roundToInt().coerceAtLeast(0)
                        },
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Center
                    )
                }
                if (folder.componentNames.isEmpty()) Text(l10n(R.string.ui_folder_is_empty), color = Color.White.copy(alpha = 0.62f))
                folder.componentNames.forEachIndexed { index, component ->
                    val app = appsByComponent[component]
                    val actionLabel = launcherActionDisplayName(component)
                    val componentLabel = app?.label ?: actionLabel ?: component
                    var itemRoot by remember(component) { mutableStateOf(Offset.Zero) }
                    var activationSerial by remember(component) { mutableIntStateOf(0) }
                    val activationInteraction = remember(component) { MutableInteractionSource() }
                    val activationPressed by activationInteraction.collectIsPressedAsState()
                    LaunchedEffect(activationPressed) {
                        if (activationPressed) activationSerial += 1
                    }
                    val isDragged = draggedComponent == component
                    Box(
                        modifier = Modifier.fillMaxWidth()
                            .onGloballyPositioned { coordinates ->
                                val topLeft = coordinates.positionOnScreen()
                                val bounds = Rect(
                                    left = topLeft.x,
                                    top = topLeft.y,
                                    right = topLeft.x + coordinates.size.width,
                                    bottom = topLeft.y + coordinates.size.height
                                )
                                if (!isDragged) {
                                    itemRoot = topLeft
                                    if (itemBounds[index] != bounds) {
                                        itemBounds = itemBounds + (index to bounds)
                                    }
                                }
                            }
                            .graphicsLayer {
                                translationX = if (isDragged) dragOffset.x else 0f
                                translationY = if (isDragged) dragOffset.y else 0f
                                scaleX = if (isDragged) 1.05f else 1f
                                scaleY = if (isDragged) 1.05f else 1f
                                shadowElevation = if (isDragged) 18f else 0f
                            }
                            .zIndex(if (isDragged) 30f else 0f)
                            .then(
                                Modifier.pointerInput(component, folder.componentNames.size, extractionAllowed) {
                                    detectDragGesturesAfterLongPress(
                                    onDragStart = { local ->
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        draggedComponent = component
                                        dragOffset = Offset.Zero
                                        dragStartRoot = itemRoot + local
                                        insertionPreview = insertionPreviewAt(dragStartRoot, index)
                                    },
                                    onDrag = { change, amount ->
                                        change.consume()
                                        val nextOffset = dragOffset + amount
                                        dragOffset = nextOffset
                                        insertionPreview = insertionPreviewAt(
                                            dragStartRoot + nextOffset,
                                            index
                                        )
                                    },
                                    onDragCancel = {
                                        draggedComponent = null
                                        dragOffset = Offset.Zero
                                        insertionPreview = null
                                    },
                                    onDragEnd = {
                                        val finalRoot = dragStartRoot + dragOffset
                                        val inside = isInsideDialog(finalRoot)
                                        if (!inside) {
                                            if (extractionAllowed) {
                                                onMoveToWorkspace(component, finalRoot)
                                            } else {
                                                // Keep the folder open; the temporary translation is reset below.
                                                currentOnExtractionLocked()
                                            }
                                        } else {
                                            val target = insertionPreviewAt(finalRoot, index)?.targetIndex
                                            if (target != null && target != index) onReorder(index, target)
                                        }
                                        draggedComponent = null
                                        dragOffset = Offset.Zero
                                        insertionPreview = null
                                    }
                                    )
                                }
                            )
                            .background(
                                Color.White.copy(alpha = if (isDragged) 0.14f else 0.06f),
                                RoundedCornerShape(16.dp)
                            )
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().clickable(
                                enabled = !isDragged,
                                interactionSource = activationInteraction,
                                indication = null
                            ) { onLaunch(component) }
                                .padding(horizontal = 12.dp, vertical = 9.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (actionLabel != null) {
                                Image(
                                    painter = painterResource(launcherActionIconRes(component)),
                                    contentDescription = componentLabel,
                                    modifier = Modifier.size(42.dp)
                                        .iconActivationEffect(activationSerial, RoundedCornerShape(14.dp))
                                )
                            } else {
                                AppIconImage(
                                    componentLabel, app?.icon,
                                    Modifier.size(42.dp)
                                        .iconActivationEffect(activationSerial, RoundedCornerShape(14.dp))
                                )
                            }
                            Text(componentLabel, Modifier.weight(1f).padding(horizontal = 10.dp), maxLines = 1, overflow = TextOverflow.Ellipsis)
                            if (editingEnabled) {
                                Box(
                                    Modifier.size(30.dp).clip(CircleShape).background(Color(0x44FF5C68))
                                        .clickable { confirmComponentRemoval = component },
                                    contentAlignment = Alignment.Center
                                ) { Text("×", color = Color(0xFFFF8A91), fontWeight = FontWeight.Bold) }
                            }
                        }
                    }
                }
                Text(
                    if (extractionAllowed) l10n(R.string.ui_hold_an_icon_to_reorder_it_drag_it_clearly)
                    else l10n(R.string.ui_folder_reordering_allowed_when_locked),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.58f),
                    textAlign = TextAlign.Center
                )
                }
                insertionPreview?.takeIf { draggedComponent != null }?.let { preview ->
                    val thicknessDp = reorderLineThicknessDp.coerceIn(1, 12)
                    val thicknessPx = with(density) { thicknessDp.dp.toPx() }
                    val lineY = (preview.lineYInRoot - dialogOrigin.y - thicknessPx / 2f)
                        .coerceIn(0f, (dialogSize.height - thicknessPx).coerceAtLeast(0f))
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height(thicknessDp.dp)
                            .offset { IntOffset(0, lineY.roundToInt()) }
                            .padding(horizontal = 6.dp)
                            .zIndex(50f)
                            .background(
                                parseAppFrameColor(reorderLineColor),
                                RoundedCornerShape(50)
                            )
                    )
                }
            }
        },
        confirmButton = {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                if (editingEnabled) {
                    TextButton(onClick = onDismiss) { Text(l10n(R.string.ui_cancel)) }
                    TextButton(onClick = { onRename(draftName); onDismiss() }) { Text(l10n(R.string.ui_save)) }
                } else {
                    Spacer(Modifier.weight(1f))
                    TextButton(onClick = onDismiss) { Text(l10n(R.string.ui_close)) }
                }
            }
        },
        dismissButton = {}
    )
    if (confirmFolderDelete) {
        DeleteConfirmationDialog(
            title = folder.label,
            message = deleteMessage,
            onDismiss = { confirmFolderDelete = false },
            onConfirm = {
                confirmFolderDelete = false
                onDeleteFolder()
            }
        )
    }
    confirmComponentRemoval?.let { component ->
        val label = appsByComponent[component]?.label ?: launcherActionDisplayName(component)
            ?: l10n(R.string.ui_application)
        DeleteConfirmationDialog(
            title = label,
            message = l10n(R.string.ui_delete_this_icon_from_the_folder),
            onDismiss = { confirmComponentRemoval = null },
            onConfirm = {
                confirmComponentRemoval = null
                onRemove(component)
            }
        )
    }
}

@Composable
private fun WorkspaceFolderPreview(folder: WorkspaceFolder, appsByComponent: Map<String, AppItem>) {
    Card(
        modifier = Modifier.size(72.dp),
        shape = folderShape(folder.shape),
        colors = CardDefaults.cardColors(
            containerColor = parseAppFrameColor(folder.colorHex).copy(
                alpha = if (folder.shape == FolderShape.NONE) 0f
                    else folder.opacityPercent.coerceIn(0, 100) / 100f
            )
        )
    ) {
        Column(Modifier.fillMaxSize().padding(7.dp), verticalArrangement = Arrangement.SpaceEvenly) {
            folder.componentNames.take(4).chunked(2).forEach { rowApps ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    rowApps.forEach { component ->
                        val app = appsByComponent[component]
                        val actionLabel = launcherActionDisplayName(component)
                        if (actionLabel != null) Image(
                            painterResource(launcherActionIconRes(component)),
                            contentDescription = actionLabel,
                            modifier = Modifier.size(25.dp)
                        ) else AppIconImage(app?.label ?: l10n(R.string.ui_application), app?.icon, Modifier.size(25.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun AppSortDialog(
    selected: AppSortMode,
    usageAccessGranted: Boolean,
    onSelect: (AppSortMode) -> Unit,
    onOpenUsageAccess: () -> Unit,
    onRefresh: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(l10n(R.string.ui_application_sorting)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                AppSortMode.values().forEach { mode ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { onSelect(mode) }
                            .padding(horizontal = 6.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = selected == mode, onClick = { onSelect(mode) })
                        Column(modifier = Modifier.weight(1f)) {
                            Text(mode.title, fontWeight = if (selected == mode) FontWeight.Bold else FontWeight.Normal)
                            Text(
                                mode.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.58f)
                            )
                        }
                    }
                }
                if (!usageAccessGranted) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0x335E74C9)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(Modifier.padding(12.dp)) {
                            Text(
                                l10n(R.string.ui_accurate_sorting_by_last_use_requires_system_usage_access),
                                style = MaterialTheme.typography.bodySmall
                            )
                            TextButton(onClick = onOpenUsageAccess) { Text(l10n(R.string.ui_grant_access)) }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onRefresh) { Text(l10n(R.string.ui_refresh_data)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(l10n(R.string.ui_close)) }
        }
    )
}

@Composable
private fun WorkspaceLongPressDialog(
    page: Int,
    editingLocked: Boolean,
    onDismiss: () -> Unit,
    onOpenSystemSettings: () -> Unit,
    onOpenLauncherSettings: () -> Unit,
    onOpenOverview: () -> Unit,
    onOpenWallpaper: () -> Unit,
    onAddToWorkspace: () -> Unit,
    onCreateFolder: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(l10n(R.string.ui_desktop, page + 1)) },
        text = {
            Column(
                Modifier.heightIn(max = 560.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (!editingLocked) {
                    WorkspaceActionButton(l10n(R.string.ui_add_to_desktop), onAddToWorkspace)
                    WorkspaceActionButton(l10n(R.string.ui_create_folder), onCreateFolder)
                }
                WorkspaceActionButton(l10n(R.string.ui_page_overview), onOpenOverview)
                WorkspaceActionButton(l10n(R.string.ui_wallpaper), onOpenWallpaper)
                WorkspaceActionButton(l10n(R.string.ui_launcher_settings), onOpenLauncherSettings)
                WorkspaceActionButton(l10n(R.string.ui_system_settings), onOpenSystemSettings)
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(l10n(R.string.ui_cancel)) }
        }
    )
}

@Composable
private fun AddToWorkspaceDialog(
    page: Int,
    onDismiss: () -> Unit,
    onAddApp: () -> Unit,
    onAddSettingsShortcut: () -> Unit,
    onAddLauncherAction: () -> Unit,
    onAddWidget: () -> Unit,
    onAddNote: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(l10n(R.string.ui_add_desktop, page + 1)) },
        text = {
            Column(
                Modifier.heightIn(max = 560.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                WorkspaceActionButton(l10n(R.string.ui_application_icon), onAddApp)
                WorkspaceActionButton(l10n(R.string.ui_widget), onAddWidget)
                WorkspaceActionButton(l10n(R.string.ui_note_title), onAddNote)
                WorkspaceActionButton(l10n(R.string.ui_settings_item_shortcut), onAddSettingsShortcut)
                WorkspaceActionButton(l10n(R.string.ui_launcher_action), onAddLauncherAction)
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(l10n(R.string.ui_cancel)) }
        }
    )
}

@Composable
private fun SettingsShortcutPickerDialog(
    onDismiss: () -> Unit,
    onPick: (SettingsShortcutOption) -> Unit
) {
    val options = remember { availableSettingsShortcutOptions() }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(l10n(R.string.ui_settings_shortcut)) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 480.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(7.dp)
            ) {
                options.forEach { option ->
                    WorkspaceActionButton(option.title) { onPick(option) }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(l10n(R.string.ui_cancel)) }
        }
    )
}

@Composable
private fun LauncherActionPickerDialog(
    title: String = l10n(R.string.ui_launcher_action),
    onDismiss: () -> Unit,
    onPick: (LauncherActionOption) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                launcherActionOptions.forEach { option ->
                    WorkspaceActionButton(
                        shortLauncherActionName(option.title)
                    ) { onPick(option) }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text(l10n(R.string.ui_cancel)) } }
    )
}

@Composable
private fun WorkspaceActionButton(
    title: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.06f))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 13.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(title, fontWeight = FontWeight.Medium)
            Text("›", color = Color.White.copy(alpha = 0.55f))
        }
    }
}

@Composable
private fun AccessibilityPromptDialog(
    onDismiss: () -> Unit,
    onOpenSettings: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(l10n(R.string.ui_system_gesture_access)) },
        text = {
            Text(l10n(R.string.ui_accessibility_prompt_body))
        },
        confirmButton = {
            TextButton(onClick = onOpenSettings) {
                Text(l10n(R.string.ui_open_settings))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(l10n(R.string.ui_cancel))
            }
        }
    )
}

@Composable
private fun DesktopHeader(
    workspaceCount: Int,
    currentPage: Int,
    transitionEffect: PageTransitionEffect,
    placementActive: Boolean,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 14.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = if (placementActive) {
                l10n(R.string.ui_edit_mode)
            } else {
                "${currentPage + 1} / $workspaceCount · ${transitionEffect.title}"
            },
            color = Color.White.copy(alpha = 0.58f),
            style = MaterialTheme.typography.labelLarge
        )

        OutlinedButton(
            onClick = onOpenSettings,
            enabled = !placementActive
        ) {
            Text(l10n(R.string.ui_settings))
        }
    }
}

@Composable
private fun PageIndicator(
    pageCount: Int,
    currentPage: Int,
    indicatorColor: Color = Color.White,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0x661B1B23))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
            horizontalArrangement = Arrangement.spacedBy(7.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(pageCount) { page ->
                val selected = page == currentPage
                Box(
                    modifier = Modifier
                        .size(if (selected) 8.dp else 6.dp)
                        .clip(CircleShape)
                        .background(
                            if (selected) indicatorColor
                            else indicatorColor.copy(alpha = 0.30f)
                        )
                )
            }
        }
    }
}

@Composable
private fun ShortcutPlacementOverlay(
    app: AppItem,
    currentPage: Int,
    pageCount: Int,
    onCancel: () -> Unit,
    onDrop: (xFraction: Float, yFraction: Float) -> Unit
) {
    var containerSize by remember { mutableStateOf(IntSize.Zero) }
    val density = LocalDensity.current
    val shortcutWidthPx = with(density) { 86.dp.roundToPx() }
    val shortcutHeightPx = with(density) { 102.dp.roundToPx() }
    val maxX = (containerSize.width - shortcutWidthPx).coerceAtLeast(0).toFloat()
    val maxY = (containerSize.height - shortcutHeightPx).coerceAtLeast(0).toFloat()

    var position by remember(app.componentName, currentPage) {
        mutableStateOf<Offset?>(null)
    }
    var hasDragged by remember(app.componentName, currentPage) { mutableStateOf(false) }
    val currentPosition = position ?: Offset(maxX / 2f, maxY / 2f)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 56.dp, bottom = 108.dp)
            .onSizeChanged { containerSize = it }
    ) {
        Card(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 4.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xE623232D))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = l10n(R.string.ui_drag_and_drop_desktop, currentPage + 1, pageCount),
                    style = MaterialTheme.typography.labelMedium
                )
                TextButton(onClick = onCancel) {
                    Text(l10n(R.string.ui_cancel))
                }
            }
        }

        Column(
            modifier = Modifier
                .offset {
                    IntOffset(currentPosition.x.roundToInt(), currentPosition.y.roundToInt())
                }
                .width(86.dp)
                .graphicsLayer {
                    scaleX = if (hasDragged) 1.12f else 1.04f
                    scaleY = if (hasDragged) 1.12f else 1.04f
                    shadowElevation = 18f
                }
                .clip(RoundedCornerShape(22.dp))
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.22f))
                .pointerInput(app.componentName, currentPage, maxX, maxY) {
                    detectDragGestures(
                        onDragStart = { hasDragged = true },
                        onDragCancel = { hasDragged = false },
                        onDragEnd = {
                            hasDragged = false
                            val droppedPosition = position ?: currentPosition
                            val xFraction = if (maxX == 0f) 0f else droppedPosition.x / maxX
                            val yFraction = if (maxY == 0f) 0f else droppedPosition.y / maxY
                            onDrop(xFraction, yFraction)
                        },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            val previousPosition = position ?: currentPosition
                            position = Offset(
                                x = (previousPosition.x + dragAmount.x).coerceIn(0f, maxX),
                                y = (previousPosition.y + dragAmount.y).coerceIn(0f, maxY)
                            )
                        }
                    )
                }
                .padding(horizontal = 4.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AppIconImage(
                label = app.label,
                icon = app.icon,
                modifier = Modifier.size(58.dp)
            )
            Spacer(modifier = Modifier.height(5.dp))
            Text(
                text = app.label,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.labelSmall,
                color = Color.White
            )
        }
    }
}

@Composable
private fun WorkspaceSettingsDialog(
    dialogStyle: LauncherDialogStyle,
    initialPage: LauncherSettingsPage,
    allApps: List<AppItem>,
    hiddenApps: Set<String>,
    workspaceCount: Int,
    workspaceLoop: Boolean,
    defaultWorkspacePage: Int,
    transitionEffect: PageTransitionEffect,
    meshSnapshotAfterIdle: Boolean,
    pageEffectTiming: PageEffectTimingSettings,
    workspaceOverviewMode: WorkspaceOverviewMode,
    effectPreviewMode: EffectPreviewMode,
    appDrawerMode: AppDrawerMode,
    appDrawerLoop: Boolean,
    appDrawerPageTransitionEffect: PageTransitionEffect,
    appDrawerIconEffect: AppDrawerIconEffect,
    appLaunchAnimationEffect: AppLaunchAnimationEffect,
    extensions: LauncherSettingsExtensions,
    appDrawerColumns: Int,
    appDrawerRows: Int,
    appDrawerItemWidth: Int,
    appDrawerItemHeight: Int,
    appDrawerIndependentSize: Boolean,
    appDrawerFillAvailable: Boolean,
    appDrawerScreenHeightDp: Int,
    appDrawerFrameShape: AppItemFrameShape,
    appDrawerFrameColor: String,
    appDrawerFrameOpacity: Int,
    appDrawerFrameTextColor: String,
    appDrawerEdgeSwitchDp: Int,
    appDrawerBackgroundMode: AppDrawerBackgroundMode,
    appDrawerBackgroundOpacity: Int,
    appDrawerBackgroundColorA: String,
    appDrawerBackgroundColorB: String,
    appDrawerBackgroundTextColor: String,
    appDrawerBackgroundImageUri: String?,
    widgetPickerMode: WidgetPickerDisplayMode,
    widgetActionWidgets: List<WidgetActionWidgetDescriptor>,
    widgetActionChains: List<WidgetActionChain>,
    ignoreGesturesOnWidgets: Boolean,
    swipeDownAction: DesktopGestureAction,
    swipeUpAction: DesktopGestureAction,
    doubleTapAction: DesktopGestureAction,
    widgetEditBarPosition: WidgetEditBarPosition,
    sideDockPosition: SideDockPosition,
    sideDockOpacity: Int,
    sideDockTextColor: String,
    bottomDockPageCount: Int,
    bottomDockLoop: Boolean,
    bottomDockIndicatorToggle: Boolean,
    bottomDockAddButtonEnabled: Boolean,
    bottomDockAlignment: BottomDockAlignment,
    bottomDockBackgroundColor: String,
    bottomDockBackgroundOpacity: Int,
    bottomDockTextColor: String,
    bottomDockShelfHeight: Int,
    bottomDockIconVerticalOffset: Int,
    bottomDockShelfVerticalOffset: Int,
    bottomDockPageIndicatorVisible: Boolean,
    bottomDockPageIndicatorGap: Int,
    bottomDockLabelsVisible: Boolean,
    bottomDockShelfShape: DockShelfShape,
    bottomDockShelfColor: String,
    bottomDockShelfOpacity: Int,
    bottomDockFolderCreationDelay: Int,
    appDrawerReorderHold: Int,
    folderFanSpeed: Int,
    folderFanDecay: Int,
    folderFanCloseMode: FolderFanCloseMode,
    folderFanIconSize: Int,
    folderFanSpacing: Int,
    folderReorderLineColor: String,
    folderReorderLineThickness: Int,
    wallpaperMode: LauncherWallpaperMode,
    wallpaperScrolling: Boolean,
    wallpaperDynamic: Boolean,
    wallpaperBackground: String,
    wallpaperCircleA: String,
    wallpaperCircleB: String,
    wallpaperCircleCount: Int,
    textShadowSettings: TextShadowSettings,
    environmentEffects: EnvironmentEffectsConfig,
    launcherInterfaceStyle: LauncherInterfaceStyle,
    launcherDialogStyle: LauncherDialogStyle,
    onWorkspaceCountChange: (Int) -> Unit,
    onWorkspaceLoopChange: (Boolean) -> Unit,
    onDefaultWorkspacePageChange: (Int) -> Unit,
    onTransitionEffectChange: (PageTransitionEffect) -> Unit,
    onMeshSnapshotAfterIdleChange: (Boolean) -> Unit,
    onWorkspaceOverviewModeChange: (WorkspaceOverviewMode) -> Unit,
    onEffectPreviewModeChange: (EffectPreviewMode) -> Unit,
    onOpenWorkspaceOverview: () -> Unit,
    onPreviewWorkspaceEffectLive: (PageTransitionEffect) -> Unit,
    onPreviewAppDrawerEffectLive: (PageTransitionEffect) -> Unit,
    onAppDrawerModeChange: (AppDrawerMode) -> Unit,
    onAppDrawerLoopChange: (Boolean) -> Unit,
    onAppDrawerPageTransitionChange: (PageTransitionEffect) -> Unit,
    onAppDrawerIconEffectChange: (AppDrawerIconEffect) -> Unit,
    onAppLaunchAnimationChange: (AppLaunchAnimationEffect) -> Unit,
    onAppDrawerColumnsChange: (Int) -> Unit,
    onAppDrawerRowsChange: (Int) -> Unit,
    onAppDrawerItemWidthChange: (Int) -> Unit,
    onAppDrawerItemHeightChange: (Int) -> Unit,
    onAppDrawerIndependentSizeChange: (Boolean) -> Unit,
    onAppDrawerFillAvailableChange: (Boolean) -> Unit,
    onAppDrawerScreenHeightDpChange: (Int) -> Unit,
    onAppDrawerFrameShapeChange: (AppItemFrameShape) -> Unit,
    onAppDrawerFrameColorChange: (String) -> Unit,
    onAppDrawerFrameOpacityChange: (Int) -> Unit,
    onAppDrawerFrameTextColorChange: (String) -> Unit,
    onAppDrawerEdgeSwitchDpChange: (Int) -> Unit,
    onAppDrawerBackgroundModeChange: (AppDrawerBackgroundMode) -> Unit,
    onAppDrawerBackgroundOpacityChange: (Int) -> Unit,
    onAppDrawerBackgroundColorAChange: (String) -> Unit,
    onAppDrawerBackgroundColorBChange: (String) -> Unit,
    onAppDrawerBackgroundTextColorChange: (String) -> Unit,
    onChooseAppDrawerImage: () -> Unit,
    onWidgetPickerModeChange: (WidgetPickerDisplayMode) -> Unit,
    onWidgetActionChainSave: (WidgetActionChain) -> Unit,
    onWidgetActionChainDelete: (String) -> Unit,
    onIgnoreGesturesOnWidgetsChange: (Boolean) -> Unit,
    onSwipeDownActionChange: (DesktopGestureAction) -> Unit,
    onSwipeUpActionChange: (DesktopGestureAction) -> Unit,
    onDoubleTapActionChange: (DesktopGestureAction) -> Unit,
    onWidgetEditBarPositionChange: (WidgetEditBarPosition) -> Unit,
    onSideDockPositionChange: (SideDockPosition) -> Unit,
    onSideDockOpacityChange: (Int) -> Unit,
    onSideDockTextColorChange: (String) -> Unit,
    onBottomDockPageCountChange: (Int) -> Unit,
    onBottomDockLoopChange: (Boolean) -> Unit,
    onBottomDockIndicatorToggleChange: (Boolean) -> Unit,
    onBottomDockAddButtonEnabledChange: (Boolean) -> Unit,
    onOpenBottomDockAdd: () -> Unit,
    onBottomDockAlignmentChange: (BottomDockAlignment) -> Unit,
    onBottomDockBackgroundColorChange: (String) -> Unit,
    onBottomDockBackgroundOpacityChange: (Int) -> Unit,
    onBottomDockTextColorChange: (String) -> Unit,
    onBottomDockShelfHeightChange: (Int) -> Unit,
    onBottomDockIconVerticalOffsetChange: (Int) -> Unit,
    onBottomDockShelfVerticalOffsetChange: (Int) -> Unit,
    onBottomDockPageIndicatorVisibleChange: (Boolean) -> Unit,
    onBottomDockPageIndicatorGapChange: (Int) -> Unit,
    onBottomDockLabelsVisibleChange: (Boolean) -> Unit,
    onBottomDockShelfShapeChange: (DockShelfShape) -> Unit,
    onBottomDockShelfColorChange: (String) -> Unit,
    onBottomDockShelfOpacityChange: (Int) -> Unit,
    onBottomDockFolderCreationDelayChange: (Int) -> Unit,
    onAppDrawerReorderHoldChange: (Int) -> Unit,
    onHiddenAppChange: (String, Boolean) -> Unit,
    onAppDrawerEdgePreviewChange: (Boolean) -> Unit,
    onTextShadowSettingsChange: (TextShadowSettings) -> Unit,
    onEnvironmentEffectsChange: (EnvironmentEffectsConfig) -> Unit,
    onLauncherInterfaceStyleChange: (LauncherInterfaceStyle) -> Unit,
    onChooseLauncherInterfaceImage: () -> Unit,
    onLauncherDialogStyleChange: (LauncherDialogStyle) -> Unit,
    onChooseLauncherDialogImage: () -> Unit,
    onFolderFanSpeedChange: (Int) -> Unit,
    onFolderFanDecayChange: (Int) -> Unit,
    onFolderFanCloseModeChange: (FolderFanCloseMode) -> Unit,
    onFolderFanIconSizeChange: (Int) -> Unit,
    onFolderFanSpacingChange: (Int) -> Unit,
    onFolderReorderLineColorChange: (String) -> Unit,
    onFolderReorderLineThicknessChange: (Int) -> Unit,
    onWallpaperModeChange: (LauncherWallpaperMode) -> Unit,
    onWallpaperScrollingChange: (Boolean) -> Unit,
    onWallpaperDynamicChange: (Boolean) -> Unit,
    onWallpaperBackgroundChange: (String) -> Unit,
    onWallpaperCircleAChange: (String) -> Unit,
    onWallpaperCircleBChange: (String) -> Unit,
    onWallpaperCircleCountChange: (Int) -> Unit,
    onChooseStaticWallpaper: () -> Unit,
    onChooseLiveWallpaper: () -> Unit,
    onRestartLauncher: () -> Unit,
    onResetLauncherArea: (LauncherResetArea) -> Unit,
    onClearWorkspaceItems: () -> Unit,
    onCreateBackup: () -> Unit,
    onRestoreBackup: () -> Unit,
    onSideDockSettingsPreviewChange: (Boolean) -> Unit,
    onWidgetCheckmarkPreviewChange: (Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    val appLaunchDelay = extensions.appLaunchDelay
    val appLaunchAnimationDuration = extensions.appLaunchAnimationDuration
    val appDragAnimationEffect = extensions.appDragAnimationEffect
    val appDragAnimationDuration = extensions.appDragAnimationDuration
    val linkLaunchDelayToAnimation = extensions.linkLaunchDelayToAnimation
    val appDrawerDragOverviewMode = extensions.appDrawerDragOverviewMode
    val appDrawerCrossPageDragMode = extensions.appDrawerCrossPageDragMode
    val appDrawerPagePreviewMode = extensions.appDrawerPagePreviewMode
    val appDrawerDragPageHover = extensions.appDrawerDragPageHover
    val appDrawerQuickAction = extensions.appDrawerQuickAction
    val appDrawerWorkspaceEdgeSwipeEnabled = extensions.appDrawerWorkspaceEdgeSwipeEnabled
    val newAppCutoffMillis = extensions.newAppCutoffMillis
    val newAppEmblemShape = extensions.newAppEmblemShape
    val newAppEmblemSize = extensions.newAppEmblemSize
    val newAppEmblemColor = extensions.newAppEmblemColor
    val newAppEmblemColorB = extensions.newAppEmblemColorB
    val newAppEmblemGradient = extensions.newAppEmblemGradient
    val newAppEmblemX = extensions.newAppEmblemX
    val newAppEmblemY = extensions.newAppEmblemY
    val newAppEmblemImageUri = extensions.newAppEmblemImageUri
    val pinchAction = extensions.pinchAction
    val multiSwipeActions = extensions.multiSwipeActions
    val bottomDockCapacity = extensions.bottomDockCapacity
    val keepUninstalledAppShortcuts = extensions.keepUninstalledAppShortcuts
    val onAppLaunchDelayChange = extensions.onAppLaunchDelayChange
    val onAppLaunchAnimationDurationChange = extensions.onAppLaunchAnimationDurationChange
    val onAppDragAnimationEffectChange = extensions.onAppDragAnimationEffectChange
    val onAppDragAnimationDurationChange = extensions.onAppDragAnimationDurationChange
    val onLinkLaunchDelayToAnimationChange = extensions.onLinkLaunchDelayToAnimationChange
    val onAppDrawerDragOverviewModeChange = extensions.onAppDrawerDragOverviewModeChange
    val onAppDrawerCrossPageDragModeChange = extensions.onAppDrawerCrossPageDragModeChange
    val onAppDrawerPagePreviewModeChange = extensions.onAppDrawerPagePreviewModeChange
    val onAppDrawerDragPageHoverChange = extensions.onAppDrawerDragPageHoverChange
    val onAppDrawerQuickActionChange = extensions.onAppDrawerQuickActionChange
    val onAppDrawerWorkspaceEdgeSwipeEnabledChange = extensions.onAppDrawerWorkspaceEdgeSwipeEnabledChange
    val onNewAppCutoffChange = extensions.onNewAppCutoffChange
    val onNewAppEmblemShapeChange = extensions.onNewAppEmblemShapeChange
    val onNewAppEmblemSizeChange = extensions.onNewAppEmblemSizeChange
    val onNewAppEmblemColorChange = extensions.onNewAppEmblemColorChange
    val onNewAppEmblemColorBChange = extensions.onNewAppEmblemColorBChange
    val onNewAppEmblemGradientChange = extensions.onNewAppEmblemGradientChange
    val onNewAppEmblemXChange = extensions.onNewAppEmblemXChange
    val onNewAppEmblemYChange = extensions.onNewAppEmblemYChange
    val onChooseNewAppEmblemImage = extensions.onChooseNewAppEmblemImage
    val onClearNewAppEmblemImage = extensions.onClearNewAppEmblemImage
    val onChooseNewAppEmblemIconPack = extensions.onChooseNewAppEmblemIconPack
    val onClearNewAppEmblemIconPack = extensions.onClearNewAppEmblemIconPack
    val onPinchActionChange = extensions.onPinchActionChange
    val onMultiSwipeActionChange = extensions.onMultiSwipeActionChange
    val onBottomDockCapacityChange = extensions.onBottomDockCapacityChange
    val onKeepUninstalledAppShortcutsChange = extensions.onKeepUninstalledAppShortcutsChange
    val advancedContext = LocalContext.current
    var pendingReset by remember { mutableStateOf<LauncherResetArea?>(null) }
    var confirmFullResetAgain by remember { mutableStateOf(false) }
    var confirmClearWorkspace by remember { mutableStateOf(false) }
    var advancedPreferences by remember {
        mutableStateOf(AdvancedLauncherStorage.load(advancedContext.applicationContext))
    }
    var securityDraft by remember { mutableStateOf(advancedPreferences) }
    var page by remember(initialPage) { mutableStateOf(initialPage) }
    DisposableEffect(page) {
        onSideDockSettingsPreviewChange(page == LauncherSettingsPage.SIDE_DOCK)
        onWidgetCheckmarkPreviewChange(page == LauncherSettingsPage.WIDGET_EDITING)
        onDispose {
            onSideDockSettingsPreviewChange(false)
            onWidgetCheckmarkPreviewChange(false)
        }
    }
    // Keep a separate scroll position for every settings page while this dialog
    // is open. This preserves a parent's position on return from a child page,
    // without inheriting the child's position or affecting unrelated sections.
    // Closing and reopening the dialog creates fresh states (ROOT starts at top).
    val settingsScrollStates = remember {
        LauncherSettingsPage.values().associateWith { ScrollState(0) }
    }
    val defaultSettingsScrollState = settingsScrollStates.getValue(page)
    // Effect pickers intentionally scroll to the selected effect on every entry.
    // Do not reuse their previous scroll position or the regular settings state.
    val effectSettingsScrollState = remember(page) { ScrollState(0) }
    var widgetActionEditor by remember { mutableStateOf<WidgetActionChain?>(null) }
    var warning by remember { mutableStateOf<String?>(null) }
    var sliderDragging by remember { mutableStateOf(false) }
    var workspaceEffectAtOpen by remember { mutableStateOf(transitionEffect) }
    var workspaceEffectDraft by remember { mutableStateOf(transitionEffect) }
    var workspaceEffectSearch by remember { mutableStateOf("") }
    var appDrawerEffectAtOpen by remember { mutableStateOf(appDrawerPageTransitionEffect) }
    var appDrawerEffectDraft by remember(appDrawerPageTransitionEffect) {
        mutableStateOf(appDrawerPageTransitionEffect)
    }
    var appDrawerEffectSearch by remember { mutableStateOf("") }
    var workspaceEffectListOpenSerial by remember { mutableIntStateOf(0) }
    var drawerEffectListOpenSerial by remember { mutableIntStateOf(0) }
    var effectGroupExpansion by remember(advancedContext) {
        mutableStateOf(
            PageEffectTechnologyGroup.values().associateWith { group ->
                LauncherStorage.loadEffectGroupExpanded(advancedContext.applicationContext, group)
            }
        )
    }
    fun setEffectGroupExpanded(group: PageEffectTechnologyGroup, expanded: Boolean) {
        effectGroupExpansion = effectGroupExpansion + (group to expanded)
        LauncherStorage.saveEffectGroupExpanded(
            advancedContext.applicationContext,
            group,
            expanded
        )
    }
    LaunchedEffect(page) {
        if (page == LauncherSettingsPage.WORKSPACE_EFFECTS) {
            workspaceEffectAtOpen = transitionEffect
            workspaceEffectDraft = transitionEffect
            workspaceEffectSearch = ""
            if (effectGroupExpansion[transitionEffect.technologyGroup] == false) {
                setEffectGroupExpanded(transitionEffect.technologyGroup, true)
            }
            workspaceEffectListOpenSerial++
        }
        if (page == LauncherSettingsPage.APP_DRAWER_PAGE_EFFECTS) {
            appDrawerEffectAtOpen = appDrawerPageTransitionEffect
            appDrawerEffectDraft = appDrawerPageTransitionEffect
            appDrawerEffectSearch = ""
            if (effectGroupExpansion[appDrawerPageTransitionEffect.technologyGroup] == false) {
                setEffectGroupExpanded(appDrawerPageTransitionEffect.technologyGroup, true)
            }
            drawerEffectListOpenSerial++
        }
    }
    val configuration = LocalConfiguration.current
    val effectiveAppDrawerScreenHeightDp = appDrawerScreenHeightDp
        .takeIf { it > 0 }
        ?: configuration.screenHeightDp
    val availableGridWidth = (configuration.screenWidthDp - 52).coerceAtLeast(120)
    // The optional search row overlays the paged drawer and must not reduce the icon grid.
    val availableGridHeight = (effectiveAppDrawerScreenHeightDp - 140).coerceAtLeast(160)

    fun horizontalSpacingDp(columns: Int): Int = when {
        columns >= 9 -> 2
        columns >= 7 -> 4
        else -> 8
    }
    fun verticalSpacingDp(rows: Int): Int = when {
        rows >= 16 -> 1
        rows >= 11 -> 2
        rows >= 8 -> 4
        else -> 8
    }
    fun maxItemWidth(columns: Int = appDrawerColumns): Int =
        ((availableGridWidth - (columns - 1) * horizontalSpacingDp(columns)) / columns).coerceAtLeast(12)
    fun maxItemHeight(rows: Int = appDrawerRows): Int =
        ((availableGridHeight - (rows - 1) * verticalSpacingDp(rows)) / rows).coerceAtLeast(12)

    fun parentOf(current: LauncherSettingsPage): LauncherSettingsPage = when (current) {
        LauncherSettingsPage.ROOT -> LauncherSettingsPage.ROOT
        LauncherSettingsPage.SECURITY -> LauncherSettingsPage.ROOT
        LauncherSettingsPage.SECURITY_RESET -> LauncherSettingsPage.SECURITY
        LauncherSettingsPage.LANGUAGE -> LauncherSettingsPage.ROOT
        LauncherSettingsPage.WORKSPACES,
        LauncherSettingsPage.GESTURES,
        LauncherSettingsPage.SIDE_DOCK,
        LauncherSettingsPage.BOTTOM_DOCK,
        LauncherSettingsPage.APP_DRAWER,
        LauncherSettingsPage.ENVIRONMENT_EFFECTS,
        LauncherSettingsPage.WALLPAPER,
        LauncherSettingsPage.FOLDERS,
        LauncherSettingsPage.WIDGETS -> LauncherSettingsPage.ROOT
        LauncherSettingsPage.INTERFACE -> LauncherSettingsPage.ROOT
        LauncherSettingsPage.DIALOGS -> LauncherSettingsPage.INTERFACE
        LauncherSettingsPage.WORKSPACE_PAGES,
        LauncherSettingsPage.WORKSPACE_EFFECTS -> LauncherSettingsPage.WORKSPACES
        LauncherSettingsPage.BOTTOM_DOCK_APPEARANCE -> LauncherSettingsPage.BOTTOM_DOCK
        LauncherSettingsPage.GESTURE_SWIPE_DOWN,
        LauncherSettingsPage.GESTURE_SWIPE_UP,
        LauncherSettingsPage.GESTURE_DOUBLE_TAP -> LauncherSettingsPage.GESTURES
        LauncherSettingsPage.APP_DRAWER_LAYOUT,
        LauncherSettingsPage.APP_DRAWER_GRID,
        LauncherSettingsPage.APP_DRAWER_ITEM_STYLE,
        LauncherSettingsPage.APP_DRAWER_PAGE_EFFECTS,
        LauncherSettingsPage.APP_DRAWER_ICON_EFFECTS,
        LauncherSettingsPage.APP_DRAWER_INTERACTION,
        LauncherSettingsPage.APP_DRAWER_NEW_APPS,
        LauncherSettingsPage.APP_DRAWER_BACKGROUND,
        LauncherSettingsPage.APP_DRAWER_HIDDEN_APPS,
        LauncherSettingsPage.APP_DRAWER_QUICK_ACTION -> LauncherSettingsPage.APP_DRAWER
        LauncherSettingsPage.APP_LAUNCH_ANIMATION -> LauncherSettingsPage.ROOT
        LauncherSettingsPage.WALLPAPER_ANIMATED -> LauncherSettingsPage.WALLPAPER
        LauncherSettingsPage.WIDGET_EDITING,
        LauncherSettingsPage.WIDGET_PICKER,
        LauncherSettingsPage.WIDGET_ACTIONS -> LauncherSettingsPage.WIDGETS
    }

    val title = when (page) {
        LauncherSettingsPage.ROOT -> l10n(R.string.ui_launcher_settings)
        LauncherSettingsPage.SECURITY -> l10n(R.string.ui_reset_security_title)
        LauncherSettingsPage.SECURITY_RESET -> l10n(R.string.ui_reset_settings_title)
        LauncherSettingsPage.LANGUAGE -> l10n(R.string.ui_language)
        LauncherSettingsPage.INTERFACE -> l10n(R.string.ui_launcher_interface)
        LauncherSettingsPage.DIALOGS -> l10n(R.string.ui_dialog_windows)
        LauncherSettingsPage.WORKSPACES -> l10n(R.string.ui_desktops_0348)
        LauncherSettingsPage.WORKSPACE_PAGES -> l10n(R.string.ui_desktop_pages)
        LauncherSettingsPage.WORKSPACE_EFFECTS -> l10n(R.string.ui_desktop_effect_0499)
        LauncherSettingsPage.GESTURES -> l10n(R.string.ui_desktop_gestures)
        LauncherSettingsPage.GESTURE_SWIPE_DOWN -> l10n(R.string.ui_swipe_down)
        LauncherSettingsPage.GESTURE_SWIPE_UP -> l10n(R.string.ui_swipe_up)
        LauncherSettingsPage.GESTURE_DOUBLE_TAP -> l10n(R.string.ui_double_tap)
        LauncherSettingsPage.SIDE_DOCK -> l10n(R.string.ui_side_dock)
        LauncherSettingsPage.BOTTOM_DOCK -> l10n(R.string.ui_bottom_dock)
        LauncherSettingsPage.BOTTOM_DOCK_APPEARANCE -> l10n(R.string.ui_bottom_dock_appearance)
        LauncherSettingsPage.APP_DRAWER -> l10n(R.string.ui_app_drawer)
        LauncherSettingsPage.APP_DRAWER_LAYOUT -> l10n(R.string.ui_app_drawer_layout)
        LauncherSettingsPage.APP_DRAWER_GRID -> l10n(R.string.ui_icon_grid_and_size)
        LauncherSettingsPage.APP_DRAWER_ITEM_STYLE -> l10n(R.string.ui_icon_frame)
        LauncherSettingsPage.APP_DRAWER_PAGE_EFFECTS -> l10n(R.string.ui_app_drawer_page_effect)
        LauncherSettingsPage.APP_DRAWER_ICON_EFFECTS -> l10n(R.string.ui_app_drawer_icon_effect)
        LauncherSettingsPage.APP_DRAWER_INTERACTION -> l10n(R.string.ui_app_drawer_dragging)
        LauncherSettingsPage.APP_DRAWER_NEW_APPS -> l10n(R.string.ui_new_app_emblem)
        LauncherSettingsPage.APP_DRAWER_BACKGROUND -> l10n(R.string.ui_app_drawer_background_0463)
        LauncherSettingsPage.APP_DRAWER_HIDDEN_APPS -> l10n(R.string.ui_hidden_applications)
        LauncherSettingsPage.APP_DRAWER_QUICK_ACTION -> l10n(R.string.ui_quick_action_button)
        LauncherSettingsPage.APP_LAUNCH_ANIMATION -> l10n(R.string.ui_icon_activation)
        LauncherSettingsPage.ENVIRONMENT_EFFECTS -> l10n(R.string.ui_environment_effects)
        LauncherSettingsPage.WALLPAPER -> l10n(R.string.ui_wallpaper)
        LauncherSettingsPage.WALLPAPER_ANIMATED -> l10n(R.string.ui_animated_background)
        LauncherSettingsPage.FOLDERS -> l10n(R.string.ui_folders)
        LauncherSettingsPage.WIDGETS -> l10n(R.string.ui_widgets)
        LauncherSettingsPage.WIDGET_EDITING -> l10n(R.string.ui_widget_editing)
        LauncherSettingsPage.WIDGET_PICKER -> l10n(R.string.ui_widget_list_display)
        LauncherSettingsPage.WIDGET_ACTIONS -> l10n(R.string.ui_widget_tap_actions)
    }

    AlertDialog(
        styleOverride = dialogStyle,
        onDismissRequest = onDismiss,
        modifier = Modifier.alpha(
            if (sliderDragging && page in setOf(
                    LauncherSettingsPage.APP_DRAWER_GRID,
                    LauncherSettingsPage.APP_DRAWER_ITEM_STYLE,
                    LauncherSettingsPage.APP_DRAWER_NEW_APPS
                )
            ) 0.20f else 1f
        ),
        title = { Text(title) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().heightIn(max = 590.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (page == LauncherSettingsPage.WORKSPACE_EFFECTS) {
                    InteractivePageEffectPreview(
                        effect = workspaceEffectDraft,
                        appDrawer = false,
                        showHint = false,
                        showPreviewLabel = true
                    )
                    WorkspaceEffectSearchRow(
                        value = workspaceEffectSearch,
                        onValueChange = { workspaceEffectSearch = it },
                        onUndo = {
                            workspaceEffectDraft = workspaceEffectAtOpen
                            onTransitionEffectChange(workspaceEffectAtOpen)
                        }
                    )
                }
                if (page == LauncherSettingsPage.APP_DRAWER_PAGE_EFFECTS) {
                    InteractivePageEffectPreview(
                        effect = appDrawerEffectDraft,
                        appDrawer = true,
                        verticalPager = appDrawerMode == AppDrawerMode.VERTICAL_PAGES,
                        showHint = false,
                        showPreviewLabel = true
                    )
                    WorkspaceEffectSearchRow(
                        value = appDrawerEffectSearch,
                        onValueChange = { appDrawerEffectSearch = it },
                        onUndo = {
                            appDrawerEffectDraft = appDrawerEffectAtOpen
                            onAppDrawerPageTransitionChange(appDrawerEffectAtOpen)
                        }
                    )
                }
                // This warning stays above the scrolling Pager/Canvas list. It
                // concerns the currently selected page effect only; Android Mesh
                // has its own incompatibility message on the icon-effect page.
                if (page == LauncherSettingsPage.APP_DRAWER_PAGE_EFFECTS &&
                    appDrawerPageTransitionEffect.technologyGroup != PageEffectTechnologyGroup.ANDROID_MESH &&
                    appDrawerIconEffect in setOf(
                        AppDrawerIconEffect.SPIRAL, AppDrawerIconEffect.FIREWORK,
                        AppDrawerIconEffect.BLACK_HOLE, AppDrawerIconEffect.BIG_BANG
                    )) {
                    Text(
                        l10n(R.string.ui_page_effect_overridden_by_icon_effect),
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 6.dp, vertical = 4.dp),
                        color = Color(0xFFFF7885), fontWeight = FontWeight.SemiBold
                    )
                }
                if (page == LauncherSettingsPage.APP_DRAWER_ICON_EFFECTS &&
                    appDrawerPageTransitionEffect.mesh) {
                    Text(
                        text = l10n(R.string.ui_icon_effect_disabled_by_android_mesh),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 6.dp, vertical = 8.dp),
                        color = Color(0xFFFF4D5E),
                        fontWeight = FontWeight.Bold
                    )
                }
                if (page == LauncherSettingsPage.APP_LAUNCH_ANIMATION) {
                    IconActivationSettingsPreview(
                        effect = appLaunchAnimationEffect,
                        durationMillis = appLaunchAnimationDuration
                    )
                }
                Column(
                    modifier = Modifier.fillMaxWidth().weight(1f).verticalScroll(
                        if (page == LauncherSettingsPage.WORKSPACE_EFFECTS ||
                            page == LauncherSettingsPage.APP_DRAWER_PAGE_EFFECTS
                        ) effectSettingsScrollState else defaultSettingsScrollState
                    ),
                    verticalArrangement = Arrangement.spacedBy(9.dp)
                ) {
                warning?.let {
                    Card(colors = CardDefaults.cardColors(containerColor = Color(0x554D381B))) {
                        Text(it, modifier = Modifier.padding(12.dp), color = Color(0xFFFFD18B))
                    }
                }
                when (page) {
                    LauncherSettingsPage.ROOT -> {
                        val context = LocalContext.current
                        SettingsNavigationItem(
                            languageSettingsTitle(),
                            currentLauncherLanguage(context).title
                        ) { page = LauncherSettingsPage.LANGUAGE }
                        SettingsNavigationItem(
                            l10n(R.string.ui_launcher_interface),
                            "${launcherInterfaceStyle.backgroundMode.title} · ${launcherInterfaceStyle.opacityPercent}%"
                        ) { page = LauncherSettingsPage.INTERFACE }
                        SettingsNavigationItem(l10n(R.string.ui_desktops_0348), l10n(R.string.ui_desktops, workspaceCount, transitionEffect.title)) { page = LauncherSettingsPage.WORKSPACES }
                        SettingsNavigationItem(l10n(R.string.ui_gestures), l10n(R.string.ui_down_up, swipeDownAction.title, swipeUpAction.title)) { page = LauncherSettingsPage.GESTURES }
                        SettingsNavigationItem(l10n(R.string.ui_side_dock), "${sideDockPosition.title} · ${sideDockOpacity}%") { page = LauncherSettingsPage.SIDE_DOCK }
                        SettingsNavigationItem(l10n(R.string.ui_bottom_dock), l10n(R.string.ui_pages, bottomDockPageCount, bottomDockAlignment.title)) { page = LauncherSettingsPage.BOTTOM_DOCK }
                        SettingsNavigationItem(l10n(R.string.ui_app_drawer), appDrawerMode.title) { page = LauncherSettingsPage.APP_DRAWER }
                        SettingsNavigationItem(l10n(R.string.ui_icon_activation), appLaunchAnimationEffect.title) { page = LauncherSettingsPage.APP_LAUNCH_ANIMATION }
                        val activeEnvironmentCount = EnvironmentEffectType.values().count { environmentEffects.options(it).enabled }
                        SettingsNavigationItem(
                            l10n(R.string.ui_environment_effects),
                            if (activeEnvironmentCount == 0) l10n(R.string.ui_effects_disabled) else l10n(R.string.ui_active, activeEnvironmentCount)
                        ) { page = LauncherSettingsPage.ENVIRONMENT_EFFECTS }
                        SettingsNavigationItem(l10n(R.string.ui_folders), l10n(R.string.ui_chain_speed, folderFanSpeed)) { page = LauncherSettingsPage.FOLDERS }
                        SettingsNavigationItem(l10n(R.string.ui_widgets), widgetPickerMode.title) { page = LauncherSettingsPage.WIDGETS }
                        SettingsNavigationItem(l10n(R.string.ui_wallpaper), wallpaperMode.title) { page = LauncherSettingsPage.WALLPAPER }
                        SettingsNavigationItem(
                            l10n(R.string.ui_reset_security_title),
                            l10n(R.string.ui_reset_security_summary)
                        ) {
                            securityDraft = AdvancedLauncherStorage.load(advancedContext.applicationContext)
                            page = LauncherSettingsPage.SECURITY
                        }
                        SettingsSectionTitle(l10n(R.string.ui_maintenance))
                        Button(onClick = onRestartLauncher, modifier = Modifier.fillMaxWidth()) {
                            Text(l10n(R.string.ui_restart_launcher))
                        }
                        SettingsSectionTitle(l10n(R.string.ui_backup_and_restore))
                        Button(onClick = onCreateBackup, modifier = Modifier.fillMaxWidth()) { Text(l10n(R.string.ui_create_backup)) }
                        OutlinedButton(onClick = onRestoreBackup, modifier = Modifier.fillMaxWidth()) { Text(l10n(R.string.ui_restore_from_backup)) }
                        SystemBackupSetting()
                    }

                    LauncherSettingsPage.SECURITY -> {
                        SettingsSectionTitle(l10n(R.string.ui_protection_and_compatibility))
                        AdvancedFeaturesSettings(
                            value = securityDraft,
                            onValueChange = { securityDraft = it },
                            onRequestHomeRole = { (advancedContext as? MainActivity)?.requestHomeRole() }
                        )
                        Button(
                            onClick = {
                                advancedPreferences = securityDraft
                                AdvancedLauncherStorage.save(advancedContext.applicationContext, securityDraft)
                                (advancedContext as? MainActivity)?.recreate()
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text(l10n(R.string.ui_save)) }
                        SettingsSectionTitle(l10n(R.string.ui_reset_settings_title), Color(0xFFDC143C))
                        OutlinedButton(
                            onClick = { page = LauncherSettingsPage.SECURITY_RESET },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text(l10n(R.string.ui_reset_settings_title), color = Color(0xFFDC143C)) }
                    }

                    LauncherSettingsPage.SECURITY_RESET -> {
                        Text(
                            l10n(R.string.ui_reset_settings_hint),
                            color = Color(0xFFDC143C),
                            style = MaterialTheme.typography.bodyMedium
                        )
                        listOf(
                            LauncherResetArea.WORKSPACE to R.string.ui_reset_workspace,
                            LauncherResetArea.APP_DRAWER to R.string.ui_reset_app_drawer,
                            LauncherResetArea.SIDE_DOCK to R.string.ui_reset_side_dock,
                            LauncherResetArea.BOTTOM_DOCK to R.string.ui_reset_bottom_dock,
                            LauncherResetArea.INTERFACE to R.string.ui_reset_interface,
                            LauncherResetArea.ALL to R.string.ui_reset_all_launcher
                        ).forEach { (area, label) ->
                            if (area == LauncherResetArea.ALL) {
                                Button(
                                    onClick = { pendingReset = area; confirmFullResetAgain = false },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC143C))
                                ) { Text(l10n(label), color = Color.White) }
                            } else {
                                OutlinedButton(
                                    onClick = { pendingReset = area; confirmFullResetAgain = false },
                                    modifier = Modifier.fillMaxWidth()
                                ) { Text(l10n(label), color = Color(0xFFDC143C)) }
                            }
                        }
                    }
                    LauncherSettingsPage.LANGUAGE -> LauncherLanguageSettings()
                    LauncherSettingsPage.INTERFACE -> {
                        SettingsSectionTitle(l10n(R.string.ui_interface_background))
                        LauncherInterfaceBackgroundMode.values().forEach { mode ->
                            SimpleRadioOption(mode.title, mode == launcherInterfaceStyle.backgroundMode) {
                                onLauncherInterfaceStyleChange(launcherInterfaceStyle.copy(
                                    backgroundMode = mode,
                                    opacityPercent = if (mode == LauncherInterfaceBackgroundMode.NONE) {
                                        launcherInterfaceStyle.opacityPercent
                                    } else launcherInterfaceStyle.opacityPercent.coerceAtLeast(35)
                                ))
                            }
                        }
                        if (launcherInterfaceStyle.backgroundMode != LauncherInterfaceBackgroundMode.NONE) {
                            VisualColorSetting(l10n(R.string.ui_primary_colour), launcherInterfaceStyle.colorAHex) {
                                onLauncherInterfaceStyleChange(launcherInterfaceStyle.copy(colorAHex = it))
                            }
                            if (launcherInterfaceStyle.backgroundMode == LauncherInterfaceBackgroundMode.GRADIENT) {
                                VisualColorSetting(l10n(R.string.ui_secondary_colour), launcherInterfaceStyle.colorBHex) {
                                    onLauncherInterfaceStyleChange(launcherInterfaceStyle.copy(colorBHex = it))
                                }
                            }
                            if (launcherInterfaceStyle.backgroundMode == LauncherInterfaceBackgroundMode.IMAGE) {
                                Button(onClick = onChooseLauncherInterfaceImage, modifier = Modifier.fillMaxWidth()) {
                                    Text(if (launcherInterfaceStyle.imageUri.isNullOrBlank()) l10n(R.string.ui_choose_picture) else l10n(R.string.ui_replace_picture))
                                }
                            }
                            IntegerSliderSetting(l10n(R.string.ui_opacity), launcherInterfaceStyle.opacityPercent, 0, 100, onDraggingChange = { sliderDragging = it }) {
                                onLauncherInterfaceStyleChange(launcherInterfaceStyle.copy(opacityPercent = it))
                            }
                            IntegerSliderSetting(l10n(R.string.ui_blur_dp), launcherInterfaceStyle.blurDp, 0, 48, onDraggingChange = { sliderDragging = it }) {
                                onLauncherInterfaceStyleChange(launcherInterfaceStyle.copy(blurDp = it))
                            }
                        }
                        SettingsSectionTitle(l10n(R.string.ui_text))
                        VisualColorSetting(l10n(R.string.ui_desktop_text_colour), launcherInterfaceStyle.textColorHex) {
                            onLauncherInterfaceStyleChange(launcherInterfaceStyle.copy(textColorHex = it))
                        }
                        TextShadowSettingsBlock(textShadowSettings, onTextShadowSettingsChange)
                        SettingsNavigationItem(
                            l10n(R.string.ui_dialog_windows),
                            "${launcherDialogStyle.backgroundMode.title} · ${launcherDialogStyle.opacityPercent}%"
                        ) { page = LauncherSettingsPage.DIALOGS }
                    }
                    LauncherSettingsPage.DIALOGS -> LauncherDialogStyleSettings(
                        value = launcherDialogStyle,
                        onValueChange = onLauncherDialogStyleChange,
                        onChooseImage = onChooseLauncherDialogImage,
                        onDraggingChange = { sliderDragging = it }
                    )
                    LauncherSettingsPage.WORKSPACES -> {
                        SettingsNavigationItem(l10n(R.string.ui_count_and_looping), l10n(R.string.ui_desktops, workspaceCount, if (workspaceLoop) l10n(R.string.ui_looping_0479) else l10n(R.string.ui_bounded))) { page = LauncherSettingsPage.WORKSPACE_PAGES }
                        SettingsNavigationItem(l10n(R.string.ui_page_transition_effect), transitionEffect.title) { page = LauncherSettingsPage.WORKSPACE_EFFECTS }
                        SettingsToggleRow(
                            title = l10n(R.string.ui_prepare_mesh_preview_after_idle),
                            description = l10n(R.string.ui_prepare_mesh_preview_after_idle_description),
                            checked = meshSnapshotAfterIdle,
                            onCheckedChange = onMeshSnapshotAfterIdleChange
                        )
                        SettingsSectionTitle(l10n(R.string.ui_page_overview))
                        SimpleRadioOption(
                            l10n(R.string.ui_overview_navigation_only),
                            workspaceOverviewMode == WorkspaceOverviewMode.NAVIGATION_ONLY
                        ) {
                            advancedPreferences = advancedPreferences.copy(
                                workspaceOverviewMode = WorkspaceOverviewMode.NAVIGATION_ONLY
                            )
                            onWorkspaceOverviewModeChange(WorkspaceOverviewMode.NAVIGATION_ONLY)
                        }
                        SimpleRadioOption(
                            l10n(R.string.ui_overview_editing),
                            workspaceOverviewMode == WorkspaceOverviewMode.EDITING
                        ) {
                            advancedPreferences = advancedPreferences.copy(
                                workspaceOverviewMode = WorkspaceOverviewMode.EDITING
                            )
                            onWorkspaceOverviewModeChange(WorkspaceOverviewMode.EDITING)
                        }
                        OutlinedButton(
                            onClick = onOpenWorkspaceOverview,
                            modifier = Modifier.fillMaxWidth()
                        ) { Text(l10n(R.string.ui_open_page_overview)) }
                        SettingsSectionTitle(l10n(R.string.ui_workspace_cleanup_title))
                        Text(l10n(R.string.ui_clear_workspace_items_hint), style = MaterialTheme.typography.bodySmall)
                        OutlinedButton(
                            onClick = { confirmClearWorkspace = true },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text(l10n(R.string.ui_clear_workspace_items), color = Color(0xFFDC143C)) }
                    }
                    LauncherSettingsPage.WORKSPACE_PAGES -> {
                        IntegerSliderSetting(l10n(R.string.ui_desktop_count), workspaceCount, 1, 9, onDraggingChange = { sliderDragging = it }) { onWorkspaceCountChange(it) }
                        IntegerSliderSetting(l10n(R.string.ui_default_page), defaultWorkspacePage + 1, 1, workspaceCount, onDraggingChange = { sliderDragging = it }) { onDefaultWorkspacePageChange(it - 1) }
                        SettingsToggleRow(l10n(R.string.ui_looping), l10n(R.string.ui_the_first_desktop_follows_the_last_desktop), workspaceLoop, onWorkspaceLoopChange)
                    }
                    LauncherSettingsPage.WORKSPACE_EFFECTS -> {
                        PageTransitionEffectList(
                            query = workspaceEffectSearch,
                            includeAppDrawerOnly = false,
                            selectedEffect = workspaceEffectDraft,
                            selectedAtOpen = workspaceEffectAtOpen,
                            openingSerial = workspaceEffectListOpenSerial,
                            groupExpansion = effectGroupExpansion,
                            onGroupExpansionChange = { group, expanded ->
                                setEffectGroupExpanded(group, expanded)
                            }
                        ) { effect ->
                            workspaceEffectDraft = effect
                            onTransitionEffectChange(effect)
                        }
                        MinimumPageEffectDurationSetting(
                            l10n(R.string.ui_min_page_effect_duration),
                            l10n(R.string.ui_min_page_effect_default),
                            pageEffectTiming.workspaceMs,
                            onDraggingChange = { sliderDragging = it },
                            onValueChange = pageEffectTiming::setWorkspace
                        )
                    }
                    LauncherSettingsPage.GESTURES -> {
                        SettingsToggleRow(l10n(R.string.ui_ignore_gestures_on_widgets),
                            l10n(R.string.ui_ignore_gestures_on_widgets_description),
                            ignoreGesturesOnWidgets, onIgnoreGesturesOnWidgetsChange)
                        SettingsNavigationItem(l10n(R.string.ui_swipe_down), swipeDownAction.title) { page = LauncherSettingsPage.GESTURE_SWIPE_DOWN }
                        SettingsNavigationItem(l10n(R.string.ui_swipe_up), swipeUpAction.title) { page = LauncherSettingsPage.GESTURE_SWIPE_UP }
                        SettingsNavigationItem(l10n(R.string.ui_double_tap), doubleTapAction.title) { page = LauncherSettingsPage.GESTURE_DOUBLE_TAP }
                        SettingsSectionTitle(l10n(R.string.ui_pinch))
                        GestureCycleSetting(l10n(R.string.ui_pinch), pinchAction, onPinchActionChange)
                        for (fingers in 2..3) {
                            SettingsSectionTitle(if (fingers == 2) l10n(R.string.ui_two_finger_gestures) else l10n(R.string.ui_three_finger_gestures))
                            SwipeDirection.values().forEach { direction ->
                                val label = when (direction) {
                                    SwipeDirection.UP -> l10n(R.string.ui_swipe_up)
                                    SwipeDirection.DOWN -> l10n(R.string.ui_swipe_down)
                                    SwipeDirection.LEFT -> l10n(R.string.ui_swipe_left)
                                    SwipeDirection.RIGHT -> l10n(R.string.ui_swipe_right)
                                }
                                GestureCycleSetting(label, multiSwipeActions[fingers to direction] ?: DesktopGestureAction.NONE) {
                                    onMultiSwipeActionChange(fingers, direction, it)
                                }
                            }
                        }
                        SettingsHint(l10n(R.string.ui_multifinger_pinch_conflict_hint))
                    }
                    LauncherSettingsPage.GESTURE_SWIPE_DOWN -> DesktopGestureAction.values().forEach { action -> SimpleRadioOption(action.title, action == swipeDownAction) { onSwipeDownActionChange(action) } }
                    LauncherSettingsPage.GESTURE_SWIPE_UP -> DesktopGestureAction.values().forEach { action -> SimpleRadioOption(action.title, action == swipeUpAction) { onSwipeUpActionChange(action) } }
                    LauncherSettingsPage.GESTURE_DOUBLE_TAP -> DesktopGestureAction.values().forEach { action -> SimpleRadioOption(action.title, action == doubleTapAction) { onDoubleTapActionChange(action) } }
                    LauncherSettingsPage.SIDE_DOCK -> {
                        SettingsSectionTitle(l10n(R.string.ui_position))
                        SideDockPosition.values().forEach { position -> SimpleRadioOption(position.title, position == sideDockPosition) { onSideDockPositionChange(position) } }
                        IntegerSliderSetting(l10n(R.string.ui_background_opacity), sideDockOpacity, 0, 100, onDraggingChange = { sliderDragging = it }, onValueChange = onSideDockOpacityChange)
                        VisualColorSetting(l10n(R.string.ui_text_colour), sideDockTextColor, onSideDockTextColorChange)
                        TextShadowSettingsBlock(textShadowSettings, onTextShadowSettingsChange)
                    }
                    LauncherSettingsPage.BOTTOM_DOCK -> {
                        IntegerSliderSetting(l10n(R.string.ui_page_count), bottomDockPageCount, 1, 5, onDraggingChange = { sliderDragging = it }, onValueChange = onBottomDockPageCountChange)
                        IntegerSliderSetting(l10n(R.string.ui_icons_per_dock_page), bottomDockCapacity, 1, 10, onDraggingChange = { sliderDragging = it }, onValueChange = onBottomDockCapacityChange)
                        IntegerSliderSetting(
                            l10n(R.string.ui_folder_creation_delay_ms),
                            bottomDockFolderCreationDelay,
                            300,
                            2000,
                            onDraggingChange = { sliderDragging = it },
                            onValueChange = onBottomDockFolderCreationDelayChange
                        )
                        SettingsHint(l10n(R.string.ui_folder_creation_delay_hint))
                        SettingsToggleRow(l10n(R.string.ui_looping), l10n(R.string.ui_disabled_by_default), bottomDockLoop, onBottomDockLoopChange)
                        SettingsToggleRow(
                            l10n(R.string.ui_show_page_indicator),
                            l10n(R.string.ui_page_indicator_is_positioned_above_the_highest_dock_element),
                            bottomDockPageIndicatorVisible,
                            onBottomDockPageIndicatorVisibleChange
                        )
                        IntegerSliderSetting(
                            l10n(R.string.ui_page_indicator_gap_dp),
                            bottomDockPageIndicatorGap,
                            0,
                            32,
                            enabled = bottomDockPageIndicatorVisible,
                            onDraggingChange = { sliderDragging = it },
                            onValueChange = onBottomDockPageIndicatorGapChange
                        )
                        SettingsHint(l10n(R.string.ui_page_indicator_gap_hint))
                        SettingsToggleRow(
                            l10n(R.string.ui_tap_page_indicator),
                            l10n(R.string.ui_show_or_hide_the_bottom_dock),
                            bottomDockIndicatorToggle,
                            onBottomDockIndicatorToggleChange,
                            enabled = bottomDockPageIndicatorVisible
                        )
                        BottomDockAddControls(
                            addButtonEnabled = bottomDockAddButtonEnabled,
                            onAddButtonEnabledChange = onBottomDockAddButtonEnabledChange,
                            onAddToCurrentPage = onOpenBottomDockAdd
                        )
                        SettingsSectionTitle(l10n(R.string.ui_icon_alignment))
                        BottomDockAlignment.values().forEach { alignment ->
                            SimpleRadioOption(alignment.title, alignment == bottomDockAlignment) { onBottomDockAlignmentChange(alignment) }
                        }
                        SettingsNavigationItem(l10n(R.string.ui_appearance), l10n(R.string.ui_background_shelf_dp, bottomDockBackgroundOpacity, bottomDockShelfHeight)) { page = LauncherSettingsPage.BOTTOM_DOCK_APPEARANCE }
                        SettingsHint(l10n(R.string.ui_add_icons_by_dragging_them_from_the_desktop_or))
                    }
                    LauncherSettingsPage.BOTTOM_DOCK_APPEARANCE -> {
                        SettingsSectionTitle(l10n(R.string.ui_dock_background))
                        DockShelfShape.values().forEach { shape ->
                            SimpleRadioOption(shape.title, shape == bottomDockShelfShape) { onBottomDockShelfShapeChange(shape) }
                        }
                        if (bottomDockShelfShape != DockShelfShape.NONE) {
                            IntegerSliderSetting(l10n(R.string.ui_shelf_height_dp), bottomDockShelfHeight.coerceAtLeast(1), 1, 72, onDraggingChange = { sliderDragging = it }, onValueChange = onBottomDockShelfHeightChange)
                            IntegerSliderSetting(
                                l10n(R.string.ui_shelf_vertical_position_dp),
                                bottomDockShelfVerticalOffset,
                                -32,
                                32,
                                onDraggingChange = { sliderDragging = it },
                                onValueChange = onBottomDockShelfVerticalOffsetChange
                            )
                            SettingsHint(l10n(R.string.ui_positive_values_move_the_shelf_down))
                            VisualColorSetting(l10n(R.string.ui_shelf_colour), bottomDockShelfColor, onBottomDockShelfColorChange)
                            IntegerSliderSetting(l10n(R.string.ui_shelf_opacity), bottomDockShelfOpacity, 0, 100, onDraggingChange = { sliderDragging = it }, onValueChange = onBottomDockShelfOpacityChange)
                            SettingsHint(l10n(R.string.ui_the_shelf_replaces_the_normal_background_and_is_always))
                        } else {
                            VisualColorSetting(l10n(R.string.ui_background_colour), bottomDockBackgroundColor, onBottomDockBackgroundColorChange)
                            IntegerSliderSetting(l10n(R.string.ui_background_opacity), bottomDockBackgroundOpacity, 0, 100, onDraggingChange = { sliderDragging = it }, onValueChange = onBottomDockBackgroundOpacityChange)
                        }
                        IntegerSliderSetting(
                            l10n(R.string.ui_bottom_dock_icon_vertical_position_dp),
                            bottomDockIconVerticalOffset,
                            -32,
                            32,
                            onDraggingChange = { sliderDragging = it },
                            onValueChange = onBottomDockIconVerticalOffsetChange
                        )
                        SettingsHint(l10n(R.string.ui_positive_values_move_dock_icons_down))
                        SettingsToggleRow(
                            l10n(R.string.ui_show_bottom_dock_icon_labels),
                            l10n(R.string.ui_show_application_and_action_names_below_dock_icons),
                            bottomDockLabelsVisible,
                            onBottomDockLabelsVisibleChange
                        )
                        VisualColorSetting(l10n(R.string.ui_text_colour), bottomDockTextColor, onBottomDockTextColorChange)
                        TextShadowSettingsBlock(textShadowSettings, onTextShadowSettingsChange)
                    }
                    LauncherSettingsPage.APP_DRAWER -> {
                        SettingsNavigationItem(l10n(R.string.ui_layout_and_looping), appDrawerMode.title) { page = LauncherSettingsPage.APP_DRAWER_LAYOUT }
                        val summaryWidth = if (appDrawerFillAvailable) maxItemWidth() else appDrawerItemWidth
                        val summaryHeight = if (appDrawerFillAvailable) maxItemHeight() else appDrawerItemHeight
                        SettingsNavigationItem(l10n(R.string.ui_icon_grid_and_size), "${appDrawerColumns}×${appDrawerRows} · ${summaryWidth}×${summaryHeight} dp") { page = LauncherSettingsPage.APP_DRAWER_GRID }
                        SettingsNavigationItem(l10n(R.string.ui_icon_frame), "${appDrawerFrameShape.title} · ${appDrawerFrameOpacity}%") { page = LauncherSettingsPage.APP_DRAWER_ITEM_STYLE }
                        SettingsNavigationItem(l10n(R.string.ui_page_effect), appDrawerPageTransitionEffect.title) { page = LauncherSettingsPage.APP_DRAWER_PAGE_EFFECTS }
                        SettingsNavigationItem(l10n(R.string.ui_icon_effect), appDrawerIconEffect.title) { page = LauncherSettingsPage.APP_DRAWER_ICON_EFFECTS }
                        SettingsNavigationItem(l10n(R.string.ui_icon_dragging), l10n(R.string.ui_hold_ms_edge_dp, appDrawerReorderHold, appDrawerEdgeSwitchDp)) { page = LauncherSettingsPage.APP_DRAWER_INTERACTION }
                        SettingsNavigationItem(l10n(R.string.ui_new_app_emblem), "${newAppEmblemShape.name.lowercase()} · ${newAppEmblemSize}%") { page = LauncherSettingsPage.APP_DRAWER_NEW_APPS }
                        SettingsNavigationItem(l10n(R.string.ui_app_drawer_background), "${appDrawerBackgroundMode.title} · ${appDrawerBackgroundOpacity}%") { page = LauncherSettingsPage.APP_DRAWER_BACKGROUND }
                        SettingsNavigationItem(l10n(R.string.ui_hidden_applications), l10n(R.string.ui_hidden, hiddenApps.size)) { page = LauncherSettingsPage.APP_DRAWER_HIDDEN_APPS }
                        SettingsNavigationItem(
                            l10n(R.string.ui_quick_action_button),
                            appDrawerQuickActionTitle(appDrawerQuickAction)
                        ) { page = LauncherSettingsPage.APP_DRAWER_QUICK_ACTION }
                        SettingsSectionTitle(l10n(R.string.ui_removed_applications))
                        SettingsToggleRow(
                            l10n(R.string.ui_keep_shortcuts_after_app_removal),
                            l10n(R.string.ui_keep_shortcuts_after_app_removal_hint),
                            keepUninstalledAppShortcuts,
                            onKeepUninstalledAppShortcutsChange
                        )
                    }
                    LauncherSettingsPage.APP_DRAWER_LAYOUT -> {
                        AppDrawerMode.values().forEach { mode -> AppDrawerModeOption(mode, mode == appDrawerMode) { onAppDrawerModeChange(mode) } }
                        SettingsToggleRow(l10n(R.string.ui_looping_pages), l10n(R.string.ui_applies_to_horizontal_and_vertical_pages), appDrawerLoop, onAppDrawerLoopChange, appDrawerMode != AppDrawerMode.VERTICAL_LIST)
                    }
                    LauncherSettingsPage.APP_DRAWER_GRID -> {
                        val widthMax = maxItemWidth().coerceAtLeast(12)
                        val heightMax = maxItemHeight().coerceAtLeast(12)
                        SettingsToggleRow(
                            l10n(R.string.ui_fill_app_drawer_grid),
                            l10n(R.string.ui_fill_app_drawer_grid_hint),
                            appDrawerFillAvailable,
                            onAppDrawerFillAvailableChange
                        )
                        SettingsToggleRow(
                            l10n(R.string.ui_use_system_screen_height),
                            l10n(R.string.ui_use_system_screen_height_hint),
                            appDrawerScreenHeightDp == 0,
                            onCheckedChange = { useSystemHeight ->
                                onAppDrawerScreenHeightDpChange(
                                    if (useSystemHeight) 0 else configuration.screenHeightDp.coerceIn(320, 2000)
                                )
                            }
                        )
                        if (appDrawerScreenHeightDp != 0) {
                            IntegerSliderSetting(
                                l10n(R.string.ui_screen_height_dp),
                                appDrawerScreenHeightDp.coerceIn(320, 2000),
                                320,
                                2000,
                                onDraggingChange = { sliderDragging = it },
                                onValueChange = onAppDrawerScreenHeightDpChange
                            )
                            SettingsHint(l10n(R.string.ui_screen_height_dp_hint))
                        }
                        IntegerSliderSetting(l10n(R.string.ui_horizontal_icon_count), appDrawerColumns, 3, 10, onDraggingChange = { sliderDragging = it }) { proposed ->
                            onAppDrawerColumnsChange(proposed)
                            val newMaxWidth = maxItemWidth(proposed)
                            if (appDrawerItemWidth > newMaxWidth) onAppDrawerItemWidthChange(newMaxWidth)
                            if (!appDrawerIndependentSize) onAppDrawerItemHeightChange(minOf(newMaxWidth, maxItemHeight(appDrawerRows)))
                            warning = null
                        }
                        IntegerSliderSetting(l10n(R.string.ui_vertical_icon_count), appDrawerRows, 3, 20, onDraggingChange = { sliderDragging = it }) { proposed ->
                            onAppDrawerRowsChange(proposed)
                            val newMaxHeight = maxItemHeight(proposed)
                            if (appDrawerItemHeight > newMaxHeight) onAppDrawerItemHeightChange(newMaxHeight)
                            if (!appDrawerIndependentSize) onAppDrawerItemWidthChange(minOf(newMaxHeight, maxItemWidth(appDrawerColumns)))
                            warning = null
                        }
                        SettingsHint(l10n(R.string.ui_allowed_grid_range_3_3_to_10_20))
                        if (appDrawerFillAvailable) {
                            SettingsHint(l10n(R.string.ui_current_limit_for_the_selected_grid_dp, widthMax, heightMax))
                        } else {
                            IntegerSliderSetting(l10n(R.string.ui_card_width_dp), appDrawerItemWidth.coerceAtMost(widthMax), 12, widthMax, onDraggingChange = { sliderDragging = it }) { proposed ->
                                onAppDrawerItemWidthChange(proposed)
                                if (!appDrawerIndependentSize) onAppDrawerItemHeightChange(proposed.coerceAtMost(heightMax))
                                warning = null
                            }
                            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(checked = appDrawerIndependentSize, onCheckedChange = { checked ->
                                    onAppDrawerIndependentSizeChange(checked)
                                    if (!checked) onAppDrawerItemHeightChange(appDrawerItemWidth.coerceAtMost(heightMax))
                                })
                                Text(l10n(R.string.ui_set_height_independently), modifier = Modifier.weight(1f))
                            }
                            IntegerSliderSetting(l10n(R.string.ui_card_height_dp), appDrawerItemHeight.coerceAtMost(heightMax), 12, heightMax, enabled = appDrawerIndependentSize, onDraggingChange = { sliderDragging = it }) {
                                onAppDrawerItemHeightChange(it)
                                warning = null
                            }
                            SettingsHint(l10n(R.string.ui_current_limit_for_the_selected_grid_dp, widthMax, heightMax))
                        }
                    }
                    LauncherSettingsPage.APP_DRAWER_ITEM_STYLE -> {
                        AppItemFrameShape.values().forEach { shape -> SimpleRadioOption(shape.title, shape == appDrawerFrameShape) { onAppDrawerFrameShapeChange(shape) } }
                        if (appDrawerFrameShape != AppItemFrameShape.NONE) {
                            VisualColorSetting(l10n(R.string.ui_frame_colour), appDrawerFrameColor) { color -> warning = null; onAppDrawerFrameColorChange(color) }
                            IntegerSliderSetting(l10n(R.string.ui_opacity), appDrawerFrameOpacity, 0, 100, onDraggingChange = { sliderDragging = it }) { onAppDrawerFrameOpacityChange(it) }
                            VisualColorSetting(l10n(R.string.ui_text_colour), appDrawerFrameTextColor, onAppDrawerFrameTextColorChange)
                            TextShadowSettingsBlock(textShadowSettings, onTextShadowSettingsChange)
                            Box(
                                modifier = Modifier.fillMaxWidth().height(70.dp).clip(appItemFrameShape(appDrawerFrameShape))
                                    .background(parseAppFrameColor(appDrawerFrameColor).copy(alpha = appDrawerFrameOpacity / 100f)),
                                contentAlignment = Alignment.Center
                            ) { Text(l10n(R.string.ui_frame_preview)) }
                        }
                    }
                    LauncherSettingsPage.APP_DRAWER_PAGE_EFFECTS -> {
                        PageTransitionEffectList(
                            query = appDrawerEffectSearch,
                            includeAppDrawerOnly = true,
                            selectedEffect = appDrawerEffectDraft,
                            selectedAtOpen = appDrawerEffectAtOpen,
                            openingSerial = drawerEffectListOpenSerial,
                            groupExpansion = effectGroupExpansion,
                            advancedIconEffectOverridesPage = appDrawerIconEffect in setOf(
                                AppDrawerIconEffect.SPIRAL, AppDrawerIconEffect.FIREWORK,
                                AppDrawerIconEffect.BLACK_HOLE, AppDrawerIconEffect.BIG_BANG
                            ),
                            onGroupExpansionChange = { group, expanded ->
                                setEffectGroupExpanded(group, expanded)
                            }
                        ) { effect ->
                            appDrawerEffectDraft = effect
                            onAppDrawerPageTransitionChange(effect)
                        }
                        MinimumPageEffectDurationSetting(
                            l10n(R.string.ui_min_page_effect_duration),
                            l10n(R.string.ui_min_page_effect_default),
                            pageEffectTiming.drawerMs,
                            onDraggingChange = { sliderDragging = it },
                            onValueChange = pageEffectTiming::setDrawer
                        )
                    }
                    LauncherSettingsPage.APP_DRAWER_ICON_EFFECTS -> {
                        var simpleExpanded by remember(advancedContext) {
                            mutableStateOf(LauncherStorage.loadDrawerIconGroupExpanded(advancedContext, false))
                        }
                        var experimentalExpanded by remember(advancedContext) {
                            mutableStateOf(LauncherStorage.loadDrawerIconGroupExpanded(advancedContext, true))
                        }
                        DrawerIconEffectGroupHeader(
                            l10n(R.string.ui_drawer_icon_group_simple), simpleExpanded
                        ) {
                            simpleExpanded = !simpleExpanded
                            LauncherStorage.saveDrawerIconGroupExpanded(advancedContext, false, simpleExpanded)
                        }
                        AnimatedVisibility(visible = simpleExpanded) {
                            Column {
                                appDrawerIconEffectPickerOrder.dropLast(4).forEach { effect ->
                                    AppDrawerIconEffectOption(effect, effect == appDrawerIconEffect) {
                                        onAppDrawerIconEffectChange(effect)
                                    }
                                }
                            }
                        }
                        DrawerIconEffectGroupHeader(
                            l10n(R.string.ui_drawer_icon_group_experimental), experimentalExpanded
                        ) {
                            experimentalExpanded = !experimentalExpanded
                            LauncherStorage.saveDrawerIconGroupExpanded(advancedContext, true, experimentalExpanded)
                        }
                        AnimatedVisibility(visible = experimentalExpanded) {
                            Column {
                                Text(
                                    l10n(R.string.ui_drawer_icon_experimental_warning),
                                    color = Color(0xFFFF7885),
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.fillMaxWidth().padding(12.dp)
                                )
                                appDrawerIconEffectPickerOrder.takeLast(4).forEach { effect ->
                                    AppDrawerIconEffectOption(effect, effect == appDrawerIconEffect) {
                                        onAppDrawerIconEffectChange(effect)
                                    }
                                }
                            }
                        }
                    }
                    LauncherSettingsPage.APP_DRAWER_INTERACTION -> {
                        IntegerSliderSetting(l10n(R.string.ui_reorder_hold_time_ms), appDrawerReorderHold, 120, 800, onDraggingChange = { sliderDragging = it }, onValueChange = onAppDrawerReorderHoldChange)
                        IntegerSliderSetting(
                            l10n(R.string.ui_page_overview_edge_zone_dp),
                            appDrawerEdgeSwitchDp,
                            16,
                            140,
                            onDraggingChange = {
                                sliderDragging = it
                                onAppDrawerEdgePreviewChange(it)
                            },
                            onValueChange = onAppDrawerEdgeSwitchDpChange
                        )
                        SettingsSectionTitle(l10n(R.string.ui_cross_page_dragging))
                        SimpleRadioOption(l10n(R.string.ui_page_overview), appDrawerCrossPageDragMode == AppDrawerCrossPageDragMode.PAGE_OVERVIEW) {
                            onAppDrawerCrossPageDragModeChange(AppDrawerCrossPageDragMode.PAGE_OVERVIEW)
                        }
                        SimpleRadioOption(l10n(R.string.ui_legacy_edge_switch), appDrawerCrossPageDragMode == AppDrawerCrossPageDragMode.EDGE_SWITCH) {
                            onAppDrawerCrossPageDragModeChange(AppDrawerCrossPageDragMode.EDGE_SWITCH)
                        }
                        if (appDrawerCrossPageDragMode == AppDrawerCrossPageDragMode.PAGE_OVERVIEW) {
                            SettingsSectionTitle(l10n(R.string.ui_page_preview_style))
                            SimpleRadioOption(l10n(R.string.ui_simple_preview), appDrawerPagePreviewMode == AppDrawerPagePreviewMode.SIMPLE) {
                                onAppDrawerPagePreviewModeChange(AppDrawerPagePreviewMode.SIMPLE)
                            }
                            SimpleRadioOption(l10n(R.string.ui_mipmap_preview), appDrawerPagePreviewMode == AppDrawerPagePreviewMode.MIPMAP) {
                                onAppDrawerPagePreviewModeChange(AppDrawerPagePreviewMode.MIPMAP)
                            }
                            IntegerSliderSetting(l10n(R.string.ui_page_switch_hover_delay_ms), appDrawerDragPageHover, 250, 2000, onDraggingChange = { sliderDragging = it }, onValueChange = onAppDrawerDragPageHoverChange)
                        }
                        SettingsToggleRow(
                            l10n(R.string.ui_workspace_edge_swipe_from_app_drawer),
                            l10n(R.string.ui_workspace_edge_swipe_from_app_drawer_hint),
                            appDrawerWorkspaceEdgeSwipeEnabled,
                            onAppDrawerWorkspaceEdgeSwipeEnabledChange
                        )
                        SettingsHint(l10n(R.string.ui_drag_page_overview_hint))
                    }
                    LauncherSettingsPage.APP_DRAWER_NEW_APPS -> {
                        SettingsSectionTitle(l10n(R.string.ui_new_app_cutoff_date))
                        Button(
                            onClick = {
                                val calendar = Calendar.getInstance().apply { timeInMillis = newAppCutoffMillis }
                                DatePickerDialog(
                                    advancedContext,
                                    { _, year, month, day ->
                                        calendar.set(year, month, day, 0, 0, 0)
                                        calendar.set(Calendar.MILLISECOND, 0)
                                        onNewAppCutoffChange(calendar.timeInMillis)
                                    },
                                    calendar.get(Calendar.YEAR),
                                    calendar.get(Calendar.MONTH),
                                    calendar.get(Calendar.DAY_OF_MONTH)
                                ).show()
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(android.text.format.DateFormat.getMediumDateFormat(advancedContext).format(Date(newAppCutoffMillis)))
                        }
                        SettingsHint(l10n(R.string.ui_new_app_cutoff_hint))
                        NewAppEmblemShape.values().forEach { shape ->
                            val label = when (shape) {
                                NewAppEmblemShape.NONE -> l10n(R.string.ui_nothing)
                                NewAppEmblemShape.FIRE -> l10n(R.string.ui_fire)
                                NewAppEmblemShape.CIRCLE -> l10n(R.string.ui_circle)
                                NewAppEmblemShape.OVAL -> l10n(R.string.ui_oval)
                                NewAppEmblemShape.STAR -> l10n(R.string.ui_star)
                                NewAppEmblemShape.DIAMOND -> l10n(R.string.ui_diamond)
                                NewAppEmblemShape.ROUNDED_RECTANGLE -> l10n(R.string.ui_rounded_rectangle)
                            }
                            SimpleRadioOption(label, shape == newAppEmblemShape) { onNewAppEmblemShapeChange(shape) }
                        }
                        IntegerSliderSetting(l10n(R.string.ui_emblem_size_percent), newAppEmblemSize, 4, 25, onDraggingChange = { sliderDragging = it }, onValueChange = onNewAppEmblemSizeChange)
                        SettingsToggleRow(l10n(R.string.ui_gradient), l10n(R.string.ui_use_two_emblem_colors), newAppEmblemGradient, onNewAppEmblemGradientChange)
                        VisualColorSetting(l10n(R.string.ui_primary_colour), newAppEmblemColor, onNewAppEmblemColorChange)
                        if (newAppEmblemGradient) VisualColorSetting(l10n(R.string.ui_secondary_colour), newAppEmblemColorB, onNewAppEmblemColorBChange)
                        IntegerSliderSetting(l10n(R.string.ui_horizontal_position_percent), newAppEmblemX, 0, 100, onDraggingChange = { sliderDragging = it }, onValueChange = onNewAppEmblemXChange)
                        IntegerSliderSetting(l10n(R.string.ui_vertical_position_percent), newAppEmblemY, 0, 100, onDraggingChange = { sliderDragging = it }, onValueChange = onNewAppEmblemYChange)
                        Button(onClick = onChooseNewAppEmblemImage, modifier = Modifier.fillMaxWidth()) {
                            Text(if (newAppEmblemImageUri == null) l10n(R.string.ui_choose_emblem_image) else l10n(R.string.ui_replace_picture))
                        }
                        if (newAppEmblemImageUri != null) OutlinedButton(onClick = onClearNewAppEmblemImage, modifier = Modifier.fillMaxWidth()) {
                            Text(l10n(R.string.ui_remove_custom_image))
                        }
                        OutlinedButton(onClick = onChooseNewAppEmblemIconPack, modifier = Modifier.fillMaxWidth()) {
                            Text(l10n(R.string.ui_choose_from_icon_pack))
                        }
                        TextButton(onClick = onClearNewAppEmblemIconPack, modifier = Modifier.fillMaxWidth()) {
                            Text(l10n(R.string.ui_clear_icon_pack_emblem))
                        }
                        SettingsHint(l10n(R.string.ui_new_app_emblem_hint))
                    }
                    LauncherSettingsPage.APP_DRAWER_BACKGROUND -> {
                        AppDrawerBackgroundMode.values().forEach { mode ->
                            SimpleRadioOption(mode.title, mode == appDrawerBackgroundMode) { onAppDrawerBackgroundModeChange(mode) }
                            if (mode == appDrawerBackgroundMode) SettingsHint(mode.description)
                        }
                        if (appDrawerBackgroundMode == AppDrawerBackgroundMode.IMAGE) {
                            Button(onClick = onChooseAppDrawerImage, modifier = Modifier.fillMaxWidth()) {
                                Text(if (appDrawerBackgroundImageUri.isNullOrBlank()) l10n(R.string.ui_choose_picture) else l10n(R.string.ui_replace_picture))
                            }
                        }
                        IntegerSliderSetting(l10n(R.string.ui_background_opacity_0341), appDrawerBackgroundOpacity, 0, 100, onDraggingChange = { sliderDragging = it }, onValueChange = onAppDrawerBackgroundOpacityChange)
                        VisualColorSetting(l10n(R.string.ui_primary_colour), appDrawerBackgroundColorA, onAppDrawerBackgroundColorAChange)
                        if (appDrawerBackgroundMode == AppDrawerBackgroundMode.GRADIENT) {
                            VisualColorSetting(l10n(R.string.ui_secondary_colour), appDrawerBackgroundColorB, onAppDrawerBackgroundColorBChange)
                        }
                        VisualColorSetting(l10n(R.string.ui_text_colour), appDrawerBackgroundTextColor, onAppDrawerBackgroundTextColorChange)
                        TextShadowSettingsBlock(textShadowSettings, onTextShadowSettingsChange)
                    }
                    LauncherSettingsPage.APP_DRAWER_QUICK_ACTION -> {
                        SettingsHint(l10n(R.string.ui_quick_action_button_hint))
                        SettingsSectionTitle(l10n(R.string.ui_navigation))
                        SimpleRadioOption(
                            l10n(R.string.ui_page_overview),
                            appDrawerQuickAction.kind == AppDrawerQuickActionKind.PAGE_OVERVIEW
                        ) {
                            onAppDrawerQuickActionChange(AppDrawerQuickAction())
                        }
                        SettingsSectionTitle(l10n(R.string.ui_launcher_settings))
                        SimpleRadioOption(
                            l10n(R.string.ui_open_launcher_settings),
                            appDrawerQuickAction.kind == AppDrawerQuickActionKind.LAUNCHER_SETTINGS
                        ) {
                            onAppDrawerQuickActionChange(AppDrawerQuickAction(AppDrawerQuickActionKind.LAUNCHER_SETTINGS))
                        }
                        SettingsSectionTitle(l10n(R.string.ui_app_drawer_settings))
                        appDrawerQuickSettingsPages.forEach { settingsPage ->
                            val candidate = AppDrawerQuickAction(
                                AppDrawerQuickActionKind.APP_DRAWER_SETTINGS,
                                settingsPage.name
                            )
                            SimpleRadioOption(
                                appDrawerSettingsPageTitle(settingsPage),
                                appDrawerQuickAction == candidate
                            ) { onAppDrawerQuickActionChange(candidate) }
                        }
                        SettingsSectionTitle(l10n(R.string.ui_system_settings))
                        availableSettingsShortcutOptions().forEach { option ->
                            val candidate = AppDrawerQuickAction(
                                AppDrawerQuickActionKind.SYSTEM_SETTINGS,
                                option.action
                            )
                            SimpleRadioOption(
                                option.title,
                                appDrawerQuickAction == candidate
                            ) { onAppDrawerQuickActionChange(candidate) }
                        }
                    }
                    LauncherSettingsPage.APP_DRAWER_HIDDEN_APPS -> {
                        SettingsHint(l10n(R.string.ui_hiding_applies_only_to_the_app_drawer_shortcuts_on))
                        allApps.sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.label }).forEach { app ->
                            val component = app.componentName.flattenToString()
                            SettingsToggleRow(
                                title = app.label,
                                description = app.packageName,
                                checked = component in hiddenApps,
                                onCheckedChange = { hidden -> onHiddenAppChange(component, hidden) }
                            )
                        }
                    }
                    LauncherSettingsPage.FOLDERS -> {
                        SettingsSectionTitle(l10n(R.string.ui_icon_reordering_in_folders))
                        VisualColorSetting(
                            l10n(R.string.ui_insertion_line_color),
                            folderReorderLineColor,
                            onFolderReorderLineColorChange
                        )
                        IntegerSliderSetting(
                            l10n(R.string.ui_insertion_line_thickness),
                            folderReorderLineThickness,
                            1,
                            12,
                            onDraggingChange = { sliderDragging = it },
                            onValueChange = onFolderReorderLineThicknessChange
                        )
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .height(24.dp)
                                .padding(horizontal = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                Modifier
                                    .fillMaxWidth()
                                    .height(folderReorderLineThickness.dp)
                                    .background(
                                        parseAppFrameColor(folderReorderLineColor),
                                        RoundedCornerShape(50)
                                    )
                            )
                        }
                        SettingsHint(l10n(R.string.ui_insertion_line_explanation))
                        SettingsSectionTitle(l10n(R.string.ui_folder_fan))
                        IntegerSliderSetting(l10n(R.string.ui_initial_following_speed), folderFanSpeed, 15, 100, onDraggingChange = { sliderDragging = it }, onValueChange = onFolderFanSpeedChange)
                        IntegerSliderSetting(l10n(R.string.ui_speed_retention), folderFanDecay, 0, 100, onDraggingChange = { sliderDragging = it }, onValueChange = onFolderFanDecayChange)
                        IntegerSliderSetting(l10n(R.string.ui_extracted_icon_size), folderFanIconSize, 24, 96, onDraggingChange = { sliderDragging = it }, onValueChange = onFolderFanIconSizeChange)
                        IntegerSliderSetting(l10n(R.string.ui_distance_between_extracted_icons), folderFanSpacing, 48, 260, onDraggingChange = { sliderDragging = it }, onValueChange = onFolderFanSpacingChange)
                        SettingsSectionTitle(l10n(R.string.ui_extracted_icon_dismissal))
                        FolderFanCloseMode.values().forEach { mode ->
                            SimpleRadioOption(mode.title, mode == folderFanCloseMode) {
                                onFolderFanCloseModeChange(mode)
                            }
                            if (mode == folderFanCloseMode) SettingsHint(mode.description)
                        }
                        SettingsHint(l10n(R.string.ui_text_100_retention_keeps_the_speed_unchanged_at_0_the))
                    }
                    LauncherSettingsPage.APP_LAUNCH_ANIMATION -> {
                        SettingsSectionTitle(l10n(R.string.ui_launch_activation))
                        SettingsToggleRow(
                            l10n(R.string.ui_link_delay_and_animation),
                            l10n(R.string.ui_link_delay_and_animation_hint),
                            linkLaunchDelayToAnimation,
                            onLinkLaunchDelayToAnimationChange
                        )
                        IntegerSliderSetting(l10n(R.string.ui_app_launch_delay_ms), appLaunchDelay, 100, 1000, enabled = !linkLaunchDelayToAnimation, onDraggingChange = { sliderDragging = it }, step = 10, onValueChange = onAppLaunchDelayChange)
                        IntegerSliderSetting(l10n(R.string.ui_animation_duration_ms), appLaunchAnimationDuration, 100, 1000, onDraggingChange = { sliderDragging = it }, step = 10, onValueChange = onAppLaunchAnimationDurationChange)
                        SettingsHint(l10n(R.string.ui_launch_delay_requires_animation))
                        AppLaunchAnimationEffect.values().forEach { effect ->
                            Row(
                                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp))
                                    .clickable { onAppLaunchAnimationChange(effect) }.padding(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(selected = effect == appLaunchAnimationEffect, onClick = { onAppLaunchAnimationChange(effect) })
                                Column(Modifier.weight(1f)) {
                                    Text(effect.title, fontWeight = if (effect == appLaunchAnimationEffect) FontWeight.Bold else FontWeight.Normal)
                                    Text(effect.description, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.60f))
                                }
                            }
                        }
                        SettingsSectionTitle(l10n(R.string.ui_drag_activation))
                        IntegerSliderSetting(l10n(R.string.ui_animation_duration_ms), appDragAnimationDuration, 100, 1000, onDraggingChange = { sliderDragging = it }, step = 10, onValueChange = onAppDragAnimationDurationChange)
                        AppLaunchAnimationEffect.values().forEach { effect ->
                            SimpleRadioOption(effect.title, effect == appDragAnimationEffect) {
                                onAppDragAnimationEffectChange(effect)
                            }
                        }
                    }
                    LauncherSettingsPage.ENVIRONMENT_EFFECTS -> {
                        EnvironmentEffectsSettingsPanel(environmentEffects, onEnvironmentEffectsChange)
                    }
                    LauncherSettingsPage.WALLPAPER -> {
                        LauncherWallpaperMode.values().forEach { mode ->
                            SimpleRadioOption(mode.title, mode == wallpaperMode) { onWallpaperModeChange(mode) }
                            if (mode == wallpaperMode) SettingsHint(mode.description)
                        }
                        if (wallpaperMode == LauncherWallpaperMode.SYSTEM) {
                            SettingsToggleRow(
                                l10n(R.string.ui_scroll_wallpaper),
                                l10n(R.string.ui_scroll_wallpaper_hint),
                                wallpaperScrolling,
                                onWallpaperScrollingChange
                            )
                            Button(onClick = onChooseStaticWallpaper, modifier = Modifier.fillMaxWidth()) { Text(l10n(R.string.ui_choose_image)) }
                            OutlinedButton(onClick = onChooseLiveWallpaper, modifier = Modifier.fillMaxWidth()) { Text(l10n(R.string.ui_choose_live_wallpaper)) }
                        } else {
                            SettingsNavigationItem(l10n(R.string.ui_configure_animated_background), l10n(R.string.ui_circles, wallpaperCircleCount)) { page = LauncherSettingsPage.WALLPAPER_ANIMATED }
                        }
                    }
                    LauncherSettingsPage.WALLPAPER_ANIMATED -> {
                        SettingsToggleRow(l10n(R.string.ui_dynamic_background_colour), l10n(R.string.ui_colour_changes_smoothly_over_time), wallpaperDynamic, onWallpaperDynamicChange)
                        VisualColorSetting(l10n(R.string.ui_background_colour_0475), wallpaperBackground, onWallpaperBackgroundChange)
                        VisualColorSetting(l10n(R.string.ui_primary_circle_colour), wallpaperCircleA, onWallpaperCircleAChange)
                        VisualColorSetting(l10n(R.string.ui_secondary_circle_colour), wallpaperCircleB, onWallpaperCircleBChange)
                        IntegerSliderSetting(l10n(R.string.ui_circle_count), wallpaperCircleCount, 0, 12, onDraggingChange = { sliderDragging = it }, onValueChange = onWallpaperCircleCountChange)
                    }
                    LauncherSettingsPage.WIDGETS -> {
                        SettingsNavigationItem(l10n(R.string.ui_position_and_size), widgetEditBarPosition.title) { page = LauncherSettingsPage.WIDGET_EDITING }
                        SettingsNavigationItem(l10n(R.string.ui_widget_picker), widgetPickerMode.title) { page = LauncherSettingsPage.WIDGET_PICKER }
                        SettingsNavigationItem(l10n(R.string.ui_widget_tap_actions), l10n(R.string.ui_widget_action_chains_configured_individually)) { page = LauncherSettingsPage.WIDGET_ACTIONS }
                    }
                    LauncherSettingsPage.WIDGET_EDITING -> {
                        SettingsSectionTitle(l10n(R.string.ui_green_checkmark_position))
                        WidgetEditBarPosition.values().forEach { position -> SimpleRadioOption(position.title, position == widgetEditBarPosition) { onWidgetEditBarPositionChange(position) } }
                        SettingsHint(l10n(R.string.ui_long_press_a_widget_to_move_it_green_handles))
                    }
                    LauncherSettingsPage.WIDGET_PICKER -> WidgetPickerDisplayMode.values().forEach { mode ->
                        Row(
                            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).clickable { onWidgetPickerModeChange(mode) }.padding(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(selected = mode == widgetPickerMode, onClick = { onWidgetPickerModeChange(mode) })
                            Column(Modifier.weight(1f)) {
                                Text(mode.title, fontWeight = if (mode == widgetPickerMode) FontWeight.Bold else FontWeight.Normal)
                                Text(mode.description, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.6f))
                            }
                        }
                    }
                    LauncherSettingsPage.WIDGET_ACTIONS -> {
                        SettingsSectionTitle(l10n(R.string.ui_widget_action_chains))
                        SettingsHint(l10n(R.string.ui_widget_action_chains_hint))
                        if (widgetActionWidgets.isEmpty()) {
                            SettingsHint(l10n(R.string.ui_no_widgets_for_action_chain))
                        } else {
                            Button(
                                onClick = {
                                    widgetActionEditor = WidgetActionChain(
                                        id = UUID.randomUUID().toString(),
                                        name = l10n(R.string.ui_widget_action_chain_default_name),
                                        widgetIds = emptySet()
                                    )
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) { Text(l10n(R.string.ui_add_action_chain)) }
                        }
                        widgetActionChains.forEach { chain ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.38f)
                                )
                            ) {
                                Column(
                                    Modifier.fillMaxWidth().padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(chain.name, fontWeight = FontWeight.Bold)
                                    Text(
                                        l10n(
                                            R.string.ui_widget_action_chain_summary,
                                            chain.widgetIds.size,
                                            chain.actions.size
                                        ),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
                                    )
                                    Row(
                                        Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.End
                                    ) {
                                        TextButton(onClick = { widgetActionEditor = chain }) {
                                            Text(l10n(R.string.ui_edit))
                                        }
                                        TextButton(onClick = { onWidgetActionChainDelete(chain.id) }) {
                                            Text(l10n(R.string.ui_delete))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                if (page == LauncherSettingsPage.ROOT) {
                    Text(
                        text = l10n(R.string.ui_current_version, BuildConfig.VERSION_NAME),
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 2.dp),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.58f)
                    )
                }
                }
            }
        },
        confirmButton = {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                if (page != LauncherSettingsPage.ROOT) TextButton(onClick = { warning = null; page = parentOf(page) }) { Text(l10n(R.string.ui_back)) }
                Spacer(Modifier.weight(1f))
                TextButton(onClick = onDismiss) { Text(l10n(R.string.ui_done)) }
            }
        },
        dismissButton = { }
    )
    if (confirmClearWorkspace) {
        AlertDialog(
            styleOverride = dialogStyle,
            onDismissRequest = { confirmClearWorkspace = false },
            title = { Text(l10n(R.string.ui_clear_workspace_items)) },
            text = { Text(l10n(R.string.ui_clear_workspace_items_warning)) },
            confirmButton = {
                Button(
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC143C)),
                    onClick = { confirmClearWorkspace = false; onClearWorkspaceItems() }
                ) { Text(l10n(R.string.ui_reset_confirm)) }
            },
            dismissButton = {
                TextButton(onClick = { confirmClearWorkspace = false }) { Text(l10n(R.string.ui_cancel)) }
            }
        )
    }
    pendingReset?.let { area ->
        val title = when (area) {
            LauncherResetArea.WORKSPACE -> l10n(R.string.ui_reset_workspace)
            LauncherResetArea.APP_DRAWER -> l10n(R.string.ui_reset_app_drawer)
            LauncherResetArea.SIDE_DOCK -> l10n(R.string.ui_reset_side_dock)
            LauncherResetArea.BOTTOM_DOCK -> l10n(R.string.ui_reset_bottom_dock)
            LauncherResetArea.INTERFACE -> l10n(R.string.ui_reset_interface)
            LauncherResetArea.ALL -> l10n(R.string.ui_reset_all_launcher)
        }
        val description = when (area) {
            LauncherResetArea.WORKSPACE -> l10n(R.string.ui_reset_workspace_warning)
            LauncherResetArea.APP_DRAWER -> l10n(R.string.ui_reset_app_drawer_warning)
            LauncherResetArea.SIDE_DOCK -> l10n(R.string.ui_reset_side_dock_warning)
            LauncherResetArea.BOTTOM_DOCK -> l10n(R.string.ui_reset_bottom_dock_warning)
            LauncherResetArea.INTERFACE -> l10n(R.string.ui_reset_interface_warning)
            LauncherResetArea.ALL -> l10n(R.string.ui_reset_all_warning)
        }
        AlertDialog(
            styleOverride = dialogStyle,
            onDismissRequest = { pendingReset = null; confirmFullResetAgain = false },
            title = { Text(if (confirmFullResetAgain) l10n(R.string.ui_reset_are_you_sure) else title) },
            text = {
                Text(
                    if (confirmFullResetAgain) l10n(R.string.ui_reset_all_second_warning)
                    else description
                )
            },
            confirmButton = {
                Button(
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC143C)),
                    onClick = {
                        if (area == LauncherResetArea.ALL && !confirmFullResetAgain) {
                            confirmFullResetAgain = true
                        } else {
                            pendingReset = null
                            confirmFullResetAgain = false
                            onResetLauncherArea(area)
                        }
                    }
                ) { Text(if (confirmFullResetAgain) l10n(R.string.ui_reset_final_confirm) else l10n(R.string.ui_reset_confirm)) }
            },
            dismissButton = {
                TextButton(onClick = { pendingReset = null; confirmFullResetAgain = false }) {
                    Text(l10n(R.string.ui_cancel))
                }
            }
        )
    }
    widgetActionEditor?.let { chain ->
        WidgetActionChainEditorDialog(
            initial = chain,
            widgets = widgetActionWidgets,
            apps = allApps,
            onDismiss = { widgetActionEditor = null },
            onSave = { updated ->
                onWidgetActionChainSave(updated)
                widgetActionEditor = null
            }
        )
    }
}

private enum class WidgetActionEditorPicker {
    APP,
    LAUNCHER_ACTION
}

@Composable
private fun WidgetActionChainEditorDialog(
    initial: WidgetActionChain,
    widgets: List<WidgetActionWidgetDescriptor>,
    apps: List<AppItem>,
    onDismiss: () -> Unit,
    onSave: (WidgetActionChain) -> Unit
) {
    var draft by remember(initial.id, initial) { mutableStateOf(initial) }
    var picker by remember { mutableStateOf<WidgetActionEditorPicker?>(null) }
    var appQuery by remember { mutableStateOf("") }

    fun updateRegion(transform: (WidgetActionRegion) -> WidgetActionRegion) {
        draft = draft.copy(region = transform(draft.region).normalized())
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                when (picker) {
                    WidgetActionEditorPicker.APP -> l10n(R.string.ui_select_application)
                    WidgetActionEditorPicker.LAUNCHER_ACTION -> l10n(R.string.ui_select_action)
                    null -> l10n(R.string.ui_widget_action_chain)
                }
            )
        },
        text = {
            when (picker) {
                WidgetActionEditorPicker.APP -> {
                    Column(
                        Modifier.fillMaxWidth().heightIn(max = 560.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = appQuery,
                            onValueChange = { appQuery = it },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            label = { Text(l10n(R.string.ui_search)) }
                        )
                        val filtered = remember(apps, appQuery) {
                            val needle = appQuery.trim().lowercase()
                            apps.asSequence()
                                .filter { needle.isBlank() || it.label.lowercase().contains(needle) || it.packageName.lowercase().contains(needle) }
                                .sortedBy { it.label.lowercase() }
                                .toList()
                        }
                        LazyColumn(Modifier.fillMaxWidth().heightIn(max = 480.dp)) {
                            lazyItems(filtered, key = { it.componentName.flattenToString() }) { app ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(14.dp))
                                        .clickable {
                                            draft = draft.copy(
                                                actions = draft.actions + WidgetActionStep(
                                                    type = WidgetActionStepType.APP,
                                                    value = app.componentName.flattenToString(),
                                                    label = app.label
                                                )
                                            )
                                            picker = null
                                            appQuery = ""
                                        }
                                        .padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    AppIconImage(app.label, app.icon, Modifier.size(40.dp))
                                    Column(Modifier.weight(1f)) {
                                        Text(app.label, fontWeight = FontWeight.Bold)
                                        Text(
                                            app.packageName,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.60f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                WidgetActionEditorPicker.LAUNCHER_ACTION -> {
                    Column(
                        Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SettingsSectionTitle(l10n(R.string.ui_launcher_actions))
                        launcherActionOptions.forEach { option ->
                            OutlinedButton(
                                onClick = {
                                    draft = draft.copy(
                                        actions = draft.actions + WidgetActionStep(
                                            type = WidgetActionStepType.LAUNCHER_ACTION,
                                            value = option.action,
                                            label = shortLauncherActionName(option.title)
                                        )
                                    )
                                    picker = null
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) { Text(shortLauncherActionName(option.title)) }
                        }
                        SettingsSectionTitle(l10n(R.string.ui_settings_shortcuts))
                        availableSettingsShortcutOptions().forEach { option ->
                            OutlinedButton(
                                onClick = {
                                    draft = draft.copy(
                                        actions = draft.actions + WidgetActionStep(
                                            type = WidgetActionStepType.INTENT_ACTION,
                                            value = option.action,
                                            label = option.title
                                        )
                                    )
                                    picker = null
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) { Text(option.title) }
                        }
                    }
                }
                null -> {
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .heightIn(max = 600.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = draft.name,
                            onValueChange = { draft = draft.copy(name = it) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            label = { Text(l10n(R.string.ui_name)) }
                        )

                        SettingsSectionTitle(l10n(R.string.ui_widgets_for_action_chain))
                        widgets.forEach { widget ->
                            val selected = widget.id in draft.widgetIds
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .clickable {
                                        draft = draft.copy(
                                            widgetIds = if (selected) draft.widgetIds - widget.id
                                            else draft.widgetIds + widget.id
                                        )
                                    }
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = selected,
                                    onCheckedChange = {
                                        draft = draft.copy(
                                            widgetIds = if (selected) draft.widgetIds - widget.id
                                            else draft.widgetIds + widget.id
                                        )
                                    }
                                )
                                Column(Modifier.weight(1f)) {
                                    Text(widget.title, fontWeight = FontWeight.Bold)
                                    Text(
                                        widget.subtitle,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.60f)
                                    )
                                }
                            }
                        }

                        SettingsSectionTitle(l10n(R.string.ui_click_area))
                        SimpleRadioOption(
                            l10n(R.string.ui_entire_widget),
                            draft.regionMode == WidgetActionRegionMode.FULL
                        ) { draft = draft.copy(regionMode = WidgetActionRegionMode.FULL) }
                        SimpleRadioOption(
                            l10n(R.string.ui_custom_area),
                            draft.regionMode == WidgetActionRegionMode.CUSTOM
                        ) { draft = draft.copy(regionMode = WidgetActionRegionMode.CUSTOM) }

                        if (draft.regionMode == WidgetActionRegionMode.CUSTOM) {
                            val region = draft.region.normalized()
                            val regionPreviewColor = MaterialTheme.colorScheme.primary
                            Canvas(
                                Modifier
                                    .fillMaxWidth()
                                    .height(130.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color.Black.copy(alpha = 0.18f))
                            ) {
                                drawRect(Color.White.copy(alpha = 0.08f))
                                drawRect(
                                    color = regionPreviewColor.copy(alpha = 0.42f),
                                    topLeft = Offset(
                                        size.width * region.leftPercent / 100f,
                                        size.height * region.topPercent / 100f
                                    ),
                                    size = Size(
                                        size.width * region.widthPercent / 100f,
                                        size.height * region.heightPercent / 100f
                                    )
                                )
                            }
                            IntegerSliderSetting(
                                l10n(R.string.ui_area_x_percent), region.leftPercent, 0, 95
                            ) { value ->
                                updateRegion { it.copy(leftPercent = value) }
                            }
                            IntegerSliderSetting(
                                l10n(R.string.ui_area_y_percent), region.topPercent, 0, 95
                            ) { value ->
                                updateRegion { it.copy(topPercent = value) }
                            }
                            IntegerSliderSetting(
                                l10n(R.string.ui_area_width_percent), region.widthPercent, 5, 100 - region.leftPercent
                            ) { value ->
                                updateRegion { it.copy(widthPercent = value) }
                            }
                            IntegerSliderSetting(
                                l10n(R.string.ui_area_height_percent), region.heightPercent, 5, 100 - region.topPercent
                            ) { value ->
                                updateRegion { it.copy(heightPercent = value) }
                            }
                        }

                        SettingsSectionTitle(l10n(R.string.ui_action_sequence))
                        if (draft.actions.isEmpty()) {
                            SettingsHint(l10n(R.string.ui_no_actions_in_chain))
                        }
                        draft.actions.forEachIndexed { index, action ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.36f)
                                ),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Row(
                                    Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("${index + 1}. ${action.label}", Modifier.weight(1f))
                                    TextButton(
                                        enabled = index > 0,
                                        onClick = {
                                            val list = draft.actions.toMutableList()
                                            val item = list.removeAt(index)
                                            list.add(index - 1, item)
                                            draft = draft.copy(actions = list)
                                        }
                                    ) { Text("↑") }
                                    TextButton(
                                        enabled = index < draft.actions.lastIndex,
                                        onClick = {
                                            val list = draft.actions.toMutableList()
                                            val item = list.removeAt(index)
                                            list.add(index + 1, item)
                                            draft = draft.copy(actions = list)
                                        }
                                    ) { Text("↓") }
                                    TextButton(
                                        onClick = {
                                            draft = draft.copy(actions = draft.actions.filterIndexed { i, _ -> i != index })
                                        }
                                    ) { Text("×") }
                                }
                            }
                        }
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { picker = WidgetActionEditorPicker.APP },
                                modifier = Modifier.weight(1f)
                            ) { Text(l10n(R.string.ui_application)) }
                            OutlinedButton(
                                onClick = { picker = WidgetActionEditorPicker.LAUNCHER_ACTION },
                                modifier = Modifier.weight(1f)
                            ) { Text(l10n(R.string.ui_launcher_action)) }
                        }
                        SettingsHint(l10n(R.string.ui_action_sequence_hint))
                    }
                }
            }
        },
        confirmButton = {
            if (picker != null) {
                TextButton(onClick = { picker = null }) { Text(l10n(R.string.ui_back)) }
            } else {
                TextButton(
                    enabled = draft.widgetIds.isNotEmpty() && draft.actions.isNotEmpty(),
                    onClick = { onSave(draft.copy(region = draft.region.normalized())) }
                ) { Text(l10n(R.string.ui_save)) }
            }
        },
        dismissButton = {
            if (picker == null) {
                TextButton(onClick = onDismiss) { Text(l10n(R.string.ui_cancel)) }
            }
        }
    )
}

@Composable
internal fun VisualColorSetting(
    label: String,
    value: String,
    onValueChange: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val normalized = normalizedColorHex(value) ?: "#FFFFFF"
    val initialHsv = remember(normalized) {
        FloatArray(3).also { hsv ->
            android.graphics.Color.colorToHSV(
                android.graphics.Color.parseColor(normalized),
                hsv
            )
        }
    }
    var hue by remember(normalized) { mutableStateOf(initialHsv[0]) }
    var saturation by remember(normalized) { mutableStateOf(initialHsv[1]) }
    var brightness by remember(normalized) { mutableStateOf(initialHsv[2]) }

    fun commit(h: Float = hue, s: Float = saturation, v: Float = brightness) {
        val argb = android.graphics.Color.HSVToColor(floatArrayOf(h, s, v))
        onValueChange(String.format("#%06X", 0xFFFFFF and argb))
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f))
    ) {
        Column(Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                Modifier.fillMaxWidth().clickable { expanded = !expanded },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    Modifier.size(36.dp).clip(CircleShape)
                        .background(parseAppFrameColor(normalized))
                        .border(1.dp, Color.White.copy(alpha = 0.35f), CircleShape)
                )
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text(label, fontWeight = FontWeight.Bold)
                    Text(normalized, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.62f))
                }
                Text(if (expanded) "⌃" else "⌄", fontSize = 22.sp)
            }
            if (expanded) {
                Text(l10n(R.string.ui_hue), style = MaterialTheme.typography.labelMedium)
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    repeat(18) { index ->
                        val sampleHue = index * 20f
                        val sampleColor = Color(android.graphics.Color.HSVToColor(floatArrayOf(sampleHue, 0.92f, 1f)))
                        Box(
                            Modifier.size(26.dp).clip(CircleShape).background(sampleColor)
                                .border(
                                    width = if (abs(hue - sampleHue) < 10f) 2.dp else 1.dp,
                                    color = Color.White.copy(alpha = if (abs(hue - sampleHue) < 10f) 0.9f else 0.28f),
                                    shape = CircleShape
                                )
                                .clickable {
                                    hue = sampleHue
                                    commit(h = sampleHue)
                                }
                        )
                    }
                }
                Slider(
                    value = hue,
                    onValueChange = { hue = it; commit(h = it) },
                    valueRange = 0f..360f
                )
                Text(l10n(R.string.ui_saturation), style = MaterialTheme.typography.labelMedium)
                Slider(
                    value = saturation,
                    onValueChange = { saturation = it; commit(s = it) },
                    valueRange = 0f..1f
                )
                Text(l10n(R.string.ui_brightness), style = MaterialTheme.typography.labelMedium)
                Slider(
                    value = brightness,
                    onValueChange = { brightness = it; commit(v = it) },
                    valueRange = 0.05f..1f
                )
                var text by remember(normalized) { mutableStateOf(normalized) }
                OutlinedTextField(
                    value = text,
                    onValueChange = { raw ->
                        text = raw.take(7)
                        normalizedColorHex(text)?.let(onValueChange)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    label = { Text(l10n(R.string.ui_exact_rrggbb_value)) }
                )
            }
        }
    }
}

@Composable
private fun TextShadowSettingsBlock(
    value: TextShadowSettings,
    onValueChange: (TextShadowSettings) -> Unit
) {
    SettingsSectionTitle(l10n(R.string.ui_text_shadow))
    SettingsToggleRow(
        title = l10n(R.string.ui_show_shadow),
        description = l10n(R.string.ui_shared_text_shadow_for_the_app_drawer_docks_and),
        checked = value.enabled,
        onCheckedChange = { onValueChange(value.copy(enabled = it)) }
    )
    if (value.enabled) {
        IntegerSliderSetting(l10n(R.string.ui_shadow_offset_dp), value.offsetDp, 0, 12) {
            onValueChange(value.copy(offsetDp = it))
        }
        IntegerSliderSetting(l10n(R.string.ui_shadow_blur_dp), value.blurDp, 0, 24) {
            onValueChange(value.copy(blurDp = it))
        }
        VisualColorSetting(l10n(R.string.ui_shadow_colour), value.colorHex) {
            onValueChange(value.copy(colorHex = it))
        }
        IntegerSliderSetting(l10n(R.string.ui_shadow_opacity), value.opacityPercent, 0, 100) {
            onValueChange(value.copy(opacityPercent = it))
        }
    }
}

@Composable
internal fun SimpleRadioOption(
    title: String,
    selected: Boolean,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(enabled = enabled, onClick = onClick)
            .alpha(if (enabled) 1f else 0.42f)
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, enabled = enabled, onClick = onClick)
        Text(
            title,
            modifier = Modifier.weight(1f),
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            softWrap = true
        )
    }
}

@Composable
internal fun IntegerSliderSetting(
    label: String,
    value: Int,
    minValue: Int,
    maxValue: Int,
    enabled: Boolean = true,
    onDraggingChange: (Boolean) -> Unit = {},
    step: Int = 1,
    onValueChange: (Int) -> Unit
) {
    val safeMax = maxValue.coerceAtLeast(minValue)
    val safeValue = value.coerceIn(minValue, safeMax)
    Column(modifier = Modifier.fillMaxWidth()) {
        SettingsSliderHeader(label = label, valueText = safeValue.toString())
        if (safeMax > minValue) {
            Slider(
                modifier = Modifier.fillMaxWidth(),
                value = safeValue.toFloat(),
                onValueChange = { onDraggingChange(true); onValueChange((minValue + ((it - minValue) / step.coerceAtLeast(1)).roundToInt() * step.coerceAtLeast(1)).coerceIn(minValue, safeMax)) },
                onValueChangeFinished = { onDraggingChange(false) },
                valueRange = minValue.toFloat()..safeMax.toFloat(),
                steps = ((safeMax - minValue) / step.coerceAtLeast(1) - 1).coerceAtLeast(0),
                enabled = enabled
            )
        } else Spacer(Modifier.height(10.dp))
    }
}

@Composable
private fun EdgeZonePreview(edgeDp: Int) {
    val configuration = LocalConfiguration.current
    val fraction = (edgeDp.toFloat() / configuration.screenWidthDp.coerceAtLeast(1))
        .coerceIn(0.04f, 0.42f)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(Color.Black.copy(alpha = 0.28f))
            .border(1.dp, Color.White.copy(alpha = 0.16f), RoundedCornerShape(20.dp))
    ) {
        Box(
            Modifier
                .align(Alignment.CenterStart)
                .fillMaxWidth(fraction)
                .height(150.dp)
                .background(Color(0xFF4FC3F7).copy(alpha = 0.30f))
        )
        Box(
            Modifier
                .align(Alignment.CenterEnd)
                .fillMaxWidth(fraction)
                .height(150.dp)
                .background(Color(0xFF4FC3F7).copy(alpha = 0.30f))
        )
        Text(
            l10n(R.string.ui_highlighted_zones_switch_the_page),
            modifier = Modifier.align(Alignment.Center).padding(horizontal = 48.dp),
            textAlign = TextAlign.Center,
            color = Color.White.copy(alpha = 0.78f),
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
private fun NumericSettingField(
    label: String,
    value: Int,
    minValue: Int,
    maxValue: Int,
    enabled: Boolean = true,
    onValueChange: (Int) -> Unit
) {
    var text by remember(value) { mutableStateOf(value.toString()) }
    OutlinedTextField(
        value = text,
        onValueChange = { raw ->
            text = raw.filter(Char::isDigit).take(4)
            text.toIntOrNull()?.coerceIn(minValue, maxValue)?.let(onValueChange)
        },
        modifier = Modifier.fillMaxWidth(),
        enabled = enabled,
        singleLine = true,
        label = { Text(label) },
        supportingText = { Text("$minValue–$maxValue") },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
    )
}

@Composable
private fun GestureCycleSetting(
    label: String,
    action: DesktopGestureAction,
    onChange: (DesktopGestureAction) -> Unit
) {
    val actions = DesktopGestureAction.values()
    Column(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).clickable {
            onChange(actions[(action.ordinal + 1) % actions.size])
        }.background(Color.White.copy(alpha = 0.055f)).padding(horizontal = 13.dp, vertical = 10.dp)
    ) {
        Text(label, modifier = Modifier.fillMaxWidth(), softWrap = true)
        Text(
            action.title,
            modifier = Modifier.fillMaxWidth().padding(top = 2.dp),
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.End,
            softWrap = true
        )
    }
}

@Composable
private fun SettingsNavigationItem(
    title: String,
    summary: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.06f))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    summary,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.62f)
                )
            }
            Text("›", style = MaterialTheme.typography.headlineSmall)
        }
    }
}

@Composable
private fun SettingsToggleRow(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean = true
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable(enabled = enabled) { onCheckedChange(!checked) }
            .background(Color.White.copy(alpha = if (enabled) 0.05f else 0.025f))
            .padding(horizontal = 14.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.Bold, color = Color.White.copy(alpha = if (enabled) 1f else 0.45f))
            Text(
                description,
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = if (enabled) 0.60f else 0.32f)
            )
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange, enabled = enabled)
    }
}

@Composable
private fun SystemBackupSetting() {
    val context = LocalContext.current
    var enabled by remember(context) {
        mutableStateOf(LauncherSystemBackup.isEnabled(context.applicationContext))
    }
    SettingsToggleRow(
        title = l10n(R.string.ui_system_backup),
        description = l10n(R.string.ui_system_backup_description),
        checked = enabled,
        onCheckedChange = { value ->
            enabled = value
            LauncherSystemBackup.setEnabled(context.applicationContext, value)
        }
    )
}

@Composable
internal fun SettingsSectionTitle(text: String, color: Color = Color.Unspecified) {
    Text(
        text = text,
        color = color,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold
    )
}

@Composable
private fun SettingsHint(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodySmall,
        color = Color.White.copy(alpha = 0.62f)
    )
}

@Composable
private fun AppDrawerDragOverlay(
    state: AppDrawerDragState,
    showTargets: Boolean,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val iconHalfPx = with(density) { 34.dp.toPx() }
    Box(modifier = modifier) {
        if (showTargets) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 12.dp, vertical = 8.dp)
                    .height(48.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                DrawerDropTarget(R.drawable.ic_delete, l10n(R.string.ui_delete), state.overDelete, Color(0xFFE34B55), Modifier.weight(1f))
                DrawerDropTarget(R.drawable.ic_edit, l10n(R.string.ui_edit), state.overEdit, Color(0xFFFFB84D), Modifier.weight(1f))
                DrawerDropTarget(R.drawable.ic_info, l10n(R.string.ui_information), state.overInfo, MaterialTheme.colorScheme.primary, Modifier.weight(1f))
            }
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .navigationBarsPadding()
            ) {
                DrawerDropTarget(
                    R.drawable.ic_apps,
                    l10n(R.string.ui_to_desktop),
                    state.overWorkspace,
                    Color(0xFF72E28A),
                    Modifier.fillMaxWidth().height(96.dp)
                )
            }
        }
        AppIconImage(
            state.app.label,
            state.app.icon,
            Modifier.offset { IntOffset((state.position.x - iconHalfPx).roundToInt(), (state.position.y - iconHalfPx).roundToInt()) }
                .size(68.dp).zIndex(100f).graphicsLayer { shadowElevation = 20f; scaleX = 1.08f; scaleY = 1.08f }
        )
    }
}

@Composable
private fun DrawerDropTarget(
    iconRes: Int,
    label: String,
    active: Boolean,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.padding(horizontal = 3.dp),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = if (active) 0.94f else 0.42f)),
        elevation = CardDefaults.cardElevation(defaultElevation = if (active) 18.dp else 6.dp)
    ) {
        Row(Modifier.fillMaxSize().padding(horizontal = 9.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Image(painterResource(iconRes), label, Modifier.size(23.dp), colorFilter = ColorFilter.tint(Color.White))
            Text(label, modifier = Modifier.weight(1f), maxLines = 2, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
private fun AppDrawer(
    apps: List<AppItem>,
    totalAppCount: Int,
    query: String,
    searchVisible: Boolean,
    visuallyHidden: Boolean,
    reorderHoldMillis: Int,
    onQueryChange: (String) -> Unit,
    isLoading: Boolean,
    loadError: String?,
    onRetry: () -> Unit,
    onLaunchApp: (AppItem) -> Unit,
    onStartPlacement: (AppItem) -> Unit,
    onDragStart: (AppItem, Offset, Int) -> Unit,
    onDragMove: (Offset) -> Unit,
    onDragEnd: (Offset) -> Unit,
    mode: AppDrawerMode,
    pickForPlacement: Boolean,
    pickForSideDock: Boolean,
    pickForBottomDock: Boolean,
    savedPage: Int,
    loopEnabled: Boolean,
    pageTransitionEffect: PageTransitionEffect,
    minimumEffectDurationMs: Int,
    iconEffect: AppDrawerIconEffect,
    gridColumns: Int,
    gridRows: Int,
    itemWidthDp: Int,
    itemHeightDp: Int,
    frameShape: AppItemFrameShape,
    frameColorHex: String,
    frameOpacityPercent: Int,
    frameTextColorHex: String,
    backgroundMode: AppDrawerBackgroundMode,
    backgroundOpacityPercent: Int,
    backgroundColorAHex: String,
    backgroundColorBHex: String,
    backgroundTextColorHex: String,
    backgroundImage: ImageBitmap?,
    activeDragPosition: Offset?,
    activeDragSourceIndex: Int?,
    activeDragPreviewIndex: Int?,
    edgeSwitchDp: Int,
    dragOverviewMode: AppDrawerDragOverviewMode,
    crossPageDragMode: AppDrawerCrossPageDragMode,
    pagePreviewMode: AppDrawerPagePreviewMode,
    dragPageHoverMillis: Int,
    edgePreviewVisible: Boolean,
    textShadowSettings: TextShadowSettings,
    bottomBarHeightDp: Int,
    overviewRequestSerial: Int,
    onOverviewRequestConsumed: () -> Unit,
    workspaceEdgeSwipeEnabled: Boolean,
    onWorkspaceEdgeSwipeStart: () -> Unit,
    onWorkspaceEdgeSwipeDrag: (Float) -> Unit,
    onWorkspaceEdgeSwipeEnd: (Float) -> Unit,
    onPageChanged: (Int) -> Unit
) {
    // A drag gesture starts inside an AppIconCell, but that cell belongs to a
    // pager page. When the pager programmatically switches pages, the source
    // cell itself moves off-screen while it still owns the pointer sequence.
    // Pointer coordinates resolved in that moving cell therefore acquire a
    // page-width/page-height offset. Track the same pointer on this stable,
    // full-screen drawer container instead. Its coordinate system does not move
    // when pages change, so X/Y continue to describe the physical finger.
    var drawerRootOffset by remember { mutableStateOf(Offset.Zero) }
    var pagedDragTracking by remember { mutableStateOf(false) }
    var latestPagedPointerInRoot by remember { mutableStateOf<Offset?>(null) }
    val stableOnDragMove by rememberUpdatedState(onDragMove)
    val stableOnWorkspaceEdgeSwipeStart by rememberUpdatedState(onWorkspaceEdgeSwipeStart)
    val stableOnWorkspaceEdgeSwipeDrag by rememberUpdatedState(onWorkspaceEdgeSwipeDrag)
    val stableOnWorkspaceEdgeSwipeEnd by rememberUpdatedState(onWorkspaceEdgeSwipeEnd)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .onGloballyPositioned { drawerRootOffset = it.positionInRoot() }
            .pointerInput(mode, workspaceEdgeSwipeEnabled) {
                awaitEachGesture {
                    val down = awaitFirstDown(
                        requireUnconsumed = false,
                        pass = PointerEventPass.Initial
                    )
                    val edgeWidthPx = 22.dp.toPx()
                    val startedAtLeftEdge = workspaceEdgeSwipeEnabled && down.position.x <= edgeWidthPx
                    val startedAtRightEdge = workspaceEdgeSwipeEnabled && down.position.x >= size.width - edgeWidthPx
                    var workspaceEdgeCandidate = startedAtLeftEdge || startedAtRightEdge
                    var workspaceEdgeDragging = false
                    val startPosition = down.position
                    val startTimeMillis = down.uptimeMillis
                    var previousPosition = down.position
                    var lastPosition = down.position
                    var lastTimeMillis = down.uptimeMillis
                    var pressed = true
                    while (pressed) {
                        val event = awaitPointerEvent(pass = PointerEventPass.Initial)
                        val change = event.changes.firstOrNull { it.id == down.id } ?: break
                        val rootPosition = drawerRootOffset + change.position
                        latestPagedPointerInRoot = rootPosition

                        if (pagedDragTracking) {
                            if (workspaceEdgeDragging) {
                                val velocityDpPerSecond = if (lastTimeMillis > startTimeMillis) {
                                    ((lastPosition.x - startPosition.x) * 1000f /
                                        (lastTimeMillis - startTimeMillis).toFloat()) / density
                                } else 0f
                                stableOnWorkspaceEdgeSwipeEnd(velocityDpPerSecond)
                                workspaceEdgeDragging = false
                            }
                            workspaceEdgeCandidate = false
                            stableOnDragMove(rootPosition)
                        } else if (workspaceEdgeCandidate) {
                            val dx = change.position.x - startPosition.x
                            val dy = change.position.y - startPosition.y
                            val fingerDeltaX = change.position.x - previousPosition.x
                            val inwardDistance = if (startedAtLeftEdge) dx else -dx
                            val mostlyHorizontal = kotlin.math.abs(dx) > kotlin.math.abs(dy) * 1.15f

                            if (!workspaceEdgeDragging &&
                                inwardDistance > 0f &&
                                kotlin.math.abs(dx) > viewConfiguration.touchSlop &&
                                mostlyHorizontal
                            ) {
                                workspaceEdgeDragging = true
                                stableOnWorkspaceEdgeSwipeStart()
                                // Catch the workspace up to the finger immediately,
                                // including movement accumulated before touch slop.
                                stableOnWorkspaceEdgeSwipeDrag(dx)
                                change.consume()
                            } else if (workspaceEdgeDragging) {
                                stableOnWorkspaceEdgeSwipeDrag(fingerDeltaX)
                                change.consume()
                            } else if (mostlyHorizontal && inwardDistance > 0f) {
                                // Reserve an inward gesture that started on the physical edge;
                                // this prevents the drawer pager from beginning its own swipe.
                                change.consume()
                            } else if (kotlin.math.abs(dx) > viewConfiguration.touchSlop &&
                                mostlyHorizontal && inwardDistance <= 0f
                            ) {
                                workspaceEdgeCandidate = false
                            } else if (kotlin.math.abs(dy) > viewConfiguration.touchSlop &&
                                kotlin.math.abs(dy) > kotlin.math.abs(dx)
                            ) {
                                workspaceEdgeCandidate = false
                            }
                        }

                        previousPosition = change.position
                        lastPosition = change.position
                        lastTimeMillis = change.uptimeMillis
                        pressed = change.pressed
                    }

                    if (workspaceEdgeDragging) {
                        val elapsedMillis = (lastTimeMillis - startTimeMillis).coerceAtLeast(1L)
                        val velocityDpPerSecond = ((lastPosition.x - startPosition.x) * 1000f /
                            elapsedMillis.toFloat()) / density
                        stableOnWorkspaceEdgeSwipeEnd(velocityDpPerSecond)
                    }
                }
            }
            .alpha(if (visuallyHidden) 0f else 1f)
    ) {
        val backgroundAlpha = backgroundOpacityPercent.coerceIn(0, 100) / 100f
        val textColor = parseAppFrameColor(backgroundTextColorHex)
        val densityValue = LocalDensity.current.density
        Box(modifier = Modifier.fillMaxSize()) {
            when (backgroundMode) {
                AppDrawerBackgroundMode.SOLID -> Box(
                    Modifier.fillMaxSize().background(parseAppFrameColor(backgroundColorAHex).copy(alpha = backgroundAlpha))
                )
                AppDrawerBackgroundMode.GRADIENT -> Box(
                    Modifier.fillMaxSize().background(
                        Brush.verticalGradient(
                            listOf(
                                parseAppFrameColor(backgroundColorAHex).copy(alpha = backgroundAlpha),
                                parseAppFrameColor(backgroundColorBHex).copy(alpha = backgroundAlpha)
                            )
                        )
                    )
                )
                AppDrawerBackgroundMode.IMAGE -> {
                    if (backgroundImage != null) {
                        Image(backgroundImage, null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                    }
                    Box(Modifier.fillMaxSize().background(parseAppFrameColor(backgroundColorAHex).copy(alpha = backgroundAlpha * 0.65f)))
                }
            }
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .padding(bottom = bottomBarHeightDp.dp)
                    .padding(start = 16.dp, end = 16.dp, top = 12.dp)
            ) {
            AnimatedVisibility(visible = searchVisible) {
                Column {
                    OutlinedTextField(
                        value = query,
                        onValueChange = onQueryChange,
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        textStyle = MaterialTheme.typography.bodyLarge.copy(
                            color = textColor,
                            shadow = textShadow(textShadowSettings, densityValue)
                        ),
                        label = { Text(l10n(R.string.ui_search), color = textColor.copy(alpha = 0.82f)) },
                        placeholder = { Text(l10n(R.string.ui_application_name), color = textColor.copy(alpha = 0.56f)) },
                        shape = RoundedCornerShape(22.dp)
                    )
                    Spacer(Modifier.height(10.dp))
                }
            }
            when {
                isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                loadError != null -> InfoCard(l10n(R.string.ui_failed_to_load_applications), loadError.ifBlank { l10n(R.string.ui_unknown_error) }, l10n(R.string.ui_retry), onRetry)
                apps.isEmpty() -> InfoCard(l10n(R.string.ui_nothing_found), if (query.isBlank()) l10n(R.string.ui_the_system_returned_no_available_applications_total_found, totalAppCount) else l10n(R.string.ui_try_changing_the_search_query), null, null)
                mode == AppDrawerMode.VERTICAL_LIST -> LazyVerticalGrid(
                    columns = GridCells.Fixed(gridColumns),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(when { gridColumns >= 9 -> 2.dp; gridColumns >= 7 -> 4.dp; else -> 8.dp }),
                    verticalArrangement = Arrangement.spacedBy(when { gridRows >= 16 -> 1.dp; gridRows >= 11 -> 2.dp; gridRows >= 8 -> 4.dp; else -> 8.dp })
                ) {
                    itemsIndexed(apps, key = { _, app -> app.componentName.flattenToShortString() + "#" + app.user.hashCode() }) { index, app ->
                        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.TopCenter) {
                            AppIconCell(
                                app = app,
                                onClick = { onLaunchApp(app) },
                                onLongClick = { onStartPlacement(app) },
                                dragEnabled = !pickForPlacement && !pickForSideDock && !pickForBottomDock,
                                longPressMillis = reorderHoldMillis,
                                onDragStart = { onDragStart(app, it, index) },
                                onDragMove = onDragMove,
                                onDragEnd = onDragEnd,
                                itemWidthDp = itemWidthDp,
                                itemHeightDp = itemHeightDp,
                                frameShape = frameShape,
                                frameColorHex = frameColorHex,
                                frameOpacityPercent = frameOpacityPercent,
                                textColorHex = frameTextColorHex,
                                textShadowSettings = textShadowSettings
                            )
                        }
                    }
                }
                else -> PagedAppDrawer(
                    apps = apps,
                    queryActive = query.isNotBlank(),
                    savedPage = savedPage,
                    loopEnabled = loopEnabled,
                    mode = mode,
                    pageTransitionEffect = pageTransitionEffect,
                    minimumEffectDurationMs = minimumEffectDurationMs,
                    iconEffect = iconEffect,
                    gridColumns = gridColumns,
                    gridRows = gridRows,
                    itemWidthDp = itemWidthDp,
                    itemHeightDp = itemHeightDp,
                    frameShape = frameShape,
                    frameColorHex = frameColorHex,
                    frameOpacityPercent = frameOpacityPercent,
                    frameTextColorHex = backgroundTextColorHex,
                    backgroundTextColorHex = backgroundTextColorHex,
                    activeDragPosition = activeDragPosition,
                    activeDragSourceIndex = activeDragSourceIndex,
                    activeDragPreviewIndex = activeDragPreviewIndex,
                    edgeSwitchDp = edgeSwitchDp,
                    dragOverviewMode = dragOverviewMode,
                    crossPageDragMode = crossPageDragMode,
                    pagePreviewMode = pagePreviewMode,
                    dragPageHoverMillis = dragPageHoverMillis,
                    edgePreviewVisible = edgePreviewVisible,
                    bottomBarHeightDp = bottomBarHeightDp,
                    overviewRequestSerial = overviewRequestSerial,
                    onOverviewRequestConsumed = onOverviewRequestConsumed,
                    textShadowSettings = textShadowSettings,
                    reorderHoldMillis = reorderHoldMillis,
                    dragEnabled = !pickForPlacement && !pickForSideDock && !pickForBottomDock,
                    onPageChanged = onPageChanged,
                    onLaunchApp = onLaunchApp,
                    onStartPlacement = onStartPlacement,
                    onDragStart = { app, cellPosition, sourceIndex ->
                        pagedDragTracking = true
                        onDragStart(
                            app,
                            latestPagedPointerInRoot ?: cellPosition,
                            sourceIndex
                        )
                    },
                    // Movement for paged modes is deliberately NOT taken from
                    // the source AppIconCell. The stable drawer-level pointer
                    // tracker above supplies root-space movement instead.
                    onDragMove = {},
                    onDragEnd = { cellPosition ->
                        val finalPosition = latestPagedPointerInRoot ?: cellPosition
                        pagedDragTracking = false
                        onDragEnd(finalPosition)
                    }
                )
            }
        }
        }
    }
}

@Composable
private fun AppDrawerModeOption(
    mode: AppDrawerMode,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .background(
                if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)
                else Color.Transparent
            )
            .padding(horizontal = 8.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = onClick)
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = mode.title,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
            )
            Text(
                text = mode.description,
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.60f)
            )
        }
    }
}

@Composable
private fun InteractivePageEffectPreview(
    effect: PageTransitionEffect,
    appDrawer: Boolean,
    verticalPager: Boolean = false,
    showHint: Boolean = true,
    showPreviewLabel: Boolean = false
) {
    val realPageCount = 2
    val pagerPageCount = virtualPageCount(realPageCount, loopEnabled = true)
    val pagerState = rememberPagerState(
        initialPage = initialVirtualPage(realPageCount, logicalPage = 0, loopEnabled = true),
        pageCount = { pagerPageCount }
    )
    val gestureTuning = rememberPageGestureTuningState()
    val meshSnapshotCache = rememberMeshPageSnapshotCache(maxEntries = 4)
    var transitionSourcePage by remember(pagerState) {
        mutableIntStateOf(pagerState.currentPage)
    }
    LaunchedEffect(pagerState.isScrollInProgress, pagerState.currentPage) {
        if (!pagerState.isScrollInProgress) {
            transitionSourcePage = pagerState.currentPage
            delay(48)
            gestureTuning.reset()
        }
    }
    LaunchedEffect(pagerState.isScrollInProgress) {
        if (!pagerState.isScrollInProgress) {
            val edge = realPageCount * 3
            if (pagerState.currentPage < edge || pagerState.currentPage > pagerPageCount - edge) {
                pagerState.scrollToPage(
                    initialVirtualPage(
                        realPageCount,
                        logicalPage(pagerState.currentPage, realPageCount),
                        loopEnabled = true
                    )
                )
            }
        }
    }
    // In landscape, a fixed 190.dp preview can consume nearly all of the
    // settings dialog, leaving no room to choose an effect. Reserve most of
    // the available height for the effect list and its search row instead.
    // The landscape preview is at most 28% of the screen, comfortably below
    // the requested 50% ceiling; portrait retains the original 190.dp.
    val screenConfiguration = LocalConfiguration.current
    val previewHeight = if (
        screenConfiguration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE
    ) {
        (screenConfiguration.screenHeightDp * 0.28f).dp.coerceAtMost(190.dp)
    } else {
        190.dp
    }
    Box(
        modifier = Modifier.fillMaxWidth().height(previewHeight)
    ) {
        Card(
            modifier = Modifier.fillMaxSize(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF11141C))
        ) {
            val pageContent: @Composable (Int) -> Unit = { virtualPage ->
                val page = logicalPage(virtualPage, realPageCount)
                val signedOffset = (
                    (pagerState.currentPage - virtualPage) + pagerState.currentPageOffsetFraction
                ).coerceIn(-1f, 1f)
                val outgoing = virtualPage == transitionSourcePage
                MeshPageSurface(
                    effect = effect,
                    signedPageOffset = signedOffset,
                    verticalPager = verticalPager,
                    captureKey = listOf("effect-preview", effect.name, appDrawer, verticalPager, page),
                    snapshotCache = meshSnapshotCache,
                    meshDeform = outgoing || effect.usesPairedMeshTextures(),
                    isOutgoingPage = outgoing,
                    liveContentInteractive = virtualPage == pagerState.currentPage &&
                        !pagerState.isScrollInProgress,
                    transitionInProgress = pagerState.isScrollInProgress,
                    gestureTravelFraction = gestureTuning.travelFraction,
                    gestureReleased = gestureTuning.released,
                    modifier = Modifier.fillMaxSize()
                ) { EffectPreviewDemoPage(page = page, appDrawer = appDrawer) }
            }
            if (verticalPager) {
                VerticalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize().trackPageGestureTuning(gestureTuning, vertical = true),
                    beyondViewportPageCount = 1,
                    key = { virtualPage -> virtualPage }
                ) { virtualPage -> pageContent(virtualPage) }
            } else {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize().trackPageGestureTuning(gestureTuning, vertical = false),
                    beyondViewportPageCount = 1,
                    key = { virtualPage -> virtualPage }
                ) { virtualPage -> pageContent(virtualPage) }
            }
        }
        if (showPreviewLabel) {
            Text(
                text = l10n(R.string.ui_preview_label),
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(10.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color.Black.copy(alpha = 0.46f))
                    .padding(horizontal = 7.dp, vertical = 3.dp)
                    .zIndex(4f),
                style = MaterialTheme.typography.bodySmall,
                color = Color.White
            )
        }
    }
    if (showHint) {
        Text(
            l10n(R.string.ui_swipe_preview_in_both_directions),
            modifier = Modifier.fillMaxWidth().padding(top = 5.dp),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.58f)
        )
    }
}

@Composable
private fun EffectPreviewDemoPage(page: Int, appDrawer: Boolean) {
    val accent = if (page == 0) Color(0xFF6074C7) else Color(0xFF4E9B7B)
    BoxWithConstraints(Modifier.fillMaxSize().background(Color(0xFF171C28))) {
        // Shrink the demo elements along with the preview in landscape so the
        // page contents do not get cut off by the shorter preview viewport.
        val compact = maxHeight < 140.dp
        Box(Modifier.fillMaxSize().padding(if (compact) 7.dp else 16.dp)) {
            if (appDrawer) {
                Column(
                    Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceEvenly
                ) {
                    repeat(3) { row ->
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                            repeat(4) { column ->
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Box(
                                        Modifier.size(if (compact) 15.dp else 25.dp)
                                            .clip(RoundedCornerShape(if (compact) 5.dp else 8.dp))
                                            .background(accent.copy(alpha = 0.72f + (row + column) % 2 * 0.14f))
                                    )
                                    Spacer(Modifier.height(if (compact) 1.dp else 2.dp))
                                    Box(
                                        Modifier.size(
                                            width = if (compact) 14.dp else 24.dp,
                                            height = if (compact) 2.dp else 3.dp
                                        ).background(Color.White.copy(alpha = 0.30f))
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                Box(
                    Modifier.size(if (compact) 28.dp else 54.dp)
                        .align(Alignment.TopStart)
                        .clip(RoundedCornerShape(if (compact) 8.dp else 14.dp))
                        .background(accent)
                )
                Card(
                    modifier = Modifier.size(
                        width = if (compact) 76.dp else 112.dp,
                        height = if (compact) 38.dp else 62.dp
                    ).align(Alignment.Center),
                    colors = CardDefaults.cardColors(containerColor = accent.copy(alpha = 0.55f))
                ) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(l10n(R.string.ui_widget), fontSize = if (compact) 9.sp else 11.sp)
                    }
                }
                Box(
                    Modifier.size(if (compact) 27.dp else 52.dp)
                        .align(Alignment.BottomEnd)
                        .clip(RoundedCornerShape(if (compact) 8.dp else 14.dp))
                        .background(accent.copy(alpha = 0.75f)),
                    contentAlignment = Alignment.Center
                ) { Text("▦", fontSize = if (compact) 13.sp else 21.sp) }
            }
            Text(
                l10n(R.string.ui_page_number, page + 1),
                modifier = Modifier.align(Alignment.TopEnd),
                color = Color.White.copy(alpha = 0.65f),
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}

@Composable
private fun PageTransitionEffectList(
    query: String,
    includeAppDrawerOnly: Boolean,
    selectedEffect: PageTransitionEffect,
    selectedAtOpen: PageTransitionEffect,
    openingSerial: Int,
    groupExpansion: Map<PageEffectTechnologyGroup, Boolean>,
    onGroupExpansionChange: (PageEffectTechnologyGroup, Boolean) -> Unit,
    advancedIconEffectOverridesPage: Boolean = false,
    onEffectSelected: (PageTransitionEffect) -> Unit
) {
    val normalizedQuery = query.trim()
    val initialEffectRequester = remember { BringIntoViewRequester() }
    val matchingEffects = orderedPageTransitionEffects
        .asSequence()
        .filter { includeAppDrawerOnly || !it.appDrawerOnly }
        // Keep legacy IDs readable in stored settings, but no longer offer Canvas Glitter.
        .filter { it != PageTransitionEffect.GLITTER_OREO }
        // Retain enum constants to read older backups, but stop offering the discarded Mesh bars.
        .filter { it != PageTransitionEffect.RANDOM_BARS_SNAP_HORIZONTAL_MESH &&
            it != PageTransitionEffect.RANDOM_BARS_SNAP_VERTICAL_MESH }
        .filter { effect ->
            normalizedQuery.isBlank() ||
                effect.title.contains(normalizedQuery, ignoreCase = true) ||
                effect.description.contains(normalizedQuery, ignoreCase = true) ||
                l10n(effect.technologyGroup.titleRes).contains(normalizedQuery, ignoreCase = true)
        }
        .toList()
    // Older backups may still select one of the removed Mesh random-strip effects.
    // Always attach the bring-into-view requester to a visible row in that case.
    val selectedAtEntry = selectedAtOpen.takeIf { it in matchingEffects }
        ?: matchingEffects.firstOrNull { it.technologyGroup == selectedAtOpen.technologyGroup }
        ?: PageTransitionEffect.DEFAULT.takeIf { it in matchingEffects }
    LaunchedEffect(openingSerial) {
        if (openingSerial > 0 && selectedAtEntry != null) {
            // Only on entering the picker, never after selecting another effect.
            delay(150)
            // If the row was filtered away or the dialog closed, do not crash.
            runCatching { initialEffectRequester.bringIntoView() }
        }
    }

    PageEffectTechnologyGroup.values().forEach { group ->
        val groupEffects = matchingEffects.filter { it.technologyGroup == group }
        if (groupEffects.isNotEmpty()) {
            val storedExpanded = groupExpansion[group] != false
            val effectiveExpanded = normalizedQuery.isNotBlank() || storedExpanded
            PageEffectGroupHeader(
                group = group,
                effectCount = groupEffects.size,
                expanded = effectiveExpanded,
                enabled = normalizedQuery.isBlank(),
                onClick = { onGroupExpansionChange(group, !storedExpanded) }
            )
            AnimatedVisibility(visible = effectiveExpanded) {
                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    if (includeAppDrawerOnly &&
                        group != PageEffectTechnologyGroup.ANDROID_MESH &&
                        advancedIconEffectOverridesPage) {
                        Text(
                            l10n(R.string.ui_page_effect_overridden_by_icon_effect),
                            color = Color(0xFFFF7885),
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 8.dp)
                        )
                    }
                    groupEffects.forEach { effect ->
                        PageTransitionOption(
                            effect,
                            effect == selectedEffect,
                            modifier = if (effect == selectedAtEntry) {
                                Modifier.bringIntoViewRequester(initialEffectRequester)
                            } else Modifier
                        ) { onEffectSelected(effect) }
                    }
                }
            }
        }
    }
}

@Composable
private fun PageEffectGroupHeader(
    group: PageEffectTechnologyGroup,
    effectCount: Int,
    expanded: Boolean,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled, onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.075f))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 13.dp, vertical = 11.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (expanded) "▾" else "▸",
                modifier = Modifier.width(24.dp),
                color = MaterialTheme.colorScheme.primary,
                fontSize = 19.sp
            )
            Text(
                text = l10n(group.titleRes),
                modifier = Modifier.weight(1f),
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = effectCount.toString(),
                style = MaterialTheme.typography.labelMedium,
                color = Color.White.copy(alpha = 0.48f)
            )
        }
    }
}

@Composable
private fun PageTransitionOption(
    effect: PageTransitionEffect,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .background(
                if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)
                else Color.Transparent
            )
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = onClick)
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = effect.pickerTitle,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
            )
            Text(
                text = effect.description,
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.60f)
            )
        }
    }
}

@Composable
private fun DrawerIconEffectGroupHeader(
    title: String,
    expanded: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White.copy(alpha = 0.075f))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            if (expanded) "▾" else "▸",
            color = MaterialTheme.colorScheme.primary,
            fontSize = 20.sp,
            modifier = Modifier.width(28.dp)
        )
        Text(title, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun AppDrawerIconEffectOption(
    effect: AppDrawerIconEffect,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .background(
                if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)
                else Color.Transparent
            )
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = onClick)
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = effect.title,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
            )
            Text(
                text = effect.description,
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.60f)
            )
        }
    }
}

@Composable
private fun IconActivationSettingsPreview(
    effect: AppLaunchAnimationEffect,
    durationMillis: Int
) {
    val context = LocalContext.current
    val previewIcon = remember(context.packageName) {
        runCatching {
            context.packageManager
                .getApplicationIcon(context.packageName)
                .toBitmap(width = 160, height = 160)
                .asImageBitmap()
        }.getOrNull()
    }
    var previewSerial by remember(effect, durationMillis) { mutableIntStateOf(1) }
    LaunchedEffect(effect, durationMillis) {
        previewSerial += 1
    }
    Card(
        modifier = Modifier.fillMaxWidth().height(252.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.055f))
    ) {
        Box(
            modifier = Modifier.fillMaxSize().padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier.align(Alignment.TopStart),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(l10n(R.string.ui_preview), fontWeight = FontWeight.Bold)
                Text(
                    effect.title,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.68f)
                )
            }
            if (previewIcon != null) {
                Image(
                    bitmap = previewIcon,
                    contentDescription = l10n(R.string.ui_preview),
                    modifier = Modifier
                        .size(54.dp)
                        .iconActivationEffect(
                            activationSerial = previewSerial,
                            shape = RoundedCornerShape(16.dp),
                            kind = IconActivationKind.LAUNCH,
                            effectOverride = effect,
                            durationOverride = durationMillis
                        )
                        .clickable { previewSerial += 1 }
                )
            }
        }
    }
}

@Composable
private fun PagedAppDrawer(
    apps: List<AppItem>,
    queryActive: Boolean,
    savedPage: Int,
    loopEnabled: Boolean,
    mode: AppDrawerMode,
    pageTransitionEffect: PageTransitionEffect,
    minimumEffectDurationMs: Int,
    iconEffect: AppDrawerIconEffect,
    gridColumns: Int,
    gridRows: Int,
    itemWidthDp: Int,
    itemHeightDp: Int,
    frameShape: AppItemFrameShape,
    frameColorHex: String,
    frameOpacityPercent: Int,
    frameTextColorHex: String,
    backgroundTextColorHex: String,
    activeDragPosition: Offset?,
    activeDragSourceIndex: Int?,
    activeDragPreviewIndex: Int?,
    edgeSwitchDp: Int,
    dragOverviewMode: AppDrawerDragOverviewMode,
    crossPageDragMode: AppDrawerCrossPageDragMode,
    pagePreviewMode: AppDrawerPagePreviewMode,
    dragPageHoverMillis: Int,
    edgePreviewVisible: Boolean,
    bottomBarHeightDp: Int,
    overviewRequestSerial: Int,
    onOverviewRequestConsumed: () -> Unit,
    textShadowSettings: TextShadowSettings,
    reorderHoldMillis: Int,
    dragEnabled: Boolean,
    onPageChanged: (Int) -> Unit,
    onLaunchApp: (AppItem) -> Unit,
    onStartPlacement: (AppItem) -> Unit,
    onDragStart: (AppItem, Offset, Int) -> Unit,
    onDragMove: (Offset) -> Unit,
    onDragEnd: (Offset) -> Unit
) {
    BoxWithConstraints(Modifier.fillMaxSize()) {
        // In landscape (or on short screens), the manually configured number
        // of rows may not fit inside one pager page. A disabled-scrolling
        // LazyVerticalGrid silently hides the overflowing app icons. Page
        // capacity must follow the measured space actually available here,
        // without changing the user's stored grid settings.
        val rowSpacingDp = when {
            gridRows >= 16 -> 1f
            gridRows >= 11 -> 2f
            gridRows >= 8 -> 4f
            else -> 8f
        }
        // Reserve the page indicator below the pager (top/bottom padding and
        // its label), plus the grid's vertical content padding (6dp each side).
        val gridViewportDp = (maxHeight.value - 32f - 12f).coerceAtLeast(0f)
        val rowsThatFit = ((gridViewportDp + rowSpacingDp) /
            (itemHeightDp.coerceAtLeast(1) + rowSpacingDp)).toInt().coerceAtLeast(1)
        val visibleRows = minOf(gridRows.coerceAtLeast(1), rowsThatFit)
        key(queryActive, loopEnabled, gridColumns, visibleRows, mode) {
        val itemsPerPage = (gridColumns * visibleRows).coerceAtLeast(1)
        val pages = remember(apps, itemsPerPage) { apps.chunked(itemsPerPage) }
        val realPageCount = pages.size.coerceAtLeast(1)
        // Looping remains enabled during unified dragging; page switches resolve to the nearest virtual page.
        val effectiveLoop = loopEnabled && !queryActive && realPageCount > 1
        val pagerPageCount = virtualPageCount(realPageCount, effectiveLoop)
        val initialLogicalPage = if (queryActive) 0 else savedPage.coerceIn(0, realPageCount - 1)
        val pagerState = rememberPagerState(initialPage = initialVirtualPage(realPageCount, initialLogicalPage, effectiveLoop), pageCount = { pagerPageCount })
        val densityValue = LocalDensity.current.density
        val effectiveIconEffect = if (pageTransitionEffect.mesh) {
            AppDrawerIconEffect.NONE
        } else {
            iconEffect
        }
        // Advanced icon transitions own the visual movement. Keep BOTH pages
        // at their resting positions instead of letting Pager slide the grid or
        // page background behind the exploding/collapsing icon fragments.
        val stationaryIconTransition = effectiveIconEffect == AppDrawerIconEffect.SPIRAL ||
            effectiveIconEffect == AppDrawerIconEffect.FIREWORK ||
            effectiveIconEffect == AppDrawerIconEffect.BLACK_HOLE ||
            effectiveIconEffect == AppDrawerIconEffect.BIG_BANG
        val currentLogicalPage = logicalPage(pagerState.currentPage, realPageCount)
        val vertical = mode == AppDrawerMode.VERTICAL_PAGES
        val effectFling = if (minimumEffectDurationMs == 0) {
            PagerDefaults.flingBehavior(pagerState)
        } else {
            PagerDefaults.flingBehavior(
                pagerState,
                snapAnimationSpec = tween(durationMillis = maxOf(300, minimumEffectDurationMs))
            )
        }
        val gestureTuning = rememberPageGestureTuningState()
        val meshSnapshotCache = rememberMeshPageSnapshotCache(
            maxEntries = realPageCount.coerceIn(4, 12)
        )
        var transitionSourcePage by remember(pagerState) {
            mutableIntStateOf(pagerState.currentPage)
        }
        val drawerTextColor = parseAppFrameColor(backgroundTextColorHex)
        // Actual clipped page area, not the whole device screen. Firework
        // pieces should reach this boundary before leaving the view.
        var drawerPageBounds by remember { mutableStateOf<Rect?>(null) }
        val configuration = LocalConfiguration.current
        val density = LocalDensity.current
        val navigationScope = rememberCoroutineScope()
        val currentDragPosition by rememberUpdatedState(activeDragPosition)
        var showDragPageOverview by remember { mutableStateOf(false) }
        var showNavigationPageOverview by remember { mutableStateOf(false) }
        val dragPageBounds = remember { mutableStateMapOf<Int, Rect>() }
        var dragPageHoverTarget by remember { mutableStateOf<Int?>(null) }
        var dragPageHoverProgress by remember { mutableFloatStateOf(0f) }

        val currentOnOverviewRequestConsumed by rememberUpdatedState(onOverviewRequestConsumed)
        // The request is a one-shot command, not persistent drawer state.
        // Consume it in the parent; otherwise rebuilding the paged drawer on every
        // open replays the last request and shows the overview again.
        LaunchedEffect(overviewRequestSerial, activeDragPosition == null) {
            if (overviewRequestSerial > 0 && activeDragPosition == null) {
                showNavigationPageOverview = true
                currentOnOverviewRequestConsumed()
            }
        }

        LaunchedEffect(pagerState.isScrollInProgress, pagerState.currentPage) {
            if (!pagerState.isScrollInProgress) {
                transitionSourcePage = pagerState.currentPage
                delay(48)
                gestureTuning.reset()
            }
        }

        LaunchedEffect(edgeSwitchDp, realPageCount, vertical, crossPageDragMode) {
            var edgeArmed = true
            while (true) {
                delay(45)
                val position = currentDragPosition
                if (position == null) {
                    edgeArmed = true
                    showDragPageOverview = false
                    continue
                }
                if (realPageCount <= 1) continue
                val edgePx = with(density) { edgeSwitchDp.coerceIn(16, 140).dp.toPx() }
                val widthPx = with(density) { configuration.screenWidthDp.dp.toPx() }
                val heightPx = with(density) { configuration.screenHeightDp.dp.toPx() }
                val topActionsPx = with(density) { 110.dp.toPx() }
                val bottomTargetPx = with(density) { 104.dp.toPx() }
                val inEdge = position.y >= topActionsPx &&
                    position.y <= heightPx - bottomTargetPx &&
                    (position.x < edgePx || position.x > widthPx - edgePx)
                if (!inEdge) {
                    edgeArmed = true
                } else if (edgeArmed && !showDragPageOverview) {
                    edgeArmed = false
                    if (crossPageDragMode == AppDrawerCrossPageDragMode.PAGE_OVERVIEW) {
                        showDragPageOverview = true
                    } else {
                        val currentPage = logicalPage(pagerState.currentPage, realPageCount)
                        val towardPrevious = position.x < widthPx / 2f
                        val targetPage = Math.floorMod(currentPage + if (towardPrevious) -1 else 1, realPageCount)
                        pagerState.scrollToPage(if (effectiveLoop) nearestVirtualPage(pagerState.currentPage, targetPage, realPageCount) else targetPage)
                    }
                }
            }
            showDragPageOverview = false
        }

        LaunchedEffect(showDragPageOverview, activeDragPosition, dragPageBounds.toMap(), crossPageDragMode) {
            if (!showDragPageOverview || crossPageDragMode != AppDrawerCrossPageDragMode.PAGE_OVERVIEW) {
                dragPageHoverTarget = null
                return@LaunchedEffect
            }
            val position = activeDragPosition
            val target = position?.let { pointer ->
                dragPageBounds.entries.firstOrNull { it.value.contains(pointer) }?.key
            }
            if (target != dragPageHoverTarget) dragPageHoverTarget = target
        }

        LaunchedEffect(showDragPageOverview, dragPageHoverTarget, crossPageDragMode, dragPageHoverMillis) {
            val target = dragPageHoverTarget ?: return@LaunchedEffect
            if (!showDragPageOverview || crossPageDragMode != AppDrawerCrossPageDragMode.PAGE_OVERVIEW) return@LaunchedEffect
            delay(dragPageHoverMillis.coerceIn(250, 2000).toLong())
            val stillPosition = currentDragPosition
            if (showDragPageOverview && dragPageHoverTarget == target &&
                stillPosition != null && dragPageBounds[target]?.contains(stillPosition) == true
            ) {
                val pagerTarget = if (effectiveLoop) {
                    nearestVirtualPage(pagerState.currentPage, target, realPageCount)
                } else target
                pagerState.scrollToPage(pagerTarget)
                showDragPageOverview = false
                dragPageHoverTarget = null
            }
        }

        LaunchedEffect(showDragPageOverview, dragPageHoverTarget, dragPageHoverMillis) {
            dragPageHoverProgress = 0f
            if (!showDragPageOverview || dragPageHoverTarget == null) return@LaunchedEffect
            val durationNanos = dragPageHoverMillis.coerceIn(250, 2000) * 1_000_000L
            val visualDelayNanos = minOf(120_000_000L, durationNanos / 4L)
            var startNanos = 0L
            while (showDragPageOverview && dragPageHoverTarget != null && dragPageHoverProgress < 1f) {
                withFrameNanos { now ->
                    if (startNanos == 0L) startNanos = now
                    val elapsed = now - startNanos
                    dragPageHoverProgress = if (elapsed < visualDelayNanos) {
                        0f
                    } else {
                        (elapsed.toFloat() / durationNanos.toFloat()).coerceIn(0f, 1f)
                    }
                }
            }
        }

        LaunchedEffect(pagerState.currentPage, queryActive, realPageCount) { if (!queryActive) onPageChanged(currentLogicalPage) }
        LaunchedEffect(pagerState.isScrollInProgress, effectiveLoop, realPageCount) {
            if (!pagerState.isScrollInProgress && effectiveLoop) {
                val edge = realPageCount * 3
                if (pagerState.currentPage < edge || pagerState.currentPage > pagerPageCount - edge) {
                    pagerState.scrollToPage(initialVirtualPage(realPageCount, currentLogicalPage, true))
                }
            }
        }

        @Composable
        fun PageContent(virtualPage: Int) {
            val page = if (effectiveLoop) logicalPage(virtualPage, realPageCount) else virtualPage
            val pageApps = pages.getOrElse(page) { emptyList() }
            val signedPageOffset = ((pagerState.currentPage - virtualPage) + pagerState.currentPageOffsetFraction).coerceIn(-1f, 1f)
            val distance = signedPageOffset.absoluteValue
            val interactive = distance < 0.08f && virtualPage == pagerState.currentPage
            val pageLive = virtualPage == pagerState.currentPage && !pagerState.isScrollInProgress
            val isOutgoingPage = virtualPage == transitionSourcePage

            MeshPageSurface(
                // Ignore another page-level transform while advanced icons
                // are animating: otherwise the background still slides/rotates.
                effect = if (stationaryIconTransition) PageTransitionEffect.DEFAULT else pageTransitionEffect,
                signedPageOffset = signedPageOffset,
                verticalPager = vertical,
                captureKey = "drawer-$page-${pageApps.hashCode()}-$gridColumns-$visibleRows",
                snapshotCache = meshSnapshotCache,
                meshDeform = isOutgoingPage || pageTransitionEffect.usesPairedMeshTextures(),
                isOutgoingPage = isOutgoingPage,
                liveContentInteractive = pageLive,
                transitionInProgress = pagerState.isScrollInProgress,
                gestureTravelFraction = gestureTuning.travelFraction,
                gestureReleased = gestureTuning.released,
                modifier = Modifier
                    .zIndex(
                        if (interactive) 3f
                        else if (pageTransitionEffect.isMeshCube()) (1f - distance).coerceIn(0f, 1f)
                        else if (isOutgoingPage) 2f else 1f
                    )
                    .then(
                        if (stationaryIconTransition) Modifier.graphicsLayer {
                            // Pager moves the outgoing page by -offset * size,
                            // and the incoming page by the complementary offset.
                            // Counteract that movement ONCE at page level.
                            if (vertical) translationY = signedPageOffset * size.height
                            else translationX = signedPageOffset * size.width
                        } else Modifier
                    )
                    .onGloballyPositioned { coordinates ->
                        if (!pagerState.isScrollInProgress && virtualPage == pagerState.currentPage) {
                            val bounds = coordinates.boundsInRoot()
                            if (drawerPageBounds != bounds) drawerPageBounds = bounds
                        }
                    }
            ) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(gridColumns),
                    modifier = Modifier.fillMaxSize(),
                    userScrollEnabled = false,
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(when { gridColumns >= 9 -> 2.dp; gridColumns >= 7 -> 4.dp; else -> 8.dp }),
                    verticalArrangement = Arrangement.spacedBy(rowSpacingDp.dp)
                ) {
                    itemsIndexed(pageApps, key = { _, app -> app.componentName.flattenToShortString() + "#" + app.user.hashCode() }) { index, app ->
                        val globalIndex = page * itemsPerPage + index
                        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.TopCenter) {
                            val sourceAnchor = activeDragSourceIndex == globalIndex &&
                                activeDragPreviewIndex != null && activeDragPreviewIndex != activeDragSourceIndex
                            val previewTarget = activeDragPreviewIndex == globalIndex &&
                                activeDragPreviewIndex != activeDragSourceIndex
                            val previewApp = activeDragPreviewIndex?.let(apps::getOrNull)

                            if (sourceAnchor && previewApp != null) {
                                AppIconCell(
                                    app = previewApp,
                                    onClick = {},
                                    onLongClick = {},
                                    enabled = false,
                                    dragEnabled = false,
                                    itemWidthDp = itemWidthDp,
                                    itemHeightDp = itemHeightDp,
                                    frameShape = frameShape,
                                    frameColorHex = frameColorHex,
                                    frameOpacityPercent = frameOpacityPercent,
                                    textColorHex = frameTextColorHex,
                                    textShadowSettings = textShadowSettings,
                                    modifier = Modifier.appDrawerIconTransform(effectiveIconEffect, signedPageOffset, index, densityValue, vertical, drawerPageBounds, isOutgoingPage)
                                )
                            }

                            // Keep the original source cell alive for the whole pointer sequence.
                            // Only its appearance is hidden; the gesture owner is never moved to
                            // another LazyGrid or pager page.
                            AppIconCell(
                                app = app,
                                onClick = { onLaunchApp(app) },
                                onLongClick = { onStartPlacement(app) },
                                enabled = interactive || activeDragSourceIndex == globalIndex,
                                dragEnabled = dragEnabled,
                                longPressMillis = reorderHoldMillis,
                                onDragStart = { onDragStart(app, it, globalIndex) },
                                onDragMove = onDragMove,
                                onDragEnd = onDragEnd,
                                itemWidthDp = itemWidthDp,
                                itemHeightDp = itemHeightDp,
                                frameShape = frameShape,
                                frameColorHex = frameColorHex,
                                frameOpacityPercent = frameOpacityPercent,
                                textColorHex = frameTextColorHex,
                                textShadowSettings = textShadowSettings,
                                modifier = Modifier
                                    .alpha(if (sourceAnchor || previewTarget) 0.025f else 1f)
                                    .appDrawerIconTransform(effectiveIconEffect, signedPageOffset, index, densityValue, vertical, drawerPageBounds, isOutgoingPage)
                            )
                        }
                    }
                }
            }
        }

        Box(Modifier.fillMaxSize()) {
            Column(Modifier.fillMaxSize()) {
            if (vertical) {
                VerticalPager(state = pagerState, flingBehavior = effectFling, modifier = Modifier.weight(1f).trackPageGestureTuning(gestureTuning, vertical = true), userScrollEnabled = activeDragPosition == null, beyondViewportPageCount = if (activeDragPosition != null) realPageCount else 1, key = { it }) { PageContent(it) }
            } else {
                HorizontalPager(state = pagerState, flingBehavior = effectFling, modifier = Modifier.weight(1f).trackPageGestureTuning(gestureTuning, vertical = false), userScrollEnabled = activeDragPosition == null, beyondViewportPageCount = if (activeDragPosition != null) realPageCount else 1, key = { it }) { PageContent(it) }
            }
            Row(Modifier.fillMaxWidth().padding(top = 6.dp, bottom = 2.dp), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                repeat(realPageCount.coerceAtMost(20)) { index ->
                    val selected = index == currentLogicalPage
                    Box(Modifier.padding(horizontal = 3.dp).size(if (selected) 9.dp else 6.dp).clip(CircleShape).background(if (selected) MaterialTheme.colorScheme.primary else drawerTextColor.copy(alpha = 0.28f)))
                }
                Spacer(Modifier.width(8.dp))
                Text(
                    "${currentLogicalPage + 1} / $realPageCount",
                    style = MaterialTheme.typography.labelSmall.copy(
                        shadow = textShadow(textShadowSettings, densityValue)
                    ),
                    color = drawerTextColor.copy(alpha = 0.62f)
                )
            }
            }
            if (showNavigationPageOverview && activeDragPosition == null) {
                AppDrawerDragPageOverview(
                    pageCount = realPageCount,
                    currentPage = currentLogicalPage,
                    mode = AppDrawerDragOverviewMode.FILL,
                    previewMode = pagePreviewMode,
                    pages = pages,
                    hoveredPage = null,
                    hoverProgress = 0f,
                    onPageBounds = { _, _ -> },
                    onPageClick = { targetPage ->
                        val pagerTarget = if (effectiveLoop) {
                            nearestVirtualPage(pagerState.currentPage, targetPage, realPageCount)
                        } else targetPage
                        showNavigationPageOverview = false
                        navigationScope.launch {
                            pagerState.scrollToPage(pagerTarget)
                        }
                    },
                    onDismiss = { showNavigationPageOverview = false },
                    modifier = Modifier.fillMaxSize().zIndex(126f)
                )
            } else if (showDragPageOverview && activeDragPosition != null) {
                AppDrawerDragPageOverview(
                    pageCount = realPageCount,
                    currentPage = currentLogicalPage,
                    mode = dragOverviewMode,
                    previewMode = pagePreviewMode,
                    pages = pages,
                    hoveredPage = dragPageHoverTarget,
                    hoverProgress = dragPageHoverProgress,
                    onPageBounds = { page, bounds -> dragPageBounds[page] = bounds },
                    onPageClick = null,
                    onDismiss = null,
                    modifier = Modifier.fillMaxSize().zIndex(125f)
                )
            } else if (edgePreviewVisible || activeDragPosition != null) {
                DrawerEdgeZonesOverlay(
                    edgeDp = edgeSwitchDp,
                    vertical = false,
                    modifier = Modifier.fillMaxSize().zIndex(120f).padding(top = 110.dp, bottom = 104.dp)
                )
            }
        }
    }
    }
}

@Composable
private fun AppDrawerDragPageOverview(
    pageCount: Int,
    currentPage: Int,
    mode: AppDrawerDragOverviewMode,
    previewMode: AppDrawerPagePreviewMode,
    pages: List<List<AppItem>>,
    hoveredPage: Int?,
    hoverProgress: Float,
    onPageBounds: (Int, Rect) -> Unit,
    onPageClick: ((Int) -> Unit)? = null,
    onDismiss: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val orderedPages = remember(pageCount, currentPage) {
        List(pageCount) { Math.floorMod(currentPage + it, pageCount) }
    }
    @Composable
    fun PageCard(page: Int, modifier: Modifier = Modifier, registerBounds: Boolean = true) {
        val active = page == currentPage
        val hovered = page == hoveredPage
        Box(
            modifier = modifier
                .padding(5.dp)
                .then(if (registerBounds) Modifier.onGloballyPositioned { onPageBounds(page, it.boundsInRoot()) } else Modifier),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier.fillMaxSize().then(
                    if (onPageClick != null) Modifier.clickable { onPageClick(page) } else Modifier
                ),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (hovered) MaterialTheme.colorScheme.primary.copy(alpha = 0.86f)
                    else if (active) Color(0xF05A78B8) else Color(0xE05A6170)
                )
            ) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    if (previewMode == AppDrawerPagePreviewMode.MIPMAP) {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(4),
                            modifier = Modifier.fillMaxSize().padding(12.dp),
                            userScrollEnabled = false,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            items(pages.getOrElse(page) { emptyList() }.take(12), key = { it.componentName.flattenToShortString() }) { app ->
                                AppIconImage(app.label, app.icon, Modifier.size(18.dp))
                            }
                        }
                        Text(
                            "${page + 1}",
                            modifier = Modifier.align(Alignment.BottomEnd).padding(7.dp)
                                .background(Color.Black.copy(alpha = 0.48f), CircleShape).padding(horizontal = 7.dp, vertical = 3.dp),
                            fontWeight = FontWeight.Bold
                        )
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("${page + 1}", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                            Text(
                                if (active) l10n(R.string.ui_current_page)
                                else if (onPageClick != null) l10n(R.string.ui_tap_to_open)
                                else l10n(R.string.ui_hold_to_switch),
                                fontSize = 9.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
            if (hovered && hoverProgress > 0f) {
                Canvas(Modifier.size(50.dp).zIndex(4f)) {
                    val strokePx = 3.dp.toPx()
                    drawArc(
                        color = Color.White.copy(alpha = 0.20f),
                        startAngle = -90f,
                        sweepAngle = 360f,
                        useCenter = false,
                        style = Stroke(width = strokePx)
                    )
                    drawArc(
                        color = Color.White,
                        startAngle = -90f,
                        sweepAngle = 360f * hoverProgress.coerceIn(0f, 1f),
                        useCenter = false,
                        style = Stroke(width = strokePx, cap = StrokeCap.Round)
                    )
                }
            }
        }
    }
    Box(modifier.background(Color.Black.copy(alpha = 0.42f))) {
        if (onDismiss != null) {
            Box(
                Modifier.fillMaxSize().clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDismiss
                )
            )
        }
        Box(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().padding(18.dp)) {
        when (mode) {
            AppDrawerDragOverviewMode.FILL -> LazyVerticalGrid(
                columns = GridCells.Adaptive(110.dp),
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalArrangement = Arrangement.Center
            ) { items(orderedPages, key = { it }) { PageCard(it, Modifier.height(142.dp)) } }
        }
        }
    }
}

@Composable
private fun DrawerEdgeZonesOverlay(
    edgeDp: Int,
    vertical: Boolean,
    modifier: Modifier = Modifier
) {
    val alpha = 0.25f
    Box(modifier = modifier) {
        if (vertical) {
            Box(
                Modifier.align(Alignment.TopCenter).fillMaxWidth()
                    .height(edgeDp.dp).background(Color(0xFF4FC3F7).copy(alpha = alpha))
            )
            Box(
                Modifier.align(Alignment.BottomCenter).fillMaxWidth()
                    .height(edgeDp.dp).background(Color(0xFF4FC3F7).copy(alpha = alpha))
            )
        } else {
            Box(
                Modifier.align(Alignment.CenterStart).width(edgeDp.dp)
                    .fillMaxHeight().background(Color(0xFF4FC3F7).copy(alpha = alpha))
            )
            Box(
                Modifier.align(Alignment.CenterEnd).width(edgeDp.dp)
                    .fillMaxHeight().background(Color(0xFF4FC3F7).copy(alpha = alpha))
            )
        }
    }
}

@Composable
private fun AppDrawerBottomBar(
    sortMode: AppSortMode,
    searchActive: Boolean,
    quickAction: AppDrawerQuickAction,
    onSort: () -> Unit,
    onSearch: () -> Unit,
    onClose: () -> Unit,
    onQuickAction: () -> Unit,
    onSettings: () -> Unit,
    onHeightChanged: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .onSizeChanged { onHeightChanged(it.height) }
            .navigationBarsPadding()
            .padding(horizontal = 8.dp, vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        DrawerBarIcon(R.drawable.ic_sort, l10n(R.string.ui_sort, sortMode.title), false, onSort)
        DrawerBarIcon(R.drawable.ic_search, l10n(R.string.ui_search), searchActive, onSearch)
        Button(onClick = onClose, shape = CircleShape, contentPadding = PaddingValues(0.dp), modifier = Modifier.size(58.dp)) {
            Image(painterResource(R.drawable.ic_close), l10n(R.string.ui_close), Modifier.size(28.dp), colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onPrimary))
        }
        DrawerBarIcon(
            appDrawerQuickActionIconRes(quickAction),
            appDrawerQuickActionTitle(quickAction),
            false,
            onQuickAction
        )
        DrawerBarIcon(R.drawable.ic_settings, l10n(R.string.ui_app_drawer_settings), false, onSettings)
    }
}

@Composable
private fun DrawerBarIcon(iconRes: Int, description: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier.size(48.dp).clip(CircleShape)
            .background(if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.28f) else Color.White.copy(alpha = 0.06f))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Image(painterResource(iconRes), description, Modifier.size(25.dp), colorFilter = ColorFilter.tint(if (selected) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.86f)))
    }
}

@Composable
private fun SideDockPanel(
    position: SideDockPosition,
    opacityPercent: Int,
    textColorHex: String,
    textShadowSettings: TextShadowSettings,
    entries: List<SideDockEntry>,
    appsByComponent: Map<String, AppItem>,
    deleteMode: Boolean,
    editingLocked: Boolean,
    onLockedDrag: () -> Unit,
    externalDragPointerInRoot: Offset?,
    externalDragIsFolder: Boolean,
    onFolderFanStart: (SideDockEntry, Offset) -> Unit,
    onFolderFanDrag: (SideDockEntry, Offset) -> Unit,
    onFolderFanEnd: (SideDockEntry) -> Unit,
    folderCreationDelayMillis: Int,
    onAdd: () -> Unit,
    onToggleDeleteMode: () -> Unit,
    onLaunch: (SideDockEntry) -> Unit,
    onRemove: (String) -> Unit,
    onReorder: (Int, Int) -> Unit,
    onCreateFolder: (String, String) -> Unit,
    onAddToFolder: (String, String) -> Unit,
    onMoveToWorkspace: (SideDockEntry, Offset) -> Unit,
    onContextMenu: (SideDockEntry) -> Unit,
    onExternalInsertionTargetChange: (Int?) -> Unit,
    onExternalHoverTargetChange: (String?) -> Unit,
    onEmptyTap: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val density = LocalDensity.current
    val configuration = LocalConfiguration.current
    val scope = rememberCoroutineScope()
    val currentEntries by rememberUpdatedState(entries)
    val textColor = parseAppFrameColor(textColorHex)
    val currentOnReorder by rememberUpdatedState(onReorder)
    var draggedId by remember { mutableStateOf<String?>(null) }
    var dragOffset by remember { mutableStateOf(Offset.Zero) }
    var dragStartPointer by remember { mutableStateOf<Offset?>(null) }
    val entryBounds = remember { mutableStateMapOf<String, Rect>() }
    var dockBounds by remember { mutableStateOf(Rect.Zero) }
    // Convert the dock's root coordinates to screen coordinates before
    // computing the workspace drop. Root coordinates omit window insets.
    var dockRootToScreen by remember { mutableStateOf(Offset.Zero) }
    var insertionIndex by remember { mutableStateOf<Int?>(null) }
    var pendingRemoval by remember { mutableStateOf<SideDockEntry?>(null) }
    var folderHoverTargetId by remember { mutableStateOf<String?>(null) }
    var folderDropReadyTargetId by remember { mutableStateOf<String?>(null) }
    var folderHoverSerial by remember { mutableIntStateOf(0) }
    val removeThresholdPx = with(density) { 125.dp.toPx() }
    val screenWidthPx = with(density) { configuration.screenWidthDp.dp.toPx() }
    val screenHeightPx = with(density) { configuration.screenHeightDp.dp.toPx() }
    val externalHoverTarget = if (draggedId == null && !externalDragIsFolder) externalDragPointerInRoot?.let { pointer ->
        entries.firstOrNull { entryBounds[it.id]?.contains(pointer) == true }?.id
    } else null
    val externalInsertionIndex = if (draggedId == null && externalDragPointerInRoot != null &&
        externalDragPointerInRoot.x in dockBounds.left..dockBounds.right &&
        externalDragPointerInRoot.y in dockBounds.top..dockBounds.bottom && externalHoverTarget == null
    ) {
        entries.mapNotNull { entryBounds[it.id] }.takeIf { it.size == entries.size }
            ?.indexOfFirst { externalDragPointerInRoot.y < it.center.y }
            ?.let { if (it < 0) entries.size else it }
    } else null
    LaunchedEffect(externalInsertionIndex) { onExternalInsertionTargetChange(externalInsertionIndex) }
    LaunchedEffect(externalHoverTarget) { onExternalHoverTargetChange(externalHoverTarget) }

    Box(modifier = Modifier.width(90.dp).fillMaxSize().onGloballyPositioned {
        dockBounds = it.boundsInRoot()
        dockRootToScreen = it.positionOnScreen() - it.positionInRoot()
    }) {
        Card(
            modifier = Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().padding(
                start = if (position == SideDockPosition.LEFT) 8.dp else 0.dp,
                end = if (position == SideDockPosition.RIGHT) 8.dp else 0.dp,
                top = 70.dp,
                bottom = 104.dp
            ),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1B1B23).copy(alpha = opacityPercent.coerceIn(0, 100) / 100f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 14.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier.size(52.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary)
                        .pointerInput(Unit) { detectTapGestures(onTap = { onAdd() }, onLongPress = { haptic.performHapticFeedback(HapticFeedbackType.LongPress); if (editingLocked) onLockedDrag() else onToggleDeleteMode() }) },
                    contentAlignment = Alignment.Center
                ) { Text("+", color = MaterialTheme.colorScheme.onPrimary, style = MaterialTheme.typography.headlineMedium) }

                entries.forEachIndexed { index, entry ->
                    val app = entry.componentName?.let { appsByComponent[it] }
                    val isDragging = draggedId == entry.id
                    var activationSerial by remember(entry.id) { mutableIntStateOf(0) }
                    val activationInteraction = remember(entry.id) { MutableInteractionSource() }
                    val activationPressed by activationInteraction.collectIsPressedAsState()
                    LaunchedEffect(activationPressed) {
                        if (activationPressed) activationSerial += 1
                    }
                    Box(modifier = Modifier.width(72.dp)
                        .onGloballyPositioned { entryBounds[entry.id] = it.boundsInRoot() }
                        .alpha(if (isDragging) 0.12f else 1f)
                        .graphicsLayer {
                            val scale = if (externalHoverTarget == entry.id || folderDropReadyTargetId == entry.id) 1.10f else 1f
                            scaleX = scale; scaleY = scale
                        }) {
                        SideDockEntryContent(entry, app, appsByComponent, textColor, textShadowSettings, activationSerial,
                            modifier = Modifier.fillMaxWidth().pointerInput(entry.id, deleteMode, editingLocked, entries.size) {
                                if (editingLocked) {
                                    detectDragGesturesAfterLongPress(
                                        onDragStart = { onLockedDrag() },
                                        onDrag = { change, _ -> change.consume() }
                                    )
                                } else if (!deleteMode) {
                                    // Long-press still moves the folder itself. A vertical
                                    // swipe on the CLOSED folder instead starts its icon fan.
                                    val startDrag: (Offset) -> Unit = { local ->
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        draggedId = entry.id
                                        dragOffset = Offset.Zero
                                        dragStartPointer = entryBounds[entry.id]?.topLeft?.plus(local)
                                        insertionIndex = null
                                        folderHoverTargetId = null
                                        folderDropReadyTargetId = null
                                        folderHoverSerial += 1
                                    }
                                    val cancelDrag: () -> Unit = {
                                        draggedId = null; dragOffset = Offset.Zero; dragStartPointer = null; insertionIndex = null
                                        folderHoverTargetId = null; folderDropReadyTargetId = null; folderHoverSerial += 1
                                    }
                                    val endDrag: () -> Unit = {
                                        val away = if (position == SideDockPosition.LEFT) dragOffset.x > removeThresholdPx else dragOffset.x < -removeThresholdPx
                                        val folderTarget = currentEntries.firstOrNull { it.id == folderDropReadyTargetId }
                                        if (folderTarget?.isFolder == true) {
                                            onAddToFolder(entry.id, folderTarget.id)
                                        } else if (folderTarget != null) {
                                            onCreateFolder(entry.id, folderTarget.id)
                                        } else if (dragOffset.getDistance() < with(density) { 12.dp.toPx() }) {
                                            onContextMenu(entry)
                                        } else if (away) {
                                            val dropPoint = (dragStartPointer ?: dockBounds.center) + dragOffset
                                            onMoveToWorkspace(entry, dropPoint + dockRootToScreen)
                                        } else {
                                            val latest = currentEntries
                                            val from = latest.indexOfFirst { it.id == entry.id }
                                            if (from >= 0) insertionIndex?.let { currentOnReorder(from, it) }
                                        }
                                        cancelDrag()
                                    }
                                    val moveDrag: (Offset) -> Unit = { amount ->
                                        dragOffset += amount
                                        val finger = dragStartPointer?.plus(dragOffset)
                                        val remaining = currentEntries.filter { it.id != entry.id }
                                            .mapNotNull { entryBounds[it.id] }
                                        insertionIndex = if (finger != null &&
                                            finger.x in dockBounds.left..dockBounds.right &&
                                            finger.y in dockBounds.top..dockBounds.bottom &&
                                            remaining.size == currentEntries.size - 1
                                        ) remaining.indexOfFirst { finger.y < it.center.y }
                                            .let { if (it < 0) remaining.size else it }
                                        else null
                                        val hover = finger?.let { point ->
                                            currentEntries.firstOrNull { candidate ->
                                                candidate.id != entry.id && entryBounds[candidate.id]?.contains(point) == true &&
                                                    entry.componentName != null && entry.intentAction == null &&
                                                    (candidate.isFolder || (candidate.componentName != null && candidate.intentAction == null))
                                            }?.id
                                        }
                                        if (hover != folderHoverTargetId) {
                                            folderHoverTargetId = hover
                                            folderDropReadyTargetId = null
                                            folderHoverSerial += 1
                                            val serial = folderHoverSerial
                                            if (hover != null) scope.launch {
                                                delay(folderCreationDelayMillis.coerceIn(300, 2000).toLong())
                                                if (serial == folderHoverSerial && draggedId == entry.id && folderHoverTargetId == hover) {
                                                    folderDropReadyTargetId = hover
                                                }
                                            }
                                        }
                                    }
                                    if (entry.isFolder) {
                                        // The dock's verticalScroll is a parent pointer
                                        // handler. Observe Initial before it can consume a
                                        // vertical swipe; do not consume taps or horizontal
                                        // movements, which have their usual behavior.
                                        awaitEachGesture {
                                            val down = awaitFirstDown(
                                                requireUnconsumed = false,
                                                pass = PointerEventPass.Initial
                                            )
                                            val start = down.position
                                            var startFan = false
                                            var firstFanMovement = Offset.Zero
                                            val releasedOrMoved = withTimeoutOrNull(
                                                viewConfiguration.longPressTimeoutMillis
                                            ) {
                                                while (true) {
                                                    val change = awaitPointerEvent(
                                                        PointerEventPass.Initial
                                                    ).changes.firstOrNull { it.id == down.id }
                                                        ?: return@withTimeoutOrNull true
                                                    if (!change.pressed) return@withTimeoutOrNull true
                                                    val movement = change.position - start
                                                    if (movement.getDistance() > viewConfiguration.touchSlop) {
                                                        if (kotlin.math.abs(movement.x) <=
                                                            kotlin.math.abs(movement.y) * 0.75f
                                                        ) {
                                                            startFan = true
                                                            firstFanMovement = movement
                                                            change.consume()
                                                        }
                                                        return@withTimeoutOrNull true
                                                    }
                                                }
                                            }
                                            when {
                                                startFan -> {
                                                    onFolderFanStart(
                                                        entry,
                                                        entryBounds[entry.id]?.center ?: dockBounds.center
                                                    )
                                                    if (firstFanMovement != Offset.Zero) {
                                                        onFolderFanDrag(entry, firstFanMovement)
                                                    }
                                                    try {
                                                        while (true) {
                                                            val change = awaitPointerEvent(
                                                                PointerEventPass.Initial
                                                            ).changes.firstOrNull { it.id == down.id }
                                                                ?: break
                                                            if (!change.pressed) break
                                                            val delta = change.position - change.previousPosition
                                                            change.consume()
                                                            if (delta != Offset.Zero) onFolderFanDrag(entry, delta)
                                                        }
                                                    } finally {
                                                        onFolderFanEnd(entry)
                                                    }
                                                }
                                                releasedOrMoved == null -> {
                                                    startDrag(start)
                                                    var endedNormally = false
                                                    try {
                                                        while (true) {
                                                            val change = awaitPointerEvent(
                                                                PointerEventPass.Initial
                                                            ).changes.firstOrNull { it.id == down.id }
                                                                ?: break
                                                            if (!change.pressed) {
                                                                endedNormally = true
                                                                break
                                                            }
                                                            val delta = change.position - change.previousPosition
                                                            change.consume()
                                                            if (delta != Offset.Zero) moveDrag(delta)
                                                        }
                                                    } finally {
                                                        if (endedNormally) endDrag() else cancelDrag()
                                                    }
                                                }
                                            }
                                        }
                                    } else {
                                        detectDragGesturesAfterLongPress(
                                            onDragStart = startDrag,
                                            onDragCancel = cancelDrag,
                                            onDragEnd = endDrag,
                                            onDrag = { change, amount ->
                                                change.consume()
                                                moveDrag(amount)
                                            }
                                        )
                                    }
                                }
                            }.clickable(
                                enabled = !deleteMode && !isDragging,
                                interactionSource = activationInteraction,
                                indication = null
                            ) { onLaunch(entry) }
                        )
                        if (folderDropReadyTargetId == entry.id) {
                            val source = currentEntries.firstOrNull { it.id == draggedId }
                            val previewComponents = buildList {
                                if (entry.isFolder) addAll(entry.componentNames) else entry.componentName?.let { add(it) }
                                source?.componentName?.let { add(it) }
                            }.distinct().take(4)
                            Card(
                                modifier = Modifier.align(Alignment.TopCenter).padding(top = 8.dp).size(48.dp).alpha(0.76f),
                                shape = folderShape(entry.folderShape),
                                colors = CardDefaults.cardColors(containerColor = parseAppFrameColor(entry.folderColorHex))
                            ) {
                                Column(Modifier.fillMaxSize().padding(4.dp), verticalArrangement = Arrangement.SpaceEvenly) {
                                    previewComponents.chunked(2).forEach { row ->
                                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                                            row.forEach { component ->
                                                val child = appsByComponent[component]
                                                AppIconImage(child?.label ?: entry.label, child?.icon, Modifier.size(16.dp))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        if (deleteMode) Box(
                            modifier = Modifier.align(Alignment.TopEnd).size(25.dp).clip(CircleShape)
                                .background(Color(0xFFE34B55)).clickable { pendingRemoval = entry },
                            contentAlignment = Alignment.Center
                        ) { Text("×", color = Color.White, fontWeight = FontWeight.Bold) }
                    }
                }
                Spacer(Modifier.height(80.dp).fillMaxWidth().clickable { onEmptyTap() })
            }
        }

        val draggedIndex = entries.indexOfFirst { it.id == draggedId }
        val draggedEntry = entries.getOrNull(draggedIndex)
        val visibleInsertionIndex = if (folderDropReadyTargetId != null) null
            else if (draggedEntry != null) insertionIndex else externalInsertionIndex
        if (visibleInsertionIndex != null) {
            val remaining = if (draggedEntry != null) entries.filter { it.id != draggedEntry.id }.mapNotNull { entryBounds[it.id] }
                else entries.mapNotNull { entryBounds[it.id] }
            if (remaining.size == entries.size - if (draggedEntry != null) 1 else 0) {
                val slot = visibleInsertionIndex.coerceIn(0, remaining.size)
                val lineY = when {
                    remaining.isEmpty() -> dockBounds.center.y
                    slot == 0 -> remaining.first().top
                    slot == remaining.size -> remaining.last().bottom
                    else -> (remaining[slot - 1].bottom + remaining[slot].top) / 2f
                }
                Box(Modifier.offset { IntOffset(with(density) { 16.dp.roundToPx() },
                    (lineY - dockBounds.top).roundToInt()) }
                    .width(56.dp).height(3.dp).background(MaterialTheme.colorScheme.primary, RoundedCornerShape(2.dp))
                    .zIndex(59f))
            }
        }
        if (draggedEntry != null) {
            val app = draggedEntry.componentName?.let { appsByComponent[it] }
            SideDockEntryContent(
                draggedEntry,
                app,
                appsByComponent,
                textColor,
                textShadowSettings,
                modifier = Modifier.offset {
                    IntOffset(
                        ((entryBounds[draggedEntry.id]?.left ?: dockBounds.left) - dockBounds.left).roundToInt(),
                        ((entryBounds[draggedEntry.id]?.top ?: dockBounds.top) - dockBounds.top).roundToInt()
                    )
                }.width(72.dp).graphicsLayer {
                    translationX = dragOffset.x
                    translationY = dragOffset.y
                    scaleX = 1.10f
                    scaleY = 1.10f
                    shadowElevation = 18f
                }.zIndex(60f)
            )
        }
    }
    pendingRemoval?.let { entry ->
        DeleteConfirmationDialog(
            title = entry.label,
            message = l10n(R.string.ui_delete_this_icon_from_the_side_dock),
            onDismiss = { pendingRemoval = null },
            onConfirm = {
                pendingRemoval = null
                onRemove(entry.id)
            }
        )
    }
}

@Composable
private fun SideDockEntryContent(
    entry: SideDockEntry,
    app: AppItem?,
    appsByComponent: Map<String, AppItem>,
    textColor: Color,
    textShadowSettings: TextShadowSettings,
    activationSerial: Int = 0,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current.density
    val customFolderIcon = rememberLauncherCustomIcon(
        entry.customIconUri, entry.customIconPackPackage, entry.customIconPackDrawable
    )
    Column(
        modifier = modifier.background(Color.White.copy(alpha = 0.06f), RoundedCornerShape(20.dp)).padding(vertical = 8.dp, horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val iconModifier = Modifier.size(if (app != null) 46.dp else 42.dp)
            .iconActivationEffect(activationSerial, RoundedCornerShape(14.dp))
        if (entry.isFolder && customFolderIcon != null) {
            AppIconImage(entry.label, customFolderIcon, iconModifier.clip(folderShape(entry.folderShape)))
        } else if (entry.isFolder) {
            Card(
                modifier = iconModifier,
                shape = folderShape(entry.folderShape),
                colors = CardDefaults.cardColors(containerColor = parseAppFrameColor(entry.folderColorHex).copy(alpha = entry.folderOpacityPercent / 100f))
            ) {
                Column(Modifier.fillMaxSize().padding(4.dp), verticalArrangement = Arrangement.SpaceEvenly) {
                    entry.componentNames.take(4).chunked(2).forEach { row ->
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                            row.forEach { component ->
                                val child = appsByComponent[component]
                                AppIconImage(child?.label ?: entry.label, child?.icon, Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        } else if (app != null) AppIconImage(app.label, app.icon, iconModifier) else Image(
            painter = painterResource(dockActionIconRes(canonicalLauncherAction(entry.intentAction))), contentDescription = entry.label,
            modifier = iconModifier, colorFilter = ColorFilter.tint(textColor)
        )
        Spacer(Modifier.height(4.dp))
        Text(
            if (entry.isFolder) entry.label else app?.label ?: entry.label,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.labelSmall.copy(shadow = textShadow(textShadowSettings, density)),
            color = textColor
        )
    }
}

@Composable
private fun SideDockAddDialog(
    onDismiss: () -> Unit,
    availablePresets: List<PresetDockIcon>,
    onAddPreset: (PresetDockIcon) -> Unit,
    onAddApp: () -> Unit,
    onAddSettings: () -> Unit,
    onAddLauncherAction: () -> Unit,
    onAddWorkspaceEffect: () -> Unit,
    onAddMenuEffect: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(l10n(R.string.ui_add_to_side_dock)) },
        text = {
            Column(
                Modifier.heightIn(max = 560.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PresetDockIconSection(availablePresets, onAddPreset)
                WorkspaceActionButton(l10n(R.string.ui_application), onAddApp)
                WorkspaceActionButton(l10n(R.string.ui_settings_item_shortcut), onAddSettings)
                WorkspaceActionButton(l10n(R.string.ui_desktop_effect), onAddWorkspaceEffect)
                WorkspaceActionButton(l10n(R.string.ui_app_drawer_effect), onAddMenuEffect)
                WorkspaceActionButton(l10n(R.string.ui_launcher_action), onAddLauncherAction)
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text(l10n(R.string.ui_cancel)) } }
    )
}

@Composable
private fun BottomDockAddDialog(
    onDismiss: () -> Unit,
    availablePresets: List<PresetDockIcon>,
    onAddPreset: (PresetDockIcon) -> Unit,
    onAddApp: () -> Unit,
    onAddSettings: () -> Unit,
    onAddLauncherAction: () -> Unit,
    onAddWorkspaceEffect: () -> Unit,
    onAddMenuEffect: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(l10n(R.string.ui_add_to_bottom_dock)) },
        text = {
            Column(
                Modifier.heightIn(max = 560.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PresetDockIconSection(availablePresets, onAddPreset)
                WorkspaceActionButton(l10n(R.string.ui_application), onAddApp)
                WorkspaceActionButton(l10n(R.string.ui_settings_item_shortcut), onAddSettings)
                WorkspaceActionButton(l10n(R.string.ui_desktop_effect), onAddWorkspaceEffect)
                WorkspaceActionButton(l10n(R.string.ui_app_drawer_effect), onAddMenuEffect)
                WorkspaceActionButton(l10n(R.string.ui_launcher_action), onAddLauncherAction)
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text(l10n(R.string.ui_cancel)) } }
    )
}

@Composable
private fun SideDockEntryDialog(
    entry: SideDockEntry,
    onDismiss: () -> Unit,
    onRemove: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(entry.label) },
        text = { Text(l10n(R.string.ui_delete_this_item_from_the_side_dock)) },
        confirmButton = { TextButton(onClick = onRemove) { Text(l10n(R.string.ui_delete), color = Color(0xFFFF7A83)) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(l10n(R.string.ui_cancel)) } }
    )
}

private data class BottomDockInsertionPreview(
    val targetIndex: Int,
    val lineXInRoot: Float,
    val lineTopInRoot: Float,
    val lineBottomInRoot: Float
)

private fun calculateBottomDockInsertionPreview(
    pointerInRoot: Offset,
    draggedIndex: Int,
    orderedBounds: List<Rect>
): BottomDockInsertionPreview? {
    if (draggedIndex !in orderedBounds.indices || orderedBounds.isEmpty()) return null
    val lineTop = orderedBounds.minOf { it.top }
    val lineBottom = orderedBounds.maxOf { it.bottom }
    if (pointerInRoot.y !in lineTop..lineBottom) return null

    // Remove the source slot before calculating both the marker and the final
    // insertion index. Otherwise the source leaves a phantom gap and makes
    // right-to-left moves display one slot to the right of the actual result.
    val remainingBounds = orderedBounds.filterIndexed { index, _ -> index != draggedIndex }
    if (remainingBounds.isEmpty()) {
        val onlyBounds = orderedBounds[draggedIndex]
        val lineX = if (pointerInRoot.x < onlyBounds.center.x) onlyBounds.left else onlyBounds.right
        return BottomDockInsertionPreview(0, lineX, lineTop, lineBottom)
    }

    val insertionIndex = remainingBounds.indexOfFirst { pointerInRoot.x < it.center.x }
        .let { if (it < 0) remainingBounds.size else it }
    val lineX = when (insertionIndex) {
        0 -> remainingBounds.first().left
        remainingBounds.size -> remainingBounds.last().right
        else -> (remainingBounds[insertionIndex - 1].right + remainingBounds[insertionIndex].left) / 2f
    }
    return BottomDockInsertionPreview(insertionIndex, lineX, lineTop, lineBottom)
}

private fun calculateExternalBottomDockInsertionPreview(
    pointerInRoot: Offset,
    orderedBounds: List<Rect>,
    dockBoundsInRoot: Rect,
    emptyLineXInRoot: Float
): BottomDockInsertionPreview? {
    if (dockBoundsInRoot.width <= 0f || dockBoundsInRoot.height <= 0f ||
        !dockBoundsInRoot.contains(pointerInRoot)
    ) return null

    if (orderedBounds.isEmpty()) {
        val verticalInset = dockBoundsInRoot.height * 0.12f
        return BottomDockInsertionPreview(
            targetIndex = 0,
            lineXInRoot = emptyLineXInRoot,
            lineTopInRoot = dockBoundsInRoot.top + verticalInset,
            lineBottomInRoot = dockBoundsInRoot.bottom - verticalInset
        )
    }

    val insertionIndex = orderedBounds.indexOfFirst { pointerInRoot.x < it.center.x }
        .let { if (it < 0) orderedBounds.size else it }
    val lineX = when (insertionIndex) {
        0 -> orderedBounds.first().left
        orderedBounds.size -> orderedBounds.last().right
        else -> (orderedBounds[insertionIndex - 1].right + orderedBounds[insertionIndex].left) / 2f
    }
    return BottomDockInsertionPreview(
        targetIndex = insertionIndex,
        lineXInRoot = lineX,
        lineTopInRoot = orderedBounds.minOf { it.top },
        lineBottomInRoot = orderedBounds.maxOf { it.bottom }
    )
}

@Composable
private fun PagedBottomDock(
    entries: List<BottomDockEntry>,
    capacity: Int,
    pageCount: Int,
    loopEnabled: Boolean,
    selectedPage: Int,
    showAddButton: Boolean,
    alignment: BottomDockAlignment,
    appsByComponent: Map<String, AppItem>,
    backgroundColorHex: String,
    backgroundOpacityPercent: Int,
    textColorHex: String,
    shelfHeightDp: Int,
    iconVerticalOffsetDp: Int,
    shelfVerticalOffsetDp: Int,
    showLabels: Boolean,
    shelfShape: DockShelfShape,
    shelfColorHex: String,
    shelfOpacityPercent: Int,
    textShadowSettings: TextShadowSettings,
    reorderLineColor: String,
    reorderLineThicknessDp: Int,
    folderCreationDelayMillis: Int,
    externalDragPointerInRoot: Offset?,
    editingLocked: Boolean,
    onLockedDrag: () -> Unit,
    onPageChanged: (Int) -> Unit,
    onLaunch: (BottomDockEntry) -> Unit,
    onAddApp: () -> Unit,
    onRemove: (String) -> Unit,
    onReorder: (String, Int) -> Unit,
    onCreateFolder: (String, String) -> Unit,
    onAddToFolder: (String, String) -> Unit,
    onMoveToPage: (String, Int) -> Unit,
    onMoveToWorkspace: (BottomDockEntry, Float, Float) -> Unit,
    onContextMenu: (BottomDockEntry) -> Unit,
    onFolderFanStart: (BottomDockEntry, Offset) -> Unit,
    onFolderFanDrag: (BottomDockEntry, Offset) -> Unit,
    onFolderFanEnd: (BottomDockEntry) -> Unit,
    onExternalInsertionTargetChange: (Int?) -> Unit,
    onExternalHoverTargetChange: (String?) -> Unit,
    onInteractionActiveChange: (Boolean) -> Unit,
    isOverDeleteTarget: (Offset) -> Boolean,
    onDeleteDragStateChange: (Boolean, Boolean) -> Unit
) {
    val safePages = pageCount.coerceIn(1, 5)
    val centerPage = safePages / 2
    val virtualCount = virtualPageCount(safePages, loopEnabled)
    val pagerState = key(safePages, loopEnabled) {
        rememberPagerState(
            initialPage = initialVirtualPage(safePages, selectedPage.coerceIn(0, safePages - 1), loopEnabled),
            pageCount = { virtualCount }
        )
    }
    val density = LocalDensity.current
    val configuration = LocalConfiguration.current
    val scope = rememberCoroutineScope()
    val screenWidthPx = with(density) { configuration.screenWidthDp.dp.toPx() }
    val screenHeightPx = with(density) { configuration.screenHeightDp.dp.toPx() }
    val shortcutWidthPx = with(density) { 78.dp.toPx() }
    val dockCellWidthDp = ((configuration.screenWidthDp - 18) / capacity.coerceIn(1, 10)).coerceIn(34, 66)
    val workspaceTopPx = with(density) { 70.dp.toPx() }
    val workspaceVerticalReservedPx = with(density) { 190.dp.toPx() }
    val textColor = parseAppFrameColor(textColorHex)
    var draggedEntry by remember { mutableStateOf<BottomDockEntry?>(null) }
    var dragStartRoot by remember { mutableStateOf(Offset.Zero) }
    var dragOffset by remember { mutableStateOf(Offset.Zero) }
    var dockRootOffset by remember { mutableStateOf(Offset.Zero) }
    var pendingRemoval by remember { mutableStateOf<BottomDockEntry?>(null) }
    val entryBounds = remember { mutableMapOf<String, Rect>() }
    var folderHoverTargetId by remember { mutableStateOf<String?>(null) }
    var folderHoverAnchor by remember { mutableStateOf(Offset.Zero) }
    var folderDropReadyTargetId by remember { mutableStateOf<String?>(null) }
    var folderHoverSerial by remember { mutableIntStateOf(0) }
    var insertionPreview by remember { mutableStateOf<BottomDockInsertionPreview?>(null) }
    var folderFanActive by remember { mutableStateOf(false) }
    fun draggedCenterInRoot(): Offset = dragStartRoot + dragOffset + Offset(
        with(density) { 33.dp.toPx() }, with(density) { 34.dp.toPx() }
    )

    LaunchedEffect(pagerState.currentPage, safePages) {
        onPageChanged(logicalPage(pagerState.currentPage, safePages))
    }
    LaunchedEffect(pagerState.isScrollInProgress, loopEnabled, safePages) {
        if (!pagerState.isScrollInProgress && loopEnabled && safePages > 1) {
            val page = logicalPage(pagerState.currentPage, safePages)
            val edge = safePages * 3
            if (pagerState.currentPage < edge || pagerState.currentPage > virtualCount - edge) {
                pagerState.scrollToPage(initialVirtualPage(safePages, page, true))
            }
        }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .height(104.dp)
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .onGloballyPositioned { dockRootOffset = it.positionInRoot() }
    ) {
        val pagerModifier = Modifier
            .align(Alignment.BottomCenter)
            .fillMaxWidth()
            .height(92.dp)
            .zIndex(2f)

        val pagerContent: @Composable () -> Unit = {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
                userScrollEnabled = draggedEntry == null && !folderFanActive,
                beyondViewportPageCount = 1,
                key = { it }
            ) { virtualPage ->
                val logical = if (loopEnabled) logicalPage(virtualPage, safePages) else virtualPage
                val dataPage = Math.floorMod(logical - centerPage, safePages)
                val pageEntries = entries.filter { it.page == dataPage }.take(capacity.coerceIn(1, 10))
                val isCurrentPage = virtualPage == pagerState.currentPage
                var pageRootOffset by remember(virtualPage) { mutableStateOf(Offset.Zero) }
                var pageBoundsInRoot by remember(virtualPage) { mutableStateOf(Rect.Zero) }
                fun insertionPreviewAt(
                    pointerInRoot: Offset,
                    draggedId: String
                ): BottomDockInsertionPreview? {
                    val edge = with(density) { 18.dp.toPx() }
                    if (pointerInRoot.x <= edge || pointerInRoot.x >= screenWidthPx - edge) return null
                    val draggedIndex = pageEntries.indexOfFirst { it.id == draggedId }
                    val orderedBounds = pageEntries.map { entryBounds[it.id] ?: return null }
                    return calculateBottomDockInsertionPreview(
                        pointerInRoot = pointerInRoot,
                        draggedIndex = draggedIndex,
                        orderedBounds = orderedBounds
                    )
                }
                val externalInsertionPreview = if (
                    isCurrentPage && draggedEntry == null && !editingLocked
                ) {
                    externalDragPointerInRoot?.let { pointer ->
                        val orderedBounds = pageEntries.mapNotNull { entryBounds[it.id] }
                        if (orderedBounds.size != pageEntries.size) null else {
                            val emptyLineX = when (alignment) {
                                BottomDockAlignment.LEFT -> pageBoundsInRoot.left + with(density) { 8.dp.toPx() }
                                BottomDockAlignment.CENTER -> pageBoundsInRoot.center.x
                                BottomDockAlignment.RIGHT -> pageBoundsInRoot.right - with(density) { 8.dp.toPx() }
                            }
                            calculateExternalBottomDockInsertionPreview(
                                pointerInRoot = pointer,
                                orderedBounds = orderedBounds,
                                dockBoundsInRoot = pageBoundsInRoot,
                                emptyLineXInRoot = emptyLineX
                            )
                        }
                    }
                } else null
                val externalHoverTarget = if (isCurrentPage && draggedEntry == null && !editingLocked) {
                    externalDragPointerInRoot?.let { pointer ->
                        pageEntries.firstOrNull { entry -> entryBounds[entry.id]?.contains(pointer) == true }?.id
                    }
                } else null
                LaunchedEffect(isCurrentPage, externalInsertionPreview?.targetIndex) {
                    if (isCurrentPage) {
                        onExternalInsertionTargetChange(externalInsertionPreview?.targetIndex)
                    }
                }
                LaunchedEffect(isCurrentPage, externalHoverTarget) {
                    if (isCurrentPage) onExternalHoverTargetChange(externalHoverTarget)
                }
                Box(
                    Modifier.fillMaxSize().onGloballyPositioned {
                        pageRootOffset = it.positionInRoot()
                        pageBoundsInRoot = it.boundsInRoot()
                    }
                ) {
                    DockPageBackground(
                        shelfHeightDp = shelfHeightDp,
                        shelfVerticalOffsetDp = shelfVerticalOffsetDp,
                        shelfShape = shelfShape,
                        shelfColorHex = shelfColorHex,
                        shelfOpacityPercent = shelfOpacityPercent,
                        backgroundColorHex = backgroundColorHex,
                        backgroundOpacityPercent = backgroundOpacityPercent,
                        modifier = Modifier.fillMaxSize()
                    )
                if (pageEntries.isEmpty()) {
                    if (showAddButton && !editingLocked) {
                        Box(
                            Modifier.fillMaxSize().offset(y = iconVerticalOffsetDp.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            BottomDockAddButton(textColor = textColor, onClick = onAddApp)
                        }
                    }
                } else {
                    val arrangement = when (alignment) {
                        BottomDockAlignment.LEFT -> Arrangement.spacedBy(7.dp, Alignment.Start)
                        BottomDockAlignment.CENTER -> Arrangement.SpaceEvenly
                        BottomDockAlignment.RIGHT -> Arrangement.spacedBy(7.dp, Alignment.End)
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .offset(y = iconVerticalOffsetDp.dp)
                            .padding(horizontal = 6.dp, vertical = 4.dp),
                        horizontalArrangement = arrangement,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        pageEntries.forEach { entry ->
                            key(entry.id) {
                            BottomDockEntryCell(
                                entry = entry,
                                app = entry.componentName?.let { appsByComponent[it] },
                                appsByComponent = appsByComponent,
                                textColor = textColor,
                                textShadowSettings = textShadowSettings,
                                showLabel = showLabels,
                                cellWidthDp = dockCellWidthDp,
                                hidden = draggedEntry?.id == entry.id,
                                folderPreviewSource = draggedEntry?.takeIf {
                                    folderDropReadyTargetId == entry.id
                                },
                                dragEnabled = !editingLocked,
                                onLockedDrag = onLockedDrag,
                                onBoundsChanged = { entryBounds[entry.id] = it },
                                onClick = { onLaunch(entry) },
                                onFanStart = { origin ->
                                    folderFanActive = true
                                    onInteractionActiveChange(true)
                                    onFolderFanStart(entry, origin)
                                },
                                onFanDrag = { amount -> onFolderFanDrag(entry, amount) },
                                onFanEnd = {
                                    onFolderFanEnd(entry)
                                    folderFanActive = false
                                    onInteractionActiveChange(false)
                                },
                                onDragStart = { root ->
                                    onInteractionActiveChange(true)
                                    onDeleteDragStateChange(true, false)
                                    draggedEntry = entry
                                    dragStartRoot = root
                                    dragOffset = Offset.Zero
                                    folderHoverTargetId = null
                                    folderHoverAnchor = Offset.Zero
                                    folderDropReadyTargetId = null
                                    folderHoverSerial += 1
                                    val startCenter = entryBounds[entry.id]?.center ?: root + Offset(
                                        with(density) { 33.dp.toPx() },
                                        with(density) { 34.dp.toPx() }
                                    )
                                    insertionPreview = insertionPreviewAt(startCenter, entry.id)
                                },
                                onDrag = { amount ->
                                    dragOffset += amount
                                    val movingCenter = draggedCenterInRoot()
                                    val overDelete = isOverDeleteTarget(movingCenter)
                                    onDeleteDragStateChange(true, overDelete)
                                    insertionPreview = if (overDelete) null else insertionPreviewAt(movingCenter, entry.id)
                                    val hoverId = pageEntries.firstOrNull { candidate ->
                                        candidate.id != entry.id &&
                                            entryBounds[candidate.id]?.contains(movingCenter) == true
                                    }?.id
                                    val folderHoldSlop = with(density) { 8.dp.toPx() }
                                    val movedOutsideHoldArea = hoverId != null &&
                                        (movingCenter - folderHoverAnchor).getDistance() > folderHoldSlop
                                    if (hoverId != folderHoverTargetId || movedOutsideHoldArea) {
                                        folderHoverTargetId = hoverId
                                        folderHoverAnchor = movingCenter
                                        folderDropReadyTargetId = null
                                        folderHoverSerial += 1
                                        val serial = folderHoverSerial
                                        val hoverTarget = pageEntries.firstOrNull { it.id == hoverId }
                                        val canCreateFolder = entry.componentName != null && entry.intentAction == null &&
                                            (hoverTarget?.isFolder == true ||
                                                (hoverTarget?.componentName != null && hoverTarget.intentAction == null))
                                        if (canCreateFolder && hoverId != null) {
                                            scope.launch {
                                                delay(folderCreationDelayMillis.coerceIn(300, 2000).toLong())
                                                if (serial == folderHoverSerial &&
                                                    draggedEntry?.id == entry.id &&
                                                    folderHoverTargetId == hoverId
                                                ) {
                                                    folderDropReadyTargetId = hoverId
                                                }
                                            }
                                        }
                                    }
                                },
                                onDragCancel = {
                                    onInteractionActiveChange(false)
                                    onDeleteDragStateChange(false, false)
                                    draggedEntry = null
                                    folderHoverTargetId = null
                                    folderHoverAnchor = Offset.Zero
                                    folderDropReadyTargetId = null
                                    folderHoverSerial += 1
                                    insertionPreview = null
                                    dragOffset = Offset.Zero
                                },
                                onDragEnd = {
                                    val moved = draggedEntry
                                    val finalInsertionPreview = insertionPreview
                                    if (moved != null) {
                                        val finalRoot = dragStartRoot + dragOffset
                                        val finalCenter = finalRoot + Offset(
                                            with(density) { 33.dp.toPx() },
                                            with(density) { 34.dp.toPx() }
                                        )
                                        val upwardThreshold = with(density) { 82.dp.toPx() }
                                        val stationaryThreshold = with(density) { 12.dp.toPx() }
                                        val target = pageEntries.firstOrNull { candidate ->
                                            candidate.id != moved.id &&
                                                entryBounds[candidate.id]?.contains(finalCenter) == true
                                        }
                                        val folderDropReady = target != null &&
                                            target.id == folderHoverTargetId &&
                                            target.id == folderDropReadyTargetId
                                        if (isOverDeleteTarget(finalCenter)) {
                                            // Removes the dock entry, never uninstalls its app.
                                            // Preserve confirmation for system/preset dock actions.
                                            if (isSystemDockAction(moved.intentAction)) pendingRemoval = moved
                                            else onRemove(moved.id)
                                        } else if (dragOffset.getDistance() < stationaryThreshold) {
                                            onContextMenu(moved)
                                        } else if (isSystemDockAction(moved.intentAction) &&
                                            (dragOffset.y < -upwardThreshold || dragOffset.getDistance() < stationaryThreshold)
                                        ) {
                                            pendingRemoval = moved
                                        } else if (dragOffset.y < -upwardThreshold) {
                                            val x = ((finalRoot.x - shortcutWidthPx / 2f) /
                                                (screenWidthPx - shortcutWidthPx).coerceAtLeast(1f)).safeWorkspaceFraction()
                                            val y = ((finalRoot.y - workspaceTopPx) /
                                                (screenHeightPx - workspaceVerticalReservedPx).coerceAtLeast(1f)).safeWorkspaceFraction(0.72f)
                                            onMoveToWorkspace(moved, x, y)
                                        } else if (folderDropReady && moved.componentName != null && moved.intentAction == null &&
                                            target.isFolder
                                        ) {
                                            onAddToFolder(moved.id, target.id)
                                        } else if (folderDropReady && moved.componentName != null && moved.intentAction == null &&
                                            target.componentName != null && target.intentAction == null
                                        ) {
                                            onCreateFolder(moved.id, target.id)
                                        } else if (finalInsertionPreview != null) {
                                            onReorder(moved.id, finalInsertionPreview.targetIndex)
                                        } else {
                                            val edge = with(density) { 18.dp.toPx() }
                                            val direction = when {
                                                finalCenter.x <= edge -> -1
                                                finalCenter.x >= screenWidthPx - edge -> 1
                                                else -> 0
                                            }
                                            if (direction != 0) {
                                                val nextPage = moved.page + direction
                                                if (loopEnabled || nextPage in 0 until safePages) {
                                                    onMoveToPage(moved.id, Math.floorMod(nextPage, safePages))
                                                }
                                            }
                                        }
                                    }
                                    onInteractionActiveChange(false)
                                    onDeleteDragStateChange(false, false)
                                    draggedEntry = null
                                    folderHoverTargetId = null
                                    folderHoverAnchor = Offset.Zero
                                    folderDropReadyTargetId = null
                                    folderHoverSerial += 1
                                    insertionPreview = null
                                    dragOffset = Offset.Zero
                                },
                                modifier = Modifier.graphicsLayer {
                                    val readyForFolderDrop = folderDropReadyTargetId == entry.id ||
                                        externalHoverTarget == entry.id
                                    scaleX = if (readyForFolderDrop) 1.10f else 1f
                                    scaleY = if (readyForFolderDrop) 1.10f else 1f
                                }
                            )
                            }
                        }
                    }
                }
                val visibleInsertionPreview = when {
                    folderDropReadyTargetId != null -> null
                    draggedEntry?.page == dataPage && pageEntries.any { it.id == draggedEntry?.id } -> insertionPreview
                    externalInsertionPreview != null && externalHoverTarget == null -> externalInsertionPreview
                    else -> null
                }
                visibleInsertionPreview
                    ?.takeIf {
                        pageRootOffset != Offset.Zero &&
                            (draggedEntry != null || externalDragPointerInRoot != null)
                    }
                    ?.let { preview ->
                        val thicknessDp = reorderLineThicknessDp.coerceIn(1, 12)
                        val thicknessPx = with(density) { thicknessDp.dp.toPx() }
                        val lineHeightDp = with(density) {
                            (preview.lineBottomInRoot - preview.lineTopInRoot)
                                .coerceAtLeast(thicknessPx)
                                .toDp()
                        }
                        Box(
                            Modifier
                                .width(thicknessDp.dp)
                                .height(lineHeightDp)
                                .offset {
                                    IntOffset(
                                        (preview.lineXInRoot - pageRootOffset.x - thicknessPx / 2f).roundToInt(),
                                        (preview.lineTopInRoot - pageRootOffset.y).roundToInt()
                                    )
                                }
                                .zIndex(70f)
                                .background(
                                    parseAppFrameColor(reorderLineColor),
                                    RoundedCornerShape(50)
                                )
                        )
                    }
                }
            }
        }

        // Draw the shelf shadows in the unclipped dock host, not inside HorizontalPager's
        // horizontally clipped pages. The surface itself remains in each moving page.
        if (shelfShape != DockShelfShape.NONE && shelfHeightDp > 0 && shelfOpacityPercent > 0) {
            DockPageShelfShadows(
                shelfHeightDp = shelfHeightDp,
                shelfVerticalOffsetDp = shelfVerticalOffsetDp,
                shelfShape = shelfShape,
                shelfOpacityPercent = shelfOpacityPercent,
                pageWidthDp = maxWidth,
                pagerState = pagerState,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .zIndex(1f)
            )
        }
        Box(pagerModifier) { pagerContent() }

        val dragged = draggedEntry
        if (dragged != null) {
            BottomDockEntryCell(
                entry = dragged,
                app = dragged.componentName?.let { appsByComponent[it] },
                appsByComponent = appsByComponent,
                textColor = textColor,
                textShadowSettings = textShadowSettings,
                showLabel = showLabels,
                cellWidthDp = dockCellWidthDp,
                hidden = false,
                dragEnabled = false,
                onLockedDrag = {},
                onBoundsChanged = {},
                onClick = {},
                onFanStart = {},
                onFanDrag = {},
                onFanEnd = {},
                onDragStart = { _ -> }, onDrag = { _ -> }, onDragEnd = {}, onDragCancel = {},
                modifier = Modifier
                    .offset {
                        IntOffset(
                            (dragStartRoot.x - dockRootOffset.x + dragOffset.x).roundToInt(),
                            (dragStartRoot.y - dockRootOffset.y + dragOffset.y).roundToInt()
                        )
                    }
                    .zIndex(80f)
                    .graphicsLayer { scaleX = 1.10f; scaleY = 1.10f; shadowElevation = 18f }
            )
        }
    }

    pendingRemoval?.let { entry ->
        AlertDialog(
            onDismissRequest = { pendingRemoval = null },
            title = { Text(entry.label) },
            text = { Text(l10n(R.string.ui_delete_the_preset_icon_from_the_bottom_dock_you)) },
            confirmButton = {
                TextButton(onClick = {
                    pendingRemoval = null
                    onRemove(entry.id)
                }) { Text(l10n(R.string.ui_delete), color = Color(0xFFFF7A83)) }
            },
            dismissButton = { TextButton(onClick = { pendingRemoval = null }) { Text(l10n(R.string.ui_cancel)) } }
        )
    }
}

@Composable
private fun BottomDockEntryCell(
    entry: BottomDockEntry,
    app: AppItem?,
    appsByComponent: Map<String, AppItem>,
    textColor: Color,
    textShadowSettings: TextShadowSettings,
    showLabel: Boolean,
    cellWidthDp: Int = 66,
    hidden: Boolean,
    folderPreviewSource: BottomDockEntry? = null,
    dragEnabled: Boolean,
    onLockedDrag: () -> Unit,
    onBoundsChanged: (Rect) -> Unit,
    onClick: () -> Unit,
    onFanStart: (Offset) -> Unit,
    onFanDrag: (Offset) -> Unit,
    onFanEnd: () -> Unit,
    onDragStart: (Offset) -> Unit,
    onDrag: (Offset) -> Unit,
    onDragEnd: () -> Unit,
    onDragCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val density = LocalDensity.current.density
    var rootOffset by remember(entry.id) { mutableStateOf(Offset.Zero) }
    var activationSerial by remember(entry.id) { mutableIntStateOf(0) }
    val activationInteraction = remember(entry.id) { MutableInteractionSource() }
    val activationPressed by activationInteraction.collectIsPressedAsState()
    val customFolderIcon = if (entry.isFolder) rememberLauncherCustomIcon(
        entry.customIconUri,
        entry.customIconPackPackage,
        entry.customIconPackDrawable
    ) else null
    val folderPreviewComponents = folderPreviewSource?.let { source ->
        buildList {
            if (entry.isFolder) addAll(entry.componentNames)
            else entry.componentName?.let { add(it) }
            source.componentName?.let { add(it) }
        }.distinct().take(4)
    }
    LaunchedEffect(activationPressed) {
        if (activationPressed) activationSerial += 1
    }
    Column(
        modifier = modifier
            .width(cellWidthDp.dp)
            .iconActivationEffect(
                activationSerial,
                RoundedCornerShape(20.dp),
                excludedEffect = AppLaunchAnimationEffect.ICON_THEN_WIREFRAME
            )
            .onGloballyPositioned {
                rootOffset = it.positionInRoot()
                onBoundsChanged(it.boundsInRoot())
            }
            .alpha(if (hidden) 0f else 1f)
            .pointerInput(entry.id, dragEnabled) {
                if (!dragEnabled) {
                    detectDragGesturesAfterLongPress(
                        onDragStart = { onLockedDrag() },
                        onDrag = { change, _ -> change.consume() }
                    )
                    return@pointerInput
                }
                if (!entry.isFolder) {
                    detectDragGesturesAfterLongPress(
                        onDragStart = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onDragStart(rootOffset)
                        },
                        onDrag = { change, amount -> change.consume(); onDrag(amount) },
                        onDragEnd = onDragEnd,
                        onDragCancel = onDragCancel
                    )
                    return@pointerInput
                }
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    val start = down.position
                    var fanStarted = false
                    var initialFanDelta = Offset.Zero
                    val completedBeforeLongPress = withTimeoutOrNull(
                        viewConfiguration.longPressTimeoutMillis
                    ) {
                        while (true) {
                            val event = awaitPointerEvent()
                            val change = event.changes.firstOrNull { it.id == down.id }
                                ?: return@withTimeoutOrNull true
                            if (!change.pressed) return@withTimeoutOrNull true
                            val totalDelta = change.position - start
                            if (totalDelta.getDistance() > viewConfiguration.touchSlop) {
                                val vertical = abs(totalDelta.x) <= abs(totalDelta.y) * 0.5773503f
                                if (vertical) {
                                    fanStarted = true
                                    initialFanDelta = totalDelta
                                    change.consume()
                                }
                                return@withTimeoutOrNull true
                            }
                        }
                    }
                    when {
                        completedBeforeLongPress == null && dragEnabled -> {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onDragStart(rootOffset)
                            var endedNormally = false
                            try {
                                while (true) {
                                    val event = awaitPointerEvent()
                                    val change = event.changes.firstOrNull { it.id == down.id } ?: break
                                    if (!change.pressed) {
                                        endedNormally = true
                                        break
                                    }
                                    val amount = change.position - change.previousPosition
                                    if (amount != Offset.Zero) {
                                        change.consume()
                                        onDrag(amount)
                                    }
                                }
                            } finally {
                                if (endedNormally) onDragEnd() else onDragCancel()
                            }
                        }
                        fanStarted -> {
                            onFanStart(
                                rootOffset + Offset(33f * density, 22f * density)
                            )
                            if (initialFanDelta != Offset.Zero) onFanDrag(initialFanDelta)
                            try {
                                while (true) {
                                    val event = awaitPointerEvent()
                                    val change = event.changes.firstOrNull { it.id == down.id } ?: break
                                    if (!change.pressed) break
                                    val amount = change.position - change.previousPosition
                                    if (amount != Offset.Zero) {
                                        change.consume()
                                        onFanDrag(amount)
                                    }
                                }
                            } finally {
                                onFanEnd()
                            }
                        }
                    }
                }
            }
            .clickable(
                enabled = !hidden,
                interactionSource = activationInteraction,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 3.dp, vertical = 5.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.size(42.dp)
                .iconActivationEffect(
                    activationSerial,
                    RoundedCornerShape(14.dp),
                    onlyEffect = AppLaunchAnimationEffect.ICON_THEN_WIREFRAME
                ),
            contentAlignment = Alignment.Center
        ) {
        if (folderPreviewComponents != null) {
            Card(
                modifier = Modifier.fillMaxSize().alpha(0.62f),
                shape = folderShape(if (entry.isFolder) entry.folderShape else FolderShape.DEFAULT),
                colors = CardDefaults.cardColors(
                    containerColor = parseAppFrameColor(
                        if (entry.isFolder) entry.folderColorHex else "#4F5872"
                    ).copy(alpha = 0.72f)
                )
            ) {
                Column(
                    Modifier.fillMaxSize().padding(4.dp),
                    verticalArrangement = Arrangement.SpaceEvenly
                ) {
                    folderPreviewComponents.chunked(2).forEach { rowComponents ->
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                            rowComponents.forEach { component ->
                                val folderApp = appsByComponent[component]
                                AppIconImage(
                                    folderApp?.label ?: l10n(R.string.ui_application),
                                    folderApp?.icon,
                                    Modifier.size(15.dp).alpha(0.78f)
                                )
                            }
                        }
                    }
                }
            }
        } else if (entry.isFolder && customFolderIcon != null) {
            AppIconImage(entry.label, customFolderIcon, Modifier.fillMaxSize().clip(folderShape(entry.folderShape)))
        } else if (entry.isFolder) {
            Card(
                modifier = Modifier.fillMaxSize(),
                shape = folderShape(entry.folderShape),
                colors = CardDefaults.cardColors(
                    containerColor = parseAppFrameColor(entry.folderColorHex).copy(
                        alpha = if (entry.folderShape == FolderShape.NONE) 0f
                        else entry.folderOpacityPercent.coerceIn(0, 100) / 100f
                    )
                )
            ) {
                Column(Modifier.fillMaxSize().padding(4.dp), verticalArrangement = Arrangement.SpaceEvenly) {
                    entry.componentNames.take(4).chunked(2).forEach { rowComponents ->
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                            rowComponents.forEach { component ->
                                val folderApp = appsByComponent[component]
                                AppIconImage(
                                    folderApp?.label ?: l10n(R.string.ui_application),
                                    folderApp?.icon,
                                    Modifier.size(15.dp)
                                )
                            }
                        }
                    }
                }
            }
        } else if (app != null) {
            AppIconImage(app.label, app.icon, Modifier.fillMaxSize())
        } else {
            val icon = dockActionIconRes(canonicalLauncherAction(entry.intentAction))
            Image(
                painterResource(icon), entry.label, Modifier.size(36.dp),
                colorFilter = ColorFilter.tint(textColor)
            )
        }
        }
        Spacer(Modifier.height(2.dp))
        if (showLabel) {
            Text(
                if (folderPreviewComponents != null) l10n(R.string.ui_folder)
                else if (entry.isFolder) entry.label else app?.label ?: entry.label,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                fontSize = 9.sp,
                color = textColor.copy(alpha = 0.92f),
                style = MaterialTheme.typography.labelSmall.copy(
                    shadow = textShadow(textShadowSettings, density)
                )
            )
        } else {
            Spacer(Modifier.height(11.dp))
        }
    }
}

@Composable
private fun DockBar(
    items: List<DockItem>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 12.dp, vertical = 10.dp),
        shape = RoundedCornerShape(30.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xD91B1B23)),
        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Bottom
        ) {
            items.forEach { item -> DockButton(item = item) }
        }
    }
}

@Composable
private fun DockButton(item: DockItem) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = when {
            pressed -> 0.88f
            item.prominent -> 1.08f
            else -> 1f
        },
        animationSpec = spring(dampingRatio = 0.58f, stiffness = 520f),
        label = "dock_scale_${item.label}"
    )

    val backgroundAlpha = when {
        item.selected -> 0.30f
        item.prominent -> 0.16f
        pressed -> 0.18f
        else -> 0.06f
    }

    Column(
        modifier = Modifier
            .width(64.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(RoundedCornerShape(22.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = item.onClick
            )
            .background(MaterialTheme.colorScheme.primary.copy(alpha = backgroundAlpha))
            .padding(horizontal = 4.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(item.iconRes),
            contentDescription = item.label,
            modifier = Modifier.size(if (item.prominent) 31.dp else 27.dp),
            colorFilter = ColorFilter.tint(
                if (item.selected || item.prominent) {
                    MaterialTheme.colorScheme.primary
                } else {
                    Color.White.copy(alpha = 0.90f)
                }
            )
        )

        Spacer(modifier = Modifier.height(5.dp))

        Text(
            text = item.label,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = 0.82f)
        )
    }
}

@Composable
private fun AppIconCell(
    app: AppItem,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    dragEnabled: Boolean = false,
    longPressMillis: Int = 430,
    onDragStart: (Offset) -> Unit = {},
    onDragMove: (Offset) -> Unit = {},
    onDragEnd: (Offset) -> Unit = {},
    itemWidthDp: Int = 72,
    itemHeightDp: Int = 72,
    frameShape: AppItemFrameShape = AppItemFrameShape.ROUNDED,
    frameColorHex: String = "#242936",
    frameOpacityPercent: Int = 36,
    textColorHex: String = "#FFFFFF",
    textShadowSettings: TextShadowSettings = TextShadowSettings()
) {
    val haptic = LocalHapticFeedback.current
    val density = LocalDensity.current.density
    val context = LocalContext.current
    val openedApps = LauncherStorage.loadOpenedApps(context)
    val newAppCutoffMillis = LauncherStorage.ensureNewAppCutoff(context)
    val isNewApp = app.componentName.flattenToString() !in openedApps &&
        app.installTimeMillis > 0L &&
        app.installTimeMillis > newAppCutoffMillis &&
        (app.lastUsedMillis <= 0L || app.lastUsedMillis < app.installTimeMillis)
    var launchSerial by remember(app.componentName) { mutableIntStateOf(0) }
    var dragSerial by remember(app.componentName) { mutableIntStateOf(0) }
    var pressed by remember(app.componentName) { mutableStateOf(false) }
    var rootOffset by remember(app.componentName) { mutableStateOf(Offset.Zero) }
    var layoutCoordinates by remember(app.componentName) {
        mutableStateOf<androidx.compose.ui.layout.LayoutCoordinates?>(null)
    }
    val currentOnClick by rememberUpdatedState(onClick)
    val currentOnLongClick by rememberUpdatedState(onLongClick)
    val currentOnDragStart by rememberUpdatedState(onDragStart)
    val currentOnDragMove by rememberUpdatedState(onDragMove)
    val currentOnDragEnd by rememberUpdatedState(onDragEnd)
    val currentEnabled by rememberUpdatedState(enabled)
    val scale by animateFloatAsState(if (pressed) 0.90f else 1f, spring(dampingRatio = 0.55f, stiffness = 520f), label = "app_cell_scale")
    val shape = appItemFrameShape(frameShape)
    val labelSpaceDp = when {
        itemHeightDp >= 84 -> 34
        itemHeightDp >= 58 -> 24
        else -> 14
    }
    val iconSize = minOf(itemWidthDp - 8, itemHeightDp - labelSpaceDp - 6).coerceAtLeast(6)
    val compact = itemHeightDp < 64 || itemWidthDp < 56
    val textColor = parseAppFrameColor(textColorHex)
    val backgroundColor = parseAppFrameColor(frameColorHex).copy(
        alpha = if (frameShape == AppItemFrameShape.NONE) 0f else ((frameOpacityPercent / 100f) + if (pressed) 0.12f else 0f).coerceIn(0f, 1f)
    )
    Column(
        modifier = modifier.width(itemWidthDp.dp).height(itemHeightDp.dp)
            .onGloballyPositioned {
                layoutCoordinates = it
                rootOffset = it.positionInRoot()
            }
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .iconActivationEffect(
                launchSerial,
                shape,
                IconActivationKind.LAUNCH,
                excludedEffect = AppLaunchAnimationEffect.ICON_THEN_WIREFRAME
            )
            .iconActivationEffect(
                dragSerial,
                shape,
                IconActivationKind.DRAG,
                excludedEffect = AppLaunchAnimationEffect.ICON_THEN_WIREFRAME
            )
            .then(if (frameShape == AppItemFrameShape.NONE) Modifier else Modifier.background(backgroundColor, shape))
            .pointerInput(app.componentName, dragEnabled, longPressMillis) {
                if (!currentEnabled) return@pointerInput
                if (!dragEnabled) {
                    detectTapGestures(
                        onPress = { launchSerial += 1; pressed = true; tryAwaitRelease(); pressed = false },
                        onTap = { currentOnClick() },
                        onLongPress = { haptic.performHapticFeedback(HapticFeedbackType.LongPress); currentOnLongClick() }
                    )
                } else {
                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        var current = down
                        val start = down.position
                        val completedBeforeHold = withTimeoutOrNull(longPressMillis.coerceIn(100, 900).toLong()) {
                            while (true) {
                                val event = awaitPointerEvent()
                                current = event.changes.firstOrNull { it.id == down.id } ?: return@withTimeoutOrNull false
                                if (!current.pressed) return@withTimeoutOrNull true
                                if ((current.position - start).getDistance() > viewConfiguration.touchSlop) return@withTimeoutOrNull false
                            }
                        }
                        if (completedBeforeHold == null && current.pressed) {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            dragSerial += 1
                            pressed = true
                            var absolutePosition = layoutCoordinates
                                ?.takeIf { it.isAttached }
                                ?.localToRoot(current.position)
                                ?: (rootOffset + current.position)
                            currentOnDragStart(absolutePosition)
                            try {
                                while (true) {
                                    val event = awaitPointerEvent()
                                    current = event.changes.firstOrNull { it.id == down.id } ?: break
                                    val coordinates = layoutCoordinates
                                    val rootPosition = if (coordinates != null && coordinates.isAttached) {
                                        // Use the finger's absolute position in the launcher root on every
                                        // event. The gesture remains owned by the source cell even when the
                                        // pager moves that cell to another page, so accumulating local/root
                                        // deltas can freeze or distort one axis after an automatic page
                                        // switch. Re-resolving the current pointer through the cell's current
                                        // LayoutCoordinates makes both X and Y follow the physical finger.
                                        coordinates.localToRoot(current.position)
                                    } else {
                                        absolutePosition + (current.position - current.previousPosition)
                                    }
                                    if (rootPosition.x.isFinite() && rootPosition.y.isFinite()) {
                                        absolutePosition = rootPosition
                                    }
                                    currentOnDragMove(absolutePosition)
                                    current.consume()
                                    if (!current.pressed) break
                                }
                            } finally {
                                currentOnDragEnd(absolutePosition)
                                pressed = false
                            }
                        } else if (completedBeforeHold == true) {
                            launchSerial += 1
                            currentOnClick()
                        }
                    }
                }
            }
            .padding(horizontal = if (itemWidthDp < 24) 0.dp else if (compact) 2.dp else 5.dp, vertical = if (itemHeightDp < 24) 0.dp else if (compact) 2.dp else 5.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            Modifier.size(iconSize.dp)
                .iconActivationEffect(
                    launchSerial,
                    RoundedCornerShape(14.dp),
                    IconActivationKind.LAUNCH,
                    onlyEffect = AppLaunchAnimationEffect.ICON_THEN_WIREFRAME
                )
                .iconActivationEffect(
                    dragSerial,
                    RoundedCornerShape(14.dp),
                    IconActivationKind.DRAG,
                    onlyEffect = AppLaunchAnimationEffect.ICON_THEN_WIREFRAME
                )
        ) {
            AppIconImage(app.label, app.icon, Modifier.fillMaxSize())
            if (isNewApp) NewAppEmblem(iconSizeDp = iconSize)
        }
        Spacer(Modifier.height(if (compact) 1.dp else 4.dp))
        Text(
            text = app.label,
            maxLines = if (itemHeightDp >= 74) 2 else 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            fontSize = (itemHeightDp / 8f).coerceIn(6f, 12f).sp,
            lineHeight = (itemHeightDp / 7f).coerceIn(7f, 14f).sp,
            color = textColor.copy(alpha = 0.94f),
            style = MaterialTheme.typography.labelSmall.copy(
                shadow = textShadow(textShadowSettings, density)
            )
        )
    }
}

@Composable
private fun NewAppEmblem(iconSizeDp: Int) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val shape = LauncherStorage.loadNewAppEmblemShape(context)
    if (shape == NewAppEmblemShape.NONE) return
    val percent = LauncherStorage.loadNewAppEmblemSize(context).coerceIn(4, 25)
    val sizeDp = (iconSizeDp * percent / 100f).coerceAtLeast(3f)
    val xPercent = LauncherStorage.loadNewAppEmblemX(context) / 100f
    val yPercent = LauncherStorage.loadNewAppEmblemY(context) / 100f
    val colorA = parseAppFrameColor(LauncherStorage.loadNewAppEmblemColor(context))
    val colorB = parseAppFrameColor(LauncherStorage.loadNewAppEmblemColorB(context))
    val gradient = LauncherStorage.loadNewAppEmblemGradient(context)
    val pulse = rememberInfiniteTransition(label = "new_app_fire").animateFloat(
        initialValue = 0.82f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(tween(420, easing = LinearEasing), RepeatMode.Reverse),
        label = "new_app_fire_pulse"
    ).value
    val xPx = with(density) { ((iconSizeDp - sizeDp) * xPercent).dp.roundToPx() }
    val yPx = with(density) { ((iconSizeDp - sizeDp) * yPercent).dp.roundToPx() }
    val customImage = rememberLauncherCustomIcon(
        LauncherStorage.loadNewAppEmblemImageUri(context),
        LauncherStorage.loadNewAppEmblemPackPackage(context),
        LauncherStorage.loadNewAppEmblemPackDrawable(context)
    )
    if (customImage != null) {
        Image(
            bitmap = customImage,
            contentDescription = l10n(R.string.ui_new_app_emblem),
            modifier = Modifier.size(sizeDp.dp).offset { IntOffset(xPx, yPx) }
        )
        return
    }
    Canvas(
        Modifier.size(sizeDp.dp).offset { IntOffset(xPx, yPx) }
            .graphicsLayer {
                if (shape == NewAppEmblemShape.FIRE) {
                    scaleX = pulse
                    scaleY = pulse
                }
            }
    ) {
        val brush = if (gradient) Brush.linearGradient(listOf(colorA, colorB)) else Brush.linearGradient(listOf(colorA, colorA))
        when (shape) {
            NewAppEmblemShape.FIRE -> {
                val path = Path().apply {
                    moveTo(size.width * 0.52f, 0f)
                    cubicTo(size.width * 0.92f, size.height * 0.38f, size.width, size.height * 0.72f, size.width * 0.5f, size.height)
                    cubicTo(0f, size.height * 0.72f, size.width * 0.15f, size.height * 0.38f, size.width * 0.42f, size.height * 0.18f)
                    close()
                }
                drawPath(path, brush)
            }
            NewAppEmblemShape.CIRCLE -> drawCircle(brush)
            NewAppEmblemShape.OVAL -> drawOval(brush)
            NewAppEmblemShape.ROUNDED_RECTANGLE -> drawRoundRect(brush, cornerRadius = androidx.compose.ui.geometry.CornerRadius(size.minDimension * 0.24f))
            NewAppEmblemShape.DIAMOND -> {
                val path = Path().apply {
                    moveTo(size.width / 2f, 0f); lineTo(size.width, size.height / 2f)
                    lineTo(size.width / 2f, size.height); lineTo(0f, size.height / 2f); close()
                }
                drawPath(path, brush)
            }
            NewAppEmblemShape.STAR -> {
                val path = Path()
                repeat(10) { index ->
                    val radius = if (index % 2 == 0) size.minDimension * 0.5f else size.minDimension * 0.22f
                    val angle = -Math.PI / 2.0 + index * Math.PI / 5.0
                    val point = Offset(
                        size.width / 2f + (kotlin.math.cos(angle) * radius).toFloat(),
                        size.height / 2f + (kotlin.math.sin(angle) * radius).toFloat()
                    )
                    if (index == 0) path.moveTo(point.x, point.y) else path.lineTo(point.x, point.y)
                }
                path.close(); drawPath(path, brush)
            }
            NewAppEmblemShape.NONE -> Unit
        }
    }
}

@Composable
private fun AppIconImage(
    label: String,
    icon: ImageBitmap?,
    modifier: Modifier = Modifier
) {
    if (icon != null) {
        Image(
            bitmap = icon,
            contentDescription = label,
            modifier = modifier.clip(RoundedCornerShape(14.dp))
        )
    } else {
        Box(
            modifier = modifier
                .clip(RoundedCornerShape(14.dp))
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label.take(1).uppercase(),
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun InfoCard(
    title: String,
    text: String,
    buttonText: String?,
    onButtonClick: (() -> Unit)?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.08f))
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = text,
                color = Color.White.copy(alpha = 0.72f),
                style = MaterialTheme.typography.bodyMedium
            )
            if (buttonText != null && onButtonClick != null) {
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedButton(onClick = onButtonClick) {
                    Text(buttonText)
                }
            }
        }
    }
}

@Composable
private fun AnimatedLauncherBackground(
    dynamicBackground: Boolean,
    backgroundColorHex: String,
    circleColorAHex: String,
    circleColorBHex: String,
    circleCount: Int,
    modifier: Modifier = Modifier
) {
    val transition = rememberInfiniteTransition(label = "background")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(animation = tween(durationMillis = 9000, easing = LinearEasing), repeatMode = RepeatMode.Reverse),
        label = "phase"
    )
    val base = parseAppFrameColor(backgroundColorHex)
    val circleA = parseAppFrameColor(circleColorAHex)
    val circleB = parseAppFrameColor(circleColorBHex)
    Canvas(modifier = modifier) {
        val background = if (dynamicBackground) Color.hsv((210f + phase * 130f) % 360f, 0.42f, 0.16f) else base
        drawRect(background)
        val count = circleCount.coerceIn(0, 12)
        repeat(count) { index ->
            val ratio = if (count <= 1) 0f else index / (count - 1f)
            val movement = sin((phase * 2f + ratio * 2.7f) * Math.PI).toFloat()
            val radius = size.minDimension * (0.12f + (index % 4) * 0.055f + phase * 0.018f)
            val center = Offset(
                x = size.width * ((0.12f + ratio * 0.78f + movement * 0.08f).coerceIn(0.05f, 0.95f)),
                y = size.height * ((0.10f + ((index * 37) % 83) / 100f + movement * 0.05f).coerceIn(0.05f, 0.95f))
            )
            drawCircle(color = (if (index % 2 == 0) circleA else circleB).copy(alpha = 0.42f + (index % 3) * 0.10f), radius = radius, center = center)
        }
    }
}
