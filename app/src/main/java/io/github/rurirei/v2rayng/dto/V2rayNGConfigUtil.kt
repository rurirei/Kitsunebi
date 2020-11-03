package io.github.rurirei.v2rayng.dto

import org.json.JSONArray
import org.json.JSONObject


object V2rayNGConfigUtil {

    fun parseSocksPort(jsonConfig: String): String {
        val jObj = JSONObject(jsonConfig)
        if (jObj.has("inbounds")) {
            for (i in 0 until jObj.optJSONArray("inbounds")!!.length()) {
                jObj.optJSONArray("inbounds")!!.getJSONObject(i).let {
                    if (it.getString("protocol") == "socks") {
                        val socksPort = it.getInt("port").toString()
                        if (socksPort.isNotEmpty()) {
                            return socksPort
                        }
                    }
                }
            }
        }
        return ""
    }

    fun parseDomainName(jsonConfig: String): String {
        val jObj = JSONObject(jsonConfig)
        if (jObj.has("outbounds")) {
            for (i in 0 until jObj.optJSONArray("outbounds")!!.length()) {
                val domainName = parseDomainName(jObj.optJSONArray("outbounds")!!.getJSONObject(i))
                if (domainName.isNotEmpty()) {
                    return domainName
                }
            }
        }
        return ""
    }

    private fun parseDomainName(outbound: JSONObject): String {
        try {
            if (outbound.has("settings")) {
                val vnext: JSONArray? = when {
                    outbound.optJSONObject("settings")!!.has("vnext") -> {
                        // vmess
                        outbound.optJSONObject("settings")!!.optJSONArray("vnext")
                    }
                    outbound.optJSONObject("settings")!!.has("servers") -> {
                        // shadowsocks or socks
                        outbound.optJSONObject("settings")!!.optJSONArray("servers")
                    }
                    else -> {
                        return ""
                    }
                }
                for (i in 0 until vnext!!.length()) {
                    val item = vnext.getJSONObject(i)
                    val address = item.getString("address")
                    val port = item.getString("port")
                    return if(isIpv6Address(address)) {
                        String.format("[%s]:%s", address, port)
                    } else {
                        String.format("%s:%s", address, port)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return ""
    }

    private fun isIpv6Address(value: String): Boolean {
        var addr = value
        if (addr.indexOf("[") == 0 && addr.lastIndexOf("]") > 0) {
            addr = addr.drop(1)
            addr = addr.dropLast(addr.count() - addr.lastIndexOf("]"))
        }
        val regV6 = Regex("^((?:[0-9A-Fa-f]{1,4}))?((?::[0-9A-Fa-f]{1,4}))*::((?:[0-9A-Fa-f]{1,4}))?((?::[0-9A-Fa-f]{1,4}))*|((?:[0-9A-Fa-f]{1,4}))((?::[0-9A-Fa-f]{1,4})){7}$")
        return regV6.matches(addr)
    }

}
