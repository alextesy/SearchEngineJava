package query;
import engine.Indexer;
import engine.Parse;
import engine.ReadFile;
import javafx.util.Pair;
import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import static gui.EngineMenu.Stemming.False;

/**
 * A class that represents DocumentSummarize object which can extract the top 5 sentences out of a document
 */
public class DocumentSummarize {
    private String docContent;
    private int position=0;
    private Map<String,Pair<Integer,ArrayList<String>>> sentenceTerms;
    private Map<String,ArrayList<Double>> termsData;
    private Map<String,Double> sentenceRank;
    private int maxWordFreq;
    private String currentSentence;
    private int currentSentenceNum;

    public DocumentSummarize(String docNum){
        this.docContent=getDocContent(docNum);   //getDocContent(docNum);
        sentenceTerms=new HashMap<String,Pair<Integer, ArrayList<String>>>();
        currentSentenceNum=0;
        maxWordFreq=0;
        sentenceRank=new HashMap<>();
        termsData=new HashMap<>();
        getSentences();
        setSetntenceWeight();

    }

    /**
     * Returns Docs Content - TEXT
     * @param docNum
     * @return
     */
    private String getDocContent(String docNum){
        String fileName="";
        InputStream file =getClass().getResourceAsStream("..//engine//docs//documentData.txt") ;
        Scanner in = null;
        try {
            in = new Scanner(file);
            while(in.hasNext())
            {
                String line=in.nextLine();
                if(line.contains(docNum)) {
                    fileName = line.substring(0, line.indexOf("#"));
                    break;
                }
            }

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        File docFileDir= new File(Indexer.pathToCorpus+ "\\" +fileName);
        File[] docFile=docFileDir.listFiles();
        String content= null;
        try {
            content = ReadFile.getContent(docFile[0]);
            int index=content.indexOf(docNum);
            Pattern pattern = Pattern.compile("<TEXT>(.*?)</TEXT>",Pattern.DOTALL);
            Matcher m = pattern.matcher(content);
            String textCont="";
            if(m.find(index))
                textCont= m.group();
            return textCont;
        } catch (IOException e) {
            e.printStackTrace();
        }



        return "a";
    }

    /**
     * The function fills a map, where the key is the sentence and the value is a pair - <sentence index, list of terms that conatins the sentence>
     */
    private void getSentences(){
        String[] sentences=docContent.split("\\.");
        for (String sentence:sentences) {
            currentSentence=sentence;
            currentSentenceNum++;
            sentenceTerms.put(currentSentence,new Pair<>(currentSentenceNum,new ArrayList<>()));
            new Parse(sentence, null, False, null,this).Parse();
        }
    }


    /**
     * The Function adds the Term to a specific sentence
     * Called by parser
     * @param term
     */
    public void addSentenceTerm(String term){
        if(termsData.containsKey(term)){
            termsData.get(term).add((double)currentSentenceNum);
        }
        else {
            termsData.put(term, new ArrayList<>());
            termsData.get(term).add((double)currentSentenceNum);
        }
        if(termsData.get(term).size()>maxWordFreq)
            maxWordFreq=termsData.get(term).size();
        position++;
        sentenceTerms.get(currentSentence).getValue().add(term);
    }

    /**
     * gets weight of term in the DOC
     * @param term
     * @return
     */
    private double getTermWeight(String term){
        double weight=0;
        int first20Sent=0;
        int halfWords=0;
        ArrayList<Double> positions=termsData.get(term);
        int tf=positions.size()/maxWordFreq;
        if(positions.get(0)<20)
            first20Sent=1;
        if(positions.size()>maxWordFreq/2)
            halfWords=1;
        weight=tf*0.5+first20Sent*0.25+halfWords*0.25;
        return weight;
    }

    /**
     * Fills up sentence weight in the sentenceRank Map
     */
    private void setSetntenceWeight(){
        double weight=0;
        for (Map.Entry<String,Pair<Integer,ArrayList<String>>> terms:sentenceTerms.entrySet()) {
            ArrayList<String> termList=terms.getValue().getValue();
            for (String term:termList) {
                weight+=getTermWeight(term)/terms.getValue().getValue().size();
            }
            sentenceRank.put(terms.getKey(),weight);
        }
    }

    /**
     * Gets top 5 Sentences in the File
     * @return
     */
    public List<Pair<String,Integer>> getTop5Sentences(){
        PriorityQueue<Map.Entry<String,Double>> pq = new PriorityQueue<>((o1, o2) ->Double.compare(o2.getValue(), o1.getValue()));
        for (Map.Entry<String,Double> d:sentenceRank.entrySet()){
            pq.add(d);
        }
        Map<String,Integer> rankedSentences=new HashMap<>();
        int pqSize=pq.size();
        for(int i=0;i<5&&i<pqSize;i++){
            rankedSentences.put(pq.peek().getKey(),i+1);
            pq.poll();
        }
        List<Pair<String,Integer>> sentenceChronology=new ArrayList<>();

        PriorityQueue<Map.Entry<String,Integer>> pqChronology = new PriorityQueue<>((o1, o2) ->sentenceTerms.get(o1.getKey()).getKey()-sentenceTerms.get(o2.getKey()).getKey());
        for (Map.Entry<String,Integer> sentence:rankedSentences.entrySet()){
           // sentenceTerms.get(alex)
            pqChronology.add(sentence);
        }

        int pqChronologySize=pqChronology.size();
        for(int i=0;i<5&&i<pqChronologySize;i++){
            sentenceChronology.add(new Pair<>(pqChronology.peek().getKey(),pqChronology.peek().getValue()));
            pqChronology.poll();
        }
        return sentenceChronology;
    }


    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        List<Pair<String,Integer>> tal= getTop5Sentences();
        for (Pair<String,Integer> sent:tal) {
            str.append("Rank: "+sent.getValue()+"\n"+ sent.getKey()+"\n");
        }
        return str.toString();
    }
}
