package com.imaginantia.monaka

import android.util.Log
import android.view.inputmethod.InputConnection

class MonakaCore {
    var ic: InputConnection? = null

    fun init() {
        Log.d("Monaka","Init")
    }

    fun surfaceCreated() {
        Log.d("Monaka", "Surface Created")
    }

    fun surfaceChanged(width: Int, height: Int) {
        Log.d("Monaka", "SurfaceChanged $width $height")
    }

    fun bindInput(ic: InputConnection) {
        Log.d("Monaka","BindInput")
        this.ic = ic
    }

    fun unbindInput() {
        Log.d("Monaka","UnbindInput")
        ic = null
    }

    fun windowHidden() {
        Log.d("Monaka","WindowHidden")
    }

    fun windowShown() {
        Log.d("Monaka","WindowShown")
    }

    fun touched() {
        ic?.commitText("mochi", 1)
    }
}