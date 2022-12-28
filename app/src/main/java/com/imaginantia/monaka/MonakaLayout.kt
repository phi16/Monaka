package com.imaginantia.monaka

import android.graphics.PointF
import android.util.Log
import androidx.core.graphics.minus
import kotlin.math.exp

class MonakaLayout(private val core: MonakaCore) {
    var point: PointF = PointF(0f, 0f)
    var prevPoint: PointF = PointF(0f, 0f)
    var center: PointF = PointF(0f, 0f)
    var radial: Radial? = null
    var openTime: Float = 0f
    var closeTime: Float = 1f

    class Candidate(val code: Code, val weight: Float) // TODO: weight + angle, distance, radius
    class Code(val str: String)
    class Radial(val cands: Array<Candidate>)
    class Button(val p: PointF, val s: Float, val c: Code)

    var dict: Map<String, Radial> = mapOf(
        "A" to Radial(arrayOf(Candidate(Code("E"), 1.0f), Candidate(Code("F"), 0.5f))),
        "B" to Radial(arrayOf(Candidate(Code("E"), 1.0f), Candidate(Code("F"), 0.5f), Candidate(Code("G"), 0.5f))),
        "C" to Radial(arrayOf(Candidate(Code("A"), 1.0f), Candidate(Code("B"), 0.5f))),
        "D" to Radial(arrayOf(Candidate(Code("C"), 1.0f), Candidate(Code("D"), 0.5f))),
        "E" to Radial(arrayOf(Candidate(Code("A"), 1.0f), Candidate(Code("B"), 0.5f))),
    ) // TODO: from DB

    private fun loadRadial(code: Code): Radial? {
        return dict[code.str]
    }

    val buttons: Array<Button> = arrayOf(
        Button(PointF(-0.5f, -0.4f), 0.3f, Code("A")),
        Button(PointF(-0.7f, -0.7f), 0.1f, Code("B")),
        Button(PointF(-0.95f, -0.55f), 0.2f, Code("C")),
        Button(PointF(-0.85f, -0.2f), 0.15f, Code("D"))
    )

    fun open(b: Button) {
        center = b.p
        radial = loadRadial(b.c)
        openTime = 0f
        closeTime = 0f
        point.set(center)
        prevPoint.set(point)
        if(radial == null) {
            core.panic("Empty button: ${b.c.str}")
        }
        Log.d("Monaka", "Open ${b.c.str}")
    }

    fun fadeOut() {
        // TODO: animation
        radial = null
    }

    fun close() {
        fadeOut()
        openTime = 0f
        closeTime = 0.001f // TODO
        Log.d("Monaka", "Close")
    }

    fun frame(dt: Float) {
        if(radial != null) openTime += dt
        else closeTime += dt

        val a = 40.0f
        prevPoint.x += (point.x - prevPoint.x) * (1f - exp(-a*dt))
        prevPoint.y += (point.y - prevPoint.y) * (1f - exp(-a*dt))
    }
}