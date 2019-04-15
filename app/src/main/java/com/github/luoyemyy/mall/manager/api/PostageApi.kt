package com.github.luoyemyy.mall.manager.api

import com.github.luoyemyy.mall.manager.bean.Postage
import io.reactivex.Single
import retrofit2.http.Body
import retrofit2.http.GET

interface PostageApi {

    @GET("admin/postage/list")
    fun list(): Single<ListResult<Postage>>

    @GET("admin/postage/edit")
    fun edit(@Body list: List<Postage>): Single<ApiResult>


}