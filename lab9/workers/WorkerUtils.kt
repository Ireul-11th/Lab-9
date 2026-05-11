package com.example.lab9.workers

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.annotation.WorkerThread
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.lab9.CHANNEL_ID
import com.example.lab9.NOTIFICATION_ID
import com.example.lab9.NOTIFICATION_TITLE
import com.example.lab9.OUTPUT_PATH
import com.example.lab9.R
import com.example.lab9.VERBOSE_NOTIFICATION_CHANNEL_DESCRIPTION
import com.example.lab9.VERBOSE_NOTIFICATION_CHANNEL_NAME
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.util.UUID

private const val TAG = "WorkerUtils"

@RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
fun makeStatusNotification(message: String, context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val name = VERBOSE_NOTIFICATION_CHANNEL_NAME
        val description = VERBOSE_NOTIFICATION_CHANNEL_DESCRIPTION
        val importance = NotificationManager.IMPORTANCE_HIGH
        val channel = NotificationChannel(CHANNEL_ID, name, importance)
        channel.description = description
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager?
        notificationManager?.createNotificationChannel(channel)
    }
    val builder = NotificationCompat.Builder(context, CHANNEL_ID)
        .setSmallIcon(R.drawable.ic_launcher_foreground)
        .setContentTitle(NOTIFICATION_TITLE)
        .setContentText(message)
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setVibrate(LongArray(0))
    NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, builder.build())
}

@WorkerThread
fun blurBitmap(bitmap: Bitmap, blurLevel: Int): Bitmap {
    // Lặp blur nhiều lần theo blurLevel để sự khác biệt rõ ràng
    // blurLevel=1: scale 1/8 rồi phóng to -> hơi mờ
    // blurLevel=2: lặp 2 lần -> mờ hơn
    // blurLevel=3: lặp 3 lần -> mờ nhất
    var output = bitmap
    repeat(blurLevel) {
        val scaleFactor = 8  // cố định scale factor, lặp nhiều lần để tăng độ mờ
        val scaled = Bitmap.createScaledBitmap(
            output,
            output.width / scaleFactor,
            output.height / scaleFactor,
            true
        )
        output = Bitmap.createScaledBitmap(
            scaled,
            bitmap.width,
            bitmap.height,
            true
        )
    }
    return output
}

@Throws(FileNotFoundException::class)
fun writeBitmapToFile(applicationContext: Context, bitmap: Bitmap): Uri {
    val name = String.format(
        "blur-filter-output-%s.png",
        UUID.randomUUID().toString()
    )
    val outputDir = File(applicationContext.filesDir, OUTPUT_PATH)
    if (!outputDir.exists()) {
        outputDir.mkdirs()
    }
    val outputFile = File(outputDir, name)
    var out: FileOutputStream? = null
    try {
        out = FileOutputStream(outputFile)
        bitmap.compress(Bitmap.CompressFormat.PNG, 0, out)
    } finally {
        out?.let {
            try {
                it.close()
            } catch (e: IOException) {
                Log.e(TAG, e.message.toString())
            }
        }
    }
    return Uri.fromFile(outputFile)
}