package com.v2ray.ang.dto


enum class AngConfigProtocolType(val value: Int) {
    HTTP(1),
    SOCKS(2),
    SHADOWSOCKS(3),
    VMESS(4),
    VLESS(5),
    `DOKODEMO-DOOR`(6);

    companion object {
        fun fromInt(value: Int) = values().firstOrNull { it.value == value }
    }

}
