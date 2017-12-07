package Main;



import gui.EngineMenu;

import java.io.*;

public class Main {

    public static void main(String[] args) {

/*
        try {

            String corpusPath = "C:\\Users\\אלי\\Desktop\\corpus" ;
            String docsPath = "C:\\Users\\אלי\\doc\\";

            Indexer indexer = new Indexer(corpusPath,docsPath,Indexer.CORPUS_BYTE_SIZE/10);
            indexer.toIndex();
            indexer.writeDictionary(docsPath);
            System.out.println(indexer.getIndexRunningTime());

            Indexer.buildCache(Indexer.readDictionary(docsPath));

        } catch (Exception e) {
            e.printStackTrace();
        }
*/
        new EngineMenu();




    }

}
