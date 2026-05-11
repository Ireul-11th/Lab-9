package com.example.lab9.data

import android.net.Uri
import androidx.work.WorkInfo
import kotlinx.coroutines.flow.Flow

interface BluromaticRepository {
    val outputWorkInfo: Flow<WorkInfo?>
    fun applyBlur(blurLevel: Int)
    fun cancelWork()
    fun setImageUri(uri: Uri)
}
