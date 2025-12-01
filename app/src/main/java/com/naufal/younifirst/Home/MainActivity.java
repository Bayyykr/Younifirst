    package com.naufal.younifirst.Home;

    import android.content.Intent;
    import android.graphics.Color;
    import android.os.Bundle;
    import android.view.View;
    import android.view.animation.AlphaAnimation;
    import android.widget.ImageView;
    import android.widget.LinearLayout;
    import android.content.res.ColorStateList;
    import com.google.android.material.bottomsheet.BottomSheetDialog;
    import android.widget.RadioButton;
    import android.widget.Button;
    import android.widget.Toast;
    import android.view.LayoutInflater;
    import android.widget.TextView;

    import androidx.annotation.Nullable;
    import androidx.appcompat.app.AppCompatActivity;
    import androidx.fragment.app.Fragment;
    import androidx.fragment.app.FragmentManager;
    import androidx.fragment.app.FragmentTransaction;

    import com.google.android.material.floatingactionbutton.FloatingActionButton;
    import com.naufal.younifirst.Event.BuatEvent;
    import com.naufal.younifirst.Forum.BuatForumActivity;
    import com.naufal.younifirst.Event.BuatEvent;
    import com.naufal.younifirst.Kompetisi.PostingLomba;
    import com.naufal.younifirst.R;
    import com.naufal.younifirst.databinding.ActivityMainBinding;

    public class MainActivity extends AppCompatActivity {

        ActivityMainBinding binding;

        private LinearLayout fabMenuHome, fabMenuEvent, fabMenuKompetisi, fabMenuLostFound,fabMenuForum, headerUtama, headerSecond, headerThird;
        private ImageView btnAdd, iconFilter, iconLeftUtama, iconLeftSecond, iconLeftThird;
        private View fadeBackground;
        private boolean isMenuVisible = false;
        private String currentTag = "home";
        private View btnAddContainer;
        private int colorAddDefault;
        private int colorAddActive;
        private FloatingActionButton btnMenemukanFab, btnAddForumFab, btnKehilanganFab, btnPostingEvent, btnPostingLomba;

        @Override
        protected void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            binding = ActivityMainBinding.inflate(getLayoutInflater());
            setContentView(binding.getRoot());

            fabMenuHome = findViewById(R.id.fabMenuHome);
            fabMenuEvent = findViewById(R.id.fabMenuEvent);
            fabMenuKompetisi = findViewById(R.id.fabMenuKompetisi);
            fabMenuLostFound = findViewById(R.id.fabMenuLostFound);
            fabMenuForum = findViewById(R.id.fabMenuForum);
            headerUtama = findViewById(R.id.header_utama);
            headerSecond = findViewById(R.id.header_second);
            headerThird = findViewById(R.id.header_third);
            btnAdd = findViewById(R.id.btnAdd);
            fadeBackground = findViewById(R.id.globalFadeBackground);
            btnAddContainer = findViewById(R.id.btnAddContainer);
            colorAddDefault = android.graphics.Color.parseColor("#3B5CCC");
            colorAddActive = android.graphics.Color.parseColor("#121A2C");
            iconFilter = findViewById(R.id.icon_filter);
            iconFilter.setOnClickListener(v -> showFilterBottomSheet());

            hideAllMenusInstant();

            replaceFragment(new HomeFragment(), "home");
            updateFabMenuState();

            btnAdd.setOnClickListener(v -> toggleMenu());

            fadeBackground.setOnClickListener(v -> hideMenu());

            binding.bottomNavigationView.setOnItemSelectedListener(item -> {
                hideMenu();
                switch (item.getItemId()) {
                    case R.id.nav_home:
                        replaceFragment(new HomeFragment(), "home");
                        break;
                    case R.id.nav_event:
                        replaceFragment(new EventFragment(), "event");
                        break;
                    case R.id.nav_forum:
                        replaceFragment(new ForumFragment(), "forum");
                        break;
                    case R.id.nav_competition:
                        replaceFragment(new CompetitionTeamFragment(), "competition");
                        break;
                    case R.id.nav_lostfound:
                        replaceFragment(new LostnFoundFragment(), "lostfound");
                        break;
                }
                return true;
            });

            fadeBackground.setOnClickListener(v -> {
                if (isMenuVisible) {
                    animateHideFabMenu();
                }
            });

            iconLeftUtama = findViewById(R.id.icon_left_utama);
            iconLeftUtama.setOnClickListener(v -> {
                startActivity(new Intent(MainActivity.this, PengaturanActivity.class));
            });

            iconLeftSecond = findViewById(R.id.icon_left_second);
            iconLeftSecond.setOnClickListener(v -> {
                startActivity(new Intent(MainActivity.this, PengaturanActivity.class));
            });

            iconLeftThird = findViewById(R.id.icon_left_third);
            iconLeftThird.setOnClickListener(v -> {
                startActivity(new Intent(MainActivity.this, PengaturanActivity.class));
            });

            btnMenemukanFab = fabMenuLostFound.findViewById(R.id.btnMenemukan);
            btnMenemukanFab.setOnClickListener(v -> {
                hideMenu();
                startActivity(new Intent(MainActivity.this, PostingFoundActivity.class));
            });

            btnKehilanganFab = fabMenuLostFound.findViewById(R.id.btnKehilangan);
            btnKehilanganFab.setOnClickListener(v -> {
                hideMenu();
                startActivity(new Intent(MainActivity.this, PostingLostActivity.class));
            });

            btnPostingEvent = fabMenuEvent.findViewById(R.id.btnPostingEvent);
            btnPostingEvent.setOnClickListener(v -> {
                hideMenu();
                startActivity(new Intent(MainActivity.this, BuatEvent.class));
            });

            btnPostingLomba = fabMenuKompetisi.findViewById(R.id.btnPostingLomba);
            btnPostingLomba.setOnClickListener(v -> {
                hideMenu();
                startActivity(new Intent(MainActivity.this, PostingLomba.class));
            });

            btnAddForumFab = fabMenuForum.findViewById(R.id.btnPostingForum);
            btnAddForumFab.setOnClickListener(v -> {
                startActivity(new Intent(MainActivity.this, BuatForumActivity.class));
            });

        }

        private void replaceFragment(Fragment fragment, String tag) {
            stopAllAnimations();
            hideAllMenusInstant();
            isMenuVisible = false;

            currentTag = tag;
            FragmentManager fm = getSupportFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();
            ft.replace(R.id.fragment_container, fragment);
            ft.commitNowAllowingStateLoss();

            updateFabMenuState();
            updateHeaderVisibility(tag);
        }

        private void toggleMenu() {
            if (isMenuVisible) {
                animateHideFabMenu();
            } else {
                animateShowFabMenu();
            }
        }

        private void hideMenu() {
            LinearLayout currentMenu = getMenuByTag(currentTag);
            if (currentMenu == null) return;

            AlphaAnimation fadeOut = new AlphaAnimation(1f, 0f);
            fadeOut.setDuration(200);
            fadeBackground.startAnimation(fadeOut);
            currentMenu.startAnimation(fadeOut);

            fadeOut.setAnimationListener(new android.view.animation.Animation.AnimationListener() {
                @Override public void onAnimationStart(android.view.animation.Animation animation) {}
                @Override public void onAnimationRepeat(android.view.animation.Animation animation) {}

                @Override
                public void onAnimationEnd(android.view.animation.Animation animation) {
                    fadeBackground.setVisibility(View.GONE);
                    currentMenu.setVisibility(View.INVISIBLE);
                }
            });

            isMenuVisible = false;
        }

        private LinearLayout getMenuByTag(String tag) {
            switch (tag) {
                case "home":
                    return fabMenuHome;
                case "competition":
                    return fabMenuKompetisi;
                case "event":
                    return fabMenuEvent;
                case "lostfound":
                    return fabMenuLostFound;
                case "forum":
                    return fabMenuForum;
                default:
                    return null;
            }
        }

        private void hideAllMenusInstant() {
            if (fabMenuHome != null) fabMenuHome.setVisibility(View.GONE);
            if (fabMenuEvent != null) fabMenuEvent.setVisibility(View.GONE);
            if (fabMenuKompetisi != null) fabMenuKompetisi.setVisibility(View.GONE);
            if (fabMenuLostFound != null) fabMenuLostFound.setVisibility(View.GONE);
            if (fabMenuForum != null) fabMenuForum.setVisibility(View.GONE);
            if (fadeBackground != null) fadeBackground.setVisibility(View.GONE);
            isMenuVisible = false;
        }

        private void stopAllAnimations() {
            if (fabMenuHome != null) fabMenuHome.clearAnimation();
            if (fabMenuEvent != null) fabMenuEvent.clearAnimation();
            if (fabMenuKompetisi != null) fabMenuKompetisi.clearAnimation();
            if (fabMenuLostFound != null) fabMenuLostFound.clearAnimation();
            if (fabMenuForum != null) fabMenuForum.clearAnimation();
            if (fadeBackground != null) fadeBackground.clearAnimation();
        }

        private void animateShowFabMenu() {
            LinearLayout currentMenu = getMenuByTag(currentTag);
            if (currentMenu == null || isFinishing()) return;

            stopAllAnimations();

            fadeBackground.setVisibility(View.VISIBLE);
            AlphaAnimation fadeIn = new AlphaAnimation(0f, 1f);
            fadeIn.setDuration(200);
            fadeIn.setFillAfter(true);

            fadeBackground.startAnimation(fadeIn);
            currentMenu.setVisibility(View.VISIBLE);
            currentMenu.startAnimation(fadeIn);
            btnAdd.animate().rotation(45f).setDuration(200).start();

            btnAddContainer.setBackgroundTintList(ColorStateList.valueOf(colorAddActive));

            isMenuVisible = true;
        }

        private void animateHideFabMenu() {
            LinearLayout currentMenu = getMenuByTag(currentTag);
            if (currentMenu == null) return;

            AlphaAnimation fadeOut = new AlphaAnimation(1f, 0f);
            fadeOut.setDuration(200);
            fadeBackground.startAnimation(fadeOut);
            currentMenu.startAnimation(fadeOut);

            fadeOut.setAnimationListener(new android.view.animation.Animation.AnimationListener() {
                @Override public void onAnimationStart(android.view.animation.Animation animation) {}
                @Override public void onAnimationRepeat(android.view.animation.Animation animation) {}
                @Override
                public void onAnimationEnd(android.view.animation.Animation animation) {
                    fadeBackground.setVisibility(View.GONE);
                    currentMenu.setVisibility(View.GONE);
                }
            });

            btnAdd.animate().rotation(0f).setDuration(200).start();

            btnAddContainer.setBackgroundTintList(ColorStateList.valueOf(colorAddDefault));

            isMenuVisible = false;
        }

        private void updateFabMenuState() {
            LinearLayout currentMenu = getMenuByTag(currentTag);
            if (currentMenu == null) return;

            hideAllMenusInstant();

            int childCount = currentMenu.getChildCount();

            if (childCount == 1) {
                currentMenu.setVisibility(View.VISIBLE);
                btnAddContainer.setVisibility(View.GONE);

                currentMenu.setTranslationY(55f);
                fadeBackground.setVisibility(View.GONE);
                isMenuVisible = false;
                btnAdd.setRotation(0f);

            } else if (childCount > 1) {
                btnAddContainer.setVisibility(View.VISIBLE);
                currentMenu.setVisibility(View.GONE);
                fadeBackground.setVisibility(View.GONE);
                isMenuVisible = false;
            } else {
                btnAddContainer.setVisibility(View.GONE);
                fadeBackground.setVisibility(View.GONE);
            }
        }
        private void updateHeaderVisibility(String tag) {
            if (headerUtama == null || headerSecond == null || headerThird == null) return;
            headerUtama.setVisibility(View.GONE);
            headerSecond.setVisibility(View.GONE);
            headerThird.setVisibility(View.GONE);
            switch (tag) {
                case "home":
                    headerUtama.setVisibility(View.VISIBLE);
                    break;
                case "event":
                    headerThird.setVisibility(View.VISIBLE);
                    break;
                case "competition":
                    headerThird.setVisibility(View.VISIBLE);
                    break;
                default:
                    headerSecond.setVisibility(View.VISIBLE);
                    break;
            }
        }

        private void showFilterBottomSheet() {
            BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);

            View sheetView = LayoutInflater.from(this).inflate(R.layout.filter_layout, null);

            Button btnTerapkan = sheetView.findViewById(R.id.btnTerapkan);
            btnTerapkan.setOnClickListener(v -> {
                bottomSheetDialog.dismiss();
                Toast.makeText(this, "Filter diterapkan", Toast.LENGTH_SHORT).show();
            });

            TextView btnReset = sheetView.findViewById(R.id.text_reset);
            btnReset.setOnClickListener(v -> {
                ((RadioButton) sheetView.findViewById(R.id.rb_terbaru)).setChecked(false);
                ((RadioButton) sheetView.findViewById(R.id.rb_terlama)).setChecked(false);
                ((RadioButton) sheetView.findViewById(R.id.rb_populer)).setChecked(false);
                ((RadioButton) sheetView.findViewById(R.id.rb_kompetisi)).setChecked(false);
                ((RadioButton) sheetView.findViewById(R.id.rb_lostfound)).setChecked(false);
                ((RadioButton) sheetView.findViewById(R.id.rb_event)).setChecked(false);
                ((RadioButton) sheetView.findViewById(R.id.rb_forum)).setChecked(false);
                Toast.makeText(this, "Filter direset", Toast.LENGTH_SHORT).show();
            });

            bottomSheetDialog.setContentView(sheetView);
            View parent = (View) sheetView.getParent();
            parent.setBackgroundColor(Color.TRANSPARENT);
            bottomSheetDialog.show();
        }

        @Override
        protected void onResume() {
            super.onResume();
            btnAdd.animate().rotation(0f).setDuration(0).start();
            btnAddContainer.setBackgroundTintList(ColorStateList.valueOf(colorAddDefault));
            hideAllMenusInstant();
            updateFabMenuState();
        }

    }
