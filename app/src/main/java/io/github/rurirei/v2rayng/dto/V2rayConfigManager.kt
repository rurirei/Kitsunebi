package io.github.rurirei.v2rayng.dto

import android.content.Context
import com.v2ray.ang.dto.AngConfigManager
import io.github.rurirei.kitsunebi.GsonUtil
import io.github.rurirei.kitsunebi.PreferencesUtil.get
import io.github.rurirei.kitsunebi.PreferencesUtil.put
import io.github.rurirei.kitsunebi.PreferencesUtil.sharedPreferences


class V2rayConfigManager(private val context: Context) {

    companion object {
        private const val ANG_CONFIG = "ang_config"
    }

    private val angConfigManager: AngConfigManager by lazy { AngConfigManager(context) }

    // TODO: data class Instance needs be fixed
    // fun v2rayConfigInstance(index: Int): V2rayCustomConfig? = GsonUtil.toV2ray(v2rayConfigString(index))
    fun v2rayConfigStringCurrent(): String? = v2rayConfigString(angConfigManager.angConfigs?.order ?: 0)
    fun v2rayConfigString(index: Int): String? = GsonUtil.format(context.sharedPreferences.get(configId(index), ""))
    private fun configId(index: Int): String = "${ANG_CONFIG}${index}"

    private fun storeConfig(index: Int, content: String?) {
        context.sharedPreferences.put(configId(index), content ?: "")
    }

    // Preferences.put() used to both addServer and editServer
    fun editServer(index: Int, content: String?) {
        storeConfig(index, content)
    }

    // V2rayConfigManager.addServer must be the pre-operation, due to
    // angConfigManager.angConfigsCount will change after AngConfigManager.addServer
    fun addServer(content: String?) {
        editServer(angConfigManager.angConfigsCount ?: 0, content)
    }

    // just delete one item at AngListView @AngConfigManager.deleteServer()
    @Suppress("unused")
    fun deleteServer(index: Int) {
    }

}
