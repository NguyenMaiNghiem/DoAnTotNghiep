package com.nghiem.rilleyServer.ui.category;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.nghiem.rilleyServer.Adapter.MyCategoriesAdapter;
import com.nghiem.rilleyServer.Common.Common;
import com.nghiem.rilleyServer.Common.MySwipeHelper;
import com.nghiem.rilleyServer.EventBus.ToastEvent;
import com.nghiem.rilleyServer.Model.CategoryModel;
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

public class CategoryFragment extends Fragment {

    private CategoryViewModel categoryViewModel;

    Unbinder unbinder;
    @BindView(R.id.recycler_menu)
    RecyclerView recycler_menu;
    AlertDialog dialog;
    LayoutAnimationController layoutAnimationController;
    MyCategoriesAdapter adapter;

    ImageView img_category;
    private Uri imageUri = null;

    FirebaseStorage storage;
    StorageReference storageReference;

    List<CategoryModel> categoryModels;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        categoryViewModel =
                new ViewModelProvider(this).get(CategoryViewModel.class);
        View root = inflater.inflate(R.layout.fragment_category, container, false);

        unbinder = ButterKnife.bind(this, root);
        initViews();

        categoryViewModel.getMessageError().observe(getViewLifecycleOwner(), s -> {
            Toast.makeText(getContext(), "" + s, Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        categoryViewModel.getCategoryListMutable().observe(getViewLifecycleOwner(), categoryModelList -> {
            dialog.dismiss();
            categoryModels = categoryModelList;
            adapter = new MyCategoriesAdapter(getContext(), categoryModels);
            recycler_menu.setAdapter(adapter);
            recycler_menu.setLayoutAnimation(layoutAnimationController);

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
        recycler_menu.setLayoutManager(layoutManager);
        recycler_menu.addItemDecoration(new DividerItemDecoration(getContext(), layoutManager.getOrientation()));

        MySwipeHelper mySwipeHelper = new MySwipeHelper(getContext(), recycler_menu, 200) {
            @Override
            public void instantiateMyButton(RecyclerView.ViewHolder viewHolder, List<MyButton> buf) {
                buf.add(new MyButton(getContext(), "Delete", 30, 0, Color.parseColor("#333639"), pos -> {
                    Common.categorySelected = categoryModels.get(pos);

                    showDeleteDialog();

                }));
                buf.add(new MyButton(getContext(), "Update", 30, 0, Color.parseColor("#560027"), pos -> {
                    Common.categorySelected = categoryModels.get(pos);

                    showUpdateDialog();

                }));
            }
        };

        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.action_bar_menu,menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_create)
        {
            showAddDialog();
        }
        return super.onOptionsItemSelected(item);
    }

    private void showDeleteDialog() {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(getContext());
        builder.setTitle("Delete");
        builder.setMessage("Do you really want to delete this category?");

        builder.setNegativeButton("CANCEL", (dialogInterface, which) -> dialogInterface.dismiss())
                .setPositiveButton("DELETE", (dialogInterface, which) -> deleteCategory());

        androidx.appcompat.app.AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void deleteCategory() {
        FirebaseDatabase.getInstance(Common.URL)
                .getReference(Common.CATEGORY_REF)
                .child(Common.categorySelected.getMenu_id())
                .removeValue()
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                })
                .addOnCompleteListener(task -> {
                    categoryViewModel.loadCategories();

                    EventBus.getDefault().postSticky(new ToastEvent(Common.ACTION.DELETE, false));
                });
    }

    private void showUpdateDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Update");
        builder.setMessage("Please fill information");

        View itemView = LayoutInflater.from(getContext()).inflate(R.layout.layout_update_category, null);
        EditText edt_category_name = (EditText) itemView.findViewById(R.id.edt_category_name);
        img_category = (ImageView) itemView.findViewById(R.id.img_category);

        //Set Data
        edt_category_name.setText(new StringBuilder("").append(Common.categorySelected.getName()));
        Glide.with(getContext()).load(Common.categorySelected.getImage()).into(img_category);

        //Set Event
        img_category.setOnClickListener(view -> {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), Common.PICK_IMAGE_REQUEST);

        });
        builder.setNegativeButton("CANCEL", (dialogInterface, which) -> {
            dialog.dismiss();

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
                                       updateCategory(updateData);
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
                updateCategory(updateData);
            }

        });

        builder.setView(itemView);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == Common.PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK)
        {
            if(data!=null && data.getData() !=null)
            {
                imageUri = data.getData();
                img_category.setImageURI(imageUri);

            }
        }
    }

    private void updateCategory(Map<String, Object> updateData) {
        FirebaseDatabase.getInstance(Common.URL)
                .getReference(Common.CATEGORY_REF)
                .child(Common.categorySelected.getMenu_id())
                .updateChildren(updateData)
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                })
                .addOnCompleteListener(task -> {
                    categoryViewModel.loadCategories();
//                    Toast.makeText(getContext(), "Update success!", Toast.LENGTH_SHORT).show();
                    EventBus.getDefault().postSticky(new ToastEvent(Common.ACTION.UPDATE, false));
                });
    }

    private void showAddDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Create Category");
        builder.setMessage("Please fill information");

        View itemView = LayoutInflater.from(getContext()).inflate(R.layout.layout_update_category, null);
        EditText edt_category_name = (EditText) itemView.findViewById(R.id.edt_category_name);
        img_category = (ImageView) itemView.findViewById(R.id.img_category);

        //Set Data
        Glide.with(getContext()).load(R.drawable.ic_image_gray_24dp).into(img_category);

        //Set Event
        img_category.setOnClickListener(view -> {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), Common.PICK_IMAGE_REQUEST);

        });
        builder.setNegativeButton("CANCEL", (dialogInterface, which) -> {
            dialog.dismiss();

        });
        builder.setPositiveButton("CREATE", (dialogInterface, which) -> {


            CategoryModel categoryModel = new CategoryModel();
            categoryModel.setName(edt_category_name.getText().toString());

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
                                        categoryModel.setImage(uri.toString());
                                        addCategory(categoryModel);
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
                addCategory(categoryModel);            }

        });

        builder.setView(itemView);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void addCategory(CategoryModel categoryModel) {
        FirebaseDatabase.getInstance(Common.URL)
                .getReference(Common.CATEGORY_REF)
                .push()
                .setValue(categoryModel)
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                })
                .addOnCompleteListener(task -> {
                    categoryViewModel.loadCategories();
//                    Toast.makeText(getContext(), "Update success!", Toast.LENGTH_SHORT).show();
                    EventBus.getDefault().postSticky(new ToastEvent(Common.ACTION.CREATE, false));
                });
    }

}