package com.alejandro.productservice.test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.alejandro.productservice.dao.ProductDAO;
import com.alejandro.productservice.dao.ProductDAOImpl;
import com.alejandro.productservice.model.Product;
import com.alejandro.productservice.util.DBConnection;

public class ProductServiceTest {

    private static final int STOCK_INICIAL = 1000;
    private static final int HILOS = 10;
    private static final int TAREAS_POR_HILO = 10; // Total de tareas = HILOS * TAREAS_POR_HILO
    private static final int CANTIDAD_A_DESCONTAR = 1; // Cada tarea descuenta 1
    
    // Total a descontar = 10 * 10 * 1 = 100
    // Stock final esperado = 1000 - 100 = 900

    public static void main(String[] args) throws InterruptedException {
        
        // 1. Inicializar la Base de Datos
        DBConnection.setupDatabase();
        ProductDAO dao = new ProductDAOImpl();

        // 2. Ejecutar Simulación SIN Transacciones
        runSimulation(dao, false);

        // 3. Ejecutar Simulación CON Transacciones
        runSimulation(dao, true);
    }

    private static void runSimulation(ProductDAO dao, boolean conTransaccion) throws InterruptedException {
        
        // (Re)Inicializar el producto para cada simulación
        dao.addProduct(new Product(1, "SKU-001", STOCK_INICIAL, "WH-A"));

        System.out.println("\n=======================================================");
        System.out.printf("🚀 INICIANDO SIMULACIÓN: %s TRANSACCIONES 🚀\n", conTransaccion ? "CON" : "SIN");
        System.out.println("=======================================================");
        System.out.printf("Stock Inicial: %d. Hilos: %d. Tareas: %d. Descuento: %d.\n",
                STOCK_INICIAL, HILOS, TAREAS_POR_HILO * HILOS, CANTIDAD_A_DESCONTAR);
        System.out.printf("Stock Final Esperado: %d\n", STOCK_INICIAL - (HILOS * TAREAS_POR_HILO * CANTIDAD_A_DESCONTAR));
        System.out.println("-------------------------------------------------------");

        
        ExecutorService executor = Executors.newFixedThreadPool(HILOS);
        
        for (int i = 0; i < HILOS * TAREAS_POR_HILO; i++) {
            executor.submit(() -> {
                try {
                    if (conTransaccion) {
                        dao.deductStockTransactional(1, CANTIDAD_A_DESCONTAR);
                    } else {
                        dao.deductStockNonTransactional(1, CANTIDAD_A_DESCONTAR);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }

        // Esperar a que todas las tareas terminen
        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.MINUTES);

        System.out.println("-------------------------------------------------------");
        System.out.println("🏁 SIMULACIÓN FINALIZADA 🏁");
        
        // 4. Verificar Resultados
        Product productoFinal = dao.getProductById(1);
        int stockFinal = productoFinal.getStock();
        int stockEsperado = STOCK_INICIAL - (HILOS * TAREAS_POR_HILO * CANTIDAD_A_DESCONTAR);

        System.out.println("Stock Final Registrado en DB: " + stockFinal);

        if (stockFinal == stockEsperado) {
            System.out.println("✅ RESULTADO: CORRECTO. La consistencia de datos se mantuvo.");
        } else {
            System.out.println("❌ RESULTADO: INCORRECTO. Se produjo inconsistencia de datos.");
            System.out.printf(" (Se perdieron %d actualizaciones)\n", stockEsperado - stockFinal);
        }

        // Limpiar para la próxima simulación
        dao.deleteProduct(1);
    }
}
