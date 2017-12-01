package Main;

import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ReadFile {

    private ReadFile(){}

    public static long readTextFile(File currentFile) {
        File[] myCurrentFile = currentFile.listFiles();

        String fullPath = myCurrentFile[0].toString();
        int index = fullPath.lastIndexOf("\\");
        String fileName = fullPath.substring(index + 1);

        String content=null;

        try {
            content = getContent(myCurrentFile[0]);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Pattern pattern = Pattern.compile("<TEXT>(.*?)</TEXT>",Pattern.DOTALL);
        Pattern patternDocN = Pattern.compile("<DOCNO>(.+?)</DOCNO>",Pattern.DOTALL);
        Matcher m = pattern.matcher(content);
        Matcher mDocNum = patternDocN.matcher(content);
        while (m.find()&& mDocNum.find()) {
            String textCont = m.group();
            String docNum = mDocNum.group();
            String textContSub=docNum.substring(8,docNum.length()-9);
            Document document = Document.addDocument(fileName,textContSub);
            Parse parser = new Parse(textCont,document);
            parser.ParseFile();
        }
        return StringSizeEstimator.estimatedSizeOf(content);
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

