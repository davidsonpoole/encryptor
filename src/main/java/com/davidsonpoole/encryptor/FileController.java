package com.davidsonpoole.encryptor;

import java.io.*;

public class FileController {
    public byte[] readFile(File file) {
        try {
            FileInputStream fis = new FileInputStream(file);
            byte[] fileContent = new byte[(int) file.length()];
            fis.read(fileContent);
            fis.close();
            return fileContent;
        } catch (Exception e) {
            System.out.println(e);
            return null;
        }
    }

    public int[] toIntArray(byte[] b) {
        int[] unsignedContent = new int[b.length];
        for (int i=0; i< b.length; i++) {
            unsignedContent[i] = b[i] & 0xff;
        }
        return unsignedContent;
    }

    public int[] packFile(byte[] fileContent, String name) {
        try {
            byte[] filename = name.getBytes();
            byte[] fullContent = new byte[fileContent.length + 32];
            for (int i=0; i<32; i++) {
                if (i < filename.length) {
                    fullContent[i] = filename[i];
                } else {
                    fullContent[i] = 0;
                }

            }
            for (int i=0; i< fileContent.length; i++) {
                fullContent[i + 32] = fileContent[i];
            }
            int remainder = (16 - fullContent.length % 16);
            // need to pad with remainder
            byte[] padded = new byte[fullContent.length + remainder];
            for (int i=0; i< fullContent.length; i++) {
                padded[i] = fullContent[i];
            }
            for (int i= fullContent.length; i< padded.length; i++) {
                padded[i] = (byte) remainder;
            }
            fullContent = padded;
            return toIntArray(fullContent);
        } catch (Exception e) {
            System.out.println(e);
            return null;
        }
    }

    public byte[] unpackFile(byte[] fileContent) {
        int remainder = fileContent[fileContent.length - 1];
        byte[] contents = new byte[fileContent.length-remainder];
        for (int i=0; i< fileContent.length-remainder;i++) {
            contents[i] = fileContent[i];
        }
        //System.out.println(contents.length);
        return contents;
    }

    public void saveDecryptedFile(byte[] decrypted, File file) {
        try {
            byte[] content = unpackFile(decrypted);
            content = getFileContent(content);
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(content);
            fos.close();
        } catch (IOException e) {
            System.out.println("Error");
        }

    }

    public void saveEncryptedFile(byte[] encrypted, File file) {
        // eventually will implement custom filepath
        try {
            File fileToSave = new File(file.getPath() + ".encrypted");
            FileOutputStream fos = new FileOutputStream(fileToSave);
            fos.write(encrypted);
            fos.close();
        } catch (IOException e) {
            System.out.println(e);
        }

    }

    public void saveKey(int[] content, File file) {
        // eventually will implement custom filepath
        try {
            File keyToSave = new File(file.getPath() + ".key");
            FileOutputStream fos = new FileOutputStream(keyToSave);
            fos.write(toByteArray(content));
            fos.close();
        } catch (IOException e) {
            System.out.println("Error");
        }

    }

    public byte[] getFilename(byte[] decrypted) {
        byte[] filename = new byte[32];
        int filename_len = 0;
        for (int i=0; i< 32; i++) {
            filename[i] = decrypted[i];
            if (decrypted[i] != 0) {
                filename_len++;
            }
        }
        filename = new byte[filename_len];
        for (int i=0; i< filename.length; i++) {
            filename[i] = decrypted[i];
        }
        return filename;
    }

    public byte[] getFileContent(byte[] decrypted) {
        byte[] fileContents = new byte[decrypted.length - 32];
        // get filename and extension
        for (int i=0; i< fileContents.length; i++) {
            fileContents[i] = decrypted[i + 32];
        }
        return fileContents;
    }

    private byte[] toByteArray(int[] input) {
        byte[] result = new byte[input.length];
        for (int i=0; i< input.length; i++) {
            result[i] = (byte) input[i];
        }
        return result;
    }
}
