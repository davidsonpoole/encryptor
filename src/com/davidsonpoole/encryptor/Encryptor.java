package com.davidsonpoole.encryptor;


import javax.crypto.KeyGenerator;
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
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class Encryptor extends JFrame{
    private String filename, dir;
    private static int[] sbox =  {
        0x63 ,0x7c ,0x77 ,0x7b ,0xf2 ,0x6b ,0x6f ,0xc5 ,0x30 ,0x01 ,0x67 ,0x2b ,0xfe ,0xd7 ,0xab ,0x76
                ,0xca ,0x82 ,0xc9 ,0x7d ,0xfa ,0x59 ,0x47 ,0xf0 ,0xad ,0xd4 ,0xa2 ,0xaf ,0x9c ,0xa4 ,0x72 ,0xc0
                ,0xb7 ,0xfd ,0x93 ,0x26 ,0x36 ,0x3f ,0xf7 ,0xcc ,0x34 ,0xa5 ,0xe5 ,0xf1 ,0x71 ,0xd8 ,0x31 ,0x15
                ,0x04 ,0xc7 ,0x23 ,0xc3 ,0x18 ,0x96 ,0x05 ,0x9a ,0x07 ,0x12 ,0x80 ,0xe2 ,0xeb ,0x27 ,0xb2 ,0x75
                ,0x09 ,0x83 ,0x2c ,0x1a ,0x1b ,0x6e ,0x5a ,0xa0 ,0x52 ,0x3b ,0xd6 ,0xb3 ,0x29 ,0xe3 ,0x2f ,0x84
                ,0x53 ,0xd1 ,0x00 ,0xed ,0x20 ,0xfc ,0xb1 ,0x5b ,0x6a ,0xcb ,0xbe ,0x39 ,0x4a ,0x4c ,0x58 ,0xcf
                ,0xd0 ,0xef ,0xaa ,0xfb ,0x43 ,0x4d ,0x33 ,0x85 ,0x45 ,0xf9 ,0x02 ,0x7f ,0x50 ,0x3c ,0x9f ,0xa8
                ,0x51 ,0xa3 ,0x40 ,0x8f ,0x92 ,0x9d ,0x38 ,0xf5 ,0xbc ,0xb6 ,0xda ,0x21 ,0x10 ,0xff ,0xf3 ,0xd2
                ,0xcd ,0x0c ,0x13 ,0xec ,0x5f ,0x97 ,0x44 ,0x17 ,0xc4 ,0xa7 ,0x7e ,0x3d ,0x64 ,0x5d ,0x19 ,0x73
                ,0x60 ,0x81 ,0x4f ,0xdc ,0x22 ,0x2a ,0x90 ,0x88 ,0x46 ,0xee ,0xb8 ,0x14 ,0xde ,0x5e ,0x0b ,0xdb
                ,0xe0 ,0x32 ,0x3a ,0x0a ,0x49 ,0x06 ,0x24 ,0x5c ,0xc2 ,0xd3 ,0xac ,0x62 ,0x91 ,0x95 ,0xe4 ,0x79
                ,0xe7 ,0xc8 ,0x37 ,0x6d ,0x8d ,0xd5 ,0x4e ,0xa9 ,0x6c ,0x56 ,0xf4 ,0xea ,0x65 ,0x7a ,0xae ,0x08
                ,0xba ,0x78 ,0x25 ,0x2e ,0x1c ,0xa6 ,0xb4 ,0xc6 ,0xe8 ,0xdd ,0x74 ,0x1f ,0x4b ,0xbd ,0x8b ,0x8a
                ,0x70 ,0x3e ,0xb5 ,0x66 ,0x48 ,0x03 ,0xf6 ,0x0e ,0x61 ,0x35 ,0x57 ,0xb9 ,0x86 ,0xc1 ,0x1d ,0x9e
                ,0xe1 ,0xf8 ,0x98 ,0x11 ,0x69 ,0xd9 ,0x8e ,0x94 ,0x9b ,0x1e ,0x87 ,0xe9 ,0xce ,0x55 ,0x28 ,0xdf
                ,0x8c ,0xa1 ,0x89 ,0x0d ,0xbf ,0xe6 ,0x42 ,0x68 ,0x41 ,0x99 ,0x2d ,0x0f ,0xb0 ,0x54 ,0xbb ,0x16};

    private static int[] rc = {0x00, 0x01, 0x02, 0x04, 0x08, 0x10, 0x20, 0x40, 0x80, 0x1B, 0x36};

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

    // Generate and Expand AES Key

    private static int[] generate_AES_key() {
        Key key;
        SecureRandom rand = new SecureRandom();
        try {
            KeyGenerator generator = KeyGenerator.getInstance("AES");
            generator.init(256, rand);
             key = generator.generateKey();
             byte[] encoded = key.getEncoded();
             int[] unsigned_key = new int[encoded.length];
             for (int i=0; i<encoded.length; i++) {
                 unsigned_key[i] = encoded[i] & 0xff;
             }
             return unsigned_key;
        } catch (NoSuchAlgorithmException e) {
            System.out.println(e);
            return null;
        }
    }

    private static int[] expand_AES_key(int[] key) {
        int[] result = new int[240]; // 120-word array (4 bytes per int)
        int rcon_iter = 1;
        int i = 0;
        while(i < 32) {
            result[i] = key[i]; // Copies key into first 32 bytes
            i++;
        }
        int[] temp = new int[4];
        while (i < 240) {
            for (int j=0; j<4; j++) {
                temp[j] = result[i+j-4];
            }
            if (i % 32 == 0) {
                temp = schedule_core(temp, rcon_iter++);
            }
            if (i % 32 == 16) {
                for (int j=0; j<4; j++) {
                    temp[j] = subWord(temp[j]);
                }
            }
            // assign prev 4 bytes of temp key to temp
            for (int j=0; j<4; j++) {
                result[i] = (result[i-32] ^ temp[j]);
                i++;
            }
        }
        return result;
    }

    private static int[] schedule_core(int[] in, int i) {
        // takes in 32-bit/4-byte word
        int[] rotated = rotWord(in);
        int[] result = new int[4];
        for (int j=0;j<4;j++) {

            result[j] = subWord(rotated[j]);
        }
        result[0] ^= rc[i];
        return result;
    }

    private static int[] rotWord(int[] initial) {
        // rotate by 8 bits
        int[] result = new int[4];
        for (int i=0; i<4; i++) {
            result[i] = initial[(i+1)%4];
        }
        return result;
    }

    private static int subWord(int initial) {
        return sbox[initial];
    }

    // Encrypt file using key

    private static int[] encryptFile(int[] fileContent, int[] key) {
        int index = 0;

        while (index < fileContent.length) {
            // split into 16-byte blocks
            int[][] data = new int[4][4];
            for (int i=0; i< 4; i++) {
                for (int j=0;j<4;j++) {
                    data[j][i] = fileContent[index++];
                }
            }
        }
        return null;
    }

    public static void main(String[] args) {
        //new Encryptor();
        File file = new File("/Users/davidson/Documents/testFile.txt");
        int[] key = generate_AES_key();
        int[] expanded_key = expand_AES_key(key);
        int[] fileContent = convertFile(file.toPath());
        int[] encrypted = encryptFile(fileContent, expanded_key);
    }

    // -----File helper methods------

    public static int[] convertFile(Path pathName) {
        try {
            byte[] fileContent = Files.readAllBytes(pathName);
            if (fileContent.length % 16 != 0) {
                int remainder = fileContent.length % 16;
                // need to pad with remainder
                byte[] padded = new byte[fileContent.length + remainder];
                for (int i=0; i< fileContent.length; i++) {
                    padded[i] = fileContent[i];
                }
                for (int i= fileContent.length; i< padded.length; i++) {
                    padded[i] = (byte) remainder;
                }
                fileContent = padded;
            }
            int[] unsignedContent = new int[fileContent.length];
            for (int i=0; i< fileContent.length; i++) {
                unsignedContent[i] = fileContent[i] & 0xff;
            }
            return unsignedContent;
        } catch (Exception e) {
            System.out.println(e);
            return null;
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
