package org.apache.commons.geometry.examples.io.internal;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FilterInputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;

public final class IOUtils {

    /** Utility class; no instantiation. */
    private IOUtils() {}

    public static InputStream createCloseShieldInputStream(final InputStream in) {
        return new CloseShieldInputStream(in);
    }

    public static OutputStream createCloseShieldOutputStream(final OutputStream out) {
        return new CloseShieldOutputStream(out);
    }

    public static Reader createCloseShieldReader(final InputStream in, final Charset charset) {
        final InputStream shielded = createCloseShieldInputStream(in);
        return new BufferedReader(new InputStreamReader(shielded, charset));
    }

    public static Writer createCloseShieldWriter(final OutputStream out, final Charset charset) {
        final OutputStream shielded = createCloseShieldOutputStream(out);
        return new BufferedWriter(new OutputStreamWriter(shielded, charset));
    }

    private static final class CloseShieldInputStream extends FilterInputStream {

        CloseShieldInputStream(final InputStream in) {
            super(in);
        }

        /**
         * Do nothing. The underlying stream is <em>not</em> closed.
         */
        @Override
        public void close() throws IOException {
            // do nothing
        }
    }

    private static final class CloseShieldOutputStream extends FilterOutputStream {

        CloseShieldOutputStream(final OutputStream out) {
            super(out);
        }

        /**
         * Do nothing. The underlying stream is <em>not</em> closed.
         */
        @Override
        public void close() throws IOException {
            // do nothing
        }
    }
}
