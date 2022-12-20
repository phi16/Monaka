package com.imaginantia.monaka

import android.graphics.PointF
import android.util.Log

class MonakaStroke(private val core: MonakaCore, private val id: Int) {
    lateinit var cur: PointF

    fun down(p: PointF) {
        cur = p
    }

    fun move(p: PointF) {
        cur = p
    }

    fun up(p: PointF) {
        cur = p
    }

    fun frame() {
        core.renderer.setPoint(cur)
    }
}