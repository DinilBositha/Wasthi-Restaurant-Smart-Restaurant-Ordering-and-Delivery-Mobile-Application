package com.dinilbositha.wasthirestaurant.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.Nullable;

import com.dinilbositha.wasthirestaurant.model.Category;
import com.dinilbositha.wasthirestaurant.model.Product;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class AppDatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = "AppDatabaseHelper";
    private static final String DATABASE_NAME = "wasthi_restaurant.db";
    private static final int DATABASE_VERSION = 1;

    private static AppDatabaseHelper instance;
    private final Gson gson = new Gson();

    // Table: Products
    public static final String TABLE_PRODUCTS = "products";
    public static final String COL_PRODUCT_ID = "product_id";
    public static final String COL_PRODUCT_TITLE = "title";
    public static final String COL_PRODUCT_DESC = "description";
    public static final String COL_PRODUCT_CATEGORY_ID = "category_id";
    public static final String COL_PRODUCT_CATEGORY_NAME = "category_name";
    public static final String COL_PRODUCT_BASE_PRICE = "base_price";
    public static final String COL_PRODUCT_STATUS = "status";
    public static final String COL_PRODUCT_IMAGES_JSON = "images_json";
    public static final String COL_PRODUCT_VARIANTS_JSON = "variants_json";
    public static final String COL_PRODUCT_UPDATED_AT = "updated_at";

    // Table: Categories
    public static final String TABLE_CATEGORIES = "categories";
    public static final String COL_CATEGORY_ID = "category_id";
    public static final String COL_CATEGORY_NAME = "category_name";
    public static final String COL_CATEGORY_IMG_URL = "image_url";
    public static final String COL_CATEGORY_STATUS = "status";

    // Table: Recent Searches
    public static final String TABLE_RECENT_SEARCHES = "recent_searches";
    public static final String COL_SEARCH_ID = "id";
    public static final String COL_SEARCH_QUERY = "query";
    public static final String COL_SEARCH_TIMESTAMP = "timestamp";

    // Table: Favorites
    public static final String TABLE_FAVORITES = "favorites";
    public static final String COL_FAV_PRODUCT_ID = "product_id";
    public static final String COL_FAV_TITLE = "title";
    public static final String COL_FAV_CATEGORY = "category";
    public static final String COL_FAV_PRICE = "price";
    public static final String COL_FAV_IMAGE_URL = "image_url";
    public static final String COL_FAV_TIMESTAMP = "created_at";

    public static synchronized AppDatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new AppDatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }

    private AppDatabaseHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create Products Table
        String createProductsTable = "CREATE TABLE " + TABLE_PRODUCTS + " ("
                + COL_PRODUCT_ID + " TEXT PRIMARY KEY, "
                + COL_PRODUCT_TITLE + " TEXT, "
                + COL_PRODUCT_DESC + " TEXT, "
                + COL_PRODUCT_CATEGORY_ID + " TEXT, "
                + COL_PRODUCT_CATEGORY_NAME + " TEXT, "
                + COL_PRODUCT_BASE_PRICE + " REAL, "
                + COL_PRODUCT_STATUS + " INTEGER, "
                + COL_PRODUCT_IMAGES_JSON + " TEXT, "
                + COL_PRODUCT_VARIANTS_JSON + " TEXT, "
                + COL_PRODUCT_UPDATED_AT + " INTEGER"
                + ");";
        db.execSQL(createProductsTable);

        // Create Categories Table
        String createCategoriesTable = "CREATE TABLE " + TABLE_CATEGORIES + " ("
                + COL_CATEGORY_ID + " TEXT PRIMARY KEY, "
                + COL_CATEGORY_NAME + " TEXT, "
                + COL_CATEGORY_IMG_URL + " TEXT, "
                + COL_CATEGORY_STATUS + " INTEGER"
                + ");";
        db.execSQL(createCategoriesTable);

        // Create Recent Searches Table
        String createSearchesTable = "CREATE TABLE " + TABLE_RECENT_SEARCHES + " ("
                + COL_SEARCH_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_SEARCH_QUERY + " TEXT UNIQUE, "
                + COL_SEARCH_TIMESTAMP + " INTEGER"
                + ");";
        db.execSQL(createSearchesTable);

        // Create Favorites Table
        String createFavoritesTable = "CREATE TABLE " + TABLE_FAVORITES + " ("
                + COL_FAV_PRODUCT_ID + " TEXT PRIMARY KEY, "
                + COL_FAV_TITLE + " TEXT, "
                + COL_FAV_CATEGORY + " TEXT, "
                + COL_FAV_PRICE + " REAL, "
                + COL_FAV_IMAGE_URL + " TEXT, "
                + COL_FAV_TIMESTAMP + " INTEGER"
                + ");";
        db.execSQL(createFavoritesTable);

        Log.d(TAG, "SQLite Database & Tables created successfully.");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PRODUCTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CATEGORIES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RECENT_SEARCHES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_FAVORITES);
        onCreate(db);
    }

    // ==========================================
    // PRODUCTS OFFLINE CACHE
    // ==========================================

    public void saveProducts(List<Product> products) {
        if (products == null || products.isEmpty()) return;

        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            for (Product product : products) {
                if (product == null || product.getProductId() == null) continue;

                ContentValues values = new ContentValues();
                values.put(COL_PRODUCT_ID, product.getProductId());
                values.put(COL_PRODUCT_TITLE, product.getProductTitle());
                values.put(COL_PRODUCT_DESC, product.getDescription());
                values.put(COL_PRODUCT_CATEGORY_ID, product.getCategoryId());
                values.put(COL_PRODUCT_CATEGORY_NAME, product.getCategoryName());
                values.put(COL_PRODUCT_BASE_PRICE, product.getBasePrice());
                values.put(COL_PRODUCT_STATUS, product.isStatus() ? 1 : 0);
                values.put(COL_PRODUCT_IMAGES_JSON, gson.toJson(product.getProductImage()));
                values.put(COL_PRODUCT_VARIANTS_JSON, gson.toJson(product.getProductVariants()));
                values.put(COL_PRODUCT_UPDATED_AT, System.currentTimeMillis());

                db.insertWithOnConflict(TABLE_PRODUCTS, null, values, SQLiteDatabase.CONFLICT_REPLACE);
            }
            db.setTransactionSuccessful();
            Log.d(TAG, "Cached " + products.size() + " products to SQLite.");
        } catch (Exception e) {
            Log.e(TAG, "Error saving products to SQLite", e);
        } finally {
            db.endTransaction();
        }
    }

    public List<Product> getAllCachedProducts() {
        List<Product> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.query(
                TABLE_PRODUCTS,
                null,
                COL_PRODUCT_STATUS + " = 1",
                null,
                null,
                null,
                COL_PRODUCT_TITLE + " ASC"
        );

        if (cursor != null && cursor.moveToFirst()) {
            Type imageListType = new TypeToken<List<String>>() {}.getType();
            Type variantListType = new TypeToken<List<Product.ProductVariant>>() {}.getType();

            do {
                Product product = new Product();
                product.setProductId(cursor.getString(cursor.getColumnIndexOrThrow(COL_PRODUCT_ID)));
                product.setProductTitle(cursor.getString(cursor.getColumnIndexOrThrow(COL_PRODUCT_TITLE)));
                product.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(COL_PRODUCT_DESC)));
                product.setCategoryId(cursor.getString(cursor.getColumnIndexOrThrow(COL_PRODUCT_CATEGORY_ID)));
                product.setCategoryName(cursor.getString(cursor.getColumnIndexOrThrow(COL_PRODUCT_CATEGORY_NAME)));
                product.setBasePrice(cursor.getDouble(cursor.getColumnIndexOrThrow(COL_PRODUCT_BASE_PRICE)));
                product.setStatus(cursor.getInt(cursor.getColumnIndexOrThrow(COL_PRODUCT_STATUS)) == 1);

                String imagesJson = cursor.getString(cursor.getColumnIndexOrThrow(COL_PRODUCT_IMAGES_JSON));
                if (imagesJson != null) {
                    product.setProductImage(gson.fromJson(imagesJson, imageListType));
                }

                String variantsJson = cursor.getString(cursor.getColumnIndexOrThrow(COL_PRODUCT_VARIANTS_JSON));
                if (variantsJson != null) {
                    product.setProductVariants(gson.fromJson(variantsJson, variantListType));
                }

                list.add(product);
            } while (cursor.moveToNext());

            cursor.close();
        }

        return list;
    }

    public List<Product> searchCachedProducts(String query) {
        List<Product> list = new ArrayList<>();
        if (query == null || query.trim().isEmpty()) return list;

        SQLiteDatabase db = getReadableDatabase();
        String selection = COL_PRODUCT_STATUS + " = 1 AND (" + COL_PRODUCT_TITLE + " LIKE ? OR " + COL_PRODUCT_CATEGORY_NAME + " LIKE ?)";
        String[] selectionArgs = new String[]{"%" + query + "%", "%" + query + "%"};

        Cursor cursor = db.query(TABLE_PRODUCTS, null, selection, selectionArgs, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            Type imageListType = new TypeToken<List<String>>() {}.getType();
            do {
                Product product = new Product();
                product.setProductId(cursor.getString(cursor.getColumnIndexOrThrow(COL_PRODUCT_ID)));
                product.setProductTitle(cursor.getString(cursor.getColumnIndexOrThrow(COL_PRODUCT_TITLE)));
                product.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(COL_PRODUCT_DESC)));
                product.setCategoryId(cursor.getString(cursor.getColumnIndexOrThrow(COL_PRODUCT_CATEGORY_ID)));
                product.setCategoryName(cursor.getString(cursor.getColumnIndexOrThrow(COL_PRODUCT_CATEGORY_NAME)));
                product.setBasePrice(cursor.getDouble(cursor.getColumnIndexOrThrow(COL_PRODUCT_BASE_PRICE)));
                product.setStatus(true);

                String imagesJson = cursor.getString(cursor.getColumnIndexOrThrow(COL_PRODUCT_IMAGES_JSON));
                if (imagesJson != null) {
                    product.setProductImage(gson.fromJson(imagesJson, imageListType));
                }
                list.add(product);
            } while (cursor.moveToNext());
            cursor.close();
        }
        return list;
    }

    // ==========================================
    // CATEGORIES OFFLINE CACHE
    // ==========================================

    public void saveCategories(List<Category> categories) {
        if (categories == null || categories.isEmpty()) return;

        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            for (Category cat : categories) {
                if (cat == null || cat.getCategoryId() == null) continue;

                ContentValues values = new ContentValues();
                values.put(COL_CATEGORY_ID, cat.getCategoryId());
                values.put(COL_CATEGORY_NAME, cat.getCategoryName());
                values.put(COL_CATEGORY_IMG_URL, cat.getImgUrl());
                values.put(COL_CATEGORY_STATUS, cat.isStatus() ? 1 : 0);

                db.insertWithOnConflict(TABLE_CATEGORIES, null, values, SQLiteDatabase.CONFLICT_REPLACE);
            }
            db.setTransactionSuccessful();
            Log.d(TAG, "Cached " + categories.size() + " categories to SQLite.");
        } catch (Exception e) {
            Log.e(TAG, "Error saving categories to SQLite", e);
        } finally {
            db.endTransaction();
        }
    }

    public List<Category> getAllCachedCategories() {
        List<Category> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.query(
                TABLE_CATEGORIES,
                null,
                COL_CATEGORY_STATUS + " = 1",
                null,
                null,
                null,
                COL_CATEGORY_NAME + " ASC"
        );

        if (cursor != null && cursor.moveToFirst()) {
            do {
                Category cat = new Category();
                cat.setCategoryId(cursor.getString(cursor.getColumnIndexOrThrow(COL_CATEGORY_ID)));
                cat.setCategoryName(cursor.getString(cursor.getColumnIndexOrThrow(COL_CATEGORY_NAME)));
                cat.setImgUrl(cursor.getString(cursor.getColumnIndexOrThrow(COL_CATEGORY_IMG_URL)));
                cat.setStatus(cursor.getInt(cursor.getColumnIndexOrThrow(COL_CATEGORY_STATUS)) == 1);
                list.add(cat);
            } while (cursor.moveToNext());

            cursor.close();
        }
        return list;
    }

    // ==========================================
    // RECENT SEARCHES
    // ==========================================

    public void addRecentSearch(String query) {
        if (query == null || query.trim().isEmpty()) return;

        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_SEARCH_QUERY, query.trim());
        values.put(COL_SEARCH_TIMESTAMP, System.currentTimeMillis());

        db.insertWithOnConflict(TABLE_RECENT_SEARCHES, null, values, SQLiteDatabase.CONFLICT_REPLACE);
    }

    public List<String> getRecentSearches(int limit) {
        List<String> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.query(
                TABLE_RECENT_SEARCHES,
                new String[]{COL_SEARCH_QUERY},
                null,
                null,
                null,
                null,
                COL_SEARCH_TIMESTAMP + " DESC",
                String.valueOf(limit)
        );

        if (cursor != null && cursor.moveToFirst()) {
            do {
                list.add(cursor.getString(cursor.getColumnIndexOrThrow(COL_SEARCH_QUERY)));
            } while (cursor.moveToNext());
            cursor.close();
        }
        return list;
    }

    public void clearRecentSearches() {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_RECENT_SEARCHES, null, null);
    }

    // ==========================================
    // FAVORITES / WISHLIST
    // ==========================================

    public void addFavorite(Product product) {
        if (product == null || product.getProductId() == null) return;

        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_FAV_PRODUCT_ID, product.getProductId());
        values.put(COL_FAV_TITLE, product.getProductTitle());
        values.put(COL_FAV_CATEGORY, product.getCategoryName());
        values.put(COL_FAV_PRICE, product.getBasePrice());

        String img = (product.getProductImage() != null && !product.getProductImage().isEmpty())
                ? product.getProductImage().get(0) : "";
        values.put(COL_FAV_IMAGE_URL, img);
        values.put(COL_FAV_TIMESTAMP, System.currentTimeMillis());

        db.insertWithOnConflict(TABLE_FAVORITES, null, values, SQLiteDatabase.CONFLICT_REPLACE);
    }

    public void removeFavorite(String productId) {
        if (productId == null) return;
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_FAVORITES, COL_FAV_PRODUCT_ID + " = ?", new String[]{productId});
    }

    public boolean isFavorite(String productId) {
        if (productId == null) return false;
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(
                TABLE_FAVORITES,
                new String[]{COL_FAV_PRODUCT_ID},
                COL_FAV_PRODUCT_ID + " = ?",
                new String[]{productId},
                null,
                null,
                null
        );
        boolean exists = (cursor != null && cursor.getCount() > 0);
        if (cursor != null) cursor.close();
        return exists;
    }
}
