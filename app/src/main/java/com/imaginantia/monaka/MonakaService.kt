package com.imaginantia.monaka

import android.inputmethodservice.InputMethodService
import android.view.View
import android.view.inputmethod.EditorInfo

class MonakaService : InputMethodService() {
    var core: MonakaCore = MonakaCore()

    override fun onCreate() {
        super.onCreate()
        core.init()
    }

    override fun onCreateInputView(): View? {
        return MonakaView(this, core)
    }

    override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {
        info?.also { core.applyInputType(it.inputType) }
    }

    override fun onWindowHidden() {
        core.dismiss()
    }

    override fun onWindowShown() {
        core.present(currentInputConnection)
    }
}