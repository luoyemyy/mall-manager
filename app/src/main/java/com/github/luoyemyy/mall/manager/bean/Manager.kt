package com.github.luoyemyy.mall.manager.bean

class Manager {
    var id: Long = 0
    var headImage: String? = null
    var name: String? = null
    var phone: String? = null
    var gender: Int = 0
    var roleId: Int = 0
    var roleName: String? = null
    var state: Int = 0

    fun gender(): String {
        return if (gender == 1) "♂" else if (gender == 2) "♀" else ""
    }
}