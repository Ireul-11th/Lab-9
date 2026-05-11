package com.example.lab9.data

import android.content.Context
import android.net.Uri
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.example.lab9.IMAGE_MANIPULATION_WORK_NAME
import com.example.lab9.KEY_BLUR_LEVEL
import com.example.lab9.KEY_IMAGE_URI
import com.example.lab9.TAG_OUTPUT
import com.example.lab9.getImageUri
import com.example.lab9.workers.BlurWorker
import com.example.lab9.workers.CleanupWorker
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class WorkManagerBluromaticRepository(context: Context) : BluromaticRepository {

    private var imageUri: Uri = context.getImageUri()
    private val workManager = WorkManager.getInstance(context)

    override val outputWorkInfo: Flow<WorkInfo?> =
        workManager.getWorkInfosByTagFlow(TAG_OUTPUT).map { workInfos ->
            workInfos.firstOrNull()
        }

    override fun setImageUri(uri: Uri) {
        imageUri = uri
    }

    override fun applyBlur(blurLevel: Int) {
        var continuation = workManager.beginUniqueWork(
            IMAGE_MANIPULATION_WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            OneTimeWorkRequest.from(CleanupWorker::class.java)
        )

        // Gắn TAG_OUTPUT thẳng vào BlurWorker, bỏ SaveImageToFileWorker
        val blurBuilder = OneTimeWorkRequestBuilder<BlurWorker>()
            .setInputData(createInputDataForWorkRequest(blurLevel, imageUri))
            .addTag(TAG_OUTPUT)
            .build()
        continuation = continuation.then(blurBuilder)

        continuation.enqueue()
    }

    override fun cancelWork() {
        workManager.cancelUniqueWork(IMAGE_MANIPULATION_WORK_NAME)
    }

    private fun createInputDataForWorkRequest(blurLevel: Int, imageUri: Uri): Data {
        val builder = Data.Builder()
        builder.putString(KEY_IMAGE_URI, imageUri.toString()).putInt(KEY_BLUR_LEVEL, blurLevel)
        return builder.build()
    }
}