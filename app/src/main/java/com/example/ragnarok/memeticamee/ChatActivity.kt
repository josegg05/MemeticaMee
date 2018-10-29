package com.example.ragnarok.memeticamee

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.media.MediaRecorder
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.View
import android.webkit.MimeTypeMap
import android.widget.ProgressBar
import android.widget.Toast
import com.example.ragnarok.memeticamee.model.FileMessage
import com.example.ragnarok.memeticamee.model.ImageMessage
import com.example.ragnarok.memeticamee.model.TextMessage
import com.example.ragnarok.memeticamee.model.User
import com.example.ragnarok.memeticamee.recyclerview.item.FileMessageItem
import com.example.ragnarok.memeticamee.recyclerview.item.ImageMessageItem
import com.example.ragnarok.memeticamee.util.FirestoreUtil
import com.example.ragnarok.memeticamee.util.StorageUtil
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.OnItemClickListener
import com.xwray.groupie.Section
import com.xwray.groupie.kotlinandroidextensions.Item
import com.xwray.groupie.kotlinandroidextensions.ViewHolder
import kotlinx.android.synthetic.main.activity_chat.*
import kotlinx.android.synthetic.main.activity_sing_in.*
import kotlinx.android.synthetic.main.item_file_message.*
import org.apache.commons.io.FileUtils
import org.apache.commons.io.FilenameUtils
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.util.*

private const val RC_SELECT_IMAGE = 2
private const val RC_SELECT_FILE = 3
private const val RC_SELECT_IMAGE_FILE = 4

private const val REQUEST_CODE_CAMERA_PERMISSION = 9
private const val REQUEST_CODE_RECORD_PERMISSION = 8
private const val REQUEST_CODE_WRITE_EXTERNAL_PERMISSION = 10
private const val REQUEST_CODE_READ_EXTERNAL_PERMISSION = 11


class ChatActivity : AppCompatActivity() {

    private lateinit var currentChannelId: String
    private lateinit var currentUser: User
    private lateinit var otherUserId: String

    private lateinit var messagesListenerRegistration: ListenerRegistration
    private var shouldInitRecyclerView = true
    private lateinit var messagesSection: Section


    private var cameraPermissionGranted = false
        get() = checkSelfPermission(android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED

    private var permissionToRecordAccepted = false
        get() = checkSelfPermission(android.Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED

    private var permissionToWriteAccepted = false
        get() = checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED

    private var mFileName: String = ""
    lateinit var file: File
    private var mRecorder: MediaRecorder? = null

    private val currentUserRef: StorageReference
        get() = StorageUtil.storageInstance.reference
                .child(FirebaseAuth.getInstance().currentUser?.uid
                        ?: throw NullPointerException("UID is null."))



    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<String>,
            grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_CODE_RECORD_PERMISSION -> {
                permissionToRecordAccepted = if (requestCode == REQUEST_CODE_RECORD_PERMISSION) {
                    grantResults[0] == PackageManager.PERMISSION_GRANTED
                } else {
                    false
                }
                if (!permissionToRecordAccepted) finish()
            }
            REQUEST_CODE_CAMERA_PERMISSION -> {
                cameraPermissionGranted = if (requestCode == REQUEST_CODE_CAMERA_PERMISSION) {
                    grantResults[0] == PackageManager.PERMISSION_GRANTED
                } else {
                    false
                }
                if (!cameraPermissionGranted) finish()
            }
            REQUEST_CODE_WRITE_EXTERNAL_PERMISSION -> {
                permissionToWriteAccepted = if (requestCode == REQUEST_CODE_WRITE_EXTERNAL_PERMISSION) {
                    grantResults[0] == PackageManager.PERMISSION_GRANTED
                } else {
                    false
                }
                if (!permissionToWriteAccepted) finish()
            }
        }
    }


    private fun onRecord(start: Boolean) = if (start) {
        startRecording()
    } else {
        stopRecording()
    }

    private fun startRecording() {
        mRecorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            //setOutputFile(mFileName)
            setOutputFile(file.absolutePath)
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)

            try {
                prepare()
            } catch (e: IOException) {
                Log.e("AudioRecordTest", "prepare() failed")
            }

            start()
            Toast.makeText(this@ChatActivity, "Record Starts", Toast.LENGTH_SHORT).show()
        }
    }

    private fun stopRecording() {
        mRecorder?.apply {
            stop()
            release()
            Toast.makeText(this@ChatActivity, "Record Stops", Toast.LENGTH_SHORT).show()
        }
        mRecorder = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        //requestPermissions(arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE), REQUEST_CODE_WRITE_EXTERNAL_PERMISSION)
        //requestPermissions(arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE), REQUEST_CODE_READ_EXTERNAL_PERMISSION)

        fab_send_audio.hide()
        mFileName = "${filesDir.absolutePath}/audiorecordtest.3gp"
        var mStartRecording = true


        fab_send_file.hide()
        fab_send_photo.hide()
        fab_send_image.hide()
        downloadProgressBar.visibility = View.GONE

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = intent.getStringExtra(AppConstants.USER_NAME)

        FirestoreUtil.getCurrentUser {
            currentUser = it
        }

        otherUserId = intent.getStringExtra(AppConstants.USER_ID)
        FirestoreUtil.getOrCreateChatChannel(otherUserId) { channelId ->
            currentChannelId = channelId

            messagesListenerRegistration =
                    FirestoreUtil.addChatMessagesListener(channelId, this, this::onMessageChanged)

            imageView_send.setOnClickListener {
                val messageToSend =
                        TextMessage(editText_message.text.toString(), Calendar.getInstance().time,
                                FirebaseAuth.getInstance().currentUser!!.uid,
                                otherUserId, currentUser.name)
                editText_message.setText("")
                FirestoreUtil.sendMessage(messageToSend, channelId)
            }


            fab_send.setOnClickListener {
                fab_send_audio.show()
                fab_send_file.show()
                fab_send_photo.show()
                fab_send_image.show()
            }

            fab_send_file.setOnClickListener {
                val intent = Intent().apply {
                    type = "*/*"
                    action = Intent.ACTION_GET_CONTENT
                    //putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/jpeg", "image/png"))
                }
                startActivityForResult(Intent.createChooser(intent, "Select file"), RC_SELECT_FILE)
            }

            fab_send_image.setOnClickListener {
                val intent = Intent().apply {
                    type = "image/*"
                    action = Intent.ACTION_GET_CONTENT
                    putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/jpeg", "image/png"))
                }
                startActivityForResult(Intent.createChooser(intent, "Select image"), RC_SELECT_IMAGE_FILE)
            }

            fab_send_photo.setOnClickListener {
                if (!cameraPermissionGranted) {
                    requestCameraPermission()
                }
                else {
                    val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    //startActivityForResult(Intent.createChooser(intent, "Camera"), RC_SELECT_IMAGE)
                    startActivityForResult(intent, RC_SELECT_IMAGE)
                }
            }


            fab_send_audio.setOnClickListener {
                if (!permissionToRecordAccepted) {
                    requestRecordPermission()
                }
                else {
                    if (!permissionToWriteAccepted) {
                        requestWritePermission()
                    }
                    else {
                        var path = File(Environment.getExternalStorageDirectory().getPath() + "/Music")
                        file = File.createTempFile("temporary", ".3gp", path)
                        onRecord(mStartRecording)
                        mStartRecording = !mStartRecording
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        fab_send_audio.hide()
        fab_send_file.hide()
        fab_send_photo.hide()
        fab_send_image.hide()
    }

    override fun onStop() {
        super.onStop()
        mRecorder?.release()
        mRecorder = null
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == RC_SELECT_IMAGE && resultCode == Activity.RESULT_OK && data != null) {
            val selectedImageBmp = data!!.extras!!.get("data") as Bitmap
            val outputStream = ByteArrayOutputStream()

            selectedImageBmp.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
            val selectedImageBytes = outputStream.toByteArray()

            StorageUtil.uploadMessageImage(selectedImageBytes, this) { imagePath ->
                Toast.makeText(this@ChatActivity, "Foto Enviada", Toast.LENGTH_SHORT).show()
                val messageToSend =
                        ImageMessage(imagePath, Calendar.getInstance().time,
                                FirebaseAuth.getInstance().currentUser!!.uid,
                                otherUserId, currentUser.name)
                FirestoreUtil.sendMessage(messageToSend, currentChannelId)
            }
        }

        if (requestCode == RC_SELECT_FILE && resultCode == Activity.RESULT_OK &&
                data != null && data.data != null) {
            //val uri = data.data
            //var file = contentResolver.openInputStream(uri)
            //val filesize = (file.available()/1000).toString()
            //val filename = java.io.File(data.data.path).nameWithoutExtension
            //val name = uri.lastPathSegment

            /*
            StorageUtil.uploadMessageFile(uri, this) { filePath ->
                Toast.makeText(this@ChatActivity, "Archivo Enviado", Toast.LENGTH_SHORT).show()
                val messageToSend =
                        FileMessage(filePath, filename, Calendar.getInstance().time,
                                FirebaseAuth.getInstance().currentUser!!.uid,
                                otherUserId, currentUser.name)
                FirestoreUtil.sendMessage(messageToSend, currentChannelId)
            }*/

            val uri = data.data
            var file = contentResolver.openInputStream(uri)
            val filesize = (file.available()/1000).toString()
            val filename = java.io.File(data.data.path).nameWithoutExtension

            val cr  = this.getContentResolver()
            val mime = MimeTypeMap.getSingleton()
            val ext = mime.getExtensionFromMimeType(cr.getType(uri))

            val name = uri.lastPathSegment
            var mReference = currentUserRef.child(uri.lastPathSegment)
            try {
                mReference.putFile(uri).addOnSuccessListener {
                    taskSnapshot: UploadTask.TaskSnapshot? -> var url = taskSnapshot!!.downloadUrl.toString()
                    Toast.makeText(this, "Archivo Enviado", Toast.LENGTH_LONG).show()
                    val messageToSend =
                            //FileMessage(mReference.toString(), name, Calendar.getInstance().time,
                            FileMessage(FirebaseAuth.getInstance().currentUser!!.uid+"/"+name, filename, ext, Calendar.getInstance().time,
                                    FirebaseAuth.getInstance().currentUser!!.uid,
                                    otherUserId, currentUser.name)
                    FirestoreUtil.sendMessage(messageToSend, currentChannelId)
                }.addOnProgressListener { taskSnapshot ->
                    Toast.makeText(this, "Subiendo", Toast.LENGTH_LONG).show()
                }
            }catch (e: Exception) {
                Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show()
            }
        }

        if (requestCode == RC_SELECT_IMAGE_FILE && resultCode == Activity.RESULT_OK &&
                data != null && data.data != null) {
            val selectedImagePath = data.data

            val selectedImageBmp = MediaStore.Images.Media.getBitmap(contentResolver, selectedImagePath)

            val outputStream = ByteArrayOutputStream()

            selectedImageBmp.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
            val selectedImageBytes = outputStream.toByteArray()

            StorageUtil.uploadMessageImage(selectedImageBytes, this) { imagePath ->
                Toast.makeText(this@ChatActivity, "Imagen Enviada", Toast.LENGTH_SHORT).show()
                val messageToSend =
                        ImageMessage(imagePath, Calendar.getInstance().time,
                                FirebaseAuth.getInstance().currentUser!!.uid,
                                otherUserId, currentUser.name)
                FirestoreUtil.sendMessage(messageToSend, currentChannelId)
            }
        }
    }


    private fun onMessageChanged(messages: List<Item>){
        fun init() {
            recycler_view_messages.apply {
                layoutManager = LinearLayoutManager(this@ChatActivity)
                adapter = GroupAdapter<ViewHolder>().apply {
                    messagesSection = Section(messages)
                    this.add(messagesSection)
                    setOnItemClickListener(onItemClick)
                }
            }
            shouldInitRecyclerView = false
        }

        fun updateItems() = messagesSection.update(messages)

        if (shouldInitRecyclerView)
            init()
        else
            updateItems()

        recycler_view_messages.scrollToPosition(recycler_view_messages.adapter!!.itemCount - 1)
    }

    //exp3i
    private fun requestCameraPermission() {
        if (shouldShowRequestPermissionRationale(android.Manifest.permission.CAMERA)) {
            AlertDialog.Builder(this)
                    .setMessage(getString(R.string.camera_permissions_rationale))
                    .setPositiveButton(getString(R.string.ok)) { _, _ ->
                        requestPermissions(arrayOf(android.Manifest.permission.CAMERA), REQUEST_CODE_CAMERA_PERMISSION)
                    }
                    .setNegativeButton(getString(R.string.cancel), null)
                    .create()
                    .show();

        } else {
            requestPermissions(arrayOf(android.Manifest.permission.CAMERA), REQUEST_CODE_CAMERA_PERMISSION)
        }
    }

    private fun requestRecordPermission() {
        if (shouldShowRequestPermissionRationale(android.Manifest.permission.RECORD_AUDIO)) {
            AlertDialog.Builder(this)
                    .setMessage(getString(R.string.record_permissions_rationale))
                    .setPositiveButton(getString(R.string.ok)) { _, _ ->
                        requestPermissions(arrayOf(android.Manifest.permission.RECORD_AUDIO), REQUEST_CODE_RECORD_PERMISSION)
                    }
                    .setNegativeButton(getString(R.string.cancel), null)
                    .create()
                    .show();

        } else {
            requestPermissions(arrayOf(android.Manifest.permission.RECORD_AUDIO), REQUEST_CODE_RECORD_PERMISSION)
        }
    }

    private fun requestWritePermission() {
        if (shouldShowRequestPermissionRationale(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            AlertDialog.Builder(this)
                    .setMessage(getString(R.string.record_permissions_rationale))
                    .setPositiveButton(getString(R.string.ok)) { _, _ ->
                        requestPermissions(arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE), REQUEST_CODE_WRITE_EXTERNAL_PERMISSION)
                    }
                    .setNegativeButton(getString(R.string.cancel), null)
                    .create()
                    .show();

        } else {
            requestPermissions(arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE), REQUEST_CODE_WRITE_EXTERNAL_PERMISSION)
        }
    }
    //exp3f}


    //exp4
    private val onItemClick = OnItemClickListener {item, view ->
        if (item is FileMessageItem){
            downloadToLocalFile(StorageUtil.storageInstance.getReference(item.message.filePath), item.message.extension)
        }
        else if (item is ImageMessageItem){
            downloadToLocalImage(StorageUtil.storageInstance.getReference(item.message.imagePath))
        }
    }


    private fun downloadToLocalFile(fileReference: StorageReference, ext: String){
        if (!permissionToWriteAccepted) {
            requestWritePermission()
        }
        else{
            if (fileReference != null){
                try {
                    val dir = File(Environment.getExternalStorageDirectory().toString() + File.separator + "MemeticaMee")
                    if (!dir.exists()) {
                        dir.mkdirs()
                    }
                    val file = File(Environment.getExternalStorageDirectory().toString() + File.separator + "MemeticaMee" + "/" + UUID.randomUUID() + '.' + ext)

                    fileReference.getFile(file)
                            .addOnSuccessListener {
                                Toast.makeText(this,"Descarga Finalizada", Toast.LENGTH_SHORT).show()
                                downloadProgressBar.visibility = View.GONE
                            }
                            .addOnProgressListener {
                                //Toast.makeText(this,"Descargando", Toast.LENGTH_SHORT).show()
                                downloadProgressBar.visibility = View.VISIBLE
                            }
                }catch (e: IOException){
                    e.printStackTrace()
                    Toast.makeText(this,"Descarga Fallida", Toast.LENGTH_SHORT).show()
                    downloadProgressBar.visibility = View.GONE
                }
            }
        }
    }

    private fun downloadToLocalImage(fileReference: StorageReference){
        if (!permissionToWriteAccepted) {
            requestWritePermission()
        }
        else{
            if (fileReference != null){
                try {
                    val dir = File(Environment.getExternalStorageDirectory().toString() + File.separator + "MemeticaMee")
                    if (!dir.exists()) {
                        dir.mkdirs()
                    }
                    val file = File(Environment.getExternalStorageDirectory().toString() + File.separator + "MemeticaMee" + "/" + UUID.randomUUID() + ".jpeg")

                    fileReference.getFile(file)
                            .addOnSuccessListener {
                                Toast.makeText(this,"Descarga Finalizada", Toast.LENGTH_SHORT).show()
                                downloadProgressBar.visibility = View.GONE
                            }
                            .addOnProgressListener {
                                //Toast.makeText(this,"Descargando", Toast.LENGTH_SHORT).show()
                                downloadProgressBar.visibility = View.VISIBLE
                            }
                }catch (e: IOException){
                    e.printStackTrace()
                    Toast.makeText(this,"Descarga Fallida", Toast.LENGTH_SHORT).show()
                    downloadProgressBar.visibility = View.GONE
                }
            }
        }
    }

    //exp4
}
