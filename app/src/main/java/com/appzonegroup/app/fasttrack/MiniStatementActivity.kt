package com.appzonegroup.app.fasttrack

import android.os.Bundle
import com.creditclub.core.util.delegates.contentView
import com.creditclub.ui.databinding.ActivityMiniStatementBinding
import com.creditclub.ui.manager.ActivityMiniStatementManager

class MiniStatementActivity : BaseActivity() {
    private val binding by contentView<MiniStatementActivity, ActivityMiniStatementBinding>(R.layout.activity_mini_statement)
    private val manager by lazy { ActivityMiniStatementManager(this, binding) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        manager.onCreate(savedInstanceState)
    }
}
