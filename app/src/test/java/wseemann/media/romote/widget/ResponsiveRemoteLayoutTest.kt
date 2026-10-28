package wseemann.media.romote.widget

import android.app.Application
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.RectF
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageButton
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
                verify(root, count)
                if (fontScale == 1f) preview(root, "${context.resources.getResourceEntryName(layout)}-${width}x$height")
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
            if (view is ImageButton) {
                buttons++
                assertTrue("Missing command", view.hasOnClickListeners())
                assertTrue("Empty touch target", view.width > 4 && view.height > 4)
                val image = RectF(view.drawable.bounds)
                view.imageMatrix.mapRect(image)
                assertEquals("Ring must remain circular", image.width(), image.height(), 0.1f)
                assertTrue("Ring crosses cell edge", image.left >= 0 && image.top >= 0 && image.right <= view.width - view.paddingLeft - view.paddingRight + 0.1f && image.bottom <= view.height - view.paddingTop - view.paddingBottom + 0.1f)
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
