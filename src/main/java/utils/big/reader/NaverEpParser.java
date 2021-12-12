package utils.big.reader;

import utils.big.reader.entity.NaverModelBO;
import utils.big.reader.entity.NaverProductBO;
import utils.big.reader.entity.NaverProductListBO;
import utils.big.reader.entity.NaverRootBO;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.XMLEvent;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <modelProduct>
 *                 <matchNvMid>24743791013</matchNvMid>
 *                 <modelType>AUTO</modelType>
 *                 <isPopularModel>false</isPopularModel>
 *                 <productName><![CDATA[캠핑 일산화탄소 경보기]]></productName>
 *                 <cateCode>50002711</cateCode>
 *                 <cateName><![CDATA[경보기]]></cateName>
 *                 <fullCateCode><![CDATA[50000008>50000078>50000916>50002711]]></fullCateCode>
 *                 <fullCateName><![CDATA[생활/건강>생활용품>보안용품>경보기]]></fullCateName>
 *                 <lowestPrice>24000</lowestPrice>
 *                 <lowestPriceDevice>ALL</lowestPriceDevice>
 *                 <productCount>9</productCount>
 *                 <useAttr>false</useAttr>
 *                 <lowestProductList>
 *                         <product>
 *                                 <ranking>1</ranking>
 *                                 <price>24000</price>
 *                                 <deliveryCost>2500</deliveryCost>
 *                                 <nvMid>82720695840</nvMid>
 *                                 <mallId>d497fd29f63cde7307837ac0c4ec1efe</mallId>
 *                                 <mallPid>5176174696</mallPid>
 *                         </product>
 *                         <product>
 *                                 <ranking>2</ranking>
 *                                 <price>24000</price>
 *                                 <deliveryCost>2500</deliveryCost>
 *                                 <nvMid>82745742514</nvMid>
 *                                 <mallId>d497fd29f63cde7307837ac0c4ec1efe</mallId>
 *                                 <mallPid>5201220885</mallPid>
 *                         </product>
 *                         <product>
 *                                 <ranking>3</ranking>
 *                                 <price>26560</price>
 *                                 <deliveryCost>3000</deliveryCost>
 *                                 <nvMid>29537186190</nvMid>
 *                                 <mallId>4e31e4cda2225ba8137d1838c7376e7a</mallId>
 *                                 <mallPid>2269824354</mallPid>
 *                         </product>
 *                         <product>
 *                                 <ranking>4</ranking>
 *                                 <price>28130</price>
 *                                 <deliveryCost>3000</deliveryCost>
 *                                 <nvMid>29401037086</nvMid>
 *                                 <mallId>a42de7a7234b5ce136bf1f1e6bcf8f14</mallId>
 *                                 <mallPid>8619803203</mallPid>
 *                         </product>
 *                         <product>
 *                                 <ranking>5</ranking>
 *                                 <price>29610</price>
 *                                 <deliveryCost>3000</deliveryCost>
 *                                 <nvMid>29599815805</nvMid>
 *                                 <mallId>4e31e4cda2225ba8137d1838c7376e7a</mallId>
 *                                 <mallPid>2008220023</mallPid>
 *                         </product>
 *                         <product>
 *                                 <ranking>6</ranking>
 *                                 <price>29900</price>
 *                                 <deliveryCost>2500</deliveryCost>
 *                                 <nvMid>82881639604</nvMid>
 *                                 <mallId>679492594924f01462c4d30b00f0b2f7</mallId>
 *                                 <mallPid>5337146787</mallPid>
 *                         </product>
 *                         <product>
 *                                 <ranking>7</ranking>
 *                                 <price>32000</price>
 *                                 <deliveryCost>3000</deliveryCost>
 *                                 <nvMid>29542790039</nvMid>
 *                                 <mallId>56f986beb01796aeb8338e617bb896de</mallId>
 *                                 <mallPid>1939655983_1939655983</mallPid>
 *                         </product>
 *                         <product>
 *                                 <ranking>8</ranking>
 *                                 <price>35500</price>
 *                                 <deliveryCost>2500</deliveryCost>
 *                                 <nvMid>25855184776</nvMid>
 *                                 <mallId>66a798bfa586f42db59df01e79fd5cdd</mallId>
 *                                 <mallPid>P4807014787</mallPid>
 *                         </product>
 *                         <product>
 *                                 <ranking>9</ranking>
 *                                 <price>35500</price>
 *                                 <deliveryCost>2500</deliveryCost>
 *                                 <nvMid>25626278442</nvMid>
 *                                 <mallId>bc355cd709cb1725161e7129df01095f</mallId>
 *                                 <mallPid>3272499458</mallPid>
 *                         </product>
 *                 </lowestProductList>
 *                 <lowestProductListByMall>
 *                         <product>
 *                                 <price>24000</price>
 *                                 <nvMid>82720695840</nvMid>
 *                                 <mallId>d497fd29f63cde7307837ac0c4ec1efe</mallId>
 *                                 <mallPid>5176174696</mallPid>
 *                         </product>
 *                         <product>
 *                                 <price>26560</price>
 *                                 <nvMid>29537186190</nvMid>
 *                                 <mallId>4e31e4cda2225ba8137d1838c7376e7a</mallId>
 *                                 <mallPid>2269824354</mallPid>
 *                         </product>
 *                         <product>
 *                                 <price>28130</price>
 *                                 <nvMid>29401037086</nvMid>
 *                                 <mallId>a42de7a7234b5ce136bf1f1e6bcf8f14</mallId>
 *                                 <mallPid>8619803203</mallPid>
 *                         </product>
 *                         <product>
 *                                 <price>29900</price>
 *                                 <nvMid>82881639604</nvMid>
 *                                 <mallId>679492594924f01462c4d30b00f0b2f7</mallId>
 *                                 <mallPid>5337146787</mallPid>
 *                         </product>
 *                         <product>
 *                                 <price>32000</price>
 *                                 <nvMid>29542790039</nvMid>
 *                                 <mallId>56f986beb01796aeb8338e617bb896de</mallId>
 *                                 <mallPid>1939655983_1939655983</mallPid>
 *                         </product>
 *                 </lowestProductListByMall>
 *         </modelProduct>
 */
public class NaverEpParser {

    private String XML_HEADER = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
    private String XML_ROOT = "modelProductList";
    private String XML_ELEMENT = "modelProduct";
    private StringBuffer xmlTmpStr = new StringBuffer();
    private StringBuffer xmlElementStr = new StringBuffer();

    public List<NaverModelBO> staxParser(Path path) throws XMLStreamException, FileNotFoundException {
        XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
        XMLStreamReader reader = xmlInputFactory.createXMLStreamReader(new FileInputStream(path.toFile()));
        int eventType = reader.getEventType();

        List<NaverProductBO> product = null;
        NaverProductBO productBO = null;
        List<NaverModelBO> modelBOList = new ArrayList<>();
        Map<String, String> elementMaps = new HashMap<>();
        List<String> constrantKeys = List.of("matchNvMid", "modelType", "isPopularModel", "productName", "cateCode" ,
                "cateName", "fullCateCode", "fullCateName", "lowestPrice", "lowestPriceDevice", "productCount", "useAtte"
        );

        while (reader.hasNext()) {
            eventType = reader.next();

            if (eventType == XMLEvent.START_ELEMENT) {
                if (reader.getName().getLocalPart().equals("modelProduct")) {
                    elementMaps = new HashMap<>();
                }
            }

            //Constran값을 빼옴
            if (eventType == XMLStreamConstants.START_ELEMENT) {
                String elementName = reader.getLocalName();
                if (constrantKeys.contains(elementName)) {
                    elementMaps.put(elementName, reader.getElementText());
                }
            }

//            List<String> elementKeys = List.of("ranking", "price", "deliveryCost", "nvMid", "mallId","mallPid");
            if (eventType == XMLEvent.START_ELEMENT) {
                if(reader.getName().getLocalPart().equals("lowestProductList")) {
                    product = new ArrayList<>();
                }

                if(reader.getName().getLocalPart().equals("product")) {
                    productBO = new NaverProductBO();
                }

                switch (reader.getName().getLocalPart()) {
                    case "ranking":
                        eventType = reader.next();
                        if (eventType == XMLEvent.CHARACTERS) {
                            productBO.setRanking(Long.parseLong(reader.getText()));
                        }
                        break;
                    case "price":
                        eventType = reader.next();
                        if (eventType == XMLEvent.CHARACTERS) {
                            productBO.setPrice(Long.parseLong(reader.getText()));
                        }
                        break;
                    case "deliveryCost":
                        eventType = reader.next();
                        if (eventType == XMLEvent.CHARACTERS) {
                            productBO.setDeliveryCost(Long.parseLong(reader.getText()));
                        }
                        break;
                    case "mallId":
                        eventType = reader.next();
                        if (eventType == XMLEvent.CHARACTERS) {
                            productBO.setMallId(reader.getText());
                        }
                        break;
                    case "mallPid":
                        eventType = reader.next();
                        if (eventType == XMLEvent.CHARACTERS) {
                            productBO.setMallPid(reader.getText());
                            product.add(productBO);
                        }
                        break;
                }
            }

            if (eventType == XMLEvent.END_ELEMENT) {
                if (reader.getName().getLocalPart().equals("modelProduct")) {
                    NaverModelBO modelBO = new NaverModelBO();
                    modelBO.setMatchNvMid(elementMaps.get("matchNvMid"));
                    modelBO.setModelType(elementMaps.get("modelType"));
                    modelBO.setIsPopularModel(elementMaps.get("isPopularModel"));
                    modelBO.setProductName(elementMaps.get("productName"));
                    modelBO.setCateCode(elementMaps.get("cateName"));
                    modelBO.setCateName(elementMaps.get("cateName"));
                    modelBO.setFullCateCode(elementMaps.get("fullCateCode"));
                    modelBO.setFullCateName(elementMaps.get("fullCateName"));

                    if(elementMaps.get("lowestPrice") != null)
                        modelBO.setLowestPrice(Long.parseLong(elementMaps.get("lowestPrice")));

                    modelBO.setLowestPriceDevice(elementMaps.get("lowestPriceDevice"));

                    if(elementMaps.get("productCount") != null)
                        modelBO.setProductCount(Long.parseLong(elementMaps.get("productCount")));

                    modelBO.setUseAttr(elementMaps.get("useAttr"));
                    NaverProductListBO naverProductListBO = new NaverProductListBO();
                    naverProductListBO.setProduct(product);
                    modelBO.setLowestProductList(naverProductListBO);
                    modelBOList.add(modelBO);
                }
            }
        }
//        product.forEach(NaverProductBO::toString);
        return modelBOList;
    }

    public void lineParser(final String buffer ) throws JAXBException {
        JAXBContext jaxbContext = JAXBContext.newInstance(NaverRootBO.class);
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        try {
            String txt = buffer.trim();
            xmlTmpStr.append(txt);

            if(txt.contains("</" + XML_ELEMENT + ">")){
                if(xmlTmpStr.indexOf(XML_HEADER) <= -1){
                    xmlElementStr.append(XML_HEADER);
                }

                if(xmlTmpStr.indexOf("<"+XML_ROOT+">") <= -1){
                    xmlElementStr.append("<"+XML_ROOT+">");
                }

                xmlElementStr.append(xmlTmpStr.toString());

                if(xmlTmpStr.indexOf("</"+XML_ROOT+">") <= -1){
                    xmlElementStr.append("</"+XML_ROOT+">");
                }

                NaverRootBO root = (NaverRootBO)unmarshaller.unmarshal(new StringReader(xmlElementStr.toString()));
                xmlElementStr.delete(0,  xmlElementStr.toString().length());
                xmlTmpStr.delete(0,  xmlTmpStr.toString().length());
            }
        } catch (Exception e) {
            e.printStackTrace();
            xmlElementStr.delete(0, xmlElementStr.toString().length());
            xmlTmpStr.delete(0, xmlTmpStr.toString().length());
        }
    }

    public boolean parser(ByteBuffer buffer) throws IOException, JAXBException {
        InputStream is = new ByteBufferBackedInputStream(buffer);
        InputStreamReader r = new InputStreamReader(is, Charset.defaultCharset());
        BufferedReader input = new BufferedReader(r);

        JAXBContext jaxbContext = JAXBContext.newInstance(NaverRootBO.class);
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();

        while(input.ready()){
            try {
                String txt = input.readLine().trim();
                xmlTmpStr.append(txt);

                if(txt.contains("</" + XML_ELEMENT + ">")){
                    if(xmlTmpStr.indexOf(XML_HEADER) <= -1){
                        xmlElementStr.append(XML_HEADER);
                    }
//                    if(xmlTmpStr.indexOf("<"+XML_ROOT+">") <= -1){
//                        xmlElementStr.append("<"+XML_ROOT+">");
//                    }
                    xmlElementStr.append(xmlTmpStr.toString());
//                    if(xmlTmpStr.indexOf("</"+XML_ROOT+">") <= -1){
//                        xmlElementStr.append("</"+XML_ROOT+">");
//                    }

                    System.out.println("## to string " + xmlElementStr.toString());
                    NaverRootBO root = (NaverRootBO)unmarshaller.unmarshal(new StringReader(xmlElementStr.toString()));

                    System.out.println("## to size " + root.getModelProduct().size());

                    xmlElementStr.delete(0,  xmlElementStr.toString().length());
                    xmlTmpStr.delete(0,  xmlTmpStr.toString().length());
                }
            } catch (Exception e) {
                xmlElementStr.delete(0, xmlElementStr.toString().length());
                xmlTmpStr.delete(0, xmlTmpStr.toString().length());
            }
        }

        return true;
    }
}


class ByteBufferBackedInputStream extends InputStream {

    ByteBuffer buf;
    public ByteBufferBackedInputStream(ByteBuffer buf) {
        this.buf = buf;
    }

    public synchronized int read() throws IOException {
        if (!buf.hasRemaining()) {
            return -1;
        }
        return buf.get() & 0xFF;
    }

    @Override
    public int available() throws IOException {
        return buf.remaining();
    }

    public synchronized int read(byte[] bytes, int off, int len) throws IOException {
        if (!buf.hasRemaining()) {
            return -1;
        }

        len = Math.min(len, buf.remaining());
        buf.get(bytes, off, len);
        return len;
    }
}