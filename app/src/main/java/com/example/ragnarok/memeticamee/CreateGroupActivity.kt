package com.example.ragnarok.memeticamee

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.os.PersistableBundle
import android.provider.MediaStore
import android.support.v7.app.AppCompatActivity
import com.example.ragnarok.memeticamee.glide.GlideApp
import com.example.ragnarok.memeticamee.util.FirestoreUtil
import com.example.ragnarok.memeticamee.util.StorageUtil
import kotlinx.android.synthetic.main.activity_create_group.*
import kotlinx.android.synthetic.main.fragment_my_account.*
import kotlinx.android.synthetic.main.fragment_my_account.view.*
import org.jetbrains.anko.support.v4.toast
import org.jetbrains.anko.toast
import java.io.ByteArrayOutputStream

class CreateGroupActivity : AppCompatActivity() {

    private val RC_SELECT_IMAGE = 2
    private lateinit var selectedImageBytes: ByteArray


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_group)

        imageView_group_picture.setOnClickListener{
            val intent = Intent().apply {
                type = "image/*"
                action = Intent.ACTION_GET_CONTENT
                putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/jpeg", "image/png"))
            }
            startActivityForResult(Intent.createChooser(intent,"Select Image"), RC_SELECT_IMAGE)
        }

        btnCreate.setOnClickListener {
            if (::selectedImageBytes.isInitialized)
                StorageUtil.uploadGroupPhoto(selectedImageBytes) { imagePath ->
                    FirestoreUtil.createGroup(editText_group_name.text.toString(),
                            editText_group_bio.text.toString(), imagePath)
                }
            else
                FirestoreUtil.createGroup(editText_group_name.text.toString(),
                        editText_group_bio.text.toString(), null)
            toast("Grupo Creado")
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        if (requestCode == RC_SELECT_IMAGE && resultCode == Activity.RESULT_OK &&
                data != null && data.data != null) {
            val selectedImagePath = data.data
            val selectedImageBmp = MediaStore.Images.Media
                    .getBitmap(this.contentResolver, selectedImagePath)

            val outputStream = ByteArrayOutputStream()
            selectedImageBmp.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
            selectedImageBytes = outputStream.toByteArray()

            GlideApp.with(this)
                    .load(selectedImageBytes)
                    .placeholder(R.drawable.ic_people_black_24dp)
                    .into(imageView_group_picture)

        }
        toast("Ok")
    }

}