package com.github.luoyemyy.mall.manager.bean

import android.app.Application
import com.github.luoyemyy.ext.toast
import com.github.luoyemyy.mall.manager.util.Oss

class UploadImage {

    var upload: Boolean = false
    var localUrl: String? = null
    var uploadUrl: String? = null

    fun clear() {
        upload = false
        localUrl = null
        uploadUrl = null
    }

    fun uploadUrl(app: Application, error: Int): String? {
        return if (!upload) {
            uploadUrl = Oss.upload(app, localUrl)?.apply {
                upload = true
            } ?: let {
                app.toast(error)
                null
            }
            uploadUrl
        } else {
            uploadUrl
        }
    }
}