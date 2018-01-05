package query;

import engine.Document;
import engine.Term;
import gui.EngineMenu.Stemming;

import java.io.*;
import java.util.*;


/*
static class represent ranker functionality,
class usage through getRelevantDocs function, get parsed query terms and stemming checkbox value
return list of most relevant docs by file and doc name for specific query.
 */

public class Ranker {

    private static Map<String,Double> documentData = new HashMap<>();
    private static Stemming lastStem = null;


    private Ranker(){
        throw new RuntimeException("class 'Ranker' not for initializing");
    }

    public static List<String> getRelevantDocs(List<Term> queryTerms, Stemming stemming) {
        initDocumentDataMap(stemming);
        Map<Document,Double> docWeights=new HashMap<>();
        for (Term term: queryTerms){
            Map<Document,List<Integer>> docDictionary = term.getDocDictionary();
            for (Map.Entry<Document,List<Integer>> termInDoc: docDictionary.entrySet()) {
                if(!docWeights.containsKey(termInDoc.getKey())) {
                    double similarity = getSimilarity(termInDoc.getKey(),queryTerms); //only numerator
                    //More attributes
                    docWeights.put(termInDoc.getKey(),similarity);
                }
            }
        }

        return findTop50(docWeights);
    }


    /*
    initiate map of documents and their weight. the map initialized depends stem param
     */
    private static Map<String,Document> initDocumentDataMap(Stemming stemming){
        if(lastStem==null || lastStem.isStem() != stemming.isStem()){
            lastStem = stemming;
            try{
                documentData.clear();
                InputStream in = Ranker.class.getResourceAsStream("..//engine//docs//documentData" +stemming.toString() + ".txt");
                BufferedReader br = new BufferedReader(new InputStreamReader(in));
                String line = br.readLine();
                while (line != null) {
                    Document doc = Document.decryptDocFromStr(line);
                    documentData.put(doc.getFileName()+doc.getDocName(),doc.weight);
                    line = br.readLine();

                }
            }
            catch (IOException io){
                io.printStackTrace();
                lastStem =null;
            }
        }

        return null;
        }




    private static List<String> findTop50(Map<Document,Double> docVectors){
        PriorityQueue<Map.Entry<Document,Double>> pq = new PriorityQueue<>((o1, o2) ->Double.compare(o2.getValue(), o1.getValue()));
        for (Map.Entry<Document,Double> d:docVectors.entrySet()){
            pq.add(d);
        }
        List<String> rankedDocs=new ArrayList<>();
        int size=pq.size();
        for(int i=0;i<50&&i<size;i++){
            rankedDocs.add(pq.peek().getKey().getFileName()+" "+pq.peek().getKey().getDocName());
            pq.poll();
        }
        return rankedDocs;
    }



    /*
     cosine implementation - term query weight - 1kg
      */
    private static double getSimilarity(Document document,List<Term> queryTerms) {
        try{
            double termWeightInDoc=0;
            for (Term term: queryTerms){
                int freqInDoc = term.getDocDictionary().get(document).size();
                double tf = freqInDoc / document.getMostFrequentWord();
                termWeightInDoc += tf * term.getTermIDF();
            }
            termWeightInDoc/= Math.pow(documentData.get(document.getFileName()+document.getDocName()),2)*queryTerms.size();
            return Math.sqrt(termWeightInDoc);
        }
        //TODO there are docs with weight 0 - read file bug probably
        catch (Exception e ){
            return 0;
        }
    }




}
