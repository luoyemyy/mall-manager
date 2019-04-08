package com.github.luoyemyy.mall.manager.activity.product

import android.annotation.SuppressLint
import android.app.Application
import android.os.Bundle
import android.view.*
import androidx.core.os.bundleOf
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.github.luoyemyy.bus.Bus
import com.github.luoyemyy.ext.hide
import com.github.luoyemyy.ext.show
import com.github.luoyemyy.ext.toList
import com.github.luoyemyy.ext.toast
import com.github.luoyemyy.mall.manager.R
import com.github.luoyemyy.mall.manager.activity.base.BaseFragment
import com.github.luoyemyy.mall.manager.activity.base.hideLoading
import com.github.luoyemyy.mall.manager.activity.base.showLoading
import com.github.luoyemyy.mall.manager.api.data
import com.github.luoyemyy.mall.manager.api.getProductApi
import com.github.luoyemyy.mall.manager.api.list
import com.github.luoyemyy.mall.manager.api.result
import com.github.luoyemyy.mall.manager.bean.Product
import com.github.luoyemyy.mall.manager.bean.ProductCategory
import com.github.luoyemyy.mall.manager.bean.ProductImage
import com.github.luoyemyy.mall.manager.databinding.FragmentProductAoeBinding
import com.github.luoyemyy.mall.manager.databinding.FragmentProductAoeImageBinding
import com.github.luoyemyy.mall.manager.util.*
import com.github.luoyemyy.mvp.getPresenter
import com.github.luoyemyy.mvp.getRecyclerPresenter
import com.github.luoyemyy.mvp.recycler.GridDecoration
import com.github.luoyemyy.mvp.recycler.LoadType
import com.github.luoyemyy.mvp.recycler.VH
import com.github.luoyemyy.mvp.recycler.setGridManager
import com.github.luoyemyy.mvp.runOnWorker

class ProductAoeFragment : BaseFragment(), View.OnClickListener {

    private lateinit var mBinding: FragmentProductAoeBinding
    private lateinit var mPresenter: Presenter
    private lateinit var mSwipeImagePresenter: SwipeImagePresenter
    private lateinit var mDescImagePresenter: DescImagePresenter
    private lateinit var mSwipeSortHelper: ItemTouchHelper
    private lateinit var mDescSortHelper: ItemTouchHelper

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return FragmentProductAoeBinding.inflate(inflater, container, false).apply { mBinding = this }.root
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.save, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        getInputs()
        mPresenter.save()
        return true
    }

    private fun getInputs() {
        mPresenter.swipeImages = mSwipeImagePresenter.getDataSet().dataList()
        mPresenter.descImages = mDescImagePresenter.getDataSet().dataList()
    }

    private fun getFloat(text: String): Float {
        return text.let {
            if (it.isEmpty()) {
                0f
            } else {
                it.toFloat()
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        mPresenter = getPresenter()
        mPresenter.setFlagObserver(this, Observer {
            when (it) {
                0 -> findNavController().navigateUp()
                1 -> {
                    //category
                    mBinding.layoutCategory.chips(mPresenter.category)
                    if (mPresenter.category.isNullOrEmpty()) {
                        mBinding.layoutCategory.hide()
                        mBinding.txtCategory.hide()
                    } else {
                        mBinding.layoutCategory.show()
                        mBinding.txtCategory.show()
                    }

                    //images
                    mSwipeImagePresenter.setData(mPresenter.swipeImages)
                    mDescImagePresenter.setData(mPresenter.descImages)
                }
            }
        })
        mPresenter.setDataObserver(this, Observer {
            mBinding.entity = it
        })
        mSwipeImagePresenter = getRecyclerPresenter(this, Adapter(1, mBinding.swipeImageRecyclerView))
        mDescImagePresenter = getRecyclerPresenter(this, Adapter(2, mBinding.descImageRecyclerView))
        mSwipeSortHelper = ItemTouchHelper(object : SortCallback() {
            override fun move(source: Int, target: Int): Boolean = mSwipeImagePresenter.move(source, target)
        }).apply {
            attachToRecyclerView(mBinding.swipeImageRecyclerView)
        }
        mDescSortHelper = ItemTouchHelper(object : SortCallback() {
            override fun move(source: Int, target: Int): Boolean = mDescImagePresenter.move(source, target)
        }).apply {
            attachToRecyclerView(mBinding.descImageRecyclerView)
        }

        mBinding.apply {
            edtName.observeValue {
                mPresenter.product?.name = it
            }
            edtDesc.observeValue {
                mPresenter.product?.description = it
            }
            edtActualPrice.limitMoney {
                mPresenter.product?.actualPrice = getFloat(it)
            }
            edtMarketPrice.apply {
                limitMoney {
                    mPresenter.product?.marketPrice = getFloat(it)
                }
                setKeyActionDone(requireActivity())
            }
            swipeImageRecyclerView.apply {
                setGridManager(3)
                setHasFixedSize(true)
                addItemDecoration(GridDecoration.create(requireContext(), 3, 1))
            }
            descImageRecyclerView.apply {
                setGridManager(3)
                setHasFixedSize(true)
                addItemDecoration(GridDecoration.create(requireContext(), 3, 1))
            }
            imgCoverPicker.setOnClickListener(this@ProductAoeFragment)
            imgCoverClear.setOnClickListener(this@ProductAoeFragment)
        }

        mPresenter.loadInit(arguments)
    }

    override fun onClick(v: View?) {
        when (v) {
            mBinding.imgCoverClear -> mPresenter.clearLocalCover()
            mBinding.imgCoverPicker -> pickerCover(requireContext()) {
                mPresenter.updateCover(it)
            }
            mBinding.imgCover -> preview(0, 0, v)
        }
    }

    inner class Adapter(val type: Int, recyclerView: RecyclerView) : StaticAdapter<ProductImage, FragmentProductAoeImageBinding>(recyclerView) {
        override fun getLayoutId(): Int {
            return R.layout.fragment_product_aoe_image
        }

        override fun getItemClickViews(binding: FragmentProductAoeImageBinding): Array<View> {
            return arrayOf(binding.imgPreview, binding.imgPicker, binding.imgClear)
        }

        override fun onItemClickListener(vh: VH<FragmentProductAoeImageBinding>, view: View?) {
            if (view == null) return
            val binding = vh.binding ?: return
            when (view) {
                binding.imgClear -> clear(type, vh.adapterPosition)
                binding.imgPreview -> preview(type, vh.adapterPosition, view)
                binding.imgPicker -> picker(type)
            }
        }

        @SuppressLint("ClickableViewAccessibility")
        override fun bindItemEvents(vh: VH<FragmentProductAoeImageBinding>) {
            vh.binding?.imgPreview?.setOnTouchListener { _, _ ->
                when (type) {
                    1 -> mSwipeSortHelper.startDrag(vh).let { true }
                    2 -> mDescSortHelper.startDrag(vh).let { true }
                    else -> false
                }
            }
        }
    }

    private fun clear(type: Int, position: Int) {
        when (type) {
            1 -> mSwipeImagePresenter.delete(position)
            2 -> mDescImagePresenter.delete(position)
        }
    }

    private fun preview(type: Int, position: Int, view: View) {
        val image = when (type) {
            0 -> mPresenter.product?.showCover()
            1 -> mSwipeImagePresenter.getDataSet().item(position)?.image()
            2 -> mDescImagePresenter.getDataSet().item(position)?.image()
            else -> null
        } ?: return
        previewImage(view, image)
    }

    private fun picker(type: Int) {

        when (type) {
            1 -> pickerImages(requireContext()) {
                mSwipeImagePresenter.add(it)
            }
            2 -> pickerDescImages(requireContext()) {
                mDescImagePresenter.add(it)
            }
        }
    }

    open class SwipeImagePresenter(var app: Application) : StaticPresenter<ProductImage>(app) {

        fun delete(position: Int) {
            getAdapterSupport()?.apply {
                getDataSet().remove(intArrayOf(position), getAdapter())
            }
        }

        fun add(list: List<String>?) {
            if (!list.isNullOrEmpty()) {
                getAdapterSupport()?.apply {
                    list.map { ProductImage(it) }.apply {
                        val last = getDataSet().dataList().lastOrNull { it.isImage() }
                        getDataSet().addDataAfter(last, this, getAdapter())
                    }
                }
            }
        }

        fun move(source: Int, target: Int): Boolean {
            if (source == target) return false
            if (getDataSet().item(target)?.isImage() == true) {
                getAdapterSupport()?.apply {
                    getDataSet().move(source, target, getAdapter())
                    return true
                }
            }
            return false
        }
    }

    class DescImagePresenter(app: Application) : SwipeImagePresenter(app)

    class Presenter(var app: Application) : MvpSimplePresenter<Product>(app) {
        var category: List<ProductCategory>? = null
        var product: Product? = null
        var swipeImages: List<ProductImage>? = null
        var descImages: List<ProductImage>? = null

        private var mOldCategoryIds: LongArray? = null

        override fun loadData(loadType: LoadType, bundle: Bundle?) {
            category = bundle?.getString("category")?.toList()
            val productId = bundle?.getLong("productId", 0L) ?: 0L
            if (productId == 0L) {
                add()
            } else {
                edit(productId)
            }
        }

        override fun reload(): Boolean {
            return false
        }

        private fun edit(id: Long) {
            showLoading()
            getProductApi().get(id).data { ok, value ->
                hideLoading()
                if (ok && value != null) {
                    product = value
                    swipeImages = mutableListOf<ProductImage>().apply {
                        value.swipeImages?.forEach {
                            add(it)
                        }
                        add(ProductImage(1))
                    }
                    descImages = mutableListOf<ProductImage>().apply {
                        value.descImages?.forEach {
                            add(it)
                        }
                        add(ProductImage(1))
                    }
                    updateCategory()
                    updateProduct()
                    flag.postValue(1)
                } else {
                    flag.postValue(0)
                }
            }
        }

        private fun add() {
            product = Product()
            updateCategory()
            updateProduct()
            swipeImages = listOf(ProductImage(1))
            getProductApi().template().list { ok, value ->
                val list = mutableListOf<ProductImage>()
                if (ok) {
                    value?.apply {
                        value.forEach {
                            it.type = 0
                            it.id = 0
                        }
                        list += value
                    }
                }
                list += ProductImage(1)
                flag.postValue(1)
                descImages = list
                flag.postValue(1)
            }
        }

        private fun updateCategory() {
            mOldCategoryIds = product?.categoryIds?.toLongArray()
            product?.categoryIds?.apply {
                category?.forEach {
                    if (contains(it.id)) {
                        it.selected = true
                    }
                }
            }
        }

        private fun updateProduct() {
            data.postValue(product)
        }

        fun clearLocalCover() {
            product?.uploadCover?.clear()
            updateProduct()
        }

        fun updateCover(image: String?) {
            product?.uploadCover?.localUrl = image
            updateProduct()
        }

        fun save() {
            val p = product ?: return
            p.categoryIds = category?.filter { it.selected }?.map { it.id }
            if (p.id == 0L) {
                p.online = true
            }
            if (p.name.isNullOrEmpty()) {
                app.toast(R.string.product_name_hint)
                return
            }
            if (p.description.isNullOrEmpty()) {
                app.toast(R.string.product_desc_hint)
                return
            }
            if (p.actualPrice == 0f) {
                app.toast(R.string.product_actual_price_hint)
                return
            }
            if (p.marketPrice == 0f) {
                app.toast(R.string.product_market_price_hint)
                return
            }
            if (p.showCover().isNullOrEmpty()) {
                app.toast(R.string.product_cover_image_hint)
                return
            }
            if (swipeImages.isNullOrEmpty()) {
                app.toast(R.string.product_swipe_image_hint)
                return
            }
            showLoading()
            runOnWorker {
                p.coverImage = p.submitCover(app, R.string.product_upload_cover_error) ?: let {
                    hideLoading()
                    return@runOnWorker
                }
                swipeImages?.forEachIndexed { index, productImage ->
                    if (productImage.needUpload()) {
                        productImage.image = Oss.upload(app, productImage.localImage)
                        if (productImage.image.isNullOrEmpty()) {
                            app.getString(R.string.product_upload_swipe_error, index + 1).apply {
                                app.toast(message = this)
                            }
                            hideLoading()
                            return@runOnWorker
                        } else {
                            productImage.uploadImage = true
                        }
                    }
                }
                descImages?.forEachIndexed { index, productImage ->
                    if (productImage.needUpload()) {
                        productImage.image = Oss.upload(app, productImage.localImage)
                        if (productImage.image.isNullOrEmpty()) {
                            app.getString(R.string.product_upload_desc_error, index + 1).apply {
                                app.toast(message = this)
                            }
                            hideLoading()
                            return@runOnWorker
                        } else {
                            productImage.uploadImage = true
                        }
                    }
                }
                p.swipeImages = swipeImages?.filter { it.isImage() }
                p.descImages = descImages?.filter { it.isImage() }
                getProductApi().aoe(p).result {
                    hideLoading()
                    if (it) {
                        Bus.post(BusEvent.PRODUCT_AOE, extra = bundleOf("oldIds" to mOldCategoryIds, "newIds" to p.categoryIds?.toLongArray()))
                        flag.postValue(0)
                        Bus.post(BusEvent.CLEAR_IMAGE_CACHE)
                    }
                }
            }
        }
    }
}