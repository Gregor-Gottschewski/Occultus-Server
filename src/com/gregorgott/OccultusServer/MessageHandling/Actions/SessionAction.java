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

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.gregorgott.OccultusServer.DatabaseManager;
import com.gregorgott.OccultusServer.MessageHandling.Exceptions.NotEnoughParametersException;
import com.gregorgott.OccultusServer.MessageHandling.Exceptions.UserDataException;
import com.gregorgott.OccultusServer.MessageHandling.Message;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SessionAction extends Action {
    public SessionAction(Message message) {
        super(message);
    }

    public void addSession() throws SQLException, UserDataException, NotEnoughParametersException {
        checkUserValidity();
        checkNeededParameters(new String[]{"session_name"});

        JsonArray sessionsArray = getSessionsArray(getMessage().getUser());
        assert sessionsArray != null;
        sessionsArray.add(getMessage().getParameters().get("session_name").getAsString());

        Connection connection = new DatabaseManager().getConnection();

        PreparedStatement preparedStatement = connection.prepareStatement("""
            UPDATE Users SET sessions = ? WHERE email = ?;
        """);
        preparedStatement.setString(1, sessionsArray.toString());
        preparedStatement.setString(2, getMessage().getUser());
        preparedStatement.executeUpdate();

        // Ressourcen aufräumen
        preparedStatement.close();
        connection.close();
    }

    protected JsonArray getSessionsArray(String user) throws SQLException {
        // Rückgabewert
        JsonArray array = new JsonArray();

        Connection connection = new DatabaseManager().getConnection();

        PreparedStatement preparedStatement = connection.prepareStatement("""
            SELECT sessions FROM Users WHERE email = ?;
        """);
        preparedStatement.setString(1, user);

        ResultSet resultSet = preparedStatement.executeQuery();
        if (resultSet.next()) {
            array = new Gson().fromJson(resultSet.getString(1), JsonArray.class);
        }

        // Ressourcen aufräumen
        resultSet.close();
        preparedStatement.close();
        connection.close();

        return array;
    }
}
