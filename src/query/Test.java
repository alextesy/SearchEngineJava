package query;

import engine.Indexer;
import gui.EngineMenu;

import java.io.*;
import java.util.regex.Pattern;

public class Test {
    public static void main(String[] args) throws Exception {

        Indexer.pathToCorpus = "C:\\Users\\אלי\\Desktop\\corpus";
        Indexer.pathToPosting = "C:\\Users\\אלי\\Desktop\\doc";
        Indexer.stemming = EngineMenu.Stemming.True;
        Indexer.Dictionary = Indexer.readDictionary("C:\\Users\\אלי\\Desktop\\doc\\dictionaryStem.txt");
        //Indexer.cacheTerms = Indexer.readCache("C:\Users\אלי\Desktop\doc\\cacheStem.txt");
        // Ranker.pw = new PrintWriter(new FileWriter("C:\\Users\\אלי\\Desktop\\doc\\test2.txt"));
       // while (true)
            QuerySearcher.writeQueriesResult("C:\\Users\\אלי\\Desktop\\doc\\queries.txt");


    }
}
