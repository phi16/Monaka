package com.imaginantia.monaka.front

import android.inputmethodservice.InputMethodService
import android.view.View
import android.view.inputmethod.EditorInfo
import com.imaginantia.monaka.MonakaCore

class MonakaService : InputMethodService() {
    private var core: MonakaCore = MonakaCore()

    override fun onCreate() {
        super.onCreate()
        core.init(this)
    }

    override fun onCreateInputView(): View? {
        core.mainView = MonakaView(this, core, true)
        return core.mainView
    }

    override fun onCreateCandidatesView(): View? {
        core.subView = MonakaView(this, core, false)
        core.subHide()
        return core.subView
    }

    override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {
        info?.also { core.applyInputType(it.inputType) }
    }

    override fun onWindowHidden() {
        core.dismiss()
    }

    override fun onWindowShown() {
        core.present()
    }
}