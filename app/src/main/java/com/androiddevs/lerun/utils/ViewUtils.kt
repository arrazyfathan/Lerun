package com.androiddevs.lerun.utils

import android.animation.ValueAnimator
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.view.animation.LinearInterpolator
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
        ValueAnimator.ofInt(minusTopMargin, 9)
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
