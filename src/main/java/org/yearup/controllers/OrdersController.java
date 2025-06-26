package org.yearup.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.yearup.models.Order;
import org.yearup.models.User;
import org.yearup.services.OrderService; // NEW: Import the OrderService

@RestController
@RequestMapping("/orders")
@CrossOrigin
@PreAuthorize("isAuthenticated()") // Ensure only authenticated users can access this controller
public class OrdersController
{
    // UPDATED: No longer injecting individual DAOs directly
    // private final ShoppingCartDao cartDao;
    // private final OrderDao orderDao;
    // private final ProfileDao profileDao;

    // NEW: Inject the OrderService
    private final OrderService orderService;

    // UPDATED: Constructor now injects OrderService
    @Autowired
    public OrdersController(OrderService orderService)
    {
        this.orderService = orderService;
    }

    // This endpoint handles the checkout process
    @PostMapping
    public ResponseEntity<Order> checkout(@AuthenticationPrincipal User user)
    {
        // Basic check for authenticated user (though @PreAuthorize handles most of this)
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not authenticated.");
        }

        try
        {
            // NEW: Delegate the entire checkout business logic to the OrderService
            // The service method will handle fetching cart, profile, creating order,
            // adding line items, clearing cart, and crucially, transactional management.
            Order newOrder = orderService.placeOrder(user);

            // Return the newly created order with a 200 OK status
            return ResponseEntity.ok(newOrder);
        }
        // NEW: More specific exception handling for known service-level errors
        catch (IllegalArgumentException e) {
            // This might be thrown by the service for an empty cart, etc.
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
        catch (IllegalStateException e) {
            // This might be thrown by the service for a missing profile, etc.
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage()); // Or HttpStatus.NOT_FOUND if preferred
        }
        catch (RuntimeException e) {
            // Catch any unexpected RuntimeExceptions (e.g., from DAO layer)
            e.printStackTrace(); // Log the full stack trace for debugging
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "An error occurred during checkout: " + e.getMessage());
        }
        catch (Exception e)
        {
            // Catch any other unexpected exceptions
            e.printStackTrace(); // Log the full stack trace
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred during checkout.");
        }
    }
}