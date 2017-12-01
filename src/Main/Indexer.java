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
                    if(counter == 1 ) break;
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
        mergeSortedFiles(postingFilesList,new File("C:\\Users\\אלי\\doc\\Hallelujah"),cmp);

        long then=System.currentTimeMillis();
        this.indexRunningTime = ((then - now)/1000)/60;
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
    private long mergeSortedFiles(BufferedWriter fbw, Comparator<String> cmp, List<BinaryFileBuffer> buffers) throws IOException {
        PriorityQueue<BinaryFileBuffer> pq = new PriorityQueue<>(
                11, (i, j) -> cmp.compare(i.peek(), j.peek()));
        for (BinaryFileBuffer bfb : buffers) {
            if (!bfb.empty()) {
                pq.add(bfb);
            }
        }
        long rowCounter = 0;
        try {
            String lastLine = null;
            if(pq.size() > 0) {
                BinaryFileBuffer bfb = pq.poll();
                lastLine = bfb.pop();
                fbw.write(lastLine);
                fbw.newLine();
                ++rowCounter;
                if (bfb.empty())
                    bfb.fbr.close();
                else
                    pq.add(bfb); // add it back
            }
            while (pq.size() > 0) {
                BinaryFileBuffer bfb = pq.poll();
                String r = bfb.pop();
                // Skip duplicate lines
                if  (cmp.compare(r, lastLine) != 0) {
                    fbw.write(r);
                    fbw.newLine();
                    lastLine = r;
                }
                ++rowCounter;
                if (bfb.empty())
                    bfb.fbr.close();
                 else
                    pq.add(bfb); // add it back

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
        File newTmpFile = new File("C:\\Users\\אלי\\doc\\DocNum" + counter + ".txt");
        tmpList = tmpList.parallelStream().sorted(cmp).collect(Collectors.toCollection(ArrayList<String>::new));
        OutputStream out = new FileOutputStream(newTmpFile);
        BufferedWriter fbw = new BufferedWriter(new OutputStreamWriter(out));
        for (String s : tmpList) {
            fbw.write(s);
            fbw.newLine();
        }
        return newTmpFile;
    }

    private void writeDocumentData(){
        HashMap<String,Document> corpusDocumentsData = Document.corpusDocuments;
        try {
            PrintWriter corpusDocFile = new PrintWriter("d:\\documents\\users\\talbense\\Documents\\blabla\\documentsData"+ ".txt");
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
