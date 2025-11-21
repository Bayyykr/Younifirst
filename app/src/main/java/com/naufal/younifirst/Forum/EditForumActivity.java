package com.naufal.younifirst.Forum;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import com.naufal.younifirst.R;

public class EditForumActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_forum_edit_forumm);

        findViewById(R.id.back_to_mainactivity).setOnClickListener(v -> {
            finish();
        });
    }
}
