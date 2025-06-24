package com.zeticai.yolov8demo

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.FrameLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.zeticai.mlange.feature.objectdetection.ObjectDetection
import com.zeticai.mlange.feature.objectdetection.yolov8.YOLOResult
import com.zeticai.mlange.inputsource.camera.CameraSource
import com.zeticai.mlange.inputsource.camera.PreviewSurfaceView
import com.zeticai.mlange.inputsource.camera.YOLOResultSurfaceView
import com.zeticai.mlange.pipeline.ZeticMLangePipeline

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.root)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        askForPermission()
    }

    private fun initYOLO() {
        val preview = PreviewSurfaceView(this)
        val result = YOLOResultSurfaceView(this)

        val pipeline = ZeticMLangePipeline(
            feature = ObjectDetection(this),
            inputSource = CameraSource(this, preview.holder),
        )

        findViewById<FrameLayout>(R.id.root).run {
            addView(preview)
            addView(result)
        }

        pipeline.loop {
            runOnUiThread {
                result.visualize(YOLOResult(it.value), true)
            }
        }
    }

    private fun askForPermission() {
        val requestPermissionLauncher =
            registerForActivityResult(RequestPermission()) { isGranted: Boolean ->
                if (isGranted) initYOLO()
                else Toast.makeText(this, "Camera Permission Not Granted!", Toast.LENGTH_SHORT)
                    .show()
            }

        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            initYOLO()
        } else if (ActivityCompat.shouldShowRequestPermissionRationale(
                this, Manifest.permission.CAMERA
            )
        ) {
            Toast.makeText(this, "Camera Permission Not Granted!", Toast.LENGTH_SHORT).show()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }
}