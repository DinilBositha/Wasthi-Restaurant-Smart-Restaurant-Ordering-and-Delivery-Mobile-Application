package com.dinilbositha.wasthirestaurant.activity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.dinilbositha.wasthirestaurant.R;
import com.dinilbositha.wasthirestaurant.databinding.ActivityMainBinding;
import com.dinilbositha.wasthirestaurant.databinding.SideNavHeaderBinding;
import com.dinilbositha.wasthirestaurant.fragment.CartFragment;
import com.dinilbositha.wasthirestaurant.fragment.HomeFragment;
import com.dinilbositha.wasthirestaurant.fragment.SearchFragment;
import com.dinilbositha.wasthirestaurant.fragment.SettingsFragment;
import com.dinilbositha.wasthirestaurant.model.User;
import com.dinilbositha.wasthirestaurant.viewmodel.CartViewModel;
import com.dinilbositha.wasthirestaurant.viewmodel.TableViewModel;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, NavigationBarView.OnItemSelectedListener {

    private static final String TAG = "MainActivity";

    private ActivityMainBinding binding;
    private DrawerLayout drawerLayout;
    private MaterialToolbar toolbar;
    private NavigationView navigationView;
    private BottomNavigationView bottomNavigationView;

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;
    private SideNavHeaderBinding sideNavHeaderBinding;

    private CartViewModel cartViewModel;
    private TableViewModel tableViewModel;

    private ListenerRegistration userStatusListener;
    private boolean forceLogoutTriggered = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        drawerLayout = binding.drawerLayout;
        toolbar = binding.toolbar;
        navigationView = binding.sideNavigationView;
        bottomNavigationView = binding.bottomNavigationView;

        cartViewModel = new ViewModelProvider(this).get(CartViewModel.class);
        tableViewModel = new ViewModelProvider(this).get(TableViewModel.class);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        View headerView = navigationView.getHeaderView(0);
        sideNavHeaderBinding = SideNavHeaderBinding.bind(headerView);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this,
                drawerLayout,
                toolbar,
                R.string.drawer_oprn,
                R.string.drawer_close
        );
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                } else {
                    finish();
                }
            }
        });

        navigationView.setNavigationItemSelectedListener(this);
        bottomNavigationView.setOnItemSelectedListener(this);

        sideNavHeaderBinding.getRoot().setOnClickListener(v -> {
            if (!isUserLoggedIn()) {
                startActivity(new Intent(MainActivity.this, SignInActivity.class));
            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 101);
        }

        updateDrawerHeader();
        updateMenuForAuthState();

        if (savedInstanceState == null) {
            loadFragment(new HomeFragment());
            clearNavSelections();
            setCheckedItems(R.id.side_nav_home, R.id.bottom_nav_home);
        }

        binding.fabQr.setOnClickListener(v -> {
            Intent intent = new Intent(this, QrScannerActivity.class);
            intent.putExtra("scan_type", "TABLE");
            qrScannerLauncher.launch(intent);
        });

        observeCurrentUserStatus();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateDrawerHeader();
        updateMenuForAuthState();
        checkCurrentUserStatusOnce();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        removeUserStatusListener();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();

        clearNavSelections();

        if (itemId == R.id.side_nav_home || itemId == R.id.bottom_nav_home) {
            loadFragment(new HomeFragment());
            setCheckedItems(R.id.side_nav_home, R.id.bottom_nav_home);

        } else if (itemId == R.id.side_nav_profile || itemId == R.id.bottom_nav_profile) {
            if (requireLogin()) return true;
            loadFragment(new SettingsFragment());
            setCheckedItems(R.id.side_nav_profile, R.id.bottom_nav_profile);

        } else if (itemId == R.id.side_nav_orders || itemId == R.id.bottom_nav_search) {
            if (requireLogin()) return true;
            loadFragment(SearchFragment.newInstance(true));
            setCheckedItems(R.id.side_nav_orders, R.id.bottom_nav_search);

        } else if (itemId == R.id.side_nav_cart || itemId == R.id.bottom_nav_cart) {
            loadFragment(CartFragment.newInstance(true));
            setCheckedItems(R.id.side_nav_cart, R.id.bottom_nav_cart);

        } else if (itemId == R.id.side_nav_wishlist) {
            if (requireLogin()) return true;
            Toast.makeText(this, "Wishlist screen", Toast.LENGTH_SHORT).show();
            setCheckedItems(R.id.side_nav_wishlist, null);

        } else if (itemId == R.id.side_nav_chat) {
            if (requireLogin()) return true;
            Toast.makeText(this, "Chat screen", Toast.LENGTH_SHORT).show();
            setCheckedItems(R.id.side_nav_chat, null);

        } else if (itemId == R.id.side_nav_setting) {
            if (requireLogin()) return true;
            loadFragment(new SettingsFragment());
            setCheckedItems(R.id.side_nav_setting, null);

        } else if (itemId == R.id.side_nav_login) {
            startActivity(new Intent(MainActivity.this, SignInActivity.class));

        } else if (itemId == R.id.side_nav_logout) {
            logoutUser(false);
        }

        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        }

        return true;
    }

    private void clearNavSelections() {
        Menu navMenu = navigationView.getMenu();
        Menu bottomMenu = bottomNavigationView.getMenu();

        for (int i = 0; i < navMenu.size(); i++) {
            navMenu.getItem(i).setChecked(false);
        }

        for (int i = 0; i < bottomMenu.size(); i++) {
            bottomMenu.getItem(i).setChecked(false);
        }
    }

    private void setCheckedItems(Integer sideNavItemId, Integer bottomNavItemId) {
        if (sideNavItemId != null) {
            MenuItem sideItem = navigationView.getMenu().findItem(sideNavItemId);
            if (sideItem != null) {
                sideItem.setChecked(true);
            }
        }

        if (bottomNavItemId != null) {
            MenuItem bottomItem = bottomNavigationView.getMenu().findItem(bottomNavItemId);
            if (bottomItem != null) {
                bottomItem.setChecked(true);
            }
        }
    }

    private boolean isUserLoggedIn() {
        return firebaseAuth != null && firebaseAuth.getCurrentUser() != null;
    }

    private boolean requireLogin() {
        if (!isUserLoggedIn()) {
            Toast.makeText(this, "Please sign in first", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(MainActivity.this, SignInActivity.class));
            return true;
        }
        return false;
    }

    private void showGuestHeader() {
        sideNavHeaderBinding.headerName.setText("Guest User");
        sideNavHeaderBinding.headerEmail.setText("Tap to sign in");
        sideNavHeaderBinding.headerProfileImage.setImageResource(R.drawable.profile);
    }

    private void updateDrawerHeader() {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();

        if (currentUser != null) {
            firebaseFirestore.collection("users")
                    .document(currentUser.getUid())
                    .get()
                    .addOnSuccessListener(ds -> {
                        if (isFinishing() || isDestroyed()) return;
                        if (ds.exists()) {
                            User user = ds.toObject(User.class);
                            if (user != null) {
                                sideNavHeaderBinding.headerName.setText(user.getName());
                                sideNavHeaderBinding.headerEmail.setText(user.getEmail());

                                Glide.with(MainActivity.this)
                                        .load(user.getProfileUrl())
                                        .placeholder(R.drawable.profile)
                                        .error(R.drawable.profile)
                                        .centerCrop()
                                        .into(sideNavHeaderBinding.headerProfileImage);
                            } else {
                                showGuestHeader();
                            }
                        } else {
                            showGuestHeader();
                        }
                    })
                    .addOnFailureListener(e -> {
                        if (isFinishing() || isDestroyed()) return;
                        Log.e(TAG, "Failed to load user header", e);
                        showGuestHeader();
                    });
        } else {
            showGuestHeader();
        }
    }

    private void updateMenuForAuthState() {
        boolean loggedIn = isUserLoggedIn();

        MenuItem loginItem = navigationView.getMenu().findItem(R.id.side_nav_login);
        MenuItem logoutItem = navigationView.getMenu().findItem(R.id.side_nav_logout);

        MenuItem profileItem = navigationView.getMenu().findItem(R.id.side_nav_profile);
        MenuItem ordersItem = navigationView.getMenu().findItem(R.id.side_nav_orders);
        MenuItem wishlistItem = navigationView.getMenu().findItem(R.id.side_nav_wishlist);
        MenuItem cartItem = navigationView.getMenu().findItem(R.id.side_nav_cart);
        MenuItem chatItem = navigationView.getMenu().findItem(R.id.side_nav_chat);
        MenuItem settingItem = navigationView.getMenu().findItem(R.id.side_nav_setting);

        if (loginItem != null) loginItem.setVisible(!loggedIn);
        if (logoutItem != null) logoutItem.setVisible(loggedIn);

        if (profileItem != null) profileItem.setVisible(loggedIn);
        if (ordersItem != null) ordersItem.setVisible(loggedIn);
        if (wishlistItem != null) wishlistItem.setVisible(loggedIn);
        if (chatItem != null) chatItem.setVisible(loggedIn);
        if (settingItem != null) settingItem.setVisible(loggedIn);

        if (cartItem != null) cartItem.setVisible(true);
    }

    private void observeCurrentUserStatus() {
        removeUserStatusListener();

        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null) {
            return;
        }

        userStatusListener = firebaseFirestore.collection("users")
                .document(currentUser.getUid())
                .addSnapshotListener((snapshot, error) -> {
                    if (isFinishing() || isDestroyed()) return;
                    if (error != null) {
                        Log.e(TAG, "User status listener error", error);
                        return;
                    }

                    if (snapshot == null || !snapshot.exists()) {
                        Log.d(TAG, "User document missing. Forcing logout.");
                        forceLogoutIfBlocked("Your account is no longer available");
                        return;
                    }

                    Boolean active = snapshot.getBoolean("active");
                    if (active == null || !active) {
                        Log.d(TAG, "User account blocked. Forcing logout.");
                        forceLogoutIfBlocked("Your account has been blocked");
                    }
                });
    }

    private void checkCurrentUserStatusOnce() {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null) {
            return;
        }

        firebaseFirestore.collection("users")
                .document(currentUser.getUid())
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (isFinishing() || isDestroyed()) return;
                    if (snapshot == null || !snapshot.exists()) {
                        forceLogoutIfBlocked("Your account is no longer available");
                        return;
                    }

                    Boolean active = snapshot.getBoolean("active");
                    if (active == null || !active) {
                        forceLogoutIfBlocked("Your account has been blocked");
                    }
                })
                .addOnFailureListener(e -> {
                    if (isFinishing() || isDestroyed()) return;
                    Log.e(TAG, "Failed to check user status", e);
                });
    }

    private void forceLogoutIfBlocked(String message) {
        if (forceLogoutTriggered) return;
        forceLogoutTriggered = true;

        removeUserStatusListener();

        try {
            cartViewModel.clearCart();
        } catch (Exception e) {
            Log.e(TAG, "Failed to clear cart", e);
        }

        try {
            tableViewModel.clearSelectedTable();
        } catch (Exception e) {
            Log.e(TAG, "Failed to clear table selection", e);
        }

        firebaseAuth.signOut();

        Toast.makeText(this, message, Toast.LENGTH_LONG).show();

        Intent intent = new Intent(MainActivity.this, SignInActivity.class);
        intent.putExtra("account_disabled", true);
        intent.putExtra("disabled_message", message);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                Intent.FLAG_ACTIVITY_CLEAR_TASK |
                Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    private void logoutUser(boolean blockedLogout) {
        removeUserStatusListener();

        try {
            cartViewModel.clearCart();
        } catch (Exception e) {
            Log.e(TAG, "Failed to clear cart during logout", e);
        }

        try {
            tableViewModel.clearSelectedTable();
        } catch (Exception e) {
            Log.e(TAG, "Failed to clear table during logout", e);
        }

        firebaseAuth.signOut();

        if (!blockedLogout) {
            Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
        }

        updateDrawerHeader();
        updateMenuForAuthState();

        clearNavSelections();
        loadFragment(new HomeFragment());
        setCheckedItems(R.id.side_nav_home, R.id.bottom_nav_home);

        Intent intent = new Intent(MainActivity.this, SignInActivity.class);
        if (blockedLogout) {
            intent.putExtra("account_disabled", true);
        }
        startActivity(intent);
        finish();
    }

    private void removeUserStatusListener() {
        if (userStatusListener != null) {
            userStatusListener.remove();
            userStatusListener = null;
        }
    }

    private final ActivityResultLauncher<Intent> qrScannerLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    String scannedValue = result.getData().getStringExtra("scanned_qr");

                    if (!TextUtils.isEmpty(scannedValue)) {
                        String tableNumber = extractTableNumber(scannedValue);

                        if (!TextUtils.isEmpty(tableNumber)) {
                            cartViewModel.setOrderType("TABLE_ORDER");
                            cartViewModel.setTableNumber(tableNumber);

                            Toast.makeText(this, "Table " + tableNumber + " selected", Toast.LENGTH_SHORT).show();

                            loadFragment(new HomeFragment());
                            clearNavSelections();
                            setCheckedItems(R.id.side_nav_home, R.id.bottom_nav_home);
                        } else {
                            Toast.makeText(this, "Invalid table QR", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });

    private String extractTableNumber(String scannedText) {
        if (TextUtils.isEmpty(scannedText)) return null;

        scannedText = scannedText.trim();

        if (scannedText.matches("\\d+")) {
            return scannedText;
        }

        String onlyDigits = scannedText.replaceAll("\\D+", "");
        if (!TextUtils.isEmpty(onlyDigits)) {
            return onlyDigits;
        }

        return null;
    }

    private void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragemenet_container, fragment);
        transaction.commit();
    }

    public void showMainNavigationUi() {
        binding.toolbar.setVisibility(View.VISIBLE);
        binding.bottomAppBar.setVisibility(View.VISIBLE);
        binding.bottomNavigationView.setVisibility(View.VISIBLE);
        binding.fabQr.setVisibility(View.VISIBLE);
    }

    public void hideMainNavigationUi() {
        binding.toolbar.setVisibility(View.GONE);
        binding.bottomAppBar.setVisibility(View.GONE);
        binding.bottomNavigationView.setVisibility(View.GONE);
        binding.fabQr.setVisibility(View.GONE);
    }

    public void showGlobalLoading() {
        hideMainNavigationUi();
        binding.logoOverlay.bringToFront();
        binding.logoOverlay.showLogo();
    }

    public void hideGlobalLoading() {
        binding.logoOverlay.hideLogo(this::showMainNavigationUi);
    }

    public void showGlobalLoadingOnly() {
        binding.logoOverlay.bringToFront();
        binding.logoOverlay.showLogo();
    }

    public void hideGlobalLoadingOnly() {
        binding.logoOverlay.hideLogo(null);
    }

    public void selectBottomNavItem(int itemId) {
        clearNavSelections();

        if (itemId == R.id.bottom_nav_home) {
            loadFragment(new HomeFragment());
            setCheckedItems(R.id.side_nav_home, R.id.bottom_nav_home);

        } else if (itemId == R.id.bottom_nav_profile) {
            if (requireLogin()) return;
            loadFragment(new SettingsFragment());
            setCheckedItems(R.id.side_nav_profile, R.id.bottom_nav_profile);

        } else if (itemId == R.id.bottom_nav_search) {
            if (requireLogin()) return;
            loadFragment(SearchFragment.newInstance(true));
            setCheckedItems(R.id.side_nav_orders, R.id.bottom_nav_search);

        } else if (itemId == R.id.bottom_nav_cart) {
            loadFragment(CartFragment.newInstance(true));
            setCheckedItems(R.id.side_nav_cart, R.id.bottom_nav_cart);
        }
    }
}