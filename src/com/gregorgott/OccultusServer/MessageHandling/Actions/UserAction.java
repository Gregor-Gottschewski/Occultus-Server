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

package com.gregorgott.OccultusServer.MessageHandling.Actions;

import com.gregorgott.OccultusServer.DatabaseManager;
import com.gregorgott.OccultusServer.MessageHandling.Exceptions.NotEnoughParametersException;
import com.gregorgott.OccultusServer.MessageHandling.Exceptions.UserAlreadyInDatabaseException;
import com.gregorgott.OccultusServer.MessageHandling.Message;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserAction extends Action {
    public UserAction(Message message) {
        super(message);
    }

    public void createNewUser() throws SQLException, NotEnoughParametersException, UserAlreadyInDatabaseException {
        checkNeededParameters(new String[]{"username", "password"});

        // benötigte Parameter
        String username = getMessage().getParameters().get("username").getAsString();
        String password = getMessage().getParameters().get("password").getAsString();

        // überprüfe, ob Nutzer bereits in Datenbank gespeichert ist
        if (isUserInDatabase(username)) {
            throw new UserAlreadyInDatabaseException(username);
        }

        // Connection
        Connection connection = new DatabaseManager().getConnection();

        // Prepared Statement
        PreparedStatement preparedStatement = connection.prepareStatement(
                "INSERT INTO Users (email, password, sessions) VALUES (?, ?, ?);"
        );
        preparedStatement.setString(1, username);
        preparedStatement.setString(2, password);
        preparedStatement.setString(3, "[]");
        preparedStatement.executeUpdate();

        // Release resources
        preparedStatement.close();
        connection.close();
    }

    private boolean isUserInDatabase(String username) throws SQLException {
        // Connection
        Connection connection = new DatabaseManager().getConnection();

        // PreparedStatement
        PreparedStatement preparedStatement = connection.prepareStatement(
                "SELECT email FROM Users WHERE email = ?;"
        );
        preparedStatement.setString(1, username);

        // ResultSet
        ResultSet result = preparedStatement.executeQuery();
        // return Wert
        boolean b = result.next();

        // Release resources
        result.close();
        preparedStatement.close();
        connection.close();

        return b;
    }
}
