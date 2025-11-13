package com.naufal.younifirst.Home;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import com.naufal.younifirst.R;
import com.naufal.younifirst.custom.CustomEditText;

import java.util.ArrayList;

public class PengaturanActivity extends AppCompatActivity {
    private SwitchCompat switchtema, switchnotifikasi;
    private ImageButton btnSettingProfile;
    private CustomEditText etDeskripsiMasukan;
    private LinearLayout layoutUploadedImages;
    private ImageButton btnGalery;
    private static final int REQUEST_CODE_GALLERY = 101;
    private ArrayList<Uri> imageUris = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        showPengaturanLayout();
    }

    private void showPengaturanLayout() {
        setContentView(R.layout.layout_pengaturan);

        switchtema = findViewById(R.id.switch_tema);
        switchnotifikasi = findViewById(R.id.switch_notifikasi);
        btnSettingProfile = findViewById(R.id.btn_setting_profile);
        ImageView backButtonMain = findViewById(R.id.back_to_mainactivity);
        LinearLayout bantuanDanMasukkan = findViewById(R.id.bantuandanmasukkan);

        int[][] states = new int[][]{
                new int[]{android.R.attr.state_checked},
                new int[]{}
        };
        int[] trackColors = new int[]{Color.WHITE, Color.WHITE};
        ColorStateList customTrackTint = new ColorStateList(states, trackColors);
        switchtema.setTrackTintList(customTrackTint);
        switchnotifikasi.setTrackTintList(customTrackTint);

        btnSettingProfile.setOnClickListener(v -> startActivity(new Intent(this, ProfileActivity.class)));
        backButtonMain.setOnClickListener(v -> finish());
        bantuanDanMasukkan.setOnClickListener(v -> showBantuanLayout());
    }

    private void showBantuanLayout() {
        setContentView(R.layout.layout_bantuan);

        ImageView backFromBantuan = findViewById(R.id.back_to_mainactivity);
        LinearLayout pusatBantuan = findViewById(R.id.pusatbantuan);
        LinearLayout kirimMasukan = findViewById(R.id.kirimmasukan);
        LinearLayout navigasiFaq = findViewById(R.id.bantuanfaq);
        LinearLayout infoAplikasi = findViewById(R.id.infoaplikasi);

        backFromBantuan.setOnClickListener(v -> showPengaturanLayout());
        if (pusatBantuan != null) pusatBantuan.setOnClickListener(v -> showPusatBantuanLayout());
        if (kirimMasukan != null) kirimMasukan.setOnClickListener(v -> showKirimMasukanLayout());
        if (navigasiFaq != null) navigasiFaq.setOnClickListener(v -> showFAQLayout());
        if (infoAplikasi != null) infoAplikasi.setOnClickListener(v -> showInfoAplikasi());
    }

    private void showPusatBantuanLayout() {
        setContentView(R.layout.layout_pusat_bantuan);

        ImageView backFromPusat = findViewById(R.id.back_to_mainactivity);
        TextView akundanlogin = findViewById(R.id.akundanlogin);
        TextView panduanUmum = findViewById(R.id.panduanumum);
        TextView fiturAplikasi = findViewById(R.id.fituraplikasi);
        TextView hubungiKami = findViewById(R.id.hubungikami);

        backFromPusat.setOnClickListener(v -> showBantuanLayout());

        if (akundanlogin != null) akundanlogin.setOnClickListener(v -> showFragmentAkunLogin());
        if (panduanUmum != null) panduanUmum.setOnClickListener(v -> showFragmentPanduanUmum());
        if (fiturAplikasi != null) fiturAplikasi.setOnClickListener(v -> showFragmentFiturAplikasi());
        if (hubungiKami != null) hubungiKami.setOnClickListener(v -> showFragmentHubungiKami());
    }

    private void showpengaturan() {
        setContentView(R.layout.layout_bantuan);

        ImageView backFromPusat = findViewById(R.id.back_to_mainactivity);
        backFromPusat.setOnClickListener(v -> showBantuanLayout());
    }

    private void showInfoAplikasilayout() {
        setContentView(R.layout.layout_bantuan);

        ImageView backFromPusat = findViewById(R.id.back_to_mainactivity);
        LinearLayout infoApk = findViewById(R.id.infoaplikasi);

        backFromPusat.setOnClickListener(v -> showBantuanLayout());

        if (infoApk != null) infoApk.setOnClickListener(v -> showInfoAplikasi());
    }

    private void showKirimMasukanLayout() {
        setContentView(R.layout.layout_kirim_masukan);

        ImageView backFromMasukan = findViewById(R.id.back_to_mainactivity);
        if (backFromMasukan != null) backFromMasukan.setOnClickListener(v -> showBantuanLayout());

        etDeskripsiMasukan = findViewById(R.id.etdeskripsimasalahteknis);
        if (etDeskripsiMasukan != null) {
            etDeskripsiMasukan.getEditText().setSingleLine(false);
            etDeskripsiMasukan.getEditText().setLines(5);
            etDeskripsiMasukan.getEditText().setGravity(Gravity.LEFT | Gravity.START);
            etDeskripsiMasukan.setHint("Deskripsikan masalah teknis");

            int paddingDp = 20;
            float density = getResources().getDisplayMetrics().density;
            int paddingPx = (int) (paddingDp * density + 0.5f);
            etDeskripsiMasukan.getEditText().setPadding(paddingPx, paddingPx, paddingPx, paddingPx);
        }

        layoutUploadedImages = findViewById(R.id.layout_uploaded_images);
        btnGalery = findViewById(R.id.btn_galery);
        if (btnGalery != null) {
            btnGalery.setOnClickListener(v -> openGallery());
        }
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_CODE_GALLERY);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_GALLERY && resultCode == RESULT_OK && data != null) {
            Uri selectedImage = data.getData();
            if (selectedImage != null) {
                addImageToLayout(selectedImage);
            }
        }
    }

    private void addImageToLayout(Uri imageUri) {
        imageUris.add(imageUri);

        FrameLayout frame = new FrameLayout(this);
        LinearLayout.LayoutParams frameParams = new LinearLayout.LayoutParams(200, 200);
        frameParams.setMargins(10, 0, 10, 0);
        frame.setLayoutParams(frameParams);

        ImageView imageView = new ImageView(this);
        imageView.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageView.setImageURI(imageUri);

        ImageButton btnRemove = new ImageButton(this);
        FrameLayout.LayoutParams btnParams = new FrameLayout.LayoutParams(50, 50);
        btnParams.gravity = Gravity.END | Gravity.TOP;
        btnRemove.setLayoutParams(btnParams);
        btnRemove.setBackgroundColor(Color.TRANSPARENT);
        btnRemove.setImageResource(R.drawable.icon_silang_kecil);
        btnRemove.setOnClickListener(v -> {
            layoutUploadedImages.removeView(frame);
            imageUris.remove(imageUri);
        });

        frame.addView(imageView);
        frame.addView(btnRemove);
        layoutUploadedImages.addView(frame);
    }

    private void showFragmentNavigasiAplikasi() {
        setContentView(R.layout.fragment_navigasi_aplikasi_pusat_bantuan);

        ImageView back = findViewById(R.id.back_to_mainactivity);
        if (back != null) {
            back.setOnClickListener(v -> showFragmentPanduanUmum());
        }

        LinearLayout navigasiBawah = findViewById(R.id.navigasibawah);
        if (navigasiBawah != null) {
            navigasiBawah.setOnClickListener(v -> showFragmentNavigasiAplikasiBawah());
        }
    }

    private void showFragmentNavigasiAplikasiBawah() {
        setContentView(R.layout.fragment_navigasi_aplikasi_bawah_pusat_bantuan);

        ImageView back = findViewById(R.id.back_to_mainactivity);
        if (back != null) {
            back.setOnClickListener(v -> showFragmentNavigasiAplikasi());
        }
    }

    private void showFragmentAkunLogin() {
        setContentView(R.layout.fragment_akun_login_bantuan);
        ImageView back = findViewById(R.id.back_to_mainactivity);
        if (back != null) back.setOnClickListener(v -> showPusatBantuanLayout());
    }

    private void showInfoAplikasi() {
        setContentView(R.layout.layout_bantuan_info_aplikasi);
        ImageView back = findViewById(R.id.back_to_mainactivity);
        if (back != null) back.setOnClickListener(v -> showpengaturan());
    }

    private void showFragmentPanduanUmum() {
        setContentView(R.layout.fragment_panduan_umum_pusat_bantuan);

        ImageView back = findViewById(R.id.back_to_mainactivity);
        if (back != null) back.setOnClickListener(v -> showPusatBantuanLayout());

        LinearLayout navigasiAplikasi = findViewById(R.id.navigasiaplikasi);
        if (navigasiAplikasi != null) {
            navigasiAplikasi.setOnClickListener(v -> showFragmentNavigasiAplikasi());
        }
    }

    private void showFragmentFiturAplikasi() {
        setContentView(R.layout.fragment_fitur_aplikasi_pusat_bantuan);
        ImageView back = findViewById(R.id.back_to_mainactivity);
        if (back != null) back.setOnClickListener(v -> showPusatBantuanLayout());
    }

    private void showFragmentHubungiKami() {
        setContentView(R.layout.fragment_hubungi_kami_pusat_bantuan);
        ImageView back = findViewById(R.id.back_to_mainactivity);
        if (back != null) back.setOnClickListener(v -> showPusatBantuanLayout());
    }

//INI DRODOWN FAQ WOI
    private void showFAQLayout() {
        setContentView(R.layout.layout_bantuan_faq);

        ImageView back = findViewById(R.id.back_to_mainactivity);
        if (back != null) back.setOnClickListener(v -> showBantuanLayout());

        LinearLayout faqItem1 = findViewById(R.id.dropdwon_faq_item_1);
        LinearLayout faqHeader1 = findViewById(R.id.faq_header_1);
        TextView faqAnswer1 = findViewById(R.id.faq_answer_1);
        ImageView faqIcon1 = findViewById(R.id.faq_icon_1);

        final boolean[] isExpanded1 = {false};

        faqHeader1.setOnClickListener(v -> {
            if (!isExpanded1[0]) {
                faqAnswer1.setVisibility(View.VISIBLE);
                faqIcon1.setImageResource(R.drawable.icon_drop_down_on);
                faqItem1.setBackgroundResource(R.drawable.bg_faq_active);
                isExpanded1[0] = true;
            } else {
                faqAnswer1.setVisibility(View.GONE);
                faqIcon1.setImageResource(R.drawable.icon_drop_down_off);
                faqItem1.setBackgroundResource(R.drawable.bg_faq_default);
                isExpanded1[0] = false;
            }
        });

        LinearLayout faqItem2 = findViewById(R.id.dropdwon_faq_item_2);
        LinearLayout faqHeader2 = findViewById(R.id.faq_header_2);
        TextView faqAnswer2 = findViewById(R.id.faq_answer_2);
        ImageView faqIcon2 = findViewById(R.id.faq_icon_2);

        final boolean[] isExpanded2 = {false};

        faqHeader2.setOnClickListener(v -> {
            if (!isExpanded2[0]) {
                faqAnswer2.setVisibility(View.VISIBLE);
                faqIcon2.setImageResource(R.drawable.icon_drop_down_on);
                faqItem2.setBackgroundResource(R.drawable.bg_faq_active);
                isExpanded2[0] = true;
            } else {
                faqAnswer2.setVisibility(View.GONE);
                faqIcon2.setImageResource(R.drawable.icon_drop_down_off);
                faqItem2.setBackgroundResource(R.drawable.bg_faq_default);
                isExpanded2[0] = false;
            }
        });

        LinearLayout faqItem3 = findViewById(R.id.dropdwon_faq_item_3);
        LinearLayout faqHeader3 = findViewById(R.id.faq_header_3);
        TextView faqAnswer3 = findViewById(R.id.faq_answer_3);
        ImageView faqIcon3 = findViewById(R.id.faq_icon_3);

        final boolean[] isExpanded3 = {false};

        faqHeader3.setOnClickListener(v -> {
            if (!isExpanded3[0]) {
                faqAnswer3.setVisibility(View.VISIBLE);
                faqIcon3.setImageResource(R.drawable.icon_drop_down_on);
                faqItem3.setBackgroundResource(R.drawable.bg_faq_active);
                isExpanded3[0] = true;
            } else {
                faqAnswer3.setVisibility(View.GONE);
                faqIcon3.setImageResource(R.drawable.icon_drop_down_off);
                faqItem3.setBackgroundResource(R.drawable.bg_faq_default);
                isExpanded3[0] = false;
            }
        });

        LinearLayout faqItem4 = findViewById(R.id.dropdwon_faq_item_4);
        LinearLayout faqHeader4 = findViewById(R.id.faq_header_4);
        TextView faqAnswer4 = findViewById(R.id.faq_answer_4);
        ImageView faqIcon4 = findViewById(R.id.faq_icon_4);

        final boolean[] isExpanded4 = {false};

        faqHeader4.setOnClickListener(v -> {
            if (!isExpanded4[0]) {
                faqAnswer4.setVisibility(View.VISIBLE);
                faqIcon4.setImageResource(R.drawable.icon_drop_down_on);
                faqItem4.setBackgroundResource(R.drawable.bg_faq_active);
                isExpanded4[0] = true;
            } else {
                faqAnswer4.setVisibility(View.GONE);
                faqIcon4.setImageResource(R.drawable.icon_drop_down_off);
                faqItem4.setBackgroundResource(R.drawable.bg_faq_default);
                isExpanded4[0] = false;
            }
        });

        LinearLayout faqItem5 = findViewById(R.id.dropdwon_faq_item_5);
        LinearLayout faqHeader5 = findViewById(R.id.faq_header_5);
        TextView faqAnswer5 = findViewById(R.id.faq_answer_5);
        ImageView faqIcon5 = findViewById(R.id.faq_icon_5);

        final boolean[] isExpanded5 = {false};

        faqHeader5.setOnClickListener(v -> {
            if (!isExpanded5[0]) {
                faqAnswer5.setVisibility(View.VISIBLE);
                faqIcon5.setImageResource(R.drawable.icon_drop_down_on);
                faqItem5.setBackgroundResource(R.drawable.bg_faq_active);
                isExpanded5[0] = true;
            } else {
                faqAnswer5.setVisibility(View.GONE);
                faqIcon5.setImageResource(R.drawable.icon_drop_down_off);
                faqItem5.setBackgroundResource(R.drawable.bg_faq_default);
                isExpanded5[0] = false;
            }
        });
    }
}
