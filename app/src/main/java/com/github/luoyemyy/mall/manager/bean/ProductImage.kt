package com.github.luoyemyy.mall.manager.bean

class ProductImage() {
    var id: Long = 0
    var image: String? = null


    var type: Int = 0  // 0 image 1 picker
    var localImage: String? = null
    var uploadImage: Boolean = false

    //template
    var sort: Int = 0

    fun isImage(): Boolean {
        return type == 0
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

    constructor(localImage: String?) : this() {
        this.localImage = localImage
    }
}