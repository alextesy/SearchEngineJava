package Main;



import java.util.*;

import static Main.Document.addDocument;
import static Main.Indexer.currentTermDictionary;

public class Term{

    private Map<Document,List<Integer>> docDictionary;
    private String value;
    private int termTDF;
    /* private Kind kind; */

    private Term(String value /*,Kind kind*/){
        this.docDictionary = new HashMap<>();
        this.value = value;
        this.termTDF = 0 ;
        /*this.kind = kind*/;
    }

    public static void addTerm(String value/*,Kind kind*/,Document document,int location){
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
        /*
        if (termTDF==0){
            for(List<Integer> locations : this.docDictionary.values())
                termTDF+= locations.size();
        }
        */
        return termTDF;
    }

    public String getValue(){
        return this.value;
    }

    public Term termsUnion(Term another){
        this.docDictionary.putAll(another.docDictionary);
        this.termTDF+=another.termTDF;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder term=new StringBuilder(value + ", TermTDF: " + termTDF);
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
        StringBuilder term=new StringBuilder(value + "#" + termTDF);
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


    public static Term decryptTermFromStr(String str){
        String[] termData = str.split("#");
        Term term = new Term(termData[0]);
        term.termTDF = Integer.parseInt(termData[1]);
        for (int i = 2; i < termData.length; i += 1) {
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
                return "01";
            }
        },
        February{
            @Override
            public String toString() {
                return "02";
            }
        },
        March{
            @Override
            public String toString() {
                return "03";
            }
        },
        April{
            @Override
            public String toString() {
                return "04";
            }
        },
        May{
            @Override
            public String toString() {
                return "05";
            }
        },
        June{
            @Override
            public String toString() {
                return "06";
            }
        },
        July{
            @Override
            public String toString() {
                return "07";
            }
        },
        August{
            @Override
            public String toString() {
                return "08";
            }
        },
        September{
            @Override
            public String toString() {
                return "09";
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
                put("January",January);
                put("JANUARY",January);
                put("February",February);
                put("FEBRUARY",February);
                put("March",March);
                put("MARCH",March);
                put("April",April);
                put("APRIL",April);
                put("May",May);
                put("MAY",May);
                put("June",June);
                put("JUNE",June);
                put("July",July);
                put("JULY",July);
                put("August",August);
                put("AUGUST",August);
                put("September",September);
                put("SEPTEMBER",September);
                put("October",October);
                put("OCTOBER",October);
                put("November",November);
                put("NOVEMBER",November);
                put("December",December);
                put("DECEMBER",December);
            }};
        }
        public static boolean isMonth(String str){
           return monthsMap.containsKey(str);
        }
        public static Month getMonth(String str){
            return monthsMap.get(str);
        }
    }

}

