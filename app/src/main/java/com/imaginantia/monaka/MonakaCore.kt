package com.imaginantia.monaka

import android.graphics.PointF
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputConnection
import com.imaginantia.monaka.front.MonakaRenderer
import com.imaginantia.monaka.front.MonakaService
import com.imaginantia.monaka.front.MonakaStroke
import com.imaginantia.monaka.front.MonakaView
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

class MonakaCore {
    val heightScale: Float = 1.5f
    var time: Float = 0.0f
    var resolution: PointF = PointF(0f, 0f)
    var width: Int = 0
    var height: Int = 0
    var subRatio: Float = 0f
    var layout: MonakaLayout = MonakaLayout()
    lateinit var service: MonakaService
    lateinit var mainView: MonakaView
    lateinit var subView: MonakaView

    private var ic: InputConnection? = null
    private var presented: Boolean = false
    private var strokes: Map<Int, MonakaStroke> = mapOf()
    private lateinit var presentTime: LocalDateTime
    private val handler: Handler = Handler(Looper.getMainLooper())
    private var frameTimer: Runnable? = null
    private var subTimer: Runnable? = null
    private var mainStroke: MonakaStroke? = null

    fun init(service: MonakaService) {
        Log.d("Monaka","Init")
        this.service = service
    }

    fun resized(width: Int, height: Int) {
        Log.d("Monaka", "Resized $width $height")
        this.width = width
        this.height = height
        this.resolution = PointF(width.toFloat(), height.toFloat())
    }

    fun subResized(height: Int) {
        this.subRatio = resolution.y / height.toFloat()
    }

    fun subShow() {
        // service.setCandidatesViewShown(true)
        subView.visibility = View.VISIBLE
    }

    fun subHide() {
        // service.setCandidatesViewShown(false)
        subView.visibility = View.GONE
    }

    fun frame() {
        val now = LocalDateTime.now()
        val diff = presentTime.until(now, ChronoUnit.MILLIS)
        time = diff / 1000.0f;
    }

    fun applyInputType(inputType: Int) {
        Log.d("Monaka", "InputType $inputType")
    }

    fun present() {
        if(presented) return
        Log.d("Monaka","Present")
        presented = true
        presentTime = LocalDateTime.now()
        frameTimer = Runnable {
            frame()
            frameTimer?.let { handler.postDelayed(it, 16 ) }
        }
        frameTimer?.run()
    }

    fun dismiss() {
        if(!presented) return
        Log.d("Monaka","Dismiss")
        ic = null
        presented = false
        frameTimer?.let {
            handler.removeCallbacks(it)
            frameTimer = null
        }
        subTimer?.let {
            handler.removeCallbacks(it)
            subTimer = null
        }
        subHide()
    }

    fun touched(event: MotionEvent) {
        this.ic = service.currentInputConnection
        val getPoint = { index: Int ->
            val x = event.getX(index)
            val y = event.getY(index)
            PointF((x - resolution.x) / resolution.y, (y - resolution.y) / resolution.y)
        }
        val action = event.actionMasked
        if(action == MotionEvent.ACTION_DOWN) {
            subShow()
            subTimer?.let {
                handler.removeCallbacks(it)
                subTimer = null
            }
        }
        if(action == MotionEvent.ACTION_UP) {
            subTimer = Runnable {
                subHide()
            }
            subTimer?.let { handler.postDelayed(it, 500) }
        }

        val pointerIndex = event.actionIndex
        val pointerId = event.getPointerId(pointerIndex)
        if (!strokes.containsKey(pointerId)) {
            strokes += pointerId to MonakaStroke(this, pointerId)
        }
        val p = getPoint(pointerIndex)
        strokes[pointerId]?.also { stroke ->
            when (action) {
                MotionEvent.ACTION_DOWN -> stroke.down(p)
                MotionEvent.ACTION_POINTER_DOWN -> stroke.down(p)
                MotionEvent.ACTION_MOVE -> stroke.move(p)
                MotionEvent.ACTION_UP,
                MotionEvent.ACTION_POINTER_UP,
                MotionEvent.ACTION_CANCEL,
                MotionEvent.ACTION_OUTSIDE -> {
                    stroke.up(p)
                    strokes -= pointerId
                }
            }
        }
        for (id in strokes.keys) {
            if(id == pointerId) continue
            val index = event.findPointerIndex(id)
            val p = getPoint(index)
            strokes[id]?.move(p)
        }
    }

    fun retainStroke(stroke: MonakaStroke): Boolean {
        if(mainStroke != null) {
            return false
        }
        mainStroke = stroke
        Log.d("Monaka", "Retained")
        return true
    }

    fun releaseStroke() {
        mainStroke = null
        Log.d("Monaka", "Released")
    }

    fun commit(s: String) {
       ic?.commitText(s, 1)
    }
}