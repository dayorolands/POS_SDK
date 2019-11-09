package com.appzonegroup.creditclub.pos

import android.content.Intent
import android.os.Bundle
import android.provider.Settings.ACTION_APN_SETTINGS
import android.text.InputType
import android.view.MenuItem
import android.view.View
import androidx.databinding.DataBindingUtil
import com.appzonegroup.creditclub.pos.databinding.ActivityNetworkParametersBinding
import com.appzonegroup.creditclub.pos.databinding.NetworkSettingsItemBinding
import com.appzonegroup.creditclub.pos.util.PosMode
import com.appzonegroup.creditclub.pos.widget.Dialogs
import com.creditclub.core.ui.widget.DialogOptionItem
import com.creditclub.core.ui.widget.TextFieldParams
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class TerminalOptionsActivity : PosActivity(), View.OnClickListener {
    lateinit var binding: ActivityNetworkParametersBinding

    override fun onClick(v: View?) {

        if (v?.id == R.id.terminal_id_item) {
            dialogProvider.showError("Terminal ID cannot be set manually. Please contact your administrator")
            return
        }

        val hint: String
        val value: String?
        when (v?.id) {
            R.id.apn_item -> {
//                hint = "APN"
//                value = config.apn

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
                        callHomeService.callHome()
                    }
                }
            }
        }.show()
    }

    fun resetNetwork(view: View?) {
        config.resetNetwork()
        refresh()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        binding.apn = "Retrieving..."
        GlobalScope.launch(Dispatchers.Main) {
            delay(5000)
            binding.apn = config.getApnInfo(this@TerminalOptionsActivity)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_network_parameters)

        refresh()

        binding.apnItem.cont.setOnClickListener(this)
        binding.hostItem.cont.setOnClickListener(this)
        binding.ipAddressItem.cont.setOnClickListener(this)
        binding.portItem.cont.setOnClickListener(this)
        binding.terminalIdItem.cont.setOnClickListener(this)
        binding.callHomeItem.cont.setOnClickListener(this)

        binding.posModeItem.run {
            val posModeOptions = PosMode.values().map { DialogOptionItem(it.label) }
            cont.setOnClickListener {
                dialogProvider.showOptions("Select POS mode", posModeOptions) {
                    onSubmit { position ->
                        val newPosMode = PosMode.values()[position]
                        config.posMode = newPosMode
                        binding.posMode = newPosMode.label
                    }
                }
            }
        }

//        binding.hostItem.createValueChangeListener(
//            TextFieldParams("Enter Host", initialValue = config.host),
//            onChange = { value ->
//                config.host = value
//            }
//        )

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
        binding.apn = config.getApnInfo(this)
        binding.host = config.host
        binding.ip = config.ip
        binding.port = config.port.toString()
        binding.callHome = "Call Home interval in seconds: " + config.callHome
        binding.terminalId = config.terminalId
        binding.posMode = config.posMode.label
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> onBackPressed()
        }
        return true
    }

    private fun NetworkSettingsItemBinding.createValueChangeListener(
        textFieldParams: TextFieldParams,
        onChange: (String) -> Unit
    ) {
        cont.setOnClickListener {
            dialogProvider.showInput(textFieldParams) {
                onSubmit { newValue ->
                    dismiss()
                    value = newValue
                    onChange(newValue)
                }
            }
        }
    }
}
