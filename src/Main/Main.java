package Main;


import gui.EngineMenu;

import java.io.IOException;

public class Main {

    public static void main(String[] args) throws IOException {

        new Indexer("d:\\documents\\users\\kremians\\Documents\\corpus","").toIndex();
        //new EngineMenu();

    }
}
