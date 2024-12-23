package type;

import lombok.NonNull;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

public record RBlob(byte[] content, boolean bulk) implements RValue {

    public InputStream inputStream() {
        return new ByteArrayInputStream(content);
    }

    public static RBlob simple(@NonNull byte[] content) {
        return new RBlob(content, false);
    }

    public static RBlob bulk(@NonNull byte[] content) {
        return new RBlob(content, true);
    }

}
