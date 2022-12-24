package com.imaginantia.monaka.front

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
            for(b in core.layout.buttons) {
                val d = Math.abs(p.x - b.p.x) + Math.abs(p.y - b.p.y) - b.s
                if(d < 0f) {
                    core.commit("${b.tag}")
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