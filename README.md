# WAW LAUNDRY - POS Android Application

Aplikasi Point of Sale (POS) lengkap untuk usaha laundry dengan fitur manajemen pesanan, pelaporan keuangan, dan notifikasi WhatsApp.

## 📱 Fitur Utama

### 🛒 Kasir & Transaksi
- Input pesanan cepat dengan pemilihan layanan
- Pilih pelanggan atau tambah pelanggan baru
- Support promo/diskon
- Pilihan metode pembayaran (Cash, Transfer, QRIS)
- Pilihan tipe pengiriman (Ambil Sendiri / Diantar)
- Input ongkos kirim untuk pesanan diantar
- Catatan/alamat wajib untuk pesanan diantar

### 📋 Manajemen Antrian
- Tab status: Antrian Baru → Dicuci → Selesai → Diambil
- Update status dengan satu klik
- Kirim notifikasi WhatsApp ke pelanggan
- Generate & share struk PDF
- Filter berdasarkan status

### 📊 Laporan Keuangan (Owner)
- Ringkasan pemasukan, pengeluaran, laba bersih
- Filter periode: Hari ini, Minggu ini, Bulan ini, Bulan lalu, Custom
- Perbandingan dengan bulan sebelumnya
- Top 5 pelanggan
- Performa layanan (terlaris)
- Export ke CSV

### ⚙️ Pengaturan
- **Layanan:** Atur nama & harga layanan laundry
- **Pelanggan:** Database pelanggan
- **Promo:** Atur promo persentase/nominal
- **Pengeluaran:** Catat pengeluaran operasional
- **Profil:** Info bisnis (nama, alamat, no HP)
- **Printer:** Setup printer thermal Bluetooth
- **Keamanan:** PIN untuk akses owner

### 🔐 Keamanan
- PIN unlock untuk akses Owner Dashboard
- Enkripsi database dengan SQLCipher
- Auto-lock setelah keluar

## 🛠️ Tech Stack

| Category | Technology |
|----------|------------|
| **Language** | Kotlin |
| **UI Framework** | Jetpack Compose (Material 3) |
| **Database** | Room + SQLCipher (encrypted) |
| **DI** | Hilt/Dagger |
| **Async** | Kotlin Coroutines + Flow |
| **Navigation** | Jetpack Navigation Compose |
| **PDF** | Android PdfDocument API |
| **Printer** | Bluetooth Thermal Printer |
| **Cloud Sync** | Supabase (optional) |

## 📦 APK Size

| Variant | Size |
|---------|------|
| armeabi-v7a | ~8MB (signed) |
| arm64-v8a | ~9MB (signed) |
| Target | < 10MB ✅ |

Optimized untuk:
- ✅ HP spek kentang (RAM 2GB+)
- ✅ Jaringan lambat
- ✅ Offline-first (tanpa internet tetap jalan)

## 🚀 Quick Start

### Prerequisites

- **JDK:** 11 atau higher
- **SDK:** Android 9 (API 28) atau higher
- **Gradle:** 8.13

### Build Development APK

```bash
./gradlew assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb shell am start -n net.rcdevgames.wawlaundry/.MainActivity
```

### Build Production APK

#### Option 1: Quick Build (Tanpa Clean)
```cmd
build-quick.bat
```

#### Option 2: Full Build (Dengan Clean)
```cmd
build-release.bat
```

Output akan ada di folder `release-output/`

## 📜 Scripts Automation

### Windows

| Script | Fungsi |
|--------|---------|
| `build-release.bat` | Clean + build release + copy ke output |
| `build-quick.bat` | Build release saja (lebih cepat) |
| `generate-keystore.bat` | Generate keystore untuk signing |

### Linux/macOS

| Script | Fungsi |
|--------|---------|
| `build-release.sh` | Clean + build release + copy ke output |

## 🔑 Signing Configuration (Production)

### Generate Keystore

```cmd
keytool -genkey -v -keystore app/keystore.jks -keyalg RSA -keysize 2048 -validity 10000 -alias upload -storepass YOUR_PASSWORD -keypass YOUR_PASSWORD -dname "CN=Your Name, OU=IT, O=WAW Laundry, L=Jakarta, ST=DKI, C=ID"
```

### Setup Signing Config

Buat file `app/keystore.properties`:

```properties
STORE_FILE=keystore.jks
STORE_PASSWORD=your_keystore_password
KEY_ALIAS=upload
KEY_PASSWORD=your_key_password
```

⚠️ **IMPORTANT:** File `keystore.properties` ada di `.gitignore` - jangan pernah commit ke git!

### Upload ke Play Store

1. Build release APK dengan production keystore
2. Upload ke Google Play Console
3. Atau gunakan AAB (Android App Bundle) untuk ukuran lebih kecil

## 📁 Project Structure

```
app/src/main/java/net/rcdevgames/wawlaundry/
├── data/
│   ├── local/              # Room Database, Entities, DAOs
│   ├── remote/             # Supabase configuration
│   └── domain/
│       ├── repository/     # Data repositories
│       └── printer/         # Thermal printer service
├── di/                     # Dependency Injection modules
├── ui/
│   ├── cashier/            # Cashier screens
│   ├── owner/              # Owner dashboard & settings
│   ├── queue/              # Order queue management
│   ├── order/              # New order (POS)
│   ├── security/           # PIN lock
│   ├── setup/              # Initial setup screens
│   ├── navigation/         # Navigation graph
│   └── theme/              # Compose theming
├── util/                   # Utilities (PDF, WhatsApp, Formatter)
└── worker/                 # Background sync worker
```

## 🎨 Screens

### Cashier
- **Home:** Quick access to New Order, Queue, Owner Dashboard
- **New Order (POS):** Select services, customer, payment
- **Queue:** Manage order status, send WA, share PDF

### Owner Dashboard
- **Dashboard:** Financial summary & stats
- **Reports:** Income, expenses, profit by period
- **Services:** Manage laundry services & pricing
- **Customers:** Customer database
- **Promos:** Create & manage promotions
- **Expenses:** Track operational expenses
- **Store:** Business profile settings
- **Printer:** Thermal printer setup
- **Data:** Cloud sync & backup

## 🔧 Performance Optimizations

### Database Indexes
- Orders: `(entryDate, orderStatus, customerId, isDeleted, isSynced)`
- Customers: `(name, phone, isDeleted, isSynced)`
- Expenses: `(date, isDeleted, isSynced)`

### Build Optimizations
- **R8/ProGuard:** Enabled (removes unused code)
- **Resource Shrinking:** Enabled
- **ABI Splitting:** ARM only (untuk size lebih kecil)
- **Minification:** Enabled untuk release builds

### Memory Optimization
- **CurrencyFormatter:** Singleton pattern (tidak recreate di setiap recompose)
- **LazyColumn:** Virtualized scrolling
- **Flow-based reactive:** Efficient state management

## 🚨 Troubleshooting

### Build Gagal

```bash
# Clean cache
./gradlew clean

# Invalidate Android Studio caches
File → Invalidate Cashes → Invalidate and Restart
```

### Aplikasi Crash Saat Share PDF

Pastikan `file_paths.xml` sudah dikonfigurasi dengan benar:
```xml
<external-files-path name="receipts" path="receipts/" />
```

### Database Migration Error

Versi database saat ini: 3

Jika ada error migration, hapus data aplikasi:
```bash
adb shell pm clear net.rcdevgames.wawlaundry
```

## 📝 Versioning

Version otomatis increment setiap kali `assembleRelease` dijalankan:

- `VERSION_CODE`: +1 (1, 2, 3, ...)
- `VERSION_NAME`: +1 patch version (1.0.0 → 1.0.1 → 1.0.2)

Disimpan di `app/version.properties`

## 📄 License

```
Copyright (c) 2025 WAW LAUNDRY

Licensed under the Apache License, Version 2.0
