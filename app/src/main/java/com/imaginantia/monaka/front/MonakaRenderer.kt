package com.imaginantia.monaka.front

import android.graphics.PointF
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import com.imaginantia.monaka.MonakaCore
import com.imaginantia.monaka.draw.Draw
import com.imaginantia.monaka.draw.Mesh
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class MonakaRenderer(private val core: MonakaCore, private val primary: Boolean): GLSurfaceView.Renderer {
    var time: Float = 0.0f

    private lateinit var rect: Mesh
    private lateinit var quads: Mesh
    private var background: Draw? = null
    private var buttons: Draw? = null

    public override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        rect = Mesh.build(floatArrayOf(-1f, -1f, -1f, 1f, 1f, 1f, 1f, 1f, 1f, -1f, -1f, -1f), 2)
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
            float map(float a, float b, float x) {
                return clamp((x-a)/(b-a), 0., 1.);
            }
            float alpha(float x) {
                return clamp(-x + 0.5, 0., 1.);
            }
            vec4 over(vec4 a, vec4 b) {
                float o = a.w + b.w * (1. - a.w);
                if(o < 0.00001) return vec4(0);
                vec3 c = a.rgb * a.w + b.rgb * b.w * (1. - a.w);
                return vec4(c/o, o);
            }
            float e0(float t, float k) {
                if(t < 0.) return 0.;
                if(t > 1.) return 1.;
                float x = exp(-t * k);
                float s0 = 1.;
                float s1 = exp(-k);
                return (x-s0) / (s1-s0);
            }
            void main() {
                float w = resolution.x;
                float h = resolution.y;
                vec3 baseColor = mix(vec3(0.5,1,0), vec3(0,1,0.5), coord.x/w);
                vec3 frontColor = baseColor * 0.5;
                vec3 shadowColor = vec3(0);
                vec3 borderColor = vec3(1);
                frontColor = mix(frontColor, vec3(1.) * dot(frontColor, vec3(1./3.)), 0.5);
                
                float radOffset = h * mix(1., 0.05, e0(time*2., 8.));
                float slant = length(vec2(w/2., radOffset));
                float fullR = slant/2. * (slant / radOffset);
                float radius = fullR;
                float d = length(coord - vec2(w/2., radius)) - radius + 5.0 /* outer shadow */;
                float bold = h * 0.02;
                vec4 c = vec4(shadowColor, 1);
                if(d > - bold/2.) c.a = exp(-max(d, 0.) * 0.3) * 0.2;
                else {
                    c.rgb = frontColor;
                    c.a = exp(-max(-d-bold, 0.) * 0.01) * 0.2 + 0.5;
                }
                float borderAlpha = alpha(abs(d+bold/2.) - bold/2.);
                c = over(vec4(borderColor, borderAlpha), c);
                gl_FragColor = c;
            }
            """.trimIndent() , arrayOf()
        )
        val quad = floatArrayOf(-1f, 0f, 0f, 1f, 0f, -1f, 0f, -1f, 0f, 1f, 1f, 0f)
        val points = core.layout.buttons
        val qCount = points.size / 3
        var qVerts = FloatArray((2 + 3) * 6 * qCount)
        var k = 0
        for(i in 0 until qCount) {
            for(j in 0 until 6) {
                qVerts[k+0] = quad[j*2+0]
                qVerts[k+1] = quad[j*2+1]
                qVerts[k+2] = points[i*3+0]
                qVerts[k+3] = points[i*3+1]
                qVerts[k+4] = points[i*3+2]
                k += 5
            }
        }
        quads = Mesh.build(qVerts, 2, 3)
        buttons = Draw.build(core,
            """
            attribute vec2 vertex; 
            attribute vec3 uv;
            varying vec2 coord; 
            varying vec3 local;
            uniform vec2 p;
            float e0(float t, float k) {
                if(t < 0.) return 0.;
                if(t > 1.) return 1.;
                float x = exp(-t * k);
                float s0 = 1.;
                float s1 = exp(-k);
                return (x-s0) / (s1-s0);
            }
            void main() { 
                float size = uv.z * e0(time*2., 4.);
                float realSize = size + 0.01; // shadow
                coord = uv.xy;
                if(length(uv.xy) < 0.01) coord = p;
                coord += vertex * realSize;
                local = vec3(vertex.x + vertex.y, vertex.x - vertex.y, 0) * realSize;
                local.z = size;
                
                coord *= resolution.y;
                coord += resolution;
                coord.y *= native.x;
                local *= resolution.y;
                gl_Position = vec4((coord / resolution * 2. - 1.) * vec2(1,-1) + vec2(0, native.y), 0.0, 1.0);
            }
            """.trimIndent(),
            """
            varying vec2 coord;
            varying vec3 local;
            float map(float a, float b, float x) {
                return clamp((x-a)/(b-a), 0., 1.);
            }
            float alpha(float x) {
                return clamp(-x + 0.5, 0., 1.);
            }
            vec4 over(vec4 a, vec4 b) {
                float o = a.w + b.w * (1. - a.w);
                if(o < 0.00001) return vec4(0);
                vec3 c = a.rgb * a.w + b.rgb * b.w * (1. - a.w);
                return vec4(c/o, o);
            }
            void main() {
                float w = resolution.x;
                float h = resolution.y;
                vec3 baseColor = mix(vec3(0.5,1,0), vec3(0,1,0.5), coord.x/w);
                vec3 frontColor = mix(baseColor, vec3(1), 0.8);
                vec3 borderColor = mix(baseColor, vec3(1), 0.95);
                vec3 shadowColor = baseColor * 0.2;
                
                float corner = h * 0.02;
                vec2 q = abs(local.xy) - (local.z - corner);
                float d = length(max(q,0.)) + min(max(q.x, q.y), 0.) - corner;
                vec4 c = vec4(shadowColor, 0.5);
                if(d > - corner/2.) c.a = exp(-max(d, 0.) * 0.3) * 0.2;
                
                vec2 shadowOffset = vec2(1, -1) * h * 0.005;
                q = abs(local.xy + shadowOffset) - (local.z - corner);
                d = length(max(q,0.)) + min(max(q.x, q.y), 0.) - corner;
                if(d < - corner / 2.) {
                    c.rgb = frontColor;
                    c.a = 0.5;
                }
                float borderAlpha = alpha(abs(d+corner/2.) - corner/2.);
                c = over(vec4(borderColor, borderAlpha), c);
                gl_FragColor = c;
            }
            """.trimIndent(), arrayOf("p")
        )
    }

    public override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        if(primary) core.resized(width, height)
        else core.subResized(height)

        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f)
        GLES20.glViewport(0, 0, width, height)
        GLES20.glDisable(GLES20.GL_CULL_FACE)
        GLES20.glEnable(GLES20.GL_BLEND)
        GLES20.glBlendFuncSeparate(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA, GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA)
    }

    public override fun onDrawFrame(gl: GL10?) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)

        if(primary) background?.draw(rect, primary)
        buttons?.f2("p", core.layout.p.x, core.layout.p.y)
        buttons?.draw(quads, primary)
    }
}