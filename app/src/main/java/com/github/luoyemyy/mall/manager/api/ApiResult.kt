package com.github.luoyemyy.mall.manager.api

import android.util.Log
import com.github.luoyemyy.bus.Bus
import com.github.luoyemyy.mall.manager.util.BusEvent
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

open class ApiResult(var code: Int = -1, var msg: String? = null)

class DataResult<T>(var data: T? = null) : ApiResult()

class ListResult<T>(var list: List<T>? = null) : ApiResult()

class AlertResult(var alert: String? = null) : ApiResult()

private fun checkHasError(error: Throwable?, value: ApiResult?): Boolean {
    return (error != null || value == null).apply {
        if (this) {
            Log.e("Api", "result:  ", error)
        }
    }
}

private fun checkCode(value: ApiResult): Boolean {
    if (value.code == 5) {
        Bus.post(BusEvent.LOGIN_EXPIRE)
        return false
    }
    if (value.code != 0) {
        Log.e("Api", "result:  code=${value.code},msg=${value.msg}")
        Bus.post(BusEvent.ERROR_API, stringValue = value.msg)
    }
    return true
}

fun Single<ApiResult>.result(result: (ok: Boolean) -> Unit): Disposable {
    return subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread()).subscribe { value, error ->
        if (checkHasError(error, value)) {
            result(false)
        } else {
            if (checkCode(value)) {
                result(value.code == 0)
            }
        }
    }
}

fun <T> Single<DataResult<T>>.data(result: (ok: Boolean, value: T?) -> Unit): Disposable {
    return subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread()).subscribe { value, error ->
        if (checkHasError(error, value)) {
            result(false, null)
        } else {
            if (checkCode(value)) {
                result(value.code == 0, value.data)
            }
        }
    }
}

fun <T> Single<ListResult<T>>.list(result: (ok: Boolean, value: List<T>?) -> Unit): Disposable {
    return subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread()).subscribe { value, error ->
        if (checkHasError(error, value)) {
            result(false, null)
        } else {
            if (checkCode(value)) {
                result(value.code == 0, value.list)
            }
        }
    }
}

fun Single<AlertResult>.alert(result: (ok: Boolean, value: String?) -> Unit): Disposable {
    return subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread()).subscribe { value, error ->
        if (checkHasError(error, value)) {
            result(false, null)
        } else {
            if (checkCode(value)) {
                result(value.code == 0, value.alert)
            }
        }
    }
}

