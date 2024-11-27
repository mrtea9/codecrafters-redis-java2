package serial;

import lombok.RequiredArgsConstructor;
import type.RString;
import type.RValue;
import util.TrackedInputStream;

import java.io.IOException;
import java.io.InputStream;

@RequiredArgsConstructor
public class Deserializer {

    private final InputStream inputStream;

    public Deserializer(TrackedInputStream inputStream) {
        this.inputStream = inputStream;
    }

    public RValue read() throws IOException {
        return read(false);
    }

    public RValue read(boolean likelyBlob) throws IOException {
        final int first = inputStream.read();
        if (first == -1) return null;

        return parseString();
    }

    private RString parseString() throws IOException {
        final StringBuilder builder = new StringBuilder();

        int value;
        while ((value = inputStream.read()) != -1) {
            if (value == '\r') {
                inputStream.read(); // \n
                break;
            }

            builder.append((char) value);
        }

        return RString.simple(builder.toString());
    }
}
