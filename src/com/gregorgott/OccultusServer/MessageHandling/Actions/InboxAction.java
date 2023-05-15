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

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.gregorgott.OccultusServer.DatabaseManager;
import com.gregorgott.OccultusServer.MessageHandling.Exceptions.NotEnoughParametersException;
import com.gregorgott.OccultusServer.MessageHandling.Exceptions.UserDataException;
import com.gregorgott.OccultusServer.MessageHandling.Message;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;

public class InboxAction extends Action {
    private static JsonObject generateGroupMessageObject(int groupId, String sender, String message) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("message_type", "group");
        jsonObject.addProperty("group_id", groupId);
        jsonObject.addProperty("sender", sender);
        jsonObject.addProperty("message", message);
        return jsonObject;
    }

    private static JsonObject generatePrivateMessageObject(String message, String sender) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("message_type", "private");
        jsonObject.addProperty("sender", sender);
        jsonObject.addProperty("message", message);
        return jsonObject;
    }

    public InboxAction(Message message) {
        super(message);
    }

    public JsonArray getInbox() throws SQLException, UserDataException, NotEnoughParametersException {
        // überprüfe Nutzerdaten
        checkUserValidity();
        checkNeededParameters(new String[]{"session_name"});

        // return Variable
        JsonArray jsonArray = new JsonArray();

        // Connection
        Connection connection = new DatabaseManager().getConnection();

        // PreparedStatement
        PreparedStatement preparedStatement = connection.prepareStatement("""
            SELECT message, message_id, sender, group_id, message_type FROM Messages WHERE recipient = ? AND session_name = ?;
        """);
        preparedStatement.setString(1, getMessage().getUser());
        preparedStatement.setString(2, getMessage().getParameters().get("session_name").getAsString());

        // ResultSet
        ResultSet resultSet = preparedStatement.executeQuery();
        while (resultSet.next()) {
            if (Objects.equals(resultSet.getString("message_type"), "group")) {
                jsonArray.add(generateGroupMessageObject(resultSet.getInt("group_id"),
                        resultSet.getString("sender"),
                        resultSet.getString("message")));
            } else {
                jsonArray.add(generatePrivateMessageObject(resultSet.getString("message"),
                        resultSet.getString("sender")));
            }

            deleteMessage(resultSet.getBigDecimal("message_id"));
        }

        // Release resources
        resultSet.close();
        preparedStatement.close();
        connection.close();

        return jsonArray;
    }

    private void deleteMessage(BigDecimal bigDecimal) throws SQLException {
        // Connection
        Connection connection = new DatabaseManager().getConnection();

        // PreparedStatement
        PreparedStatement preparedStatement = connection.prepareStatement(
                "DELETE FROM Messages WHERE message_id = ?;"
        );
        preparedStatement.setBigDecimal(1, bigDecimal);
        preparedStatement.executeUpdate();

        // Release resources
        preparedStatement.close();
        connection.close();
    }
}
