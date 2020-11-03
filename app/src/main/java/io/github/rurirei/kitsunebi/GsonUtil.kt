package io.github.rurirei.kitsunebi

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import com.v2ray.ang.dto.AngConfig
import io.github.rurirei.v2rayng.dto.V2rayConfig


object GsonUtil {

    @Throws(com.google.gson.JsonSyntaxException::class, NullPointerException::class)
    fun format(json: String?): String? {
        return GsonBuilder().setPrettyPrinting().create().toJson(JsonParser.parseString(json))
    }

    @Throws(com.google.gson.JsonSyntaxException::class, NullPointerException::class)
    fun toAng(json: String?): AngConfig? {
        return Gson().fromJson(format(json), AngConfig::class.java)
    }

    @Throws(com.google.gson.JsonSyntaxException::class, NullPointerException::class)
    fun fromAng(angConfigs: AngConfig?): String? {
        return format(Gson().toJson(angConfigs))
    }

    @Throws(com.google.gson.JsonSyntaxException::class, NullPointerException::class)
    fun toV2ray(json: String?): V2rayConfig? {
        return Gson().fromJson(format(json), V2rayConfig::class.java)
    }

    @Throws(com.google.gson.JsonSyntaxException::class, NullPointerException::class)
    fun fromV2ray(v2rayConfig: V2rayConfig?): String? {
        return format(Gson().toJson(v2rayConfig))
    }

}
