package com.imaginantia.monaka.draw

import android.opengl.GLES20
import android.util.Log
import com.imaginantia.monaka.MonakaCore
import java.nio.IntBuffer
import java.util.Dictionary

class Draw(private val core: MonakaCore, private val p: Int, params: Array<String>) {
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
    }

    companion object {
        fun build(core: MonakaCore, vs: String, fs: String, params: Array<String>): Draw? {
            val buildShader = { type: Int, src: String ->
                val s = GLES20.glCreateShader(type)
                GLES20.glShaderSource(s, "precision highp float; uniform float time; uniform vec2 resolution;\n$src")
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
            return Draw(core, p, params)
        }
    }

    fun i1(name: String, x: Int) { uniforms[name]?.value = Value.I1(x) }
    fun i2(name: String, x: Int, y: Int) { uniforms[name]?.value = Value.I2(x, y) }
    fun i3(name: String, x: Int, y: Int, z: Int) { uniforms[name]?.value = Value.I3(x, y, z) }
    fun i4(name: String, x: Int, y: Int, z: Int, w: Int) { uniforms[name]?.value = Value.I4(x, y, z, w) }
    fun f1(name: String, x: Float) { uniforms[name]?.value = Value.F1(x) }
    fun f2(name: String, x: Float, y: Float) { uniforms[name]?.value = Value.F2(x, y) }
    fun f3(name: String, x: Float, y: Float, z: Float) { uniforms[name]?.value = Value.F3(x, y, z) }
    fun f4(name: String, x: Float, y: Float, z: Float, w: Float) { uniforms[name]?.value = Value.F4(x, y, z, w) }

    fun draw(m: Mesh) {
        GLES20.glUseProgram(p)
        GLES20.glBindAttribLocation(p, 0, "vertex")
        GLES20.glBindAttribLocation(p, 1, "uv")
        f1("time", core.time)
        f2("resolution", core.resolution.x, core.resolution.y)
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
            }
        }
        m.use()
    }
}