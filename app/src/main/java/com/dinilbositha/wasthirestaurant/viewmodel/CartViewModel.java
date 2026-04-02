package com.dinilbositha.wasthirestaurant.viewmodel;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.dinilbositha.wasthirestaurant.model.CartItem;
import com.dinilbositha.wasthirestaurant.model.CartValidationResult;
import com.dinilbositha.wasthirestaurant.repository.CartRepository;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class CartViewModel extends AndroidViewModel {

    public interface ValidateCartCallback {
        void onComplete(List<CartValidationResult> results);
        void onFailure(Exception e);
    }

    private final MutableLiveData<List<CartItem>> cartItems = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<String> orderType = new MutableLiveData<>("TABLE_ORDER");
    private final MutableLiveData<String> tableNumber = new MutableLiveData<>("");
    private final MutableLiveData<String> deliveryAddress = new MutableLiveData<>("");

    private final MutableLiveData<Boolean> cartHasInvalidItems = new MutableLiveData<>(false);
    private final MutableLiveData<String> cartValidationMessage = new MutableLiveData<>("");

    private final CartRepository repository;

    public CartViewModel(@NonNull Application application) {
        super(application);
        repository = new CartRepository();
    }

    public LiveData<List<CartItem>> getCartItems() {
        return cartItems;
    }

    public LiveData<String> getOrderType() {
        return orderType;
    }

    public LiveData<String> getTableNumber() {
        return tableNumber;
    }

    public LiveData<String> getDeliveryAddress() {
        return deliveryAddress;
    }

    public LiveData<Boolean> getCartHasInvalidItems() {
        return cartHasInvalidItems;
    }

    public LiveData<String> getCartValidationMessage() {
        return cartValidationMessage;
    }

    public void setOrderType(String type) {
        orderType.setValue(type);
    }

    public void setTableNumber(String table) {
        tableNumber.setValue(table);
    }

    public void setDeliveryAddress(String address) {
        deliveryAddress.setValue(address);
    }

    public void addToCart(CartItem item) {
        List<CartItem> current = cartItems.getValue();
        if (current == null) current = new ArrayList<>();

        boolean found = false;

        for (CartItem cartItem : current) {
            if (isSameItem(cartItem, item)) {
                cartItem.setQuantity(cartItem.getQuantity() + item.getQuantity());
                found = true;

                if (repository.isUserLoggedIn()) {
                    repository.saveCartItemToFirestore(
                            cartItem,
                            unused -> Log.d("CartSync", "Cart item updated in Firestore"),
                            e -> Log.e("CartSync", "Failed to update cart item", e)
                    );
                }
                break;
            }
        }

        if (!found) {
            item.setAvailable(true);
            item.setChanged(false);
            item.setValidationMessage("");
            current.add(item);

            if (repository.isUserLoggedIn()) {
                repository.saveCartItemToFirestore(
                        item,
                        unused -> Log.d("CartSync", "Cart item saved to Firestore"),
                        e -> Log.e("CartSync", "Failed to save cart item", e)
                );
            }
        }

        cartItems.setValue(new ArrayList<>(current));
    }

    public void syncLocalCartToFirestore() {
        List<CartItem> current = cartItems.getValue();

        repository.syncCartToFirestore(
                current,
                unused -> Log.d("CartSync", "Local cart synced successfully"),
                e -> Log.e("CartSync", "Cart sync failed", e)
        );
    }

    public void loadCartFromFirestore() {
        repository.getCartItemsFromFirestore(new CartRepository.CartLoadCallback() {
            @Override
            public void onSuccess(List<CartItem> items) {
                if (items != null) {
                    for (CartItem item : items) {
                        if (item != null) {
                            if (item.getValidationMessage() == null) item.setValidationMessage("");
                            item.setAvailable(true);
                            item.setChanged(false);
                        }
                    }
                }

                cartItems.postValue(items != null ? items : new ArrayList<>());
            }

            @Override
            public void onFailure(Exception e) {
                Log.e("CartViewModel", "Error loading cart from Firestore", e);
            }
        });
    }

    public void validateCart(ValidateCartCallback callback) {
        List<CartItem> current = cartItems.getValue();

        repository.validateCartItems(current, new CartRepository.CartValidationCallback() {
            @Override
            public void onComplete(List<CartValidationResult> results, List<CartItem> updatedCartItems) {
                boolean hasInvalid = false;
                StringBuilder builder = new StringBuilder();

                for (CartValidationResult result : results) {
                    if (result != null && !result.isValid()) {
                        hasInvalid = true;
                        if (builder.length() > 0) {
                            builder.append("\n");
                        }
                        builder.append(result.getMessage());
                    }
                }

                cartHasInvalidItems.postValue(hasInvalid);
                cartValidationMessage.postValue(builder.toString());
                cartItems.postValue(new ArrayList<>(updatedCartItems));

                if (repository.isUserLoggedIn()) {
                    repository.syncCartToFirestore(
                            updatedCartItems,
                            unused -> Log.d("CartValidation", "Validated cart synced"),
                            e -> Log.e("CartValidation", "Failed to sync validated cart", e)
                    );
                }

                if (callback != null) {
                    callback.onComplete(results);
                }
            }

            @Override
            public void onFailure(Exception e) {
                if (callback != null) {
                    callback.onFailure(e);
                }
            }
        });
    }

    public List<CartItem> getInvalidItems() {
        List<CartItem> current = cartItems.getValue();
        List<CartItem> invalidItems = new ArrayList<>();

        if (current != null) {
            for (CartItem item : current) {
                if (item != null && !item.isAvailable()) {
                    invalidItems.add(item);
                }
            }
        }

        return invalidItems;
    }

    public void removeInvalidItems() {
        List<CartItem> current = cartItems.getValue();
        if (current == null) return;

        Iterator<CartItem> iterator = current.iterator();
        while (iterator.hasNext()) {
            CartItem cartItem = iterator.next();
            if (cartItem != null && !cartItem.isAvailable()) {
                if (repository.isUserLoggedIn()) {
                    repository.removeCartItemFromFirestore(
                            cartItem,
                            unused -> Log.d("CartCleanup", "Invalid item removed"),
                            e -> Log.e("CartCleanup", "Failed to remove invalid item", e)
                    );
                }
                iterator.remove();
            }
        }

        cartItems.setValue(new ArrayList<>(current));
        cartHasInvalidItems.setValue(false);
        cartValidationMessage.setValue("");
    }

    public void increaseQty(CartItem item) {
        List<CartItem> current = cartItems.getValue();
        if (current == null) return;

        for (CartItem cartItem : current) {
            if (isSameItem(cartItem, item)) {
                cartItem.setQuantity(cartItem.getQuantity() + 1);

                if (repository.isUserLoggedIn()) {
                    repository.saveCartItemToFirestore(
                            cartItem,
                            unused -> Log.d("CartUpdate", "Quantity increased in Firestore"),
                            e -> Log.e("CartUpdate", "Failed to increase quantity", e)
                    );
                }
                break;
            }
        }

        cartItems.setValue(new ArrayList<>(current));
    }

    public void decreaseQty(CartItem item) {
        List<CartItem> current = cartItems.getValue();
        if (current == null) return;

        Iterator<CartItem> iterator = current.iterator();

        while (iterator.hasNext()) {
            CartItem cartItem = iterator.next();

            if (isSameItem(cartItem, item)) {
                if (cartItem.getQuantity() > 1) {
                    cartItem.setQuantity(cartItem.getQuantity() - 1);

                    if (repository.isUserLoggedIn()) {
                        repository.saveCartItemToFirestore(
                                cartItem,
                                unused -> Log.d("CartUpdate", "Quantity decreased in Firestore"),
                                e -> Log.e("CartUpdate", "Failed to decrease quantity", e)
                        );
                    }
                } else {
                    iterator.remove();

                    if (repository.isUserLoggedIn()) {
                        repository.removeCartItemFromFirestore(
                                cartItem,
                                unused -> Log.d("CartUpdate", "Item removed from Firestore"),
                                e -> Log.e("CartUpdate", "Failed to remove item", e)
                        );
                    }
                }
                break;
            }
        }

        cartItems.setValue(new ArrayList<>(current));
    }

    public void removeItem(CartItem item) {
        List<CartItem> current = cartItems.getValue();
        if (current == null) return;

        Iterator<CartItem> iterator = current.iterator();

        while (iterator.hasNext()) {
            CartItem cartItem = iterator.next();
            if (isSameItem(cartItem, item)) {
                iterator.remove();

                if (repository.isUserLoggedIn()) {
                    repository.removeCartItemFromFirestore(
                            cartItem,
                            unused -> Log.d("CartRemove", "Removed from Firestore"),
                            e -> Log.e("CartRemove", "Failed to remove item", e)
                    );
                }
                break;
            }
        }

        cartItems.setValue(new ArrayList<>(current));
    }

    public void clearCart() {
        cartItems.setValue(new ArrayList<>());

        if (repository.isUserLoggedIn()) {
            repository.clearCartFromFirestore(
                    unused -> Log.d("CartClear", "Cart cleared from Firestore"),
                    e -> Log.e("CartClear", "Failed to clear Firestore cart", e)
            );
        }
    }

    public boolean hasCartItems() {
        List<CartItem> items = cartItems.getValue();
        return items != null && !items.isEmpty();
    }

    private boolean isSameItem(CartItem a, CartItem b) {
        if (a == null || b == null) return false;
        if (a.getProductId() == null || b.getProductId() == null) return false;

        return a.getProductId().equals(b.getProductId()) &&
                variantsEqual(a.getSelectedVariants(), b.getSelectedVariants());
    }

    private boolean variantsEqual(List<CartItem.SelectedVariant> a, List<CartItem.SelectedVariant> b) {
        if (a == null && b == null) return true;
        if (a == null || b == null) return false;
        if (a.size() != b.size()) return false;

        for (int i = 0; i < a.size(); i++) {
            CartItem.SelectedVariant va = a.get(i);
            CartItem.SelectedVariant vb = b.get(i);

            if (va == null || vb == null) return false;

            if (!safeEquals(va.getType(), vb.getType())) return false;
            if (!safeEquals(va.getName(), vb.getName())) return false;
        }

        return true;
    }

    private boolean safeEquals(String a, String b) {
        if (a == null) return b == null;
        return a.equals(b);
    }

    public double getSubtotal() {
        double subtotal = 0;
        List<CartItem> current = cartItems.getValue();
        if (current != null) {
            for (CartItem item : current) {
                if (item != null && item.isAvailable()) {
                    subtotal += item.getUnitPrice() * item.getQuantity();
                }
            }
        }
        return subtotal;
    }

    public double getTax() {
        return getSubtotal() * 0.05;
    }

    public double getDeliveryFee() {
        String type = orderType.getValue();
        return "DELIVERY".equals(type) ? 300.0 : 0.0;
    }

    public double getTotal() {
        return getSubtotal() + getTax() + getDeliveryFee();
    }
}