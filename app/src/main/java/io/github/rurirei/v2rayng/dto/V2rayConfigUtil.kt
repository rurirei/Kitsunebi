package io.github.rurirei.v2rayng.dto

import android.content.Context
import com.google.gson.Gson
import com.v2ray.ang.dto.AngConfigManager
import com.v2ray.ang.dto.AngConfigProtocolType
import io.github.rurirei.kitsunebi.PreferencesUtil.get
import io.github.rurirei.kitsunebi.PreferencesUtil.sharedPreferences
import java.util.Locale


object V2rayConfigUtil {

    object Type {
        val VMESS get() = AngConfigProtocolType.VMESS.name.toLowerCase(Locale.ROOT)
        val VLess get() = AngConfigProtocolType.VLESS.name.toLowerCase(Locale.ROOT)
        val SHADOWSOCKS get() = AngConfigProtocolType.SHADOWSOCKS.name.toLowerCase(Locale.ROOT)
        val SOCKS get() = AngConfigProtocolType.SOCKS.name.toLowerCase(Locale.ROOT)
        val DOKODEMO get() = AngConfigProtocolType.`DOKODEMO-DOOR`.name.toLowerCase(Locale.ROOT)
    }

    fun inboundAddressPort(context: Context, guid: String, filter: String? = null): MutableList<String?> {
        return inboundAddressPort(inbound(context, guid, filter))
    }

    fun outboundAddressPort(context: Context, guid: String, filter: String? = null): MutableList<String?> {
        return outboundAddressPort(outbound(context, guid, filter))
    }

    fun inboundAddressPort(jsonConfig: String?, filter: String? = null): MutableList<String?> {
        return inboundAddressPort(inbound(jsonConfig, filter))
    }

    fun outboundAddressPort(jsonConfig: String?, filter: String? = null): MutableList<String?> {
        return outboundAddressPort(outbound(jsonConfig, filter))
    }

    /**
     * return address and port of selected inbound
     */
    @Throws(NullPointerException::class)
    private fun inboundAddressPort(inbound: V2rayConfig.InboundBean?): MutableList<String?> {
        val address = inbound?.settings?.address
        val port = inbound?.port.toString()
        return mutableListOf(address, port)
    }

    /**
     * return address and port of selected outbound
     */
    @Throws(NullPointerException::class)
    private fun outboundAddressPort(outbound: V2rayConfig.OutboundBean?): MutableList<String?> {
        val address = when(outbound?.protocol?.toLowerCase(Locale.ROOT)) {
            Type.VMESS -> {
                outbound.settings.vnext?.get(0)?.address
            }
            Type.VLess -> {
                outbound.settings.vnext?.get(0)?.address
            }
            Type.SHADOWSOCKS -> {
                outbound.settings.servers?.get(0)?.address
            }
            Type.SOCKS -> {
                outbound.settings.servers?.get(0)?.address
            }
            else -> {
                null
            }
        }
        val port = when(outbound?.protocol?.toLowerCase(Locale.ROOT)) {
            Type.VMESS -> {
                outbound.settings.vnext?.get(0)?.port.toString()
            }
            Type.VLess -> {
                outbound.settings.vnext?.get(0)?.port.toString()
            }
            Type.SHADOWSOCKS -> {
                outbound.settings.servers?.get(0)?.port.toString()
            }
            Type.SOCKS -> {
                outbound.settings.servers?.get(0)?.port.toString()
            }
            else -> {
                null
            }
        }
        return mutableListOf(address, port)
    }

    /**
     * return inbound of specified type
     */
    @Throws(NullPointerException::class)
    private fun inbound(context: Context, guid: String, filter: String?): V2rayConfig.InboundBean? {
        return inbounds(context, guid)?.filter {
            filter == null || it.protocol.toLowerCase(Locale.ROOT) == filter
        }?.get(0)
    }

    /**
     * return outbound of specified type
     */
    @Throws(NullPointerException::class)
    private fun outbound(context: Context, guid: String, filter: String?): V2rayConfig.OutboundBean? {
        return outbounds(context, guid)?.filter {
            filter == null || it.protocol.toLowerCase(Locale.ROOT) == filter
        }?.get(0)
    }

    /**
     * return inbound of specified type
     */
    @Throws(NullPointerException::class)
    private fun inbound(jsonConfig: String?, filter: String?): V2rayConfig.InboundBean? {
        return inbounds(jsonConfig)?.filter {
            filter == null || it.protocol.toLowerCase(Locale.ROOT) == filter
        }?.get(0)
    }

    /**
     * return outbound of specified type
     */
    @Throws(NullPointerException::class)
    private fun outbound(jsonConfig: String?, filter: String?): V2rayConfig.OutboundBean? {
        return outbounds(jsonConfig)?.filter {
            filter == null || it.protocol.toLowerCase(Locale.ROOT) == filter
        }?.get(0)
    }

    /**
     * return inbounds list of selected custom config
     */
    @Throws(NullPointerException::class)
    private fun inbounds(context: Context, guid: String): ArrayList<V2rayConfig.InboundBean>? {
        val v2rayConfig = v2rayConfig(context, guid)
        return v2rayConfig?.inbounds
    }

    /**
     * return outbounds list of selected custom config
     */
    @Throws(NullPointerException::class)
    private fun outbounds(context: Context, guid: String): ArrayList<V2rayConfig.OutboundBean>? {
        val v2rayConfig = v2rayConfig(context, guid)
        return v2rayConfig?.outbounds
    }

    /**
     * return inbounds list of selected custom config
     */
    @Throws(NullPointerException::class)
    private fun inbounds(jsonConfig: String?): ArrayList<V2rayConfig.InboundBean>? {
        val v2rayConfig = v2rayConfig(jsonConfig)
        return v2rayConfig?.inbounds
    }

    /**
     * return outbounds list of selected custom config
     */
    @Throws(NullPointerException::class)
    private fun outbounds(jsonConfig: String?): ArrayList<V2rayConfig.OutboundBean>? {
        val v2rayConfig = v2rayConfig(jsonConfig)
        return v2rayConfig?.outbounds
    }

    /**
     * return parsed config from provided String
     */
    @Throws(NullPointerException::class)
    private fun v2rayConfig(context: Context, guid: String): V2rayConfig? {
        val jsonConfig = context.sharedPreferences.get(AngConfigManager.ANG_CONFIG + guid, "")
        return v2rayConfig(jsonConfig)
    }

    /**
     * return parsed config from provided String
     */
    @Throws(com.google.gson.JsonSyntaxException::class, NullPointerException::class)
    private fun v2rayConfig(jsonConfig: String?): V2rayConfig? {
        return Gson().fromJson(jsonConfig, V2rayConfig::class.java)
    }

}
