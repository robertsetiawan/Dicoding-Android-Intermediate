package com.robertas.storyapp.views.components

import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.content.ContextCompat
import com.robertas.storyapp.R

open class CustomEditText: AppCompatEditText {

    private val checkListIcon = ContextCompat.getDrawable(context, R.drawable.ic_baseline_check_24) as Drawable

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        this.background = ContextCompat.getDrawable(context, R.drawable.edit_text_bg) as Drawable

        this.setPaddingRelative(28,0,28,0)
    }

    fun displayCheckListIcon() {
        hideIconDrawable()

        setIconDrawable(null, null, checkListIcon, null)
    }

    private fun setIconDrawable(
        start: Drawable ?= null,
        top: Drawable ?= null,
        end: Drawable ?= null,
        bottom: Drawable ?= null
    ) {
        setCompoundDrawablesWithIntrinsicBounds(start, top, end, bottom)
    }

    fun hideIconDrawable() {
        setIconDrawable()
    }
}