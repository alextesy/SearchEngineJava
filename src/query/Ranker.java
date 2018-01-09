package query;

import engine.Document;
import engine.Indexer;
import engine.Stemmer;
import engine.Term;
import gui.EngineMenu.Stemming;

import javax.print.Doc;
import java.io.*;
import java.util.*;


/**
static class represent ranker functionality,
class usage through getRelevantDocs function, get parsed query terms and stemming checkbox value
return list of most relevant docs by file and doc name for specific query.
 **/

public class Ranker {

    private static Map<String,Double> documentData = new HashMap<>();
    private static Stemming lastStem = null;


    private Ranker(){
        throw new RuntimeException("class 'Ranker' not for initializing");
    }

    public static List<String> getRelevantDocs(List<Term> queryTerms, Stemming stemming, boolean extend) {
        initDocumentDataMap(stemming);
        Map<Document,Double> docWeights=new HashMap<>();
        for (Term term: queryTerms){
            Map<Document,List<Integer>> docDictionary = term.getDocDictionary();
            for (Map.Entry<Document,List<Integer>> termInDoc: docDictionary.entrySet()) {
                double bm=bm25Similarity(termInDoc.getKey(),queryTerms);
                double cossine=cosinSimilarity(termInDoc.getKey(),queryTerms);
                double similarity = 0.8 * bm + 0.2 * cossine; //only numerator
                if(similarity!=0)
                    System.out.println("tal");
                if(!docWeights.containsKey(termInDoc.getKey())) {
                    similarity = extend == true && queryTerms.get(1).equals(term) ? 0.3*similarity : similarity;
                    docWeights.put(termInDoc.getKey(),similarity);
                }
                else{
                    docWeights.put(termInDoc.getKey(),docWeights.get(termInDoc.getKey())+similarity);
                }
            }
        }

        return extend==false ? findTopDoc(docWeights,50) : findTopDoc(docWeights,70);
    }


    /**
    initiate map of documents and their weight. the map initialized depends stem param
    **/
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




    private static List<String> findTopDoc(Map<Document,Double> docVectors,int topNumb){
        PriorityQueue<Map.Entry<Document,Double>> pq = new PriorityQueue<>((o1, o2) ->Double.compare(o2.getValue(), o1.getValue()));
        for (Map.Entry<Document,Double> d:docVectors.entrySet()){
            pq.add(d);
        }
        List<String> rankedDocs=new ArrayList<>();
        int size=pq.size();
        for(int i=0;i<topNumb && i<size;i++){
            rankedDocs.add(pq.peek().getKey().getDocName());
            pq.poll();
        }
        return rankedDocs;
    }



    /**
     cosine implementation - term query weight - 1kg
      **/
    private static double cosinSimilarity(Document document,List<Term> queryTerms) {
        try{
            double termWeightInDoc=0;
            for (Term term: queryTerms){
                int freqInDoc = term.getDocDictionary().get(document).size();
                double tf = freqInDoc / document.getMostFrequentWord();
                termWeightInDoc += tf * term.getTermIDF();
            }
            termWeightInDoc/= Math.pow(documentData.get(document.getFileName()+document.getDocName()),2)*Math.pow(queryTerms.size(),2);
            return Math.sqrt(termWeightInDoc);
        }
        //TODO there are docs with weight 0 - read file bug probably
        catch (Exception e ){
            return 0;
        }
    }

    private static double locationSimilarity(){
        return 0;
    }
    private static double bm25Similarity(Document document , List<Term> queryTerms){
        /* k = [1.2,2.0]
        b = 0.5
         */
        try{
            double k = 2;
            double b = 0.5;
            double N = 468370;
            double bm25Sim = 0;
            double avgD = 261.46614428763587;
             for(Term term : queryTerms){
                double tfD = term.getDocDictionary().get(document).size();
                bm25Sim += termIDFBM25(term)*(tfD * (k+1)/(
                        tfD + k*(1-b+b*(document.getDocLength()/
                                        avgD))));
            }
            return bm25Sim;
        }
        catch (Exception e){
            return 0;
        }
    }
    private static double termIDFBM25(Term term){
        double N = 468370;
        double base = (N - term.getDocDictionary().size() + 0.5)/
                (term.getDocDictionary().size() + 0.5);
        return (Math.log(base) / Math.log(2));

    }

    public static double getAverageDocLength(String path){
        try {
            FileInputStream fs = new FileInputStream(path);
            BufferedReader br = new BufferedReader(new InputStreamReader(fs));
            String strLine;
            int counter = 0;
            double averageDocLength = 0;
            while ((strLine = br.readLine()) != null) {
                averageDocLength += Document.decryptDocFromStr(strLine).getDocLength();
                counter += 1;
            }
            return averageDocLength/counter;
        }
        catch (IOException e){
            e.printStackTrace();
        }

        return 0;
    } /* 261.46614428763587 */




}
