package Main;

import java.util.HashMap;

public class Document {

    public static final HashMap<String,Document> corpusDocuments = new HashMap<String, Document>();

    private String docName;
    private String fileName;
    private int wordsSize;
    private int mostFrequentWord;

    private Document(String fileName, String docName) {
        this.wordsSize = 0;
        this.mostFrequentWord = 0;
        this.docName = docName;
        this.fileName = fileName;
    }

    private Document(String docName, String fileName, int wordsSize, int mostFrequentWord) {
        this.docName = docName;
        this.fileName = fileName;
        this.wordsSize = wordsSize;
        this.mostFrequentWord = mostFrequentWord;
    }

    public static Document addDocument(String fileName, String docName){
        if(!corpusDocuments.containsKey(fileName+docName)){
            Document document = new Document(fileName,docName);
            corpusDocuments.put(fileName+docName,document);
            return document;
        }
        else{
            return new Document(fileName,docName);
        }
    }

    @Override
    public String toString() {
        return "FileName: " + fileName+" ,DocName: "+docName+" ,DocLength: "+wordsSize+" , MostFrequentWordAppearance: "+mostFrequentWord;
    }

    public String encryptingDocToStr(){
        return fileName+"#"+docName+"#"+wordsSize+"#"+mostFrequentWord;
    }

    public static Document decryptDocFromStr(String str){
        String[] documentData = str.split("#");
        return new Document(documentData[0],documentData[1],Integer.parseInt(documentData[2]),Integer.parseInt(documentData[3]));
    }


    public String getDocName() {
        return docName;
    }


    public String getFileName() {
        return fileName;
    }


    public int getWordsSize() {
        return wordsSize;
    }

    public void setWordsSize(int wordsSize) {
        this.wordsSize = wordsSize;
    }

    public int getMostFrequentWord() {
        return mostFrequentWord;
    }

    public void setMostFrequentWord(int mostFrequentWord) {
        this.mostFrequentWord = mostFrequentWord;
    }
}
