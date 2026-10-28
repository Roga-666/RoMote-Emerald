package wseemann.media.romote.composables

import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import wseemann.media.romote.R
import wseemann.media.romote.model.NotificationStyle
import wseemann.media.romote.utils.RemoteControlNotification

@Composable
fun NotificationStylePicker(selectedStyle: NotificationStyle, onSelect: (NotificationStyle) -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(16.dp).selectableGroup(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(stringResource(R.string.notification_style_title), style = MaterialTheme.typography.titleMedium)
        Text(stringResource(R.string.notification_style_help), style = MaterialTheme.typography.bodySmall)
        for (style in listOf(NotificationStyle.THUMB_FRIENDLY, NotificationStyle.CLASSIC)) {
            val title = stringResource(if (style == NotificationStyle.CLASSIC) R.string.notification_style_classic else R.string.notification_style_thumb)
            val selected = style == selectedStyle
            Surface(
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(if (selected) 2.dp else 1.dp, if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier.fillMaxWidth().selectable(selected = selected, role = Role.RadioButton, onClick = { onSelect(style) }),
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(title, style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
                        RadioButton(selected = selected, onClick = null)
                    }
                    val previewDescription = stringResource(R.string.notification_style_preview, title)
                    AndroidView(
                        factory = { context ->
                            FrameLayout(context).apply {
                                val controls = RemoteControlNotification.createPreviewViews(context, style).apply(context, this)
                                // A settings preview must never send remote commands or capture selection taps.
                                disablePreviewInput(controls)
                                importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS
                                addView(controls, FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, android.view.Gravity.CENTER))
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(164.dp)
                            .clip(RoundedCornerShape(12.dp)).background(Color(0xff20282b))
                            .semantics { contentDescription = previewDescription },
                    )
                }
            }
        }
        Text(stringResource(R.string.notification_style_preview_note), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

private fun disablePreviewInput(view: View) {
    view.isClickable = false
    view.isFocusable = false
    view.isEnabled = false
    if (view is ViewGroup) for (index in 0 until view.childCount) disablePreviewInput(view.getChildAt(index))
}
