package com.app.project.products;

import java.util.List;

public class ProductRepository {
 
    private final ProductDao productDao;

    public ProductRepository(ProductDao productDao) {
        this.productDao = productDao;
    }

    public Product findById(long id) {
        return productDao.findById(id);
    }

    public Product createProduct(Product product) {
        try {
            // O Room retorna o ID gerado na inserção
            long generatedId = productDao.insertProduct(product);
            
            
            product.setProductId(generatedId); 
            
            return product;
        
        } catch (android.database.sqlite.SQLiteConstraintException e) {
            throw new IllegalArgumentException("Conflito de dados: não foi possível salvar o produto.");
        } catch (Exception e) {
            System.err.println("Erro no banco de dados ao salvar produto: " + e.getMessage());
            return null;
        }
    }

    public Product updateProduct(Product product) {
        try {
            // O Room retorna o número de linhas afetadas
            int rowsAffected = productDao.updateProduct(product);
            return rowsAffected > 0 ? product : null;
            
        } catch (android.database.sqlite.SQLiteConstraintException e) {
            throw new IllegalArgumentException("Conflito de dados: um produto com esta informação única já existe.");
        } catch (Exception e) {
            System.err.println("Erro no banco de dados ao atualizar produto: " + e.getMessage());
            return null;
        }
    }

    public boolean deleteProduct(Product product) {
        try {
            int rowsAffected = productDao.deleteProduct(product);
            return rowsAffected > 0;
        } catch (Exception e) {
            System.err.println("Erro no banco de dados ao deletar produto: " + e.getMessage());
            return false;
        }
    }

    public List<Product> findAll() {
        return productDao.findAll();
    }

    public List<Product> findByNameContaining(String name) {
        return productDao.findByNameContaining(name);
    }

    public List<Product> findByNamePaginated(String name, int limit, int offset) {
        return productDao.findByNamePaginated(name, limit, offset);
    }

    public List<Product> findAllPaginated(int limit, int offset) {
        return productDao.findAllPaginated(limit, offset);
    }
}