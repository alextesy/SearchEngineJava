package Main;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
    //TODO - posted file should be sorted before merged
    //TODO - merge files and decide num of posted file
    //TODO - initialize for the last time our dictionary
    //TODO - CACHE and parsing
    public void toIndex(){
        long now=System.currentTimeMillis();
        File dir = new File(this.pathToCorpus);
        File[] directoryListing = dir.listFiles();
        if (directoryListing != null) {
            for (File child : directoryListing) {
                ReadFile.readTextFile(child);
                try{
                    PrintWriter postFile = new PrintWriter( "d:\\documents\\users\\talbense\\Documents\\blabla\\myText" +counter + ".txt");
                    for (Term term : currentTermDictionary.values()){
                        postFile.println(term.encryptTermToStr());
                    }
                    currentTermDictionary.clear();
                    postFile.close();
                    /*
                    counter+=1;
                    if(counter==10)
                        break;
                    */
                }
                catch (Exception e){e.printStackTrace();}
            }
            writeDocumentData();
        }



        long then=System.currentTimeMillis();
        this.indexRunningTime = then - now;
        System.out.print((double)indexRunningTime/1000);
    }

    private void writeDocumentData(){
        HashMap<String,Document> corpusDocumentsData = Document.corpusDocuments;
        try {
            PrintWriter corpusDocFile = new PrintWriter("d:\\documents\\users\\talbense\\Documents\\blabla\\documentsData"+ ".txt");
            for(Document doc : corpusDocumentsData.values()){
                corpusDocFile.println(doc);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Collection<Term> iterateThroughTermFile(String path, int termLine, int range){
        List<Term> termsFileIteration = new ArrayList<>();
        try (Stream<String> lines = Files.lines(Paths.get(path))) {
            Collection <String> termsString = lines.skip(termLine).limit(range).collect(Collectors.toList());
            for(String str : termsString)
                termsFileIteration.add(Term.decryptTermFromStr(str));
        }catch (Exception e){
            e.printStackTrace();
        }
        return termsFileIteration;
    }

}
