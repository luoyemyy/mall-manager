package com.github.luoyemyy.mall.manager.util

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.annotation.WorkerThread
import com.alibaba.sdk.android.oss.OSSClient
import com.alibaba.sdk.android.oss.common.auth.OSSCustomSignerCredentialProvider
import com.alibaba.sdk.android.oss.common.utils.OSSUtils
import com.alibaba.sdk.android.oss.model.PutObjectRequest
import java.io.File

object Oss {

    @Volatile
    private var single: OSSClient? = null

    private var url: String? = null

    @JvmStatic
    fun init(app: Application) {
        if (single == null) {
            synchronized(Oss::class) {
                if (single == null) {
                    val ep = UserInfo.getOssEp(app)
                    val ak = UserInfo.getOssAk(app)
                    val sk = UserInfo.getOssSk(app)
                    url = "http://${UserInfo.getOssBucket(app)}.$ep/"
                    single = OSSClient(app, ep, object : OSSCustomSignerCredentialProvider() {
                        override fun signContent(content: String?): String {
                            return OSSUtils.sign(ak, sk, content)
                        }
                    })
                }
            }
        }
    }

    @JvmStatic
    fun getInstance(): OSSClient {
        return single ?: let {
            throw NullPointerException("call after OSSClient.init(app)")
        }
    }

    fun url(name: String): String {
        return url?.let {
            it + name
        } ?: let {
            throw NullPointerException("call after OSSClient.init(app)")
        }
    }

    private fun getFileName(path: String): String {
        val index = path.lastIndexOf(File.separator)
        return if (index == -1) {
            path
        } else {
            path.substring(index + 1)
        }
    }

    @WorkerThread
    fun upload(context: Context, path: String?): String? {
        if (path == null) return null
        val name = getFileName(path)
        return try {
            getInstance().putObject(PutObjectRequest(UserInfo.getOssBucket(context), name, path))
            Log.i("Oss", "upload:success,path=$path")
            url(name)
        } catch (e: Throwable) {
            Log.e("Oss", "upload:failure,path=$path", e)
            null
        }
    }
}