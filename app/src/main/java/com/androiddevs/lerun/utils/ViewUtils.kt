package com.androiddevs.lerun.utils

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.marginTop
import androidx.fragment.app.Fragment
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


/**
 * Created by Ar Razy Fathan Rabbani on 06/12/22.
 */

internal fun View.expand() {
    GlobalScope.launch(Dispatchers.Default) {
        val layoutParams = this@expand.layoutParams as ViewGroup.MarginLayoutParams
        val measureHeight = this@expand.measuredHeight
        val minusTopMargin = measureHeight - (measureHeight * 2)
        ValueAnimator.ofInt(minusTopMargin, 0)
            .apply {
                addUpdateListener {
                    val value = it.animatedValue as Int
                    layoutParams.setMargins(0, value, 0, 0)
                    this@expand.layoutParams = layoutParams
                }
                duration = 300
                interpolator = DecelerateInterpolator()
            }
            .also {
                withContext(Dispatchers.Main) {
                    it.start()
                }
            }
    }
}

internal fun View.collapse() {
    GlobalScope.launch(Dispatchers.Default) {
        val layoutParams = this@collapse.layoutParams as ViewGroup.MarginLayoutParams
        val measureHeight = this@collapse.measuredHeight
        val minusTopMargin = measureHeight - (measureHeight * 2)
        ValueAnimator.ofInt(this@collapse.marginTop, minusTopMargin)
            .apply {
                addUpdateListener {
                    val value = it.animatedValue as Int
                    layoutParams.setMargins(0, value, 0, 0)
                    this@collapse.layoutParams = layoutParams
                }
                duration = 300
                interpolator = DecelerateInterpolator()
            }
            .also {
                withContext(Dispatchers.Main) {
                    it.start()
                }
            }
    }
}

fun View.changeBackgroundColor(
    fromColor: Int,
    toColor: Int,
) {
    val valueAnimator = colorAnimator(fromColor, toColor)
    valueAnimator.addUpdateListener {
        setBackgroundColor(it.animatedValue as Int)
    }
    valueAnimator.start()
}

fun ImageButton.changeTintColor(
    fromColor: Int,
    toColor: Int,
) {
    val valueAnimator = colorAnimator(fromColor, toColor)
    valueAnimator.addUpdateListener {
        imageTintList = ColorStateList.valueOf(it.animatedValue as Int)
    }
    valueAnimator.start()
}

fun TextView.changeTextColor(
    fromColor: Int,
    toColor: Int,
) {
    val animator = colorAnimator(fromColor, toColor)
    animator.addUpdateListener { value ->
        setTextColor(value.animatedValue as Int)
    }
    animator.start()
}

fun color(
    context: Context,
    color: Int,
): Int {
    return ContextCompat.getColor(context, color)
}

fun colorAnimator(
    fromColor: Int,
    toColor: Int,
): ValueAnimator {
    val valueAnimator = ValueAnimator.ofObject(ArgbEvaluator(), fromColor, toColor)
    valueAnimator.duration = 300
    valueAnimator.interpolator = DecelerateInterpolator()
    return valueAnimator
}

context(Fragment)
fun String.toast() {
    Toast.makeText(requireContext(), this, Toast.LENGTH_SHORT).show()
}

fun View.gone() {
    this.visibility = View.GONE
}


fun View.visible() {
    this.visibility = View.VISIBLE
}

fun Fragment.getBitmapFromVectorDrawable(drawableId: Int): Bitmap {
    val drawable = ContextCompat.getDrawable(requireContext(), drawableId)
    val bitmap = Bitmap.createBitmap(
        drawable!!.intrinsicWidth,
        drawable.intrinsicHeight, Bitmap.Config.ARGB_8888
    )
    val canvas = Canvas(bitmap)
    drawable.setBounds(0, 0, canvas.width, canvas.height)
    drawable.draw(canvas)
    return bitmap
}

fun Fragment.bitmapDescriptorFromVector(vectorResId: Int, size: Int = 10): BitmapDescriptor? {
    return ContextCompat.getDrawable(requireContext(), vectorResId)?.run {
        setBounds(0, 0, intrinsicWidth, intrinsicHeight)
        val bitmap = Bitmap.createBitmap(intrinsicWidth, intrinsicHeight, Bitmap.Config.ARGB_8888)
        draw(Canvas(bitmap))
        val resize = Bitmap.createScaledBitmap(bitmap, size, size, false)
        BitmapDescriptorFactory.fromBitmap(resize)
    }
}


