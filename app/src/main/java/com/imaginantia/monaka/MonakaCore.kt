package com.imaginantia.monaka

import android.graphics.PointF
import android.os.Build
import android.util.Log
import android.view.MotionEvent
import android.view.inputmethod.InputConnection
import androidx.annotation.RequiresApi
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.*

class MonakaCore {
    val heightScale: Float = 1.5f
    var time: Float = 0.0f
    var resolution: PointF = PointF(0f, 0f)
    var width: Int = 0
    var height: Int = 0
    lateinit var renderer: MonakaRenderer

    private var ic: InputConnection? = null
    private var presented: Boolean = false
    private var strokes: Map<Int, MonakaStroke> = mapOf()
    private lateinit var presentTime: LocalDateTime

    fun init() {
        Log.d("Monaka","Init")
    }

    fun resized(width: Int, height: Int) {
        Log.d("Monaka", "Resized $width $height")
        this.width = width
        this.height = height
        this.resolution = PointF(width.toFloat(), height.toFloat())
    }

    fun frame() {
        val now = LocalDateTime.now()
        val diff = presentTime.until(now, ChronoUnit.MILLIS)
        this.time = diff / 1000.0f;
        for(stroke in strokes.values) {
            stroke.frame()
        }
    }

    fun applyInputType(inputType: Int) {
        Log.d("Monaka", "InputType $inputType")
    }

    fun present(ic: InputConnection) {
        this.ic = ic
        if(presented) return
        Log.d("Monaka","Present")
        presented = true
        presentTime = LocalDateTime.now()
    }

    fun dismiss() {
        if(!presented) return
        Log.d("Monaka","Dismiss")
        ic = null
        presented = false
    }

    fun touched(event: MotionEvent) {
        val action = event.actionMasked
        val pointerIndex = event.actionIndex
        val pointerId = event.getPointerId(pointerIndex)
        if (!strokes.containsKey(pointerId)) {
            strokes += pointerId to MonakaStroke(this, pointerId)
        }
        val x = event.getX(pointerIndex)
        val y = event.getY(pointerIndex)
        val p = PointF(x, y)
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
            val x = event.getX(index)
            val y = event.getY(index)
            val p = PointF(x, y)
            strokes[id]?.move(p)
        }
    }

    fun commit(s: String) {
       ic?.commitText(s, 1)
    }
}