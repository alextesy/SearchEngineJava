package Main;

import java.io.Serializable;
import java.util.*;
import java.util.regex.Pattern;

import static Main.Document.addDocument;
import static Main.Indexer.currentTermDictionary;

public class Term{
    //public static final Collection<String> termProperties = new ArrayList<>(Arrays.asList("Number", "Percentage", "Date","Dollar","Other"));
    private Map<Document,List<Integer>> docDictionary;
    private String value;
    private String kind;

    private Term(String value, String kind){
        this.docDictionary = new HashMap<>();
        this.value = value;
        this.kind = kind;
    }

    public static void addTerm(String value,String kind,Document document,int location){
        if(currentTermDictionary.containsKey(value)){
            Term term = currentTermDictionary.get(value);
            term.updatedDoc(document,location);
        }
        else{
            Term newTerm = new Term(value,kind) ;
            newTerm.updatedDoc(document,location);
            currentTermDictionary.put(value,newTerm);
        }
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

    @Override
    public String toString() {
        StringBuilder term=new StringBuilder(value + ", ");
        for (Map.Entry<Document,List<Integer>> doc : docDictionary.entrySet()){
            //term.append(" ");
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

    public static Term decryptTermFromStr(String str){
        String[] termData = str.split("#");
        Term term = new Term(termData[0],"");
        for(int i=1 ; i<termData.length; i+=1){
            String[] docData = termData[i].split("&");
            Document doc = addDocument(docData[0],docData[1]);
            String[] termIndex = docData[2].split("\\^");
            term.docDictionary.put(doc,new ArrayList<>());
            term.docDictionary.get(doc).add(Integer.parseInt(termIndex[0]));
            for(int j=1; j < termIndex.length; j+=1)
                term.docDictionary.get(doc).add(Integer.parseInt(termIndex[j]));
        }
        return null;
    }




    /* Serializable Implementation
    public Term(){};

    public Map<Document, List<Integer>> getDocDictionary() {
        return this.docDictionary;
    }

    public void setDocDictionary(Map<Document, List<Integer>> docDictionary) {
        this.docDictionary = docDictionary;
    }

    public String getValue() {
        return this.value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getKind() {
        return this.kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    */
}

