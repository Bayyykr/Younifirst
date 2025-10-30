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

public class CustomEditText extends FrameLayout {
    private EditText editText;
    private TextView hintTextView;
    private boolean isHintUp = false;
    private static final int DEFAULT_WIDTH = 0;
    private static final int DEFAULT_HEIGHT = 56;

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
        // Setup default size
        setLayoutParams(new LayoutParams(dpToPx(DEFAULT_WIDTH), dpToPx(DEFAULT_HEIGHT)));

        // Setup background dengan border rounded
        GradientDrawable background = new GradientDrawable();
        background.setShape(GradientDrawable.RECTANGLE);
        background.setCornerRadius(dpToPx(8));
        background.setStroke(dpToPx(1), Color.parseColor("#2A3256"));
        background.setColor(Color.parseColor("#0A1124"));
        setBackground(background);

        // Setup padding
        setPadding(dpToPx(10), dpToPx(10), dpToPx(10), dpToPx(10));

        // Create hint text view
        hintTextView = new TextView(getContext());
        hintTextView.setTextColor(Color.parseColor("#666C8E"));
        hintTextView.setTextSize(16);
        hintTextView.setGravity(Gravity.CENTER_VERTICAL);

        LayoutParams hintParams = new LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT
        );
        hintParams.gravity = Gravity.CENTER_VERTICAL;
        hintTextView.setLayoutParams(hintParams);
        addView(hintTextView);

        // Create edit text
        editText = new EditText(getContext());
        editText.setBackground(null);
        editText.setTextColor(Color.WHITE);
        editText.setTextSize(16);
        editText.setGravity(Gravity.CENTER_VERTICAL);
        editText.setPadding(15, 15, 0, 0);

        LayoutParams editTextParams = new LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT
        );
        editTextParams.topMargin = dpToPx(8); // Space for hint
        editText.setLayoutParams(editTextParams);
        addView(editText);

        // Setup text watcher untuk animasi hint
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 0 && !isHintUp) {
                    animateHintUp();
                } else if (s.length() == 0 && isHintUp && !editText.hasFocus()) {
                    animateHintDown();
                }
            }
        });

        // Focus listener
        editText.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                if (editText.getText().length() == 0 && !isHintUp) {
                    animateHintUp();
                }
            } else {
                if (editText.getText().length() == 0 && isHintUp) {
                    animateHintDown();
                }
            }
        });

        // Click listener untuk hint
        hintTextView.setOnClickListener(v -> {
            if (!isHintUp) {
                animateHintUp();
            }
            editText.requestFocus();
            showKeyboard();
        });

        // Baca atribut dari XML jika ada
        if (attrs != null) {
            TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.CustomEditText);
            String hint = a.getString(R.styleable.CustomEditText_android_hint);
            if (hint != null) {
                setHint(hint);
            }
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
        hintTextView.setTextColor(Color.parseColor("#666C8E"));
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
            android.view.inputmethod.InputMethodManager imm = (android.view.inputmethod.InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(editText, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT);
        }, 100);
    }

    // Public methods
    public void setHint(String hint) {
        hintTextView.setText(hint);
    }

    public String getText() {
        return editText.getText().toString();
    }

    public void setText(String text) {
        editText.setText(text);
        if (text.length() > 0 && !isHintUp) {
            animateHintUp();
        }
    }

    public EditText getEditText() {
        return editText;
    }

    public void setError(String error) {
        editText.setError(error);
    }

    public void setCustomSize(int widthDp, int heightDp) {
        LayoutParams params = (LayoutParams) getLayoutParams();
        if (params != null) {
            params.width = dpToPx(widthDp);
            params.height = dpToPx(heightDp);
            setLayoutParams(params);
        }
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }
}