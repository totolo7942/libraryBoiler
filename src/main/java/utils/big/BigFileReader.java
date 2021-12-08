package utils.big;

import utils.big.spilter.BigXmlSpilter;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class BigFileReader {

    public static void main(String[] args) throws Exception {
//        Path sourceNioFile1 = Paths.get("/Users/a1101381/naver_ep" , "v1.0_ep_all_sample_20190508.xml");

        Path sourceNioFile1 = Paths.get("/Users/a1101381/Develop/totolo7942/git/libraryBoiler/src/main/java/utils/big/spilter/", "epall.xml");
        List<Path> fileList1 = BigXmlSpilter.largeXmlSplitter(  sourceNioFile1 ,
                1 ,
                "naverEp" ,
                "modelProductList" ,
                "modelProduct");

    }
}
