package net.cyberplanete.drawingkid

import android.Manifest
import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.media.MediaScannerConnection
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.get
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.cyberplanete.drawingkid.databinding.ActivityMainBinding
import net.cyberplanete.drawingkid.databinding.DialogBrushSizeBinding
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.lang.Exception

class MainActivity : AppCompatActivity() {

    //private var drawingView : DrawingView? = null
    private lateinit var bindingDialogBrushSize: DialogBrushSizeBinding
    private lateinit var bindingMainActivity: ActivityMainBinding
    private lateinit var brushDialog: Dialog
    var customProgressDialog: Dialog? = null

    // 1 - Accès à la palette de couleur
    private var mImageButtonCurrentPaint: ImageButton? = null

    /**
     * ********* REQUEST PERMISSIONS ***********
     */
    val requestPermission: ActivityResultLauncher<Array<String>> =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            permissions.entries.forEach {
                val perMissionName = it.key
                val isGranted = it.value
                //if permission is granted show a toast and perform operation
                if (isGranted) {
                    Toast.makeText(
                        this@MainActivity,
                        "Permission granted now you can read the storage files.",
                        Toast.LENGTH_LONG
                    ).show()
                    val pickIntent =
                        Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    openGalleryLauncher.launch(pickIntent)
                } else {
                    //Displaying another toast if permission is not granted and this time focus on
                    //    Read external storage
                    if (perMissionName == Manifest.permission.READ_EXTERNAL_STORAGE)
                        Toast.makeText(
                            this@MainActivity,
                            "Oops you just denied the permission.",
                            Toast.LENGTH_LONG
                        ).show()
                }
            }

        }

    /**
     *  ON CREATE
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bindingMainActivity = ActivityMainBinding.inflate(layoutInflater)
        setContentView(bindingMainActivity.root)

        /// J'intègre le dialog dans la page MainActivity
        bindingDialogBrushSize = DialogBrushSizeBinding.inflate(layoutInflater)
        brushDialog = Dialog(this)
        brushDialog.setContentView(bindingDialogBrushSize.root)
        brushDialog.setTitle("Taille pinceau: ")


        bindingMainActivity.drawingView?.setTaillePinceau(20.toFloat())


        /// 2 - Accès à la palette de couleur et configuration de la couleur du pinceau
        /// val linearLayoutPaintColors = this.findViewById<ImageButton>(R.id.ll_paint_colors) ou
        val linearLayoutPaintColors = bindingMainActivity.llPaintColors
        //Couleur skin à l'index 1
        mImageButtonCurrentPaint = linearLayoutPaintColors[1] as ImageButton
        //Configuration du contour de la couleur de l'icone lorsque celui-ci est séléctionné
        mImageButtonCurrentPaint!!.setImageDrawable(
            ContextCompat.getDrawable(
                this,
                R.drawable.pallet_selected
            )
        )

        /// Afficher le dialog des pinceaux
        val ib_brush = bindingMainActivity.ibBrush
        ib_brush.setOnClickListener()
        {
            showBrushSizeChooserDialog()
        }
        // Bouton gallery -- Changer l'image du background
        val ibGallery: ImageButton = bindingMainActivity.ibGallery
        ibGallery.setOnClickListener()
        {
            requestStoragePermission()
        }

        val ibUndo: ImageButton = bindingMainActivity.ibUndo
        ibUndo.setOnClickListener()
        {
            bindingMainActivity.drawingView.onClickUndo()
        }

        val ibRedo: ImageButton = bindingMainActivity.ibRedo
        ibRedo.setOnClickListener()
        {
            bindingMainActivity.drawingView.onClickRedo()
        }

        val ibSave: ImageButton = bindingMainActivity.ibSave
        ibSave.setOnClickListener()
        {
            if (isReadStorageAllowed()) {
                showProgressDialog()
                lifecycleScope.launch()
                {
                    val frameLayoutViewImported: FrameLayout =
                        findViewById(R.id.fl_drawing_view_container)
                    val myBitmap: Bitmap = getBitmapFromView(frameLayoutViewImported)
                    sauvegardeImageBitmap(myBitmap)

                }
            }

        }
    }


    /**
     * ********* LAUNCHER PICK PHOTO ***********
     * Method is used to launch the dialog to select different brush sizes.
     */
    val openGalleryLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult())
        {
            //Après validation je change le fond d'ecran par la photo selectionnée
                result ->
            if (result.resultCode == RESULT_OK && result.data != null) {
                val imageBackground: ImageView = bindingMainActivity.ivBackground

                imageBackground.setImageURI(result.data?.data)
            }
        }





    private fun showBrushSizeChooserDialog() {

        brushDialog.show()

        val smallBtn = bindingDialogBrushSize.ibSmallBrush
        val mediumBtn = bindingDialogBrushSize.ibMediumBrush
        val largeBtn = bindingDialogBrushSize.ibLargeBrush

        smallBtn.setOnClickListener()
        {
            bindingMainActivity.drawingView.setTaillePinceau(10.toFloat())
            brushDialog.dismiss()
        }

        mediumBtn.setOnClickListener()
        {
            bindingMainActivity.drawingView.setTaillePinceau(20.toFloat())
            brushDialog.dismiss()
        }
        largeBtn.setOnClickListener()
        {
            bindingMainActivity.drawingView.setTaillePinceau(30.toFloat())
            brushDialog.dismiss()
        }


    }

    ///Activé depuis activity.xml
    fun paintClicked(imageButtonSlelectedView: View) {
        Toast.makeText(this, "Clicked paint", Toast.LENGTH_LONG).show()

        ///La couleur du pinceau et du bouton est changé seulement si il s'agit d'un autre bouton selectionné
        if (imageButtonSlelectedView !== mImageButtonCurrentPaint) {
            imageButtonSlelectedView as ImageButton
            // tag du dossier values fichier color.xml  eg: #FFcc99  <color name="skin">#FFcc99</color>
            val colorTag = imageButtonSlelectedView.tag.toString()
            //la couleur du pinceau est configuré par colorTag
            bindingMainActivity.drawingView.setCouleurPinceau(colorTag)


            /// Du dossier drawable pallet_selected
            /// Apparence pour un bouton séléctionné
            imageButtonSlelectedView.setImageDrawable(
                ContextCompat.getDrawable(
                    this,
                    R.drawable.pallet_selected
                )
            )
            // imageButton!!.setImageDrawable(ContextCompat.getDrawable(this,R.drawable.pallet_selected))
        }
        /**
         * Etait : mImageButtonCurrentPaint!!.setImageDrawable(ContextCompat.getDrawable(this,R.drawable.pallet_selected)) quand séléctionné
         * Apparence du bouton en non sélectionné
         * Reset par defaut des boutons en non selectionné
         */

        mImageButtonCurrentPaint!!.setImageDrawable(
            ContextCompat.getDrawable(
                this,
                R.drawable.pallet_normal
            )
        )

        mImageButtonCurrentPaint = imageButtonSlelectedView as ImageButton
    }

    /**
     * 1. Creation du bitmap memorisé dans une variable pour ensuite être sauvegarder localement.
     */
    private fun getBitmapFromView(dessin_view: View): Bitmap {
        /**
         * Ici le dessin est mémorisé dans ma variable Bitmap
         */
        val returnedBitmap =
            Bitmap.createBitmap(dessin_view.width, dessin_view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(returnedBitmap)
        val imageViewBackground = dessin_view.background
        if (imageViewBackground != null) {
            imageViewBackground.draw(canvas)
        } else {
            canvas.drawColor(Color.WHITE)
        }
        dessin_view.draw(canvas)

        return returnedBitmap
    }

    /**
     * 2.
     */
    private suspend fun sauvegardeImageBitmap(fichierBitmap: Bitmap): String {
        var result = ""
        /**
         * J'ai ajouter dans build.gradle implementation 'androidx.activity:activity-ktx:1.4.0' pour permettre le fonctionnement
         * Coroutines
         */
        withContext(Dispatchers.IO)
        {
            if (fichierBitmap != null) {
                try {
                    val byteArrayOutputStream = ByteArrayOutputStream()
                    /**
                     * modification du bitmap en png
                     */
                    fichierBitmap.compress(Bitmap.CompressFormat.PNG, 90, byteArrayOutputStream)
                    /**
                     * Write a compressed version of the bitmap to the specified outputstream.
                     * If this returns true, the bitmap can be reconstructed by passing a
                     * corresponding inputstream to BitmapFactory.decodeStream(). Note: not
                     * all Formats support all bitmap configs directly, so it is possible that
                     * the returned bitmap from BitmapFactory could be in a different bitdepth,
                     * and/or may have lost per-pixel alpha (e.g. JPEG only supports opaque
                     * pixels).
                     *
                     * @param format   The format of the compressed image
                     * @param quality  Hint to the compressor, 0-100. 0 meaning compress for
                     *                 small size, 100 meaning compress for max quality. Some
                     *                 formats, like PNG which is lossless, will ignore the
                     *                 quality setting
                     * @param stream   The outputstream to write the compressed data.
                     * @return true if successfully compressed to the specified stream.
                     */
                    val filePath =
                        File(externalCacheDir?.absoluteFile.toString() + File.separator + "KidDrawing_" + System.currentTimeMillis() / 1000 + ".png")

                    // Here the Environment : Provides access to environment variables.
                    // getExternalStorageDirectory : returns the primary shared/external storage directory.
                    // absoluteFile : Returns the absolute form of this abstract pathname.
                    // File.separator : The system-dependent default name-separator character. This string contains a single character.
                    val fileOutputStream =
                        FileOutputStream(filePath)// Creates a file output stream to write to the file represented by the specified object.
                    fileOutputStream.write(byteArrayOutputStream.toByteArray())
                    fileOutputStream.close()// Closes this file output stream and releases any system resources associated with this stream. This file output stream may no longer be used for writing bytes.

                    result = filePath.absolutePath

                    runOnUiThread()
                    {cancelProgressDialog()
                        if (result.isNotEmpty()) {

                            Toast.makeText(
                                this@MainActivity,
                                "Fichier sauvegarder correctement: $result",
                                Toast.LENGTH_SHORT
                            ).show()
                            shareImage(result)
                        } else {
                            Toast.makeText(
                                this@MainActivity,
                                "Erreur lors de la sauvegarde: $result",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                } catch (e: Exception) {
                    result = ""
                    e.printStackTrace()
                }
            }
        }
        return result
    }


    /**
     * Shows rationale dialog for displaying why the app needs permission
     * Only shown if the user has denied the permission request previously
     */
    private fun showRationaleDialog(title: String, message: String) {
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setTitle(title)
            .setMessage(message)
            .setPositiveButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
        builder.create().show()
    }

    /**
     *
     * Method is used to show the custom Progress Dialog
     *
     */
    private fun showProgressDialog() {
        customProgressDialog = Dialog(this)
        /*
        Set the screen content from a layout ressource.
        The ressource will be inflated, adding all the top-level views to the screen.
         */
        customProgressDialog?.setContentView(R.layout.dialog_custom_progress)
        /*
        Start the dialog and display it on screen
         */
        customProgressDialog?.show()
    }

    /**
     * This function is used to dismiss the progress dialog if it is visible to user.
     */
    private fun cancelProgressDialog() {
        if (customProgressDialog != null) {
            customProgressDialog?.dismiss()
            customProgressDialog = null
        }
    }

    //create a method to requestStorage permission
    private fun requestStoragePermission(){
        // Check if the permission was denied and show rationale
        if (
            ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
        ){
            //call the rationale dialog to tell the user why they need to allow permission request
            showRationaleDialog("Kids Drawing App","Kids Drawing App " +
                    "needs to Access Your External Storage")
        }
        else {
            // You can directly ask for the permission.
            //if it has not been denied then request for permission
            //  The registered ActivityResultCallback gets the result of this request.
            requestPermission.launch(
                arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
            )
        }

    }



    /**
     * Check if read permissions
     */
    private fun isReadStorageAllowed(): Boolean {
        //Getting the permission status
        // Here the checkSelfPermission is
        /**
         * Determine whether <em>you</em> have been granted a particular permission.
         *
         * @param permission The name of the permission being checked.
         *
         */
        val result =
            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
        /**
         *
         * @return {@link android.content.pm.PackageManager#PERMISSION_GRANTED} if you have the
         * permission, or {@link android.content.pm.PackageManager#PERMISSION_DENIED} if not.
         *
         */
        //If permission is granted returning true and If permission is not granted returning false
        return result == PackageManager.PERMISSION_GRANTED

    }
/**
 * Method used to enable sharing of this file
 */
    private fun shareImage (result: String)
    {
        MediaScannerConnection.scanFile(this, arrayOf(result),null)
        {
          path,uri ->
            val shareIntent =  Intent()
            shareIntent.action = Intent.ACTION_SEND
            shareIntent.putExtra(Intent.EXTRA_STREAM,uri)
            shareIntent.type = "image/png"
            startActivity(Intent.createChooser(shareIntent,"share"))
        }
    }

}