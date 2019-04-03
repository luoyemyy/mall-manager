package com.github.luoyemyy.mall.manager.api

import com.github.luoyemyy.mall.manager.bean.Category
import com.github.luoyemyy.mall.manager.bean.Product
import com.github.luoyemyy.mall.manager.bean.Sort
import io.reactivex.Single
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface ProductApi {

    @GET("admin/category/list/valid")
    fun categoryList(): Single<ListResult<Category>>

    @GET("admin/product/list")
    fun list(@Query("categoryId") categoryId: Long, @Query("page") page: Int): Single<ListResult<Product>>

    @GET("admin/product/get")
    fun get(@Query("id") id: Long): Single<DataResult<Product>>

    @POST("admin/product/online")
    fun online(@Query("id") id: Long, @Query("online") online: Boolean): Single<ApiResult>

    @POST("admin/product/delete")
    fun delete(@Query("id") id: Long): Single<ApiResult>

    @POST("admin/product/aoe")
    fun aoe(@Body product: Product): Single<ApiResult>

    @POST("admin/product/sort")
    fun sort(@Body sort: List<Sort>): Single<ApiResult>



}