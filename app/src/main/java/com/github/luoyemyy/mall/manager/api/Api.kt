package com.github.luoyemyy.mall.manager.api

import com.github.luoyemyy.api.AbstractApiManager
import com.github.luoyemyy.mall.manager.util.MallProfile
import okhttp3.OkHttpClient

class Api : AbstractApiManager() {
    override fun baseUrl(): String {
        return MallProfile.getApiUrl()
    }

    override fun client(): OkHttpClient.Builder {
        return super.client().addInterceptor(AppInterceptor())
    }

    companion object {
        fun changeProfile() {
            AbstractApiManager.mRetrofit = Api().getRetrofit()
        }
    }
}

fun getUserApi(): UserApi = Api().getApi()

fun getCategoryApi(): CategoryApi = Api().getApi()

fun getProductApi(): ProductApi = Api().getApi()

fun getHotApi(): HotApi = Api().getApi()

fun getPostageApi(): PostageApi = Api().getApi()

