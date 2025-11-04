package com.alejandro.productservice.test;

import java.util.concurrent.*; // Para manejo de hilos y concurrencia
import com.alejandro.productservice.dao.*;
import com.alejandro.productservice.model.Product;
import com.alejandro.productservice.util.DBConnection;

/**
 * Clase de prueba que simula concurrencia al descontar stock.
 * Compara resultados con y sin transacciones.
 */
public class ProductServiceTest {

    // Parámetros de simulación
    private static final int STOCK_INICIAL = 1000;
    private static final int HILOS = 10;
    private static final int TAREAS_POR_HILO = 10;
    private static final int CANTIDAD_A_DESCONTAR = 1;

    public static void main(String[] args) throws InterruptedException {

        // 1. Crear base de datos en memoria y DAO
        DBConnection.setupDatabase();
        ProductDAO dao = new ProductDAOImpl();

        // 2. Prueba sin transacciones (esperar inconsistencia)
        runSimulation(dao, false);

        // 3. Prueba con transacciones (resultado consistente)
        runSimulation(dao, true);
    }

    // Ejecuta una simulación de concurrencia
    private static void runSimulation(ProductDAO dao, boolean conTransaccion) throws InterruptedException {

        // Insertar producto base antes de iniciar
        dao.addProduct(new Product(1, "SKU-001", STOCK_INICIAL, "WH-A"));

        System.out.println("\n=======================================================");
        System.out.printf("iniciando simulacion: %s transacciones\n", conTransaccion ? "con" : "sin");
        System.out.println("=======================================================");

        // Crear un pool de hilos para ejecutar tareas simultáneamente
        ExecutorService executor = Executors.newFixedThreadPool(HILOS);

        // Lanzar tareas concurrentes de descuento
        for (int i = 0; i < HILOS * TAREAS_POR_HILO; i++) {
            executor.submit(() -> {
                try {
                    if (conTransaccion)
                        dao.deductStockTransactional(1, CANTIDAD_A_DESCONTAR);
                    else
                        dao.deductStockNonTransactional(1, CANTIDAD_A_DESCONTAR);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }

        // Esperar que terminen todas las tareas
        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.MINUTES);

        System.out.println("simulacion finalizada");

        // 4. Verificar resultados
        Product productoFinal = dao.getProductById(1);
        int stockFinal = productoFinal.getStock();
        int stockEsperado = STOCK_INICIAL - (HILOS * TAREAS_POR_HILO * CANTIDAD_A_DESCONTAR);

        System.out.println("Stock Final en BD: " + stockFinal);

        if (stockFinal == stockEsperado)
            System.out.println(" Resultado correcto: datos consistentes.");
        else
            System.out.printf(" Inconsistencia detectada: se perdieron %d actualizaciones.\n",
                    stockEsperado - stockFinal);

        // Eliminar producto para la próxima ejecución
        dao.deleteProduct(1);
    }
}