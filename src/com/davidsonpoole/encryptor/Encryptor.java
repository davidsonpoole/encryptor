package com.davidsonpoole.encryptor;


import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Encryptor extends JFrame{
    private String filename, dir;

    private JButton open = new JButton("Open");

    public Encryptor() {
        JPanel p = new JPanel();
        open.addActionListener(new OpenL());
        p.add(open);
        Container cp = getContentPane();
        cp.add(p, BorderLayout.SOUTH);
        p = new JPanel();
        p.setLayout(new GridLayout(2, 1));
        JLabel title = new JLabel("Welcome to Davidson's Encryptor!");
        JLabel subtitle = new JLabel("Choose a file to encrypt:");
        p.add(title);
        p.add(subtitle);
        cp.add(p, BorderLayout.NORTH);
        this.setTitle("Davidson's Encryptor");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setSize(250, 110);
        this.setVisible(true);
    }

    class OpenL implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            JFileChooser c = new JFileChooser();
            int dialog = c.showOpenDialog(Encryptor.this);
            if (dialog == JFileChooser.APPROVE_OPTION) {
                filename = c.getSelectedFile().getName();
                dir = c.getCurrentDirectory().toString();
                convertFile(Paths.get(dir + "/" + filename));
            }
            if (dialog == JFileChooser.CANCEL_OPTION) {
                filename = "You pressed cancel";
                dir = "";
                System.out.println(filename);
            }

        }
    }

    public static void main(String[] args) {
        //new Encryptor();
        File file = new File("/Users/davidson/Documents/testFile.txt");
        convertFile(file.toPath());
    }

    public static void convertFile(Path pathName) {
        try {
            byte[] fileContent = Files.readAllBytes(pathName);
            byte[] encrypted = new byte[fileContent.length];
            int count = 0;
            for (byte bit: fileContent) {
                bit += count;
                encrypted[count] = bit;
                count++;
            }
            System.out.println(new String(encrypted));
            saveFile(encrypted);
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public static void saveFile(byte[] encrypted) {
        try {
            FileWriter fileWriter = new FileWriter("/Users/davidson/Documents/encryptedTestFile.txt");
            fileWriter.write(new String(encrypted));
            fileWriter.close();
        } catch (IOException e) {
            System.out.println("Error");
        }

    }
}
