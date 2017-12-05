package Main;



import java.io.*;
import java.lang.reflect.Array;
import java.util.*;

import static javafx.application.Platform.exit;

public class Main {

    public static void main(String[] args) {


        try {

            Indexer indexer = new Indexer("d:\\documents\\users\\kremians\\Documents\\corpus","d:\\documents\\users\\kremians\\Documents\\blabla\\",Indexer.CORPUS_BYTE_SIZE/10);
            indexer.toIndex();
            //indexer.printDictionary("d:\\documents\\users\\kremians\\Documents\\blabla\\");
            System.out.println(indexer.getIndexRunningTime());

        } catch (IOException e) {
            e.printStackTrace();
        }
        //new EngineMenu();


        //Indexer.buildCache(Indexer.readDictionary("d:\\documents\\users\\kremians\\Documents\\blabla\\"));

        /*
        try (BufferedReader br = new BufferedReader(new FileReader("C:\\Users\\אלי\\doc\\Hallelujah"))) {

            String line;
            while ((line = br.readLine()) != null)
                try {
                    Term term = Term.decryptTermFromStr(line);
                    Indexer.Dictionary.put(term.getValue(), term.getTermTDF());
                } catch (Exception e) {
                    System.out.println(line);



                }


        } catch (IOException e) {
        }




       //Map<String,Integer> map = MapUtil.sortByValue(Indexer.Dictionary);
       //System.out.println(ReadFile.docNummberOfFiles);

        */
    }

}
