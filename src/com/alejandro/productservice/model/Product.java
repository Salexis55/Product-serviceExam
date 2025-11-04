package com.alejandro.productservice.model;

public class Product {
    private int id;
    private String productCode;
    private int stock;
    private String warehouse;

    public Product() {}

    public Product(int id, String productCode, int stock, String warehouse) {
        this.id = id;
        this.productCode = productCode;
        this.stock = stock;
        this.warehouse = warehouse;
    }

    // --- Getters y Setters ---

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public String getProductCode() { return productCode; }
    public void setProductCode(String productCode) { this.productCode = productCode; }
    
    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }
    
    public String getWarehouse() { return warehouse; }
    public void setWarehouse(String warehouse) { this.warehouse = warehouse; }

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
