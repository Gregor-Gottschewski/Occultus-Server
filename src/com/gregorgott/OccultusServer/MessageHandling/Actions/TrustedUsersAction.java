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
import com.google.gson.JsonElement;
import com.gregorgott.OccultusServer.DatabaseManager;
import com.gregorgott.OccultusServer.MessageHandling.Exceptions.NotEnoughParametersException;
import com.gregorgott.OccultusServer.MessageHandling.Exceptions.UserDataException;
import com.gregorgott.OccultusServer.MessageHandling.Message;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;

public class TrustedUsersAction extends Action {
    public TrustedUsersAction(Message message) {
        super(message);
    }

    public JsonArray getTrustedUsers() throws SQLException, UserDataException {
        checkUserValidity();

        return getTrustedUsersArray(getMessage().getUser());
    }

    public JsonArray getVerifiedUsers() throws SQLException, UserDataException {
        checkUserValidity();

        // Rückgabewert
        JsonArray trustedUsersArray = getTrustedUsersArray(
                getMessage().getUser());

        for (int i = 0; i < trustedUsersArray.size(); i++) {
            JsonArray trustedUsersOfUser = getTrustedUsersArray(
                    trustedUsersArray.get(i).getAsString());
            for (JsonElement element : trustedUsersOfUser) {
                if (!trustedUsersArray.contains(element)) {
                    trustedUsersArray.add(element.getAsString());
                }
            }
        }

        return trustedUsersArray;
    }

    public void addTrustedUser() throws SQLException, UserDataException, NotEnoughParametersException {
        checkUserValidity();

        checkNeededParameters(new String[]{"email"});

        JsonArray trustedUsers = getTrustedUsersArray(getMessage().getUser());
        trustedUsers.add(getMessage().getParameters().get("email").getAsString());
        setTrustedUsers(trustedUsers.toString());
    }

    public void removeTrustedUser() throws SQLException, UserDataException, NotEnoughParametersException {
        checkUserValidity();

        checkNeededParameters(new String[]{"email"});

        JsonArray trustedUsers = getTrustedUsersArray(getMessage().getUser());

        for (int i = 0; i < trustedUsers.size(); i++) {
            if (Objects.equals(trustedUsers.get(i).getAsString(), getMessage().getParameters().get("email").getAsString())) {
                trustedUsers.remove(i);
            }
        }
    }

    private JsonArray getTrustedUsersArray(String email) throws SQLException {
        // Return element
        JsonArray trustedUsersArray = new JsonArray();

        // Connection
        Connection connection = new DatabaseManager().getConnection();

        // PreparedStatement
        PreparedStatement preparedStatement = connection.prepareStatement(
                "SELECT trusted_users FROM Users WHERE email = ?;"
        );
        preparedStatement.setString(1, email);

        // ResultSet
        ResultSet resultSet = preparedStatement.executeQuery();
        if (resultSet.next()) {
            String s = resultSet.getString("trusted_users");
            if (s != null) {
                trustedUsersArray = new Gson().fromJson(s, JsonArray.class);
            }
        }

        // Release resources
        resultSet.close();
        preparedStatement.close();
        connection.close();

        return trustedUsersArray;
    }

    private void setTrustedUsers(String trustedUserString) throws SQLException {
        // Connection
        Connection connection = new DatabaseManager().getConnection();

        // PreparedStatement
        PreparedStatement preparedStatement = connection.prepareStatement(
                "UPDATE Users SET trusted_users = ? WHERE email = ? AND password = ?"
        );
        preparedStatement.setString(1, trustedUserString);
        preparedStatement.setString(2, getMessage().getUser());
        preparedStatement.setString(3, getMessage().getPassword());
        preparedStatement.executeUpdate();

        // Release resources
        preparedStatement.close();
        connection.close();
    }
}
