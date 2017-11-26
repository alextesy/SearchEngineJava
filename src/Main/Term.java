package Main;

import java.io.Serializable;
import java.util.*;

import static Main.Indexer.currentTermDictionary;

public class Term implements Serializable{
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
        if(docDictionary.containsKey(document)){
            List<Integer> termFrequency = docDictionary.remove(document);
            termFrequency.add(location);
            docDictionary.put(document,termFrequency);
        }
        else{
            List<Integer> newList = new ArrayList<>();
            newList.add(location);
            docDictionary.put(document,newList);
        }
    }

    public int getWordFrequencyAtDoc(Document document){
        return docDictionary.get(document).size();
    }



    /* Serializable Implementation */
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
}

