package com.naufal.younifirst.Home;

import android.graphics.Color;
import android.os.Bundle;
import android.text.Layout;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.naufal.younifirst.R;
import com.naufal.younifirst.api.ApiHelper;
import com.naufal.younifirst.controller.LostFoundController;
import com.naufal.younifirst.model.LostFound;

import java.util.ArrayList;
import java.util.List;

public class LostnFoundFragment extends Fragment {

    private static final String TAG = "LostFoundFragment";
    private RecyclerView recyclerView;
    private LostFoundAdapter adapter;
    private List<LostFound> lostFoundList;
    private LostFoundController controller;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_lostn_found, container, false);

        // Initialize views
        recyclerView = rootView.findViewById(R.id.recyclerViewLostFound);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        lostFoundList = new ArrayList<>();
        adapter = new LostFoundAdapter(lostFoundList);
        recyclerView.setAdapter(adapter);

        // Initialize controller
        if (getActivity() != null) {
            ApiHelper.initialize(getActivity());
            controller = new LostFoundController(getActivity());
        }

        // Load data
        loadLostFoundData();

        return rootView;
    }

    private void loadLostFoundData() {
        Log.d(TAG, "Loading lost found data...");

        if (controller == null) {
            Toast.makeText(getContext(), "Controller not initialized", Toast.LENGTH_SHORT).show();
            return;
        }

        controller.getAllLostFound(new LostFoundController.LostFoundCallback() {
            @Override
            public void onSuccess(List<LostFound> dataList) {
                Log.d(TAG, "Successfully loaded " + dataList.size() + " items");

                if (getActivity() == null) return;

                getActivity().runOnUiThread(() -> {
                    if (dataList.isEmpty()) {
                        Toast.makeText(getContext(), "Tidak ada data ditemukan", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Update adapter with new data
                    adapter.updateData(dataList);
                    recyclerView.setVisibility(View.VISIBLE);

                    // Log for debugging
                    for (LostFound item : dataList) {
                        Log.d(TAG, "Item: " + item.getItemName() +
                                ", Status: " + item.getStatus() +
                                ", Desc: " + (item.getDescription() != null && item.getDescription().length() > 20 ?
                                item.getDescription().substring(0, 20) + "..." : item.getDescription()));
                    }
                });
            }

            @Override
            public void onFailure(String error) {
                Log.e(TAG, "Failed to load data: " + error);

                if (getActivity() == null) return;

                getActivity().runOnUiThread(() -> {
                    Toast.makeText(getContext(), "Gagal memuat data: " + error, Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    // Adapter class
    private class LostFoundAdapter extends RecyclerView.Adapter<LostFoundAdapter.ViewHolder> {

        private List<LostFound> items;

        public LostFoundAdapter(List<LostFound> items) {
            this.items = items != null ? items : new ArrayList<>();
        }

        public void updateData(List<LostFound> newItems) {
            this.items = newItems != null ? newItems : new ArrayList<>();
            Log.d(TAG, "Adapter updated with " + items.size() + " items");
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            try {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_lost_found, parent, false);
                return new ViewHolder(view);
            } catch (Exception e) {
                Log.e(TAG, "Error creating view holder: " + e.getMessage());
                e.printStackTrace();
                View view = new TextView(parent.getContext());
                return new ViewHolder(view);
            }
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            try {
                if (position < 0 || position >= items.size()) {
                    Log.e(TAG, "Invalid position: " + position);
                    return;
                }

                LostFound item = items.get(position);
                Log.d(TAG, "Binding item at position " + position + ": " + item.getItemName() +
                        ", Status: " + item.getStatus() + ", Desc length: " +
                        (item.getDescription() != null ? item.getDescription().length() : 0));

                // Set user info
                holder.tvUserName.setText(item.getUserName() != null ? item.getUserName() : "Pengguna");
                holder.tvTimeAgo.setText(item.getTimeAgo() != null && !item.getTimeAgo().isEmpty() ?
                        item.getTimeAgo() : "Baru saja");

                // Load user image if available
                String userImageUrl = item.getUserImage();
                if (userImageUrl != null && !userImageUrl.isEmpty() && !userImageUrl.equals("null")) {
                    Log.d(TAG, "Loading user image: " + userImageUrl);
                    Glide.with(holder.itemView.getContext())
                            .load(userImageUrl)
                            .apply(new RequestOptions()
                                    .placeholder(R.drawable.profile_coba)
                                    .error(R.drawable.profile_coba)
                                    .circleCrop())
                            .into(holder.ivUserImage);
                } else {
                    holder.ivUserImage.setImageResource(R.drawable.profile_coba);
                }

                // Load item image if available
                String itemImageUrl = item.getItemImage();
                if (itemImageUrl != null && !itemImageUrl.isEmpty() && !itemImageUrl.equals("null")) {
                    Log.d(TAG, "Loading item image: " + itemImageUrl);
                    Glide.with(holder.itemView.getContext())
                            .load(itemImageUrl)
                            .apply(new RequestOptions()
                                    .placeholder(R.drawable.gambar_lostandfound)
                                    .error(R.drawable.gambar_lostandfound)
                                    .centerCrop())
                            .into(holder.ivItemImage);
                } else {
                    holder.ivItemImage.setImageResource(R.drawable.gambar_lostandfound);
                }

                // Set location
                String location = item.getLocation();
                if (location != null && !location.isEmpty() && !location.equals("null")) {
                    holder.tvLocation.setText(location);
                } else {
                    holder.tvLocation.setText("Lokasi tidak diketahui");
                }

                // Set description with toggle
                String description = item.getDescription();
                if (description != null && !description.isEmpty() && !description.equals("null")) {
                    Log.d(TAG, "Setting description: " + description.substring(0, Math.min(30, description.length())) + "...");

                    // Setup description dengan toggle
                    setupDescriptionWithToggle(holder.tvDescription, item);
                } else {
                    holder.tvDescription.setText("Tidak ada deskripsi");
                    holder.tvDescription.setMovementMethod(null);
                }

                // Set kategori
                String kategori = item.getCategory();
                Log.d(TAG, "Item kategori: " + kategori);

                if (kategori != null && !kategori.isEmpty()) {
                    if (kategori.contains("found") || kategori.equals("menemukan")) {
                        holder.btnStatus.setText("Menemukan");
                    } else if (kategori.contains("lost") || kategori.equals("kehilangan")) {
                        holder.btnStatus.setText("Kehilangan");
                    } else {
                        holder.btnStatus.setText(kategori);
                    }
                } else {
                    holder.btnStatus.setText("Menemukan");
                }

                // Set likes and comments count
                String likesText = formatCount(item.getLikes());
                String commentsText = formatCount(item.getComments());
                holder.tvLikes.setText(likesText);
                holder.tvComments.setText(commentsText);

                // Set click listeners
                holder.btnShare.setOnClickListener(v -> {
                    shareItem(item);
                });

                holder.btnMore.setOnClickListener(v -> {
                    showOptionsMenu(item, position);
                });

                // Debug log
                Log.d(TAG, "Successfully bound item: " + item.getItemName());

            } catch (Exception e) {
                Log.e(TAG, "Error binding view holder at position " + position + ": " + e.getMessage());
                e.printStackTrace();
            }
        }

        // Metode setupDescriptionWithToggle yang diperbaiki (dalam adapter)
        private void setupDescriptionWithToggle(TextView textView, LostFound item) {
            try {
                String fullDescription = item.getDescription();

                if (fullDescription == null || fullDescription.isEmpty() || fullDescription.equals("null")) {
                    textView.setText("Tidak ada deskripsi");
                    textView.setMovementMethod(null);
                    return;
                }

                // Reset movement method
                textView.setMovementMethod(null);

                // Buat view measurement untuk menghitung baris
                textView.measure(
                        View.MeasureSpec.makeMeasureSpec(textView.getWidth(), View.MeasureSpec.EXACTLY),
                        View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
                );

                // Hitung tinggi per baris (approximate)
                float lineHeight = textView.getPaint().getTextSize() * 1.2f; // 1.2 adalah line spacing
                int measuredHeight = textView.getMeasuredHeight();
                int estimatedLines = (int) Math.ceil(measuredHeight / lineHeight);

                Log.d(TAG, "Estimated lines: " + estimatedLines + ", Height: " + measuredHeight + ", Line height: " + lineHeight);

                // Jika diperkirakan lebih dari 2 baris, tampilkan dengan toggle
                if (estimatedLines > 2) {
                    // Potong teks hingga kira-kira 2 baris
                    int charsPerLine = (int) (textView.getWidth() / textView.getPaint().measureText("A"));
                    int maxCharsFor2Lines = charsPerLine * 2;

                    String shortText;
                    if (fullDescription.length() > maxCharsFor2Lines) {
                        shortText = fullDescription.substring(0, maxCharsFor2Lines).trim() + "...";
                    } else {
                        shortText = fullDescription;
                    }

                    SpannableString spannable = new SpannableString(shortText + " Selengkapnya");

                    ClickableSpan clickableSpan = new ClickableSpan() {
                        @Override
                        public void onClick(@NonNull View widget) {
                            // Show full description with contact info and "Lebih Sedikit" option
                            String contactInfo = "\n\n📞 Kontak: ";
                            if (item.getPhone() != null && !item.getPhone().isEmpty() && !item.getPhone().equals("null")) {
                                contactInfo += item.getPhone();
                            } else {
                                contactInfo += "Tidak tersedia";
                            }

                            contactInfo += "\n📧 Email: ";
                            if (item.getEmail() != null && !item.getEmail().isEmpty() && !item.getEmail().equals("null")) {
                                contactInfo += item.getEmail();
                            } else {
                                contactInfo += "Tidak tersedia";
                            }

                            String fullTextWithContact = fullDescription + contactInfo;

                            SpannableString lessSpannable = new SpannableString(fullTextWithContact + "\n\nLebih Sedikit");

                            ClickableSpan lessSpan = new ClickableSpan() {
                                @Override
                                public void onClick(@NonNull View widget) {
                                    setupDescriptionWithToggle(textView, item);
                                }

                                @Override
                                public void updateDrawState(@NonNull TextPaint ds) {
                                    ds.setColor(Color.parseColor("#5E8BFF"));
                                    ds.setUnderlineText(false);
                                    ds.setFakeBoldText(true);
                                }
                            };

                            lessSpannable.setSpan(lessSpan, fullTextWithContact.length(),
                                    lessSpannable.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                            textView.setText(lessSpannable);
                            textView.setMovementMethod(LinkMovementMethod.getInstance());
                            textView.setMaxLines(Integer.MAX_VALUE); // Tampilkan semua baris
                        }

                        @Override
                        public void updateDrawState(@NonNull TextPaint ds) {
                            ds.setColor(Color.parseColor("#5E8BFF"));
                            ds.setUnderlineText(false);
                            ds.setFakeBoldText(true);
                        }
                    };

                    spannable.setSpan(clickableSpan, shortText.length(),
                            spannable.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                    textView.setText(spannable);
                    textView.setMovementMethod(LinkMovementMethod.getInstance());
                    textView.setMaxLines(2); // Batasi 2 baris untuk tampilan singkat

                } else {
                    // Jika kurang dari atau sama dengan 2 baris, tampilkan langsung
                    textView.setText(fullDescription);
                    textView.setMovementMethod(null);
                    textView.setMaxLines(Integer.MAX_VALUE);
                }

            } catch (Exception e) {
                Log.e(TAG, "Error setting up description toggle: " + e.getMessage());
                e.printStackTrace();
                // Fallback: gunakan metode sederhana
                String fullDescription = item.getDescription();
                if (fullDescription != null && fullDescription.length() > 100) {
                    String shortText = fullDescription.substring(0, Math.min(100, fullDescription.length())) + "...";
                    SpannableString spannable = new SpannableString(shortText + " Selengkapnya");

                    ClickableSpan clickableSpan = new ClickableSpan() {
                        @Override
                        public void onClick(@NonNull View widget) {
                            // Show full description with contact info
                            String contactInfo = "\n\n📞 Kontak: ";
                            if (item.getPhone() != null && !item.getPhone().isEmpty() && !item.getPhone().equals("null")) {
                                contactInfo += item.getPhone();
                            } else {
                                contactInfo += "Tidak tersedia";
                            }

                            contactInfo += "\n📧 Email: ";
                            if (item.getEmail() != null && !item.getEmail().isEmpty() && !item.getEmail().equals("null")) {
                                contactInfo += item.getEmail();
                            } else {
                                contactInfo += "Tidak tersedia";
                            }

                            String fullTextWithContact = fullDescription + contactInfo;

                            SpannableString lessSpannable = new SpannableString(fullTextWithContact + "\n\nLebih Sedikit");

                            ClickableSpan lessSpan = new ClickableSpan() {
                                @Override
                                public void onClick(@NonNull View widget) {
                                    textView.setText(spannable);
                                    textView.setMovementMethod(LinkMovementMethod.getInstance());
                                    textView.setMaxLines(2);
                                }

                                @Override
                                public void updateDrawState(@NonNull TextPaint ds) {
                                    ds.setColor(Color.parseColor("#5E8BFF"));
                                    ds.setUnderlineText(false);
                                    ds.setFakeBoldText(true);
                                }
                            };

                            lessSpannable.setSpan(lessSpan, fullTextWithContact.length(),
                                    lessSpannable.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                            textView.setText(lessSpannable);
                            textView.setMovementMethod(LinkMovementMethod.getInstance());
                            textView.setMaxLines(Integer.MAX_VALUE);
                        }

                        @Override
                        public void updateDrawState(@NonNull TextPaint ds) {
                            ds.setColor(Color.parseColor("#5E8BFF"));
                            ds.setUnderlineText(false);
                            ds.setFakeBoldText(true);
                        }
                    };

                    spannable.setSpan(clickableSpan, shortText.length(),
                            spannable.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                    textView.setText(spannable);
                    textView.setMovementMethod(LinkMovementMethod.getInstance());
                    textView.setMaxLines(2);
                } else {
                    textView.setText(fullDescription != null ? fullDescription : "Deskripsi");
                    textView.setMovementMethod(null);
                }
            }
        }

        // Metode fallback jika perhitungan line count gagal
        private void fallbackDescriptionToggle(TextView textView, String fullDescription, LostFound item) {
            try {
                // Gunakan metode berdasarkan karakter jika line count gagal
                if (fullDescription.length() > 100) {
                    String shortText = fullDescription.substring(0, Math.min(100, fullDescription.length())) + "...";

                    SpannableString spannable = new SpannableString(shortText + " Selengkapnya");

                    ClickableSpan clickableSpan = new ClickableSpan() {
                        @Override
                        public void onClick(@NonNull View widget) {
                            // Show full description with contact info
                            String contactInfo = "\n\n📞 Kontak: ";
                            if (item.getPhone() != null && !item.getPhone().isEmpty() && !item.getPhone().equals("null")) {
                                contactInfo += item.getPhone();
                            } else {
                                contactInfo += "Tidak tersedia";
                            }

                            contactInfo += "\n📧 Email: ";
                            if (item.getEmail() != null && !item.getEmail().isEmpty() && !item.getEmail().equals("null")) {
                                contactInfo += item.getEmail();
                            } else {
                                contactInfo += "Tidak tersedia";
                            }

                            String fullTextWithContact = fullDescription + contactInfo;

                            SpannableString lessSpannable = new SpannableString(fullTextWithContact + "\n\nLebih Sedikit");

                            ClickableSpan lessSpan = new ClickableSpan() {
                                @Override
                                public void onClick(@NonNull View widget) {
                                    setupDescriptionWithToggle(textView, item);
                                }

                                @Override
                                public void updateDrawState(@NonNull TextPaint ds) {
                                    ds.setColor(Color.parseColor("#5E8BFF"));
                                    ds.setUnderlineText(false);
                                    ds.setFakeBoldText(true);
                                }
                            };

                            lessSpannable.setSpan(lessSpan, fullTextWithContact.length(),
                                    lessSpannable.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                            textView.setText(lessSpannable);
                            textView.setMovementMethod(LinkMovementMethod.getInstance());
                        }

                        @Override
                        public void updateDrawState(@NonNull TextPaint ds) {
                            ds.setColor(Color.parseColor("#5E8BFF"));
                            ds.setUnderlineText(false);
                            ds.setFakeBoldText(true);
                        }
                    };

                    spannable.setSpan(clickableSpan, shortText.length(),
                            spannable.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                    textView.setText(spannable);
                    textView.setMovementMethod(LinkMovementMethod.getInstance());
                } else {
                    textView.setText(fullDescription);
                    textView.setMovementMethod(null);
                }
            } catch (Exception e) {
                textView.setText(fullDescription);
            }
        }

        private String formatCount(int count) {
            try {
                if (count >= 1000000) {
                    return String.format("%.1f", count / 1000000.0) + "M";
                } else if (count >= 1000) {
                    return String.format("%.1f", count / 1000.0) + "k";
                }
                return String.valueOf(count);
            } catch (Exception e) {
                return "0";
            }
        }

        private void shareItem(LostFound item) {
            try {
                String shareText = "📢 " + item.getItemName() + "\n\n" +
                        item.getDescription() + "\n\n" +
                        "📍 Lokasi: " + item.getLocation() + "\n" +
                        "👤 Diposting oleh: " + item.getUserName() + "\n" +
                        "🔍 Status: " + (item.getStatus().equalsIgnoreCase("found") ? "Ditemukan" : "Hilang");

                android.content.Intent shareIntent = new android.content.Intent(
                        android.content.Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareText);
                startActivity(android.content.Intent.createChooser(shareIntent, "Bagikan"));
            } catch (Exception e) {
                Log.e(TAG, "Error sharing item: " + e.getMessage());
                Toast.makeText(getContext(), "Gagal membagikan", Toast.LENGTH_SHORT).show();
            }
        }

        private void showOptionsMenu(LostFound item, int position) {
            try {
                if (getActivity() == null) return;

                android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(getActivity());
                builder.setTitle("Pilihan")
                        .setItems(new String[]{"Edit", "Hapus", "Laporkan", "Tandai Klaim"}, (dialog, which) -> {
                            switch (which) {
                                case 0:
                                    editItem(item);
                                    break;
                                case 1:
                                    deleteItem(item, position);
                                    break;
                                case 2:
                                    reportItem(item);
                                    break;
                                case 3:
                                    markAsClaimed(item);
                                    break;
                            }
                        })
                        .show();
            } catch (Exception e) {
                Log.e(TAG, "Error showing options menu: " + e.getMessage());
            }
        }

        private void editItem(LostFound item) {
            Toast.makeText(getContext(), "Edit: " + item.getItemName(), Toast.LENGTH_SHORT).show();
        }

        private void deleteItem(LostFound item, int position) {
            try {
                if (getActivity() == null) return;

                new android.app.AlertDialog.Builder(getActivity())
                        .setTitle("Hapus Item")
                        .setMessage("Apakah Anda yakin ingin menghapus item ini?")
                        .setPositiveButton("Ya", (dialog, which) -> {
                            items.remove(position);
                            notifyItemRemoved(position);
                            Toast.makeText(getContext(), "Item berhasil dihapus", Toast.LENGTH_SHORT).show();
                        })
                        .setNegativeButton("Tidak", null)
                        .show();
            } catch (Exception e) {
                Log.e(TAG, "Error deleting item: " + e.getMessage());
            }
        }

        private void reportItem(LostFound item) {
            Toast.makeText(getContext(), "Melaporkan: " + item.getItemName(), Toast.LENGTH_SHORT).show();
        }

        private void markAsClaimed(LostFound item) {
            Toast.makeText(getContext(), "Item ditandai sebagai sudah diklaim", Toast.LENGTH_SHORT).show();
        }

        @Override
        public int getItemCount() {
            return items != null ? items.size() : 0;
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            ImageView ivUserImage, ivItemImage, btnShare;
            ImageButton btnMore;
            TextView tvUserName, tvTimeAgo, tvLocation, tvDescription;
            Button btnStatus;
            TextView tvLikes, tvComments;

            ViewHolder(View itemView) {
                super(itemView);

                try {
                    // Initialize views with proper null checks
                    ivUserImage = itemView.findViewById(R.id.iv_user_image);
                    if (ivUserImage == null) Log.e(TAG, "iv_user_image not found");

                    ivItemImage = itemView.findViewById(R.id.iv_item_image);
                    if (ivItemImage == null) Log.e(TAG, "iv_item_image not found");

                    tvUserName = itemView.findViewById(R.id.tv_user_name);
                    if (tvUserName == null) Log.e(TAG, "tv_user_name not found");

                    tvTimeAgo = itemView.findViewById(R.id.tv_time_ago);
                    if (tvTimeAgo == null) Log.e(TAG, "tv_time_ago not found");

                    tvLocation = itemView.findViewById(R.id.tv_location);
                    if (tvLocation == null) Log.e(TAG, "tv_location not found");

                    tvDescription = itemView.findViewById(R.id.tv_description);
                    if (tvDescription == null) Log.e(TAG, "tv_description not found");

                    btnStatus = itemView.findViewById(R.id.btn_status);
                    if (btnStatus == null) Log.e(TAG, "btn_status not found");

                    btnMore = itemView.findViewById(R.id.btn_more);
                    if (btnMore == null) Log.e(TAG, "btn_more not found");
                    btnShare = itemView.findViewById(R.id.btn_share);
                    if (btnShare == null) Log.e(TAG, "btn_share not found");

                } catch (Exception e) {
                    Log.e(TAG, "Error finding views: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(TAG, "Fragment destroyed");
    }
}