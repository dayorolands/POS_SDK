package com.cluster.ui.util

import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import com.esafirm.imagepicker.features.ImagePicker
import com.esafirm.imagepicker.features.ImagePickerConfig
import com.esafirm.imagepicker.features.common.BaseConfig
import com.esafirm.imagepicker.features.createImagePickerIntent
import com.esafirm.imagepicker.helper.ConfigUtils
import com.esafirm.imagepicker.model.Image
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
inline fun registerImagePicker(
    coroutineScope: CoroutineScope = rememberCoroutineScope(),
    crossinline callback: suspend CoroutineScope.(List<Image>) -> Unit,
): (BaseConfig) -> Unit {
    val context = LocalContext.current
    val activityResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            val images = ImagePicker.getImages(it.data) ?: emptyList()
            coroutineScope.launch { callback(images) }
        }
    return { config: BaseConfig ->
        val finalConfig =
            if (config is ImagePickerConfig) ConfigUtils.checkConfig(config) else config
        val intent = createImagePickerIntent(context, finalConfig)
        activityResult.launch(intent)
    }
}