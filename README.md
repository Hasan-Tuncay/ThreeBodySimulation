Harika bir README dosyası, GitHub reponu ziyaret eden bir işe alım uzmanı (recruiter) veya teknik yönetici için senin **vitrinindir**. Özellikle böyle teknik (C++ ve KMP entegrasyonu gibi) bir projede, sadece kodu değil, **mimar şapkanı** da göstermeliyiz.

GitHub README dosyaları **Markdown (.md)** formatında yazılır. Ancak Markdown içinde HTML etiketleri kullanarak ortalama, renklendirme ve düzenleme yapabiliriz.

Aşağıda, LinkedIn ve GitHub için profesyonel, **Mermaid diyagramları ile desteklenmiş**, proje yapısını net anlatan "Copy-Paste" yapabileceğin hazır bir şablon hazırladım.

-----

### README Tasarımı ve Kullanım Talimatı

1.  Aşağıdaki kod bloğunu kopyala.
2.  Projenin ana dizinindeki `README.md` dosyasının içine yapıştır.
3.  GitHub, **Mermaid** diyagramlarını otomatik olarak çizer, ekstra bir şey yapmana gerek yok.

-----

````markdown
# 🪐 ThreeBodySimulation: KMP & C++ Physics Engine Integration

<div align="center">

![Kotlin](https://img.shields.io/badge/Kotlin-2.0-purple?style=for-the-badge&logo=kotlin)
![Android](https://img.shields.io/badge/Android-JNI-green?style=for-the-badge&logo=android)
![iOS](https://img.shields.io/badge/iOS-CInterop-black?style=for-the-badge&logo=apple)
![C++](https://img.shields.io/badge/C++-Box2D-blue?style=for-the-badge&logo=c%2B%2B)

**"Write Once, Simulate Everywhere"**
<br>
<i>Box2D Fizik Motorunun, Kotlin Multiplatform (KMP) üzerinde Android (JNI) ve iOS (Native CInterop) ile hibrit entegrasyonu.</i>

[🎥 Demo Videosu İzle](#) | [📄 Teknik Makaleyi Oku](#) | [🐛 Hata Bildir](#)

</div>

---

## 📖 Proje Hakkında

Bu proje, mobil geliştirmenin en zorlu alanlarından biri olan **Sistem Programlama ve UI Katmanı Ayrımını** ele alır. Standart bir veri uygulaması değildir; C++ tabanlı yüksek performanslı bir fizik motorunun (Box2D), modern Kotlin Multiplatform arayüzleri arkasında nasıl soyutlanacağını gösteren bir **Proof of Concept (PoC)** çalışmasıdır.

**Temel Hedef:**
`commonMain` içinde tanımlanan tek bir `PhysicsEngine` arayüzü ile, işletim sistemine özel (Native) C++ kütüphanelerini performans kaybı olmadan çalıştırmak.

---

## 🏗️ Mimari ve Veri Akışı (Architecture Flow)

Bu proje, her iki platformun doğasına uygun olarak **iki farklı bellek yönetimi stratejisi** kullanır. Aşağıdaki diyagram, Kotlin kodunun C++ motoruna nasıl eriştiğini özetler:

```mermaid
graph TD
    subgraph Shared [KMP Common Logic]
        K[Kotlin Common Interface]
        style K fill:#7F52FF,stroke:#333,stroke-width:2px,color:white
    end

    subgraph Android_World [Android Ecosystem]
        JVM[Android JVM]
        JNI[JNI Bridge (native-lib.cpp)]
        CPP_SRC[C++ Source Code]
        CMake[CMake Build System]
        
        K -->|Calls| JVM
        JVM -->|Marshalling| JNI
        JNI -->|Direct Call| CPP_SRC
        CMake -.->|Compiles| CPP_SRC
    end

    subgraph iOS_World [iOS Ecosystem]
        KN[Kotlin/Native Runtime]
        CInterop[CInterop Stub]
        StaticLib[LibBox2D.a (Static Library)]
        
        K -->|Compiles to| KN
        KN -->|Direct Memory Access| CInterop
        CInterop -->|Links| StaticLib
    end

    style Android_World fill:#e8f5e9,stroke:#4caf50,stroke-width:2px
    style iOS_World fill:#f3e5f5,stroke:#9c27b0,stroke-width:2px
````

### Teknik Farklılıklar

| Özellik | Android (JVM) | iOS (Native) |
| :--- | :--- | :--- |
| **Entegrasyon** | **JNI (Java Native Interface)** | **CInterop & cinterop tool** |
| **Kaynak Tipi** | Ham C++ Kaynak Kodları (.cpp) | Derlenmiş Statik Kütüphane (.a) |
| **Derleme** | CMake ile Runtime'da derlenir | Link Time'da (Xcode) bağlanır |
| **Bellek** | Manuel bellek yönetimi & Garbage Collector | `memScoped` ve Arena Allocators |

-----

## 📂 Proje Yapısı: Neyi Nerede Bulurum?

Proje, KMP standartlarına uygun olarak modüler bir yapıda tasarlanmıştır.

### 1\. Ortak Mantık (`commonMain`)

📍 `composeApp/src/commonMain/kotlin/com/hsntncy/...`

  * **`ThreeBodyEngine.kt` (Expect Class):** Android ve iOS'e "Bu fonksiyonları (step, initialize) uygulamak zorundasın" dediğimiz kontrat.
  * **`SimulationModels.kt`:** Fizik dünyasındaki objelerin (Gezegen, Yıldız) Kotlin veri sınıfları.

### 2\. Android Uygulaması (`androidMain`)

📍 `composeApp/src/androidMain/`

  * **`cpp/box2d_source`:** Box2D motorunun ham C++ kodları.
  * **`cpp/native-lib.cpp`:** **Kritik Dosya.** Kotlin'den gelen çağrıları yakalayıp C++'a çeviren JNI köprüsü.
  * **`cpp/CMakeLists.txt`:** C++ kodlarının nasıl derleneceğini Android Studio'ya anlatan reçete.

### 3\. iOS ve CInterop (`nativeInterop` & `iosApp`)

📍 `composeApp/src/nativeInterop/cinterop/`

  * **`box2d.def`:** **Kritik Dosya.** Kotlin/Native derleyicisine C başlık dosyalarını (.h) ve `.a` kütüphanesini tanıtan harita.
  * **`include/box2d`:** C++ Header dosyaları (API Tanımları).
  * **`lib/`:** `libbox2d.a` ve `libshared.a` (Derlenmiş statik kütüphaneler).

📍 `iosApp/`

  * **`iOSApp.swift`:** iOS tarafındaki giriş noktası (SwiftUI).

-----

## 🚀 Kurulum ve Çalıştırma

### Gereksinimler

  * Android Studio Ladybug veya daha yeni sürüm.
  * Xcode 15+ (iOS derlemesi için).
  * JDK 17+.
  * Kotlin Multiplatform Plugin.

### Adım Adım

1.  **Repoyu Klonlayın:**
    ```bash
    git clone [https://github.com/kullaniciadi/ThreeBodySimulation.git](https://github.com/kullaniciadi/ThreeBodySimulation.git)
    ```
2.  **Android İçin:**
      * Android Studio'da projeyi açın ve Gradle senkronizasyonunu bekleyin.
      * `CMake` otomatik olarak C++ kodlarını derleyecektir.
      * `composeApp` konfigürasyonunu seçip **Run**'a basın.
3.  **iOS İçin:**
      * Terminalden proje dizininde `./gradlew podInstall` (veya ilgili build komutu) çalıştırın.
      * `iosApp/iosApp.xcodeproj` dosyasını Xcode ile açın.
      * Simülatör seçip **Run**'a basın.

-----

## 🛠️ Kullanılan Teknolojiler

  * **Dil:** Kotlin, C++, Swift
  * **UI Framework:** Jetpack Compose (Multiplatform), SwiftUI
  * **Fizik Motoru:** Box2D (C++ Physics Engine)
  * **Build Systems:** Gradle (Kotlin DSL), CMake (C++ Build), Xcode Build
  * **Architecture:** Clean Architecture, MVI (Model-View-Intent)

-----

\<div align="center"\>

**Geliştirici**
<br>
[Senin Adın]
<br>
[](https://www.google.com/search?q=LINKEDIN_PROFIL_LINKIN)

\</div\>

```

### Bu README Neden İyi? (İşe Alımcı Gözüyle)

1.  **Diyagram Konuşur:** `mermaid` bloğu sayesinde, karmaşık C++ bağlantısını saniyeler içinde anlarlar. Kod okumalarına gerek kalmaz.
2.  **Emoji ve Rozetler (Badges):** Modern ve yaşayan bir proje hissi verir.
3.  **"Neyi Nerede Bulurum?" Bölümü:** Bu çok kritiktir. Birisi kodunu incelemek isterse, 100 dosya arasında kaybolmaz. Direkt `native-lib.cpp` veya `.def` dosyasına odaklanabilir.
4.  **"Architecture Flow":** Bu kısım senin sadece kod yazan biri (coder) değil, sistem tasarlayan biri (engineer) olduğunu kanıtlar.

**Son Tavsiye:** Projenin Android ve iOS simülatöründe yan yana çalışırkenki ekran kaydını alıp, en başa bir GIF veya video linki olarak eklersen LinkedIn'de etkileşimin 3 kat artar.
```
