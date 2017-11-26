package gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;

public class InitializationPathTable extends JDialog {
    private File corpusDir;
    private File postingDir;

    public InitializationPathTable(Frame owner, boolean modal) {
        super(owner, modal);
        JPanel myPanel = new JPanel(new GridLayout(0,1));
        this.getContentPane().add(myPanel);
        myPanel.add(new JLabel("Path to corpus directory:"));
        Button corpusDirPath = new Button("Browse...");
        myPanel.add(corpusDirPath);
        myPanel.add(new JLabel("Path to posting directory:"));
        Button postingDirPath = new Button("Browse...");
        myPanel.add(postingDirPath);
        JButton cancelButton = new JButton("Cancel");
        myPanel.add(cancelButton);
        JButton okButton = new JButton("Ok");
        myPanel.add(okButton);

        setLocationRelativeTo(owner);
        pack();
        this.setVisible(true);




        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
                System.exit(0);
            }
        });
        okButton.addActionListener(e -> {
            setVisible(false);
            owner.setVisible(true);
        });
        postingDirPath.addActionListener(e -> {
            JFileChooser postingDirPathChooser = new JFileChooser();
            postingDirPathChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            postingDirPathChooser.setName("Posting Dir Path");
            int returnVal = postingDirPathChooser.showOpenDialog(null);
            if(returnVal == JFileChooser.APPROVE_OPTION) {
                File postingDir = postingDirPathChooser.getSelectedFile();
            }
        });
        corpusDirPath.addActionListener(e -> {
            JFileChooser corpusDirPathChooser = new JFileChooser();
            corpusDirPathChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            corpusDirPathChooser.setName("Corpus Dir Path");
            int returnVal =corpusDirPathChooser.showOpenDialog(null);
            if(returnVal == JFileChooser.APPROVE_OPTION) {
                File corpusDir = corpusDirPathChooser.getSelectedFile();
            }
        });
        cancelButton.addActionListener(e -> System.exit(1));
    }
}
