package com.alejandro.productservice.model;

/**
 * Clase modelo que representa un producto en la base de datos.
 * Contiene sus atributos, constructores, getters/setters y toString().
 */
public class Product {
    // Atributos que corresponden a las columnas de la tabla 'products'
    private int id;
    private String productCode;
    private int stock;
    private String warehouse;

    // Constructor vacío (requerido por frameworks o JDBC)
    public Product() {}

    // Constructor con parámetros para crear objetos fácilmente
    public Product(int id, String productCode, int stock, String warehouse) {
        this.id = id;
        this.productCode = productCode;
        this.stock = stock;
        this.warehouse = warehouse;
    }

    // Métodos getter y setter para acceder y modificar los atributos
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public String getProductCode() { return productCode; }
    public void setProductCode(String productCode) { this.productCode = productCode; }
    
    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }
    
    public String getWarehouse() { return warehouse; }
    public void setWarehouse(String warehouse) { this.warehouse = warehouse; }

    // Muestra los datos del producto en formato legible (útil para logs o depuración)
    @Override
    public String toString() {
        return "Product{" +
                "id=" + id +
                ", productCode='" + productCode + '\'' +
                ", stock=" + stock +
                ", warehouse='" + warehouse + '\'' +
                '}';
    }
}