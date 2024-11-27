package type;

import lombok.NonNull;

import java.util.List;

public record RArray<T extends RValue>(@NonNull List<T> items) implements RValue {

}
