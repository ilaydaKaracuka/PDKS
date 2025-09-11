package com.example.pdks;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Random;

public class VerifyMailActivity extends AppCompatActivity {

    EditText editTextEnterEmail;
    Button verify_continue;
    DatabaseHelper dbHelper;
    ImageButton backLoginBtn;
    String generatedCode;
    String currentEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_mail);

        dbHelper = new DatabaseHelper(this);
        editTextEnterEmail = findViewById(R.id.editTextEnterEmail);
        verify_continue = findViewById(R.id.verify_continue);
        backLoginBtn = findViewById(R.id.backLoginBtn);

        verify_continue.setOnClickListener(v -> {
            String email = editTextEnterEmail.getText().toString().trim();
            if(email.isEmpty()){
                Toast.makeText(this, "Email boş olamaz!", Toast.LENGTH_SHORT).show();
                return;
            }

            SQLiteDatabase db = dbHelper.getWritableDatabase();
            Cursor cursor = db.query(DatabaseContract.KullaniciEntry.TABLE_NAME,
                    null,
                    DatabaseContract.KullaniciEntry.COLUMN_NAME_EMAIL + "=?",
                    new String[]{email},
                    null,null,null);

            if(cursor.moveToFirst()){
                currentEmail = email;
                generatedCode = String.format("%06d", new Random().nextInt(999999));

                String htmlBody = "<p>Doğrulama kodunuz: <b>" + generatedCode + "</b></p>";
                new SendMailTask(this, email, "Doğrulama Kodu", htmlBody).execute();

                showVerificationCard();

            } else {
                Toast.makeText(this, "Kayıtlı email bulunamadı!", Toast.LENGTH_SHORT).show();
            }

            cursor.close();
            db.close();
        });

        backLoginBtn.setOnClickListener(v -> {
            Intent intent = new Intent(VerifyMailActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });

    }

    private void showVerificationCard() {
        FrameLayout rootLayout = findViewById(android.R.id.content);
        View cardView = LayoutInflater.from(this).inflate(R.layout.enter_number_card, null);

        EditText codeInput = cardView.findViewById(R.id.editText_verification_code);
        Button exitButton = cardView.findViewById(R.id.button_exit);

        exitButton.setOnClickListener(v -> {
            String enteredCode = codeInput.getText().toString().trim();
            if(enteredCode.equals(generatedCode)){

                Intent intent = new Intent(VerifyMailActivity.this, ChangePasswordActivity.class);
                intent.putExtra("email", currentEmail);
                startActivity(intent);
            } else {
                Toast.makeText(this, "Kod yanlış, lütfen tekrar deneyin!", Toast.LENGTH_SHORT).show();
            }
        });

        rootLayout.addView(cardView);
    }

    private static class SendMailTask extends AsyncTask<Void, Void, Boolean> {
        private final String toEmail, subject, body;
        private final AppCompatActivity activity;

        public SendMailTask(AppCompatActivity activity, String toEmail, String subject, String body) {
            this.activity = activity;
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
            if(success){
                Toast.makeText(activity, "Mail başarıyla gönderildi!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(activity, "Mail gönderilemedi!", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
