package jp.vcoin.gratuitybot;

import java.util.function.*;

/**
 * パクリ元 http://shironeko.hateblo.jp/entry/2016/07/16/140236
 */
@SuppressWarnings("unused")
public class StreamHelper {

    private StreamHelper() {
    }

    public static <T> Consumer<T> throwingConsumer(ThrowingConsumer<T> target) {
        return (param -> {
            try {
                target.accept(param);
            } catch (RuntimeException e) {
                throw e;
            } catch (Exception e) {
                throw new ExceptionWrapper(e);
            }
        });
    }

    public static <T> Supplier<T> throwingSupplier(ThrowingSupplier<T> target) {
        return (() -> {
            try {
                return target.get();
            } catch (RuntimeException e) {
                throw e;
            } catch (Exception e) {
                throw new ExceptionWrapper(e);
            }
        });
    }

    public static <T, R> Function<T, R> throwingFunction(ThrowingFunction<T, R> target) {
        return (arg -> {
            try {
                return target.apply(arg);
            } catch (RuntimeException e) {
                throw e;
            } catch (Exception e) {
                throw new ExceptionWrapper(e);
            }
        });
    }

    public static <T> Predicate<T> throwingPredicate(ThrowingPredicate<T> target) {
        return (arg -> {
            try {
                return target.test(arg);
            } catch (RuntimeException e) {
                throw e;
            } catch (Exception e) {
                throw new ExceptionWrapper(e);
            }
        });
    }

    public static <T> Consumer<T> index(int start, ObjIntConsumer<T> consumer) {
        int[] counter = {start};
        return obj -> consumer.accept(obj, counter[0]++);
    }

    @FunctionalInterface
    public interface ThrowingConsumer<T> {
        void accept(T t) throws Exception;
    }

    @FunctionalInterface
    public interface ThrowingSupplier<T> {
        T get() throws Exception;
    }

    @FunctionalInterface
    public interface ThrowingFunction<T, R> {
        R apply(T arg) throws Exception;
    }

    @FunctionalInterface
    public interface ThrowingPredicate<T> {
        boolean test(T arg) throws Exception;
    }

    @SuppressWarnings("WeakerAccess")
    public static class ExceptionWrapper extends RuntimeException {
        private ExceptionWrapper(Throwable cause) {
            super(cause);
        }
    }
}