package com.dinilbositha.wasthirestaurantadmin.fragment;

import android.app.AlertDialog;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.dinilbositha.wasthirestaurantadmin.R;
import com.dinilbositha.wasthirestaurantadmin.adapter.AdminCategoryAdapter;
import com.dinilbositha.wasthirestaurantadmin.databinding.DialogAddCategoryBinding;
import com.dinilbositha.wasthirestaurantadmin.databinding.FragmentCategoryBinding;
import com.dinilbositha.wasthirestaurantadmin.model.Category;
import com.dinilbositha.wasthirestaurantadmin.viewmodel.CategoryViewModel;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CategoryFragment extends Fragment {

    private FragmentCategoryBinding binding;
    private CategoryViewModel categoryViewModel;
    private AdminCategoryAdapter adapter;
    private final List<Category> categoryList = new ArrayList<>();

    private DialogAddCategoryBinding dialogBinding;
    private AlertDialog activeDialog;
    private Uri selectedImageUri;

    private final ActivityResultLauncher<String> imagePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    selectedImageUri = uri;

                    if (dialogBinding != null) {
                        Glide.with(requireContext())
                                .load(uri)
                                .placeholder(R.drawable.app_logo)
                                .error(R.drawable.app_logo)
                                .into(dialogBinding.imgCategoryPreview);
                    }
                }
            });

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentCategoryBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        categoryViewModel = new ViewModelProvider(this).get(CategoryViewModel.class);

        setupRecyclerView();
        setupClicks();
        observeViewModel();

        categoryViewModel.loadCategories();
    }

    private void setupRecyclerView() {
        adapter = new AdminCategoryAdapter(category -> {
            if (category != null && category.getCategoryId() != null) {
                boolean newStatus = !category.isStatus();

                Log.d("CATEGORY_DEBUG", "Clicked category: " + category.getCategoryName());
                Log.d("CATEGORY_DEBUG", "Doc ID used: " + category.getCategoryId());
                Log.d("CATEGORY_DEBUG", "Old status: " + category.isStatus() + ", New status: " + newStatus);

                // local instant update
                category.setStatus(newStatus);
                adapter.updateData(new ArrayList<>(categoryList));

                // firestore update
                categoryViewModel.updateCategoryStatus(category.getCategoryId(), newStatus);
            }
        });

        binding.recyclerCategories.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerCategories.setAdapter(adapter);
    }

    private void setupClicks() {
        binding.btnBack.setOnClickListener(v ->
                requireActivity().getSupportFragmentManager().popBackStack()
        );

        binding.fabAddCategory.setOnClickListener(v -> showAddCategoryDialog());
    }

    private void showAddCategoryDialog() {
        if (getContext() == null) return;

        selectedImageUri = null;
        dialogBinding = DialogAddCategoryBinding.inflate(getLayoutInflater());

        dialogBinding.txtDialogTitle.setText("Add Category");
        dialogBinding.switchCategoryStatus.setChecked(true);

        dialogBinding.btnSelectCategoryImage.setOnClickListener(v ->
                imagePickerLauncher.launch("image/*")
        );

        activeDialog = new AlertDialog.Builder(requireContext())
                .setView(dialogBinding.getRoot())
                .setPositiveButton("Add", null)
                .setNegativeButton("Cancel", null)
                .create();

        activeDialog.setOnShowListener(dialog -> activeDialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener(v -> validateAndSaveCategory()));

        activeDialog.show();
    }

    private void validateAndSaveCategory() {
        if (dialogBinding == null) return;

        String categoryName = getText(dialogBinding.etCategoryName);
        boolean status = dialogBinding.switchCategoryStatus.isChecked();

        dialogBinding.layoutCategoryName.setError(null);

        if (categoryName.isEmpty()) {
            dialogBinding.layoutCategoryName.setError("Category name is required");
            dialogBinding.etCategoryName.requestFocus();
            return;
        }

        if (selectedImageUri == null) {
            Toast.makeText(requireContext(), "Please select a category image", Toast.LENGTH_SHORT).show();
            return;
        }

        binding.progressBar.setVisibility(View.VISIBLE);
        uploadCategoryImageAndSave(categoryName, status);
    }

    private void uploadCategoryImageAndSave(String categoryName, boolean status) {
        StorageReference storageReference = FirebaseStorage.getInstance()
                .getReference("categories")
                .child(UUID.randomUUID().toString() + ".jpg");

        storageReference.putFile(selectedImageUri)
                .continueWithTask(task -> {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }
                    return storageReference.getDownloadUrl();
                })
                .addOnSuccessListener(uri -> {
                    Category category = Category.builder()
                            .categoryName(categoryName)
                            .imgUrl(uri.toString())
                            .status(status)
                            .build();

                    categoryViewModel.addCategory(category);
                    binding.progressBar.setVisibility(View.GONE);

                    if (activeDialog != null) {
                        activeDialog.dismiss();
                    }
                })
                .addOnFailureListener(e -> {
                    binding.progressBar.setVisibility(View.GONE);
                    Toast.makeText(requireContext(), "Image upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void observeViewModel() {
        categoryViewModel.getLoading().observe(getViewLifecycleOwner(), isLoading -> {
            if (binding == null) return;
            binding.progressBar.setVisibility(Boolean.TRUE.equals(isLoading) ? View.VISIBLE : View.GONE);
        });

        categoryViewModel.getCategories().observe(getViewLifecycleOwner(), categories -> {
            if (binding == null) return;

            categoryList.clear();

            if (categories != null && !categories.isEmpty()) {
                for (Category category : categories) {
                    Log.d("CATEGORY_DEBUG", category.getCategoryName() + " -> " + category.isStatus());
                }

                categoryList.addAll(categories);
                binding.recyclerCategories.setVisibility(View.VISIBLE);
                binding.txtEmpty.setVisibility(View.GONE);
            } else {
                binding.recyclerCategories.setVisibility(View.GONE);
                binding.txtEmpty.setVisibility(View.VISIBLE);
            }

            adapter.updateData(new ArrayList<>(categoryList));
        });

        categoryViewModel.getError().observe(getViewLifecycleOwner(), error -> {
            if (binding == null) return;

            if (!TextUtils.isEmpty(error)) {
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
                categoryViewModel.clearError();
                categoryViewModel.loadCategories();
            }
        });

        categoryViewModel.getMessage().observe(getViewLifecycleOwner(), message -> {
            if (binding == null) return;

            if (!TextUtils.isEmpty(message)) {
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
                categoryViewModel.clearMessage();
                categoryViewModel.loadCategories();
            }
        });
    }

    private String getText(com.google.android.material.textfield.TextInputEditText editText) {
        return editText.getText() != null ? editText.getText().toString().trim() : "";
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        dialogBinding = null;
        activeDialog = null;
    }
}