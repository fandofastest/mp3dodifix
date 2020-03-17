package com.siskiapp.downloadmp3music.adapter;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.siskiapp.downloadmp3music.SiskiAppPlayActivityOffline;
import com.siskiapp.downloadmp3music.R;
import com.siskiapp.downloadmp3music.model.TrackOffline;

import java.util.ArrayList;
import java.util.List;

public class TrackAdapterOffline extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    Context context;
    List<Object> listOffline;
    ArrayList<Integer> count = new ArrayList<>();

    public TrackAdapterOffline(Context context, List<Object> listOffline) {
        this.context = context;
        this.listOffline = listOffline;
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        TextView songTitle;
        LinearLayout action;
        RelativeLayout listArea;
        AppCompatImageView rate, playerstate;

        public ViewHolder(View itemView) {
            super(itemView);
            songTitle = itemView.findViewById(R.id.trackTitleOffline);
            action = itemView.findViewById(R.id.actionAreaOffline);
            rate = itemView.findViewById(R.id.miniplayer_rate);
            playerstate = itemView.findViewById(R.id.miniplayer_play);
            listArea = itemView.findViewById(R.id.listArea);
        }
    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.itemtrack_offline,parent,false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {
        final ViewHolder offlineViewHolder = (ViewHolder)holder;
        final TrackOffline trackoff = (TrackOffline)listOffline.get(position);

        offlineViewHolder.songTitle.setText(trackoff.getSongTitle());

        for (int i = 0; i < listOffline.size(); i++) {
            count.add(0);
        }
        offlineViewHolder.listArea.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (count.get(position) % 2 == 0) {
                    offlineViewHolder.action.setVisibility(View.VISIBLE);

                } else {
                    offlineViewHolder.action.setVisibility(View.GONE);
                }

                count.set(position, count.get(position) + 1);

            }
        });

        offlineViewHolder.rate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri uri = Uri.parse("market://details?id=" + context.getPackageName());
                Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
                try {
                    context.startActivity(goToMarket);
                } catch (ActivityNotFoundException e) {
                    context.startActivity(new Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("http://play.google.com/store/apps/details?id="
                                    + context.getPackageName())));
                }
            }
        });

        offlineViewHolder.playerstate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(context, SiskiAppPlayActivityOffline.class);
                i.putExtra("songtitle", trackoff.getSongTitle());
                i.putExtra("songurl", trackoff.getSongUrl());
                i.putExtra("source", "offline");
                context.startActivity(i);
            }
        });




    }

    @Override
    public int getItemCount() {
        return listOffline.size();
    }
}
