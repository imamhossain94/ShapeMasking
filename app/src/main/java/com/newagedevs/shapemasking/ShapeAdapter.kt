package com.newagedevs.shapemasking

import android.graphics.*
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.recyclerview.widget.RecyclerView

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