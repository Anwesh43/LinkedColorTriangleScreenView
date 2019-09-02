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
val scGap : Float = 0.01f
val backColor : Int = Color.parseColor("#BDBDBD")
val triSizeFactor : Float = 2f
val squareSides : Int = 4
val squareSideFactor : Float = 4.5f
val strokeFactor : Float = 45f
val squareColor : Int = Color.WHITE
val delay : Long = 25

fun Int.inverse() : Float = 1f / this
fun Float.maxScale(i : Int, n : Int) : Float = Math.max(0f, this - i * n.inverse())
fun Float.divideScale(i : Int, n : Int) : Float = Math.min(n.inverse(), maxScale(i, n)) * n

fun Canvas.clipTriangle(size : Float) {
    val path : Path = Path()
    path.moveTo(-size, 2 * size)
    path.lineTo(0f, 0f)
    path.lineTo(size, 2 * size)
    clipPath(path)
}

fun Canvas.drawSquareLine(i : Int, size : Float, sc : Float, paint : Paint) {
    save()
    rotate(90f * i)
    drawLine(-size + 2 * size * sc.divideScale(i, squareSides), -size, size, -size, paint)
    restore()
}

fun Canvas.drawStrokedSquare(size : Float, sc : Float, paint : Paint) {
    val squareSize : Float = size / squareSideFactor
    paint.strokeCap = Paint.Cap.ROUND
    paint.strokeWidth = size / strokeFactor
    paint.color = squareColor
    for (j in 0..(squareSides - 1)) {
        drawSquareLine(j, squareSize, sc, paint)
    }
}

fun Canvas.drawTriangleScreen(i : Int, scale : Float, sc : Float, paint : Paint) : Float {
    val w : Float = width.toFloat()
    val h : Float = height.toFloat()
    val size : Float = Math.min(w, h) / triSizeFactor
    val sc1 : Float = scale.divideScale(0, 2)
    val sc2 : Float = scale.divideScale(1, 2)
    var y : Float = 0f
    if (sc > 0f) {
        y = 2 * size * (1 - sc)
    }
    save()
    translate(w / 2, h / 2 - size)
    clipTriangle(size)
    save()
    translate(0f,- 2 * size * sc2 + y)
    paint.color = Color.parseColor(colors[i])
    drawRect(RectF(-size, 0f, size, 2 * size), paint)
    save()
    translate(0f, size)
    drawStrokedSquare(size, sc1, paint)
    restore()
    restore()
    restore()
    return sc2
}

class ColorTriangleScreenView(ctx : Context) : View(ctx) {

    private val paint : Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val renderer : Renderer = Renderer(this)

    override fun onDraw(canvas : Canvas) {
        renderer.render(canvas, paint)
    }

    override fun onTouchEvent(event : MotionEvent) : Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                renderer.handleTap()
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
                    Thread.sleep(delay)
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

    data class CTSNode(var i : Int, val state : State = State()) {

        private var next : CTSNode? = null
        private var prev : CTSNode? = null

        init {
            addNeighbor()
        }

        fun addNeighbor() {
            if (i < colors.size - 1) {
                next = CTSNode(i + 1)
                next?.prev = this
            }
        }

        fun draw(canvas : Canvas, sc : Float, paint : Paint) {
            val sck : Float = canvas.drawTriangleScreen(i, state.scale, sc, paint)
            if (sck > 0f) {
                next?.draw(canvas, sck, paint)
            }
        }

        fun update(cb : (Float) -> Unit) {
            state.update(cb)
        }

        fun startUpdating(cb : () -> Unit) {
            state.startUpdating(cb)
        }

        fun getNext(dir : Int, cb : () -> Unit) : CTSNode {
            var curr : CTSNode? = prev
            if (dir == 1) {
                curr = next
            }
            if (curr != null) {
                return curr
            }
            cb()
            return this
        }
    }

    data class ColorTriangleScreen(var i : Int) {

        private val root : CTSNode = CTSNode(0)
        private var curr : CTSNode = root
        private var dir : Int = 1

        fun draw(canvas : Canvas, paint : Paint) {
            root.draw(canvas, 0f, paint)
        }

        fun update(cb : (Float) -> Unit) {
            curr.update {
                curr = curr.getNext(dir) {
                    dir *= -1
                }
                cb(it)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            curr.startUpdating(cb)
        }
    }

    data class Renderer(var view : ColorTriangleScreenView) {

        private val cts : ColorTriangleScreen = ColorTriangleScreen(0)
        private val animator : Animator = Animator(view)

        fun render(canvas : Canvas, paint : Paint) {
            canvas.drawColor(backColor)
            cts.draw(canvas, paint)
            animator.animate {
                cts.update {
                    animator.stop()
                }
            }
        }

        fun handleTap() {
            cts.startUpdating {
                animator.start()
            }
        }
    }

    companion object {

        fun create(activity : Activity) : ColorTriangleScreenView {
            val view : ColorTriangleScreenView = ColorTriangleScreenView(activity)
            activity.setContentView(view)
            return view
        }
    }
}