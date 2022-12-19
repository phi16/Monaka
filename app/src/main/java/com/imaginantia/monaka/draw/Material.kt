package com.imaginantia.monaka.draw

import android.opengl.GLES20
import android.util.Log
import java.nio.IntBuffer

class Material(val p: Int) {
    companion object {
        fun build(vs: String, fs: String): Material? {
            val buildShader = { type: Int, src: String ->
                val s = GLES20.glCreateShader(type)
                GLES20.glShaderSource(s, "precision mediump float; $src")
                GLES20.glCompileShader(s)
                val buff = IntBuffer.allocate(1)
                GLES20.glGetShaderiv(s, GLES20.GL_COMPILE_STATUS, buff)
                if(buff[0] == GLES20.GL_FALSE) {
                    val err = GLES20.glGetShaderInfoLog(s)
                    Log.e("Monaka", err)
                    -1
                } else {
                    s
                }
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
                    -1
                } else {
                    p
                }
            }
            val v = buildShader(GLES20.GL_VERTEX_SHADER, vs)
            val f = buildShader(GLES20.GL_FRAGMENT_SHADER, fs)
            if(v == -1 || f == -1) {
                return null
            }
            val p = buildProgram(v, f)
            if(p == -1) {
                return null
            }
            return Material(p)
        }
    }

    fun draw(m: Mesh) {
        GLES20.glUseProgram(p)
        GLES20.glBindAttribLocation(p, 0, "vertex")
        GLES20.glBindAttribLocation(p, 1, "uv")
        m.use()
    }
}