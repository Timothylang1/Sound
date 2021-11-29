import java.io.File;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

public class convertFile {
    public static void main(String[] args) {
        convert("testRecording.mid", "recording.WAV");
    }

    public static void convert(String inputPath, String outputPath) {
        AudioFileFormat inFileFormat;
        File inFile;
        File outFile;
        try {
            inFile = new File(inputPath);
            outFile = new File(outputPath);     
        }
        catch (NullPointerException ex) {
            System.out.println("Error: one of the ConvertFileToWAVE" +" parameters is null!");
            return;
        }
        try {
            // query file type
            inFileFormat = AudioSystem.getAudioFileFormat(inFile);
            if (inFileFormat.getType() != AudioFileFormat.Type.WAVE) {
                // inFile is not WAVE, so let's try to convert it.
                AudioInputStream inFileAIS = AudioSystem.getAudioInputStream(inFile);
                inFileAIS.reset(); // rewind
                if (AudioSystem.isFileTypeSupported(
                    AudioFileFormat.Type.WAVE, inFileAIS)) {
                    // inFileAIS can be converted to WAVE. 
                    // so write the AudioInputStream to the
                    // output file.
                    AudioSystem.write(inFileAIS,
                    AudioFileFormat.Type.WAVE, outFile);
                    System.out.println("Success");
                    inFileAIS.close();
                    return; // All done now
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
