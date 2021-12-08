package utils.big;

import utils.big.reader.LargeFileContextHandler;

import java.io.IOException;

/**
 * @author a1101381
 */
public class AsyncBigFileReaderMain {

    public static void main(String[] args) {
        final String FILE_NAME="/Users/a1101381/Developer/git/libraryBoiler/src/main/java/utils/big/spilter/BigXmlSpilter.java";

        try {
            new LargeFileContextHandler().read( FILE_NAME);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
