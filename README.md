## 📱 App Screenshots

<details>
<summary><b>Click here to view all App Screenshots (Tap to expand)</b></summary>

<br>
## 🚀 App Introduction & Onboarding

| Step 1: Welcome / Launch | Step 2: Explore Features | Step 3: Fast Ordering | Step 4: Get Started |
| :---: | :---: | :---: | :---: |
| <img src="https://github.com/user-attachments/assets/d3b69a7d-2216-4659-9e10-5ab0fcf13f2b" width="200" /> | <img src="https://github.com/user-attachments/assets/a55b8533-eb38-4b6b-833f-4e1a9c19a2b1" width="200" /> | <img src="https://github.com/user-attachments/assets/0be14dc1-0a08-4761-a8c5-43d1c9cd250f" width="200" /> | <img src="https://github.com/user-attachments/assets/c6a9120a-dc7f-41a5-88cc-78b683b0b971" width="200" /> |

| Authentication & Login | Sign Up | Home | Menu |
| :---: | :---: | :---: | :---: |
|<img width="200" alt="Screenshot_20261031_112333" src="https://github.com/user-attachments/assets/59b2b111-1bc4-4708-a716-bee42cd74cc8" /> |<img width="200" alt="Screenshot_20261031_112346" src="https://github.com/user-attachments/assets/342b0a21-bd75-44db-8d84-b7194945ec56" />  | <img src="https://github.com/user-attachments/assets/276fa555-645b-49e1-ab44-00c6050a5b1d" width="200" />| <img src="https://github.com/user-attachments/assets/78cb05d8-1b6a-460c-818f-0ebfdd403f32" width="200" /> | 

| Side bar | Help Center | About us | Security |
| :---: | :---: | :---: | :---: |
| <img src="https://github.com/user-attachments/assets/88dfdfbc-b170-4b60-8674-3f47beba1dd3" width="200" /> | <img src="https://github.com/user-attachments/assets/fe7d7677-f91b-47ee-90c7-91c3a46d0f67" width="200" /> | <img src="https://github.com/user-attachments/assets/0e268cc4-00be-45c7-85bb-48db14deced3" width="200" /> | <img src="https://github.com/user-attachments/assets/b4fceedc-bbac-4ea3-a0ca-fad28be41166" width="200" /> |

| QR Scanning | Pin on map | New Address | Cart |
| :---: | :---: | :---: | :---: |
| <img src="https://github.com/user-attachments/assets/56435233-9c69-4bec-9ad5-6af34124e2ee" width="200" /> | <img src="https://github.com/user-attachments/assets/531825ab-d288-457f-89c9-a93056c9ecd1" width="200" /> | <img src="https://github.com/user-attachments/assets/f420ec44-4ee7-4253-917f-f7aaa47c477f" width="200" /> | <img src="https://github.com/user-attachments/assets/ace42568-8573-474e-a523-b17d97d0a969" width="200" /> |

| UI Screen 13 | UI Screen 14 | UI Screen 15 | UI Screen 16 |
| :---: | :---: | :---: | :---: |
| <img src="https://github.com/user-attachments/assets/fd8409ee-5013-4f9c-b59c-bff3b6e5f672" width="200" /> | <img src="https://github.com/user-attachments/assets/90023748-df9d-4d90-8f46-32e52c5caaa9" width="200" /> | <img src="https://github.com/user-attachments/assets/8a067ca4-1abb-46bc-9d39-c05aab8f1694" width="200" /> | <img src="https://github.com/user-attachments/assets/0d5ad068-6c27-4f3f-ba67-980c1bc685b0" width="200" /> |

|  | Light Home | My order | Final Screen |
| :---: | :---: | :---: | :---: |
|  | <img src="https://github.com/user-attachments/assets/d4917c33-aace-4e6c-90f4-12c4e14c9700" width="200" /> |<img src="https://github.com/user-attachments/assets/4929f570-287c-452f-99ec-62b7a296ec44" width="200" /> |<img src="https://github.com/user-attachments/assets/b6953288-2ac4-4b01-aa6a-028d6f6f6811" width="200" /> |


</details>
## ✨ Key Features & Capabilities

### 🔐 Authentication & Validation
* **Secure Sign-Up & Sign-In:** Powered by Firebase Authentication for reliable user management.
* **Smart Form Validation:** Built-in validation rules for secure passwords and Sri Lankan mobile numbers (`07XXXXXXXX`).

### 🍔 Food Discovery & Browsing
* **Promotional Banners:** Interactive ad banner slider on the home screen using `ViewPager2`.
* **Category Filtering:** Horizontal food category filtering (Burgers, Pizzas, Rice, Beverages, etc.).
* **Real-time Search:** Instant search bar to quickly find favorite food items.
* **Smooth UX:** Equipped with shimmer skeleton loading animations and swipe-to-refresh.

### 🍕 Product Customization & Pricing
* **Photo Carousel:** Multi-image photo preview carousel with dot indicators.
* **Dynamic Variants:** Portion size and variant selection (e.g., Regular, Large) with automatic price re-calculation.
* **Interactive Controls:** Quantity increment/decrement steppers along with smart food recommendations.

### 🛒 Smart Cart & Checkout
* **Real-time Calculations:** Instant calculation of item subtotals, variant add-on costs, and grand totals.
* **Flexible Order Modes:** Choose seamlessly between Home Delivery and Dine-In order modes.
* **Pre-checkout Validation:** Automated stock checking and price change verification before placing orders.

### 📷 Dine-In QR Table Ordering
* **QR Scanner:** Built-in in-app camera QR code scanner (ZXing) to scan restaurant table QR codes.
* **Table Binding:** Automatically binds the scanned table number to the active order.

### 📍 Location & Map Pinning
* **GPS Auto-Detect:** Automatically detects current GPS location using `FusedLocationProviderClient`.
* **Interactive Maps:** Pin-drop map selection powered by OpenStreetMap (`OSMDroid`).
* **Reverse Geocoding:** Converts map coordinates into precise street addresses with Sri Lanka boundary restrictions and a saved address book (Home, Work, etc.).

### 📦 Real-time Order Tracking
* **Live Status Updates:** Live status progression via Firestore listeners (`Pending` ➔ `Preparing` ➔ `Completed` / `Delivered`).
* **Order History:** Complete history logs with digital receipts, timestamps, and detailed item breakdowns.

### 👤 Profile & Cloud Storage
* **Profile Management:** Clean profile details editor for Name, Phone number, and delivery addresses.
* **Cloud Avatars:** Cloud photo upload and storage for profile avatars via Cloudinary API.

