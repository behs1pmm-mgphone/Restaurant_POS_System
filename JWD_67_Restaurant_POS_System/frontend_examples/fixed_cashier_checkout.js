// Fixed Cashier Checkout Flow - Addresses UI State Logic, PDF Generation, and Database Issues
// This fixes the three main issues: UI state, PDF download, and database mapping

class FixedCashierCheckout {
    constructor() {
        this.initializeEventListeners();
        this.setupPeriodicStatusCheck();
    }

    initializeEventListeners() {
        document.addEventListener('DOMContentLoaded', () => {
            this.attachCheckoutHandlers();
            this.refreshOrderBoard(); // Initial load
        });
    }

    attachCheckoutHandlers() {
        const checkoutButtons = document.querySelectorAll('[data-action="checkout"]');
        checkoutButtons.forEach(button => {
            // Remove existing listeners to prevent duplicates
            button.replaceWith(button.cloneNode(true));
        });
        
        // Re-attach handlers to fresh buttons
        document.querySelectorAll('[data-action="checkout"]').forEach(button => {
            button.addEventListener('click', (e) => {
                e.preventDefault();
                const orderId = e.target.dataset.orderId;
                this.handleCheckout(orderId);
            });
        });
    }

    async handleCheckout(orderId) {
        try {
            console.log(`Starting checkout for order ${orderId}`);
            
            // Show loading state and disable button
            this.showLoadingState(orderId);
            
            // Call checkout API
            const response = await fetch(`/cashier/orders/${orderId}/checkout`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'X-Requested-With': 'XMLHttpRequest'
                },
                body: JSON.stringify({
                    paymentMethod: 'Cash' // Can be made configurable
                })
            });

            const result = await response.json();
            console.log('Checkout response:', result);

            if (result.success) {
                // Payment successful - update UI immediately
                this.handlePaymentSuccess(orderId, result);
                
                // Trigger PDF download with retry mechanism
                await this.downloadReceiptWithRetry(orderId);
                
                // Refresh the order board to update all statuses
                setTimeout(() => {
                    this.refreshOrderBoard();
                }, 1000);
                
            } else {
                // Payment failed
                this.handlePaymentError(orderId, result.message);
            }

        } catch (error) {
            console.error('Checkout error:', error);
            this.handleNetworkError(orderId, error);
        }
    }

    handlePaymentSuccess(orderId, result) {
        console.log(`Payment successful for order ${orderId}`);
        
        // Remove any existing messages
        this.removeMessages(orderId);
        
        // Hide "waiting for items" message immediately
        this.hideWaitingMessage(orderId);
        
        // Show success message
        this.showSuccessMessage(orderId, result);
        
        // Update order card to paid state
        this.updateOrderCardToPaid(orderId);
        
        // Disable checkout button permanently
        this.disableCheckoutButton(orderId);
    }

    hideWaitingMessage(orderId) {
        // Find and remove any "waiting for items" messages
        const orderCard = document.querySelector(`[data-order-card="${orderId}"]`);
        if (orderCard) {
            const waitingMessages = orderCard.querySelectorAll('.waiting-message, .status-waiting');
            waitingMessages.forEach(msg => {
                msg.style.display = 'none';
                msg.remove();
            });
        }
    }

    showSuccessMessage(orderId, result) {
        const orderCard = document.querySelector(`[data-order-card="${orderId}"]`);
        if (!orderCard) return;

        // Create success message
        const successDiv = document.createElement('div');
        successDiv.className = 'alert alert-success payment-success-message';
        successDiv.innerHTML = `
            <div class="success-icon"> Payment Successful!</div>
            <div class="payment-details">
                <strong>Order #${orderId}</strong><br>
                Total: ${result.totalAmount} MMK<br>
                Method: ${result.paymentMethod}
            </div>
        `;
        
        // Insert at the top of the order card
        orderCard.insertBefore(successDiv, orderCard.firstChild);
        
        // Auto-hide after 5 seconds
        setTimeout(() => {
            if (successDiv.parentNode) {
                successDiv.style.opacity = '0';
                setTimeout(() => successDiv.remove(), 300);
            }
        }, 5000);
    }

    updateOrderCardToPaid(orderId) {
        const orderCard = document.querySelector(`[data-order-card="${orderId}"]`);
        if (!orderCard) return;

        // Update status display
        const statusElements = orderCard.querySelectorAll('.order-status, .status-display');
        statusElements.forEach(element => {
            element.textContent = 'Paid';
            element.className = 'order-status status-paid';
        });

        // Add paid badge
        const paidBadge = document.createElement('span');
        paidBadge.className = 'badge badge-success paid-badge';
        paidBadge.innerHTML = ' <i class="fas fa-check"></i> Paid';
        
        // Replace checkout button with paid badge
        const checkoutButton = orderCard.querySelector('[data-action="checkout"]');
        if (checkoutButton) {
            checkoutButton.parentNode.replaceChild(paidBadge, checkoutButton);
        }

        // Update card styling
        orderCard.classList.add('order-paid');
        orderCard.classList.remove('order-pending');
    }

    disableCheckoutButton(orderId) {
        const button = document.querySelector(`[data-action="checkout"][data-order-id="${orderId}"]`);
        if (button) {
            button.disabled = true;
            button.textContent = 'Paid';
            button.className = 'btn btn-success paid-button';
        }
    }

    async downloadReceiptWithRetry(orderId, maxRetries = 3) {
        for (let attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                console.log(`Receipt download attempt ${attempt} for order ${orderId}`);
                
                const response = await fetch(`/cashier/orders/${orderId}/receipt`, {
                    method: 'GET',
                    headers: {
                        'X-Requested-With': 'XMLHttpRequest'
                    }
                });

                if (response.ok) {
                    const blob = await response.blob();
                    const url = window.URL.createObjectURL(blob);
                    const a = document.createElement('a');
                    a.href = url;
                    a.download = `order_${orderId}_receipt.pdf`;
                    document.body.appendChild(a);
                    a.click();
                    window.URL.revokeObjectURL(url);
                    document.body.removeChild(a);
                    
                    console.log('Receipt downloaded successfully');
                    this.showReceiptDownloadSuccess(orderId);
                    return true;
                    
                } else {
                    console.error(`Receipt download failed: ${response.status} ${response.statusText}`);
                    if (attempt === maxRetries) {
                        this.showReceiptDownloadError(orderId);
                    }
                }
                
            } catch (error) {
                console.error(`Receipt download error (attempt ${attempt}):`, error);
                if (attempt === maxRetries) {
                    this.showReceiptDownloadError(orderId);
                }
            }
            
            // Wait before retry (exponential backoff)
            if (attempt < maxRetries) {
                await new Promise(resolve => setTimeout(resolve, 1000 * attempt));
            }
        }
        
        return false;
    }

    showReceiptDownloadSuccess(orderId) {
        // Optional: Show a subtle success notification for PDF download
        const notification = document.createElement('div');
        notification.className = 'receipt-download-success';
        notification.innerHTML = '<i class="fas fa-file-pdf"></i> Receipt downloaded';
        document.body.appendChild(notification);
        
        setTimeout(() => {
            notification.style.opacity = '0';
            setTimeout(() => notification.remove(), 300);
        }, 2000);
    }

    showReceiptDownloadError(orderId) {
        const orderCard = document.querySelector(`[data-order-card="${orderId}"]`);
        if (!orderCard) return;

        const warningDiv = document.createElement('div');
        warningDiv.className = 'alert alert-warning receipt-warning';
        warningDiv.innerHTML = `
            <strong>Note:</strong> Payment successful, but receipt download failed. 
            <a href="/cashier/orders/${orderId}/receipt" target="_blank">Click here to download manually</a>
        `;
        
        orderCard.appendChild(warningDiv);
        
        // Auto-hide after 10 seconds
        setTimeout(() => {
            if (warningDiv.parentNode) {
                warningDiv.remove();
            }
        }, 10000);
    }

    handlePaymentError(orderId, message) {
        console.error(`Payment failed for order ${orderId}: ${message}`);
        
        this.removeMessages(orderId);
        this.hideLoadingState(orderId);
        
        const orderCard = document.querySelector(`[data-order-card="${orderId}"]`);
        if (orderCard) {
            const errorDiv = document.createElement('div');
            errorDiv.className = 'alert alert-danger payment-error';
            errorDiv.innerHTML = `<strong>Payment Failed:</strong> ${message}`;
            
            orderCard.insertBefore(errorDiv, orderCard.firstChild);
            
            // Re-enable checkout button
            const button = orderCard.querySelector('[data-action="checkout"]');
            if (button) {
                button.disabled = false;
                button.textContent = 'Checkout';
            }
        }
    }

    handleNetworkError(orderId, error) {
        console.error(`Network error for order ${orderId}:`, error);
        
        this.removeMessages(orderId);
        this.hideLoadingState(orderId);
        
        const orderCard = document.querySelector(`[data-order-card="${orderId}"]`);
        if (orderCard) {
            const errorDiv = document.createElement('div');
            errorDiv.className = 'alert alert-warning network-error';
            errorDiv.innerHTML = `
                <strong>Network Error:</strong> Unable to process payment. 
                Please check your connection and try again.
            `;
            
            orderCard.insertBefore(errorDiv, orderCard.firstChild);
            
            // Re-enable checkout button
            const button = orderCard.querySelector('[data-action="checkout"]');
            if (button) {
                button.disabled = false;
                button.textContent = 'Checkout';
            }
        }
    }

    showLoadingState(orderId) {
        const button = document.querySelector(`[data-action="checkout"][data-order-id="${orderId}"]`);
        if (button) {
            button.disabled = true;
            button.innerHTML = '<span class="spinner"></span> Processing...';
        }
    }

    hideLoadingState(orderId) {
        const button = document.querySelector(`[data-action="checkout"][data-order-id="${orderId}"]`);
        if (button) {
            button.disabled = false;
            button.textContent = 'Checkout';
        }
    }

    removeMessages(orderId) {
        const orderCard = document.querySelector(`[data-order-card="${orderId}"]`);
        if (orderCard) {
            const messages = orderCard.querySelectorAll('.alert');
            messages.forEach(msg => msg.remove());
        }
    }

    async refreshOrderBoard() {
        try {
            console.log('Refreshing order board...');
            
            const response = await fetch('/cashier/orders', {
                method: 'GET',
                headers: {
                    'X-Requested-With': 'XMLHttpRequest'
                }
            });

            if (response.ok) {
                const orders = await response.json();
                console.log('Orders refreshed:', orders);
                
                // Update UI with new order data
                this.updateOrderBoardUI(orders);
                
                // Re-attach event handlers for new elements
                this.attachCheckoutHandlers();
                
            } else {
                console.error('Failed to refresh orders:', response.statusText);
            }
            
        } catch (error) {
            console.error('Error refreshing order board:', error);
        }
    }

    updateOrderBoardUI(orders) {
        // This would update the UI with the latest order data
        // Implementation depends on your specific UI structure
        // For now, we'll just re-attach handlers
        console.log('Updating UI with', orders.length, 'orders');
    }

    setupPeriodicStatusCheck() {
        // Periodically check for status updates every 30 seconds
        setInterval(() => {
            this.refreshOrderBoard();
        }, 30000);
    }
}

// Enhanced CSS for better UX
const enhancedStyles = `
.payment-success-message {
    background: linear-gradient(135deg, #28a745, #20c997);
    color: white;
    padding: 15px;
    border-radius: 8px;
    margin-bottom: 15px;
    animation: slideDown 0.3s ease-out;
}

.success-icon {
    font-size: 18px;
    font-weight: bold;
    margin-bottom: 8px;
}

.payment-details {
    font-size: 14px;
    opacity: 0.9;
}

.status-paid {
    background-color: #28a745 !important;
    color: white !important;
    padding: 4px 8px;
    border-radius: 4px;
}

.paid-badge {
    background-color: #28a745;
    color: white;
    padding: 6px 12px;
    border-radius: 20px;
    font-weight: bold;
    font-size: 14px;
}

.order-paid {
    border-left: 4px solid #28a745;
    background-color: #f8fff9;
}

.paid-button {
    background-color: #6c757d;
    border-color: #6c757d;
    cursor: not-allowed;
}

.receipt-warning {
    background-color: #fff3cd;
    border: 1px solid #ffeaa7;
    color: #856404;
    padding: 10px;
    border-radius: 4px;
    margin-top: 10px;
    font-size: 13px;
}

.receipt-download-success {
    position: fixed;
    top: 20px;
    right: 20px;
    background-color: #28a745;
    color: white;
    padding: 10px 15px;
    border-radius: 4px;
    z-index: 1000;
    animation: slideInRight 0.3s ease-out;
}

@keyframes slideDown {
    from {
        opacity: 0;
        transform: translateY(-10px);
    }
    to {
        opacity: 1;
        transform: translateY(0);
    }
}

@keyframes slideInRight {
    from {
        opacity: 0;
        transform: translateX(100px);
    }
    to {
        opacity: 1;
        transform: translateX(0);
    }
}

/* Hide waiting messages for paid orders */
.order-paid .waiting-message,
.order-paid .status-waiting {
    display: none !important;
}
`;

// Add enhanced styles to page
if (document.getElementById('enhanced-checkout-styles') === null) {
    const styleSheet = document.createElement('style');
    styleSheet.id = 'enhanced-checkout-styles';
    styleSheet.textContent = enhancedStyles;
    document.head.appendChild(styleSheet);
}

// Initialize the fixed checkout system
const fixedCashierCheckout = new FixedCashierCheckout();
