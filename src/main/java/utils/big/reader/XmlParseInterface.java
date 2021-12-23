package utils.big.reader;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.nio.file.Path;

abstract class  XmlParseInterface {

    abstract void parsing(Path path, StringBuilder stringBuilder, boolean fileWrite) throws XMLStreamException, IOException;
    abstract void divider(Path path, StringBuilder epDataBuilder, boolean fileWrite) throws XMLStreamException, IOException;
}
