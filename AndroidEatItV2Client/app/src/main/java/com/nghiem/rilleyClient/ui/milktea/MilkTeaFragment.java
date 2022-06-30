package com.nghiem.rilleyClient.ui.milktea;

import androidx.lifecycle.ViewModelProvider;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;

import com.nghiem.rilleyClient.Adapter.MyMilkTeaAdapter;
import com.nghiem.rilleyClient.EventBus.CounterCartEvent;
import com.nghiem.rilleyClient.EventBus.HideFABCart;
import com.nghiem.rilleyClient.EventBus.MenuInflateEvent;
import com.nghiem.rilleyClient.R;

import org.greenrobot.eventbus.EventBus;

public class MilkTeaFragment extends Fragment {

    private MilkTeaViewModel mViewModel;

    RecyclerView recycler_milktea;
    AlertDialog dialog;
    LayoutAnimationController layoutAnimationController;
    MyMilkTeaAdapter adapter;

    public static MilkTeaFragment newInstance() {
        return new MilkTeaFragment();
    }

    @SuppressLint("FragmentLiveDataObserve")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        mViewModel = ViewModelProviders.of(this).get(MilkTeaViewModel.class);
        View root = inflater.inflate(R.layout.fragment_milk_tea, container, false);

        recycler_milktea = root.findViewById(R.id.recycler_milktea);
        
        initViews();

        mViewModel.getMilkteaListMutable().observe(this,milkTeaModels -> {
            dialog.dismiss();
            adapter = new MyMilkTeaAdapter(getContext(),milkTeaModels);
            recycler_milktea.setAdapter(adapter);
            recycler_milktea.setLayoutAnimation(layoutAnimationController);
        });
        
        return root;
    }

    private void initViews() {
        EventBus.getDefault().postSticky(new HideFABCart(true)); //Hide when user back to this fragment
        setHasOptionsMenu(true);
        dialog = new AlertDialog.Builder(getContext()).setCancelable(false)
                .setMessage("Vui lòng đợi ....").create();
        dialog.show();
        layoutAnimationController = AnimationUtils.loadLayoutAnimation(getContext(),R.anim.layout_item_from_left);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setOrientation(RecyclerView.VERTICAL);
        recycler_milktea.setLayoutManager(linearLayoutManager);
        recycler_milktea.addItemDecoration(new DividerItemDecoration(getContext(),linearLayoutManager.getOrientation()));
    }

    @Override
    public void onResume() {
        super.onResume();
        EventBus.getDefault().postSticky(new CounterCartEvent(true));
        EventBus.getDefault().postSticky(new MenuInflateEvent(false));
    }
}