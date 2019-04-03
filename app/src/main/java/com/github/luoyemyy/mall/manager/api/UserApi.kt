package com.github.luoyemyy.mall.manager.api

import com.github.luoyemyy.mall.manager.bean.Manager
import com.github.luoyemyy.mall.manager.bean.User
import io.reactivex.Single
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface UserApi {

    @GET("admin/login")
    fun login(@Query("phone") phone: String, @Query("password") password: String): Single<DataResult<User>>

    @GET("admin/user/list/admin")
    fun adminList(@Query("page") page: Int): Single<ListResult<Manager>>

    @GET("admin/user/list/manager")
    fun managerList(@Query("page") page: Int): Single<ListResult<Manager>>

    @POST("admin/user/add")
    fun add(@Query("phone") phone: String, @Query("password") password: String, @Query("roleId") roleId: Int): Single<AlertResult>

    @POST("admin/user/editRole")
    fun editRole(@Query("userId") userId: Long, @Query("roleId") roleId: Int): Single<ApiResult>

    @POST("admin/user/editState")
    fun editState(@Query("userId") userId: Long, @Query("state") state: Int): Single<ApiResult>

    @POST("admin/user/delete")
    fun delete(@Query("userId") userId: Long): Single<ApiResult>

    @POST("admin/user/passwordByAdmin")
    fun passwordByAdmin(@Query("userId") userId: Long, @Query("newPassword") newPassword: String): Single<ApiResult>

    @POST("admin/user/passwordBySelf")
    fun passwordBySelf(@Query("userId") userId: Long, @Query("oldPassword") oldPassword: String, @Query("newPassword") newPassword: String): Single<ApiResult>

    @POST("admin/user/editInfo")
    fun editInfo(@Query("userId") userId: Long, @Query("name") name: String?, @Query("gender") gender: Int, @Query("image") image: String?): Single<ApiResult>

}