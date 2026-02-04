// Use relative path when served from same origin, or absolute for standalone
const API_BASE_URL = '/api';

// Global state
let allAccounts = [];
let currentTransactionTab = 'deposit';
let confirmCallback = null;

// Theme management
function toggleTheme() {
    const currentTheme = document.documentElement.getAttribute('data-theme');
    const newTheme = currentTheme === 'dark' ? 'light' : 'dark';
    document.documentElement.setAttribute('data-theme', newTheme);
    localStorage.setItem('theme', newTheme);
    
    const themeIcon = document.querySelector('.theme-toggle i');
    themeIcon.className = newTheme === 'dark' ? 'fas fa-sun' : 'fas fa-moon';
}

// Initialize theme
function initTheme() {
    const savedTheme = localStorage.getItem('theme') || 'light';
    document.documentElement.setAttribute('data-theme', savedTheme);
    const themeIcon = document.querySelector('.theme-toggle i');
    if (themeIcon) {
        themeIcon.className = savedTheme === 'dark' ? 'fas fa-sun' : 'fas fa-moon';
    }
}

// Tab management
function showTab(tabName) {
    // Hide all tabs
    document.querySelectorAll('.tab-content').forEach(tab => {
        tab.classList.remove('active');
    });
    document.querySelectorAll('.nav-item').forEach(item => {
        item.classList.remove('active');
    });

    // Show selected tab
    document.getElementById(`${tabName}-tab`).classList.add('active');
    const navItems = document.querySelectorAll('.nav-item');
    navItems.forEach((item, index) => {
        const tabs = ['dashboard', 'accounts', 'transactions', 'transfer'];
        if (tabs[index] === tabName) {
            item.classList.add('active');
        }
    });

    // Load data when switching tabs
    if (tabName === 'dashboard') {
        loadDashboard();
    } else if (tabName === 'accounts') {
        loadAccounts();
    }
}

// Toast notification
function showToast(message, type = 'info') {
    const toast = document.getElementById('toast');
    const toastIcon = toast.querySelector('.toast-icon');
    const toastMessage = toast.querySelector('.toast-message');
    
    const icons = {
        success: 'fa-check-circle',
        error: 'fa-exclamation-circle',
        info: 'fa-info-circle'
    };
    
    toastIcon.className = `toast-icon fas ${icons[type]}`;
    toastMessage.textContent = message;
    toast.className = `toast ${type} show`;
    
    setTimeout(() => {
        toast.classList.remove('show');
    }, 5000);
}

function hideToast() {
    document.getElementById('toast').classList.remove('show');
}

// Loading overlay
function showLoading() {
    document.getElementById('loadingOverlay').classList.add('show');
}

function hideLoading() {
    document.getElementById('loadingOverlay').classList.remove('show');
}

// Confirmation modal
function showConfirmModal(title, message, callback) {
    document.getElementById('confirmTitle').textContent = title;
    document.getElementById('confirmMessage').textContent = message;
    document.getElementById('confirmModal').classList.add('show');
    confirmCallback = callback;
}

function hideConfirmModal() {
    document.getElementById('confirmModal').classList.remove('show');
    confirmCallback = null;
}

function confirmAction() {
    if (confirmCallback) {
        confirmCallback();
    }
    hideConfirmModal();
}

// API Helper
async function apiCall(endpoint, method = 'GET', body = null) {
    try {
        const options = {
            method,
            headers: {
                'Content-Type': 'application/json',
            }
        };
        if (body) {
            options.body = JSON.stringify(body);
        }
        const response = await fetch(`${API_BASE_URL}${endpoint}`, options);
        if (!response.ok) {
            const errorText = await response.text();
            let errorMessage = `HTTP error! status: ${response.status}`;
            try {
                const errorJson = JSON.parse(errorText);
                errorMessage = errorJson.message || errorText;
            } catch {
                errorMessage = errorText || errorMessage;
            }
            throw new Error(errorMessage);
        }
        return await response.json();
    } catch (error) {
        showToast(`Error: ${error.message}`, 'error');
        throw error;
    }
}

// Format currency
function formatCurrency(amount, currency = 'USD') {
    return new Intl.NumberFormat('en-US', {
        style: 'currency',
        currency: currency
    }).format(amount);
}

// Format account ID (short version)
function formatAccountId(accountId) {
    return accountId ? `${accountId.substring(0, 4)}-${accountId.substring(4, 8)}-${accountId.substring(8, 12)}-${accountId.substring(12, 16)}` : '';
}

// Populate account dropdowns
function populateAccountDropdowns() {
    const selects = ['depositAccountId', 'withdrawAccountId', 'fromAccountId', 'toAccountId', 'historyAccountId'];
    selects.forEach(selectId => {
        const select = document.getElementById(selectId);
        if (select) {
            const currentValue = select.value;
            select.innerHTML = '<option value="">Select account</option>';
            allAccounts.forEach(account => {
                const option = document.createElement('option');
                option.value = account.accountId;
                option.textContent = `${formatAccountId(account.accountId)} - ${account.accountType} (${formatCurrency(account.balance, account.currency)})`;
                if (account.accountId === currentValue) {
                    option.selected = true;
                }
                select.appendChild(option);
            });
        }
    });
}

// Load Dashboard
async function loadDashboard() {
    try {
        showLoading();
        const accounts = await apiCall('/accounts');
        allAccounts = accounts;
        
        // Calculate stats
        const totalAccounts = accounts.length;
        const activeAccounts = accounts.filter(acc => acc.active).length;
        const totalBalance = accounts.reduce((sum, acc) => sum + acc.balance, 0);
        
        // Update header stats
        document.getElementById('headerTotalBalance').textContent = formatCurrency(totalBalance);
        document.getElementById('headerAccountCount').textContent = totalAccounts;
        
        // Update dashboard stats
        document.getElementById('totalAccounts').textContent = totalAccounts;
        document.getElementById('activeAccounts').textContent = activeAccounts;
        document.getElementById('totalBalance').textContent = formatCurrency(totalBalance);
        document.getElementById('totalTransactions').textContent = 'N/A'; // Could be calculated if needed
        
        // Display recent accounts
        const dashboardAccounts = document.getElementById('dashboardAccounts');
        if (accounts.length === 0) {
            dashboardAccounts.innerHTML = `
                <div class="empty-state">
                    <i class="fas fa-wallet"></i>
                    <p>No accounts found. Create your first account to get started.</p>
                </div>
            `;
        } else {
            dashboardAccounts.innerHTML = '';
            accounts.slice(0, 4).forEach(account => {
                const accountCard = document.createElement('div');
                accountCard.className = 'account-card';

                const accountType = document.createElement('span');
                accountType.className = 'account-type';
                accountType.textContent = account.accountType;

                const accountStatus = document.createElement('span');
                accountStatus.className = `account-status ${account.active ? 'active' : 'inactive'}`;
                accountStatus.textContent = account.active ? 'Active' : 'Inactive';

                const accountBalanceLabel = document.createElement('div');
                accountBalanceLabel.className = 'account-balance-label';
                accountBalanceLabel.textContent = 'Available Balance';

                const accountBalanceAmount = document.createElement('div');
                accountBalanceAmount.className = 'account-balance-amount';
                accountBalanceAmount.textContent = formatCurrency(account.balance, account.currency);

                const accountId = document.createElement('div');
                accountId.className = 'account-id';
                const strongElement = document.createElement('strong');
                strongElement.textContent = 'Account:';
                accountId.appendChild(strongElement);
                accountId.appendChild(document.createTextNode(` ${formatAccountId(account.accountId)}`));

                accountCard.appendChild(accountType);
                accountCard.appendChild(accountStatus);
                accountCard.appendChild(accountBalanceLabel);
                accountCard.appendChild(accountBalanceAmount);
                accountCard.appendChild(accountId);

                dashboardAccounts.appendChild(accountCard);
            });
        }
        
        // Load recent activity (placeholder for now)
        const recentActivity = document.getElementById('recentActivity');
        recentActivity.innerHTML = `
            <div class="empty-state">
                <i class="fas fa-chart-line"></i>
                <p>No recent activity</p>
            </div>
        `;
        
    } catch (error) {
        console.error('Error loading dashboard:', error);
    } finally {
        hideLoading();
    }
}

// Account Management
document.getElementById('createAccountForm').addEventListener('submit', async (e) => {
    e.preventDefault();
    try {
        showLoading();
        const account = await apiCall('/accounts', 'POST', {
            customerId: document.getElementById('customerId').value,
            accountType: document.getElementById('accountType').value,
            initialBalance: parseFloat(document.getElementById('initialBalance').value),
            currency: document.getElementById('currency').value
        });
        showToast(`Account created successfully! Account: ${formatAccountId(account.accountId)}`, 'success');
        document.getElementById('createAccountForm').reset();
        await loadAccounts();
        await loadDashboard();
    } catch (error) {
        // Error already shown by apiCall
    } finally {
        hideLoading();
    }
});

async function loadAccounts() {
    try {
        showLoading();
        const accounts = await apiCall('/accounts');
        allAccounts = accounts;
        populateAccountDropdowns();
        
        const accountsList = document.getElementById('accountsList');
        if (accounts.length === 0) {
            accountsList.innerHTML = `
                <div class="empty-state">
                    <i class="fas fa-wallet"></i>
                    <p>No accounts found. Create your first account to get started.</p>
                </div>
            `;
            return;
        }
        
        displayFilteredAccounts(accounts);
    } catch (error) {
        console.error('Error loading accounts:', error);
    } finally {
        hideLoading();
    }
}

document.getElementById('customerAccountsForm').addEventListener('submit', async (e) => {
    e.preventDefault();
    try {
        const customerId = document.getElementById('customerIdSearch').value;
        const accounts = await apiCall(`/accounts/customer/${customerId}`);
        const customerAccountsList = document.getElementById('customerAccountsList');
        if (accounts.length === 0) {
            customerAccountsList.innerHTML = `
                <div class="empty-state">
                    <i class="fas fa-search"></i>
                    <p>No accounts found for customer ${customerId}.</p>
                </div>
            `;
            return;
        }
        customerAccountsList.innerHTML = '';
        accounts.forEach(account => {
            const accountCard = document.createElement('div');
            accountCard.className = 'account-card';

            const accountHeader = document.createElement('div');
            accountHeader.className = 'account-header';

            const accountType = document.createElement('span');
            accountType.className = 'account-type';
            accountType.textContent = account.accountType;

            const accountStatus = document.createElement('span');
            accountStatus.className = `account-status ${account.active ? 'active' : 'inactive'}`;
            accountStatus.textContent = account.active ? 'Active' : 'Inactive';

            accountHeader.appendChild(accountType);
            accountHeader.appendChild(accountStatus);

            const accountBalance = document.createElement('div');
            accountBalance.className = 'account-balance';

            const balanceLabel = document.createElement('div');
            balanceLabel.className = 'account-balance-label';
            balanceLabel.textContent = 'Available Balance';

            const balanceAmount = document.createElement('div');
            balanceAmount.className = 'account-balance-amount';
            balanceAmount.textContent = formatCurrency(account.balance, account.currency);

            accountBalance.appendChild(balanceLabel);
            accountBalance.appendChild(balanceAmount);

            const accountId = document.createElement('div');
            accountId.className = 'account-id';

            const accountIdLabel = document.createElement('strong');
            accountIdLabel.textContent = 'Account:';

            accountId.appendChild(accountIdLabel);
            accountId.appendChild(document.createTextNode(` ${formatAccountId(account.accountId)}`));

            accountCard.appendChild(accountHeader);
            accountCard.appendChild(accountBalance);
            accountCard.appendChild(accountId);

            customerAccountsList.appendChild(accountCard);
        });
    } catch (error) {
        // Error already shown by apiCall
    }
});

// Transaction Management
document.getElementById('depositForm').addEventListener('submit', async (e) => {
    e.preventDefault();
    try {
        showLoading();
        const accountId = document.getElementById('depositAccountId').value;
        const account = allAccounts.find(acc => acc.accountId === accountId);
        const currency = account ? account.currency : 'USD';
        
        const transaction = await apiCall('/transactions/deposit', 'POST', {
            accountId: accountId,
            amount: parseFloat(document.getElementById('depositAmount').value),
            currency: currency,
            description: document.getElementById('depositDescription').value
        });
        showToast(`Deposit successful! Amount: ${formatCurrency(transaction.amount, transaction.currency)}`, 'success');
        document.getElementById('depositForm').reset();
        await loadAccounts();
        await loadDashboard();
    } catch (error) {
        // Error already shown by apiCall
    } finally {
        hideLoading();
    }
});

document.getElementById('withdrawForm').addEventListener('submit', async (e) => {
    e.preventDefault();
    try {
        showLoading();
        const accountId = document.getElementById('withdrawAccountId').value;
        const account = allAccounts.find(acc => acc.accountId === accountId);
        const currency = account ? account.currency : 'USD';
        
        const transaction = await apiCall('/transactions/withdraw', 'POST', {
            accountId: accountId,
            amount: parseFloat(document.getElementById('withdrawAmount').value),
            currency: currency,
            description: document.getElementById('withdrawDescription').value
        });
        showToast(`Withdrawal successful! Amount: ${formatCurrency(transaction.amount, transaction.currency)}`, 'success');
        document.getElementById('withdrawForm').reset();
        await loadAccounts();
        await loadDashboard();
    } catch (error) {
        // Error already shown by apiCall
    } finally {
        hideLoading();
    }
});

document.getElementById('transferForm').addEventListener('submit', async (e) => {
    e.preventDefault();
    
    if (!validateTransferForm()) {
        return;
    }
    
    const fromAccountId = document.getElementById('fromAccountId').value;
    const toAccountId = document.getElementById('toAccountId').value;
    const amount = parseFloat(document.getElementById('transferAmount').value);
    const description = document.getElementById('transferDescription').value;
    
    showConfirmModal(
        'Confirm Transfer',
        `Transfer ${formatCurrency(amount)} from ${formatAccountId(fromAccountId)} to ${formatAccountId(toAccountId)}?`,


package com.banking.api.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.CorsRegistry;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/");
    }

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOrigins("*")
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        .allowedHeaders("*")
                        .exposedHeaders("Authorization")
                        .allowCredentials(true)
                        .maxAge(3600);
            }

            @Override
            public void addResourceHandlers(ResourceHandlerRegistry registry) {
                registry.addResourceHandler("/**")
                        .addResourceLocations("classpath:/static/")
                        .setCachePeriod(0)
                        .resourceChain(true);
            }
        };
    }

    @Bean
    public WebMvcConfigurer contentSecurityPolicyConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addResourceHandlers(ResourceHandlerRegistry registry) {
                registry.addResourceHandler("/**")
                        .addResourceLocations("classpath:/static/")
                        .setCachePeriod(0)
                        .resourceChain(true)
                        .addTransformer((request, response, chain) -> {
                            response.setHeader("Content-Security-Policy", 
                                "default-src 'self'; " +
                                "script-src 'self' 'unsafe-inline' 'unsafe-eval'; " +
                                "style-src 'self' 'unsafe-inline'; " +
                                "img-src 'self' data:; " +
                                "font-src 'self'; " +
                                "connect-src 'self'; " +
                                "frame-src 'none'; " +
                                "object-src 'none'; " +
                                "base-uri 'self'");
                            chain.transform(request, response);
                        });
            }
        };
    }
}


package com.banking.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BankingApiApplication {
    public static void main(String[] args) {
        SpringApplication.run(BankingApiApplication.class, args);
    }
}