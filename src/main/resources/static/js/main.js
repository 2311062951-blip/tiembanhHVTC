// HVTC BAKERY - Main Frontend JavaScript

// ============================================================
// Product Category Fallback Images (Unsplash - liên quan đến sản phẩm)
// ============================================================
const CATEGORY_FALLBACK_IMAGES = {
    // Bánh sinh nhật
    'banh-sinh-nhat':   'https://images.unsplash.com/photo-1464349095431-e9a21285b5f3?w=600&auto=format&fit=crop',
    // Bánh mì / Bánh mì ngàn lớp / Croissant
    'banh-mi':          'https://images.unsplash.com/photo-1509440159596-0249088772ff?w=600&auto=format&fit=crop',
    'croissant':        'https://images.unsplash.com/photo-1555507036-ab1f4038808a?w=600&auto=format&fit=crop',
    // Bánh ngọt / Mousse / Cheesecake
    'banh-ngot':        'https://images.unsplash.com/photo-1565958011703-44f9829ba187?w=600&auto=format&fit=crop',
    'mousse':           'https://images.unsplash.com/photo-1488477181946-6428a0291777?w=600&auto=format&fit=crop',
    'cheesecake':       'https://images.unsplash.com/photo-1533134242443-d4fd215305ad?w=600&auto=format&fit=crop',
    // Socola / Kẹo
    'socola':           'https://images.unsplash.com/photo-1548907040-4baa42d10919?w=600&auto=format&fit=crop',
    'keo':              'https://images.unsplash.com/photo-1548907040-4baa42d10919?w=600&auto=format&fit=crop',
    // Đồ uống / Sinh tố
    'do-uong':          'https://images.unsplash.com/photo-1546173159-315724a31696?w=600&auto=format&fit=crop',
    'sinh-to':          'https://images.unsplash.com/photo-1497534446932-c925b458314e?w=600&auto=format&fit=crop',
    'tra-sua':          'https://images.unsplash.com/photo-1558857563-b371033873b8?w=600&auto=format&fit=crop',
    // Bánh cupcake / Muffin
    'cupcake':          'https://images.unsplash.com/photo-1486427944299-d1955d23e34d?w=600&auto=format&fit=crop',
    'muffin':           'https://images.unsplash.com/photo-1486427944299-d1955d23e34d?w=600&auto=format&fit=crop',
    // Macaron / Bánh Pháp
    'macaron':          'https://images.unsplash.com/photo-1558326567-98ae2405596b?w=600&auto=format&fit=crop',
    // Bánh Trung Thu
    'banh-trung-thu':   'https://images.unsplash.com/photo-1547592180-85f173990554?w=600&auto=format&fit=crop',
    // Bánh quy / Cookie
    'banh-quy':         'https://images.unsplash.com/photo-1499636136210-6f4ee915583e?w=600&auto=format&fit=crop',
    'cookie':           'https://images.unsplash.com/photo-1499636136210-6f4ee915583e?w=600&auto=format&fit=crop',
};

// Default fallback nếu không khớp danh mục nào
const DEFAULT_FALLBACK_IMG = 'https://images.unsplash.com/photo-1565958011703-44f9829ba187?w=600&auto=format&fit=crop';

/**
 * Lấy ảnh fallback phù hợp theo category slug.
 * Gọi trong onerror của <img>: onerror="handleImgError(this)"
 */
function handleImgError(imgEl) {
    const cat = imgEl.getAttribute('data-category') || '';
    // Tìm chính xác slug
    let fallback = CATEGORY_FALLBACK_IMAGES[cat];
    // Nếu không tìm thấy, tìm slug con trỏ gần đúng nhất
    if (!fallback) {
        const keys = Object.keys(CATEGORY_FALLBACK_IMAGES);
        const match = keys.find(k => cat.includes(k) || k.includes(cat));
        fallback = match ? CATEGORY_FALLBACK_IMAGES[match] : DEFAULT_FALLBACK_IMG;
    }
    // Tránh loop vô hạn: chỉ thay một lần
    imgEl.onerror = null;
    imgEl.src = fallback;
}

// ============================================================
document.addEventListener('DOMContentLoaded', function () {
    // 1. Sticky Header Effect
    const header = document.querySelector('.hvtc-header');
    if (header) {
        window.addEventListener('scroll', function () {
            if (window.scrollY > 40) {
                header.classList.add('is-sticky');
            } else {
                header.classList.remove('is-sticky');
            }
        });
    }

    // 2. Auto-dismiss alerts after 4 seconds
    const alerts = document.querySelectorAll('.alert-dismissible');
    alerts.forEach(function (alert) {
        setTimeout(function () {
            const bsAlert = new bootstrap.Alert(alert);
            bsAlert.close();
        }, 4000);
    });
});
