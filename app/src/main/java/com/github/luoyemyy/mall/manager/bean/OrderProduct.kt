package com.github.luoyemyy.mall.manager.bean

class OrderProduct {
    var id: Long = 0
    var coverImage: String? = null
    var name: String? = null
    var price: Float = 0f
    var count: Int = 0

    fun price(): String {
        return "￥$price"
    }

    fun count(): String {
        return "x$count"
    }
}