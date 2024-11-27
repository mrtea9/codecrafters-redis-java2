package util;


import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.io.InputStream;

@RequiredArgsConstructor
public class TrackedInputStream extends InputStream {

    private final InputStream delegate;
    private long read;

    public TrackedInputStream(InputStream inputStream) {
        this.delegate = inputStream;
    }

    @Override
    public int available() throws IOException {
        return delegate.available();
    }

    @Override
    public int read() throws IOException {
        final int value = delegate.read();

        if (value != -1) read++;

        return value;
    }

    @Override
    public void close() throws IOException {
        delegate.close();
    }

    public void begin() {
        read = 0;
    }

    public long count() {
        return read;
    }
}
