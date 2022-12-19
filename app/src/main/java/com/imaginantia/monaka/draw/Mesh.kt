package com.imaginantia.monaka.draw

import android.opengl.GLES20
import android.util.Log
import java.nio.FloatBuffer

class Mesh(val vbo: Int, val dim: Int, val subDim: Int, val count: Int) {
    companion object {
        fun build(verts: FloatArray, dim: Int, subDim: Int = 0): Mesh {
            val buffs = IntArray(1)
            GLES20.glGenBuffers(1, buffs, 0)
            val vbo = buffs[0]
            var vertBuffer = FloatBuffer.allocate(verts.size)
            vertBuffer.put(verts)
            vertBuffer.flip()
            GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vbo)
            GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, verts.size * 4, vertBuffer, GLES20.GL_STATIC_DRAW )
            return Mesh(vbo, dim, subDim, verts.size / (dim + subDim))
        }
    }

    fun use() {
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vbo)
        val stride = dim + subDim
        GLES20.glVertexAttribPointer(0, dim, GLES20.GL_FLOAT, false, stride * 4, 0)
        GLES20.glEnableVertexAttribArray(0)
        if(subDim > 0) {
            val offset = dim
            GLES20.glVertexAttribPointer(1, subDim, GLES20.GL_FLOAT, false, stride * 4, offset * 4)
            GLES20.glEnableVertexAttribArray(1)
        }
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, count)
    }
}