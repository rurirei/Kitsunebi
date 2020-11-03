package io.github.rurirei.v2rayng.util

import `fun`.kitsunebi.kitsunebi4android.R
import `fun`.kitsunebi.kitsunebi4android.ui.MainActivity
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.Icon
import androidx.annotation.RequiresApi
import com.v2ray.ang.dto.AngConfigManager
import io.github.rurirei.kitsunebi.PreferencesUtil.get
import io.github.rurirei.kitsunebi.PreferencesUtil.sharedPreferences
import io.github.rurirei.v2rayng.service.V2RayProxyService
import io.github.rurirei.v2rayng.service.V2RayVpnService
import io.github.rurirei.v2rayng.service.entity.V2RayServiceManager


@RequiresApi(26)
object NotificationUtil {

    private const val notificationsCHANNELID = "RAY_NG_M_CH_ID"
    private const val notificationsChannelName = "V2rayNG Background Service"
    private fun notificationsTextTitle(context: Context) = AngConfigManager(context).angConfigs?.let { it.getName(it.order) }
    private const val notificationsSmallIcon = R.drawable.ic_kitsunebi_tile
    private const val notificationsImportance = NotificationManager.IMPORTANCE_LOW
    private const val notificationsNotificationPendingIntentContent = 0
    /* private const val notificationsNotificationPendingIntentSTOP = 1 */
    const val notificationsNotificationId = 1
    const val notificationsStopV2RayFlag = "stop"

    fun notificationBuilder(context: Context): Notification.Builder {
        val startMainIntent = Intent(context, MainActivity::class.java)
                .let {
                    PendingIntent.getActivity(context,
                            notificationsNotificationPendingIntentContent, it,
                            PendingIntent.FLAG_UPDATE_CURRENT)
                }
        /* val stopV2RayIntent = Intent(AppConfig.BROADCAST_ACTION_SERVICE).apply {
            `package` = AppConfig.ANG_PACKAGE
            putExtra("key", AppConfig.MSG_STATE_STOP)
        }
        val stopV2RayPendingIntent = PendingIntent.getBroadcast(context,
                notificationsNotificationPendingIntentSTOP, stopV2RayIntent,
                PendingIntent.FLAG_UPDATE_CURRENT) */
        val stopV2RayPendingIntent = if (context.sharedPreferences.get(V2RayServiceManager.PREF_PROXY_ONLY, false)) {
            Intent(context, V2RayProxyService::class.java)
        } else {
            Intent(context, V2RayVpnService::class.java)
        }.apply {
            putExtra(notificationsStopV2RayFlag, true)
        }.let {
            PendingIntent.getService(context, 0, it, PendingIntent.FLAG_UPDATE_CURRENT)
        }
        val notificationAction: Notification.Action = Notification.Action.Builder(
                Icon.createWithResource(context, R.drawable.ic_close_grey_800_24dp),
                "Stop",
                stopV2RayPendingIntent
        ).build()
        return Notification.Builder(context, notificationsCHANNELID)
                .setSmallIcon(notificationsSmallIcon)
                .setContentTitle(notificationsTextTitle(context))
                .setContentIntent(startMainIntent)
                .setOngoing(true)
                .setShowWhen(false)
                .setOnlyAlertOnce(true)
                .addAction(notificationAction)
    }

    fun createNotificationChannel(context: Context) {
        notificationManager(context).createNotificationChannel(notificationChannel())
    }

    private fun notificationChannel(): NotificationChannel =
            NotificationChannel(
                    notificationsCHANNELID,
                    notificationsChannelName,
                    notificationsImportance
            ).apply {
                lightColor = Color.DKGRAY
                lockscreenVisibility = Notification.VISIBILITY_PRIVATE
            }

    private fun notificationManager(context: Context): NotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

}
