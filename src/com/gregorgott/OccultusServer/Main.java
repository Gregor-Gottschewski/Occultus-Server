/*
 * MIT License
 *
 * Copyright (c) 2023 Gregor Gottschewski
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.gregorgott.OccultusServer;

import java.net.*;
import java.io.*;
import java.sql.*;
import java.util.Objects;
import java.util.Scanner;
import java.util.logging.Level;

public class Main {

    public static void main(String[] args) {
        System.out.println("Version: 0.0.1");
        System.out.println("FOR EDUCATIONAL USE ONLY!");

        int port = 2100;

        // args
        for (int i = 0; i < args.length; i++) {
            if (i + 1 <= args.length) {
                String next = args[i + 1];
                if (Objects.equals(args[i], "--port") || Objects.equals(args[i], "-p")) {
                    port = Integer.parseInt(next);
                    if (port < 1023 || port > 49152) {
                        Log.log(Level.SEVERE, String.format("Can't listen on port %d!", port));
                    }
                } else if (Objects.equals(args[i], "--password") || Objects.equals(args[i], "-ps")) {
                    DatabaseManager.password = next;
                }

                i++;
            }
        }

        if (args.length == 0) {
            welcome();
        } else {
            System.out.println("Started with parameters.");
        }

        if (!checkDatabaseConnection()) {
            welcome();
        }

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            Log.log(Level.INFO, "Server running on port " + port + ".");
            while (true) {
                new ServerThread(serverSocket.accept()).start();
                Log.log(Level.INFO, "New connection");
            }
        } catch (IOException e) {
            Log.log(Level.SEVERE, "Can't listen on port " + port + ".");
            System.exit(1);
        }
    }

    private static void welcome() {
        loop:
        while (true) {
            System.out.println("""
                    Welcome!
                        (C)reate new Occultus database.
                        (D)elete Occultus database.
                        (E)nter database password and start server.
                        (H)elp and License
                        (Q)uit
                    """);

            System.out.print("> ");
            Scanner sc = new Scanner(System.in);
            String in = sc.next().toUpperCase();
            switch (in) {
                case "Q":
                    System.exit(0);
                case "H":
                    System.out.println("""
                            Occultus server is a Java-based server application to demonstrate PGP in communication.
                            Visit https://github.com/Gregor-Gottschewski/Occultus-Server for more information.
                                                        
                            Copyright notice:
                                MIT License
                                                            
                                Copyright (c) 2023 Gregor Gottschewski
                                                            
                                Permission is hereby granted, free of charge, to any person obtaining a copy
                                of this software and associated documentation files (the "Software"), to deal
                                in the Software without restriction, including without limitation the rights
                                to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
                                copies of the Software, and to permit persons to whom the Software is
                                furnished to do so, subject to the following conditions:
                                                            
                                The above copyright notice and this permission notice shall be included in all
                                copies or substantial portions of the Software.
                                                            
                                THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
                                IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
                                FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
                                AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
                                LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
                                OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
                                SOFTWARE.
                                                        
                            Author: Gregor Gottschewski
                            Version: 0.0.1
                            Year: 2023
                            """);
                    break;
                case "C":
                    try {
                        createDatabase();
                        break loop;
                    } catch (SQLException e) {
                        System.err.println(e.getMessage());
                        System.exit(1);
                    }
                case "E":
                    System.out.print("Occultus database password: ");
                    DatabaseManager.password = sc.next();
                    break loop;
                case "D":
                    try {
                        deleteDatabase();
                    } catch (SQLException e) {
                        System.err.println(e.getMessage());
                    }
                    break;
            }
        }
    }

    private static boolean checkDatabaseConnection() {
        try {
            DriverManager.getConnection(DatabaseManager.DB_URL, DatabaseManager.DB_USERNAME, DatabaseManager.password);
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    private static void createDatabase() throws SQLException {
        // root password
        System.out.print("\nPlease enter the root's database password: ");
        String rootPassword = new Scanner(System.in).next();

        // Connection
        Connection connection = DriverManager.getConnection(DatabaseManager.DB_URL, "root", rootPassword);
        Log.log(Level.INFO, "Step 1/7: Connected to MySQL.");

        // database password
        System.out.print("\nSet a password for the Occultus database user: ");
        DatabaseManager.password = new Scanner(System.in).next();

        // statement
        Statement statement = connection.createStatement();

        // create new db user
        statement.executeUpdate(String.format(
                "CREATE USER '%s'@'localhost' IDENTIFIED WITH mysql_native_password BY '%s';",
                DatabaseManager.DB_USERNAME, DatabaseManager.password));
        Log.log(Level.INFO, "Step 2/7: Created a new database user.");

        // create new database
        statement.executeUpdate(String.format("CREATE DATABASE %s;", DatabaseManager.DB_NAME));
        Log.log(Level.INFO, "Step 2/7: New database created.");

        // use created database
        statement.executeUpdate(String.format("USE %s;", DatabaseManager.DB_NAME));

        // create 'Users' table
        statement.executeUpdate("""
                    CREATE TABLE Users (
                        user_id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
                        email TEXT NOT NULL,
                        password TEXT NOT NULL,
                        public_key TEXT,
                        sessions TEXT,
                        trusted_users TEXT
                    );
                """);
        Log.log(Level.INFO, "Step 3/7: 'Users' table generated.");

        // create 'Messenger_Groups' table
        statement.executeUpdate("""
                    CREATE TABLE Messenger_Groups (
                        group_id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
                        group_name TEXT NOT NULL,
                        members TEXT NOT NULL,
                        group_admins TEXT NOT NULL
                    );
                """);
        Log.log(Level.INFO, "Step 4/7: 'Messenger_Groups' table generated.");

        // create 'Messages' table
        statement.executeUpdate("""
                    CREATE TABLE Messages (
                        message_id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
                        message_type TINYTEXT NOT NULL,
                        recipient TEXT NOT NULL,
                        sender TEXT NOT NULL,
                        group_id INT,
                        message TEXT NOT NULL,
                        session_name TEXT
                    );
                """);
        Log.log(Level.INFO, "Step 5/7: 'Messages' table generated.");

        // set privileges
        statement.executeUpdate(String.format("GRANT ALL PRIVILEGES ON %s.* TO '%s'@'localhost';",
                DatabaseManager.DB_NAME, DatabaseManager.DB_USERNAME));
        Log.log(Level.INFO, "Step 6/7: Privileges set.");

        // Release resources
        statement.close();
        connection.close();
        Log.log(Level.INFO, "Step 7/7: Connection closed.");

        Log.log(Level.INFO, "Database created.");
    }

    private static void deleteDatabase() throws SQLException {
        // root password
        System.out.print("\nPlease enter the root's database password: ");
        String rootPassword = new Scanner(System.in).next();

        // Connection
        Connection connection = DriverManager.getConnection(DatabaseManager.DB_URL, "root", rootPassword);

        // statement
        Statement statement = connection.createStatement();

        statement.executeUpdate(String.format("DROP USER '%s'@'localhost';", DatabaseManager.DB_USERNAME));
        statement.executeUpdate(String.format("DROP DATABASE %s;", DatabaseManager.DB_NAME));

        statement.close();
        connection.close();

        System.out.println("Database successfully deleted.");
    }

}
