package com.arrazyfathan.lerun.utils

import android.content.Context
import android.widget.Toast
import androidx.core.content.ContextCompat

fun color(
    context: Context,
    color: Int,
): Int = ContextCompat.getColor(context, color)

fun Context.toast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}
