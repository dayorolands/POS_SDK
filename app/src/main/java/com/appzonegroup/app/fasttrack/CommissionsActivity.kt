package com.appzonegroup.app.fasttrack

import android.os.Bundle
import com.creditclub.core.ui.CreditClubActivity
import com.creditclub.ui.dataBinding
import com.creditclub.ui.databinding.ActivityCommissionsBinding
import com.creditclub.ui.manager.ActivityCommissionsManager

class CommissionsActivity : CreditClubActivity(R.layout.activity_commissions) {
    private val binding: ActivityCommissionsBinding by dataBinding()
    private val manager by lazy {
        ActivityCommissionsManager(
            this,
            binding,
            institutionConfig.transactionTypes
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        manager.onCreate(savedInstanceState)
    }
}
