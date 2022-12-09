package com.newagedevs.shapemasking

import android.content.res.Resources
import android.graphics.*
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.*
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmap


class MainActivity : AppCompatActivity(), View.OnTouchListener {

    private var _xDelta = 0
    private var _yDelta = 0

    private lateinit var tableLayout: TableLayout
    private lateinit var canvasSizeSeekBar:SeekBar
    private lateinit var borderSizeSeekBar:SeekBar
    private lateinit var canvasSizeTextView:TextView
    private lateinit var borderSizeTextView:TextView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tableLayout = findViewById(R.id.tableLayout)
        canvasSizeSeekBar = findViewById(R.id.seekBarCanvasSize)
        borderSizeSeekBar = findViewById(R.id.seekBarBorderSize)
        canvasSizeTextView = findViewById(R.id.textViewCanvasSize)
        borderSizeTextView = findViewById(R.id.textViewBorderSize)

        canvasSizeSeekBar.progress = Resources.getSystem().displayMetrics.widthPixels/4
        canvasSizeTextView.text = (Resources.getSystem().displayMetrics.widthPixels/4).toString()


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

        renderImages()
    }


    private fun renderImages() {
        for (i in 1..24) {
            val resID = resources.getIdentifier("imageView_$i", "id", packageName)
            val maskID = resources.getIdentifier("mask_$i", "drawable", packageName)

            ResourcesCompat.getDrawable(resources, maskID, null)?.let {
                maskImage(
                    view = this.findViewById(resID),
                    image = BitmapFactory.decodeResource(resources, R.drawable.image),
                    mask = it.toBitmap(),
                    canvasSize = if(canvasSizeSeekBar.progress <= 1) 5 else canvasSizeSeekBar.progress,
                    borderSize = borderSizeSeekBar.progress,
                )
            }
        }

        val layoutParams = TableLayout.LayoutParams(
            if(canvasSizeSeekBar.progress <= 1) 5 else canvasSizeSeekBar.progress*4,
            if(canvasSizeSeekBar.progress <= 1) 5 else canvasSizeSeekBar.progress*6
        )
        layoutParams.leftMargin = 0
        layoutParams.topMargin = 0
        layoutParams.bottomMargin = -1200
        layoutParams.rightMargin = -1200
        tableLayout.layoutParams = layoutParams
        tableLayout.setOnTouchListener(this)

    }



    private fun maskImage(
        view: ImageView,
        image: Bitmap,
        mask: Bitmap,
        canvasSize: Int,
        borderSize: Int
    ) {
        val scaledImage = Bitmap.createScaledBitmap(image, canvasSize, canvasSize, false)
        val scaledMask = Bitmap.createScaledBitmap(mask, canvasSize, canvasSize, false)

        val bitmap =
            Bitmap.createBitmap(canvasSize, canvasSize, Bitmap.Config.ARGB_8888)

        val mCanvas = Canvas(bitmap)

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_IN)
        mCanvas.drawBitmap(scaledImage, 0f, 0f, null)
        mCanvas.drawBitmap(scaledMask, 0f, 0f, paint)
        paint.xfermode = null


        val stroke = borderSize.toFloat()
        val newBitmap = Bitmap.createBitmap(bitmap.width + borderSize, bitmap.height + borderSize, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(newBitmap)
        val paint2 = Paint(Paint.ANTI_ALIAS_FLAG)
        val filter = PorterDuffColorFilter(Color.BLACK, PorterDuff.Mode.SRC_IN)
        paint2.colorFilter = filter
        canvas.drawBitmap(bitmap, -stroke, 0f, paint2)
        canvas.drawBitmap(bitmap, 0f, -stroke, paint2)
        canvas.drawBitmap(bitmap, stroke, 0f, paint2)
        canvas.drawBitmap(bitmap, 0f, stroke, paint2)
        paint2.colorFilter = null

        canvas.drawBitmap(bitmap, 0f, 0f, paint2)

        view.setImageBitmap(newBitmap)
        view.scaleType = ImageView.ScaleType.CENTER
        view.layoutParams.height = canvasSize + borderSize
        view.layoutParams.width = canvasSize + borderSize
    }

    override fun onTouch(view: View, event: MotionEvent): Boolean {
        val X = event.rawX.toInt()
        val Y = event.rawY.toInt()
        when (event.action and MotionEvent.ACTION_MASK) {
            MotionEvent.ACTION_DOWN -> {
                val lParams = view.layoutParams as TableLayout.LayoutParams
                _xDelta = X - lParams.leftMargin
                _yDelta = Y - lParams.topMargin
            }
            MotionEvent.ACTION_UP -> {}
            MotionEvent.ACTION_POINTER_DOWN -> {}
            MotionEvent.ACTION_POINTER_UP -> {}
            MotionEvent.ACTION_MOVE -> {
                val layoutParams = view.layoutParams as TableLayout.LayoutParams
                layoutParams.leftMargin = X - _xDelta
                layoutParams.topMargin = Y - _yDelta
                layoutParams.rightMargin = -250
                layoutParams.bottomMargin = -250
                view.layoutParams = layoutParams
            }
        }
        tableLayout.invalidate()
        return true
    }

}
