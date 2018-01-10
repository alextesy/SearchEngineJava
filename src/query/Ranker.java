package query;

import engine.Document;
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

    private static Map<String,Document> documentData = new HashMap<>();
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
                //double cossine= cosimSimilarity(termInDoc.getKey(),queryTerms);
                double loc = locationSimilarity(termInDoc.getKey(),queryTerms);
                double avgDist=termDistance(termInDoc.getKey(),queryTerms);

                double similarity = bm; //+0.5* loc  /* +  cossine */; //only numerator
                if(!docWeights.containsKey(termInDoc.getKey())) {
                    similarity = extend == true && queryTerms.get(1).equals(term) ? 0.3*similarity : similarity;
                    docWeights.put(termInDoc.getKey(),similarity);
                }
                else{
                    docWeights.put(termInDoc.getKey(),docWeights.get(termInDoc.getKey())+similarity);
                }
            }
            /*
            current rel doc - 113
             */
        }

        return extend==false ? findTopDoc(docWeights,50) : findTopDoc(docWeights,70);
    }


    /**
    initiate map of documents. the map initialized depends stem param
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
                    documentData.put(doc.getDocName(),doc);
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
    private static double cosimSimilarity(Document document, List<Term> queryTerms) {
        try{
            double termWeightInDoc=0;
            document = documentData.get(document.getDocName());
            for (Term term: queryTerms){
                if(term.getDocDictionary().get(document)!=null){
                    int freqInDoc = term.getDocDictionary().get(document).size();
                    double tf = (double)(freqInDoc) /(double) document.getMostFrequentWord();
                    termWeightInDoc += tf * term.getTermIDF();
                }
            }
            termWeightInDoc/= Math.sqrt(Math.pow(documentData.get(document.getDocName()).getWeight(),2)*Math.pow(queryTerms.size(),2));
            return termWeightInDoc;
        }
        //TODO there are docs with weight 0 - read file bug probably
        catch (Exception e ){
            return 0;
        }
    }

    private static double locationSimilarity(Document document, List<Term> queryTerms){
        /*
        tf * (N - index(t))
        ------------------
               N
         */
        double locWeight=0;
        for(Term term : queryTerms){
            double termLoc = 0;
            document = documentData.get(document.getDocName());
            if(term.getDocDictionary().containsKey(document)){
                double tf = term.getDocDictionary().get(document).size();
                double N = document.getDocLength();
                for(Integer index : term.getDocDictionary().get(document)){
                    termLoc += (tf * (N-index))/N;
                }
                termLoc /= term.getDocDictionary().get(document).size();
            }
            locWeight += termLoc;

        }

        return locWeight/queryTerms.size();
    }
    private static double termDistance(Document document,List<Term> queryTerms) {
        document = documentData.get(document.getDocName());
        List<Double> avgIndexList=new ArrayList<>();
        for (Term term : queryTerms) {
            if (term.getDocDictionary().containsKey(document)) {
                List<Integer> indexList=term.getDocDictionary().get(document);
                double avgDist=0;
                for (Integer index:indexList){
                    avgDist+=index;
                }
                avgDist/=indexList.size();
                avgIndexList.add(avgDist);
            }
        }
        double avgDistance=0;
        double numOfPairs=0;
        if(avgIndexList.size()>1) {
            for (Iterator<Double> iterator = avgIndexList.iterator(); iterator.hasNext(); ) {
                double avgIndex1 = iterator.next();
                for (Double avgIndex2 : avgIndexList) {
                    if (avgIndex1 != avgIndex2) {
                        numOfPairs++;
                        avgDistance += Math.abs(avgIndex1 - avgIndex2);
                    }
                }
                iterator.remove();
            }
            avgDistance /= numOfPairs;
        }
        else
            return 0;
        return avgDistance;
    }
    public static double factorial(int number) {
        long result = 1;

        for (int factor = 2; factor <= number; factor++) {
            result *= factor;
        }

        return result;
    }

    private static double bm25Similarity(Document document , List<Term> queryTerms){
        /* k = [1.2,2.0]
        b = 0.5

        IDF(Qi) * f(Qi,D) * (k + 1)
        -------------------------------
        f(Qi,D) + k ( 1 - b +b + |D|   )
                    (            ---   )
                    (           AvgDl  )

         */
        try{
            double k = 1.3;
            double b = 0.75;
            double bm25Sim = 0;
            double avgD = 261.46614428763587;
            document = documentData.get(document.getDocName());
             for(Term term : queryTerms){
                if(term.getDocDictionary().get(document)!=null) {
                    double tfD = term.getDocDictionary().get(document).size();
                    bm25Sim += (termIDFBM25(term) * tfD * (k + 1)) / (
                            tfD + k * (1 - b + b * (document.getDocLength() /
                                    avgD)));
                }
            }
            return bm25Sim;
        }
        catch (Exception e){
            return 0;
        }
    }
    private static double termIDFBM25(Term term){
        /*
             ( N - IDF + 0.5 )
        LOG  (-------------  )
             (  IDF + 0.5    )

         */
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
