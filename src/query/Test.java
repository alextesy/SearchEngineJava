package query;

import engine.Indexer;
import gui.EngineMenu;

public class Test {
    public static void main(String[] args) throws Exception {
        Indexer.pathToCorpus = "d:\\documents\\users\\talbense\\Documents\\corpus";
        Indexer.pathToPosting = "d:\\documents\\users\\talbense\\Documents\\doc";
        Indexer.stemming = EngineMenu.Stemming.True;
        Indexer.Dictionary = Indexer.readDictionary("d:\\documents\\users\\talbense\\Documents\\doc\\dictionaryStem.txt");
        //Indexer.cacheTerms = Indexer.readCache("d:\\documents\\users\\talbense\\Documents\\doc\\cacheStem.txt");
        QuerySearcher.writeQueriesResult("d:\\documents\\users\\talbense\\Documents\\doc\\queries.txt");
    }
}
