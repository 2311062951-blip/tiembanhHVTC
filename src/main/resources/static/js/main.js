// HVTC BAKERY - Main Frontend JavaScript
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
    // 3. Global Image Fallback Handler
    const DEFAULT_FALLBACK_IMG = 'https://images.unsplash.com/photo-1578985545062-69928b1d9587?w=600&auto=format&fit=crop';
    document.querySelectorAll('img').forEach(function (img) {
        img.addEventListener('error', function () {
            if (this.src !== DEFAULT_FALLBACK_IMG) {
                this.src = DEFAULT_FALLBACK_IMG;
            }
        });
    });
});
