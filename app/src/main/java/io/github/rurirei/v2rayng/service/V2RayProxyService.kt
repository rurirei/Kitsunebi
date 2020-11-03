package io.github.rurirei.v2rayng.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import io.github.rurirei.v2rayng.service.entity.V2RayServiceManager
import io.github.rurirei.v2rayng.service.entity.tun2socks.ITun2socksService
import io.github.rurirei.v2rayng.service.entity.vpn.IVPNService
import io.github.rurirei.v2rayng.util.NotificationUtil


class V2RayProxyService: Service(), IVPNService, ITun2socksService {

    companion object {
        var isRunning: Boolean = false
    }

    private val v2RayServiceManager by lazy { V2RayServiceManager() }

    private fun startV2ray() {
        try {
            v2RayServiceManager.startV2ray()
        } catch (ignored: NullPointerException) {
        } catch (ignored: com.google.gson.JsonSyntaxException) {
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun stopV2ray() {
        try {
            v2RayServiceManager.stopV2Ray()
        } catch (ignored: NullPointerException) {
        } catch (ignored: com.google.gson.JsonSyntaxException) {
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun startInForeground() {
        isRunning = true
        startForeground(NotificationUtil.notificationsNotificationId, NotificationUtil.notificationBuilder(this).build())
    }

    private fun stopInForeground() {
        stopForeground(STOP_FOREGROUND_REMOVE)
        isRunning = false
    }

    override fun onCreate() {
        super.onCreate()
        v2RayServiceManager.softReferenceVPN(this)
        v2RayServiceManager.softReferenceTun2socks(this)
    }

    override fun onLowMemory() {
        super.onLowMemory()
        stopV2ray()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopV2ray()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.getBooleanExtra(NotificationUtil.notificationsStopV2RayFlag, false) == true) {
            stopV2ray()
        } else {
            startV2ray()
        }
        return START_NOT_STICKY
    }

    override fun getService(): Service {
        return this
    }

    override fun vpnSetup() {
        startInForeground()
    }

    override fun vpnShutdown() {
        stopInForeground()
        stopSelf()
    }

    override fun vpnProtect(socket: Int): Boolean {
        return false
    }

    override fun handlePackets(): ByteArray? {
        return null
    }

    override fun packetFlow(data: ByteArray?) {
    }

}
