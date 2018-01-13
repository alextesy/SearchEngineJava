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
    public static double gamma;
    public static double delta;


    private Ranker(){
        throw new RuntimeException("class 'Ranker' not for initializing");
    }


    public static List<String> getRelevantDocs(List<Term> queryTerms, Stemming stemming, boolean extend) {
        initDocumentDataMap(stemming);
        List <Document> potentialRelDocs = new ArrayList();

        Map<Document,Double> bm25Weight=new HashMap<>();



        for(Term term : queryTerms)
            potentialRelDocs.addAll(new ArrayList(term.getDocDictionary().keySet()));
        for (Document potentialDoc : potentialRelDocs){
            Document document = documentData.get(potentialDoc.getDocName());
            double bm25 = 0.114*Math.exp(0.1821*bm25Similarity(document,queryTerms)) ;
            double cossin = cosinSimilarity(document,queryTerms) ;
            //double location = locationSimilarity(document,queryTerms);
            //double distance = termsDistance(document,queryTerms);

            bm25Weight.put(document,alpha*bm25 +beta*cossin);


        }

        return extend==false ? findTopDoc(bm25Weight,50) : findTopDoc(bm25Weight,70);
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
            double termQueryWeight=0;
            for (Term term: queryTerms){
                double bm25IDF = termIDFBM25(term);
                termQueryWeight += Math.pow(bm25IDF,2);
                if(term.getDocDictionary().get(document)!=null){
                    double tf = (double)(term.getDocDictionary().get(document).size()) /(double) document.getMostFrequentWord();
                    termWeightInDoc += tf * term.getTermIDF() /*tf idf */ * bm25IDF /* term query weight */;
                }
            }

            termWeightInDoc/= Math.sqrt(document.getWeight()* termQueryWeight);
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
        for (Term term : queryTerms){
            if(term.getDocDictionary().containsKey(document)){
                if((currentIndex = term.getDocDictionary().get(document).get(0)) < firstTermInstanceIndex){
                    firstTermInstanceIndex = currentIndex;
                }
            }

        }

        return /*termsDistance(document,queryTerms) * */((document.getDocLength() - firstTermInstanceIndex)
                                                    / (double)document.getDocLength());
    }
    private static double termsDistance(Document document, List<Term> queryTerms) {
        List<Integer> locations = new ArrayList<>();
        for(Term term : queryTerms){
            if(term.getDocDictionary().containsKey(document)){
                locations.addAll(new ArrayList<>(term.getDocDictionary().get(document)));
            }
        }
        locations = locations.parallelStream().sorted(Integer::compareTo).collect(Collectors.toCollection(ArrayList::new));
        double counter=0;
        for(int i=0; i<locations.size()-1; i+=1){
            if(locations.get(i+1) - locations.get(i) == 1)
                counter+=1;
        }
        return counter;
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
            double b = 0.5;
            double bm25Sim = 0;
            double avgD = 261.46614428763587;
            for(Term term : queryTerms){
                if(term.getDocDictionary().get(document)!=null) {
                    double fD = term.getDocDictionary().get(document).size();
                    bm25Sim += (termIDFBM25(term) * fD * (k + 1)) / (
                            fD + k * (1 - b + b * (document.getDocLength() /
                                                     avgD))) ;
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
        return Math.log(base)/Math.log(2);

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
            Process p = runtime.exec("cmd /c C:\\Users\\אלי\\Desktop\\doc\\treceval.exe C:\\Users\\אלי\\Desktop\\doc\\qrels.txt C:\\Users\\אלי\\Desktop\\doc\\queriesResult.txt");
            BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;
            String rank = "";
            while ((line = input.readLine()) != null)
                if (line.contains("Rel_ret:")) {
                    rank = line.replace(" ", "");
                    rank = rank.substring("Rel_ret:".length());
                    break;
                }
            pw.println(rank+ " " + alpha + " " + beta + " " + gamma + " " + delta);
            pw.flush();




        } catch (IOException e) {
            e.printStackTrace();
        }
    }



}
