package com.alejandro.productservice.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import com.mysql.cj.xdevapi.Statement;

public class DBConnection {

	// Usamos H2 en memoria. DB_CLOSE_DELAY=-1 evita que la DB se borre
	// mientras la JVM esté activa.
	private static final String URL = "jdbc:h2:mem:productservice;DB_CLOSE_DELAY=-1";
	private static final String USER = "sa";
	private static final String PASSWORD = "";

	static {
		try {
			// Cargar el driver de H2
			Class.forName("org.h2.Driver");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			throw new RuntimeException("Error al cargar el driver H2");
		}
	}

	public static Connection getConnection() throws SQLException {
		return DriverManager.getConnection(URL, USER, PASSWORD);
	}

	// Método para inicializar el schema de la BD
	public static void setupDatabase() {
		String createTableSQL = "CREATE TABLE IF NOT EXISTS products (" + "id INT PRIMARY KEY, "
				+ "productCode VARCHAR(255) NOT NULL, " + "stock INT, " + "warehouse VARCHAR(255)" + ")";

		try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {

			stmt.execute(createTableSQL);
			// Limpiamos la tabla para cada ejecución de la demo
			stmt.execute("DELETE FROM products");
			System.out.println("Base de datos y tabla 'products' inicializadas.");

		} catch (SQLException e) {
			e.printStackTrace();
			throw new RuntimeException("No se pudo inicializar la base de datos");
		}
	}
}
