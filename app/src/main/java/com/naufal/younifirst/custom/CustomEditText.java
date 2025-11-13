package com.naufal.younifirst.custom;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.naufal.younifirst.R;

import java.util.WeakHashMap;

public class CustomEditText extends FrameLayout {
    private EditText editText;
    private TextView hintTextView;
    private boolean isHintUp = false;
    private GradientDrawable backgroundDrawable;

    private static final WeakHashMap<CustomEditText, Boolean> instances = new WeakHashMap<>();

    private static final String COLOR_BORDER_DEFAULT = "#2A3256";
    private static final String COLOR_BORDER_FOCUS = "#5E8BFF";
    private static final String COLOR_FILL = "#0A1124";

    private static final int DEFAULT_WIDTH = 0;
    private static final int DEFAULT_HEIGHT = 56;

    public interface OnCustomEditTextClickListener {
        void onClick(CustomEditText view);
    }
    private OnCustomEditTextClickListener onCustomClickListener;
    public void setOnCustomClickListener(OnCustomEditTextClickListener listener) {
        this.onCustomClickListener = listener;
    }

    public CustomEditText(Context context) {
        super(context);
        init(null);
    }

    public CustomEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public CustomEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        instances.put(this, true);

        setLayoutParams(new LayoutParams(dpToPx(DEFAULT_WIDTH), dpToPx(DEFAULT_HEIGHT)));

        backgroundDrawable = new GradientDrawable();
        backgroundDrawable.setShape(GradientDrawable.RECTANGLE);
        backgroundDrawable.setCornerRadius(dpToPx(8));
        backgroundDrawable.setStroke(dpToPx(2), Color.parseColor(COLOR_BORDER_DEFAULT));
        backgroundDrawable.setColor(Color.parseColor(COLOR_FILL));
        setBackground(backgroundDrawable);


        hintTextView = new TextView(getContext());
        hintTextView.setTextColor(Color.parseColor("#666C8E"));
        hintTextView.setTextSize(16);

        editText = new EditText(getContext());
        editText.setBackground(null);
        editText.setTextColor(Color.WHITE);
        editText.setTextSize(16);

        LayoutParams hintParams;
        LayoutParams editTextParams = new LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT
        );

        if (getId() == R.id.et_deskripsiforum || getId() == R.id.etdeskripsimasalahteknis) {
            editText.setGravity(Gravity.TOP | Gravity.START);
            editText.setPadding(dpToPx(10), dpToPx(20), dpToPx(10), dpToPx(10));
            editText.setSingleLine(false);
            editText.setLines(5);

            hintParams = new LayoutParams(
                    LayoutParams.WRAP_CONTENT,
                    LayoutParams.WRAP_CONTENT
            );
            hintParams.gravity = Gravity.TOP | Gravity.START;
            hintParams.topMargin = dpToPx(15);
            hintParams.leftMargin = dpToPx(10);
            hintTextView.setLayoutParams(hintParams);

        } else {
            editText.setGravity(Gravity.CENTER_VERTICAL | Gravity.START);
            editText.setSingleLine(true);
            editText.setPadding(dpToPx(10), dpToPx(10), dpToPx(10), dpToPx(0));

            hintParams = new LayoutParams(
                    LayoutParams.WRAP_CONTENT,
                    LayoutParams.WRAP_CONTENT
            );
            hintParams.gravity = Gravity.CENTER_VERTICAL | Gravity.START;
            hintParams.leftMargin = dpToPx(10);
            hintTextView.setLayoutParams(hintParams);
        }

        addView(hintTextView);
        editText.setLayoutParams(editTextParams);
        addView(editText);

        editText.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 0 && !isHintUp) animateHintUp();
                else if (s.length() == 0 && isHintUp) animateHintDown();
            }
        });

        editText.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                for (CustomEditText other : instances.keySet()) {
                    if (other != this && !other.getEditText().hasFocus()) {
                        if (other.getText().isEmpty()) {
                            other.animateHintDown();
                            other.backgroundDrawable.setStroke(dpToPx(2), Color.parseColor(COLOR_BORDER_DEFAULT));
                        }
                    }
                }
                if (editText.getText().length() == 0 && !isHintUp) animateHintUp();
                backgroundDrawable.setStroke(dpToPx(2), Color.parseColor(COLOR_BORDER_FOCUS));
            } else {
                if (editText.getText().length() == 0 && isHintUp) animateHintDown();
                backgroundDrawable.setStroke(dpToPx(2), Color.parseColor(COLOR_BORDER_DEFAULT));
            }
        });

        hintTextView.setOnClickListener(v -> {
            if (!isHintUp) animateHintUp();
            editText.requestFocus();
            showKeyboard();
        });

        if (attrs != null) {
            TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.CustomEditText);
            String hint = a.getString(R.styleable.CustomEditText_android_hint);
            if (hint != null) setHint(hint);
            a.recycle();
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            if (!isHintUp && editText.getText().length() == 0) {
                animateHintUp();
            }
            editText.requestFocus();
            showKeyboard();

            if (onCustomClickListener != null) {
                onCustomClickListener.onClick(this);
            }
        }
        return super.onInterceptTouchEvent(ev);
    }

    private void animateHintUp() {
        isHintUp = true;
        hintTextView.animate()
                .scaleX(0.8f)
                .scaleY(0.8f)
                .translationY(-dpToPx(12))
                .setDuration(200)
                .start();
        hintTextView.setTextColor(Color.parseColor("#5E8BFF"));
    }

    private void animateHintDown() {
        isHintUp = false;
        hintTextView.animate()
                .scaleX(1f)
                .scaleY(1f)
                .translationY(0)
                .setDuration(200)
                .start();
        hintTextView.setTextColor(Color.parseColor("#666C8E"));
    }

    private void showKeyboard() {
        editText.postDelayed(() -> {
            editText.requestFocus();
            android.view.inputmethod.InputMethodManager imm =
                    (android.view.inputmethod.InputMethodManager) getContext()
                            .getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(editText, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT);
        }, 100);
    }

    public void forceHintDown() {
        if (editText.getText().length() == 0 && isHintUp) animateHintDown();
    }

    public void clearAndReset() {
        editText.setText("");
        if (isHintUp) animateHintDown();
        editText.clearFocus();
    }

    public void setHint(String hint) { hintTextView.setText(hint); }
    public String getText() { return editText.getText().toString(); }
    public void setText(String text) {
        editText.setText(text);
        if (text.length() > 0 && !isHintUp) animateHintUp();
        else if (text.length() == 0 && isHintUp) animateHintDown();
    }
    public EditText getEditText() { return editText; }
    public void setError(String error) { editText.setError(error); }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }
}
