package com.dinilbositha.wasthirestaurant.fragment;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.dinilbositha.wasthirestaurant.R;
import com.dinilbositha.wasthirestaurant.activity.MainActivity;
import com.dinilbositha.wasthirestaurant.adapter.MoreProductsAdapter;
import com.dinilbositha.wasthirestaurant.adapter.SearchKeywordAdapter;
import com.dinilbositha.wasthirestaurant.databinding.FragmentSearchBinding;
import com.dinilbositha.wasthirestaurant.model.Product;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SearchFragment extends Fragment {

    private static final String ARG_SHOW_NAV = "show_nav";
    private static final String ARG_SELECTED_CATEGORY = "selected_category";

    private FragmentSearchBinding binding;

    private SearchKeywordAdapter recentAdapter;
    private MoreProductsAdapter resultsAdapter;

    private final List<String> recentSearches = new ArrayList<>();
    private final List<Product> allProducts = new ArrayList<>();
    private final List<Product> filteredProducts = new ArrayList<>();

    private boolean showNavigation = true;
    private String selectedCategory = "";

    public SearchFragment() {
    }

    public static SearchFragment newInstance(boolean showNav) {
        SearchFragment fragment = new SearchFragment();
        Bundle args = new Bundle();
        args.putBoolean(ARG_SHOW_NAV, showNav);
        fragment.setArguments(args);
        return fragment;
    }

    public static SearchFragment newInstance(boolean showNav, String category) {
        SearchFragment fragment = new SearchFragment();
        Bundle args = new Bundle();
        args.putBoolean(ARG_SHOW_NAV, showNav);
        args.putString(ARG_SELECTED_CATEGORY, category);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            showNavigation = getArguments().getBoolean(ARG_SHOW_NAV, true);
            selectedCategory = getArguments().getString(ARG_SELECTED_CATEGORY, "");
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentSearchBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        updateNavigationVisibility();

        setupRecyclerViews();
        setupClicks();
        setupSearchInput();
        showInitialState();
        loadProducts();
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

    private void setupRecyclerViews() {
        recentAdapter = new SearchKeywordAdapter(recentSearches, keyword -> {
            selectedCategory = "";
            binding.edtSearch.setText(keyword);
            binding.edtSearch.setSelection(keyword.length());
            performSearch(keyword);
        });

        resultsAdapter = new MoreProductsAdapter(filteredProducts, product -> {
            if (product == null || product.getProductId() == null) return;

            saveRecentSearch(product.getProductTitle());

            ProductDetailsFragment fragment = ProductDetailsFragment.newInstance(product.getProductId());
            getParentFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragemenet_container, fragment)
                    .addToBackStack(null)
                    .commit();
        });

        binding.recyclerRecentSearches.setLayoutManager(
                new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        );
        binding.recyclerRecentSearches.setAdapter(recentAdapter);

        binding.recyclerSearchResults.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        binding.recyclerSearchResults.setAdapter(resultsAdapter);
    }

    private void setupClicks() {
        binding.btnBack.setOnClickListener(v -> {
            if (getParentFragmentManager().getBackStackEntryCount() > 0) {
                getParentFragmentManager().popBackStack();
            } else {
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).selectBottomNavItem(R.id.bottom_nav_home);
                }
            }
        });

        binding.btnClearSearch.setOnClickListener(v -> {
            selectedCategory = "";
            binding.edtSearch.setText("");
            showInitialState();
        });

        binding.btnFilter.setOnClickListener(v -> {
            // TODO: Add filter bottom sheet later
        });
    }

    private void setupSearchInput() {
        binding.edtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().trim();

                binding.btnClearSearch.setVisibility(
                        TextUtils.isEmpty(query) ? View.GONE : View.VISIBLE
                );

                if (TextUtils.isEmpty(query)) {
                    showInitialState();
                } else {
                    performSearch(query);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        binding.edtSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                String query = binding.edtSearch.getText().toString().trim();
                if (!TextUtils.isEmpty(query)) {
                    saveRecentSearch(query);
                    performSearch(query);
                }
                return true;
            }
            return false;
        });
    }

    private void loadProducts() {
        FirebaseFirestore.getInstance()
                .collection("products")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    allProducts.clear();

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Product product = doc.toObject(Product.class);
                        if (product != null) {
                            allProducts.add(product);
                        }
                    }

                    if (!TextUtils.isEmpty(selectedCategory)) {
                        binding.edtSearch.setText(selectedCategory);
                        binding.edtSearch.setSelection(selectedCategory.length());
                        performSearch(selectedCategory);
                    }
                });
    }

    private void performSearch(String query) {
        String lowerQuery = query.toLowerCase(Locale.getDefault());

        filteredProducts.clear();

        for (Product product : allProducts) {
            String title = product.getProductTitle() != null
                    ? product.getProductTitle().toLowerCase(Locale.getDefault()) : "";

            String category = product.getCategoryName() != null
                    ? product.getCategoryName().toLowerCase(Locale.getDefault()) : "";

            String description = product.getDescription() != null
                    ? product.getDescription().toLowerCase(Locale.getDefault()) : "";

            if (title.contains(lowerQuery) ||
                    category.contains(lowerQuery) ||
                    description.contains(lowerQuery)) {
                filteredProducts.add(product);
            }
        }

        updateSearchResultUi(query);
        resultsAdapter.notifyDataSetChanged();
    }

    private void updateSearchResultUi(String query) {
        boolean hasResults = !filteredProducts.isEmpty();
        boolean hasQuery = !TextUtils.isEmpty(query);

        binding.txtRecentTitle.setVisibility(hasQuery ? View.GONE : View.VISIBLE);
        binding.recyclerRecentSearches.setVisibility(hasQuery ? View.GONE : View.VISIBLE);

        binding.txtResultsTitle.setVisibility(hasQuery ? View.VISIBLE : View.GONE);
        binding.recyclerSearchResults.setVisibility(hasQuery && hasResults ? View.VISIBLE : View.GONE);
        binding.layoutEmptySearch.setVisibility(hasQuery && !hasResults ? View.VISIBLE : View.GONE);
    }

    private void showInitialState() {
        binding.txtRecentTitle.setVisibility(View.VISIBLE);
        binding.recyclerRecentSearches.setVisibility(View.VISIBLE);

        binding.txtResultsTitle.setVisibility(View.GONE);
        binding.recyclerSearchResults.setVisibility(View.GONE);
        binding.layoutEmptySearch.setVisibility(View.GONE);
    }

    private void saveRecentSearch(String keyword) {
        if (TextUtils.isEmpty(keyword)) return;

        recentSearches.remove(keyword);
        recentSearches.add(0, keyword);

        if (recentSearches.size() > 10) {
            recentSearches.remove(recentSearches.size() - 1);
        }

        recentAdapter.notifyDataSetChanged();
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