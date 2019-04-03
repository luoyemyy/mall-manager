package com.github.luoyemyy.mall.manager.util

import android.view.View
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.github.luoyemyy.mall.manager.R

object DataBindingAdapter {

    @JvmStatic
    @BindingAdapter("is_show")
    fun isShow(view: View, show: Boolean) {
        view.visibility = if (show) View.VISIBLE else View.GONE
    }

    @JvmStatic
    @BindingAdapter("is_hide")
    fun isHide(view: View, hide: Boolean) {
        view.visibility = if (hide) View.GONE else View.VISIBLE
    }

    @JvmStatic
    @BindingAdapter("is_invisible")
    fun isInvisible(view: View, invisible: Boolean) {
        view.visibility = if (invisible) View.INVISIBLE else View.VISIBLE
    }

    @JvmStatic
    @BindingAdapter("photo")
    fun photo(view: ImageView, url: String?) {
        Glide.with(view)
            .load(url)
            .placeholder(R.drawable.ic_placeholder_photo)
            .fallback(R.drawable.ic_default_photo)
            .error(R.drawable.ic_default_photo)
            .apply(RequestOptions.circleCropTransform())
            .into(view)
    }

    @JvmStatic
    @BindingAdapter("image")
    fun image(view: ImageView, url: String?) {
        Glide.with(view)
            .load(url)
            .fallback(R.drawable.ic_default_image)
            .error(R.drawable.ic_default_image)
            .into(view)
    }
}