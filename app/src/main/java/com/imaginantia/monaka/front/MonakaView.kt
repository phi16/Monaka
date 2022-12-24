package com.imaginantia.monaka.front

import android.content.Context
import android.content.res.Resources
import android.graphics.PixelFormat
import android.graphics.PointF
import android.opengl.GLSurfaceView
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.MotionEvent
import android.view.View
import com.imaginantia.monaka.MonakaCore
import kotlin.math.roundToInt

class MonakaView : GLSurfaceView {
    private val core: MonakaCore
    private val primary: Boolean

    constructor(context: Context, core: MonakaCore, primary: Boolean) : super(context) {
        this.core = core
        this.primary = primary
        holder.setFormat(PixelFormat.TRANSLUCENT)
        val dip = 160
        val heightScale = core.heightScale
        val metrics = Resources.getSystem().displayMetrics
        var height = (metrics.density * dip * heightScale).roundToInt()
        if(!primary) {
            height = metrics.heightPixels - 132 - height
        }
        holder.setFixedSize(metrics.widthPixels, height)

        setZOrderOnTop(true)
        setEGLContextClientVersion(2)
        setEGLConfigChooser(8, 8, 8, 8, 0, 0)
        val renderer = MonakaRenderer(core, primary)
        setRenderer(renderer)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if(!primary) return false
        event?.let { core.touched(it) }
        return true
    }
}