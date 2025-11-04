package com.alejandro.productservice.dao;

import com.alejandro.productservice.model.Product;

/**
 * Interfaz DAO: define las operaciones CRUD y manejo de stock. Separa la lógica
 * de negocio del acceso a base de datos.
 */
public interface ProductDAO {

	// Crear un nuevo producto
	void addProduct(Product product);

	// Obtener un producto por ID
	Product getProductById(int id);

	// Actualizar un producto existente
	void updateProduct(Product product);

	// Eliminar un producto por ID
	void deleteProduct(int id);

	/**
	 * Descuenta stock sin transacción (riesgo de condiciones de carrera).
	 */
	void deductStockNonTransactional(int id, int quantity) throws InterruptedException;

	/**
	 * Descuenta stock con transacción y bloqueo de fila (seguro).
	 */
	void deductStockTransactional(int id, int quantity);
}