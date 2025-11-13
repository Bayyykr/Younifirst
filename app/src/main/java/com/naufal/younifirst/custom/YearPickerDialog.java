package com.naufal.younifirst.custom;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.naufal.younifirst.R;
import java.util.ArrayList;
import java.util.List;

public class YearPickerDialog extends Dialog {
    private OnYearSelectedListener listener;
    private int startYear = 2020;
    private int endYear = 2029;
    private YearAdapter adapter;
    private int selectedYear = -1;
    private CustomEditText targetEditText; // TAMBAHKAN INI

    public interface OnYearSelectedListener {
        void onYearSelected(int year);
    }

    // TAMBAHKAN CONSTRUCTOR BARU
    public YearPickerDialog(@NonNull Context context, CustomEditText targetEditText, OnYearSelectedListener listener) {
        super(context);
        this.targetEditText = targetEditText;
        this.listener = listener;
    }

    // CONSTRUCTOR LAMA (untuk kompatibilitas)
    public YearPickerDialog(@NonNull Context context, OnYearSelectedListener listener) {
        super(context);
        this.listener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_year_picker);

        getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        setupViews();
    }

    private void setupViews() {
        RecyclerView recyclerViewYears = findViewById(R.id.recyclerViewYears);
        Button btnBatal = findViewById(R.id.btnBatal);
        Button btnOke = findViewById(R.id.btnOke);

        // Create years list
        List<Integer> years = new ArrayList<>();
        for (int year = startYear; year <= endYear; year++) {
            years.add(year);
        }

        // Setup RecyclerView dengan 3 kolom
        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 3);
        recyclerViewYears.setLayoutManager(layoutManager);

        // TAMBAHKAN ITEM DECORATION UNTUK JARAK
        int spacingInPixels = getContext().getResources().getDimensionPixelSize(R.dimen.year_item_spacing);
        recyclerViewYears.addItemDecoration(new GridSpacingItemDecoration(3, spacingInPixels, true));

        adapter = new YearAdapter(years, new YearAdapter.OnYearClickListener() {
            @Override
            public void onYearClick(int year, int position) {
                selectedYear = year;
            }
        });
        recyclerViewYears.setAdapter(adapter);

        // Button BATAL - PERBAIKAN: Panggil forceHintDown()
        btnBatal.setOnClickListener(v -> {
            // JIKA ADA targetEditText, reset hint-nya
            if (targetEditText != null && targetEditText.getText().isEmpty()) {
                targetEditText.forceHintDown();
            }
            dismiss();
        });

        // Button OKE
        btnOke.setOnClickListener(v -> {
            if (selectedYear != -1 && listener != null) {
                listener.onYearSelected(selectedYear);
            }
            dismiss();
        });
    }
}