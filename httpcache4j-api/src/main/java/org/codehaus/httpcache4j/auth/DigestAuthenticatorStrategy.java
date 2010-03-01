/*
 * Copyright (c) 2009. The Codehaus. All Rights Reserved.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package org.codehaus.httpcache4j.auth;

import com.google.common.collect.Lists;
import org.codehaus.httpcache4j.*;
import org.codehaus.httpcache4j.auth.digest.Digest;
import org.codehaus.httpcache4j.auth.digest.RequestDigest;

import java.nio.charset.Charset;
import java.util.*;

/**
 * @author <a href="mailto:hamnis@codehaus.org">Erlend Hamnaberg</a>
 * @version $Revision: $
 */
public class DigestAuthenticatorStrategy implements AuthenticatorStrategy {
    private static final String AUTHENTICATION_INFO = "Authentication-Info";
    private static final String PROXY_AUTHENTICATION_INFO = "Proxy-Authentication-Info";

    public boolean supports(final AuthScheme scheme) {
        return "digest".equalsIgnoreCase(scheme.getType());
    }

    public HTTPRequest prepare(HTTPRequest request, AuthScheme scheme) {
        return prepare(request, request.getChallenge(), scheme, false);
    }

    public HTTPRequest prepareWithProxy(HTTPRequest request, Challenge challenge, AuthScheme scheme) {
        return prepare(request, challenge, scheme, true);
    }

    public AuthScheme afterSuccessfulAuthentication(AuthScheme scheme, Headers headers) {
        return afterSuccessfulAuthentication(scheme, headers, false);
    }

    public AuthScheme afterSuccessfulProxyAuthentication(AuthScheme scheme, Headers headers) {
        return afterSuccessfulAuthentication(scheme, headers, true);
    }

    private HTTPRequest prepare(final HTTPRequest request, Challenge challenge, AuthScheme scheme, boolean proxy) {
        Digest digest = new Digest(new HTTPHost(request.getRequestURI()), scheme);
        HTTPRequest req = request;
        if (challenge instanceof UsernamePasswordChallenge) {
            UsernamePasswordChallenge upc = (UsernamePasswordChallenge) challenge;
            RequestDigest requestDigest = RequestDigest.newInstance(upc, req, digest);
            Header authHeader;
            if (proxy) {
                authHeader = new Header("Proxy-Authorization", requestDigest.toHeaderValue());
            }
            else {
                authHeader = new Header("Authorization", requestDigest.toHeaderValue());
            }
            req = req.addHeader(authHeader);
        }
        return req;
    }

    public AuthScheme afterSuccessfulAuthentication(AuthScheme scheme, Headers headers, boolean proxy) {
        Header header;
        if (proxy) {
            header = headers.getFirstHeader(PROXY_AUTHENTICATION_INFO);
        }
        else {
            header = headers.getFirstHeader(AUTHENTICATION_INFO);
        }
        if (header != null) {
            String nextNonce = header.getDirectives().get("nextnonce");
            if (nextNonce != null) {
                List<Directive> directives = Lists.newArrayList(scheme.getDirectives());
                for (Directive directive : scheme.getDirectives()) {
                    if ("nonce".equals(directive.getName())) {
                        directives.remove(directive);
                    }
                }
                directives.add(new Directive("nonce", nextNonce));
                Directives dirs = new Directives(directives);
                return new AuthScheme(new Header(scheme.getHeader().getName(), dirs));
            }
        }
        return scheme;
    }
}