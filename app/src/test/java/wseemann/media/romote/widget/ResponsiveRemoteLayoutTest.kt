package wseemann.media.romote.widget

import android.app.Application
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.RectF
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.Button
import android.widget.ImageView
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import wseemann.media.romote.R
import wseemann.media.romote.utils.RemoteControlNotification
import java.io.File

/** Measure and render actual RemoteViews under constrained notification/widget bounds. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class ResponsiveRemoteLayoutTest {
    @Test
    fun notificationFitsConstrainedHosts() {
        checkLayouts(R.layout.notification_remote_expanded, listOf(160 to 88, 200 to 120, 216 to 164, 280 to 164, 600 to 164), 10, View.MeasureSpec.AT_MOST)
    }

    @Test
    fun thumbNotificationFitsConstrainedHosts() {
        checkLayouts(R.layout.notification_remote_thumb, listOf(160 to 88, 200 to 120, 216 to 164, 216 to 180, 280 to 164, 280 to 180, 600 to 180), 11, View.MeasureSpec.AT_MOST)
    }


    @Test
    fun thumbControlsRetainUsableTouchTargets() {
        val context = RuntimeEnvironment.getApplication()
        val density = context.resources.displayMetrics.density
        val ids = listOf(
            R.id.notification_up, R.id.notification_down, R.id.notification_left,
            R.id.notification_right, R.id.notification_ok,
        )
        for ((width, minimum) in listOf(216 to 32, 280 to 42)) {
            val root = RemoteControlNotification.createRemoteViews(context, R.layout.notification_remote_thumb).apply(context, FrameLayout(context))
            root.measure(
                View.MeasureSpec.makeMeasureSpec((width * density).toInt(), View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec((180 * density).toInt(), View.MeasureSpec.AT_MOST),
            )
            root.layout(0, 0, root.measuredWidth, root.measuredHeight)
            for (id in ids) {
                val button = root.findViewById<View>(id)
                assertTrue("Small touch width: ${context.resources.getResourceEntryName(id)}", button.width / density >= minimum - 0.5f)
                assertTrue("Small touch height: ${context.resources.getResourceEntryName(id)}", button.height / density >= minimum - 0.5f)
            }
        }
    }


    @Test
    fun thumbFitsInsideTheNotificationContainer() {
        val context = RuntimeEnvironment.getApplication()
        val density = context.resources.displayMetrics.density
        for ((width, height) in listOf(160 to 88, 216 to 144, 216 to 164, 280 to 164)) {
            val host = FrameLayout(context)
            val root = RemoteControlNotification.createRemoteViews(context, R.layout.notification_remote_thumb).apply(context, host)
            host.addView(root)
            host.measure(
                View.MeasureSpec.makeMeasureSpec((width * density).toInt(), View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec((height * density).toInt(), View.MeasureSpec.EXACTLY),
            )
            host.layout(0, 0, host.measuredWidth, host.measuredHeight)
            assertTrue("Notification content overflows its host", root.right <= host.width && root.bottom <= host.height)
            verify(root, 11)
        }
    }

    @Test
    fun auxiliaryButtonsUseThePanelWithLargerIconsAndRealGaps() {
        val context = RuntimeEnvironment.getApplication()
        val density = context.resources.displayMetrics.density
        for ((width, height) in listOf(216 to 164, 280 to 164, 280 to 180, 360 to 180, 600 to 180)) {
            val root = RemoteControlNotification.createRemoteViews(context, R.layout.notification_remote_thumb).apply(context, FrameLayout(context))
            root.measure(
                View.MeasureSpec.makeMeasureSpec((width * density).toInt(), View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec((height * density).toInt(), View.MeasureSpec.AT_MOST),
            )
            root.layout(0, 0, root.measuredWidth, root.measuredHeight)
            val grid = root.findViewById<ViewGroup>(R.id.thumb_auxiliary_grid)
            val rows = listOf(
                listOf(R.id.notification_volume_up, R.id.notification_power),
                listOf(R.id.notification_volume_down, R.id.notification_home),
                listOf(R.id.notification_mute, R.id.notification_play_pause),
            ).map { row -> row.map { id -> root.findViewById<ImageButton>(id) } }
            fun bounds(view: View): Rect = Rect(0, 0, view.width, view.height).also {
                grid.offsetDescendantRectToMyCoords(view, it)
            }
            val minimum = if (width >= 280) 48 else 40
            for (row in rows) {
                val left = bounds(row[0])
                val right = bounds(row[1])
                assertEquals("Horizontal gap", 8f, (right.left - left.right) / density, 0.6f)
                assertEquals(0, left.left)
                assertEquals("Use the whole available width", grid.width, right.right)
                assertEquals(left.top, right.top)
                for (button in row) {
                    assertTrue("Auxiliary width must be >= ${minimum}dp", button.width / density >= minimum - 0.5f)
                    assertTrue("Auxiliary height must be >= ${minimum}dp", button.height / density >= minimum - 0.5f)
                    val image = RectF(button.drawable.bounds)
                    button.imageMatrix.mapRect(image)
                    if (width >= 280) assertTrue("Icons must grow beyond the old ~26dp artwork", image.width() / density >= 36f)
                }
            }
            assertEquals(0, bounds(rows.first()[0]).top)
            assertEquals("Use the whole available height", grid.height, bounds(rows.last()[0]).bottom)
            for (index in 0 until rows.lastIndex) {
                assertEquals("Vertical gap", 8f, (bounds(rows[index + 1][0]).top - bounds(rows[index][0]).bottom) / density, 0.6f)
            }
            val targets = rows.flatten().map { bounds(it) }
            for (i in targets.indices) for (j in i + 1 until targets.size) {
                assertTrue("Command targets must never overlap", !Rect.intersects(targets[i], targets[j]))
            }
        }
    }

    @Test
    fun statusBarRemoteHasVisibleDetailsAndTransparentCutouts() {
        val context = RuntimeEnvironment.getApplication()
        val icon = context.getDrawable(R.drawable.ic_notification_remote)!!
        for (size in listOf(24, 48)) {
            val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
            icon.setBounds(0, 0, size, size)
            icon.draw(Canvas(bitmap))
            fun alpha(x: Int, y: Int) = android.graphics.Color.alpha(bitmap.getPixel(x * size / 24, y * size / 24))
            assertTrue("The outline must be visible", alpha(5, 11) > 128)
            assertTrue("The directional cross must be visible", alpha(12, 11) > 128)
            assertEquals("The remote body must not become a solid white pill", 0, alpha(8, 15))
            assertEquals("The space around the remote must stay transparent", 0, alpha(1, 11))
            val file = File("build/reports/remote-layouts/notification-status-remote-${size}px.png")
            file.parentFile.mkdirs()
            file.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
            bitmap.recycle()
        }
    }

    @Test
    fun fullWidgetFitsResizedHosts() {
        checkLayouts(R.layout.emerald_remote_widget, listOf(160 to 144, 216 to 180, 284 to 228, 500 to 144, 600 to 400), 10)
    }

    @Test
    fun slimWidgetFitsLockStarSlots() {
        checkLayouts(R.layout.emerald_slim_widget, listOf(120 to 40, 180 to 48, 250 to 56, 500 to 80), 3)
    }

    private fun checkLayouts(layout: Int, sizes: List<Pair<Int, Int>>, count: Int, heightMode: Int = View.MeasureSpec.EXACTLY) {
        val app = RuntimeEnvironment.getApplication()
        for (fontScale in listOf(1f, 2f)) {
            val context = app.createConfigurationContext(Configuration(app.resources.configuration).apply { this.fontScale = fontScale })
            for ((width, height) in sizes) {
                val views = if (layout == R.layout.emerald_slim_widget) RemoteControlNotification.createSlimRemoteViews(context)
                    else RemoteControlNotification.createRemoteViews(context, layout)
                val root = views.apply(context, FrameLayout(context))
                val density = context.resources.displayMetrics.density
                root.measure(
                    View.MeasureSpec.makeMeasureSpec((width * density).toInt(), View.MeasureSpec.EXACTLY),
                    View.MeasureSpec.makeMeasureSpec((height * density).toInt(), heightMode),
                )
                root.layout(0, 0, root.measuredWidth, root.measuredHeight)
                if (fontScale == 1f) preview(root, "${context.resources.getResourceEntryName(layout)}-${width}x$height")
                try {
                    verify(root, count)
                } catch (failure: AssertionError) {
                    fun dump(view: View, indent: String = "") {
                        println("$indent${view.javaClass.simpleName} id=${view.id} measured=${view.measuredWidth}x${view.measuredHeight} bounds=${view.left},${view.top},${view.right},${view.bottom}")
                        if (view is ViewGroup) for (i in 0 until view.childCount) dump(view.getChildAt(i), "$indent  ")
                    }
                    dump(root)
                    throw AssertionError("${width}x$height font=$fontScale: ${failure.message}", failure)
                }
            }
        }
    }

    private fun verify(root: View, expectedButtons: Int) {
        var buttons = 0
        fun visit(view: View) {
            if (view is ViewGroup) {
                for (index in 0 until view.childCount) {
                    val child = view.getChildAt(index)
                    assertTrue("Child extends beyond host: ${child.id}", child.left >= 0 && child.top >= 0 && child.right <= view.width && child.bottom <= view.height)
                    visit(child)
                }
            }
            if (view is ImageButton || view is Button) {
                buttons++
                assertTrue("Missing command", view.hasOnClickListeners())
                assertTrue("Empty touch target ${view.resources.getResourceEntryName(view.id)}: ${view.width}x${view.height}", view.width > 4 && view.height > 4)
                assertTrue("Missing accessibility description", !view.contentDescription.isNullOrBlank())
            }
            if (view is ImageView && view.drawable != null) {
                val image = RectF(view.drawable.bounds)
                view.imageMatrix.mapRect(image)
                assertEquals("Icon artwork must keep its aspect ratio", image.width(), image.height(), 0.1f)
                assertTrue("Artwork crosses cell edge", image.left >= 0 && image.top >= 0 && image.right <= view.width - view.paddingLeft - view.paddingRight + 0.1f && image.bottom <= view.height - view.paddingTop - view.paddingBottom + 0.1f)
                if (view.id == R.id.thumb_dpad_face) {
                    val parent = view.parent as ViewGroup
                    val targets = parent.getChildAt(1)
                    assertEquals("Hit regions must track the artwork", android.graphics.Rect(view.left, view.top, view.right, view.bottom), android.graphics.Rect(targets.left, targets.top, targets.right, targets.bottom))
                }
            }
        }
        visit(root)
        assertEquals(expectedButtons, buttons)
    }

    private fun preview(root: View, name: String) {
        val bitmap = Bitmap.createBitmap(root.width, root.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(0xff20242b.toInt())
        root.draw(canvas)
        val file = File("build/reports/remote-layouts/$name.png")
        file.parentFile.mkdirs()
        file.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        bitmap.recycle()
    }
}
