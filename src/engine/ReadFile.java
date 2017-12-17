package engine;

import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
/**
 * The class represents a static object that basically divides the File int Documents and send them to parser
 */
public class ReadFile {

    public static int docNumberOfFiles=0;
    private ReadFile(){}
    private static int counter =0;

    public static long readTextFile(File currentFile,boolean stemming) {
        /**
         * divides the File into Documents and send them to parser
         */
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
            System.out.println(counter+=1);
            String textCont = m.group();
            String docNum = mDocNum.group();
            String textContSub=docNum.substring(7,docNum.length()-8);
            textContSub=textContSub.replace(" ","");
            Document document = Document.addDocument(fileName,textContSub);
            Parse parser = new Parse(textCont,document,stemming);
            docNumberOfFiles+=1;
            parser.ParseFile();
        }
        return StringSizeEstimator.estimatedSizeOf(content);
    }



    public static String getContent(File myFile) throws IOException {
        /**
         * Reads the File
         * @return the content of the file as a String
         */
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

