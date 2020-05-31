package com.htueko.android.imagedetectiondemo.ui

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.htueko.android.imagedetectiondemo.R
import com.htueko.android.imagedetectiondemo.util.GET_IMAGE_FROM_GALLERY_REQUEST_CODE
import com.htueko.android.imagedetectiondemo.util.PermissionObject
import com.htueko.android.imagedetectiondemo.util.createImageFile
import com.yalantis.ucrop.UCrop
import kotlinx.android.synthetic.main.content_main.*


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar))

        // click event handler
        btn_gallery_main.setOnClickListener {
            requestAndGetTheImage()
        }

    }

    // result to handle
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == GET_IMAGE_FROM_GALLERY_REQUEST_CODE) {
                // return from image gallery
                val sourceUri: Uri = data?.data!!
                val file = createImageFile(this)
                val destinationUri = Uri.fromFile(file)
                openCropActivity(sourceUri, destinationUri)
            } else if (requestCode == UCrop.REQUEST_CROP) {
                val uri = UCrop.getOutput(data!!)
                showImage(uri!!)
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    // check permission and act according to result
    private fun requestAndGetTheImage(){
        PermissionObject.toCheckAndRequestPermissions(
            this,
            arrayOf(PermissionObject.READ_EXTERNAL_STORAGE_PERMISSION),
            PermissionObject.REQUEST_PERMISSION_CODE,
            resources.getString(R.string.title_permission),
            onSuccess = ::openImagesDocument
        )
    }

    // to open gallery
    private fun openImagesDocument() {
        val pictureIntent = Intent(Intent.ACTION_GET_CONTENT)
        pictureIntent.type = "image/*"
        pictureIntent.addCategory(Intent.CATEGORY_OPENABLE)
        val mimeTypes =
            arrayOf("image/jpeg", "image/png")
        pictureIntent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
        startActivityForResult(
            Intent.createChooser(pictureIntent, "Select Picture"),
            GET_IMAGE_FROM_GALLERY_REQUEST_CODE
        )
    }

    // to crop image
    private fun openCropActivity(
        sourceUri: Uri,
        destinationUri: Uri
    ) {
        UCrop.of(sourceUri, destinationUri)
            .withMaxResultSize(300, 300)
            .withAspectRatio(5f, 5f)
            .start(this)
    }

    // to show image
    private fun showImage(imageUri: Uri) {
       Glide.with(this).load(imageUri).into(imv_placeholder_main)
    }

}
