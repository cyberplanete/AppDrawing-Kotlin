package net.cyberplanete.drawingkid

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View


/// Cette classe herite de la classe View
class DrawingView(context: Context, attrs: AttributeSet) : View(context, attrs) {

    ///Hérite de customPath
    private var mDrawPath: CustomPath? = null;
    private var mCanvasBitmap: Bitmap? = null
    private var mDrawPaint: Paint? = null
    private var mCanvasPaint: Paint? = null
    private var mPinceauTaille: Float = 0.toFloat()
    private var couleur = Color.BLACK
    private var canvas: Canvas? = null

    init {
        setupDrawing()
    }

    private fun setupDrawing() {
        mDrawPaint = Paint()
        mDrawPaint!!.color = couleur
        mDrawPaint!!.style = Paint.Style.STROKE
        mDrawPaint!!.strokeJoin = Paint.Join.BEVEL
        mDrawPaint!!.strokeCap = Paint.Cap.SQUARE

        mDrawPath = CustomPath(couleur, mPinceauTaille)
        mCanvasPaint = Paint(Paint.DITHER_FLAG)
        mPinceauTaille = 20.toFloat()
    }

    /// Called during layout when the size of this view has changed
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mCanvasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        canvas = Canvas(mCanvasBitmap!!)
    }

    ///Fonction permettant de créer une ligne
    override fun draw(canvas: Canvas) {
        super.draw(canvas)
        canvas.drawBitmap(mCanvasBitmap!!, 0f, 0f, mCanvasPaint)
        if (!mDrawPath!!.isEmpty) {
            mDrawPaint!!.strokeWidth = mDrawPath!!.epaisseurPinceau
            canvas.drawPath(mDrawPath!!, mDrawPaint!!)
        }
    }

    ///Fonction permettant de gerer les evenements sur l'ecran
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        val touchX = event?.x
        val touchY = event?.y

        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                mDrawPath!!.couleur = couleur
                mDrawPath!!.epaisseurPinceau = mPinceauTaille

                mDrawPath!!.reset()

                if (touchX != null) {
                    if (touchY != null) {
                        mDrawPath!!.moveTo(touchX, touchY)
                    }
                }
            }

            MotionEvent.ACTION_MOVE -> {
                if (touchX != null) {
                    if (touchY != null) {
                        mDrawPath!!.lineTo(touchX, touchY)
                    }
                }
            }

            MotionEvent.ACTION_UP -> {
                mDrawPath = CustomPath(couleur, mPinceauTaille)
            }
            ///Pour tous les autres évenements je retourne false
            else -> return false

        }
        ///Generally, invalidate() means 'redraw on screen' and results to a call of the view's onDraw() method.
        // So if something changes and it needs to be reflected on screen, you need to call invalidate()
        invalidate()
        return true
    }


    internal inner class CustomPath(var couleur: Int, var epaisseurPinceau: Float) : Path() {

    }

}