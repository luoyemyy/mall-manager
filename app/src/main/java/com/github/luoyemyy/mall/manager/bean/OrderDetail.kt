package com.github.luoyemyy.mall.manager.bean

import java.util.*

class OrderDetail {
    var id: Long = 0
    var orderNo: String? = null
    var userId: Long = 0
    var state: Int = 0
    var money: Float = 0f
    var postage: Float = 0f
    var productCount: Int = 0
    var username: String? = null
    var phone: String? = null
    var address: String? = null
    var postcode: String? = null
    var createTime: String? = null
    var payTime: String? = null
    var deliverTime: String? = null
    var signTime: String? = null
    var wxPayId: String? = null
    var wxOrderId: String? = null
    var expressCompany: String? = null
    var expressNo: String? = null
    var cancelReason: String? = null
    var refuseOrderNo: String? = null
    var refuseWxNo: String? = null

    var deliverPathName: String? = null
    var deliverPathTime: String? = null
    var products: List<OrderProduct>? = null

    fun state(): String? {
        return null
    }

    fun productMoney(): String? {
        return null
    }

    fun postage(): String? {
        return null
    }

    fun money(): String? {
        return null
    }
}