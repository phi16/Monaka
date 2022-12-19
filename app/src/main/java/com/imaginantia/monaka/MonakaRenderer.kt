package com.imaginantia.monaka

import android.opengl.GLSurfaceView
import android.util.Log
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class MonakaRenderer: GLSurfaceView.Renderer {
    val core: MonakaCore

    constructor(core: MonakaCore) {
        this.core = core
    }

    public override fun onDrawFrame(gl: GL10?) {
        gl?.glClearColor(0.0f, 0.5f, 1.0f, 0.5f)
        gl?.glClear(GL10.GL_COLOR_BUFFER_BIT)
    }

    public override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        core.surfaceChanged(width, height)
    }

    public override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        core.surfaceCreated()
    }
}