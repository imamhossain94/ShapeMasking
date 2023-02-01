package com.newagedevs.shapemasking

import android.content.res.Resources
import android.graphics.*
import android.os.Bundle
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.slaviboy.svgpath.SvgPath


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
        for (i in 1..6) {
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

        view.setImageBitmap(drawBorderUsingVectorPath(maskedBitmap, mask, borderSize.toFloat()))

        //view.setImageBitmap(drawBorderColorFilter(maskedBitmap, borderSize.toFloat()))

    }

    private fun drawBorderUsingVectorPath(bitmap: Bitmap, mask: Int, stroke: Float): Bitmap {
        val vectorDrawable = VectorDrawableParser.parsedVectorDrawable(resources, mask)

        if (vectorDrawable != null) {
            var strokeBitmap =
                Bitmap.createBitmap(
                    vectorDrawable.viewportWidth.toInt(),
                    vectorDrawable.viewportHeight.toInt(),
                    Bitmap.Config.ARGB_8888
                )

            val strokeCanvas = Canvas(strokeBitmap)
            val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG)
            strokePaint.style = Paint.Style.STROKE
            strokePaint.strokeWidth = stroke

            strokePaint.color = Color.parseColor("#000000")

            for (path in vectorDrawable.pathData) {
                val data = SvgPath(path!!)
                strokeCanvas.drawPath(data.generatePath(), strokePaint)
            }

            strokeBitmap =
                Bitmap.createScaledBitmap(strokeBitmap, bitmap.height, bitmap.width, false)

            val clipCanvas = Canvas(bitmap)
            val clipPaint = Paint(Paint.ANTI_ALIAS_FLAG)

            clipCanvas.drawBitmap(bitmap, 0f, 0f, clipPaint)
            clipCanvas.drawBitmap(strokeBitmap, 0f, 0f, clipPaint)

            return bitmap
        }

        return bitmap
    }

    private fun drawBorderColorFilter(bitmap: Bitmap, stroke: Float): Bitmap {
        val options = BitmapFactory.Options()
        options.inMutable = true
        val newBitmap = Bitmap.createBitmap(
            (bitmap.width + (2 * stroke)).toInt(),
            (bitmap.height + (2 * stroke)).toInt(),
            Bitmap.Config.ARGB_8888
        )

        // Calculate bitmap start point
        val x: Float = ((newBitmap.width - bitmap.width) / 2).toFloat()
        val y: Float = ((newBitmap.height - bitmap.height) / 2).toFloat()

        val canvas = Canvas(newBitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val filter = PorterDuffColorFilter(Color.BLACK, PorterDuff.Mode.SRC_IN)
        paint.colorFilter = filter
        canvas.drawBitmap(bitmap, -stroke + x, y, paint)
        canvas.drawBitmap(bitmap, x, -stroke + y, paint)
        canvas.drawBitmap(bitmap, stroke + x, y, paint)
        canvas.drawBitmap(bitmap, x, stroke + y, paint)
        paint.colorFilter = null
        canvas.drawBitmap(bitmap, x, y, paint)
        return newBitmap
    }

}


