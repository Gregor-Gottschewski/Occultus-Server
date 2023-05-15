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

import com.gregorgott.OccultusServer.Log;
import com.gregorgott.OccultusServer.MessageHandling.Actions.*;
import com.gregorgott.OccultusServer.MessageHandling.Exceptions.UserNotMemberOfGroupException;
import com.gregorgott.OccultusServer.MessageHandling.Exceptions.NotEnoughParametersException;
import com.gregorgott.OccultusServer.MessageHandling.Exceptions.UserDataException;
import com.gregorgott.OccultusServer.MessageHandling.Message;

import javax.naming.NoPermissionException;
import java.sql.SQLException;
import java.util.logging.Level;

public class GetMethod extends Method {
    private static final String KEY = "KEY";
    private static final String INBOX = "INBOX";
    private static final String LOGIN = "LOGIN";
    private static final String TRUSTED_USERS = "TRUSTED_USERS";
    private static final String VERIFIED_USERS = "VERIFIED_USERS";
    private static final String GROUP = "GROUP";
    private static final String GROUP_MEMBERS = "GROUP_MEMBERS";

    public GetMethod(Message message) throws SQLException, UserDataException, NotEnoughParametersException, UserNotMemberOfGroupException, NoPermissionException {
        switch (message.getAction()) {
            case KEY -> getReturnMessage().setReturnText(new KeyAction(message).getPublicKey());
            case INBOX -> getReturnMessage().setReturnElement(new InboxAction(message).getInbox());
            case TRUSTED_USERS -> getReturnMessage().setReturnElement(new TrustedUsersAction(message).getTrustedUsers());
            case VERIFIED_USERS -> getReturnMessage().setReturnElement(new TrustedUsersAction(message).getVerifiedUsers());
            case LOGIN -> new Action(message).checkUserValidity();
            case GROUP -> getReturnMessage().setReturnText(new GroupAction(message).getGroupName());
            case GROUP_MEMBERS -> getReturnMessage().setReturnElement(new GroupAction(message).getMembers());
            default -> {
                getReturnMessage().setErrorMessage("Erhaltene Aktion " + message.getAction() + " existiert nicht.");
                Log.log(Level.WARNING, "Erhaltene Aktion " + message.getAction() + " existiert nicht.");
            }
        }
    }
}
