package com.imaginantia.monaka.draw

import android.opengl.GLES20
import android.util.Log
import com.imaginantia.monaka.MonakaCore
import java.nio.IntBuffer
import java.util.Dictionary

class Draw(private val core: MonakaCore, private val primary: Boolean, private val p: Int, params: Array<String>) {
    sealed class Value {
        object N: Value()
        data class I1(val x: Int): Value()
        data class I2(val x: Int, val y: Int): Value()
        data class I3(val x: Int, val y: Int, val z: Int): Value()
        data class I4(val x: Int, val y: Int, val z: Int, val w: Int): Value()
        data class F1(val x: Float): Value()
        data class F2(val x: Float, val y: Float): Value()
        data class F3(val x: Float, val y: Float, val z: Float): Value()
        data class F4(val x: Float, val y: Float, val z: Float, val w: Float): Value()
        data class FV1(val v: FloatArray): Value()
        data class FV2(val v: FloatArray): Value()
        data class FV3(val v: FloatArray): Value()
        data class FV4(val v: FloatArray): Value()
    }

    class Uniform(val location: Int, var value: Value)

    var uniforms: Map<String, Uniform> = mapOf()

    init {
        fun addUniform(name: String) {
            uniforms += name to Uniform(GLES20.glGetUniformLocation(p, name), Value.N)
        }
        for(name in params) {
            addUniform(name)
        }
        addUniform("time")
        addUniform("resolution")
        addUniform("native")
    }

    companion object {
        fun build(core: MonakaCore, primary: Boolean, vs: String, fs: String, params: Array<String>): Draw? {
            val buildShader = { type: Int, src: String ->
                val s = GLES20.glCreateShader(type)
                GLES20.glShaderSource(s,
                    """
                    precision highp float;
                    uniform float time;
                    uniform vec2 resolution;
                    uniform vec2 native;
                    vec4 coordToScreen(inout vec2 coord) {
                        coord *= resolution.y;
                        coord += resolution;
                        coord.y *= native.x;
                        return vec4((coord / resolution * 2. - 1.) * vec2(1, -1) + vec2(0, native.y), 0.0, 1.0);
                    }
                    float saturate(float x) {
                        return clamp(x, 0., 1.);
                    }
                    float map(float a, float b, float x) {
                        return saturate((x-a)/(b-a));
                    }
                    float alpha(float x) {
                        return saturate(-x + 0.5);
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
                    """.trimIndent().replace("\n", "") + src)
                GLES20.glCompileShader(s)
                val buff = IntBuffer.allocate(1)
                GLES20.glGetShaderiv(s, GLES20.GL_COMPILE_STATUS, buff)
                if(buff[0] == GLES20.GL_FALSE) {
                    val err = GLES20.glGetShaderInfoLog(s)
                    Log.e("Monaka", err)
                    null
                } else s
            }
            val buildProgram = { v: Int, f: Int ->
                val p = GLES20.glCreateProgram()
                GLES20.glAttachShader(p, v)
                GLES20.glAttachShader(p, f)
                GLES20.glBindAttribLocation(p, 0, "vertex")
                GLES20.glBindAttribLocation(p, 1, "uv")
                GLES20.glLinkProgram(p)
                val buff = IntBuffer.allocate(1)
                GLES20.glGetProgramiv(p, GLES20.GL_LINK_STATUS, buff)
                if(buff[0] == GLES20.GL_FALSE) {
                    val err = GLES20.glGetProgramInfoLog(p)
                    Log.e("Monaka", err)
                    null
                } else p
            }
            val v = buildShader(GLES20.GL_VERTEX_SHADER, vs) ?: return null
            val f = buildShader(GLES20.GL_FRAGMENT_SHADER, fs) ?: return null
            val p = buildProgram(v, f) ?: return null
            return Draw(core, primary, p, params)
        }
    }

    private fun setUniform(name: String, value: Value) {
        var u = uniforms[name]
        if(u == null) {
            core.panic("Uniform not registered: $name")
        } else {
            u.value = value
        }
    }
    fun i1(name: String, x: Int) { setUniform(name, Value.I1(x)) }
    fun i2(name: String, x: Int, y: Int) { setUniform(name, Value.I2(x, y)) }
    fun i3(name: String, x: Int, y: Int, z: Int) { setUniform(name, Value.I3(x, y, z)) }
    fun i4(name: String, x: Int, y: Int, z: Int, w: Int) { setUniform(name, Value.I4(x, y, z, w)) }
    fun f1(name: String, x: Float) { setUniform(name, Value.F1(x)) }
    fun f2(name: String, x: Float, y: Float) { setUniform(name, Value.F2(x, y)) }
    fun f3(name: String, x: Float, y: Float, z: Float) { setUniform(name, Value.F3(x, y, z)) }
    fun f4(name: String, x: Float, y: Float, z: Float, w: Float) { setUniform(name, Value.F4(x, y, z, w)) }
    fun fv1(name: String, v: FloatArray) { setUniform(name, Value.FV1(v)) }
    fun fv2(name: String, v: FloatArray) { setUniform(name, Value.FV2(v)) }
    fun fv3(name: String, v: FloatArray) { setUniform(name, Value.FV3(v)) }
    fun fv4(name: String, v: FloatArray) { setUniform(name, Value.FV4(v)) }

    fun draw(m: Mesh) {
        GLES20.glUseProgram(p)
        f1("time", core.time)
        f2("resolution", core.resolution.x, core.resolution.y)
        if(primary) f2("native", 1f, 0f)
        else f2("native", core.resolution.y / core.subHeight.toFloat(), -2f)
        for(u in uniforms.values) {
            if(u.location == -1) continue
            when(val v = u.value) {
                is Value.N -> {}
                is Value.I1 -> GLES20.glUniform1i(u.location, v.x)
                is Value.I2 -> GLES20.glUniform2i(u.location, v.x, v.y)
                is Value.I3 -> GLES20.glUniform3i(u.location, v.x, v.y, v.z)
                is Value.I4 -> GLES20.glUniform4i(u.location, v.x, v.y, v.z, v.w)
                is Value.F1 -> GLES20.glUniform1f(u.location, v.x)
                is Value.F2 -> GLES20.glUniform2f(u.location, v.x, v.y)
                is Value.F3 -> GLES20.glUniform3f(u.location, v.x, v.y, v.z)
                is Value.F4 -> GLES20.glUniform4f(u.location, v.x, v.y, v.z, v.w)
                is Value.FV1 -> GLES20.glUniform1fv(u.location, v.v.size / 1, v.v, 0)
                is Value.FV2 -> GLES20.glUniform2fv(u.location, v.v.size / 2, v.v, 0)
                is Value.FV3 -> GLES20.glUniform3fv(u.location, v.v.size / 3, v.v, 0)
                is Value.FV4 -> GLES20.glUniform4fv(u.location, v.v.size / 4, v.v, 0)
            }
        }
        m.use()
    }
}