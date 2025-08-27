const userId = 1; // Fixed user ID for demo
let products = [];
let cart = null;

$(document).ready(function() {
    loadProducts();
    loadCart();
});

// Load products from API
function loadProducts() {
    $.ajax({
        url: '/api/merchandise',
        method: 'GET',
        success: function(response) {
            console.log('[v0] Products loaded:', response);
            if (response.status && response.data) {
                products = response.data;
                displayProducts(products);
            } else {
                showError('Gagal memuat produk');
            }
        },
        error: function(xhr, status, error) {
            console.log('[v0] Error loading products:', error);
            showError('Gagal memuat produk: ' + error);
        }
    });
}

// Display products in grid
function displayProducts(products) {
    const container = $('#products-container');

    if (products.length === 0) {
        container.html('<div class="loading">Tidak ada produk tersedia</div>');
        return;
    }

    let html = '<div class="products-grid">';

    products.forEach(product => {
        const stockClass = product.stock === 0 ? 'stock-out' :
            product.stock < 10 ? 'stock-low' : 'stock-available';
        const stockText = product.stock === 0 ? 'Habis' :
            product.stock < 10 ? `Sisa ${product.stock}` : 'Tersedia';

        html += `
                    <div class="product-card">
                        <div class="product-image">
                            ${product.image_url ?
            `<img src="${product.image_url}" alt="${product.title}" onerror="this.style.display='none'; this.parentNode.innerHTML='📦';">` :
            '📦'
        }
                        </div>
                        <div class="product-info">
                            <h3 class="product-title">${product.title}</h3>
                            <p class="product-band">by ${product.band_name}</p>
                            ${product.description ? `<p class="product-description">${product.description}</p>` : ''}
                            <div class="product-price">Rp ${formatPrice(product.price)}</div>
                            <div class="product-stock ${stockClass}">Stok: ${stockText}</div>
                            <button class="add-to-cart" 
                                    onclick="addToCart(${product.id})" 
                                    ${product.stock === 0 ? 'disabled' : ''}>
                                ${product.stock === 0 ? 'Habis' : 'Tambah ke Keranjang'}
                            </button>
                        </div>
                    </div>
                `;
    });

    html += '</div>';
    container.html(html);
}

// Add product to cart
function addToCart(merchandiseId) {
    const product = products.find(p => p.id === merchandiseId);
    if (!product || product.stock === 0) {
        showError('Produk tidak tersedia');
        return;
    }

    $.ajax({
        url: '/api/cart/add-item',
        method: 'POST',
        contentType: 'application/json',
        data: JSON.stringify({
            userId: userId,
            merchandiseId: merchandiseId,
            qty: 1
        }),
        success: function(response) {
            console.log('[v0] Item added to cart:', response);
            if (response.status) {
                showSuccess(`${product.title} berhasil ditambahkan ke keranjang`);
                loadCart(); // Refresh cart
                loadProducts(); // Refresh products to update stock
            } else {
                showError(response.message || 'Gagal menambahkan ke keranjang');
            }
        },
        error: function(xhr, status, error) {
            console.log('[v0] Error adding to cart:', error);
            showError('Gagal menambahkan ke keranjang: ' + error);
        }
    });
}

// Load cart from API
function loadCart() {
    $.ajax({
        url: `/api/cart/user/${userId}/active-detail`,
        method: 'GET',
        success: function(response) {
            console.log('[v0] Cart loaded:', response);
            if (response.status && response.data) {
                cart = response.data;
                updateCartSummary();
            } else {
                // No active cart
                cart = null;
                updateCartSummary();
            }
        },
        error: function(xhr, status, error) {
            console.log('[v0] Error loading cart:', error);
            cart = null;
            updateCartSummary();
        }
    });
}

// Update cart summary in header
function updateCartSummary() {
    const cartCount = cart && cart.items ? cart.items.length : 0;
    const cartTotal = cart ? cart.cart_total_price : 0;

    $('#cart-count').text(`${cartCount} item${cartCount !== 1 ? 's' : ''}`);
    $('#cart-total').text(`Rp ${formatPrice(cartTotal)}`);
}

// Add new product
$('#addProductForm').on('submit', function(e) {
    e.preventDefault();

    const formData = {
        title: $('#title').val(),
        bandName: $('#bandName').val(),
        merchTypeId: parseInt($('#merchTypeId').val()),
        description: $('#description').val(),
        price: parseFloat($('#price').val()),
        imageUrl: $('#imageUrl').val(),
        stock: parseInt($('#stock').val())
    };

    $.ajax({
        url: '/api/merchandise',
        method: 'POST',
        contentType: 'application/json',
        data: JSON.stringify(formData),
        success: function(response) {
            console.log('[v0] Product added:', response);
            if (response.status) {
                showSuccess('Produk berhasil ditambahkan');
                closeAddProductModal();
                $('#addProductForm')[0].reset();
                loadProducts(); // Refresh products
            } else {
                showError(response.message || 'Gagal menambahkan produk');
            }
        },
        error: function(xhr, status, error) {
            console.log('[v0] Error adding product:', error);
            showError('Gagal menambahkan produk: ' + error);
        }
    });
});

// Modal functions
function openAddProductModal() {
    $('#addProductModal').show();
}

function closeAddProductModal() {
    $('#addProductModal').hide();
}

function openCartModal() {
    $('#cartModal').show();
    displayCart();
}

function closeCartModal() {
    $('#cartModal').hide();
}

// Display cart items
function displayCart() {
    const container = $('#cart-items');

    if (!cart || !cart.items || cart.items.length === 0) {
        container.html('<div class="loading">Keranjang kosong</div>');
        return;
    }

    let html = '';
    cart.items.forEach(item => {
        html += `
                    <div class="cart-item">
                        <div class="cart-item-info">
                            <h4>${item.title}</h4>
                            <p>by ${item.band_name}</p>
                            <p>Qty: ${item.qty} × Rp ${formatPrice(item.unit_price)}</p>
                        </div>
                        <div class="cart-item-price">
                            Rp ${formatPrice(item.qty * item.unit_price)}
                        </div>
                    </div>
                `;
    });

    html += `
                <div class="cart-total">
                    Total: Rp ${formatPrice(cart.cart_total_price)}
                </div>
            `;

    container.html(html);
}

// Utility functions
function formatPrice(price) {
    return new Intl.NumberFormat('id-ID').format(price);
}

function showError(message) {
    // Remove existing alerts
    $('.error, .success').remove();

    const alert = $(`<div class="error">${message}</div>`);
    $('.container').prepend(alert);

    setTimeout(() => {
        alert.fadeOut(() => alert.remove());
    }, 5000);
}

function showSuccess(message) {
    // Remove existing alerts
    $('.error, .success').remove();

    const alert = $(`<div class="success">${message}</div>`);
    $('.container').prepend(alert);

    setTimeout(() => {
        alert.fadeOut(() => alert.remove());
    }, 3000);
}

// Close modals when clicking outside
$(window).on('click', function(e) {
    if (e.target.classList.contains('modal')) {
        $(e.target).hide();
    }
});