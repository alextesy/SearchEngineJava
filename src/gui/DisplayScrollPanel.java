package gui;


import Main.Term;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class DisplayScrollPanel extends JDialog{

    protected final DataModel model;
    protected final JScrollPane scrollPane;
    private static final Dimension DICTIONARY_PANEL_DIMENSION = new Dimension(200,600);

    public DisplayScrollPanel(Frame owner, boolean modal ,String title){
        super(owner,modal);
        this.setTitle(title);
        this.model = new DataModel();
        JTable table = new JTable(model);
        table.setRowHeight(20);
        this.scrollPane = new JScrollPane(table);
        this.scrollPane.setColumnHeaderView(table.getTableHeader());
        this.setSize(DICTIONARY_PANEL_DIMENSION);
        this.add(scrollPane, BorderLayout.CENTER);
        this.setVisible(false);
        setLocationRelativeTo(owner);
    }

    public abstract void redo();



    private static class DataModel extends DefaultTableModel{
        private List<Row> values;
        private static final String[] NAMES = {"Term", "TermTDF"};

        public DataModel() { this.values = new ArrayList<>();}
        public void clear(){
            this.values.clear();
            setRowCount(0);
        }

        @Override
        public int getRowCount() {
            return values==null ? 0 : values.size();
        }

        @Override
        public int getColumnCount() {
            return this.NAMES.length;
        }

        @Override
        public Object getValueAt(int row, int column) {
            if(column==0)
                return values.get(row).getTermValue();
            else if(column==1)
                return values.get(row).getTermTDF();
            return null;
        }

        @Override
        public void setValueAt(Object aValue, int row, int column) {
            Row currentRow;
            if(row < this.values.size())
                currentRow = this.values.get(row);
            else{
                currentRow = new Row();
                this.values.add(currentRow);
            }
            if(column==0){
                currentRow.setTermValue((String)aValue);
                fireTableRowsInserted(row,row);
            }
            else if(column==1){
                currentRow.setTermTDF(aValue.toString());
                fireTableCellUpdated(row,column);
            }
            else{
                throw new RuntimeException("no such column exists");
            }
        }

        @Override
        public String getColumnName(int column) {
            if(column<NAMES.length)
                return NAMES[column];
            return null;
        }
    }

    private static class Row{
        private String termValue;
        private String termTDF;


        public Row(){}

        public String getTermValue() {
            return termValue;
        }
        public void setTermValue(String termValue) {
            this.termValue = termValue;
        }
        public String getTermTDF() {
            return termTDF;
        }
        public void setTermTDF(String termTDF) {
            this.termTDF = termTDF;
        }
    }
    public static class DisplayDictionaryPanel extends DisplayScrollPanel{
        private Map<String,Object[]> dictionary;

        public DisplayDictionaryPanel(Frame owner, boolean modal,Map<String,Object[]> dictionary) {
            super(owner, modal,"Dictionary Panel");
            this.dictionary = dictionary;
        }

        @Override
        public void redo() {
            int currentRow = 0;
            this.model.clear();
            for(Map.Entry<String,Object[]> term : dictionary.entrySet()){
                this.model.setValueAt(term.getKey(),currentRow,0);
                this.model.setValueAt(term.getValue()[0],currentRow,1);
                currentRow+=1;
            }
            JScrollBar vertical = scrollPane.getVerticalScrollBar();
            vertical.setValue(vertical.getMaximum());
        }

        public void setDictionary(Map<String, Object[]> dictionary) {
            this.dictionary = dictionary;
        }
    }
    public static class DisplayCachePanel extends DisplayScrollPanel{
        private Map<String, Term> cache;

        public DisplayCachePanel(Frame owner, boolean modal, Map<String, Term> cache) {
            super(owner, modal, "Cache Panel");
            this.cache = cache;
        }

        @Override
        public void redo() {
            int currentRow = 0;
            this.model.clear();
            for(Term term : cache.values()){
                this.model.setValueAt(term.getValue(),currentRow,0);
                this.model.setValueAt(term.getTermTDF(),currentRow,1);
                currentRow+=1;
            }
            JScrollBar vertical = scrollPane.getVerticalScrollBar();
            vertical.setValue(vertical.getMaximum());
        }

        public void setCache(Map<String, Term> cache) {
            this.cache = cache;
        }
    }

}