package com.github.luoyemyy.mall.manager.bean

data class User(
    var id: Long = 0,
    var headImage: String? = null,
    var name: String? = null,
    var phone: String? = null,
    var gender: Int = 0,
    var token: String? = null,
    var roleId: Int = 0,
    var roleName: String? = null,
    var ossAk: String? = null,
    var ossSk: String? = null,
    var ossEp: String? = null,
    var ossBucket: String? = null
) {
    fun gender(): String {
        return if (gender == 1) "♂" else if (gender == 2) "♀" else ""
    }

    fun gender2(): String {
        return if (gender == 1) "男" else if (gender == 2) "女" else ""
    }
}