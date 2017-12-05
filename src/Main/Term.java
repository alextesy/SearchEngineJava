package Main;


import com.sun.org.apache.xml.internal.resolver.readers.ExtendedXMLCatalogReader;

import java.util.*;

import static Main.Document.addDocument;
import static Main.Indexer.currentTermDictionary;

public class Term{

    private Map<Document,List<Integer>> docDictionary;
    private String value;
    private Kind kind;
    private int termTDF=0;

    private Term(String value /*,Kind kind*/){
        this.docDictionary = new HashMap<>();
        this.value = value;
        /*this.kind = kind*/;
    }

    public static void addTerm(String value/*,String kind*/,Document document,int location){
        Term term;
        if(currentTermDictionary.containsKey(value)){
            term = currentTermDictionary.get(value);
            term.updatedDoc(document,location);
        }
        else{
            term = new Term(value/*,kind*/) ;
            term.updatedDoc(document,location);
            currentTermDictionary.put(value,term);
        }
        term.termTDF+=1;
    }

    private void updatedDoc(Document document,int location){
        document.setWordsSize(document.getWordsSize()+1);
        if(docDictionary.containsKey(document)){
            List<Integer> termFrequency = docDictionary.remove(document);
            termFrequency.add(location);
            docDictionary.put(document,termFrequency);
            if(termFrequency.size() > document.getMostFrequentWord())
                document.setMostFrequentWord(termFrequency.size());
        }
        else{
            List<Integer> newList = new ArrayList<>();
            newList.add(location);
            docDictionary.put(document,newList);
        }
    }

    public int getWordFrequencyAtDoc(Document document) {
        return docDictionary.get(document).size();

    }


    public int getTermIDF() {
        return this.docDictionary.size();
    }
    public int getTermTDF(){
        /*if (termTDF==0){
            for(List<Integer> locations : this.docDictionary.values())
                termTDF+= locations.size();
        }*/
        return termTDF;
    }

    public String getValue(){
        return this.value;
    }

    public Term termsUnion(Term another){
        this.docDictionary.putAll(another.docDictionary);
        return this;
    }

    @Override
    public String toString() {
        StringBuilder term=new StringBuilder(value + ", ");
        for (Map.Entry<Document,List<Integer>> doc : docDictionary.entrySet()){
            term.append("FileName: " + doc.getKey().getFileName() + " ,DocName: " + doc.getKey().getDocName() +  " ,TermLocations: ");
            List<Integer> termLocations = doc.getValue();
            for(int i=0; i<termLocations.size() && i < 100 ; i+=1){
                term.append(termLocations.get(i));
                term.append(" ");

            }
            term.append(". ");
        }
        return term.toString();
    }
    public String encryptTermToStr(){
        StringBuilder term=new StringBuilder(value);
        for (Map.Entry<Document,List<Integer>> doc : docDictionary.entrySet()){
            term.append("#");
            term.append(doc.getKey().getFileName() + "&" + doc.getKey().getDocName() +  "&");
            List<Integer> termLocations = doc.getValue();

            for(int i=0; i<termLocations.size() && i < 100 ; i+=1){
                term.append(termLocations.get(i));
                term.append("^");
            }
        }

        return term.toString();
    }


    //TODO - FIND THE FUCKING BUG.
    public static Term decryptTermFromStr(String str){
        try {
            String[] termData = str.split("#");
            Term term = new Term(termData[0]);
            for (int i = 1; i < termData.length; i += 1) {
                String[] docData = termData[i].split("&");
                Document doc = addDocument(docData[0], docData[1]);
                String[] termIndex = docData[2].split("\\^");
                term.docDictionary.put(doc, new ArrayList<>());
                term.docDictionary.get(doc).add(Integer.parseInt(termIndex[0]));
                for (int j = 1; j < termIndex.length; j += 1)
                    term.docDictionary.get(doc).add(Integer.parseInt(termIndex[j]));
            }
            return term;
        }
        catch(Exception e){

        }
        return null;

    }

    public enum Kind {
        Number{
            @Override
            public String toString() {
                return "Number";
            }
        },
        Percentage{
            @Override
            public String toString() {
                return "Percentage";
            }
        },
        Date{
            @Override
            public String toString() {
                return "Date";
            }
        },
        Dollar{
            @Override
            public String toString() {
                return "Dollar";
            }
        }
    }
    public enum Month {
        January{
            @Override
            public String toString() {
                return "1";
            }
        },
        February{
            @Override
            public String toString() {
                return "2";
            }
        },
        March{
            @Override
            public String toString() {
                return "3";
            }
        },
        April{
            @Override
            public String toString() {
                return "4";
            }
        },
        May{
            @Override
            public String toString() {
                return "5";
            }
        },
        June{
            @Override
            public String toString() {
                return "6";
            }
        },
        July{
            @Override
            public String toString() {
                return "7";
            }
        },
        August{
            @Override
            public String toString() {
                return "8";
            }
        },
        September{
            @Override
            public String toString() {
                return "9";
            }
        },
        October{
            @Override
            public String toString() {
                return "10";
            }
        },
        November{
            @Override
            public String toString() {
                return "11";
            }
        },
        December{
            @Override
            public String toString() {
                return "12";
            }
        };
        private static final Map<String,Month> monthsMap = initMonthsMap();
        private static Map<String,Month> initMonthsMap() {
            return new HashMap<String, Month>(){{
                put("january",January);
                put("jan",January);
                put("february",February);
                put("feb",February);
                put("march",March);
                put("mar",March);
                put("april",April);
                put("apr",April);
                put("may",May);
                put("june",June);
                put("jun",June);
                put("july",July);
                put("jul",July);
                put("august",August);
                put("aug",August);
                put("september",September);
                put("sep",September);
                put("october",October);
                put("oct",October);
                put("november",November);
                put("nov",November);
                put("december",December);
                put("dec",December);
            }};
        }
        public static boolean isMonth(String str){
           return monthsMap.containsKey(str);
        }
        public Month getMonth(String str){
            return monthsMap.get(str);
        }
    }

}

