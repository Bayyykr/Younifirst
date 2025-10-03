package com.naufal.younifirst.LoginDanRegister;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

import com.naufal.younifirst.R;
import com.naufal.younifirst.opening.ShownHidePwKt;

public class login  extends AppCompatActivity {
    private Button btn_masuk;
    private ImageButton btn_next_first;
    private ImageButton btn_next_second;
    private ImageButton btn_next_third;
    private Button btn_lewati;
    private EditText etpassword;

    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.page_login);

        etpassword = findViewById(R.id.etPassword);

        ShownHidePwKt.setupPasswordToggle(etpassword, R.drawable.open_eye, R.drawable.close_eye);

//        View view_one = getLayoutInflater().inflate(R.layout.opening_app_two, null);
//        View view_two = getLayoutInflater().inflate(R.layout.opening_app_three, null);
//        View view_three = getLayoutInflater().inflate(R.layout.opening_app_four, null);

//        btn_masuk = findViewById(R.id.btn_mulai);
//        btn_next_first = view_one.findViewById(R.id.first_next);
//        btn_next_second = view_two.findViewById(R.id.next_second);
//        btn_next_third = view_three.findViewById(R.id.next_third);
//        btn_masuk.setOnClickListener(e->go());
//        btn_next_first.setOnClickListener(e->next_one());
//        btn_next_second.setOnClickListener(e->next_two());
//        btn_next_third.setOnClickListener(e->next_three());
    }

    private void go(){
        setContentView(R.layout.opening_app_two);
    }
//
//    private void next_one(){
//        setContentView(R.layout.opening_app_three);
//    }
//
//    private void next_two(){
//        setContentView(R.layout.opening_app_four);
//    }
//
//    private void next_three(){
//        setContentView(R.layout.page_login);
//    }

}
