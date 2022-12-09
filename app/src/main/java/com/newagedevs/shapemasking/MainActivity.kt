package com.newagedevs.shapemasking

import android.content.res.Resources
import android.graphics.*
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView


class MainActivity : AppCompatActivity() {

    private var displayWidth: Int = 1080

    private lateinit var recyclerView: RecyclerView
    private lateinit var canvasImageView: ImageView
    private lateinit var canvasSizeSeekBar: SeekBar
    private lateinit var borderSizeSeekBar: SeekBar
    private lateinit var canvasSizeTextView: TextView
    private lateinit var borderSizeTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        displayWidth = Resources.getSystem().displayMetrics.widthPixels

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        canvasImageView = findViewById(R.id.canvasImageView)
        canvasSizeSeekBar = findViewById(R.id.seekBarCanvasSize)
        borderSizeSeekBar = findViewById(R.id.seekBarBorderSize)
        canvasSizeTextView = findViewById(R.id.textViewCanvasSize)
        borderSizeTextView = findViewById(R.id.textViewBorderSize)

        canvasSizeSeekBar.progress = displayWidth/2
        canvasSizeTextView.text = (displayWidth/2).toString()

        canvasSizeSeekBar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                canvasSizeTextView.text = progress.toString()
                renderImages(null)
            }
        })

        borderSizeSeekBar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                borderSizeTextView.text = progress.toString()
                renderImages(null)
            }
        })

        val shapes = ArrayList<Drawable>()
        for (i in 1..24) {
            val maskID = resources.getIdentifier("mask_$i", "drawable", packageName)
            ResourcesCompat.getDrawable(resources, maskID, null)?.let {
                shapes.add(it)
            }
        }
        recyclerView.adapter = ShapeAdapter(
            shapeList = shapes,
            size = (displayWidth / 8) - 26,
            image = BitmapFactory.decodeResource(resources, R.drawable.image),
            onClick = {
                renderImages(it)
            }
        )

        renderImages(null)
    }


    private fun renderImages(shape:Drawable?) {
        val mask = shape?:ContextCompat.getDrawable(this, R.drawable.mask_1)

        maskImage(
            view = canvasImageView,
            image = BitmapFactory.decodeResource(resources, R.drawable.image),
            mask = mask!!.toBitmap(),
            canvasSize = if (canvasSizeSeekBar.progress <= 1) 5 else canvasSizeSeekBar.progress,
            borderSize = borderSizeSeekBar.progress,
        )

    }


    private fun maskImage(
        view: ImageView,
        image: Bitmap,
        mask: Bitmap,
        canvasSize: Int,
        borderSize: Int
    ): Bitmap {
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
        val newBitmap = Bitmap.createBitmap(
            bitmap.width + borderSize,
            bitmap.height + borderSize,
            Bitmap.Config.ARGB_8888
        )
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

        return newBitmap
    }

    class ShapeAdapter(
        private val shapeList: List<Drawable>,
        private var image: Bitmap,
        private var size: Int,
        val onClick: (Drawable) -> Unit
    ) :
        RecyclerView.Adapter<ShapeAdapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.shape_view, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            var shape = shapeList[position].toBitmap()
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

            holder.view.setOnClickListener{ onClick(shapeList[position]) }
        }

        override fun getItemCount(): Int {
            return shapeList.size
        }

        class ViewHolder(ItemView: View) : RecyclerView.ViewHolder(ItemView) {
            val view: ImageView = itemView.findViewById(R.id.imageview)
        }
    }

}
