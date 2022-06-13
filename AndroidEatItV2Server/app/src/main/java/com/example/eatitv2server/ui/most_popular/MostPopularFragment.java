package com.example.eatitv2server.ui.most_popular;

import androidx.lifecycle.ViewModelProvider;

import android.app.Activity;
import android.app.AlertDialog;
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
import com.example.eatitv2server.Adapter.MyMostPopularAdapter;
import com.example.eatitv2server.Common.Common;
import com.example.eatitv2server.Common.MySwipeHelper;
import com.example.eatitv2server.EventBus.ToastEvent;
import com.example.eatitv2server.Model.MostPopularModel;
import com.example.eatitv2server.R;
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

public class MostPopularFragment extends Fragment {

    private MostPopularViewModel mostPopularViewModel;

    Unbinder unbinder;
    @BindView(R.id.recycler_most_popular)
    RecyclerView recycler_most_popular;
    AlertDialog dialog;
    LayoutAnimationController layoutAnimationController;
    MyMostPopularAdapter adapter;

    List<MostPopularModel> mostPopularModels;

    ImageView img_most_popular;
    private Uri imageUri = null;

    FirebaseStorage storage;
    StorageReference storageReference;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        mostPopularViewModel =
                new ViewModelProvider(this).get(MostPopularViewModel.class);

        View root = inflater.inflate(R.layout.most_popular_fragment, container, false);

        unbinder = ButterKnife.bind(this, root);
        initViews();

        mostPopularViewModel.getMessageError().observe(getViewLifecycleOwner(), s -> {
            Toast.makeText(getContext(), "" + s, Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        mostPopularViewModel.getMostPopularListtMutable().observe(getViewLifecycleOwner(), mostPopularModelList -> {
            dialog.dismiss();
            mostPopularModels = mostPopularModelList;
            adapter = new MyMostPopularAdapter(getContext(), mostPopularModels);
            recycler_most_popular.setAdapter(adapter);
            recycler_most_popular.setLayoutAnimation(layoutAnimationController);

        });

        return root;
    }

    private void initViews() {
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        dialog = new SpotsDialog.Builder().setContext(getContext()).setCancelable(false).build();
        layoutAnimationController = AnimationUtils.loadLayoutAnimation(getContext(), R.anim.layout_item_from_left);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recycler_most_popular.setLayoutManager(layoutManager);
        recycler_most_popular.addItemDecoration(new DividerItemDecoration(getContext(), layoutManager.getOrientation()));

        MySwipeHelper mySwipeHelper = new MySwipeHelper(getContext(), recycler_most_popular, 200) {
            @Override
            public void instantiateMyButton(RecyclerView.ViewHolder viewHolder, List<MyButton> buf) {
                buf.add(new MyButton(getContext(), "Delete", 30, 0, Color.parseColor("#333639"), pos -> {
                    Common.mostPopularSelected = mostPopularModels.get(pos);
                    showDeleteDialog();

                }));


                buf.add(new MyButton(getContext(), "Update", 30, 0, Color.parseColor("#560027"), pos -> {
                    Common.mostPopularSelected = mostPopularModels.get(pos);
                    showUpdateDialog();

                }));
            }
        };
    }

    private void showDeleteDialog() {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(getContext());
        builder.setTitle("Delete");
        builder.setMessage("Do you really want to delete this item?");

        builder.setNegativeButton("CANCEL", (dialogInterface, which) -> dialogInterface.dismiss())
                .setPositiveButton("DELETE", (dialogInterface, which) -> deleteMostPopular());

        androidx.appcompat.app.AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void deleteMostPopular() {
        FirebaseDatabase.getInstance()
                .getReference(Common.MOST_POPULAR)
                .child(Common.mostPopularSelected.getKey())
                .removeValue()
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                })
                .addOnCompleteListener(task -> {
                    mostPopularViewModel.loadMostPopular();
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
        img_most_popular = (ImageView) itemView.findViewById(R.id.img_category);

        //Set Data
        edt_category_name.setText(new StringBuilder("").append(Common.mostPopularSelected.getName()));
        Glide.with(getContext()).load(Common.mostPopularSelected.getImage()).into(img_most_popular);

        //Set Event
        img_most_popular.setOnClickListener(view -> {
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
                                        updateMostPopular(updateData);
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
                                //Don't Worry about this Erro
                                double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                                dialog.setMessage(new StringBuilder("Uploading: ").append(progress).append("%"));

                            }
                        });

            } else {
                updateMostPopular(updateData);
            }

        });

        builder.setView(itemView);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Common.PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            if (data != null && data.getData() != null) {
                imageUri = data.getData();
                img_most_popular.setImageURI(imageUri);

            }
        }
    }

    private void updateMostPopular(Map<String, Object> updateData) {
        FirebaseDatabase.getInstance()
                .getReference(Common.MOST_POPULAR)
                .child(Common.mostPopularSelected.getKey())
                .updateChildren(updateData)
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                })
                .addOnCompleteListener(task -> {
                    mostPopularViewModel.loadMostPopular();
                    Toast.makeText(getContext(), "Update success!", Toast.LENGTH_SHORT).show();
                    EventBus.getDefault().postSticky(new ToastEvent(Common.ACTION.UPDATE, true));
                });
    }


}
