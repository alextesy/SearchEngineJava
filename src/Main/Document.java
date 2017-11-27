package Main;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class Document implements Serializable {

    private static final HashMap<String,Document> corpusDocuments = new HashMap<String, Document>();

    String docName;
    String fileName;
    int wordsSize;
    int mostFrequentWord;

    private Document(String fileName, String docName) {
        this.wordsSize=0;
        this.mostFrequentWord=0;
        this.docName = docName;
        this.fileName = fileName;
    }

    public static Document addDocument(String fileName,String docName){
        Document document = new Document(docName,fileName);
        corpusDocuments.put(fileName+docName,document);
        return document;
    }
    /*
    @Override
    public int hashCode() {
        return (docName+fileName).hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof Document){
            Document doc = (Document)obj;
            return doc.hashCode() == this.hashCode();
        }
        return false;
    }
    */

    @Override
    public String toString() {
        return fileName+" "+docName+" "+wordsSize+" "+mostFrequentWord;
    }

    /* Serializable Implementation */
    public Document(){};

    public String getDocName() {
        return docName;
    }

    public void setDocName(String docName) {
        this.docName = docName;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
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
