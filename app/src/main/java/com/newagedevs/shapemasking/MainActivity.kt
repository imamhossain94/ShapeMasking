package com.newagedevs.shapemasking

import android.content.res.Resources
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmap


class MainActivity : AppCompatActivity() {


    private lateinit var canvasSizeSeekBar:SeekBar
    private lateinit var borderSizeSeekBar:SeekBar
    private lateinit var canvasSizeTextView:TextView
    private lateinit var borderSizeTextView:TextView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

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
    }



    private fun maskImage(
        view: ImageView,
        image: Bitmap,
        mask: Bitmap,
        canvasSize: Int,
        borderSize: Int
    ) {


        val centreX = (canvasSize  - (canvasSize - borderSize)) /2
        val centreY = (canvasSize - (canvasSize - borderSize)) /2


        val scaledImage = Bitmap.createScaledBitmap(image, canvasSize - borderSize, canvasSize - borderSize, false)
        val scaledMask = Bitmap.createScaledBitmap(mask, canvasSize - borderSize, canvasSize - borderSize, false)

        val maskBorder = Bitmap.createScaledBitmap(mask, canvasSize, canvasSize, false)

        val bitmap =
            Bitmap.createBitmap(canvasSize, canvasSize, Bitmap.Config.ARGB_8888)

        val mCanvas = Canvas(bitmap)

        val paintBorder = Paint()
        paintBorder.xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_IN)
        mCanvas.drawBitmap(maskBorder, 0f, 0f, paintBorder)
        paintBorder.xfermode = null


        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_IN)
        mCanvas.drawBitmap(scaledImage, centreX.toFloat(), centreY.toFloat(), null)
        mCanvas.drawBitmap(scaledMask, centreX.toFloat(), centreY.toFloat(), paint)
        paint.xfermode = null

        view.setImageBitmap(bitmap)
        view.scaleType = ImageView.ScaleType.CENTER
        view.layoutParams.height = canvasSize
        view.layoutParams.width = canvasSize
        view.background = BitmapDrawable(resources, maskBorder)
    }

}