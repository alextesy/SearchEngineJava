package Main;



import gui.EngineMenu;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Main {

    public static void main(String[] args) {

    /*
        try {

            String corpusPath = "d:\\documents\\users\\kremians\\Documents\\corpus" ;
            String docsPath = "d:\\documents\\users\\kremians\\Documents\\blabla\\";

            Indexer indexer = new Indexer(corpusPath,docsPath,Indexer.CORPUS_BYTE_SIZE/10,true);
            indexer.toIndex();
            indexer.writeDictionary(docsPath,indexer.Dictionary);
            System.out.println(indexer.getIndexRunningTime());
            //Map<String,long[]> Dictionary = (Map<String,long[]>)(new ObjectInputStream(new FileInputStream("C:\\Users\\IBM_ADMIN\\Documents\\blabla\\dictionary"))).readObject();

            //Indexer.buildCache(Indexer.readDictionary(docsPath));

        } catch (Exception e) {

            e.printStackTrace();
        }





        try {
            Map<String,long[]> dict = Indexer.readDictionary("C:\\Users\\אלי\\doc\\dictionary.txt");
            RandomAccessFile raf = new RandomAccessFile(new File("C:\\Users\\אלי\\doc\\Hallelujah.txt"),"r");
            raf.seek(dict.get("accurate")[2]);
            Term term = Term.decryptTermFromStr(raf.readLine());
            System.out.println(term);
        } catch (IOException e) {
            e.printStackTrace();
        }

        new EngineMenu();
    }
*/
        new EngineMenu();

    }
}
