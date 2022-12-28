package com.imaginantia.monaka.front

import android.graphics.PointF
import android.util.Log
import androidx.core.graphics.plus
import com.imaginantia.monaka.MonakaCore
import com.imaginantia.monaka.MonakaLayout
import kotlin.math.abs

class MonakaStroke(private val core: MonakaCore, private val id: Int) {
    private var main = false
    private var opened = false
    private var pp: PointF = PointF(0f, 0f)
    private var cur: PointF = PointF(0f, 0f)
    private val scale = 4.0f;

    private fun buttonCheck(p: PointF): Boolean {
        var b0: MonakaLayout.Button? = null
        var b1: MonakaLayout.Button? = null
        var d0: Float = 0f
        var d1: Float = 0f
        for(b in core.layout.buttons) {
            val d = abs(p.x - b.p.x) + abs(p.y - b.p.y) - b.s // TODO: SDF
            if(b0 == null || d < d0) {
                b1 = b0
                d1 = d0
                b0 = b
                d0 = d
            } else if(b1 == null || d < d1) {
                b1 = b
                d1 = d
            }
        }

        val eps = 0.1f
        return if(eps < d1 - d0) {
            b0?.let {
                core.layout.open(it)
                cur.set(b0.p)
                core.layout.point.set(cur)
                core.layout.prevPoint.set(cur)
            }
            true
        } else false
    }

    fun down(p: PointF) {
        pp = p
        cur.set(p)
        if(core.retainStroke(this)) {
            main = true
            opened = false
            if(buttonCheck(cur)) {
                opened = true
            }
        }
    }

    fun move(p: PointF) {
        val dx = (p.x - pp.x) * scale
        val dy = (p.y - pp.y) * scale
        cur.x += dx
        cur.y += dy
        pp = p
        if(main) {
            if(!opened) {
                if(buttonCheck(cur)) {
                    opened = true
                }
            } else {
                core.layout.point.set(cur)
            }
        }
    }

    fun up(p: PointF) {
        val dx = (p.x - pp.x) * scale
        val dy = (p.y - pp.y) * scale
        cur.x += dx
        cur.y += dy
        pp = p
        if(main) {
            if(opened) core.layout.close()
            core.releaseStroke()
            main = false
            opened = false
        }
    }
}