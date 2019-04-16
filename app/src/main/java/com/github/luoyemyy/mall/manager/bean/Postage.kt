package com.github.luoyemyy.mall.manager.bean

class Postage {
    var id: Long = 0
    var area: String? = null
    var price: Float = 0f
    var post: Int = 0

    var backPrice: Float = 0f
    var backPost: Int = 0

    fun priceStr(): String {
        return "￥$price"
    }

    fun showPrice(): Boolean {
        return post == 1
    }

    fun edit() {
        backPrice = price
        backPost = post
    }
}