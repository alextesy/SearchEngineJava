package Main;


import gui.EngineMenu;

import java.io.IOException;

public class Main {

    public static void main(String[] args) throws IOException {

        new Indexer("d:\\documents\\users\\talbense\\Documents\\corpus","").toIndex();
        //new EngineMenu();
        /*
        for(Document doc : Document.corpusDocuments.values())
            System.out.println(doc);
        */
        for(Term term : Indexer.currentTermDictionary.values())
            System.out.println(term);
    }
}
