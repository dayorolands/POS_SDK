package com.cluster.pos

import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.navigation.fragment.findNavController
import com.cluster.core.data.api.retrofitService
import com.cluster.core.ui.CreditClubActivity
import com.cluster.core.ui.widget.TextFieldParams
import com.cluster.core.util.delegates.getArrayList
import com.cluster.core.util.format
import com.cluster.core.util.safeRunIO
import com.cluster.pos.api.PosApiService
import com.cluster.pos.data.PosPreferences
import com.cluster.pos.databinding.PosMenuFragmentBinding
import com.cluster.pos.util.MenuPage
import com.cluster.pos.util.MenuPages
import com.cluster.ui.dataBinding
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import java.time.Instant


class PosMenuFragment : PosFragment(R.layout.pos_menu_fragment) {
    private val binding: PosMenuFragmentBinding by dataBinding()
    private val posPreferences: PosPreferences by inject()
    private val defaultParameterStore: PosParameter by inject()
    private val posApiService: PosApiService by retrofitService()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val creditClubActivity = requireActivity() as CreditClubActivity
        creditClubActivity.setSupportActionBar(binding.toolbar)
        creditClubActivity.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.cont.visibility = View.INVISIBLE
        checkRequirements()
        binding.run {
            purchaseButton.cardMenu.visibility = View.VISIBLE
        }
    }

    private fun checkRequirements() {
        mainScope.launch {
            binding.cont.visibility = View.VISIBLE
            bindView()
        }
    }

    private fun bindView() {
        binding.purchaseButton.button.setOnClickListener {
            startActivity(CardWithdrawalActivity::class.java)
        }
    }
}
