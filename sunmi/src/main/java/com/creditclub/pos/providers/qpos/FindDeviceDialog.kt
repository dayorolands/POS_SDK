package com.appzonegroup.creditclub.pos.provider.qpos

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
import com.appzonegroup.creditclub.pos.R
import com.appzonegroup.creditclub.pos.databinding.PosDialogFindDeviceBinding
import com.appzonegroup.creditclub.pos.databinding.PosItemDeviceBinding
import com.creditclub.core.ui.SimpleBindingAdapter
import com.creditclub.core.ui.widget.DialogListenerBlock
import com.creditclub.core.ui.widget.build
import java.util.*


/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 05/12/2019.
 * Appzone Ltd
 */
class FindDeviceDialog(context: Context, val block: DialogListenerBlock<BluetoothDevice>?) :
    Dialog(context, false, null) {

    private var adapter = DeviceAdapter(emptyList())

    fun updateList(devices: List<BluetoothDevice>?) {
        binding.progressBar.visibility = if (devices.isNullOrEmpty()) View.VISIBLE else View.GONE
        adapter.setData(devices)
    }

    private var binding: PosDialogFindDeviceBinding = DataBindingUtil.inflate(
        LayoutInflater.from(context),
        R.layout.pos_dialog_find_device,
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
        SimpleBindingAdapter<BluetoothDevice, PosItemDeviceBinding>(R.layout.pos_item_device) {

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            with(holder.binding) {
                val device = values[position]
                titleTv.text = device.name ?: device.address
                root.setOnClickListener {
                    block?.build()?.submit(this@FindDeviceDialog, device)
                }
            }
        }
    }
}