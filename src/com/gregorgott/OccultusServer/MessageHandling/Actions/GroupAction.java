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
import com.google.gson.JsonPrimitive;
import com.gregorgott.OccultusServer.DatabaseManager;
import com.gregorgott.OccultusServer.MessageHandling.Exceptions.UserNotMemberOfGroupException;
import com.gregorgott.OccultusServer.MessageHandling.Exceptions.NotEnoughParametersException;
import com.gregorgott.OccultusServer.MessageHandling.Exceptions.UserDataException;
import com.gregorgott.OccultusServer.MessageHandling.Message;

import javax.naming.NoPermissionException;
import java.sql.*;

public class GroupAction extends Action {

    protected static boolean isUserNotInGroup(int groupId, String user) throws SQLException {
        boolean b = false;

        // Connection
        Connection connection = new DatabaseManager().getConnection();

        // PreparedStatement
        PreparedStatement preparedStatement = connection.prepareStatement("""
                SELECT members FROM Messenger_Groups WHERE group_id = ?;
        """);
        preparedStatement.setInt(1, groupId);

        // ResultSet
        ResultSet resultSet = preparedStatement.executeQuery();
        if (resultSet.next()) {
            JsonArray jsonArray = new Gson().fromJson(resultSet.getString("members"), JsonArray.class);
            b = jsonArray.contains(new JsonPrimitive(user));
        }

        // Ressourcen aufräumen
        resultSet.close();
        preparedStatement.close();
        connection.close();

        return !b;
    }

    public GroupAction(Message message) {
        super(message);
    }

    public String getGroupName() throws SQLException, UserDataException, NotEnoughParametersException, UserNotMemberOfGroupException {
        checkUserValidity();
        checkNeededParameters(new String[]{"group_id"});

        int groupId = getMessage().getParameters().get("group_id").getAsInt();

        if (isUserNotInGroup(groupId, getMessage().getUser())) {
            throw new UserNotMemberOfGroupException(groupId, getMessage().getUser());
        }

        // Rückgabewert
        String s = "";

        // Connection
        Connection connection = new DatabaseManager().getConnection();

        // Prepared Statement
        PreparedStatement preparedStatement = connection.prepareStatement("""
            SELECT group_name FROM Messenger_Groups WHERE group_id = ?;
        """);
        preparedStatement.setInt(1, groupId);

        ResultSet resultSet = preparedStatement.executeQuery();

        if (resultSet.next()) {
            s = resultSet.getString("group_name");
        }

        // Ressourcen freigeben
        resultSet.close();
        preparedStatement.close();
        connection.close();

        return s;
    }

    public synchronized int createGroup() throws SQLException, UserDataException, NotEnoughParametersException {
        checkUserValidity();
        checkNeededParameters(new String[]{"group_name", "members"});

        int groupId = -1;

        String groupName = getMessage().getParameters().get("group_name").getAsString();
        JsonArray membersArray = getMessage().getParameters().get("members").getAsJsonArray();
        JsonArray adminsArray = new JsonArray();

        adminsArray.add(new JsonPrimitive(getMessage().getUser()));
        membersArray.add(new JsonPrimitive(getMessage().getUser()));

        for (JsonElement e : membersArray) {
            if (!isUserExistent(e.getAsString())) {
                throw new UserDataException(e.getAsString());
            }
        }

        // Connection
        Connection connection = new DatabaseManager().getConnection();

        // Prepared Statement
        PreparedStatement preparedStatement = connection.prepareStatement("""
            INSERT INTO Messenger_Groups (group_name, members, group_admins) VALUES (?, ?, ?);
        """);
        preparedStatement.setString(1, groupName);
        preparedStatement.setString(2, membersArray.toString());
        preparedStatement.setString(3, adminsArray.toString());
        preparedStatement.executeUpdate();

        // ResultSet - groupId
        ResultSet resultSet = preparedStatement.executeQuery("SELECT LAST_INSERT_ID();");
        if (!resultSet.next()) {
            throw new SQLException();
        }

        groupId = resultSet.getInt(1);

        // Ressourcen freigeben
        resultSet.close();
        preparedStatement.close();
        connection.close();

        return groupId;
    }

    public void deleteGroup() throws SQLException, UserDataException, NotEnoughParametersException, NoPermissionException {
        checkUserValidity();
        checkNeededParameters(new String[]{"group_id"});

        int groupId = getMessage().getParameters().get("group_id").getAsInt();

        if (!isUserGroupAdmin(groupId)) {
            throw new NoPermissionException("Client ist nicht Gruppenadmin.");
        }

        // Connection
        Connection connection = new DatabaseManager().getConnection();

        // Prepared Statement
        PreparedStatement preparedStatement = connection.prepareStatement("""
            DELETE FROM Messenger_Groups WHERE group_id = ?;
        """);
        preparedStatement.setInt(1, groupId);
        preparedStatement.executeUpdate();

        // Ressourcen freigeben
        preparedStatement.close();
        connection.close();
    }

    public JsonArray getMembers() throws SQLException, UserDataException, NotEnoughParametersException, NoPermissionException {
        checkUserValidity();
        checkNeededParameters(new String[]{"group_id"});

        JsonArray jsonArray = new JsonArray();

        int groupId = getMessage().getParameters().get("group_id").getAsInt();

        if (isUserNotInGroup(groupId, getMessage().getUser())) {
            throw new NoPermissionException(getMessage().getUser() + " nicht Mitglied von Gruppe mit ID " + groupId);
        }

        // Connection
        Connection connection = new DatabaseManager().getConnection();

        // Prepared Statement
        PreparedStatement preparedStatement = connection.prepareStatement("""
            SELECT members FROM Messenger_Groups WHERE group_id = ?;
        """);
        preparedStatement.setInt(1, groupId);

        // ResultSet
        ResultSet resultSet = preparedStatement.executeQuery();
        if (resultSet.next()) {
            jsonArray = new Gson().fromJson(resultSet.getString("members"), JsonArray.class);
        }

        // Ressourcen freigeben
        resultSet.close();
        preparedStatement.close();
        connection.close();

        return jsonArray;
    }

    private boolean isUserExistent(String user) throws SQLException {
        // Connection
        Connection connection = new DatabaseManager().getConnection();

        // Prepared Statement
        PreparedStatement preparedStatement = connection.prepareStatement("""
            SELECT email FROM Users WHERE email = ?;
        """);
        preparedStatement.setString(1, user);

        // ResultSet
        ResultSet resultSet = preparedStatement.executeQuery();
        boolean b = resultSet.next();

        // Ressourcen aufräumen
        resultSet.close();
        preparedStatement.close();
        connection.close();

        return b;
    }

    private boolean isUserGroupAdmin(int groupId) throws SQLException, UserDataException {
        checkUserValidity();

        boolean b = false;

        // Connection
        Connection connection = new DatabaseManager().getConnection();

        // Prepared Statement
        PreparedStatement preparedStatement = connection.prepareStatement("""
            SELECT group_admins FROM Messenger_Groups WHERE group_id = ?;
        """);
        preparedStatement.setInt(1, groupId);

        // ResultSet
        ResultSet resultSet = preparedStatement.executeQuery();
        if (resultSet.next()) {
            JsonArray adminArray = new Gson().fromJson(resultSet.getString("group_admins"), JsonArray.class);
            b = adminArray.contains(new JsonPrimitive(getMessage().getUser()));
        }

        // Ressourcen freigeben
        resultSet.close();
        preparedStatement.close();
        connection.close();

        return b;
    }
}
