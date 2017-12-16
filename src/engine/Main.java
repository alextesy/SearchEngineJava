package engine;



import gui.EngineMenu;
import javafx.util.Pair;

import java.util.Map;
import java.util.Objects;
import java.util.PriorityQueue;

import static engine.Indexer.readDictionary;

public class Main {

    public static void main(String[] args) throws Exception {

    /*
        try {

            String corpusPath = "dictionary:\\documents\\users\\kremians\\Documents\\corpus" ;
            String docsPath = "dictionary:\\documents\\users\\kremians\\Documents\\blabla\\";

            Indexer indexer = new Indexer(corpusPath,docsPath,Indexer.CORPUS_BYTE_SIZE/10,true);
            indexer.toIndex();
            indexer.writeDictionary(docsPath,indexer.dictionary);
            System.out.println(indexer.getIndexRunningTime());
            //Map<String,long[]> dictionary = (Map<String,long[]>)(new ObjectInputStream(new FileInputStream("C:\\Users\\IBM_ADMIN\\Documents\\blabla\\dictionary"))).readObject();

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
            Map<String, Object[]> alex = (Map<String, Object[]>) readDictionary("d:\\documents\\users\\kremians\\Documents\\nostem\\dictionary.txt");
            PriorityQueue<Map.Entry<String, Object[]>> tf = new PriorityQueue<>((o1, o2) -> Long.compare((long) o2.getValue()[0], (long) o1.getValue()[0]));
            PriorityQueue<Map.Entry<String, Object[]>> df = new PriorityQueue<>((o1, o2) -> (Integer) o2.getValue()[1] - (Integer) o1.getValue()[1]);

            for (Map.Entry<String, Object[]> termData : alex.entrySet()) {
                tf.add(termData);
                df.add(termData);
            }
            System.out.println("Total Tf");
            for (int i = 0; i < 10; i += 1) {
                Map.Entry<String, Object[]> ttf = tf.poll();
                System.out.println(ttf.getKey() + " " + ttf.getValue()[0]);
            }
            System.out.println();
            System.out.println("Total DF");
            for (int i = 0; i < 10; i++) {
                Map.Entry<String, Object[]> tdf = df.poll();
                System.out.println(tdf.getKey() + " " + tdf.getValue()[1]);
            }
//
        //}
        //Map<String,Object[]> tal=(Map<String, Object[]>) readDictionary("d:\\documents\\users\\kremians\\Documents\\blabla\\dictionary.txt");
        //System.out.println(tal.size());


        //new EngineMenu();
    }


}
