package Main;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class Indexer {
    public static final Map<String,Term> currentTermDictionary = new HashMap<>();

    private long indexRunningTime;

    private String pathToCorpus;
    private String pathToPosting;
    private int counter=0;

    public Indexer(String pathToCorpus, String pathToPosting) {
        this.pathToCorpus = pathToCorpus;
        this.pathToPosting = pathToPosting;
    }

    public void toIndex(){
        long now=System.currentTimeMillis();
        File dir = new File(this.pathToCorpus);
        File[] directoryListing = dir.listFiles();
        if (directoryListing != null) {
            for (File child : directoryListing) {
                ReadFile.readTextFile(child);
                try{
                    FileOutputStream f = new FileOutputStream(new File("myObjects.txt"));
                    ObjectOutputStream o = new ObjectOutputStream(f);
                    for (Term term : currentTermDictionary.values()){
                        o.writeObject(term);
                    }
                    currentTermDictionary.clear();
                    f.close();
                    o.close();
                }
                catch (Exception e){e.printStackTrace();}
                //break; /* first posting file! */
            }
            openFile();
        }



        long then=System.currentTimeMillis();
        this.indexRunningTime = then - now;
        System.out.print(indexRunningTime);
    }

    public void openFile(){
        try{
            FileInputStream fi = new FileInputStream(new File("d:\\documents\\users\\talbense\\Document\\MyText" + counter));
            counter+=1;
            ObjectInputStream oi = new ObjectInputStream(fi);
            Term term =null;
            try{
                while((term = (Term)oi.readObject())!= null){
                    currentTermDictionary.put(term.getValue(),term);
                }
            }catch (Exception e){};
        }
        catch (Exception e){e.printStackTrace(); }
    }

}
