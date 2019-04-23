package com.github.luoyemyy.mall.manager.util

import android.app.Application
import android.content.Context
import androidx.appcompat.app.AlertDialog
import com.github.luoyemyy.config.Profile
import com.github.luoyemyy.config.devDevDevPro
import com.github.luoyemyy.config.spfInt
import com.github.luoyemyy.mall.manager.api.Api

object MallProfile {
    fun getApiUrl(): String {
        return devDevDevPro("http://192.168.0.141:8080/", "http://47.106.181.28/").value()
    }

    fun current(): String {
        return "当前环境：${Profile.currentTypeInfo()}"
    }

    fun init(app: Application) {
        val current = app.spfInt("profile").let {
            if (it == 0) {
                Profile.DEV
            } else {
                it
            }
        }
        Profile.setType(current)
    }

    fun setCurrent(app: Context, type: Int) {
        app.spfInt("profile", type)
    }

    fun select(actContext: Context, result: (String) -> Unit) {
        AlertDialog.Builder(actContext).setItems(arrayOf("dev", "pro")) { _, which ->
            val type = if (which == 0) Profile.DEV else Profile.PRO
            Profile.setType(type)
            setCurrent(actContext, type)
            Api.changeProfile()
            result(current())
        }.show()
    }

}