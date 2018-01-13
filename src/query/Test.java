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

        Indexer.pathToCorpus = "C:\\Users\\אלי\\Desktop\\corpus";
        Indexer.pathToPosting = "C:\\Users\\אלי\\Desktop\\doc";
        Indexer.stemming = EngineMenu.Stemming.True;
        Indexer.Dictionary = Indexer.readDictionary("C:\\Users\\אלי\\Desktop\\doc\\dictionary" + Indexer.stemming.toString() + ".txt");
        //Indexer.cacheTerms = Indexer.readCache("C:\Users\אלי\Desktop\doc\\cache" + Indexer.stemming.toString() + "txt");
        Ranker.pw = new PrintWriter(new FileWriter("C:\\Users\\אלי\\Desktop\\doc\\experiment2.txt"));

        while (true) {
            Ranker.alpha = new Random().nextInt(200)/ (double) 100;
            Ranker.beta = new Random().nextInt(200) / (double) 100;
            Ranker.gamma = new Random().nextInt(100) / (double) 100;
            Ranker.delta = new Random().nextInt(100) / (double) 100;

            QuerySearcher.addQueriesResult("C:\\Users\\אלי\\Desktop\\doc\\queries.txt");
            PrintWriter pww = new PrintWriter(new FileWriter("C:\\Users\\אלי\\Desktop\\doc\\queriesResult.txt"));
            for (Map.Entry<Integer, List<String>> queryRes : QuerySearcher.queriesResult.entrySet()) {
                List<String> sorted = queryRes.getValue().parallelStream().sorted(String::compareTo).collect(Collectors.toCollection(ArrayList::new));
                for (String document : sorted) {
                    pww.println(queryRes.getKey() + " 0 " + document + " 1 42.38 mt");
                }
            }
            pww.close();
            Ranker.experimentsFunc();
            pww.flush();
        }
    }




}
