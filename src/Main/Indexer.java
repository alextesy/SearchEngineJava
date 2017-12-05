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
    public static Map<String,int[]> Dictionary = new HashMap<>();

    private double indexRunningTime;
    private String pathToCorpus;
    private String pathToPosting;
    private long readFileSize;
    private int counter=0;

    public Indexer(String pathToCorpus, String pathToPosting,long readFileSize) {
        this.readFileSize = readFileSize;
        this.pathToCorpus = pathToCorpus;
        this.pathToPosting = pathToPosting;
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
                    //if(counter == 3 ) break;
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
        mergeSortedFiles(postingFilesList,new File(pathToPosting + "Hallelujah"),cmp);

        long then=System.currentTimeMillis();
        this.indexRunningTime = (then - now)/1000;
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
        BufferedWriter fbw = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(outputFile)));
        long rowCounter = mergeSortedFiles(fbw, cmp, bfbs);
        for (File f : postingFilesList) {
            f.delete();
        }
        return rowCounter;
    }
    private int mergeSortedFiles(BufferedWriter fbw, Comparator<String> cmp, List<BinaryFileBuffer> buffers) throws IOException {
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
                try{
                    if(cmp.compare(rT.getValue(), lastTermLine.getValue()) != 0) {
                        fbw.write(lastTermLine.encryptTermToStr());
                        fbw.newLine();
                        lastTermLine = rT;
                        //TODO - ADD FIELDS IF NEEDED
                        Dictionary.put(rT.getValue(),new int[]{rT.getTermTDF(),rT.getTermIDF(),rowCounter} /* add more fields ,}*/ );
                    }
                    else
                        lastTermLine.termsUnion(rT);
                }
                catch (Exception e){
                    System.out.println("tal");
                    System.out.println(rT);
                    System.out.println(lastTermLine);
                }
                ++rowCounter;
                if (bfb.empty()) {
                    bfb.fbr.close();
                } else {
                    pq.add(bfb); // add it back
                }

            }
        }
        finally {
            fbw.close();
            for (BinaryFileBuffer bfb : pq) {
                bfb.close();
            }
        }
        return  rowCounter;
    }


    private File sortAndSave(List<String> tmpList,Comparator<String> cmp) throws IOException {
        File newTmpFile = new File(pathToPosting +"DocNum" + counter + ".txt");
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
        HashMap<String,Document> corpusDocumentsData = Document.corpusDocuments;
        try {
            PrintWriter corpusDocFile = new PrintWriter(pathToPosting+"documentsData"+ ".txt");
            for(Document doc : corpusDocumentsData.values()){
                corpusDocFile.println(doc);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
   /*
    public static Collection<Term> iterateThroughTermFile(String path, int termLine, int range){
        List<Term> termsFileIteration = new ArrayList<>();
        try (Stream<String> lines = Files.lines(Paths.get(path))) {
            Collection <String> termsString = lines.skip(termLine).limit(range).collect(Collectors.toList());
            for(String str : termsString)
                termsFileIteration.add(Term.decryptTermFromStr(str));
        }catch (Exception e){
            e.printStackTrace();
        }
        return termsFileIteration;
    }
    */
    public void printDictionary(String path){
        try {
            FileOutputStream fout = new FileOutputStream(path + "dictionary");
            ObjectOutputStream oos = new ObjectOutputStream(fout);
            oos.writeObject(Dictionary);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static Map<String,int[]> readDictionary(String path){
        FileInputStream fin = null;
        try {
            fin = new FileInputStream(path +"dictionary");
            ObjectInputStream ois = new ObjectInputStream(fin);
            Dictionary = (Map<String,int[]>) ois.readObject();
            System.out.println(Dictionary.get("TEXT")[1]);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Dictionary;
    }
    public static void buildCache(Map<String,int[]> dict){
        PriorityQueue<Map.Entry<String,int[]>> pq = new PriorityQueue<>(new Comparator<Map.Entry<String, int[]>>() {
            @Override
            public int compare(Map.Entry<String, int[]> o1, Map.Entry<String, int[]> o2) {
                return o1.getValue()[1] - o2.getValue()[1];
            }
        });
        for(Map.Entry<String,int[]> termData : dict.entrySet()){
            pq.add(termData);
        }
        for(int i=0; i<10000; i+=1){
            Map.Entry<String,int[]> bla = pq.poll();
            System.out.println(bla.getKey() + " " + bla.getValue()[0]);
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
