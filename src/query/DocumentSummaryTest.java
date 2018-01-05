package query;

import engine.ReadFile;
import javafx.util.Pair;

import javax.print.Doc;
import java.io.File;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DocumentSummaryTest {
    public static void main(String[] args) throws Exception {

        DocumentSummarize alex=new DocumentSummarize("FBIS3-20471");
        List<Pair<String,Integer>> tal=alex.getTop5Sentences();
        for (Pair<String,Integer> sent:tal) {
            System.out.println("Rank: "+sent.getValue()+"\n"+ sent.getKey());
        }
        /*File file=new File("d:\\documents\\users\\kremians\\Documents\\corpus\\FB396001\\FB396001");
        String content= ReadFile.getContent(file);
        Pattern pattern = Pattern.compile("<TEXT>(.*?)</TEXT>",Pattern.DOTALL);
        String docnum="FBIS3-22";
        int index=content.indexOf(docnum);
        Matcher m = pattern.matcher(content);
        String textCont="";
        if(m.find(index))
            textCont= m.group();
        DocumentSummarize documentSummarize=new DocumentSummarize(textCont);
        List<Pair<String,Integer>> alex=documentSummarize.getTop5Sentences();
        for (Pair<String,Integer> sent:alex) {
            System.out.println("Rank: "+sent.getValue()+"\n"+ sent.getKey());
        }*/
    }
}
