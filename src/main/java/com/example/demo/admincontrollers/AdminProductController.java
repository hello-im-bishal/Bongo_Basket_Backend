package com.example.demo.admincontrollers;

import com.example.demo.entity.Product;
import com.example.demo.adminservices.AdminProductService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/admin/products")
@CrossOrigin(origins = "*") // Allow frontend calls
public class AdminProductController {

    private final AdminProductService adminProductService;

    public AdminProductController(AdminProductService adminProductService) {
        this.adminProductService = adminProductService;
    }

    // =========================
    // ADD PRODUCT
    // =========================
    @PostMapping("/add")
    public ResponseEntity<?> addProduct(@RequestBody Map<String, Object> productRequest) {
        try {

            String name = (String) productRequest.get("name");
            String description = (String) productRequest.get("description");

            Double price = productRequest.get("price") != null
                    ? Double.valueOf(productRequest.get("price").toString())
                    : null;

            Integer stock = productRequest.get("stock") != null
                    ? Integer.valueOf(productRequest.get("stock").toString())
                    : null;

            Integer categoryId = productRequest.get("categoryId") != null
                    ? Integer.valueOf(productRequest.get("categoryId").toString())
                    : null;

            String imageUrl = (String) productRequest.get("imageUrl");

            Product addedProduct = adminProductService.addProductWithImage(
                    name, description, price, stock, categoryId, imageUrl
            );

            return ResponseEntity.status(HttpStatus.CREATED).body(addedProduct);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Something went wrong while adding product");
        }
    }

    // =========================
    // DELETE PRODUCT
    // =========================
    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteProduct(@RequestBody Map<String, Object> requestBody) {
        try {

            Integer productId = requestBody.get("productId") != null
                    ? Integer.valueOf(requestBody.get("productId").toString())
                    : null;

            adminProductService.deleteProduct(productId);

            return ResponseEntity.ok("Product deleted successfully");

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Something went wrong while deleting product");
        }
    }
}