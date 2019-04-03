package com.github.luoyemyy.mall.manager.util

import com.github.luoyemyy.bus.Bus
import com.github.luoyemyy.bus.BusMsg
import com.github.luoyemyy.ext.toast
import com.github.luoyemyy.mall.manager.R
import com.github.luoyemyy.mall.manager.app.App

object BusEvent {

    const val LOADING_START = "loading.start"
    const val LOADING_END = "loading.end"
    const val ERROR_NETWORK = "network.error"
    const val ERROR_API = "api.error"
    const val ERROR_CONNECT = "connect.error"
    const val LOGIN_EXPIRE = "token.expire"
    const val USER_EDIT_NAME = "edit.user.name"
    const val MANAGER_ADD = "add.manager"
    const val ADMIN_ADD = "add.admin"
    const val CATEGORY_AOE = "aoe.category"
    const val PRODUCT_EDIT = "edit.product"
    const val PRODUCT_AOE = "aoe.product"
    const val PRODUCT_SORT = "sort.product"
    const val PRODUCT_ONLINE = "online.product"
    const val PRODUCT_DETAIL = "detail.product"
    const val PRODUCT_DELETE = "delete.product"
    const val PRODUCT_PICKER = "picker.product"
    const val PRODUCT_PICKER_SELECT = "select.picker.product"
    const val HOT_AOE = "aoe.hot"
    const val HOT_COUNT = "count.hot@REPLACE"

    fun appEvent(app: App) {
        Bus.register(AppEventCallback(app, BusEvent.ERROR_API))
        Bus.register(AppEventCallback(app, BusEvent.ERROR_CONNECT))
        Bus.register(AppEventCallback(app, BusEvent.ERROR_NETWORK))
    }

    class AppEventCallback(private val app: App, private val key: String) : Bus.Callback {
        override fun busResult(event: String, msg: BusMsg) {
            when (event) {
                BusEvent.ERROR_CONNECT -> app.toast(R.string.connect_error)
                BusEvent.ERROR_NETWORK -> app.toast(R.string.network_error)
                BusEvent.ERROR_API -> (msg.stringValue ?: app.getString(R.string.api_error)).apply {
                    app.toast(message = this)
                }
            }
        }

        override fun interceptEvent(): String {
            return key
        }

    }
}