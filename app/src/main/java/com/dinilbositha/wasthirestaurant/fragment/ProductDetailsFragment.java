package com.dinilbositha.wasthirestaurant.fragment;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.dinilbositha.wasthirestaurant.R;
import com.dinilbositha.wasthirestaurant.activity.MainActivity;
import com.dinilbositha.wasthirestaurant.adapter.MoreProductsAdapter;
import com.dinilbositha.wasthirestaurant.adapter.ProductSliderAdapter;
import com.dinilbositha.wasthirestaurant.adapter.RelatedProductsAdapter;
import com.dinilbositha.wasthirestaurant.databinding.FragmentProductDetailsBinding;
import com.dinilbositha.wasthirestaurant.model.CartItem;
import com.dinilbositha.wasthirestaurant.model.Product;
import com.dinilbositha.wasthirestaurant.utils.NetworkUtil;
import com.dinilbositha.wasthirestaurant.viewmodel.CartViewModel;
import com.dinilbositha.wasthirestaurant.viewmodel.ProductViewModel;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProductDetailsFragment extends Fragment {

    private static final String ARG_PRODUCT_ID = "productId";
    private static final int MAX_QTY = 10;
    private static final String CHANNEL_ID = "cart_channel";
    private static final int NOTIFICATION_ID = 2001;

    private String productId;
    private FragmentProductDetailsBinding binding;
    private Product currentProduct;
    private View bottomSheetView;

    private final Map<String, List<Product.ProductVariant>> selectedVariantsMap = new HashMap<>();

    private CartViewModel cartViewModel;
    private ProductViewModel productViewModel;
    private int quantity = 1;

    private MoreProductsAdapter moreProductsAdapter;
    private final List<Product> moreProductsList = new ArrayList<>();

    public static ProductDetailsFragment newInstance(String productId) {
        ProductDetailsFragment fragment = new ProductDetailsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PRODUCT_ID, productId);
        fragment.setArguments(args);
        return fragment;
    }

    public ProductDetailsFragment() {
    }

    private final ActivityResultLauncher<String> requestNotificationPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    showCartNotification();
                }
            });

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            productId = getArguments().getString(ARG_PRODUCT_ID);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentProductDetailsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        cartViewModel = new ViewModelProvider(requireActivity()).get(CartViewModel.class);
        productViewModel = new ViewModelProvider(this).get(ProductViewModel.class);

        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).hideMainNavigationUi();
        }

        requireActivity().getOnBackPressedDispatcher().addCallback(
                getViewLifecycleOwner(),
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        if (getActivity() instanceof MainActivity) {
                            ((MainActivity) getActivity()).showMainNavigationUi();
                        }
                        requireActivity().getSupportFragmentManager().popBackStack();
                    }
                }
        );

        binding.btnBack.setOnClickListener(v ->
                requireActivity().getOnBackPressedDispatcher().onBackPressed()
        );
        binding.btnRetryNetwork.setOnClickListener(v -> {
            if (isNetworkAvailable()) {
                hideNetworkLostBar();
            } else {
                showNetworkLostBar();
            }
        });
        setupMoreProductsRecycler();
        loadProductDetails();
    }

    private void loadProductDetails() {
        productViewModel.getProductById(productId).observe(getViewLifecycleOwner(), product -> {
            if (binding == null) return;

            if (product != null) {
                currentProduct = product;
                displayProductData(product);
                loadRelatedProducts(product.getCategoryId(), product.getProductId());
                loadMoreProducts(product.getProductId(), product.getCategoryId());
            } else {
                Log.e("ProductDetails", "No product found for id: " + productId);
            }
        });
    }

    private void displayProductData(Product product) {
        if (binding == null) return;

        ProductSliderAdapter adapter = new ProductSliderAdapter(product.getProductImage());
        binding.productDetailsImg.setAdapter(adapter);
        binding.dotsIndicator.attachTo(binding.productDetailsImg);

        binding.productDetailsPname.setText(product.getProductTitle());
        binding.productDetailsPrice.setText(String.format("LKR %.2f", product.getBasePrice()));
        binding.productDetailsDesc.setText(product.getDescription());
        binding.productDetailsCname.setText(product.getCategoryName() != null ? product.getCategoryName() : "");

        if (!product.isStatus()) {
            binding.btnAddToCart.setEnabled(false);
            binding.btnAddToCart.setAlpha(0.5f);
            binding.btnAddToCart.setText("Currently Unavailable");
        } else {
            binding.btnAddToCart.setEnabled(true);
            binding.btnAddToCart.setAlpha(1.0f);
            binding.btnAddToCart.setText("Add to Cart");

            binding.btnAddToCart.setOnClickListener(v -> {
                if (!isNetworkAvailable()) {
                    showNetworkLostBar();
                    return;
                }

                hideNetworkLostBar();
                if (currentProduct != null) {
                    showVariantBottomSheet(currentProduct);
                }
            });
        }
    }

    private void loadRelatedProducts(String categoryId, String currentProductId) {
        if (binding == null) return;

        if (categoryId == null || categoryId.isEmpty()) {
            binding.txtRelatedProducts.setVisibility(View.GONE);
            binding.recyclerRelatedProducts.setVisibility(View.GONE);
            return;
        }

        productViewModel.getRelatedProducts(categoryId, currentProductId)
                .observe(getViewLifecycleOwner(), relatedList -> {
                    if (binding == null) return;
                    setupRelatedProducts(relatedList);
                });
    }

    private void setupRelatedProducts(List<Product> relatedProducts) {
        if (binding == null) return;

        if (relatedProducts == null || relatedProducts.isEmpty()) {
            binding.txtRelatedProducts.setVisibility(View.GONE);
            binding.recyclerRelatedProducts.setVisibility(View.GONE);
            return;
        }

        binding.txtRelatedProducts.setVisibility(View.VISIBLE);
        binding.recyclerRelatedProducts.setVisibility(View.VISIBLE);

        RelatedProductsAdapter adapter = new RelatedProductsAdapter(relatedProducts, product -> {
            if (product == null || product.getProductId() == null) return;

            ProductDetailsFragment fragment = ProductDetailsFragment.newInstance(product.getProductId());

            getParentFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragemenet_container, fragment)
                    .addToBackStack(null)
                    .commit();
        });

        binding.recyclerRelatedProducts.setLayoutManager(
                new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        );
        binding.recyclerRelatedProducts.setHasFixedSize(true);
        binding.recyclerRelatedProducts.setNestedScrollingEnabled(false);
        binding.recyclerRelatedProducts.setAdapter(adapter);
    }

    private void setupMoreProductsRecycler() {
        if (binding == null) return;

        GridLayoutManager gridLayoutManager = new GridLayoutManager(requireContext(), 2);
        binding.recyclerMoreProducts.setLayoutManager(gridLayoutManager);
        binding.recyclerMoreProducts.setNestedScrollingEnabled(false);
        binding.recyclerMoreProducts.setHasFixedSize(true);

        moreProductsAdapter = new MoreProductsAdapter(moreProductsList, product -> {
            if (product == null || product.getProductId() == null) return;

            ProductDetailsFragment fragment = ProductDetailsFragment.newInstance(product.getProductId());

            getParentFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragemenet_container, fragment)
                    .addToBackStack(null)
                    .commit();
        });

        binding.recyclerMoreProducts.setAdapter(moreProductsAdapter);
    }

    private void loadMoreProducts(String currentProductId, String currentCategoryId) {
        productViewModel.getMoreProducts(currentProductId, currentCategoryId)
                .observe(getViewLifecycleOwner(), moreList -> {
                    if (binding == null) return;

                    if (moreList == null || moreList.isEmpty()) {
                        binding.txtMoreProducts.setVisibility(View.GONE);
                        binding.recyclerMoreProducts.setVisibility(View.GONE);
                        return;
                    }

                    moreProductsList.clear();
                    moreProductsList.addAll(moreList);
                    moreProductsAdapter.notifyDataSetChanged();

                    binding.txtMoreProducts.setVisibility(View.VISIBLE);
                    binding.recyclerMoreProducts.setVisibility(View.VISIBLE);
                });
    }

    private void displayProductVariants(Product product, ViewGroup container) {
        if (product.getProductVariants() == null || product.getProductVariants().isEmpty()) {
            return;
        }

        Map<String, List<Product.ProductVariant>> groupedVariants = new HashMap<>();

        for (Product.ProductVariant variant : product.getProductVariants()) {
            if (variant == null || variant.getType() == null) continue;
            if (!variant.isVariantStatus()) continue;

            if (!groupedVariants.containsKey(variant.getType())) {
                groupedVariants.put(variant.getType(), new ArrayList<>());
            }
            groupedVariants.get(variant.getType()).add(variant);
        }

        for (Map.Entry<String, List<Product.ProductVariant>> entry : groupedVariants.entrySet()) {
            renderVariant(entry.getKey(), entry.getValue(), container);
        }
    }

    private void renderVariant(String typeName, List<Product.ProductVariant> variants, ViewGroup container) {
        if (variants == null || variants.isEmpty()) return;

        boolean isRequired = variants.get(0).isRequired();
        boolean isMultiSelect = variants.get(0).isMultiSelect();

        LinearLayout row = new LinearLayout(getContext());
        row.setOrientation(LinearLayout.VERTICAL);
        row.setPadding(10, 10, 10, 10);

        TextView header = new TextView(getContext());
        header.setText(isRequired ? "Select " + typeName + " *" : "Select " + typeName + " (Optional)");
        row.addView(header);

        ChipGroup chipGroup = new ChipGroup(getContext());
        chipGroup.setSingleSelection(!isMultiSelect);
        chipGroup.setSelectionRequired(false);
        chipGroup.setChipSpacingHorizontal(12);
        chipGroup.setChipSpacingVertical(12);

        int selectedBgColor = Color.parseColor("#C70000");
        int unselectedBgColor = Color.parseColor("#F5F5F5");
        int selectedTextColor = Color.WHITE;
        int unselectedTextColor = Color.parseColor("#333333");

        for (Product.ProductVariant variant : variants) {
            if (variant == null) continue;
            if (!variant.isVariantStatus()) continue;

            Chip chip = new Chip(getContext());
            chip.setText(variant.getVariantName());
            chip.setCheckable(true);
            chip.setChipStrokeWidth(0f);
            chip.setEnsureMinTouchTargetSize(false);

            chip.setChipBackgroundColor(ColorStateList.valueOf(unselectedBgColor));
            chip.setTextColor(unselectedTextColor);

            chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    chip.setChipBackgroundColor(ColorStateList.valueOf(selectedBgColor));
                    chip.setTextColor(selectedTextColor);

                    if (isMultiSelect) {
                        List<Product.ProductVariant> selectedList = selectedVariantsMap.get(typeName);
                        if (selectedList == null) {
                            selectedList = new ArrayList<>();
                        }

                        if (!selectedList.contains(variant)) {
                            selectedList.add(variant);
                        }

                        selectedVariantsMap.put(typeName, selectedList);
                    } else {
                        List<Product.ProductVariant> selectedList = new ArrayList<>();
                        selectedList.add(variant);
                        selectedVariantsMap.put(typeName, selectedList);
                    }

                    updateUI(bottomSheetView);
                    updateConfirmButtonState(currentProduct);

                } else {
                    chip.setChipBackgroundColor(ColorStateList.valueOf(unselectedBgColor));
                    chip.setTextColor(unselectedTextColor);

                    List<Product.ProductVariant> selectedList = selectedVariantsMap.get(typeName);
                    if (selectedList != null) {
                        selectedList.remove(variant);

                        if (selectedList.isEmpty()) {
                            selectedVariantsMap.remove(typeName);
                        } else {
                            selectedVariantsMap.put(typeName, selectedList);
                        }
                    }

                    updateUI(bottomSheetView);
                    updateConfirmButtonState(currentProduct);
                }
            });

            chipGroup.addView(chip);
        }

        row.addView(chipGroup);
        container.addView(row);
    }

    private boolean areRequiredVariantsSelected(Product product) {
        if (product == null || product.getProductVariants() == null || product.getProductVariants().isEmpty()) {
            return true;
        }

        Map<String, Boolean> requiredTypes = new HashMap<>();

        for (Product.ProductVariant variant : product.getProductVariants()) {
            if (variant == null || variant.getType() == null) continue;
            if (!variant.isVariantStatus()) continue;

            if (variant.isRequired()) {
                requiredTypes.put(variant.getType(), false);
            }
        }

        for (String requiredType : requiredTypes.keySet()) {
            List<Product.ProductVariant> selectedList = selectedVariantsMap.get(requiredType);
            if (selectedList != null && !selectedList.isEmpty()) {
                requiredTypes.put(requiredType, true);
            }
        }

        for (boolean selected : requiredTypes.values()) {
            if (!selected) return false;
        }

        return true;
    }

    private void updateConfirmButtonState(Product product) {
        if (bottomSheetView == null) return;

        AppCompatButton btnConfirm = bottomSheetView.findViewById(R.id.btn_confirm_add);
        if (btnConfirm == null) return;

        boolean enable = areRequiredVariantsSelected(product);
        btnConfirm.setEnabled(enable);
        btnConfirm.setAlpha(enable ? 1.0f : 0.5f);
    }

    private List<CartItem.SelectedVariant> getSelectedVariantsList() {
        List<CartItem.SelectedVariant> list = new ArrayList<>();

        for (List<Product.ProductVariant> variants : selectedVariantsMap.values()) {
            if (variants != null) {
                for (Product.ProductVariant variant : variants) {
                    if (variant != null) {
                        list.add(CartItem.SelectedVariant.builder()
                                .type(variant.getType())
                                .name(variant.getVariantName())
                                .extraPrice(variant.getProductPrice())
                                .build());
                    }
                }
            }
        }

        return list;
    }

    private double getTotalVariantExtraPrice() {
        double total = 0;

        for (List<Product.ProductVariant> variants : selectedVariantsMap.values()) {
            if (variants != null) {
                for (Product.ProductVariant variant : variants) {
                    if (variant != null) {
                        total += variant.getProductPrice();
                    }
                }
            }
        }

        return total;
    }

    private void showVariantBottomSheet(Product product) {
        if (!isAdded() || getContext() == null) return;

        selectedVariantsMap.clear();
        quantity = 1;

        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(requireContext());
        bottomSheetView = getLayoutInflater().inflate(R.layout.variant_selector_view, null);
        bottomSheetDialog.setContentView(bottomSheetView);

        ImageView closeBtn = bottomSheetView.findViewById(R.id.btn_close_bottom_sheet);
        if (closeBtn != null) {
            closeBtn.setOnClickListener(v -> bottomSheetDialog.dismiss());
        }

        TextView productName = bottomSheetView.findViewById(R.id.variant_selector_food_name);
        TextView productDesc = bottomSheetView.findViewById(R.id.variant_selector_food_desc);
        TextView productPrice = bottomSheetView.findViewById(R.id.variant_selector_food_price);
        ImageView productImg = bottomSheetView.findViewById(R.id.variant_selector_img);
        TextView stockText = bottomSheetView.findViewById(R.id.txt_stock_count);

        if (productName != null) productName.setText(product.getProductTitle());
        if (productDesc != null) productDesc.setText(product.getDescription());
        if (productPrice != null) productPrice.setText(String.format("LKR %.2f", product.getBasePrice()));
        if (stockText != null) {
            stockText.setText("Customize your item");
            stockText.setTextColor(Color.parseColor("#757575"));
        }

        if (productImg != null && product.getProductImage() != null && !product.getProductImage().isEmpty()) {
            Glide.with(this)
                    .load(product.getProductImage().get(0))
                    .into(productImg);
        }

        LinearLayout variantContainer = bottomSheetView.findViewById(R.id.variantContainer);
        if (variantContainer != null) {
            variantContainer.removeAllViews();
        }

        AppCompatButton btnConfirm = bottomSheetView.findViewById(R.id.btn_confirm_add);
        AppCompatButton btnPlus = bottomSheetView.findViewById(R.id.btn_qty_plus);
        AppCompatButton btnMinus = bottomSheetView.findViewById(R.id.btn_qty_minus);

        if (btnConfirm != null) {
            boolean enabled = areRequiredVariantsSelected(product);
            btnConfirm.setEnabled(enabled);
            btnConfirm.setAlpha(enabled ? 1.0f : 0.5f);
        }

        updateUI(bottomSheetView);

        if (btnPlus != null) {
            btnPlus.setOnClickListener(v -> {
                if (quantity < MAX_QTY) {
                    quantity++;
                    updateUI(bottomSheetView);
                }
            });
        }

        if (btnMinus != null) {
            btnMinus.setOnClickListener(v -> {
                if (quantity > 1) {
                    quantity--;
                    updateUI(bottomSheetView);
                }
            });
        }

        if (btnConfirm != null) {
            btnConfirm.setOnClickListener(v -> {
                if (product == null) {
                    Log.e("CartError", "Product is null");
                    return;
                }

                if (!areRequiredVariantsSelected(product)) {
                    Log.e("CartError", "Please select required variants");
                    return;
                }

                String imageUrl = "";
                if (product.getProductImage() != null && !product.getProductImage().isEmpty()) {
                    imageUrl = product.getProductImage().get(0);
                }

                double finalUnitPrice = product.getBasePrice() + getTotalVariantExtraPrice();

                CartItem cartItem = CartItem.builder()
                        .productId(product.getProductId())
                        .productTitle(product.getProductTitle())
                        .productImage(imageUrl)
                        .selectedVariants(getSelectedVariantsList())
                        .unitPrice(finalUnitPrice)
                        .quantity(quantity)
                        .build();

                cartViewModel.addToCart(cartItem);

                checkNotificationPermissionAndShow();

                bottomSheetDialog.dismiss();

                if (isAdded()) {
                    getParentFragmentManager()
                            .beginTransaction()
                            .replace(R.id.fragemenet_container, CartFragment.newInstance(false))
                            .addToBackStack(null)
                            .commit();
                }
            });
        }

        if (variantContainer != null) {
            displayProductVariants(product, variantContainer);
        }

        updateConfirmButtonState(product);
        bottomSheetDialog.show();
    }

    private void updateUI(View bottomSheetView) {
        if (bottomSheetView == null) return;

        AppCompatButton btnPlus = bottomSheetView.findViewById(R.id.btn_qty_plus);
        AppCompatButton btnMinus = bottomSheetView.findViewById(R.id.btn_qty_minus);
        TextView qtyNum = bottomSheetView.findViewById(R.id.variant_food_qty);
        TextView foodPrice = bottomSheetView.findViewById(R.id.variant_selector_food_price);

        if (qtyNum != null) {
            qtyNum.setText(String.valueOf(quantity));
        }

        if (btnMinus != null) {
            btnMinus.setEnabled(quantity > 1);
            btnMinus.setAlpha(quantity > 1 ? 1.0f : 0.3f);
        }

        boolean canAddMore = quantity < MAX_QTY;

        if (btnPlus != null) {
            btnPlus.setEnabled(canAddMore);
            btnPlus.setAlpha(canAddMore ? 1.0f : 0.3f);
        }

        if (foodPrice != null && currentProduct != null) {
            double unitPrice = currentProduct.getBasePrice() + getTotalVariantExtraPrice();
            double total = unitPrice * quantity;
            foodPrice.setText(String.format("LKR %.2f", total));
        }
    }

    private void checkNotificationPermissionAndShow() {
        if (!isAdded() || getContext() == null) return;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS)
                    == PackageManager.PERMISSION_GRANTED) {
                showCartNotification();
            } else {
                requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        } else {
            showCartNotification();
        }
    }

    private void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Cart Notifications",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Notifications for cart updates");
            channel.enableLights(true);
            channel.enableVibration(true);

            NotificationManager notificationManager =
                    context.getSystemService(NotificationManager.class);

            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    private void showCartNotification() {
        if (!isAdded() || getContext() == null) return;
        Context context = requireContext();

        createNotificationChannel(context);

        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra("open_cart", true);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        String productTitle = currentProduct != null ? currentProduct.getProductTitle() : "Item";

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.app_logo)
                .setContentTitle("Added to Cart")
                .setContentText(productTitle + " has been added to your cart")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, builder.build());
        } catch (SecurityException e) {
            Log.e("NotificationError", "Notification permission denied", e);
        }
    }
    private boolean isNetworkAvailable() {
        return NetworkUtil.isNetworkAvailable(requireContext());
    }

    private void showNetworkLostBar() {
        if (binding == null) return;

        binding.networkLostBar.setVisibility(View.VISIBLE);
        binding.networkLostBar.setAlpha(0f);
        binding.networkLostBar.setTranslationY(100f);

        binding.networkLostBar.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(250)
                .start();

        binding.btnAddToCart.setEnabled(false);
        binding.btnAddToCart.setAlpha(0.5f);
    }

    private void hideNetworkLostBar() {
        if (binding == null) return;

        binding.networkLostBar.animate()
                .alpha(0f)
                .translationY(100f)
                .setDuration(200)
                .withEndAction(() -> binding.networkLostBar.setVisibility(View.GONE))
                .start();

        binding.btnAddToCart.setEnabled(true);
        binding.btnAddToCart.setAlpha(1f);
    }

    private void checkNetworkStateForButton() {
        if (!isNetworkAvailable()) {
            showNetworkLostBar();
        } else {
            hideNetworkLostBar();
        }
    }
    @Override
    public void onResume() {
        super.onResume();
        checkNetworkStateForButton();
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).hideMainNavigationUi();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).showMainNavigationUi();
        }

        binding = null;
    }
}
