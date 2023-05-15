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

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class Message {
    private final JsonElement method;
    private final JsonObject message;

    public Message(String input) {
        message = new Gson().fromJson(input, JsonObject.class);
        method = message.get("method");
    }

    public String getMethod() {
        if (message != null)
            return method.getAsString().toUpperCase();
        else
            return "";
    }

    public String getAction() {
        JsonElement jsonElement = message.get("action");
        if (jsonElement != null)
            return jsonElement.getAsString().toUpperCase();
        else
            return "";
    }

    public JsonObject getParameters() {
        JsonElement jsonElement = message.get("parameters");
        if (jsonElement != null)
            return jsonElement.getAsJsonObject();
        else
            return new JsonObject();
    }

    public String getUser() {
        return getParameters().get("user").getAsString();
    }

    public String getPassword() {
        return getParameters().get("password").getAsString();
    }

    public boolean isValidMessage() {
        return method != null;
    }
}
