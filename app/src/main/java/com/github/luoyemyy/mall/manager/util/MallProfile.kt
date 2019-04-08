package com.github.luoyemyy.mall.manager.util

import com.github.luoyemyy.config.devDevDevPro

object MallProfile {
    fun getApiUrl(): String {
        return devDevDevPro("http://192.168.0.136:8080/", "http://47.106.181.28/").value()
    }
}