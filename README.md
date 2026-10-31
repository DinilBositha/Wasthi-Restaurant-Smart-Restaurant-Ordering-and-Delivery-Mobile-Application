Adamin App - Wasthi
<div align="center">

<p>
  <img src="https://github.com/user-attachments/assets/c145ad23-5ac2-4fc0-9163-63ea82371d8c" width="210" style="border-radius: 16px; box-shadow: 0 8px 24px rgba(0,0,0,0.15); margin: 8px;" />
  <img src="https://github.com/user-attachments/assets/de20a0c1-a61d-4a06-b718-5d262646df93" width="210" style="border-radius: 16px; box-shadow: 0 8px 24px rgba(0,0,0,0.15); margin: 8px;" />
  <img src="https://github.com/user-attachments/assets/2906c029-2c6e-4fa1-aa04-b89c370d02bf" width="210" style="border-radius: 16px; box-shadow: 0 8px 24px rgba(0,0,0,0.15); margin: 8px;" />
  <img src="https://github.com/user-attachments/assets/a7879bb2-e565-46a3-a67c-2c2c1bd53475" width="210" style="border-radius: 16px; box-shadow: 0 8px 24px rgba(0,0,0,0.15); margin: 8px;" />
</p>
<p>
  <img src="https://github.com/user-attachments/assets/76faa5a2-161c-41b3-849a-dc9397f589f2" width="210" style="border-radius: 16px; box-shadow: 0 8px 24px rgba(0,0,0,0.15); margin: 8px;" />
  <img src="https://github.com/user-attachments/assets/ca8c84f9-63cf-486a-af07-102b332fdffb" width="210" style="border-radius: 16px; box-shadow: 0 8px 24px rgba(0,0,0,0.15); margin: 8px;" />
  <img src="https://github.com/user-attachments/assets/dca3367d-1750-439c-b2e0-998cdc8927ea" width="210" style="border-radius: 16px; box-shadow: 0 8px 24px rgba(0,0,0,0.15); margin: 8px;" />
  <img src="https://github.com/user-attachments/assets/91f45f36-75f6-4763-9876-c2b01cfff1ef" width="210" style="border-radius: 16px; box-shadow: 0 8px 24px rgba(0,0,0,0.15); margin: 8px;" />
</p>

</div>
## 🛠️ Admin Panel & Management Features

### 🔐 Security & Authentication
* **Role-Based Access:** Validates Firestore user credentials to permit only accounts with `role == "admin"`.
* **Biometric Lock:** Secure login protected via AndroidX `BiometricPrompt` for fingerprint authentication.
* **Secure Logout:** One-tap session termination that safely clears cached credentials.

### 📊 Dashboard & Business Analytics
* **Live Metric Cards:** Real-time counters tracking Total Revenue (LKR), Total Orders, Pending Orders, Active Users, and Products.
* **Visual Sales Analytics:** Interactive performance charts displaying sales and revenue trends for the Last 7 Days and Last 30 Days.
* **Quick Action Shortcuts:** Direct buttons to quickly add products, create categories, or generate financial reports.

### 🍔 Product Management (Full CRUD)
* **Add & Edit Items:** Comprehensive forms to manage item titles, descriptions, categories, base prices, and images.
* **Cloudinary Upload:** Upload up to 2 high-resolution photos per item with live image previews and delete badges.
* **Master Visibility Toggle:** Instant Active/Inactive switch to control item visibility on the customer app.
* **Product Deletion:** Permanently remove food items from Cloud Firestore.

### ⚙️ Advanced Variant Management
* **Portion & Size Variants:** Custom portions (Small, Medium, Large) with individual pricing and stock counts.
* **Variant Status Toggle:** Dedicated switches on each variant card with dynamic color badges (Green for ACTIVE / Red for DEACTIVE).
* **In-Place Editing:** Tap any variant to edit its name, type, price, stock, or status via intuitive dialogs.
* **Variant Deletion:** Quick-remove buttons on individual variant cards.

### 📂 Category Management (Full CRUD)
* **Category Control:** Complete creation, editing, and deletion capabilities for food categories.
* **Icon Uploads:** Cloudinary image uploads for clean category icons.
* **Category Toggle:** Instant Active/Inactive switches at the category level.

### 📦 Order Lifecycle Management
* **Order Queue Dashboard:** Tabbed filtering system for All, Pending, Preparing, and Completed orders.
* **Real-time Status Controller:** Instant status transitions (`Pending` ➔ `Preparing` ➔ `Completed` ➔ `Cancelled`) that update customer screens instantly.
* **Detailed Inspection:** View customer names, contact numbers, delivery addresses, table numbers, and itemized variant summaries.

### 📷 Table QR Code Generation
* **QR Generator:** High-resolution QR code generation for dining tables powered by the ZXing library.
* **Table Management:** Manage table numbers and seating capacities for seamless dine-in operations.

### 👥 Customer User Management
* **Customer Directory:** View registered customer profiles, emails, and contact phone numbers.
* **Account Controls:** Ability to activate or block customer accounts for security.

### 📄 Financial & Sales PDF Report Export
* **Native PDF Engine:** Built using `PdfDocument` to generate high-resolution branded A4 financial reports without external dependencies.
* **Branded Layout:** Includes restaurant header logos, executive summary cards, and itemized tabular breakdowns.
* **Timeframe Selector:** Export customized statements for Today, Last 7 Days, or Last 30 Days.
* **Secure Sharing:** Uses Android `FileProvider` to view or share reports instantly via WhatsApp, Email, or Print.
