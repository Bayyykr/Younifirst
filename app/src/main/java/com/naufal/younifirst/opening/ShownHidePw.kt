package com.naufal.younifirst.opening

import android.text.InputType
import android.view.MotionEvent
import android.widget.EditText
import androidx.core.content.res.ResourcesCompat
import com.naufal.younifirst.R

fun EditText.setupPasswordToggle(openEyeDrawable: Int, closeEyeDrawable: Int) {
    val customFont = ResourcesCompat.getFont(this.context, R.font.is_r)
    this.typeface = customFont

    var isPasswordVisible = false

    this.setOnTouchListener { _, event ->
        if (event.action == MotionEvent.ACTION_UP) {
            val drawableEnd = 2
            if (this.compoundDrawables[drawableEnd] != null) {
                if (event.rawX >= (this.right - this.compoundDrawables[drawableEnd].bounds.width() - this.paddingEnd)) {
                    isPasswordVisible = !isPasswordVisible
                    if (isPasswordVisible) {
                        this.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                        this.setCompoundDrawablesWithIntrinsicBounds(0, 0, openEyeDrawable, 0)
                    } else {
                        this.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                        this.setCompoundDrawablesWithIntrinsicBounds(0, 0, closeEyeDrawable, 0)
                    }
                    this.setSelection(this.text.length)
                    this.typeface  = customFont
                    return@setOnTouchListener true
                }
            }
        }
        false
    }
}
