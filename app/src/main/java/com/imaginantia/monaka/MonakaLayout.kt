package com.imaginantia.monaka

import android.graphics.PointF

class MonakaLayout {
    var p: PointF = PointF(0f, 0f)

    class Button(val p: PointF, val s: Float, val tag: String)
    val buttons: Array<Button> = arrayOf(
        Button(PointF(-0.5f, -0.4f), 0.3f, "A"),
        Button(PointF(-0.7f, -0.7f), 0.1f, "B"),
        Button(PointF(-0.95f, -0.55f), 0.2f, "C"),
        Button(PointF(0f, 0f), 0.1f, "D")
    )
}