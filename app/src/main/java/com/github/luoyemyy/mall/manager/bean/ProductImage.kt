package com.github.luoyemyy.mall.manager.bean

import android.app.Application
import androidx.annotation.WorkerThread
import com.github.luoyemyy.ext.toast
import com.github.luoyemyy.mall.manager.util.Oss

class ProductImage() {
    var id: Long = 0
    var image: String? = null


    var type: Int = 0  // 0 picker 1 2 3 4 image
    var localImage: String? = null
    var uploadImage: Boolean = false

    //template
    var sort: Int = 0

    fun isImage(): Boolean {
        return type > 0
    }

    fun image(): String? {
        return localImage ?: image
    }

    fun needUpload(): Boolean {
        return isImage() && !uploadImage && !localImage.isNullOrEmpty()
    }

    constructor(type: Int) : this() {
        this.type = type
    }

    constructor(type: Int, localImage: String?) : this() {
        this.type = type
        this.localImage = localImage
    }

    @WorkerThread
    fun tryUpload(app: Application, failToast: String): Boolean {
        return if (needUpload()) {
            Oss.upload(app, localImage)?.let {
                uploadImage = true
                image = it
                true
            } ?: let {
                app.toast(message = failToast)
                false
            }
        } else {
            true
        }
    }
}