package com.imaginantia.monaka.front

import android.graphics.Point
import android.graphics.PointF
import android.util.Log
import com.imaginantia.monaka.MonakaCore

class MonakaStroke(private val core: MonakaCore, private val id: Int) {
    var main = false
    lateinit var cur: PointF

    fun down(p: PointF) {
        cur = p
        if(core.retainStroke(this)) {
            main = true
            core.layout.p = cur
            Log.d("Monaka", "Down $cur")
            for(i in 0 until core.layout.buttons.size / 3) {
                val bp = PointF(core.layout.buttons[i*3+0], core.layout.buttons[i*3+1])
                val bs = core.layout.buttons[i*3+2]
                val d = Math.abs(p.x - bp.x) + Math.abs(p.y - bp.y) - bs
                if(d < 0f) {
                    core.commit("$i")
                }
            }
        }
    }

    fun move(p: PointF) {
        cur = p
        if(main) {
            core.layout.p = cur
        }
    }

    fun up(p: PointF) {
        cur = p
        if(main) {
            core.layout.p = PointF(0f,0f)
            core.releaseStroke()
        }
    }
}