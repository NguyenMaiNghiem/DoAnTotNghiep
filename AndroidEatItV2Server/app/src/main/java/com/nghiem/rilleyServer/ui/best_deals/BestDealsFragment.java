package com.nghiem.rilleyServer.ui.best_deals;

import androidx.lifecycle.ViewModelProvider;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.nghiem.rilleyServer.Adapter.MyBestDealsAdapter;
import com.nghiem.rilleyServer.Common.Common;
import com.nghiem.rilleyServer.Common.MySwipeHelper;
import com.nghiem.rilleyServer.EventBus.ToastEvent;
import com.nghiem.rilleyServer.Model.BestDealsModel;
import com.nghiem.rilleyServer.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.greenrobot.eventbus.EventBus;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import dmax.dialog.SpotsDialog;

public class BestDealsFragment extends Fragment {

    private BestDealsViewModel bestDealsViewModel;

    Unbinder unbinder;
    @BindView(R.id.recycler_best_deal)
    RecyclerView recycler_best_deal;
    AlertDialog dialog;
    LayoutAnimationController layoutAnimationController;
    MyBestDealsAdapter adapter;

    List<BestDealsModel> bestDealsModels;

    ImageView img_best_deals;
    private Uri imageUri = null;

    FirebaseStorage storage;
    StorageReference storageReference;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        bestDealsViewModel =
                new ViewModelProvider(this).get(BestDealsViewModel.class);

        View root = inflater.inflate(R.layout.best_deals_fragment, container, false);

        unbinder = ButterKnife.bind(this, root);
        initViews();

        bestDealsViewModel.getMessageError().observe(getViewLifecycleOwner(), s -> {
            Toast.makeText(getContext(), "" + s, Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        bestDealsViewModel.getBestDealsListtMutable().observe(getViewLifecycleOwner(), bestDealsModelList -> {
            dialog.dismiss();
            bestDealsModels = bestDealsModelList;
            adapter = new MyBestDealsAdapter(getContext(), bestDealsModels);
            recycler_best_deal.setAdapter(adapter);
            recycler_best_deal.setLayoutAnimation(layoutAnimationController);

        });

        return root;

    }

    private void initViews() {

        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        dialog = new SpotsDialog.Builder().setContext(getContext()).setCancelable(false).build();
//        dialog.show(); //Remove it to fix loading show when resume fragment
        layoutAnimationController = AnimationUtils.loadLayoutAnimation(getContext(), R.anim.layout_item_from_left);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recycler_best_deal.setLayoutManager(layoutManager);
        recycler_best_deal.addItemDecoration(new DividerItemDecoration(getContext(), layoutManager.getOrientation()));

        MySwipeHelper mySwipeHelper = new MySwipeHelper(getContext(), recycler_best_deal, 200) {
            @Override
            public void instantiateMyButton(RecyclerView.ViewHolder viewHolder, List<MyButton> buf) {
                buf.add(new MyButton(getContext(), "Delete", 30, 0, Color.parseColor("#333639"), pos -> {
                    Common.bestDealsSelected = bestDealsModels.get(pos);

                    showDeleteDialog();

                }));


                buf.add(new MyButton(getContext(), "Update", 30, 0, Color.parseColor("#560027"), pos -> {
                    Common.bestDealsSelected = bestDealsModels.get(pos);

                    showUpdateDialog();

                }));
            }
        };

    }

    private void showDeleteDialog() {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(getContext());
        builder.setTitle("Delete");
        builder.setMessage("Do you really want to delete this item?");

        builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                dialogInterface.dismiss();
            }
        }).setPositiveButton("DELETE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                deleteBestDeals();
            }
        });

        androidx.appcompat.app.AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void deleteBestDeals() {
        FirebaseDatabase.getInstance(Common.URL)
                .getReference(Common.BEST_DEALS)
                .child(Common.bestDealsSelected.getKey())
                .removeValue()
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                })
                .addOnCompleteListener(task -> {
                    bestDealsViewModel.loadBestDeals();
                    Toast.makeText(getContext(), "Update success!", Toast.LENGTH_SHORT).show();
                    EventBus.getDefault().postSticky(new ToastEvent(Common.ACTION.DELETE, true));
                });

    }

    private void showUpdateDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Update");
        builder.setMessage("Please fill information");

        View itemView = LayoutInflater.from(getContext()).inflate(R.layout.layout_update_category, null);
        EditText edt_category_name = (EditText) itemView.findViewById(R.id.edt_category_name);
        img_best_deals = (ImageView) itemView.findViewById(R.id.img_category);

        //Set Data
        edt_category_name.setText(new StringBuilder("").append(Common.bestDealsSelected.getName()));
        Glide.with(getContext()).load(Common.bestDealsSelected.getImage()).into(img_best_deals);

        //Set Event
        img_best_deals.setOnClickListener(view -> {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), Common.PICK_IMAGE_REQUEST);

        });
        builder.setNegativeButton("CANCEL", (dialogInterface, which) -> {
            dialogInterface.dismiss();

        });
        builder.setPositiveButton("UPDATE", (dialogInterface, which) -> {
            Map<String, Object> updateData = new HashMap<>();
            updateData.put("name", edt_category_name.getText().toString());

            if (imageUri != null) {

                dialog.setMessage("Uploading...");
                dialog.show();

                String unique_name = UUID.randomUUID().toString();
                StorageReference imageFolder = storageReference.child("images/" + unique_name);
                imageFolder.putFile(imageUri)
                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                dialog.dismiss();
                                imageFolder.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        //Set Value for newCategory if Image Uploaded and we can get download LINK
                                        updateData.put("image", uri.toString());
                                        updateBestDeals(updateData);
                                    }
                                });
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                dialog.dismiss();
                                Toast.makeText(getContext(), "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        })

                        .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
                                //Don't Worry about this Error
                                double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                                dialog.setMessage(new StringBuilder("Uploading: ").append(progress).append("%"));

                            }
                        });

            } else {
                updateBestDeals(updateData);
            }

        });

        builder.setView(itemView);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == Common.PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK)
        {
            if(data!=null && data.getData() !=null)
            {
                imageUri = data.getData();
                img_best_deals.setImageURI(imageUri);

            }
        }
    }

    private void updateBestDeals(Map<String, Object> updateData) {
        FirebaseDatabase.getInstance(Common.URL)
                .getReference(Common.BEST_DEALS)
                .child(Common.bestDealsSelected.getKey())
                .updateChildren(updateData)
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                })
                .addOnCompleteListener(task -> {
                    bestDealsViewModel.loadBestDeals();
                    Toast.makeText(getContext(), "Update success!", Toast.LENGTH_SHORT).show();
                    EventBus.getDefault().postSticky(new ToastEvent(Common.ACTION.UPDATE, true));
                });
    }


}
