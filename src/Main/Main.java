package Main;

import gui.EngineMenu;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Date;

public class Main {

    public static void main(String[] args) throws IOException {

        new Indexer("d:\\documents\\users\\talbense\\Documents\\corpus","").toIndex();
        //new EngineMenu();

    }
}
