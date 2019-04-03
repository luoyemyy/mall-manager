package com.github.luoyemyy.mall.manager.activity.manager

import android.app.Application
import android.os.Bundle
import android.view.*
import androidx.navigation.fragment.findNavController
import com.github.luoyemyy.bus.Bus
import com.github.luoyemyy.bus.BusMsg
import com.github.luoyemyy.ext.confirm
import com.github.luoyemyy.mall.manager.R
import com.github.luoyemyy.mall.manager.activity.base.BaseFragment
import com.github.luoyemyy.mall.manager.api.getUserApi
import com.github.luoyemyy.mall.manager.api.list
import com.github.luoyemyy.mall.manager.api.result
import com.github.luoyemyy.mall.manager.bean.Manager
import com.github.luoyemyy.mall.manager.databinding.FragmentManagerBinding
import com.github.luoyemyy.mall.manager.databinding.FragmentManagerRecyclerBinding
import com.github.luoyemyy.mall.manager.util.*
import com.github.luoyemyy.mvp.getRecyclerPresenter
import com.github.luoyemyy.mvp.recycler.*

class ManagerFragment : BaseFragment() {

    private lateinit var mBinding: FragmentManagerBinding
    private lateinit var mPresenter: Presenter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return FragmentManagerBinding.inflate(inflater, container, false).apply { mBinding = this }.root
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
            setLinearManager()
            addItemDecoration(LinearDecoration.middle(requireContext()))
            setHasFixedSize(true)
        }
        mBinding.swipeRefreshLayout.setOnRefreshListener { mPresenter.loadRefresh() }

        Bus.addCallback(lifecycle, this, BusEvent.MANAGER_ADD)

        mPresenter.loadInit()
    }

    override fun busResult(event: String, msg: BusMsg) {
        when (event) {
            BusEvent.MANAGER_ADD -> mPresenter.loadRefresh()
        }
    }

    inner class Adapter : MvpSingleAdapter<Manager, FragmentManagerRecyclerBinding>(mBinding.recyclerView) {

        override fun getLayoutId(): Int {
            return R.layout.fragment_manager_recycler
        }

        override fun setRefreshState(refreshing: Boolean) {
            mBinding.swipeRefreshLayout.isRefreshing = refreshing
        }

        override fun bindContentViewHolder(binding: FragmentManagerRecyclerBinding, content: Manager, position: Int, payloads: MutableList<Any>): Boolean {
            binding.switchEnable.isChecked = payloads[0] as Boolean
            return false
        }

        override fun bindItemEvents(vh: VH<FragmentManagerRecyclerBinding>) {
            vh.binding?.root?.setOnLongClickListener {
                val menuId = if (Role.isSystemAdmin(UserInfo.getRoleId(requireContext()))) R.menu.manager else R.menu.delete
                popupMenu(requireContext(), it, menuId, getTouchX(), getTouchY()) { id ->
                    when (id) {
                        R.id.delete -> {
                            requireActivity().confirm(messageId = R.string.manager_delete_confirm, cancelButtonId = R.string.cancel, okButtonId = R.string.submit) {
                                mPresenter.delete(vh.adapterPosition)
                            }
                        }
                        R.id.toAdmin -> {
                            requireActivity().confirm(messageId = R.string.manager_as_admin_confirm, cancelButtonId = R.string.cancel, okButtonId = R.string.submit) {
                                mPresenter.asAdmin(vh.adapterPosition)
                            }
                        }
                        R.id.resetPassword -> {
                            findNavController().navigate(R.id.action_managerFragment_to_managerResetPasswordFragment)
                        }
                    }
                }
                true
            }

            vh.binding?.switchEnable?.apply {
                setOnClickListener {
                    switchChange(vh.adapterPosition, isChecked)
                }
            }
        }

        private fun switchChange(position: Int, isChecked: Boolean) {
            val msg = if (isChecked) R.string.manager_enable_confirm else R.string.manager_disable_confirm
            requireActivity().confirm(messageId = msg, cancelButtonId = R.string.cancel, cancelCallback = {
                notifyItemChanged(position, !isChecked)
            }, okButtonId = R.string.submit) {
                mPresenter.enable(position, isChecked)
            }
        }
    }

    class Presenter(var app: Application) : MvpRecyclerPresenter<Manager>(app) {

        override fun loadData(loadType: LoadType, paging: Paging, bundle: Bundle?, search: String?, afterLoad: (Boolean, List<Manager>?) -> Unit): Boolean {
            getUserApi().managerList(paging.current().toInt()).list { ok, value ->
                afterLoad(ok, value)
            }
            return true
        }

        fun delete(position: Int) {
            val manager = getDataSet().item(position) ?: return
            getUserApi().delete(manager.id).result {
                if (it) {
                    getAdapterSupport()?.apply {
                        getDataSet().remove(listOf(manager), getAdapter())
                    }
                }
            }
        }

        fun asAdmin(position: Int) {
            val manager = getDataSet().item(position) ?: return
            getUserApi().editRole(manager.id, Role.ADMIN_ID).result {
                if (it) {
                    getAdapterSupport()?.apply {
                        getDataSet().remove(listOf(manager), getAdapter())
                    }
                }
            }
        }

        fun enable(position: Int, enable: Boolean) {
            val manager = getDataSet().item(position) ?: return
            val state = if (enable) 1 else 0
            getUserApi().editState(manager.id, state).result {
                if (!it) {
                    getAdapterSupport()?.getAdapter()?.notifyItemChanged(position, !enable)
                } else {
                    getDataSet().item(position)?.state = state
                }
            }
        }
    }
}