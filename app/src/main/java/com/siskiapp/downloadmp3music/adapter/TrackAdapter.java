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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.siskiapp.downloadmp3music.SiskiAppMusicPlayActivity;
import com.siskiapp.downloadmp3music.R;
import com.siskiapp.downloadmp3music.model.TrackOnline;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class TrackAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    Context context;
    List<Object> listOnline;
    ArrayList<Integer> count = new ArrayList<Integer>();
    final ViewHolder[] viewHolderArray = {null};


    public TrackAdapter(Context context, List<Object> listOnline) {
        this.context = context;
        this.listOnline = listOnline;

    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        RelativeLayout listArea;
        LinearLayout action;
        ImageView songImg;
        TextView songTitle, songUser, songLike, songDur;
        AppCompatImageView rate, play;


        public ViewHolder(View itemView) {
            super(itemView);

            listArea = itemView.findViewById(R.id.listArea);
            action = itemView.findViewById(R.id.actionAreaOnline);
            songImg = itemView.findViewById(R.id.trackImg);
            songTitle = itemView.findViewById(R.id.trackTitle);
            songUser = itemView.findViewById(R.id.trackArtist);
            songLike = itemView.findViewById(R.id.txt_like);
            songDur = itemView.findViewById(R.id.txt_dur);
            rate = itemView.findViewById(R.id.miniplayer_rate);
            play = itemView.findViewById(R.id.miniplayer_play);

        }
    }

    public static String formatValue(Number number) {
        char[] suffix = {' ', 'k', 'M', 'B', 'T', 'P', 'E'};
        long numValue = number.longValue();
        int value = (int) Math.floor(Math.log10(numValue));
        int base = value / 3;
        if (value >= 3 && base < suffix.length) {
            return new DecimalFormat("#0.#").format(numValue / Math.pow(10, base * 3)) + suffix[base];
        } else {
            return new DecimalFormat("#,##0").format(numValue);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_track_online,parent,false);

        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {
        final ViewHolder viewHolderOnline = (ViewHolder)holder;

        final TrackOnline track = (TrackOnline) listOnline.get(position);

        viewHolderOnline.songTitle.setText(track.getSongTitle());
        viewHolderOnline.songUser.setText(track.getSongUsr());
        Glide.with(context).load(track.getSongImg()).error(R.mipmap.ic_launcher).into(viewHolderOnline.songImg);
        viewHolderOnline.songLike.setText(formatValue(track.getSongLikes()));
        int dur = track.getSongDur();
        long time = (long) dur;
        String timeprint = new SimpleDateFormat("mm:ss").format(new Date(time));
        viewHolderOnline.songDur.setText(timeprint);

        for (int i = 0; i < listOnline.size(); i++) {
            count.add(0);
        }

        viewHolderOnline.listArea.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                tutup(viewHolderArray[0],position);

                if (viewHolderOnline.action.getVisibility()==View.GONE){
                    viewHolderOnline.action.setVisibility(View.VISIBLE);

                }
                else {
                    viewHolderOnline.action.setVisibility(View.GONE);
                }




//                if (viewHolderOnline.action.isShown()){
//                        viewHolderOnline.action.setVisibility(View.GONE);
//
//                    } else {
//                        viewHolderOnline.action.setVisibility(View.VISIBLE);
//                    }

                viewHolderArray[0] =viewHolderOnline;



            }
        });

        viewHolderOnline.play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(context, SiskiAppMusicPlayActivity.class);
                i.putExtra("songtitle", track.getSongTitle());
                i.putExtra("songartist", track.getSongUsr());
                i.putExtra("songimg", track.getSongImg());
                i.putExtra("songurl", track.getSongUrl());
                i.putExtra("source", "online");
                context.startActivity(i);
            }
        });

        viewHolderOnline.rate.setOnClickListener(new View.OnClickListener() {
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

    }

    private void tutup(ViewHolder holder, int pos){

        if (holder!=null){
            holder.action.setVisibility(View.GONE);


        }



    }


    @Override
    public int getItemCount() {
        return listOnline.size();
    }
}
