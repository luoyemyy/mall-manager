package com.github.luoyemyy.mall.manager.util

import android.app.Activity
import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.text.Editable
import android.util.Pair
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import androidx.appcompat.widget.PopupMenu
import androidx.core.view.ViewCompat
import androidx.core.view.forEachIndexed
import androidx.core.view.plusAssign
import androidx.fragment.app.Fragment
import com.github.luoyemyy.ext.hideKeyboard
import com.github.luoyemyy.mall.manager.R
import com.github.luoyemyy.mall.manager.bean.ProductCategory
import com.github.luoyemyy.picker.ImagePicker
import com.github.luoyemyy.picker.preview.PreviewActivity
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import java.lang.reflect.Method
import kotlin.math.abs

fun EditText.observeValue(outValue: (String) -> Unit) {
    addTextChangedListener(object : TextChangeAdapter() {
        override fun afterTextChanged(s: Editable?) {
            if (s == null) return
            outValue(s.toString())
        }
    })
}

fun EditText.limitMoney(outValue: (String) -> Unit = {}) {
    addTextChangedListener(object : TextChangeAdapter() {
        override fun afterTextChanged(s: Editable?) {
            if (s == null) return
            val str = s.toString()
            if (str == ".") { //禁止第一个字符为.
                this@limitMoney.setText("")
                outValue("")
                return
            }
            val a = str.split(".")
            if (a.size == 2 && a[1].length > 2) { //小数部分不能超过2位
                val reset = a[0] + "." + a[1].substring(0, 2)
                this@limitMoney.setText(reset)
                this@limitMoney.setSelection(reset.length)
                outValue(reset)
            } else {
                outValue(str)
            }
        }
    })
}

fun EditText.setKeyActionDone(activity: Activity, callback: (String) -> Unit = {}) {
    setOnEditorActionListener { _, actionId, _ ->
        if (actionId == EditorInfo.IME_ACTION_DONE) {
            clearFocus()
            activity.hideKeyboard()
            callback(text.toString())
        }
        return@setOnEditorActionListener true
    }
}

fun EditText.enableSubmit(enableView: View, vararg otherEditText: EditText?) {
    val list = mutableListOf<TextChangeAdapter>()
    listOf(this, *otherEditText).forEach { edt ->
        edt?.apply {
            val adapter = object : TextChangeAdapter() {
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    value = s?.toString()
                    enableView.isEnabled = list.all { !it.value.isNullOrEmpty() }
                }
            }
            list.add(adapter)
            addTextChangedListener(adapter)
        }
    }
}

fun getMethod(obj: Any, name: String, vararg args: Class<*>): Method {
    return obj::class.java.getMethod(name, *args).apply {
        isAccessible = true
    }
}

fun getField(obj: Any, name: String): Any {
    return obj::class.java.getDeclaredField(name).apply {
        isAccessible = true
    }.get(obj)
}

fun PopupMenu.showAnchor(anchor: View, touchX: Int, touchY: Int) {
    val location = intArrayOf(0, 0)
    anchor.getLocationInWindow(location)
    val x = touchX - location[0]
    val y = touchY - location[1] - anchor.height

    val popup = getField(this, "mPopup")
    getMethod(popup, "show", Int::class.java, Int::class.java).invoke(popup, x, y)
}

fun popupMenu(context: Context, anchor: View, menuId: Int, x: Int, y: Int, listener: (Int) -> Unit): Boolean {
    PopupMenu(context, anchor).apply {
        inflate(menuId)
        setOnMenuItemClickListener { menuItem ->
            listener(menuItem.itemId)
            true
        }
        showAnchor(anchor, x, y)
    }
    return true
}

fun ChipGroup.chips(chips: List<ProductCategory>?) {
    if (chips.isNullOrEmpty()) {
        removeAllViews()
    } else {
        val i = chips.size - childCount
        if (i > 0) {
            val inflater = LayoutInflater.from(context)
            for (x in 0 until i) {
                this += inflater.inflate(R.layout.layout_chip_label, this, false)
            }
        } else if (i < 0) {
            removeViews(0, abs(i))
        }
        forEachIndexed { index, view ->
            val chip = chips[index]
            (view as? Chip)?.apply {
                text = chip.name
                isChecked = chip.selected
                setOnCheckedChangeListener { _, isChecked ->
                    chip.selected = isChecked
                }
            }
        }
    }
}

fun Fragment.previewImage(view: View, url: String) {
    val shareName = "${view.hashCode()}_image"
    ViewCompat.setTransitionName(view, shareName)

    startActivity(Intent(requireContext(), PreviewActivity::class.java).apply {
        putExtra("shareName", shareName)
        putExtra("image", url)
    }, ActivityOptions.makeSceneTransitionAnimation(requireActivity(), Pair(view, shareName)).toBundle())
}

fun Fragment.pickerPhoto(context: Context, callback: (String) -> Unit) {
    ImagePicker.create(context.packageName).albumAndCamera().maxSelect(1).cropByPercent().compress(100).build()
        .picker(this) {
            it?.apply {
                if (isNotEmpty()) {
                    callback(it[0])
                }
            }
        }
}

fun Fragment.pickerCover(context: Context, callback: (String) -> Unit) {
    ImagePicker.create(context.packageName).albumAndCamera().maxSelect(1).cropByPercent().compress(200).build()
        .picker(this) {
            it?.apply {
                if (isNotEmpty()) {
                    callback(it[0])
                }
            }
        }
}

fun Fragment.pickerImages(context: Context, callback: (List<String>?) -> Unit) {
//    ImagePicker.create(context.packageName).albumAndCamera().maxSelect(9).cropByPercent(0.8f, 1f, true).compress(360).build().picker(this) {
    ImagePicker.create(context.packageName).albumAndCamera().maxSelect(99).compress(750).build().picker(this) {
        callback(it)
    }
}

fun Fragment.pickerDescImages(context: Context, callback: (List<String>?) -> Unit) {
//    ImagePicker.create(context.packageName).albumAndCamera().maxSelect(9).cropByPercent(0.8f, 0.55f, true, false).compress(360).build().picker(this) {
    ImagePicker.create(context.packageName).albumAndCamera().maxSelect(99).compress(750).build().picker(this) {
        callback(it)
    }
}

fun Fragment.pickerImage(context: Context, callback: (String) -> Unit) {
    ImagePicker.create(context.packageName).albumAndCamera().maxSelect(1).cropByPercent(0.8f, 0.55f, true).compress(360)
        .build().picker(this) {
        it?.apply {
            if (isNotEmpty()) {
                callback(it[0])
            }
        }
    }
}