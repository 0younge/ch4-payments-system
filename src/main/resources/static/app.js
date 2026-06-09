let savedCustomer = {};
try {
    savedCustomer = JSON.parse(localStorage.getItem("paymentCustomer") || "{}");
} catch {
    savedCustomer = {};
}

let paymentConfig = {};

const state = {
    token: localStorage.getItem("accessToken") || "",
    customer: savedCustomer,
    products: [],
    cart: null,
    selectedCartItemIds: new Set(),
    lastOrder: null,
    orderPage: 1,
    toastTimer: null
};

const productImages = {
    TOP: [
        "https://images.unsplash.com/photo-1521572163474-6864f9cf17ab?auto=format&fit=crop&w=900&q=80",
        "https://images.unsplash.com/photo-1503341504253-dff4815485f1?auto=format&fit=crop&w=900&q=80"
    ],
    BOTTOM: [
        "https://images.unsplash.com/photo-1542272604-787c3835535d?auto=format&fit=crop&w=900&q=80",
        "https://images.unsplash.com/photo-1475178626620-a4d074967452?auto=format&fit=crop&w=900&q=80"
    ],
    OUTER: [
        "https://images.unsplash.com/photo-1543076447-215ad9ba6923?auto=format&fit=crop&w=900&q=80",
        "https://images.unsplash.com/photo-1551028719-00167b16eac5?auto=format&fit=crop&w=900&q=80"
    ],
    SHOES: [
        "https://images.unsplash.com/photo-1542291026-7eec264c27ff?auto=format&fit=crop&w=900&q=80",
        "https://images.unsplash.com/photo-1549298916-b41d501d3772?auto=format&fit=crop&w=900&q=80"
    ],
    BAG: [
        "https://images.unsplash.com/photo-1594223274512-ad4803739b7c?auto=format&fit=crop&w=900&q=80",
        "https://images.unsplash.com/photo-1587836374828-4dbafa94cf0e?auto=format&fit=crop&w=900&q=80"
    ],
    DEFAULT: [
        "https://images.unsplash.com/photo-1496747611176-843222e1e57c?auto=format&fit=crop&w=900&q=80"
    ]
};

const $ = (id) => document.getElementById(id);

function escapeHtml(value) {
    return String(value ?? "")
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll("\"", "&quot;")
        .replaceAll("'", "&#039;");
}

function money(value) {
    return `${Number(value || 0).toLocaleString()}원`;
}

function formatDate(value) {
    if (!value) {
        return "";
    }
    return String(value).replace("T", " ").slice(0, 16);
}

function value(id) {
    return $(id).value.trim();
}

function numberValue(id, fallback = 0) {
    const text = value(id);
    return text === "" ? fallback : Number(text);
}

function queryString(params) {
    const search = new URLSearchParams();
    Object.entries(params).forEach(([key, val]) => {
        if (val !== undefined && val !== null && val !== "") {
            search.set(key, val);
        }
    });
    const text = search.toString();
    return text ? `?${text}` : "";
}

function cartItemQuery(ids) {
    if (ids.length === 0) {
        return "";
    }
    const search = new URLSearchParams();
    ids.forEach((id) => search.append("cartItemIds", String(id)));
    return `?${search.toString()}`;
}

function imageFor(product) {
    const images = productImages[product?.category] || productImages.DEFAULT;
    const index = Math.abs(Number(product?.productId || 0)) % images.length;
    return images[index];
}

function statusPill(valueText) {
    const text = escapeHtml(valueText || "UNKNOWN");
    const good = ["ON_SALE", "PAID", "COMPLETED"];
    const bad = ["SOLD_OUT", "FAILED", "CANCELLED", "REFUNDED"];
    const kind = good.includes(valueText) ? "good" : bad.includes(valueText) ? "bad" : "";
    return `<span class="pill ${kind}">${text}</span>`;
}

function showToast(message, type = "success") {
    const toast = $("toast");
    toast.textContent = message;
    toast.className = `toast show ${type === "error" ? "error" : ""}`;
    clearTimeout(state.toastTimer);
    state.toastTimer = setTimeout(() => {
        toast.className = "toast";
    }, 2600);
}

function setFeedback(title, payload) {
    const feedback = $("api-feedback");
    if (feedback) {
        feedback.textContent = `${title}\n${JSON.stringify(payload, null, 2)}`;
    }
}

function updateAuthStatus() {
    $("auth-status").textContent = state.token ? "로그인됨" : "로그아웃";
    $("logout-button").disabled = !state.token;
}

function fillPaymentCustomerFields() {
    $("customer-email").value = state.customer.email || "";
    $("customer-name").value = state.customer.fullName || "테스트회원";
    $("customer-phone").value = state.customer.phoneNumber || "01012345678";
}

function savePaymentCustomer(customer) {
    state.customer = {
        ...state.customer,
        ...customer
    };
    localStorage.setItem("paymentCustomer", JSON.stringify(state.customer));
    fillPaymentCustomerFields();
}

function paymentCustomer() {
    const customer = {
        email: value("customer-email"),
        fullName: value("customer-name"),
        phoneNumber: value("customer-phone")
    };
    if (!customer.email) {
        throw new Error("구매자 이메일을 입력해 주세요.");
    }
    savePaymentCustomer(customer);
    return customer;
}

function requireLogin() {
    if (state.token) {
        return true;
    }
    showToast("로그인이 필요한 기능입니다.", "error");
    location.hash = "top";
    return false;
}

async function api(name, method, path, body, options = {}) {
    const headers = {
        ...(options.raw ? {} : { "Content-Type": "application/json" }),
        ...(options.headers || {})
    };
    if (options.auth !== false && state.token) {
        headers.Authorization = `Bearer ${state.token}`;
    }
    let response;
    try {
        response = await fetch(path, {
            method,
            headers,
            body: body === undefined ? undefined : options.raw ? body : JSON.stringify(body)
        });
    } catch {
        throw new Error("서버에 연결할 수 없습니다. ./gradlew bootRun 실행 후 http://localhost:8080/로 접속해 주세요.");
    }
    const text = await response.text();
    let payload = null;
    try {
        payload = text ? JSON.parse(text) : null;
    } catch {
        payload = { raw: text };
    }
    setFeedback(`${method} ${path} ${response.status}`, payload);
    if (!response.ok || payload?.success === false) {
        throw new Error(payload?.message || `${name} 요청에 실패했습니다.`);
    }
    return payload?.data;
}

async function loadPaymentConfig() {
    paymentConfig = await api("결제 설정", "GET", "/api/payments/config", undefined, { auth: false });
}

function empty(message) {
    return `<div class="empty">${escapeHtml(message)}</div>`;
}

function renderProductDetail(product) {
    if (!product) {
        $("product-detail").innerHTML = empty("상품을 선택하면 상세 정보가 표시됩니다.");
        return;
    }
    const soldOut = product.status !== "ON_SALE" || Number(product.stock || 0) <= 0;
    $("product-detail").innerHTML = `
        <img class="detail-image" src="${imageFor(product)}" alt="${escapeHtml(product.name)}">
        <div class="product-meta">
            <span class="badge">${escapeHtml(product.category)}</span>
            ${statusPill(product.status)}
        </div>
        <h3>${escapeHtml(product.name)}</h3>
        <p class="price">${money(product.price)}</p>
        <p class="detail-description">${escapeHtml(product.description || "상품 설명이 없습니다.")}</p>
        <p class="detail-description">남은 재고 ${Number(product.stock || 0).toLocaleString()}개</p>
        <button type="button" data-add-product="${product.productId}" ${soldOut ? "disabled" : ""}>${soldOut ? "품절" : "장바구니 담기"}</button>
    `;
}

function renderProducts(data) {
    state.products = data?.products || [];
    if (state.products.length === 0) {
        $("product-grid").innerHTML = empty("조건에 맞는 상품이 없습니다.");
        renderProductDetail(null);
        return;
    }
    $("product-grid").innerHTML = state.products.map((product) => {
        const soldOut = product.status !== "ON_SALE" || Number(product.stock || 0) <= 0;
        return `
            <article class="product-card">
                <img src="${imageFor(product)}" alt="${escapeHtml(product.name)}">
                <div class="product-body">
                    <div class="product-meta">
                        <span class="badge">${escapeHtml(product.category)}</span>
                        ${statusPill(product.status)}
                        <span>재고 ${Number(product.stock || 0).toLocaleString()}개</span>
                    </div>
                    <div class="product-title-row">
                        <h3>${escapeHtml(product.name)}</h3>
                        <p class="price">${money(product.price)}</p>
                    </div>
                    <div class="card-actions">
                        <button type="button" data-add-product="${product.productId}" ${soldOut ? "disabled" : ""}>${soldOut ? "품절" : "담기"}</button>
                        <button class="secondary-button" type="button" data-product-detail="${product.productId}">상세</button>
                    </div>
                </div>
            </article>
        `;
    }).join("");
    renderProductDetail(state.products[0]);
}

async function loadProducts(event) {
    event?.preventDefault();
    try {
        const data = await api("상품 목록 조회", "GET", `/api/products${queryString({
            category: value("product-category"),
            minPrice: value("product-min-price"),
            maxPrice: value("product-max-price"),
            status: value("product-status"),
            sort: value("product-sort"),
            page: 0,
            size: 12
        })}`, undefined, { auth: false });
        renderProducts(data);
        if (state.products[0]) {
            await showProductDetail(state.products[0].productId);
        }
    } catch (error) {
        $("product-grid").innerHTML = empty(error.message);
        renderProductDetail(null);
        throw error;
    }
}

async function showProductDetail(productId) {
    const data = await api("상품 상세 조회", "GET", `/api/products/${productId}`, undefined, { auth: false });
    renderProductDetail(data);
}

async function addCartItem(productId, quantity = 1) {
    if (!requireLogin()) {
        return;
    }
    await api("장바구니 담기", "POST", "/api/cart/items", {
        productId: Number(productId),
        quantity
    });
    showToast("장바구니에 담았습니다.");
    await refreshCart();
}

function selectedCartIds() {
    const items = state.cart?.items || [];
    const selected = items
        .filter((item) => state.selectedCartItemIds.has(item.cartItemId))
        .map((item) => item.cartItemId);
    return selected.length > 0 ? selected : items.map((item) => item.cartItemId);
}

function updateCartSummary() {
    const items = state.cart?.items || [];
    const ids = selectedCartIds();
    const selected = items.filter((item) => ids.includes(item.cartItemId));
    const total = selected.reduce((sum, item) => sum + Number(item.itemTotalAmount || 0), 0);
    $("selected-count").textContent = `${selected.length}개`;
    $("selected-total").textContent = money(total);
}

function renderCart(cart) {
    state.cart = cart;
    const items = cart?.items || [];
    const validIds = new Set(items.map((item) => item.cartItemId));
    state.selectedCartItemIds.forEach((id) => {
        if (!validIds.has(id)) {
            state.selectedCartItemIds.delete(id);
        }
    });
    if (state.selectedCartItemIds.size === 0) {
        items.forEach((item) => state.selectedCartItemIds.add(item.cartItemId));
    }
    if (items.length === 0) {
        $("cart-list").innerHTML = empty("장바구니가 비어 있습니다.");
        updateCartSummary();
        return;
    }
    $("cart-list").innerHTML = items.map((item) => `
        <article class="cart-item">
            <input class="cart-select" type="checkbox" data-select-cart="${item.cartItemId}" ${state.selectedCartItemIds.has(item.cartItemId) ? "checked" : ""}>
            <div>
                <h3>${escapeHtml(item.productName)}</h3>
                <div class="cart-meta">
                    <span>상품 ID ${item.productId}</span>
                    <span>${money(item.price)}</span>
                    <strong>${money(item.itemTotalAmount)}</strong>
                </div>
            </div>
            <div class="cart-actions">
                <div class="quantity-control">
                    <button type="button" data-cart-dec="${item.cartItemId}">-</button>
                    <span>${item.quantity}</span>
                    <button type="button" data-cart-inc="${item.cartItemId}">+</button>
                </div>
                <button class="danger-button" type="button" data-cart-delete="${item.cartItemId}">삭제</button>
            </div>
        </article>
    `).join("");
    updateCartSummary();
}

async function refreshCart() {
    if (!state.token) {
        $("cart-list").innerHTML = empty("로그인하면 장바구니를 사용할 수 있습니다.");
        state.cart = null;
        updateCartSummary();
        return;
    }
    const data = await api("장바구니 조회", "GET", "/api/cart");
    renderCart(data);
}

async function updateCartQuantity(cartItemId, quantity) {
    if (quantity < 1) {
        return;
    }
    await api("장바구니 수량 변경", "PATCH", `/api/cart/items/${cartItemId}`, { quantity });
    await refreshCart();
}

async function deleteCartItem(cartItemId) {
    await api("장바구니 삭제", "DELETE", `/api/cart/items/${cartItemId}`);
    showToast("상품을 삭제했습니다.");
    await refreshCart();
}

async function clearCart() {
    if (!requireLogin()) {
        return;
    }
    await api("장바구니 비우기", "DELETE", "/api/cart/items");
    state.selectedCartItemIds.clear();
    showToast("장바구니를 비웠습니다.");
    await refreshCart();
}

function renderPreview(data) {
    const items = data?.items || [];
    if (items.length === 0) {
        $("order-preview").innerHTML = empty("미리보기할 상품이 없습니다.");
        return;
    }
    $("order-preview").innerHTML = `
        <div class="cart-list">
            ${items.map((item) => `
                <div class="preview-item">
                    <strong>${escapeHtml(item.productName)}</strong>
                    <div class="cart-meta">
                        <span>${money(item.price)}</span>
                        <span>${item.quantity}개</span>
                        <span>재고 ${item.stockQuantity}개</span>
                        <strong>${money(item.itemTotalAmount)}</strong>
                    </div>
                </div>
            `).join("")}
        </div>
        <div class="summary-row total"><span>총 상품 금액</span><strong>${money(data.totalAmount)}</strong></div>
    `;
}

async function previewOrder() {
    if (!requireLogin()) {
        return;
    }
    const ids = selectedCartIds();
    if (ids.length === 0) {
        showToast("장바구니에 상품이 없습니다.", "error");
        return;
    }
    const data = await api("주문서 미리보기", "GET", `/api/orders/preview${cartItemQuery(ids)}`);
    renderPreview(data);
}

function fillOrderFields(order) {
    state.lastOrder = order;
    $("payment-order-id").value = order?.orderId || "";
    $("payment-portone-id").value = order?.portonePaymentId || order?.payment?.portonePaymentId || "";
    $("refund-order-id").value = order?.orderId || "";
}

function renderLastOrder(order) {
    if (!order) {
        $("last-order").innerHTML = empty("주문 생성 후 결제창이 열립니다.");
        return;
    }
    $("last-order").innerHTML = `
        <h3>${escapeHtml(order.orderNumber || `주문 ${order.orderId}`)}</h3>
        <div class="order-meta">
            ${statusPill(order.orderStatus)}
            ${statusPill(order.paymentStatus || order.payment?.paymentStatus)}
        </div>
        <div class="summary-row"><span>상품 금액</span><strong>${money(order.totalAmount)}</strong></div>
        <div class="summary-row"><span>사용 포인트</span><strong>${money(order.usedPointAmount)}</strong></div>
        <div class="summary-row total"><span>PG 결제 금액</span><strong>${money(order.pgAmount)}</strong></div>
        <p class="detail-description">PortOne Payment ID: ${escapeHtml(order.portonePaymentId || order.payment?.portonePaymentId || "")}</p>
    `;
}

function orderName(order) {
    const items = order?.items || [];
    if (items.length === 0) {
        return order?.orderNumber || "CH4 Store 주문";
    }
    const firstName = items[0].productName;
    return items.length === 1 ? firstName : `${firstName} 외 ${items.length - 1}건`;
}

async function confirmPaymentByIds(orderId, portonePaymentId) {
    const data = await api("결제 확정", "POST", "/api/payments/confirm", {
        orderId: Number(orderId),
        portonePaymentId
    });
    renderLastOrder(data);
    showToast("결제 확정이 완료되었습니다.");
    await refreshMyPage();
}

function sleep(ms) {
    return new Promise((resolve) => setTimeout(resolve, ms));
}

async function confirmPaymentWithRetry(orderId, portonePaymentId) {
    let lastError;
    for (let count = 0; count < 5; count += 1) {
        try {
            await confirmPaymentByIds(orderId, portonePaymentId);
            return;
        } catch (error) {
            lastError = error;
            if (!error.message.includes("결제가 완료되지 않았습니다.")) {
                throw error;
            }
            await sleep(1000);
        }
    }
    throw lastError;
}

async function openPaymentWindow(order = state.lastOrder) {
    if (!requireLogin()) {
        return;
    }
    if (!order?.orderId || !order?.portonePaymentId) {
        showToast("먼저 주문을 생성해 주세요.", "error");
        return;
    }
    if (Number(order.pgAmount || 0) <= 0) {
        await confirmPaymentByIds(order.orderId, order.portonePaymentId);
        return;
    }
    if (!window.PortOne?.requestPayment) {
        throw new Error("PortOne SDK를 불러오지 못했습니다. 인터넷 연결을 확인해 주세요.");
    }
    if (!paymentConfig.storeId || !paymentConfig.channelKey) {
        throw new Error("PortOne 결제 설정을 불러오지 못했습니다.");
    }
    const customer = paymentCustomer();
    const response = await window.PortOne.requestPayment({
        storeId: paymentConfig.storeId,
        channelKey: paymentConfig.channelKey,
        paymentId: order.portonePaymentId,
        orderName: orderName(order),
        totalAmount: Number(order.pgAmount),
        currency: "CURRENCY_KRW",
        payMethod: "CARD",
        customer: {
            customerId: customer.email,
            email: customer.email,
            fullName: customer.fullName,
            phoneNumber: customer.phoneNumber
        },
        redirectUrl: `${location.origin}${location.pathname}?orderId=${order.orderId}&paymentId=${encodeURIComponent(order.portonePaymentId)}`
    });
    if (response?.code) {
        throw new Error(response.message || "결제창에서 결제가 실패했습니다.");
    }
    await confirmPaymentWithRetry(order.orderId, response?.paymentId || order.portonePaymentId);
}

async function createOrder() {
    if (!requireLogin()) {
        return;
    }
    const ids = selectedCartIds();
    if (ids.length === 0) {
        showToast("주문할 장바구니 상품이 없습니다.", "error");
        return;
    }
    const data = await api("주문 생성", "POST", "/api/orders", {
        cartItemIds: ids,
        usedPointAmount: numberValue("used-point", 0)
    });
    fillOrderFields(data);
    renderLastOrder(data);
    showToast("주문이 생성되었습니다.");
    try {
        await openPaymentWindow(data);
    } finally {
        await refreshCart();
        await refreshMyPage();
    }
}

async function confirmPayment(event) {
    event.preventDefault();
    if (!requireLogin()) {
        return;
    }
    await confirmPaymentByIds(numberValue("payment-order-id"), value("payment-portone-id"));
}

function renderPoints(balance, histories) {
    $("point-balance").textContent = `${Number(balance?.pointBalance || 0).toLocaleString()} P`;
    const rows = histories?.histories || [];
    $("point-histories").innerHTML = rows.length === 0 ? empty("포인트 내역이 없습니다.") : rows.map((item) => `
        <div class="history-item">
            <strong>${escapeHtml(item.pointType)}</strong>
            <div class="cart-meta">
                <span>${Number(item.amount || 0).toLocaleString()} P</span>
                <span>잔액 ${Number(item.balanceAfter || 0).toLocaleString()} P</span>
                <span>${formatDate(item.createdAt)}</span>
            </div>
            <p class="detail-description">${escapeHtml(item.description || "")}</p>
        </div>
    `).join("");
}

function renderOrders(data) {
    const orders = data?.content || [];
    if (orders.length === 0) {
        $("order-list").innerHTML = empty("주문 내역이 없습니다.");
        return;
    }
    $("order-list").innerHTML = orders.map((order) => `
        <article class="order-item">
            <div class="order-top">
                <div>
                    <h3>${escapeHtml(order.orderNumber)}</h3>
                    <div class="order-meta">
                        ${statusPill(order.orderStatus)}
                        ${statusPill(order.paymentStatus)}
                        <span>${formatDate(order.createdAt)}</span>
                    </div>
                </div>
                <strong>${money(order.totalAmount)}</strong>
            </div>
            <div class="summary-row"><span>PG 결제</span><strong>${money(order.pgAmount)}</strong></div>
            <div class="summary-row"><span>사용 포인트</span><strong>${money(order.usedPointAmount)}</strong></div>
            <div class="order-actions">
                <button type="button" data-order-detail="${order.orderId}">상세</button>
                <button class="danger-button" type="button" data-order-cancel="${order.orderId}">주문 취소</button>
                <button class="secondary-button" type="button" data-order-refund="${order.orderId}">환불 준비</button>
            </div>
        </article>
    `).join("");
}

function renderOrderDetail(order) {
    if (!order) {
        $("order-detail").innerHTML = empty("주문을 선택하면 상세 정보가 표시됩니다.");
        return;
    }
    fillOrderFields(order);
    const firstItem = order.items?.[0];
    if (firstItem) {
        $("refund-order-item-id").value = firstItem.orderItemId;
    }
    $("order-detail").innerHTML = `
        <h3>${escapeHtml(order.orderNumber)}</h3>
        <div class="order-meta">
            ${statusPill(order.orderStatus)}
            ${statusPill(order.payment?.paymentStatus)}
            <span>${formatDate(order.createdAt)}</span>
        </div>
        <div class="summary-row"><span>총 금액</span><strong>${money(order.totalAmount)}</strong></div>
        <div class="summary-row"><span>PG 금액</span><strong>${money(order.pgAmount)}</strong></div>
        <div class="summary-row"><span>적립 포인트</span><strong>${Number(order.savedPointAmount || 0).toLocaleString()} P</strong></div>
        <div class="cart-list">
            ${(order.items || []).map((item) => `
                <div class="preview-item">
                    <strong>${escapeHtml(item.productName)}</strong>
                    <div class="cart-meta">
                        <span>주문 상품 ID ${item.orderItemId}</span>
                        <span>${money(item.productPrice)}</span>
                        <span>${item.quantity}개</span>
                        <strong>${money(item.itemTotalAmount)}</strong>
                    </div>
                </div>
            `).join("")}
        </div>
    `;
}

async function refreshMyPage() {
    if (!state.token) {
        $("point-balance").textContent = "로그인이 필요합니다";
        $("point-histories").innerHTML = empty("로그인 후 포인트 내역을 확인할 수 있습니다.");
        $("order-list").innerHTML = empty("로그인 후 주문 내역을 확인할 수 있습니다.");
        renderOrderDetail(null);
        return;
    }
    const [balance, histories, orders] = await Promise.all([
        api("포인트 잔액", "GET", "/api/points/balance"),
        api("포인트 내역", "GET", "/api/points/histories?page=0&size=10"),
        api("주문 내역", "GET", `/api/orders${queryString({
            status: value("order-status-filter"),
            page: state.orderPage,
            size: 10
        })}`)
    ]);
    renderPoints(balance, histories);
    renderOrders(orders);
}

async function showOrderDetail(orderId) {
    if (!requireLogin()) {
        return;
    }
    const data = await api("주문 상세", "GET", `/api/orders/${orderId}`);
    renderOrderDetail(data);
}

async function cancelOrder(orderId) {
    if (!requireLogin()) {
        return;
    }
    const data = await api("주문 취소", "POST", `/api/orders/${orderId}/cancel`, {
        reason: "사용자 요청"
    });
    showToast("주문을 취소했습니다.");
    renderLastOrder(data);
    await refreshMyPage();
}

async function requestRefund(event) {
    event.preventDefault();
    if (!requireLogin()) {
        return;
    }
    const data = await api("환불 요청", "POST", "/api/refunds", {
        orderId: numberValue("refund-order-id"),
        refundReason: value("refund-reason"),
        refundItems: [{
            orderItemId: numberValue("refund-order-item-id"),
            quantity: numberValue("refund-quantity", 1)
        }]
    });
    $("refund-result").innerHTML = `
        <h3>환불 ${escapeHtml(data.refundStatus)}</h3>
        <div class="summary-row"><span>총 환불</span><strong>${money(data.totalRefundAmount)}</strong></div>
        <div class="summary-row"><span>PG 환불</span><strong>${money(data.pgRefundAmount)}</strong></div>
        <div class="summary-row"><span>포인트 환불</span><strong>${money(data.pointRefundAmount)}</strong></div>
    `;
    showToast("환불 요청을 처리했습니다.");
    await refreshMyPage();
}

async function signup(event) {
    event.preventDefault();
    const email = value("signup-email");
    const password = value("signup-password");
    const name = value("signup-name");
    const phoneNumber = value("signup-phone");
    await api("회원가입", "POST", "/api/auth/signup", {
        email,
        password,
        name,
        phoneNumber
    }, { auth: false });
    await loginWith(email, password, {
        email,
        fullName: name,
        phoneNumber
    });
}

async function loginWith(email, password, customer = {}) {
    const data = await api("로그인", "POST", "/api/auth/login", {
        email,
        password
    }, { auth: false });
    state.token = data.accessToken;
    localStorage.setItem("accessToken", state.token);
    savePaymentCustomer({
        email,
        ...customer
    });
    updateAuthStatus();
    showToast("로그인되었습니다.");
    await Promise.all([
        refreshCart(),
        refreshMyPage()
    ]);
}

async function login(event) {
    event.preventDefault();
    await loginWith(value("login-email"), value("login-password"));
}

function logout() {
    state.token = "";
    localStorage.removeItem("accessToken");
    localStorage.removeItem("paymentCustomer");
    state.customer = {};
    state.cart = null;
    state.selectedCartItemIds.clear();
    fillPaymentCustomerFields();
    updateAuthStatus();
    renderCart({ items: [], totalAmount: 0 });
    refreshMyPage();
    showToast("로그아웃되었습니다.");
}

function fillRandomUser() {
    const email = `tester-${Date.now()}@test.com`;
    $("signup-email").value = email;
    $("login-email").value = email;
    if (!state.customer.email && !state.token) {
        savePaymentCustomer({
            email,
            fullName: value("signup-name"),
            phoneNumber: value("signup-phone")
        });
    } else {
        fillPaymentCustomerFields();
    }
}

function switchAuthTab(tab) {
    document.querySelectorAll("[data-auth-tab]").forEach((button) => {
        button.classList.toggle("active", button.dataset.authTab === tab);
    });
    document.querySelectorAll("[data-auth-panel]").forEach((panel) => {
        panel.classList.toggle("hidden", panel.dataset.authPanel !== tab);
    });
}

function bindEvents() {
    $("product-filter-form").addEventListener("submit", (event) => {
        loadProducts(event).catch((error) => showToast(error.message, "error"));
    });
    $("signup-form").addEventListener("submit", (event) => {
        signup(event).catch((error) => showToast(error.message, "error"));
    });
    $("login-form").addEventListener("submit", (event) => {
        login(event).catch((error) => showToast(error.message, "error"));
    });
    $("logout-button").addEventListener("click", logout);
    $("cart-refresh-button").addEventListener("click", () => {
        refreshCart().catch((error) => showToast(error.message, "error"));
    });
    $("cart-clear-button").addEventListener("click", () => {
        clearCart().catch((error) => showToast(error.message, "error"));
    });
    $("preview-order-button").addEventListener("click", () => {
        previewOrder().catch((error) => showToast(error.message, "error"));
    });
    $("create-order-button").addEventListener("click", () => {
        createOrder().catch((error) => showToast(error.message, "error"));
    });
    $("payment-confirm-form").addEventListener("submit", (event) => {
        confirmPayment(event).catch((error) => showToast(error.message, "error"));
    });
    $("payment-window-button").addEventListener("click", () => {
        openPaymentWindow().catch((error) => showToast(error.message, "error"));
    });
    $("my-refresh-button").addEventListener("click", () => {
        refreshMyPage().catch((error) => showToast(error.message, "error"));
    });
    $("order-status-filter").addEventListener("change", () => {
        refreshMyPage().catch((error) => showToast(error.message, "error"));
    });
    $("refund-form").addEventListener("submit", (event) => {
        requestRefund(event).catch((error) => showToast(error.message, "error"));
    });
    document.querySelectorAll("[data-auth-tab]").forEach((button) => {
        button.addEventListener("click", () => switchAuthTab(button.dataset.authTab));
    });
    document.addEventListener("click", (event) => {
        const target = event.target;
        if (!(target instanceof HTMLElement)) {
            return;
        }
        const run = (promise) => promise.catch((error) => showToast(error.message, "error"));
        if (target.dataset.addProduct) {
            run(addCartItem(target.dataset.addProduct));
        }
        if (target.dataset.productDetail) {
            run(showProductDetail(target.dataset.productDetail));
        }
        if (target.dataset.cartDelete) {
            run(deleteCartItem(target.dataset.cartDelete));
        }
        if (target.dataset.cartInc || target.dataset.cartDec) {
            const id = Number(target.dataset.cartInc || target.dataset.cartDec);
            const item = state.cart?.items?.find((cartItem) => cartItem.cartItemId === id);
            if (item) {
                const quantity = target.dataset.cartInc ? item.quantity + 1 : item.quantity - 1;
                run(updateCartQuantity(id, quantity));
            }
        }
        if (target.dataset.orderDetail) {
            run(showOrderDetail(target.dataset.orderDetail));
        }
        if (target.dataset.orderCancel) {
            run(cancelOrder(target.dataset.orderCancel));
        }
        if (target.dataset.orderRefund) {
            run(showOrderDetail(target.dataset.orderRefund).then(() => {
                location.hash = "mypage";
                $("refund-reason").focus();
            }));
        }
    });
    document.addEventListener("change", (event) => {
        const target = event.target;
        if (!(target instanceof HTMLInputElement)) {
            return;
        }
        if (target.dataset.selectCart) {
            const id = Number(target.dataset.selectCart);
            if (target.checked) {
                state.selectedCartItemIds.add(id);
            } else {
                state.selectedCartItemIds.delete(id);
            }
            updateCartSummary();
        }
    });
}

async function init() {
    await loadPaymentConfig();
    fillRandomUser();
    fillPaymentCustomerFields();
    updateAuthStatus();
    renderProductDetail(null);
    renderLastOrder(null);
    renderOrderDetail(null);
    bindEvents();
    await handlePaymentRedirect();
    await loadProducts();
    await refreshCart();
    await refreshMyPage();
}

async function handlePaymentRedirect() {
    const params = new URLSearchParams(location.search);
    const orderId = params.get("orderId");
    const paymentId = params.get("paymentId");
    const code = params.get("code");
    const message = params.get("message");
    if (code) {
        showToast(message || "결제가 실패했습니다.", "error");
        return;
    }
    if (!orderId || !paymentId || !state.token) {
        return;
    }
    await confirmPaymentByIds(orderId, paymentId);
    history.replaceState({}, document.title, location.pathname);
}

document.addEventListener("DOMContentLoaded", () => {
    init().catch((error) => showToast(error.message, "error"));
});
