package com.cluster.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign

@Composable
fun <T> Select(
    title: String,
    options: List<T>,
    selected: T?,
    onChange: (T) -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: @Composable (() -> Unit)? = null,
    itemContent: @Composable RowScope.(T) -> Unit = {
        Text(
            text = it.toString(),
            textAlign = TextAlign.Start,
            modifier = Modifier
                .wrapContentWidth(),
        )
    }
) {
    val (text, setText) = remember(selected) {
        mutableStateOf(selected?.toString() ?: "")
    } // initial value
    val (isOpen, setOpen) = remember { mutableStateOf(false) } // initial value
    val onSelect: (T) -> Unit = {
        setText(it.toString())
        onChange(it)
    }
    Box(modifier = modifier) {
        Column {
            OutlinedTextField(
                value = text,
                onValueChange = setText,
                label = { Text(text = title) },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = leadingIcon,
                trailingIcon = {
                    Icon(
                        Icons.Filled.ArrowDropDown,
                        contentDescription = null,
                        tint = MaterialTheme.colors.onSurface.copy(0.52f)
                    )
                }
            )
            DropdownMenu(
                modifier = Modifier.fillMaxWidth(),
                expanded = isOpen,
                onDismissRequest = { setOpen(false) },
            ) {
                options.forEach {
                    DropdownMenuItem(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            setOpen(false)
                            onSelect(it)
                        }
                    ) {
                        ProvideTextStyle(value = TextStyle(textAlign = TextAlign.Start)) {
                            itemContent(it)
                        }
                    }
                }
            }
        }
        Spacer(
            modifier = Modifier
                .matchParentSize()
                .background(Color.Transparent)
                .clickable(onClick = { setOpen(true) })
        )
    }
}