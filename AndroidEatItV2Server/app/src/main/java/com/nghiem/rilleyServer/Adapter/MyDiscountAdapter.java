package com.nghiem.rilleyServer.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.nghiem.rilleyServer.Model.DiscountModel;
import com.nghiem.rilleyServer.R;

import java.text.SimpleDateFormat;
import java.util.List;

public class MyDiscountAdapter extends RecyclerView.Adapter<MyDiscountAdapter.MyViewHolder> {
    Context context;
    List<DiscountModel> discountModelList;
    SimpleDateFormat simpleDateFormat;

    public MyDiscountAdapter(Context context, List<DiscountModel> discountModelList) {
        this.context = context;
        this.discountModelList = discountModelList;
        simpleDateFormat = new SimpleDateFormat("dd/MM/yyy");
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(context).inflate(R.layout.layout_discount_item,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        holder.txt_code.setText(new StringBuilder("Code: ").append(discountModelList.get(position).getKey()));
        holder.txt_percent.setText(new StringBuilder("Percent: ").append(discountModelList.get(position).getPercent()));
        holder.txt_valid.setText(new StringBuilder("Valid until: ").append(simpleDateFormat.format(discountModelList.get(position).getUntilDate())));
    }

    @Override
    public int getItemCount() {
        if (discountModelList != null)
            return discountModelList.size();
        else
            return 0;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{
        TextView txt_code,txt_percent,txt_valid;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            txt_code = itemView.findViewById(R.id.txt_code);
            txt_percent = itemView.findViewById(R.id.txt_percent);
            txt_valid = itemView.findViewById(R.id.txt_valid);
        }
    }
}
