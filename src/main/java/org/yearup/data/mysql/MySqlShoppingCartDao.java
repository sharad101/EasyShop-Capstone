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
    public void addProduct(int userId, int productId)
    {

        //to check if the product is already in the cart
        String selectSql = "SELECT quantity FROM shopping_cart WHERE user_id = ? AND product_id = ?";
        //if the product does not exist, insert it with quantity 1
        String insertSql = "INSERT INTO shopping_cart (user_id, product_id, quantity) VALUES (?, ?, ?)";
        //if the product exists, increase the quantity
        String updateSql = "UPDATE shopping_cart SET quantity = quantity + 1 WHERE user_id = ? AND product_id = ?";

        try (Connection connection = getConnection())
        {
            PreparedStatement selectStatement = connection.prepareStatement(selectSql);
            selectStatement.setInt(1, userId);
            selectStatement.setInt(2, productId);

            ResultSet resultSet = selectStatement.executeQuery();

            if (resultSet.next())
            {
                // Already in cart → increase quantity
                PreparedStatement updateStatement = connection.prepareStatement(updateSql);
                updateStatement.setInt(1, userId);
                updateStatement.setInt(2, productId);
                updateStatement.executeUpdate();
            }
            else
            {
                // Not in cart → insert new row
                PreparedStatement insertStatement = connection.prepareStatement(insertSql);
                insertStatement.setInt(1, userId);
                insertStatement.setInt(2, productId);
                insertStatement.setInt(3, 1); // default quantity
                insertStatement.executeUpdate();
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
    }

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
