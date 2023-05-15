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

import java.sql.*;

public class DatabaseManager {
    protected static final String DB_URL = "jdbc:mysql://localhost:3306/";
    protected static final String DB_NAME = "occultus_messenger";
    protected static final String DB_USERNAME = "occultus-user";
    protected static String password = "";
    private final Connection connection;

    public DatabaseManager() throws SQLException {
        connection = DriverManager.getConnection(DB_URL + DB_NAME, DB_USERNAME, password);
    }

    public Connection getConnection() {
        return connection;
    }
}
