package com.robertas.storyapp.views.components

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import com.robertas.storyapp.R

class CustomPasswordEditText: CustomEditText {

    private var _isInputValid : Boolean ?= false

    val isInputValid get() = _isInputValid

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init()
    }

    private fun init(){
        addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                s?.let {
                    if (it.length > 5) {

                        hideErrorMessage()

                        _isInputValid = true

                        displayCheckListIcon()

                    } else {

                        _isInputValid = false

                        showErrorMessage()
                    }
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun hideErrorMessage(){
        this@CustomPasswordEditText.error = null
    }

    fun showErrorMessage(){
        hideIconDrawable()

        this@CustomPasswordEditText.error = context.getString(R.string.error_password)
    }
}