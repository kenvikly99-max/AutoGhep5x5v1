package com.example.autoghep5x5

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Path
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import kotlin.math.abs

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION, 
                Uri.parse("package:$packageName")
            )
            startActivityForResult(intent, 1234)
        } else {
            startService(Intent(this, FloatingButtonService::class.java))
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1234) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Settings.canDrawOverlays(this)) {
                startService(Intent(this, FloatingButtonService::class.java))
            }
        }
    }

    companion object {
        fun compareBitmaps(bmp1: Bitmap, bmp2: Bitmap): Double {
            var matchingPixels = 0
            val totalPixels = bmp1.width * bmp1.height
            val step = 4

            for (x in 0 until bmp1.width step step) {
                for (y in 0 until bmp1.height step step) {
                    val p1 = bmp1.getPixel(x, y)
                    val p2 = bmp2.getPixel(x, y)

                    val rDiff = abs((p1 shr 16 and 0xff) - (p2 shr 16 and 0xff))
                    val gDiff = abs((p1 shr 8 and 0xff) - (p2 shr 8 and 0xff))
                    val bDiff = abs((p1 and 0xff) - (p2 and 0xff))

                    if (rDiff + gDiff + bDiff < 45) {
                        matchingPixels++
                    }
                }
            }
            return matchingPixels.toDouble() / (totalPixels / (step * step))
        }
    }
}

class AutoService : AccessibilityService() {
    private val handler = Handler(Looper.getMainLooper())

    override fun onAccessibilityEvent(event: android.view.accessibility.AccessibilityEvent?) {}
    override fun onInterrupt() {}

    fun dragAndDrop(startX: Float, startY: Float, endX: Float, endY: Float, onComplete: () -> Unit) {
        val path = Path().apply {
            moveTo(startX, startY)
            lineTo(endX, endY)
        }
        val gesture = GestureDescription.Builder()
            .addStroke(GestureDescription.StrokeDescription(path, 0, 300))
            .build()

        dispatchGesture(gesture, object : GestureResultCallback() {
            override fun onCompleted(gestureDescription: GestureDescription?) {
                handler.postDelayed({ onComplete() }, 400)
            }
        }, null)
    }
}
