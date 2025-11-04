package com.alejandro.productservice.dao;

import com.alejandro.productservice.model.Product;

public interface ProductDAO {

    void addProduct(Product product);

    Product getProductById(int id);

    void updateProduct(Product product);

    void deleteProduct(int id);

    /**
     * Simula una deducción de stock sin manejo de transacciones,
     * vulnerable a condiciones de carrera (race conditions).
     */
    void deductStockNonTransactional(int id, int quantity) throws InterruptedException;

    /**
     * Realiza una deducción de stock de forma segura usando
     * transacciones JDBC y bloqueo de filas (SELECT ... FOR UPDATE).
     */
    void deductStockTransactional(int id, int quantity);
}
