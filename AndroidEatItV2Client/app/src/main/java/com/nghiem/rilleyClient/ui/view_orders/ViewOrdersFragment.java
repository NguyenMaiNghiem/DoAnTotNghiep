package com.nghiem.rilleyClient.ui.view_orders;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.nghiem.rilleyClient.Adapter.MyOrdersAdapter;
import com.nghiem.rilleyClient.Callback.ILoadOrderCallbackListener;
import com.nghiem.rilleyClient.Common.Common;
import com.nghiem.rilleyClient.Common.MySwipeHelper;
import com.nghiem.rilleyClient.Database.CartDataSource;
import com.nghiem.rilleyClient.Database.CartDatabase;
import com.nghiem.rilleyClient.Database.CartItem;
import com.nghiem.rilleyClient.Database.LocalCartDataSource;
import com.nghiem.rilleyClient.EventBus.CounterCartEvent;
import com.nghiem.rilleyClient.EventBus.MenuItemBack;
import com.nghiem.rilleyClient.Model.OrderModel;
import com.nghiem.rilleyClient.Model.ShippingOrderModel;
import com.nghiem.rilleyClient.R;
import com.nghiem.rilleyClient.TrackingOrderActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import dmax.dialog.SpotsDialog;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class ViewOrdersFragment extends Fragment implements ILoadOrderCallbackListener {

    CartDataSource cartDataSource;
    CompositeDisposable compositeDisposable = new CompositeDisposable();


    @BindView(R.id.recycler_orders)
    RecyclerView recycler_orders;

    AlertDialog dialog;

    private Unbinder unbinder;


    private ViewOrdersViewModel viewOrdersViewModel;

    private ILoadOrderCallbackListener listener;

    @SuppressLint("FragmentLiveDataObserve")
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        viewOrdersViewModel =
                ViewModelProviders.of(this).get(ViewOrdersViewModel.class);
        View root = inflater.inflate(R.layout.fragment_view_order, container, false);
        unbinder = ButterKnife.bind(this, root);
        initView(root);
        loadOrdersFromFirebase();

        viewOrdersViewModel.getMutableLiveDataOrderList().observe(this, orderModelList -> {
            Collections.reverse(orderModelList);
            MyOrdersAdapter adapter = new MyOrdersAdapter(getContext(), orderModelList);
            recycler_orders.setAdapter(adapter);
        });

        return root;
    }

    private void loadOrdersFromFirebase() {
        List<OrderModel> orderList = new ArrayList<>();

        FirebaseDatabase.getInstance(Common.URL)
                .getReference(Common.MILKTEA_REF)
                .child(Common.currentMilktea.getUid())
                .child(Common.ORDER_REF)
                .orderByChild("userId")
                .equalTo(Common.currentUser.getUid())
                .limitToLast(100)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot orderSnapshot : dataSnapshot.getChildren()) {
                            OrderModel order = orderSnapshot.getValue(OrderModel.class);
                            order.setOrderNumber(orderSnapshot.getKey());
                            orderList.add(order);
                        }

                        listener.onLoadOrderSuccess(orderList);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        listener.onLoadOrderFailed(databaseError.getMessage());
                    }
                });
    }

    private void initView(View root) {

        cartDataSource = new LocalCartDataSource(CartDatabase.getInstance(getContext()).cartDAO());

        listener = this;

        dialog = new SpotsDialog.Builder().setCancelable(false).setContext(getContext()).build();

        recycler_orders.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recycler_orders.setLayoutManager(layoutManager);
        recycler_orders.addItemDecoration(new DividerItemDecoration(getContext(), layoutManager.getOrientation()));

        MySwipeHelper mySwipeHelper = new MySwipeHelper(getContext(), recycler_orders, 250) {
            @Override
            public void instantiateMyButton(RecyclerView.ViewHolder viewHolder, List<MyButton> buf) {
                buf.add(new MyButton(getContext(), "Hủy đơn hàng", 30, 0, Color.parseColor("#FF3C30"),
                        pos -> {
                            OrderModel orderModel = ((MyOrdersAdapter) recycler_orders.getAdapter())
                                    .getItemAtPosition(pos);
                            if (orderModel.getOrderStatus() == 0) {
                                androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(getContext());
                                builder.setTitle("Hủy đơn hàng")
                                        .setMessage("Bạn có chắc muốn hủy đơn hàng này không?")
                                        .setNegativeButton("Không", (dialogInterface, which) -> {
                                            dialogInterface.dismiss();
                                        })
                                        .setPositiveButton("Có", (dialog, which) -> {
                                            Map<String, Object> update_data = new HashMap<>();
                                            update_data.put("orderStatus", -1); //Cancel Order
                                            FirebaseDatabase.getInstance(Common.URL)
                                                    .getReference(Common.MILKTEA_REF)
                                                    .child(Common.currentMilktea.getUid())
                                                    .child(Common.ORDER_REF)
                                                    .child(orderModel.getOrderNumber())
                                                    .updateChildren(update_data)
                                                    .addOnFailureListener(e -> {
                                                        Toast.makeText(getContext(), "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                    })
                                                    .addOnSuccessListener(aVoid -> {
                                                        orderModel.setOrderStatus(-1); //Local Update
                                                        ((MyOrdersAdapter) recycler_orders.getAdapter()).setItemAtPosition(pos, orderModel);
                                                        recycler_orders.getAdapter().notifyItemChanged(pos);
                                                        Toast.makeText(getContext(), "Hủy đơn hàng thành công!", Toast.LENGTH_SHORT).show();
                                                    });

                                        });
                                androidx.appcompat.app.AlertDialog dialog = builder.create();
                                dialog.show();

                            } else {
                                Toast.makeText(getContext(), new StringBuilder("Trạng thái đơn hàng đã được thay đổi thành ")
                                                .append(Common.convertStatusToText(orderModel.getOrderStatus()))
                                                .append(" , vì vậy bạn không thể hủy nó!")
                                        , Toast.LENGTH_SHORT).show();
                            }
                        }

                ));

                buf.add(new MyButton(getContext(), "Tracking Order", 30, 0, Color.parseColor("#001970"),
                        pos -> {
                            OrderModel orderModel = ((MyOrdersAdapter) recycler_orders.getAdapter())
                                    .getItemAtPosition(pos);

                            //Fetch from Firebase
                            FirebaseDatabase.getInstance(Common.URL)
                                    .getReference(Common.MILKTEA_REF)
                                    .child(Common.currentMilktea.getUid())
                                    .child(Common.SHIPPING_ORDER_REF) //Copy from Shipper App
                                    .child(orderModel.getOrderNumber())
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            if (dataSnapshot.exists()) {
                                                Common.currentShippingOrder = dataSnapshot.getValue(ShippingOrderModel.class);
                                                Common.currentShippingOrder.setKey(dataSnapshot.getKey());
                                                if (Common.currentShippingOrder.getCurrentLat() != -1 &&
                                                        Common.currentShippingOrder.getCurrentLng() != -1)
                                                {
                                                    startActivity(new Intent(getContext(), TrackingOrderActivity.class));
                                                }
                                                else
                                                {
                                                    Toast.makeText(getContext(), "Shipepr not start ship your order, just wait!", Toast.LENGTH_SHORT).show();
                                                }
                                            } else {
                                                Toast.makeText(getContext(), "Your order is just placed, must be wait for Shipping!", Toast.LENGTH_SHORT).show();
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {
                                            Toast.makeText(getContext(), "" + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });


                        }

                ));
                buf.add(new MyButton(getContext(), "Repeat Order", 30, 0, Color.parseColor("#5d4037"),
                        pos -> {
                            OrderModel orderModel = ((MyOrdersAdapter) recycler_orders.getAdapter())
                                    .getItemAtPosition(pos);
                            dialog.show();

                            cartDataSource.cleanCart(Common.currentUser.getUid(),
                                    Common.currentMilktea.getUid())
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(new SingleObserver<Integer>() {
                                        @Override
                                        public void onSubscribe(Disposable d) {

                                        }

                                        @Override
                                        public void onSuccess(Integer integer) {
//                                            Toast.makeText(getContext(), "Clear Cart Success!", Toast.LENGTH_SHORT).show();
                                            //After Clean Cart, Just add new
                                            CartItem[] cartItems = orderModel.getCartItemList()
                                                    .toArray(new CartItem[orderModel.getCartItemList().size()]);

                                            //Insert New
                                            compositeDisposable.add(cartDataSource.insertOrReplaceAll(cartItems)
                                                    .subscribeOn(Schedulers.io())
                                                    .observeOn(AndroidSchedulers.mainThread())
                                                    .subscribe(()->{
                                                        dialog.dismiss();
                                                        Toast.makeText(getContext(), "Add all item to Cart Success!", Toast.LENGTH_SHORT).show();
                                                        EventBus.getDefault().postSticky(new CounterCartEvent(true)); //Counter Fab
                                                    }, throwable -> {
                                                        dialog.dismiss();
                                                        Toast.makeText(getContext(), "[Repeat Cart]" + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                                                    }));

                                        }

                                        @Override
                                        public void onError(Throwable e) {
                                            dialog.dismiss();
                                            Toast.makeText(getContext(), "[ERROR]"+e.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });

                        }

                ));
            }
        };


    }

    @Override
    public void onLoadOrderSuccess(List<OrderModel> orderList) {
        dialog.dismiss();
        viewOrdersViewModel.setMutableLiveDataOrderList(orderList);
    }

    @Override
    public void onLoadOrderFailed(String message) {
        dialog.dismiss();
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().postSticky(new MenuItemBack());
        super.onDestroy();
    }

    @Override
    public void onStop() {
        compositeDisposable.clear();
        super.onStop();
    }


}