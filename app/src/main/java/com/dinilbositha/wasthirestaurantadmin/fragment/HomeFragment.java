package com.dinilbositha.wasthirestaurantadmin.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.dinilbositha.wasthirestaurantadmin.R;
import com.dinilbositha.wasthirestaurantadmin.databinding.FragmentHomeBinding;
import com.dinilbositha.wasthirestaurantadmin.model.ChartDataPoint;
import com.dinilbositha.wasthirestaurantadmin.model.DashboardStats;
import com.dinilbositha.wasthirestaurantadmin.utils.CustomChartMarkerView;
import com.dinilbositha.wasthirestaurantadmin.viewmodel.HomeViewModel;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class HomeFragment extends Fragment {

    private HomeViewModel homeViewModel;
    private FragmentHomeBinding binding;

    private CustomChartMarkerView chartMarkerView;
    private DashboardStats currentStats;

    private enum ChartFilter {
        LAST_7_DAYS,
        LAST_30_DAYS,
        STATUS
    }

    private ChartFilter activeFilter = ChartFilter.LAST_7_DAYS;

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

        setupCharts();
        setupFilterChips();
        setupNavigationClicks();
        observeDashboardStats();
    }

    private void setupCharts() {
        setupBarChart(binding.barChartSales);
        setupPieChart(binding.pieChartStatus);

        chartMarkerView = new CustomChartMarkerView(requireContext(), R.layout.custom_marker_view);
        chartMarkerView.setChartView(binding.barChartSales);
        binding.barChartSales.setMarker(chartMarkerView);
    }

    private void setupBarChart(BarChart barChart) {
        barChart.getDescription().setEnabled(false);
        barChart.setDrawGridBackground(false);
        barChart.setDrawBarShadow(false);
        barChart.setHighlightFullBarEnabled(false);
        barChart.setDrawValueAboveBar(true);
        barChart.setPinchZoom(false);
        barChart.setScaleEnabled(false);
        barChart.setDoubleTapToZoomEnabled(false);
        barChart.setExtraOffsets(6, 10, 6, 8);

        XAxis xAxis = barChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setDrawAxisLine(true);
        xAxis.setAxisLineColor(ContextCompat.getColor(requireContext(), R.color.md_theme_outlineVariant));
        xAxis.setTextColor(ContextCompat.getColor(requireContext(), R.color.md_theme_outline));
        xAxis.setTextSize(10.5f);
        xAxis.setGranularity(1f);

        YAxis leftAxis = barChart.getAxisLeft();
        leftAxis.setDrawGridLines(true);
        leftAxis.setGridColor(Color.parseColor("#15000000"));
        leftAxis.setDrawAxisLine(false);
        leftAxis.setTextColor(ContextCompat.getColor(requireContext(), R.color.md_theme_outline));
        leftAxis.setTextSize(10f);
        leftAxis.setAxisMinimum(0f);
        leftAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                if (value >= 1000000) {
                    return String.format(Locale.getDefault(), "%.1fM", value / 1000000);
                } else if (value >= 1000) {
                    return String.format(Locale.getDefault(), "%.0fk", value / 1000);
                }
                return String.valueOf((int) value);
            }
        });

        barChart.getAxisRight().setEnabled(false);

        Legend legend = barChart.getLegend();
        legend.setEnabled(false);
    }

    private void setupPieChart(PieChart pieChart) {
        pieChart.getDescription().setEnabled(false);
        pieChart.setUsePercentValues(true);
        pieChart.setExtraOffsets(5, 5, 5, 5);
        pieChart.setDragDecelerationFrictionCoef(0.95f);
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleColor(ContextCompat.getColor(requireContext(), R.color.md_theme_background));
        pieChart.setTransparentCircleColor(Color.WHITE);
        pieChart.setTransparentCircleAlpha(110);
        pieChart.setHoleRadius(54f);
        pieChart.setTransparentCircleRadius(58f);
        pieChart.setDrawCenterText(true);
        pieChart.setCenterText("Orders");
        pieChart.setCenterTextSize(14f);
        pieChart.setCenterTextColor(ContextCompat.getColor(requireContext(), R.color.md_theme_onBackground));
        pieChart.setRotationAngle(0);
        pieChart.setRotationEnabled(true);
        pieChart.setHighlightPerTapEnabled(true);

        Legend legend = pieChart.getLegend();
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        legend.setDrawInside(false);
        legend.setTextSize(11f);
        legend.setTextColor(ContextCompat.getColor(requireContext(), R.color.md_theme_onBackground));
        legend.setWordWrapEnabled(true);
    }

    private void setupFilterChips() {
        binding.chipGroupTimeFilter.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return;

            int checkedId = checkedIds.get(0);
            if (checkedId == R.id.chipLast7Days) {
                activeFilter = ChartFilter.LAST_7_DAYS;
                renderBarChartData(currentStats != null ? currentStats.getLast7DaysSales() : null);
            } else if (checkedId == R.id.chipLast30Days) {
                activeFilter = ChartFilter.LAST_30_DAYS;
                renderBarChartData(currentStats != null ? currentStats.getLast30DaysSales() : null);
            } else if (checkedId == R.id.chipStatus) {
                activeFilter = ChartFilter.STATUS;
                renderPieChartData(currentStats != null ? currentStats.getStatusDistribution() : null);
            }
        });
    }

    private void setupNavigationClicks() {
        binding.cardManage.setOnClickListener(v -> loadFragment(new CategoryFragment()));
        binding.cardManageProducts.setOnClickListener(v -> loadFragment(new ProductsFragment()));
        binding.cardManageUsers.setOnClickListener(v -> loadFragment(new UsersFragment()));
        binding.btnExportPdf.setOnClickListener(v -> {
            com.dinilbositha.wasthirestaurantadmin.dialog.ExportReportBottomSheetDialog
                    .newInstance()
                    .show(getParentFragmentManager(), "ExportReportDialog");
        });
    }

    private void observeDashboardStats() {
        homeViewModel.getDashboardStats().observe(getViewLifecycleOwner(), stats -> {
            if (stats != null) {
                currentStats = stats;

                // Summary Counters
                binding.totalOrdersCount.setText(String.valueOf(stats.getTotalOrders()));
                binding.pendingOrdersCount.setText(String.valueOf(stats.getPendingOrders()));
                binding.productsCount.setText(String.valueOf(stats.getProductsCount()));
                binding.usersCount.setText(String.valueOf(stats.getUsersCount()));

                // Total Revenue Header Badge
                binding.txtTotalRevenueBadge.setText(String.format(Locale.getDefault(), "LKR %,.2f", stats.getTotalRevenue()));

                // Mini Key Metrics
                binding.txtTodayRevenue.setText(String.format(Locale.getDefault(), "LKR %,.0f", stats.getTodayRevenue()));
                binding.txtTodayOrders.setText(String.valueOf(stats.getTodayOrders()));

                // Calculate daily average over last 7 days
                double avgDaily = 0.0;
                if (stats.getLast7DaysSales() != null && !stats.getLast7DaysSales().isEmpty()) {
                    double sum = 0.0;
                    for (ChartDataPoint p : stats.getLast7DaysSales()) {
                        sum += p.getRevenue();
                    }
                    avgDaily = sum / stats.getLast7DaysSales().size();
                }
                binding.txtAvgDailyRevenue.setText(String.format(Locale.getDefault(), "LKR %,.0f", avgDaily));

                // Render active chart
                if (activeFilter == ChartFilter.LAST_7_DAYS) {
                    renderBarChartData(stats.getLast7DaysSales());
                } else if (activeFilter == ChartFilter.LAST_30_DAYS) {
                    renderBarChartData(stats.getLast30DaysSales());
                } else if (activeFilter == ChartFilter.STATUS) {
                    renderPieChartData(stats.getStatusDistribution());
                }
            }
        });
    }

    private void renderBarChartData(List<ChartDataPoint> dataPoints) {
        binding.pieChartStatus.setVisibility(View.GONE);

        if (dataPoints == null || dataPoints.isEmpty()) {
            binding.barChartSales.setVisibility(View.GONE);
            binding.layoutChartEmpty.setVisibility(View.VISIBLE);
            return;
        }

        binding.barChartSales.setVisibility(View.VISIBLE);
        binding.layoutChartEmpty.setVisibility(View.GONE);

        chartMarkerView.setDataPoints(dataPoints);

        List<BarEntry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();

        for (int i = 0; i < dataPoints.size(); i++) {
            ChartDataPoint point = dataPoints.get(i);
            entries.add(new BarEntry(i, (float) point.getRevenue()));
            labels.add(point.getLabel());
        }

        BarDataSet dataSet = new BarDataSet(entries, "Revenue");
        int primaryColor = ContextCompat.getColor(requireContext(), R.color.md_theme_primary);
        dataSet.setColor(primaryColor);
        dataSet.setDrawValues(false);
        dataSet.setHighLightColor(Color.parseColor("#FF9800"));
        dataSet.setHighLightAlpha(180);

        BarData barData = new BarData(dataSet);
        barData.setBarWidth(dataPoints.size() > 15 ? 0.65f : 0.48f);

        binding.barChartSales.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
        binding.barChartSales.getXAxis().setLabelCount(Math.min(labels.size(), 7), false);
        binding.barChartSales.setData(barData);
        binding.barChartSales.animateY(800);
        binding.barChartSales.invalidate();
    }

    private void renderPieChartData(Map<String, Integer> statusMap) {
        binding.barChartSales.setVisibility(View.GONE);

        if (statusMap == null || statusMap.isEmpty()) {
            binding.pieChartStatus.setVisibility(View.GONE);
            binding.layoutChartEmpty.setVisibility(View.VISIBLE);
            return;
        }

        binding.pieChartStatus.setVisibility(View.VISIBLE);
        binding.layoutChartEmpty.setVisibility(View.GONE);

        List<PieEntry> entries = new ArrayList<>();
        List<Integer> colors = new ArrayList<>();

        for (Map.Entry<String, Integer> entry : statusMap.entrySet()) {
            if (entry.getValue() > 0) {
                entries.add(new PieEntry(entry.getValue(), entry.getKey()));

                String key = entry.getKey().toLowerCase();
                if (key.contains("completed") || key.contains("delivered")) {
                    colors.add(Color.parseColor("#4CAF50")); // Green
                } else if (key.contains("pending")) {
                    colors.add(Color.parseColor("#FF9800")); // Orange
                } else if (key.contains("preparing")) {
                    colors.add(Color.parseColor("#2196F3")); // Blue
                } else if (key.contains("cancelled")) {
                    colors.add(Color.parseColor("#F44336")); // Red
                } else {
                    colors.add(Color.parseColor("#9C27B0")); // Purple
                }
            }
        }

        if (entries.isEmpty()) {
            binding.pieChartStatus.setVisibility(View.GONE);
            binding.layoutChartEmpty.setVisibility(View.VISIBLE);
            return;
        }

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(colors);
        dataSet.setSliceSpace(3f);
        dataSet.setSelectionShift(6f);

        PieData pieData = new PieData(dataSet);
        pieData.setValueFormatter(new PercentFormatter(binding.pieChartStatus));
        pieData.setValueTextSize(11f);
        pieData.setValueTextColor(Color.WHITE);

        binding.pieChartStatus.setData(pieData);
        binding.pieChartStatus.animateY(900);
        binding.pieChartStatus.invalidate();
    }

    private void loadFragment(Fragment fragment) {
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}