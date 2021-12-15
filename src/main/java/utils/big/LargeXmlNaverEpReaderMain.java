package utils.big;

import utils.big.reader.XmlFileContextHandler;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

/**
 * @author a1101381
 */
public class LargeXmlNaverEpReaderMain {

    public static void main(String[] args) {
        final String FILE_NAME_40G="/Users/a1101381/naver_data/naver_ep.xml";
        final String FILE_NAME_2G="/Users/a1101381/naver_data/naver_ep_2g.xml";
        final String FILE_NAME_ONE_DATA ="/Users/a1101381/Develop/totolo7942/git/libraryBoiler/src/main/resources/NaverEp.xml";

        try {
            warmUpCleanDirectoryFiles();

            new XmlFileContextHandler().read( FILE_NAME_2G);
            new XmlFileContextHandler().legacyRead(FILE_NAME_2G);
        } catch (IOException | InterruptedException | JAXBException | XMLStreamException e) {
            e.printStackTrace();
        }
    }

    private static final String TARGET_DIRE="/Users/a1101381/naver_data/parse";
    private static void warmUpCleanDirectoryFiles() throws IOException {
        Path path = Path.of(TARGET_DIRE);
        Stream<Path> fstream = Files.list(path);
        fstream.forEach( r -> {
            try {
                System.out.println("warmup : delete file " + r);
                Files.delete(r);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        Files.createDirectories(path);
    }
}
