package Main;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.*;

import static java.lang.System.exit;

public class Parse {
    private static final Collection<String> stopWords = initStopWords();

    private int termIndex;

    public String docContent;
    public Document document;
    //private static final Collection<String> month = new ArrayList<>(Arrays.asList("JANUARY", "FEBRUARY", "MARCH","APRIL","MAY","JUNE","JULY","AUGUST","SEPTEMBER","OCTOBER","NOVEMBER","DECEMBER"));

    private static Collection<String> initStopWords() {
        try {
            String stopWordsContent= ReadFile.getContent(new File("stop_words.txt"));
            return new HashSet<>(Arrays.asList(stopWordsContent.split(" ")));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }



    public Parse(String content, Document document){
        this.docContent = content;
        this.document = document;
        this.termIndex=0;
    }
    public void ParseFile(){
        StringTokenizer stk=new StringTokenizer(docContent, " \t\n\r\f,.:;?![]/()<>\"");
        while(stk.hasMoreElements() ){
            String token = stk.nextToken();
            if(stopWords.contains(token))
                continue;
            Stemmer stemmer = new Stemmer();
            stemmer.add(token.toCharArray(),token.length());
            stemmer.stem();
            parseTokens(stemmer.toString(),stk);

        }
    }

    private Term parseTokens(String token,StringTokenizer stk){
        /*
        if(isNumberic(token)){
            Double.parseDouble(token);
            token = Double.parseDouble(new DecimalFormat("##.##").format(Double.parseDouble(token))) + "";
        }
        */
        Term.addTerm(token,"",document,termIndex);
        termIndex +=1;

        return null;
    }




    private boolean isNumberic(String s) {
        return s != null && s.matches("[-+]?\\d*\\.?\\d+");
    }


}
