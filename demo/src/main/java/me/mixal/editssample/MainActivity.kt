package me.mixal.editssample

import android.Manifest
import me.mixal.editssample.imagepicker.utils.generateEditFile
import androidx.appcompat.app.AppCompatActivity
import android.graphics.Bitmap
import io.reactivex.disposables.CompositeDisposable
import android.content.Intent
import android.os.Bundle
import android.app.Dialog
import me.mixal.edits.BaseActivity
import me.mixal.edits.editor.ImageEditorIntentBuilder
import me.mixal.edits.editor.EditImageActivity
import android.widget.Toast
import android.os.Build
import me.mixal.editssample.imagepicker.activity.ImagePickerActivity
import android.content.pm.PackageManager
import android.util.Log
import android.view.View
import android.widget.ImageView
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import io.reactivex.android.schedulers.AndroidSchedulers
import me.mixal.edits.editor.utils.BitmapUtils
import java.lang.Exception

class MainActivity : AppCompatActivity(), View.OnClickListener {
    private var editResultLauncher: ActivityResultLauncher<Intent>? = null
    private var pickResultLauncher: ActivityResultLauncher<Intent>? = null
    private var loadingDialog: Dialog? = null
    private var imgView: ImageView? = null
    private var mainBitmap: Bitmap? = null
    private var path: String? = null
    private var imageWidth = 0
    private var imageHeight = 0

    private val compositeDisposable = CompositeDisposable()

    companion object {
        const val REQUEST_PERMISSION_STORAGE = 1
        const val ACTION_REQUEST_EDIT_IMAGE = 9
    }

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setupActivityResultLaunchers()
        initView()
    }

    private fun setupActivityResultLaunchers() {
        pickResultLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result: ActivityResult ->
            if (result.resultCode == RESULT_OK) {
                val data = result.data
                handleSelectFromAlbum(data)
            }
        }
        editResultLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result: ActivityResult ->
            if (result.resultCode == RESULT_OK) {
                val data = result.data
                handleEditorImage(data)
            }
        }
    }

    override fun onDestroy() {
        compositeDisposable.dispose()
        super.onDestroy()
    }

    private fun initView() {
        val metrics = resources.displayMetrics
        imageWidth = metrics.widthPixels
        imageHeight = metrics.heightPixels
        imgView = findViewById(R.id.img)
        val selectAlbum = findViewById<View>(R.id.photo_picker)
        val editImage = findViewById<View>(R.id.edit_image)
        selectAlbum.setOnClickListener(this)
        editImage.setOnClickListener(this)
        loadingDialog = BaseActivity.getLoadingDialog(
            this, R.string.loading,
            false
        )
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.edit_image -> editImageClick()
            R.id.photo_picker -> selectFromAlbum()
        }
    }

    private fun editImageClick() {
        val outputFile = generateEditFile()
        try {
            assert(outputFile != null)
            val intent = ImageEditorIntentBuilder(this, path, outputFile!!.absolutePath)
                .withAddText()
                .withPaintFeature()
                .withFilterFeature()
                .withRotateFeature()
                .withCropFeature()
                .withBrightnessFeature()
                .withSaturationFeature()
                .withBeautyFeature()
                .withStickerFeature()
                .withEditorTitle("Photo Editor")
                .forcePortrait(true)
                .setSupportActionBarVisibility(false)
                .build()
            EditImageActivity.start(editResultLauncher, intent /*, this*/)
        } catch (e: Exception) {
            Toast.makeText(this, R.string.not_selected, Toast.LENGTH_SHORT).show()
            Log.e("Demo App", e.message!!)
        }
    }

    private fun selectFromAlbum() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            openAlbumWithPermissionsCheck()
        } else {
            openAlbum()
        }
    }

    private fun openAlbum() {
        val intent = Intent(this, ImagePickerActivity::class.java)
        pickResultLauncher!!.launch(intent)
    }

    private fun openAlbumWithPermissionsCheck() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                REQUEST_PERMISSION_STORAGE
            )
            return
        }
        openAlbum()
    }

    private fun handleEditorImage(data: Intent?) {
        var newFilePath = data!!.getStringExtra(ImageEditorIntentBuilder.OUTPUT_PATH)
        val isImageEdit = data.getBooleanExtra(EditImageActivity.IS_IMAGE_EDITED, false)
        if (isImageEdit) {
            Toast.makeText(this, getString(R.string.save_path, newFilePath), Toast.LENGTH_LONG)
                .show()
        } else {
            newFilePath = data.getStringExtra(ImageEditorIntentBuilder.SOURCE_PATH)
        }
        loadImage(newFilePath)
    }

    private fun handleSelectFromAlbum(data: Intent?) {
        path = data!!.getStringExtra(ImagePickerActivity.BUNDLE_EXTRA_IMAGE_PATH)
        loadImage(path)
    }

    private fun loadImage(imagePath: String?) {
        val applyRotationDisposable = loadBitmapFromFile(imagePath)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe { loadingDialog!!.show() }
            .doFinally { loadingDialog!!.dismiss() }
            .subscribe({ sourceBitmap: Bitmap -> setMainBitmap(sourceBitmap) })
            { e: Throwable ->
                e.printStackTrace()
                Toast.makeText(this, R.string.load_error, Toast.LENGTH_SHORT).show()
            }
        compositeDisposable.add(applyRotationDisposable)
    }

    private fun setMainBitmap(sourceBitmap: Bitmap) {
        if (mainBitmap != null) {
            mainBitmap!!.recycle()
            mainBitmap = null
            System.gc()
        }
        mainBitmap = sourceBitmap
        imgView!!.setImageBitmap(mainBitmap)
    }

    private fun loadBitmapFromFile(filePath: String?): Single<Bitmap> {
        return Single.fromCallable {
            BitmapUtils.getSampledBitmap(
                filePath,
                imageWidth / 4,
                imageHeight / 4
            )
        }
    }
}