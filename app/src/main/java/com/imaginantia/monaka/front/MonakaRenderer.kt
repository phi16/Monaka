package com.imaginantia.monaka.front

import android.graphics.PointF
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.util.Log
import androidx.core.graphics.minus
import com.imaginantia.monaka.MonakaCore
import com.imaginantia.monaka.MonakaLayout
import com.imaginantia.monaka.draw.Draw
import com.imaginantia.monaka.draw.Mesh
import java.lang.Math.exp
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class MonakaRenderer(private val core: MonakaCore, private val primary: Boolean): GLSurfaceView.Renderer {
    var time: Float = 0.0f

    private lateinit var rect: Mesh
    private lateinit var quads: Mesh
    private lateinit var rods: Mesh
    private var background: Draw? = null
    private var buttons: Draw? = null
    private var radial: Draw? = null
    private var annulus: Draw? = null

    public override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        rect = Mesh.build(floatArrayOf(-1f, -1f, -1f, 1f, 1f, 1f, 1f, 1f, 1f, -1f, -1f, -1f), 2)
        background = Draw.build(core, primary,
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
        val rhomb = floatArrayOf(-1f, 0f, 0f, 1f, 0f, -1f, 0f, -1f, 0f, 1f, 1f, 0f)
        val qCount = core.layout.buttons.size
        var qVerts = FloatArray((2 + 3) * 6 * qCount)
        var k = 0
        for((i, b) in core.layout.buttons.withIndex()) {
            for(j in 0 until 6) {
                qVerts[k+0] = rhomb[j*2+0]
                qVerts[k+1] = rhomb[j*2+1]
                qVerts[k+2] = b.p.x
                qVerts[k+3] = b.p.y
                qVerts[k+4] = b.s
                k += 5
            }
        }
        quads = Mesh.build(qVerts, 2, 3)
        buttons = Draw.build(core, primary,
            """
            attribute vec2 vertex; 
            attribute vec3 uv;
            varying vec2 coord; 
            varying vec3 local;
            uniform vec2 p;
            void main() { 
                float size = uv.z * e0(time*2., 4.);
                float realSize = size + 0.01; // shadow
                coord = uv.xy;
                coord += vertex * realSize;
                local = vec3(vertex.x + vertex.y, vertex.x - vertex.y, 0) * realSize;
                local.z = size;
                
                local *= resolution.y;
                gl_Position = coordToScreen(coord);
            }
            """.trimIndent(),
            """
            varying vec2 coord;
            varying vec3 local;
            uniform float openTime;
            uniform float closeTime;
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
                if(openTime > 0.0) c.a *= mix(0.5, 1.0, exp(-openTime*20.));
                if(closeTime > 0.0) c.a *= mix(1.0, 0.5, exp(-closeTime*5.));
                gl_FragColor = c;
            }
            """.trimIndent(), arrayOf("openTime", "closeTime")
        )
        val quad = floatArrayOf(-1f, -1f, 1f, 1f, 1f, -1f, 1f, 1f, -1f, -1f, -1f, 1f)
        var rodVerts = FloatArray(16 * 2 * 6 * 4)
        k = 0
        for(i in 0 until 16) {
            for(m in 0 until 2) {
                for(j in 0 until 6) {
                    rodVerts[k+0] = quad[j*2+0]
                    rodVerts[k+1] = quad[j*2+1]
                    rodVerts[k+2] = i.toFloat()
                    rodVerts[k+3] = m.toFloat()
                    k += 4
                }
            }
        }
        rods = Mesh.build(rodVerts, 2, 2)
        radial = Draw.build(core, primary,
            """
            attribute vec2 vertex; 
            attribute vec2 uv;
            varying vec4 local;
            varying vec2 shadow;
            uniform vec2 center;
            uniform float openTime;
            uniform float closeTime;
            uniform vec3 radial[16];
            uniform int radialCount;
            void main() { 
                float o = e0(openTime*2., 12.0);
                int i = int(uv.x + 0.5);
                if(i >= radialCount) {
                    gl_Position = vec4(0);
                    return;
                }
                vec3 rr = radial[i];
                float len = max(rr.y * o, 0.01);
                float rad = max(rr.z * e0(openTime*2., 24.0), 0.01);
                bool rod = uv.y < 0.5;
                vec2 size = rod ? vec2(len/2., 0.01) : vec2(rad);
                float shadowOffset = 0.01;
                vec2 realSize = size + shadowOffset; // shadow
                if(rod) realSize = vec2(size.x, size.y + shadowOffset);
                vec2 coord = vec2(0);
                coord += vertex * realSize;
                if(rod) coord.x += len/2. - shadowOffset;
                else coord.x += len + rad;
                local = vec4(coord, len, rad);
                local *= resolution.y;
                coord.x += 0.1 * o;
                float a = rr.x;
                shadow = vec2(0, -1) * resolution.y * 0.005;
                coord = mat2(cos(a), -sin(a), sin(a), cos(a)) * coord;
                shadow = mat2(cos(a), sin(a), -sin(a), cos(a)) * shadow;
                coord += center;
                gl_Position = coordToScreen(coord);
            }
            """.trimIndent(),
            """
            varying vec4 local;
            varying vec2 shadow;
            uniform float openTime;
            uniform float closeTime;
            void main() {
                float w = resolution.x;
                float h = resolution.y;
                float width = h * 0.01;
                float len = local.z;
                float rad = local.w;
                vec2 q = max(vec2(0), abs(local.xy - vec2(len/2. + width, 0)) - vec2(len/2., 0));
                vec2 q2 = local.xy - vec2(len + rad, 0);
                float d = min(length(q), abs(length(q2) - rad + width)) - width;
                
                vec3 shadowColor = vec3(0.2);
                vec3 borderColor = vec3(1);
                float borderAlpha = alpha(d);
                
                q = max(vec2(0), abs(local.xy + shadow - vec2(len/2. + width, 0)) - vec2(len/2., 0));
                q2 = local.xy + shadow - vec2(len + rad, 0);
                d = min(length(q), abs(length(q2) - rad + width)) - width;
                float shadowAlpha = 0.5;
                if(d > - width/2.) shadowAlpha = exp(-max(d, 0.) * 0.3) * 0.2;
                
                vec4 c = vec4(shadowColor, shadowAlpha);
                c = over(vec4(borderColor, borderAlpha), c);
                
                gl_FragColor = c;
            }
            """.trimIndent(), arrayOf("center", "openTime", "closeTime", "radial", "radialCount"))
        annulus = Draw.build(core, primary,
            """
            attribute vec2 vertex; 
            varying vec2 local;
            uniform vec2 center;
            uniform float radius;
            uniform float shadow;
            void main() { 
                float size = radius;
                float realSize = size + shadow * 1.5;
                vec2 coord = vertex * realSize;
                local = coord;
                local *= resolution.y;
                coord += center;
                gl_Position = coordToScreen(coord);
            }
            """.trimIndent(),
            """
            varying vec2 local;
            uniform float radius;
            uniform float width;
            uniform float shadow;
            void main() {
                float h = resolution.y;
                float d = length(local) - (radius - width/2.) * h;
                d = abs(d) - width/2. * h;
                float borderAlpha = alpha(d);
                
                vec2 shadowOffset = - h * vec2(0, 0.005);
                d = length(local + shadowOffset) - (radius - width/2.) * h;
                d = abs(d) - width/2. * h;
                vec3 borderColor = vec3(1);
                vec3 shadowColor = vec3(0.2);
                float shadowAlpha = 0.5;
                if(d > - width/4.) shadowAlpha = exp(-max(d, 0.) * 0.003 / shadow) * 0.4;
                
                vec4 c = vec4(shadowColor, shadowAlpha);
                c = over(vec4(borderColor, borderAlpha), c);
                
                gl_FragColor = c;
            }
            """.trimIndent(), arrayOf("center", "radius", "width", "shadow"))
    }

    fun e0(t: Float, k: Float): Float {
        if(t < 0f) return 0f;
        if(t > 1f) return 1f;
        val x = exp(-(t * k).toDouble()).toFloat();
        val s0 = 1f;
        val s1 = exp(-k.toDouble()).toFloat();
        return (x-s0) / (s1-s0);
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

    private var currentRadial: MonakaLayout.Radial? = null

    override fun onDrawFrame(gl: GL10?) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)

        if(primary) background?.draw(rect)
        buttons?.f1("openTime", core.layout.openTime)
        buttons?.f1("closeTime", core.layout.closeTime)
        buttons?.draw(quads)
        if(core.layout.radial != currentRadial) {
            currentRadial = core.layout.radial
            if (currentRadial != null) {
                val r = currentRadial!!
                val rad = FloatArray(16 * 3)
                val count = r.cands.size
                for ((i, c) in r.cands.withIndex()) {
                    rad[i*3+0] = (i.toFloat() / count + 0.25f) * 2f * Math.PI.toFloat()
                    rad[i*3+1] = c.weight * 0.2f
                    rad[i*3+2] = c.weight * 0.2f
                }
                radial?.fv3("radial", rad)
                radial?.i1("radialCount", count)
            } else {
                radial?.i1("radialCount", 0)
            }
        }
        if(currentRadial != null) {
            radial?.f2("center", core.layout.center.x, core.layout.center.y)
            radial?.f1("openTime", core.layout.openTime)
            radial?.f1("closeTime", core.layout.closeTime)
            radial?.draw(rods)

            annulus?.f2("center", core.layout.center.x, core.layout.center.y)
            annulus?.f1("radius", 0.1f * e0(core.layout.openTime * 2.0f, 12.0f) + 0.01f)
            annulus?.f1("width", 0.03f)
            annulus?.f1("shadow", 0.02f)
            annulus?.draw(rect)

            annulus?.f2("center", core.layout.center.x, core.layout.center.y)
            annulus?.f1("radius", core.layout.maxVelocity + 0.01f)
            annulus?.f1("width", 0.02f)
            annulus?.f1("shadow", 0.01f)
            annulus?.draw(rect)

            annulus?.f2("center", core.layout.center.x, core.layout.center.y)
            annulus?.f1("radius", core.layout.velocity + 0.01f)
            annulus?.f1("width", 0.02f)
            annulus?.f1("shadow", 0.01f)
            annulus?.draw(rect)

            annulus?.f2("center", core.layout.point.x, core.layout.point.y)
            annulus?.f1("radius", 0.02f)
            annulus?.f1("width", 0.02f)
            annulus?.f1("shadow", 0.02f)
            annulus?.draw(rect)
        }
    }
}