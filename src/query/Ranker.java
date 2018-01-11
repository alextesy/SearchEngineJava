package query;

import engine.Document;
import engine.Term;
import gui.EngineMenu.Stemming;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;


/**
 static class represent ranker functionality,
 class usage through getRelevantDocs function, get parsed query terms and stemming checkbox value
 return list of most relevant docs by file and doc name for specific query.
 **/

public class Ranker {

    private static Map<String,Document> documentData = new HashMap<>();
    private static Stemming lastStem = null;

    public static PrintWriter pw;
    public static double alpha;
    public static double beta;
    public static double lambda;
    public static double epsilon;


    private Ranker(){
        throw new RuntimeException("class 'Ranker' not for initializing");
    }

    public static List<String> getRelevantDocs(List<Term> queryTerms, Stemming stemming, boolean extend) {
        initDocumentDataMap(stemming);
        Map<Document,Double> docsWeight = new HashMap<>();
        for (Term term: queryTerms){
            Map<Document,List<Integer>> docDictionary = term.getDocDictionary();
            for (Map.Entry<Document,List<Integer>> termInDoc: docDictionary.entrySet()) {
                double bm25 = Math.sqrt(bm25Similarity(termInDoc.getKey(),queryTerms));
                double cossinSimilarity = cosinSimilarity(termInDoc.getKey(),queryTerms);
                double termDistance = termsDistance(termInDoc.getKey(),queryTerms);
                double locationSimilarity = locationSimilarity(termInDoc.getKey(),queryTerms);
                double similarity = alpha * bm25  + beta * cossinSimilarity  +  lambda *termDistance   + epsilon * locationSimilarity ;

                if(!docsWeight.containsKey(termInDoc.getKey())) {
                    similarity = extend == true && queryTerms.get(1).equals(term) ? 0.3 * similarity : similarity;
                    docsWeight.put(termInDoc.getKey(),similarity);
                }
                else{
                    docsWeight.put(termInDoc.getKey(),docsWeight.get(termInDoc.getKey())+similarity);

                }
            }
            /*
            Cosin relevant docs - 124
            BM25 relevant docs - 109
            locations relevant docs - 72
            distance relevant docs - 67
            so far - 134
             */
        }

        return extend==false ? findTopDoc(docsWeight,50) : findTopDoc(docsWeight,70);
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
    private static double cosinSimilarity(Document document, List<Term> queryTerms) {
        try{
            double termWeightInDoc=0;
            document = documentData.get(document.getDocName());
            double termQueryWeight=0;
            for (Term term: queryTerms){
                termQueryWeight += Math.pow(term.getTermIDF(),3);
                if(term.getDocDictionary().get(document)!=null){
                    int freqInDoc = term.getDocDictionary().get(document).size();
                    double tf = (double)(freqInDoc) /(double) document.getMostFrequentWord();
                    termWeightInDoc += tf * Math.pow(term.getTermIDF(),4);
                }
            }

            termWeightInDoc/= Math.sqrt(documentData.get(document.getDocName()).getWeight()* termQueryWeight);
            return termWeightInDoc;
        }
        catch (Exception e ){
            return 0;
        }
    }

    private static double locationSimilarity(Document document, List<Term> queryTerms){
        /*
        N - index(t)
        -----------
            N
        */
        double firstTermInstanceIndex = Integer.MAX_VALUE;
        double currentIndex;
        document = documentData.get(document.getDocName());
        for (Term term : queryTerms){
            if(term.getDocDictionary().containsKey(document)){
                if((currentIndex = term.getDocDictionary().get(document).get(0)) < firstTermInstanceIndex){
                    firstTermInstanceIndex = currentIndex;
                }
            }

        }
        if( (document.getDocLength() - firstTermInstanceIndex)<0)
            System.out.println();
        return (document.getDocLength() - firstTermInstanceIndex) / (double)document.getDocLength();
    }
    private static double termsDistance(Document document, List<Term> queryTerms) {
        List<Integer> locations = new ArrayList<>();
        double termsIDF =0;
        for(Term term : queryTerms){
            termsIDF += term.getTermIDF();
            if(term.getDocDictionary().containsKey(document)){
                document = documentData.get(document.getDocName());
                locations.addAll(new ArrayList<>(term.getDocDictionary().get(document)));
            }
        }
        locations = locations.parallelStream().sorted(Integer::compareTo).collect(Collectors.toCollection(ArrayList::new));
        double counter=0;
        for(int i=0; i<locations.size()-1; i+=1){
            if(locations.get(i+1) - locations.get(i) == 1)
                counter+=1;
        }
        return counter*termsIDF/queryTerms.size();
    }


    private static double bm25Similarity(Document document , List<Term> queryTerms){
        /* k = [1.2,2.0]
        b = 0.5

        IDF(Qi) * f(Qi,D) * (k + 1)
        -------------------------------
                    ( 1 - b +b + |D|   )
        f(Qi,D) + k (            ---   )
                    (           AvgDl  )

         */
        try{
            double k = 1.2;
            double b = 0;
            double bm25Sim = 0;
            double avgD = 261.46614428763587;
            document = documentData.get(document.getDocName());
            for(Term term : queryTerms){
                if(term.getDocDictionary().get(document)!=null) {
                    double fD = term.getDocDictionary().get(document).size();
                    bm25Sim += (termIDFBM25(term) * fD * (k + 1)) / (
                            fD + k * (1 - b + b * (document.getDocLength() /
                                    avgD))) + 1;
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

    public static void experimentsFunc(){
        try {
            Runtime runtime = Runtime.getRuntime();
            Process p = runtime.exec("cmd /c C:\\Users\\אלי\\Desktop\\doc\\treceval.exe C:\\Users\\אלי\\Desktop\\doc\\qries.txt C:\\Users\\אלי\\Desktop\\doc\\queriesResult.txt");
            BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;
            String rank = "";
            while ((line = input.readLine()) != null)
                if (line.contains("Rel_ret:")) {
                    rank = line.replace(" ", "");
                    rank = rank.substring("Rel_ret:".length());
                    break;
                }
            if(!rank.equals("")){
                pw.println(rank+ " " + alpha + " " + beta/* + " " + lambda + " " + epsilon*/);
                pw.flush();
            }



        } catch (IOException e) {
            e.printStackTrace();
        }
    }



}
