package com.dinilbositha.wasthirestaurant.repository;

import com.dinilbositha.wasthirestaurant.model.CartItem;
import com.dinilbositha.wasthirestaurant.model.CartValidationResult;
import com.dinilbositha.wasthirestaurant.model.Product;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class CartRepository {

    public interface CartLoadCallback {
        void onSuccess(List<CartItem> cartItems);
        void onFailure(Exception e);
    }

    public interface CartValidationCallback {
        void onComplete(List<CartValidationResult> results, List<CartItem> updatedCartItems);
        void onFailure(Exception e);
    }

    private final FirebaseFirestore firestore;
    private final FirebaseAuth auth;

    public CartRepository() {
        firestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
    }

    public boolean isUserLoggedIn() {
        return auth.getCurrentUser() != null;
    }

    public void saveCartItemToFirestore(CartItem cartItem,
                                        OnSuccessListener<Void> onSuccess,
                                        OnFailureListener onFailure) {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) return;

        String cartItemId = generateCartItemId(cartItem);
        cartItem.setCartItemId(cartItemId);

        firestore.collection("users")
                .document(user.getUid())
                .collection("cart")
                .document(cartItemId)
                .set(cartItem)
                .addOnSuccessListener(onSuccess)
                .addOnFailureListener(onFailure);
    }

    public void syncCartToFirestore(List<CartItem> cartItems,
                                    OnSuccessListener<Void> onSuccess,
                                    OnFailureListener onFailure) {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) return;

        if (cartItems == null || cartItems.isEmpty()) {
            if (onSuccess != null) onSuccess.onSuccess(null);
            return;
        }

        final int total = cartItems.size();
        final int[] completed = {0};
        final boolean[] failed = {false};

        for (CartItem item : cartItems) {
            String cartItemId = generateCartItemId(item);
            item.setCartItemId(cartItemId);

            firestore.collection("users")
                    .document(user.getUid())
                    .collection("cart")
                    .document(cartItemId)
                    .set(item)
                    .addOnSuccessListener(unused -> {
                        if (failed[0]) return;

                        completed[0]++;
                        if (completed[0] == total && onSuccess != null) {
                            onSuccess.onSuccess(null);
                        }
                    })
                    .addOnFailureListener(e -> {
                        if (!failed[0]) {
                            failed[0] = true;
                            if (onFailure != null) {
                                onFailure.onFailure(e);
                            }
                        }
                    });
        }
    }

    public void getCartItemsFromFirestore(CartLoadCallback callback) {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            if (callback != null) {
                callback.onSuccess(new ArrayList<>());
            }
            return;
        }

        firestore.collection("users")
                .document(user.getUid())
                .collection("cart")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<CartItem> cartItems = new ArrayList<>();

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        CartItem item = document.toObject(CartItem.class);
                        if (item != null) {
                            item.setCartItemId(document.getId());
                            cartItems.add(item);
                        }
                    }

                    if (callback != null) {
                        callback.onSuccess(cartItems);
                    }
                })
                .addOnFailureListener(e -> {
                    if (callback != null) {
                        callback.onFailure(e);
                    }
                });
    }

    public void removeCartItemFromFirestore(CartItem cartItem,
                                            OnSuccessListener<Void> onSuccess,
                                            OnFailureListener onFailure) {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null || cartItem == null) return;

        String cartItemId = cartItem.getCartItemId();
        if (cartItemId == null || cartItemId.isEmpty()) {
            cartItemId = generateCartItemId(cartItem);
        }

        firestore.collection("users")
                .document(user.getUid())
                .collection("cart")
                .document(cartItemId)
                .delete()
                .addOnSuccessListener(onSuccess)
                .addOnFailureListener(onFailure);
    }

    public void clearCartFromFirestore(OnSuccessListener<Void> onSuccess,
                                       OnFailureListener onFailure) {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) return;

        firestore.collection("users")
                .document(user.getUid())
                .collection("cart")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        if (onSuccess != null) onSuccess.onSuccess(null);
                        return;
                    }

                    final int total = queryDocumentSnapshots.size();
                    final int[] completed = {0};
                    final boolean[] failed = {false};

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        document.getReference().delete()
                                .addOnSuccessListener(unused -> {
                                    if (failed[0]) return;

                                    completed[0]++;
                                    if (completed[0] == total && onSuccess != null) {
                                        onSuccess.onSuccess(null);
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    if (!failed[0]) {
                                        failed[0] = true;
                                        if (onFailure != null) {
                                            onFailure.onFailure(e);
                                        }
                                    }
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    if (onFailure != null) {
                        onFailure.onFailure(e);
                    }
                });
    }

    public void validateCartItems(List<CartItem> cartItems, CartValidationCallback callback) {
        if (cartItems == null || cartItems.isEmpty()) {
            if (callback != null) {
                callback.onComplete(new ArrayList<>(), new ArrayList<>());
            }
            return;
        }

        List<CartValidationResult> results = new ArrayList<>();
        List<CartItem> updatedItems = new ArrayList<>();

        final int total = cartItems.size();
        final int[] completed = {0};
        final boolean[] failed = {false};

        for (CartItem cartItem : cartItems) {
            firestore.collection("products")
                    .whereEqualTo("productId", cartItem.getProductId())
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        if (failed[0]) return;

                        CartValidationResult result;

                        if (queryDocumentSnapshots.isEmpty()) {
                            cartItem.setAvailable(false);
                            cartItem.setChanged(false);
                            cartItem.setValidationMessage("Product not found");
                            result = new CartValidationResult(cartItem, false, false, "Product not found");
                        } else {
                            Product product = queryDocumentSnapshots.getDocuments()
                                    .get(0)
                                    .toObject(Product.class);

                            result = validateSingleCartItem(cartItem, product);
                        }

                        results.add(result);
                        updatedItems.add(result.getCartItem());

                        completed[0]++;
                        if (completed[0] == total && callback != null) {
                            callback.onComplete(results, updatedItems);
                        }
                    })
                    .addOnFailureListener(e -> {
                        if (!failed[0]) {
                            failed[0] = true;
                            if (callback != null) {
                                callback.onFailure(e);
                            }
                        }
                    });
        }
    }

    private CartValidationResult validateSingleCartItem(CartItem cartItem, Product product) {
        if (product == null) {
            cartItem.setAvailable(false);
            cartItem.setChanged(false);
            cartItem.setValidationMessage("Product not found");
            return new CartValidationResult(cartItem, false, false, "Product not found");
        }

        if (!product.isStatus()) {
            cartItem.setAvailable(false);
            cartItem.setChanged(false);
            cartItem.setValidationMessage("This product is no longer available");
            return new CartValidationResult(cartItem, false, false, "This product is no longer available");
        }

        boolean changed = false;
        StringBuilder messageBuilder = new StringBuilder();

        if (!safeEquals(cartItem.getProductTitle(), product.getProductTitle())) {
            cartItem.setProductTitle(product.getProductTitle());
            changed = true;
        }

        String latestImage = "";
        if (product.getProductImage() != null && !product.getProductImage().isEmpty()) {
            latestImage = product.getProductImage().get(0);
        }
        if (!safeEquals(cartItem.getProductImage(), latestImage)) {
            cartItem.setProductImage(latestImage);
            changed = true;
        }

        double latestUnitPrice = product.getBasePrice();
        List<CartItem.SelectedVariant> selectedVariants = cartItem.getSelectedVariants();

        if (selectedVariants != null && !selectedVariants.isEmpty()) {
            List<CartItem.SelectedVariant> updatedSelectedVariants = new ArrayList<>();

            for (CartItem.SelectedVariant selected : selectedVariants) {
                Product.ProductVariant matchedVariant = findMatchingVariant(product.getProductVariants(), selected);

                if (matchedVariant == null) {
                    cartItem.setAvailable(false);
                    cartItem.setChanged(false);
                    cartItem.setValidationMessage(
                            "Selected variant unavailable: " + selected.getType() + " - " + selected.getName()
                    );
                    return new CartValidationResult(
                            cartItem,
                            false,
                            false,
                            "Selected variant unavailable: " + selected.getType() + " - " + selected.getName()
                    );
                }

                if (!matchedVariant.isVariantStatus()) {
                    cartItem.setAvailable(false);
                    cartItem.setChanged(false);
                    cartItem.setValidationMessage(
                            "Selected variant is disabled: " + selected.getType() + " - " + selected.getName()
                    );
                    return new CartValidationResult(
                            cartItem,
                            false,
                            false,
                            "Selected variant is disabled: " + selected.getType() + " - " + selected.getName()
                    );
                }

                if (Double.compare(selected.getExtraPrice(), matchedVariant.getProductPrice()) != 0) {
                    changed = true;
                }

                updatedSelectedVariants.add(
                        CartItem.SelectedVariant.builder()
                                .type(matchedVariant.getType())
                                .name(matchedVariant.getVariantName())
                                .extraPrice(matchedVariant.getProductPrice())
                                .build()
                );

                latestUnitPrice += matchedVariant.getProductPrice();
            }

            cartItem.setSelectedVariants(updatedSelectedVariants);
        }

        if (Double.compare(cartItem.getUnitPrice(), latestUnitPrice) != 0) {
            changed = true;
            messageBuilder.append("Price updated. ");
            cartItem.setUnitPrice(latestUnitPrice);
        }

        cartItem.setAvailable(true);
        cartItem.setChanged(changed);

        String message = changed ? "Cart item updated with latest product changes" : "Valid";
        if (messageBuilder.length() > 0) {
            message = messageBuilder.toString().trim();
        }

        cartItem.setValidationMessage(message);

        return new CartValidationResult(cartItem, true, changed, message);
    }

    private Product.ProductVariant findMatchingVariant(List<Product.ProductVariant> variants,
                                                       CartItem.SelectedVariant selectedVariant) {
        if (variants == null || selectedVariant == null) return null;

        for (Product.ProductVariant variant : variants) {
            if (variant == null) continue;

            if (safeEquals(variant.getType(), selectedVariant.getType()) &&
                    safeEquals(variant.getVariantName(), selectedVariant.getName())) {
                return variant;
            }
        }
        return null;
    }

    private boolean safeEquals(String a, String b) {
        if (a == null) return b == null;
        return a.equals(b);
    }

    private String generateCartItemId(CartItem item) {
        StringBuilder builder = new StringBuilder();
        builder.append(item.getProductId() != null ? item.getProductId() : "");

        if (item.getSelectedVariants() != null) {
            for (CartItem.SelectedVariant variant : item.getSelectedVariants()) {
                if (variant != null) {
                    builder.append("_")
                            .append(variant.getType() != null ? variant.getType() : "")
                            .append("_")
                            .append(variant.getName() != null ? variant.getName() : "");
                }
            }
        }

        return builder.toString().replaceAll("\\s+", "_");
    }
}