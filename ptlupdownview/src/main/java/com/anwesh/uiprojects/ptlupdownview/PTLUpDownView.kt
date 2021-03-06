package com.anwesh.uiprojects.ptlupdownview

/**
 * Created by anweshmishra on 07/09/18.
 */

import android.app.Activity
import android.view.View
import android.view.MotionEvent
import android.graphics.Canvas
import android.graphics.Paint
import android.content.Context
import android.graphics.Color

val nodes : Int = 5

fun Canvas.drawPTLUDNode(i : Int, scale : Float, paint : Paint) {
    val w : Float = width.toFloat()
    val h : Float = height.toFloat()
    val gap : Float = (w / (nodes + 1))
    val index : Int = i % 2
    val sf : Float = 1f - 2 * index
    val size : Float = gap / 3
    val sc1 : Float = Math.min(0.5f, scale) * 2
    val sc2 : Float = Math.min(0.5f, Math.max(scale -0.5f, 0f)) * 2
    paint.color = Color.WHITE
    paint.strokeWidth = Math.min(w, h) / 60
    paint.strokeCap = Paint.Cap.ROUND
    save()
    translate(gap/4 + gap/2 + gap * i, h/2 + (h/2 - size/2) * sc2 * sf)
    for (j in 0..1) {
        save()
        rotate(90f * (1 - sc1) * sf * j)
        drawLine(0f, -size/2, 0f, size/2, paint)
        restore()
    }
    restore()
}

class PTLUpDownView(ctx : Context) : View(ctx) {

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
            scale += 0.05f * dir
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
    }

    data class PTLUDNode(var i : Int, val state : State = State()) {
        private var next : PTLUDNode? = null
        private var prev  : PTLUDNode? = null

        fun addNeighbor() {
            if (i < nodes - 1) {
                next = PTLUDNode(i + 1)
                next?.prev = this
            }
        }

        init {
            addNeighbor()
        }

        fun draw(canvas : Canvas, paint : Paint) {
            canvas.drawPTLUDNode(i, state.scale, paint)
            next?.draw(canvas, paint)
        }

        fun startUpdating(cb : () -> Unit) {
            state.startUpdating(cb)
        }

        fun update(cb : (Int, Float) -> Unit) {
            state.update {
                cb(i, it)
            }
        }

        fun getNext(dir : Int, cb : () -> Unit)  : PTLUDNode {
            var curr : PTLUDNode? = prev
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

    data class LinkedPTLUD(var i : Int) {
        private var root : PTLUDNode = PTLUDNode(0)
        private var curr : PTLUDNode = root
        private var dir : Int = 1

        fun draw(canvas : Canvas, paint : Paint) {
            root.draw(canvas, paint)
        }

        fun update(cb : (Int, Float) -> Unit) {
            curr.update {i, scl ->
                curr = curr.getNext(dir) {
                    dir *= -1
                }
                cb(i, scl)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            curr.startUpdating(cb)
        }
    }

    data class Renderer(var view : PTLUpDownView) {

        private val animator : Animator = Animator(view)
        private val lptlud : LinkedPTLUD = LinkedPTLUD(0)

        fun render(canvas : Canvas, paint : Paint) {
            canvas.drawColor(Color.parseColor("#212121"))
            lptlud.draw(canvas, paint)
            animator.animate {
                lptlud.update {i, scl ->
                    animator.stop()
                }
            }
        }

        fun handleTap() {
            lptlud.startUpdating {
                animator.start()
            }
        }
    }

    companion object {
        fun create(activity : Activity) : PTLUpDownView {
            val view : PTLUpDownView = PTLUpDownView(activity)
            activity.setContentView(view)
            return view
        }
    }
}