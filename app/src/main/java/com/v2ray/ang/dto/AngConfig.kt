package com.v2ray.ang.dto


data class AngConfig(
        var order: Int,
        var server: ArrayList<AngConfigItem> = ArrayList()
) {
    data class AngConfigItem(
            var index: Int,
            var name: String
    )

    fun getName(index: Int): String? {
        return this.server.filter { it.index == index }.let {
            if (it.isNotEmpty()) {
                it[0].name
            } else {
                null
            }
        }
    }
}
