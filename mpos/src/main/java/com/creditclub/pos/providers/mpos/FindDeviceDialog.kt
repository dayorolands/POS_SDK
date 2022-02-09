package com.cluster.pos.providers.mpos

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.cluster.core.ui.SimpleBindingAdapter
import com.cluster.core.ui.widget.DialogListenerBlock
import com.cluster.core.ui.widget.build
import com.cluster.pos.providers.mpos.databinding.PosDialogFindDeviceBinding
import com.cluster.pos.providers.mpos.databinding.PosItemDeviceBinding
import com.jhl.bluetooth.ibridge.BluetoothIBridgeDevice
import java.util.*


/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 05/12/2019.
 * Appzone Ltd
 */
class FindDeviceDialog(context: Context, val block: DialogListenerBlock<BluetoothIBridgeDevice>?) :
    Dialog(context, false, null) {

    private var adapter = DeviceAdapter(emptyList())

    fun updateList(devices: ArrayList<BluetoothIBridgeDevice>?) {
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

    inner class DeviceAdapter(override var values: List<BluetoothIBridgeDevice>) :
        SimpleBindingAdapter<BluetoothIBridgeDevice, PosItemDeviceBinding>(R.layout.pos_item_device) {

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            with(holder.binding) {
                val device = values[position]
                titleTv.text = device.deviceName ?: device.deviceAddress
                root.setOnClickListener {
                    block?.build()?.submit(this@FindDeviceDialog, device)
                }
            }
        }
    }
}