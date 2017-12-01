package Main;



import java.io.*;

public class Main {

    public static void main(String[] args)  {

        try {
            Indexer indexer = new Indexer("C:\\Users\\אלי\\Desktop\\corpus","",Indexer.CORPUS_BYTE_SIZE/11);
            indexer.toIndex();
            System.out.println(indexer.getIndexRunningTime());

        } catch (IOException e) {
            e.printStackTrace();
        }
        //new EngineMenu();


    }

}
