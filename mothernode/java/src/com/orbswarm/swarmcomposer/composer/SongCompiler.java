package com.orbswarm.swarmcomposer.composer;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * SongCompiler
 *  reads in an orbsong (.orbs), finds all the Sounds,
 *  determines their length (in seconds),
 *  hashes their filename (max length 11 characters),
 *  and writes out a compiled song file with lengths & hashes.
 *
 *   Option to place copies of all the sound files in a flat directory, named
 *   by their hashes.
 *
 * @author Simran Gleason
 */

public class SongCompiler {
    public static final String COMPILED_SONG_EXTENSION = ".orbc";

    public SongCompiler() {
    }

    public void compile(String basePath, String songFileName, String targetFileName, String compiledSongDir ) {
        PrintWriter targetWriter = null;
        try {
            File songFile = new File(songFileName);
            String songFilePath = songFile.getParent();
            File targetFile;
            System.out.println("SongCompiler");
            System.out.println("    songFile: " + songFile);
            System.out.println("    songFile.getPath(): " + songFile.getPath());
            System.out.println("    songFile.getParent(): " + songFile.getPath());
            System.out.println("    songFile.getCanonicalPath(): " + songFile.getCanonicalPath());
            System.out.println("    baseName(songFile): " + baseName(songFile));
            if (targetFileName == null) {
                String songFileBase = baseName(songFile); // includes the full path.
                targetFileName = songFileBase + COMPILED_SONG_EXTENSION;
            }
            System.out.println("    targetFileName: " + targetFileName);
            targetFile = new File(targetFileName);
            List songs = Bot.readSongFile(songFile.getCanonicalPath(), basePath);
            StringBuffer targetBuf = new StringBuffer(4096);
            for(Iterator it = songs.iterator(); it.hasNext(); ) {
                Song song = (Song)it.next();
                song.findSoundDurations();
                song.calculateHashes();
                song.setCompiled(true);
                Bot.writeSong(targetBuf, song);
            }

            try {
                targetWriter = new PrintWriter(targetFile);
                targetWriter.println(targetBuf.toString());
            } catch (IOException ex) {
                System.out.println("SongCompiler caught exception writing compiled song file: " + targetFileName);
                ex.printStackTrace();
            } finally {
                if (targetWriter != null) {
                    targetWriter.close();
                }
            }
        } catch (IOException ex) {
            System.out.println("SongCompiler caught exception compiling song file: " + songFileName);
            ex.printStackTrace();
        }
    }

    public int testCompiledFile(String compiledFilePath, String resultFilePath) {
        PrintWriter targetWriter = null;
        int error_code = 0;
        try {
            File songFile = new File(compiledFilePath);
            File targetFile = new File(resultFilePath);
            List songs = Bot.readSongFile(songFile.getCanonicalPath());
            StringBuffer targetBuf = new StringBuffer(4096);
            for(Iterator it = songs.iterator(); it.hasNext(); ) {
                Song song = (Song)it.next();
                Bot.writeSong(targetBuf, song);
            }

            String songFilePath = songFile.getParent();
            try {
                targetWriter = new PrintWriter(targetFile);
                targetWriter.println(targetBuf.toString());
            } catch (IOException ex) {
                System.out.println("SongCompiler  TEST_COMPILED_SONG caught exception writing result: " + resultFilePath);
                ex.printStackTrace();
                error_code = 1;
            } finally {
                if (targetWriter != null) {
                    targetWriter.close();
                }
            }
        } catch (IOException ex) {
            System.out.println("SongCompiler TEST_COMPILED_SONG caught exception onsong file: " + compiledFilePath);
            ex.printStackTrace();
            error_code = 1;
        }
        return error_code;
    }


    public String baseName(File file) {
        return baseName(file.getName());
    }
    
    public String baseName(String filename) {
        int lastDot = filename.lastIndexOf('.');
        if (lastDot == -1) {
            return filename;
        }
        return filename.substring(0, lastDot);
    }

    public static void main(String []args) {
        if (args.length > 0) {
            if (args[0].equalsIgnoreCase("test")) {
                if (args.length < 2) {
                    System.out.println("Usage java orb.swarm.composer.SongCompiler test compiledSongFile");
                    System.exit(0);
                }
                String compiledFileName = args[1];
                String resultFileName = compiledFileName + "_results";
                if (args.length > 2) {
                    resultFileName = args[2];
                }
                SongCompiler scribe = new SongCompiler();
                int error_code = scribe.testCompiledFile(compiledFileName, resultFileName);
                System.exit(error_code);
            }
        }
        if (args.length < 3) {
            System.out.println("Usage java orb.swarm.composer.SongCompiler songFile songDir compiledSongDir");
            System.exit(0);
        }

        String songFileName = args[0];
        String targetFileName = null; // default.
        String basePath = args[1];
        String compiledSongDir = args[2];
        
        SongCompiler scribe = new SongCompiler();
        System.out.println("Compiling...");
        scribe.compile(basePath, songFileName, targetFileName, compiledSongDir );
        System.out.println("Compiled.");
        System.exit(0);
    }
}