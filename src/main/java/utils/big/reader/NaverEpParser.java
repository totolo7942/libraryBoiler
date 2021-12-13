package utils.big.reader;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.time.StopWatch;
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
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

public class NaverEpParser {

    private String XML_HEADER = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
    private String XML_ROOT = "modelProductList";
    private String XML_ELEMENT = "modelProduct";
    private StringBuffer xmlTmpStr = new StringBuffer();
    private StringBuffer xmlElementStr = new StringBuffer();
    Map<String, String> elementMaps = new HashMap<>();
    private int extFileName=0;
    StopWatch stopWatch = new StopWatch();

    public List<NaverModelBO> staxParser(Path path) throws XMLStreamException, IOException, JAXBException {
        XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
        XMLOutputFactory output = XMLOutputFactory.newInstance();
        XMLStreamReader reader = xmlInputFactory.createXMLStreamReader(new FileInputStream(path.toFile()));
//        XMLStreamReader reader = xmlInputFactory.createXMLStreamReader(new URL("http://localhost:8083/naver_ep_2g.xml").openConnection().getInputStream());
//        int eventType = reader.getEventType();

        List<NaverProductBO> product = null;
        NaverProductBO productBO = null;
        List<NaverModelBO> modelBOList = new ArrayList<>();
        List<String> constrantKeys = List.of("matchNvMid", "modelType", "isPopularModel", "productName", "cateCode" ,
                "cateName", "fullCateCode", "fullCateName", "lowestPrice", "lowestPriceDevice", "productCount", "useAtte"
        );

        StringBuilder stringBuilder = new StringBuilder();


        int fileseq=1;
        while (reader.hasNext()) {
            int eventType = reader.next();

            if (eventType == XMLEvent.START_ELEMENT) {
                if (reader.getName().getLocalPart().equals("modelProduct")) {
                    elementMaps = new HashMap<>();
                    stringBuilder.setLength(0);
                    stringBuilder.append( "<modelProduct>\n");
                    stopWatch.reset();
                    stopWatch.start();
                    fileseq +=1;

                }
            }

            //Constran값을 빼옴
            if (eventType == XMLStreamConstants.START_ELEMENT) {
                String elementName = reader.getLocalName();
                if (constrantKeys.contains(elementName)) {
                    final String elementText = reader.getElementText();
                    elementMaps.put(elementName, elementText);
                    stringBuilder.append( "<"+elementName+">"+ elementText + "</"+elementName+">\n");
                }
            }

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
//                    extracted(product, modelBOList);
                    stringBuilder.append( "<lowestProductList>\n");

                    for( NaverProductBO productBO1 : product) {
                        stringBuilder.append( "\t\t<product>\n");
                        stringBuilder.append( "\t\t\t<ranking>"+ productBO1.getRanking() + "</ranking>\n");
                        stringBuilder.append( "\t\t\t<price>"+ productBO1.getPrice() + "</price>\n");
                        stringBuilder.append( "\t\t\t<deliveryCost>"+ productBO1.getDeliveryCost() + "</deliveryCost>\n");
                        stringBuilder.append( "\t\t\t<nvMid>"+ productBO1.getNvMid() + "</nvMid>\n");
                        stringBuilder.append( "\t\t\t<mallId>"+ productBO1.getMallId() + "</mallId>\n");
                        stringBuilder.append( "\t\t\t<mallPid>"+ productBO1.getMallPid() + "</mallPid>\n");
                        stringBuilder.append( "\t\t</product>\n");
                    }
                    stringBuilder.append( "\t</lowestProductList>\n");
                    stringBuilder.append( "</modelProduct>\n");

                    if(fileseq > 0) {
                        NioFileWrite(stringBuilder, "/Users/a1101381/naver_data/projects");
                        fileseq = 0;
                    }
                }
            }
        }
//        product.forEach(NaverProductBO::toString);
        return modelBOList;
    }

    long kilobyte = 1024;
    long megabyte = kilobyte * 1024;
    long gigabyte = megabyte * 1024;
    long terabyte = gigabyte * 1024;

    private void NioFileWrite(StringBuilder stringBuilder, String defaultFileName) throws IOException {

        Path path = Paths.get("/Users/a1101381/naver_data/projects_"+extFileName+".xml");
        try {
            path.toFile().createNewFile();
            Files.createDirectories(path.getParent());
        }catch(Exception e ) {}

        long fileSize = Files.size(path);
        if ((fileSize >= gigabyte) && (fileSize < terabyte)) {
            int maxByteSize = Math.toIntExact(fileSize / megabyte);
            if( maxByteSize > 2000) {
                extFileName += 1;
                stopWatch.stop();
                System.out.println("### file seq " + extFileName + " , " + maxByteSize + " = " + stopWatch);
            }
        }

        FileChannel fileOut = new FileOutputStream(path.toFile(), true).getChannel();
        fileOut.write(ByteBuffer.wrap(stringBuilder.toString().getBytes(StandardCharsets.UTF_8)));
        fileOut.close();
    }

    private void extracted(List<NaverProductBO> product, List<NaverModelBO> modelBOList) {
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
    private BlockingQueue<List<CollectBO>> naverLumpQueue;
    private int modelFetchSize;
    private int modelCnt = 0;


    public void lineParser(final String fileNamePath ) throws JAXBException {
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