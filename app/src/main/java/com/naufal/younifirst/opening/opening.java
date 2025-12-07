package com.naufal.younifirst.opening;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.naufal.younifirst.Home.MainActivity;
import com.naufal.younifirst.LognReg.login;
import com.naufal.younifirst.R;
import com.naufal.younifirst.api.ApiHelper;

public class opening extends AppCompatActivity {

    private Button btnMulai, btnNext, btnLewati, btnLewati2, btnLewati3;
    private ImageButton firstNext, nextSecond, nextThird;
    private int currentLayout = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        showLayoutOne();
        checkAutoLogin();
    }

    private void checkAutoLogin() {
        if (ApiHelper.isLoggedIn()) {
            Log.d("opening", "🔍 Already logged in, redirecting to MainActivity...");
            Toast.makeText(this, "Auto-login...", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
    }

    private void showLayoutOne() {
        setContentView(R.layout.opening_app_one);
        currentLayout = 1;

        btnMulai = findViewById(R.id.btn_mulai);
        if (btnMulai != null) {
            btnMulai.setOnClickListener(v -> showLayoutTwo());
        }
    }

    private void showLayoutTwo() {
        setContentView(R.layout.opening_app_two);
        currentLayout = 2;

        firstNext = findViewById(R.id.first_next);
        btnLewati = findViewById(R.id.btn_lewati);

        if (firstNext != null) {
            firstNext.setOnClickListener(v -> showLayoutThree());
        }

        if (btnLewati != null) {
            btnLewati.setOnClickListener(v -> goToLogin());
        }
    }

    private void showLayoutThree() {
        setContentView(R.layout.opening_app_three);
        currentLayout = 3;

        nextSecond = findViewById(R.id.next_second);
        btnLewati2 = findViewById(R.id.btn_lewati_second);

        if (nextSecond != null) {
            nextSecond.setOnClickListener(v -> showLayoutFour());
        }

        if (btnLewati2 != null) {
            btnLewati2.setOnClickListener(v -> goToLogin());
        }
    }

    private void showLayoutFour() {
        setContentView(R.layout.opening_app_four);
        currentLayout = 4;

        nextThird = findViewById(R.id.next_third);
        btnLewati3 = findViewById(R.id.btn_lewati_third);

        if (nextThird != null) {
            nextThird.setOnClickListener(v -> goToLogin());
        }

        if (btnLewati3 != null) {
            btnLewati3.setOnClickListener(v -> goToLogin());
        }
    }

    private void goToLogin() {
        Intent intent = new Intent(opening.this, login.class);
        startActivity(intent);
        finish();
    }
}
