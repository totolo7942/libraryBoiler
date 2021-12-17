package utils.big;

import org.eclipse.persistence.exceptions.JAXBException;
import utils.big.reader.XmlFileContextHandler;

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

            new XmlFileContextHandler().read( FILE_NAME_40G);
//            new XmlFileContextHandler().legacyRead(FILE_NAME_ONE_DATA);
        } catch (IOException | InterruptedException | XMLStreamException | JAXBException e) {
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

/**
 * Graalvm Build
 * native-image -cp /Users/a1101381/Develop/totolo7942/git/libraryBoiler/out/production/classes:/Users/a1101381/Develop/totolo7942/git/libraryBoiler/out/production/resources:/Users/a1101381/.gradle/caches/modules-2/files-2.1/org.apache.commons/commons-lang3/3.0/8873bd0bb5cb9ee37f1b04578eb7e26fcdd44cb0/commons-lang3-3.0.jar:/Users/a1101381/.gradle/caches/modules-2/files-2.1/commons-io/commons-io/2.6/815893df5f31da2ece4040fe0a12fd44b577afaf/commons-io-2.6.jar:/Users/a1101381/.gradle/caches/modules-2/files-2.1/com.sun.xml.bind/jaxb-impl/3.0.1/b2de35b8535302825d945462d2684823dedbf1bb/jaxb-impl-3.0.1.jar:/Users/a1101381/.gradle/caches/modules-2/files-2.1/com.sun.xml.bind/jaxb-core/3.0.1/9fd8003b748dd66776aa72890fe30313a5a34d1d/jaxb-core-3.0.1.jar:/Users/a1101381/.gradle/caches/modules-2/files-2.1/org.eclipse.persistence/org.eclipse.persistence.moxy/3.0.0/e4d94743631925fa20e1506d919014d7a07f4b75/org.eclipse.persistence.moxy-3.0.0.jar:/Users/a1101381/.gradle/caches/modules-2/files-2.1/jakarta.xml.bind/jakarta.xml.bind-api/3.0.1/5257932df36ff3e4e6de50429dde946490a6a800/jakarta.xml.bind-api-3.0.1.jar:/Users/a1101381/.gradle/caches/modules-2/files-2.1/org.apache.commons/commons-collections4/4.0/da217367fd25e88df52ba79e47658d4cf928b0d1/commons-collections4-4.0.jar:/Users/a1101381/.gradle/caches/modules-2/files-2.1/com.jamesmurty.utils/java-xmlbuilder/1.3/224a643bacd1bf78b7055000bee043b17c879787/java-xmlbuilder-1.3.jar:/Users/a1101381/.gradle/caches/modules-2/files-2.1/com.google.code.gson/gson/2.8.9/8a432c1d6825781e21a02db2e2c33c5fde2833b9/gson-2.8.9.jar:/Users/a1101381/.gradle/caches/modules-2/files-2.1/com.sun.activation/jakarta.activation/2.0.1/828b80e886a52bb09fe41ff410b10b342f533ce1/jakarta.activation-2.0.1.jar:/Users/a1101381/.gradle/caches/modules-2/files-2.1/org.eclipse.persistence/org.eclipse.persistence.asm/3.0.0/2e626dc82f094beaa6b1cc218feeaf266f1e2014/org.eclipse.persistence.asm-3.0.0.jar:/Users/a1101381/.gradle/caches/modules-2/files-2.1/org.eclipse.persistence/org.eclipse.persistence.core/3.0.0/8c25757fc18e86a70dd9e2e26ab1a14151464607/org.eclipse.persistence.core-3.0.0.jar:/Users/a1101381/.gradle/caches/modules-2/files-2.1/net.iharder/base64/2.3.8/7d2e2cea90cc51169fd02a35888820ab07f6d02f/base64-2.3.8.jar:/Users/a1101381/.gradle/caches/modules-2/files-2.1/com.sun.mail/jakarta.mail/2.0.0/409efff67e50a231b965c2247f7c92ab70d43044/jakarta.mail-2.0.0.jar -jar build/libs/libraryBoiler-1.0-SNAPSHOT.jar
 */