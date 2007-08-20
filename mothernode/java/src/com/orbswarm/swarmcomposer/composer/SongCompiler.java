package com.orbswarm.swarmcomposer.composer;

import java.io.File;
import java.io.IOException;
import java.io.FileWriter;
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
 *  Also creates the fullpath->hash, duration list needed to run sounds onthe orb boards. 
 *
 * @author Simran Gleason
 */

public class SongCompiler {
    public static final String COMPILED_SONG_EXTENSION = ".orbc";

    private static int numConflicts = 0;
    
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

    public int catalog(String catalogFilePath, String targetDirPath, ArrayList sourceDirs) {
        try {
            File catalogFile = new File(catalogFilePath);
            if (!catalogFile.canWrite()) {
                // TODO: need to test if we can write the catalog file whether it's there or not. 
                throw new IOException("Cannot write to catalog file: " + catalogFile);
            }
            File targetDir = new File(targetDirPath);
            if (!targetDir.isDirectory()) {
                throw new IOException("TargetDir: " + targetDir + " is not a directory.");
            }
            ArrayList catalog = new ArrayList();
            for(Iterator it = sourceDirs.iterator(); it.hasNext(); ) {
                String sourceDir = (String)it.next();
                addSoundFiles(catalog, sourceDir);
            }
            writeCatalogFile(catalogFile, catalog);
            copySoundFiles(catalog, targetDir, catalogFile + "_copies.sh");
            return catalog.size();
        } catch (Exception ex) {
            System.out.println("SongCompiler caught exception creating catalog: " + ex);
            ex.printStackTrace();
            System.exit(1);
        }
        return -1;
    }

    public void addSoundFiles(ArrayList catalog, String sourceDirPath) {
        File sourceDir = new File(sourceDirPath);
        addSoundFiles(catalog, sourceDir);
    }
            
        
    public void addSoundFiles(ArrayList catalog, File sourceDir) {
        File[] filesAndDirs = sourceDir.listFiles();
        for(int i=0; i < filesAndDirs.length; i++) {
            if (filesAndDirs[i].isDirectory()) {
                addSoundFiles(catalog, filesAndDirs[i]);
            } else {
                String ext = Sound.getExtension(filesAndDirs[i]);
                if (ext == null || ext.equals("")) {
                    // ignore this one
                } else if (ext.equalsIgnoreCase("mp3")) {
                    addMP3File(catalog, filesAndDirs[i]);
                } else if (ext.equalsIgnoreCase("aif") || ext.equalsIgnoreCase("aiff") || ext.equalsIgnoreCase("wav")) {
                    addPCMFile(catalog, filesAndDirs[i]);
                }
            }
        }
    }

    public void addMP3File(ArrayList catalog, File file) {
        Sound sound = makeSoundWithUniqueHash(catalog, file);
        sound.setPCMHash(null); // no PCM file. // Todo: rename this PCMHash
        catalog.add(sound);
    }

    public void addPCMFile(ArrayList catalog, File file) {
        Sound sound = makeSoundWithUniqueHash(catalog, file);
        catalog.add(sound);
    }

    public Sound makeSoundWithUniqueHash(ArrayList catalog, File file) {
        Sound sound = new Sound(null, file.getAbsolutePath());
        sound.findSoundDuration(); // Note: we might not (yet?) have something that works for MP3 files?
        int offset = 0;
        boolean foundUniqueHash = false;
        boolean conflictFound = false;
        while (offset < 1000 && !foundUniqueHash) {
            sound.calculateHash(null, offset);
            Sound conflictingSound = findConflict(catalog, sound);
            if (conflictingSound == null) {
                foundUniqueHash = true;
            } else {
                offset++;
                System.out.println("CONFLICT! Hash for sound " + sound + " conflicts with " + conflictingSound);
                conflictFound = true;
            }
        }
        if (conflictFound) {
            numConflicts ++;
        }
        return sound;
    }

    // ouch! we're n squared
    private Sound findConflict(ArrayList catalog, Sound sound) {
        // we test the mp3 hash, because not all files will have a PCM hash.
        // (possible alternative: store & test the hash base)
        String mp3Hash = sound.getMP3Hash();
        for(Iterator it = catalog.iterator(); it.hasNext(); ) {
            Sound test = (Sound)it.next();
            if (mp3Hash.equals(test.getMP3Hash())) {
                return test;
            }
        }
        return null;
    }
     
    /////////////////////////////////////////////////////////

    public void writeCatalogFile(File catalogFile, ArrayList catalog) throws IOException {
        FileWriter writer = new FileWriter(catalogFile);
        for(Iterator it = catalog.iterator(); it.hasNext(); ) {
            Sound sound = (Sound)it.next();
            String fpath = sound.getFullPath();
            writer.write(fpath, 0, fpath.length());
            writer.write(' ');
            String dur = "" + sound.getDuration();
            writer.write(dur, 0, dur.length());
            writer.write(' ');
            String pcmHash = sound.getPCMHash();
            if (pcmHash == null) {
                writer.write('-');
            } else {
                writer.write(pcmHash, 0, pcmHash.length());
            }
            String mp3Hash = sound.getMP3Hash();
            writer.write(' ');
            writer.write(mp3Hash, 0, mp3Hash.length());
            writer.write(' ');
            writer.write('\n');
        }
        writer.flush();
        writer.close();
    }
    
    /////////////////////////////////////////////////////////

    public void copySoundFiles(ArrayList catalog, File targetDir, String cmdFilePath) {
        try {
            PrintWriter cmdFileWriter = new PrintWriter(cmdFilePath);
            cmdFileWriter.println("#!/bin/sh");
            System.out.println("Copying " + catalog.size() + " sound files.");
            for(Iterator it = catalog.iterator(); it.hasNext(); ) {
                Sound sound = (Sound)it.next();
                String filepath = sound.getFullPath();
                String target = sound.getPCMHash();
                if (target == null) {
                    target = sound.getMP3Hash();
                }
                String cmd = copy(filepath, targetDir + "/" + target, false);
                if (cmd != null) {
                    cmdFileWriter.println(cmd);
                }
            }
            cmdFileWriter.close();
        } catch (Exception ex) {
            System.out.println("CopySoundFiles caught exception writing catalog file copy script: " + cmdFilePath);
            ex.printStackTrace();
        }
    }

    public String copy(String src, String tgt, boolean runcmd) {
        try {
            Runtime run=Runtime.getRuntime();
            String cmd="cp ";
            cmd=cmd+src+" "+tgt;
            System.out.println("Copy: cmd=[" + cmd + "]");
            if (runcmd) {
                run.exec(cmd);
            }
            return cmd;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }
    
    /////////////////////////////////////////////////////////
   
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
        if (args.length == 0) {
            usage();
            System.exit(0);
        }
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

        if (args[0].equalsIgnoreCase("compile")) {
            if (args.length < 4) {
                usage();
                System.exit(0);
            }

            String songFileName = args[1];
            String targetFileName = null; // default.
            String basePath = args[2];
            String compiledSongDir = args[3];
            
            SongCompiler scribe = new SongCompiler();
            System.out.println("Compiling...");
            scribe.compile(basePath, songFileName, targetFileName, compiledSongDir );
            System.out.println("Compiled.");
            System.exit(0);
        }

        if (args[0].equalsIgnoreCase("catalog")) {
            if (args.length < 5) {
                usage();
                System.exit(0);
            }
            String catalogFile = null;
            String targetDir = null;
            ArrayList sourceDirs = new ArrayList();
            int i=1;
            while(i < args.length) {
                if (args[i].equalsIgnoreCase("--catalog")) {
                    i++;
                    catalogFile = args[i];
                } else if (args[i].equalsIgnoreCase("--targetdir")) {
                    i++;
                    targetDir = args[i];
                } else {
                    sourceDirs.add(args[i]);
                }
                i++;
            }
            SongCompiler scribe = new SongCompiler();
            System.out.println("Catalogueing...");
            numConflicts = 0;
            int nSounds = scribe.catalog(catalogFile, targetDir, sourceDirs);
            System.out.println("Catalogued " + nSounds + " sounds. " + numConflicts + " conflicts found.");
            System.exit(0);
        }

        usage();
        System.exit(0);
    }
    
    private static void usage() {
        System.out.println("Usages: ");
        System.out.println("    java orb.swarm.composer.SongCompiler compile songFile songDir compiledSongDir");
        System.out.println("    java orb.swarm.composer.SongCompiler test compiledSongFile");
        System.out.println("    java orb.swarm.composer.SongCompiler catalog --catalog catalogFile --targetdir targetDir [directories]");
    }
}