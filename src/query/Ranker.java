package query;

import engine.Document;
import engine.Indexer;
import engine.Term;

import javax.print.Doc;
import java.util.*;
import java.util.regex.Matcher;

public class Ranker {
    private List<Term> queryTerm;
    private int size;


    public Ranker(List<Term> queryTerm){
        this.queryTerm=queryTerm;
        this.size=queryTerm.size();
    }

    public List<String> rankDocs() {
        Map<Document,Double> docVectors=new HashMap<>();
        List<String> rankList=new ArrayList<>();
        double similirarity=0;
        for (Term term: queryTerm){
            Map<Document,List<Integer>> docDictionary=term.getDocDictionary();
            for (Map.Entry<Document,List<Integer>> d: docDictionary.entrySet()) {
                if(!docVectors.containsKey(d.getKey())) {
                    double similarity = getSimilarity(d.getKey()); //only numerator
                    //More attributes
                    docVectors.put(d.getKey(),similarity);
                }
            }
        }

        return top50(docVectors);
    }

    private List<String> top50(Map<Document,Double> docVectors){
        PriorityQueue<Map.Entry<Document,Double>> pq = new PriorityQueue<>((o1, o2) ->Double.compare(o2.getValue(), o1.getValue()));
        for (Map.Entry<Document,Double> d:docVectors.entrySet()){
            pq.add(d);
        }
        List<String> rankedDocs=new ArrayList<>();
        for(int i=0;i<50;i++){
            rankedDocs.add(pq.poll().getKey().getFileName()+" "+pq.poll().getKey().getDocName());
        }
        return rankedDocs;
    }




    private double getSimilarity(Document document) {
        double counter=0;
        for (Term term:queryTerm){
            int f = term.getDocDictionary().get(document).size();
            double tf = f / document.getDocLength();
            counter+= tf * term.getTermIDF();
        }
        return counter;
    }





}
