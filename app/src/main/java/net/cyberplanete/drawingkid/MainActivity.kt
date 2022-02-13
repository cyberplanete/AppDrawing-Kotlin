package net.cyberplanete.drawingkid

import android.app.Dialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import net.cyberplanete.drawingkid.databinding.ActivityMainBinding
import net.cyberplanete.drawingkid.databinding.DialogBrushSizeBinding

class MainActivity : AppCompatActivity() {

    //private var drawingView : DrawingView? = null
    private lateinit var bindingDialogBrushSize: DialogBrushSizeBinding
    private lateinit var bindingMainActivity:  ActivityMainBinding
    private lateinit var brushDialog :Dialog

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

        /// Afficher le dialog des pinceaux
        val ib_brush =  bindingMainActivity.ibBrush
        ib_brush.setOnClickListener()
        {
            showBrushSizeChooserDialog()
        }
    }

    private fun showBrushSizeChooserDialog()
    {

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


}