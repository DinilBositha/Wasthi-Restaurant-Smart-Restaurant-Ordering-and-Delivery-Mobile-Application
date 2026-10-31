package com.dinilbositha.wasthirestaurantadmin.repository;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.dinilbositha.wasthirestaurantadmin.model.ChartDataPoint;
import com.dinilbositha.wasthirestaurantadmin.model.DashboardStats;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class HomeRepository {

    private static final String TAG = "HomeRepository";
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public LiveData<DashboardStats> getDashboardStats() {
        MutableLiveData<DashboardStats> liveData = new MutableLiveData<>();

        db.collection("orders").get().addOnSuccessListener(orderSnapshot -> {
            DashboardStats stats = new DashboardStats();
            stats.setTotalOrders(orderSnapshot.size());

            int pendingCount = 0;
            double totalRevenue = 0.0;
            double todayRevenue = 0.0;
            int todayOrders = 0;

            Calendar todayCal = Calendar.getInstance();
            int todayYear = todayCal.get(Calendar.YEAR);
            int todayDayOfYear = todayCal.get(Calendar.DAY_OF_YEAR);

            // Prepare 7-day buckets
            Map<String, ChartDataPoint> last7DaysMap = new LinkedHashMap<>();
            SimpleDateFormat dayFormat7 = new SimpleDateFormat("EEE", Locale.getDefault());
            SimpleDateFormat fullDateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
            SimpleDateFormat keyFormat = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());

            for (int i = 6; i >= 0; i--) {
                Calendar c = Calendar.getInstance();
                c.add(Calendar.DAY_OF_YEAR, -i);
                String key = keyFormat.format(c.getTime());
                String label = dayFormat7.format(c.getTime());
                String fullDate = fullDateFormat.format(c.getTime());
                last7DaysMap.put(key, new ChartDataPoint(label, fullDate, 0.0, 0));
            }

            // Prepare 30-day buckets
            Map<String, ChartDataPoint> last30DaysMap = new LinkedHashMap<>();
            SimpleDateFormat dayFormat30 = new SimpleDateFormat("dd MMM", Locale.getDefault());

            for (int i = 29; i >= 0; i--) {
                Calendar c = Calendar.getInstance();
                c.add(Calendar.DAY_OF_YEAR, -i);
                String key = keyFormat.format(c.getTime());
                String label = dayFormat30.format(c.getTime());
                String fullDate = fullDateFormat.format(c.getTime());
                last30DaysMap.put(key, new ChartDataPoint(label, fullDate, 0.0, 0));
            }

            // Prepare status map
            Map<String, Integer> statusMap = new HashMap<>();

            for (DocumentSnapshot doc : orderSnapshot.getDocuments()) {
                String status = doc.getString("orderStatus");
                if (status == null || status.trim().isEmpty()) {
                    status = "Pending";
                }
                status = capitalize(status.trim());
                statusMap.put(status, statusMap.getOrDefault(status, 0) + 1);

                if ("Pending".equalsIgnoreCase(status)) {
                    pendingCount++;
                }

                // Extract total amount
                double orderTotal = 0.0;
                Object totalObj = doc.get("total");
                if (totalObj instanceof Number) {
                    orderTotal = ((Number) totalObj).doubleValue();
                }

                // Add to overall revenue if not cancelled
                if (!"Cancelled".equalsIgnoreCase(status)) {
                    totalRevenue += orderTotal;
                }

                // Extract creation timestamp
                long createdAt = 0;
                Object createdAtObj = doc.get("createdAt");
                if (createdAtObj instanceof Number) {
                    createdAt = ((Number) createdAtObj).longValue();
                } else if (createdAtObj instanceof Timestamp) {
                    createdAt = ((Timestamp) createdAtObj).toDate().getTime();
                }

                if (createdAt > 0) {
                    Date orderDate = new Date(createdAt);
                    Calendar orderCal = Calendar.getInstance();
                    orderCal.setTime(orderDate);

                    if (orderCal.get(Calendar.YEAR) == todayYear && orderCal.get(Calendar.DAY_OF_YEAR) == todayDayOfYear) {
                        todayOrders++;
                        if (!"Cancelled".equalsIgnoreCase(status)) {
                            todayRevenue += orderTotal;
                        }
                    }

                    String orderKey = keyFormat.format(orderDate);

                    if (last7DaysMap.containsKey(orderKey)) {
                        ChartDataPoint p = last7DaysMap.get(orderKey);
                        if (p != null) {
                            p.setOrderCount(p.getOrderCount() + 1);
                            if (!"Cancelled".equalsIgnoreCase(status)) {
                                p.setRevenue(p.getRevenue() + orderTotal);
                            }
                        }
                    }

                    if (last30DaysMap.containsKey(orderKey)) {
                        ChartDataPoint p = last30DaysMap.get(orderKey);
                        if (p != null) {
                            p.setOrderCount(p.getOrderCount() + 1);
                            if (!"Cancelled".equalsIgnoreCase(status)) {
                                p.setRevenue(p.getRevenue() + orderTotal);
                            }
                        }
                    }
                }
            }

            stats.setPendingOrders(pendingCount);
            stats.setTotalRevenue(totalRevenue);
            stats.setTodayRevenue(todayRevenue);
            stats.setTodayOrders(todayOrders);
            stats.setLast7DaysSales(new ArrayList<>(last7DaysMap.values()));
            stats.setLast30DaysSales(new ArrayList<>(last30DaysMap.values()));
            stats.setStatusDistribution(statusMap);

            db.collection("products").get().addOnSuccessListener(productSnapshot -> {
                stats.setProductsCount(productSnapshot.size());

                db.collection("users").get().addOnSuccessListener(userSnapshot -> {
                    stats.setUsersCount(userSnapshot.size());
                    liveData.setValue(stats);
                }).addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to load users", e);
                    liveData.setValue(stats);
                });
            }).addOnFailureListener(e -> {
                Log.e(TAG, "Failed to load products", e);
                liveData.setValue(stats);
            });
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Failed to load orders", e);
            liveData.setValue(new DashboardStats());
        });

        return liveData;
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) return "";
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }
}