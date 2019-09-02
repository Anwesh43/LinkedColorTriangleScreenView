package com.anwesh.uiprojects.colortrainglescreenview

/**
 * Created by anweshmishra on 02/09/19.
 */

import android.content.Context
import android.app.Activity
import android.view.View
import android.view.MotionEvent
import android.graphics.Paint
import android.graphics.Canvas
import android.graphics.Path
import android.graphics.Color
import android.graphics.RectF

val colors : Array<String> = arrayOf("#00BFA5", "#9C27B0", "#64DD17", "#f44336", "#0D47A1")
val scGap : Float = 0.05f
val backColor : Int = Color.parseColor("#BDBDBD")
val triSizeFactor : Float = 3f

fun Int.inverse() : Float = 1f / this
fun Float.maxScale(i : Int, n : Int) : Float = Math.max(0f, this - i * n.inverse())
fun Float.divideScale(i : Int, n : Int) : Float = Math.min(n.inverse(), maxScale(i, n)) * n

fun Canvas.drawTriangle(size : Float, paint : Paint) {
    val path : Path = Path()
    path.moveTo(-size, size)
    path.lineTo(0f, -size)
    path.lineTo(size, size)
    drawPath(path, paint)
}

fun Canvas.drawTriangleScreen(i : Int, scale : Float, sc : Float, paint : Paint) {
    val w : Float = width.toFloat()
    val h : Float = height.toFloat()
    val size : Float = Math.min(w, h) / triSizeFactor
    var y : Float = 0f
    if (sc > 0f) {
        y = 2 * size * (1 - sc)
    }
    paint.color = Color.parseColor(colors[i])
    save()
    translate(w / 2, h / 2)
    clipRect(RectF(-size, -size, size, size))
    save()
    translate(0f, -size -2 * size * scale + y)
    drawTriangle(size, paint)
    restore()
    restore()
}

class ColorTriangleScreenView(ctx : Context) : View(ctx) {

    private val paint : Paint = Paint(Paint.ANTI_ALIAS_FLAG)

    override fun onDraw(canvas : Canvas) {

    }

    override fun onTouchEvent(event : MotionEvent) : Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {

            }
        }
        return true
    }

    data class State(var scale : Float = 0f, var prevScale : Float = 0f, var dir : Float = 0f) {

        fun update(cb : (Float) -> Unit) {
            scale += dir * scGap
            if (Math.abs(scale - prevScale) > 1) {
                scale = prevScale + dir
                dir = 0f
                prevScale = scale
                cb(prevScale)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            if (dir == 0f) {
                dir = 1f - 2 * prevScale
                cb()
            }
        }
    }

    data class Animator(var view : View, var animated : Boolean = false) {

        fun animate(cb : () -> Unit) {
            if (animated) {
                cb()
                try {
                    Thread.sleep(50)
                    view.invalidate()
                } catch(ex : Exception) {

                }
            }
        }

        fun start() {
            if (!animated) {
                animated = true
                view.postInvalidate()
            }
        }

        fun stop() {
            if (animated) {
                animated = false
            }
        }
    }
}