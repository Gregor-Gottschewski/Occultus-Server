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

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class ReturnMessage {
    private final JsonObject returnObject;
    public static final String ACTION_DOES_NOT_EXISTS = "Übergebene Methode existiert nicht.";

    public ReturnMessage() {
        returnObject = new JsonObject();
        setSuccess(true);
    }

    public JsonObject getReturnMessage() {
        return returnObject;
    }

    public boolean isSuccess() {
        return returnObject.get("success").getAsBoolean();
    }

    public void setSuccess(boolean success) {
        returnObject.addProperty("success", success);
    }

    public void setErrorMessage(String errorMessage) {
        setSuccess(false);
        returnObject.addProperty("error", errorMessage);
    }

    public void setReturnText(String returnText) {
        returnObject.addProperty("return", returnText);
    }

    public void setReturnElement(JsonElement jsonElement) {
        returnObject.add("return", jsonElement);
    }
}
