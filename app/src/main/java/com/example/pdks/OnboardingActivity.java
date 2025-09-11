package com.example.pdks;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;



public class OnboardingActivity extends AppCompatActivity {

    private ImageView imageView;
    private TextView textView;
    private Button nextButton;
    private View indicator1, indicator2, indicator3;
    private int currentPage = 0;


    private static final String PREFS_NAME = "AppPrefs";
    private static final String KEY_FIRST_TIME = "first_time";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_onboarding);

        imageView = findViewById(R.id.imageView);
        textView = findViewById(R.id.textView);
        nextButton = findViewById(R.id.nextButton);
        indicator1 = findViewById(R.id.indicator1);
        indicator2 = findViewById(R.id.indicator2);
        indicator3 = findViewById(R.id.indicator3);

        updateUI();

        nextButton.setOnClickListener(v -> {
            currentPage++;
            if (currentPage > 2) {
                SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean(KEY_FIRST_TIME, false);
                editor.apply();

                Intent intent = new Intent(OnboardingActivity.this, WelcomeActivity.class);
                startActivity(intent);
                finish();
            } else {
                updateUI();
            }
        });
    }

    private void updateUI() {
        switch (currentPage) {
            case 0:
                imageView.setImageResource(R.drawable.tanitim_ekrani_1);
                textView.setText("Giriş ve çıkışı birkaç dokunuşla yap.");
                indicator1.setBackgroundResource(R.drawable.circle_blue);
                indicator2.setBackgroundResource(R.drawable.circle_gray);
                indicator3.setBackgroundResource(R.drawable.circle_gray);

                imageView.setTranslationX(0);
                break;
            case 1:
                imageView.setImageResource(R.drawable.tanitim_ekrani_2);
                textView.setText("Sadece yetkililer konum ve yüz doğrulama ile giriş yapabilir.");
                indicator1.setBackgroundResource(R.drawable.circle_gray);
                indicator2.setBackgroundResource(R.drawable.circle_blue);
                indicator3.setBackgroundResource(R.drawable.circle_gray);

                imageView.setTranslationX(200);
                break;
            case 2:
                imageView.setImageResource(R.drawable.tanitim_ekrani_3);
                textView.setText("Kayıtları görüntüle, haritada konumunu takip et.");
                indicator1.setBackgroundResource(R.drawable.circle_gray);
                indicator2.setBackgroundResource(R.drawable.circle_gray);
                indicator3.setBackgroundResource(R.drawable.circle_blue);

                imageView.setTranslationX(0);
                break;
        }
    }

}
