package com.github.luoyemyy.mall.manager.bean

import android.content.Context
import com.github.luoyemyy.mall.manager.R

/**
 * 订单逻辑
 * 状态：
 * 0 未支付 1 已支付，待确认 2 支付成功，待发货 3 发货中 4 运输中 5 已签收，交易完成 6 取消订单，待审核  7 退货，待审核 8 退货中 9 退款中 10 已取消
 * 状态流转：
 * 0-> 客户支付 1；客户取消 10
 * 1-> 商户确认支付 2；客户取消 6
 * 2-> 商户备货 3
 * 3-> 商户发货 4
 * 4-> 客户确认收货 5
 * 5-> 客户申请退货 7
 * 6-> 商户审核退款 9
 * 7-> 商户审核退货 8
 * 8-> 商户确认已退货 9
 * 9-> 商户确认已退款 10
 *
 */
class OrderState(var id: Int = 0, var name: String? = null) {

    companion object {
        fun all(app: Context): List<OrderState> {
            return app.resources.getStringArray(R.array.order_type).mapIndexed { index, s ->
                OrderState(index, s)
            }
        }
    }
}