package org.yearup.data;

import org.yearup.models.ShoppingCart;

public interface ShoppingCartDao
{
    ShoppingCart getByUserId(int userId);
    // add additional method signatures here

    // Add product to cart, increment quantity if already present
    void addProduct(int userId, int productId);

    // Update quantity of a specific product in user's cart
    void updateQuantity(int userId, int productId, int quantity);

    // Clear all products from the user's cart
    void clearCart(int userId);
}
