package com.dinilbositha.wasthirestaurantadmin.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.dinilbositha.wasthirestaurantadmin.model.DashboardStats;
import com.dinilbositha.wasthirestaurantadmin.repository.HomeRepository;

public class HomeViewModel extends ViewModel {

    private final HomeRepository repository;
    private final LiveData<DashboardStats> dashboardStatsLiveData;

    public HomeViewModel() {
        repository = new HomeRepository();
        dashboardStatsLiveData = repository.getDashboardStats();
    }

    public LiveData<DashboardStats> getDashboardStats() {
        return dashboardStatsLiveData;
    }
}