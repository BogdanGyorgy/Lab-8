package db.DAO;

import db.DBConnection;
import error.ExceptieVarsta;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Scanner;

public class PersoaneDAO {
    public static void adaugaPersoana() {
        try (Connection connection = DBConnection.getConnection();
             Scanner scanner = new Scanner(System.in)) {

            System.out.print("Introduceți numele: ");
            String nume = scanner.nextLine();

            System.out.print("Introduceți vârsta: ");
            int varsta = scanner.nextInt();

            if (varsta < 0 || varsta > 120) {
                throw new ExceptieVarsta("Vârsta trebuie să fie între 0 și 120.");
            }

            String sql = "INSERT INTO persoane (nume, varsta) VALUES (?, ?)";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, nume);
                statement.setInt(2, varsta);
                statement.executeUpdate();
                System.out.println("Persoana a fost adăugată cu succes.");
            }

        } catch (ExceptieVarsta | SQLException e) {
            System.err.println("Eroare: " + e.getMessage());
        }
    }
}
