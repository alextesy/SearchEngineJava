package gui;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;

public class EngineMenu {

    private static final Dimension OUTER_FRAME_DIMENSION = new Dimension(400,400);

    private JFrame engineFrame;
    private InitializationPathTable pathsInitializing;
    private boolean performStemming;



    public EngineMenu(){
        initEngineMainFrame();
        pathsInitializing = new InitializationPathTable(this.engineFrame,false);
        performStemming = true;
    }

    private void initEngineMainFrame() {
        this.engineFrame = new JFrame("MyEngine");
        this.engineFrame.setSize(OUTER_FRAME_DIMENSION);
        this.engineFrame.setJMenuBar(menuBar());
        try {
            this.engineFrame.add(new JLabel(new ImageIcon(ImageIO.read(new File("imgs/gears.png")))));
        } catch (IOException e) {
            e.printStackTrace();
        }

        this.engineFrame.setVisible(false);
        this.engineFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
                System.exit(0);
            }
        });
    }

    private JMenuBar menuBar() {
        JMenuBar engineMenuBar = new JMenuBar();
        engineMenuBar.add(createFileMenu());
        engineMenuBar.add(createOptionsMenu());
        engineMenuBar.add(createPreferencesMenu());

        return engineMenuBar;
    }


    private JMenu createFileMenu() {
        JMenu fileMenu = new JMenu("File");

        JMenuItem initializationFrame = new JMenuItem("Initialize paths");
        JMenuItem saveCacheDictionary = new JMenuItem("Save cache/dictionary");
        JMenuItem openCacheDictionary = new JMenuItem("Open cache/dictionary");
        JMenuItem exitMenuItem = new JMenuItem("Exit");

        fileMenu.add(initializationFrame);
        fileMenu.add(saveCacheDictionary);
        fileMenu.add(openCacheDictionary);
        fileMenu.add(exitMenuItem);



        initializationFrame.addActionListener(e -> {
            engineFrame.setVisible(false);
            pathsInitializing.setVisible(true);
        });

        saveCacheDictionary.addActionListener(e -> {
            JFileChooser savingDirChooser = new JFileChooser();
            savingDirChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            savingDirChooser.setName("Save cache/dictionary at");
            int returnVal = savingDirChooser.showSaveDialog(null);
            if(returnVal == JFileChooser.APPROVE_OPTION) {
                File savedDirChoosed = savingDirChooser.getSelectedFile();
                //TODO - save the cache and dictonary at the chosen path
            }
        });

        openCacheDictionary.addActionListener(e -> {
            JFileChooser openingDirChooser = new JFileChooser();
            openingDirChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            openingDirChooser.setName("Open cache/dictionary at");
            int returnVal = openingDirChooser.showOpenDialog(null);
            if(returnVal == JFileChooser.APPROVE_OPTION) {
                File openDirChoosed = openingDirChooser.getSelectedFile();
                //TODO - update the new cache and dictionary location to the class path field
            }
        });


        exitMenuItem.addActionListener(e -> {
            System.exit(0);
        });
        return  fileMenu;
    }

    private JMenu createOptionsMenu(){
        JMenu optionsMenu = new JMenu("Options");

        JMenuItem displayCache = new JMenuItem("Display cache");
        JMenuItem displayDictionary = new JMenuItem("Display dictionary");
        JMenuItem resetEnginePosting = new JMenuItem("Reset engine");

        resetEnginePosting.addActionListener(e -> {
            //TODO - add resetEnginePosting option - ask if user is sure before reseting
        });


        optionsMenu.add(displayCache);
        optionsMenu.add(displayDictionary);
        optionsMenu.add(resetEnginePosting);

        return optionsMenu;
    }

    private JMenu createPreferencesMenu() {
        JMenu preferencesMenu = new JMenu("Preferences");

        JCheckBoxMenuItem performStemming = new JCheckBoxMenuItem("Perform Porter Stemming" , true);
        performStemming.addActionListener(e -> {
            this.performStemming = performStemming.isSelected();
        });


        preferencesMenu.add(performStemming);
        return preferencesMenu;
    }


}
