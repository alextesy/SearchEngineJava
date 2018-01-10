package query;

import engine.Indexer;
import gui.EngineMenu;

public class Test {
    public static void main(String[] args) throws Exception {
        Indexer.pathToCorpus = "C:\\Users\\talbense\\Downloads\\corpus";
        Indexer.pathToPosting = "C:\\Users\\talbense\\Downloads\\doc";
        Indexer.stemming = EngineMenu.Stemming.True;
        Indexer.Dictionary = Indexer.readDictionary("C:\\Users\\talbense\\Downloads\\doc\\dictionaryStem.txt");
        Indexer.cacheTerms = Indexer.readCache("C:\\Users\\talbense\\Downloads\\doc\\cacheStem.txt");
        QuerySearcher.writeQueriesResult("C:\\Users\\talbense\\Downloads\\doc\\queries.txt");


    }
}
