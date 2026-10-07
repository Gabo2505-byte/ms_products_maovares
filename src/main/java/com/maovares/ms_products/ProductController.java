package com.maovares.ms_products;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/Products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductRepository repository;

    /** GET /v1/Products?page=0&size=10 */
    @GetMapping
    public Page<Product> list(@PageableDefault(size = 10) Pageable pageable) {
        return repository.findAll(pageable);
    }

    /** POST /v1/Products */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Product create(@RequestBody Product product) {
        product.setId(null); // Mongo genera el id
        return repository.save(product);
    }
}
