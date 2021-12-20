package utils.big.reader;

import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.eclipse.persistence.jaxb.JAXBContext;
import utils.big.reader.defUtils.ByteTypes;
import utils.big.reader.entity.*;

import javax.xml.stream.*;
import javax.xml.stream.events.XMLEvent;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

public class NaverEpParserHandler extends XmlParseInterface {

    private final StringBuffer xmlTmpStr = new StringBuffer();
    private Map<String, String> elementMaps = new ConcurrentHashMap<>();
    private int extFileName=0;
    boolean useAttr = false;

    StopWatch stopWatch = new StopWatch();

    private final List<String> NAVER_HEADER_ELEMENTS = List.of("matchNvMid", "modelType", "isPopularModel", "productName", "cateCode" ,
            "cateName", "fullCateCode", "fullCateName", "lowestPrice", "lowestPriceDevice", "productCount", "useAttr"
    );

    private final List<String> NAVER_HEADER_ATTR_ELEMENTS = List.of("attrName", "attrId", "attrLowestPrice", "attrProductCount");


    @Override
    public void parsing(Path path, StringBuilder stringBuilder) throws XMLStreamException, IOException {
        XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
        XMLStreamReader reader = xmlInputFactory.createXMLStreamReader(new FileInputStream(path.toFile()));
        List<NaverProductBO> lowProduct = null;
        List<NaverProductBO> lowProductByMall = null;
        List<NaverProductBO> attrListAttr = null;
        NaverProductBO productBO = null;


        int ParseBlockSize =0;
        boolean lowPriceProductMall = false;
        boolean flag_attrList = false;

        stopWatch.reset();
        stopWatch.start();

        boolean processAttribute = false;
        int attrProductDone = 0;
        boolean attrListAttrHeader = true;

        while (reader.hasNext()) {
            int eventType = reader.next();


            ParseBlockSize = parsingXMLHeaderElement(reader, stringBuilder, ParseBlockSize, eventType);

            /**
             * 	private NaverProductListBO lowestProductList;
             * 	private NaverProductListBO mallProductList;
             * 	private NaverProductListBO lowestProductListByMall;
             * 	private NaverAttrRootBO attrList;
             * 	4개의 파싱 목록 종재
             */
            //AttrList Parsing
            if (eventType == XMLEvent.START_ELEMENT) {
                if (reader.getName().getLocalPart().equals("attrList")) {
                    stringBuilder.append( "\t<attrList>\n");
                    attrProductDone =0;
                    attrListAttrHeader = true;
                }
            }

            if (eventType == XMLEvent.START_ELEMENT) {
                if (reader.getName().getLocalPart().equals("attr")) {
                    if( attrProductDone != 0) {
                        stringBuilder.append("\t\t</attrProductList>\n");
                        stringBuilder.append( "\t</attr>\n");
                    }
                    stringBuilder.append( "\t<attr>\n");
                    processAttribute = true;
                }
            }



            if (eventType == XMLEvent.START_ELEMENT) {
                String elementName = reader.getLocalName();
                if (NAVER_HEADER_ATTR_ELEMENTS.contains(elementName)) {
                    final String elementText = reader.getElementText();
                    if (elementName.equals("attrName")) {
                        stringBuilder.append("\t<" + elementName + "><![CDATA[" + elementText + "]]></" + elementName + ">\n");
                    } else {
                        stringBuilder.append("\t\t\t<" + elementName + ">" + elementText + "</" + elementName + ">\n");
                    }
                    attrProductDone++;
                }

                if (reader.getName().getLocalPart().equals("attrProductList")) {
                    attrListAttr = new ArrayList<>();
                    flag_attrList = true;
                    lowPriceProductMall=false;
                    stringBuilder.append( "\t\t<attrProductList>\n");
                }


                if (reader.getName().getLocalPart().equals("lowestProductList")) {
                    if(processAttribute) {
                        stringBuilder.append("\t\t</attrProductList>\n");
                        stringBuilder.append("\t\t\t</attr>\n");
                        stringBuilder.append("\t\t\t</attrList>\n");
                        processAttribute = false;
                    }else if(useAttr && !processAttribute) {
                        stringBuilder.append("\n\t\t\t</attrList>\n");
                    }

                    lowProduct = new ArrayList<>();
                    flag_attrList = false;
                    lowPriceProductMall=false;
                }

                if (reader.getName().getLocalPart().equals("lowestProductListByMall")) {
                    lowProductByMall = new ArrayList<>();
                    lowPriceProductMall = true;
                    flag_attrList = false;
                }

                if (reader.getName().getLocalPart().equals("product")) {
                    productBO = new NaverProductBO();
                }
            }

            parsingXMLBodyElements(reader, lowProduct, lowPriceProductMall, lowProductByMall, productBO, eventType, flag_attrList, attrListAttr);

            preAttrListProduct(attrListAttr, flag_attrList, stringBuilder);

            ParseBlockSize = parsingXMLDoneElemntToWriteFile(reader, lowProduct, lowProductByMall, stringBuilder, ParseBlockSize, eventType, attrListAttr);
        }

        if(stringBuilder.length() > 0 ) {
            stringBuilder.append("</modelProductList>\n");
            nioBufferWriteToFile(stringBuilder);
        }

    }

    private void preAttrListProduct(List<NaverProductBO> attrListAttr, boolean flag_attrList, StringBuilder stringBuilder){
        if(attrListAttr != null && attrListAttr.size() > 0) {
            lowPriceProductParsing(stringBuilder, attrListAttr);
            attrListAttr.clear();
        }
    }

    private int parsingXMLDoneElemntToWriteFile(XMLStreamReader reader, List<NaverProductBO> lowProduct, List<NaverProductBO> lowProductByMall, StringBuilder stringBuilder, int ParseBlockSize, int eventType, List<NaverProductBO> attrListAttr) throws IOException {
        if (eventType == XMLEvent.END_ELEMENT) {
            if (reader.getName().getLocalPart().equals("modelProduct")) {
                doneMainXMLBlockedAppend(lowProduct, lowProductByMall, stringBuilder, attrListAttr);

                final int ELEMENT_OVERFLOW_LIMIT_COUNT = 5000; //Time:0:00:28.060, 40G:0:08:47.128
                if(ParseBlockSize > ELEMENT_OVERFLOW_LIMIT_COUNT) {
                    stopWatch.reset();
                    stopWatch.start();
                    nioBufferWriteToFile(stringBuilder);
                    stopWatch.stop();
                    System.out.println("append build : " + stopWatch);

                    ParseBlockSize =0;
                    stringBuilder.setLength(0);
                }
            }
        }

        return ParseBlockSize;
    }

    private void parsingXMLBodyElements(XMLStreamReader reader, List<NaverProductBO> lowProduct, boolean lowPriceProductMall, List<NaverProductBO> lowProductByMall, NaverProductBO productBO, int eventType,
                                        boolean flag_attrList, List<NaverProductBO> attrListAttr) throws XMLStreamException {

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
                        }else if ( flag_attrList ) {
                            attrListAttr.add(productBO);
                        } else{
                            lowProduct.add(productBO);
                        }
                    }
                    break;
            }
        }
    }

    private int parsingXMLHeaderElement(XMLStreamReader reader, StringBuilder stringBuilder, int mainCategory, int eventType) throws XMLStreamException {
        if (eventType == XMLEvent.START_ELEMENT) {
            if (reader.getName().getLocalPart().equals("modelProduct")) {
                stringBuilder.append( "<modelProduct>\n");
                mainCategory += 1;
            }
        }

        if (eventType == XMLStreamConstants.START_ELEMENT) {
            String elementName = reader.getLocalName();
            if (NAVER_HEADER_ELEMENTS.contains(elementName)) {
                final String elementText = reader.getElementText();
                if(elementName.equals("productName") || elementName.equals("cateName") || elementName.equals("fullCateCode") || elementName.equals("fullCateName")) {
                    stringBuilder.append("\t<" + elementName + "><![CDATA[" + elementText + "]]></" + elementName + ">\n");
                }else {
                    stringBuilder.append("\t<" + elementName + ">" + elementText + "</" + elementName + ">\n");
                    if(elementName.equals("useAttr"))
                        useAttr = elementText.equalsIgnoreCase("true");
                }
            }
        }

        return mainCategory;
    }

    private void doneMainXMLBlockedAppend(List<NaverProductBO> lowProduct, List<NaverProductBO> lowProductByMall, StringBuilder stringBuilder, List<NaverProductBO> attrListAttr) {


        stringBuilder.append( "\t<lowestProductList>\n");
        lowPriceProductParsing(stringBuilder, lowProduct);
        lowProduct.clear();
        stringBuilder.append( "\t</lowestProductList>\n");

        stringBuilder.append( "<lowestProductListByMall>\n");
        lowPriceProductMallParsing(stringBuilder, lowProductByMall);
        lowProductByMall.clear();
        stringBuilder.append( "\t</lowestProductListByMall>\n");

        stringBuilder.append( "</modelProduct>\n");
    }

    private void lowPriceProductParsing(StringBuilder stringBuilder, List<NaverProductBO> products) {
        for( NaverProductBO product : products) {
            stringBuilder.append("\t\t<product>\n");
            if( product.getRanking() > 0 )
                stringBuilder.append("\t\t\t<ranking>"+product.getRanking()+"</ranking>\n");

            if( product.getPrice() > 0 )
            stringBuilder.append("\t\t\t<price>"+product.getPrice()+"</price>\n");

//            if( product.getDeliveryCost() >= 0 product.getPrice() < 0 )
            stringBuilder.append("\t\t\t<deliveryCost>"+product.getDeliveryCost()+"</deliveryCost>\n");

            if(product.getNvMid() != null )
                stringBuilder.append("\t\t\t<nvMid>"+product.getNvMid()+"</nvMid>\n");
            if(product.getMallId() != null )
                stringBuilder.append("\t\t\t<mallId>"+product.getMallId()+"</mallId>\n");
            if(product.getMallPid() != null )
                stringBuilder.append("\t\t\t<mallPid>"+product.getMallPid()+"</mallPid>\n");

            stringBuilder.append("\t\t</product>\n");
        }

        if(products.size() <= 0 )
            stringBuilder.append("\n");
    }

    private void lowPriceProductMallParsing(StringBuilder stringBuilder, List<NaverProductBO> products) {
        for( NaverProductBO product : products) {
            stringBuilder.append("\t\t<product>\n");
            if( product.getRanking() > 0 )
                stringBuilder.append("\t\t\t<ranking>"+product.getRanking()+"</ranking>\n");

            if( product.getPrice() > 0 )
                stringBuilder.append("\t\t\t<price>"+product.getPrice()+"</price>\n");

            if( product.getDeliveryCost() > 0 )
                stringBuilder.append("\t\t\t<deliveryCost>"+product.getDeliveryCost()+"</deliveryCost>\n");

            if(product.getNvMid() != null )
                stringBuilder.append("\t\t\t<nvMid>"+product.getNvMid()+"</nvMid>\n");
            if(product.getMallId() != null )
                stringBuilder.append("\t\t\t<mallId>"+product.getMallId()+"</mallId>\n");
            if(product.getMallPid() != null )
                stringBuilder.append("\t\t\t<mallPid>"+product.getMallPid()+"</mallPid>\n");

            stringBuilder.append("\t\t</product>\n");
        }
    }

//    long kilobyte = 1024;
//    long megabyte = kilobyte * 1024;
//    long gigabyte = megabyte * 1024;
//    long terabyte = gigabyte * 1024;

    private void nioBufferWriteToFile(StringBuilder stringBuilder) throws IOException {
        Path path = Paths.get("/Users/a1101381/naver_data/parse/projects_"+extFileName+".xml");
        try {
            Files.createFile(path);
        }catch(Exception ignored) {}

//        long fileSize = Files.size(path);
//        if ((fileSize >= gigabyte) && (fileSize < terabyte)) {
//            int maxByteSize = Math.toIntExact(fileSize / megabyte);
//            if( maxByteSize > 1000) {
//                extFileName += 1;
//                System.out.println("### file seq " + extFileName + " , " + maxByteSize );
//            }
//        }

        try (FileChannel channel = FileChannel.open(path, StandardOpenOption.READ)) {
            long fileSize = channel.size();
            if ((fileSize >= ByteTypes.GIGA_BYTE.toValue()) && (fileSize < ByteTypes.TERA_BYTE.toValue())) {
                int maxByteSize = Math.toIntExact(fileSize / ByteTypes.MEGA_BYTE.toValue());
                if( maxByteSize > ByteTypes.GIGA_BYTE.toByteValue(1) / ByteTypes.MEGA_BYTE.toValue()) {
//                    extFileName += 1;
                    extFileName = 0;
                    System.out.println("### file seq " + extFileName + " , " + maxByteSize );
                }
            }
        }
        Path wpath = Paths.get("/Users/a1101381/naver_data/parse/projects_"+extFileName+".xml");
        try (BufferedWriter bufferedWriter = Files.newBufferedWriter(wpath, StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
            bufferedWriter.write(stringBuilder.toString());
            bufferedWriter.flush();
        }

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
        jakarta.xml.bind.JAXBContext context = null;
        try {
            context = JAXBContext.newInstance(NaverRootBO.class);
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshaller.marshal(rootBO, new PrintWriter(new FileOutputStream("/Users/a1101381/naver_data/projects"+"_"+seq+".xml")));
        } catch (FileNotFoundException | JAXBException e) {
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

    private final String XML_HEADER = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
    private final String XML_ELEMENT = "modelProduct";
    private final ThreadLocal<StringBuffer> xmlElementStr = ThreadLocal.withInitial(StringBuffer::new);
    private BlockingQueue<List<CollectBO>> naverLumpQueue;


    public void lineParser(final String fileNamePath ) {
        List<CollectBO> resultList = new ArrayList<CollectBO>();
        boolean isCompleted = true;

        BufferedReader input;

        try {
            input = new BufferedReader(new InputStreamReader(new FileInputStream(fileNamePath), StandardCharsets.UTF_8));

            jakarta.xml.bind.JAXBContext jaxbContext = JAXBContext.newInstance(NaverRootBO.class);
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();

            while(input.ready()){
                try {
                    String txt = input.readLine().trim();
                    xmlTmpStr.append(txt);
                    if(txt.indexOf("</"+XML_ELEMENT+">") > -1){
                        if(xmlTmpStr.indexOf(XML_HEADER) <= -1){
                            xmlElementStr.get().append(XML_HEADER);
                        }
                        String XML_ROOT = "modelProductList";
                        if(xmlTmpStr.indexOf("<"+ XML_ROOT +">") <= -1){
                            xmlElementStr.get().append("<"+ XML_ROOT +">");
                        }
                        xmlElementStr.get().append(xmlTmpStr);
                        if(xmlTmpStr.indexOf("</"+ XML_ROOT +">") <= -1){
                            xmlElementStr.get().append("</"+ XML_ROOT +">");
                        }
                        NaverRootBO root = (NaverRootBO) unmarshaller.unmarshal(new StringReader(xmlElementStr.toString()));
                        xmlElementStr.get().delete(0,  xmlElementStr.toString().length());
                        xmlTmpStr.delete(0,  xmlTmpStr.toString().length());

//                        List<CollectBO> parseCollectBOs = convertCollectBO(root);
//                        for (CollectBO collectBO : parseCollectBOs) {
//                            resultList.add(collectBO);
//                        }

                        int modelFetchSize = 0;
                        int modelCnt = 0;
                        if(resultList.size() > 0 && (modelCnt % modelFetchSize) == 0) {
                            naverLumpQueue.offer(resultList);
                            resultList = new ArrayList<>();
                        }
                    }
                } catch (Exception e) {
                    xmlElementStr.get().delete(0, xmlElementStr.toString().length());
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

        jakarta.xml.bind.JAXBContext jaxbContext = JAXBContext.newInstance(NaverRootBO.class);
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();

        while(input.ready()){
            try {
                String txt = input.readLine().trim();
                xmlTmpStr.append(txt);

                if(txt.contains("</" + XML_ELEMENT + ">")){
                    if(xmlTmpStr.indexOf(XML_HEADER) <= -1){
                        xmlElementStr.get().append(XML_HEADER);
                    }
//                    if(xmlTmpStr.indexOf("<"+XML_ROOT+">") <= -1){
//                        xmlElementStr.append("<"+XML_ROOT+">");
//                    }
                    xmlElementStr.get().append(xmlTmpStr);
//                    if(xmlTmpStr.indexOf("</"+XML_ROOT+">") <= -1){
//                        xmlElementStr.append("</"+XML_ROOT+">");
//                    }

                    System.out.println("## to string " + xmlElementStr);
                    NaverRootBO root = (NaverRootBO)unmarshaller.unmarshal(new StringReader(xmlElementStr.toString()));

                    System.out.println("## to size " + root.getModelProduct().size());

                    xmlElementStr.get().delete(0,  xmlElementStr.toString().length());
                    xmlTmpStr.delete(0,  xmlTmpStr.toString().length());
                }
            } catch (Exception e) {
                xmlElementStr.get().delete(0, xmlElementStr.toString().length());
                xmlTmpStr.delete(0, xmlTmpStr.toString().length());
            }
        }

        return true;
    }

    public void setNaverLumpQueue(BlockingQueue<List<CollectBO>> naverLumpQueue) {
        this.naverLumpQueue = naverLumpQueue;
    }
}


class ByteBufferBackedInputStream extends InputStream {

    ByteBuffer buf;
    public ByteBufferBackedInputStream(ByteBuffer buf) {
        this.buf = buf;
    }

    @Override
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
