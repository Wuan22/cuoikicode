package com.minimart.service;

import com.minimart.exception.ExpiredProductException;
import com.minimart.exception.InvalidProductException;
import com.minimart.exception.OutOfStockException;
import com.minimart.exception.ProductNotFoundException;
import com.minimart.model.CartItem;
import com.minimart.model.FoodProduct;
import com.minimart.model.Invoice;
import com.minimart.model.Product;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Nghiệp vụ bán hàng: giỏ hàng, thanh toán, hóa đơn, doanh thu. */
public class SalesService {
    private final ProductRepository repo;
    private final List<CartItem> cart = new ArrayList<>();
    private final List<Invoice> invoices = new ArrayList<>();
    private int invoiceCounter = 0;

    public SalesService(ProductRepository repo) {
        this.repo = repo;
    }

    public void addToCart(String productId, int qty)
            throws ProductNotFoundException, OutOfStockException, ExpiredProductException {
        if (qty <= 0) {
            throw new InvalidProductException("Số lượng phải lớn hơn 0.");
        }
        Product p = repo.findById(productId);
        if (p instanceof FoodProduct food && food.isExpired()) {
            throw new ExpiredProductException(p.getName());
        }
        CartItem existing = cart.stream()
                .filter(ci -> ci.getProduct().getId().equals(productId))
                .findFirst().orElse(null);
        int inCart = existing == null ? 0 : existing.getQuantity();
        if (inCart + qty > p.getQuantity()) {
            throw new OutOfStockException(p.getName(), inCart + qty, p.getQuantity());
        }
        if (existing != null) {
            existing.addQuantity(qty);
        } else {
            cart.add(new CartItem(p, qty));
        }
    }

    public void removeFromCart(int index) {
        cart.remove(index);
    }

    public void clearCart() {
        cart.clear();
    }

    public List<CartItem> getCart() {
        return Collections.unmodifiableList(cart);
    }

    public double getCartTotal() {
        return cart.stream().mapToDouble(CartItem::getTotal).sum();
    }

    /** Thanh toán: kiểm tra lại tồn kho, trừ kho, tạo hóa đơn, xóa giỏ. */
    public Invoice checkout(String customerName) throws OutOfStockException {
        if (cart.isEmpty()) {
            throw new IllegalStateException("Giỏ hàng đang trống.");
        }
        for (CartItem ci : cart) {
            Product p = ci.getProduct();
            if (ci.getQuantity() > p.getQuantity()) {
                throw new OutOfStockException(p.getName(), ci.getQuantity(), p.getQuantity());
            }
        }
        cart.forEach(ci -> ci.getProduct().setQuantity(ci.getProduct().getQuantity() - ci.getQuantity()));
        String name = (customerName == null || customerName.isBlank()) ? "Khách lẻ" : customerName.trim();
        Invoice inv = new Invoice(String.format("HD%03d", ++invoiceCounter), name, cart);
        invoices.add(inv);
        cart.clear();
        return inv;
    }

    public List<Invoice> getInvoices() {
        return Collections.unmodifiableList(invoices);
    }

    public double getTotalRevenue() {
        return invoices.stream().mapToDouble(Invoice::getTotal).sum();
    }
}
