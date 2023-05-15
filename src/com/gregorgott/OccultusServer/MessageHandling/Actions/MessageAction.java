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
import com.gregorgott.OccultusServer.DatabaseManager;
import com.gregorgott.OccultusServer.MessageHandling.Exceptions.UserNotMemberOfGroupException;
import com.gregorgott.OccultusServer.MessageHandling.Exceptions.NotEnoughParametersException;
import com.gregorgott.OccultusServer.MessageHandling.Exceptions.UserDataException;
import com.gregorgott.OccultusServer.MessageHandling.Message;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class MessageAction extends Action {
    public MessageAction(Message message) {
        super(message);
    }

    public void sendMessage() throws SQLException, NotEnoughParametersException, UserDataException,
            UserNotMemberOfGroupException, IllegalArgumentException {
        checkUserValidity();
        checkNeededParameters(new String[]{"message_type", "recipient", "message"});

        // benötigte Elemente
        String recipient = getMessage().getParameters().get("recipient").getAsString();
        String messageToSend = getMessage().getParameters().get("message").getAsString();
        String messageType = getMessage().getParameters().get("message_type").getAsString();

        // Connection
        Connection connection = new DatabaseManager().getConnection();

        JsonArray sessionArray = new SessionAction(getMessage()).getSessionsArray(recipient);
        for (int i = 0; i <sessionArray.size(); i++) {
            // PreparedStatement
            PreparedStatement preparedStatement = connection.prepareStatement("""
                    INSERT INTO Messages (recipient, sender, message, message_type, group_id, session_name) VALUES (?, ?, ?, ?, ?, ?);
                    """);
            preparedStatement.setString(1, recipient);
            preparedStatement.setString(2, getMessage().getUser());
            preparedStatement.setString(3, messageToSend);
            preparedStatement.setString(4, messageType);
            if (messageType.equals("group")) {
                checkNeededParameters(new String[]{"group_id"});
                int groupId = getMessage().getParameters().get("group_id").getAsInt();
                checkIfUserIsMemberOf(groupId);
                preparedStatement.setInt(5, groupId);
            } else {
                preparedStatement.setNull(5, 0);
            }
            preparedStatement.setString(6, sessionArray.get(i).getAsString());
            preparedStatement.executeUpdate();

            // Release resources
            preparedStatement.close();
        }


        connection.close();
    }

    private void checkIfUserIsMemberOf(int groupId) throws UserNotMemberOfGroupException, SQLException {
        if (GroupAction.isUserNotInGroup(groupId, getMessage().getUser())) {
            throw new UserNotMemberOfGroupException(groupId, getMessage().getUser());
        }
    }
}
