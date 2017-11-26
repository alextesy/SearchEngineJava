package Main;

import java.io.Serializable;
import java.util.*;

import static Main.Indexer.currentTermDictionary;

public class Term implements Serializable{
    //public static final Collection<String> termProperties = new ArrayList<>(Arrays.asList("Number", "Percentage", "Date","Dollar","Other"));
    private Map<Document,Double> docDictionary;
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
            Double termFrequency = docDictionary.remove(document);
            //think how to make the Double more valuable
            //termFrequency.add(location);
            docDictionary.put(document,termFrequency);
        }
        else{
            double newList = 0.0;
            //newList.add(location);
            docDictionary.put(document,newList);
        }
    }

    public double getWordFrequencyAtDoc(Document document){
        return docDictionary.get(document);
    }

    @Override
    public String toString() {
        StringBuilder term=new StringBuilder(value+" ");

        for (Map.Entry<Document,Double> e : docDictionary.entrySet()){
            term.append(e.getKey().toString());
            term.append(" ");
            term.append(e.getValue());
            term.append(" ");


        }

        return term.toString();
    }

    /* Serializable Implementation */
    public Term(){};

    public Map<Document, Double> getDocDictionary() {
        return this.docDictionary;
    }

    public void setDocDictionary(Map<Document, Double> docDictionary) {
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

