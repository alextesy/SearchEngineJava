package engine;




import java.util.*;

import static engine.Document.addDocument;
import static engine.Indexer.currentTermDictionary;
/**
 *Object of Term that holds its value, TDF and Map of all the documents that the term appears in and its location in it.
 */
public class Term{

    private static final int numOfDocs = 468370;
    private Map<Document,List<Integer>> docDictionary;
    private String value;
    private long termTDF;
    /* private Kind kind; */

    private Term(String value /*,Kind kind*/){
        this.docDictionary = new HashMap<>();
        this.value = value;
        this.termTDF = 0 ;
        /*this.kind = kind;*/
    }

    public static void addTerm(String value/*,Kind kind*/,Document document,int location){
        /**
         * Creates Term if its not already exists and add it to currentTermDictionary
         */

        Term term;
        if (currentTermDictionary.containsKey(value)) {
            term = currentTermDictionary.get(value);
            term.updatedDoc(document, location);
        } else {
            term = new Term(value/*,kind*/);
            term.updatedDoc(document, location);
            currentTermDictionary.put(value, term);
        }
        term.termTDF += 1;
    }

    public Map<Document, List<Integer>> getDocDictionary() {
        return docDictionary;
    }

    private void updatedDoc(Document document, int location){
        /**
         * Creates Document if its not already exists and add it to docDictionary
         */

        try{document.updateDocWeight(this.value,Indexer.termsIDF.get(this.value));
        }catch (NullPointerException e){
            /* non frequent term - less then 4 instance */
        }
        document.setDocLength(document.getDocLength()+1);
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


    public int getTermDF() {
        return this.docDictionary.size();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Term term = (Term) o;

        return value.equals(term.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    public double getTermIDF(){

        return Math.log(numOfDocs/getTermDF()) / Math.log(2);
    }
    public long getTermTDF(){
        return termTDF;
    }

    public String getValue(){
        return this.value;
    }

    public Term termsUnion(Term another){
        /**
         * Unites the data of two terms with the same value - used in merging
         */
        this.docDictionary.putAll(another.docDictionary);
        this.termTDF+=another.termTDF;
        return this;
    }
    public Term termsSub(List<Document> popularDocuments){
        /**
         * divides the data of term - used in creating Cache
         */
        Term term = new Term(this.value);
        term.termTDF = this.termTDF;
        for(Document doc : popularDocuments)
            term.docDictionary.put(doc,docDictionary.get(doc));
        return term;

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
        /**
         * @return String that represents the Term
         */
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
        /**
         * @return Term that decrypts from the given String
         */
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


    public List<Document> getPopularDocs(){
        /**
         * @return 25% of the most popular docs of the term
         */
        List<Document> popularDocs = new ArrayList<>();
        PriorityQueue<Document> pq = new PriorityQueue<Document>((Comparator<Document>) (o1, o2) -> Integer.compare(docDictionary.get(o2).size(),docDictionary.get(o1).size()));
        pq.addAll(docDictionary.keySet());
        if(pq.size()<4){
            for (int i=0; i<pq.size() ; i+=1)
                popularDocs.add(pq.poll());
        }
        else{
            for (int i=0; i<pq.size()/4 ; i+=1)
                popularDocs.add(pq.poll());
        }
        return popularDocs;
    }

    /* create file - IDF value for each term in corpus */
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
    public enum Number{
        Zero{
            @Override
            public String toString(){return "0";}
            },

        One{
            @Override
            public String toString() {
                return "1";
            }

        },
        Two{
            @Override
            public String toString() {
                return "2";
            }
        },
        Three{
            @Override
            public String toString() {
                return "3";
            }
        },
        Four{
            @Override
            public String toString() {
                return "4";
            }

        },
        Five{
            @Override
            public String toString() {
                return "5";
            }
        },
        Six{
            @Override
            public String toString() {
                return "6";
            }
        },
        Seven{
            @Override
            public String toString() {
                return "7";
            }
        },
        Eight{
            @Override
            public String toString() {
                return "8";
            }
        },
        Nine{
            @Override
            public String toString() {
                return "9";
            }
        },
        Ten{
            @Override
            public String toString() {
                return "10";
            }
        },
        Eleven{
            @Override
            public String toString(){return "11";}
        } ,
        Twelve{
            @Override
            public String toString(){return "12";}
        }
        ;

        private static final Map<String,Number> numberMap = initNumberMap();
        private static Map<String,Number> initNumberMap() {
            return new HashMap<String, Number>(){{
                put("Zero",Zero);
                put("zero",Zero);
                put("One",One);
                put("one",One);
                put("Two",Two);
                put("two",Two);
                put("Three",Three);
                put("three",Three);
                put("Four",Four);
                put("four",Four);
                put("Five", Five);
                put("five",Five);
                put("Six",Six);
                put("six",Six);
                put("Seven",Seven);
                put("seven",Seven);
                put("Eight",Eight);
                put("eight",Eight);
                put("Nine",Nine);
                put("nine",Nine);
                put("Ten",Ten);
                put("ten",Ten);
                put("Eleven",Eleven);
                put("eleven",Eleven);
                put("Dozen",Twelve);
                put("Twelve",Twelve);
                put("twelve",Twelve);


            }};
        }
        public static boolean isNumber(String str){
            return numberMap.containsKey(str);
        }
        public static Number getNumber(String str){
            return numberMap.get(str);
        }


    }
}

