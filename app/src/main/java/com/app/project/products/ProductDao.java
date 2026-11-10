package com.app.project.products;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import androidx.room.Delete;
import java.util.List;

@Dao
public interface ProductDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    long insertProduct(Product product);

    @Update
    int updateProduct(Product product);

    @Delete
    int deleteProduct(Product product);

    @Query("SELECT * FROM products WHERE productId = :id LIMIT 1")
    Product findById(long id);

    @Query("SELECT * FROM products ORDER BY name ASC")
    List<Product> findAll();

    @Query("SELECT * FROM products WHERE name LIKE '%' || :name || '%' ORDER BY name ASC")
    List<Product> findByNameContaining(String name);

    @Query("SELECT * FROM products WHERE name LIKE '%' || :name || '%' ORDER BY name ASC LIMIT :limit OFFSET :offset")
    List<Product> findByNamePaginated(String name, int limit, int offset);

    @Query("SELECT * FROM products ORDER BY name ASC LIMIT :limit OFFSET :offset")
    List<Product> findAllPaginated(int limit, int offset);
}