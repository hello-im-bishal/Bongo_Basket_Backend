package com.example.demo.adminservices;

import com.example.demo.entity.Category;
import com.example.demo.entity.Product;
import com.example.demo.entity.ProductImage;
import com.example.demo.repository.CategoryRepository;
import com.example.demo.repository.ProductImageRepository;
import com.example.demo.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class AdminProductService {

    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;
    private final CategoryRepository categoryRepository;

    public AdminProductService(ProductRepository productRepository,
                               ProductImageRepository productImageRepository,
                               CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.productImageRepository = productImageRepository;
        this.categoryRepository = categoryRepository;
    }

    // =========================
    // ADD PRODUCT
    // =========================
    public Product addProductWithImage(String name,
                                       String description,
                                       Double price,
                                       Integer stock,
                                       Integer categoryId,
                                       String imageUrl) {

        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Product name is required");
        }

        if (price == null || price <= 0) {
            throw new IllegalArgumentException("Invalid product price");
        }

        if (stock == null || stock < 0) {
            throw new IllegalArgumentException("Invalid stock value");
        }

        if (categoryId == null) {
            throw new IllegalArgumentException("Category ID is required");
        }

        if (imageUrl == null || imageUrl.isBlank()) {
            throw new IllegalArgumentException("Product image URL cannot be empty");
        }

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid category ID"));

        Product product = new Product();
        product.setName(name);
        product.setDescription(description);
        product.setPrice(BigDecimal.valueOf(price));
        product.setStock(stock);
        product.setCategory(category);
        product.setCreatedAt(LocalDateTime.now());
        product.setUpdatedAt(LocalDateTime.now());

        Product savedProduct = productRepository.save(product);

        ProductImage productImage = new ProductImage();
        productImage.setProduct(savedProduct);
        productImage.setImageUrl(imageUrl);
        productImageRepository.save(productImage);

        return savedProduct;
    }

    // =========================
    // DELETE PRODUCT
    // =========================
    public void deleteProduct(Integer productId) {

        if (productId == null) {
            throw new IllegalArgumentException("Product ID is required");
        }

        if (!productRepository.existsById(productId)) {
            throw new IllegalArgumentException("Product not found");
        }

        productImageRepository.deleteByProductId(productId);
        productRepository.deleteById(productId);
    }
}