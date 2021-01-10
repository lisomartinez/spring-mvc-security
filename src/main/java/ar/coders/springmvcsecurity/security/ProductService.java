package ar.coders.springmvcsecurity.security;

import ar.coders.springmvcsecurity.model.Product;
import ar.coders.springmvcsecurity.repositories.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {
    private ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public List<Product> findAll() {
        return productRepository.findAll();
    }
}
