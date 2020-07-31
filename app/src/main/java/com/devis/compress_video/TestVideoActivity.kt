package com.devis.compress_video

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.location.Geocoder
import android.media.MediaMetadataRetriever
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.github.hiteshsondhi88.libffmpeg.FFmpeg
import com.github.hiteshsondhi88.libffmpeg.FFmpegExecuteResponseHandler
import com.github.hiteshsondhi88.libffmpeg.LoadBinaryResponseHandler
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException
import kotlinx.android.synthetic.main.activity_test_record_video.*
import kotlinx.coroutines.Job
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.*

/**
 * Created by devis on 18/06/20
 */

class TestVideoActivity : AppCompatActivity(), MainView {

    companion object {
        fun startThisActivity(context: Context, videoPath: String) {
            val intent = Intent(context, TestVideoActivity::class.java)
            intent.putExtra("video", videoPath)
            context.startActivity(intent)
        }
    }

    private lateinit var mPresenter: MainPresenter
    private lateinit var imageRoot: File
    private lateinit var mProgressDialog: ProgressDialog

    private var ffmpeg: FFmpeg? = null
    private var mVideoPath: String? = null
    private var fileName = ""

    private val appDirectoryName = "compress_video"

    override val job = Job()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test_record_video)

        mPresenter = MainPresenter(this)

        mVideoPath = intent.getStringExtra("video")
        imageRoot = File(Environment.getExternalStorageDirectory(), "/$appDirectoryName")
        fileName = "VID_${System.currentTimeMillis() / 1000}.mp4"

        initFFmpegLibrary()
        initProgressDialog()
        initView()
        initViewAction()
        getMetaData()
        //saveVideo()
        //compressVideo()
    }

    override fun onResume() {
        super.onResume()
        initView()
    }

    override fun onSuccessPostVideo(result: ResultMdl) {
        Toast.makeText(this, result.result, Toast.LENGTH_SHORT).show()
        Log.d("onSuccessPostVideo", result.result)
    }

    override fun onErrorPostVideo(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        Log.e("onErrorPostVideo", message)
    }

    override fun showLoading() {
        mProgressDialog.show()
    }

    override fun stopLoading() {
        mProgressDialog.dismiss()
    }

    private fun initProgressDialog() {
        mProgressDialog = ProgressDialog(this)
        mProgressDialog.setCancelable(false)
        mProgressDialog.setMessage("Uploading...")
    }

    private fun initView() {
        videoview_test.setOnPreparedListener {
            it.isLooping = true
        }
        videoview_test.setVideoPath(mVideoPath)
        videoview_test.start()
    }

    private fun initViewAction() {
        btn_upload.setOnClickListener {
            /*val videoBody = File(Environment.getExternalStorageDirectory(),
                "$appDirectoryName/$fileName").asRequestBody("video/mp4".toMediaTypeOrNull())*/
            val videoBody = File(mVideoPath.toString()).asRequestBody("video/mp4".toMediaTypeOrNull())
            mPresenter.postVideo(videoBody)
        }
    }

    private fun saveVideo() {
        try {
            val imageRoot = File(
                Environment.getExternalStorageDirectory(), appDirectoryName)
            if (!(imageRoot.exists() && imageRoot.isDirectory)) {
                imageRoot.mkdir()
            }

            val file = File(imageRoot, fileName)
            val inputStream = FileInputStream(File(mVideoPath.toString()))
            val outputStream = FileOutputStream(file)
            val byteArray = ByteArray(1024)
            var len: Int

            do {
                len = inputStream.read(byteArray)
                if (len != -1) {
                    outputStream.write(byteArray, 0, len)
                } else {
                    break
                }
            } while (true)

            inputStream.close()
            outputStream.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun initFFmpegLibrary() {
        ffmpeg = FFmpeg.getInstance(this)
        try {
            ffmpeg?.loadBinary(object : LoadBinaryResponseHandler() {})
        } catch (e: FFmpegNotSupportedException) {
            e.printStackTrace()
        }
    }

    private fun compressVideo() {
        if (!(imageRoot.exists() && imageRoot.isDirectory)) {
            imageRoot.mkdir()
        }

        val cmd = arrayOf("-i", "$mVideoPath", "-b", "800k", "${Environment.getExternalStorageDirectory()}/$appDirectoryName/$fileName")
        //val cmd = arrayOf("-i", "${Environment.getExternalStorageDirectory()}/${appDirectoryName}/${fileName}", "-vcodec", "h264", "-b:v", "1000k", "-acodec", "mp3" ,"-preset", "ultrafast", "${Environment.getExternalStorageDirectory()}/$appDirectoryName/sample_output.mp4")
        //val cmd = arrayOf("-i", "$mVideoPath", "${Environment.getExternalStorageDirectory()}/$appDirectoryName/${fileName}")
        try {
            val progressBar = ProgressDialog(this)
            progressBar.setCancelable(false)
            progressBar.setMessage("Compressing...")
            progressBar.show()

            ffmpeg?.execute(cmd, object : FFmpegExecuteResponseHandler {
                override fun onFinish() {
                    Toast.makeText(this@TestVideoActivity, "video saved", Toast.LENGTH_SHORT).show()
                    progressBar.dismiss()
                }

                override fun onSuccess(message: String?) {
                    Log.d("ffmpeg", message.toString())
                }

                override fun onFailure(message: String?) {
                    Log.e("ffmpeg", message.toString())
                }

                override fun onProgress(message: String?) {
                }

                override fun onStart() {
                }
            })
        } catch (e: FFmpegCommandAlreadyRunningException) {
            e.printStackTrace()
        }
    }

    private fun getMetaData() {
        val metadataRetriever = MediaMetadataRetriever()
        metadataRetriever.setDataSource(mVideoPath)
        val location = metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_LOCATION)
        val duration = (metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION).toLong()/1000)
        val date = metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DATE)
        val geocode = Geocoder(this, Locale.getDefault())
        try {
            val size = (File(mVideoPath.toString()).length()/1024).toDouble()
            Log.e("location", location)
            Log.e("duration", duration.toString())
            Log.e("date", date)
            Log.e("size", size.toString())
            val lat = location.substringBefore("+").toDouble()
            val lng = location.substringAfter("+").substringBefore("/").toDouble()
            val address = geocode.getFromLocation(lat, lng, 1)
            val obj = address[0]
            val newAddress = obj.getAddressLine(0)
            val thoroughFare = obj.thoroughfare
            val city = obj.subAdminArea

            Log.e("address", newAddress)
            Log.e("thoroughFare", thoroughFare)
            Log.e("city", city)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}