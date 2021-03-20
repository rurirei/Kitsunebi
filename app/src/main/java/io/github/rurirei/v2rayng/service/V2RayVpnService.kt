package io.github.rurirei.v2rayng.service

import `fun`.kitsunebi.kitsunebi4android.BuildConfig
import `fun`.kitsunebi.kitsunebi4android.R
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.*
import android.os.Build
import android.os.ParcelFileDescriptor
import android.os.StrictMode
import androidx.annotation.RequiresApi
import io.github.rurirei.kitsunebi.PreferencesUtil.get
import io.github.rurirei.kitsunebi.PreferencesUtil.sharedPreferences
import io.github.rurirei.v2rayng.service.entity.V2RayServiceManager
import io.github.rurirei.v2rayng.service.entity.tun2socks.ITun2socksService
import io.github.rurirei.v2rayng.service.entity.tun2socks.Tun2socksManager
import io.github.rurirei.v2rayng.service.entity.vpn.IVPNService
import io.github.rurirei.v2rayng.util.NotificationUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.ByteBuffer


class V2RayVpnService: VpnService(), IVPNService, ITun2socksService {

    companion object {
        var isRunning: Boolean = false
    }

    private val tun2socksManager by lazy { Tun2socksManager(this) }
    private val v2RayServiceManager by lazy { V2RayServiceManager() }

    private var pfd: ParcelFileDescriptor? = null

    private val fileInputStream: FileInputStream by lazy { FileInputStream(pfd?.fileDescriptor) }
    private val fileOutputStream: FileOutputStream by lazy { FileOutputStream(pfd?.fileDescriptor) }
    private val buffer: ByteBuffer by lazy { ByteBuffer.allocate(1501) }
    private val array: ByteArray by lazy { buffer.array() }

    private val fileScope: (() -> Unit) -> Unit = { unit -> GlobalScope.launch (Dispatchers.IO) { unit() } }

    /**
        * Unfortunately registerDefaultNetworkCallback is going to return our VPN interface: https://android.googlesource.com/platform/frameworks/base/+/dda156ab0c5d66ad82bdcf76cda07cbc0a9c8a2e
        *
        * This makes doing a requestNetwork with REQUEST necessary so that we don't get ALL possible networks that
        * satisfies default network capabilities but only THE default network. Unfortunately we need to have
        * android.permission.CHANGE_NETWORK_STATE to be able to call requestNetwork.
        *
        * Source: https://android.googlesource.com/platform/frameworks/base/+/2df4c7d/services/core/java/com/android/server/ConnectivityService.java#887
        */

    @delegate:RequiresApi(28)
    private val defaultNetworkRequest by lazy {
        NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .addCapability(NetworkCapabilities.NET_CAPABILITY_NOT_RESTRICTED)
                .build()
    }

    @delegate:RequiresApi(28)
    private val connectivity by lazy {
        getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    }

    @delegate:RequiresApi(28)
    private val defaultNetworkCallback by lazy {
        object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                setUnderlyingNetworks(arrayOf(network))
            }
            override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
                // it's a good idea to refresh capabilities
                setUnderlyingNetworks(arrayOf(network))
            }
            override fun onLost(network: Network) {
                setUnderlyingNetworks(null)
            }
        }
    }

    override fun onLowMemory() {
        super.onLowMemory()
        stopV2Ray()
    }

    override fun onRevoke() {
        super.onRevoke()
        stopV2Ray()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopV2Ray()
    }

    override fun onCreate() {
        super.onCreate()
        StrictMode.setThreadPolicy(StrictMode.ThreadPolicy.Builder().permitAll().build())
        v2RayServiceManager.softReferenceVPN(this)
        v2RayServiceManager.softReferenceTun2socks(this)
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        if (intent.getBooleanExtra(NotificationUtil.notificationsStopV2RayFlag, false)) {
            stopV2Ray()
        } else {
            startV2ray()
        }
        return Service.START_NOT_STICKY
    }

    private fun setup() {
        // Close the old interface since the parameters have been changed.
        try {
            pfd?.close()
        } catch (ignored: Exception) {
        }

        val prepare = prepare(this)
        if (prepare != null) {
            return
        }

        // If the old interface has exactly the same parameters, use it!
        // Configure a builder while parsing the parameters.
        val builder = Builder().apply {
            addAddress("172.19.0.1", 30)
            addRoute("0.0.0.0", 0)
            addDnsServer("8.8.8.8")
            if (tun2socksManager.useIPv6) {
                addAddress("fdfe:dcba:9876::1", 126)
                addRoute("::", 0)
                addDnsServer("2001:4860:4860::8888")
            }
            setMtu(1500)
            setSession(BuildConfig.APPLICATION_ID)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                setMetered(false)
            }

            if (sharedPreferences.get(getString(R.string.is_enable_per_app_vpn), false)) {
                val perAppMode = sharedPreferences.get(getString(R.string.per_app_mode), "0")
                when (Integer.parseInt(perAppMode)) {
                    0 -> {
                        val allowedAppList = sharedPreferences.get(getString(R.string.per_app_allowed_app_list), "")
                        try {
                            allowedAppList.split(",").filter { it.isNotEmpty() }.forEach {
                                addAllowedApplication(it)
                            }
                        } catch (ignored: PackageManager.NameNotFoundException) {
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                    1 -> {
                        val disallowedAppList = sharedPreferences.get(getString(R.string.per_app_disallowed_app_list), "")
                        try {
                            disallowedAppList.split(",").filter { it.isNotEmpty() }.forEach {
                                addDisallowedApplication(it)
                            }
                            addDisallowedApplication(BuildConfig.APPLICATION_ID)
                        } catch (ignored: PackageManager.NameNotFoundException) {
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                    else -> {
                    }
                }
            } else {
                addDisallowedApplication(BuildConfig.APPLICATION_ID)
            }
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                connectivity.requestNetwork(defaultNetworkRequest, defaultNetworkCallback)
            }
        } catch (ignored: Exception) {
        }

        try {
            // Create a new interface using the builder and save the parameters.
            pfd = builder.establish()
        } catch (e: Exception) {
            e.printStackTrace()
            stopV2Ray()
            return
        }
    }

    private fun shutdown() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                connectivity.unregisterNetworkCallback(defaultNetworkCallback)
            }
        } catch (ignored: Exception) {
        }

        try {
            pfd?.close()
        } catch (ignored: Exception) {
        }
    }

    // TODO: java.io.IOException: read failed: EBADF (Bad file descriptor)
    // TODO: crash it inside multi-process
    // TODO: return value and catch exceptions
    private fun inputPacket(): ByteArray? {
        return fileInputStream.read(array).let { len ->
            if (len > 0) {
                buffer.limit(len)
                array
            } else {
                null
            }
        }
    }

    private fun outputPacket(data: ByteArray?) {
        fileScope {
            fileOutputStream.write(data)
        }
    }

    private fun startV2ray() {
        try {
            v2RayServiceManager.startV2ray()
        } catch (ignored: NullPointerException) {
        } catch (ignored: com.google.gson.JsonSyntaxException) {
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun stopV2Ray() {
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
        stopForeground(Service.STOP_FOREGROUND_REMOVE)
        isRunning = false
    }

    override fun getService(): Service {
        return this
    }

    override fun vpnSetup() {
        startInForeground()
        setup()
    }

    override fun vpnShutdown() {
        shutdown()
        stopInForeground()
        stopSelf()
    }

    override fun vpnProtect(socket: Int): Boolean {
        return protect(socket)
    }

    override fun handlePackets(): ByteArray? {
        return inputPacket()
    }

    override fun packetFlow(data: ByteArray?) {
        outputPacket(data)
    }

}
