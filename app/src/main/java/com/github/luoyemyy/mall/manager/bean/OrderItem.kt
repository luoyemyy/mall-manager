package com.github.luoyemyy.mall.manager.bean

import android.app.Application
import com.github.luoyemyy.mall.manager.R

class OrderItem {
    var orderId: Long = 0
    var state: Int = 0
    var username: String? = null
    var phone: String? = null
    var address: String? = null
    var money: Float = 0f
    var count: Int = 0
    var date: String? = null

    var nameAndPhone: String? = null
    var moneyAndCount: String? = null
    var stateName: String? = null

    fun map(app: Application) {
        nameAndPhone = "$username,$phone"
        moneyAndCount = app.getString(R.string.order_item_count, count.toString(), money.toString())
        stateName = app.resources.getStringArray(R.array.order_type)[state]
    }

}