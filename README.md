# SwiftCart

SwiftCart, Spring Boot ile geliştirilmiş bir e-ticaret backend API'sidir. Kullanıcı yönetimi, ürün/kategori kataloğu, sepet, sipariş, ödeme ve adres yönetimi gibi temel e-ticaret akışlarını içerir.

## İçindekiler

- [Teknoloji Yığını](#teknoloji-yığını)
- [Mimari Notlar](#mimari-notlar)
- [API Endpoint'leri](#api-endpointleri)
    - [Auth](#auth--apiv1auth)
    - [User](#user--apiv1user)
    - [User Profile](#user-profile--apiv1profile)
    - [Address](#address--apiv1addresses)
    - [Category](#category--apiv1categories)
    - [Product](#product--apiv1products)
    - [Cart](#cart--apiv1carts)
    - [Order](#order--apiv1orders)
    - [Payment](#payment--apiv1payments)
- [Yetkilendirme Modeli](#yetkilendirme-modeli)
- [Ortak Yanıt Formatı](#ortak-yanıt-formatı)

## Teknoloji Yığını

- **Java / Spring Boot**
- **Spring Security** — JWT tabanlı authentication, `@AuthenticationPrincipal UserPrincipal` ile kullanıcı context'i
- **Spring Data JPA** — repository katmanı, sayfalama (`Pageable`)
- **Spring Mail** — `@Async` e-posta gönderimi (HTML şablonlu bildirimler)
- **Spring Events** — `ApplicationEventPublisher` + `@TransactionalEventListener(phase = AFTER_COMMIT)` ile transaction-sonrası yan etkiler (email gönderimi)

## Mimari Notlar

Proje boyunca benimsenen bazı temel prensipler:

- **Atomic stok yönetimi:** Stok azaltma/artırma işlemleri "oku-değiştir-yaz" yerine tek bir atomic `UPDATE` sorgusu (`decreaseStock` / `increaseStock`) ile yapılır. Bu, eşzamanlı isteklerde oluşabilecek race condition ve overselling riskini ortadan kaldırır.
- **Transaction-sonrası bildirimler:** E-posta gönderimi gibi yan etkiler, ana veritabanı transaction'ı commit olmadan tetiklenmez. `ApplicationEventPublisher` ile event yayınlanır, `@TransactionalEventListener(phase = AFTER_COMMIT)` ile dinlenir — böylece bir email hatası, başarılı bir sipariş/ödeme işlemini rollback etmez.
- **Whitelist edilmiş sıralama alanları:** Sayfalanan listelerde (`sortBy`, `direction`) kullanıcıdan gelen alan adı doğrudan sorguya verilmez; izin verilen alanlar setiyle (`ALLOWED_SORT_FIELDS`) doğrulanır. Bu, geçersiz/istenmeyen entity path'leri üzerinden sıralama yapılmasını engeller.
- **Sahiplik (ownership) kontrolü ID üzerinden yapılır:** `order.getUser().getId().equals(user.getId())` gibi karşılaştırmalar entity referans eşitliğine değil, ID eşitliğine dayanır.
- **Durum geçiş kuralları (state machine):** Sipariş statüsü güncellemeleri (`PENDING → PAID → PROCESSING → SHIPPED → DELIVERED`) bir transition map ile kontrol edilir, geçersiz/geriye dönük geçişler engellenir.

## API Endpoint'leri

Tüm endpoint'ler `/api/v1` öneki altındadır.

### Auth — `/api/v1/auth`

| Method | Path | Açıklama | Yetki |
|---|---|---|---|
| POST | `/api/v1/auth` | Kayıt (signup) | Herkese açık |
| POST | `/api/v1/auth/login` | Giriş | Herkese açık |
| POST | `/api/v1/auth/logout` | Çıkış (access + refresh token header ile) | Kimliği doğrulanmış |
| GET | `/api/v1/auth/verify?token=` | E-posta doğrulama | Herkese açık |
| POST | `/api/v1/auth/refresh` | Access token yenileme | Refresh token header ile |

### User — `/api/v1/user`

| Method | Path | Açıklama | Yetki |
|---|---|---|---|
| GET | `/api/v1/user` | Tüm kullanıcıları sayfalı listele | ADMIN |
| GET | `/api/v1/user/{id}` | ID ile kullanıcı getir | ADMIN veya kendisi |
| GET | `/api/v1/user/me` | Giriş yapan kullanıcının bilgisi | Kimliği doğrulanmış |
| GET | `/api/v1/user/search?email=` | E-posta ile kullanıcı ara | ADMIN |
| PATCH | `/api/v1/user/{id}/status` | Kullanıcı durumunu değiştir (aktif/pasif) | ADMIN |
| PATCH | `/api/v1/user/{id}/role?role=` | Kullanıcı rolünü güncelle | ADMIN |
| DELETE | `/api/v1/user/{id}` | Kullanıcı sil | ADMIN veya kendisi |

### User Profile — `/api/v1/profile`

| Method | Path | Açıklama | Yetki |
|---|---|---|---|
| POST | `/api/v1/profile` | Profil oluştur | Kimliği doğrulanmış |
| GET | `/api/v1/profile/me` | Kendi profilini getir | Kimliği doğrulanmış |
| GET | `/api/v1/profile/{userId}` | Belirli kullanıcının profili | ADMIN |
| PUT | `/api/v1/profile/me` | Profili güncelle | Kimliği doğrulanmış |
| POST | `/api/v1/profile/me/avatar` | Avatar yükle (multipart) | Kimliği doğrulanmış |
| DELETE | `/api/v1/profile/me/avatar` | Avatarı sil | Kimliği doğrulanmış |

### Address — `/api/v1/addresses`

| Method | Path | Açıklama | Yetki |
|---|---|---|---|
| GET | `/api/v1/addresses` | Kendi adreslerini listele | Kimliği doğrulanmış |
| GET | `/api/v1/addresses/{id}` | Adres detayı | Sahibi |
| POST | `/api/v1/addresses` | Yeni adres oluştur | Kimliği doğrulanmış |
| PUT | `/api/v1/addresses/{id}` | Adres güncelle | Sahibi |
| DELETE | `/api/v1/addresses/{id}` | Adres sil | Sahibi |
| PATCH | `/api/v1/addresses/{id}/default` | Varsayılan adres olarak işaretle | Sahibi |

### Category — `/api/v1/categories`

| Method | Path | Açıklama | Yetki |
|---|---|---|---|
| POST | `/api/v1/categories` | Kategori oluştur | ADMIN |
| GET | `/api/v1/categories/tree` | Kategori ağacını getir (hiyerarşik) | Herkese açık |
| GET | `/api/v1/categories` | Tüm kategorileri listele | Herkese açık |
| GET | `/api/v1/categories/{id}` | Kategori detayı | Herkese açık |
| PATCH | `/api/v1/categories/{id}` | Kategori güncelle | ADMIN |
| DELETE | `/api/v1/categories/{id}` | Kategori sil | ADMIN |

### Product — `/api/v1/products`

| Method | Path | Açıklama | Yetki |
|---|---|---|---|
| POST | `/api/v1/products` | Ürün oluştur | SELLER |
| GET | `/api/v1/products` | Ürünleri sayfalı listele (kategoriye göre filtrelenebilir) | Herkese açık |
| GET | `/api/v1/products/{id}` | Ürün detayı | Herkese açık |
| GET | `/api/v1/products/seller/me` | Satıcının kendi ürünleri | SELLER |
| PUT | `/api/v1/products/{id}` | Ürün güncelle | Ürünün sahibi SELLER |
| PATCH | `/api/v1/products/{id}/stock` | Stok güncelle | Ürünün sahibi SELLER |
| PATCH | `/api/v1/products/{id}/status` | Ürün durumunu değiştir (yayında/kaldırıldı) | Ürünün sahibi SELLER |
| DELETE | `/api/v1/products/{id}` | Ürün sil | Ürünün sahibi SELLER |

### Cart — `/api/v1/carts`

| Method | Path | Açıklama | Yetki |
|---|---|---|---|
| GET | `/api/v1/carts/me` | Kendi sepetini getir | Kimliği doğrulanmış |
| POST | `/api/v1/carts` | Sepete ürün ekle | Kimliği doğrulanmış |
| PUT | `/api/v1/carts/cart-item/{cartItemId}` | Sepet ürününü güncelle (miktar vb.) | Sahibi |
| DELETE | `/api/v1/carts/cart-item/{cartItemId}` | Sepetten ürün çıkar | Sahibi |
| DELETE | `/api/v1/carts/clear-cart` | Sepeti tamamen boşalt | Kimliği doğrulanmış |

### Order — `/api/v1/orders`

| Method | Path | Açıklama | Yetki |
|---|---|---|---|
| POST | `/api/v1/orders` | Sepetten sipariş oluştur | Kimliği doğrulanmış |
| GET | `/api/v1/orders/me` | Kendi siparişlerini sayfalı listele | Kimliği doğrulanmış |
| GET | `/api/v1/orders/{orderId}` | Sipariş detayı | Sahibi |
| POST | `/api/v1/orders/{orderId}/cancel` | Siparişi iptal et (yalnızca `PENDING`/`PAID`) | Sahibi |
| GET | `/api/v1/orders/seller` | Satıcının ürünlerini içeren siparişleri sayfalı listele | SELLER |
| PATCH | `/api/v1/orders/{orderId}/status` | Sipariş durumunu güncelle (kurallı geçiş) | Siparişte satıcı olan SELLER veya ADMIN |

**Sipariş oluşturma akışı:** sepet doğrulama → adres doğrulama ve sahiplik kontrolü → atomic stok düşürme → sipariş kaydı → sepeti boşaltma → sipariş onay e-postası (transaction commit sonrası, async).

**Sipariş iptal akışı:** sahiplik kontrolü → durum kontrolü (`PENDING`/`PAID` dışında iptal edilemez) → stokların atomic olarak geri yüklenmesi → iptal e-postası.

**Durum geçiş kuralları:**

```
PENDING → PAID → PROCESSING → SHIPPED → DELIVERED
```

Geriye dönük geçiş (örn. `DELIVERED → PROCESSING`) veya tanımsız bir geçiş `INVALID_STATUS_TRANSITION` hatası döner. İptal işlemi bu endpoint üzerinden değil, yalnızca `/cancel` endpoint'i üzerinden yapılabilir.

### Payment — `/api/v1/payments`

| Method | Path | Açıklama | Yetki |
|---|---|---|---|
| POST | `/api/v1/payments` | Ödeme işlemini başlat | Kimliği doğrulanmış |
| GET | `/api/v1/payments/{id}` | Ödeme detayı | Sahibi |
| GET | `/api/v1/payments/order/{orderId}` | Siparişe ait ödeme bilgisi | Sahibi |
| POST | `/api/v1/payments/order/{orderId}/refund` | İade işlemi başlat | Sahibi (veya yetkili) |

## Yetkilendirme Modeli

Roller: `BUYER` (varsayılan), `SELLER`, `ADMIN`.

- Endpoint seviyesinde `@PreAuthorize("hasRole('...')")` ile rol bazlı kontrol yapılır.
- Kaynak sahipliği (bir siparişin, adresin, ürünün gerçekten o kullanıcıya ait olup olmadığı) servis katmanında, ID karşılaştırmasıyla ayrıca doğrulanır — yalnızca role bakmak yeterli değildir.
- `#id == authentication.principal.id` gibi SpEL ifadeleriyle "kendi kaydına erişim" senaryoları expression tabanlı yetkilendirme ile desteklenir.

## Ortak Yanıt Formatı

Çoğu endpoint `ApiResponse<T>` sarmalayıcısı ile yanıt döner:

```json
{
  "success": true,
  "message": "...",
  "data": { }
}
```

Sayfalı listeler `PaginationResponse<T>` içinde döner:

```json
{
  "content": [ ],
  "page": 0,
  "size": 20,
  "totalElements": 123,
  "totalPages": 7,
  "last": false
}
```