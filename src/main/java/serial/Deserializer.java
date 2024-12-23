package serial;

import lombok.RequiredArgsConstructor;
import type.*;
import util.TrackedInputStream;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

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

        return switch (first) {
            case Protocol.ARRAY -> parseArray();
            case Protocol.SIMPLE_STRING -> parseString();
            case Protocol.SIMPLE_ERROR -> new RError(parseString());
            case Protocol.BULK_STRING -> likelyBlob ? parseBulkBlob() : parseBulkString();

            default -> {
                throw new IllegalArgumentException("Unexpected value: %s (%s)".formatted(first, (char) first));
            }
        };
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

    private RString parseBulkString() throws IOException {
        final var length = parseLength();
        final var bytes = inputStream.readNBytes(length);

        inputStream.read();
        inputStream.read();

        return RString.bulk(new String(bytes));
    }

    private RBlob parseBulkBlob() throws IOException {
        final var length = parseLength();
        final var bytes = inputStream.readNBytes(length);

        return RBlob.bulk(bytes);
    }

    private RValue parseArray() throws IOException {
        final var length = parseLength();

        if (length == -1) return RNil.SIMPLE;

        if (length == 0) return RArray.empty();

        final var array = new ArrayList<RValue>();
        for (int index = 0; index < length; index++) {
            array.add(read());
        }

        return RArray.view(array);
    }

    private int parseLength() throws IOException {
        final var line = parseUntilEndOfLine();

        if ("-1".equals(line)) return -1;

        return Integer.parseUnsignedInt(line);
    }

    private String parseUntilEndOfLine() throws IOException {
        final var builder = new StringBuilder();

        var cariageReturn = false;

        int value;
        while ((value = inputStream.read()) != -1) {
            if ('\n' == value && cariageReturn) {
                break;
            } else if ('\r' == value) {
                cariageReturn = true;
            } else {
                if (cariageReturn) builder.append('\r');

                builder.append((char) value);
                cariageReturn = false;
            }
        }

        return builder.toString();
    }
}
