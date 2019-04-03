package com.github.luoyemyy.mall.manager.util

import android.app.Application
import android.os.Bundle
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView
import com.github.luoyemyy.mall.manager.BR
import com.github.luoyemyy.mvp.AbstractPresenter
import com.github.luoyemyy.mvp.recycler.*

abstract class MvpSimplePresenter<T>(app: Application) : AbstractPresenter<T>(app) {
    override fun delayInitTime(): Long {
        return 0L
    }
}

abstract class MvpRecyclerPresenter<T>(app: Application) : AbstractRecyclerPresenter<T>(app) {
    override fun delayInitTime(): Long {
        return 0L
    }
}

abstract class MvpSingleAdapter<T, BIND : ViewDataBinding>(recyclerView: RecyclerView) : AbstractSingleRecyclerAdapter<T, BIND>(recyclerView) {
    override fun bindContentViewHolder(binding: BIND, content: T, position: Int) {
        binding.setVariable(BR.entity, content)
        binding.executePendingBindings()
    }
}

abstract class MvpMultiAdapter(recyclerView: RecyclerView) : AbstractMultiRecyclerAdapter(recyclerView)

abstract class StaticAdapter<T, BIND : ViewDataBinding>(recyclerView: RecyclerView) : MvpSingleAdapter<T, BIND>(recyclerView) {

    override fun enableEmpty(): Boolean {
        return false
    }

    override fun enableLoadMore(): Boolean {
        return false
    }
}

abstract class StaticPresenter<T>(app: Application) : AbstractRecyclerPresenter<T>(app) {

    private var mList: List<T>? = null

    fun setData(list: List<T>?) {
        mList = list
        if (isInitialized()) {
            loadRefresh()
        } else {
            loadInit()
        }
    }

    override fun loadData(loadType: LoadType, paging: Paging, bundle: Bundle?, search: String?): List<T>? {
        return mList
    }
}