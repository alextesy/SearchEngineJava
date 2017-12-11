package gui;

import Main.Indexer;
import Main.ReadFile;
import Main.Term;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;

public class EngineMenu {

    private static final Dimension OUTER_FRAME_DIMENSION = new Dimension(400,380);

    private JFrame engineFrame;
    private InitializationPathTable pathsInitializing;
    private DisplayDictionaryPanel dictionaryPanel;
    private boolean performStemming;

    private Indexer indexer;

    public Map<String,Term> cacheDictionary = null;
    public Map<String,long[]> Dictionary =  null;

    JLabel background;

    public EngineMenu(){
        initEngineMainFrame();
        dictionaryPanel = new DisplayDictionaryPanel(this.engineFrame,false);
        pathsInitializing = new InitializationPathTable(this.engineFrame,false);
        performStemming = true;


    }


    private void initEngineMainFrame() {
        this.engineFrame = new JFrame("MyEngine");
        this.engineFrame.setResizable(false);
        this.engineFrame.setSize(OUTER_FRAME_DIMENSION);
        this.engineFrame.setJMenuBar(menuBar());
        try {
            this.background = new JLabel(new ImageIcon(ImageIO.read(new File("imgs/img.png"))));
            this.engineFrame.add(this.background);
        } catch (Exception e) {
            e.printStackTrace();
        }


        this.engineFrame.setVisible(true);
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

        JMenuItem createIndexFile = new JMenuItem("Create index");
        JMenuItem initializationFrame = new JMenuItem("Initial paths");
        JMenuItem saveCacheDictionary = new JMenuItem("Save cache/dictionary");
        JMenuItem openCacheDictionary = new JMenuItem("Open cache/dictionary");
        JMenuItem exitMenuItem = new JMenuItem("Exit");

        fileMenu.add(createIndexFile);
        fileMenu.add(initializationFrame);
        fileMenu.add(saveCacheDictionary);
        fileMenu.add(openCacheDictionary);
        fileMenu.add(exitMenuItem);

        initializationFrame.addActionListener(e -> pathsInitializing.setVisible(true));

        createIndexFile.addActionListener(e -> {
            try{
                String corpusPath = pathsInitializing.getCorpusDir();
                String postingPath = pathsInitializing.getPostingDir();
                indexer = new Indexer(corpusPath,postingPath,Indexer.CORPUS_BYTE_SIZE/10,performStemming);
                new Thread(() -> {
                    try {
                        saveCacheDictionary.setEnabled(false);
                        openCacheDictionary.setEnabled(false);
                        createIndexFile.setEnabled(false);
                        indexer.toIndex();
                        Dictionary = new TreeMap<>(indexer.Dictionary);
                        cacheDictionary = new TreeMap<>(indexer.cacheTerms);
                        createIndexFile.setEnabled(true);
                        saveCacheDictionary.setEnabled(true);
                        openCacheDictionary.setEnabled(true);
                        drawIndexedEngine();

                    } catch (Exception e1) {
                        JOptionPane.showMessageDialog(this.engineFrame,"Incorrect paths.try initial paths again.");
                        createIndexFile.setEnabled(true);

                    }
                }).start();

            }
            catch (Exception e2){
                JOptionPane.showMessageDialog(this.engineFrame,"You should initial paths first.");
            }

        });
        //TODO - SAVE CACHE
        saveCacheDictionary.addActionListener(e -> {
            try {
                if (Dictionary == null)
                    throw new RuntimeException();
                JFileChooser savingDirChooser = new JFileChooser();
                savingDirChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                savingDirChooser.setName("Save cache/dictionary at");
                int returnVal = savingDirChooser.showSaveDialog(null);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    File savedDirChoosed = savingDirChooser.getSelectedFile();
                    openCacheDictionary.setEnabled(false);
                    new Thread(() -> Indexer.writeDictionary(savedDirChoosed.getPath(), Dictionary)).start();
                    openCacheDictionary.setEnabled(true);


                }
            }
            catch (Exception e2 ){
                JOptionPane.showMessageDialog(engineFrame,"No dictionary or cache uploaded to RAM yet");
            }
        });


        openCacheDictionary.addActionListener(e -> {
            JFileChooser openingDirChooser = new JFileChooser();
            openingDirChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            openingDirChooser.setName("Open cache/dictionary at");
            int returnVal = openingDirChooser.showOpenDialog(null);
            if(returnVal == JFileChooser.APPROVE_OPTION) {
                File openDirChoosed = openingDirChooser.getSelectedFile();
                new Thread(() -> {
                    try{
                        saveCacheDictionary.setEnabled(false);
                        Dictionary = new TreeMap<>(Indexer.readDictionary(openDirChoosed + "//dictionary.txt"));
                        saveCacheDictionary.setEnabled(true);
                    }
                    catch (Exception e13){
                        JOptionPane.showMessageDialog(engineFrame,"No dictionary or cache in dir");
                    }
                }).start();
            }
        });


        exitMenuItem.addActionListener(e -> {
            System.exit(0);
        });
        return  fileMenu;
    }
    private void drawIndexedEngine(){

        try {
            engineFrame.remove(background);
            BufferedImage img = ImageIO.read(new File("imgs/img.png"));
            Graphics g = img.getGraphics();
            g.setFont(g.getFont().deriveFont(15f));
            g.drawString("Number of Documents: " + ReadFile.docNumberOfFiles,20,80);
            g.drawString( "Index Size: " + indexer.getIndexSize() + " Bytes",20,105);
            g.drawString("Cache Size: " + indexer.getCacheSize() + " Bytes",20,130);
            g.drawString( "Running Time: " + indexer.getIndexRunningTime() + " seconds",20,155);
            g.dispose();
            ImageIO.write(img,"png", new File("imgs/results.png"));
            background = new JLabel(new ImageIcon(ImageIO.read(new File("imgs/results.png"))));
            engineFrame.add(background);
            engineFrame.validate();
            engineFrame.repaint();




        } catch (IOException e) {
            e.printStackTrace();
        }


    }
    private JMenu createOptionsMenu(){
        JMenu optionsMenu = new JMenu("Options");

        JMenuItem displayCache = new JMenuItem("Display cache");
        JMenuItem displayDictionary = new JMenuItem("Display dictionary");
        JMenuItem resetEnginePosting = new JMenuItem("Reset engine");



        optionsMenu.add(displayCache);
        optionsMenu.add(displayDictionary);
        optionsMenu.add(resetEnginePosting);

        resetEnginePosting.addActionListener(e -> {
            //TODO - add resetEnginePosting option - ask if user is sure before reseting
        });
        displayDictionary.addActionListener(e -> {
            try{
                dictionaryPanel.redo(Dictionary);
                dictionaryPanel.setVisible(true);
            }catch (Exception e1) {
                JOptionPane.showMessageDialog(this.engineFrame,"No dictionary uploaded yet.");
            }

        });
        displayCache.addActionListener(e -> {

        });
        return optionsMenu;
    }

    private JMenu createPreferencesMenu() {
        JMenu preferencesMenu = new JMenu("Preferences");

        JCheckBoxMenuItem performStemming = new JCheckBoxMenuItem("Perform Porter Stemming" , true);
        performStemming.addActionListener(e -> {
            this.performStemming = performStemming.isSelected();
            indexer.setStemming(this.performStemming);
        });
        preferencesMenu.add(performStemming);
        return preferencesMenu;
    }


}
