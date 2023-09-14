package com.cluster.ui

import android.view.View
import com.cluster.R


object Dialogs {
    interface PinChangeHandler {
        fun onSelectNumber(view: View) {
            onChange(
                when (view.id) {
                    R.id.number1 -> 1
                    R.id.number2 -> 2
                    R.id.number3 -> 3
                    R.id.number4 -> 4
                    R.id.number5 -> 5
                    R.id.number6 -> 6
                    R.id.number7 -> 7
                    R.id.number8 -> 8
                    R.id.number9 -> 9
                    else -> 0
                }
            )
        }

        fun onBackspacePressed(view: View)

        fun onEnterPressed(view: View)

        fun onChange(digit: Byte)
    }
}