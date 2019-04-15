package com.github.luoyemyy.mall.manager.bean

data class Postage(var id: Long = 0, var area: String? = null, var price: Float = 0f, var post: Int = 0) {

    fun priceStr(): String {
        return "￥$price"
    }
}