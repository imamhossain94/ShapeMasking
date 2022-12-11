package com.newagedevs.shapemasking

import android.annotation.SuppressLint
import android.content.res.Resources
import android.graphics.*
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.xmlpull.v1.XmlPullParser
import java.util.*


class MainActivity : AppCompatActivity() {

    private var displayWidth: Int = 1080

    private lateinit var recyclerView: RecyclerView
    private lateinit var canvasImageView: ImageView
    private lateinit var canvasSizeSeekBar: SeekBar
    private lateinit var borderSizeSeekBar: SeekBar
    private lateinit var canvasSizeTextView: TextView
    private lateinit var borderSizeTextView: TextView

    private var mask: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        displayWidth = Resources.getSystem().displayMetrics.widthPixels
        mask = R.drawable.shape_1

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        canvasImageView = findViewById(R.id.canvasImageView)
        canvasSizeSeekBar = findViewById(R.id.seekBarCanvasSize)
        borderSizeSeekBar = findViewById(R.id.seekBarBorderSize)
        canvasSizeTextView = findViewById(R.id.textViewCanvasSize)
        borderSizeTextView = findViewById(R.id.textViewBorderSize)

        canvasSizeSeekBar.progress = displayWidth / 2
        canvasSizeTextView.text = (displayWidth / 2).toString()

        canvasSizeSeekBar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                canvasSizeTextView.text = progress.toString()
                renderImages()
            }
        })

        borderSizeSeekBar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                borderSizeTextView.text = progress.toString()
                renderImages()
            }
        })

        val shapes = ArrayList<Int>()
        for (i in 1..64) {
            shapes.add(resources.getIdentifier("shape_$i", "drawable", packageName))
        }
        recyclerView.adapter = ShapeAdapter(
            shapeList = shapes,
            size = (displayWidth / 8) - 26,
            image = BitmapFactory.decodeResource(resources, R.drawable.image),
            onClick = {
                mask = it
                renderImages()
            }
        )

        renderImages()
    }


    private fun renderImages() {
        maskImage(
            view = canvasImageView,
            image = BitmapFactory.decodeResource(resources, R.drawable.image),
            mask = mask!!,
            canvasSize = if (canvasSizeSeekBar.progress <= 1) 5 else canvasSizeSeekBar.progress,
            borderSize = borderSizeSeekBar.progress,
        )
    }


    private fun maskImage(
        view: ImageView,
        image: Bitmap,
        mask: Int,
        canvasSize: Int,
        borderSize: Int
    ) {

        val scaledImage = Bitmap.createScaledBitmap(image, canvasSize, canvasSize, false)
        val scaledMask = Bitmap.createScaledBitmap(
            ResourcesCompat.getDrawable(resources, mask, null)!!.toBitmap(),
            canvasSize, canvasSize, false
        )

        val maskedBitmap = Bitmap.createBitmap(canvasSize, canvasSize, Bitmap.Config.ARGB_8888)
        val maskedCanvas = Canvas(maskedBitmap)

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_IN)
        maskedCanvas.drawBitmap(scaledImage, 0f, 0f, null)
        maskedCanvas.drawBitmap(scaledMask, 0f, 0f, paint)
        paint.xfermode = null

        view.setImageBitmap(drawBorder(maskedBitmap, borderSize.toFloat()))


//        val vectorDrawable = VectorDrawableParser.parsedVectorDrawable(resources, mask)
//
//        if (vectorDrawable != null) {
//            var strokeBitmap =
//                Bitmap.createBitmap(
//                    vectorDrawable.viewportWidth.toInt(),
//                    vectorDrawable.viewportHeight.toInt(),
//                    Bitmap.Config.ARGB_8888
//                )
//
//            val strokeCanvas = Canvas(strokeBitmap)
//            val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG)
//            strokePaint.style = Paint.Style.STROKE
//            strokePaint.strokeWidth = borderSize.toFloat()
//
//            strokePaint.color = Color.parseColor("#000000")
//
//            for (path in vectorDrawable.pathData) {
//                val data = SvgPath(path!!)
//                strokeCanvas.drawPath(data.generatePath(), strokePaint)
//            }
//
//            strokeBitmap = Bitmap.createScaledBitmap(strokeBitmap, canvasSize, canvasSize, false)
//
//
//            val  clipBitmap =
//                Bitmap.createBitmap(canvasSize, canvasSize, Bitmap.Config.ARGB_8888)
//
//            val clipCanvas = Canvas(maskedBitmap)
//            val clipPaint = Paint(Paint.ANTI_ALIAS_FLAG)
//
//            clipCanvas.drawBitmap(maskedBitmap, 0f, 0f, clipPaint)
//            clipCanvas.drawBitmap(strokeBitmap, 0f, 0f, clipPaint)
//
//            view.setImageBitmap(maskedBitmap)
//        }


    }

    fun drawBorder(bitmap: Bitmap, stroke: Float): Bitmap? {
        val options = BitmapFactory.Options()
        options.inMutable = true
        val newBitmap = Bitmap.createBitmap(
            (bitmap.width + (2 * stroke)).toInt(),
            (bitmap.height + (2 * stroke)).toInt(),
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(newBitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val filter = PorterDuffColorFilter(Color.BLACK, PorterDuff.Mode.SRC_IN)
        paint.colorFilter = filter
        canvas.drawBitmap(bitmap, -stroke, 0f, paint)
        canvas.drawBitmap(bitmap, 0f, -stroke, paint)
        canvas.drawBitmap(bitmap, stroke, 0f, paint)
        canvas.drawBitmap(bitmap, 0f, stroke, paint)
        paint.colorFilter = null
        canvas.drawBitmap(bitmap, 0f, 0f, paint)
        return newBitmap
    }


    class ShapeAdapter(
        private val shapeList: List<Int>,
        private var image: Bitmap,
        private var size: Int,
        val onClick: (Int) -> Unit
    ) :
        RecyclerView.Adapter<ShapeAdapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.shape_view, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {

            var shape =
                ContextCompat.getDrawable(holder.view.context, shapeList[position])!!.toBitmap()
            shape = Bitmap.createScaledBitmap(shape, size, size, false)
            image = Bitmap.createScaledBitmap(image, size, size, false)

            val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            val paint = Paint(Paint.ANTI_ALIAS_FLAG)
            paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_IN)
            canvas.drawBitmap(image, 0f, 0f, null)
            canvas.drawBitmap(shape, 0f, 0f, paint)
            paint.xfermode = null

            holder.view.setImageBitmap(bitmap)
            holder.view.scaleType = ImageView.ScaleType.CENTER
            holder.view.layoutParams.height = size
            holder.view.layoutParams.width = size

            holder.view.setOnClickListener { onClick(shapeList[position]) }
        }

        override fun getItemCount(): Int {
            return shapeList.size
        }

        class ViewHolder(ItemView: View) : RecyclerView.ViewHolder(ItemView) {
            val view: ImageView = itemView.findViewById(R.id.imageview)
        }
    }

}

// ---------------------

data class ParsedVectorDrawable(
    val width: Float,
    val height: Float,
    val viewportWidth: Float,
    val viewportHeight: Float,
    val pathData: ArrayList<String?>
)

object VectorDrawableParser {

    private val digitsOnly = Regex("[^0-9.]")

    @SuppressLint("ResourceType")
    fun parsedVectorDrawable(
        resources: Resources,
        @DrawableRes drawable: Int

    ): ParsedVectorDrawable? {
        val pathData: ArrayList<String?> = ArrayList()
        var width: Float? = null
        var height: Float? = null
        var viewportWidth: Float? = null
        var viewportHeight: Float? = null

        // This is very simple parser, it doesn't support <group> tag, nested tags and other stuff
        resources.getXml(drawable).use { xml ->
            var event = xml.eventType

            while (event != XmlPullParser.END_DOCUMENT) {

                if (event != XmlPullParser.START_TAG) {
                    event = xml.next()
                    continue
                }

                when (xml.name) {
                    "vector" -> {
                        width = xml.getAttributeValue(getAttrPosition(xml, "width"))
                            .replace(digitsOnly, "")
                            .toFloatOrNull()
                        height = xml.getAttributeValue(getAttrPosition(xml, "height"))
                            .replace(digitsOnly, "")
                            .toFloatOrNull()
                        viewportWidth = xml.getAttributeValue(getAttrPosition(xml, "viewportWidth"))
                            .toFloatOrNull()
                        viewportHeight =
                            xml.getAttributeValue(getAttrPosition(xml, "viewportHeight"))
                                .toFloatOrNull()
                    }
                    "path" -> {
                        pathData.add(xml.getAttributeValue(getAttrPosition(xml, "pathData")))
                    }
                }

                event = xml.next()
            }

        }

        return ParsedVectorDrawable(
            width ?: return null,
            height ?: return null,
            viewportWidth ?: return null,
            viewportHeight ?: return null,
            pathData,
        )
    }

    private fun getAttrPosition(xml: XmlPullParser, attrName: String): Int =
        (0 until xml.attributeCount)
            .firstOrNull { i -> xml.getAttributeName(i) == attrName }
            ?: -1
}
// https://gist.github.com/aednlaxer/9e3ccc56bb72253966b2e298e2700751
