package org.yearup.models;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public class ShoppingCart
{
//    private Map<Integer, ShoppingCartItem> items = new HashMap<>();
//
//    public Map<Integer, ShoppingCartItem> getItems()
//    {
//        return items;
//    }
//
//    public void setItems(Map<Integer, ShoppingCartItem> items)
//    {
//        this.items = items;
//    }
//
//    public boolean contains(int productId)
//    {
//        return items.containsKey(productId);
//    }
//
//    public void add(ShoppingCartItem item)
//    {
//        items.put(item.getProductId(), item);
//    }
//
//    public ShoppingCartItem get(int productId)
//    {
//        return items.get(productId);
//    }
//
//    public BigDecimal getTotal()
//    {
//        BigDecimal total = items.values()
//                                .stream()
//                                .map(i -> i.getLineTotal())
//                                .reduce( BigDecimal.ZERO, (lineTotal, subTotal) -> subTotal.add(lineTotal));
//
//        return total;
//    }


    //Phase 3 New Code
// *** UPDATED: Changed the Map key type from Integer to String. ***
// REASON: The JSON specification for the shopping cart "items" requires
// product IDs as STRING keys (e.g., "1", "15"), not integer keys.
// This ensures correct JSON serialization and deserialization by front-end clients.
private Map<String, ShoppingCartItem> items = new HashMap<>();

    // Getters and Setters
    // *** UPDATED: Return type of getItems() now matches Map<String, ShoppingCartItem>. ***
    public Map<String, ShoppingCartItem> getItems()
    {
        return items;
    }

    // *** UPDATED: Parameter type of setItems() now matches Map<String, ShoppingCartItem>. ***
    public void setItems(Map<String, ShoppingCartItem> items)
    {
        this.items = items;
    }

    public boolean contains(int productId)
    {
        // *** UPDATED: Convert the productId (int) to a String to match the map's key type. ***
        return items.containsKey(String.valueOf(productId));
    }

    public void add(ShoppingCartItem item)
    {
        // *** UPDATED: Convert the item's productId (int) to a String when adding to the map. ***
        items.put(String.valueOf(item.getProductId()), item);
    }

    public ShoppingCartItem get(int productId)
    {
        // *** UPDATED: Convert the productId (int) to a String when retrieving from the map. ***
        return items.get(String.valueOf(productId));
    }

    public BigDecimal getTotal()
    {
        // This logic remains correct for summing BigDecimal values from line totals.
        BigDecimal total = items.values()
                .stream()
                .map(ShoppingCartItem::getLineTotal) // Uses method reference for clarity
                .reduce( BigDecimal.ZERO, BigDecimal::add); // Uses method reference for summing

        return total;
    }

}
