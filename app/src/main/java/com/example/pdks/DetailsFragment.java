package com.example.pdks;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class DetailsFragment extends Fragment {

    private static final String ARG_DATE = "date";
    private static final String ARG_ENTITY_ID = "entity_id";

    private String selectedDate;
    private String entityId;
    private RecyclerView recyclerView;
    private TextView headerDate;

    public static DetailsFragment newInstance(String entityId, String date) {
        DetailsFragment fragment = new DetailsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_ENTITY_ID, entityId);
        args.putString(ARG_DATE, date);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            entityId = getArguments().getString(ARG_ENTITY_ID);
            selectedDate = getArguments().getString(ARG_DATE);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_details, container, false);

        headerDate = view.findViewById(R.id.header_date);
        if (selectedDate != null) {
            headerDate.setText(selectedDate);
        }

        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        List<KayitModel> kayitList = getKayitlarFromDb();
        DetailsAdapter adapter = new DetailsAdapter(kayitList);
        recyclerView.setAdapter(adapter);

        return view;
    }

    private List<KayitModel> getKayitlarFromDb() {
        List<KayitModel> list = new ArrayList<>();
        DatabaseHelper dbHelper = new DatabaseHelper(getContext());
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String[] projection = {
                DatabaseContract.KayitEntry.COLUMN_NAME_BLOK_ADI,
                DatabaseContract.KayitEntry.COLUMN_NAME_TIP,
                DatabaseContract.KayitEntry.COLUMN_NAME_TARIH,
                DatabaseContract.KayitEntry.COLUMN_NAME_SAAT,
                DatabaseContract.KayitEntry.COLUMN_NAME_PHOTO
        };

        String selection = DatabaseContract.KayitEntry.COLUMN_NAME_ENTITY_ID + " = ?";
        List<String> selectionArgsList = new ArrayList<>();
        selectionArgsList.add(entityId);

        if (selectedDate != null) {
            selection += " AND " + DatabaseContract.KayitEntry.COLUMN_NAME_TARIH + " = ?";
            selectionArgsList.add(selectedDate);
        }

        String[] selectionArgs = selectionArgsList.toArray(new String[0]);

        Cursor cursor = db.query(
                DatabaseContract.KayitEntry.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                DatabaseContract.KayitEntry.COLUMN_NAME_TARIH + " DESC, " +
                        DatabaseContract.KayitEntry.COLUMN_NAME_SAAT + " DESC"
        );

        while (cursor.moveToNext()) {
            String blok = cursor.getString(cursor.getColumnIndexOrThrow(
                    DatabaseContract.KayitEntry.COLUMN_NAME_BLOK_ADI));
            String tip = cursor.getString(cursor.getColumnIndexOrThrow(
                    DatabaseContract.KayitEntry.COLUMN_NAME_TIP));
            String tarih = cursor.getString(cursor.getColumnIndexOrThrow(
                    DatabaseContract.KayitEntry.COLUMN_NAME_TARIH));
            String saat = cursor.getString(cursor.getColumnIndexOrThrow(
                    DatabaseContract.KayitEntry.COLUMN_NAME_SAAT));

            byte[] photoBytes = cursor.getBlob(cursor.getColumnIndexOrThrow(
                    DatabaseContract.KayitEntry.COLUMN_NAME_PHOTO));
            Bitmap userImage = ImageStorage.getBitmap(photoBytes);

            KayitModel kayit = new KayitModel(blok, tip, tarih, saat);
            if (userImage != null) kayit.setUserImage(userImage);

            list.add(kayit);
        }

        cursor.close();
        db.close();

        return list;
    }
}
