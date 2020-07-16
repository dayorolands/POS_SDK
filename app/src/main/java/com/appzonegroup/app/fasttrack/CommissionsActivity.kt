package com.appzonegroup.app.fasttrack

import android.os.Bundle
import com.creditclub.core.util.delegates.contentView
import com.creditclub.ui.databinding.ActivityCommissionsBinding
import com.creditclub.ui.manager.ActivityCommissionsManager

class CommissionsActivity : BaseActivity() {
    private val binding by contentView<CommissionsActivity, ActivityCommissionsBinding>(R.layout.activity_commissions)
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
