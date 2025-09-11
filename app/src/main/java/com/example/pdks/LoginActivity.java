package com.example.pdks;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.Random;

public class LoginActivity extends AppCompatActivity {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 100;

    private EditText etEntityId, etSifre;
    private Button btnLogin;
    private TextView sifreDegistir;
    private DatabaseHelper dbHelper;

    private String generatedCode;
    private String userEmail, userAd, userSoyad, userEntityId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!checkLocationPermission()) {
            requestLocationPermission();
        } else {
            initViewsAndLogin();
        }
    }

    private void initViewsAndLogin() {
        setContentView(R.layout.activity_login);

        etEntityId = findViewById(R.id.id_kullanici);
        etSifre = findViewById(R.id.id_sifre2);
        btnLogin = findViewById(R.id.id_girişButon);
        sifreDegistir = findViewById(R.id.id_sifreDegistir);

        dbHelper = new DatabaseHelper(this);

        btnLogin.setOnClickListener(v -> attemptLogin());

        etSifre.setOnEditorActionListener((TextView v, int actionId, KeyEvent event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE ||
                    (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN)) {
                attemptLogin();
                return true;
            }
            return false;
        });

        sifreDegistir.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, VerifyMailActivity.class);
            startActivity(intent);
        });
    }

    private boolean checkLocationPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                LOCATION_PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initViewsAndLogin();
            } else {
                Toast.makeText(this, "Konum izni olmadan uygulama kullanılamaz!", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    private void attemptLogin() {
        String input = etEntityId.getText().toString().trim();
        String sifre = etSifre.getText().toString().trim();

        if (input.isEmpty() || sifre.isEmpty()) {
            Toast.makeText(this, "Lütfen tüm alanları doldurun", Toast.LENGTH_SHORT).show();
            return;
        }

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor;

        boolean isEmail = input.contains("@");

        if (isEmail) {
            cursor = db.query(
                    DatabaseContract.KullaniciEntry.TABLE_NAME,
                    null,
                    DatabaseContract.KullaniciEntry.COLUMN_NAME_EMAIL + "=? AND " +
                            DatabaseContract.KullaniciEntry.COLUMN_NAME_SIFRE + "=?",
                    new String[]{input, sifre},
                    null, null, null
            );
        } else {
            cursor = db.query(
                    DatabaseContract.KullaniciEntry.TABLE_NAME,
                    null,
                    DatabaseContract.KullaniciEntry.COLUMN_NAME_ENTITY_ID + "=? AND " +
                            DatabaseContract.KullaniciEntry.COLUMN_NAME_SIFRE + "=?",
                    new String[]{input, sifre},
                    null, null, null
            );
        }

        if (cursor != null && cursor.moveToFirst()) {
            userEntityId = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.KullaniciEntry.COLUMN_NAME_ENTITY_ID));
            userAd = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.KullaniciEntry.COLUMN_NAME_AD));
            userSoyad = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.KullaniciEntry.COLUMN_NAME_SOYAD));
            userEmail = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.KullaniciEntry.COLUMN_NAME_EMAIL));

            cursor.close();
            db.close();

            generatedCode = String.format("%06d", new Random().nextInt(999999));
            String htmlBody = "<p>Doğrulama kodunuz: <b>" + generatedCode + "</b></p>";
            new SendMailTask(userEmail, "Doğrulama Kodu", htmlBody).execute();

            showVerificationCard();

        } else {
            if (isEmail) {
                Toast.makeText(this, "Hatalı E-Posta veya Şifre", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Hatalı ID veya Şifre", Toast.LENGTH_SHORT).show();
            }

            if (cursor != null) cursor.close();
            db.close();
        }
    }

    private void showVerificationCard() {
        FrameLayout rootLayout = findViewById(android.R.id.content);
        View cardView = LayoutInflater.from(this).inflate(R.layout.enter_number_card, null);

        EditText codeInput = cardView.findViewById(R.id.editText_verification_code);
        Button exitButton = cardView.findViewById(R.id.button_exit);

        exitButton.setOnClickListener(v -> {
            String enteredCode = codeInput.getText().toString().trim();
            if (enteredCode.equals(generatedCode)) {
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                intent.putExtra("ENTITY_ID", userEntityId);
                intent.putExtra("AD", userAd);
                intent.putExtra("SOYAD", userSoyad);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(this, "Kod yanlış, lütfen tekrar deneyin!", Toast.LENGTH_SHORT).show();
            }
        });

        rootLayout.addView(cardView);
    }

    private static class SendMailTask extends AsyncTask<Void, Void, Boolean> {
        private final String toEmail, subject, body;

        public SendMailTask(String toEmail, String subject, String body) {
            this.toEmail = toEmail;
            this.subject = subject;
            this.body = body;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                MailSender.sendMail(toEmail, subject, body);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
        }
    }
}
