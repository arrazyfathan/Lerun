package com.androiddevs.lerun.utils

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.content.Context
import android.content.res.ColorStateList
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.marginTop
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

fun View.changeBackgroundColor(fromColor: Int, toColor: Int) {
    val valueAnimator = colorAnimator(fromColor, toColor)
    valueAnimator.addUpdateListener {
        setBackgroundColor(it.animatedValue as Int)
    }
    valueAnimator.start()
}

fun ImageButton.changeTintColor(fromColor: Int, toColor: Int) {
    val valueAnimator = colorAnimator(fromColor, toColor)
    valueAnimator.addUpdateListener {
        imageTintList = ColorStateList.valueOf(it.animatedValue as Int)
    }
    valueAnimator.start()
}

fun TextView.changeTextColor(fromColor: Int, toColor: Int) {
    val animator = colorAnimator(fromColor, toColor)
    animator.addUpdateListener { value ->
        setTextColor(value.animatedValue as Int)
    }
    animator.start()
}

fun color(context: Context, color: Int): Int {
    return ContextCompat.getColor(context, color)
}

fun colorAnimator(fromColor: Int, toColor: Int): ValueAnimator {
    val valueAnimator = ValueAnimator.ofObject(ArgbEvaluator(), fromColor, toColor)
    valueAnimator.duration = 300
    valueAnimator.interpolator = DecelerateInterpolator()
    return valueAnimator
}
