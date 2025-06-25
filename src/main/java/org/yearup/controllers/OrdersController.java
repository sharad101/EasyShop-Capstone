package org.yearup.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.yearup.data.OrderDao;
import org.yearup.data.ProfileDao;
import org.yearup.data.ShoppingCartDao;
import org.yearup.models.*;
//import org.yearup.models.security.User;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/orders")
public class OrdersController
{
    private final ShoppingCartDao cartDao;
    private final OrderDao orderDao;
    private final ProfileDao profileDao;

    public OrdersController(ShoppingCartDao cartDao, OrderDao orderDao, ProfileDao profileDao)
    {
        this.cartDao = cartDao;
        this.orderDao = orderDao;
        this.profileDao = profileDao;
    }

    // POST /orders – Convert cart to order
    @PostMapping
    public ResponseEntity<Order> checkout(@AuthenticationPrincipal User user)
    {
        int userId = user.getId();

        // Get the current user's cart
        ShoppingCart cart = cartDao.getByUserId(userId);

        // 1. Get cart items
        List<ShoppingCartItem> cartItems = new ArrayList<>(cart.getItems().values());

        if (cartItems.isEmpty())
        {
            return ResponseEntity.badRequest().build(); // nothing to checkout
        }


        // 2. Get profile for shipping info
        Profile profile = profileDao.getByUserId(userId);
        if (profile == null)
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build(); // missing profile
        }

        // 3. Create Order
        Order order = new Order();
        order.setUserId(userId);
        order.setDate(LocalDateTime.now());
        order.setAddress(profile.getAddress());
        order.setCity(profile.getCity());
        order.setState(profile.getState());
        order.setZip(profile.getZip());
        order.setShippingAmount(BigDecimal.ZERO); // You can later customize shipping logic

        // 4. Insert order
        Order savedOrder = orderDao.createOrder(order);

        // 5. Add line items
        for (ShoppingCartItem item : cartItems)
        {
            OrderItem lineItem = new OrderItem();
            lineItem.setOrderId(savedOrder.getOrderId());
            lineItem.setProductId(item.getProductId());
            lineItem.setSalesPrice(item.getPrice());     // product price at time of checkout
            lineItem.setQuantity(item.getQuantity());
            lineItem.setDiscount(BigDecimal.ZERO);        // You could apply coupons/promos here
            orderDao.addLineItem(lineItem);
        }

        // 6. Clear the cart
        cartDao.clearCart(userId);

        // 7. Return order
        return ResponseEntity.ok(savedOrder);
    }
}
