package gui;

import engine.Indexer;
import engine.ReadFile;
import engine.Term;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import gui.DisplayScrollPanel.*;
public class EngineMenu {

    private static final Dimension OUTER_FRAME_DIMENSION = new Dimension(400,380);

    private JFrame engineFrame;
    private InitializationPathTable pathsInitializing;
    private DisplayDictionaryPanel dictionaryPanel;
    private DisplayCachePanel cachePanel;
    private boolean performStemming;

    private Indexer indexer;

    public Map<String,Term> cache = null;
    public Map<String,Object[]> dictionary =  null;

    JLabel background;

    public EngineMenu(){
        initEngineMainFrame();
        dictionaryPanel = new DisplayDictionaryPanel(this.engineFrame,false,null);
        cachePanel = new DisplayCachePanel(this.engineFrame,false,null);
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
        JMenuItem resetEnginePosting = new JMenuItem("Reset engine");
        JMenuItem exitMenuItem = new JMenuItem("Exit");

        fileMenu.add(createIndexFile);
        fileMenu.add(initializationFrame);
        fileMenu.add(saveCacheDictionary);
        fileMenu.add(openCacheDictionary);
        fileMenu.add(resetEnginePosting);
        fileMenu.add(exitMenuItem);

        initializationFrame.addActionListener(e -> pathsInitializing.setVisible(true));

        createIndexFile.addActionListener(e -> {
            try{
                String corpusPath = pathsInitializing.getCorpusDir();
                String postingPath = pathsInitializing.getPostingDir();
                indexer = new Indexer(corpusPath,postingPath,Indexer.CORPUS_BYTE_SIZE/10);
                Indexer.setStemming(performStemming);
                Indexer.cacheTerms =Indexer.initCacheStrings();
                new Thread(() -> {
                    try {
                        saveCacheDictionary.setEnabled(false);
                        openCacheDictionary.setEnabled(false);
                        createIndexFile.setEnabled(false);
                        resetEnginePosting.setEnabled(false);
                        indexer.toIndex();
                        dictionary = new TreeMap<>(indexer.Dictionary);
                        cache = new TreeMap<>(indexer.cacheTerms);
                        createIndexFile.setEnabled(true);
                        saveCacheDictionary.setEnabled(true);
                        openCacheDictionary.setEnabled(true);
                        resetEnginePosting.setEnabled(true);
                        drawIndexedEngine();

                    } catch (Exception e1) {
                        e1.getMessage();
                        JOptionPane.showMessageDialog(this.engineFrame,"Incorrect paths.try initial paths again.");
                        createIndexFile.setEnabled(true);

                   }
                }).start();

            }
            catch (Exception e2){
                JOptionPane.showMessageDialog(this.engineFrame,"You should initial paths first.");
            }

        });
        saveCacheDictionary.addActionListener(e -> {
            new Thread(() -> {
                try {
                    if (dictionary == null || cache ==null)
                        throw new RuntimeException();
                    JFileChooser savingDirChooser = new JFileChooser();
                    savingDirChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                    savingDirChooser.setName("Save cache/dictionary at");
                    int returnVal = savingDirChooser.showSaveDialog(null);
                    if (returnVal == JFileChooser.APPROVE_OPTION) {
                        File savedDirChoosed = savingDirChooser.getSelectedFile();
                        openCacheDictionary.setEnabled(false);
                        String pathStemming = performStemming ==true ? "Stem" : "";
                        Thread t1 = new Thread(() -> Indexer.writeDictionary(savedDirChoosed.getPath() + "/dictionary" + pathStemming  +  ".txt", dictionary));
                        //Thread t2 = new Thread(() -> Indexer.writeCache(savedDirChoosed.getPath()+"/cache" + pathStemming  +  ".txt", cache));
                        t1.start();
                        //t2.start();
                        t1.join();
                        //t2.join();
                        openCacheDictionary.setEnabled(true);
                    }
                }
                catch (Exception e2 ){
                    JOptionPane.showMessageDialog(engineFrame,"No dictionary or cache uploaded to RAM yet.");
                }
            }).start();

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
                        String pathStemming = performStemming ==true ? "Stem" : "";
                        Thread t1 =new Thread(() -> {
                            try {
                                dictionary = new TreeMap<>(Indexer.readDictionary(openDirChoosed + "//dictionary" + pathStemming + ".txt"));
                            } catch (Exception e1) {
                                JOptionPane.showMessageDialog(engineFrame,"Non exist dictionary in dir");
                            }
                        });
                        Thread t2 =new Thread(() -> {
                            try {
                                cache = new TreeMap<>(Indexer.readCache(openDirChoosed + "//cache" + pathStemming + ".txt"));
                            } catch (Exception e1) {
                                JOptionPane.showMessageDialog(engineFrame,"Non exist cache in dir");
                            }
                        });

                        t1.start(); t2.start();
                        t1.join(); t2.join();


                        saveCacheDictionary.setEnabled(true);
                    }
                    catch (Exception e13){
                        e13.printStackTrace();
                    }
                }).start();
            }
        });


        resetEnginePosting.addActionListener(e -> {
            cache = null;
            dictionary = null;
            if(indexer !=null)
                indexer.clear();
            try{
                String path = pathsInitializing.getPostingDir()+ "//Hallelujah.txt";
                if(new File(path).isFile()) {
                    try {
                        Files.delete(Paths.get(path));
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
            }
            catch (Exception e1 )
            {
                JOptionPane.showMessageDialog(engineFrame,"Need a path to posting dir.");
            }

        });
        exitMenuItem.addActionListener(e -> System.exit(0));
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



        optionsMenu.add(displayCache);
        optionsMenu.add(displayDictionary);

        displayDictionary.addActionListener(e -> new Thread(() -> {
            dictionaryPanel.setDictionary(dictionary);
            try{
                dictionaryPanel.redo();
                dictionaryPanel.setVisible(true);
            }catch (Exception e1) {
                JOptionPane.showMessageDialog(this.engineFrame,"No dictionary uploaded yet.");
            }
        }).start());

        displayCache.addActionListener(e -> new Thread(() -> {
            cachePanel.setCache(cache);
            try{
                cachePanel.redo();
                cachePanel.setVisible(true);
            }catch (Exception e1) {
                JOptionPane.showMessageDialog(this.engineFrame,"No cache uploaded yet.");
            }
        }).start());

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
