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

package com.gregorgott.OccultusServer.MessageHandling.Methods;

import com.google.gson.JsonPrimitive;
import com.gregorgott.OccultusServer.Log;
import com.gregorgott.OccultusServer.MessageHandling.Actions.*;
import com.gregorgott.OccultusServer.MessageHandling.Exceptions.UserNotMemberOfGroupException;
import com.gregorgott.OccultusServer.MessageHandling.Exceptions.NotEnoughParametersException;
import com.gregorgott.OccultusServer.MessageHandling.Exceptions.UserAlreadyInDatabaseException;
import com.gregorgott.OccultusServer.MessageHandling.Exceptions.UserDataException;
import com.gregorgott.OccultusServer.MessageHandling.Message;
import com.gregorgott.OccultusServer.MessageHandling.ReturnMessage;

import java.sql.SQLException;
import java.util.logging.Level;

public class PostMethod extends Method {
    private static final String MESSAGE_ACTION = "MESSAGE";
    private static final String NEW_USER_ACTION = "NEW_USER";
    private static final String KEY = "KEY";
    private static final String TRUSTED_USER = "TRUSTED_USER";
    private static final String GROUP = "GROUP";
    private static final String SESSION = "SESSION";

    public PostMethod(Message message) throws SQLException, NotEnoughParametersException, UserDataException,
            UserAlreadyInDatabaseException, UserNotMemberOfGroupException {
        switch (message.getAction()) {
            case MESSAGE_ACTION -> new MessageAction(message).sendMessage();
            case NEW_USER_ACTION -> new UserAction(message).createNewUser();
            case KEY -> new KeyAction(message).uploadKey();
            case TRUSTED_USER -> new TrustedUsersAction(message).addTrustedUser();
            case GROUP -> getReturnMessage().setReturnElement(new JsonPrimitive(new GroupAction(message).createGroup()));
            case SESSION -> new SessionAction(message).addSession();
            default -> {
                Log.log(Level.WARNING, "Erhaltene Aktion " + message.getAction() + " existiert nicht.");
                getReturnMessage().setErrorMessage(ReturnMessage.ACTION_DOES_NOT_EXISTS);
            }
        }
    }
}
