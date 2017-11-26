package Main;

import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ReadFile {
    String path;


    private ReadFile(){}

    public static void readTextFile(File currentFile) {
        File[] myCurrentFile = currentFile.listFiles();
        String content = null;
        try {
            content = getContent(myCurrentFile[0]);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Pattern pattern = Pattern.compile("<TEXT>(.*?)</TEXT>",Pattern.DOTALL);
         Pattern patternDocN = Pattern.compile("<DOCNO>(.*?)</DOCNO>",Pattern.DOTALL);
         Matcher m = pattern.matcher(content);
         Matcher mDocNum = patternDocN.matcher(content);
         while (m.find()&& mDocNum.find()) {
             Document document = new Document(mDocNum.group(),myCurrentFile[0].toString());
             String textCont = m.group();
             String docNum = mDocNum.group();
             Parse parser = new Parse(textCont,document);
             parser.ParseFile();
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

