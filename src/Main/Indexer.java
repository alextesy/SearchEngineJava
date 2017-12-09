package Main;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class Indexer {
    public static final Map<String,Term> currentTermDictionary = new HashMap<>();
    public static final long CORPUS_BYTE_SIZE = 1578400481; // 1.47 - GB - 1.47*2^30 bytes
    public final Map<String,Term> cacheDictionary = new HashMap<>();
    public Map<String,long[]> Dictionary = new HashMap<>();

    private double indexRunningTime;
    private String pathToCorpus;
    private String pathToPosting;
    private long readFileSize;
    private int counter=0;
    public static boolean stemming;

    public Indexer(String pathToCorpus, String pathToPosting,long readFileSize,boolean stemming) {
        this.readFileSize = readFileSize;
        this.pathToCorpus = pathToCorpus;
        this.pathToPosting = pathToPosting;
        this.stemming=stemming;
    }


    public void toIndex() throws IOException {
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
                    if(counter == 2 ) break;
                    currentSize+=ReadFile.readTextFile(child);
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
        catch (IOException e){
            e.printStackTrace();
        }
        mergeSortedFiles(postingFilesList,new File(pathToPosting + "\\Hallelujah.txt"),cmp);

        long then=System.currentTimeMillis();
        this.indexRunningTime = (then - now)/1000;

        //findCacheTerms();
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
                    long startPos = raf.getFilePointer();
                    raf.writeBytes(lastTermLine.encryptTermToStr());
                    raf.writeBytes("\n");
                    //TODO - ADD FIELDS IF NEEDED
                    Dictionary.put(lastTermLine.getValue(),new long[]{lastTermLine.getTermTDF(),lastTermLine.getTermIDF(),startPos} /* add more fields ,}*/ );
                    lastTermLine = rT;
                }
                else
                     lastTermLine.termsUnion(rT);

                ++rowCounter;
                if (bfb.empty()) {
                    bfb.fbr.close();
                } else {
                    pq.add(bfb); // add it back
                }

            }
        }
        finally {
            raf.close();
            for (BinaryFileBuffer bfb : pq) {
                bfb.close();
            }
        }
        return  rowCounter;
    }

    /*
    private void check (){
        try {
            RandomAccessFile raf = new RandomAccessFile(new File("C:\\Users\\אלי\\doc\\Hallelujah.txt"),"r");

            raf.seek(Dictionary.get("zoo")[2]);
            String str = raf.readLine();
            Term term = Term.decryptTermFromStr(raf.readLine());

        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    */

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

    private void writeDocumentData(){
        try {
            PrintWriter corpusDocFile = new PrintWriter(pathToPosting+"\\documentsData"+ ".txt");
            for(Document doc : Document.corpusDocuments.values()){
                corpusDocFile.println(doc);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void writeDictionary(String path){
        try {
            FileOutputStream fout = new FileOutputStream(path + "\\dictionary");
            ObjectOutputStream oos = new ObjectOutputStream(fout);
            oos.writeObject(Dictionary);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    /*
    public Map<String,int[]> readDictionary(String path){
        try {
            FileInputStream fin = new FileInputStream(path +"\\dictionary");
            ObjectInputStream ois = new ObjectInputStream(fin);
            Dictionary = (Map<String,int[]>) ois.readObject();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Dictionary;
    }
    */
    private void findCacheTerms(){
        PriorityQueue<Map.Entry<String,long[]>> pq = new PriorityQueue<>((o1, o2) -> Long.compare(o2.getValue()[0], o1.getValue()[0]));
        for(Map.Entry<String,long[]> termData : Dictionary.entrySet()){
            pq.add(termData);
        }
        try {
            PrintWriter cacheFile = new PrintWriter("cacheWords.txt");

            for (int i = 0; i < 10000; i += 1) {
                Map.Entry<String, long[]> freTerm = pq.poll();
                cacheFile.println(freTerm.getKey());
            }
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public double getIndexRunningTime() {
        return this.indexRunningTime;
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
