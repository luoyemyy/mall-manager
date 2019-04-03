package com.github.luoyemyy.mall.manager.api

import com.github.luoyemyy.mall.manager.bean.Hot
import com.github.luoyemyy.mall.manager.bean.Product
import com.github.luoyemyy.mall.manager.bean.Sort
import io.reactivex.Single
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface HotApi {

    @GET("admin/hot/list")
    fun list(): Single<ListResult<Hot>>

    @GET("admin/product/list/hot")
    fun listProduct(@Query("hotId") hotId: Long): Single<ListResult<Product>>

    @POST("admin/hot/add")
    fun add(@Query("image") image: String, @Query("description") description: String): Single<ApiResult>

    @POST("admin/hot/edit")
    fun edit(@Query("id") id: Long, @Query("image") image: String, @Query("description") description: String): Single<ApiResult>

    @POST("admin/hot/state")
    fun state(@Query("id") id: Long, @Query("state") state: Int): Single<ApiResult>

    @POST("admin/hot/delete")
    fun delete(@Query("id") id: Long): Single<ApiResult>

    @POST("admin/hot/sort")
    fun sort(@Body sort: List<Sort>): Single<ApiResult>

    @POST("admin/hot/add/product")
    fun addProduct(@Query("id") id: Long, @Query("productIds") productIds: String): Single<ApiResult>

    @POST("admin/hot/delete/product")
    fun deleteProduct(@Query("id") id: Long, @Query("productId") productId: Long): Single<ApiResult>

}