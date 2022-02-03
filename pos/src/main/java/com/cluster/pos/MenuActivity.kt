package com.cluster.pos

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.cluster.pos.databinding.ActivityMenuBinding
import com.cluster.pos.databinding.CardMenuButtonBinding
import com.cluster.pos.util.MenuPage
import com.cluster.pos.util.MenuPages
import kotlinx.coroutines.launch

open class MenuActivity : PosActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainScope.launch { setup() }
    }

    private suspend fun setup() {
        val isProtectedPage = intent.getBooleanExtra(MenuPage.IS_SECURE_PAGE, false)
        if (isProtectedPage) {
            val pin = dialogProvider.getPin("Enter PIN") ?: return
            if (pin != config.supervisorPin) {
                Toast.makeText(
                    this@MenuActivity,
                    "Authentication failed",
                    Toast.LENGTH_LONG,
                ).show()
                finish()
                return
            }
        }

        val binding: ActivityMenuBinding = DataBindingUtil.setContentView(
            this,
            R.layout.activity_menu,
        )
        binding.title = intent.getStringExtra(MenuPage.TITLE)
        val pageNumber = intent.getIntExtra(MenuPage.PAGE_NUMBER, 0)
        val module = MenuPages[pageNumber] ?: return
        val options = module.options ?: return
        for (option in options.value) {
            val menuButton: CardMenuButtonBinding = DataBindingUtil.inflate(
                LayoutInflater.from(this),
                R.layout.card_menu_button,
                findViewById(R.id.main_menu),
                true,
            )

            menuButton.text = option.name
            menuButton.src = ContextCompat.getDrawable(this, option.icon)
            menuButton.button.setOnClickListener {
                when {
                    option.isClickable -> option.performClick(this)
                    option is MenuPage -> {
                        val newIntent = Intent(this, MenuActivity::class.java).apply {
                            putExtra(MenuPage.TITLE, option.name)
                            putExtra(MenuPage.PAGE_NUMBER, option.id)
                            putExtra(
                                MenuPage.IS_SECURE_PAGE,
                                option.isSecure
                            )
                        }
                        startActivity(newIntent)
                    }
                    else -> showError("This function is not available.")
                }
            }
        }
    }

    open fun goBack(v: View) = finish()
}
