package com.ilan.screenshare.serversdetails.Utils;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ilan.screenshare.serversdetails.R;

import java.util.ArrayList;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.MYViewHolder> {

    Context context;
    ArrayList<ServerDetail> list;

    public RecyclerViewAdapter(Context context, ArrayList<ServerDetail> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public MYViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {

        View view = null;
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        view = layoutInflater.inflate(R.layout.cardview_item,parent , false);

        return new MYViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MYViewHolder myViewHolder, int position) {
        int bgColor = 0;
        switch (list.get(position).getStatus()){
            case 0:
                bgColor = Color.LTGRAY;
                break;
            case 1:
                bgColor = Color.GREEN;
                break;
            case 2:
                bgColor = Color.RED;
            break;
            case 3:
                bgColor = Color.rgb(249,203,156);
                break;
            case 4:
                bgColor = Color.WHITE;
                break;

                default:
                    bgColor = Color.WHITE;
                    break;
        }
        myViewHolder.serverName.setText(list.get(position).getName());
        myViewHolder.cardView.setCardBackgroundColor(bgColor);

    }

    @Override
    public int getItemCount() {
        return list.size();
    }



    // view holder
    public static class MYViewHolder extends RecyclerView.ViewHolder{

        CardView cardView;
        TextView serverName;

        public MYViewHolder(@NonNull View itemView) {
            super(itemView);

            cardView = (CardView) itemView.findViewById(R.id.cardView);
            serverName = (TextView) itemView.findViewById(R.id.serverName);
        }
    }


}
