package com.github.luoyemyy.mall.manager.api

import com.github.luoyemyy.mall.manager.bean.OrderItem
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Query

interface OrderApi {


    @GET("admin/order/list")
    fun list(@Query("stateId") stateId: Int, @Query("page") page: Int): Single<ListResult<OrderItem>>


}