package com.orbswarm.swarmcomposer.sound;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;


public class JSoundTest {

    public JSoundTest() {
    }
    public void setup() {
    }

    public  static final int EXTERNAL_BUFFER_SIZE = 128000;

    public void startPlaying(String soundFilePath, int channel) {
        AudioInputStream	audioInputStream = null;
		try	{
            File soundFile = new File(soundFilePath);
			audioInputStream = AudioSystem.getAudioInputStream(soundFile);
		} catch (Exception e) {
			e.printStackTrace();
		}
        AudioFormat	audioFormat = audioInputStream.getFormat();

        SourceDataLine line = null;
		DataLine.Info info = new DataLine.Info(SourceDataLine.class,
												 audioFormat);
		try {
			line = (SourceDataLine) AudioSystem.getLine(info);
			line.open(audioFormat);
		} catch (LineUnavailableException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

		line.start();

        int nBytesRead = 0;
		byte[]	abData = new byte[EXTERNAL_BUFFER_SIZE];
		while (nBytesRead != -1) {
			try {
				nBytesRead = audioInputStream.read(abData, 0, abData.length);
			} catch (IOException e)	{
				e.printStackTrace();
			}
			if (nBytesRead >= 0) {
				int	nBytesWritten = line.write(abData, 0, nBytesRead);
			}
		}
        line.drain();

		line.close();
    }

    public void waitForIt(int channel) {
    }
    
    public static void main(String[] args) {
        System.out.println("JSoundTest. ");
        Mixer.Info[] mixerInfos = AudioSystem.getMixerInfo();
        System.out.println("Mixer infos: " + mixerInfos.length);
        for(int i=0; i < mixerInfos.length; i++) {
            Mixer.Info mi = mixerInfos[i];
            System.out.println("MixerInfo: " + mi);
            System.out.println("    Name: " + mi.getName());
            System.out.println("    Version: " + mi.getVersion());
            System.out.println("    Vendor: " + mi.getVendor());
            System.out.println("    Description: " + mi.getDescription());
            Mixer mixer = AudioSystem.getMixer(mi);
            System.out.println("      maxLines(MIC): " + mixer.getMaxLines(Port.Info.MICROPHONE));
            System.out.println("      maxLines(HEADPHONE): " + mixer.getMaxLines(Port.Info.HEADPHONE));
            System.out.println("      maxLines(LINE_OUT): " + mixer.getMaxLines(Port.Info.LINE_OUT));
            System.out.println("      maxLines(LINE_OUT): " + mixer.getMaxLines(Port.Info.SPEAKER));
            Line.Info[] sources = mixer.getSourceLineInfo();
            System.out.println("      SOURCE LINE INFOs: " + sources.length);
            for(int s=0; s < sources.length; s++) {
                System.out.println("        " + sources[s]);
            }
            Line.Info[] targets = mixer.getTargetLineInfo();
            System.out.println("      TARGET (OUTPUT) LINE INFOs: " + targets.length);
            for(int s=0; s < targets.length; s++) {
                System.out.println("        " + targets[s]);
            }
        }

        JSoundTest jst = new JSoundTest();
        for(int i=0; i < args.length; i++) {
            String filename = args[i];
            int channel = 0;
            System.out.println("Playing["+ channel + "]: " + filename);
            jst.startPlaying(filename, channel);
            jst.waitForIt(channel);
            System.out.println("Finished["+ channel + "]: " + filename);
        }
        System.exit(0);
    }
}