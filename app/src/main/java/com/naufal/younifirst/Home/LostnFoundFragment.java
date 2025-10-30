package com.naufal.younifirst.Home;

import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.naufal.younifirst.R;

public class LostnFoundFragment extends Fragment {

    private LinearLayout includeSingkat, includeLengkap;
    private TextView btnToggleSingkat, btnToggleSelengkapnya;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_lostn_found, container, false);

        includeSingkat = view.findViewById(R.id.include_singkat);
        includeLengkap = view.findViewById(R.id.include_lengkap);

        btnToggleSelengkapnya = includeSingkat.findViewById(R.id.tv_lebih_banyak);
        btnToggleSingkat = includeLengkap.findViewById(R.id.btn_toggle_singkat);

        String text = "Ada yang merasa kehilangan earphone? Bisa segera hubungi 0812345678901...";

        SpannableString spannable = new SpannableString(text + " Selengkapnya");

        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                includeLengkap.setVisibility(View.VISIBLE);
                includeSingkat.setVisibility(View.GONE);
            }

            @Override
            public void updateDrawState(@NonNull TextPaint ds) {
                ds.setColor(Color.parseColor("#5E8BFF"));
                ds.setUnderlineText(false);
                ds.setFakeBoldText(true);
            }
        };

        spannable.setSpan(clickableSpan, text.length(), spannable.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        btnToggleSelengkapnya.setText(spannable);
        btnToggleSelengkapnya.setMovementMethod(LinkMovementMethod.getInstance());

        btnToggleSingkat.setOnClickListener(v -> {
            includeSingkat.setVisibility(View.VISIBLE);
            includeLengkap.setVisibility(View.GONE);
        });

        includeSingkat.setVisibility(View.VISIBLE);
        includeLengkap.setVisibility(View.GONE);

        return view;
    }

}
