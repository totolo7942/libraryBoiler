package utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RetryOnFailFncTest {

    @Test
    void test_ntry_exception(){
        assertThrows(Exception.class , this::extracted);
    }

    private void extracted() {
        RetryOnFailFnc.retry(() -> {
            System.out.println(5 / 0);
            return null;
        }, 5, Exception.class);
    }
}