package query;

import engine.*;
import gui.EngineMenu.Stemming;
import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Map.*;
import java.util.stream.Collectors;

public class QuerySearcher {
    private List<Term> queryTerms;
    private Stemming stemming;
    private Map<String, Integer> extensionTerms;
    private boolean extension;

    public static Map<Integer,List<String>> queriesResult = null;


    public QuerySearcher(String query, boolean extension) {
        queryTerms = new ArrayList<>();
        stemming = Indexer.stemming;
        extensionTerms = new HashMap<>();
        new Parse(query, null, stemming, this, null).Parse();
        this.extension = extension;
        if (extension) {
            List<String> synonymTerms = queryExtension(query);
            queryTerms.add(findTerm(synonymTerms.get(1)));
        }
    }

    /**
     * as we parse a query the Parse class uses this function
     **/
    public void addQueryTerm(String str) {
        Term term = findTerm(str);
        if(term!=null)
            queryTerms.add(term);
    }


    /**
     * this function get term string and return object Term (if exists) from cache or posting file if necessary
     **/
    public Term findTerm(String str) {
        if (Indexer.Dictionary.containsKey(str)) {
            String pointer = (String) Indexer.Dictionary.get(str)[2];

            if(pointer.startsWith("C") && extension){
                return Indexer.cacheTerms.get(str);
            }
            else{

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

    public List<String> rankQueryDoc() {
        List<String> strings =  Ranker.getRelevantDocs(queryTerms, stemming, extension);
        return strings;
    }

    public boolean isExtension() {
        return extension;
    }


    /**
     * used to extend query, get 1 word term, find its value at wiki, parse the content of the page
     * and update the the extension class field by the most relevant synonym term
     **/
    public List<String> queryExtension(String queryTerm) {
        try {
            StringBuilder wikiHTTP = new StringBuilder();
            URL oracle = new URL("https://en.wikipedia.org/wiki/" + queryTerm);
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(oracle.openStream()));
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                wikiHTTP.append(inputLine);
            }
            StringBuilder wikiContent = new StringBuilder();
            Matcher p = Pattern.compile("<p>(.+?)</p>").matcher(wikiHTTP);
            while (p.find())
                wikiContent.append(p.group(1));
            in.close();
            new Parse(wikiContent.toString(), null, stemming, this, null).Parse();
            List<String> synonymTerms = sortMaxFrequentWord(extensionTerms);
            return synonymTerms;
        } catch (FileNotFoundException e1) {
            throw new RuntimeException("wiki - page not found ");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;

    }

    /**
     * if we chose to extend the term, the Parse class using this function for add relevant terms to our extension class field
     **/
    public void addExtensionTerm(String term) {
        if (extensionTerms.containsKey(term)) {
            int tf = extensionTerms.remove(term);
            extensionTerms.put(term, tf + 1);
        } else {
            extensionTerms.put(term, 1);
        }
    }

    /**
     * returns list of 50 resemble word to the extended term
     **/
    private List<String> sortMaxFrequentWord(Map<String, Integer> terms) {
        Map sortedMap = sortByValue(terms);
        Map synonymTerms = new HashMap<String, Double>();
        Term queryTerm = queryTerms.get(0);
        if (queryTerm == null)
            throw new RuntimeException("Non result");
        int numOfWords = 0;
        List<Document> queryTermDoc = queryTerm.getPopularDocs();
        for (Object str : sortedMap.keySet()) {
            if (numOfWords < 50 && !((String) str).matches("-?\\d+")) {
                Term term = findTerm(str.toString());
                synonymTerms.put(str, numOfCommonDocs(queryTerm, queryTermDoc, term));
                numOfWords += 1;
            }
        }
        Map sortedSynonymTerms = sortByValue(synonymTerms);
        sortedSynonymTerms.remove(queryTerm);
        List<String> ret = new ArrayList<>(sortedSynonymTerms.keySet());
        return ret;
    }

    /**
     * get the term we wish to extend and list of his documents instance, and another term we
     * suspect resemble to him, and calculate their similarity
     **/
    private double numOfCommonDocs(Term queryTerm, List<Document> queryTermDoc, Term other) {
        double counter = 0;
        double counter2 = 0;
        if (other != null) {
            List<Document> otherDocs = other.getPopularDocs();
            for (Document doc : queryTermDoc) {
                if (otherDocs.contains(doc))
                    counter += 1;
            }
            counter *= other.getTermIDF();

            for (Document doc : queryTermDoc) {
                if (queryTermDoc.contains(doc))
                    counter2 += 1;
            }
            counter2 *= queryTerm.getTermIDF();
        }
        return Math.pow(counter, 0.5) + Math.pow(counter2, 0.5);
    }

    /**
     * helper function for sorting a map
     **/
    private static <K, V> Map<K, V> sortByValue(Map<K, V> map) {
        List<Entry<K, V>> list = new LinkedList<>(map.entrySet());
        Collections.sort(list, (o1, o2) -> ((Comparable<V>) ((Entry<K, V>) (o2)).getValue()).compareTo(((Entry<K, V>) (o1)).getValue()));

        Map<K, V> result = new LinkedHashMap<>();
        for (Iterator<Entry<K, V>> it = list.iterator(); it.hasNext(); ) {
            Map.Entry<K, V> entry = it.next();
            result.put(entry.getKey(), entry.getValue());
        }

        return result;
    }
    private static <K, V> Map<K, V> sortByKey(Map<K, V> map) {
        List<Entry<K, V>> list = new LinkedList<>(map.entrySet());
        Collections.sort(list, (o1, o2) -> ((Comparable<K>) ((Entry<K, V>) (o1)).getKey()).compareTo(((Entry<K, V>) (o2)).getKey()));

        Map<K, V> result = new LinkedHashMap<>();
        for (Iterator<Entry<K, V>> it = list.iterator(); it.hasNext(); ) {
            Map.Entry<K, V> entry = it.next();
            result.put(entry.getKey(), entry.getValue());
        }

        return result;
    }

    public static Map<Integer, String> readQueriesDoc(String path) {
        try {
            Map<Integer, String> queriesDetails = new HashMap<>();
            FileReader fr = new FileReader(new File(path));
            BufferedReader br = new BufferedReader(fr);

            String sCurrentLine;
            int qID = 0;
            while ((sCurrentLine = br.readLine()) != null) {
                if (sCurrentLine.startsWith("<num> Number:"))
                    qID = Integer.parseInt(sCurrentLine.substring("<num> Number:".length()).replace(" ", ""));
                else if (sCurrentLine.startsWith("<title>"))
                    queriesDetails.put(qID, sCurrentLine.substring("<title>".length()));
            }
            fr.close();
            return queriesDetails;

        } catch (IOException e) {

            e.printStackTrace();
        }
        return null;
    }

    public static void addQueriesResult(String path) {
        try {

            if (queriesResult == null) {
                queriesResult = new HashMap<>();
            } else
                queriesResult.clear();

            Map<Integer, String> queriesDetails = readQueriesDoc(path);
            for (Map.Entry<Integer, String> query : queriesDetails.entrySet()) {
                List<String> docs = new QuerySearcher(query.getValue(), false).rankQueryDoc();
                for (String doc : docs) {
                    List<String> queryResult;
                    if (queriesResult.containsKey(query.getKey()))
                        queryResult = queriesResult.remove(query.getKey());
                    else
                        queryResult = new ArrayList();

                    queryResult.add(doc);
                    queriesResult.put(query.getKey(), queryResult);

                }
            }
            queriesResult = sortByKey(queriesResult);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
