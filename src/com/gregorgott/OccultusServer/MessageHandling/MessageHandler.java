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

package com.gregorgott.OccultusServer.MessageHandling;

import com.gregorgott.OccultusServer.MessageHandling.Exceptions.UserNotMemberOfGroupException;
import com.gregorgott.OccultusServer.MessageHandling.Exceptions.NotEnoughParametersException;
import com.gregorgott.OccultusServer.MessageHandling.Exceptions.UserAlreadyInDatabaseException;
import com.gregorgott.OccultusServer.MessageHandling.Exceptions.UserDataException;
import com.gregorgott.OccultusServer.MessageHandling.Methods.DeleteMethod;
import com.gregorgott.OccultusServer.MessageHandling.Methods.GetMethod;
import com.gregorgott.OccultusServer.MessageHandling.Methods.Method;
import com.gregorgott.OccultusServer.MessageHandling.Methods.PostMethod;

import javax.naming.NoPermissionException;
import java.sql.SQLException;

public class MessageHandler {
    private static final String GET = "GET";
    private static final String POST = "POST";
    private static final String DELETE = "DELETE";
    private final ReturnMessage returnMessage;

    public MessageHandler(Message message) throws SQLException, NotEnoughParametersException, UserDataException,
            UserAlreadyInDatabaseException, UserNotMemberOfGroupException, NoPermissionException {
        Method method = new Method();

        if (message.getMethod().equals(GET)) {
            method = new GetMethod(message);
        } else if (message.getMethod().equals(POST)) {
            method = new PostMethod(message);
        } else if (message.getMethod().equals(DELETE)) {
            method = new DeleteMethod(message);
        }

        returnMessage = method.getReturnMessage();
    }

    public ReturnMessage getResponse() {
        return returnMessage;
    }
}
