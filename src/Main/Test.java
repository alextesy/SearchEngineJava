package Main;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Test {
    public static ArrayList<String> test=new ArrayList<String>();
    private static final Collection<String> stopWords = initStopWords();

    public static void main(String[] args) {
        StringTokenizer stk=new StringTokenizer("<F P=106> [Report by P Bulger] </F>\n" +
                "  [Text] Pretoria March 29 SAPA -- The Transitional Executive \n" +
                "Council [TEC] was looking into greater security force powers \n" +
                "that would amount to a state of emergency being declared in \n" +
                "Natal/kwaZulu. TEC Executive Secretary Mac Maharaj told the TEC \n" +
                "on Tuesday [29 March] that security force action had already \n" +
                "been stepped up in the territory as violence continued to \n" +
                "escalate. He said the TEC's law and order sub-council was \n" +
                "devising regulations to give security forces greater powers.", " \t\n\r\f:;?!'[`]/|()<#>*&+-\"");
        String token;
        boolean check = true;

        while (stk.hasMoreElements()) {
            token = stk.nextToken();
            if (token.length()>1&&Character.isUpperCase(token.charAt(0))) {
                if (stk.hasMoreElements()) {
                    String nextTkn = stk.nextToken();
                    if (nextTkn.length()>1&&Character.isUpperCase(nextTkn.charAt(0))) {
                        token=token.toLowerCase();
                        System.out.println(token);
                        while (nextTkn.length()>1&&Character.isUpperCase(nextTkn.charAt(0))) {
                            nextTkn=nextTkn.toLowerCase();
                            System.out.println(nextTkn);
                            System.out.println(token+" "+nextTkn);
                            if(!stk.hasMoreElements()) break;
                            token = nextTkn;
                            nextTkn=stk.nextToken();
                        }

                    } else {
                        System.out.println(token.toLowerCase());
                        Parse(nextTkn);
                    }

                }
            }
            else {
                System.out.println(token.toLowerCase());
            }
        }

        //while(stk.hasMoreElements()){
        //    String token = stk.nextToken();
        //    if(stopWords.contains(token))
        //        continue;
        //    Stemmer stemmer = new Stemmer();
        //    stemmer.add(token.toCharArray(),token.length());
        //    stemmer.stem();
        //    parseTokens(stemmer.toString(),stk);
        //}
        //System.out.println("a");

    }
    private static void Parse(String s){
        System.out.println(s);
    }
    private static Collection<String> initStopWords() {
        try {
            String stopWordsContent= ReadFile.getContent(new File("stop_words.txt"));
            return new HashSet<>(Arrays.asList(stopWordsContent.split(" ")));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    private static Term parseTokens(String token,StringTokenizer stk){

        token = removeComma(token);
        if(isNumeric(token)){
            if(token.contains(".")) {
                Double.parseDouble(token);
                token = Double.parseDouble(new DecimalFormat("##.##").format(Double.parseDouble(token))) + "";
            }
            String nextTkn = removeComma(stk.nextToken());
            String nextTknLow=nextTkn.toLowerCase();
            if(nextTknLow.equals("percent") || nextTknLow.equals("percentag")){
                test.add(token + " percent" );
            }
            //TODO date implementation - you better use Month enum at class Term
            else if(Term.Month.isMonth(nextTkn)){
                ParseDDMONTH(token,nextTkn,stk);
            }
            else{ /* is simple number */
                test.add(token);
                parseTokens(nextTkn,stk);
            }
        }
        else if ((token.endsWith("%") || token.startsWith("$")) && token.length()>1){
            if(token.endsWith("%")){
                token = token.substring(0,token.length()-1);
                if(isNumeric(token)) {
                    Double.parseDouble(token);
                    token = Double.parseDouble(new DecimalFormat("##.##").format(Double.parseDouble(token))) + "";
                   test.add(token + " percent");
                }
            }
            else {
                token = token.substring(1, token.length());
                if (isNumeric(token)) {
                    Double.parseDouble(token);
                    token = Double.parseDouble(new DecimalFormat("##.##").format(Double.parseDouble(token))) + "";
                   test.add(token + " dollars");
                }
            }

        }
        else if(Term.Month.isMonth(token)){
            ParseMonthDD(token,stk.nextToken(),stk);
        }
        else{
            test.add(token);
        }
        return null;
    }

    private static void ParseDDMONTH(String token,String nextTkn, StringTokenizer stk){
        String nextNextoken=stk.nextToken();
        String year=yearCheck(nextNextoken);
        if(year!=null){//DD MONTH YY/DD MONTH YY->DD/MM/YYYY
            test.add(token + "/" + Term.Month.getMonth(nextTkn) + "/" + year);
        }
        else{//DD/Month->DD/MM
            test.add(token+"/"+Term.Month.getMonth(nextTkn));
            parseTokens(nextNextoken,stk);
        }
    }
    private static void ParseMonthDD(String token,String nextTkn, StringTokenizer stk){
        nextTkn=removeComma(nextTkn);
        if (isNumeric(nextTkn)){
            int day=Integer.parseInt(nextTkn);
            if(day>0&&day<32) {
                String nextNextoken = stk.nextToken();
                String year = yearCheck(nextNextoken);
                if (year != null) {
                    test.add(nextTkn+"/"+Term.Month.getMonth(token)+"/"+year);
                } else {
                    test.add(nextTkn+"/"+Term.Month.getMonth(token));
                    parseTokens(nextNextoken,stk);
                }
            }
        }
        else {
            test.add(token);
            parseTokens(nextTkn, stk);
        }
    }





    private static String yearCheck(String s) {
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

   