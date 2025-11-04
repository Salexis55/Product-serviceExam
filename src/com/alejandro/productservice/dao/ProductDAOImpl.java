package com.alejandro.productservice.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.alejandro.productservice.model.Product;
import com.alejandro.productservice.util.DBConnection;

public class ProductDAOImpl implements ProductDAO {

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

    @Override
    public Product getProductById(int id) {
        String sql = "SELECT * FROM products WHERE id = ?";
        Product product = null;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    product = new Product();
                    product.setId(rs.getInt("id"));
                    product.setProductCode(rs.getString("productCode"));
                    product.setStock(rs.getInt("stock"));
                    product.setWarehouse(rs.getString("warehouse"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return product;
    }

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
     * IMPLEMENTACIÓN "INCORRECTA" (No transaccional)
     * Esta es la operación de "Leer-Modificar-Escribir" que causa la condición de carrera.
     */
    @Override
    public void deductStockNonTransactional(int id, int quantity) throws InterruptedException {
        // 1. LEER (READ)
        // Cada hilo obtiene su propia conexión (auto-commit=true por defecto)
        Product product = getProductById(id);
        
        if (product != null && product.getStock() >= quantity) {
            // Se simula un pequeño retardo (ej. lógica de negocio, validación)
            // Esto da tiempo a que otros hilos lean el valor "antiguo" del stock.
            Thread.sleep(10); // <-- CLAVE DE LA DEMOSTRACIÓN

            // 2. MODIFICAR (MODIFY)
            int newStock = product.getStock() - quantity;
            product.setStock(newStock);

            // 3. ESCRIBIR (WRITE)
            // El método updateProduct() abre OTRA conexión y actualiza.
            updateProduct(product);
            
            System.out.println(Thread.currentThread().getName() + " procesó. Stock ahora (teóricamente): " + newStock);
        } else {
            System.out.println(Thread.currentThread().getName() + " no pudo procesar (sin stock).");
        }
    }

    /**
     * IMPLEMENTACIÓN "CORRECTA" (Transaccional)
     * Usa una sola conexión, setAutoCommit(false) y SELECT ... FOR UPDATE.
     */
    @Override
    public void deductStockTransactional(int id, int quantity) {
        String selectForUpdate = "SELECT id, stock FROM products WHERE id = ? FOR UPDATE";
        String updateStock = "UPDATE products SET stock = ? WHERE id = ?";
        
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            // 1. Iniciar Transacción
            conn.setAutoCommit(false);

            int currentStock = -1;

            // 2. LEER Y BLOQUEAR LA FILA
            // H2 (y otras DBs) bloquearán esta fila hasta que la transacción haga commit o rollback.
            // Otros hilos que intenten un "SELECT ... FOR UPDATE" esperarán aquí.
            try (PreparedStatement selectStmt = conn.prepareStatement(selectForUpdate)) {
                selectStmt.setInt(1, id);
                try (ResultSet rs = selectStmt.executeQuery()) {
                    if (rs.next()) {
                        currentStock = rs.getInt("stock");
                    } else {
                        throw new SQLException("Producto no encontrado, rollback.");
                    }
                }
            }

            // 3. MODIFICAR (en memoria)
            if (currentStock >= quantity) {
                int newStock = currentStock - quantity;

                // 4. ESCRIBIR
                try (PreparedStatement updateStmt = conn.prepareStatement(updateStock)) {
                    updateStmt.setInt(1, newStock);
                    updateStmt.setInt(2, id);
                    updateStmt.executeUpdate();
                }
                System.out.println(Thread.currentThread().getName() + " procesó. Stock actualizado a: " + newStock);
            } else {
                System.out.println(Thread.currentThread().getName() + " no pudo procesar (sin stock).");
            }

            // 5. Confirmar Transacción
            conn.commit();

        } catch (SQLException e) {
            // 6. Manejar Error y Revertir
            System.err.println(Thread.currentThread().getName() + " | Error: " + e.getMessage() + ". ¡Haciendo rollback!");
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        } finally {
            // 7. Cerrar conexión
            if (conn != null) {
                try {
                    conn.setAutoCommit(true); // Restaurar estado
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}