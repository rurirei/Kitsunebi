package io.github.rurirei.v2rayng.util

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.widget.Toast
import io.github.rurirei.kitsunebi.ConstantUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


object ActivitiesUtil {

    fun startActivity(context: Context, T: Activity, int: Int) {
        Intent(context, T::class.java).apply {
            putExtra(ConstantUtil.INTENT.POSITION, int)
        }.let {
            context.startActivity(it)
        }
    }

    fun startActivity(context: Context, T: Activity) {
        Intent(context, T::class.java).let {
            context.startActivity(it)
        }
    }

    fun toast(context: Context, str: String) {
        CoroutineScope(Dispatchers.Main).launch {
            context.let {
                Toast.makeText(it, str, Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun toast(context: Context, id: Int) {
        CoroutineScope(Dispatchers.Main).launch {
            context.let {
                Toast.makeText(it, it.getString(id), Toast.LENGTH_SHORT).show()
            }
        }
    }

}
