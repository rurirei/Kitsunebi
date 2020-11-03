package com.v2ray.ang

import android.app.Application
import android.content.Context
import go.Seq
import io.github.rurirei.v2rayng.util.NotificationUtil


@Suppress("unused")
class AngApplication: Application() {

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        base.let {
            NotificationUtil.createNotificationChannel(it)
            Seq.setContext(it)
        }
    }

}
