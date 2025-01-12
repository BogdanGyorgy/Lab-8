import db.DAO.ExcursieDAO;
import db.DAO.PersoaneDAO;
import db.DBConnection;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Scanner;

import java.util.Scanner;

import java.sql.*;
import java.util.Scanner;

public class MainApp {
    // Conexiunea la baza de date
    private static final String URL = "jdbc:postgresql://localhost:5432/lab8";
    private static final String USER = "postgres";
    private static final String PASSWORD = "parola123";

    public static void main(String[] args) {
        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             Scanner scanner = new Scanner(System.in)) {

            while (true) {
                System.out.println("\nMeniu:");
                System.out.println("1. Adaugare persoana");
                System.out.println("2. Adaugare excursie");
                System.out.println("3. Afisare persoane si excursii");
                System.out.println("4. Afisare excursii pentru o persoana dupa nume");
                System.out.println("5. Afisare persoanele care au vizitat o destinatie");
                System.out.println("6. Afisare persoanele care au facut excursii într-un an");
                System.out.println("7. Stergerea unei excursii");
                System.out.println("8. Stergerea unei persoane (impreuna cu excursiile in care a fost)");
                System.out.println("0. Iesire");
                System.out.print("Alegeti o optiune: ");

                int optiune;
                try {
                    optiune = scanner.nextInt();
                    scanner.nextLine();
                } catch (Exception e) {
                    System.out.println("Eroare: Va rugam sa introduceti o optiune valida (numar intreg).");
                    scanner.nextLine();
                    continue;
                }

                switch (optiune) {
                    case 1 -> adaugaPersoana(connection, scanner);
                    case 2 -> adaugaExcursie(connection, scanner);
                    case 3 -> afisarePersoaneSiExcursii(connection);
                    case 4 -> afisareExcursiiPentruPersoana(connection, scanner);
                    case 5 -> afisarePersoaneCareAuVizitatDestinatie(connection, scanner);
                    case 6 -> afisarePersoaneCareAuFacutExcursiiIntrUnAn(connection, scanner);
                    case 7 -> stergeExcursie(connection, scanner);
                    case 8 -> stergePersoana(connection, scanner);
                    case 0 -> {
                        return;
                    }
                    default -> System.out.println("Optiune invalida. Alegeti din nou.");
                }
            }
        } catch (SQLException e) {
            System.out.println("Eroare la conectarea la baza de date: " + e.getMessage());
        }
    }

    private static void adaugaPersoana(Connection connection, Scanner scanner) {
        try {
            System.out.print("Introduceti numele: ");
            String nume = scanner.nextLine();

            System.out.print("Introduceti varsta: ");
            int varsta;
            try {
                varsta = scanner.nextInt();
                scanner.nextLine(); // Consuma newline-ul
                if (varsta < 0 || varsta > 120) {
                    throw new IllegalArgumentException("Varsta trebuie sa fie intre 0 si 120.");
                }
            } catch (Exception e) {
                System.out.println("Eroare: Varsta trebuie sa fie un numar intreg valid.");
                scanner.nextLine(); // Curata buffer-ul
                return;
            }

            String sql = "INSERT INTO persoane (nume, varsta) VALUES (?, ?)";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, nume);
                statement.setInt(2, varsta);
                statement.executeUpdate();
                System.out.println("Persoana a fost adaugata cu succes.");
            }
        } catch (SQLException e) {
            System.out.println("Eroare la adaugarea persoanei: " + e.getMessage());
        }
    }

    private static void adaugaExcursie(Connection connection, Scanner scanner) {
        try {
            System.out.print("Introduceti ID-ul persoanei: ");
            int idPersoana;
            try {
                idPersoana = scanner.nextInt();
                scanner.nextLine(); // Consuma newline-ul
            } catch (Exception e) {
                System.out.println("Eroare: ID-ul persoanei trebuie sa fie un numar intreg valid.");
                scanner.nextLine(); // Curata buffer-ul
                return;
            }

            // Verifica daca persoana exista in tabela persoane
            String checkSql = "SELECT COUNT(*) FROM persoane WHERE id = ?";
            try (PreparedStatement checkStmt = connection.prepareStatement(checkSql)) {
                checkStmt.setInt(1, idPersoana);
                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (rs.next() && rs.getInt(1) == 0) {
                        System.out.println("Eroare: Persoana cu ID-ul specificat nu exista.");
                        return;
                    }
                }
            }

            // Preluare date excursie
            System.out.print("Introduceti destinatia: ");
            String destinatia = scanner.nextLine();

            System.out.print("Introduceti anul excursiei: ");
            int anul;
            try {
                anul = scanner.nextInt();
                scanner.nextLine(); // Consuma newline-ul
                if (anul < 1900 || anul > 2100) {
                    throw new IllegalArgumentException("Anul excursiei trebuie sa fie intre 1900 si 2100.");
                }
            } catch (Exception e) {
                System.out.println("Eroare: Anul excursiei trebuie sa fie un numar intreg valid.");
                scanner.nextLine(); // Curata buffer-ul
                return;
            }

            // Adaugare excursie in baza de date
            String insertSql = "INSERT INTO excursii (id_persoana, destinatia, anul) VALUES (?, ?, ?)";
            try (PreparedStatement insertStmt = connection.prepareStatement(insertSql)) {
                insertStmt.setInt(1, idPersoana);
                insertStmt.setString(2, destinatia);
                insertStmt.setInt(3, anul);
                insertStmt.executeUpdate();
                System.out.println("Excursia a fost adaugata cu succes.");
            }
        } catch (SQLException e) {
            System.out.println("Eroare la adaugarea excursiei: " + e.getMessage());
        }
    }

    private static void afisarePersoaneSiExcursii(Connection connection) {
        String persoaneSql = "SELECT id, nume, varsta FROM persoane";
        String excursiiSql = "SELECT destinatia, anul FROM excursii WHERE id_persoana = ?";

        try (PreparedStatement persoaneStmt = connection.prepareStatement(persoaneSql);
             ResultSet persoaneRs = persoaneStmt.executeQuery()) {

            if (!persoaneRs.isBeforeFirst()) {
                System.out.println("Nu exista persoane in baza de date.");
                return;
            }

            while (persoaneRs.next()) {
                int idPersoana = persoaneRs.getInt("id");
                String nume = persoaneRs.getString("nume");
                int varsta = persoaneRs.getInt("varsta");

                System.out.println("\nPersoana:");
                System.out.println("ID: " + idPersoana);
                System.out.println("Nume: " + nume);
                System.out.println("Varsta: " + varsta);

                try (PreparedStatement excursiiStmt = connection.prepareStatement(excursiiSql)) {
                    excursiiStmt.setInt(1, idPersoana);

                    try (ResultSet excursiiRs = excursiiStmt.executeQuery()) {
                        if (!excursiiRs.isBeforeFirst()) {
                            System.out.println("Nu exista excursii pentru aceasta persoana.");
                        } else {
                            System.out.println("Excursii:");
                            while (excursiiRs.next()) {
                                String destinatia = excursiiRs.getString("destinatia");
                                int anul = excursiiRs.getInt("anul");
                                System.out.println("- Destinatia: " + destinatia + ", Anul: " + anul);
                            }
                        }
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("Eroare la afisarea persoanelor si excursiilor: " + e.getMessage());
        }
    }

    private static void afisareExcursiiPentruPersoana(Connection connection, Scanner scanner) {
        System.out.print("Introduceti numele persoanei: ");
        String numeCautat = scanner.nextLine();

        // Verificare dacă persoana există în baza de date
        String persoanaSql = "SELECT id FROM persoane WHERE nume = ?";
        try (PreparedStatement persoanaStmt = connection.prepareStatement(persoanaSql)) {
            persoanaStmt.setString(1, numeCautat);

            try (ResultSet persoanaRs = persoanaStmt.executeQuery()) {
                if (!persoanaRs.next()) {
                    System.out.println("Persoana cu numele \"" + numeCautat + "\" nu exista in baza de date.");
                    return;
                }

                int idPersoana = persoanaRs.getInt("id");

                // Afișarea excursiilor pentru persoana respectivă
                String excursiiSql = "SELECT destinatia, anul FROM excursii WHERE id_persoana = ?";
                try (PreparedStatement excursiiStmt = connection.prepareStatement(excursiiSql)) {
                    excursiiStmt.setInt(1, idPersoana);

                    try (ResultSet excursiiRs = excursiiStmt.executeQuery()) {
                        System.out.println("Excursiile persoanei \"" + numeCautat + "\":");

                        boolean existaExcursii = false;
                        while (excursiiRs.next()) {
                            existaExcursii = true;
                            String destinatia = excursiiRs.getString("destinatia");
                            int anul = excursiiRs.getInt("anul");
                            System.out.println(" - Destinatia: " + destinatia + ", Anul: " + anul);
                        }

                        if (!existaExcursii) {
                            System.out.println("Aceasta persoana nu are nicio excursie inregistrata.");
                        }
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("Eroare la afisarea excursiilor: " + e.getMessage());
        }
    }

    private static void afisarePersoaneCareAuVizitatDestinatie(Connection connection, Scanner scanner) {
        try {
            // Citește destinația de la utilizator
            System.out.print("Introduceți destinația: ");
            String destinatia = scanner.nextLine();

            // Verifica persoanele care au vizitat destinatia respectiva
            String sql = "SELECT p.nume, p.varsta FROM persoane p " +
                    "JOIN excursii e ON p.id = e.id_persoana " +
                    "WHERE e.destinatia = ?";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, destinatia);
                try (ResultSet rs = stmt.executeQuery()) {
                    boolean found = false;
                    while (rs.next()) {
                        String nume = rs.getString("nume");
                        int varsta = rs.getInt("varsta");
                        System.out.println("Nume: " + nume + ", Vârsta: " + varsta);
                        found = true;
                    }
                    if (!found) {
                        System.out.println("Nu au fost găsite persoane care au vizitat această destinație.");
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("Eroare la afișarea persoanelor: " + e.getMessage());
        }
    }

    private static void afisarePersoaneCareAuFacutExcursiiIntrUnAn(Connection connection, Scanner scanner) {
        try {
            // Citește anul de la utilizator
            System.out.print("Introduceți anul excursiei: ");
            int anul;
            try {
                anul = scanner.nextInt();
                scanner.nextLine();
                if (anul < 1900 || anul > 2100) {
                    throw new IllegalArgumentException("Anul trebuie să fie între 1900 și 2100.");
                }
            } catch (Exception e) {
                System.out.println("Eroare: Anul trebuie sa fie un numar intreg valid.");
                scanner.nextLine();
                return;
            }

            // Interogare pentru a obține persoanele care au facut excursii in anul respectiv
            String sql = "SELECT p.nume, p.varsta FROM persoane p " +
                    "JOIN excursii e ON p.id = e.id_persoana " +
                    "WHERE e.anul = ?";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setInt(1, anul);
                try (ResultSet rs = stmt.executeQuery()) {
                    boolean found = false;
                    while (rs.next()) {
                        String nume = rs.getString("nume");
                        int varsta = rs.getInt("varsta");
                        System.out.println("Nume: " + nume + ", Vârsta: " + varsta);
                        found = true;
                    }
                    if (!found) {
                        System.out.println("Nu au fost găsite persoane care au făcut excursii în acest an.");
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("Eroare la afișarea persoanelor: " + e.getMessage());
        }
    }

    private static void stergeExcursie(Connection connection, Scanner scanner) {
        try {
            // Citește ID-ul excursiei de la utilizator
            System.out.print("Introduceți ID-ul excursiei de șters: ");
            int idExcursie;
            try {
                idExcursie = scanner.nextInt();
                scanner.nextLine(); // Consuma newline-ul
            } catch (Exception e) {
                System.out.println("Eroare: ID-ul excursiei trebuie să fie un număr întreg valid.");
                scanner.nextLine(); // Curata buffer-ul
                return;
            }

            // Interogare pentru a verifica dacă excursia exista
            String checkSql = "SELECT COUNT(*) FROM excursii WHERE id_excursie = ?";
            try (PreparedStatement checkStmt = connection.prepareStatement(checkSql)) {
                checkStmt.setInt(1, idExcursie);
                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (rs.next() && rs.getInt(1) == 0) {
                        System.out.println("Eroare: Nu exista excursia cu ID-ul specificat.");
                        return;
                    }
                }
            }

            // Ștergerea excursiei din baza de date
            String deleteSql = "DELETE FROM excursii WHERE id_excursie = ?";
            try (PreparedStatement deleteStmt = connection.prepareStatement(deleteSql)) {
                deleteStmt.setInt(1, idExcursie);
                int rowsAffected = deleteStmt.executeUpdate();
                if (rowsAffected > 0) {
                    System.out.println("Excursia a fost stearsa cu succes.");
                } else {
                    System.out.println("Eroare: Nu s-a reusit stergerea excursiei.");
                }
            }
        } catch (SQLException e) {
            System.out.println("Eroare la stergerea excursiei: " + e.getMessage());
        }
    }

    private static void stergePersoana(Connection connection, Scanner scanner) {
        try {
            // Citește ID-ul persoanei de la utilizator
            System.out.print("Introduceti ID-ul persoanei de sters: ");
            int idPersoana;
            try {
                idPersoana = scanner.nextInt();
                scanner.nextLine();
            } catch (Exception e) {
                System.out.println("Eroare: ID-ul persoanei trebuie să fie un numar intreg valid.");
                scanner.nextLine();
                return;
            }

            // Verifica daca persoana exista
            String checkSql = "SELECT COUNT(*) FROM persoane WHERE id = ?";
            try (PreparedStatement checkStmt = connection.prepareStatement(checkSql)) {
                checkStmt.setInt(1, idPersoana);
                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (rs.next() && rs.getInt(1) == 0) {
                        System.out.println("Eroare: Nu exista persoana cu ID-ul specificat.");
                        return;
                    }
                }
            }

            // Sterge excursiile asociate persoanei
            String deleteExcursiiSql = "DELETE FROM excursii WHERE id_persoana = ?";
            try (PreparedStatement deleteExcursiiStmt = connection.prepareStatement(deleteExcursiiSql)) {
                deleteExcursiiStmt.setInt(1, idPersoana);
                deleteExcursiiStmt.executeUpdate();
                System.out.println("Excursiile persoanei au fost șterse cu succes.");
            }

            // Sterge persoana din tabela persoane
            String deletePersoanaSql = "DELETE FROM persoane WHERE id = ?";
            try (PreparedStatement deletePersoanaStmt = connection.prepareStatement(deletePersoanaSql)) {
                deletePersoanaStmt.setInt(1, idPersoana);
                int rowsAffected = deletePersoanaStmt.executeUpdate();
                if (rowsAffected > 0) {
                    System.out.println("Persoana a fost stearsa cu succes.");
                } else {
                    System.out.println("Eroare: Nu s-a reusit ștergerea persoanei.");
                }
            }
        } catch (SQLException e) {
            System.out.println("Eroare la stergerea persoanei si a excursiilor: " + e.getMessage());
        }
    }

}
