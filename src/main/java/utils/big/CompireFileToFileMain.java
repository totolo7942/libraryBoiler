package utils.big;

import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;

public class CompireFileToFileMain {
    public static void main(String[] args) throws IOException {

        LineNumberReader reader1 = new LineNumberReader(new FileReader("/Users/a1101381/naver_data/naver_ep.xml"));
        LineNumberReader reader2 = new LineNumberReader(new FileReader("/Users/a1101381/naver_data/parse/projects_0.xml"));

//        LineNumberReader reader1 = new LineNumberReader(new FileReader( "/Users/a1101381/Develop/totolo7942/git/libraryBoiler/src/main/resources/NaverEpAttrVersion.xml"));

        final String name2 ="file";
        final String name1 ="file";

        String line1 = reader1.readLine();
        String line2 = reader2.readLine();

        while (line1 != null && line2 != null) {
            if (!line1.replaceAll("^[ \\t]+|[ \\t]+$", "").equals(line2.replaceAll("^[ \\t]+|[ \\t]+$", ""))) {
                System.out.println("differ at line " + reader1.getLineNumber() + ":" + "\n" + line1 + "\n" + line2);
                break;
            }
            line1 = reader1.readLine();
            line2 = reader2.readLine();
        }

        if (line1 == null && line2 != null)
            System.out.println("File first file End. has extra lines at line " + reader2.getLineNumber() + ":\n" + line2);
        else if (line1 != null && line2 == null)
            System.out.println("File secound file End. has extra lines at line " + reader1.getLineNumber() + ":\n" + line1);
        else
            System.out.println("File Equals DONE");
    }
}
