package Main;

import java.util.*;

public class Term {
    public static final Collection<String> termProperties = new ArrayList<>(Arrays.asList("Number", "Percentage", "Date","Dollar","Other"));
    public static final Map<String,Term> termDictionary = new HashMap<>();
    private Map<Document,List<Integer>> docDictionary;
    private String value;
    private String kind;

    private Term(String value, String kind){
        this.docDictionary = new HashMap<>();
        this.value = value;
        this.kind = kind;
    }

    public static void addTerm(String value,String kind,Document document,int location){
        if(termDictionary.containsKey(value)){
            Term term = termDictionary.get(value);
            term.updatedDoc(document,location);
        }
        else{
            Term newTerm = new Term(value,kind) ;
            newTerm.updatedDoc(document,location);
            termDictionary.put(value,newTerm);
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

}

