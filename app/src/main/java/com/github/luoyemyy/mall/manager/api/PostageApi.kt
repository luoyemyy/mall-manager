package com.github.luoyemyy.mall.manager.api

import com.github.luoyemyy.mall.manager.bean.Postage
import io.reactivex.Single
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface PostageApi {

    @GET("admin/postage/list")
    fun list(): Single<ListResult<Postage>>

    @POST("admin/postage/edit")
    fun edit(@Body list: List<Postage>): Single<ApiResult>


}