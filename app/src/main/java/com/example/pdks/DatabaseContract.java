package com.example.pdks;

import android.provider.BaseColumns;

public final class DatabaseContract {
    private DatabaseContract() {}

    public static class KullaniciEntry implements BaseColumns {
        public static final String TABLE_NAME = "kullanicilar";
        public static final String COLUMN_NAME_ENTITY_ID = "entity_id";
        public static final String COLUMN_NAME_AD = "ad";
        public static final String COLUMN_NAME_SOYAD = "soyad";
        public static final String COLUMN_NAME_SIFRE = "sifre";
        public static final String COLUMN_NAME_EMAIL = "email";
        public static final String COLUMN_NAME_RESET_TOKEN = "reset_token";
        public static final String COLUMN_NAME_TOKEN_TIME = "token_time";
    }

    public static class KayitEntry implements BaseColumns {
        public static final String TABLE_NAME = "kayitlar";
        public static final String COLUMN_NAME_ENTITY_ID = "entity_id";
        public static final String COLUMN_NAME_BLOK_ADI = "blok_adi";
        public static final String COLUMN_NAME_TARIH = "tarih";
        public static final String COLUMN_NAME_SAAT = "saat";
        public static final String COLUMN_NAME_TIP = "tip";
        public static final String COLUMN_NAME_PHOTO = "photo";



    }
}
