package com.devis.compress_video

import android.Manifest
import android.app.Activity
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File

class MainActivity : AppCompatActivity() {

    companion object {
        private const val REQUEST_RECORD_VIDEO = 101
        private const val REQUEST_OPEN_VIDEO = 102
        private const val REQUEST_CODE_ASK_PERMISSIONS = 1
    }

    private var mFilePhotoTaken: File? = null
    private var mUriPhotoTaken: Uri? = null
    private var mImageUri: Uri? = null

    private val requiredSDKPermissions = arrayOf(
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.CAMERA
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initViewAction()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_RECORD_VIDEO -> {
                    mImageUri = if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
                        Uri.fromFile(mFilePhotoTaken)
                    } else {
                        mUriPhotoTaken
                    }

                    TestVideoActivity.startThisActivity(this, mImageUri?.path.toString())
                }
                REQUEST_OPEN_VIDEO -> {
                    if (data != null) {
                        val videoUri = data.data
                        Log.e("videoUri", getPath(this, videoUri!!).toString())
                        TestVideoActivity.startThisActivity(this, getPath(this, videoUri).toString())
                    }
                }
                REQUEST_CODE_ASK_PERMISSIONS -> {
                    intentVideo()
                }
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_CODE_ASK_PERMISSIONS -> {
                if ((grantResults.isNotEmpty()) && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    intentVideo()
                } else {
                    requestPermissions(requiredSDKPermissions, REQUEST_CODE_ASK_PERMISSIONS)
                }
            }
        }
    }

    private fun initViewAction() {
        btn_record_video.setOnClickListener {
            checkPermissions(REQUEST_RECORD_VIDEO)
        }

        btn_open_video.setOnClickListener {
            checkPermissions(REQUEST_OPEN_VIDEO)
        }
    }

    private fun checkPermissions(requestCode: Int) {
        when {
            (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED)
            && (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED)
            && (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED) && requestCode == REQUEST_OPEN_VIDEO -> {
                intentVideo()
            }
            (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED)
                    && (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED)
                    && (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED) && requestCode == REQUEST_RECORD_VIDEO -> {
                recordVideo()
            }
            else -> {
                requestPermissions(requiredSDKPermissions, REQUEST_CODE_ASK_PERMISSIONS)
            }
        }
    }

    private fun recordVideo() {
        val intent = Intent(MediaStore.ACTION_VIDEO_CAPTURE)
        if (intent.resolveActivity(packageManager) != null) {
            val storageDir: File? = if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
                getExternalFilesDir(Environment.DIRECTORY_MOVIES)
            } else {
                getExternalFilesDir("Videos")
            }
            try {
                mFilePhotoTaken = File.createTempFile(
                    "VID_",
                    ".mp4",
                    storageDir)
                if (mFilePhotoTaken != null) {
                    mUriPhotoTaken = if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
                        FileProvider.getUriForFile(
                            this,
                            BuildConfig.APPLICATION_ID + ".provider",
                            mFilePhotoTaken!!
                        )
                    } else {
                        Uri.fromFile(mFilePhotoTaken)
                    }
                    intent.putExtra("return-data", true)
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, mUriPhotoTaken)
                    if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP) {
                        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                        val resInfoList: List<ResolveInfo> = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
                        for (resolveInfo in resInfoList) {
                            val packageName = resolveInfo.activityInfo.packageName
                            grantUriPermission(packageName, mUriPhotoTaken, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }

                        startActivityForResult(intent, REQUEST_RECORD_VIDEO)
                    } else {
                        startActivityForResult(intent, REQUEST_RECORD_VIDEO)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("MainActivity", e.message.toString())
            }
        }
    }

    private fun intentVideo() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
        intent.type = "video/*"
        startActivityForResult(Intent.createChooser(intent, "Choose action"), REQUEST_OPEN_VIDEO)
    }

    fun getPath(context: Context?, uri: Uri): String? {
        val isKitKat = true
        // DocumentProvider
        if (DocumentsContract.isDocumentUri(context, uri)) { // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                val docId = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":").toTypedArray()
                val type = split[0]
                if ("primary".equals(type, ignoreCase = true)) {
                    return Environment.getExternalStorageDirectory().toString() + "/" + split[1]
                }
                // TODO handle non-primary volumes
            } else if (isDownloadsDocument(uri)) {
                val id = DocumentsContract.getDocumentId(uri)
                val contentUri = ContentUris.withAppendedId(
                    Uri.parse("content://downloads/public_downloads"),
                    java.lang.Long.valueOf(id)
                )
                return getDataColumn(
                    this,
                    contentUri,
                    null,
                    null
                )
            } else if (isMediaDocument(uri)) {
                val docId = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":").toTypedArray()
                val type = split[0]
                var contentUri: Uri? = null
                when (type) {
                    "image" -> {
                        contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                    }
                    "video" -> {
                        contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                    }
                    "audio" -> {
                        contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                    }
                }
                val selection = "_id=?"
                val selectionArgs = arrayOf(
                    split[1]
                )
                return getDataColumn(
                    this,
                    contentUri,
                    selection,
                    selectionArgs
                )
            }
        } else if ("content".equals(
                uri.scheme,
                ignoreCase = true
            )
        ) { // Return the remote address
            return if (isGooglePhotosUri(uri)) uri.lastPathSegment else getDataColumn(
                this,
                uri,
                null,
                null
            )
        } else if ("file".equals(uri.scheme, ignoreCase = true)) {
            return uri.path
        }
        return null
    }

    private fun isExternalStorageDocument(uri: Uri): Boolean {
        return "com.android.externalstorage.documents" == uri.authority
    }

    private fun isDownloadsDocument(uri: Uri): Boolean {
        return "com.android.providers.downloads.documents" == uri.authority
    }

    private fun isMediaDocument(uri: Uri): Boolean {
        return "com.android.providers.media.documents" == uri.authority
    }

    private fun isGooglePhotosUri(uri: Uri): Boolean {
        return "com.google.android.apps.PhotosMdl.content" == uri.authority
    }

    private fun getDataColumn(
        context: Context, uri: Uri?, selection: String?,
        selectionArgs: Array<String>?
    ): String? {
        val column = "_data"
        val projection = arrayOf(
            column
        )
        context.contentResolver.query(
            uri!!, projection, selection, selectionArgs,
            null
        ).use { cursor ->
            if (cursor != null && cursor.moveToFirst()) {
                val index = cursor.getColumnIndexOrThrow(column)
                return cursor.getString(index)
            }
        }
        return null
    }

}
