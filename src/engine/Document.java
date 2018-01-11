package engine;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * The class represents a Document object
 */
public class Document {

    public static final Map<String,Document> corpusDocuments = new HashMap<>();

    private Map<String,Double> termInDocWeights =  new HashMap<>();

    private String docName;
    private String fileName;
    private int docLength;
    private int mostFrequentWord;
    private double weight;

    private Document(String fileName, String docName) {
        this.docLength = 0;
        this.mostFrequentWord = 0;
        this.docName = docName;
        this.fileName = fileName;
    }

    private Document(String docName, String fileName, int wordsSize, int mostFrequentWord, double weight) {
        this.docName = docName;
        this.fileName = fileName;
        this.docLength = wordsSize;
        this.mostFrequentWord = mostFrequentWord;
        this.weight = weight;
    }

    public static Document addDocument(String fileName, String docName){
        /**
         * Creates Document if its not already exists and add it to docDictionary
         */
        if(!corpusDocuments.containsKey(fileName+docName)){
            Document document = new Document(fileName,docName);
            corpusDocuments.put(fileName+docName,document);
            return document;
        }
        else{
            return corpusDocuments.get(fileName+docName);
        }
    }

    @Override
    public String toString() {
        return "FileName: " + fileName+" ,DocName: "+docName+" ,DocLength: "+ docLength +" ,MostFrequentWordAppearance: "+mostFrequentWord;
    }


    public String encryptingDocToStr(){
        return fileName+"#"+docName+"#"+ docLength +"#"+mostFrequentWord+"#"+weight;
    }

    public static Document decryptDocFromStr(String str){
        String[] documentData = str.split("#");
        return new Document(documentData[1]/*Document Name*/,documentData[0]/*File Name*/,Integer.parseInt(documentData[2])/*Document Length*/,
                                 Integer.parseInt(documentData[3])/*Most Frequent Word*/,Double.parseDouble(documentData[4]/*Document weight*/));
    }



    public String getDocName() {
        return docName;
    }


    public String getFileName() {
        return fileName;
    }


    public int getDocLength() {
        return docLength;
    }

    public void setDocLength(int docLength) {
        this.docLength = docLength;
    }

    public int getMostFrequentWord() {
        return mostFrequentWord;
    }

    public void setMostFrequentWord(int mostFrequentWord) {
        this.mostFrequentWord = mostFrequentWord;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Document document = (Document) o;

        return this.fileName.equals(document.fileName) && this.docName.equals(document.docName);
    }

    public double getWeight() {
        return weight;
    }

    public void updateDocWeight(String term, double weight) {

        if(termInDocWeights.containsKey(term)){
            double formerWeight = termInDocWeights.remove(term);
            termInDocWeights.put(term,formerWeight + weight);
        }
        else{
            termInDocWeights.put(term,weight);
        }
    }

    @Override
    public int hashCode() {
        return (fileName + docName).hashCode();
    }

    public void writeData(PrintWriter pw) {
        for(double weight: termInDocWeights.values()){
            this.weight += Math.pow(weight/(double)mostFrequentWord,2);
        }
        pw.println(encryptingDocToStr());
        pw.flush();
        termInDocWeights.clear();
    }
}
