#include <jni.h>
#include <cmath>
#include <vector>
#include <cstdlib>
#include <mutex>
#include <box2d/box2d.h>

// --- GLOBAL DEĞİŞKENLER ---
b2WorldId worldId = b2_nullWorldId;
std::vector<b2BodyId> bodies;
std::mutex simulationLock;

// --- FIGURE-8 SABİTLERİ (Chenciner & Montgomery) ---
const float P1_X = 0.97000436f;
const float P1_Y = -0.24308753f;
const float V1_X = 0.4662036850f;
const float V1_Y = 0.4323657300f;
const float V3_X = -2.0f * V1_X;
const float V3_Y = -2.0f * V1_Y;

const float G = 1.0f;

float randomFloat(float min, float max) {
    return min + static_cast <float> (rand()) /( static_cast <float> (RAND_MAX/(max-min)));
}

void applyGravity() {
    int count = bodies.size();
    if (count < 2) return;

    for (int i = 0; i < count; ++i) {
        for (int j = i + 1; j < count; ++j) {
            b2BodyId bodyA = bodies[i];
            b2BodyId bodyB = bodies[j];

            if (!b2Body_IsValid(bodyA) || !b2Body_IsValid(bodyB)) continue;

            b2Vec2 posA = b2Body_GetPosition(bodyA);
            b2Vec2 posB = b2Body_GetPosition(bodyB);

            float massA = b2Body_GetMass(bodyA);
            float massB = b2Body_GetMass(bodyB);

            b2Vec2 distVec = b2Sub(posB, posA);
            float distanceSq = b2LengthSquared(distVec);
            float distance = sqrtf(distanceSq);

            // Güneş sistemi için softening biraz daha esnek olmalı
            if (distance < 0.15f) distance = 0.15f;

            float forceMagnitude = (G * massA * massB) / distanceSq;
            b2Vec2 forceVec = b2MulSV(forceMagnitude / distance, distVec);

            b2Body_ApplyForceToCenter(bodyA, forceVec, true);
            b2Body_ApplyForceToCenter(bodyB, b2Neg(forceVec), true);
        }
    }
}

extern "C" JNIEXPORT void JNICALL
Java_com_hsntncy_gdgeskisehir_ThreeBodyEngine_nativeInitScenario(JNIEnv* env, jobject, jint mode) {
std::lock_guard<std::mutex> lock(simulationLock);

if (b2World_IsValid(worldId)) {
b2DestroyWorld(worldId);
worldId = b2_nullWorldId;
}
bodies.clear();

b2WorldDef worldDef = b2DefaultWorldDef();
worldDef.gravity = {0.0f, 0.0f};
worldId = b2CreateWorld(&worldDef);

// --- KRİTİK DÜZELTME: KÜTLE AYARI ---
// Yarıçap 0.1 iken Alan = 0.0314
// Kütlenin 1.0 olması için Density = 1.0 / 0.0314 = 31.83
float radius = 0.1f;
float magicDensity = 1.0f / (3.14159f * radius * radius);

b2Circle circle = {{0.0f, 0.0f}, radius};

if (mode == 0) {
// --- MOD 0: FIGURE-8 (STABİL) ---
// Kütleler kesinlikle 1.0 olmalı, yoksa uçar giderler.
b2ShapeDef shapeDef = b2DefaultShapeDef();
shapeDef.density = magicDensity; // <-- İŞTE ÇÖZÜM BU


// Cisim 1
b2BodyDef def1 = b2DefaultBodyDef(); def1.type = b2_dynamicBody;
def1.position = {P1_X, P1_Y}; def1.linearVelocity = {V3_X / 2.0f, V3_Y / 2.0f};
bodies.push_back(b2CreateBody(worldId, &def1));

// Cisim 2
b2BodyDef def2 = b2DefaultBodyDef(); def2.type = b2_dynamicBody;
def2.position = {-P1_X, -P1_Y}; def2.linearVelocity = {V3_X / 2.0f, V3_Y / 2.0f};
bodies.push_back(b2CreateBody(worldId, &def2));

// Cisim 3
b2BodyDef def3 = b2DefaultBodyDef(); def3.type = b2_dynamicBody;
def3.position = {0.0f, 0.0f}; def3.linearVelocity = {-V3_X, -V3_Y};
bodies.push_back(b2CreateBody(worldId, &def3));

for(b2BodyId b : bodies) b2CreateCircleShape(b, &shapeDef, &circle);
}
else if (mode == 1) {
// --- MOD 1: KAOS ---
b2ShapeDef shapeDef = b2DefaultShapeDef();
shapeDef.density = magicDensity; // Yine 1.0 kütle kullanalım

for(int i=0; i<3; i++) {
b2BodyDef def = b2DefaultBodyDef();
def.type = b2_dynamicBody;
// Daha merkeze yakın başlat
def.position = {randomFloat(-0.5f, 0.5f), randomFloat(-0.5f, 0.5f)};
// Hızları düşürdük ki hemen kaçmasınlar
def.linearVelocity = {randomFloat(-0.2f, 0.2f), randomFloat(-0.2f, 0.2f)};
b2BodyId b = b2CreateBody(worldId, &def);
bodies.push_back(b);
b2CreateCircleShape(b, &shapeDef, &circle);
}
}
else if (mode == 2) {
// --- MOD 2: GÜNEŞ SİSTEMİ ---
// Mesafeleri azalttık ki ekrana sığsın

// GÜNEŞ (Ağır)
b2BodyDef sunDef = b2DefaultBodyDef(); sunDef.type = b2_dynamicBody;
sunDef.position = {0.0f, 0.0f};
b2BodyId sun = b2CreateBody(worldId, &sunDef);

b2ShapeDef sunShape = b2DefaultShapeDef();
sunShape.density = 50.0f; // Çok yoğun
b2Circle sunCircle = {{0.0f, 0.0f}, 0.2f}; // Biraz daha küçük
b2CreateCircleShape(sun, &sunShape, &sunCircle);
bodies.push_back(sun);

float sunMass = b2Body_GetMass(sun);

// DÜNYA (Daha yakın)
float earthDist = 1.5f; // Mesafeyi kısalttık (2.5 -> 1.5)
float earthVel = sqrtf((G * sunMass) / earthDist);

b2BodyDef earthDef = b2DefaultBodyDef(); earthDef.type = b2_dynamicBody;
earthDef.position = {earthDist, 0.0f};
earthDef.linearVelocity = {0.0f, earthVel};
b2BodyId earth = b2CreateBody(worldId, &earthDef);

b2ShapeDef earthShape = b2DefaultShapeDef();
earthShape.density = 2.0f;
b2Circle earthCircle = {{0.0f, 0.0f}, 0.08f};
b2CreateCircleShape(earth, &earthShape, &earthCircle);
bodies.push_back(earth);

// AY
float earthMass = b2Body_GetMass(earth);
float moonDist = 0.15f; // Dünyaya daha yakın
float moonOrbitalVel = sqrtf((G * earthMass) / moonDist);

b2BodyDef moonDef = b2DefaultBodyDef(); moonDef.type = b2_dynamicBody;
moonDef.position = {earthDist + moonDist, 0.0f};
moonDef.linearVelocity = {0.0f, earthVel + moonOrbitalVel};
b2BodyId moon = b2CreateBody(worldId, &moonDef);

b2ShapeDef moonShape = b2DefaultShapeDef();
moonShape.density = 0.5f;
b2Circle moonCircle = {{0.0f, 0.0f}, 0.03f};
b2CreateCircleShape(moon, &moonShape, &moonCircle);
bodies.push_back(moon);
}
}

// ... Diğer fonksiyonlar (step, getState, destroy) AYNI KALSIN ...
// Onları tekrar yazmadım çünkü zaten doğrulardı.
extern "C" JNIEXPORT void JNICALL
Java_com_hsntncy_gdgeskisehir_ThreeBodyEngine_nativeStep(JNIEnv* env, jobject) {
std::lock_guard<std::mutex> lock(simulationLock);
if (!b2World_IsValid(worldId) || bodies.size() < 3) return;
applyGravity();
b2World_Step(worldId, 1.0f / 60.0f, 4);
}

extern "C" JNIEXPORT jfloatArray JNICALL
Java_com_hsntncy_gdgeskisehir_ThreeBodyEngine_nativeGetState(JNIEnv* env, jobject) {
    std::lock_guard<std::mutex> lock(simulationLock);
    jfloatArray result = env->NewFloatArray(6);
    float temp[6] = {0};

    if (b2World_IsValid(worldId) && bodies.size() >= 3) {
        for (int i = 0; i < 3; i++) {
            if (b2Body_IsValid(bodies[i])) {
                b2Vec2 pos = b2Body_GetPosition(bodies[i]);
                temp[i * 2] = pos.x;
                temp[i * 2 + 1] = pos.y;
            }
        }
    }
    env->SetFloatArrayRegion(result, 0, 6, temp);
    return result;
}

extern "C" JNIEXPORT void JNICALL
Java_com_hsntncy_gdgeskisehir_ThreeBodyEngine_nativeDestroy(JNIEnv* env, jobject) {
std::lock_guard<std::mutex> lock(simulationLock);
if (b2World_IsValid(worldId)) {
b2DestroyWorld(worldId);
worldId = b2_nullWorldId;
}
bodies.clear();
}
extern "C" JNIEXPORT void JNICALL
Java_com_hsntncy_gdgeskisehir_ThreeBodyEngine_nativeSetBodyPosition(JNIEnv* env, jobject, jint index, jfloat x, jfloat y) {
// Simülasyon çalışırken veri değiştireceğimiz için kilit şart
std::lock_guard<std::mutex> lock(simulationLock);

if (!b2World_IsValid(worldId) || index < 0 || index >= bodies.size()) return;

b2BodyId bodyId = bodies[index];
if (b2Body_IsValid(bodyId)) {
// Cismi yeni konuma ışınla (Açıyı 0 kabul ediyoruz)
b2Body_SetTransform(bodyId, {x, y}, b2Body_GetRotation(bodyId));

// İstersen sürüklerken hızını sıfırlayabilirsin (daha kontrollü olur)
// b2Body_SetLinearVelocity(bodyId, {0.0f, 0.0f});
}
}