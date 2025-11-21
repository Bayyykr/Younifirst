package com.naufal.younifirst.Forum;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.naufal.younifirst.R;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_forum_buat_forum);

        Button btnBuatForum = findViewById(R.id.btnBuatForum);

        btnBuatForum.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, BerhasilBuatForumActivity.class);
            startActivity(intent);
        });
    }
}
