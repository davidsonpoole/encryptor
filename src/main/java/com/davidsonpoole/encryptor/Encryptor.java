package com.davidsonpoole.encryptor;

import javax.swing.*;
import javax.swing.SwingWorker;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.*;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

public class Encryptor {
    private File file, key;
    boolean encrypted = false;

    private static final String PROGRESS_PROPERTY_NAME = "progress";

    JFrame frame;
    JLabel error;

    Task task;
    AESEncryptor aes;

    JButton encrypt;
    JButton decrypt;

    JProgressBar progressBar;

    int[] keyData;
    byte[] encryptedData;
    byte[] decryptedData;
    String filename;

    public Encryptor() {
        frame = new JFrame();
        frame.setTitle("Davidson's Encryptor");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 400);
        frame.setResizable(false);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        loadHome();
    }

    public void loadHome() {
        frame.getContentPane().removeAll();
        file = null;
        key = null;
        GridLayout g = new GridLayout(6, 1);
        frame.setLayout(g);

        JPanel p = new JPanel();
        frame.add(p);
        p = new JPanel();
        frame.add(p);

        p = new JPanel();
        JLabel title = new JLabel("Welcome to Davidson's Encryptor.");
        title.setFont(title.getFont().deriveFont(22.0f));
        p.add(title);
        frame.add(p);

        p = new JPanel();
        JLabel subtitle = new JLabel("This program uses AES 256-bit encryption to make your files secure.");
        subtitle.setFont(subtitle.getFont().deriveFont(14.0f));
        p.add(subtitle);
        frame.add(p);

        p = new JPanel();
        frame.add(p);

        JButton encrypt = new JButton("Encrypt");
        JButton decrypt = new JButton("Decrypt");
        encrypt.setFont(encrypt.getFont().deriveFont(14.0f));
        decrypt.setFont(decrypt.getFont().deriveFont(14.0f));
        encrypt.addActionListener(e -> loadEncrypt());
        decrypt.addActionListener(e -> loadDecrypt());
        p = new JPanel();
        p.add(encrypt);
        p.add(decrypt);
        frame.add(p, BorderLayout.SOUTH);

        frame.revalidate();
        frame.repaint();
    }

    public void loadEncrypt() {
        GridLayout home = new GridLayout(7, 1);
        frame.getContentPane().removeAll();
        frame.setLayout(home);

        JPanel p = new JPanel();
        frame.add(p);

        p = new JPanel();
        JLabel title = new JLabel("Encrypt");
        title.setFont(title.getFont().deriveFont(20.0f));
        p.add(title);
        frame.add(p);

        p = new JPanel();
        JLabel fileLabel = new JLabel("Choose a file to encrypt (150MB max):");
        fileLabel.setFont(fileLabel.getFont().deriveFont(14.0f));
        JButton browseFile = new JButton("Browse");
        browseFile.setFont(browseFile.getFont().deriveFont(14.0f));
        JLabel chosenFile = new JLabel();
        chosenFile.setFont(chosenFile.getFont().deriveFont(14.0f));
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
        keyFile.setFont(keyFile.getFont().deriveFont(14.0f));
        JButton browseKeyFile = new JButton("Browse");
        browseKeyFile.setFont(browseKeyFile.getFont().deriveFont(14.0f));
        JLabel chosenKey = new JLabel();
        chosenKey.setFont(chosenKey.getFont().deriveFont(14.0f));
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
        progressBar = new JProgressBar(0, 100);
        progressBar.setValue(0);
        progressBar.setStringPainted(true);
        p.add(progressBar);
        frame.add(p);

        p = new JPanel();
        JButton back = new JButton("Cancel");
        back.setFont(back.getFont().deriveFont(14.0f));
        back.addActionListener(e -> {
            if (task != null) {
                task.cancel();
            }
            loadHome();
        });
        encrypt = new JButton("Encrypt");
        encrypt.setFont(encrypt.getFont().deriveFont(14.0f));
        encrypt.addActionListener(new EncryptListener());
        p.add(back);
        p.add(encrypt);
        frame.add(p);

        p = new JPanel();
        error = new JLabel();
        error.setFont(error.getFont().deriveFont(14.0f));
        error.setForeground(Color.RED);
        p.add(error);
        frame.add(p);

        frame.revalidate();
        frame.repaint();
    }

    public void loadDecrypt() {
        GridLayout home = new GridLayout(7, 1);
        frame.getContentPane().removeAll();
        frame.setLayout(home);

        JPanel p = new JPanel();
        frame.add(p);

        p = new JPanel();
        JLabel title = new JLabel("Decrypt");
        title.setFont(title.getFont().deriveFont(20.0f));
        p.add(title);
        frame.add(p);

        p = new JPanel();
        JLabel fileLabel = new JLabel("Choose a file to decrypt (150MB max): ");
        fileLabel.setFont(fileLabel.getFont().deriveFont(14.0f));
        JButton browseFile = new JButton("Browse");
        browseFile.setFont(browseFile.getFont().deriveFont(14.0f));
        JLabel chosenFile = new JLabel();
        chosenFile.setFont(chosenFile.getFont().deriveFont(14.0f));
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
        keyFile.setFont(keyFile.getFont().deriveFont(14.0f));
        JButton browseKeyFile = new JButton("Browse");
        browseKeyFile.setFont(browseKeyFile.getFont().deriveFont(14.0f));
        JLabel chosenKey = new JLabel();
        chosenKey.setFont(chosenKey.getFont().deriveFont(14.0f));
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
        progressBar = new JProgressBar(0, 100);
        progressBar.setValue(0);
        progressBar.setStringPainted(true);
        p.add(progressBar);
        frame.add(p);

        p = new JPanel();
        JButton back = new JButton("Cancel");
        back.setFont(back.getFont().deriveFont(14.0f));
        back.addActionListener(e -> {
            if (task != null) {
                task.cancel();
            }
            loadHome();
        });
        p.add(back);
        decrypt = new JButton("Decrypt");
        decrypt.setFont(decrypt.getFont().deriveFont(14.0f));
        decrypt.addActionListener(new DecryptListener());
        p.add(decrypt);
        frame.add(p);

        p = new JPanel();
        error = new JLabel();
        error.setFont(error.getFont().deriveFont(14.0f));
        error.setForeground(Color.RED);
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
        frame.add(p);

        p = new JPanel();
        JLabel title = new JLabel("Success!");
        title.setFont(title.getFont().deriveFont(20.0f));
        p.add(title);
        frame.add(p);

        p = new JPanel();
        if (encrypted) {
            JTextArea subtitle_key = new JTextArea("You may now download your file and key. Be sure to save your\nkey in a secure location and only share with those you trust.");
            subtitle_key.setBackground(null);
            subtitle_key.setFont(subtitle_key.getFont().deriveFont(14.0f));
            p.add(subtitle_key);
        } else {
            JLabel subtitle_file = new JLabel("You can now download your decrypted file.");
            subtitle_file.setFont(subtitle_file.getFont().deriveFont(14.0f));
            p.add(subtitle_file);
        }
        frame.add(p);

        p = new JPanel();
        frame.add(p);

        p = new JPanel();
        JButton back = new JButton("Back");
        back.setFont(back.getFont().deriveFont(14.0f));
        back.addActionListener(e -> loadHome());
        p.add(back);

        JButton downloadFileButton = (encrypted) ?
                new JButton("Download Encrypted File") :
                new JButton("Download Decrypted File");
        downloadFileButton.addActionListener(new SaveFileListener());
        downloadFileButton.setFont(downloadFileButton.getFont().deriveFont(14.0f));
        p.add(downloadFileButton);

        if (encrypted) {
            JButton downloadKeyButton = new JButton("Download Key");
            downloadKeyButton.addActionListener(new SaveKeyListener());
            downloadKeyButton.setFont(downloadKeyButton.getFont().deriveFont(14.0f));
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
                encrypted = true;
                task = new Task();
                task.execute();
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
                encrypted = false;
                decrypt.setEnabled(false);
                frame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                decryptFile(file, key);
                task = new Task();
                task.execute();

            } catch (Exception a) {
                error.setText(a.getMessage());
            }
        }
    }

    public class Task extends SwingWorker<Integer, Integer> {

        @Override
        protected Integer doInBackground() {
            int success = 0;
            frame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            if (encrypted) {
                encrypt.setEnabled(false);
                if (key != null) {
                    success = encryptFile(file, key);
                } else {
                    success = encryptFile(file);
                }
            } else {
                decrypt.setEnabled(false);
                success = decryptFile(file, key);
            }
            return success;
        }



        @Override
        protected void process(List<Integer> chunks) {
            if (!isCancelled()) {
                int recentValue = chunks.get(chunks.size() - 1);
                progressBar.setValue(recentValue);
            }

        }

        public void publishData(Integer data) {
            publish(data);
        }

        public void cancel() {
            cancel(true);
        }

        @Override
        protected void done() {
            try {
                get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (CancellationException e) {

                progressBar.setValue(0);
                frame.setCursor(null);
            }
            progressBar.setValue(0);
            frame.setCursor(null);
            loadSuccess();
        }
    }

    public int encryptFile(File file, File key) {
        aes = new AESEncryptor();
        FileController fc = new FileController();
        keyData = fc.toIntArray(fc.readFile(key));
        int[] expandedKey = aes.expandKey(keyData);
        byte[] fileContent = fc.readFile(file);
        int[] packedFile = fc.packFile(fileContent, file.getName());
        aes.addPropertyChangeListener(AESEncryptor.INDEX, evt -> {
                int fileSize = packedFile.length;
                double d = (double) (int) evt.getNewValue();
                int percentage = (int) ((d / (double) fileSize) * 100);
                task.publishData(percentage);
            }
        );
        int[] encrypted = aes.encryptFile(packedFile, expandedKey);
        encryptedData = aes.toByteArray(encrypted);
        return 1;

    }

    public int encryptFile(File file) {
        aes = new AESEncryptor();
        FileController fc = new FileController();
        keyData = aes.generateKey();
        int[] expandedKey = aes.expandKey(keyData);
        byte[] fileContent = fc.readFile(file);
        int[] packedFile = fc.packFile(fileContent, file.getName());
        aes.addPropertyChangeListener(AESEncryptor.INDEX, evt -> {
                    int fileSize = packedFile.length;
                    double d = (double) (int) evt.getNewValue();
                    int percentage = (int) ((d / (double) fileSize) * 100);
                    task.publishData(percentage);

                }
        );
        int[] encrypted = aes.encryptFile(packedFile, expandedKey);
        encryptedData = aes.toByteArray(encrypted);
        return 1;
    }

    public int decryptFile(File file, File key) {
        aes = new AESEncryptor();
        FileController fc = new FileController();
        int[] keyData = fc.toIntArray(fc.readFile(key));
        int[] expandedKey = aes.expandKey(keyData);
        int[] fileData = fc.toIntArray(fc.readFile(file));
        aes.addPropertyChangeListener(AESEncryptor.INDEX, evt -> {
                    int fileSize = fileData.length;
                    System.out.println(fileSize);
                    double d = (double) fileSize - (int) evt.getNewValue();
                    System.out.println(evt.getNewValue());
                    int percentage = (int) ((d / (double) fileSize) * 100);
                    task.publishData(percentage);
                }
        );
        int[] decrypted = aes.decryptFile(fileData, expandedKey);
        decryptedData = aes.toByteArray(decrypted);
        return 1;
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

    public void updateProgressBar(int value) {
        progressBar.setValue(value);
    }

    public static void main(String[] args) {
        new Encryptor();
    }
}
