package com.github.luoyemyy.mall.manager.util

import android.text.Editable
import android.text.TextWatcher

open class TextChangeAdapter : TextWatcher {

    var value: String? = null

    override fun afterTextChanged(s: Editable?) {
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
    }
}