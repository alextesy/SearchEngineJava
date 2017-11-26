package Main;

import java.io.Serializable;

public class Document implements Serializable {
    @Override
    public String toString() {
        return fileName+" "+docName+" "+wordsSize+" "+mostFrequentWord;
    }

    String docName;
    String fileName;
    int wordsSize;
    int mostFrequentWord;

    public Document(String docName, String fileName) {
        this.docName = docName;
        this.fileName = fileName;
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
