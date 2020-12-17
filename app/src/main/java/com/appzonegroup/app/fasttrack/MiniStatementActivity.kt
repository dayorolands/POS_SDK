package com.appzonegroup.app.fasttrack

import android.os.Bundle
import com.appzonegroup.app.fasttrack.utility.FunctionIds
import com.creditclub.core.ui.CreditClubActivity
import com.creditclub.ui.dataBinding
import com.creditclub.ui.databinding.ActivityMiniStatementBinding
import com.creditclub.ui.manager.ActivityMiniStatementManager

class MiniStatementActivity : CreditClubActivity(R.layout.activity_mini_statement) {
    private val binding: ActivityMiniStatementBinding by dataBinding()
    private val manager by lazy { ActivityMiniStatementManager(this, binding) }
    override val functionId = FunctionIds.AGENT_MINI_STATEMENT

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        manager.onCreate(savedInstanceState)
    }
}
