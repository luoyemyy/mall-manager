package com.github.luoyemyy.mall.manager.api

import com.github.luoyemyy.bus.Bus
import com.github.luoyemyy.mall.manager.util.BusEvent
import com.github.luoyemyy.mall.manager.util.LiveValues
import okhttp3.Interceptor
import okhttp3.Response

class AppInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().let {
            LiveValues.token?.let { token ->
                it.newBuilder().addHeader("token", token).build()
            } ?: it
        }
        val response = try {
            chain.proceed(request)
        } catch (e: Throwable) {
            if (!LiveValues.network) {
                Bus.post(BusEvent.ERROR_NETWORK)
            } else {
                Bus.post(BusEvent.ERROR_CONNECT)
            }
            throw RuntimeException(e)
        }
        if (!response.isSuccessful) {
            Bus.post(BusEvent.ERROR_API)
        }
        return response
    }
}