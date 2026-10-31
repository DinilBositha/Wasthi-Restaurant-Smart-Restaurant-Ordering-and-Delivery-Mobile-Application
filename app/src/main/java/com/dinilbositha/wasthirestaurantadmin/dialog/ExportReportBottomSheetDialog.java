package com.dinilbositha.wasthirestaurantadmin.dialog;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.dinilbositha.wasthirestaurantadmin.R;
import com.dinilbositha.wasthirestaurantadmin.databinding.BottomSheetExportReportBinding;
import com.dinilbositha.wasthirestaurantadmin.model.ChartDataPoint;
import com.dinilbositha.wasthirestaurantadmin.model.DashboardStats;
import com.dinilbositha.wasthirestaurantadmin.utils.PdfReportGenerator;
import com.dinilbositha.wasthirestaurantadmin.viewmodel.HomeViewModel;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ExportReportBottomSheetDialog extends BottomSheetDialogFragment {

    private BottomSheetExportReportBinding binding;
    private HomeViewModel homeViewModel;
    private DashboardStats currentStats;

    public static ExportReportBottomSheetDialog newInstance() {
        return new ExportReportBottomSheetDialog();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = BottomSheetExportReportBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        try {
            homeViewModel = new ViewModelProvider(requireActivity()).get(HomeViewModel.class);
            homeViewModel.getDashboardStats().observe(getViewLifecycleOwner(), stats -> {
                currentStats = stats;
            });
        } catch (Exception e) {
            // In case launched from non-Home activity
        }

        setupClickListeners();
    }

    private void setupClickListeners() {
        if (binding == null) return;
        binding.btnGenerateAndOpen.setOnClickListener(v -> generateReport(false));
        binding.btnSharePdf.setOnClickListener(v -> generateReport(true));
    }

    private void generateReport(boolean isShare) {
        if (getContext() == null) return;
        final Context context = requireContext().getApplicationContext();

        if (currentStats == null) {
            Toast.makeText(context, "Loading latest sales data. Please try in a moment.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (binding != null) {
            binding.progressReportLoading.setVisibility(View.VISIBLE);
            binding.btnGenerateAndOpen.setEnabled(false);
            binding.btnSharePdf.setEnabled(false);
        }

        String periodName = "Last 7 Days";
        List<ChartDataPoint> dataPoints = currentStats.getLast7DaysSales();

        if (binding != null) {
            int checkedId = binding.chipGroupReportPeriod.getCheckedChipId();
            if (checkedId == R.id.chipExport30Days) {
                periodName = "Last 30 Days";
                dataPoints = currentStats.getLast30DaysSales();
            } else if (checkedId == R.id.chipExportToday) {
                periodName = "Today";
                List<ChartDataPoint> todayList = new ArrayList<>();
                if (currentStats.getLast7DaysSales() != null && !currentStats.getLast7DaysSales().isEmpty()) {
                    todayList.add(currentStats.getLast7DaysSales().get(currentStats.getLast7DaysSales().size() - 1));
                }
                dataPoints = todayList;
            }
        }

        final String finalPeriodName = periodName;
        final List<ChartDataPoint> finalDataPoints = dataPoints;

        new Thread(() -> {
            File pdfFile = PdfReportGenerator.generateSalesReport(
                    context,
                    currentStats,
                    finalPeriodName,
                    finalDataPoints
            );

            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    if (binding != null) {
                        binding.progressReportLoading.setVisibility(View.GONE);
                        binding.btnGenerateAndOpen.setEnabled(true);
                        binding.btnSharePdf.setEnabled(true);
                    }

                    if (pdfFile != null && pdfFile.exists()) {
                        if (isShare) {
                            PdfReportGenerator.sharePdf(context, pdfFile);
                        } else {
                            PdfReportGenerator.openPdf(context, pdfFile);
                        }
                        try {
                            dismissAllowingStateLoss();
                        } catch (Exception ignored) {
                        }
                    } else {
                        Toast.makeText(context, "Failed to generate PDF report", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }).start();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
