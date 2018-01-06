package query;

import engine.*;
import gui.EngineMenu.Stemming;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Map.*;

public class QuerySearcher {
    private List<Term> queryTerms;
    private Stemming stemming;
    private Map<String,Integer> extensionTerms;
    private boolean extension;


    public QuerySearcher(String query, boolean extension) {
        queryTerms = new ArrayList<>();
        stemming = Indexer.stemming;
        extensionTerms = new HashMap<>();
        new Parse(query, null, stemming, this).Parse();
        this.extension = extension;
        if(extension)
            queryExtension(query);

    }

    /** as we parse a query the Parse class uses this function **/
    public void addQueryTerm(String str) {
        queryTerms.add(findTerm(str));
    }


    /** this function get term string and return object Term (if exists) from cache or posting file if necessary **/
    public Term findTerm(String str) {
        if (Indexer.Dictionary.containsKey(str)) {
            String pointer = (String) Indexer.Dictionary.get(str)[2];
            if (pointer.charAt(0) == 'C') {
                return Indexer.cacheTerms.get(str);
            } else if (pointer.charAt(0) == 'P') {
                try {
                    RandomAccessFile raf = new RandomAccessFile(new File(Indexer.pathToPosting + "\\Hallelujah" + stemming.toString() + ".txt"), "r");
                    raf.seek(Long.parseLong(pointer.substring(1)));
                    return Term.decryptTermFromStr(raf.readLine());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }
    public List<String> rankQueryDoc(){
        return Ranker.getRelevantDocs(queryTerms,stemming);
    }

    public boolean isExtension() {
        return extension;
    }

    public void queryExtension(String queryTerm){
        try{
            StringBuilder wikiHTTP = new StringBuilder();
            URL oracle = new URL("https://en.wikipedia.org/wiki/" + queryTerm);
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(oracle.openStream()));
            String inputLine;
            while ((inputLine = in.readLine()) != null){
                wikiHTTP.append(inputLine);
            }
            StringBuilder wikiContent = new StringBuilder();
            Matcher p = Pattern.compile("<p>(.+?)</p>").matcher(wikiHTTP);
            while (p.find())
                wikiContent.append(p.group(1));
            in.close();
            new Parse(wikiContent.toString(), null, stemming, this).Parse();
            sortMaxFrequentWord(extensionTerms);

        }
        catch (FileNotFoundException e1){
            throw new RuntimeException("wiki - page not found ");
        }
        catch (IOException e){
            e.printStackTrace();
        }

    }

    public void addExtensionTerm(String term){
        if(extensionTerms.containsKey(term)){
            int tf = extensionTerms.remove(term);
            extensionTerms.put(term,tf+1);
        }
        else{
            extensionTerms.put(term,1);
        }
    }
    private void sortMaxFrequentWord(Map<String,Integer> terms){
        Map sortedMap = sortByValue(terms);
        Map synonymTerms = new HashMap<String,Double>();
        Term queryTerm = queryTerms.get(0);
        if(queryTerm == null)
            throw new RuntimeException("Non result");
        int numOfWords=0;
        List<Document> queryTermDoc = queryTerm.getPopularDocs();
        for(Object str : sortedMap.keySet()){
            if(numOfWords<50 && !((String)str).matches("-?\\d+")){
                Term term = findTerm(str.toString());
                synonymTerms.put(str,numOfCommonDocs(queryTerm,queryTermDoc,term));
                numOfWords+=1;
            }
        }
        Map sorted = sortByValue(synonymTerms);
        int counter =0;
        sorted.remove(queryTerm);
        for(Object synonym : sorted.keySet()){
            if(counter<15){
                System.out.println(synonym.toString());
                counter+=1;
            }
        }
        System.out.println();
    }

    private double numOfCommonDocs(Term queryTerm,List<Document> queryTermDoc, Term other) {
        double counter=0;
        double counter2=0;
        if(other!=null){
            List<Document> otherDocs = other.getPopularDocs();
            for(Document doc : queryTermDoc){
                if(otherDocs.contains(doc))
                    counter+=1;
            }
            counter*= other.getTermIDF();

            for(Document doc : queryTermDoc){
                if(queryTermDoc.contains(doc))
                    counter2+=1;
            }
            counter2*= queryTerm.getTermIDF();
        }
        return Math.pow(counter,0.5)+Math.pow(counter2,0.5);
    }

    /** helper function for sorting a map **/
    private static <K, V> Map<K, V> sortByValue(Map<K, V> map) {
        List<Entry<K, V>> list = new LinkedList<>(map.entrySet());
        Collections.sort(list, (o1, o2) -> ((Comparable<V>) ((Entry<K, V>) (o2)).getValue()).compareTo(((Entry<K, V>) (o1)).getValue()));

        Map<K, V> result = new LinkedHashMap<>();
        for (Iterator<Entry<K, V>> it = list.iterator(); it.hasNext();) {
            Map.Entry<K, V> entry = it.next();
            result.put(entry.getKey(), entry.getValue());
        }

        return result;
    }

}
