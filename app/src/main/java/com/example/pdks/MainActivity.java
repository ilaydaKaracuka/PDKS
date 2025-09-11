package com.example.pdks;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.navigation.NavigationView;

public class MainActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ImageView btnMenu;
    private String entityId;
    private String ad;
    private String soyad;
    private static final int CAMERA_LOGOUT_REQUEST = 200;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        btnMenu = findViewById(R.id.btn_menu);

        btnMenu.setOnClickListener(view -> drawerLayout.openDrawer(GravityCompat.START));
        navigationView.setItemIconTintList(null);

        entityId = getIntent().getStringExtra("ENTITY_ID");
        ad = getIntent().getStringExtra("AD");
        soyad = getIntent().getStringExtra("SOYAD");

        View headerView = navigationView.getHeaderView(0);
        TextView tvAd = headerView.findViewById(R.id.header_text1);
        TextView tvSoyad = headerView.findViewById(R.id.header_text2);

        if (ad != null) tvAd.setText(ad);
        if (soyad != null) tvSoyad.setText(soyad);

        navigationView.getMenu().findItem(R.id.nav_home).setChecked(true);
        replaceFragment(new MainFragment());

        navigationView.setNavigationItemSelectedListener(item -> {
            drawerLayout.closeDrawer(GravityCompat.START);

            for (int i = 0; i < navigationView.getMenu().size(); i++) {
                navigationView.getMenu().getItem(i).setChecked(false);
            }
            item.setChecked(true);

            int id = item.getItemId();
            if (id == R.id.nav_home) replaceFragment(new MainFragment());
            else if (id == R.id.nav_settings) replaceFragment(new SettingsFragment());
            else if (id == R.id.nav_logout)  showLogoutDialog();


            return true;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        View headerView = navigationView.getHeaderView(0);
        ImageView ivHeader = headerView.findViewById(R.id.header_image);

        Bitmap bitmap = ImageStorage.getBitmap();
        if (bitmap != null) {
            ivHeader.setImageBitmap(bitmap);

        }
    }

    public void replaceFragment(Fragment fragment) {
        Bundle args = fragment.getArguments();
        if (args == null) args = new Bundle();

        args.putString("ENTITY_ID", entityId);
        fragment.setArguments(args);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }


    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
    private void logout() {


        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);

        finish();
    }
    private void showLogoutDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.logout_box, null);
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        Button btnCancel = dialogView.findViewById(R.id.button_iptal);
        Button btnLogout = dialogView.findViewById(R.id.button_log_out);

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnLogout.setOnClickListener(v -> {
            logout();
            dialog.dismiss();
        });

        dialog.show();
    }
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_LOGOUT_REQUEST && resultCode == RESULT_OK) {
            logout();
        } else if (requestCode == CAMERA_LOGOUT_REQUEST) {
            Toast.makeText(this, "İnsan doğrulaması başarısız! Çıkış yapılmadı.", Toast.LENGTH_SHORT).show();
        }
    }

}
