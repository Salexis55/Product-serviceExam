package com.alejandro.productservice.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DBConnection {

	// Base de datos H2 en memoria.
	// DB_CLOSE_DELAY=-1 mantiene los datos mientras la JVM esté activa.
	private static final String URL = "jdbc:mysql://localhost:3306/examen";
	private static final String USER = "root";
	private static final String PASSWORD = "Admin";

	static {
		try {
			// Carga el driver de H2 (necesario para crear conexiones JDBC)
			Class.forName("org.h2.Driver");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			throw new RuntimeException("Error al cargar el driver H2");
		}
	}

	// Retorna una nueva conexión con la base de datos H2
	public static Connection getConnection() throws SQLException {
		return DriverManager.getConnection(URL, USER, PASSWORD);
	}

	// Crea la tabla "products" si no existe y limpia su contenido
	public static void setupDatabase() {
		// Script SQL para crear la tabla
		String createTableSQL = "CREATE TABLE IF NOT EXISTS products (" 
				+ "id INT PRIMARY KEY, "
				+ "productCode VARCHAR(255) NOT NULL, "
				+ "stock INT, "
				+ "warehouse VARCHAR(255)" 
				+ ")";

		// try-with-resources: cierra la conexión y el statement automáticamente
		try (Connection conn = getConnection(); 
			 PreparedStatement stmt = conn.prepareStatement(createTableSQL)) {

			// Crea la tabla si no existe
			stmt.execute(createTableSQL);

			// Limpia los registros anteriores (para reiniciar las pruebas)
			stmt.execute("DELETE FROM products");

			System.out.println("Base de datos y tabla 'products' inicializadas.");

		} catch (SQLException e) {
			e.printStackTrace();
			throw new RuntimeException("No se pudo inicializar la base de datos");
		}
	}
}