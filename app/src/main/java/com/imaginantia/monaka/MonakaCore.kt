package com.imaginantia.monaka

import android.graphics.PointF
import android.util.Log
import android.view.MotionEvent
import android.view.inputmethod.InputConnection

class MonakaCore {
    var ic: InputConnection? = null
    var presented: Boolean = false
    var strokes: Map<Int, MonakaStroke> = mapOf()

    fun init() {
        Log.d("Monaka","Init")
    }

    fun resized(width: Int, height: Int) {
        Log.d("Monaka", "Resized $width $height")
    }

    fun applyInputType(inputType: Int) {
        Log.d("Monaka", "InputType $inputType")
    }

    fun present(ic: InputConnection) {
        this.ic = ic
        if(presented) return
        Log.d("Monaka","Present")
        presented = true
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
        Log.d("Monaka", "Event ${pointerId} ${action}")
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