package com.appzonegroup.app.fasttrack.fragment

import android.os.Bundle
import android.view.View
import android.widget.EditText
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.appzonegroup.app.fasttrack.R
import com.appzonegroup.app.fasttrack.databinding.FragmentAgentPinSubmitBtnBinding
import com.appzonegroup.app.fasttrack.ui.dataBinding
import com.creditclub.core.ui.CreditClubFragment
import com.creditclub.core.util.safeRunIO
import kotlinx.coroutines.launch

class AgentPINFragment : CreditClubFragment(R.layout.fragment_agent_pin_submit_btn) {
    private val binding by dataBinding<FragmentAgentPinSubmitBtnBinding>()
    private val viewModel by activityViewModels<OpenAccountViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.viewModel = viewModel
        binding.accountInfoNextBtn.setOnClickListener { next() }
    }

    private fun next() {
        val agentPIN = viewModel.agentPIN.value?.trim()
        if (agentPIN.isNullOrBlank()) {
            indicateError("Please enter your PIN", binding.agentPinEt)
            return
        }

        if (agentPIN.length != 4) {
            indicateError("Please enter the correct PIN", binding.agentPinEt)
            return
        }

        viewModel.afterAgentPin.value?.invoke()
    }

    private fun indicateError(message: String, view: EditText?) {
        view?.error = message
        view?.requestFocus()
    }
}