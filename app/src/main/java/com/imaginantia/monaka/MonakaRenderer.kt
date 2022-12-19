package com.imaginantia.monaka

import android.opengl.GLSurfaceView
import android.util.Log
import com.imaginantia.monaka.draw.Material
import com.imaginantia.monaka.draw.Mesh
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class MonakaRenderer(val core: MonakaCore): GLSurfaceView.Renderer {
    var m: Mesh? = null
    var r: Material? = null

    public override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        m = Mesh.build(floatArrayOf(-0.5f, -0.5f, 0f, 0.5f, 0.5f, -0.5f), 2)
        r = Material.build(
            "attribute vec2 vertex; varying vec2 coord; void main() { coord = vertex; gl_Position = vec4(vertex, 0.0, 1.0); }",
            "varying vec2 coord; void main() { gl_FragColor = vec4(fract(coord+.5), 0, 1); }")
    }

    public override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        core.resized(width, height)
    }

    public override fun onDrawFrame(gl: GL10?) {
        gl?.glClearColor(0.0f, 0.5f, 1.0f, 0.5f)
        gl?.glClear(GL10.GL_COLOR_BUFFER_BIT)
        m?.also { r?.draw(it) }
    }
}