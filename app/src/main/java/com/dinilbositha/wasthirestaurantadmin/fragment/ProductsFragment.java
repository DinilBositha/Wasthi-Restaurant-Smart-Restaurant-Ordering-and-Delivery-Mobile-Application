package com.dinilbositha.wasthirestaurantadmin.fragment;

import android.app.AlertDialog;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.dinilbositha.wasthirestaurantadmin.adapter.AdminProductAdapter;
import com.dinilbositha.wasthirestaurantadmin.adapter.ImagePreviewAdapter;
import com.dinilbositha.wasthirestaurantadmin.adapter.VariantPreviewAdapter;
import com.dinilbositha.wasthirestaurantadmin.databinding.DialogAddEditProductBinding;
import com.dinilbositha.wasthirestaurantadmin.databinding.DialogAddVariantBinding;
import com.dinilbositha.wasthirestaurantadmin.databinding.FragmentProductBinding;
import com.dinilbositha.wasthirestaurantadmin.model.Category;
import com.dinilbositha.wasthirestaurantadmin.model.Product;
import com.dinilbositha.wasthirestaurantadmin.viewmodel.CategoryViewModel;
import com.dinilbositha.wasthirestaurantadmin.viewmodel.ProductViewModel;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.Timestamp;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ProductsFragment extends Fragment {

    private FragmentProductBinding binding;
    private ProductViewModel productViewModel;
    private CategoryViewModel categoryViewModel;
    private AdminProductAdapter adapter;

    private final List<Product> productList = new ArrayList<>();
    private final List<Category> categoryList = new ArrayList<>();

    private final List<Uri> selectedImageUris = new ArrayList<>();
    private final List<String> existingImageUrls = new ArrayList<>();
    private final List<Product.ProductVariant> tempVariantList = new ArrayList<>();

    private ImagePreviewAdapter imagePreviewAdapter;
    private VariantPreviewAdapter variantPreviewAdapter;

    private DialogAddEditProductBinding activeDialogBinding;
    private Product editingProduct;
    private AlertDialog activeProductDialog;
    private Category selectedCategory;

    private ArrayAdapter<String> categoryDropdownAdapter;
    private AutoCompleteTextView dropdownCategory;

    private final ActivityResultLauncher<String> imagePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    selectedImageUris.add(uri);
                    if (imagePreviewAdapter != null) {
                        imagePreviewAdapter.updateData(selectedImageUris);
                    }
                }
            });

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentProductBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        productViewModel = new ViewModelProvider(this).get(ProductViewModel.class);
        categoryViewModel = new ViewModelProvider(this).get(CategoryViewModel.class);

        setupRecyclerView();
        setupSearch();
        setupClicks();
        observeViewModel();

        categoryViewModel.loadCategories();
        productViewModel.loadProducts();
    }

    private void setupRecyclerView() {
        adapter = new AdminProductAdapter(new AdminProductAdapter.OnProductClickListener() {
            @Override
            public void onEditClick(Product product) {
                showProductDialog(product);
            }

            @Override
            public void onStatusClick(Product product) {
                if (product != null && product.getProductId() != null) {
                    boolean newStatus = !product.isStatus();

                    // instant ui update
                    product.setStatus(newStatus);
                    adapter.notifyDataSetChanged();

                    // firestore update
                    productViewModel.updateProductStatus(product.getProductId(), newStatus);
                }
            }
        });

        binding.recyclerProducts.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerProducts.setAdapter(adapter);
    }

    private void setupSearch() {
        binding.etSearchProducts.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (adapter != null) {
                    adapter.filter(s.toString());
                    updateEmptyState();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void setupClicks() {
        binding.btnBack.setOnClickListener(v ->
                requireActivity().getSupportFragmentManager().popBackStack()
        );

        binding.fabAddProduct.setOnClickListener(v -> showProductDialog(null));
    }

    private void showProductDialog(@Nullable Product product) {
        if (getContext() == null) return;

        editingProduct = product;
        selectedCategory = null;
        selectedImageUris.clear();
        existingImageUrls.clear();
        tempVariantList.clear();

        activeDialogBinding = DialogAddEditProductBinding.inflate(getLayoutInflater());
        dropdownCategory = activeDialogBinding.dropdownCategory;

        setupImagePreviewRecycler();
        setupVariantPreviewRecycler();

        boolean isEdit = product != null;

        if (isEdit) {
            activeDialogBinding.txtDialogTitle.setText("Edit Product");
            activeDialogBinding.txtDialogSubtitle.setText("Update product details");

            activeDialogBinding.etProductTitle.setText(product.getProductTitle());
            activeDialogBinding.etDescription.setText(product.getDescription());
            activeDialogBinding.etBasePrice.setText(String.valueOf(product.getBasePrice()));
            activeDialogBinding.switchProductStatus.setChecked(product.isStatus());

            if (product.getProductImage() != null) {
                existingImageUrls.addAll(product.getProductImage());
            }

            if (product.getProductVariants() != null) {
                tempVariantList.addAll(product.getProductVariants());
                variantPreviewAdapter.updateData(tempVariantList);
            }
        } else {
            activeDialogBinding.txtDialogTitle.setText("Add Product");
            activeDialogBinding.txtDialogSubtitle.setText("Enter product details below");
            activeDialogBinding.switchProductStatus.setChecked(true);
        }

        activeDialogBinding.btnSelectImages.setOnClickListener(v ->
                imagePickerLauncher.launch("image/*")
        );

        activeDialogBinding.btnManageVariants.setOnClickListener(v ->
                showAddVariantDialog()
        );

        setupCategoryDropdown();

        activeProductDialog = new AlertDialog.Builder(requireContext())
                .setView(activeDialogBinding.getRoot())
                .setPositiveButton(isEdit ? "Update" : "Add", null)
                .setNegativeButton("Cancel", null)
                .create();

        activeProductDialog.setOnShowListener(dialog -> {
            if (isEdit && product != null && !categoryList.isEmpty()) {
                setDropdownSelectionForEdit(product);
            }

            activeProductDialog.getButton(AlertDialog.BUTTON_POSITIVE)
                    .setOnClickListener(v -> validateAndSaveProduct());
        });

        activeProductDialog.show();
    }

    private void setupCategoryDropdown() {
        if (dropdownCategory == null || getContext() == null) return;

        List<String> categoryNames = new ArrayList<>();
        for (Category category : categoryList) {
            if (category != null && category.getCategoryName() != null
                    && !category.getCategoryName().trim().isEmpty()) {
                categoryNames.add(category.getCategoryName());
            }
        }

        categoryDropdownAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                categoryNames
        );

        dropdownCategory.setAdapter(categoryDropdownAdapter);
        dropdownCategory.setThreshold(0);

        dropdownCategory.setOnItemClickListener((parent, view, position, id) -> {
            if (position >= 0 && position < categoryList.size()) {
                selectedCategory = categoryList.get(position);
            } else {
                selectedCategory = null;
            }
        });

        dropdownCategory.setOnClickListener(v -> dropdownCategory.showDropDown());
        dropdownCategory.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) dropdownCategory.showDropDown();
        });
    }

    private void setDropdownSelectionForEdit(Product product) {
        if (product == null || dropdownCategory == null || categoryList.isEmpty()) return;

        for (Category category : categoryList) {
            if (category != null
                    && category.getCategoryId() != null
                    && category.getCategoryId().equals(product.getCategoryId())) {
                selectedCategory = category;
                dropdownCategory.setText(category.getCategoryName(), false);
                break;
            }
        }
    }

    private void setupImagePreviewRecycler() {
        imagePreviewAdapter = new ImagePreviewAdapter();
        activeDialogBinding.recyclerImagesPreview.setLayoutManager(
                new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        );
        activeDialogBinding.recyclerImagesPreview.setAdapter(imagePreviewAdapter);
    }

    private void setupVariantPreviewRecycler() {
        variantPreviewAdapter = new VariantPreviewAdapter();
        activeDialogBinding.recyclerVariantsPreview.setLayoutManager(
                new LinearLayoutManager(requireContext())
        );
        activeDialogBinding.recyclerVariantsPreview.setAdapter(variantPreviewAdapter);
    }

    private void showAddVariantDialog() {
        if (getContext() == null) return;

        DialogAddVariantBinding variantBinding = DialogAddVariantBinding.inflate(getLayoutInflater());

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle("Add Variant")
                .setView(variantBinding.getRoot())
                .setPositiveButton("Add", null)
                .setNegativeButton("Cancel", null)
                .create();

        dialog.setOnShowListener(d ->
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                    String variantName = getText(variantBinding.etVariantName);
                    String type = getText(variantBinding.etVariantType);
                    String priceText = getText(variantBinding.etVariantPrice);
                    String stockText = getText(variantBinding.etStockCount);
                    boolean variantStatus = variantBinding.switchVariantStatus.isChecked();

                    if (variantName.isEmpty()) {
                        variantBinding.etVariantName.requestFocus();
                    } else if (type.isEmpty()) {
                        variantBinding.etVariantType.requestFocus();
                    } else if (priceText.isEmpty()) {
                        variantBinding.etVariantPrice.requestFocus();
                    } else if (stockText.isEmpty()) {
                        variantBinding.etStockCount.requestFocus();
                    } else {
                        try {
                            double price = Double.parseDouble(priceText);
                            int stock = Integer.parseInt(stockText);

                            Product.ProductVariant variant = Product.ProductVariant.builder()
                                    .variantName(variantName)
                                    .type(type)
                                    .productPrice(price)
                                    .stockCount(stock)
                                    .variantStatus(variantStatus)
                                    .build();

                            tempVariantList.add(variant);

                            if (variantPreviewAdapter != null) {
                                variantPreviewAdapter.updateData(tempVariantList);
                            }

                            dialog.dismiss();

                        } catch (Exception e) {
                            Toast.makeText(requireContext(), "Invalid variant values", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
        );

        dialog.show();
    }

    private void validateAndSaveProduct() {
        if (activeDialogBinding == null || activeProductDialog == null) return;

        clearDialogErrors();

        String title = getText(activeDialogBinding.etProductTitle);
        String description = getText(activeDialogBinding.etDescription);
        String basePriceText = getText(activeDialogBinding.etBasePrice);
        boolean status = activeDialogBinding.switchProductStatus.isChecked();

        if (title.isEmpty()) {
            activeDialogBinding.layoutProductTitle.setError("Product title is required");
            activeDialogBinding.etProductTitle.requestFocus();
            return;
        }

        if (description.isEmpty()) {
            activeDialogBinding.layoutDescription.setError("Description is required");
            activeDialogBinding.etDescription.requestFocus();
            return;
        }

        if (selectedCategory == null) {
            Toast.makeText(requireContext(), "Please select a category", Toast.LENGTH_SHORT).show();
            return;
        }

        if (basePriceText.isEmpty()) {
            activeDialogBinding.layoutBasePrice.setError("Base price is required");
            activeDialogBinding.etBasePrice.requestFocus();
            return;
        }

        try {
            double basePrice = Double.parseDouble(basePriceText);

            binding.progressBar.setVisibility(View.VISIBLE);

            uploadImagesAndSaveProduct(
                    title,
                    description,
                    selectedCategory.getCategoryId(),
                    selectedCategory.getCategoryName(),
                    basePrice,
                    status
            );

        } catch (Exception e) {
            activeDialogBinding.layoutBasePrice.setError("Enter valid base price");
            activeDialogBinding.etBasePrice.requestFocus();
        }
    }

    private void uploadImagesAndSaveProduct(String title,
                                            String description,
                                            String categoryId,
                                            String categoryName,
                                            double basePrice,
                                            boolean status) {

        List<String> finalImageUrls = new ArrayList<>(existingImageUrls);

        if (selectedImageUris.isEmpty()) {
            saveProductToFirestore(title, description, categoryId, categoryName, basePrice, status, finalImageUrls);
            return;
        }

        StorageReference storageReference = FirebaseStorage.getInstance().getReference("foodImg");

        final int totalImages = selectedImageUris.size();
        final int[] uploadedCount = {0};

        for (Uri imageUri : selectedImageUris) {
            String fileName = UUID.randomUUID().toString() + ".jpg";
            StorageReference imageRef = storageReference.child(fileName);

            imageRef.putFile(imageUri)
                    .continueWithTask(task -> {
                        if (!task.isSuccessful()) {
                            throw task.getException();
                        }
                        return imageRef.getDownloadUrl();
                    })
                    .addOnSuccessListener(uri -> {
                        finalImageUrls.add(uri.toString());
                        uploadedCount[0]++;

                        if (uploadedCount[0] == totalImages) {
                            saveProductToFirestore(title, description, categoryId, categoryName, basePrice, status, finalImageUrls);
                        }
                    })
                    .addOnFailureListener(e -> {
                        binding.progressBar.setVisibility(View.GONE);
                        Toast.makeText(requireContext(), "Image upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();

                    });
        }
    }

    private void saveProductToFirestore(String title,
                                        String description,
                                        String categoryId,
                                        String categoryName,
                                        double basePrice,
                                        boolean status,
                                        List<String> imageUrls) {

        Product product = Product.builder()
                .productId(editingProduct != null ? editingProduct.getProductId() : null)
                .productTitle(title)
                .description(description)
                .categoryId(categoryId)
                .categoryName(categoryName)
                .productImage(imageUrls)
                .basePrice(basePrice)
                .status(status)
                .createAt(editingProduct != null && editingProduct.getCreateAt() != null
                        ? editingProduct.getCreateAt()
                        : Timestamp.now())
                .productVariants(new ArrayList<>(tempVariantList))
                .build();

        if (editingProduct != null) {
            productViewModel.updateProduct(product);
        } else {
            productViewModel.addProduct(product);
        }

        binding.progressBar.setVisibility(View.GONE);

        if (activeProductDialog != null) {
            activeProductDialog.dismiss();
        }
    }

    private void observeViewModel() {
        productViewModel.getLoading().observe(getViewLifecycleOwner(), isLoading -> {
            if (binding == null) return;
            binding.progressBar.setVisibility(Boolean.TRUE.equals(isLoading) ? View.VISIBLE : View.GONE);
        });

        productViewModel.getProducts().observe(getViewLifecycleOwner(), products -> {
            if (binding == null) return;

            productList.clear();

            if (products != null && !products.isEmpty()) {
                productList.addAll(products);
                binding.recyclerProducts.setVisibility(View.VISIBLE);
                binding.txtEmpty.setVisibility(View.GONE);
            } else {
                binding.recyclerProducts.setVisibility(View.GONE);
                binding.txtEmpty.setVisibility(View.VISIBLE);
            }

            adapter.updateData(productList);
            updateEmptyState();
        });

        categoryViewModel.getCategories().observe(getViewLifecycleOwner(), categories -> {
            categoryList.clear();

            if (categories != null && !categories.isEmpty()) {
                categoryList.addAll(categories);
            }

            if (activeDialogBinding != null && dropdownCategory != null) {
                setupCategoryDropdown();

                if (editingProduct != null) {
                    setDropdownSelectionForEdit(editingProduct);
                }
            }
        });

        productViewModel.getError().observe(getViewLifecycleOwner(), error -> {
            if (binding == null) return;

            if (!TextUtils.isEmpty(error)) {
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
                productViewModel.clearError();
                productViewModel.loadProducts();
            }
        });

        categoryViewModel.getError().observe(getViewLifecycleOwner(), error -> {
            if (binding == null) return;

            if (!TextUtils.isEmpty(error)) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
            }
        });

        productViewModel.getMessage().observe(getViewLifecycleOwner(), message -> {
            if (binding == null) return;

            if (!TextUtils.isEmpty(message)) {
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
                productViewModel.loadProducts();
                productViewModel.clearMessage();
            }
        });
    }

    private void updateEmptyState() {
        if (binding == null || adapter == null) return;

        if (adapter.getItemCount() == 0) {
            binding.recyclerProducts.setVisibility(View.GONE);
            binding.txtEmpty.setVisibility(View.VISIBLE);
        } else {
            binding.recyclerProducts.setVisibility(View.VISIBLE);
            binding.txtEmpty.setVisibility(View.GONE);
        }
    }

    private void clearDialogErrors() {
        if (activeDialogBinding == null) return;

        activeDialogBinding.layoutProductTitle.setError(null);
        activeDialogBinding.layoutDescription.setError(null);
        activeDialogBinding.layoutBasePrice.setError(null);
    }

    private String getText(TextInputEditText editText) {
        return editText.getText() != null ? editText.getText().toString().trim() : "";
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        activeDialogBinding = null;
        activeProductDialog = null;
        dropdownCategory = null;
        categoryDropdownAdapter = null;
    }
}