package com.imaginantia.monaka

import android.graphics.PointF
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import com.imaginantia.monaka.draw.Draw
import com.imaginantia.monaka.draw.Mesh
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class MonakaRenderer(val core: MonakaCore): GLSurfaceView.Renderer {
    var time: Float = 0.0f

    private var background: Draw? = null

    public override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        val m = Mesh.build(floatArrayOf(-1f, -1f, -1f, 1f, 1f, 1f, 1f, 1f, 1f, -1f, -1f, -1f), 2)
        background = Draw.build(core,
            """
            attribute vec2 vertex; 
            varying vec2 coord; 
            void main() { 
                coord = (vertex*vec2(1,-1)*0.5+0.5) * resolution;
                gl_Position = vec4(vertex, 0.0, 1.0); 
            }
            """.trimIndent(),
            """
            varying vec2 coord;
            uniform vec2 p;
            float alpha(float x) {
                return clamp(x + 0.5, 0., 1.);
            }
            void main() {
                float w = resolution.x;
                float h = resolution.y;
                float d = length(coord - vec2(w/2., h*4.)) - h*4. + 10.0;
                float bold = 4.;
                vec4 c = vec4(0, 1, 0, exp(- max(abs(d)-bold, 0.) * 0.3));
                if(d < 0.) c.a = c.a * 0.5 + 0.5;
                c = mix(vec4(1), c, alpha(abs(d)-bold));
                if(length(p - coord) < 20.) c = vec4(1, 0.5, 0, 1);
                gl_FragColor = c;
            }
            """.trimIndent() , m, arrayOf("p")
        )
    }

    public override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        core.resized(width, height)
    }

    public override fun onDrawFrame(gl: GL10?) {
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f)
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
        GLES20.glEnable(GLES20.GL_BLEND)
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA)
        core.frame()
        background?.draw()
    }

    fun setPoint(p: PointF) {
        background?.f2("p", p.x, p.y)
    }
}