package engine;


import gui.EngineMenu.Stemming;
import query.QuerySearcher;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.*;
import java.util.regex.Pattern;

/**
 *  Parser class used to parse document's content
 */
public class Parse {

    public static final Collection<String> stopWords = initStopWords();

    private int termIndex;
    public String docContent;
    public Document document;
    public static Stemming stemming;
    public static Stemmer stemmer;
    public static Pattern patternTH= Pattern.compile("([4-9]|[12][0-9]|[3][0])th");
    private QuerySearcher querySearcher;

    private static Collection<String> initStopWords() {

        try {
            String stopWordsContent= ReadFile.getContent(new File(Indexer.pathToCorpus +  "\\stop_words.txt"));
            return new HashSet<>(Arrays.asList(stopWordsContent.split(" ")));
        } catch (IOException e) {
        }
        return null;
    }

    public static String stem(String token){
        /**
         *  Performs Stemming if needed
         */
        if(stemming.isStem()) {
            stemmer.add(token.toCharArray(), token.length());
            stemmer.stem();
            return stemmer.toString();
        }
        return token;
    }

    public Parse(String content, Document document, Stemming stemming, QuerySearcher querySearcher){

        this.docContent = content;
        this.document = document;
        this.termIndex = 0;
        this.stemming=stemming;
        this.stemmer = stemming.isStem() ? new Stemmer() : null;
        this.querySearcher = querySearcher;
    }
    public void Parse(){
        /**
         *  Iteration over words and creating Terms out of them
         */
        StringTokenizer stk=new StringTokenizer(docContent, " \t\n\r\f:{};?!'[`]/|()<#>*&+-\"");
        while(stk.hasMoreElements() ){
            String token =stk.nextToken();

            parseTokens(token,stk);
        }
    }


    private void parseTokens(String token,StringTokenizer stk){

            token = removeComma(token);
            boolean dot=false;
            if (isNumeric(token)) {//if NUMBER
                if (token.contains(".")) {
                    Double.parseDouble(token);
                    token = Double.parseDouble(new DecimalFormat("##.##").format(Double.parseDouble(token))) + "";
                    dot=true;
                }
                String nextTkn = removeComma(stk.nextToken());
                String nextTknLow = nextTkn.toLowerCase();
                if (nextTknLow.equals("percent") || nextTknLow.equals("percentage")) {
                   stemStop(token + " percent", document, termIndex);
                    termIndex += 1;
                }
                else if (!dot&&Term.Month.isMonth(nextTkn)) {

                    ParseDDMONTH(token, nextTkn, stk);
                } else { /* is simple number */
                    stemStop(token, document, termIndex);
                    termIndex += 1;
                    parseTokens(nextTkn, stk);
                }
            } else {

                token=removeDot(token);
                if (token.length() > 1&&(token.endsWith("%") || token.startsWith("$")) ) {//percent or dollar
                    if (token.endsWith("%")) {
                        token = token.substring(0, token.length() - 1);
                        if (isNumeric(token)) {
                            if(token.contains(".")) {
                                Double.parseDouble(token);
                                token = Double.parseDouble(new DecimalFormat("##.##").format(Double.parseDouble(token))) + "";
                            }
                            stemStop(token + " percent", document, termIndex);
                            termIndex += 1;
                        }
                    } else {
                        token = token.substring(1, token.length());
                        if (isNumeric(token)) {
                            if(token.contains(".")) {
                                Double.parseDouble(token);
                                token = Double.parseDouble(new DecimalFormat("##.##").format(Double.parseDouble(token))) + "";
                            }
                            stemStop(token + " dollars", document, termIndex);
                            termIndex += 1;
                        }
                    }

                } else if (Term.Month.isMonth(token)) {
                    String nextTkn=stk.nextToken();
                    if (isNumeric(nextTkn)&&!nextTkn.contains(".")) {
                        if (nextTkn.length() == 4){
                            ParseMONTHYYYY(token,nextTkn,stk);
                        }
                        else if ((nextTkn.length() == 1 || nextTkn.length() == 2))
                            ParseMonthDD(token, nextTkn, stk);
                    }
                    else{
                        if(token.equals(""))
                            return;
                        stemStop(token.toLowerCase(), document, termIndex);
                        termIndex += 1;
                        parseTokens(nextTkn,stk);
                    }
                }

                else if(token.length()>1 && Character.isUpperCase(token.charAt(0))){//upper case words
                    if(stk.hasMoreElements()) {
                        String nextTkn = stk.nextToken();
                        token=token.toLowerCase();
                        if (nextTkn.length()>1&&Character.isUpperCase(nextTkn.charAt(0))) {
                            if(Term.Number.isNumber(token))
                                stemStop(Term.Number.getNumber(token).toString(),document,termIndex);
                            else
                               stemStop(token, document, termIndex);
                            boolean check = true;
                            termIndex += 1;
                            while (nextTkn.length()>1&&Character.isUpperCase(nextTkn.charAt(0))&&!stopWords.contains(nextTkn.toLowerCase())) {
                                String temp=nextTkn;
                                nextTkn=removeComma(nextTkn).toLowerCase();
                                nextTkn=removeDot(nextTkn);
                                if(Term.Number.isNumber(nextTkn))
                                    stemStop(Term.Number.getNumber(nextTkn).toString(),document,termIndex);
                                else
                                    stemStop(nextTkn, document, termIndex);
                                termIndex += 1;
                                stemStop(token+" "+nextTkn,document,termIndex);
                                termIndex++;
                                if (!stk.hasMoreElements()||temp.charAt(temp.length()-1)==','||temp.charAt(temp.length()-1)=='.')break;
                                token=nextTkn;
                                nextTkn=stk.nextToken();
                            }

                        } else {
                            if(Term.Number.isNumber(token))
                                stemStop(Term.Number.getNumber(token).toString(), document, termIndex);
                            else {
                               stemStop(token.toLowerCase(), document, termIndex);
                            }
                            termIndex += 1;
                            parseTokens(nextTkn, stk);
                        }
                    }
                    else {
                        if(token.equals(""))
                            return;
                        if(Term.Number.isNumber(token))
                            stemStop(Term.Number.getNumber(token).toString(), document, termIndex);
                        else
                            stemStop(token.toLowerCase(), document, termIndex);
                        termIndex += 1;
                    }
                }
                else if(patternTH.matcher(token).find()) {
                    String temp=stk.nextToken();
                    if(Term.Month.isMonth(temp)){
                        token=token.substring(0,token.length()-2);
                        ParseDDMONTH(token,temp,stk);
                    }
                    else{
                        stemStop(token, document, termIndex);
                        termIndex++;
                        parseTokens(temp,stk);
                    }
                }
                else {
                    if(token.equals(""))
                        return;
                    if(Term.Number.isNumber(token))
                       stemStop(Term.Number.getNumber(token).toString(), document, termIndex);
                    else{
                       stemStop(token.toLowerCase(), document, termIndex);
                    }
                    termIndex += 1;
                }
            }


    }

    private void ParseDDMONTH(String token,String nextTkn, StringTokenizer stk){
        int day=Integer.parseInt(token);
        if(day>0&&day<32) {
            String nextNextoken = stk.nextToken();
            String year = yearCheck(nextNextoken);
            if (year != null) {//DD MONTH YY/DD MONTH YY->DD/MM/YYYY
                stemStop(token + "/" + Term.Month.getMonth(nextTkn) + "/" + year, document, termIndex);
                termIndex += 1;
            } else {//DD/Month->DD/MM
                stemStop(token + "/" + Term.Month.getMonth(nextTkn), document, termIndex);
                termIndex += 1;
                parseTokens(nextNextoken, stk);
            }
        }
        else{

           stemStop(token, document, termIndex);
            termIndex++;
            parseTokens(nextTkn,stk);
        }
    }
    private void ParseMonthDD(String token,String nextTkn, StringTokenizer stk){//token=month, nextTKN=number with 1,2 or 4 chars
        int day=Integer.parseInt(nextTkn);
        if(day>0&&day<32) {
            String nextNextoken = stk.nextToken();
            String year = yearCheck(nextNextoken);
            if (year != null) {
               stemStop(nextTkn+"/"+Term.Month.getMonth(token)+"/"+year,document,termIndex);
                termIndex++;
            } else {
                stemStop(nextTkn+"/"+Term.Month.getMonth(token),document,termIndex);
                termIndex++;
                parseTokens(nextNextoken,stk);
            }
        }
        else{
            stemStop(token.toLowerCase(), document, termIndex);
            termIndex++;
            parseTokens(nextTkn,stk);
        }

    }
    private void ParseMONTHYYYY(String token,String nextTkn,StringTokenizer stk){//token=month nextTkn=YEAR
        String year = yearCheck(nextTkn);
        if(year!=null) {
            stemStop(Term.Month.getMonth(token) + "/" + year, document, termIndex);
            termIndex++;
        }
        else {
            stemStop(token.toLowerCase(), document, termIndex);
            termIndex++;
            parseTokens(nextTkn,stk);
        }


    }

    private String yearCheck(String s) {
        if (isNumeric(s)) {
            if (s.length() == 4) {
                return s;
            } else if (s.length() == 2) {//DD/MM/YY->DD/MM/YYYY
                if (s.charAt(0) > '1')
                    s = "19" + s;
                else
                    s = "20" + s;
                return s;
            }
            else
                return null;
        }
        return null;
    }
    public void stemStop(String value,Document document,int termIndex){
        /**
         * Last step in parser, after the parser we eliminate stopwords and perform stepping if needed
         */
        if (!stopWords.contains(value)) {

            if (querySearcher == null) {
                Term.addTerm(stem(value), document, termIndex);

            } else{
                if(!querySearcher.isExtension())
                    querySearcher.addQueryTerm(stem(value));
                else{
                    querySearcher.addExtensionTerm(stem(value));
                }
            }
        }

    }

    public static String removeComma(String s) {
        /**
         * Last step in parser, after the parser we eliminate stopwords and perform stepping if needed
         */
            return s.replace(",", "");
    }
    public static String removeDot(String s){
        /**
         * @return true if the String is a number, false otherwise
         */
        return s.replace(".","");
    }
    public static boolean isNumeric(String s) {
        /**
         * @return true if the String is a number, false otherwise
         */
        return s != null && s.matches("[-+]?\\d*\\.?\\d+");
    }

}
