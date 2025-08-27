let currentCart = null;
let merchTypes = [];

// Initialize the application
$(document).ready(function() {
    loadProducts();
    loadCart();
    loadMerchTypes();
    loadTransactions();
});

// Tab functionality
function showTab(tabName) {
    $('.tab').removeClass('active');
    $('.tab-content').removeClass('active');

    $(`button:contains("${tabName.charAt(0).toUpperCase() + tabName.slice(1)}")`).addClass('active');
    $(`#${tabName}`).addClass('active');

    // Load data when switching tabs
    if (tabName === 'cart') {
        loadCart();
    } else if (tabName === 'transactions') {
        loadTransactions();
    } else if (tabName === 'merchtypes') {
        loadMerchTypes();
    }
}

// Load products from API
function loadProducts() {
    $.ajax({
        url: '/api/merchandise',
        method: 'GET',
        success: function(response) {
            displayProducts(response.data);
        },
        error: function(xhr, status, error) {
            $('#productsContainer').html('<div class="alert alert-error">Error loading products: ' + error + '</div>');
        }
    });
}

// Display products
function displayProducts(products) {
    let html = '<div class="products-grid">';

    products.forEach(product => {
        const stockClass = product.stock > 0 ? 'in-stock' : 'out-of-stock';
        const stockText = product.stock > 0 ? `In Stock (${product.stock})` : 'Out of Stock';
        const addToCartBtn = product.stock > 0 ?
            `<button class="btn btn-success" onclick="addToCart(${product.id})">Add to Cart</button>` :
            `<button class="btn" disabled>Out of Stock</button>`;

        html += `
                    <div class="product-card">
                        <div class="product-image">
                            ${product.image_url ?
            `<img src="${product.image_url}" alt="${product.title}" style="width:100%;height:100%;object-fit:cover;border-radius:5px;">` :
            '📦 No Image'
        }
                        </div>
                        <div class="product-title">${product.title}</div>
                        <div class="product-band">by ${product.band_name}</div>
                        <div class="product-price">Rp ${product.price.toLocaleString()}</div>
                        <div class="product-stock ${stockClass}">${stockText}</div>
                        <div class="product-description">${product.description || 'No description available'}</div>
                        <div style="margin-top: 1rem;">
                            ${addToCartBtn}
                        </div>
                    </div>
                `;
    });

    html += '</div>';
    $('#productsContainer').html(html);
}

// Load merchandise types
function loadMerchTypes() {
    $.ajax({
        url: '/api/merchtypes',
        method: 'GET',
        success: function(response) {
            merchTypes = response.data;
            displayMerchTypes(response.data);
            updateMerchTypeSelect();
        },
        error: function(xhr, status, error) {
            $('#merchTypesContainer').html('<div class="alert alert-error">Error loading merchandise types: ' + error + '</div>');
        }
    });
}

// Display merchandise types
function displayMerchTypes(types) {
    let html = '<div class="merch-types-grid">';

    types.forEach(type => {
        html += `
                    <div class="merch-type-card">
                        <h3>${type.name}</h3>
                        <p>${type.description}</p>
                        <small>Created: ${new Date(type.created_at).toLocaleDateString()}</small>
                    </div>
                `;
    });

    html += '</div>';
    $('#merchTypesContainer').html(html);
}

// Update merchandise type select options
function updateMerchTypeSelect() {
    const select = $('#productMerchType');
    select.find('option:not(:first)').remove();

    merchTypes.forEach(type => {
        select.append(`<option value="${type.id}">${type.name}</option>`);
    });
}

// Load cart from API
function loadCart() {
    $.ajax({
        url: '/api/cart/user/1/active-detail',
        method: 'GET',
        success: function(response) {
            currentCart = response.data;
            displayCart(response.data);
            updateCartSummary(response.data);
        },
        error: function(xhr, status, error) {
            $('#cartContainer').html('<div class="alert alert-error">Error loading cart: ' + error + '</div>');
            $('#cartCount').text('0');
        }
    });
}

// Display cart
function displayCart(cart) {
    if (!cart || !cart.items || cart.items.length === 0) {
        $('#cartContainer').html('<p>Your cart is empty</p>');
        return;
    }

    let html = '<div class="cart-items">';

    cart.items.forEach(item => {
        html += `
                    <div class="cart-item">
                        <div>
                            <div class="product-title">${item.title}</div>
                            <div class="product-band">by ${item.band_name}</div>
                            <div class="product-price">Rp ${item.unit_price.toLocaleString()} each</div>
                        </div>
                        <div class="quantity-controls">
                            <span>Qty: ${item.qty}</span>
                            <div>Rp ${(item.unit_price * item.qty).toLocaleString()}</div>
                        </div>
                    </div>
                `;
    });

    html += '</div>';
    html += `
                <div style="margin-top: 1rem; padding-top: 1rem; border-top: 2px solid #667eea;">
                    <div style="display: flex; justify-content: space-between; font-size: 1.25rem; font-weight: bold;">
                        <span>Total:</span>
                        <span>Rp ${cart.cart_total_price.toLocaleString()}</span>
                    </div>
                </div>
            `;

    // Add checkout form
    html += `
                <div class="checkout-form">
                    <h3>Checkout</h3>
                    <div class="form-group">
                        <label for="deliveryPrice">Delivery Service Price:</label>
                        <input type="number" id="deliveryPrice" class="form-control" value="10000" required>
                    </div>
                    <button class="btn btn-success" onclick="checkout()">Checkout</button>
                </div>
            `;

    $('#cartContainer').html(html);
}

// Update cart summary
function updateCartSummary(cart) {
    if (cart && cart.items) {
        const totalItems = cart.items.reduce((sum, item) => sum + item.qty, 0);
        $('#cartCount').text(totalItems);
    } else {
        $('#cartCount').text('0');
    }
}

// Add to cart
function addToCart(merchandiseId) {
    $.ajax({
        url: '/api/cart/add-item',
        method: 'POST',
        contentType: 'application/json',
        data: JSON.stringify({
            userId: 1,
            merchandiseId: merchandiseId,
            qty: 1
        }),
        success: function(response) {
            showAlert('Product added to cart!', 'success');
            loadCart();
            loadProducts(); // Refresh to update stock
        },
        error: function(xhr, status, error) {
            showAlert('Error adding to cart: ' + error, 'error');
        }
    });
}

// Checkout
function checkout() {
    if (!currentCart || !currentCart.cart_id) {
        showAlert('No active cart found', 'error');
        return;
    }

    const deliveryPrice = parseFloat($('#deliveryPrice').val()) || 10000;

    $.ajax({
        url: '/api/transactions',
        method: 'POST',
        contentType: 'application/json',
        data: JSON.stringify({
            cartId: currentCart.cart_id,
            deliveryServicePrice: deliveryPrice
        }),
        success: function(response) {
            showAlert('Checkout successful!', 'success');
            loadCart();
            loadTransactions();
        },
        error: function(xhr, status, error) {
            showAlert('Error during checkout: ' + error, 'error');
        }
    });
}

// Load transactions
function loadTransactions() {
    $.ajax({
        url: '/api/transactions-detail',
        method: 'GET',
        success: function(response) {
            displayTransactions(response.data);
        },
        error: function(xhr, status, error) {
            $('#transactionsContainer').html('<div class="alert alert-error">Error loading transactions: ' + error + '</div>');
        }
    });
}

// Display transactions
function displayTransactions(transactions) {
    if (!transactions || transactions.length === 0) {
        $('#transactionsContainer').html('<p>No transactions found</p>');
        return;
    }

    let html = '';

    transactions.forEach(transaction => {
        html += `
                    <div class="transaction-card">
                        <div class="transaction-header">
                            <div>
                                <h3>Transaction #${transaction.id}</h3>
                                <p><strong>Customer:</strong> ${transaction.cart.user.name}</p>
                                <p><strong>Date:</strong> ${new Date(transaction.created_at).toLocaleDateString()}</p>
                            </div>
                            <div style="text-align: right;">
                                <div style="font-size: 1.5rem; font-weight: bold; color: #667eea;">
                                    Rp ${transaction.total_price.toLocaleString()}
                                </div>
                                <div style="font-size: 0.875rem; color: #6c757d;">
                                    Status: ${transaction.cart.status}
                                </div>
                            </div>
                        </div>
                        
                        <div class="transaction-items">
                            <h4>Items:</h4>
                `;

        transaction.cart.items.forEach(item => {
            html += `
                        <div class="transaction-item">
                            <div>
                                <strong>${item.title}</strong> by ${item.band_name}
                                <br><small>Qty: ${item.qty} × Rp ${item.unit_price.toLocaleString()}</small>
                            </div>
                            <div>Rp ${(item.qty * item.unit_price).toLocaleString()}</div>
                        </div>
                    `;
        });

        html += `
                        </div>
                        
                        <div style="margin-top: 1rem; padding-top: 1rem; border-top: 1px solid #eee;">
                            <div style="display: flex; justify-content: space-between;">
                                <span>Cart Total:</span>
                                <span>Rp ${transaction.cart_price.toLocaleString()}</span>
                            </div>
                            <div style="display: flex; justify-content: space-between;">
                                <span>Delivery:</span>
                                <span>Rp ${transaction.delivery_service_price.toLocaleString()}</span>
                            </div>
                            <div style="display: flex; justify-content: space-between; font-weight: bold; font-size: 1.1rem; margin-top: 0.5rem; padding-top: 0.5rem; border-top: 1px solid #eee;">
                                <span>Total:</span>
                                <span>Rp ${transaction.total_price.toLocaleString()}</span>
                            </div>
                        </div>
                    </div>
                `;
    });

    $('#transactionsContainer').html(html);
}

// Modal functions
function openAddProductModal() {
    $('#addProductModal').show();
}

function openAddMerchTypeModal() {
    $('#addMerchTypeModal').show();
}

function openCartModal() {
    $('#cartModalContent').html($('#cartContainer').html());
    $('#cartModal').show();
}

function closeModal(modalId) {
    $('#' + modalId).hide();
}

// Form submissions
$('#addProductForm').submit(function(e) {
    e.preventDefault();

    const productData = {
        title: $('#productTitle').val(),
        bandName: $('#productBandName').val(),
        merchTypeId: parseInt($('#productMerchType').val()),
        description: $('#productDescription').val(),
        price: parseFloat($('#productPrice').val()),
        imageUrl: $('#productImageUrl').val(),
        stock: parseInt($('#productStock').val())
    };

    $.ajax({
        url: '/api/merchandise',
        method: 'POST',
        contentType: 'application/json',
        data: JSON.stringify(productData),
        success: function(response) {
            showAlert('Product added successfully!', 'success');
            closeModal('addProductModal');
            $('#addProductForm')[0].reset();
            loadProducts();
        },
        error: function(xhr, status, error) {
            showAlert('Error adding product: ' + error, 'error');
        }
    });
});

$('#addMerchTypeForm').submit(function(e) {
    e.preventDefault();

    const typeData = {
        name: $('#merchTypeName').val(),
        description: $('#merchTypeDescription').val()
    };

    $.ajax({
        url: '/api/merchtypes',
        method: 'POST',
        contentType: 'application/json',
        data: JSON.stringify(typeData),
        success: function(response) {
            showAlert('Merchandise type added successfully!', 'success');
            closeModal('addMerchTypeModal');
            $('#addMerchTypeForm')[0].reset();
            loadMerchTypes();
        },
        error: function(xhr, status, error) {
            showAlert('Error adding merchandise type: ' + error, 'error');
        }
    });
});

// Utility functions
function showAlert(message, type) {
    const alertClass = type === 'success' ? 'alert-success' : 'alert-error';
    const alertHtml = `<div class="alert ${alertClass}">${message}</div>`;

    // Show alert at the top of the current tab
    const activeTab = $('.tab-content.active');
    activeTab.prepend(alertHtml);

    // Remove alert after 3 seconds
    setTimeout(() => {
        activeTab.find('.alert').first().remove();
    }, 3000);
}

// Close modals when clicking outside
$(window).click(function(event) {
    if ($(event.target).hasClass('modal')) {
        $(event.target).hide();
    }
});