package db.DAO;

import db.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Scanner;

public class ExcursieDAO {
    public static void adaugaExcursie() throws SQLException {
        try (Connection connection = DBConnection.getConnection();
             Scanner scanner = new Scanner(System.in)) {

            System.out.print("Introduceți ID-ul persoanei: ");
            int idPersoana = scanner.nextInt();

            System.out.print("Introduceți destinația: ");
            scanner.nextLine(); // consume newline
            String destinatia = scanner.nextLine();

            System.out.print("Introduceți anul excursiei: ");
            int anul = scanner.nextInt();

            // if daca persoana exista
            String verificareSql = "SELECT COUNT(*) FROM persoane WHERE id = ?";
            try (PreparedStatement verificareStmt = connection.prepareStatement(verificareSql)) {
                verificareStmt.setInt(1, idPersoana);
                var resultSet = verificareStmt.executeQuery();
                resultSet.next();
                if (resultSet.getInt(1) == 0) {
                    System.out.println("Persoana nu există în baza de date.");
                    return;
                }
            }

            // adauga excursie
            String sql = "INSERT INTO excursii (id_persoana, destinatia, anul) VALUES (?, ?, ?)";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setInt(1, idPersoana);
                statement.setString(2, destinatia);
                statement.setInt(3, anul);
                statement.executeUpdate();
                System.out.println("Excursia a fost adăugată cu succes.");
            }

        } catch (SQLException e) {
            System.err.println("Eroare: " + e.getMessage());
        }
    }
}

