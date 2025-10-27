package com.naufal.younifirst.LognReg;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.naufal.younifirst.R;
import com.naufal.younifirst.opening.ShownHidePwKt;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class login extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnMulai;
    private TextView tvLupaSandi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.page_login);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.pwlogin);
        btnMulai = findViewById(R.id.btn_mulai);
        tvLupaSandi = findViewById(R.id.lupa_sandi);

        ShownHidePwKt.setupPasswordToggle(etPassword, R.drawable.open_eye, R.drawable.close_eye);

        btnMulai.setOnClickListener(v -> handleLogin());

        tvLupaSandi.setOnClickListener(v -> openForgotPassword());
    }

    private void handleLogin() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Isi semua data", Toast.LENGTH_SHORT).show();
            return;
        }

        String url = "http://192.168.1.18:8000/api_login.php";

        new Thread(() -> {
            try {
                URL link = new URL(url);
                HttpURLConnection conn = (HttpURLConnection) link.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

                String data = "email=" + email + "&password=" + password;

                OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream());
                writer.write(data);
                writer.flush();

                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(conn.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }

                reader.close();

                Log.e("ServerResponse", sb.toString());
                JSONObject response = new JSONObject(sb.toString());
                boolean success = response.getBoolean("success");

                runOnUiThread(() -> {
                    if (success) {
                        // Simpan status login ke SharedPreferences
                        SharedPreferences prefs = getSharedPreferences("UserSession", MODE_PRIVATE);
                        prefs.edit()
                                .putBoolean("isLoggedIn", true)
                                .putString("nama", response.optString("nama"))
                                .apply();

                        Toast.makeText(this, "Login berhasil", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(login.this, com.naufal.younifirst.Home.MainActivity.class));
                        finish();
                    } else {
                        Toast.makeText(this, response.optString("message"), Toast.LENGTH_SHORT).show();
                    }
                });

            }  catch (Exception e) {
            e.printStackTrace();
            runOnUiThread(() -> Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show());
        }
        }).start();
    }


    private void openForgotPassword() {
        android.widget.Toast.makeText(this, "Fitur lupa sandi belum tersedia", android.widget.Toast.LENGTH_SHORT).show();
    }

    private void showLoginFailedDialog() {
        android.app.Dialog dialog = new android.app.Dialog(this);
        dialog.setContentView(R.layout.popup_gagal);
        dialog.getWindow().setLayout(
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT
        );
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        Button btnTryAgain = dialog.findViewById(R.id.Btryagain);
        btnTryAgain.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }
}
