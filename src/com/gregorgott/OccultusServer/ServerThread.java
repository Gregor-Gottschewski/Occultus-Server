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

package com.gregorgott.OccultusServer;

import com.gregorgott.OccultusServer.MessageHandling.Exceptions.UserNotMemberOfGroupException;
import com.gregorgott.OccultusServer.MessageHandling.Exceptions.NotEnoughParametersException;
import com.gregorgott.OccultusServer.MessageHandling.Exceptions.UserAlreadyInDatabaseException;
import com.gregorgott.OccultusServer.MessageHandling.Exceptions.UserDataException;
import com.gregorgott.OccultusServer.MessageHandling.Message;
import com.gregorgott.OccultusServer.MessageHandling.MessageHandler;
import com.gregorgott.OccultusServer.MessageHandling.ReturnMessage;

import javax.naming.NoPermissionException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.sql.SQLException;
import java.util.logging.Level;

public class ServerThread extends Thread {
    private final Socket socket;

    public ServerThread(Socket socket) {
        super("ServerThread");
        this.socket = socket;

    }

    public void run() {
        try (
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))
        ) {
            String input;
            while ((input = in.readLine()) != null) {
                Log.log(Level.INFO, "Received: " + input);
                Message message = new Message(input);

                if (message.isValidMessage()) {
                    try {
                        MessageHandler messageHandler = new MessageHandler(message);
                        String s = messageHandler.getResponse().getReturnMessage().toString();
                        Log.log(Level.INFO, "Send: " + s);
                        out.println(s);
                    } catch (SQLException e) {
                        Log.log(Level.WARNING, "SQL Syntax Error.");

                        ReturnMessage returnMessage = new ReturnMessage();
                        returnMessage.setErrorMessage("Server Fehler. Bitte Administrator kontaktieren.");
                        out.println(returnMessage.getReturnMessage().toString());
                    } catch (NotEnoughParametersException e) {
                        ReturnMessage returnMessage = new ReturnMessage();
                        returnMessage.setErrorMessage(e.getMessage() + e.getNeededParametersInfo());
                        out.println(returnMessage.getReturnMessage().toString());
                    } catch (UserDataException | UserAlreadyInDatabaseException | NoPermissionException e) {
                        ReturnMessage returnMessage = new ReturnMessage();
                        returnMessage.setErrorMessage(e.getMessage());
                        out.println(returnMessage.getReturnMessage().toString());
                    } catch (UserNotMemberOfGroupException e) {
                        // TODO: write catch-Block
                    }
                } else {
                    Log.log(Level.WARNING, "Received bad type: " + input);
                    out.println("Error: Message not valid");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
