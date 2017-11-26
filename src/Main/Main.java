package Main;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Date;

public class Main {

    public static void main(String[] args) throws IOException {

        ReadFile alex=new ReadFile("d:\\documents\\users\\talbense\\Documents\\corpus");
        long now=System.currentTimeMillis();
        alex.readAll();
        long then=System.currentTimeMillis();
        System.out.println(then-now);


    }
}
