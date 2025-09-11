package com.example.pdks;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class DetailsAdapter extends RecyclerView.Adapter<DetailsAdapter.ViewHolder> {

    private List<KayitModel> kayitList;

    public DetailsAdapter(List<KayitModel> kayitList) {
        this.kayitList = kayitList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.details_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        KayitModel kayit = kayitList.get(position);

        holder.locationValue.setText(kayit.getBlokAdi());
        holder.movementValue.setText(kayit.getTip());
        holder.dateValue.setText(kayit.getTarih());
        holder.timeValue.setText(kayit.getSaat());

        int color = "Çıkış Kaydı".equals(kayit.getTip()) ?
                ContextCompat.getColor(holder.itemView.getContext(), R.color.acik_kirmizi) :
                ContextCompat.getColor(holder.itemView.getContext(), R.color.yesil);

        holder.detailsLayout.setBackgroundColor(color);

        if (kayit.getUserImage() != null) {
            holder.userImage.setImageBitmap(kayit.getUserImage());
        } else {
            holder.userImage.setImageResource(R.drawable.user_photo);
        }
    }

    @Override
    public int getItemCount() {
        return kayitList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView locationValue, movementValue, dateValue, timeValue;
        ImageView userImage;
        View detailsLayout;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            locationValue = itemView.findViewById(R.id.location_value);
            movementValue = itemView.findViewById(R.id.movement_value);
            dateValue = itemView.findViewById(R.id.date_value);
            timeValue = itemView.findViewById(R.id.time_value);
            userImage = itemView.findViewById(R.id.user_image2);
            detailsLayout = itemView.findViewById(R.id.details_layout);
        }
    }
}
