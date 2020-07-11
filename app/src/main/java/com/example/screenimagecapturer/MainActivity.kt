package com.example.screenimagecapturer

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.Image
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity


class MainActivity : AppCompatActivity() {
    private lateinit var mediaProjectionManager: MediaProjectionManager
    private val REQUEST_CAPTURE = 1
    private var mResultCode = 0
    private var mResultData: Intent? = null
    private var mMediaProjection: MediaProjection? = null
    private lateinit var mVirtualDisplay: VirtualDisplay
    private val width = 480
    private val height = 720

    private lateinit var imageview: ImageView



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        imageview = findViewById<ImageView>(R.id.imageview)

        mediaProjectionManager = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        startActivityForResult(
                mediaProjectionManager.createScreenCaptureIntent(),
                REQUEST_CAPTURE
        )
    }


    // we must make this variable a field, but not a variable in the listener.
    lateinit var image: Image

    @SuppressLint("WrongConstant")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CAPTURE) {
            if (resultCode == RESULT_OK) {
                mResultData = data
                mResultCode = resultCode
                mMediaProjection = mediaProjectionManager.getMediaProjection(mResultCode, mResultData!!)
                Toast.makeText(this, "Capture Screen Successfully.", Toast.LENGTH_SHORT).show()


                // RGBA_8888 is the only constant that makes it work
                val imageReader = ImageReader.newInstance(width, height, PixelFormat.RGBA_8888, 3)

                mVirtualDisplay = mMediaProjection!!.createVirtualDisplay("ScreenCapture",
                        width, height, 1,
                        DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                        imageReader.surface, null, null
                )

                imageReader.setOnImageAvailableListener({
                    image = it.acquireLatestImage();
                    if (image != null) {

                        Log.e("Shiheng", image.format.toString())

                        val planes = image.planes
                        val buffer = planes[0].buffer



                        val pixelStride = planes[0].pixelStride
                        val rowStride = planes[0].rowStride
                        val rowPadding: Int = rowStride - pixelStride * width

                        val bitmap = Bitmap.createBitmap(width + rowPadding / pixelStride, height, Bitmap.Config.ARGB_8888)

                        bitmap.copyPixelsFromBuffer(buffer);
                        imageview.setImageBitmap(bitmap)

                        // TODO: Send this array
                        val arr = ByteArray(buffer.remaining())
                        buffer.get(arr)

                        image.close()
                    }
                }, null)


            } else {
                mMediaProjection = null
                Toast.makeText(this, "ERROR Capture Picture", Toast.LENGTH_SHORT).show()
            }
        }
    }

}








