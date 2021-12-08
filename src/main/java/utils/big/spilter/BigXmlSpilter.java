package utils.big.spilter;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class BigXmlSpilter {

    private  final static Logger log = Logger.getGlobal();

    public static List<Path> largeXmlSplitter(Path readPath, int sizeOfFileInMB, String mkdirsForlder, String rootEle, String splitEle) throws Exception{
        int counter = 1;
        List<Path> files = new ArrayList<>();
        long sizeOfChunk = 1024 * 1024 * sizeOfFileInMB;// 21억이 넘을 수 있으므로 long 처리..
        String eof = System.lineSeparator();

        try (BufferedReader br = Files.newBufferedReader(readPath, Charset.forName("UTF-8"))){

            String fileName = FilenameUtils.getBaseName(readPath.getFileName().toString());
            String extension = FilenameUtils.getExtension(readPath.getFileName().toString());

            String readStrPath = readPath.getParent() + "/split/" + mkdirsForlder;
            FileUtils.deleteDirectory(new File(readStrPath));//import org.apache.commons.io.FileUtils; 라이브러리 사용

            log.info("filePath : {}" + readPath.getParent());
            log.info("fileSize : {}" + Files.size(readPath));
            log.info("fileName : {}" + readPath.getFileName().toString());

            String preFixXmlEle = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
            String xmlStartRootEle = "<" + rootEle + ">";
            String xmlEndRootEle = "</" + rootEle + ">";
            String xmlStartSplitEle = "<" + splitEle + ">";//실제로 이 구간이 반복 구간이며 splitter 할 구간이다..
            String xmlEndSplitEle = "</" + splitEle + ">";
            String line = br.readLine();

            while(line != null){
                //일단 파일을 먼저 생성을 한다..
                Path newFile = Paths.get(readStrPath, fileName + "_" + String.format(("%03d" + extension), counter++));
                Files.createDirectories(newFile.getParent());//폴더를 신규 생성
                try(BufferedWriter bufferWriter  = Files.newBufferedWriter(newFile)){
                    long fileSize = 0;
                    boolean flag = false;

                    bufferWriter.write(preFixXmlEle);
                    bufferWriter.newLine();
                    bufferWriter.write(xmlStartRootEle);
                    bufferWriter.newLine();

                    while(line != null){

                        byte[] bytes = (line + eof).getBytes(Charset.defaultCharset());

                        if (fileSize + bytes.length > sizeOfChunk){
                            //여기서 마지막 엘리먼트가 xml의 엘리먼트인지 반드시 확인 처리를 해야 한다.. xml노드가 중간에 짤리면 안되므로..
                            if(!line.trim().equalsIgnoreCase(xmlEndSplitEle)){

                                while(line != null){
                                    bufferWriter.write(line);
                                    bufferWriter.newLine();
                                    if(line.trim().equalsIgnoreCase(xmlEndSplitEle)){
                                        bufferWriter.write(xmlEndRootEle);
                                        bufferWriter.newLine();
                                        break;
                                    }
                                    line = br.readLine();
                                }
                            }
                            break;
                        }

                        if(line.trim().equalsIgnoreCase(xmlStartSplitEle)) {
                            flag = true;
                        }

                        if(flag == true){
                            bufferWriter.write(line);
                            bufferWriter.newLine();
                            fileSize += bytes.length; //파일의 사이즈를 숫자로 들고 있으므로 부하를 피한다..메모리 효율적 처리...
                        }

                        if(line.trim().equalsIgnoreCase(xmlEndSplitEle)) {
                            flag = false;
                        }
                        line = br.readLine();

                        //마지막..처리
                        if(line == null){
                            bufferWriter.write(xmlEndRootEle);
                        }
                    }
                }
                files.add(newFile);
            }
        }
        return files;
    }
}
