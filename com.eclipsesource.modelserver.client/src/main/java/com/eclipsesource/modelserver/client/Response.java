/*******************************************************************************
 * Copyright (c) 2019 EclipseSource and others.
 *
 *   This program and the accompanying materials are made available under the
 *   terms of the Eclipse Public License v. 2.0 which is available at
 *   http://www.eclipse.org/legal/epl-2.0.
 *
 *   This Source Code may also be made available under the following Secondary
 *   Licenses when the conditions for such availability set forth in the Eclipse
 *   Public License v. 2.0 are satisfied: GNU General Public License, version 2
 *   with the GNU Classpath Exception which is available at
 *   https://www.gnu.org/software/classpath/license.html.
 *
 *   SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 *******************************************************************************/
package com.eclipsesource.modelserver.client;

import java.io.IOException;
import java.util.Objects;
import java.util.function.Function;

public class Response<T> {

    private okhttp3.Response response;
    private Function<String, T> demarshaller;
    private T body;

    public Response(okhttp3.Response response, Function<String, T> demarshaller) {
        this.response = response;
        this.demarshaller = demarshaller;
    }

    public Response(okhttp3.Response response) {
        this.response = response;
        this.demarshaller = (Function<String, T>) Function.<String>identity();
    }

    public Integer getStatusCode() {
        return this.response.code();
    }

    public T body() {
        if (body == null) {
            try {
                body = this.demarshaller.apply(Objects.requireNonNull(this.response.body()).string());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return body;
    }

    public String getMessage() {
        return this.response.message();
    }

    public <U> Response<U> mapBody(Function<T, U> mapper) {
        return new Response<>(this.response, demarshaller.andThen(mapper));
    }
}
