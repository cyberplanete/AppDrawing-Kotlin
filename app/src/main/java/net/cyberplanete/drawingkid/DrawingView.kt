package net.cyberplanete.drawingkid

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View


/// Cette classe herite de la classe View
class DrawingView(context: Context, attrs: AttributeSet) : View(context, attrs) {

    private var mDrawPath: CustomPath? = null;
    private var mCanvasBitmap: Bitmap? = null
    private var mDrawPaint: Paint? = null
    private var mCanvasPaint: Paint? = null
    private var mBrushSize: Float = 0.toFloat()
    private var couleur = Color.BLACK
    private var canvas : Canvas? = null

    init {
        setupDrawing()
    }

    private fun setupDrawing() {
        mDrawPaint = Paint()
        mDrawPath = CustomPath(couleur,mBrushSize)
        mDrawPaint!!.color = couleur
        mDrawPaint!!.style = Paint.Style.STROKE
        mDrawPaint!!.strokeJoin = Paint.Join.ROUND
        mDrawPaint!!.strokeCap = Paint.Cap.ROUND
        mCanvasPaint = Paint(Paint.DITHER_FLAG)
        mBrushSize = 20.toFloat()
    }


    internal inner class CustomPath(var couleur: Int, var EpaisseurPinceau: Float) : Path() {

    }

}