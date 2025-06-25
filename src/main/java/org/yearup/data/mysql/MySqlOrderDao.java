package org.yearup.data.mysql;


import org.springframework.stereotype.Component;
import org.yearup.data.OrderDao;
import org.yearup.models.Order;
import org.yearup.models.OrderItem;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Component
public class MySqlOrderDao extends MySqlDaoBase implements OrderDao
{
    public MySqlOrderDao(DataSource dataSource)
    {
        super(dataSource);
    }

    @Override
    public Order createOrder(Order order)
    {
        String sql = "INSERT INTO orders (user_id, date, address, city, state, zip, shipping_amount) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection connection = getConnection())
        {
            PreparedStatement preparedStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            preparedStatement.setInt(1, order.getUserId());
            preparedStatement.setTimestamp(2, Timestamp.valueOf(order.getDate()));
            preparedStatement.setString(3, order.getAddress());
            preparedStatement.setString(4, order.getCity());
            preparedStatement.setString(5, order.getState());
            preparedStatement.setString(6, order.getZip());
            preparedStatement.setBigDecimal(7, order.getShippingAmount());

            preparedStatement.executeUpdate();

            ResultSet keys = preparedStatement.getGeneratedKeys();
            if (keys.next())
            {
                order.setOrderId(keys.getInt(1));
            }

            return order;
        }
        catch (SQLException e)
        {
            throw new RuntimeException("Error creating order", e);
        }
    }

    @Override
    public void addLineItem(OrderItem item)
    {
        String sql = "INSERT INTO order_line_items (order_id, product_id, sales_price, quantity, discount) " +
                "VALUES (?, ?, ?, ?, ?)";

        try (Connection connection = getConnection())
        {
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setInt(1, item.getOrderId());
            preparedStatement.setInt(2, item.getProductId());
            preparedStatement.setBigDecimal(3, item.getSalesPrice());
            preparedStatement.setInt(4, item.getQuantity());
            preparedStatement.setBigDecimal(5, item.getDiscount());

            preparedStatement.executeUpdate();
        }
        catch (SQLException e)
        {
            throw new RuntimeException("Error adding line item", e);
        }
    }

    @Override
    public List<OrderItem> getLineItemsByOrderId(int orderId)
    {
        List<OrderItem> items = new ArrayList<>();
        String sql = "SELECT * FROM order_line_items WHERE order_id = ?";

        try (Connection connection = getConnection())
        {
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setInt(1, orderId);

            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next())
            {
                OrderItem item = new OrderItem();
                item.setOrderItemId(resultSet.getInt("order_line_item_id"));
                item.setOrderId(resultSet.getInt("order_id"));
                item.setProductId(resultSet.getInt("product_id"));
                item.setSalesPrice(resultSet.getBigDecimal("sales_price"));
                item.setQuantity(resultSet.getInt("quantity"));
                item.setDiscount(resultSet.getBigDecimal("discount"));
                items.add(item);
            }
        }
        catch (SQLException e)
        {
            throw new RuntimeException("Error fetching order line items", e);
        }

        return items;
    }
}
