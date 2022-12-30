package com.imaginantia.monaka.front

import android.graphics.PointF
import android.util.Log
import androidx.core.graphics.minus
import androidx.core.graphics.plus
import com.imaginantia.monaka.MonakaCore
import com.imaginantia.monaka.MonakaLayout
import java.lang.Float.max
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.exp

class MonakaStroke(private val core: MonakaCore, private val id: Int) {
    private var main = false
    private var opened = false
    private var pp: PointF = PointF(0f, 0f)
    private var cur: PointF = PointF(0f, 0f)
    private var prev: PointF = PointF(0f, 0f)
    private var maxVelocity: Float = 0f
    private val scale = 4.0f;

    private fun buttonCheck(p: PointF): Boolean {
        var b0: MonakaLayout.Button? = null
        var b1: MonakaLayout.Button? = null
        var d0 = 0f
        var d1 = 0f
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
                prev.set(cur)
                maxVelocity = 0f
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
            if(opened) {
                // TODO: flick recognition
                core.layout.close()
            }
            core.releaseStroke()
            main = false
            opened = false
        }
    }

    fun frame(dt: Float) {
        core.layout.point.set(cur)
        if(main && opened) {
            val vel = cur - prev
            val velocity = vel.length()
            maxVelocity = max(maxVelocity, velocity)
            core.layout.velocity = velocity
            core.layout.maxVelocity = maxVelocity

            val c = cur - core.layout.center
            val d = c.length()
            if(d > 0.2f && maxVelocity > 0.001f && velocity < maxVelocity * 0.75f && velocity < 0.1f) {
                val cands = core.layout.radial!!.cands
                val count = cands.size
                var select: Int = -1
                var selectDir = PointF(0f, 0f)
                var selectDot = 0f
                for(i in 0 until count) {
                    val a = (i.toFloat() / count - 0.25f) * 2f * 3.1415926535f
                    val dir = PointF(cos(a), sin(a))
                    val dot = c.x * dir.x + c.y * dir.y
                    if(select == -1 || selectDot < dot) {
                        select = i
                        selectDir = dir
                        selectDot = dot
                    }
                }
                val cand = cands[select]
                val c = cand.code
                val dx = selectDir.x * cand.weight * 0.2f
                val dy = selectDir.y * cand.weight * 0.2f
                val center = core.layout.center + PointF(dx, dy)
                cur.set(center)
                prev.set(cur)
                core.commit(c.str)
                core.layout.point.set(cur)
                core.layout.open(c)
                maxVelocity = 0f
            }
            if(d <= 0.2f) {
                maxVelocity = 0f
            }

            val a = 40.0f
            prev.x += vel.x * (1f - exp(-a*dt))
            prev.y += vel.y * (1f - exp(-a*dt))
            core.layout.prevPoint.set(prev)
        }
    }
}