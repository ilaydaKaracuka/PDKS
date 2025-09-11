package com.example.pdks;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

import java.util.UUID;

public class RegisterActivity extends AppCompatActivity {

    private EditText adEditText, soyadEditText, emailEditText, sifreEditText;
    private DatabaseHelper dbHelper;
    TextView girisYapText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        dbHelper = new DatabaseHelper(this);

        adEditText = findViewById(R.id.id_ad);
        soyadEditText = findViewById(R.id.id_soyad);
        emailEditText = findViewById(R.id.id_email);
        sifreEditText = findViewById(R.id.id_sifre);

        MaterialButton kayitButon = findViewById(R.id.id_kayıtButon);
        kayitButon.setOnClickListener(v -> kayitOl());
       girisYapText = findViewById(R.id.id_girisYap);
        girisYapText.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intent);
        });
    }

    private void kayitOl() {
        String ad = adEditText.getText().toString().trim();
        String soyad = soyadEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String sifre = sifreEditText.getText().toString().trim();

        if (ad.isEmpty() || soyad.isEmpty() || email.isEmpty() || sifre.isEmpty()) {
            Toast.makeText(this, "Lütfen tüm alanları doldurun", Toast.LENGTH_SHORT).show();
            return;
        }

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = "SELECT * FROM " + DatabaseContract.KullaniciEntry.TABLE_NAME +
                " WHERE " + DatabaseContract.KullaniciEntry.COLUMN_NAME_AD + " = ?" +
                " AND " + DatabaseContract.KullaniciEntry.COLUMN_NAME_SOYAD + " = ?" +
                " AND " + DatabaseContract.KullaniciEntry.COLUMN_NAME_EMAIL + " = ?";
        String[] selectionArgs = { ad, soyad, email };
        Cursor cursor = db.rawQuery(query, selectionArgs);

        if (cursor.getCount() > 0) {
            Toast.makeText(this, "Bu kullanıcı zaten mevcut", Toast.LENGTH_SHORT).show();
            cursor.close();
            return;
        }
        cursor.close();

        String entityId = UUID.randomUUID().toString();
        ContentValues values = new ContentValues();
        values.put(DatabaseContract.KullaniciEntry.COLUMN_NAME_ENTITY_ID, entityId);
        values.put(DatabaseContract.KullaniciEntry.COLUMN_NAME_AD, ad);
        values.put(DatabaseContract.KullaniciEntry.COLUMN_NAME_SOYAD, soyad);
        values.put(DatabaseContract.KullaniciEntry.COLUMN_NAME_EMAIL, email);
        values.put(DatabaseContract.KullaniciEntry.COLUMN_NAME_SIFRE, sifre);

        long newRowId = dbHelper.getWritableDatabase()
                .insert(DatabaseContract.KullaniciEntry.TABLE_NAME, null, values);

        if (newRowId != -1) {
            Toast.makeText(this, "Kayıt başarılı", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Kayıt sırasında bir hata oluştu", Toast.LENGTH_SHORT).show();
        }
    }

}
