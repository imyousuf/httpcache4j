/*
 * Copyright (c) 2008, The Codehaus. All Rights Reserved.
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
 *
 */

package org.codehaus.httpcache4j.payload;

import org.codehaus.httpcache4j.MIMEType;

import org.apache.commons.lang.Validate;
import org.apache.commons.io.input.ClosedInputStream;

import java.io.IOException;
import java.io.InputStream;

/**
 * Represents a payload that is backed by a one-shot inputstream.
 * The stream may be an arbitrary stream type. If you start
 * reading from the stream, the available flag will be set to false, and the
 * next invocation of  
 */
public class InputStreamPayload implements Payload {
    private final InputStream stream;
    private final MIMEType mimeType;
    private boolean available = true;

    /**
     * Constructs an Inputstream payload.
     *
     * @param stream the stream to create the payload from; may not be {@code null}
     * @param mimeType the mime type of the stream. Defaults to
     * application/octet-stream if not set.
     */
    public InputStreamPayload(final InputStream stream, final MIMEType mimeType) {
        Validate.notNull(stream, "Inputstream may not be null");
        if (mimeType != null) {
            this.mimeType = mimeType;
        }
        else {
            this.mimeType = MIMEType.APPLICATION_OCTET_STREAM;
        }
        this.stream = new WrappedInputStream(stream);
    }

    public MIMEType getMimeType() {
        return mimeType;
    }

    public boolean isAvailable() {
        return available;
    }

    public InputStream getInputStream() {
        if (available) {
            return stream;
        }
        return ClosedInputStream.CLOSED_INPUT_STREAM;
    }

    private class WrappedInputStream extends DelegatingInputStream {

        public WrappedInputStream(InputStream delegate) {
            super(delegate);
        }

        public int read() throws IOException {
            if (isAvailable()) {
                available = false;
            }
            return super.read();
        }

        public int read(byte[] b) throws IOException {
            if (isAvailable()) {
                available = false;
            }

            return super.read(b);
        }

        public int read(byte[] b, int off, int len) throws IOException {
            if (isAvailable()) {
                available = false;
            }
            return super.read(b, off, len);
        }

        public long skip(long n) throws IOException {
            if (isAvailable()) {
                available = false;
            }
            return super.skip(n);
        }
    }
}
