package utils.big;

import utils.big.reader.XmlFileContextHandler;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;
import java.io.IOException;

/**
 * @author a1101381
 */
public class LargeXmlNaverEpReaderMain {

    public static void main(String[] args) {
        final String FILE_NAME_40G="/Users/a1101381/naver_data/naver_ep.xml";
        final String FILE_NAME_2G="/Users/a1101381/naver_data/naver_ep_2g.xml";
        final String FILE_NAME_ONE_DATA ="/Users/a1101381/Develop/totolo7942/git/libraryBoiler/src/main/resources/NaverEp.xml";
        try {
            new XmlFileContextHandler().read( FILE_NAME_2G);

            new XmlFileContextHandler().legacyRead(FILE_NAME_2G);
        } catch (IOException | InterruptedException | JAXBException | XMLStreamException e) {
            e.printStackTrace();
        }
    }
}
