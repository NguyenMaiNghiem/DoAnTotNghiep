package com.nghiem.rilleyShipper.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.nghiem.rilleyShipper.R;
import com.nghiem.rilleyShipper.ShippingActivity;
import com.nghiem.rilleyShipper.common.Common;
import com.nghiem.rilleyShipper.model.ShippingOrderModel;
import com.google.android.material.button.MaterialButton;
import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.paperdb.Paper;

public class MyShippingOrderAdapter extends RecyclerView.Adapter<MyShippingOrderAdapter.MyViewHolder> {

    Context context;
    List<ShippingOrderModel> shippingOrderModelList;
    SimpleDateFormat simpleDateFormat;

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemview = LayoutInflater.from(context)
                .inflate(R.layout.layout_order_shipper,parent,false);
        return new MyViewHolder(itemview);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Glide.with(context)
                .load(shippingOrderModelList.get(position).getOrderModel().getCartItemList()
                .get(0).getFoodImage())
                .into(holder.img_food);
        holder.txt_date.setText(new StringBuilder(
                simpleDateFormat.format(shippingOrderModelList.get(position).getOrderModel().getCreateDate())
        ));

        Common.setSpanStringColor("No : ",
                shippingOrderModelList.get(position).getOrderModel().getKey(),
                holder.txt_order_number, Color.parseColor("#BA454A"));
        Common.setSpanStringColor("Address : ",
                shippingOrderModelList.get(position).getOrderModel().getShippingAddress(),
                holder.txt_order_address, Color.parseColor("#BA454A"));
        Common.setSpanStringColor("Payment : ",
                shippingOrderModelList.get(position).getOrderModel().getTransactionId(),
                holder.txt_payment, Color.parseColor("#BA454A"));
        //Disable button if already start trip
        if (shippingOrderModelList.get(position).isStartTrip())
        {
            holder.btn_ship_now.setEnabled(false);
        }

        //Event
        holder.btn_ship_now.setOnClickListener(v -> {
            Paper.book().write(Common.SHIPPER_ORDER_DATA,new Gson().toJson(shippingOrderModelList.get(position)));

            Intent intent = new Intent(context, ShippingActivity.class);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return shippingOrderModelList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        private Unbinder unbinder;

        @BindView(R.id.txt_date)
        TextView txt_date;
        @BindView(R.id.txt_order_address)
        TextView txt_order_address;
        @BindView(R.id.txt_order_number)
        TextView txt_order_number;
        @BindView(R.id.txt_payment)
        TextView txt_payment;
        @BindView(R.id.img_food)
        ImageView img_food;
        @BindView(R.id.btn_ship_now)
        MaterialButton btn_ship_now;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            unbinder = ButterKnife.bind(this,itemView);
        }
    }

    public MyShippingOrderAdapter(Context context, List<ShippingOrderModel> shippingOrderModelList) {
        this.context = context;
        this.shippingOrderModelList = shippingOrderModelList;
        this.simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        Paper.init(context);
    }
}
