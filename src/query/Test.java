package query;

import engine.Indexer;
import gui.EngineMenu;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Random;


public class Test {
    public static void main(String[] args) throws Exception {

        Indexer.pathToCorpus = "C:\\Users\\אלי\\Desktop\\corpus";
        Indexer.pathToPosting = "C:\\Users\\אלי\\Desktop\\doc";
        Indexer.stemming = EngineMenu.Stemming.True;
        Indexer.Dictionary = Indexer.readDictionary("C:\\Users\\אלי\\Desktop\\doc\\dictionary" + Indexer.stemming.toString() + ".txt");
        //Indexer.cacheTerms = Indexer.readCache("C:\Users\אלי\Desktop\doc\\cache" + Indexer.stemming.toString() + "txt");
        Ranker.pw = new PrintWriter(new FileWriter("C:\\Users\\אלי\\Desktop\\doc\\experiment2.txt"));
         while (true){
             Ranker.alpha =    (double)(new Random().nextInt(100))/200;
             Ranker.beta  =    (double)(new Random().nextInt(100))/200;
             Ranker.lambda =   (double)(new Random().nextInt(100))/200;
             Ranker.epsilon  = (double)(new Random().nextInt(100))/200;
             QuerySearcher.writeQueriesResult("C:\\Users\\אלי\\Desktop\\doc\\queries.txt");
             Ranker.experimentsFunc();
        }


    }
}
