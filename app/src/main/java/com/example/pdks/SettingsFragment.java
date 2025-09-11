package com.example.pdks;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.fragment.app.Fragment;

public class SettingsFragment extends Fragment {

    private String entityId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_settings, container, false);

        Bundle args = getArguments();
        if (args != null) {
            entityId = args.getString("ENTITY_ID");
        }

        LinearLayout layoutTerminal = root.findViewById(R.id.layout_terminal2);

        layoutTerminal.setOnClickListener(v -> {
            Fragment terminalFragment = new TerminalFragment();

            Bundle bundle = new Bundle();
            bundle.putString("ENTITY_ID", entityId);
            terminalFragment.setArguments(bundle);

            requireActivity()
                    .getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, terminalFragment)
                    .addToBackStack(null)
                    .commit();
        });

        return root;
    }
}
