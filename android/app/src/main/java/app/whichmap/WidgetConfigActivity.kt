package app.whichmap

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

/** Vraagt bij het toevoegen van de widget om Solo, Duos of Quads. */
class WidgetConfigActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setResult(RESULT_CANCELED)

        val id = intent?.extras?.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
            ?: AppWidgetManager.INVALID_APPWIDGET_ID
        if (id == AppWidgetManager.INVALID_APPWIDGET_ID) { finish(); return }

        val pad = (20 * resources.displayMetrics.density).toInt()
        val box = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(pad, pad, pad, pad)
        }
        box.addView(TextView(this).apply {
            text = getString(R.string.config_title)
            textSize = 18f
            setPadding(0, 0, 0, pad / 2)
        })
        Rotation.MODE_LABEL.forEach { (mode, label) ->
            box.addView(Button(this).apply {
                text = label
                setOnClickListener {
                    MapWidgetProvider.saveMode(this@WidgetConfigActivity, id, mode)
                    MapWidgetProvider.update(this@WidgetConfigActivity, AppWidgetManager.getInstance(this@WidgetConfigActivity), id)
                    MapWidgetProvider.scheduleNext(this@WidgetConfigActivity)
                    setResult(RESULT_OK, Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, id))
                    finish()
                }
            })
        }
        setContentView(box)
    }
}
