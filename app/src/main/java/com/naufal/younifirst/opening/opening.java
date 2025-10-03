package com.naufal.younifirst.opening;

import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.naufal.younifirst.R;

public class opening extends AppCompatActivity {

    private Button btn_mulai;
    private Button btn_lewati;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
       showLayoutOne();
    }

    private  void showLayoutOne () {
        setContentView(R.layout.opening_app_one);
        btn_mulai = findViewById(R.id.btn_mulai);
        btn_mulai.setOnClickListener(v -> showLayoutTwo());
    }

    private  void showLayoutTwo () {
        setContentView(R.layout.opening_app_two);
        btn_lewati = findViewById(R.id.btn_lewati);
        btn_lewati.setOnClickListener(v -> showLayoutOne());
    }

}
