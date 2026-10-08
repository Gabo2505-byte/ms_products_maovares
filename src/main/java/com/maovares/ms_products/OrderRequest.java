package com.maovares.ms_products;

import java.util.List;

public record OrderRequest(
        String orderId,
        String customerEmail,
        String customerName,
        Double total,
        List<OrderItem> items,
        String createdAt) {

    public record OrderItem(String sku, Integer qty) {}
}