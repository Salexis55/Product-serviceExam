package com.alejandro.productservice.dao;

import java.sql.*; // Clases JDBC para conexión, consultas y resultados
import com.alejandro.productservice.model.Product;
import com.alejandro.productservice.util.DBConnection;

/**
 * Implementa las operaciones definidas en ProductDAO.
 * Maneja la conexión a base de datos mediante JDBC.
 */
public class ProductDAOImpl implements ProductDAO {

    // Inserta un nuevo producto
    @Override
    public void addProduct(Product product) {
        String sql = "INSERT INTO products (id, productCode, stock, warehouse) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, product.getId());
            pstmt.setString(2, product.getProductCode());
            pstmt.setInt(3, product.getStock());
            pstmt.setString(4, product.getWarehouse());
            pstmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Busca un producto por su ID
    @Override
    public Product getProductById(int id) {
        String sql = "SELECT * FROM products WHERE id = ?";
        Product product = null;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    product = new Product(
                        rs.getInt("id"),
                        rs.getString("productCode"),
                        rs.getInt("stock"),
                        rs.getString("warehouse")
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return product;
    }

    // Actualiza un producto existente
    @Override
    public void updateProduct(Product product) {
        String sql = "UPDATE products SET productCode = ?, stock = ?, warehouse = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, product.getProductCode());
            pstmt.setInt(2, product.getStock());
            pstmt.setString(3, product.getWarehouse());
            pstmt.setInt(4, product.getId());
            pstmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Elimina un producto por ID
    @Override
    public void deleteProduct(int id) {
        String sql = "DELETE FROM products WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Deducción SIN transacción — propensa a errores por concurrencia.
     * Cada hilo usa su propia conexión (auto-commit=true), lo que genera
     * condiciones de carrera al leer/modificar/escribir.
     */
    @Override
    public void deductStockNonTransactional(int id, int quantity) throws InterruptedException {
        Product product = getProductById(id);
        
        if (product != null && product.getStock() >= quantity) {
            Thread.sleep(10); // simula retraso para provocar error de concurrencia
            product.setStock(product.getStock() - quantity);
            updateProduct(product);
            System.out.println(Thread.currentThread().getName() + " procesó. Nuevo stock: " + product.getStock());
        } else {
            System.out.println(Thread.currentThread().getName() + " no pudo procesar (sin stock).");
        }
    }

    /**
     * Deducción CON transacción — segura frente a concurrencia.
     * Usa SELECT ... FOR UPDATE para bloquear la fila durante la operación.
     */
    @Override
    public void deductStockTransactional(int id, int quantity) {
        String selectForUpdate = "SELECT id, stock FROM products WHERE id = ? FOR UPDATE";
        String updateStock = "UPDATE products SET stock = ? WHERE id = ?";
        
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false); // inicia transacción

            int currentStock = -1;

            // Bloquea la fila hasta el commit/rollback
            try (PreparedStatement selectStmt = conn.prepareStatement(selectForUpdate)) {
                selectStmt.setInt(1, id);
                try (ResultSet rs = selectStmt.executeQuery()) {
                    if (rs.next()) currentStock = rs.getInt("stock");
                    else throw new SQLException("Producto no encontrado");
                }
            }

            if (currentStock >= quantity) {
                int newStock = currentStock - quantity;
                try (PreparedStatement updateStmt = conn.prepareStatement(updateStock)) {
                    updateStmt.setInt(1, newStock);
                    updateStmt.setInt(2, id);
                    updateStmt.executeUpdate();
                }
                System.out.println(Thread.currentThread().getName() + " procesó. Stock actualizado: " + newStock);
            } else {
                System.out.println(Thread.currentThread().getName() + " sin stock suficiente.");
            }

            conn.commit(); // confirma cambios

        } catch (SQLException e) {
            System.err.println(Thread.currentThread().getName() + " | Error: " + e.getMessage());
            try { if (conn != null) conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
        } finally {
            try {
                if (conn != null) {
                    conn.setAutoCommit(true);
                    conn.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}