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
import com.gregorgott.OccultusServer.MessageHandling.Actions.GroupAction;
import com.gregorgott.OccultusServer.MessageHandling.Actions.TrustedUsersAction;
import com.gregorgott.OccultusServer.MessageHandling.Exceptions.NotEnoughParametersException;
import com.gregorgott.OccultusServer.MessageHandling.Exceptions.UserDataException;
import com.gregorgott.OccultusServer.MessageHandling.Message;

import javax.naming.NoPermissionException;
import java.sql.SQLException;
import java.util.logging.Level;

public class DeleteMethod extends Method {
    private static final String TRUSTED_USER = "TRUSTED_USER";
    private static final String GROUP = "GROUP";

    public DeleteMethod(Message message) throws SQLException, NotEnoughParametersException, UserDataException, NoPermissionException {
        switch (message.getAction()) {
            case TRUSTED_USER -> new TrustedUsersAction(message).removeTrustedUser();
            case GROUP -> new GroupAction(message).deleteGroup();
            default -> {
                getReturnMessage().setErrorMessage("Erhaltene Aktion " + message.getAction() + " existiert nicht.");

                Log.log(Level.WARNING, "Erhaltene Aktion " + message.getAction() + " existiert nicht.");
            }
        }
    }
}
