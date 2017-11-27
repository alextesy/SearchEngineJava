package Main;



import java.io.IOException;
import java.util.Collection;

public class Main {

    public static void main(String[] args) throws IOException {

        new Indexer("d:\\documents\\users\\talbense\\Documents\\corpus","").toIndex();
        //new EngineMenu();
        /*
        for(Document doc : Document.corpusDocuments.values())
            System.out.println(doc);

        String path = "d:\\documents\\users\\talbense\\Documents\\blabla\\myText0.txt";
        Collection<Term> terms = Indexer.iterateThroughTermFile(path,4,10);
        for ( Term term : terms)
            System.out.println(term);
        */
    }

}
