package com.rohith.vsa_kotlin.activities

import android.Manifest
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.rohith.vsa_kotlin.R
import com.rohith.vsa_kotlin.databinding.LayoutCameraBinding
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


private const val REQUEST_CAMERA_PERMISSION = 1

class CameraActivity : AppCompatActivity() {

    private lateinit var binding: LayoutCameraBinding

    private var preview: Preview? = null
    private var videoCapture: VideoCapture? = null
    private var camera: Camera? = null
    private lateinit var outputDirectory: File
    private lateinit var cameraExecutor: ExecutorService
    private var maxDuration = 10000L
    private lateinit var progressAnimation : ObjectAnimator

    private val mHandler = Handler()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = LayoutCameraBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            requestCameraPermission()
        } else {
            init()
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun init(){
        binding.flip.setOnClickListener { flipCamera() }
        binding.shutter.setOnClickListener { videoRecordToggle() }
        outputDirectory = getOutputDirectory()
        cameraExecutor = Executors.newSingleThreadExecutor()
        progressAnimation = ObjectAnimator.ofInt(binding.progressBar, "progress", maxDuration.toInt()).setDuration(maxDuration)
        progressAnimation.setAutoCancel(true)

        val scaleGestureDetector = ScaleGestureDetector(this, object : ScaleGestureDetector.SimpleOnScaleGestureListener(){
            override fun onScale(detector: ScaleGestureDetector): Boolean {
                if (camera == null) return false
                val currentZoomRatio: Float = camera!!.cameraInfo.zoomState.value?.zoomRatio ?: 0F
                val delta = detector.scaleFactor
                camera!!.cameraControl.setZoomRatio(currentZoomRatio * delta)
                return true
            }
        })
        binding.preview.setOnTouchListener { _, event->
            scaleGestureDetector.onTouchEvent(event)
            if (event.action != MotionEvent.ACTION_UP) {
                return@setOnTouchListener true
            }

            val factory: MeteringPointFactory = SurfaceOrientedMeteringPointFactory(
                binding.preview.height.toFloat(), binding.preview.width.toFloat()
            )
            val point = factory.createPoint(if (lensFacing == CameraSelector.LENS_FACING_BACK) event.y else binding.preview.height - event.y, binding.preview.width - event.x)

            camera?.cameraControl?.startFocusAndMetering(
                FocusMeteringAction.Builder(point).build()
            )
            return@setOnTouchListener true
        }
        startCamera()
    }

    private fun startCamera(lensFacing : Int = CameraSelector.LENS_FACING_BACK) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener(Runnable {
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
            preview = Preview.Builder().build()
            videoCapture = VideoCapture.Builder().build()

            val cameraSelector = CameraSelector.Builder().requireLensFacing(lensFacing).build()

            try {
                cameraProvider.unbindAll()
                camera = cameraProvider.bindToLifecycle(this, cameraSelector, preview, videoCapture)
                preview?.setSurfaceProvider(binding.preview.createSurfaceProvider())
            } catch(exc: Exception) {

            }

        }, ContextCompat.getMainExecutor(this))
    }

    private var lensFacing = CameraSelector.LENS_FACING_BACK
    private fun flipCamera(){
        lensFacing = if (lensFacing == CameraSelector.LENS_FACING_BACK) CameraSelector.LENS_FACING_FRONT else CameraSelector.LENS_FACING_BACK
        startCamera(lensFacing)
    }

    fun startUploadActivity(file: String){
        val intent = Intent(this, UploadActivity::class.java)
        intent.putExtra("file", file)
        startActivity(intent)
    }
    var recording = false

    @SuppressLint("RestrictedApi")
    fun videoCaptureStart(){
        val videoCapture = videoCapture ?: return
        recording = true
        binding.shutter.setImageResource(R.drawable.shutter_bg_video_pressed)
        binding.progressBar.max = maxDuration.toInt()
        binding.progressBar.visibility = View.VISIBLE
        progressAnimation.duration = maxDuration
        progressAnimation.setIntValues(maxDuration.toInt())
        progressAnimation.start()

        videoCapture.startRecording(File(outputDirectory.absolutePath + File.separator + "vid.mp4"), cameraExecutor, object : VideoCapture.OnVideoSavedCallback{
            override fun onVideoSaved(file: File) {
                runOnUiThread {
                    recording = false
                    binding.shutter.setImageResource(R.drawable.shutter_bg_video)
                    binding.progressBar.visibility = View.INVISIBLE
                    binding.progressBar.progress = 0
                    startUploadActivity(file.absolutePath)
                }
            }
            override fun onError(videoCaptureError: Int, message: String, cause: Throwable?) = mHandler.removeCallbacksAndMessages(null)
        })
        mHandler.postDelayed({
            videoCaptureStop()
        }, maxDuration)
    }

    @SuppressLint("RestrictedApi")
    fun videoCaptureStop(){
        val videoCapture = videoCapture ?: return
        mHandler.removeCallbacksAndMessages(null)
        progressAnimation.cancel()
        videoCapture.stopRecording()
        recording = false
    }

    private fun videoRecordToggle(){
        if (!recording) {
            videoCaptureStart()
        } else {
            videoCaptureStop()
        }
    }

    override fun onPause() {
        if (recording) {
            videoRecordToggle()
        }
        super.onPause()
    }

    private fun getOutputDirectory(): File {
        val mediaDir = externalMediaDirs.firstOrNull()?.let {
            File(it, resources.getString(R.string.app_name)).apply { mkdirs() } }
        return if (mediaDir != null && mediaDir.exists())
            mediaDir else filesDir
    }
/*
    private fun getFile(name : String) : File? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val resolver = contentResolver
            val contentValues = ContentValues()
            contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, "$name.mp4")
            contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4")
            contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DCIM + File.separator + "VSA")
            val videoUri: Uri? = resolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, contentValues)
            if (videoUri != null) File(videoUri.path!!) else null
        } else {
            val imagesDir = Environment.getExternalStorageDirectory().toString() + File.separator + "VSA"
            File(imagesDir).mkdir()
            File(imagesDir, "$name.mp4")
        }
    }
*/
    private fun requestCameraPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
            AlertDialog.Builder(this).setMessage("Camera, audio and storage permissions required").setPositiveButton("OK") { _, _ ->
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE), REQUEST_CAMERA_PERMISSION)
            }.setNegativeButton("Cancel") { _, _ -> finish() }.show()
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE), REQUEST_CAMERA_PERMISSION)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String?>, grantResults: IntArray) {
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.size != 1 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "ERROR: Permissions not granted", Toast.LENGTH_LONG).show()
                finish()
            } else {
                init()
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

}