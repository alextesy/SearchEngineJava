package Main;



import java.io.*;
import java.lang.reflect.Array;
import java.util.*;

import static javafx.application.Platform.exit;

public class Main {

    public static void main(String[] args) {
/*
        try {

            Indexer indexer = new Indexer("C:\\Users\\אלי\\Desktop\\corpus","C:\\Users\\אלי\\doc\\",Indexer.CORPUS_BYTE_SIZE/13);
            indexer.toIndex();
            System.out.println(indexer.getIndexRunningTime());

        } catch (IOException e) {
            e.printStackTrace();
        }
        //new EngineMenu();

        // System.out.println(Indexer.Dictionary);


*/
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



        Map<String,Integer> map = MapUtil.sortByValue(Indexer.Dictionary);
        System.out.println(ReadFile.docNummberOfFiles);

    }


}
