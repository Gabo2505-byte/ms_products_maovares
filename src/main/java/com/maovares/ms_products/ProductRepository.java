package com.maovares.ms_products;

import org.springframework.data.mongodb.repository.MongoRepository;

/** Acceso a datos; incluye findAll(Pageable) para la paginación. */
public interface ProductRepository extends MongoRepository<Product, String> {
}