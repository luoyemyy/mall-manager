package com.github.luoyemyy.mall.manager.bean

import android.app.Application

class Hot {
    var id: Long = 0
    var image: String? = null
    var description: String? = null
    var sort: Int = 0
    var state: Int = 0
    var count: Int = 0

    /**
     * list
     */
    var enableSort: Boolean = false

    /**
     * aoe
     */
    val uploadImage = UploadImage()

    fun showImage(): String? {
        return uploadImage.localUrl ?: image
    }

    fun hasShowImage(): Boolean {
        return !showImage().isNullOrEmpty()
    }

    fun hasLocalImage(): Boolean {
        return !uploadImage.localUrl.isNullOrEmpty()
    }

    fun submitImage(app: Application, error: Int): String? {
        return if (hasLocalImage()) {
            uploadImage.uploadUrl(app, error)
        } else {
            image
        }
    }
}