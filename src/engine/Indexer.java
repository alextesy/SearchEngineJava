package engine;

import gui.EngineMenu;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;
import gui.EngineMenu.Stemming;

/**
 * The most significant class of the project that operates over the whole thing.
 */
public class Indexer {

    public static final Map<String,Term> currentTermDictionary = new HashMap<>();
    public static final long CORPUS_BYTE_SIZE = 1578400481; // 1.47 - GB - 1.47*2^30 bytes
    public static Map<String,Double> termsIDF;

    public static Map<String,Object[]> Dictionary = new HashMap<>();

    public static Map<String,Term> cacheTerms;
    private long indexSize = 0;
    private long cacheSize = 0;
    private double indexRunningTime;
    public static String pathToCorpus;
    public static String pathToPosting;
    private long readFileSize;
    private int counter = 0;
    public static Stemming stemming = Stemming.True;


    public Indexer(String pathToCorpus, String pathToPosting,long readFileSize) {
        this.readFileSize = readFileSize;
        this.pathToCorpus = pathToCorpus;
        this.pathToPosting = pathToPosting;
    }

    public static void setStemming(Stemming toStem){
        stemming = toStem;
    }
    public static Map<String,Term> initCacheStrings() {
        Map<String,Term> termsSet = new HashMap<>();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(Indexer.class.getResourceAsStream(("docs/cacheWords"+ stemming.toString() +".txt"))))) {
            String term;
            while ((term = br.readLine()) != null) {
                termsSet.put(term,null);
            }
            br.close();
            return termsSet;
        }

        catch (EOFException e1){
            // do nothing
        }
        catch (IOException e2){
            e2.printStackTrace();
        }
        return null;
    }


    public void toIndex() throws IOException {
        /**
         * The Function iterates over the files in corpus and creates temporary posting files, after this it merges the temporary posting files into a Final posting file and creates dictionary and cache
         */
        //termsIDF = readIDFDictionaryToMem("",stemming);
        long now=System.currentTimeMillis();
        File dir = new File(this.pathToCorpus);
        File[] directoryListing = dir.listFiles();
        List<String> tmpList = new ArrayList<>();
        List<File> postingFilesList = new ArrayList<>();
        Comparator<String> cmp = String::compareTo;
        long currentSize=0;
        try{
            if (directoryListing != null) {
                for (File child : directoryListing) {
                    if(!child.getName().equals("stop_words.txt")){
                        //if(counter == 5 ) break;
                        currentSize+=ReadFile.readTextFile(child,stemming);
                        if (currentSize > readFileSize) {
                            currentSize=0;
                            counter += 1;
                            for (Term term : currentTermDictionary.values())
                                tmpList.add(term.encryptTermToStr());
                            postingFilesList.add(sortAndSave(tmpList,cmp));
                            tmpList.clear();
                            currentTermDictionary.clear();
                        }
                    }
                }
            }
        }
        catch (IOException e){
            e.printStackTrace();
        }





        mergeSortedFiles(postingFilesList,new File(pathToPosting + "\\Hallelujah" +stemming.toString()+ ".txt"),cmp);


        //findCacheTerms();
        //writeDocumentIDF("",stemming,Dictionary);
        //writeDocumentData();
        Document.corpusDocuments.clear();

        long then=System.currentTimeMillis();
        this.indexRunningTime = (then - now)/1000;

    }

    private void writeDocumentData() {
        try {
            PrintWriter documentData = new PrintWriter("documentData" + stemming.toString() + ".txt");
            for(Document doc : Document.corpusDocuments.values()){
                documentData.println(doc.encryptingDocToStr());
            }
            documentData.close();
        }
        catch(IOException e){

        }
    }


    private long mergeSortedFiles(List<File> postingFilesList,File outputFile,Comparator<String> cmp) throws IOException {
        ArrayList<BinaryFileBuffer> bfbs = new ArrayList<>();
        for (File f : postingFilesList) {
            InputStream in = new FileInputStream(f);
            BufferedReader br;
            br = new BufferedReader(new InputStreamReader(in));
            BinaryFileBuffer bfb = new BinaryFileBuffer(br);
            bfbs.add(bfb);
        }
        RandomAccessFile raf = new RandomAccessFile(outputFile,"rw");
        long rowCounter = mergeSortedFiles(raf, cmp, bfbs);
        for (File f : postingFilesList) {
            f.delete();
        }
        return rowCounter;
    }
    private int mergeSortedFiles(RandomAccessFile raf, Comparator<String> cmp, List<BinaryFileBuffer> buffers) throws IOException {
        PriorityQueue<BinaryFileBuffer> pq = new PriorityQueue<>(
                30, (i, j) -> cmp.compare(i.peek(), j.peek()));
        for (BinaryFileBuffer bfb : buffers) {
            if (!bfb.empty()) {
                pq.add(bfb);
            }
        }
        int rowCounter = 0;
        try {
            Term lastTermLine = null;
            String lastLine;
            if(pq.size() > 0) {
                BinaryFileBuffer bfb = pq.poll();
                lastLine = bfb.pop();
                lastTermLine = Term.decryptTermFromStr(lastLine);
                ++rowCounter;
                if (bfb.empty())
                    bfb.fbr.close();
                else
                    pq.add(bfb); // add it back
            }
            while (pq.size() > 0) {
                BinaryFileBuffer bfb = pq.poll();
                String r = bfb.pop();
                Term rT = Term.decryptTermFromStr(r);
                if(cmp.compare(rT.getValue(), lastTermLine.getValue()) != 0) {
                    if(lastTermLine.getTermTDF()>3){
                        long startPos = raf.getFilePointer();
                        raf.writeBytes(lastTermLine.encryptTermToStr());
                        raf.writeBytes("\n");

                        if(cacheTerms.containsKey(lastTermLine.getValue())){
                            cacheSize += (raf.getFilePointer() - startPos)/4;
                            Dictionary.put(lastTermLine.getValue(),new Object[]{lastTermLine.getTermTDF(),lastTermLine.getTermIDF(),'C'+Long.toString(startPos)} /* add more fields ,}*/ );
                            lastTermLine = lastTermLine.termsSub(lastTermLine.getPopularDocs());
                            cacheTerms.put(lastTermLine.getValue(),lastTermLine);

                        }
                        else{
                            Dictionary.put(lastTermLine.getValue(),new Object[]{lastTermLine.getTermTDF(),lastTermLine.getTermIDF(),'P'+Long.toString(startPos)} /* add more fields ,}*/ );
                        }

                        ++rowCounter;
                    }
                    lastTermLine = rT;
                }
                else
                    lastTermLine.termsUnion(rT);

                if (bfb.empty()) {
                    bfb.fbr.close();
                } else {
                    pq.add(bfb); // add it back
                }

            }
            indexSize = raf.getFilePointer();
        }
        finally {
            raf.close();
            for (BinaryFileBuffer bfb : pq) {
                bfb.close();
            }
        }
        return  rowCounter;
    }

    private File sortAndSave(List<String> tmpList,Comparator<String> cmp) throws IOException {
        File newTmpFile = new File(pathToPosting +"\\DocNum" + counter + ".txt");
        tmpList = tmpList.parallelStream().sorted(cmp).collect(Collectors.toCollection(ArrayList::new));
        OutputStream out = new FileOutputStream(newTmpFile);
        BufferedWriter fbw = new BufferedWriter(new OutputStreamWriter(out));
        for (String s : tmpList) {
            fbw.write(s);
            fbw.newLine();
        }
        fbw.flush();
        return newTmpFile;
    }



    public static void writeDictionary(String path,Map<String,Object[]> dictionary){
        try {
            FileOutputStream fout = new FileOutputStream(path);
            ObjectOutputStream oos = new ObjectOutputStream(fout);
            oos.writeObject(dictionary);
            oos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void writeCache(String path, Map<String,Term> cacheTerms) {
        try {
            PrintWriter cacheFiles = new PrintWriter(path);
            for(Term term : cacheTerms.values()){
                cacheFiles.println(term.encryptTermToStr());
            }
            cacheFiles.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static Map<String,Object[]> readDictionary(String path) throws Exception{
        FileInputStream fin = new FileInputStream(path);
        ObjectInputStream ois = new ObjectInputStream(fin);
        Map<String,Object[]> map =  (Map<String,Object[]>)ois.readObject();
        fin.close();
        return map;
    }
    public static Map<String,Term> readCache(String path) throws Exception{
        Map<String,Term> cache = new HashMap<>();
        BufferedReader br = new BufferedReader(new FileReader(path));
        String line = br.readLine();
        while (line != null) {
            Term term = Term.decryptTermFromStr(line);
            cache.put(term.getValue(),term);
            line = br.readLine();
        }
        br.close();
        return cache;
     }
    public static void writeDocumentIDF(String path ,boolean stemming,Map<String,Object[]> dictionary){
        try {
            String stemString = stemming == true ? "Stem" : "";
            PrintWriter pw = new PrintWriter(path + "termsIDF" + stemString+ ".txt");
            for(Map.Entry<String,Object[]> term : dictionary.entrySet())
                pw.println(term.getKey()+"#"+term.getValue()[1]);
            pw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    /* read the IDF file and returns dictionary with the IDF value per term*/
    public static Map<String,Double> readIDFDictionaryToMem(String path,boolean stemming) {
        try {
            Map<String, Double> idfDictionary = new HashMap<>();
            String stemString = stemming == true ? "Stem" : "";
            InputStream in = Indexer.class.getResourceAsStream(path + "docs/termsIDF" + stemString + ".txt");
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String line = br.readLine();
            while (line != null) {
                String[] data = line.split("#");
                idfDictionary.put(data[0], Double.parseDouble(data[1]));
                line = br.readLine();
            }
            br.close();
            return idfDictionary;
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }



    public long getCacheSize() {
        return cacheSize;
    }

    private void findCacheTerms(){
        PriorityQueue<Map.Entry<String,Object[]>> pq = new PriorityQueue<>((o1, o2) -> Long.compare((long)o2.getValue()[0], (long)o1.getValue()[0]));
        for(Map.Entry<String,Object[]> termData : Dictionary.entrySet()){
            pq.add(termData);
        }
        try {
            PrintWriter cacheFile = new PrintWriter("cacheWords" +stemming.toString() +".txt");

            for (int i = 0; i < 10000; i += 1) {
                Map.Entry<String, Object[]> freTerm = pq.poll();
                cacheFile.println(freTerm.getKey());
            }
            cacheFile.close();
        }

        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
    public void clear(){
        cacheTerms.clear();
        Dictionary.clear();
    }

    public double getIndexRunningTime() {
        return this.indexRunningTime;
    }

    public long getIndexSize() {
        return indexSize;
    }

    final class BinaryFileBuffer {
        public BinaryFileBuffer(BufferedReader r) throws IOException {
            this.fbr = r;
            reload();
        }

        public void close() throws IOException {
            this.fbr.close();
        }

        public boolean empty() {
            return this.cache == null;
        }

        public String peek() {
            return this.cache;
        }

        public String pop() throws IOException {
            String answer = peek().toString();// make a copy
            reload();
            return answer;
        }

        private void reload() throws IOException {
            this.cache = this.fbr.readLine();
        }

        public BufferedReader fbr;

        private String cache;
    }
}


