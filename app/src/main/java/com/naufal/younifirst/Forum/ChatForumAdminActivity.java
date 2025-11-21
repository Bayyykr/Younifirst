package com.naufal.younifirst.Forum;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.naufal.younifirst.R;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class ChatForumAdminActivity extends AppCompatActivity {

    private LinearLayout chatList;
    private EditText inputMessage;
    private ScrollView scrollView;
    private ImageView previewImageView;
    private LinearLayout replyContainer;
    private TextView replyTextView;
    private ImageView replyCancel;
    private ImageButton btnSend;
    private ImageButton btnUpload;

    private View lastHighlightedBubble = null;

    private static final int REQUEST_IMAGE_PICK = 200;

    private Bitmap pendingImage = null;
    private String pendingReplyTargetId = null;

    private View editingTargetBubble = null;
    private boolean isEditing = false;

    private int bubbleCounter = 1;
    private final HashMap<String, View> bubbleMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_chat_forum_admin);

        chatList = findViewById(R.id.chat_list_container);
        inputMessage = findViewById(R.id.input_message);
        scrollView = findViewById(R.id.scroll_chat_container);
        previewImageView = findViewById(R.id.preview_image);
        replyContainer = findViewById(R.id.reply_container);
        replyTextView = findViewById(R.id.reply_text);
        replyCancel = findViewById(R.id.reply_cancel);
        btnSend = findViewById(R.id.btn_send);
        btnUpload = findViewById(R.id.btn_upload);

        btnSend.setOnClickListener(v -> sendText());
        btnUpload.setOnClickListener(v -> openGallery());

        replyCancel.setOnClickListener(v -> clearPendingReply());

        replyContainer.setOnClickListener(v -> {
            if (pendingReplyTargetId != null) scrollToBubble(pendingReplyTargetId);
        });

        ImageView btnBack = findViewById(R.id.back_to_mainactivity);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> {
                finish();
            });
        }

        previewImageView.setVisibility(View.GONE);
        replyContainer.setVisibility(View.GONE);
        inputMessage.setHint("Ketik Pesan");
    }

    private void sendText() {
        String msg = inputMessage.getText().toString().trim();

        if (isEditing && editingTargetBubble != null) {
            TextView tvMsg = editingTargetBubble.findViewById(R.id.chat_bubble_message);
            TextView tvCap = editingTargetBubble.findViewById(R.id.chat_bubble_caption);

            if (tvMsg != null && tvMsg.getVisibility() == View.VISIBLE) {
                tvMsg.setText(msg);
                tvMsg.setVisibility(msg.isEmpty() ? View.GONE : View.VISIBLE);
            }

            if (tvCap != null && tvCap.getVisibility() == View.VISIBLE) {
                tvCap.setText(msg);
                tvCap.setVisibility(msg.isEmpty() ? View.GONE : View.VISIBLE);
            }

            inputMessage.setText("");
            inputMessage.setHint("Ketik Pesan");

            isEditing = false;
            editingTargetBubble = null;

            clearPendingReply();
            return;
        }

        if (pendingImage != null) {
            addMessageBubble(msg.isEmpty() ? null : msg, pendingImage, pendingReplyTargetId);

            pendingImage = null;
            previewImageView.setImageBitmap(null);
            previewImageView.setVisibility(View.GONE);

            inputMessage.setText("");
            inputMessage.setHint("Ketik Pesan");

            clearPendingReply();
            return;
        }

        if (TextUtils.isEmpty(msg)) return;

        addMessageBubble(msg, null, pendingReplyTargetId);
        inputMessage.setText("");
        inputMessage.setHint("Ketik Pesan");

        clearPendingReply();
    }

    private void openGallery() {
        Intent i = new Intent(Intent.ACTION_PICK);
        i.setType("image/*");
        startActivityForResult(i, REQUEST_IMAGE_PICK);
    }

    @Override
    protected void onActivityResult(int req, int res, @Nullable Intent data) {
        super.onActivityResult(req, res, data);

        if (req == REQUEST_IMAGE_PICK && res == RESULT_OK && data != null) {
            Uri uri = data.getData();

            try {
                pendingImage = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);

                previewImageView.setImageBitmap(pendingImage);
                previewImageView.setVisibility(View.VISIBLE);

                inputMessage.setHint("Tambahkan deskripsi…");
                inputMessage.requestFocus();
                scrollToBottom();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void addMessageBubble(String text, Bitmap image, String replyToId) {

        View bubble = LayoutInflater.from(this).inflate(R.layout.custom_bubble_chat_forum_admin_2, chatList, false);

        String id = "bubble_" + bubbleCounter;
        bubbleCounter++;
        bubble.setTag(id);
        bubbleMap.put(id, bubble);

        LinearLayout replyPreview = bubble.findViewById(R.id.chat_reply_preview);
        TextView replySender = bubble.findViewById(R.id.reply_sender);
        TextView replyText = bubble.findViewById(R.id.reply_text);
        TextView tvMsg = bubble.findViewById(R.id.chat_bubble_message);
        ImageView imgBubble = bubble.findViewById(R.id.chat_bubble_image);
        TextView tvCaption = bubble.findViewById(R.id.chat_bubble_caption);
        LinearLayout emoteContainer = bubble.findViewById(R.id.chat_bubble_emotes);
        TextView tvTime = bubble.findViewById(R.id.chat_bubble_time);

        if (image != null) {
            imgBubble.setImageBitmap(image);
            imgBubble.setVisibility(View.VISIBLE);

            if (text != null) {
                tvCaption.setVisibility(View.VISIBLE);
                tvCaption.setText(text);
            } else {
                tvCaption.setVisibility(View.GONE);
            }

            tvMsg.setVisibility(View.GONE);

        } else {
            imgBubble.setVisibility(View.GONE);

            tvCaption.setVisibility(View.GONE);

            if (text != null) {
                tvMsg.setVisibility(View.VISIBLE);
                tvMsg.setText(text);
            } else {
                tvMsg.setVisibility(View.GONE);
            }
        }

        if (replyToId != null) {
            View target = bubbleMap.get(replyToId);
            replyPreview.setVisibility(View.VISIBLE);
            replySender.setText("Admin");

            String preview = "";

            if (target != null) {
                TextView targetMsg = target.findViewById(R.id.chat_bubble_message);
                TextView targetCap = target.findViewById(R.id.chat_bubble_caption);

                if (targetMsg != null && targetMsg.getVisibility() == View.VISIBLE)
                    preview = targetMsg.getText().toString();
                else if (targetCap != null && targetCap.getVisibility() == View.VISIBLE)
                    preview = targetCap.getText().toString();
                else
                    preview = "[Foto]";

            } else {
                preview = "[Pesan tidak ditemukan]";
            }

            replyText.setText(preview.length() > 120 ? preview.substring(0, 120) + "…" : preview);

            replyPreview.setTag(replyToId);
            replyPreview.setOnClickListener(v -> {
                Object t = replyPreview.getTag();
                if (t instanceof String) scrollToBubble((String) t);
            });

        } else {
            replyPreview.setVisibility(View.GONE);
        }

        tvTime.setText(getTime());

        setupLongPressMenu(bubble);

        bubble.findViewById(R.id.chat_bubble_message).setOnClickListener(v -> {
            if (replyPreview.getVisibility() == View.VISIBLE) {
                Object t = replyPreview.getTag();
                if (t instanceof String) scrollToBubble((String) t);
            }
        });

        chatList.addView(bubble);

        scrollToBottom();
    }

    private void setupLongPressMenu(View bubble) {

        bubble.setOnLongClickListener(v -> {
            PopupMenu menu = new PopupMenu(this, v, Gravity.END);
            menu.getMenu().add("Reply");
            menu.getMenu().add("Emote");
            menu.getMenu().add("Edit");
            menu.getMenu().add("Delete");

            menu.setOnMenuItemClickListener(item -> {

                String title = item.getTitle().toString();

                if (title.equals("Reply")) {
                    TextView original = v.findViewById(R.id.chat_bubble_message);
                    TextView caption = v.findViewById(R.id.chat_bubble_caption);
                    ImageView img = v.findViewById(R.id.chat_bubble_image);

                    String orig = "";

                    if (original != null && original.getVisibility() == View.VISIBLE)
                        orig = original.getText().toString();
                    else if (caption != null && caption.getVisibility() == View.VISIBLE)
                        orig = caption.getText().toString();
                    else if (img != null && img.getVisibility() == View.VISIBLE)
                        orig = "[Foto]";

                    pendingReplyTargetId = (String) v.getTag();

                    replyTextView.setText("Admin: " + orig);
                    replyContainer.setVisibility(View.VISIBLE);
                    inputMessage.requestFocus();
                }

                else if (title.equals("Emote")) {
                    showEmoteSelector(v);
                }

                else if (title.equals("Edit")) {
                    TextView tv = v.findViewById(R.id.chat_bubble_message);
                    TextView cap = v.findViewById(R.id.chat_bubble_caption);

                    if (tv != null && tv.getVisibility() == View.VISIBLE) {
                        inputMessage.setText(tv.getText().toString());
                    } else if (cap != null && cap.getVisibility() == View.VISIBLE) {
                        inputMessage.setText(cap.getText().toString());
                    }

                    inputMessage.requestFocus();
                    editingTargetBubble = v;
                    isEditing = true;
                    replyContainer.setVisibility(View.GONE);
                }

                else if (title.equals("Delete")) {
                    chatList.removeView(v);
                    Object tag = v.getTag();
                    if (tag instanceof String) bubbleMap.remove((String) tag);
                }

                return true;
            });

            menu.show();
            return true;
        });
    }

    private void showEmoteSelector(View bubble) {

        String[] emotes = {"👍", "❤️", "😂", "😡", "💔", "🔥", "😭"};

        AlertDialog.Builder b = new AlertDialog.Builder(this);
        b.setTitle("Pilih Emote");

        b.setItems(emotes, (d, which) -> {
            LinearLayout emoteContainer = bubble.findViewById(R.id.chat_bubble_emotes);
            if (emoteContainer == null) return;

            emoteContainer.removeAllViews();

            TextView t = new TextView(this);
            t.setText(emotes[which]);
            t.setTextSize(18);
            t.setPadding(4, 0, 4, 0);

            emoteContainer.addView(t);
            emoteContainer.setVisibility(View.VISIBLE);
        });

        b.show();
    }

    private void scrollToBubble(String id) {
        View target = bubbleMap.get(id);
        if (target == null) return;

        scrollView.post(() -> {

            scrollView.smoothScrollTo(0, target.getTop());

            if (lastHighlightedBubble != null) {
                lastHighlightedBubble.setBackgroundResource(R.drawable.custom_chat_admin_forum);
            }

            lastHighlightedBubble = target;

            target.setBackgroundColor(Color.parseColor("#33FFFF00"));

            target.postDelayed(() -> {
                target.setBackgroundResource(R.drawable.custom_chat_admin_forum);
                lastHighlightedBubble = null;
            }, 800);
        });
    }

    private void scrollToBottom() {
        scrollView.post(() -> scrollView.fullScroll(View.FOCUS_DOWN));
    }

    private String getTime() {
        return new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());
    }

    private void clearPendingReply() {
        pendingReplyTargetId = null;
        replyTextView.setText("");
        replyContainer.setVisibility(View.GONE);
    }
}
