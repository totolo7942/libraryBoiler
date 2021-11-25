package utils;

import java.util.Arrays;
import java.util.function.Supplier;
import java.util.logging.Logger;

public class RetryOnFailFnc {

    private final static Logger log = Logger.getGlobal();

    @SafeVarargs
    public static <T> T retry(Supplier<T> function, int maxRetries, Class<? extends Exception>... exceptionClazz) {
        int retryCounter = 0;
        Exception lastException = null;

        while (retryCounter < maxRetries) {
            try {
                return function.get();
            } catch (Exception e) {
                lastException = e;
                if (Arrays.stream(exceptionClazz).noneMatch(tClass -> tClass.isAssignableFrom(e.getClass())))
                    throw (RuntimeException) lastException;
                else {
                    retryCounter++;
                    log.warning("FAILED - Command failed on retry " + retryCounter + " of " + maxRetries);
                    e.printStackTrace();
                    if (retryCounter >= maxRetries) {
                        break;
                    }
                }
            }
        }
        throw lastException instanceof RuntimeException ? ((RuntimeException) lastException) : new RuntimeException(lastException);
    }

    public static void main(String... args) throws Exception {
        retry(() -> {
            System.out.println(5 / 0);
            return null;
        }, 5, Exception.class);
    }
}
