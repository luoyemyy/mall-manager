package com.github.luoyemyy.mall.manager.activity.user

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.github.luoyemyy.bus.Bus
import com.github.luoyemyy.ext.confirm
import com.github.luoyemyy.mall.manager.R
import com.github.luoyemyy.mall.manager.activity.base.BaseFragment
import com.github.luoyemyy.mall.manager.databinding.FragmentUserBinding
import com.github.luoyemyy.mall.manager.util.BusEvent
import com.github.luoyemyy.mall.manager.util.UserInfo

class UserFragment : BaseFragment(), View.OnClickListener {

    private lateinit var mBinding: FragmentUserBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return FragmentUserBinding.inflate(inflater, container, false).apply { mBinding = this }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        mBinding.layoutUser.setOnClickListener(this)
        mBinding.txtAdmin.setOnClickListener(this)
        mBinding.txtManger.setOnClickListener(this)
        mBinding.txtPostage.setOnClickListener(this)
        mBinding.txtWallet.setOnClickListener(this)
        mBinding.txtAbout.setOnClickListener(this)
        mBinding.txtExit.setOnClickListener(this)

        mBinding.entity = UserInfo.getUser(requireContext())
    }

    override fun onClick(v: View?) {
        when (v) {
            mBinding.layoutUser -> {
                findNavController().navigate(R.id.action_userFragment_to_userInfoFragment)
            }
            mBinding.txtAdmin -> {
                findNavController().navigate(R.id.action_userFragment_to_adminFragment)
            }
            mBinding.txtManger -> {
                findNavController().navigate(R.id.action_userFragment_to_managerFragment)
            }
            mBinding.txtPostage -> {
                findNavController().navigate(R.id.action_userFragment_to_managerFragment)
            }
            mBinding.txtWallet -> {
            }
            mBinding.txtAbout -> {
            }
            mBinding.txtExit -> {
                requireActivity().confirm(messageId = R.string.exit_confirm, cancelButtonId = R.string.cancel, okButtonId = R.string.submit) {
                    UserInfo.exitLogin(requireContext())
                    Bus.post(BusEvent.LOGIN_EXPIRE)
                }
            }
        }
    }
}