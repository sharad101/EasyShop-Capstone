package org.yearup.data.mysql;

import org.springframework.stereotype.Component;
import org.yearup.data.CategoryDao;
import org.yearup.models.Category;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;


//Phase 1
@Component
public class MySqlCategoryDao extends MySqlDaoBase implements CategoryDao
{
    public MySqlCategoryDao(DataSource dataSource)
    {
        super(dataSource);
    }

    @Override
    public List<Category> getAllCategories()
    {
        // get all categories
        List<Category> categories = new ArrayList<>();
        String sql = "SELECT * FROM categories ORDER BY category_id";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery())
        {
            while (resultSet.next())
            {
                Category category = mapRow(resultSet);
                categories.add(category);
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }

        return categories;
    }

    @Override
    public Category getById(int categoryId)
    {
        // get category by id
        String sql = "SELECT * FROM categories WHERE category_id = ?";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql))
        {
            statement.setInt(1, categoryId);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next())
            {
                return mapRow(resultSet);

            }

        }
        catch (SQLException e)
        {
            e.printStackTrace();

        }

        return null;
    }

    @Override
    public Category create(Category category)
    {
        // create a new category
        String sql = "INSERT INTO categories (name, description) VALUES (?, ?)";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS))
        {
            statement.setString(1, category.getName());
            statement.setString(2, category.getDescription());

            int rowsAffected = statement.executeUpdate();

            if (rowsAffected > 0)
            {
                ResultSet keys = statement.getGeneratedKeys();
                if (keys.next())
                {
                    int id = keys.getInt(1);
                    return getById(id);
                }
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public void update(int categoryId, Category category)
    {
        // update category
        String sql = "UPDATE categories SET name = ?, description = ? WHERE category_id = ?";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql))
        {
            statement.setString(1, category.getName());
            statement.setString(2, category.getDescription());
            statement.setInt(3, categoryId);

            statement.executeUpdate();
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void delete(int categoryId)
    {
        // delete category
        String sql = "DELETE FROM categories WHERE category_id = ?";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql))
        {
            statement.setInt(1, categoryId);
            statement.executeUpdate();
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
    }

    private Category mapRow(ResultSet row) throws SQLException
    {
        int categoryId = row.getInt("category_id");
        String name = row.getString("name");
        String description = row.getString("description");

        Category category = new Category()
        {{
            setCategoryId(categoryId);
            setName(name);
            setDescription(description);
        }};

        return category;
    }

}
