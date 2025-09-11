package com.example.pdks;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.fragment.app.Fragment;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class MainFragment extends Fragment {

    private CalendarView calendarView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_main, container, false);

        calendarView = root.findViewById(R.id.calendarView);
        Button btnGiris = root.findViewById(R.id.btnGiris);
        Button btnCikis = root.findViewById(R.id.btnCikis);

        btnGiris.setOnClickListener(v -> openMapFragment("GIRIS"));
        btnCikis.setOnClickListener(v -> openMapFragment("CIKIS"));
        requireActivity().getOnBackPressedDispatcher().addCallback(
                getViewLifecycleOwner(),
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                    }
                }
        );

        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            Calendar selectedCal = Calendar.getInstance();
            selectedCal.set(year, month, dayOfMonth);

            Calendar today = Calendar.getInstance();
            Calendar oneMonthAgo = Calendar.getInstance();
            oneMonthAgo.add(Calendar.MONTH, -1);

            if (selectedCal.after(today)) {
                Toast.makeText(getContext(), "Gelecek tarih seçilemez.", Toast.LENGTH_LONG).show();
                return;
            }

            if (selectedCal.before(oneMonthAgo)) {
                Toast.makeText(getContext(), "Sadece son 1 ay içindeki geçmiş tarihler seçilebilir.", Toast.LENGTH_LONG).show();
                return;
            }

            String selectedDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    .format(selectedCal.getTime());

            if (!checkRecordForDate(selectedDate)) {
                Toast.makeText(getContext(), "Seçilen tarihe ait kayıt bulunamadı.", Toast.LENGTH_LONG).show();
                return;
            }

            String entityId = getArguments() != null ? getArguments().getString("ENTITY_ID") : null;
            DetailsFragment detailsFragment = DetailsFragment.newInstance(entityId, selectedDate);

            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).replaceFragment(detailsFragment);
            }
        });

        return root;
    }

    private void openMapFragment(String type) {
        MapFragment mapFragment = new MapFragment();
        Bundle bundle = new Bundle();
        bundle.putString("type", type);

        String entityId = getArguments() != null ? getArguments().getString("ENTITY_ID") : null;
        if (entityId != null) bundle.putString("ENTITY_ID", entityId);

        mapFragment.setArguments(bundle);

        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).replaceFragment(mapFragment);
        }
    }

    private boolean checkRecordForDate(String date) {
        String entityId = getArguments() != null ? getArguments().getString("ENTITY_ID") : null;
        if (entityId == null) return false;

        DatabaseHelper dbHelper = new DatabaseHelper(getContext());
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String selection = DatabaseContract.KayitEntry.COLUMN_NAME_ENTITY_ID + " = ? AND " +
                DatabaseContract.KayitEntry.COLUMN_NAME_TARIH + " = ?";
        String[] selectionArgs = {entityId, date};

        Cursor cursor = db.query(
                DatabaseContract.KayitEntry.TABLE_NAME,
                new String[]{DatabaseContract.KayitEntry.COLUMN_NAME_TIP},
                selection,
                selectionArgs,
                null,
                null,
                null
        );

        boolean hasRecord = cursor.getCount() > 0;
        cursor.close();
        db.close();

        return hasRecord;
    }
}
