package net.cyberplanete.drawingkid

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View


/// Cette classe herite de la classe View
class DrawingView(context: Context, attrs: AttributeSet) : View(context, attrs) {

    ///Hérite de customPath
    private var mDrawDessin: CustomPath? = null;
    private var mCanvasBitmap: Bitmap? = null
    private var mDrawPaint: Paint? = null
    private var mCanvasPaint: Paint? = null
    private var mPinceauTaille: Float = 0.toFloat()
    private var couleur = Color.BLACK
    private var canvas: Canvas? = null

    ///Permettre l'enregistrement du dessin
    // Chaque mouvement de la souris formant un trait est enregistré dans un arraylist
    private val mListeDeTraits = ArrayList<CustomPath>()
    // Identique a mListdeTraits mais utilisé pour revenir sur l'action précédente
    private val mRedoListDeTraits = ArrayList<CustomPath>()


    init {
        setupDrawing()
    }

    fun onClickUndo ()
    {
        //Si liste de traits est superieur à 0
        if (mListeDeTraits.size > 0)
        {
            //Je constitue une liste permettant le retour de la derniere action et en meme temps je retire de ma view la derniere action
            mRedoListDeTraits.add(mListeDeTraits.removeAt(mListeDeTraits.size - 1))
            invalidate()
        }
    }

    fun onClickRedo ()
    {
        //Si liste de traits est superieur à 0
        if (mRedoListDeTraits.size > 0)
        {
            //j'ajoute mListeDeTraits -1 à mmUndoListDeTraits
           // mUndoListDeTraits.add(mListeDeTraits.removeAt(mListeDeTraits.size - 1))
            mListeDeTraits.add(mRedoListDeTraits.removeAt(mRedoListDeTraits.size-1))

            invalidate()
        }
    }

    private fun setupDrawing() {
        ///The paint class holds the style and color information about how to draw geometries, text and bitmaps
        mDrawPaint = Paint()
        mDrawPaint!!.color = couleur
        mDrawPaint!!.style = Paint.Style.STROKE
        mDrawPaint!!.strokeJoin = Paint.Join.BEVEL
        mDrawPaint!!.strokeCap = Paint.Cap.SQUARE

        mDrawDessin = CustomPath(couleur, mPinceauTaille)
        mCanvasPaint = Paint(Paint.DITHER_FLAG)
        /// mPinceauTaille = 20.toFloat()
    }

    /// Called during layout when the size of this view has changed
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mCanvasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        canvas = Canvas(mCanvasBitmap!!)
    }

    ///Fonction permettant de créer une ligne
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawBitmap(mCanvasBitmap!!, 0f, 0f, mCanvasPaint)

        ///Permet dessiner un ensemble d'enregistrement de dessins -- La liste customPath est completé d'un dessin à chaque reclachement du bouton
        for (path in mListeDeTraits) {
            mDrawPaint!!.strokeWidth = path!!.epaisseurPinceau
            mDrawPaint!!.color = path!!.couleur
            canvas.drawPath(path!!, mDrawPaint!!)
        }

        ///Permet de dessiner puis est enregistrer au relachement du bouton  -- mListeDessins
        if (!mDrawDessin!!.isEmpty) {
            mDrawPaint!!.strokeWidth = mDrawDessin!!.epaisseurPinceau
            mDrawPaint!!.color = mDrawDessin!!.couleur
            canvas.drawPath(mDrawDessin!!, mDrawPaint!!)
        }
    }

    ///Fonction permettant de gerer les evenements sur l'ecran
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        val touchX = event?.x
        val touchY = event?.y

        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                mDrawDessin!!.couleur = couleur
                mDrawDessin!!.epaisseurPinceau = mPinceauTaille

                mDrawDessin!!.reset()

                if (touchX != null) {
                    if (touchY != null) {
                        mDrawDessin!!.moveTo(touchX, touchY)
                    }
                }
            }

            MotionEvent.ACTION_MOVE -> {
                if (touchX != null) {
                    if (touchY != null) {
                        mDrawDessin!!.lineTo(touchX, touchY)
                    }
                }
            }

            MotionEvent.ACTION_UP -> {
                ///Permettre l'enregistrement du dessin -- Lorsque le bouton de la souris est relaché
                mListeDeTraits.add(mDrawDessin!!)
                mDrawDessin = CustomPath(couleur, mPinceauTaille)
            }
            ///Pour tous les autres évenements je retourne false
            else -> return false

        }
        ///Generally, invalidate() means 'redraw on screen' and results to a call of the view's onDraw() method.
        // So if something changes and it needs to be reflected on screen, you need to call invalidate()
        invalidate()
        return true
    }

    ///Configuration de la taile du pinceau -- Taille de l'écran prise en considération
    fun setTaillePinceau(nouvelleTaille: Float) {
        //TypedValue prends en consideration la taille de l'écran
        mPinceauTaille = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            nouvelleTaille,
            resources.displayMetrics
        )

        mDrawPaint!!.strokeWidth = mPinceauTaille
    }

    /// Configuration de la couleur du pinceau
    fun setCouleurPinceau(nouvelleCouleur: String) {
        couleur = Color.parseColor(nouvelleCouleur)
        mDrawPaint!!.color = couleur
    }

    internal inner class CustomPath(var couleur: Int, var epaisseurPinceau: Float) : Path() {

    }

}