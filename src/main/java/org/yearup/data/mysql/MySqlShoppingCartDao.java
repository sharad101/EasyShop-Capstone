//package org.yearup.data.mysql;
//
//import org.springframework.stereotype.Component;
//import org.yearup.data.ProductDao;
//import org.yearup.data.ShoppingCartDao;
//import org.yearup.models.Product;
//import org.yearup.models.ShoppingCart;
//import org.yearup.models.ShoppingCartItem;
//
//import javax.sql.DataSource;
//import java.sql.*;
//
//@Component
//public class MySqlShoppingCartDao extends MySqlDaoBase implements ShoppingCartDao
//{
//    private ProductDao productDao;
//
//    public MySqlShoppingCartDao(DataSource dataSource, ProductDao productDao)
//    {
//        super(dataSource);
//        this.productDao = productDao;
//    }
//
//    @Override
//    public ShoppingCart getByUserId(int userId)
//    {
//        ShoppingCart cart = new ShoppingCart();
//        String sql = "SELECT * FROM shopping_cart WHERE user_id = ?";
//
//        try (Connection connection = getConnection();
//             PreparedStatement statement = connection.prepareStatement(sql))
//        {
//            statement.setInt(1, userId);
//            ResultSet resultSet = statement.executeQuery();
//
//            while (resultSet.next())
//            {
//                int productId = resultSet.getInt("product_id");
//                int quantity = resultSet.getInt("quantity");
//
//                Product product = productDao.getById(productId);
//                if (product != null)
//                {
//                    ShoppingCartItem item = new ShoppingCartItem();
//                    item.setProduct(product);
//                    item.setQuantity(quantity);
//                    cart.add(item);
//                }
//            }
//        }
//        catch (SQLException e)
//        {
//            e.printStackTrace();
//        }
//
//        return cart;
//    }
//
//    @Override
//    public void addProduct(int userId, int productId)
//    {
//
//        //to check if the product is already in the cart
//        String selectSql = "SELECT quantity FROM shopping_cart WHERE user_id = ? AND product_id = ?";
//        //if the product does not exist, insert it with quantity 1
//        String insertSql = "INSERT INTO shopping_cart (user_id, product_id, quantity) VALUES (?, ?, ?)";
//        //if the product exists, increase the quantity
//        String updateSql = "UPDATE shopping_cart SET quantity = quantity + 1 WHERE user_id = ? AND product_id = ?";
//
//        try (Connection connection = getConnection())
//        {
//            PreparedStatement selectStatement = connection.prepareStatement(selectSql);
//            selectStatement.setInt(1, userId);
//            selectStatement.setInt(2, productId);
//
//            ResultSet resultSet = selectStatement.executeQuery();
//
//            if (resultSet.next())
//            {
//                // Already in cart → increase quantity
//                PreparedStatement updateStatement = connection.prepareStatement(updateSql);
//                updateStatement.setInt(1, userId);
//                updateStatement.setInt(2, productId);
//                updateStatement.executeUpdate();
//            }
//            else
//            {
//                // Not in cart → insert new row
//                PreparedStatement insertStatement = connection.prepareStatement(insertSql);
//                insertStatement.setInt(1, userId);
//                insertStatement.setInt(2, productId);
//                insertStatement.setInt(3, 1); // default quantity
//                insertStatement.executeUpdate();
//            }
//        }
//        catch (SQLException e)
//        {
//            e.printStackTrace();
//        }
//    }
//
//    @Override
//    public void updateQuantity(int userId, int productId, int quantity)
//    {
//        String sql = "UPDATE shopping_cart SET quantity = ? WHERE user_id = ? AND product_id = ?";
//
//        try (Connection connection = getConnection();
//             PreparedStatement statement = connection.prepareStatement(sql))
//        {
//            statement.setInt(1, quantity);
//            statement.setInt(2, userId);
//            statement.setInt(3, productId);
//            statement.executeUpdate();
//        }
//        catch (SQLException e)
//        {
//            e.printStackTrace();
//        }
//    }
//
//    @Override
//    public void clearCart(int userId)
//    {
//        String sql = "DELETE FROM shopping_cart WHERE user_id = ?";
//
//        try (Connection connection = getConnection();
//             PreparedStatement statement = connection.prepareStatement(sql))
//        {
//            statement.setInt(1, userId);
//            statement.executeUpdate();
//        }
//        catch (SQLException e)
//        {
//            e.printStackTrace();
//        }
//    }
//}

package org.yearup.data.mysql;

import org.springframework.stereotype.Component;
import org.yearup.data.ProductDao;
import org.yearup.data.ShoppingCartDao;
import org.yearup.models.Product;
import org.yearup.models.ShoppingCart;
import org.yearup.models.ShoppingCartItem;

import javax.sql.DataSource;
import java.sql.*;

@Component
public class MySqlShoppingCartDao extends MySqlDaoBase implements ShoppingCartDao
{
    private ProductDao productDao;

    public MySqlShoppingCartDao(DataSource dataSource, ProductDao productDao)
    {
        super(dataSource);
        this.productDao = productDao;
    }

    // Retrieves the shopping cart (all items) for a given userId
    @Override
    public ShoppingCart getByUserId(int userId)
    {
        ShoppingCart cart = new ShoppingCart();
        String sql = "SELECT * FROM shopping_cart WHERE user_id = ?";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql))
        {
            statement.setInt(1, userId);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next())
            {
                int productId = resultSet.getInt("product_id");
                int quantity = resultSet.getInt("quantity");

                Product product = productDao.getById(productId);
                if (product != null)
                {
                    ShoppingCartItem item = new ShoppingCartItem();
                    item.setProduct(product);
                    item.setQuantity(quantity);
                    cart.add(item);
                }
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }

        return cart;
    }

    @Override
    public void addProduct(int userId, int productId, int quantity)
    {
        // Check if the product is already in the user's shopping cart
        String selectSql = "SELECT quantity FROM shopping_cart WHERE user_id = ? AND product_id = ?";

        // Insert a new product into the shopping cart with the specified quantity if it does not exist
        String insertSql = "INSERT INTO shopping_cart (user_id, product_id, quantity) VALUES (?, ?, ?)";

        // Increase the quantity of the product in the shopping cart by the specified amount if it already exists
        String updateSql = "UPDATE shopping_cart SET quantity = quantity + ? WHERE user_id = ? AND product_id = ?";

        try (Connection connection = getConnection())
        {
            PreparedStatement selectStatement = connection.prepareStatement(selectSql);
            selectStatement.setInt(1, userId);
            selectStatement.setInt(2, productId);

            ResultSet resultSet = selectStatement.executeQuery();

            if (resultSet.next())
            {
                // Already in cart → increase quantity by the specified amount
                PreparedStatement updateStatement = connection.prepareStatement(updateSql);
                updateStatement.setInt(1, quantity);
                updateStatement.setInt(2, userId);
                updateStatement.setInt(3, productId);
                updateStatement.executeUpdate();
            }
            else
            {
                // Not in cart → insert new row with specified quantity
                PreparedStatement insertStatement = connection.prepareStatement(insertSql);
                insertStatement.setInt(1, userId);
                insertStatement.setInt(2, productId);
                insertStatement.setInt(3, quantity);
                insertStatement.executeUpdate();
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
    }


    // Checks if a specific product is already in the user's cart
    public boolean isProductInCart(int userId, int productId)
    {
        String sql = "SELECT 1 FROM shopping_cart WHERE user_id = ? AND product_id = ? LIMIT 1";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql))
        {
            statement.setInt(1, userId);
            statement.setInt(2, productId);

            ResultSet resultSet = statement.executeQuery();
            return resultSet.next(); // true if product exists in cart
        }
        catch (SQLException e)
        {
            e.printStackTrace();
            return false;
        }
    }

    // Increments the quantity of a product in the cart by 1
    public void incrementQuantity(int userId, int productId)
    {
        String sql = "UPDATE shopping_cart SET quantity = quantity + 1 WHERE user_id = ? AND product_id = ?";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql))
        {
            statement.setInt(1, userId);
            statement.setInt(2, productId);
            statement.executeUpdate();
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
    }


    // Updates the quantity of a product in the cart to the exact amount
    @Override
    public void updateQuantity(int userId, int productId, int quantity)
    {
        String sql = "UPDATE shopping_cart SET quantity = ? WHERE user_id = ? AND product_id = ?";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql))
        {
            statement.setInt(1, quantity);
            statement.setInt(2, userId);
            statement.setInt(3, productId);
            statement.executeUpdate();
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
    }

    // Removes all products from the user's cart
    @Override
    public void clearCart(int userId)
    {
        String sql = "DELETE FROM shopping_cart WHERE user_id = ?";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql))
        {
            statement.setInt(1, userId);
            statement.executeUpdate();
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
    }
}

