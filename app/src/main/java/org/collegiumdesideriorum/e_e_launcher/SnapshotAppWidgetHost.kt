package org.collegiumdesideriorum.e_e_launcher

import android.appwidget.AppWidgetHost
import android.appwidget.AppWidgetHostView
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProviderInfo
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.view.View
import android.widget.RemoteViews
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import java.lang.ref.WeakReference
import java.util.concurrent.ConcurrentHashMap

/** AppWidget host that retains a drawable copy for Compose and Mesh captures. */
internal class SnapshotAppWidgetHost(context: Context, hostId: Int) : AppWidgetHost(context, hostId) {
    private val visibleHostViews = ConcurrentHashMap<Int, MutableList<WeakReference<SnapshotAppWidgetHostView>>>()

    override fun onCreateView(
        context: Context,
        appWidgetId: Int,
        appWidget: AppWidgetProviderInfo
    ): AppWidgetHostView = SnapshotAppWidgetHostView(context, appWidgetId).also {
        visibleHostViews.computeIfAbsent(appWidgetId) { mutableListOf() }.add(WeakReference(it))
    }

    fun captureWidgetsNow(widgetIds: Collection<Int>) {
        widgetIds.forEach { widgetId ->
            visibleHostViews[widgetId]?.let { hosts ->
                hosts.removeAll { it.get() == null }
                hosts.toList().forEach { it.get()?.captureNow() }
            }
        }
    }
}

internal object HostedWidgetSnapshotCache {
    private val frames = ConcurrentHashMap<Int, Bitmap>()

    fun get(appWidgetId: Int): Bitmap? = frames[appWidgetId]

    fun put(appWidgetId: Int, bitmap: Bitmap) {
        frames[appWidgetId] = bitmap
    }

    fun remove(appWidgetId: Int) {
        frames.remove(appWidgetId)
    }
}

internal class SnapshotAppWidgetHostView(
    context: Context,
    private val appWidgetId: Int
) : AppWidgetHostView(context) {
    var onSnapshotReady: ((Bitmap) -> Unit)? = null
    var onFallbackStateChanged: ((Boolean) -> Unit)? = null
    val latestSnapshot: Bitmap?
        get() = HostedWidgetSnapshotCache.get(appWidgetId)
    var isFallbackActive: Boolean = false
        private set
    private var lastRemoteViews: RemoteViews? = null
    private var fallbackViewCreated = false
    private val captureRunnable = Runnable { captureCurrentFrame() }

    override fun updateAppWidget(remoteViews: RemoteViews?) {
        // A provider process can briefly publish null while Android restarts it.
        // Preserve a good child/snapshot across that transient state. On the
        // very first null update, however, let AppWidgetHostView inflate the
        // provider's initial layout instead of leaving the host completely empty.
        if (remoteViews == null) {
            LauncherDiagnostics.warning(
                "widget RemoteViews null id=$appWidgetId retained=${lastRemoteViews != null} " +
                    "snapshot=${latestSnapshot != null}"
            )
            if (lastRemoteViews != null || latestSnapshot != null) {
                if (lastRemoteViews != null) requestSnapshot()
                return
            }
            fallbackViewCreated = false
            super.updateAppWidget(null)
            return
        }
        LauncherDiagnostics.info(
            "widget RemoteViews received id=$appWidgetId package=${remoteViews.`package`}"
        )
        fallbackViewCreated = false
        super.updateAppWidget(remoteViews)
        if (!fallbackViewCreated) {
            setFallbackActive(false, "remote-views")
            lastRemoteViews = remoteViews
            requestSnapshot()
        }
    }

    override fun getDefaultView(): View {
        fallbackViewCreated = true
        setFallbackActive(true, "default-view")
        // The first host instance is allowed to inflate the provider's declared
        // initial layout. This matters on devices where the provider delays its
        // first update until a host view exists. Once a good frame is retained,
        // a transient provider restart still uses the transparent fallback so
        // it cannot cover the snapshot with Android's disabled grey layer.
        if (lastRemoteViews == null && latestSnapshot == null) {
            return runCatching { super.getDefaultView() }
                .onSuccess { LauncherDiagnostics.info("widget default layout created id=$appWidgetId") }
                .onFailure { LauncherDiagnostics.error("widget default layout failed id=$appWidgetId", it) }
                .getOrElse { transparentFallbackView() }
        }
        LauncherDiagnostics.info("widget transparent retained fallback id=$appWidgetId")
        return transparentFallbackView()
    }

    override fun getErrorView(): View {
        fallbackViewCreated = true
        setFallbackActive(true, "error-view")
        LauncherDiagnostics.error("widget error view requested id=$appWidgetId")
        // Keep a retained frame visible when one exists. With no retained frame,
        // use Android's normal error view so a failed RemoteViews application is
        // visible and diagnosable instead of becoming a fully blank widget.
        return if (latestSnapshot != null) transparentFallbackView() else super.getErrorView()
    }

    private fun transparentFallbackView(): View = View(context).apply {
        setBackgroundColor(Color.TRANSPARENT)
    }

    private fun setFallbackActive(active: Boolean, reason: String) {
        if (isFallbackActive == active) return
        isFallbackActive = active
        LauncherDiagnostics.info(
            "widget fallback state id=$appWidgetId active=$active reason=$reason " +
                "snapshot=${latestSnapshot != null}"
        )
        onFallbackStateChanged?.invoke(active)
    }

    fun requestSnapshot(delayMillis: Long = 80L) {
        removeCallbacks(captureRunnable)
        postDelayed(captureRunnable, delayMillis)
    }

    fun captureNow() {
        removeCallbacks(captureRunnable)
        captureCurrentFrame()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        LauncherDiagnostics.info("widget view attached id=$appWidgetId size=${width}x$height")
        requestSnapshot(120L)
    }

    override fun onDetachedFromWindow() {
        removeCallbacks(captureRunnable)
        LauncherDiagnostics.info("widget view detached id=$appWidgetId")
        super.onDetachedFromWindow()
    }

    override fun onSizeChanged(width: Int, height: Int, oldWidth: Int, oldHeight: Int) {
        super.onSizeChanged(width, height, oldWidth, oldHeight)
        if (width != oldWidth || height != oldHeight) requestSnapshot(120L)
    }

    private fun captureCurrentFrame() {
        if (width <= 0 || height <= 0 || !isAttachedToWindow) return
        // Android's default/error child is the grey disabled card with the
        // provider icon. Never let it overwrite the last known good frame.
        if (isFallbackActive) {
            LauncherDiagnostics.info(
                "widget snapshot skipped id=$appWidgetId reason=fallback-active " +
                    "retained=${latestSnapshot != null}"
            )
            return
        }
        runCatching {
            Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888).also { bitmap ->
                // View alpha is a RenderNode property and does not need to be
                // toggled for an explicit off-screen draw. Toggling it here could
                // expose the live AndroidView for one hardware-compositor frame.
                draw(Canvas(bitmap))
            }
        }.onSuccess { bitmap ->
            HostedWidgetSnapshotCache.put(appWidgetId, bitmap)
            onSnapshotReady?.invoke(bitmap)
            LauncherDiagnostics.info("widget snapshot ready id=$appWidgetId size=${bitmap.width}x${bitmap.height}")
        }.onFailure { error ->
            LauncherDiagnostics.error("widget snapshot failed id=$appWidgetId", error)
        }
    }
}

/** Retains provider metadata across transient AppWidget service/package restarts. */
@Composable
internal fun rememberRetainedAppWidgetInfo(
    manager: AppWidgetManager,
    appWidgetId: Int,
    refreshKey: Any
): AppWidgetProviderInfo? {
    var retainedInfo by remember(appWidgetId) {
        mutableStateOf(manager.getAppWidgetInfo(appWidgetId))
    }
    LaunchedEffect(manager, appWidgetId, refreshKey) {
        repeat(if (retainedInfo == null) 4 else 1) { attempt ->
            manager.getAppWidgetInfo(appWidgetId)?.let {
                retainedInfo = it
                return@LaunchedEffect
            }
            if (attempt < 3 && retainedInfo == null) delay(750L)
        }
    }
    return retainedInfo
}
