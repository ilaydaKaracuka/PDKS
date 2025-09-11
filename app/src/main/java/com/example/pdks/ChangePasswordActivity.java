package com.example.pdks;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;

public class ChangePasswordActivity extends AppCompatActivity {

    TextInputEditText newPassword, confirmPassword;
    Button changePasswordButton;
    DatabaseHelper dbHelper;
    String email;
    ImageButton backButton ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        newPassword = findViewById(R.id.textInputNewPassword);
        confirmPassword = findViewById(R.id.textInputConfirmPassword);
        changePasswordButton = findViewById(R.id.changePasswordButton);
        dbHelper = new DatabaseHelper(this);
        backButton = findViewById(R.id.btnBack2);
        email = getIntent().getStringExtra("email");

        changePasswordButton.setOnClickListener(v -> {
            String pass1 = newPassword.getText().toString().trim();
            String pass2 = confirmPassword.getText().toString().trim();

            if (pass1.isEmpty() || pass2.isEmpty()) {
                Toast.makeText(this, "Tüm alanları doldurun!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!pass1.equals(pass2)) {
                Toast.makeText(this, "Şifreler eşleşmiyor!", Toast.LENGTH_SHORT).show();
                return;
            }

            SQLiteDatabase db = dbHelper.getWritableDatabase();

            Cursor cursor = db.query(
                    DatabaseContract.KullaniciEntry.TABLE_NAME,
                    new String[]{DatabaseContract.KullaniciEntry.COLUMN_NAME_SIFRE},
                    DatabaseContract.KullaniciEntry.COLUMN_NAME_EMAIL + "=?",
                    new String[]{email},
                    null, null, null
            );

            if (cursor.moveToFirst()) {
                String currentPassword = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.KullaniciEntry.COLUMN_NAME_SIFRE));

                if (pass1.equals(currentPassword)) {
                    Toast.makeText(this, "Yeni şifre mevcut şifre ile aynı olamaz!", Toast.LENGTH_SHORT).show();
                    cursor.close();
                    db.close();
                    return;
                }
            }

            cursor.close();

            ContentValues cv = new ContentValues();
            cv.put(DatabaseContract.KullaniciEntry.COLUMN_NAME_SIFRE, pass1);

            int rows = db.update(
                    DatabaseContract.KullaniciEntry.TABLE_NAME,
                    cv,
                    DatabaseContract.KullaniciEntry.COLUMN_NAME_EMAIL + "=?",
                    new String[]{email}
            );

            db.close();

            if (rows > 0) {
                Toast.makeText(this, "Şifre başarıyla güncellendi!", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(ChangePasswordActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            } else {
                Toast.makeText(this, "Şifre güncellenemedi, email bulunamadı!", Toast.LENGTH_SHORT).show();
            }
        });
        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(ChangePasswordActivity.this, VerifyMailActivity.class);
            startActivity(intent);
            finish();
        });
    }
}
