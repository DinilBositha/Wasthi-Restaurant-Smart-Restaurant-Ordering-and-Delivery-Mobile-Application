package com.dinilbositha.wasthirestaurantadmin.utils;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import com.dinilbositha.wasthirestaurantadmin.R;
import com.dinilbositha.wasthirestaurantadmin.model.ChartDataPoint;
import com.dinilbositha.wasthirestaurantadmin.model.DashboardStats;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PdfReportGenerator {

    private static final String TAG = "PdfReportGenerator";
    private static final int PAGE_WIDTH = 595;   // A4 width in points
    private static final int PAGE_HEIGHT = 842;  // A4 height in points

    public static File generateSalesReport(Context context, DashboardStats stats, String periodName, List<ChartDataPoint> dataPoints) {
        if (context == null) {
            Log.e(TAG, "Context is null when generating report");
            return null;
        }

        PdfDocument document = new PdfDocument();
        try {
            PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create();
            PdfDocument.Page page = document.startPage(pageInfo);
            Canvas canvas = page.getCanvas();

            Paint paint = new Paint();
            paint.setAntiAlias(true);

            int margin = 36;
            int usableWidth = PAGE_WIDTH - (margin * 2);
            int y = margin;

            // Top Brand Color Stripe
            paint.setColor(Color.parseColor("#8F4C38"));
            canvas.drawRect(margin, y, PAGE_WIDTH - margin, y + 6, paint);
            y += 24;

            // Draw Restaurant Logo
            try {
                Bitmap logo = BitmapFactory.decodeResource(context.getResources(), R.drawable.app_logo1);
                if (logo != null) {
                    Bitmap scaledLogo = Bitmap.createScaledBitmap(logo, 54, 54, true);
                    canvas.drawBitmap(scaledLogo, margin, y, paint);
                }
            } catch (Throwable t) {
                Log.e(TAG, "Failed to load logo for PDF", t);
            }

            // Title and Subtitle
            paint.setColor(Color.parseColor("#8F4C38"));
            paint.setTextSize(18f);
            paint.setFakeBoldText(true);
            canvas.drawText("WASTHI RESTAURANT", margin + 64, y + 18, paint);

            paint.setColor(Color.parseColor("#231917"));
            paint.setTextSize(13f);
            paint.setFakeBoldText(true);
            canvas.drawText("SALES & REVENUE PERFORMANCE STATEMENT", margin + 64, y + 34, paint);

            paint.setColor(Color.parseColor("#777777"));
            paint.setTextSize(9.5f);
            paint.setFakeBoldText(false);
            String generatedDate = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(new Date());
            canvas.drawText("Generated on: " + generatedDate + "  |  Period: " + (periodName != null ? periodName : "N/A"), margin + 64, y + 48, paint);

            y += 72;

            // Divider
            paint.setColor(Color.parseColor("#E0E0E0"));
            paint.setStrokeWidth(1f);
            canvas.drawLine(margin, y, PAGE_WIDTH - margin, y, paint);
            y += 18;

            // Section Title: Overview Metrics
            paint.setColor(Color.parseColor("#231917"));
            paint.setTextSize(12f);
            paint.setFakeBoldText(true);
            canvas.drawText("EXECUTIVE PERFORMANCE SUMMARY", margin, y, paint);
            y += 12;

            // Draw 3 Summary Cards
            int cardWidth = (usableWidth - 20) / 3;
            int cardHeight = 60;

            double totalRevenue = stats != null ? stats.getTotalRevenue() : 0.0;
            int totalOrders = stats != null ? stats.getTotalOrders() : 0;
            int pendingOrders = stats != null ? stats.getPendingOrders() : 0;

            drawMetricCard(canvas, margin, y, cardWidth, cardHeight, "Total Revenue", String.format(Locale.getDefault(), "LKR %,.2f", totalRevenue), "#FFF1ED", "#8F4C38");
            drawMetricCard(canvas, margin + cardWidth + 10, y, cardWidth, cardHeight, "Total Orders", String.valueOf(totalOrders), "#F0F8FF", "#1976D2");
            drawMetricCard(canvas, margin + (cardWidth * 2) + 20, y, cardWidth, cardHeight, "Pending Orders", String.valueOf(pendingOrders), "#FFF8E1", "#FF9800");

            y += cardHeight + 24;

            // Section Title: Breakdown Table
            paint.setColor(Color.parseColor("#231917"));
            paint.setTextSize(12f);
            paint.setFakeBoldText(true);
            canvas.drawText("PERIOD BREAKDOWN (" + (periodName != null ? periodName.toUpperCase() : "CUSTOM") + ")", margin, y, paint);
            y += 14;

            // Table Header
            int col1 = margin + 10;
            int col2 = margin + 50;
            int col3 = margin + 220;
            int col4 = margin + 350;
            int col5 = margin + 440;

            paint.setColor(Color.parseColor("#8F4C38"));
            RectF headerRect = new RectF(margin, y, PAGE_WIDTH - margin, y + 26);
            canvas.drawRoundRect(headerRect, 4, 4, paint);

            paint.setColor(Color.WHITE);
            paint.setTextSize(10f);
            paint.setFakeBoldText(true);
            canvas.drawText("#", col1, y + 17, paint);
            canvas.drawText("Date / Day", col2, y + 17, paint);
            canvas.drawText("Orders Count", col3, y + 17, paint);
            canvas.drawText("Daily Revenue", col4, y + 17, paint);
            canvas.drawText("Avg/Order", col5, y + 17, paint);

            y += 26;

            // Table Rows
            paint.setFakeBoldText(false);
            paint.setTextSize(9.5f);

            double periodTotalRevenue = 0.0;
            int periodTotalOrders = 0;

            if (dataPoints != null && !dataPoints.isEmpty()) {
                int rowHeight = 22;
                int rowIndex = 1;

                for (ChartDataPoint point : dataPoints) {
                    if (point == null) continue;

                    if (rowIndex % 2 == 0) {
                        paint.setColor(Color.parseColor("#F9F9F9"));
                        canvas.drawRect(margin, y, PAGE_WIDTH - margin, y + rowHeight, paint);
                    }

                    paint.setColor(Color.parseColor("#333333"));
                    canvas.drawText(String.valueOf(rowIndex), col1, y + 15, paint);

                    String dateLabel = point.getFullDate() != null ? point.getFullDate() : (point.getLabel() != null ? point.getLabel() : "-");
                    canvas.drawText(dateLabel, col2, y + 15, paint);
                    canvas.drawText(String.valueOf(point.getOrderCount()), col3, y + 15, paint);
                    canvas.drawText(String.format(Locale.getDefault(), "LKR %,.2f", point.getRevenue()), col4, y + 15, paint);

                    double avg = point.getOrderCount() > 0 ? point.getRevenue() / point.getOrderCount() : 0.0;
                    canvas.drawText(String.format(Locale.getDefault(), "LKR %,.0f", avg), col5, y + 15, paint);

                    periodTotalRevenue += point.getRevenue();
                    periodTotalOrders += point.getOrderCount();

                    y += rowHeight;
                    rowIndex++;
                }

                // Total Summary Row
                paint.setColor(Color.parseColor("#F5DED8"));
                canvas.drawRect(margin, y, PAGE_WIDTH - margin, y + 26, paint);

                paint.setColor(Color.parseColor("#8F4C38"));
                paint.setFakeBoldText(true);
                paint.setTextSize(10f);
                canvas.drawText("PERIOD TOTAL", col2, y + 17, paint);
                canvas.drawText(String.valueOf(periodTotalOrders), col3, y + 17, paint);
                canvas.drawText(String.format(Locale.getDefault(), "LKR %,.2f", periodTotalRevenue), col4, y + 17, paint);
                y += 36;

            } else {
                paint.setColor(Color.parseColor("#777777"));
                canvas.drawText("No transaction data available for the selected timeframe.", margin + 10, y + 20, paint);
                y += 40;
            }

            // Footer Section
            int footerY = PAGE_HEIGHT - 45;
            paint.setColor(Color.parseColor("#CCCCCC"));
            paint.setStrokeWidth(1f);
            canvas.drawLine(margin, footerY, PAGE_WIDTH - margin, footerY, paint);

            paint.setColor(Color.parseColor("#888888"));
            paint.setTextSize(8.5f);
            paint.setFakeBoldText(false);
            canvas.drawText("Wasthi Restaurant Management System | Confidential Official Report", margin, footerY + 18, paint);
            canvas.drawText("Page 1 of 1", PAGE_WIDTH - margin - 50, footerY + 18, paint);

            document.finishPage(page);

            // Determine safe directory for storing report
            File reportsDir = null;
            try {
                File externalDir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
                if (externalDir != null) {
                    reportsDir = new File(externalDir, "reports");
                }
            } catch (Exception e) {
                Log.w(TAG, "External files dir not accessible, falling back to cache dir", e);
            }

            if (reportsDir == null) {
                reportsDir = new File(context.getCacheDir(), "reports");
            }

            if (!reportsDir.exists()) {
                reportsDir.mkdirs();
            }

            String fileName = "Wasthi_Sales_Report_" + new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date()) + ".pdf";
            File pdfFile = new File(reportsDir, fileName);

            FileOutputStream fos = new FileOutputStream(pdfFile);
            document.writeTo(fos);
            document.close();
            fos.flush();
            fos.close();
            Log.d(TAG, "PDF generated successfully: " + pdfFile.getAbsolutePath());
            return pdfFile;

        } catch (Throwable t) {
            Log.e(TAG, "Error generating or writing PDF file", t);
            try {
                document.close();
            } catch (Exception ignored) {
            }
            return null;
        }
    }

    private static void drawMetricCard(Canvas canvas, int x, int y, int width, int height, String title, String value, String bgColor, String accentColor) {
        Paint paint = new Paint();
        paint.setAntiAlias(true);

        // Background
        paint.setColor(Color.parseColor(bgColor));
        RectF rect = new RectF(x, y, x + width, y + height);
        canvas.drawRoundRect(rect, 8, 8, paint);

        // Border
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(1f);
        paint.setColor(Color.parseColor(accentColor));
        canvas.drawRoundRect(rect, 8, 8, paint);

        paint.setStyle(Paint.Style.FILL);

        // Title
        paint.setColor(Color.parseColor("#666666"));
        paint.setTextSize(9f);
        paint.setFakeBoldText(false);
        canvas.drawText(title, x + 12, y + 20, paint);

        // Value
        paint.setColor(Color.parseColor(accentColor));
        paint.setTextSize(12.5f);
        paint.setFakeBoldText(true);
        canvas.drawText(value, x + 12, y + 42, paint);
    }

    public static void openPdf(Context context, File pdfFile) {
        if (context == null) return;
        if (pdfFile == null || !pdfFile.exists()) {
            Toast.makeText(context, "PDF file not found", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            Uri uri = FileProvider.getUriForFile(context, context.getPackageName() + ".fileprovider", pdfFile);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(uri, "application/pdf");
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_ACTIVITY_NEW_TASK);

            context.startActivity(intent);
        } catch (Exception e) {
            Log.e(TAG, "Failed to open PDF Viewer", e);
            Toast.makeText(context, "No PDF viewer app found on device.", Toast.LENGTH_LONG).show();
        }
    }

    public static void sharePdf(Context context, File pdfFile) {
        if (context == null) return;
        if (pdfFile == null || !pdfFile.exists()) {
            Toast.makeText(context, "PDF file not found", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            Uri uri = FileProvider.getUriForFile(context, context.getPackageName() + ".fileprovider", pdfFile);
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("application/pdf");
            intent.putExtra(Intent.EXTRA_STREAM, uri);
            intent.putExtra(Intent.EXTRA_SUBJECT, "Wasthi Restaurant Sales Report");
            intent.putExtra(Intent.EXTRA_TEXT, "Here is the sales and revenue report from Wasthi Restaurant Admin.");
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            Intent chooser = Intent.createChooser(intent, "Share Wasthi PDF Report");
            chooser.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(chooser);
        } catch (Exception e) {
            Log.e(TAG, "Failed to share PDF", e);
            Toast.makeText(context, "Failed to share report: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
