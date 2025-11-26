package com.hsntncy.threebodysimulation

import kotlinx.cinterop.*
import platform.posix.rand
import platform.posix.RAND_MAX
import kotlin.math.sqrt
import box2d.* // Box2D kütüphanesinin import edildiğinden emin olun

@OptIn(ExperimentalForeignApi::class)
actual class ThreeBodyEngine actual constructor() {

    // CValue kullanarak struct'ları saklıyoruz (Box2D v3 id'leri struct'tır)
    private var worldId: CValue<b2WorldId>? = null
    private val bodyIds = mutableListOf<CValue<b2BodyId>>()

    // --- Sabitler ---
    private val P1_X = 0.97000436f
    private val P1_Y = -0.24308753f
    private val V1_X = 0.4662036850f
    private val V1_Y = 0.4323657300f
    private val V3_X = -2.0f * V1_X
    private val V3_Y = -2.0f * V1_Y
    private val G = 1.0f

    private fun randomFloat(min: Float, max: Float): Float {
        return min + (rand().toFloat() / RAND_MAX.toFloat()) * (max - min)
    }

    // ------------------------------------------------------------------------
    // Dünya oluşturma ve senaryo başlatma
    // ------------------------------------------------------------------------
    actual fun initializeScenario(mode: Int) {
        // 1. TEMİZLİK
        destroy()

        // 2. DÜNYA OLUŞTURMA
        memScoped {
            // A) Varsayılan, temiz ayarları al (CValue döner)
            val defaultWorldDef = b2DefaultWorldDef()

            // B) Belleğe yerleştir (Pointer al)
            val worldDefPtr = defaultWorldDef.placeTo(this)

            // C) Ayarları değiştir
            worldDefPtr.pointed.gravity.x = 0.0f
            worldDefPtr.pointed.gravity.y = 0.0f

            // D) Dünyayı oluştur
            worldId = b2CreateWorld(worldDefPtr)
        }

        // 3. SENARYO YÜKLEME
        if (worldId != null && b2World_IsValid(worldId!!)) {
            when (mode) {
                0 -> createFigure8()
                1 -> createChaos()
                2 -> createSolarSystem()
            }
        }
    }

    // ------------------------------------------------------------------------
    // Yardımcı: Güvenli Cisim Oluşturma (FIX BURADA)
    // ------------------------------------------------------------------------
    private fun createCircleBody(
        world: CValue<b2WorldId>,
        x: Float, y: Float,
        vx: Float, vy: Float,
        radius: Float,
        density: Float,
        categoryBits: UInt,
        maskBits: UInt,
        groupIndex: Int
    ): CValue<b2BodyId> {
        return memScoped {
            // -------------------------------------------------------
            // ADIM 1: BODY DEF (Cisim Ayarları)
            // ESKİ HATALI YÖNTEM: val bodyDef = alloc<b2BodyDef>()
            // YENİ GÜVENLİ YÖNTEM: b2DefaultBodyDef() kullanmak zorunludur!
            // -------------------------------------------------------

            val bodyDefValue = b2DefaultBodyDef() // 1. Varsayılanı al
            val bodyDefPtr = bodyDefValue.placeTo(this) // 2. Pointer yap

            // 3. Üzerine yaz (Artık linearDamping vs. gibi alanlar çöp değil, default değerde)
            with(bodyDefPtr.pointed) {
                type = b2_dynamicBody
                position.x = x
                position.y = y
                linearVelocity.x = vx
                linearVelocity.y = vy
                gravityScale = 1f
                enableSleep = false // Uzay simülasyonunda uyumasınlar
                isAwake = true
                isEnabled = true
                // rotation default olarak zaten (0,0) veya identity gelir, dokunmaya gerek yok
            }

            // Cismi oluştur
            val bodyId = b2CreateBody(world, bodyDefPtr)

            // Eğer cisim oluşmazsa (nullBodyId dönerse) işlemi durdur
            if (!b2Body_IsValid(bodyId)) {
                println("HATA: Body oluşturulamadı!")
                return@memScoped bodyId
            }

            bodyIds.add(bodyId)

            // -------------------------------------------------------
            // ADIM 2: SHAPE DEF (Şekil Ayarları)
            // -------------------------------------------------------
            val shapeDefValue = b2DefaultShapeDef()
            val shapeDefPtr = shapeDefValue.placeTo(this)

            with(shapeDefPtr.pointed) {
                this.density = density
             
                this.filter.categoryBits = categoryBits.toULong()
                this.filter.maskBits = maskBits.toULong()
                this.filter.groupIndex = groupIndex
                this.isSensor = false
            }

            // -------------------------------------------------------
            // ADIM 3: GEOMETRİ (Circle)
            // -------------------------------------------------------
            // b2Circle basit bir struct olduğu için alloc yeterlidir ama
            // manuel olarak doldurmak gerekir.
            val circlePtr = alloc<b2Circle>()
            circlePtr.radius = radius
            circlePtr.center.x = 0f
            circlePtr.center.y = 0f

            // Şekli cisme bağla
            b2CreateCircleShape(bodyId, shapeDefPtr, circlePtr.ptr)

            bodyId // Return
        }
    }

    // ------------------------------------------------------------------------
    // Senaryo Fonksiyonları (Artık optimize edilmiş createCircleBody kullanıyor)
    // ------------------------------------------------------------------------
    private fun createFigure8() {
        val w = worldId ?: return
        val r = 0.1f
        val density = 1.0f / (3.14159f * r * r)
        val cat = 0x0001u
        val mask = 0xFFFFu

        createCircleBody(w, P1_X, P1_Y, V3_X / 2f, V3_Y / 2f, r, density, cat, mask, 0)
        createCircleBody(w, -P1_X, -P1_Y, V3_X / 2f, V3_Y / 2f, r, density, cat, mask, 0)
        createCircleBody(w, 0f, 0f, -V3_X, -V3_Y, r, density, cat, mask, 0)
    }

    private fun createChaos() {
        val w = worldId ?: return
        val r = 0.1f
        val density = 1.0f / (3.14159f * r * r)

        repeat(3) {
            createCircleBody(
                w,
                randomFloat(-0.5f, 0.5f), randomFloat(-0.5f, 0.5f),
                randomFloat(-0.2f, 0.2f), randomFloat(-0.2f, 0.2f),
                r, density, 0x0001u, 0xFFFFu, 0
            )
        }
    }

    private fun createSolarSystem() {
        val w = worldId ?: return

        // Güneş
        val sunId = createCircleBody(w, 0f, 0f, 0f, 0f, 0.2f, 50.0f, 0x0001u, 0xFFFFu, 0)
        val sunMass = b2Body_GetMass(sunId)

        // Dünya
        val earthDist = 1.5f
        val earthVel = sqrt((G * sunMass) / earthDist)
        val earthId = createCircleBody(w, earthDist, 0f, 0f, earthVel, 0.08f, 2.0f, 0x0001u, 0xFFFFu, 0)
        val earthMass = b2Body_GetMass(earthId)

        // Ay
        val moonDist = 0.15f
        val moonOrbitalVel = sqrt((G * earthMass) / moonDist)
        createCircleBody(w, earthDist + moonDist, 0f, 0f, earthVel + moonOrbitalVel, 0.03f, 0.5f, 0x0001u, 0xFFFFu, 0)
    }

    // ------------------------------------------------------------------------
    // Fizik Adımı (N-Body Gravity)
    // ------------------------------------------------------------------------
    actual fun step() {
        val w = worldId ?: return
        if (!b2World_IsValid(w) || bodyIds.size < 2) return

        val minDistSq = 0.0001f // Çok yaklaşırsa patlamasın
        val count = bodyIds.size

        memScoped {
            // Tüm cisimlerin pozisyonlarını ve kütlelerini al
            // Performans için struct'ları burada tutuyoruz
            val positions = Array(count) { i -> b2Body_GetPosition(bodyIds[i]) }
            val masses = FloatArray(count) { i -> b2Body_GetMass(bodyIds[i]) }

            for (i in 0 until count) {
                // Statik cisimlere kuvvet uygulamaya gerek yok (Güneş gibi kütlesi sonsuz olanlar hariç,
                // ama burada her şey dynamic varsayılıyor)

                var fxTotal = 0f
                var fyTotal = 0f

                val posA = positions[i]
                var ax = 0f
                var ay = 0f
                posA.useContents { ax = x; ay = y }

                for (j in 0 until count) {
                    if (i == j) continue

                    val posB = positions[j]
                    var bx = 0f
                    var by = 0f
                    posB.useContents { bx = x; by = y }

                    val dx = bx - ax
                    val dy = by - ay
                    val distSq = dx * dx + dy * dy

                    if (distSq > minDistSq) {
                        val dist = sqrt(distSq)
                        val forceMag = (G * masses[i] * masses[j]) / distSq
                        fxTotal += (dx / dist) * forceMag
                        fyTotal += (dy / dist) * forceMag
                    }
                }

                // Toplam kuvveti uygula
                if (fxTotal != 0f || fyTotal != 0f) {
                    val forceVec = alloc<b2Vec2>()
                    forceVec.x = fxTotal
                    forceVec.y = fyTotal
                    // Wake = true (uyuyan cisimleri uyandır)
                    b2Body_ApplyForceToCenter(bodyIds[i], forceVec.readValue(), true)
                }
            }

            // Dünyayı ilerlet
            // TimeStep: 1/60, SubSteps: 4 (Box2D v3'te tek subStep parametresi var)
            b2World_Step(w, 1.0f / 60.0f, 4)
        }
    }

    // ------------------------------------------------------------------------
    // Diğer Fonksiyonlar
    // ------------------------------------------------------------------------
    actual fun setBodyPosition(index: Int, x: Float, y: Float) {
        val w = worldId ?: return
        if (!b2World_IsValid(w) || index !in bodyIds.indices) return

        val bId = bodyIds[index]
        if (b2Body_IsValid(bId)) {
            memScoped {
                val newPos = alloc<b2Vec2>()
                newPos.x = x
                newPos.y = y
                val currentRot = b2Body_GetRotation(bId)

                // Hızı sıfırla
                val zeroVel = alloc<b2Vec2>()
                zeroVel.x = 0f
                zeroVel.y = 0f

                b2Body_SetTransform(bId, newPos.readValue(), currentRot)
                b2Body_SetLinearVelocity(bId, zeroVel.readValue())
            }
        }
    }

    actual fun getCurrentState(): ThreeBodyState {
        val w = worldId
        // Eğer dünya yoksa veya yeterli cisim yoksa boş dön
        if (w == null || !b2World_IsValid(w) || bodyIds.size < 3) {
            return ThreeBodyState(Point2D(0f, 0f), Point2D(0f, 0f), Point2D(0f, 0f))
        }

        // Helper: Position al
        fun getP(index: Int): Point2D {
            if (index >= bodyIds.size) return Point2D(0f, 0f)

            val pos = b2Body_GetPosition(bodyIds[index])
            var px = 0f
            var py = 0f
            pos.useContents { px = x; py = y }
            return Point2D(px, py)
        }

        return ThreeBodyState(getP(0), getP(1), getP(2))
    }

    actual fun destroy() {
        val currentWorld = worldId
        if (currentWorld != null && b2World_IsValid(currentWorld)) {
            b2DestroyWorld(currentWorld)
        }
        worldId = null
        bodyIds.clear()
    }
}