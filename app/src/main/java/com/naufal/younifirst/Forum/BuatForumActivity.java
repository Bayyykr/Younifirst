package com.naufal.younifirst.Forum;

import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ReplacementSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.RadioButton;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.imageview.ShapeableImageView;
import com.naufal.younifirst.R;
import com.naufal.younifirst.custom.CustomEditText;

import java.util.ArrayList;
import java.util.List;

public class BuatForumActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 100;

    private ShapeableImageView imgProfile;
    private ImageView iconCamera;
    private CustomEditText etNamaForum, etDeskripsiForum, etTagar;
    private Uri selectedImageUri;
    private PopupWindow popupWindow;
    private RadioButton rbPublik, rbPribadi;

    private final List<String> tagarList = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_forum_buat_forum);

        findViewById(R.id.back_to_mainactivity).setOnClickListener(v -> finish());

        etNamaForum = findViewById(R.id.et_namaforum);
        etDeskripsiForum = findViewById(R.id.et_deskripsiforum);
        etTagar = findViewById(R.id.et_tagar);

        etTagar.getEditText().setFocusable(false);
        etTagar.getEditText().setFocusableInTouchMode(false);
        etTagar.getEditText().setClickable(true);
        etTagar.getEditText().setShowSoftInputOnFocus(false);

        if (etNamaForum != null) etNamaForum.setHint("Nama Forum");
        if (etDeskripsiForum != null) etDeskripsiForum.setHint("Deskripsi Forum");
        if (etTagar != null) etTagar.setHint("Cari Tagar Forum");

        etTagar.setOnCustomClickListener(view -> showTagarPopup());

        imgProfile = findViewById(R.id.img_profile);
        iconCamera = findViewById(R.id.icon_camera);

        imgProfile.setOnClickListener(v -> openGallery());
        iconCamera.setOnClickListener(v -> openGallery());

        rbPublik = findViewById(R.id.rb_publik);
        rbPribadi = findViewById(R.id.rb_pribadi);

        View.OnClickListener radioClickListener = v -> {
            if (v == rbPublik) {
                rbPublik.setChecked(true);
                rbPribadi.setChecked(false);
            } else if (v == rbPribadi) {
                rbPribadi.setChecked(true);
                rbPublik.setChecked(false);
            }
        };

        rbPublik.setOnClickListener(radioClickListener);
        rbPribadi.setOnClickListener(radioClickListener);

    }

    private void openGallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Pilih Foto"), PICK_IMAGE_REQUEST);
    }

    private void showTagarPopup() {
        if (popupWindow != null && popupWindow.isShowing()) {
            popupWindow.dismiss();
            return;
        }

        View popupView = LayoutInflater.from(this).inflate(R.layout.popup_tagar_list, null);

        popupWindow = new PopupWindow(
                popupView,
                etTagar.getWidth(),
                ScrollView.LayoutParams.WRAP_CONTENT,
                true
        );

        popupWindow.setOutsideTouchable(true);
        popupWindow.setFocusable(true);
        popupWindow.showAsDropDown(etTagar, 0, 10);

        TextView[] tagarItems = {
                popupView.findViewById(R.id.item_tagar1),
                popupView.findViewById(R.id.item_tagar2),
                popupView.findViewById(R.id.item_tagar3),
                popupView.findViewById(R.id.item_tagar4),
                popupView.findViewById(R.id.item_tagar5),
                popupView.findViewById(R.id.item_tagar6),
                popupView.findViewById(R.id.item_tagar7),
                popupView.findViewById(R.id.item_tagar8)
        };

        for (TextView item : tagarItems) {
            if (item != null) {
                item.setOnClickListener(v -> {
                    String selectedTagar = item.getText().toString();
                    addTagar(selectedTagar);
                    popupWindow.dismiss();
                });
            }
        }
    }

    private void addTagar(String tagar) {
        if (!tagarList.contains(tagar)) {
            tagarList.add(tagar);
            renderAllTagar();
        }
    }

    private void renderAllTagar() {
        Editable editable = etTagar.getEditText().getText();
        editable.clear();

        for (String t : tagarList) {
            SpannableString spannable = new SpannableString("#" + t + " ");

            DrawableWithIconSpan bgSpan = new DrawableWithIconSpan(
                    this,
                    R.drawable.custom_item_forum_tagar,
                    R.drawable.icon_silang_kecil,
                    30
            );
            spannable.setSpan(bgSpan, 0, spannable.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            ClickableSpan clickableSpan = new ClickableSpan() {
                @Override
                public void onClick(View widget) {
                    tagarList.remove(t);
                    renderAllTagar();
                }
            };
            spannable.setSpan(clickableSpan, 0, spannable.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            editable.append(spannable);
        }

        etTagar.getEditText().setMovementMethod(LinkMovementMethod.getInstance());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            selectedImageUri = data.getData();
            imgProfile.setImageURI(selectedImageUri);
        }
    }

    public static class DrawableWithIconSpan extends ReplacementSpan {

        private final Drawable backgroundDrawable;
        private final Drawable iconDrawable;
        private final int padding;

        public DrawableWithIconSpan(Context context, int backgroundResId, int iconResId, int padding) {
            this.backgroundDrawable = ContextCompat.getDrawable(context, backgroundResId);
            this.iconDrawable = ContextCompat.getDrawable(context, iconResId);
            this.padding = padding;
        }

        @Override
        public int getSize(Paint paint, CharSequence text, int start, int end, Paint.FontMetricsInt fm) {
            int textWidth = (int) paint.measureText(text, start, end);
            int iconWidth = iconDrawable.getIntrinsicWidth();
            return textWidth + iconWidth + 3 * padding;
        }

        @Override
        public void draw(Canvas canvas, CharSequence text, int start, int end,
                         float x, int top, int y, int bottom, Paint paint) {

            int width = (int) paint.measureText(text, start, end);
            int iconWidth = iconDrawable.getIntrinsicWidth();
            int totalWidth = width + iconWidth + 3 * padding;

            backgroundDrawable.setBounds((int) x, top, (int) x + totalWidth, bottom);
            backgroundDrawable.draw(canvas);

            canvas.drawText(text, start, end, x + padding, y, paint);

            int iconLeft = (int) x + padding + width + padding;
            int iconTop = top + (bottom - top - iconDrawable.getIntrinsicHeight()) / 2;
            iconDrawable.setBounds(iconLeft, iconTop, iconLeft + iconWidth, iconTop + iconDrawable.getIntrinsicHeight());
            iconDrawable.draw(canvas);
        }
    }
}
