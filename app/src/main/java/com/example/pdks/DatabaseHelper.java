package com.example.pdks;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "PDKS.db";
    public static final int DATABASE_VERSION = 6;

    private static final String SQL_CREATE_KULLANICILAR =
            "CREATE TABLE " + DatabaseContract.KullaniciEntry.TABLE_NAME + " (" +
                    DatabaseContract.KullaniciEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    DatabaseContract.KullaniciEntry.COLUMN_NAME_ENTITY_ID + " TEXT UNIQUE," +
                    DatabaseContract.KullaniciEntry.COLUMN_NAME_AD + " TEXT," +
                    DatabaseContract.KullaniciEntry.COLUMN_NAME_SOYAD + " TEXT," +
                    DatabaseContract.KullaniciEntry.COLUMN_NAME_SIFRE + " TEXT," +
                    DatabaseContract.KullaniciEntry.COLUMN_NAME_EMAIL + " TEXT UNIQUE," +
                    DatabaseContract.KullaniciEntry.COLUMN_NAME_RESET_TOKEN + " TEXT," +
                    DatabaseContract.KullaniciEntry.COLUMN_NAME_TOKEN_TIME + " INTEGER)";

    private static final String SQL_CREATE_KAYITLAR =
            "CREATE TABLE " + DatabaseContract.KayitEntry.TABLE_NAME + " (" +
                    DatabaseContract.KayitEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    DatabaseContract.KayitEntry.COLUMN_NAME_ENTITY_ID + " TEXT UNIQUE," +
                    DatabaseContract.KayitEntry.COLUMN_NAME_BLOK_ADI + " TEXT," +
                    DatabaseContract.KayitEntry.COLUMN_NAME_TARIH + " TEXT," +
                    DatabaseContract.KayitEntry.COLUMN_NAME_SAAT + " TEXT," +
                    DatabaseContract.KayitEntry.COLUMN_NAME_TIP + " TEXT," +
                    DatabaseContract.KayitEntry.COLUMN_NAME_PHOTO + " BLOB)";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_KULLANICILAR);
        db.execSQL(SQL_CREATE_KAYITLAR);

        db.execSQL("INSERT INTO " + DatabaseContract.KullaniciEntry.TABLE_NAME +
                " (entity_id, ad, soyad, sifre, email) VALUES ('4001','İlayda','Karaçuka','sifre1','ilaydakaracuka1@gmail.com')");
        db.execSQL("INSERT INTO " + DatabaseContract.KullaniciEntry.TABLE_NAME +
                " (entity_id, ad, soyad, sifre, email) VALUES ('4002','Berna','Batı','sifre2','berna@example.com')");
        db.execSQL("INSERT INTO " + DatabaseContract.KullaniciEntry.TABLE_NAME +
                " (entity_id, ad, soyad, sifre, email) VALUES ('4003','Nazmiye','Özuygunlar','sifre3','nazmiye@example.com')");
        db.execSQL("INSERT INTO " + DatabaseContract.KullaniciEntry.TABLE_NAME +
                " (entity_id, ad, soyad, sifre, email) VALUES ('4004','Büşra','İpek','sifre4','busra@example.com')");
        db.execSQL("INSERT INTO " + DatabaseContract.KullaniciEntry.TABLE_NAME +
                " (entity_id, ad, soyad, sifre, email) VALUES ('4005','Yasemin','Özçelik','sifre5','yasemin@example.com')");
        db.execSQL("INSERT INTO " + DatabaseContract.KullaniciEntry.TABLE_NAME +
                " (entity_id, ad, soyad, sifre, email) VALUES ('4006','Ebru','Gültekin','sifre6','ebru@example.com')");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 6) {
            try {
                db.execSQL("ALTER TABLE " + DatabaseContract.KullaniciEntry.TABLE_NAME +
                        " ADD COLUMN " + DatabaseContract.KullaniciEntry.COLUMN_NAME_RESET_TOKEN + " TEXT");
            } catch (Exception e) { e.printStackTrace(); }

            try {
                db.execSQL("ALTER TABLE " + DatabaseContract.KullaniciEntry.TABLE_NAME +
                        " ADD COLUMN " + DatabaseContract.KullaniciEntry.COLUMN_NAME_TOKEN_TIME + " INTEGER");
            } catch (Exception e) { e.printStackTrace(); }
        }
    }
}
