package tech.ryadom.origami

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.util.AttributeSet
import android.widget.FrameLayout
import java.io.File

/**
 * Виджет Origami
 * @param context контекст [Context]
 * @param attrs аттрибуты [AttributeSet]
 */
class OrigamiView(
    context: Context,
    attrs: AttributeSet?,
) : FrameLayout(context, attrs) {

    /**
     *
     */
    private var aspectX: Int = 1

    /**
     *
     */
    private var aspectY: Int = 1

    /**
     *
     */
    private var overlayColor: Int = Color.TRANSPARENT

    /**
     *
     */
    private var overlayShape: Shape = Shape.RECTANGLE

    /**
     * Локальная копия bitmap [Bitmap]
     */
    private lateinit var bitmap: Bitmap


    private lateinit var touchImageView: OrigamiImageView
    private lateinit var overlayView: OrigamiOverlayView

    init {
        obtainAttributes(attrs)
    }

    private fun obtainAttributes(attrs: AttributeSet?) {
        attrs ?: return
        context.obtainStyledAttributes(attrs, R.styleable.OrigamiView)
            .run {
                aspectX = getInteger(R.styleable.OrigamiView_origamiAspectX, aspectX)
                aspectY = getInteger(R.styleable.OrigamiView_origamiAspectY, aspectY)
                overlayColor = getColor(R.styleable.OrigamiView_origamiOverlayColor, overlayColor)
                overlayShape = getString(R.styleable.OrigamiView_origamiShape)
                    ?.let { Shape.valueOf(it) }
                    ?: overlayShape

                recycle()
            }
    }

    fun setup(bitmap: Bitmap) {
        this.bitmap = bitmap
    }

    fun setup(uri: Uri) {

    }

    fun setup(file: File) {

    }

    fun transform(f: (Bitmap) -> Bitmap) {
        bitmap = f.invoke(bitmap)
    }

    fun crop(): Bitmap {
        TODO()
    }
}

fun OrigamiView.cropToFile(saveToStorage: Boolean = false) {
    val bitmap = crop()
    //
}