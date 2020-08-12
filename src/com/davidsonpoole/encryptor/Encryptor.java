package com.davidsonpoole.encryptor;


import javax.crypto.KeyGenerator;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;

public class Encryptor extends JFrame{
    private String filename, dir;
    private static byte[] RC = new byte[] {
            0x0, 0x01, 0x02, 0x04, 0x08, 0x10, 0x20, 0x40, (byte) 0x80, 0x1B, 0x36
    };

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

    private static byte[] generate_AES_key() {
        Key key;
        SecureRandom rand = new SecureRandom();
        try {
            KeyGenerator generator = KeyGenerator.getInstance("AES");
            generator.init(256, rand);
             key = generator.generateKey();
             return key.getEncoded();
        } catch (NoSuchAlgorithmException e) {
            System.out.println(e);
            return null;
        }
    }

    private static byte[] expand_AES_key(byte[] key) {
        byte[] result = new byte[480]; // 120-word array (4 bytes per int)
        int rcon_iter = 1;
        for (int i=0; i < 32; i++) {
            result[i] = key[i]; // Copies key into first 32 bytes
        }

        // get first 4 bytes of expanded key
        byte[] temp; // 4 byte temporary array
        for (int i=32; i < 36; i++) {
            // just doing 4 bytes right now
            // assign prev 4 bytes of temp key to temp
            temp = Arrays.copyOfRange(result, i-4, i);
            //schedule_core()
            if (i % 8 == 0) {
                //temp = subWord(rotWord(temp)) ^ RC[i / 32];
            }
        }
        return result;
    }

    private static byte[] subWord(byte[] initial) {
        byte[] final_result = new byte[4];
        // calculate s-box
        int[] sbox = {
                143,
                199,
                227,
                241,
                248,
                124,
                62,
                31,
        };
        // lsb first
        for (int i=0;i<4;i++) {
            //each byte
            byte curr = initial[i];
            int[] result = new int[8];
            int[] curr_bits = new int[8];
            for (int x=0; x<8; x++) {
                // AND each row in sbox with input
                result[x] = curr & sbox[x]; // returns a byte
                // go through sbox and get xor for each row and reverse
                int curr_bit = ((result[x] & (1 << 7)) / (1 << 7)) ^ ((result[x] & (1 << 6)) / (1 << 6));
                for (int j=2; j<8; j++) {
                    curr_bit ^= ((result[x] & (1 << (7 - j))) / (1 << (7 - j)));
                }
                curr_bits[x] = curr_bit;
            }
            final_result[i] = (byte) (curr_bits[0]);
            for (int x=1; x<8;x++) {
                final_result[i] |= (byte) (curr_bits[x] << x);
            }
            final_result[i] ^= 0x63;
            System.out.println(String.format("0x%02X", final_result[i]));


        }
        return final_result;
    }

    private static byte[] rotWord(byte[] initial) {
        // rotate by 8 bits
        byte[] result = new byte[4];
        for (int i=0; i<4; i++) {
            result[i] = initial[(i+1)%4];
        }
        return result;
    }

    private static byte[] schedule_core(byte[] in, int i) {
        // takes in 32-bit word
        byte[] rotated = rotWord(in);
        byte[] result = subWord(rotated);
        result[0] ^= (byte) Math.pow(2, i);
        return result;
    }

    public static void main(String[] args) {
        //new Encryptor();
        //File file = new File("/Users/davidson/Documents/testFile.txt");
        //convertFile(file.toPath());
        //byte[] key = generate_AES_key();
        //byte[] expanded_key = expand_AES_key(key);
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
