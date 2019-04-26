package com.github.luoyemyy.mall.manager.api

import com.github.luoyemyy.mall.manager.bean.OrderDetail
import com.github.luoyemyy.mall.manager.bean.OrderItem
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface OrderApi {

    @GET("admin/order/list")
    fun list(@Query("stateId") stateId: Int, @Query("page") page: Int): Single<ListResult<OrderItem>>

    @GET("admin/order/detail")
    fun detail(@Query("orderId") orderId: Long): Single<DataResult<OrderDetail>>

    @POST("admin/order/state")
    fun state(@Query("orderId") orderId: Long,
               @Query("expressCompany") expressCompany: String? = null,
               @Query("expressNo") expressNo: String? = null,
               @Query("refundMoney") refundMoney: Float = 0f): Single<DataResult<OrderDetail>>
}