package Main;

import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ReadFile {
    String path;
    int counter =0;


    public ReadFile(String path) throws IOException {
        this.path=path;
    }
    public void readAll() throws IOException {
        File dir = new File(path);
        File[] directoryListing = dir.listFiles();
        if (directoryListing != null) {
            for (File child : directoryListing) {
                File[] currentFile=child.listFiles();
                String content=getContent(currentFile[0]);
                Pattern pattern= Pattern.compile("<TEXT>(.*?)</TEXT>",Pattern.DOTALL);
                Pattern patternDocN= Pattern.compile("<DOCNO>(.*?)</DOCNO>",Pattern.DOTALL);
                Matcher m = pattern.matcher(content);
                Matcher mDocNum=patternDocN.matcher(content);
                while (m.find()&&mDocNum.find()) {
                    Document document = new Document(mDocNum.group(),currentFile[0].toString());
                    String textCont=m.group();
                    String docNum=mDocNum.group();
                    Parse parser=new Parse(textCont,document);
                    parser.ParseFile();
                }
                counter+=1;
                //if(counter == 50)
                //    break;

            }
        }
    }

    public static String getContent(File myFile) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(myFile));
        try {
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();

            while (line != null) {

                sb.append(line);
                sb.append(" ");
                line = br.readLine();
            }
            return sb.toString();
        } finally {
            br.close();
        }

    }
}

