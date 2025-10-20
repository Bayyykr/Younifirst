package com.naufal.younifirst.Home;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.naufal.younifirst.R;
import com.naufal.younifirst.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        replaceFragment(new HomeFragment());

        binding.bottomNavigationView.setOnItemSelectedListener(item ->{

            switch (item.getItemId()){

                case R.id.nav_home:
                    replaceFragment(new HomeFragment());
                    break;
                case R.id.nav_competition:
                    replaceFragment(new CompetitionTeamFragment());
                    break;
                case R.id.nav_lostfound:
                    replaceFragment(new LostnFoundFragment());
                    break;
                case R.id.nav_event:
                    replaceFragment(new EventFragment());
                    break;
                case R.id.nav_forum:
                    replaceFragment(new ForumFragment());
                    break;
            }

            return true;
        });

    }

    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container,fragment);
        fragmentTransaction.commit();
    }
}
