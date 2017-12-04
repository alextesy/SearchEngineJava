package Main;



import java.io.*;
import java.lang.reflect.Array;
import java.util.*;

import static javafx.application.Platform.exit;

public class Main {

    public static void main(String[] args) {
        /*
        try {
            Indexer indexer = new Indexer("d:\\documents\\users\\talbense\\Documents\\corpus","d:\\documents\\users\\talbense\\Documents\\blabla\\",Indexer.CORPUS_BYTE_SIZE/10);
            indexer.toIndex();
            System.out.println(indexer.getIndexRunningTime());

        } catch (IOException e) {
            e.printStackTrace();
        }
        //new EngineMenu();
         System.out.println(Indexer.Dictionary);

        */
        try (BufferedReader br = new BufferedReader(new FileReader("d:\\documents\\users\\talbense\\Documents\\blabla\\Hallelujah"))) {

            String line;
            while ((line = br.readLine()) != null)
                try {
                    Term term = Term.decryptTermFromStr(line);
                    Indexer.Dictionary.put(term.getValue(), term.getTermIDF());
                } catch (Exception e) {
                    System.out.println(line);
                     /*
                     String[] termData = line.split("#");
                     String termName = termData[0];
                     String DocName;
                     String index2;
                     System.out.println(line);
                     for(int i=1 ; i<termData.length; i+=1){
                         String[] docData = termData[i].split("&");
                         String FileName = docData[0];
                         DocName = docData[1];
                         String[] termIndex = docData[2].split("\\^");
                         String index = termIndex[0];
                         System.out.println(termName + " " +  FileName + " " + DocName + " " + index + " ");
                         for(int j=1; j < termIndex.length; j+=1){
                             index2 = termIndex[j];
                             System.out.println(index2 + " ");
                         }
                     }
                     */

                }


        } catch (IOException e) {
        }

        Map<String,Integer> map = MapUtil.sortByValue(Indexer.Dictionary);
        for (Map.Entry<String, Integer> entry : Indexer.Dictionary.entrySet()) {
            System.out.println(entry.getKey() + " " + entry.getValue());
        }
    }


}
