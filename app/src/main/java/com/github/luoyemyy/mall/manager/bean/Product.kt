package com.github.luoyemyy.mall.manager.bean

import android.app.Application

class Product {
    var id: Long = 0
    var coverImage: String? = null      //产品封面
    var name: String? = null            //产品名称
    var description: String? = null     //产品描述
    var marketPrice: Float = 0f         //市场价格
    var actualPrice: Float = 0f         //实际价格
    var stock: Int = 0                  //库存
    var online: Boolean = false         //是否上架
    var sort: Int = 0                   //排序号
    var swipeImages: List<ProductImage>? = null //产品滑动展示图片
    var descImages: List<ProductImage>? = null //产品描述图片
    var categoryIds: List<Long>? = null //分类id

    /**
     *  list & sort
     */
    fun actualPrice(): String {
        return "￥$actualPrice"
    }

    fun marketPrice(): String {
        return "￥$marketPrice"
    }

    fun showMarketPrice(): Boolean {
        return actualPrice != marketPrice
    }

    /**
     * aoe
     */
    val uploadCover = UploadImage()

    fun showCover(): String? {
        return uploadCover.localUrl ?: coverImage
    }

    fun hasShowCover(): Boolean {
        return !showCover().isNullOrEmpty()
    }

    fun hasLocalCover(): Boolean {
        return !uploadCover.localUrl.isNullOrEmpty()
    }

    fun submitCover(app: Application, error: Int): String? {
        return if (hasLocalCover()) {
            uploadCover.uploadUrl(app, error)
        } else {
            coverImage
        }
    }

    private fun formatPrice(price: Float): String? {
        return if (price == 0f) null else {
            val m = price.toInt()
            if (m.toFloat() == price) {
                m.toString()
            } else {
                price.toString()
            }
        }
    }

    fun marketPriceStr(): String? {
        return formatPrice(marketPrice)
    }

    fun actualPriceStr(): String? {
        return formatPrice(actualPrice)
    }

    /**
     * picker
     */

    var selected: Boolean = false
}