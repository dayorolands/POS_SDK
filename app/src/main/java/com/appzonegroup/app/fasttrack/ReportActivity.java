package com.appzonegroup.app.fasttrack;

import android.os.Bundle;

import androidx.databinding.DataBindingUtil;

import com.appzonegroup.app.fasttrack.model.PolarisTransactionGroup;
import com.creditclub.ui.databinding.ActivityReportBinding;
import com.appzonegroup.app.fasttrack.ui.ActivityReportManager;

/**
 * Created by Oto-obong on 15/7/2017.
 */

public class ReportActivity extends BaseActivity {
    private ActivityReportManager manager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityReportBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_report);
        manager = new ActivityReportManager(this, binding, PolarisTransactionGroup.INSTANCE);
        manager.onCreate(savedInstanceState);
    }
}
