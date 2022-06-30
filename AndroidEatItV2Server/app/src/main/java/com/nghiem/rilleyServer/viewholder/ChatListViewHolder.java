package com.nghiem.rilleyServer.viewholder;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.nghiem.rilleyServer.Callback.IRecyclerClickListener;
import com.nghiem.rilleyServer.R;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatListViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    public TextView txt_time,txt_email,txt_chat_message;
    public CircleImageView profile_image;

    IRecyclerClickListener listener;

    public void setListener(IRecyclerClickListener listener) {
        this.listener = listener;
    }

    public ChatListViewHolder(@NonNull View itemView) {
        super(itemView);

        txt_time = itemView.findViewById(R.id.txt_time);
        txt_email = itemView.findViewById(R.id.txt_email);
        txt_chat_message = itemView.findViewById(R.id.txt_chat_message);
        profile_image = itemView.findViewById(R.id.profile_image);

        itemView.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        listener.onItemClickListener(view,getAdapterPosition());
    }
}
