package utils.big.reader;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

abstract class  XmlParseInterface {

    abstract void parsing(Path path) throws XMLStreamException, IOException;
}
