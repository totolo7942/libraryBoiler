package utils;

import java.util.Arrays;
import java.util.function.Supplier;
import java.util.logging.Logger;

/**
 * @author a1101381
 */
public class RetryOnFailFnc {

    private final static Logger log = Logger.getGlobal();

    @SafeVarargs
    public static <T> void retry(Supplier<T> function, int maxRetries, Class<? extends Exception>... exceptionClazz) {
        int retryCounter = 0;
        Exception lastException = null;

        while (retryCounter < maxRetries) {
            try {
                function.get();
                return;
            } catch (Exception e) {
                lastException = e;
                if (Arrays.stream(exceptionClazz).noneMatch(tClass -> tClass.isAssignableFrom(e.getClass()))) {
                    throw (RuntimeException) lastException;
                } else {
                    retryCounter++;
                    log.warning("FAILED - Command failed on retry " + retryCounter + " of " + maxRetries);
                    e.printStackTrace();
                    if (retryCounter >= maxRetries) {
                        break;
                    }
                }
            }
        }
        throw lastException != null ? ((RuntimeException) lastException) : new RuntimeException(lastException);
    }

}
