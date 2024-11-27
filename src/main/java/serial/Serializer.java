package serial;


import lombok.RequiredArgsConstructor;
import type.RString;
import type.RValue;
import util.TrackedOutputStream;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

@RequiredArgsConstructor
public class Serializer {

    private static final byte[] CRLF_BYTES = Protocol.CRLF.getBytes();
    private static final byte[] OK_BYTES = "OK".getBytes(StandardCharsets.US_ASCII);

    private final OutputStream outputStream;

    public Serializer(TrackedOutputStream outputStream) {
        this.outputStream = outputStream;
    }

    public void flush() throws IOException {
        outputStream.flush();
    }

    public void write(RValue value) throws IOException {
        switch (value) {
            case RString string -> {
                writeSimpleString(string.content());
            }
            default -> {
                break;
            }
        }
    }

    private void writeSimpleString(String string) throws IOException {
        outputStream.write(Protocol.SIMPLE_STRING);
        outputStream.write(string.getBytes());
        outputStream.write(CRLF_BYTES);
    }

}
