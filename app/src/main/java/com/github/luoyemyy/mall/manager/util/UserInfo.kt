package com.github.luoyemyy.mall.manager.util

import android.content.Context
import com.github.luoyemyy.config.editor
import com.github.luoyemyy.config.spf
import com.github.luoyemyy.ext.toJsonString
import com.github.luoyemyy.ext.toObject
import com.github.luoyemyy.mall.manager.bean.User

object UserInfo {

    fun exitLogin(context: Context) {
        context.editor().clear().apply()
    }

    fun saveUser(context: Context, user: User) {
        LiveValues.token = user.token
        LiveValues.userId = user.id
        context.editor()
            .putString("user", user.toJsonString())
            .putLong("userId", user.id)
            .putString("token", user.token)
            .putString("roleName", user.roleName)
            .putInt("roleId", user.roleId)
            .putString("ossAk", user.ossAk)
            .putString("ossSk", user.ossSk)
            .putString("ossEp", user.ossEp)
            .putString("ossBucket", user.ossBucket).apply()
    }

    fun getUser(context: Context): User? {
        return context.spf().getString("user", null)?.toObject()
    }

    fun getUserId(context: Context): Long {
        return context.spf().getLong("userId", 0L)
    }

    fun getToken(context: Context): String? {
        return context.spf().getString("token", null)
    }

    fun getRoleId(context: Context): Int {
        return context.spf().getInt("roleId", 0)
    }

    fun getRoleName(context: Context): String? {
        return context.spf().getString("roleName", null)
    }

    fun getOssAk(context: Context): String? {
        return context.spf().getString("ossAk", null)
    }

    fun getOssSk(context: Context): String? {
        return context.spf().getString("ossSk", null)
    }

    fun getOssEp(context: Context): String? {
        return context.spf().getString("ossEp", null)
    }

    fun getOssBucket(context: Context): String? {
        return context.spf().getString("ossBucket", null)
    }

    fun updateName(context: Context, name: String) {
        getUser(context)?.apply {
            this.name = name
            context.editor().putString("user", this.toJsonString()).apply()
        }
    }

    fun updateHeadImage(context: Context, headImage: String) {
        getUser(context)?.apply {
            this.headImage = headImage
            context.editor().putString("user", this.toJsonString()).apply()
        }
    }

    fun updateGender(context: Context, gender: Int) {
        getUser(context)?.apply {
            this.gender = gender
            context.editor().putString("user", this.toJsonString()).apply()
        }
    }
}