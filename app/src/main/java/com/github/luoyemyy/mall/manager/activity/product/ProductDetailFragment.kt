package com.github.luoyemyy.mall.manager.activity.product

import android.app.Application
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.viewpager.widget.PagerAdapter
import com.github.luoyemyy.mall.manager.R
import com.github.luoyemyy.mall.manager.activity.base.BaseFragment
import com.github.luoyemyy.mall.manager.api.data
import com.github.luoyemyy.mall.manager.api.getProductApi
import com.github.luoyemyy.mall.manager.bean.Product
import com.github.luoyemyy.mall.manager.databinding.FragmentProductDetailBinding
import com.github.luoyemyy.mall.manager.databinding.FragmentProductDetailRecyclerBinding
import com.github.luoyemyy.mall.manager.util.DataBindingAdapter
import com.github.luoyemyy.mall.manager.util.MvpSimplePresenter
import com.github.luoyemyy.mall.manager.util.StaticAdapter
import com.github.luoyemyy.mall.manager.util.StaticPresenter
import com.github.luoyemyy.mvp.getPresenter
import com.github.luoyemyy.mvp.getRecyclerPresenter
import com.github.luoyemyy.mvp.recycler.LoadType
import com.github.luoyemyy.mvp.recycler.setLinearManager
import com.youth.banner.loader.ImageLoader

class ProductDetailFragment : BaseFragment() {

    private lateinit var mBinding: FragmentProductDetailBinding
    private lateinit var mPresenter: Presenter
    private lateinit var mImagePresenter: ImagePresenter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return FragmentProductDetailBinding.inflate(inflater, container, false).apply { mBinding = this }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        mPresenter = getPresenter()
        mImagePresenter = getRecyclerPresenter(this, Adapter())

        mPresenter.setDataObserver(this, Observer {
            mBinding.entity = it
        })

        mPresenter.setFlagObserver(this, Observer {
            when (it) {
                0 -> findNavController().navigateUp()
                1 -> {
                    mImagePresenter.setData(mPresenter.descImages)
                    mBinding.banner.setImages(mPresenter.swipeImages).start()
                }
            }
        })

        mBinding.recyclerView.apply {
            setLinearManager()
        }

        mBinding.banner.apply {
            setImageLoader(BannerAdapter())
        }

        mPresenter.loadInit(arguments)
    }

    inner class BannerAdapter : ImageLoader() {
        override fun displayImage(context: Context, path: Any, imageView: ImageView) {
            DataBindingAdapter.image(imageView, path.toString())
        }
    }

    inner class Adapter : StaticAdapter<String, FragmentProductDetailRecyclerBinding>(mBinding.recyclerView) {
        override fun getLayoutId(): Int {
            return R.layout.fragment_product_detail_recycler
        }
    }

    class ImagePresenter(var app: Application) : StaticPresenter<String>(app)

    class Presenter(var app: Application) : MvpSimplePresenter<Product>(app) {

        var swipeImages: List<String>? = null
        var descImages: List<String>? = null

        override fun loadData(loadType: LoadType, bundle: Bundle?) {
            val id = bundle?.getLong("productId", 0L) ?: let {
                flag.postValue(0)
                return
            }
            getProductApi().get(id).data { ok, value ->
                if (ok) {
                    data.postValue(value)
                    swipeImages = value?.swipeImages?.map { it.image ?: "" }?.filter { it.isNotEmpty() }
                    descImages = value?.descImages?.map { it.image ?: "" }?.filter { it.isNotEmpty() }
                    flag.postValue(1)
                }
            }
        }
    }
}