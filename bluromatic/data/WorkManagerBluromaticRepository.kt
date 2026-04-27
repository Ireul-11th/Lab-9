package com.example.bluromatic.data

import android.content.Context
import android.net.Uri
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.example.bluromatic.IMAGE_MANIPULATION_WORK_NAME
import com.example.bluromatic.KEY_BLUR_LEVEL
import com.example.bluromatic.KEY_IMAGE_URI
import com.example.bluromatic.TAG_OUTPUT
import com.example.bluromatic.getImageUri
import com.example.bluromatic.workers.BlurWorker
import com.example.bluromatic.workers.CleanupWorker
import com.example.bluromatic.workers.SaveImageToFileWorker
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.map

class WorkManagerBluromaticRepository(context: Context) : BluromaticRepository {

    private var imageUri: Uri = context.getImageUri()
    private val workManager = WorkManager.getInstance(context)

    // Sửa lại để UI có thể quan sát trạng thái (Loading/Success)
    override val outputWorkInfo: Flow<WorkInfo?> =
        workManager.getWorkInfosByTagFlow(TAG_OUTPUT).map { workInfos ->
            workInfos.firstOrNull()
        }

    override fun applyBlur(blurLevel: Int) {
        // Sử dụng beginUniqueWork để đảm bảo tại một thời điểm chỉ có 1 chuỗi xử lý ảnh chạy
        var continuation = workManager.beginUniqueWork(
            IMAGE_MANIPULATION_WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            OneTimeWorkRequest.from(CleanupWorker::class.java)
        )

        // Add WorkRequest to blur the image
        val blurBuilder = OneTimeWorkRequestBuilder<BlurWorker>()
        blurBuilder.setInputData(createInputDataForWorkRequest(blurLevel, imageUri))
        continuation = continuation.then(blurBuilder.build())

        // Add WorkRequest to save the image - Thêm TAG_OUTPUT ở đây
        val save = OneTimeWorkRequestBuilder<SaveImageToFileWorker>()
            .addTag(TAG_OUTPUT)
            .build()
        continuation = continuation.then(save)

        // Chạy
        continuation.enqueue()
    }

    // Viết code cho nút Cancel
    override fun cancelWork() {
        workManager.cancelUniqueWork(IMAGE_MANIPULATION_WORK_NAME)
    }

    private fun createInputDataForWorkRequest(blurLevel: Int, imageUri: Uri): Data {
        val builder = Data.Builder()
        builder.putString(KEY_IMAGE_URI, imageUri.toString()).putInt(KEY_BLUR_LEVEL, blurLevel)
        return builder.build()
    }
}