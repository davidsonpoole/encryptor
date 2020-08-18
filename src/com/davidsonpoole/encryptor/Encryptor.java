package com.davidsonpoole.encryptor;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;

public class Encryptor {
    private File file, key;
    boolean encrypted = false;

    JFrame frame;
    JLabel error;

    int[] keyData;
    byte[] encryptedData;
    byte[] decryptedData;
    String filename;

    public Encryptor() {
        loadHome();
    }

    public void loadHome() {
        JButton encrypt = new JButton("Encrypt");
        JButton decrypt = new JButton("Decrypt");
        encrypt.addActionListener(e -> loadEncrypt());
        decrypt.addActionListener(e -> loadDecrypt());
        JPanel p = new JPanel();
        p.add(encrypt);
        p.add(decrypt);
        frame = new JFrame();
        frame.add(p, BorderLayout.SOUTH);

        p = new JPanel();
        p.setLayout(new GridLayout(2, 1));
        JLabel title = new JLabel("Welcome to Davidson's Encryptor!");
        p.add(title);
        frame.add(p, BorderLayout.NORTH);

        frame.setTitle("Davidson's Encryptor");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 400);
        frame.setVisible(true);
    }

    public void loadEncrypt() {
        GridLayout home = new GridLayout(5, 1);
        frame.getContentPane().removeAll();
        frame.setLayout(home);
        JPanel p = new JPanel();
        JLabel title = new JLabel("Encrypt");
        p.add(title);
        frame.add(p);

        p = new JPanel();
        JLabel fileLabel = new JLabel("Choose a file to encrypt: ");
        JButton browseFile = new JButton("Browse");
        JLabel chosenFile = new JLabel();
        browseFile.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JFileChooser c = new JFileChooser();
                int dialog = c.showOpenDialog(frame);
                if (dialog == JFileChooser.APPROVE_OPTION) {
                    file = c.getSelectedFile();
                    chosenFile.setText(file.getName());
                }
            }
        });
        p.add(fileLabel);
        p.add(browseFile);
        p.add(chosenFile);
        frame.add(p);

        p = new JPanel();
        JLabel keyFile = new JLabel("Choose a key file (Optional): ");
        JButton browseKeyFile = new JButton("Browse");
        JLabel chosenKey = new JLabel();
        browseKeyFile.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JFileChooser c = new JFileChooser();
                c.setAcceptAllFileFilterUsed(false);
                c.addChoosableFileFilter(new FileNameExtensionFilter(
                        "Key File", "key"));
                int dialog = c.showOpenDialog(frame);
                if (dialog == JFileChooser.APPROVE_OPTION) {
                    key = c.getSelectedFile();
                    chosenKey.setText(key.getName());
                }
            }
        });
        p.add(keyFile);
        p.add(browseKeyFile);
        p.add(chosenKey);
        frame.add(p);

        p = new JPanel();
        JButton back = new JButton("Back");
        back.addActionListener(e -> loadHome());
        JButton encrypt = new JButton("Encrypt");
        encrypt.addActionListener(new EncryptListener());
        p.add(back);
        p.add(encrypt);
        frame.add(p);

        p = new JPanel();
        error = new JLabel();
        p.add(error);
        frame.add(p);

        frame.revalidate();
        frame.repaint();
    }

    public void loadDecrypt() {
        GridLayout home = new GridLayout(5, 1);
        frame.getContentPane().removeAll();
        frame.setLayout(home);
        JPanel p = new JPanel();
        JLabel title = new JLabel("Decrypt");
        p.add(title);
        frame.add(p);

        p = new JPanel();
        JLabel fileLabel = new JLabel("Choose a file to decrypt: ");
        JButton browseFile = new JButton("Browse");
        JLabel chosenFile = new JLabel();
        browseFile.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JFileChooser c = new JFileChooser();
                c.setAcceptAllFileFilterUsed(false);
                c.addChoosableFileFilter(new FileNameExtensionFilter(
                        "Encrypted File", "encrypted"));
                int dialog = c.showOpenDialog(frame);
                if (dialog == JFileChooser.APPROVE_OPTION) {
                    file = c.getSelectedFile();
                    chosenFile.setText(file.getName());
                }
            }
        });
        p.add(fileLabel);
        p.add(browseFile);
        p.add(chosenFile);
        frame.add(p);

        p = new JPanel();
        JLabel keyFile = new JLabel("Choose a key file (Required): ");
        JButton browseKeyFile = new JButton("Browse");
        JLabel chosenKey = new JLabel();
        browseKeyFile.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JFileChooser c = new JFileChooser();
                c.setAcceptAllFileFilterUsed(false);
                c.addChoosableFileFilter(new FileNameExtensionFilter(
                        "Key File", "key"));
                int dialog = c.showOpenDialog(frame);
                if (dialog == JFileChooser.APPROVE_OPTION) {
                    key = c.getSelectedFile();
                    chosenKey.setText(key.getName());
                }
            }
        });
        p.add(keyFile);
        p.add(browseKeyFile);
        p.add(chosenKey);
        frame.add(p);

        p = new JPanel();
        JButton back = new JButton("Back");
        back.addActionListener(e -> loadHome());
        p.add(back);
        JButton decrypt = new JButton("Decrypt");
        decrypt.addActionListener(new DecryptListener());
        p.add(decrypt);
        frame.add(p);

        p = new JPanel();
        error = new JLabel();
        p.add(error);
        frame.add(p);

        frame.revalidate();
        frame.repaint();
    }

    public void loadSuccess() {
        GridLayout home = new GridLayout(5, 1);
        frame.getContentPane().removeAll();
        frame.setLayout(home);
        JPanel p = new JPanel();
        JLabel title = new JLabel("Success");
        p.add(title);
        frame.add(p);

        p = new JPanel();
        JButton back = new JButton("Back");
        back.addActionListener(e -> loadHome());
        p.add(back);

        JButton downloadFileButton = (encrypted) ?
                new JButton("Download Encrypted File") :
                new JButton("Download Decrypted File");
        downloadFileButton.addActionListener(new SaveFileListener());
        p.add(downloadFileButton);

        if (encrypted) {
            JButton downloadKeyButton = new JButton("Download Key");
            downloadKeyButton.addActionListener(new SaveKeyListener());
            p.add(downloadKeyButton);
        }
        frame.add(p);


        frame.revalidate();
        frame.repaint();
    }

    public class SaveFileListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            JFileChooser c = new JFileChooser();
            if (encrypted) {
                c.setAcceptAllFileFilterUsed(false);
                c.addChoosableFileFilter(new FileNameExtensionFilter(
                        "Encrypted File", "encrypted"));
            } else {
                FileController fc = new FileController();
                filename = new String(fc.getFilename(decryptedData));
                c.setSelectedFile(new File(filename));
            }
            c.setDialogTitle("Choose a location to save");
            int selection = c.showSaveDialog(frame);
            if (selection == JFileChooser.APPROVE_OPTION) {
                File fileToSave = c.getSelectedFile();
                downloadFile(fileToSave);
            }
        }
    }

    public class SaveKeyListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            JFileChooser c = new JFileChooser();
            c.setAcceptAllFileFilterUsed(false);
            c.addChoosableFileFilter(new FileNameExtensionFilter(
                    "Key File", "key"));
            c.setDialogTitle("Choose a location to save");
            int selection = c.showSaveDialog(frame);
            if (selection == JFileChooser.APPROVE_OPTION) {
                File KeyToSave = c.getSelectedFile();
                downloadKey(KeyToSave);
            }
        }
    }

    public class EncryptListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            try {
                if (file == null) {
                    throw new Exception("Must choose a file to encrypt");
                }
                if (key != null) {
                    encryptFile(file, key);
                } else {
                    encryptFile(file);
                }
                encrypted = true;
                loadSuccess();
            } catch (Exception a) {
                error.setText(a.getMessage());
            }
        }
    }

    public class DecryptListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            try {
                if (file == null) {
                    throw new Exception("Must choose a file to decrypt");
                }
                if (key == null) {
                    throw new Exception("Must provide a key");
                }
                decryptFile(file, key);
                encrypted = false;
                loadSuccess();
            } catch (Exception a) {
                error.setText(a.getMessage());
            }
        }
    }

    public void encryptFile(File file, File key) {
        AESEncryptor aes = new AESEncryptor();
        FileController fc = new FileController();
        keyData = fc.toIntArray(fc.readFile(key));
        int[] expandedKey = aes.expandKey(keyData);
        byte[] fileContent = fc.readFile(file);
        int[] packedFile = fc.packFile(fileContent, file.getName());
        int[] encrypted = aes.encryptFile(packedFile, expandedKey);
        encryptedData = aes.toByteArray(encrypted);

    }

    public void encryptFile(File file) {
        AESEncryptor aes = new AESEncryptor();
        FileController fc = new FileController();
        keyData = aes.generateKey();
        int[] expandedKey = aes.expandKey(keyData);
        byte[] fileContent = fc.readFile(file);
        int[] packedFile = fc.packFile(fileContent, file.getName());
        int[] encrypted = aes.encryptFile(packedFile, expandedKey);
        encryptedData = aes.toByteArray(encrypted);
    }

    public void decryptFile(File file, File key) {
        AESEncryptor aes = new AESEncryptor();
        FileController fc = new FileController();
        int[] keyData = fc.toIntArray(fc.readFile(key));
        int[] expandedKey = aes.expandKey(keyData);
        int[] fileData = fc.toIntArray(fc.readFile(file));
        int[] decrypted = aes.decryptFile(fileData, expandedKey);
        decryptedData = aes.toByteArray(decrypted);
    }

    public void downloadFile(File file) {
        FileController fc = new FileController();
        if (encrypted) {
            fc.saveEncryptedFile(encryptedData, file);
        } else {
            fc.saveDecryptedFile(decryptedData, file);
        }
    }

    public void downloadKey(File file) {
        FileController fc = new FileController();
        fc.saveKey(keyData, file);
    }

    public static void main(String[] args) {
        new Encryptor();
    }
}
