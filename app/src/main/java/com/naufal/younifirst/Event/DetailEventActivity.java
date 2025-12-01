package com.naufal.younifirst.Event;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.graphics.drawable.ColorDrawable;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.naufal.younifirst.R;

public class DetailEventActivity extends AppCompatActivity {

    private ImageView imgHeader;
    private ImageButton btnZoom, btnBack, btnFlag;
    private ScrollView scrollView;
    private LinearLayout containerFilter, registerSegment;
    private RelativeLayout headerContainer;
    private boolean isZoomed = false;

    private ViewGroup originalImageParent;
    private int originalImageHeight;
    private int popupOffsetX;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_detail_event);

        popupOffsetX = -(int) (-10 * getResources().getDisplayMetrics().density);

        initViews();
        setupBackButton();
        setupZoomButton();
        setupBackPressedHandler();

        btnFlag.setOnClickListener(v -> showFlagPopup());
    }

    private void initViews() {
        imgHeader = findViewById(R.id.img_header);
        btnZoom = findViewById(R.id.zoom);
        btnBack = findViewById(R.id.back_to_mainactivity);
        btnFlag = findViewById(R.id.flag);
        scrollView = findViewById(R.id.scrollContent);
        containerFilter = findViewById(R.id.container_filter);
        headerContainer = findViewById(R.id.header_container);
        registerSegment = findViewById(R.id.register_segment);

        originalImageParent = (ViewGroup) imgHeader.getParent();
        originalImageHeight = imgHeader.getLayoutParams().height;
    }

    private void setupBackButton() {
        btnBack.setOnClickListener(v -> {
            if (isZoomed) zoomOutImage();
            else finish();
        });
    }

    private void setupZoomButton() {
        btnZoom.setOnClickListener(v -> {
            if (!isZoomed) zoomInImage();
        });
    }

    private void zoomInImage() {
        scrollView.setVisibility(View.GONE);
        containerFilter.setVisibility(View.GONE);
        registerSegment.setVisibility(View.GONE);
        btnZoom.setVisibility(View.GONE);

        ViewGroup rootView = (ViewGroup) findViewById(android.R.id.content);
        if (imgHeader.getParent() != null) {
            ((ViewGroup) imgHeader.getParent()).removeView(imgHeader);
        }

        FrameLayout.LayoutParams fullscreenParams = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        );
        imgHeader.setLayoutParams(fullscreenParams);
        imgHeader.setScaleType(ImageView.ScaleType.FIT_CENTER);

        imgHeader.setScaleX(0.8f);
        imgHeader.setScaleY(0.8f);
        imgHeader.setAlpha(0f);

        rootView.addView(imgHeader);
        btnBack.setVisibility(View.VISIBLE);
        btnFlag.setVisibility(View.VISIBLE);

        imgHeader.animate()
                .scaleX(1f)
                .scaleY(1f)
                .alpha(1f)
                .setDuration(300)
                .start();

        isZoomed = true;
    }

    private void zoomOutImage() {
        imgHeader.animate()
                .scaleX(0.8f)
                .scaleY(0.8f)
                .alpha(0f)
                .setDuration(300)
                .withEndAction(() -> {
                    ViewGroup currentParent = (ViewGroup) imgHeader.getParent();
                    if (currentParent != null) {
                        currentParent.removeView(imgHeader);
                    }

                    RelativeLayout.LayoutParams originalParams = new RelativeLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            originalImageHeight
                    );
                    imgHeader.setLayoutParams(originalParams);
                    imgHeader.setScaleType(ImageView.ScaleType.CENTER_CROP);

                    originalImageParent.addView(imgHeader);

                    imgHeader.setScaleX(1f);
                    imgHeader.setScaleY(1f);
                    imgHeader.setAlpha(1f);

                    scrollView.setVisibility(View.VISIBLE);
                    containerFilter.setVisibility(View.VISIBLE);
                    registerSegment.setVisibility(View.VISIBLE);

                    btnZoom.setVisibility(View.VISIBLE);
                    btnBack.setVisibility(View.VISIBLE);
                    btnFlag.setVisibility(View.VISIBLE);
                    headerContainer.setVisibility(View.VISIBLE);

                    isZoomed = false;
                })
                .start();
    }

    private void setupBackPressedHandler() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (isZoomed) zoomOutImage();
                else finish();
            }
        });
    }
    private void showFlagPopup() {
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        int popupLayout = isZoomed ?
                R.layout.custom_popup_kompetisi_zoom_in :
                R.layout.custom_popup_kompetisi_zoom_out;

        View popupView = inflater.inflate(popupLayout, null);

        final PopupWindow popupWindow = new PopupWindow(
                popupView,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                true
        );

        popupWindow.setBackgroundDrawable(new ColorDrawable());
        popupWindow.setOutsideTouchable(true);
        popupWindow.setTouchable(true);

        int[] location = new int[2];
        btnFlag.getLocationOnScreen(location);

        popupView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        int popupWidth = popupView.getMeasuredWidth();

        popupWindow.showAtLocation(btnFlag, Gravity.NO_GRAVITY,
                location[0] - popupWidth + popupOffsetX,
                location[1]);

        View textEdit = popupView.findViewById(R.id.Edit);
        if (textEdit != null) {
            textEdit.setOnClickListener(v -> {
                popupWindow.dismiss();
                startActivity(new Intent(DetailEventActivity.this, EditEvent.class));
            });
        }

        View textHapus = popupView.findViewById(R.id.Hapus);
        if (textHapus != null) {
            textHapus.setOnClickListener(v -> {
                popupWindow.dismiss();
                showHapusPopup();
            });
        }

        View textBagikan = popupView.findViewById(R.id.Bagikan);
        if (textBagikan != null) {
            textBagikan.setOnClickListener(v -> {
                popupWindow.dismiss();
            });
        }
    }
    private void showHapusPopup() {
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.popup_hapus_event, null);

        final View overlay = new View(this);
        overlay.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));
        overlay.setBackgroundColor(0x88000000);

        ViewGroup root = findViewById(android.R.id.content);
        root.addView(overlay);

        int width = (int) (320 * getResources().getDisplayMetrics().density);
        int height = ViewGroup.LayoutParams.WRAP_CONTENT;

        final PopupWindow popupWindow = new PopupWindow(
                popupView,
                width,
                height,
                true
        );

        popupWindow.setBackgroundDrawable(new ColorDrawable());
        popupWindow.setOutsideTouchable(true);
        popupWindow.setTouchable(true);

        popupWindow.showAtLocation(root, Gravity.CENTER, 0, 0);

        popupView.setScaleX(0f);
        popupView.setScaleY(0f);
        popupView.animate()
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(300)
                .start();

        View btnHapusConfirm = popupView.findViewById(R.id.BtnHapus);
        btnHapusConfirm.setOnClickListener(v -> closePopupWithScale(popupWindow, popupView, overlay));

        View btnBatal = popupView.findViewById(R.id.BtnBatal);
        btnBatal.setOnClickListener(v -> closePopupWithScale(popupWindow, popupView, overlay));

        popupWindow.setOnDismissListener(() -> root.removeView(overlay));
    }

    private void closePopupWithScale(PopupWindow popupWindow, View popupView, View overlay) {
        popupView.animate()
                .scaleX(0f)
                .scaleY(0f)
                .setDuration(200)
                .withEndAction(() -> {
                    popupWindow.dismiss();
                    ((ViewGroup) overlay.getParent()).removeView(overlay);
                })
                .start();
    }
}