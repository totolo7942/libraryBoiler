package utils.big.reader;

import org.apache.commons.collections4.CollectionUtils;
import utils.big.reader.defUtils.ByteTypes;
import utils.big.reader.entity.*;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.*;
import javax.xml.stream.events.XMLEvent;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.FileAttribute;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

public class NaverEpParserHandler extends XmlParseInterface {

    private final StringBuffer xmlTmpStr = new StringBuffer();
    private Map<String, String> elementMaps = new ConcurrentHashMap<>();
    private int extFileName=0;

    private final List<String> NAVER_HEADER_ELEMENTS = List.of("matchNvMid", "modelType", "isPopularModel", "productName", "cateCode" ,
            "cateName", "fullCateCode", "fullCateName", "lowestPrice", "lowestPriceDevice", "productCount", "useAttr"
    );


    @Override
    public void parsing(Path path) throws XMLStreamException, IOException {
        XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
        XMLOutputFactory output = XMLOutputFactory.newInstance();
        XMLStreamReader reader = xmlInputFactory.createXMLStreamReader(new FileInputStream(path.toFile()));
//        XMLStreamReader reader = xmlInputFactory.createXMLStreamReader(new URL("http://localhost:8083/naver_ep_2g.xml").openConnection().getInputStream());
//        int eventType = reader.getEventType();

        List<NaverProductBO> lowProduct = null;
        List<NaverProductBO> lowProductByMall = null;
        NaverProductBO productBO = null;
        List<NaverModelBO> modelBOList = new ArrayList<>();

        StringBuffer stringBuilder = new StringBuffer();
        stringBuilder.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");

        int ParseBlockSize =0;
        boolean lowPriceProductMall = false;

        while (reader.hasNext()) {
            int eventType = reader.next();

            ParseBlockSize = parsingXMLHeaderElement(reader, NAVER_HEADER_ELEMENTS, stringBuilder, ParseBlockSize, eventType);

            if (eventType == XMLEvent.START_ELEMENT) {
                if (reader.getName().getLocalPart().equals("lowestProductList")) {
                    lowProduct = new ArrayList<>();
                    lowPriceProductMall = false;
                }

                if (reader.getName().getLocalPart().equals("lowestProductListByMall")) {
                    lowProductByMall = new ArrayList<>();
                    lowPriceProductMall = true;
                }

                if (reader.getName().getLocalPart().equals("product")) {
                    productBO = new NaverProductBO();
                }
            }

            parsingXMLBodyElements(reader, lowProduct, lowPriceProductMall, lowProductByMall, productBO, eventType);

            ParseBlockSize = parsingXMLDoneElemntToWriteFile(reader, lowProduct, lowProductByMall, stringBuilder, ParseBlockSize, eventType);
        }

        if(stringBuilder.length() > 0 )
            nioBufferWriteToFile(stringBuilder);

    }

    private int parsingXMLDoneElemntToWriteFile(XMLStreamReader reader, List<NaverProductBO> lowProduct, List<NaverProductBO> lowProductByMall, StringBuffer stringBuilder, int ParseBlockSize, int eventType) throws IOException {
        if (eventType == XMLEvent.END_ELEMENT) {
            if (reader.getName().getLocalPart().equals("modelProduct")) {
                doneMainXMLBlockedAppend(lowProduct, lowProductByMall, stringBuilder);
                final int ELEMENT_OVERFLOW_LIMIT_COUNT = 80000;
                if(ParseBlockSize > ELEMENT_OVERFLOW_LIMIT_COUNT) {
                    nioBufferWriteToFile(stringBuilder);
                    ParseBlockSize =0;
                    stringBuilder.setLength(0);
                }
            }
        }
        return ParseBlockSize;
    }

    private void parsingXMLBodyElements(XMLStreamReader reader, List<NaverProductBO> lowProduct, boolean lowPriceProductMall, List<NaverProductBO> lowProductByMall, NaverProductBO productBO, int eventType) throws XMLStreamException {

        if (eventType == XMLEvent.START_ELEMENT) {
            switch (reader.getName().getLocalPart()) {
                case "ranking":
                    eventType = reader.next();
                    if (eventType == XMLEvent.CHARACTERS) {
                        productBO.setRanking(reader.getText() != null ? Long.parseLong(reader.getText()) : 0 );
                    }
                    break;
                case "price":
                    eventType = reader.next();
                    if (eventType == XMLEvent.CHARACTERS) {
                        productBO.setPrice(Long.parseLong(reader.getText()));
                    }
                    break;
                case "nvMid":
                    eventType = reader.next();
                    if(eventType == XMLEvent.CHARACTERS) {
                        productBO.setNvMid(reader.getText());
                    }
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
                        if(lowPriceProductMall) {
                            lowProductByMall.add(productBO);
                        }else {
                            lowProduct.add(productBO);
                        }
                    }
                    break;
            }
        }
    }

    private int parsingXMLHeaderElement(XMLStreamReader reader, List<String> constrantKeys, StringBuffer stringBuilder, int mainCategory, int eventType) throws XMLStreamException {
        if (eventType == XMLEvent.START_ELEMENT) {
            if (reader.getName().getLocalPart().equals("modelProduct")) {
                elementMaps = new HashMap<>();
                stringBuilder.append( "<modelProduct>\n");
                mainCategory += 1;
            }
        }

        if (eventType == XMLStreamConstants.START_ELEMENT) {
            String elementName = reader.getLocalName();
            if (constrantKeys.contains(elementName)) {
                final String elementText = reader.getElementText();
                elementMaps.put(elementName, elementText);
                if(elementName.equals("productName") || elementName.equals("cateName") || elementName.equals("fullCateCode") || elementName.equals("fullCateName"))
                    stringBuilder.append("<").append(elementName).append("><![CDATA[").append(elementText).append("]]></").append(elementName).append(">\n");
                else
                    stringBuilder.append("<").append(elementName).append(">").append(elementText).append("</").append(elementName).append(">\n");
            }
        }
        return mainCategory;
    }

    private void doneMainXMLBlockedAppend(List<NaverProductBO> lowProduct, List<NaverProductBO> lowProductByMall, StringBuffer stringBuilder) {
        stringBuilder.append( "\t<lowestProductList>\n");
        lowPriceProductParsing(stringBuilder, lowProduct);
        stringBuilder.append( "\t</lowestProductList>\n");

        stringBuilder.append( "<lowestProductListByMall>\n");
        lowPriceProductParsing(stringBuilder, lowProductByMall);
        stringBuilder.append( "\t</lowestProductListByMall>\n");

        stringBuilder.append( "</modelProduct>\n");
    }

    private void lowPriceProductParsing(StringBuffer stringBuilder,  List<? extends NaverProductBO> products) {
        for( NaverProductBO product : products) {
            stringBuilder.append("\t\t<product>\n");
            stringBuilder.append("\t\t\t<ranking>").append(product.getRanking()).append("</ranking>\n");
            stringBuilder.append("\t\t\t<price>").append(product.getPrice()).append("</price>\n");
            stringBuilder.append("\t\t\t<deliveryCost>").append(product.getDeliveryCost()).append("</deliveryCost>\n");
            stringBuilder.append("\t\t\t<nvMid>").append(product.getNvMid()).append("</nvMid>\n");
            stringBuilder.append("\t\t\t<mallId>").append(product.getMallId()).append("</mallId>\n");
            stringBuilder.append("\t\t\t<mallPid>").append(product.getMallPid()).append("</mallPid>\n");
            stringBuilder.append("\t\t</product>\n");
        }
    }


    private void nioBufferWriteToFile(StringBuffer stringBuilder) throws IOException {
        Path path = Paths.get("/Users/a1101381/naver_data/parse/projects_"+extFileName+".xml");
        try {
            Files.createFile(path);
        }catch(Exception ignored) {}

//        long fileSize = Files.size(path);
//        if ((fileSize >= gigabyte) && (fileSize < terabyte)) {
//            int maxByteSize = Math.toIntExact(fileSize / megabyte);
//            if( maxByteSize > 2000) {
//                extFileName += 1;
//                System.out.println("### file seq " + extFileName + " , " + maxByteSize );
//            }
//        }

        try (FileChannel channel = FileChannel.open(path, StandardOpenOption.READ)) {
            long fileSize = channel.size();
            if ((fileSize >= ByteTypes.GIGA_BYTE.toValue()) && (fileSize < ByteTypes.TERA_BYTE.toValue())) {
                int maxByteSize = Math.toIntExact(fileSize / ByteTypes.MEGA_BYTE.toValue());
                if( maxByteSize > ByteTypes.GIGA_BYTE.toByteValue(2)) {
                    extFileName += 1;
                    System.out.println("### file seq " + extFileName + " , " + maxByteSize );
                }
            }
        }

        BufferedWriter bufferedWriter = Files.newBufferedWriter(path, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        bufferedWriter.write(stringBuilder.toString());
        bufferedWriter.flush();
        bufferedWriter.close();

        //Stax parser : 0:11:14.460 write 방식
//        FileChannel fileOut = new FileOutputStream(path.toFile(), true).getChannel();
//        fileOut.write(ByteBuffer.wrap(stringBuilder.toString().getBytes(StandardCharsets.UTF_8)));
//        fileOut.close();
    }

    private void parseHeaderElement(List<NaverProductBO> product, List<NaverModelBO> modelBOList) {
        NaverModelBO modelBO = new NaverModelBO();
        modelBO.setMatchNvMid(elementMaps.get("matchNvMid"));
        modelBO.setModelType(elementMaps.get("modelType"));
        modelBO.setIsPopularModel(elementMaps.get("isPopularModel"));
        modelBO.setProductName(elementMaps.get("productName"));
        modelBO.setCateCode(elementMaps.get("cateCode"));
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

    private void createFileXmlSpilt(List<NaverModelBO> modelBOList, int seq) {
        NaverRootBO rootBO = new NaverRootBO();
        rootBO.setModelProduct(modelBOList);
        JAXBContext context = null;
        try {
            context = JAXBContext.newInstance(NaverRootBO.class);
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshaller.marshal(rootBO, new PrintWriter(new FileOutputStream("/Users/a1101381/naver_data/projects"+"_"+seq+".xml")));
        } catch (JAXBException | FileNotFoundException e) {
            e.printStackTrace();
        }

//        Properties outputProperties = new Properties();
//        outputProperties.put(javax.xml.transform.OutputKeys.METHOD, "xml");
//        outputProperties.put(javax.xml.transform.OutputKeys.INDENT, "yes");
//        outputProperties.put("{http://xml.apache.org/xslt}indent-amount", "2");
//        outputProperties.put(javax.xml.transform.OutputKeys.OMIT_XML_DECLARATION, "yes");
//
//
//        for( NaverModelBO modelBO : modelBOList ) {
//            XMLBuilder2 builder2 = XMLBuilder2.create("modelProduct");
//            builder2.e("matchNvMid").t(modelBO.getMatchNvMid()).up()
//                    .e("isPopularModel").t(modelBO.getIsPopularModel()).up()
//                    .e("productName").t(modelBO.getProductName()).up()
//                    .e("cateCode").t(modelBO.getCateCode()).up()
//                    .e("cateName").t(modelBO.getCateName()).up()
//                    .e("fullCateCode").t(modelBO.getFullCateCode()).up()
//                    .e("fullCateName").t(modelBO.getFullCateName()).up()
//                    .e("lowestPrice").t(String.valueOf(modelBO.getLowestPrice())).up()
//                    .e("lowestPriceDevice").t(modelBO.getLowestPriceDevice()).up()
//                    .e("productCount").t(String.valueOf(modelBO.getProductCount())).up()
////                    .e("makerName").t(modelBO.getMakerName()).up()
////                    .e("brandName").t(modelBO.getBrandName()).up()
////                    .e("useAttr").t(modelBO.getUseAttr()).up()
//                    .e("modelType").t(modelBO.getModelType()).up()
//            ;
//
//            for (NaverProductBO productBO : modelBO.getLowestProductList().getProduct()) {
//                builder2
//                        .e("product")
//                        .e("ranking").t(productBO.getMallPid()).up()
//                        .e("price").t(String.valueOf(productBO.getPrice())).up()
//                        .e("deliveryCost").t(String.valueOf(productBO.getDeliveryCost())).up()
//                        .e("nvMid").t(productBO.getNvMid() != null ? productBO.getNvMid(): "").up()
//                        .e("mallId").t(productBO.getMallId() != null ? productBO.getMallId(): "").up()
//                        .e("mallPid").t(productBO.getMallPid() != null ? productBO.getMallPid(): "").up()
//                        .up();
//            }
//            builder2.up();
//
//            PrintWriter writer = null;
//            try {
//                writer = new PrintWriter(new FileOutputStream("/Users/a1101381/naver_data/projects"+"_"+seq+".xml"));
//            } catch (FileNotFoundException e) {
//                e.printStackTrace();
//            }
//            builder2.toWriter(writer, outputProperties);
//        }
    }

    private JAXBContext jaxbContext = null;
    private Unmarshaller unmarshaller = null;
    private int modelCnt = 0;
    private final String XML_HEADER = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
    private final String XML_ELEMENT = "modelProduct";
    private final StringBuffer xmlElementStr = new StringBuffer();
    private int modelFetchSize =0;
    private BlockingQueue<List<CollectBO>> naverLumpQueue;


    public void lineParser(final String fileNamePath ) {
        List<CollectBO> resultList = new ArrayList<CollectBO>();
        boolean isCompleted = true;

        BufferedReader input;

        try {
            input = new BufferedReader(new InputStreamReader(new FileInputStream(fileNamePath), "UTF-8"));

            jaxbContext = JAXBContext.newInstance(NaverRootBO.class);
            unmarshaller = jaxbContext.createUnmarshaller();

            while(input.ready()){
                try {
                    String txt = input.readLine().trim();
                    xmlTmpStr.append(txt);
                    if(txt.indexOf("</"+XML_ELEMENT+">") > -1){
                        if(xmlTmpStr.indexOf(XML_HEADER) <= -1){
                            xmlElementStr.append(XML_HEADER);
                        }
                        String XML_ROOT = "modelProductList";
                        if(xmlTmpStr.indexOf("<"+ XML_ROOT +">") <= -1){
                            xmlElementStr.append("<"+ XML_ROOT +">");
                        }
                        xmlElementStr.append(xmlTmpStr.toString());
                        if(xmlTmpStr.indexOf("</"+ XML_ROOT +">") <= -1){
                            xmlElementStr.append("</"+ XML_ROOT +">");
                        }
                        NaverRootBO root = (NaverRootBO)unmarshaller.unmarshal(new StringReader(xmlElementStr.toString()));
                        xmlElementStr.delete(0,  xmlElementStr.toString().length());
                        xmlTmpStr.delete(0,  xmlTmpStr.toString().length());

//                        List<CollectBO> parseCollectBOs = convertCollectBO(root);
//                        for (CollectBO collectBO : parseCollectBOs) {
//                            resultList.add(collectBO);
//                        }

                        if(resultList.size() > 0 && (modelCnt % modelFetchSize) == 0) {
                            naverLumpQueue.offer(resultList);
                            resultList = new ArrayList<>();
                        }
                    }
                } catch (Exception e) {
                    xmlElementStr.delete(0, xmlElementStr.toString().length());
                    xmlTmpStr.delete(0, xmlTmpStr.toString().length());
                }
            }

            if(resultList.size() > 0){
                naverLumpQueue.offer(resultList);
            }

        } catch (Exception e){
            isCompleted = false;
            e.printStackTrace();
        }
    }

    public List<CollectBO> convertCollectBO(NaverRootBO naverRootBO){
        List<CollectBO> resultList = new ArrayList<>();
        List<NaverModelBO> naverModelBOs = null;

        if(naverRootBO.getModelProduct() != null){
            naverModelBOs = naverRootBO.getModelProduct();

            for (NaverModelBO naverModelBO : naverModelBOs) {
                CollectBO collectBO = new CollectBO();
                collectBO.setPartnerCd("NAVER");
                collectBO.setModelNo(naverModelBO.getMatchNvMid());
                collectBO.setModelNm(naverModelBO.getProductName());
                collectBO.setModelMngTypCd("MANUAL".equals(naverModelBO.getModelType()) ? "01" : "02" );

                if("true".equals(naverModelBO.getUseAttr())){
                    if(naverModelBO.getAttrList() != null && CollectionUtils.isNotEmpty(naverModelBO.getAttrList().getAttr())){
                        List<NaverAttrModelBO> naverAttrModelBOs = naverModelBO.getAttrList().getAttr();
                        for (NaverAttrModelBO naverAttrModelBO : naverAttrModelBOs) {
                            collectBO.setModelSubNo(naverAttrModelBO.getAttrId());
                            collectBO.setModelNm(naverModelBO.getProductName() + "^" + naverAttrModelBO.getAttrName());

                            if(naverAttrModelBO.getAttrProductList() != null && CollectionUtils.isNotEmpty(naverAttrModelBO.getAttrProductList().getProduct())){
                                List<NaverProductBO> productBOs = naverAttrModelBO.getAttrProductList().getProduct();
//                                setNaverLPInfo(resultList, collectBO, productBOs);
                            }
                        }
                    }
                }else{
                    if(naverModelBO.getLowestProductList() != null && CollectionUtils.isNotEmpty(naverModelBO.getLowestProductList().getProduct())){
                        List<NaverProductBO> productBOs = naverModelBO.getLowestProductList().getProduct();
//                        setNaverLPInfo(resultList, collectBO, productBOs);
                    }
                }
            }
        }

        return resultList;
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
