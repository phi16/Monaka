package com.imaginantia.monaka

import android.inputmethodservice.InputMethodService
import android.opengl.GLSurfaceView
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo

class MonakaInput : InputMethodService() {
    var core: MonakaCore = MonakaCore()

    override fun onInitializeInterface() {
        core.init()
    }

    override fun onCreateInputView(): View? {
        return MonakaView(this, core)
    }

    override fun onStartInput(attribute: EditorInfo?, restarting: Boolean) {
        super.onStartInput(attribute, restarting)
    }

    override fun onFinishInput() {
        super.onFinishInput()
    }

    override fun onBindInput() {
        core.bindInput(currentInputConnection)
    }

    override fun onUnbindInput() {
        core.unbindInput()
    }

    override fun onWindowHidden() {
        core.windowHidden()
    }

    override fun onWindowShown() {
        core.windowShown()
    }
}