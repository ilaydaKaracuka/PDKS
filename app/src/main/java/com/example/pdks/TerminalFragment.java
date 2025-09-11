package com.example.pdks;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.OnBackPressedCallback;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class TerminalFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_terminal, container, false);

        RecyclerView recyclerView = view.findViewById(R.id.terminalRecyclerView);

        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 1, GridLayoutManager.VERTICAL, false));
        recyclerView.setHasFixedSize(true);

        List<Terminal> terminals = new ArrayList<>();
        terminals.add(new Terminal(R.drawable.a_blok, "Esenler- A Blok", "41.0382184, 28.8866255", "50 m."));
        terminals.add(new Terminal(R.drawable.b_blok, "Esenler- B Blok", "41.0384875, 28.8862446", "40 m."));
        terminals.add(new Terminal(R.drawable.c_blok, "Esenler- C Blok", "41.0404837, 28.8850628", "50 m."));

        recyclerView.setAdapter(new TerminalAdapter(terminals));

        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(),
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
                        transaction.replace(R.id.fragment_container, new SettingsFragment());
                        transaction.addToBackStack(null);
                        transaction.commit();
                    }
                });

        return view;
    }
}
