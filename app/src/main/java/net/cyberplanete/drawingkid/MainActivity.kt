package net.cyberplanete.drawingkid

import android.Manifest
import android.app.AlertDialog
import android.app.Dialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.get
import net.cyberplanete.drawingkid.databinding.ActivityMainBinding
import net.cyberplanete.drawingkid.databinding.DialogBrushSizeBinding

class MainActivity : AppCompatActivity() {

    // ********* PERMISSIONS ***********
    //Todo 1: Activity result launcher of type Array<String> for permission camera and storage
    private val cameraAndLocationResultLauncher: ActivityResultLauncher<Array<String>> =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions())
        { permissions ->
            /**
            Here it returns a Map of permission name as key with boolean as value
            Todo 2: We loop through the map to get the value we need which is the boolean
            value
             */
            permissions.entries.forEach {

                //Todo 3: if it is granted then we show its granted
                val permissionName = it.key
                val isGranted = it.value
                if (isGranted) {
                    //check the permission name and perform the specific operation
                    if (permissionName == Manifest.permission.ACCESS_FINE_LOCATION) {
                        Toast.makeText(
                            this,
                            "Permission granted for location",
                            Toast.LENGTH_LONG
                        )
                            .show()
                    } else {
                        //check the permission name and perform the specific operation
                        Toast.makeText(
                            this,
                            "Permission granted for Camera",
                            Toast.LENGTH_LONG
                        )
                            .show()
                    }
                } else {
                    if (permissionName == Manifest.permission.ACCESS_FINE_LOCATION) {
                        Toast.makeText(
                            this,
                            "Permission denied for location",
                            Toast.LENGTH_LONG
                        )
                            .show()
                    } else {
                        Toast.makeText(
                            this,
                            "Permission denied for Camera",
                            Toast.LENGTH_LONG
                        )
                            .show()
                    }
                }

            }
//            if (isGranted) {
//                Toast.makeText(this, "Permission autorisée pour la caméra", Toast.LENGTH_LONG)
//                    .show()
//            } else {
//                Toast.makeText(this, "Permission non autorisée pour la caméra", Toast.LENGTH_LONG)
//                    .show();
//
//            }

        }

    //private var drawingView : DrawingView? = null
    private lateinit var bindingDialogBrushSize: DialogBrushSizeBinding
    private lateinit var bindingMainActivity: ActivityMainBinding
    private lateinit var brushDialog: Dialog

    // 1 - Accès à la palette de couleur
    private var mImageButtonCurrentPaint: ImageButton? = null

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
        // I asked for permission to access external storage
        val ibGallery: ImageButton = bindingMainActivity.ibGallery
        ibGallery.setOnClickListener { requesCameraAndStoragetPermission() }
    }

    // Asking for permission to access storage and camera
    private fun requesCameraAndStoragetPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.READ_EXTERNAL_STORAGE) || ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
            showRationaleDialog(
                "Kids Drawing App",
                "Kids Drawing App" + "Needs to access your external storage and camera",

                )
        }else
            {   // I directly ask for the permission.
                // The registered ActivityResultCallback gets the result of this request.
                cameraAndLocationResultLauncher.launch(arrayOf(Manifest.permission.CAMERA,Manifest.permission.READ_EXTERNAL_STORAGE))
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
        /// Etait : mImageButtonCurrentPaint!!.setImageDrawable(ContextCompat.getDrawable(this,R.drawable.pallet_selected)) quand séléctionné
        /// Apparence du bouton en non sélectionné
        /// Reset par defaut des boutons en non selectionné
        mImageButtonCurrentPaint!!.setImageDrawable(
            ContextCompat.getDrawable(
                this,
                R.drawable.pallet_normal
            )
        )

        mImageButtonCurrentPaint = imageButtonSlelectedView as ImageButton
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

}