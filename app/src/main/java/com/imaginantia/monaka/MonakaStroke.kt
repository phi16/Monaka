package com.imaginantia.monaka

import android.graphics.PointF
import android.util.Log

class MonakaStroke(val core: MonakaCore, val id: Int) {
    fun down(p: PointF) {
        Log.d("Monaka", "down $id $p")
        core.commit("D$id ")
    }

    fun move(p: PointF) {
        Log.d("Monaka", "move $id $p")
        core.commit("M$id ")
    }

    fun up(p: PointF) {
        Log.d("Monaka", "up $id $p")
        core.commit("U$id ")
    }
}