package com.nghiem.rilleyClient.ViewHolder;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.nghiem.rilleyClient.R;
import de.hdodenhof.circleimageview.CircleImageView;

public class ChatTextHolder extends RecyclerView.ViewHolder {
    public TextView txt_time,txt_email,txt_chat_message;
    public CircleImageView profile_image;

    public ChatTextHolder(@NonNull View itemView) {
        super(itemView);
        txt_time = itemView.findViewById(R.id.txt_time);
        txt_email = itemView.findViewById(R.id.txt_email);
        txt_chat_message = itemView.findViewById(R.id.txt_chat_message);
        profile_image = itemView.findViewById(R.id.profile_image);

    }

}
