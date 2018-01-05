package query;

import engine.Indexer;
import engine.Parse;
import engine.Term;
import gui.EngineMenu.Stemming;

import java.io.File;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class QuerySearcher {
    private String query;
    private List<Term> queryTerms;
    private Stemming stemming;

    public QuerySearcher(String query) {
        //TODO USED FOR DEBUGGING. SHOULD DELETE AFTERWARD
        try{
            Indexer.Dictionary = new HashMap<>(Indexer.readDictionary("C:\\Users\\talbense\\Downloads\\doc\\dictionaryStem.txt"));
            Indexer.cacheTerms = new HashMap<>(Indexer.readCache("C:\\Users\\talbense\\Downloads\\doc\\cacheStem.txt"));
        }
        catch (Exception e){

        }

        this.query = query;
        queryTerms = new ArrayList<>();
        stemming = Indexer.stemming;
        new Parse(query, null, stemming, this,null).Parse();

    }


    //TODO BUG - Adding illegal terms - for example : stop words such as 'the' (parser class)
    public void addQueryTerm(String term) {
        if (Indexer.Dictionary.containsKey(term)) {
            String pointer = (String) Indexer.Dictionary.get(term)[2];
            if (pointer.charAt(0) == 'C') {
                queryTerms.add(Indexer.cacheTerms.get(term));
            } else if (pointer.charAt(0) == 'P') {
                try {
                    RandomAccessFile raf = new RandomAccessFile(new File(Indexer.pathToPosting + "\\Hallelujah" + stemming.toString() + ".txt"), "r");
                    raf.seek(Long.getLong(pointer.substring(1)));
                    queryTerms.add(Term.decryptTermFromStr(raf.readLine()));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
    public List<String> rankQueryDoc(){
        return Ranker.getRelevantDocs(queryTerms,stemming);
    }

}
