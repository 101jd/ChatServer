package org.example;

import java.io.Closeable;
import java.io.IOException;
import java.util.Arrays;

public class StreamManager {
    public static void closeAll(Closeable[] closeables){
        Arrays.stream(closeables).filter(closeable -> closeable != null).forEach(closeable -> {
            try {
                closeable.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
