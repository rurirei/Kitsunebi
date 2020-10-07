package io.github.rurirei.v2rayng.service.entity

import `fun`.kitsunebi.kitsunebi4android.ui.settings.SettingsFragment
import android.content.Context
import android.content.Intent
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.v2ray.ang.dto.AngConfigManager
import io.github.rurirei.kitsunebi.ConstantUtil
import io.github.rurirei.kitsunebi.PreferencesUtil.get
import io.github.rurirei.kitsunebi.PreferencesUtil.sharedPreferences
import io.github.rurirei.v2rayng.dto.V2rayConfigManager
import io.github.rurirei.v2rayng.dto.V2rayNGConfigUtil
import io.github.rurirei.v2rayng.service.V2RayProxyService
import io.github.rurirei.v2rayng.service.V2RayVpnService
import io.github.rurirei.v2rayng.service.entity.tun2socks.ITun2socksService
import io.github.rurirei.v2rayng.service.entity.tun2socks.Tun2socksManager
import io.github.rurirei.v2rayng.service.entity.vpn.IVPNService
import io.github.rurirei.v2rayng.util.NotificationUtil
import libv2ray.*
import java.lang.ref.SoftReference


class V2RayServiceManager {

    companion object {
        const val PREF_PROXY_ONLY = "pref_proxy_only"

        fun startService(context: Context) {
            val intent = if (context.sharedPreferences.get(PREF_PROXY_ONLY, false)) {
                Intent(context, V2RayProxyService::class.java)
            } else {
                Intent(context, V2RayVpnService::class.java)
            }
            context.startService(intent)
        }

        fun stopService(context: Context) {
            try {
                Intent(context, V2RayProxyService::class.java).apply {
                    putExtra(NotificationUtil.notificationsStopV2RayFlag, true)
                }.also { intent ->
                    context.startService(intent)
                }
                Intent(context, V2RayVpnService::class.java).apply {
                    putExtra(NotificationUtil.notificationsStopV2RayFlag, true)
                }.also { intent ->
                    context.startService(intent)
                }
            } catch (ignored: Exception) {
            }
        }
    }

    fun softReferenceTun2socks(context: ITun2socksService) { v2RayTun2socksService = SoftReference(context) }
    fun softReferenceVPN(context: IVPNService) { v2RayVPNService = SoftReference(context) }
    private var v2RayTun2socksService: SoftReference<ITun2socksService>? = null
    private var v2RayVPNService: SoftReference<IVPNService>? = null
    private val context: Context get() = v2RayVPNService?.get()?.getService() ?: throw NullPointerException()

    private val v2rayPointer: V2RayPointer by lazy { Libv2ray.newV2RayPointer(V2RayVPNCallback(), V2RayTun2socksCallback(), V2RayServiceCallback()) }
    private val angConfigManager: AngConfigManager by lazy { AngConfigManager(context) }
    private val v2rayConfigManager: V2rayConfigManager by lazy { V2rayConfigManager(context) }
    private val tun2socksManager by lazy { Tun2socksManager(context) }

    private val localBroadcastManager: LocalBroadcastManager by lazy { LocalBroadcastManager.getInstance(context) }

    fun startV2ray() {
        try {
            v2rayPointer.start()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        if (Libv2ray.isV2rayRunning() && (context.sharedPreferences.get(PREF_PROXY_ONLY, false) || Libv2ray.isTun2socksRunning())) {
            localBroadcastManager.sendBroadcast(Intent(ConstantUtil.BROADCAST.VPN_STARTED))
        } else {
            stopV2Ray()
            localBroadcastManager.sendBroadcast(Intent(ConstantUtil.BROADCAST.VPN_START_ERR))
        }
    }

    fun stopV2Ray() {
        try {
            v2rayPointer.stop()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        localBroadcastManager.sendBroadcast(Intent(ConstantUtil.BROADCAST.VPN_STOPPED))
    }

    private inner class V2RayVPNCallback : VPNServiceSupportsSet {
        override fun shutdown(): Long {
            return try {
                v2RayVPNService?.get()?.vpnShutdown()
                0
            } catch (e: Exception) {
                e.printStackTrace()
                -1
            }
        }

        override fun prepare(): Long {
            return 0
        }

        override fun protect(l: Long): Long {
            return if (v2RayVPNService?.get()?.vpnProtect(l.toInt()) == true) 0 else 1
        }

        override fun onEmitStatus(l: Long, s: String?): Long {
            return 0
        }

        override fun setup(): Long {
            return try {
                v2RayVPNService?.get()?.vpnSetup()
                0
            } catch (e: Exception) {
                e.printStackTrace()
                -1
            }
        }
    }

    private inner class V2RayTun2socksCallback : Tun2socksServiceSupportsSet {
        override fun useIPv6(): Boolean {
            return tun2socksManager.useIPv6
        }

        override fun logLevel(): String {
            return tun2socksManager.logLevel
        }

        override fun fakeIPRange(): String {
            return tun2socksManager.fakeIPRange
        }

        override fun socksAddress(): String {
            return tun2socksManager.socksAddress
        }

        override fun handlePackets(): ByteArray? {
            return v2RayTun2socksService?.get()?.handlePackets()
        }

        override fun packetFlow(data: ByteArray?) {
            v2RayTun2socksService?.get()?.packetFlow(data)
        }
    }

    private inner class V2RayServiceCallback : V2RayServiceSupportsSet {
        override fun configFile(): String {
            return v2rayConfigManager.v2rayConfigStringCurrent().let {
                val domainName = try {
                    V2rayNGConfigUtil.parseDomainName(it!!)
                } catch (ignored: NullPointerException) {
                    stopV2Ray()
                    ""
                } catch (ignored: com.google.gson.JsonSyntaxException) {
                    stopV2Ray()
                    ""
                } catch (e: Exception) {
                    e.printStackTrace()
                    stopV2Ray()
                    ""
                }
                val configFileContent = try {
                    Libv2ray.testConfig(it)
                    it
                } catch (ignored: NullPointerException) {
                    stopV2Ray()
                    ""
                } catch (ignored: com.google.gson.JsonSyntaxException) {
                    stopV2Ray()
                    ""
                } catch (e: Exception) {
                    e.printStackTrace()
                    stopV2Ray()
                    ""
                }
                "${domainName};;;${configFileContent}"
            }
        }

        override fun proxyOnly(): Boolean {
            return context.sharedPreferences.get(PREF_PROXY_ONLY, false)
        }

        override fun resolveDnsNext(): Boolean {
            return context.sharedPreferences.get(SettingsFragment.PREF_PARSE_DOMAIN_NEXT_ENABLED, false)
        }

        override fun logPrint(): Boolean {
            return context.sharedPreferences.get(PREF_PROXY_ONLY, false)
        }
    }

}
