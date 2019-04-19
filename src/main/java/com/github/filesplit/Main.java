package com.github.filesplit;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws Exception {
        // Use this utility program to move large file to external storage or Cloud like One Drive part by part when have little free space
        // This is a call example which moves each part directly to OneDrive folder and waits until it is transferred online to free up space
        //"C:\Users\User\Desktop\New folder\virtualbox.ova" "C:\Users\User\OneDrive\virtualbox.ova" 8
        if(args.length <= 0) {
            System.out.println("Usage: <input file> <output file> <number of parts>");
        }
        RandomAccessFile raf = new RandomAccessFile(args[0], "r");
        long numSplits = Integer.parseInt(args[2]); //from user input, extract it from args
        boolean pause = true;
        long sourceSize = raf.length();
        long bytesPerSplit = sourceSize/numSplits ;
        long remainingBytes = sourceSize % numSplits;

        int maxReadBufferSize = 32 * 1024; //32KB
        for(int destIx=1; destIx <= numSplits; destIx++) {
            BufferedOutputStream bw = new BufferedOutputStream(new FileOutputStream(args[1]+
                    "."+String.format("%03d", destIx)));
            if(bytesPerSplit > maxReadBufferSize) {
                long numReads = bytesPerSplit/maxReadBufferSize;
                long numRemainingRead = bytesPerSplit % maxReadBufferSize;
                for(int i=0; i<numReads; i++) {
                    readWrite(raf, bw, maxReadBufferSize);
                }
                if(numRemainingRead > 0) {
                    readWrite(raf, bw, numRemainingRead);
                }
            }else {
                readWrite(raf, bw, bytesPerSplit);
            }
            bw.close();

            if(pause) {
                String answer = askYesNo();
                if(!"Y".equals(answer.toUpperCase())) {
                    break;
                }

            }
        }
        if(remainingBytes > 0) {
            BufferedOutputStream bw = new BufferedOutputStream(new FileOutputStream("split."+(numSplits+1)));
            readWrite(raf, bw, remainingBytes);
            bw.close();
        }
        raf.close();
    }

    private static String askYesNo() {
        Scanner keyboard = new Scanner(System.in);
        System.out.println("Part is ready, proceed?");
        String str = keyboard.nextLine();
        if("".equals(str))
            str = "Y";
        return str;
    }

    static void readWrite(RandomAccessFile raf, BufferedOutputStream bw, long numBytes) throws IOException {
        byte[] buf = new byte[(int) numBytes];
        int val = raf.read(buf);
        if(val != -1) {
            bw.write(buf);
        }
    }
}
