package com.imaginantia.monaka

import android.content.Context
import android.content.res.Resources
import android.graphics.PixelFormat
import android.opengl.GLSurfaceView
import android.util.Log
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.ViewGroup.LayoutParams
import kotlin.math.roundToInt

class MonakaView : GLSurfaceView {
    private val core: MonakaCore

    constructor(context: Context, core: MonakaCore) : super(context) {
        this.core = core
        holder.setFormat(PixelFormat.TRANSLUCENT)
        val dip = 160
        val heightScale = core.heightScale
        val metrics = Resources.getSystem().displayMetrics
        holder.setFixedSize(metrics.widthPixels, (metrics.density * dip * heightScale).roundToInt())
        setZOrderOnTop(true)
        setEGLContextClientVersion(2)
        setEGLConfigChooser(8, 8, 8, 8, 0, 0)
        core.renderer = MonakaRenderer(core)
        setRenderer(core.renderer)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        event?.let { core.touched(it) }
        return true
    }
}