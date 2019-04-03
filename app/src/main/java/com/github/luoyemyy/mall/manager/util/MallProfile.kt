package com.github.luoyemyy.mall.manager.util

import com.github.luoyemyy.config.devDevDevPro

object MallProfile {
    fun getApiUrl(): String {
        return devDevDevPro("http://192.168.0.135:8080/", "http://192.168.0.135:8080/").value()
    }
}