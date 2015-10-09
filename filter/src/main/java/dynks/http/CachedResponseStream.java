package dynks.http;

import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by jszczepankiewicz on 2015-03-20.
 */
public class CachedResponseStream extends ServletOutputStream {
    protected boolean closed = false;
    protected HttpServletResponse response = null;
    protected ServletOutputStream output = null;
    protected OutputStream cache = null;

    public CachedResponseStream(HttpServletResponse response,
                               OutputStream cache) throws IOException {
        super();
        closed = false;
        this.response = response;
        this.cache = cache;
    }

    @Override
    public boolean isReady() {
        //  FIXM: understand me
        return true;
    }

    @Override
    public void setWriteListener(WriteListener writeListener) {
        System.out.println("\tXX: setWriteListener");
    }

    public void close() throws IOException {
        if (closed) {
            throw new IOException(
                    "This output stream has already been closed");
        }
        cache.close();
        closed = true;
    }

    public void flush() throws IOException {
        if (closed) {
            throw new IOException(
                    "Cannot flush a closed output stream");
        }
        cache.flush();
    }

    public void write(int b) throws IOException {
        if (closed) {
            throw new IOException(
                    "Cannot write to a closed output stream");
        }
        cache.write((byte)b);
    }

    public void write(byte b[]) throws IOException {
        write(b, 0, b.length);
    }

    public void write(byte b[], int off, int len)
            throws IOException {
        if (closed) {
            throw new IOException(
                    "Cannot write to a closed output stream");
        }
        cache.write(b, off, len);
    }

    public boolean closed() {
        return (this.closed);
    }

    public void reset() {
        //noop
    }
}
