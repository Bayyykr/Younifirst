package com.naufal.younifirst.Event;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.naufal.younifirst.R;

public class DaftarEvent extends AppCompatActivity {

    private static final String TAG = "DaftarEvent";
    private String contactPerson;
    private String urlInstagram;
    private String eventName;
    private String eventOrganizer;
    private String eventLocation;
    private String eventId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_register_event);

        initViews();
        loadEventData();
        setupButtons();
    }

    private void initViews() {
        // Back button
        ImageButton btnBack = findViewById(R.id.first_next);
        if (btnBack != null) {
            btnBack.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // PERBAIKAN: Kembali ke halaman sebelumnya dengan proper finish
                    onBackPressed();
                }
            });
        }

        LinearLayout btn_kembali = findViewById(R.id.button_kembali);
        if (btnBack != null) {
            btn_kembali.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // PERBAIKAN: Kembali ke halaman sebelumnya dengan proper finish
                    onBackPressed();
                }
            });
        }

        // Flag button (opsional)
        ImageButton btnFlag = findViewById(R.id.flag);
        if (btnFlag != null) {
            btnFlag.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(DaftarEvent.this, "Menu lainnya", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void loadEventData() {
        Intent intent = getIntent();

        // Ambil data dari intent
        eventId = intent.getStringExtra("event_id");
        eventName = intent.getStringExtra("event_name");
        contactPerson = intent.getStringExtra("event_contact_person");
        urlInstagram = intent.getStringExtra("event_url_instagram");
        eventOrganizer = intent.getStringExtra("event_organizer");
        eventLocation = intent.getStringExtra("event_location");

        // Log data untuk debugging
        Log.d(TAG, "=== EVENT REGISTRATION DATA ===");
        Log.d(TAG, "Event ID: " + eventId);
        Log.d(TAG, "Event Name: " + eventName);
        Log.d(TAG, "Contact Person: " + contactPerson);
        Log.d(TAG, "Instagram URL: " + urlInstagram);
        Log.d(TAG, "Organizer: " + eventOrganizer);
        Log.d(TAG, "Location: " + eventLocation);

        // Set judul event
        TextView titleView = findViewById(R.id.textViewTitle);
        if (titleView != null) {
            if (eventName != null && !eventName.isEmpty() && !"null".equals(eventName)) {
                titleView.setText("Informasi Pendaftaran");
            } else {
                titleView.setText("Informasi Pendaftaran");
            }
        }

        // Set data WhatsApp
        TextView whatsappText = findViewById(R.id.text_whatsapp_number);
        if (whatsappText != null) {
            if (contactPerson != null && !contactPerson.trim().isEmpty() && !"null".equalsIgnoreCase(contactPerson)) {
                whatsappText.setText(formatPhoneNumber(contactPerson));
            } else {
                whatsappText.setText("Tidak tersedia");
                whatsappText.setTextColor(getResources().getColor(android.R.color.darker_gray));
            }
        }

        // Set data Instagram
        TextView instagramText = findViewById(R.id.text_instagram_username);
        if (instagramText != null) {
            if (urlInstagram != null && !urlInstagram.trim().isEmpty() && !"null".equalsIgnoreCase(urlInstagram)) {
                String displayText = urlInstagram;
                if (!urlInstagram.startsWith("@") && !urlInstagram.contains("instagram.com")) {
                    displayText = "@" + urlInstagram;
                }
                instagramText.setText(displayText);
            } else {
                instagramText.setText("Tidak tersedia");
                instagramText.setTextColor(getResources().getColor(android.R.color.darker_gray));
            }
        }

        TextView locationText = findViewById(R.id.text_location);
        if (locationText != null) {
            if (eventLocation != null && !eventLocation.isEmpty() && !"null".equals(eventLocation)) {
                locationText.setText(eventLocation);
            } else {
                locationText.setText("Tidak tersedia");
            }
        }
    }

    private void setupButtons() {
        // WhatsApp container
        View whatsappContainer = findViewById(R.id.container_whatsapp);
        if (whatsappContainer != null) {
            whatsappContainer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openWhatsApp();
                }
            });
        }

        // WhatsApp text
        TextView whatsappText = findViewById(R.id.text_whatsapp_number);
        if (whatsappText != null) {
            whatsappText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openWhatsApp();
                }
            });
        }

        // Instagram container
        View instagramContainer = findViewById(R.id.container_instagram);
        if (instagramContainer != null) {
            instagramContainer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openInstagram();
                }
            });
        }

        // Instagram text
        TextView instagramText = findViewById(R.id.text_instagram_username);
        if (instagramText != null) {
            instagramText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openInstagram();
                }
            });
        }
    }

    private String formatPhoneNumber(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return "";
        }

        String formatted = phone.trim();
        // Hapus semua karakter non-digit kecuali +
        formatted = formatted.replaceAll("[^0-9+]", "");

        return formatted;
    }

    private void openWhatsApp() {
        if (contactPerson != null && !contactPerson.trim().isEmpty() && !"null".equalsIgnoreCase(contactPerson)) {
            try {
                // Format nomor WhatsApp
                String phoneNumber = formatPhoneNumber(contactPerson);

                if (phoneNumber.isEmpty()) {
                    Toast.makeText(this, "Nomor WhatsApp tidak valid", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Jika tidak dimulai dengan +, tambahkan +62 (kode Indonesia)
                if (!phoneNumber.startsWith("+")) {
                    if (phoneNumber.startsWith("0")) {
                        phoneNumber = "+62" + phoneNumber.substring(1);
                    } else if (phoneNumber.startsWith("62")) {
                        phoneNumber = "+" + phoneNumber;
                    } else {
                        phoneNumber = "+62" + phoneNumber;
                    }
                }

                // Buat pesan default
                String defaultMessage = "Halo, saya tertarik dengan event ";
                if (eventName != null && !eventName.isEmpty()) {
                    defaultMessage += "\"" + eventName + "\"";
                } else {
                    defaultMessage += "ini";
                }
                defaultMessage += ". Bagaimana cara mendaftarnya?";

                // Encode pesan untuk URL
                String encodedMessage = Uri.encode(defaultMessage);

                // Buat URL WhatsApp
                String whatsappUrl = "https://api.whatsapp.com/send?phone=" + phoneNumber + "&text=" + encodedMessage;

                Log.d(TAG, "Opening WhatsApp URL: " + whatsappUrl);

                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(whatsappUrl));

                // Cek apakah WhatsApp terinstall
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(intent);
                } else {
                    Toast.makeText(this, "WhatsApp tidak terinstall", Toast.LENGTH_SHORT).show();
                }

            } catch (Exception e) {
                Log.e(TAG, "Error opening WhatsApp: " + e.getMessage());
                Toast.makeText(this, "Tidak dapat membuka WhatsApp: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Nomor WhatsApp tidak tersedia untuk event ini", Toast.LENGTH_SHORT).show();
        }
    }

    private void openInstagram() {
        if (urlInstagram != null && !urlInstagram.trim().isEmpty() && !"null".equalsIgnoreCase(urlInstagram)) {
            try {
                String instagramUrl = urlInstagram.trim();
                String finalUrl = "";

                // Cek tipe URL Instagram
                if (instagramUrl.startsWith("@")) {
                    // Format: @username
                    String username = instagramUrl.substring(1);
                    finalUrl = "https://instagram.com/" + username;
                } else if (instagramUrl.contains("instagram.com/")) {
                    // Sudah format URL lengkap
                    if (instagramUrl.startsWith("http://") || instagramUrl.startsWith("https://")) {
                        finalUrl = instagramUrl;
                    } else {
                        finalUrl = "https://" + instagramUrl;
                    }
                } else {
                    // Format: username biasa
                    finalUrl = "https://instagram.com/" + instagramUrl;
                }

                Log.d(TAG, "Opening Instagram URL: " + finalUrl);

                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(finalUrl));

                // Cek apakah Instagram terinstall
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(intent);
                } else {
                    // Fallback ke browser
                    intent = new Intent(Intent.ACTION_VIEW, Uri.parse(finalUrl));
                    if (intent.resolveActivity(getPackageManager()) != null) {
                        startActivity(intent);
                    } else {
                        Toast.makeText(this, "Tidak dapat membuka Instagram", Toast.LENGTH_SHORT).show();
                    }
                }

            } catch (Exception e) {
                Log.e(TAG, "Error opening Instagram: " + e.getMessage());
                Toast.makeText(this, "Tidak dapat membuka Instagram: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Akun Instagram tidak tersedia untuk event ini", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "DaftarEvent activity resumed");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "DaftarEvent activity paused");
    }
}