package com.nghiem.rilleyClient.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.nghiem.rilleyClient.Callback.IRecyclerClickListener;
import com.nghiem.rilleyClient.Common.Common;
import com.nghiem.rilleyClient.EventBus.MenuItemEvent;
import com.nghiem.rilleyClient.Model.MilkTeaModel;
import com.nghiem.rilleyClient.R;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

public class MyMilkTeaAdapter extends RecyclerView.Adapter<MyMilkTeaAdapter.MyViewHolder> {

    Context context;
    List<MilkTeaModel> milkTeaModelList;

    public MyMilkTeaAdapter(Context context, List<MilkTeaModel> milkTeaModelList) {
        this.context = context;
        this.milkTeaModelList = milkTeaModelList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(context).inflate(R.layout.layout_milktea,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Glide.with(context)
                .load(milkTeaModelList.get(position).getImageUrl())
                .into(holder.img_milktea);
        holder.txt_milktea_name.setText(new StringBuilder(milkTeaModelList.get(position).getName()));
        holder.txt_milktea_address.setText(new StringBuilder(milkTeaModelList.get(position).getAddress()));

        //Event
        holder.setListener((view, pos) -> {
            Common.currentMilktea = milkTeaModelList.get(pos);
            EventBus.getDefault().postSticky(new MenuItemEvent(true,milkTeaModelList.get(pos)));
        });
    }

    @Override
    public int getItemCount() {
        if (milkTeaModelList != null)
            return milkTeaModelList.size();
        else
            return 0;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView txt_milktea_name,txt_milktea_address;
        ImageView img_milktea;

        IRecyclerClickListener listener;

        public void setListener(IRecyclerClickListener listener) {
            this.listener = listener;
        }

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            txt_milktea_name = itemView.findViewById(R.id.txt_milktea_name);
            txt_milktea_address = itemView.findViewById(R.id.txt_milktea_address);
            img_milktea = itemView.findViewById(R.id.img_milktea);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            listener.onItemClickListener(v,getAdapterPosition());
        }
    }
}
