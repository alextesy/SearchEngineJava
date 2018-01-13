package gui;

import engine.Indexer;
import engine.ReadFile;
import engine.Term;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;

import gui.DisplayScrollPanel.*;
import javafx.stage.FileChooser;
import javafx.util.Pair;
import query.DocumentSummarize;
import query.QuerySearcher;

public class EngineMenu {

    private static final Dimension OUTER_FRAME_DIMENSION = new Dimension(400,480);

    private JFrame engineFrame;
    private InitializationPathTable pathsInitializing;
    private DisplayDictionaryPanel dictionaryPanel;
    private DisplayCachePanel cachePanel;
    private Stemming performStemming;

    private Indexer indexer=null;
    private File cacheDictionaryLastPath;

    public Map<String,Term> cache = null;
    public Map<String,Object[]> dictionary =  null;

    JLabel background;


    /* Part 2 */
    private boolean toExtend;
    private boolean toSummary;
    private JTextArea documentSummaryText;



    public EngineMenu(){

        initEngineMainFrame();
        dictionaryPanel = new DisplayDictionaryPanel(this.engineFrame,false,null,2);
        cachePanel = new DisplayCachePanel(this.engineFrame,false,null,3);
        pathsInitializing = new InitializationPathTable(this.engineFrame,false);
        performStemming = Stemming.True;

    }


    private void initEngineMainFrame() {
        this.engineFrame = new JFrame("MyEngine");
        this.engineFrame.setResizable(false);
        this.engineFrame.setSize(OUTER_FRAME_DIMENSION);
        this.engineFrame.setJMenuBar(menuBar());
        mainBackgroundEngine();


        JScrollPane documentScrollPane = new JScrollPane();
        this.documentSummaryText = new JTextArea("This engine as been built by Alex Kremiansky and Tal Ben Senior as a\n" +
                "replacement for the old and lame search engine called Google,\nif you are pleased with out work - subscribe to out channel......\n" +
                "or..... just give us an A grade, it could be a fair trade as well! ;)");
        this.documentSummaryText.setEditable(false);
        documentScrollPane.setViewportView(documentSummaryText);
        documentScrollPane.setPreferredSize(new Dimension(50,95));


        this.engineFrame.add(documentScrollPane,BorderLayout.SOUTH);
        this.engineFrame.revalidate();



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
        JMenuItem saveLastQueryResult = new JMenuItem("Save Query Result");
        JMenuItem saveCacheDictionary = new JMenuItem("Save cache/dictionary");
        JMenuItem openCacheDictionary = new JMenuItem("Open cache/dictionary");
        JMenuItem resetEnginePosting = new JMenuItem("Reset engine");
        JMenuItem exitMenuItem = new JMenuItem("Exit");

        fileMenu.add(createIndexFile);
        fileMenu.add(initializationFrame);
        fileMenu.add(saveLastQueryResult);
        fileMenu.add(saveCacheDictionary);
        fileMenu.add(openCacheDictionary);
        fileMenu.add(resetEnginePosting);
        fileMenu.add(exitMenuItem);

        initializationFrame.addActionListener(e -> pathsInitializing.setVisible(true));
        saveLastQueryResult.addActionListener(e -> {
            if(QuerySearcher.queriesResult == null){
                JOptionPane.showMessageDialog(engineFrame,"Should run query first");
            }
            else{
                new Thread(() -> {
                    saveLastQueryResult.setEnabled(false);
                    JFileChooser savingQueryChooser = new JFileChooser();
                    savingQueryChooser.setName("Save query result");
                    int returnVal = savingQueryChooser.showSaveDialog(null);
                    if (returnVal == JFileChooser.APPROVE_OPTION) {
                        File savingQueryChoosed = savingQueryChooser.getSelectedFile();
                        try{
                            PrintWriter pw = new PrintWriter(new FileWriter(savingQueryChoosed));
                            for(Map.Entry<Integer,List<String>> queryRes: QuerySearcher.queriesResult.entrySet()){
                                for(String document : queryRes.getValue()){
                                    pw.println(queryRes.getKey() + " 0 "+ document + " 1 42.38 mt");
                                }
                            }
                            pw.close();
                        }catch (IOException e1){
                            e1.printStackTrace();
                        }

                    }
                    saveLastQueryResult.setEnabled(true);
                }).start();

            }


        });
        createIndexFile.addActionListener(e -> {
            try{
                if(indexer!=null)
                    indexer.clear();
                indexer = new Indexer(Indexer.CORPUS_BYTE_SIZE/10);
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
                        JOptionPane.showMessageDialog(this.engineFrame,"Incorrect paths.try initial paths again or 'stop_words' file doesn't exist.");
                        createIndexFile.setEnabled(true);

                   }
                }).start();

            }
            catch (Exception e2){
                JOptionPane.showMessageDialog(this.engineFrame,"You should initial paths first.");
            }

        });
        saveCacheDictionary.addActionListener(e -> new Thread(() -> {
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
                    Thread t1 = new Thread(() -> Indexer.writeDictionary(savedDirChoosed.getPath() + "/dictionary" + performStemming.toString()  +  ".txt", dictionary));
                    Thread t2 = new Thread(() -> Indexer.writeCache(savedDirChoosed.getPath()+"/cache" + performStemming.toString()  +  ".txt", cache));
                    t1.start();
                    t2.start();
                    t1.join();
                    t2.join();
                    openCacheDictionary.setEnabled(true);
                }
            }
            catch (Exception e2 ){
                JOptionPane.showMessageDialog(engineFrame,"No dictionary or cache uploaded to RAM yet.");
            }
        }).start());


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
                        Thread t1 =new Thread(() -> {
                            try {
                                dictionary = new TreeMap<>(Indexer.readDictionary(openDirChoosed + "//dictionary" + performStemming.toString() + ".txt"));
                                Indexer.Dictionary = dictionary;
                            } catch (Exception e1) {
                                JOptionPane.showMessageDialog(engineFrame,"Non exist dictionary in dir (notice stemming label)");
                            }
                        });
                        Thread t2 =new Thread(() -> {
                            try {
                                cache = new TreeMap<>(Indexer.readCache(openDirChoosed + "//cache" + performStemming.toString() + ".txt"));
                                Indexer.cacheTerms = cache;
                            } catch (Exception e1) {
                                JOptionPane.showMessageDialog(engineFrame,"Non exist cache in dir (notice stemming label)");
                            }
                        });
                        if(cache!=null && dictionary !=null){
                            cache.clear();
                            dictionary.clear();
                        }
                        performStemming.setRamStem(performStemming.isStem());
                        t1.start(); t2.start();
                        t1.join(); t2.join();
                        this.cacheDictionaryLastPath = openDirChoosed;
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
                String postingFilePath = pathsInitializing.getPostingPath();
                if(new File(postingFilePath +  "//Hallelujah.txt").isFile()) {
                    try {
                        Files.delete(Paths.get(postingFilePath +  "//Hallelujah.txt"));
                    } catch (IOException e1) {
                    }
                }
                if(new File(postingFilePath +  "//HallelujahStem.txt").isFile()) {
                    try {
                        Files.delete(Paths.get(postingFilePath +  "//HallelujahStem.txt"));
                    } catch (IOException e1) {
                    }
                }
                if(this.cacheDictionaryLastPath!=null){
                    String cacheDictionaryFilesPath = cacheDictionaryLastPath.getPath();
                    if(new File(cacheDictionaryFilesPath +  "//cache.txt").isFile()) {
                        try {
                            Files.delete(Paths.get(cacheDictionaryFilesPath +  "//cache.txt"));
                        } catch (IOException e1) {
                        }
                    }
                    if(new File(cacheDictionaryFilesPath +  "//cacheStem.txt").isFile()) {
                        try {
                            Files.delete(Paths.get(cacheDictionaryFilesPath +  "//cacheStem.txt"));
                        } catch (IOException e1) {
                        }
                    }
                    if(new File(cacheDictionaryFilesPath +  "//dictionary.txt").isFile()) {
                        try {
                            Files.delete(Paths.get(cacheDictionaryFilesPath +  "//dictionary.txt"));
                        } catch (IOException e1) {
                        }
                    }
                    if(new File(cacheDictionaryFilesPath +  "//dictionaryStem.txt").isFile()) {
                        try {
                            Files.delete(Paths.get(cacheDictionaryFilesPath +  "//dictionaryStem.txt"));
                        } catch (IOException e1) {
                        }
                    }
                    engineFrame.remove(background);
                    BufferedImage img = ImageIO.read(getClass().getResourceAsStream("imgs/img.png"));
                    ReadFile.docNumberOfFiles=0;
                    background.setIcon(new ImageIcon(img));
                    engineFrame.add(background);
                    engineFrame.validate();
                    engineFrame.repaint();
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
            BufferedImage img = ImageIO.read(getClass().getResourceAsStream("imgs/img.png"));
            Graphics g = img.getGraphics();
            g.setFont(new Font("Arial",Font.BOLD,12));
            g.setColor(Color.black);
            g.drawString( "Running Time: " + indexer.getIndexRunningTime() + " seconds",230,320);
            ReadFile.docNumberOfFiles=0;
            g.dispose();

            background.setIcon(new ImageIcon(img));
            engineFrame.add(background);
            engineFrame.validate();
            engineFrame.repaint();




        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void drawQueryResultData(int docReturned,long runningTime){

        try {
            engineFrame.remove(background);
            BufferedImage img = ImageIO.read(getClass().getResourceAsStream("imgs/img.png"));
            Graphics g = img.getGraphics();
            g.setFont(new Font("Arial",Font.BOLD,12));
            g.setColor(Color.black);
            g.drawString( "Document Returned: " + docReturned,230,300);
            g.drawString( "Running Time: " + runningTime/(long)1000 + " seconds",230,320);
            ReadFile.docNumberOfFiles=0;
            g.dispose();

            background.setIcon(new ImageIcon(img));
            engineFrame.add(background);
            engineFrame.validate();
            engineFrame.repaint();




        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void mainBackgroundEngine(){
        try {
            JLabel img = new JLabel(new ImageIcon(ImageIO.read(getClass().getResourceAsStream("imgs/img.png"))));
            this.background = img;
            this.background.setLayout(null);
        } catch (IOException e) {
            e.printStackTrace();
        }

            JButton runQueryButton = new JButton("Run");
            runQueryButton.setSize(new Dimension(60,25));
            runQueryButton.setLocation(250,70);
            runQueryButton.setFont(new Font("Arial",Font.ITALIC,12));

            JTextField queryText = new JTextField();
            queryText.setSize(165,25);
            queryText.setLocation(80,70);
            queryText.setText("");
            runQueryButton.setFont(new Font("Arial",Font.ITALIC,12));



            JCheckBox extension = new JCheckBox("Extend Query");
            extension.setSize(100,25);
            extension.setLocation(80,100);
            extension.setFont(new Font("Arial", Font.ITALIC, 11));
            extension.setForeground(Color.WHITE);

            extension.setOpaque(false);

            JCheckBox docSummary = new JCheckBox("Document Summary");
            docSummary.setSize(130,25);
            docSummary.setLocation(80,130);
            docSummary.setFont(new Font("Arial", Font.ITALIC, 11));
            docSummary.setForeground(Color.WHITE);
            docSummary.setOpaque(false);


            this.background.add(runQueryButton);
            this.background.add(queryText);
            this.background.add(extension);
            this.background.add(docSummary);
            this.engineFrame.add(this.background);


            extension.addActionListener(e -> {
                toExtend = extension.isSelected() ? true : false;
                if(toExtend){
                    toSummary = false;
                    docSummary.setSelected(false);
                }
            });
            docSummary.addActionListener(e -> {
                toSummary = docSummary.isSelected() ? true : false;
                if(toSummary){
                    toExtend = false;
                    extension.setSelected(false);
                }
            });

            runQueryButton.addActionListener(e -> {
                if(pathsInitializing.getPostingDir() == null || pathsInitializing.getCorpusDir()==null)
                    JOptionPane.showMessageDialog(engineFrame, "Has to initial posting and corpus dir");
                else if(cache==null || dictionary==null)
                    JOptionPane.showMessageDialog(engineFrame,"Has to upload cache/dictionary to RAM");
                else if(toSummary){
                    this.documentSummaryText.setText("");
                    try{
                        this.documentSummaryText.append(new DocumentSummarize(queryText.getText()).toString());
                    }
                    catch (Exception e2){
                        JOptionPane.showMessageDialog(engineFrame,"No such document exists");
                    }
                }
                else if( cache != null && dictionary!=null && performStemming.isStem() != performStemming.getRamStem()){
                    JOptionPane.showMessageDialog(engineFrame, "Current cache and dictionary not correlate to stem checkbox");
                }
                else if( new File(pathsInitializing.getPostingPath()+"\\Hallelujah" + performStemming.toString() + ".txt")== null)
                    JOptionPane.showMessageDialog(engineFrame,"Non exist posting file at posting dir");
                else if(queryText.getText().equals(""))
                    JOptionPane.showMessageDialog(engineFrame, "Fill the query text field first");
                else if(queryText.getText().split(" ").length>1 && toExtend )
                    JOptionPane.showMessageDialog(engineFrame,"Require just one word for extension");
                else{
                    try{
                        long start = System.currentTimeMillis();
                        List<String> docs = new QuerySearcher(queryText.getText(),toExtend).rankQueryDoc();
                        if(QuerySearcher.queriesResult!=null)
                            QuerySearcher.queriesResult.clear();
                        else
                            QuerySearcher.queriesResult = new HashMap<>();
                        QuerySearcher.queriesResult.put(new Random().nextInt((1000- 100) + 1) + 100,docs);
                        if(docs.size()==0)
                            throw new RuntimeException("not found relevant docs");
                        long finished = System.currentTimeMillis() - start;
                        DisplayQueryPanel dqp = new DisplayQueryPanel(engineFrame,false,queryText.getText(),docs,2);
                        dqp.redo();
                        dqp.setVisible(true);
                        drawQueryResultData(docs.size(),finished);
                    }catch (RuntimeException e1){
                        JOptionPane.showMessageDialog(engineFrame,"Not found result for '" + queryText.getText() + "'");

                    }
                }
            });



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
        JMenuItem runQueriesFile = new JMenuItem("Run Queries File");

        performStemming.addActionListener(e -> {
            boolean ram = this.performStemming.getRamStem();
            this.performStemming = performStemming.isSelected() ? Stemming.True : Stemming.False;
            this.performStemming.setRamStem(ram);
            Indexer.stemming = this.performStemming;
        });

        runQueriesFile.addActionListener(e -> {
            try{
                if(pathsInitializing.getPostingDir() == null || pathsInitializing.getCorpusDir()==null)
                    JOptionPane.showMessageDialog(engineFrame, "Has to initial posting and corpus dir");
                else if(cache==null || dictionary==null)
                    JOptionPane.showMessageDialog(engineFrame,"Has to upload cache/dictionary to RAM");
                else if( cache != null && dictionary!=null && this.performStemming.isStem() != this.performStemming.getRamStem()){
                    JOptionPane.showMessageDialog(engineFrame, "Current cache and dictionary not correlate to stem checkbox");
                }
                else if( new File(pathsInitializing.getPostingPath()+"\\Hallelujah" + this.performStemming.toString() + ".txt")== null)
                    JOptionPane.showMessageDialog(engineFrame,"Non exist posting file at posting dir");
                else{
                    JFileChooser fc = new JFileChooser();
                    if(fc.showOpenDialog(engineFrame) == JFileChooser.APPROVE_OPTION){
                        File file = fc.getSelectedFile();
                        new Thread(() -> {
                            long start = System.currentTimeMillis();
                            runQueriesFile.setEnabled(false);
                            QuerySearcher.addQueriesResult(file.getPath());
                            Map<Integer,List<String>> queriesResult = QuerySearcher.queriesResult;
                            try{
                                File outputFile = new File("tempQuery.txt");
                                PrintWriter pw = new PrintWriter(new FileWriter(outputFile));
                                for(Map.Entry<Integer,List<String>> query :queriesResult.entrySet()){
                                    pw.println("Query ID: " + query.getKey() + '\n');
                                    int counter =1;
                                    for(String document : query.getValue()){
                                        pw.println(counter +". "+ document );
                                        counter+=1;
                                    }
                                    pw.println("------------------------");
                                }
                                pw.flush();
                                java.awt.Desktop.getDesktop().edit(outputFile);
                                runQueriesFile.setEnabled(true);
                                drawQueryResultData(queriesResult.size()*50, System.currentTimeMillis()-start);
                            }
                            catch (IOException e2){
                                e2.printStackTrace();
                            }

                        }).start();

                    }

                }
            }

            catch (Exception e2){
                JOptionPane.showMessageDialog(engineFrame, "Incorrect queries file");
            }

        });


        preferencesMenu.add(performStemming);
        preferencesMenu.add(runQueriesFile);




        return preferencesMenu;
    }

    public enum Stemming{
        True{
            @Override
            public String toString() {
                return "Stem";
            }

            @Override
            public boolean isStem() {
                return true;
            }
        },
        False{
            @Override
            public String toString() {
                return "";
            }

            @Override
            public boolean isStem() {
                return false;
            }
        };
        public abstract boolean isStem();
        public void setRamStem(boolean stem){this.ramStem = stem;}
        public boolean getRamStem(){return this.ramStem;}
        protected boolean ramStem=true;




    }

}
