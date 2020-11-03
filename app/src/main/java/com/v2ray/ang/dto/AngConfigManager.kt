package com.v2ray.ang.dto

import android.content.Context
import io.github.rurirei.kitsunebi.GsonUtil
import io.github.rurirei.kitsunebi.PreferencesUtil.get
import io.github.rurirei.kitsunebi.PreferencesUtil.put
import io.github.rurirei.kitsunebi.PreferencesUtil.sharedPreferences


class AngConfigManager(private val context: Context) {

    companion object {
        const val ANG_CONFIG = "ang_config"
    }

    // items AngConfigs
    val angConfigsCount: Int? get() = angConfigs?.server?.count()
    val angConfigs: AngConfig? get() = GsonUtil.toAng(configs) ?: angConfigsInit
    private val angConfigsInit: AngConfig get() = AngConfig(order = 0).apply { server.add(AngConfig.AngConfigItem(index = 0, name = 0.toString())) }
    private val configs: String get() = context.sharedPreferences.get(ANG_CONFIG, "")

    private fun storeConfigs(configs: String?) {
        context.sharedPreferences.put(ANG_CONFIG, configs ?: "")
    }

    fun activeServer(index: Int) {
        storeConfigs(GsonUtil.fromAng(angConfigs?.apply {
            order = index
        }))
    }

    fun addServer(name: String? = null) {
        storeConfigs(GsonUtil.fromAng(angConfigs?.apply {
            server.add(AngConfig.AngConfigItem(index = angConfigsCount ?: 0, name = name ?: angConfigsCount.toString()))
        }))
    }

    fun editServer(index: Int, name: String) {
        storeConfigs(GsonUtil.fromAng(angConfigs?.apply {
            server[index].name = name
        }))
    }

    fun deleteServer(index: Int): Boolean {
        if (index < 0 || index > (angConfigsCount ?: -1) - 1) {
            return false
        }
        storeConfigs(GsonUtil.fromAng(angConfigs?.apply {
            server.removeAt(index)
            server.filter { it.index > index }.forEach { it.index-- }
        }))
        return true
    }

    fun moveServer(fromPosition: Int, toPosition: Int) {
        storeConfigs(GsonUtil.fromAng(angConfigs?.apply {
            if (fromPosition - toPosition == 1 or -1) {
                server[fromPosition].index = toPosition
                server[toPosition].index = fromPosition
            } else {
                if (fromPosition < toPosition) {
                    server.filter { it.index in (fromPosition + 1)..toPosition }.forEach { it.index-- }
                } else {
                    server.filter { it.index in fromPosition until toPosition }.forEach { it.index++ }
                }
            }
        }))
    }

}
