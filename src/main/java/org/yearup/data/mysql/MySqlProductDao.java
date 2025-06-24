package org.yearup.data.mysql;

import org.springframework.stereotype.Component;
import org.yearup.models.Product;
import org.yearup.data.ProductDao;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Component
public class MySqlProductDao extends MySqlDaoBase implements ProductDao
{
    public MySqlProductDao(DataSource dataSource)
    {
        super(dataSource);
    }

    @Override
    public List<Product> search(Integer categoryId, BigDecimal minPrice, BigDecimal maxPrice, String color)
    {
        // Create a list to store the search results
        List<Product> products = new ArrayList<>();

        // List to store parameters that we will bind to the SQL query
        List<Object> parameters = new ArrayList<>();

        // Start building the SQL query with a base condition that is always true (1=1)
        // This allows us to append "AND" clauses easily
        StringBuilder sql = new StringBuilder("SELECT * FROM products WHERE 1=1");

        // Filter by categoryId if it is provided (not null)
        // I was stuck here
        if (categoryId != null)
        {
            sql.append(" AND category_id = ?");
            parameters.add(categoryId);
        }

        // Filter by minimum price if provided
        if (minPrice != null)
        {
            sql.append(" AND price >= ?");
            parameters.add(minPrice);
        }

        // Filter by maximum price if provided
        if (maxPrice != null)
        {
            sql.append(" AND price <= ?");
            parameters.add(maxPrice);
        }

        // Filter by color if provided and not blank
        if (color != null && !color.isBlank())
        {
            // Use LOWER() for case-insensitive comparison
            sql.append(" AND LOWER(color) = LOWER(?)");
            parameters.add(color);
        }

        try (
                // Open database connection
                Connection connection = getConnection();
                // Create the prepared statement with the final SQL
                PreparedStatement statement = connection.prepareStatement(sql.toString())
        )
        {
            // Bind the collected parameters to the prepared statement
            for (int i = 0; i < parameters.size(); i++)
            {
                statement.setObject(i + 1, parameters.get(i));
            }

            // Execute the query
            ResultSet resultSet = statement.executeQuery();

            // Loop through the results and map each row to a Product object
            while (resultSet.next())
            {
                Product product = mapRow(resultSet);
                products.add(product);
            }
        }
        catch (SQLException e)
        {
            // Print the stack trace for debugging purposes
            e.printStackTrace();
        }

        // Return the list of matching products
        return products;
    }



//    @Override
//    public List<Product> search(Integer categoryId, BigDecimal minPrice, BigDecimal maxPrice, String color)
//    {
//        List<Product> products = new ArrayList<>();
//
//        String sql = "SELECT * FROM products " +
//                "WHERE (category_id = ? OR ? = -1) " +
//                "   AND (price <= ? OR ? = -1) " +
//                "   AND (color = ? OR ? = '') ";
//
//        categoryId = categoryId == null ? -1 : categoryId;
//        minPrice = minPrice == null ? new BigDecimal("-1") : minPrice;
//        maxPrice = maxPrice == null ? new BigDecimal("-1") : maxPrice;
//        color = color == null ? "" : color;
//
//        try (Connection connection = getConnection())
//        {
//            PreparedStatement statement = connection.prepareStatement(sql);
//            statement.setInt(1, categoryId);
//            statement.setInt(2, categoryId);
//            statement.setBigDecimal(3, minPrice);
//            statement.setBigDecimal(4, minPrice);
//            statement.setString(5, color);
//            statement.setString(6, color);
//
//            ResultSet row = statement.executeQuery();
//
//            while (row.next())
//            {
//                Product product = mapRow(row);
//                products.add(product);
//            }
//        }
//        catch (SQLException e)
//        {
//            throw new RuntimeException(e);
//        }
//
//        return products;
//    }

    @Override
    public List<Product> listByCategoryId(int categoryId)
    {
        List<Product> products = new ArrayList<>();

        String sql = "SELECT * FROM products " +
                    " WHERE category_id = ? ";

        try (Connection connection = getConnection())
        {
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, categoryId);

            ResultSet row = statement.executeQuery();

            while (row.next())
            {
                Product product = mapRow(row);
                products.add(product);
            }
        }
        catch (SQLException e)
        {
            throw new RuntimeException(e);
        }

        return products;
    }


    @Override
    public Product getById(int productId)
    {
        String sql = "SELECT * FROM products WHERE product_id = ?";
        try (Connection connection = getConnection())
        {
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, productId);

            ResultSet row = statement.executeQuery();

            if (row.next())
            {
                return mapRow(row);
            }
        }
        catch (SQLException e)
        {
            throw new RuntimeException(e);
        }
        return null;
    }

    @Override
    public Product create(Product product)
    {

        String sql = "INSERT INTO products(name, price, category_id, description, color, image_url, stock, featured) " +
                " VALUES (?, ?, ?, ?, ?, ?, ?, ?);";

        try (Connection connection = getConnection())
        {
            PreparedStatement statement = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);
            statement.setString(1, product.getName());
            statement.setBigDecimal(2, product.getPrice());
            statement.setInt(3, product.getCategoryId());
            statement.setString(4, product.getDescription());
            statement.setString(5, product.getColor());
            statement.setString(6, product.getImageUrl());
            statement.setInt(7, product.getStock());
            statement.setBoolean(8, product.isFeatured());

            int rowsAffected = statement.executeUpdate();

            if (rowsAffected > 0) {
                // Retrieve the generated keys
                ResultSet generatedKeys = statement.getGeneratedKeys();

                if (generatedKeys.next()) {
                    // Retrieve the auto-incremented ID
                    int orderId = generatedKeys.getInt(1);

                    // get the newly inserted category
                    return getById(orderId);
                }
            }
        }
        catch (SQLException e)
        {
            throw new RuntimeException(e);
        }
        return null;
    }

    @Override
    public void update(int productId, Product product)
    {
        String sql = "UPDATE products" +
                " SET name = ? " +
                "   , price = ? " +
                "   , category_id = ? " +
                "   , description = ? " +
                "   , color = ? " +
                "   , image_url = ? " +
                "   , stock = ? " +
                "   , featured = ? " +
                " WHERE product_id = ?;";

        try (Connection connection = getConnection())
        {
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, product.getName());
            statement.setBigDecimal(2, product.getPrice());
            statement.setInt(3, product.getCategoryId());
            statement.setString(4, product.getDescription());
            statement.setString(5, product.getColor());
            statement.setString(6, product.getImageUrl());
            statement.setInt(7, product.getStock());
            statement.setBoolean(8, product.isFeatured());
            statement.setInt(9, productId);

            statement.executeUpdate();
        }
        catch (SQLException e)
        {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void delete(int productId)
    {

        String sql = "DELETE FROM products " +
                " WHERE product_id = ?;";

        try (Connection connection = getConnection())
        {
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, productId);

            statement.executeUpdate();
        }
        catch (SQLException e)
        {
            throw new RuntimeException(e);
        }
    }

    protected static Product mapRow(ResultSet row) throws SQLException
    {
        int productId = row.getInt("product_id");
        String name = row.getString("name");
        BigDecimal price = row.getBigDecimal("price");
        int categoryId = row.getInt("category_id");
        String description = row.getString("description");
        String color = row.getString("color");
        int stock = row.getInt("stock");
        boolean isFeatured = row.getBoolean("featured");
        String imageUrl = row.getString("image_url");

        return new Product(productId, name, price, categoryId, description, color, stock, isFeatured, imageUrl);
    }
}
