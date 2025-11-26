cmake_minimum_required(VERSION 3.22.1)




project(box2d_bridge)

# -------------------------------------------------------------
# 1. YOL TANIMLAMALARI (Dışarıya bağımlılık bitti!)
# -------------------------------------------------------------

# CMakeLists.txt dosyasının olduğu klasör: ${CMAKE_CURRENT_SOURCE_DIR}

# Header dosyalarının yolu (nativeInterop içindeki include klasörüne gidiyoruz)
# ../../../ diyerek: cpp -> androidMain -> src -> shared köküne çıkıp oradan iniyoruz.
 set(BOX2D_INCLUDE_DIR "${CMAKE_CURRENT_SOURCE_DIR}/../../nativeInterop/cinterop/include")
# Kaynak kodların yolu (Hemen yanımızdaki box2d_source klasörü)
set(BOX2D_SOURCE_DIR "${CMAKE_CURRENT_SOURCE_DIR}/box2d_source")

# -------------------------------------------------------------
# 2. KAYNAK KODLARI TOPLA
# -------------------------------------------------------------
# Artık masaüstünden değil, proje içindeki klasörden topluyoruz.
file(GLOB_RECURSE BOX2D_SOURCES
        "${BOX2D_SOURCE_DIR}/*.c"
        "${BOX2D_SOURCE_DIR}/*.cpp"
)

# -------------------------------------------------------------
# 3. KÜTÜPHANEYİ OLUŞTUR
# -------------------------------------------------------------
add_library(
        box2d_bridge
        SHARED
        native-lib.cpp      # Senin yazdığın JNI köprüsü
        ${BOX2D_SOURCES}    # Box2D'nin kendi kodları
)

# -------------------------------------------------------------
# 4. HEADER DOSYALARINI GÖSTER
# -------------------------------------------------------------
include_directories(
        ${BOX2D_INCLUDE_DIR}
)

# -------------------------------------------------------------
# 5. LOG KÜTÜPHANESİNİ BUL VE BAĞLA
# -------------------------------------------------------------
find_library(log-lib log)

target_link_libraries(
        box2d_bridge
        ${log-lib}
)



## Proje adını tanımla (Gradle uyarısını susturmak için)
#project(box2d_bridge)
#
## 1. Box2D Kaynak Kodlarını Bul (DÜZELTME BURADA)
## Box2D v3 ".c" dosyaları kullanır. Hem .c hem .cpp aratıyoruz.
#file(GLOB_RECURSE BOX2D_SOURCES
#        "/Users/hasantuncay/Desktop/box2d-main/src/*.c"
#        "/Users/hasantuncay/Desktop/box2d-main/src/*.cpp"
#)
#
## 2. Kütüphaneni Oluştur
#add_library(
#        box2d_bridge
#        SHARED
#        native-lib.cpp   # Senin yazdığın köprü
#        ${BOX2D_SOURCES} # Bulunan tüm Box2D kodları
#)
#
## 3. Header Dosyalarını Göster
#include_directories(
#        /Users/hasantuncay/Desktop/box2d-main/include
#)
#
## 4. Log Kütüphanesini Bul
#find_library(
#        log-lib
#        log
#)
#
## 5. Bağla
#target_link_libraries(
#        box2d_bridge
#        ${log-lib}
#)