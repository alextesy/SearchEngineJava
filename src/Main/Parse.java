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
        this.termIndex = 0;
    }
    public void ParseFile(){
        StringTokenizer stk=new StringTokenizer(docContent, " \t\n\r\f.:;?!'[`]/|()<#>*&+-\"");
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

        token = removeComma(token);
        if((token.endsWith("%") || token.startsWith("$")) && token.length()>1){
            if(token.endsWith("%")){
                token = token.substring(0,token.length()-1);
                if(isNumeric(token)) {
                    Double.parseDouble(token);
                    token = Double.parseDouble(new DecimalFormat("##.##").format(Double.parseDouble(token))) + "";
                    Term.addTerm(token + " percent", document, termIndex);
                    termIndex += 1;
                }
            }
            else {
                token = token.substring(1, token.length());
                if (isNumeric(token)) {
                    Double.parseDouble(token);
                    token = Double.parseDouble(new DecimalFormat("##.##").format(Double.parseDouble(token))) + "";
                    Term.addTerm(token + " dollars", document, termIndex);
                    termIndex += 1;
                }
            }

        }

        if(isNumeric(token)){
            Double.parseDouble(token);
            token = Double.parseDouble(new DecimalFormat("##.##").format(Double.parseDouble(token))) + "";
            String nextTkn = removeComma(stk.nextToken());
            nextTkn = nextTkn.toLowerCase();
            if(nextTkn.equals("percent") || nextTkn.equals("percentag")){
                Term.addTerm(token + " percent" ,document,termIndex);
                termIndex +=1;
            }
            //TODO date implementation - you better use Month enum at class Term
            else if(nextTkn.equals("DATE")){


            }
            else{ /* is simple number */
                Term.addTerm(token,document,termIndex);
                termIndex+=1;
            }
        }
        else{
            Term.addTerm(token,document,termIndex);
            termIndex+=1;
        }
        return null;
    }


    public static String removeComma(String s){
        if(s.contains(",")){
        StringBuilder str= new StringBuilder();
            for(int i=0 ; i<s.length(); i++){
                char c = s.charAt(i);
                if(c != ','){
                    str.append(c);
                }
            }
            return str.toString();
        }
        else
            return s;
    }

    public static boolean isNumeric(String s) {
        return s != null && s.matches("[-+]?\\d*\\.?\\d+");
    }



}
