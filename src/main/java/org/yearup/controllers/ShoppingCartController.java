package org.yearup.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.yearup.data.ProductDao;
import org.yearup.data.ShoppingCartDao;
import org.yearup.data.UserDao;
import org.yearup.models.Product;
import org.yearup.models.ShoppingCart;
import org.yearup.models.ShoppingCartItem;
import org.yearup.models.User;

import java.security.Principal;

//Phase 3 Optional
// convert this class to a REST controller
@RestController
@RequestMapping("/cart")
@CrossOrigin
@PreAuthorize("isAuthenticated()")  // Ensure only logged in users can access these endpoints
public class ShoppingCartController
{
    private ShoppingCartDao shoppingCartDao;
    private UserDao userDao;
    private ProductDao productDao;

    // Constructor injection of dependencies (DAOs)
    @Autowired
    public ShoppingCartController(ShoppingCartDao shoppingCartDao, UserDao userDao, ProductDao productDao)
    {
        this.shoppingCartDao = shoppingCartDao;
        this.userDao = userDao;
        this.productDao = productDao;
    }

    // Helper method to get the logged-in user's ID using the Principal object
    private int getCurrentUserId(Principal principal)
    {
        String username = principal.getName();
        User user = userDao.getByUserName(username);
        return user.getId();
    }

    // GET /cart - Returns the current user's shopping cart with all items
    @GetMapping
    public ShoppingCart getCart(Principal principal)
    {
        try
        {
            int userId = getCurrentUserId(principal);
            return shoppingCartDao.getByUserId(userId);
        }
        catch(Exception e)
        {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Oops... our bad.");
        }
    }


    // add a POST method to add a product to the cart - the url should be
    // https://localhost:8080/cart/products/15 (15 is the productId to be added)
    @PostMapping("/products/{productId}")
    public ShoppingCart addProductToCart(@PathVariable int productId, Principal principal)
    {
        try
        {
            int userId = getCurrentUserId(principal);

            Product product = productDao.getById(productId);
            if (product == null)
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found.");

            // Check if product already exists in cart
            if (shoppingCartDao.isProductInCart(userId, productId))
            {
                // Increment quantity by 1
                shoppingCartDao.incrementQuantity(userId, productId);
            }
            else
            {
                // Add new product with quantity 1
                shoppingCartDao.addProduct(userId, productId, 1);
            }

            // Return the updated shopping cart
            return shoppingCartDao.getByUserId(userId);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to add product to cart.");
        }
    }

    // add a PUT method to update an existing product in the cart - the url should be
    // https://localhost:8080/cart/products/15 (15 is the productId to be updated)
    @PutMapping("/products/{productId}")
    public ShoppingCart updateQuantity(@PathVariable int productId, @RequestBody ShoppingCartItem item, Principal principal)
    {
        try
        {
            int userId = getCurrentUserId(principal);
            int quantity = item.getQuantity();

            if (quantity <= 0)
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Quantity must be greater than 0.");

            // Update the quantity only if product is in cart
            if (shoppingCartDao.isProductInCart(userId, productId))
            {
                shoppingCartDao.updateQuantity(userId, productId, quantity);
            }
            else
            {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found in cart.");
            }

            // Return the updated shopping cart
            return shoppingCartDao.getByUserId(userId);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to update product quantity.");
        }
    }


    // add a DELETE method to clear all products from the current users cart
    // https://localhost:8080/cart
    @DeleteMapping
    public ShoppingCart clearCart(Principal principal)
    {
        try
        {
            int userId = getCurrentUserId(principal);
            shoppingCartDao.clearCart(userId);

            // Return empty cart after clearing
            return shoppingCartDao.getByUserId(userId);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to clear cart.");
        }
    }
}

