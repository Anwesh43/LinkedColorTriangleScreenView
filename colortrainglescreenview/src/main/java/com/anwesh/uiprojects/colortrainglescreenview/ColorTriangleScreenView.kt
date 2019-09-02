package com.anwesh.uiprojects.colortrainglescreenview

/**
 * Created by anweshmishra on 02/09/19.
 */

import android.content.Context
import android.app.Activity
import android.view.View
import android.view.MotionEvent
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Color
import android.graphics.RectF

val colors : Array<String> = arrayOf("#00BFA5", "#9C27B0", "#64DD17", "#f44336", "#0D47A1")
val scGap : Float = 0.05f
val backColor : Int = Color.parseColor("#BDBDBD")
val triSizeFactor : Float = 3f
