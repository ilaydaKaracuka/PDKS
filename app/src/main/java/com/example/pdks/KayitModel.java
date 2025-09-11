package com.example.pdks;

import android.graphics.Bitmap;

public class KayitModel {
    private String blokAdi;
    private String tip;
    private String tarih;
    private String saat;
    private Bitmap userImage;

    public KayitModel(String blokAdi, String tip, String tarih, String saat) {
        this.blokAdi = blokAdi;
        this.tip = tip;
        this.tarih = tarih;
        this.saat = saat;
    }

    public String getBlokAdi() { return blokAdi; }
    public String getTip() { return tip; }
    public String getTarih() { return tarih; }
    public String getSaat() { return saat; }

    public void setUserImage(Bitmap bitmap) { this.userImage = bitmap; }
    public Bitmap getUserImage() { return userImage; }
}
