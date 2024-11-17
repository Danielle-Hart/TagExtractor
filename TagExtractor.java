import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.*;


public class TagExtractor extends JFrame {
    private JTextArea TextArea;
    private JTextField inputFileField, stopWordsFileField, outputFileField;
    private Map<String, Integer> tagFrequencyMap;

    public TagExtractor() {
        super("Tag Extractor");
        tagFrequencyMap = new HashMap<>();
        GUI();
    }
    private void GUI() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600,400);
        setLayout(new GridLayout());

        //MAIN PANEL
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(4,1));

        //INPUT FILE
        JPanel inputFilePanel = new JPanel(new FlowLayout());
        inputFileField = new JTextField(30);
        JButton inputFileButton = new JButton("Open File");
        inputFileButton.addActionListener(e -> selectFile (inputFileField));
        inputFilePanel.add(new JLabel("File Name:"));
        inputFilePanel.add(inputFileField);
        inputFilePanel.add(inputFileButton);

        //STOP WORDS FILE
        JPanel stopWordsFilePanel = new JPanel(new FlowLayout());
        stopWordsFileField = new JTextField(30);
        JButton stopWordsFileButton = new JButton("Select Stop Words");
        stopWordsFileButton.addActionListener(e -> selectFile (stopWordsFileField));
        stopWordsFilePanel.add(new JLabel("Stop Words File:"));
        stopWordsFilePanel.add(stopWordsFileField);
        stopWordsFilePanel.add(stopWordsFileButton);

        //OUTPUT FILE
        JPanel outputFilePanel = new JPanel(new FlowLayout());
        outputFileField = new JTextField(30);
        JButton outputFileButton = new JButton("Save File");
        outputFileButton.addActionListener(e -> saveOutput());
        outputFilePanel.add(new JLabel("Output File:"));
        outputFilePanel.add(outputFileField);
        outputFilePanel.add(outputFileButton);

        //BUTTONS
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton extractButton = new JButton("Extract Tags");
        extractButton.addActionListener(e -> extractTags());
        JButton clearButton = new JButton("Clear Tags");
        clearButton.addActionListener(e -> clearFields());
        buttonPanel.add(extractButton);
        buttonPanel.add(clearButton);

        //TEXT AREA
        TextArea = new JTextArea(10,50);
        TextArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(TextArea);

        //PANEL COMPONETS
        panel.add(inputFilePanel);
        panel.add(stopWordsFilePanel);
        panel.add(outputFilePanel);
        panel.add(buttonPanel);

        //FRAME PANELS
        add(panel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        setVisible(true);
    }
    private void selectFile(JTextField inputFileField) {
        JFileChooser fileChooser = new JFileChooser();
        int option = fileChooser.showOpenDialog(this);
        if (option == JFileChooser.APPROVE_OPTION) {
            stopWordsFileField.setText(fileChooser.getSelectedFile().getAbsolutePath());
        }
    }
    private void extractTags() {
        String inputFilePath = inputFileField.getText();
        String stopWordsFilePath = stopWordsFileField.getText();

        if (inputFilePath.isEmpty() || stopWordsFilePath.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select a text file and a stop word file","Error",JOptionPane.ERROR_MESSAGE);
            return;
        }
        try{
            Set<String> stopWords = loadStopWords (stopWordsFilePath);
            tagFrequencyMap = analyzeText (inputFilePath, stopWords);
            displayResults();
        }catch (IOException e){
            JOptionPane.showMessageDialog(this, "Error processing files" + e.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);
        }
    }
    private Set<String> loadStopWords(String filePath) throws IOException {
        Set<String> stopWords = new HashSet<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                stopWords.add(line.trim().toLowerCase());
            }
        }
        return stopWords;
    }
    private Map<String, Integer> analyzeText (String filePath,Set<String> stopWords) throws IOException {
        Map<String, Integer> tagFrequencyMap = new HashMap<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] words = line.toLowerCase().replaceAll("[a-zA-Z ]", "").split("\\s+");
                for (String word : words) {
                    if (!stopWords.contains(word) && !word.isEmpty()) {
                        tagFrequencyMap.put(word, tagFrequencyMap.getOrDefault(word, 0) + 1);
                    }
                }
            }
        }
        return tagFrequencyMap;
    }
    private void displayResults() {
        TextArea.setText("");
        tagFrequencyMap.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .forEach(entry -> TextArea.append(entry.getKey() + ": " + entry.getValue() + "\n"));
    }
    private void saveOutput() {
        String outputFilePath = outputFileField.getText();
        if (outputFilePath.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please specify an output file","Error",JOptionPane.ERROR_MESSAGE);
            return;
        }
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFilePath))){
            for (Map.Entry<String, Integer> entry : tagFrequencyMap.entrySet()) {
                writer.write(entry.getKey() + ": " + entry.getValue());
                writer.newLine();
            }
            JOptionPane.showMessageDialog(this, "Saved to " + outputFilePath,"Success",JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException e){
            JOptionPane.showMessageDialog(this, "Error saving files" + e.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);
        }
    }
    private void clearFields() {
        inputFileField.setText("");
        stopWordsFileField.setText("");
        outputFileField.setText("");
        TextArea.setText("");
        tagFrequencyMap.clear();
    }
    public static void main(String[] args) {
        SwingUtilities.invokeLater(TagExtractor::new);
    }
}
