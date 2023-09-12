package com.cluster.pos

import android.content.Intent
import android.os.Bundle
import android.provider.Settings.ACTION_APN_SETTINGS
import android.text.InputType
import android.view.MenuItem
import android.view.View
import com.cluster.pos.databinding.ActivityNetworkParametersBinding
import com.cluster.pos.extension.apnInfo
import com.cluster.pos.service.CallHomeService
import com.cluster.pos.util.AppConstants
import com.cluster.pos.widget.Dialogs
import com.cluster.core.ui.widget.DialogOptionItem
import com.cluster.core.util.debugOnly
import com.cluster.pos.model.PosTenant
import com.cluster.ui.dataBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class TerminalOptionsActivity : PosActivity(R.layout.activity_network_parameters),
    View.OnClickListener {
    private val binding: ActivityNetworkParametersBinding by dataBinding()
    private val posTenant: PosTenant by inject()
    private val callHomeService: CallHomeService by inject()

    override fun onClick(v: View?) {
        if (v?.id == R.id.terminal_id_item) {
            var isDebuggable = false
            debugOnly {
                isDebuggable = true
            }

            if (!isDebuggable) {
                dialogProvider.showError("Terminal ID cannot be set manually. Please contact your administrator")
                return
            }
        }

        val hint: String
        val value: String?
        when (v?.id) {
            R.id.apn_item -> {
                val intent = Intent(ACTION_APN_SETTINGS)
                startActivityForResult(intent, 0)
                return
            }
            R.id.host_item -> {
                hint = "HOST"
                value = config.host
            }
            R.id.ip_address_item -> {
                hint = "IP"
                value = config.ip
            }
            R.id.port_item -> {
                hint = "PORT"
                value = config.port.toString()
            }
            R.id.call_home_item -> {
                hint = "CALL HOME"
                value = config.callHome
            }
            R.id.terminal_id_item -> {
                hint = "TERMINAL ID"
                value = config.terminalId
            }
            else -> {
                hint = "Other"
                value = ""
            }
        }

        val inputType: Int = when (v?.id) {
            R.id.port_item -> InputType.TYPE_CLASS_NUMBER
            else -> InputType.TYPE_CLASS_TEXT
        }

        Dialogs.input(this, hint, inputType, value) {
            onSubmit { param ->
                dismiss()

                when (v?.id) {
                    R.id.apn_item -> {
                        config.apn = param
                        binding.apnItem.value = param
                    }

                    R.id.host_item -> {
                        config.host = param
                        binding.hostItem.value = param
                    }

                    R.id.ip_address_item -> {
                        config.ip = param
                        binding.ipAddressItem.value = param
                    }

                    R.id.port_item -> {
                        config.port = param.toInt()
                        binding.portItem.value = param
                    }

                    R.id.terminal_id_item -> {
                        config.terminalId = param
                        binding.terminalIdItem.value = param
                    }

                    R.id.call_home_item -> {
                        config.callHome = param
                        binding.portItem.value = param
                        callHomeService.stopCallHomeTimer()
                        callHomeService.startCallHomeTimer()
                    }
                }
            }
        }.show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        binding.apn = "Retrieving..."
        mainScope.launch {
            delay(5000)
            binding.apn = apnInfo
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        refresh()

        binding.apnItem.cont.setOnClickListener(this)
        binding.hostItem.cont.setOnClickListener(this)
        binding.ipAddressItem.cont.setOnClickListener(this)
        binding.portItem.cont.setOnClickListener(this)
        binding.terminalIdItem.cont.setOnClickListener(this)
        binding.callHomeItem.cont.setOnClickListener(this)

        binding.posModeItem.run {
            val posModeOptions = posTenant.infoList.map { DialogOptionItem(it.label) }
            cont.setOnClickListener {
                dialogProvider.showOptions("Select POS mode", posModeOptions) {
                    onSubmit { position ->
                        val newPosMode = posTenant.infoList[position]
                        config.remoteConnectionInfo = newPosMode
                        binding.posMode = newPosMode.label
                    }
                }
            }
        }

        if (config.remoteConnectionInfo == InvalidRemoteConnectionInfo) {
            binding.posModeItem.root.visibility = View.GONE
        }

        binding.resetNetworkBtn.setOnClickListener {
            config.run {
                apn = AppConstants.APN
                host = AppConstants.HOST
                terminalId = AppConstants.TID
                port = AppConstants.PORT
                ip = AppConstants.IP
                callHome = AppConstants.CALL_HOME
            }
            refresh()
        }

        val passwordType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD

        binding.adminPinItem.cont.setOnClickListener {
            Dialogs.input(this, "New Admin password", passwordType) {
                onSubmit { password ->
                    dismiss()
                    if (password.isEmpty()) {
                        showError("Password cannot be empty")
                        return@onSubmit
                    } else Dialogs.input(
                        this@TerminalOptionsActivity,
                        "Confirm Admin password",
                        passwordType
                    ) {
                        onSubmit { confirmation ->
                            dismiss()
                            if (password != confirmation) {
                                showError("Passwords don't match. Please try again")
                            } else config.adminPin = password
                        }
                    }
                }
            }
        }

        binding.supervisorPinItem.cont.setOnClickListener {
            Dialogs.requestPin(this, getString(R.string.pos_new_supervisor_pin)) { pin ->
                pin ?: return@requestPin

                if (pin.isEmpty()) {
                    showError("${getString(R.string.pos_supervisor_pin)} cannot be empty")
                    return@requestPin
                } else if(pin.length > 4){
                    showError("${getString(R.string.pos_supervisor_pin)} cannot be more than 4 digits")
                    return@requestPin
                } else Dialogs.requestPin(
                    this,
                    getString(R.string.pos_confirm_supervisor_pin)
                ) { confirmation ->
                    if (pin != confirmation) {
                        showError("PINs don't match. Please try again")
                    } else config.supervisorPin = pin
                }
            }
        }
    }

    private fun refresh() {
        binding.apn = apnInfo
        binding.host = config.host
        binding.ip = config.ip
        binding.port = config.port.toString()
        binding.callHome = "Call Home interval in seconds: " + config.callHome
        binding.terminalId = config.terminalId
        binding.posMode = config.remoteConnectionInfo.label
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> onBackPressed()
        }
        return true
    }
}
