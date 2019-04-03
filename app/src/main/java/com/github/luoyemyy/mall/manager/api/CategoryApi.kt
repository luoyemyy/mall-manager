package com.github.luoyemyy.mall.manager.api

import com.github.luoyemyy.mall.manager.bean.Category
import com.github.luoyemyy.mall.manager.bean.Sort
import io.reactivex.Single
import retrofit2.http.*

interface CategoryApi {

    @GET("admin/category/list")
    fun list(): Single<ListResult<Category>>

    @POST("admin/category/add")
    fun add(@Query("name") name: String): Single<ApiResult>

    @POST("admin/category/edit")
    fun edit(@Query("id") id: Long, @Query("name") name: String): Single<ApiResult>

    @POST("admin/category/state")
    fun state(@Query("id") id: Long, @Query("state") state: Int): Single<ApiResult>

    @POST("admin/category/delete")
    fun delete(@Query("id") id: Long): Single<ApiResult>

    @POST("admin/category/sort")
    fun sort(@Body sort: List<Sort>): Single<ApiResult>

}