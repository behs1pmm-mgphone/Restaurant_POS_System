// Enhanced Cashier Checkout Flow - Fixed PDF Download with Better Error Handling
// This addresses PDF generation issues with comprehensive error handling and fallback mechanisms

class EnhancedCashierCheckout {
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
                
                // Trigger PDF download with enhanced retry mechanism
                await this.downloadReceiptWithEnhancedRetry(orderId);
                
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
            const waitingMessages = orderCard.querySelectorAll('.waiting-message');
            waitingMessages.forEach(msg => msg.remove());
        }
    }

    showSuccessMessage(orderId, result) {
        const orderCard = document.querySelector(`[data-order-card="${orderId}"]`);
        if (!orderCard) return;

        const successDiv = document.createElement('div');
        successDiv.className = 'alert alert-success payment-success';
        successDiv.innerHTML = `
            <strong>Payment Successful!</strong><br>
            Order #${orderId} - Amount: ${result.totalAmount || 'N/A'} MMK<br>
            Method: ${result.paymentMethod || 'Cash'}
        `;
        
        orderCard.appendChild(successDiv);
        
        // Auto-hide success message after 5 seconds
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

        // Add paid styling
        orderCard.classList.add('order-paid');
        orderCard.classList.remove('order-pending');
        
        // Update status badge
        const statusBadge = orderCard.querySelector('.order-status');
        if (statusBadge) {
            statusBadge.className = 'badge badge-success';
            statusBadge.textContent = 'Paid';
        }
    }

    disableCheckoutButton(orderId) {
        const button = document.querySelector(`[data-action="checkout"][data-order-id="${orderId}"]`);
        if (button) {
            button.disabled = true;
            button.textContent = 'Paid';
            button.className = 'btn btn-success paid-button';
            
            // Add paid icon
            button.innerHTML = ' <i class="fas fa-check"></i> Paid';
        }
    }

    async downloadReceiptWithEnhancedRetry(orderId, maxRetries = 3) {
        for (let attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                console.log(`Receipt download attempt ${attempt} for order ${orderId}`);
                
                // Try main receipt endpoint first
                let response = await this.tryReceiptDownload(orderId, '/cashier/orders/' + orderId + '/receipt');
                
                if (response.success) {
                    console.log('Receipt downloaded successfully from main endpoint');
                    this.showReceiptDownloadSuccess(orderId);
                    return true;
                }
                
                // If main endpoint fails, try backup endpoint
                if (attempt === 2) {
                    console.log('Trying backup PDF endpoint...');
                    response = await this.tryReceiptDownload(orderId, '/cashier/orders/' + orderId + '/pdf');
                    
                    if (response.success) {
                        console.log('Receipt downloaded successfully from backup endpoint');
                        this.showReceiptDownloadSuccess(orderId);
                        return true;
                    }
                }
                
                // If both fail, try test endpoint (for debugging)
                if (attempt === 3) {
                    console.log('Trying test PDF endpoint for debugging...');
                    response = await this.tryReceiptDownload(orderId, '/test/pdf/' + orderId);
                    
                    if (response.success) {
                        console.log('Test PDF downloaded successfully');
                        this.showReceiptDownloadSuccess(orderId, 'Test PDF');
                        return true;
                    }
                }
                
                if (attempt === maxRetries) {
                    this.showReceiptDownloadError(orderId);
                }
                
            } catch (error) {
                console.error(`Receipt download error (attempt ${attempt}):`, error);
                if (attempt === maxRetries) {
                    this.showReceiptDownloadError(orderId, error.message);
                }
            }
            
            // Wait before retry (exponential backoff)
            if (attempt < maxRetries) {
                await new Promise(resolve => setTimeout(resolve, 1000 * attempt));
            }
        }
        
        return false;
    }

    async tryReceiptDownload(orderId, endpoint) {
        try {
            const response = await fetch(endpoint, {
                method: 'GET',
                headers: {
                    'X-Requested-With': 'XMLHttpRequest'
                }
            });

            if (response.ok) {
                const contentType = response.headers.get('Content-Type');
                
                // Check if we got a PDF or an error message
                if (contentType && contentType.includes('application/pdf')) {
                    const blob = await response.blob();
                    const url = window.URL.createObjectURL(blob);
                    const a = document.createElement('a');
                    a.href = url;
                    a.download = `order_${orderId}_receipt.pdf`;
                    document.body.appendChild(a);
                    a.click();
                    window.URL.revokeObjectURL(url);
                    document.body.removeChild(a);
                    
                    return { success: true };
                } else {
                    // Got an error message instead of PDF
                    const errorText = await response.text();
                    console.error(`Server returned error instead of PDF: ${errorText}`);
                    return { success: false, error: errorText };
                }
            } else {
                console.error(`HTTP error: ${response.status} ${response.statusText}`);
                return { success: false, error: `HTTP ${response.status}` };
            }
            
        } catch (error) {
            console.error(`Network error downloading receipt:`, error);
            return { success: false, error: error.message };
        }
    }

    showReceiptDownloadSuccess(orderId, type = 'Receipt') {
        // Show a subtle success notification for PDF download
        const notification = document.createElement('div');
        notification.className = 'receipt-download-success';
        notification.innerHTML = `<i class="fas fa-file-pdf"></i> ${type} downloaded successfully`;
        notification.style.cssText = `
            position: fixed;
            top: 20px;
            right: 20px;
            background: #28a745;
            color: white;
            padding: 10px 20px;
            border-radius: 5px;
            z-index: 9999;
            box-shadow: 0 4px 8px rgba(0,0,0,0.1);
        `;
        
        document.body.appendChild(notification);
        
        setTimeout(() => {
            notification.style.opacity = '0';
            notification.style.transition = 'opacity 0.3s';
            setTimeout(() => notification.remove(), 300);
        }, 3000);
    }

    showReceiptDownloadError(orderId, errorMessage = '') {
        const orderCard = document.querySelector(`[data-order-card="${orderId}"]`);
        if (!orderCard) return;

        const warningDiv = document.createElement('div');
        warningDiv.className = 'alert alert-warning receipt-warning';
        warningDiv.innerHTML = `
            <strong>Note:</strong> Payment successful, but receipt download failed. 
            ${errorMessage ? '<br><small>Error: ' + errorMessage + '</small>' : ''}
            <br>
            <a href="/cashier/orders/${orderId}/receipt" target="_blank" class="btn btn-sm btn-primary">Download Manually</a>
            <a href="/test/pdf/${orderId}" target="_blank" class="btn btn-sm btn-secondary ml-1">Test PDF</a>
        `;
        
        orderCard.appendChild(warningDiv);
        
        // Auto-hide after 10 seconds
        setTimeout(() => {
            if (warningDiv.parentNode) {
                warningDiv.style.opacity = '0';
                setTimeout(() => warningDiv.remove(), 300);
            }
        }, 10000);
    }

    showLoadingState(orderId) {
        const button = document.querySelector(`[data-action="checkout"][data-order-id="${orderId}"]`);
        if (button) {
            button.disabled = true;
            button.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Processing...';
            button.className = 'btn btn-warning';
        }
    }

    handlePaymentError(orderId, message) {
        this.removeLoadingState(orderId);
        this.showErrorMessage(orderId, message);
    }

    handleNetworkError(orderId, error) {
        this.removeLoadingState(orderId);
        this.showErrorMessage(orderId, 'Network error. Please try again.');
    }

    removeLoadingState(orderId) {
        const button = document.querySelector(`[data-action="checkout"][data-order-id="${orderId}"]`);
        if (button) {
            button.disabled = false;
            button.innerHTML = 'Checkout';
            button.className = 'btn btn-primary';
        }
    }

    showErrorMessage(orderId, message) {
        const orderCard = document.querySelector(`[data-order-card="${orderId}"]`);
        if (!orderCard) return;

        const errorDiv = document.createElement('div');
        errorDiv.className = 'alert alert-danger';
        errorDiv.innerHTML = `<strong>Error:</strong> ${message}`;
        
        orderCard.appendChild(errorDiv);
        
        setTimeout(() => {
            if (errorDiv.parentNode) {
                errorDiv.remove();
            }
        }, 5000);
    }

    removeMessages(orderId) {
        const orderCard = document.querySelector(`[data-order-card="${orderId}"]`);
        if (orderCard) {
            const messages = orderCard.querySelectorAll('.alert');
            messages.forEach(msg => msg.remove());
        }
    }

    setupPeriodicStatusCheck() {
        // Refresh order board every 30 seconds
        setInterval(() => {
            this.refreshOrderBoard();
        }, 30000);
    }

    async refreshOrderBoard() {
        try {
            const response = await fetch('/cashier/orders', {
                headers: {
                    'X-Requested-With': 'XMLHttpRequest'
                }
            });
            
            if (response.ok) {
                const orders = await response.json();
                this.updateOrderBoard(orders);
            }
        } catch (error) {
            console.error('Error refreshing order board:', error);
        }
    }

    updateOrderBoard(orders) {
        // This would update the order board UI
        // Implementation depends on your specific HTML structure
        console.log('Orders updated:', orders.length);
    }
}

// Initialize the enhanced checkout system
new EnhancedCashierCheckout();
