package org.yearup.data;


import org.yearup.models.Order;
import org.yearup.models.OrderItem;

import java.util.List;

// Phase 5 Code
public interface OrderDao {
    Order createOrder(Order order);
    void addLineItem(OrderItem item);
    List<OrderItem> getLineItemsByOrderId(int orderId);

}
