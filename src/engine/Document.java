package engine;

import java.util.HashMap;

/**
 * The class represents a Document object
 */
public class Document {

    public static final HashMap<String,Document> corpusDocuments = new HashMap<String, Document>();

    private String docName;
    private String fileName;
    private int docLength;
    private int mostFrequentWord;

    private Document(String fileName, String docName) {
        this.docLength = 0;
        this.mostFrequentWord = 0;
        this.docName = docName;
        this.fileName = fileName;
    }

    private Document(String docName, String fileName, int wordsSize, int mostFrequentWord) {
        this.docName = docName;
        this.fileName = fileName;
        this.docLength = wordsSize;
        this.mostFrequentWord = mostFrequentWord;
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
        return fileName+"#"+docName+"#"+ docLength +"#"+mostFrequentWord;
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

    @Override
    public int hashCode() {
        return (fileName + docName).hashCode();
    }
}
