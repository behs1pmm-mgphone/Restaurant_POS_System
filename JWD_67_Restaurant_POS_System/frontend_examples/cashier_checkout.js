// Cashier Checkout Flow - Frontend Implementation
// This demonstrates how to handle the new checkout API properly

class CashierCheckout {
    constructor() {
        this.initializeEventListeners();
    }

    initializeEventListeners() {
        // Add click handlers to all checkout buttons
        document.addEventListener('DOMContentLoaded', () => {
            const checkoutButtons = document.querySelectorAll('[data-action="checkout"]');
            checkoutButtons.forEach(button => {
                button.addEventListener('click', (e) => {
                    const orderId = e.target.dataset.orderId;
                    this.handleCheckout(orderId);
                });
            });
        });
    }

    async handleCheckout(orderId) {
        try {
            // Show loading state
            this.showLoading(orderId);
            
            // Disable checkout button to prevent multiple clicks
            const button = document.querySelector(`[data-order-id="${orderId}"]`);
            button.disabled = true;
            button.textContent = 'Processing...';

            // Call checkout API
            const response = await fetch(`/cashier/orders/${orderId}/checkout`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'X-Requested-With': 'XMLHttpRequest'
                },
                body: JSON.stringify({
                    paymentMethod: 'Cash' // or get from user input
                })
            });

            const result = await response.json();

            if (result.success) {
                // Payment successful - show success message
                this.showPaymentSuccess(orderId, result);
                
                // Automatically download PDF receipt
                await this.downloadReceipt(orderId);
                
                // Update UI to show paid status
                this.updateOrderStatus(orderId, 'Paid');
                
            } else {
                // Payment failed - show error
                this.showPaymentError(orderId, result.message);
                // Re-enable button
                button.disabled = false;
                button.textContent = 'Checkout';
            }

        } catch (error) {
            console.error('Checkout error:', error);
            this.showNetworkError(orderId);
            
            // Re-enable button on network error
            const button = document.querySelector(`[data-order-id="${orderId}"]`);
            button.disabled = false;
            button.textContent = 'Checkout';
        }
    }

    async downloadReceipt(orderId) {
        try {
            console.log(`Downloading receipt for order ${orderId}`);
            
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
            } else {
                console.error('Failed to download receipt:', response.statusText);
                // Show warning but don't fail the payment
                this.showReceiptDownloadWarning(orderId);
            }
        } catch (error) {
            console.error('Receipt download error:', error);
            // Show warning but don't fail the payment
            this.showReceiptDownloadWarning(orderId);
        }
    }

    showPaymentSuccess(orderId, result) {
        // Remove any existing messages
        this.removeMessages(orderId);
        
        // Create success message
        const successDiv = document.createElement('div');
        successDiv.className = 'alert alert-success payment-success';
        successDiv.innerHTML = `
            <strong>Payment Successful!</strong><br>
            Order #${orderId} - Total: ${result.totalAmount} MMK<br>
            Payment Method: ${result.paymentMethod}
        `;
        
        // Insert after order card
        const orderCard = document.querySelector(`[data-order-card="${orderId}"]`);
        orderCard.parentNode.insertBefore(successDiv, orderCard.nextSibling);
        
        // Auto-hide after 5 seconds
        setTimeout(() => {
            if (successDiv.parentNode) {
                successDiv.parentNode.removeChild(successDiv);
            }
        }, 5000);
    }

    showPaymentError(orderId, message) {
        this.removeMessages(orderId);
        
        const errorDiv = document.createElement('div');
        errorDiv.className = 'alert alert-danger payment-error';
        errorDiv.innerHTML = `<strong>Payment Failed:</strong> ${message}`;
        
        const orderCard = document.querySelector(`[data-order-card="${orderId}"]`);
        orderCard.parentNode.insertBefore(errorDiv, orderCard.nextSibling);
    }

    showNetworkError(orderId) {
        this.removeMessages(orderId);
        
        const errorDiv = document.createElement('div');
        errorDiv.className = 'alert alert-warning network-error';
        errorDiv.innerHTML = `
            <strong>Network Error:</strong> Unable to process payment. Please check your connection and try again.
        `;
        
        const orderCard = document.querySelector(`[data-order-card="${orderId}"]`);
        orderCard.parentNode.insertBefore(errorDiv, orderCard.nextSibling);
    }

    showReceiptDownloadWarning(orderId) {
        const warningDiv = document.createElement('div');
        warningDiv.className = 'alert alert-warning';
        warningDiv.innerHTML = `
            <strong>Note:</strong> Payment was successful, but receipt download failed. 
            You can download the receipt manually from the order details.
        `;
        
        document.body.appendChild(warningDiv);
        
        setTimeout(() => {
            if (warningDiv.parentNode) {
                warningDiv.parentNode.removeChild(warningDiv);
            }
        }, 8000);
    }

    updateOrderStatus(orderId, status) {
        // Update the status display in the UI
        const statusElement = document.querySelector(`[data-status="${orderId}"]`);
        if (statusElement) {
            statusElement.textContent = status;
            statusElement.className = `status status-${status.toLowerCase()}`;
        }

        // Hide checkout button and show paid status
        const checkoutButton = document.querySelector(`[data-order-id="${orderId}"]`);
        if (checkoutButton) {
            checkoutButton.style.display = 'none';
        }

        // Show paid badge
        const paidBadge = document.createElement('span');
        paidBadge.className = 'badge badge-success paid-badge';
        paidBadge.textContent = '✓ Paid';
        
        const orderCard = document.querySelector(`[data-order-card="${orderId}"]`);
        const buttonContainer = orderCard.querySelector('.button-container');
        if (buttonContainer) {
            buttonContainer.appendChild(paidBadge);
        }
    }

    showLoading(orderId) {
        const button = document.querySelector(`[data-order-id="${orderId}"]`);
        if (button) {
            button.disabled = true;
            button.innerHTML = '<span class="spinner"></span> Processing...';
        }
    }

    removeMessages(orderId) {
        const orderCard = document.querySelector(`[data-order-card="${orderId}"]`);
        if (orderCard) {
            const messages = orderCard.parentNode.querySelectorAll('.alert');
            messages.forEach(msg => msg.remove());
        }
    }
}

// Initialize the checkout system
const cashierCheckout = new CashierCheckout();

// CSS for styling (add to your existing CSS)
const checkoutStyles = `
.payment-success {
    background-color: #d4edda;
    border: 1px solid #c3e6cb;
    color: #155724;
    padding: 12px;
    border-radius: 4px;
    margin: 10px 0;
}

.payment-error {
    background-color: #f8d7da;
    border: 1px solid #f5c6cb;
    color: #721c24;
    padding: 12px;
    border-radius: 4px;
    margin: 10px 0;
}

.network-error {
    background-color: #fff3cd;
    border: 1px solid #ffeaa7;
    color: #856404;
    padding: 12px;
    border-radius: 4px;
    margin: 10px 0;
}

.paid-badge {
    background-color: #28a745;
    color: white;
    padding: 4px 8px;
    border-radius: 12px;
    font-size: 12px;
    font-weight: bold;
}

.spinner {
    display: inline-block;
    width: 12px;
    height: 12px;
    border: 2px solid #f3f3f3;
    border-top: 2px solid #3498db;
    border-radius: 50%;
    animation: spin 1s linear infinite;
    margin-right: 8px;
}

@keyframes spin {
    0% { transform: rotate(0deg); }
    100% { transform: rotate(360deg); }
}
`;

// Add styles to page
if (document.getElementById('checkout-styles') === null) {
    const styleSheet = document.createElement('style');
    styleSheet.id = 'checkout-styles';
    styleSheet.textContent = checkoutStyles;
    document.head.appendChild(styleSheet);
}
