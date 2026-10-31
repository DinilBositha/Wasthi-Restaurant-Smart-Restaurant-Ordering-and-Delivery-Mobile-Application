package com.dinilbositha.wasthirestaurant.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.viewpager2.widget.ViewPager2;

import com.dinilbositha.wasthirestaurant.R;
import com.dinilbositha.wasthirestaurant.activity.MainActivity;
import com.dinilbositha.wasthirestaurant.adapter.CategoryHomeAdapter;
import com.dinilbositha.wasthirestaurant.adapter.HomeAdsAdapter;
import com.dinilbositha.wasthirestaurant.adapter.ProductHomeAdapter;
import com.dinilbositha.wasthirestaurant.databinding.FragmentHomeBinding;
import com.dinilbositha.wasthirestaurant.model.HomeAds;
import com.dinilbositha.wasthirestaurant.viewmodel.HomeViewModel;
import com.tbuonomo.viewpagerdotsindicator.DotsIndicator;

import java.util.List;

public class HomeFragment extends Fragment {

    private static final String TAG = "HomeFragment";
    private FragmentHomeBinding binding;
    private HomeViewModel homeViewModel;
    DotsIndicator dotsIndicator;
    ViewPager2 viewPager;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        if (homeViewModel.hasLoadedOnce()) {
            showContent();
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).showMainNavigationUi();
            }
        } else {
            showShimmer();
        }

        homeViewModel.loadHomeDataIfNeeded();

        observePromotions();
        observeCategories();
        observeProducts();

        setupSwipeRefresh();

        homeViewModel.getAllDataLoaded().observe(getViewLifecycleOwner(), allLoaded -> {
            if (Boolean.TRUE.equals(allLoaded)) {
                showContent();
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).hideGlobalLoading();
                    ((MainActivity) getActivity()).showMainNavigationUi();
                }
            }
        });

        homeViewModel.getIsRefreshing().observe(getViewLifecycleOwner(), isRefreshing -> {
            if (binding != null) {
                binding.swipeRefreshLayout.setRefreshing(Boolean.TRUE.equals(isRefreshing));
            }
        });
    }

    private void setupSwipeRefresh() {
        binding.swipeRefreshLayout.setColorSchemeResources(
                R.color.md_theme_primary,
                android.R.color.holo_orange_dark,
                android.R.color.holo_green_dark
        );

        binding.swipeRefreshLayout.setOnRefreshListener(() ->
                homeViewModel.refreshHomeData()
        );
    }

    private void showShimmer() {
        if (binding == null) return;
        binding.shimmerHome.getRoot().setVisibility(View.VISIBLE);
        binding.shimmerHome.shimmerViewContainer.startShimmer();
        binding.swipeRefreshLayout.setVisibility(View.GONE);
    }

    private void showContent() {
        if (binding == null) return;
        binding.shimmerHome.shimmerViewContainer.stopShimmer();
        binding.shimmerHome.getRoot().setVisibility(View.GONE);
        binding.swipeRefreshLayout.setVisibility(View.VISIBLE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (binding != null && binding.shimmerHome != null) {
            binding.shimmerHome.shimmerViewContainer.stopShimmer();
        }
        binding = null;
    }

    private void observePromotions() {
        if (homeViewModel.getPromotions() == null) return;

        homeViewModel.getPromotions().observe(getViewLifecycleOwner(), resource -> {
            if (binding == null || resource == null) return;

            if (resource.isSuccess()) {
                if (resource.data != null && !resource.data.isEmpty()) {
                    setupBannerView(resource.data);
                }
                homeViewModel.setPromotionsLoaded(true);
            } else if (resource.isError()) {
                Log.e(TAG, "Promotion error: " + resource.message);
                homeViewModel.setPromotionsLoaded(true);
            }
        });
    }

    private void observeCategories() {
        if (homeViewModel.getCategories() == null) return;

        homeViewModel.getCategories().observe(getViewLifecycleOwner(), resource -> {
            if (binding == null || resource == null) return;

            if (resource.isSuccess()) {
                if (resource.data != null && !resource.data.isEmpty()) {
                    binding.categoriesView.homeCategoryRecycleView.setLayoutManager(
                            new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false)
                    );

                    binding.categoriesView.homeCategoryRecycleView.setAdapter(
                            new CategoryHomeAdapter(resource.data, category -> {
                                String categoryName = category.getCategoryName();

                                if ("All Categories".equalsIgnoreCase(categoryName)) {
                                    categoryName = "";
                                }

                                SearchFragment fragment = SearchFragment.newInstance(false, categoryName);
                                getParentFragmentManager()
                                        .beginTransaction()
                                        .replace(R.id.fragemenet_container, fragment)
                                        .addToBackStack(null)
                                        .commit();
                            })
                    );
                }
                homeViewModel.setCategoriesLoaded(true);
            } else if (resource.isError()) {
                Log.e(TAG, "Categories error: " + resource.message);
                homeViewModel.setCategoriesLoaded(true);
            }
        });
    }

    private void observeProducts() {
        if (homeViewModel.getActiveProducts() == null) return;

        homeViewModel.getActiveProducts().observe(getViewLifecycleOwner(), resource -> {
            if (binding == null || resource == null) return;

            if (resource.isSuccess()) {
                if (resource.data != null && !resource.data.isEmpty()) {
                    binding.homeProductView.productTitle.setText(R.string.newest_dishes_title);
                    binding.homeProductView.homeProductRecycleView.setLayoutManager(
                            new GridLayoutManager(getContext(), 2)
                    );

                    ProductHomeAdapter adapter = new ProductHomeAdapter(resource.data, product -> {
                        ProductDetailsFragment fragment = ProductDetailsFragment.newInstance(product.getProductId());
                        getParentFragmentManager().beginTransaction()
                                .replace(R.id.fragemenet_container, fragment)
                                .addToBackStack(null)
                                .commit();
                    });

                    binding.homeProductView.homeProductRecycleView.setAdapter(adapter);
                } else {
                    binding.homeProductView.productTitle.setText(R.string.no_products_available);
                }

                homeViewModel.setProductsLoaded(true);
            } else if (resource.isError()) {
                Log.e(TAG, "Products error: " + resource.message);
                homeViewModel.setProductsLoaded(true);
            }
        });
    }

    private void setupBannerView(List<HomeAds> list) {
        HomeAdsAdapter homeAdsAdapter = new HomeAdsAdapter(list);
        binding.homeTopAds.viewPagerAds.setAdapter(homeAdsAdapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() instanceof MainActivity && homeViewModel != null && homeViewModel.hasLoadedOnce()) {
            ((MainActivity) getActivity()).showMainNavigationUi();
        }
    }
}