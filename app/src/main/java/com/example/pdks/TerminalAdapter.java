package com.example.pdks;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class TerminalAdapter extends RecyclerView.Adapter<TerminalAdapter.ViewHolder> {

    private List<Terminal> terminalList;

    public TerminalAdapter(List<Terminal> terminalList) {
        this.terminalList = terminalList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.terminal_card, parent, false);

        RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) view.getLayoutParams();
        params.width = RecyclerView.LayoutParams.WRAP_CONTENT;
        view.setLayoutParams(params);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Terminal terminal = terminalList.get(position);
        holder.terminalImage.setImageResource(terminal.getImageResId());
        holder.terminalName.setText(terminal.getName());
        holder.terminalCoordinate.setText(terminal.getCoordinate());
        holder.terminalDistanceText.setText(terminal.getDistance());
    }

    @Override
    public int getItemCount() {
        return terminalList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView terminalImage;
        TextView terminalName, terminalCoordinate, terminalDistanceText;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            terminalImage = itemView.findViewById(R.id.terminal_image);
            terminalName = itemView.findViewById(R.id.terminal_name);
            terminalCoordinate = itemView.findViewById(R.id.terminal_coordinate);
            terminalDistanceText = itemView.findViewById(R.id.terminal_distance_text);
        }
    }
}
