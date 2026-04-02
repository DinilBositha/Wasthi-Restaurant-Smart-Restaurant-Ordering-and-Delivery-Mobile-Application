package com.dinilbositha.wasthirestaurant.fragment;

import android.Manifest;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.dinilbositha.wasthirestaurant.R;
import com.dinilbositha.wasthirestaurant.activity.MainActivity;
import com.dinilbositha.wasthirestaurant.activity.QrScannerActivity;
import com.dinilbositha.wasthirestaurant.activity.SignInActivity;
import com.dinilbositha.wasthirestaurant.adapter.AddressSelectAdapter;
import com.dinilbositha.wasthirestaurant.adapter.CartAdapter;
import com.dinilbositha.wasthirestaurant.databinding.BottomSheetSelectAddressBinding;
import com.dinilbositha.wasthirestaurant.databinding.FragmentCartBinding;
import com.dinilbositha.wasthirestaurant.model.AddressModel;
import com.dinilbositha.wasthirestaurant.model.CartItem;
import com.dinilbositha.wasthirestaurant.model.CartValidationResult;
import com.dinilbositha.wasthirestaurant.model.Order;
import com.dinilbositha.wasthirestaurant.viewmodel.AddressViewModel;
import com.dinilbositha.wasthirestaurant.viewmodel.CartViewModel;
import com.dinilbositha.wasthirestaurant.viewmodel.OrderViewModel;
import com.dinilbositha.wasthirestaurant.viewmodel.TableViewModel;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

import lk.payhere.androidsdk.PHConstants;
import lk.payhere.androidsdk.PHMainActivity;
import lk.payhere.androidsdk.PHResponse;
import lk.payhere.androidsdk.model.InitRequest;
import lk.payhere.androidsdk.model.StatusResponse;

public class CartFragment extends Fragment {

    private static final String ARG_SHOW_NAV = "show_nav";
    private static final String PAYMENT_CHANNEL_ID = "payment_notifications";
    private static final int PAYMENT_NOTIFICATION_ID = 3001;

    private FragmentCartBinding binding;
    private CartViewModel cartViewModel;
    private CartAdapter cartAdapter;
    private BottomSheetBehavior<View> bottomSheetBehavior;
    private TableViewModel tableViewModel;
    private AddressViewModel addressViewModel;
    private OrderViewModel orderViewModel;

    private final List<AddressModel> addressList = new ArrayList<>();
    private String selectedAddressId = "";
    private boolean showNavigation = true;

    public CartFragment() {
    }

    public static CartFragment newInstance(boolean showNav) {
        CartFragment fragment = new CartFragment();
        Bundle args = new Bundle();
        args.putBoolean(ARG_SHOW_NAV, showNav);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            showNavigation = getArguments().getBoolean(ARG_SHOW_NAV, true);
        }
    }

    private final ActivityResultLauncher<String> cameraPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    startQrScanner();
                } else {
                    showPaymentNotification("Permission Denied", "Camera permission denied");
                }
            });

    private final ActivityResultLauncher<String> notificationPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
            });

    private final ActivityResultLauncher<Intent> qrScannerLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    String scannedValue = result.getData().getStringExtra("scanned_qr");

                    if (!TextUtils.isEmpty(scannedValue)) {
                        scannedValue = scannedValue.trim();

                        if (isValidWasthiTableQrFormat(scannedValue)) {
                            tableViewModel.validateAndSelectTable(scannedValue);
                        } else {
                            showPaymentNotification("Invalid QR", "Invalid table QR format");
                        }
                    } else {
                        showPaymentNotification("QR Error", "QR data not found");
                    }
                }
            });

    private final ActivityResultLauncher<Intent> payhereLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Intent data = result.getData();

                    if (data.hasExtra(PHConstants.INTENT_EXTRA_RESULT)) {
                        @SuppressWarnings("unchecked")
                        PHResponse<StatusResponse> response =
                                (PHResponse<StatusResponse>) data.getSerializableExtra(PHConstants.INTENT_EXTRA_RESULT);

                        if (response != null) {
                            if (response.isSuccess()) {
                                Log.i("PAYHERE", "Payment Success");
                                showPaymentNotification("Payment Successful", "Your payment was completed successfully");
                                placeOrderDirectly("Paid");
                            } else {
                                StatusResponse status = response.getData();

                                if (status != null) {
                                    Log.e("PAYHERE", status.getMessage());
                                    showPaymentNotification("Payment Failed", status.getMessage());
                                } else {
                                    showPaymentNotification("Payment Failed", "Your payment could not be completed");
                                }
                            }
                        } else {
                            showPaymentNotification("Payment Failed", "Invalid payment response");
                        }
                    } else {
                        showPaymentNotification("Payment Failed", "Payment result not found");
                    }
                } else if (result.getResultCode() == Activity.RESULT_CANCELED) {
                    showPaymentNotification("Payment Cancelled", "Your payment was cancelled");
                } else {
                    showPaymentNotification("Payment Failed", "Unable to process payment result");
                }
            });

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentCartBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        updateNavigationVisibility();

        cartViewModel = new ViewModelProvider(requireActivity()).get(CartViewModel.class);
        tableViewModel = new ViewModelProvider(requireActivity()).get(TableViewModel.class);
        addressViewModel = new ViewModelProvider(this).get(AddressViewModel.class);
        orderViewModel = new ViewModelProvider(this).get(OrderViewModel.class);

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            cartViewModel.loadCartFromFirestore();
        }

        tableViewModel.getSelectedTable().observe(getViewLifecycleOwner(), table -> {
            if (table != null) {
                cartViewModel.setTableNumber(table.getTableNumber());

                if (binding != null) {
                    binding.txtTableNumber.setText("Table " + table.getTableNumber());
                }
            }
        });

        tableViewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                showPaymentNotification("Table Error", error);
                tableViewModel.clearError();
            }
        });

        setupRecyclerView();
        setupBottomSheet();
        setupClicks();
        observeData();
        observeAddresses();
        observeOrderSaveState();

        validateCartOnOpen();
    }

    private void validateCartOnOpen() {
        cartViewModel.validateCart(new CartViewModel.ValidateCartCallback() {
            @Override
            public void onComplete(List<CartValidationResult> results) {
                boolean hasInvalid = false;
                boolean hasChanged = false;

                for (CartValidationResult result : results) {
                    if (result != null) {
                        if (!result.isValid()) hasInvalid = true;
                        if (result.isChanged()) hasChanged = true;
                    }
                }

                if (hasInvalid) {
                    showInvalidItemsDialog();
                } else if (hasChanged) {
                    showPaymentNotification("Cart Updated", "Some cart items were updated with latest product changes");
                }
            }

            @Override
            public void onFailure(Exception e) {
                Log.e("CartValidation", "Validation failed", e);
            }
        });
    }

    private void showInvalidItemsDialog() {
        if (!isAdded()) return;

        List<CartItem> invalidItems = cartViewModel.getInvalidItems();
        if (invalidItems == null || invalidItems.isEmpty()) return;

        StringBuilder message = new StringBuilder();
        for (CartItem item : invalidItems) {
            if (message.length() > 0) message.append("\n\n");
            message.append(item.getProductTitle() != null ? item.getProductTitle() : "Item")
                    .append("\n")
                    .append(item.getValidationMessage() != null ? item.getValidationMessage() : "Unavailable");
        }

        new AlertDialog.Builder(requireContext())
                .setTitle("Unavailable Cart Items")
                .setMessage(message.toString() + "\n\nRemove these items from cart?")
                .setCancelable(false)
                .setPositiveButton("Remove", (dialog, which) -> cartViewModel.removeInvalidItems())
                .setNegativeButton("Keep", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void updateNavigationVisibility() {
        if (getActivity() instanceof MainActivity) {
            if (showNavigation) {
                ((MainActivity) getActivity()).showMainNavigationUi();
            } else {
                ((MainActivity) getActivity()).hideMainNavigationUi();
            }
        }
    }

    private void observeAddresses() {
        addressViewModel.getAddresses().observe(getViewLifecycleOwner(), addresses -> {
            addressList.clear();
            if (addresses != null) {
                addressList.addAll(addresses);
            }
        });
    }

    private void observeOrderSaveState() {
        orderViewModel.getSuccess().observe(getViewLifecycleOwner(), success -> {
            if (Boolean.TRUE.equals(success)) {
                showPaymentNotification("Order Placed", "Your order has been placed successfully");
                cartViewModel.clearCart();

                if (showNavigation) {
                    if (getActivity() instanceof MainActivity) {
                        ((MainActivity) getActivity()).selectBottomNavItem(R.id.bottom_nav_home);
                    }
                } else {
                    getParentFragmentManager().popBackStack();
                }
            }
        });

        orderViewModel.getError().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                showPaymentNotification("Order Error", error);
            }
        });
    }

    private void setupRecyclerView() {
        cartAdapter = new CartAdapter(new CartAdapter.CartListener() {
            @Override
            public void onIncrease(CartItem item) {
                cartViewModel.increaseQty(item);
            }

            @Override
            public void onDecrease(CartItem item) {
                cartViewModel.decreaseQty(item);
            }

            @Override
            public void onDelete(CartItem item) {
                cartViewModel.removeItem(item);
            }
        });

        binding.recyclerCart.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerCart.setAdapter(cartAdapter);
    }

    private void setupBottomSheet() {
        bottomSheetBehavior = BottomSheetBehavior.from(binding.bottomSheetWrapper);
        bottomSheetBehavior.setFitToContents(true);
        bottomSheetBehavior.setHideable(false);
        bottomSheetBehavior.setSkipCollapsed(false);
        bottomSheetBehavior.setPeekHeight(dpToPx(160));

        bottomSheetBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if (binding == null || cartViewModel == null) return;

                List<CartItem> items = cartViewModel.getCartItems().getValue();
                boolean empty = items == null || items.isEmpty();

                if (!empty && newState == BottomSheetBehavior.STATE_HIDDEN) {
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
            }
        });

        binding.recyclerCart.post(() -> {
            if (binding == null) return;
            binding.recyclerCart.setPadding(
                    binding.recyclerCart.getPaddingLeft(),
                    binding.recyclerCart.getPaddingTop(),
                    binding.recyclerCart.getPaddingRight(),
                    dpToPx(220)
            );
            binding.recyclerCart.setClipToPadding(false);
        });
    }

    private void setupClicks() {
        binding.btnBack.setOnClickListener(v -> {
            if (showNavigation) {
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).selectBottomNavItem(R.id.bottom_nav_home);
                }
            } else {
                getParentFragmentManager().popBackStack();
            }
        });

        binding.btnTableOrder.setOnClickListener(v -> cartViewModel.setOrderType("TABLE_ORDER"));
        binding.btnDelivery.setOnClickListener(v -> cartViewModel.setOrderType("DELIVERY"));
        binding.txtScanQr.setOnClickListener(v -> checkCameraPermissionAndScan());
        binding.txtEditAddress.setOnClickListener(v -> showAddressBottomSheet());
        binding.btnPlaceOrder.setOnClickListener(v -> proceedToPayment());

        binding.btnBrowseMenu.setOnClickListener(v -> {
            if (showNavigation) {
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).selectBottomNavItem(R.id.bottom_nav_home);
                }
            } else {
                getParentFragmentManager().popBackStack();
            }
        });
    }

    private void observeData() {
        cartViewModel.getCartItems().observe(getViewLifecycleOwner(), this::renderCartItems);

        cartViewModel.getOrderType().observe(getViewLifecycleOwner(), type -> {
            updateOrderTypeUI(type);
            updateSummary();
        });

        cartViewModel.getTableNumber().observe(getViewLifecycleOwner(), table -> {
            if (binding != null) {
                if (TextUtils.isEmpty(table)) {
                    binding.txtTableNumber.setText("Not selected");
                } else {
                    binding.txtTableNumber.setText("Table " + table);
                }
            }
        });

        cartViewModel.getDeliveryAddress().observe(getViewLifecycleOwner(), address -> {
            if (binding != null) {
                if (TextUtils.isEmpty(address)) {
                    binding.txtAddress.setText("Add your delivery address");
                } else {
                    binding.txtAddress.setText(address);
                }
            }
        });
    }

    private void renderCartItems(List<CartItem> items) {
        if (binding == null) return;

        cartAdapter.submitList(items);

        boolean empty = items == null || items.isEmpty();

        binding.emptyCartView.setVisibility(empty ? View.VISIBLE : View.GONE);
        binding.recyclerCart.setVisibility(empty ? View.GONE : View.VISIBLE);
        binding.txtItemCount.setText(String.valueOf(empty ? 0 : items.size()));

        if (empty) {
            binding.bottomSheetWrapper.setVisibility(View.GONE);
        } else {
            binding.bottomSheetWrapper.setVisibility(View.VISIBLE);

            binding.bottomSheetWrapper.post(() -> {
                if (binding != null) {
                    bottomSheetBehavior.setPeekHeight(dpToPx(160));

                    int state = bottomSheetBehavior.getState();
                    if (state != BottomSheetBehavior.STATE_EXPANDED &&
                            state != BottomSheetBehavior.STATE_COLLAPSED) {
                        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                    }
                }
            });
        }

        updateSummary();
    }

    private void updateOrderTypeUI(String type) {
        if (binding == null) return;

        boolean isDelivery = "DELIVERY".equals(type);

        if (isDelivery) {
            binding.tableOrderSection.setVisibility(View.GONE);
            binding.deliverySection.setVisibility(View.VISIBLE);
            binding.deliveryFeeRow.setVisibility(View.VISIBLE);

            animateToggleSelect(binding.btnDelivery, true);
            animateToggleSelect(binding.btnTableOrder, false);
        } else {
            binding.deliverySection.setVisibility(View.GONE);
            binding.tableOrderSection.setVisibility(View.VISIBLE);
            binding.deliveryFeeRow.setVisibility(View.GONE);

            animateToggleSelect(binding.btnTableOrder, true);
            animateToggleSelect(binding.btnDelivery, false);
        }

        binding.bottomSheetWrapper.post(() -> {
            if (binding != null && binding.bottomSheetWrapper.getVisibility() == View.VISIBLE) {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            }
        });
    }

    private void animateToggleSelect(TextView button, boolean selected) {
        if (!isAdded()) return;

        int fromColor = selected ? Color.parseColor("#C70000") : Color.parseColor("#C70000");
        int toColor = selected ? Color.parseColor("#C70000") : Color.parseColor("#F0F0F5");

        int fromTextColor = selected ? Color.parseColor("#7E849D") : Color.WHITE;
        int toTextColor = selected ? Color.WHITE : Color.parseColor("#7E849D");

        final float cornerRadius = getResources().getDimension(R.dimen.toggle_button_corner_radius);

        ValueAnimator colorAnimator = ValueAnimator.ofObject(new ArgbEvaluator(), fromColor, toColor);
        colorAnimator.setDuration(250);
        colorAnimator.addUpdateListener(animator -> {
            if (isAdded()) {
                GradientDrawable drawable = new GradientDrawable();
                drawable.setColor((int) animator.getAnimatedValue());
                drawable.setCornerRadius(cornerRadius);
                button.setBackground(drawable);
            }
        });

        ValueAnimator textAnimator = ValueAnimator.ofObject(new ArgbEvaluator(), fromTextColor, toTextColor);
        textAnimator.setDuration(250);
        textAnimator.addUpdateListener(animator -> {
            if (isAdded()) {
                button.setTextColor((int) animator.getAnimatedValue());
            }
        });

        colorAnimator.start();
        textAnimator.start();
    }

    private void showAddressBottomSheet() {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Intent intent = new Intent(requireContext(), SignInActivity.class);
            intent.putExtra("open_cart_after_login", true);
            startActivity(intent);
        } else {
            BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
            BottomSheetSelectAddressBinding sheetBinding =
                    BottomSheetSelectAddressBinding.inflate(getLayoutInflater());

            dialog.setContentView(sheetBinding.getRoot());
            sheetBinding.recyclerAddresses.setLayoutManager(new LinearLayoutManager(requireContext()));

            if (addressList.isEmpty()) {
                sheetBinding.txtEmptyAddress.setVisibility(View.VISIBLE);
                sheetBinding.recyclerAddresses.setVisibility(View.GONE);
            } else {
                sheetBinding.txtEmptyAddress.setVisibility(View.GONE);
                sheetBinding.recyclerAddresses.setVisibility(View.VISIBLE);

                AddressSelectAdapter adapter = new AddressSelectAdapter(
                        addressList,
                        selectedAddressId,
                        addressModel -> {
                            selectedAddressId = addressModel.getId();
                            cartViewModel.setDeliveryAddress(addressModel.getFormattedAddress());
                            dialog.dismiss();
                        }
                );

                sheetBinding.recyclerAddresses.setAdapter(adapter);
            }

            sheetBinding.btnAddNewAddress.setOnClickListener(v -> {
                dialog.dismiss();

                requireActivity()
                        .getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragemenet_container, new AddAddressFragment())
                        .addToBackStack(null)
                        .commit();
            });

            dialog.show();
        }
    }

    private void checkCameraPermissionAndScan() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            startQrScanner();
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    private void startQrScanner() {
        Intent intent = new Intent(requireContext(), QrScannerActivity.class);
        intent.putExtra("scan_type", "TABLE");
        qrScannerLauncher.launch(intent);
    }

    private boolean isValidWasthiTableQrFormat(String qrText) {
        if (TextUtils.isEmpty(qrText)) {
            return false;
        }
        return qrText.matches("^WASTHI_TABLE_\\d+$");
    }

    private void updateSummary() {
        if (binding != null) {
            binding.txtSubtotal.setText(String.format("LKR %.2f", cartViewModel.getSubtotal()));
            binding.txtTax.setText(String.format("LKR %.2f", cartViewModel.getTax()));
            binding.txtDeliveryFee.setText(String.format("LKR %.2f", cartViewModel.getDeliveryFee()));
            binding.txtTotal.setText(String.format("LKR %.2f", cartViewModel.getTotal()));
        }
    }

    private void proceedToPayment() {
        cartViewModel.validateCart(new CartViewModel.ValidateCartCallback() {
            @Override
            public void onComplete(List<CartValidationResult> results) {
                boolean hasInvalid = false;
                boolean hasChanged = false;

                for (CartValidationResult result : results) {
                    if (result != null) {
                        if (!result.isValid()) hasInvalid = true;
                        if (result.isChanged()) hasChanged = true;
                    }
                }

                if (hasInvalid) {
                    showInvalidItemsDialog();
                    showPaymentNotification("Cart Issue", "Some products are unavailable. Remove them before checkout");
                    return;
                }

                if (hasChanged) {
                    showPaymentNotification("Cart Updated", "Prices/items updated. Please review and checkout again");
                    return;
                }

                continuePaymentFlow();
            }

            @Override
            public void onFailure(Exception e) {
                showPaymentNotification("Validation Failed", "Unable to validate cart. Please try again");
            }
        });
    }

    private void continuePaymentFlow() {
        String type = cartViewModel.getOrderType().getValue();

        if ("TABLE_ORDER".equals(type)) {
            String table = cartViewModel.getTableNumber().getValue();

            if (TextUtils.isEmpty(table)) {
                showPaymentNotification("Table Required", "Please scan table QR");
            } else {
                startPayHerePayment();
            }
        } else if ("DELIVERY".equals(type)) {
            String address = cartViewModel.getDeliveryAddress().getValue();

            if (TextUtils.isEmpty(address)) {
                showPaymentNotification("Address Required", "Please enter delivery address");
            } else {
                startPayHerePayment();
            }
        } else {
            startPayHerePayment();
        }
    }

    private void startPayHerePayment() {
        String generatedOrderId = "ORD-" + System.currentTimeMillis();
        double totalAmount = cartViewModel.getTotal();

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        if (firebaseUser == null) {
            showPaymentNotification("Login Required", "Please login first");
        } else {
            InitRequest req = new InitRequest();
            req.setSandBox(true);
            req.setMerchantId("1231512");
            req.setMerchantSecret("MjI0MTEzNDA4MDMyMzI3NTUzNTAyNzA1MzEyMjQ4MTcxOTg2OTU2OQ==");
            req.setCurrency("LKR");
            req.setAmount(totalAmount);
            req.setOrderId(generatedOrderId);
            req.setItemsDescription("Restaurant Order");

            String fullName = firebaseUser.getDisplayName();
            if (TextUtils.isEmpty(fullName)) {
                fullName = "Customer";
            }

            String firstName = fullName;
            String lastName = "User";

            if (fullName.contains(" ")) {
                int lastSpace = fullName.lastIndexOf(" ");
                lastName = fullName.substring(lastSpace + 1);
                firstName = fullName.substring(0, lastSpace);
            }

            req.getCustomer().setFirstName(firstName);
            req.getCustomer().setLastName(lastName);
            req.getCustomer().setEmail(firebaseUser.getEmail() != null ? firebaseUser.getEmail() : "customer@email.com");
            req.getCustomer().setPhone(firebaseUser.getPhoneNumber() != null ? firebaseUser.getPhoneNumber() : "0770000000");

            String address = cartViewModel.getDeliveryAddress().getValue();
            if (TextUtils.isEmpty(address)) {
                address = "Restaurant Table Order";
            }

            req.getCustomer().getAddress().setAddress(address);
            req.getCustomer().getAddress().setCity("Colombo");
            req.getCustomer().getAddress().setCountry("Sri Lanka");
            req.setNotifyUrl("https://your-requestcatcher-url.com/payhere");

            Intent intent = new Intent(getContext(), PHMainActivity.class);
            intent.putExtra(PHConstants.INTENT_EXTRA_DATA, req);
            payhereLauncher.launch(intent);
        }
    }

    private void placeOrderDirectly(String paymentStatus) {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        if (firebaseUser == null) {
            showPaymentNotification("Login Required", "Please login first");
        } else {
            List<CartItem> cartItemList = cartViewModel.getCartItems().getValue();

            if (cartItemList == null || cartItemList.isEmpty()) {
                showPaymentNotification("Cart Empty", "Cart is empty");
            } else {
                String orderId = "ORD-" + System.currentTimeMillis();

                String fullName = firebaseUser.getDisplayName();
                if (TextUtils.isEmpty(fullName)) {
                    fullName = "Customer";
                }

                String email = firebaseUser.getEmail();
                if (TextUtils.isEmpty(email)) {
                    email = "";
                }

                String type = cartViewModel.getOrderType().getValue();
                String table = cartViewModel.getTableNumber().getValue();
                String address = cartViewModel.getDeliveryAddress().getValue();

                double latitude = 0.0;
                double longitude = 0.0;

                if ("DELIVERY".equals(type)) {
                    for (AddressModel addressModel : addressList) {
                        if (addressModel != null
                                && addressModel.getId() != null
                                && addressModel.getId().equals(selectedAddressId)) {
                            if (addressModel.getLatitude() != null) {
                                latitude = addressModel.getLatitude();
                            }
                            if (addressModel.getLongitude() != null) {
                                longitude = addressModel.getLongitude();
                            }
                            break;
                        }
                    }
                }

                Order order = Order.builder()
                        .orderId(orderId)
                        .userId(firebaseUser.getUid())
                        .customerName(fullName)
                        .customerEmail(email)
                        .orderType(type != null ? type : "TABLE_ORDER")
                        .tableNumber(table != null ? table : "")
                        .deliveryAddress(address != null ? address : "")
                        .latitude(latitude)
                        .longitude(longitude)
                        .paymentStatus(paymentStatus)
                        .orderStatus("Pending")
                        .subtotal(cartViewModel.getSubtotal())
                        .tax(cartViewModel.getTax())
                        .deliveryFee(cartViewModel.getDeliveryFee())
                        .total(cartViewModel.getTotal())
                        .items(new ArrayList<>(cartItemList))
                        .createdAt(System.currentTimeMillis())
                        .build();

                orderViewModel.saveOrder(order);
            }
        }
    }

    private void showPaymentNotification(String title, String message) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS)
                    == PackageManager.PERMISSION_GRANTED) {
                sendPaymentNotification(title, message);
            } else {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        } else {
            sendPaymentNotification(title, message);
        }
    }

    private void createPaymentNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    PAYMENT_CHANNEL_ID,
                    "Payment Notifications",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Notifications for payment and order status");
            channel.enableLights(true);
            channel.enableVibration(true);

            NotificationManager notificationManager =
                    requireContext().getSystemService(NotificationManager.class);

            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    private void sendPaymentNotification(String title, String message) {
        Context context = requireContext();

        createPaymentNotificationChannel();

        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, PAYMENT_CHANNEL_ID)
                .setSmallIcon(R.drawable.app_logo)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        try {
            NotificationManagerCompat.from(context).notify(PAYMENT_NOTIFICATION_ID, builder.build());
        } catch (SecurityException e) {
            Log.e("PaymentNotification", "Notification permission denied", e);
        }
    }

    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }

    @Override
    public void onResume() {
        super.onResume();
        updateNavigationVisibility();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}