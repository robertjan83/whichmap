package app.whichmap

import android.app.AlarmManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.SystemClock
import android.text.format.DateFormat
import android.widget.RemoteViews
import java.util.Date

class MapWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) {
        ids.forEach { update(context, manager, it) }
        scheduleNext(context)
    }

    override fun onEnabled(context: Context) = scheduleNext(context)

    override fun onDisabled(context: Context) {
        alarmManager(context).cancel(tickIntent(context))
    }

    override fun onDeleted(context: Context, ids: IntArray) {
        val edit = prefs(context).edit()
        ids.forEach { edit.remove(KEY + it) }
        edit.apply()
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        when (intent.action) {
            ACTION_TICK, Intent.ACTION_BOOT_COMPLETED, Intent.ACTION_TIMEZONE_CHANGED, Intent.ACTION_TIME_CHANGED -> updateAll(context)
        }
    }

    companion object {
        private const val ACTION_TICK = "app.whichmap.TICK"
        private const val PREFS = "widgets"
        private const val KEY = "mode_"

        private fun prefs(c: Context) = c.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        private fun alarmManager(c: Context) = c.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        fun modeOf(c: Context, id: Int): String = prefs(c).getString(KEY + id, "quads") ?: "quads"
        fun saveMode(c: Context, id: Int, mode: String) = prefs(c).edit().putString(KEY + id, mode).apply()

        fun updateAll(context: Context) {
            val mgr = AppWidgetManager.getInstance(context)
            val ids = mgr.getAppWidgetIds(ComponentName(context, MapWidgetProvider::class.java))
            ids.forEach { update(context, mgr, it) }
            if (ids.isNotEmpty()) scheduleNext(context)
        }

        fun update(context: Context, mgr: AppWidgetManager, id: Int) {
            val mode = modeOf(context, id)
            val now = System.currentTimeMillis()
            val s = Rotation.slotAt(mode, now)
            val fmt = DateFormat.getTimeFormat(context)

            val rv = RemoteViews(context.packageName, R.layout.widget)
            rv.setImageViewResource(R.id.art, Rotation.MAP_RES.getValue(s.current))
            rv.setTextViewText(R.id.mode, "RESURGENCE " + Rotation.MODE_LABEL.getValue(mode).uppercase())
            rv.setTextViewText(R.id.name, Rotation.MAP_NAME.getValue(s.current))
            rv.setTextViewText(
                R.id.next,
                context.getString(R.string.widget_next) + " " + Rotation.MAP_NAME.getValue(s.next) + " · " + fmt.format(Date(s.end))
            )
            // Live aftellen zonder dat de widget elke seconde ververst hoeft te worden
            rv.setChronometer(R.id.timer, SystemClock.elapsedRealtime() + (s.end - now), null, true)
            rv.setChronometerCountDown(R.id.timer, true)

            val open = Intent(context, MainActivity::class.java)
                .putExtra("mode", mode)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            val pi = PendingIntent.getActivity(context, id, open, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
            rv.setOnClickPendingIntent(R.id.root, pi)

            mgr.updateAppWidget(id, rv)
        }

        private fun tickIntent(context: Context): PendingIntent {
            val i = Intent(context, MapWidgetProvider::class.java).setAction(ACTION_TICK)
            return PendingIntent.getBroadcast(context, 0, i, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        }

        /** Plant een verversing vlak na de volgende mapwissel. */
        fun scheduleNext(context: Context) {
            val am = alarmManager(context)
            val at = Rotation.nextBoundary(System.currentTimeMillis()) + 1500
            val pi = tickIntent(context)
            val exact = Build.VERSION.SDK_INT < Build.VERSION_CODES.S || am.canScheduleExactAlarms()
            try {
                if (exact) am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, pi)
                else am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, pi)
            } catch (e: SecurityException) {
                am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, pi)
            }
        }
    }
}
