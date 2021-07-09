package com.appzonegroup.creditclub.pos

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.appzonegroup.creditclub.pos.databinding.ActivityMenuBinding
import com.appzonegroup.creditclub.pos.databinding.CardMenuButtonBinding
import com.appzonegroup.creditclub.pos.util.MenuPage
import com.appzonegroup.creditclub.pos.util.MenuPages
import com.appzonegroup.creditclub.pos.widget.Dialogs

open class MenuActivity : PosActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val secured = intent.getBooleanExtra(MenuPage.IS_SECURE_PAGE, false)
        if (!secured) setup()
        else dialogProvider.requestPIN("Enter pin") {
            onSubmit { pin ->
                if (pin != config.supervisorPin) {
                    Toast.makeText(this@MenuActivity, "Authentication failed", Toast.LENGTH_LONG)
                        .show()
                    finish()
                    return@onSubmit
                }
                setup()
            }
        }
    }

    private fun setup() {
        val binding =
            DataBindingUtil.setContentView<ActivityMenuBinding>(this, R.layout.activity_menu)
        binding.title = intent.getStringExtra(MenuPage.TITLE)
        val pageNumber = intent.getIntExtra(MenuPage.PAGE_NUMBER, 0)
        val module = MenuPages[pageNumber] ?: return
        module.options?.also { options ->
            for (option in options.value) {
                val menuButton = DataBindingUtil.inflate<CardMenuButtonBinding>(
                    LayoutInflater.from(this),
                    R.layout.card_menu_button,
                    findViewById(R.id.main_menu), true
                )

                menuButton.text = option.value.name
                menuButton.src = ContextCompat.getDrawable(this, option.value.icon)
                menuButton.button.setOnClickListener {
                    when {
                        option.value.isClickable -> option.value.click(this)
                        option.value is MenuPage -> startActivity(
                            Intent(
                                this,
                                MenuActivity::class.java
                            ).apply {
                                putExtra(MenuPage.TITLE, option.value.name)
                                putExtra(MenuPage.PAGE_NUMBER, option.value.id)
                                putExtra(
                                    MenuPage.IS_SECURE_PAGE,
                                    (option.value as MenuPage).isSecure
                                )
                            })
                        else -> showError("This function is not available.")
                    }
                }
            }
        }
    }

    open fun goBack(v: View) = finish()
}
