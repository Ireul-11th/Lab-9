package com.example.lab9

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.lab9.ui.theme.BluromaticScreen
import com.example.lab9.ui.theme.BlurViewModel
import com.example.lab9.ui.theme.BluromaticTheme
import com.example.lab9.R

class BlurActivity : ComponentActivity() {

    private val blurViewModel: BlurViewModel by viewModels { BlurViewModel.Factory }

    private val pickImage = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { blurViewModel.setImageUri(it) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            BluromaticTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    BluromaticScreen(
                        blurViewModel = blurViewModel,
                        onSelectImageClick = { pickImage.launch("image/*") }
                    )
                }
            }
        }
    }
}

fun Context.getImageUri(): Uri {
    val resources = this.resources
    return Uri.Builder()
        .scheme(ContentResolver.SCHEME_ANDROID_RESOURCE)
        .authority(resources.getResourcePackageName(R.drawable.android_cupcake))
        .appendPath(resources.getResourceTypeName(R.drawable.android_cupcake))
        .appendPath(resources.getResourceEntryName(R.drawable.android_cupcake))
        .build()
}