package com.github.luoyemyy.mall.manager.activity.postage

import android.app.Application
import android.os.Bundle
import android.view.*
import androidx.navigation.fragment.findNavController
import com.github.luoyemyy.ext.confirm
import com.github.luoyemyy.mall.manager.R
import com.github.luoyemyy.mall.manager.activity.base.BaseFragment
import com.github.luoyemyy.mall.manager.api.getPostageApi
import com.github.luoyemyy.mall.manager.api.getUserApi
import com.github.luoyemyy.mall.manager.api.list
import com.github.luoyemyy.mall.manager.api.result
import com.github.luoyemyy.mall.manager.bean.Manager
import com.github.luoyemyy.mall.manager.bean.Postage
import com.github.luoyemyy.mall.manager.databinding.FragmentPostageRecyclerBinding
import com.github.luoyemyy.mall.manager.databinding.LayoutRefreshRecyclerBinding
import com.github.luoyemyy.mall.manager.util.MvpRecyclerPresenter
import com.github.luoyemyy.mall.manager.util.MvpSingleAdapter
import com.github.luoyemyy.mall.manager.util.Role
import com.github.luoyemyy.mvp.getRecyclerPresenter
import com.github.luoyemyy.mvp.recycler.*

class PostageFragment : BaseFragment() {

    private lateinit var mBinding: LayoutRefreshRecyclerBinding
    private lateinit var mPresenter: Presenter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return LayoutRefreshRecyclerBinding.inflate(inflater, container, false).apply { mBinding = this }.root
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.add, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        findNavController().navigate(R.id.action_managerFragment_to_managerAddFragment)
        return true
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        mPresenter = getRecyclerPresenter(this, Adapter())
        mBinding.recyclerView.apply {
            setGridManager(3)
            addItemDecoration(GridDecoration.create(requireContext(), 3))
            setHasFixedSize(true)
        }
        mBinding.swipeRefreshLayout.setOnRefreshListener { mPresenter.loadRefresh() }

        mPresenter.loadInit()
    }

    inner class Adapter : MvpSingleAdapter<Postage, FragmentPostageRecyclerBinding>(mBinding.recyclerView) {

        override fun getLayoutId(): Int {
            return R.layout.fragment_postage_recycler
        }

        override fun setRefreshState(refreshing: Boolean) {
            mBinding.swipeRefreshLayout.isRefreshing = refreshing
        }

        override fun getItemClickViews(binding: FragmentPostageRecyclerBinding): Array<View> {
            return arrayOf(binding.switchEnable)
        }

        override fun onItemClickListener(vh: VH<FragmentPostageRecyclerBinding>, view: View?) {
            val isChecked = vh.binding?.switchEnable?.isChecked ?: false
            val msg = if (isChecked) R.string.postage_enable else R.string.postage_disable
            requireActivity().confirm(messageId = msg, cancelButtonId = R.string.cancel, cancelCallback = {
                notifyItemChanged(vh.adapterPosition, !isChecked)
            }, okButtonId = R.string.submit) {
                mPresenter.enable(vh.adapterPosition, isChecked)
            }
        }

        override fun enableLoadMore(): Boolean {
            return false
        }

        override fun enableEmpty(): Boolean {
            return false
        }
    }

    class Presenter(var app: Application) : MvpRecyclerPresenter<Postage>(app) {

        override fun loadData(loadType: LoadType, paging: Paging, bundle: Bundle?, search: String?, afterLoad: (Boolean, List<Postage>?) -> Unit): Boolean {
            getPostageApi().list().list { ok, value ->
                afterLoad(ok, value)
            }
            return true
        }

        fun enable(position: Int, enable: Boolean) {
            val manager = getDataSet().item(position) ?: return
            val state = if (enable) 1 else 0
            getUserApi().editState(manager.id, state).result {
                if (!it) {
                    getAdapterSupport()?.getAdapter()?.notifyItemChanged(position, !enable)
                } else {
                    getDataSet().item(position)?.post = state
                }
            }
        }
    }
}