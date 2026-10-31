package com.dinilbositha.wasthirestaurantadmin.utils;

import android.content.Context;
import android.widget.TextView;

import com.dinilbositha.wasthirestaurantadmin.R;
import com.dinilbositha.wasthirestaurantadmin.model.ChartDataPoint;
import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.utils.MPPointF;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class CustomChartMarkerView extends MarkerView {

    private final TextView txtMarkerDate;
    private final TextView txtMarkerRevenue;
    private final TextView txtMarkerOrders;
    private List<ChartDataPoint> dataPoints;
    private final NumberFormat currencyFormat;

    public CustomChartMarkerView(Context context, int layoutResource) {
        super(context, layoutResource);
        txtMarkerDate = findViewById(R.id.txtMarkerDate);
        txtMarkerRevenue = findViewById(R.id.txtMarkerRevenue);
        txtMarkerOrders = findViewById(R.id.txtMarkerOrders);

        currencyFormat = NumberFormat.getCurrencyInstance(new Locale("en", "LK"));
        currencyFormat.setMaximumFractionDigits(2);
    }

    public void setDataPoints(List<ChartDataPoint> dataPoints) {
        this.dataPoints = dataPoints;
    }

    @Override
    public void refreshContent(Entry e, Highlight highlight) {
        int index = (int) e.getX();

        if (dataPoints != null && index >= 0 && index < dataPoints.size()) {
            ChartDataPoint point = dataPoints.get(index);
            txtMarkerDate.setText(point.getFullDate() != null ? point.getFullDate() : point.getLabel());
            txtMarkerRevenue.setText(String.format(Locale.getDefault(), "LKR %,.2f", point.getRevenue()));
            txtMarkerOrders.setText(point.getOrderCount() + (point.getOrderCount() == 1 ? " order" : " orders"));
        } else {
            txtMarkerDate.setText("Value");
            txtMarkerRevenue.setText(String.format(Locale.getDefault(), "LKR %,.2f", e.getY()));
            txtMarkerOrders.setText("");
        }

        super.refreshContent(e, highlight);
    }

    @Override
    public MPPointF getOffset() {
        return new MPPointF(-(getWidth() / 2f), -getHeight() - 15);
    }
}
