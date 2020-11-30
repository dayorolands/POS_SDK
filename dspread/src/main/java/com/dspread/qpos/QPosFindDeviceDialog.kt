package com.dspread.qpos

import android.app.Dialog
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.creditclub.core.ui.SimpleBindingAdapter
import com.creditclub.core.ui.widget.DialogListenerBlock
import com.creditclub.core.ui.widget.build
import com.dspread.R
import com.dspread.databinding.DspreadDialogFindBluetoothDeviceBinding
import com.dspread.databinding.DspreadItemBluetoothDeviceBinding
import java.util.*


/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 05/12/2019.
 * Appzone Ltd
 */
class QPosFindDeviceDialog(
    context: Context,
    val block: DialogListenerBlock<BluetoothDevice>?
) :
    Dialog(context, false, null) {

    private var adapter = DeviceAdapter(emptyList())

    fun updateList(devices: List<BluetoothDevice>?) {
        binding.progressBar.visibility = if (devices.isNullOrEmpty()) View.VISIBLE else View.GONE
        adapter.setData(devices)
    }

    private var binding: DspreadDialogFindBluetoothDeviceBinding = DataBindingUtil.inflate(
        LayoutInflater.from(context),
        R.layout.dspread_dialog_find_bluetooth_device,
        null,
        false
    )

    init {
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setCanceledOnTouchOutside(false)
        setCancelable(false)

        binding.list.layoutManager = LinearLayoutManager(context)
        binding.list.adapter = adapter

        setContentView(binding.root)
        binding.btnClose.setOnClickListener {
            dismiss()
            block?.build()?.close()
        }
    }

    inner class DeviceAdapter(override var values: List<BluetoothDevice>) :
        SimpleBindingAdapter<BluetoothDevice, DspreadItemBluetoothDeviceBinding>(R.layout.dspread_item_bluetooth_device) {

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            with(holder.binding) {
                val device = values[position]
                titleTv.text = device.name ?: device.address
                root.setOnClickListener {
                    block?.build()?.submit(this@QPosFindDeviceDialog, device)
                }
            }
        }
    }
}