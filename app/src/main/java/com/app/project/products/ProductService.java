package com.app.project.products;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ProductService {
    
    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    
    public ProductDto getProductById(long id) {
        
        Product product = this.productRepository.findById(id);
        if (product == null) {
            return null;
        }
        return ToDto(product);
    }

    
    public ProductDto createProduct(ProductDto productDto) {
        
        Product product = new Product();
        // O Room interpreta o valor 0 como instrução para autogerar o ID no banco
        product.setProductId(0);
        product.setName(productDto.getName());
        product.setPrice(productDto.getPrice());
        
        Product savedProduct = this.productRepository.createProduct(product);
        return ToDto(savedProduct);
    }

    
    public ProductDto updateProduct(long id, ProductDto productDto) {
        
        Product product = this.productRepository.findById(id);
        
        if (product == null) {
            return null;
        }

        if (productDto.getName() != null) {
            product.setName(productDto.getName());
        }
        if (productDto.getPrice() != null) {
            product.setPrice(productDto.getPrice());
        }

        Product updatedProduct = this.productRepository.updateProduct(product);
        
        return ToDto(updatedProduct);
    }

    
    public boolean deleteProduct(long id) {
        
        Product product = this.productRepository.findById(id);
        
        if (product == null) {
            return false;
        }
        return this.productRepository.deleteProduct(product); 
    }

    
    public List<ProductDto> getAllProducts() {
        
        List<Product> products = productRepository.findAll();
        List<ProductDto> result = new ArrayList<>(products.size());
        
        for (Product product : products) {
            result.add(ToDto(product));
        }
        
        return result;
    }

    
    public List<ProductDto> getProductsByName(String name) {
        
        List<Product> products = productRepository.findByNameContaining(name);
        List<ProductDto> result = new ArrayList<>(products.size());
        
        for (Product product : products) {
            result.add(ToDto(product));
        }
        
        return result;
    }

    
    public List<ProductDto> getProductsPaginated(String name, int page, int size) {
        
        int offset = (page - 1) * size;
        
        List<Product> products;

        if (name != null && !name.trim().isEmpty()) {
            products = productRepository.findByNamePaginated(name, size, offset);
        } else {
            products = productRepository.findAllPaginated(size, offset);
        }

        List<ProductDto> result = new ArrayList<>(products.size());
        for (Product product : products) {
            result.add(ToDto(product));
        }
        
        return result;
    }

    
    private ProductDto ToDto(Product product) {
        ProductDto dto = new ProductDto();
        dto.setId(product.getProductId());
        dto.setName(product.getName());
        dto.setPrice(product.getPrice());
        return dto;
    }
}