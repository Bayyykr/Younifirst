package com.naufal.younifirst.LognReg;

import android.os.Bundle;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.naufal.younifirst.R;
import com.naufal.younifirst.opening.ShownHidePwKt;

public class login extends AppCompatActivity {
    private EditText etpassword;
//    private  EditText etpasswordd;

    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.page_login);

        etpassword = findViewById(R.id.pwlogin);
//        etpasswordd = findViewById(R.id.KataSandi1);

        ShownHidePwKt.setupPasswordToggle(etpassword, R.drawable.open_eye, R.drawable.close_eye);
//        ShownHidePwKt.setupPasswordToggle(etpasswordd, R.drawable.open_eye, R.drawable.close_eye);
    }
//    private  void showPopUp () {
//        Dialog dialog = new Dialog(this);
//        dialog.setContentView(R.layout.popup_gagal);
//
//        dialog.getWindow().setLayout(
//                android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
//                android.view.ViewGroup.LayoutParams.WRAP_CONTENT
//        );
//        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
//
//        Button bTryAgain = dialog.findViewById(R.id.Btryagain);
//
//        bTryAgain.setOnClickListener(v -> {
//            dialog.dismiss();
//            showLayoutOne();
//        });
//        dialog.show();
//    }
}
