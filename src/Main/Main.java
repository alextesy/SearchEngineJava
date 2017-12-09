package Main;



import gui.EngineMenu;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class Main {

    public static void main(String[] args) {


        try {

            String corpusPath = "C:\\Users\\IBM_ADMIN\\Desktop\\corpus\\corpus" ;
            String docsPath = "C:\\Users\\IBM_ADMIN\\Documents\\blabla\\";

            Indexer indexer = new Indexer(corpusPath,docsPath,Indexer.CORPUS_BYTE_SIZE/10,true);
            indexer.toIndex();
            indexer.writeDictionary(docsPath);
            System.out.println(indexer.getIndexRunningTime());
            //Map<String,long[]> Dictionary = (Map<String,long[]>)(new ObjectInputStream(new FileInputStream("C:\\Users\\IBM_ADMIN\\Documents\\blabla\\dictionary"))).readObject();

            //Indexer.buildCache(Indexer.readDictionary(docsPath));

        } catch (Exception e) {
            e.printStackTrace();
        }

        //new EngineMenu();




    }

}
