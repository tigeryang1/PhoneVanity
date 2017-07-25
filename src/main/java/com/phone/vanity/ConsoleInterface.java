package com.phone.vanity;

import java.io.*;
import java.text.MessageFormat;
import java.util.Scanner;

/**
 * by HYang
 */
public class ConsoleInterface {
    private VanityNumberMatcher pm;
    private InputStream in;
    private OutputStream out;


    public ConsoleInterface(InputStream dictionaryFile, InputStream in, OutputStream out) {
        this.pm = new VanityNumberMatcher(dictionaryFile);
        this.in = in;
        this.out = out;
    }

    /**
     * Main application method
     *
     * @param args
     */
    public static void main(String[] args) {
        InputStream inputStream = System.in;
        PrintStream printStream = System.out;
        System.out.println("Enter your phone number and press Enter:");
        startConsole(args, inputStream, printStream);
    }

    /**
     * Start console 
     *
     * @param args
     * @param in
     * @param out
     */
    protected static void startConsole(String[] args, InputStream in, OutputStream out) {
        InputStream dictionaryFile = getDefaultDictionary();
        
            //open as an interactive console application
            ConsoleInterface cc = new ConsoleInterface(dictionaryFile, in, out);
            cc.startConsoleInput();
    }

    /**
     * Start the console application
     */
    protected void startConsoleInput() {
  
        Scanner sc = new Scanner(this.in);
        PrintStream ps = new PrintStream(this.out);

        for (String phone = sc.nextLine(); phone != null; phone = sc.nextLine())
        {
        	if(phone.length()>3)
               this.pm.matches(phone);
        }
    }

 

    /**
     * Get the phone file input stream
     *
     */
    protected static InputStream getInputStream(String filePath) throws FileNotFoundException {
        InputStream nestedIS = null;

        File f = new File(filePath);
        if (f.exists()) {
            nestedIS = new FileInputStream(f);
        } else {
            nestedIS = ConsoleInterface.class.getResourceAsStream(filePath);
            if (nestedIS == null)
                throw new FileNotFoundException(MessageFormat.format("File {0} not found!", filePath));
        }
        return new BufferedInputStream(nestedIS);
    }


    /**
     * Load the default SO dictionary file. 
     */
    public static InputStream getDefaultDictionary() {
        InputStream inputStream = null;
        try {
            inputStream = getInputStream("/Dictonary.txt");
        } catch (FileNotFoundException e) {
            throw new RuntimeException("Error trying to open testDictonary.txt!", e);
        }

        if (inputStream != null)
            return inputStream;
        else
            throw new RuntimeException("Error trying to open testDictonary.txt! null!");

    }
}
