package com.cluster

import android.os.Bundle
import com.cluster.core.ui.CreditClubActivity
import com.cluster.ui.dataBinding
import com.cluster.ui.databinding.ActivityCommissionsBinding
import com.cluster.ui.manager.ActivityCommissionsManager

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
