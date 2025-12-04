package com.naufal.younifirst.LognReg;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.naufal.younifirst.Home.MainActivity;
import com.naufal.younifirst.R;
import com.naufal.younifirst.api.ApiHelper;
import com.naufal.younifirst.opening.ShownHidePwKt;

import org.json.JSONObject;

public class login extends AppCompatActivity {

    private static final String TAG = "LOGIN_ACTIVITY";
    private EditText etEmail, etPassword;
    private Button btnMulai;
    private TextView tvLupaSandi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.page_login);

        // Inisialisasi ApiHelper dengan context
        ApiHelper.initialize(getApplicationContext());

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.pwlogin);
        btnMulai = findViewById(R.id.btn_mulai);
        tvLupaSandi = findViewById(R.id.lupa_sandi);

        // Setup password toggle
        ShownHidePwKt.setupPasswordToggle(etPassword, R.drawable.open_eye, R.drawable.close_eye);

        // Setup listeners
        btnMulai.setOnClickListener(v -> handleLogin());
        tvLupaSandi.setOnClickListener(v -> openForgotPassword());

        // Cek auto-login
        checkAutoLogin();
    }

    private void checkAutoLogin() {
        if (ApiHelper.isLoggedIn()) {
            Log.d(TAG, "🔍 Already logged in, redirecting to MainActivity...");
            Toast.makeText(this, "Auto-login...", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
    }

    private void handleLogin() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // Validasi input
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Isi semua data", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d(TAG, "📝 Login attempt - Email: " + email + ", Password length: " + password.length());

        // Disable button selama proses login
        btnMulai.setEnabled(false);
        btnMulai.setText("Logging in...");

        // Panggil API login
        ApiHelper.login(email, password, new ApiHelper.ApiCallback() {
            @Override
            public void onSuccess(String result) {
                runOnUiThread(() -> {
                    btnMulai.setEnabled(true);
                    btnMulai.setText("Mulai");

                    try {
                        Log.d(TAG, "✅ Login success response: " + result);
                        JSONObject json = new JSONObject(result);
                        String status = json.getString("status");

                        if ("success".equals(status)) {
                            // Ambil data user
                            JSONObject user = json.getJSONObject("user");
                            String userId = user.optString("id", "");
                            String userEmail = user.optString("email", email);
                            String userName = user.optString("name", "");

                            Log.d(TAG, "👤 User logged in - ID: " + userId + ", Name: " + userName);

                            Toast.makeText(login.this,
                                    "Login berhasil! Selamat datang " + (userName.isEmpty() ? email : userName),
                                    Toast.LENGTH_SHORT).show();

                            // Redirect ke MainActivity
                            Intent intent = new Intent(login.this, MainActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            finish();
                        } else {
                            String message = json.optString("message", "Login gagal");
                            Toast.makeText(login.this, message, Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "❌ Error parsing login response: " + e.getMessage());
                        Toast.makeText(login.this, "Error parsing response", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onFailure(String error) {
                runOnUiThread(() -> {
                    btnMulai.setEnabled(true);
                    btnMulai.setText("Mulai");

                    Log.e(TAG, "❌ Login failed: " + error);

                    if (error.contains("Password salah")) {
                        Toast.makeText(login.this,
                                "Password yang Anda masukkan salah",
                                Toast.LENGTH_LONG).show();
                    } else if (error.contains("Email")) {
                        Toast.makeText(login.this,
                                "Email tidak ditemukan",
                                Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(login.this,
                                "Login gagal: " + error,
                                Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }

    private void openForgotPassword() {
        Toast.makeText(this, "Fitur lupa sandi belum tersedia", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!ApiHelper.isLoggedIn()) {
            ApiHelper.initialize(getApplicationContext());
        }
    }
}