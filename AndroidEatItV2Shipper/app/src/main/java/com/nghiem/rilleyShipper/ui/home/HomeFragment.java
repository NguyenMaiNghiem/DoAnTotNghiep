package com.nghiem.rilleyShipper.ui.home;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.nghiem.rilleyShipper.R;
import com.nghiem.rilleyShipper.adapter.MyShippingOrderAdapter;
import com.nghiem.rilleyShipper.common.Common;
import com.nghiem.rilleyShipper.databinding.FragmentHomeBinding;
import com.nghiem.rilleyShipper.model.ShippingOrderModel;
import com.nghiem.rilleyShipper.eventbus.UpdateShippingOrderEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

public class HomeFragment extends Fragment {

    RecyclerView recycler_order;

    LayoutAnimationController layoutAnimationController;
    MyShippingOrderAdapter adapter;
    List<ShippingOrderModel> shippingOrderModelList;

    private HomeViewModel homeViewModel;

    private FragmentHomeBinding binding;

    @SuppressLint("FragmentLiveDataObserve")
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        recycler_order = binding.recyclerOder;

        homeViewModel.getMessageError().observe(this,s -> {
            Toast.makeText(getContext(),s , Toast.LENGTH_LONG).show();
        });
        homeViewModel.getShippingOrderMutableData(Common.currentShipperUser.getPhone())
                .observe(getViewLifecycleOwner(), shippingOrderModels -> {
                    Log.i("data","co du lieu getShippingOrderMutableData:"+shippingOrderModels.toString());
                    shippingOrderModelList = shippingOrderModels;
                    adapter = new MyShippingOrderAdapter(getContext(), shippingOrderModels);
                    recycler_order.setAdapter(adapter);
                    recycler_order.setLayoutAnimation(layoutAnimationController);
                });
        initViews(root);
        return root;
    }

    private void initViews(View root) {
        homeViewModel.loadOrderByShipper(Common.currentShipperUser.getPhone());

        recycler_order.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recycler_order.setLayoutManager(layoutManager);
        recycler_order.addItemDecoration(new DividerItemDecoration(getContext(),layoutManager.getOrientation()));

        layoutAnimationController = AnimationUtils.loadLayoutAnimation(getContext(), R.anim.layout_item_from_left);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (EventBus.getDefault().hasSubscriberForEvent(UpdateShippingOrderEvent.class))
            EventBus.getDefault().removeStickyEvent(UpdateShippingOrderEvent.class);
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(sticky = true,threadMode = ThreadMode.MAIN)
    public void onUpdateShippingOrder(UpdateShippingOrderEvent event)
    {
        homeViewModel.getShippingOrderMutableData(Common.currentShipperUser.getPhone());    //Update data
    }
}