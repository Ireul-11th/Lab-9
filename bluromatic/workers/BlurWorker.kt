package com.example.bluromatic.workers

import android.Manifest
import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.example.bluromatic.KEY_BLUR_LEVEL
import com.example.bluromatic.KEY_IMAGE_URI
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

private const val TAG = "BlurWorker"

class BlurWorker(ctx: Context, params: WorkerParameters) : CoroutineWorker(ctx, params) {

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    override suspend fun doWork(): Result {
        val resourceUri = inputData.getString(KEY_IMAGE_URI)
        // Lấy mức độ mờ từ inputData
        val blurLevel = inputData.getInt(KEY_BLUR_LEVEL, 1)

        makeStatusNotification("Đang xử lý làm mờ ảnh...", applicationContext)

        return withContext(Dispatchers.IO) {
            // Giả lập xử lý chậm một chút để người dùng thấy được vòng xoay Loading
            delay(2000)

            try {
                require(!resourceUri.isNullOrBlank()) {
                    val errorMessage = "URI ảnh không hợp lệ"
                    Log.e(TAG, errorMessage)
                    errorMessage
                }

                val resolver = applicationContext.contentResolver

                val picture = BitmapFactory.decodeStream(
                    resolver.openInputStream(Uri.parse(resourceUri))
                )

                // Truyền biến blurLevel vào hàm làm mờ
                val output = blurBitmap(picture, blurLevel)

                val outputUri = writeBitmapToFile(applicationContext, output)

                val outputData = workDataOf(KEY_IMAGE_URI to outputUri.toString())
                Result.success(outputData)

            } catch (throwable: Throwable) {
                Log.e(TAG, "Lỗi xử lý", throwable)
                Result.failure()
            }
        }
    }
}