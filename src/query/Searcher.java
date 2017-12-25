package query;

import engine.Indexer;
import engine.Parse;
import engine.Term;

import java.io.File;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;


public class Searcher {
    private String query;
    private List<Term> queryTerms;
    private boolean stemming;
    private Parse parser;
    private String stemString;

    public Searcher(String query) {
        this.query = query;
        queryTerms = new ArrayList<>();
        stemming = Indexer.stemming;
        stemString = stemming == true ? "Stem" : "";
        parser = new Parse(query, null, stemming, this);
        parser.ParseFile();
    }



    public void addTerm(String term) {
        if (Indexer.Dictionary.containsKey(term)) {
            String pointer = (String) Indexer.Dictionary.get(term)[2];
            if (pointer.charAt(0) == 'C') {
                queryTerms.add(Indexer.cacheTerms.get(term));
            } else if (pointer.charAt(0) == 'P') {
                try {
                    RandomAccessFile raf = new RandomAccessFile(new File(Indexer.pathToPosting + "\\Hallelujah" + stemString + ".txt"), "r");
                    raf.seek(Long.getLong(pointer.substring(1)));
                    queryTerms.add(Term.decryptTermFromStr(raf.readLine()));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }


    }
}
