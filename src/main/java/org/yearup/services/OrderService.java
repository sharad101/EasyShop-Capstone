package org.yearup.services; // New package for services

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Import this!
import org.yearup.data.OrderDao;
import org.yearup.data.ProfileDao;
import org.yearup.data.ShoppingCartDao;
import org.yearup.models.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

//Phase 5 Code
// Mark this as a Spring Service component
@Service
public class OrderService {
    private final ShoppingCartDao cartDao;
    private final OrderDao orderDao;
    private final ProfileDao profileDao;

    public OrderService(ShoppingCartDao cartDao, OrderDao orderDao, ProfileDao profileDao) {
        this.cartDao = cartDao;
        this.orderDao = orderDao;
        this.profileDao = profileDao;
    }

    @Transactional // This annotation ensures atomicity for all DB operations within this method
    public Order placeOrder(User user) {
        // All the logic from your OrdersController's checkout method goes here:
        int userId = user.getId();

        ShoppingCart cart = cartDao.getByUserId(userId);
        List<ShoppingCartItem> cartItems = new ArrayList<>(cart.getItems().values());

        if (cartItems.isEmpty()) {
            // Throw a specific exception that the controller can map to a HTTP status
            throw new IllegalArgumentException("Cannot checkout an empty cart."); // Or custom exception
        }

        Profile profile = profileDao.getByUserId(userId);
        if (profile == null) {
            throw new IllegalStateException("User profile not found. Cannot complete order without shipping details."); // Or custom exception
        }

        Order order = new Order();
        order.setUserId(userId);
        order.setDate(LocalDateTime.now());
        order.setAddress(profile.getAddress());
        order.setCity(profile.getCity());
        order.setState(profile.getState());
        order.setZip(profile.getZip());
        order.setShippingAmount(BigDecimal.ZERO);
        order.setOrderTotal(cart.getTotal()); // Important: Capture the final cart total

        Order savedOrder = orderDao.createOrder(order);

        for (ShoppingCartItem item : cartItems) {
            OrderItem lineItem = new OrderItem();
            lineItem.setOrderId(savedOrder.getOrderId());
            lineItem.setProductId(item.getProductId());
            lineItem.setSalesPrice(item.getPrice());
            lineItem.setQuantity(item.getQuantity());
            lineItem.setDiscount(item.getDiscountPercent());

            orderDao.addLineItem(lineItem);
        }

        cartDao.clearCart(userId); // Clear cart ONLY if all previous steps succeed

        return savedOrder;
    }
}