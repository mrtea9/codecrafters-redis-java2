package type;

import lombok.NonNull;
import lombok.experimental.Delegate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public record RArray<T extends RValue>(@NonNull List<T> items) implements RValue {

    @SuppressWarnings("rawtypes")
    private static final RArray EMPTY = new RArray<>(Collections.emptyList());

    @Delegate
    public List<T> items() {
        return items;
    }

    @Override
    public final String toString() {
        return "RArray[%s]".formatted(items);
    }

    public static <T extends RValue> RArray<T> of(@NonNull T e1) {
        return new RArray<>(List.of(e1));
    }

    public static <T extends RValue> RArray<T> of(@NonNull T e1, @NonNull T e2) {
        return new RArray<>(List.of(e1, e2));
    }

    public static <T extends RValue> RArray<T> of(@NonNull T e1, @NonNull T e2, @NonNull T e3) {
        return new RArray<>(List.of(e1, e2, e3));
    }

    public static <T extends RValue> RArray<T> view(@NonNull List<T> list) {
        return new RArray<>(list);
    }

    public static <T extends RValue> RArray<T> copy(@NonNull Collection<T> list) {
        return new RArray<>(new ArrayList<>(list));
    }

    @SuppressWarnings("unchecked")
    public static <T extends RValue> RArray<T> empty() {
        return EMPTY;
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }
}
