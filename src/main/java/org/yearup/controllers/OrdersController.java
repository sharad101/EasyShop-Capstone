package org.yearup.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.yearup.data.OrderDao;
import org.yearup.data.ProfileDao;
import org.yearup.data.ShoppingCartDao;
import org.yearup.models.*;

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

    // This endpoint handles the checkout process
    @PostMapping
    public ResponseEntity<Order> checkout(@AuthenticationPrincipal User user)
    {
        int userId = user.getId(); // ✅ Get logged-in user ID

        // 1. Get the user's cart
        ShoppingCart cart = cartDao.getByUserId(userId);

        // 2. Extract cart items from the map and convert to a list
        List<ShoppingCartItem> cartItems = new ArrayList<>(cart.getItems().values());

        if (cartItems.isEmpty())
        {
            return ResponseEntity.badRequest().build(); // No items to checkout
        }

        //3. Get profile for shipping details
        Profile profile = profileDao.getByUserId(userId);
        if (profile == null)
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build(); // Missing profile
        }

        // 4. Create a new Order object
        Order order = new Order();
        order.setUserId(userId);
        order.setDate(LocalDateTime.now());
        order.setAddress(profile.getAddress());
        order.setCity(profile.getCity());
        order.setState(profile.getState());
        order.setZip(profile.getZip());
        order.setShippingAmount(BigDecimal.ZERO); // 💡 Customize shipping if needed

        // 5. Save the order and get back the saved order (with orderId)
        Order savedOrder = orderDao.createOrder(order);

        // 6. Convert each ShoppingCartItem into an OrderItem
        for (ShoppingCartItem item : cartItems)
        {
            OrderItem lineItem = new OrderItem();
            lineItem.setOrderId(savedOrder.getOrderId());
            lineItem.setProductId(item.getProductId());

            //NEW: Use item.getPrice() (which you added to ShoppingCartItem)
            lineItem.setSalesPrice(item.getPrice());

            lineItem.setQuantity(item.getQuantity());
            lineItem.setDiscount(BigDecimal.ZERO); // 💡 Add discount logic if needed

            orderDao.addLineItem(lineItem);
        }

        // 7. Clear cart after successful order
        cartDao.clearCart(userId);

        // 8. Return the saved order
        return ResponseEntity.ok(savedOrder);
    }
}
