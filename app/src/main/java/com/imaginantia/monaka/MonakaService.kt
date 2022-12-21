package com.imaginantia.monaka

import android.inputmethodservice.InputMethodService
import android.view.View
import android.view.inputmethod.EditorInfo

class MonakaService : InputMethodService() {
    private var core: MonakaCore = MonakaCore()

    override fun onCreate() {
        super.onCreate()
        core.init()
    }

    override fun onCreateInputView(): View? {
        core.inputView = MonakaView(this, core)
        return core.inputView
    }

    /* override fun onCreateCandidatesView(): View? {
        core.candidatesView = MonakaView(this, core)
        core.candidatesView.visibility = View.GONE
        return core.candidatesView
    } */

    override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {
        info?.also { core.applyInputType(it.inputType) }
    }

    override fun onWindowHidden() {
        core.dismiss()
    }

    override fun onWindowShown() {
        currentInputConnection?.let {
            core.present(it)
        }
    }
}