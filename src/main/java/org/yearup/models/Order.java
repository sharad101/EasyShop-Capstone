package org.yearup.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Order
{
    private int orderId; // Will be auto-generated by the database
    private int userId;
    private LocalDateTime date; // Good for mapping to SQL DATETIME/TIMESTAMP
    private String address;
    private String city;
    private String state;
    private String zip;
    private BigDecimal shippingAmount = BigDecimal.ZERO; // Default value, can be overridden
    private BigDecimal orderTotal; // Added: Important to store the total of the order at time of checkout
    private List<OrderItem> lineItems = new ArrayList<>(); // Represents the associated order line items

    // Default constructor is implicitly provided if no other constructor is defined,
    // but it's good practice to make it explicit if you plan to add other constructors later.
    public Order() {
    }

    // --- Getters and Setters ---

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getZip() {
        return zip;
    }

    public void setZip(String zip) {
        this.zip = zip;
    }

    public BigDecimal getShippingAmount() {
        return shippingAmount;
    }

    public void setShippingAmount(BigDecimal shippingAmount) {
        this.shippingAmount = shippingAmount;
    }

    // NEW: Getter and Setter for orderTotal
    public BigDecimal getOrderTotal() {
        return orderTotal;
    }

    public void setOrderTotal(BigDecimal orderTotal) {
        this.orderTotal = orderTotal;
    }

    public List<OrderItem> getLineItems() {
        return lineItems;
    }

    public void setLineItems(List<OrderItem> lineItems) {
        this.lineItems = lineItems;
    }

    public void addLineItem(OrderItem item) {
        this.lineItems.add(item);
    }
}