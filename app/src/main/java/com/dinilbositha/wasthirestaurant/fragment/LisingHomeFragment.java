package com.dinilbositha.wasthirestaurant.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.dinilbositha.wasthirestaurant.R;
import com.google.firebase.firestore.FirebaseFirestore;

public class LisingHomeFragment extends Fragment {

    public LisingHomeFragment() {

    }

      @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_lising_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
//
///// cat-1: Rice & Mains
//        Product p1 = new Product("pid1", "Rice & Curry", "Traditional steamed rice with 5 vegetable curries.", "cat1", Arrays.asList("img_1.jpg"), 850.0, true);
//        Product p2 = new Product("pid2", "Fried Rice", "Wok-fried rice with chicken, eggs, and veggies.", "cat1", Collections.emptyList(), 1100.0, true);
//
//// cat-2: Burgers & Sandwiches
//        Product p3 = new Product("pid3","Zinger Burger", "Crispy spicy chicken fillet with cheese and hash brown.", "cat2", Arrays.asList("img_3.jpg", "img_3b.jpg"), 1350.0, true);
//        Product p4 = new Product("pid4", "Club Sandwich", "Triple-decker toasted sandwich with egg and chicken.", "cat2", Collections.emptyList(), 950.0, false);
//
//// cat-3: Pizza & Pasta
//        Product p5 = new Product("pid5", "Cheese Pizza", "Classic Italian pizza with rich tomato and mozzarella.", "cat3", Arrays.asList("img_5.jpg"), 1800.0, true);
//        Product p6 = new Product("pid6", "Alfredo Pasta", "Penne pasta in a rich white mushroom sauce.", "cat3", Collections.emptyList(), 1250.0, true);
//
//// cat-4: Beverages
//        Product p7 = new Product("pid7","Watermelon Juice", "100% natural chilled fruit juice.", "cat4", Arrays.asList("img_7.jpg"), 450.0, true);
//
//// cat-5: Desserts
//        Product p8 = new Product("pid8",  "Lava Cake", "Warm chocolate cake with a molten center.", "cat5", Arrays.asList("img_8.jpg"), 550.0, true);
//        Product p9 = new Product("pid9","Watalappam", "Traditional coconut custard with jaggery and nuts.", "cat-5", Collections.emptyList(), 400.0, true);
//
//// cat-6: Snacks/Sides
//        Product p10 = new Product("pid10", "French Fries", "Golden crispy potato fries with sea salt.", "cat6", Arrays.asList("img_10.jpg"), 500.0, true);
//
//        List<Product> list = List.of(p1,p2,p3,p4,p5,p6,p7,p8,p9,p10);
//        WriteBatch batch = db.batch();
//        for(Product c : list){
//            DocumentReference ref = db.collection("products").document();
//            batch.set(ref,c);
//        }
//        batch.commit();

    }
}