package query;

import engine.Indexer;
import gui.EngineMenu;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;


public class Test {
    public static void main(String[] args) throws Exception {

        Indexer.pathToCorpus = "d:\\documents\\users\\talbense\\Documents\\corpus";
        Indexer.pathToPosting = "d:\\documents\\users\\talbense\\Documents\\doc";
        Indexer.stemming = EngineMenu.Stemming.False;
        Indexer.Dictionary = Indexer.readDictionary("d:\\documents\\users\\talbense\\Documents\\doc\\dictionary" + Indexer.stemming.toString() + ".txt");
        //Indexer.cacheTerms = Indexer.readCache("C:\Users\אלי\Desktop\doc\\cache" + Indexer.stemming.toString() + "txt");
        //Ranker.pw = new PrintWriter(new FileWriter("d:\\documents\\users\\talbense\\Documents\\doc\\experiment2.txt"));

        // while (true) {
        Ranker.alpha = 1.4;//(new Random().nextInt(150)+50) /  (double)100;
        Ranker.beta =  0.85;//(new Random().nextInt(50)+50) /   (double)100;
        Ranker.gamma = 0.98;// new Random().nextInt(100) /  (double)100;

        QuerySearcher.addQueriesResult("d:\\documents\\users\\talbense\\Documents\\doc\\queries.txt");
        PrintWriter pww = new PrintWriter(new FileWriter("d:\\documents\\users\\talbense\\Documents\\doc\\queriesResult.txt"));
        for (Map.Entry<Integer, List<String>> queryRes : QuerySearcher.queriesResult.entrySet()) {
            List<String> sorted = queryRes.getValue().parallelStream().sorted(String::compareTo).collect(Collectors.toCollection(ArrayList::new));
            for (String document : sorted) {
                pww.println(queryRes.getKey() + " 0 " + document + " 1 42.38 mt");
            }
        }
        pww.close();
        //Ranker.experimentsFunc();
        pww.flush();

    //}
    }




}
