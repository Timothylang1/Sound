import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.List;
import java.util.ArrayList;

import javax.sound.sampled.*;

public class Testing {
    // https://stackoverflow.com/questions/3297749/java-reading-manipulating-and-writing-wav-files
    // https://docs.oracle.com/javase/8/docs/technotes/guides/sound/programmer_guide/chapter7.html#a114527
    // http://www.labbookpages.co.uk/audio/javaWavFiles.html
    // https://stackoverflow.com/questions/2416935/how-to-play-wav-files-with-java

    // Used to convert to wav file
    // https://loader.to/en24/youtube-wav-converter.html

    // Piano notes for testing
    // https://www.youtube.com/watch?v=Janf9Q3Cj94&t=36s

    // Anaylzing piano notes
    // https://en.wikipedia.org/wiki/Piano_key_frequencies

    // Analyzing chord progressions
    // https://pages.mtu.edu/~suits/chords.html

    // Testing song youtube file
    // https://www.youtube.com/watch?v=EaUQbQ2PL-4


    private static File fileIn, fileout;
    private static ArrayList<byte[]> list = new ArrayList<>();
    private static ArrayList<AudioInputStream> audioInputStreamList = new ArrayList<>();
    private static ArrayList<String> output = new ArrayList<>(); // Made it a list of one variable so that it can be accessed by construtor and static methods

    public Testing(List<String> pathname, String output1, boolean isSongWavFile) {
        output.add(output1);
        pathname.forEach(word -> {
            fileIn = new File("SoundInput/" + word + ".wav");
            try {
                // Read audio input stream
                AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(fileIn);
                audioInputStreamList.add(audioInputStream);    
    
                // Gets all of the bytes in a file (all of the data)
                byte[] array = audioInputStream.readAllBytes().clone();
                list.add(array);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        });

        // If it's a song wavfile from the website, make sure that all of the input songs have the same type
        // If all of these tests pass, then we can say that all of the songs have the same settings, and we can use there audioinput to set up
        // the audio output file (for the format)
        if (isSongWavFile) {
            float sampleRate = 48000;
            // int lengthinseconds = 8; // in Seconds, how long is the audio file going to be
            int frameSize = 4;
            int sampleSizeInBits = 16;
            int channels = 2;
            // int totalLength = lengthinseconds * SampleRate;
            audioInputStreamList.forEach(inputStream -> {
                if (inputStream.getFormat().getChannels() != channels) { // Should be 2 channels
                    throw new IllegalArgumentException("Channels doesn't match");
                }
                if (inputStream.getFormat().isBigEndian()) { // Should be false for isBigEndian
                    throw new IllegalArgumentException("BigEndian doesn't match");
                }
                if (Math.abs(inputStream.getFormat().getFrameSize() - frameSize) > 0.1) { // Should have framesize of 4
                    throw new IllegalArgumentException("FrameSize doesn't match");
                }
                if (Math.abs(inputStream.getFormat().getSampleRate() - sampleRate) > 0.1) { // Should have samplerate of 48000
                    throw new IllegalArgumentException("FrameRate doesn't match");
                }
                if (Math.abs(inputStream.getFormat().getSampleSizeInBits() - sampleSizeInBits) > 0.1) { // Should have a sampleSizeinBits of 16
                    throw new IllegalArgumentException("SampleSizeinBits doesn't match");
                }
            });
        }
    }

    public static List<byte[]> getArrayInfo() {
        return list; // Arrays length = (SampleSize * length (in seconds) * frameSize) OR audioInputStream.getFrameLength()
    }

    public static List<AudioInputStream> getAudioInputStreamList() {
        return audioInputStreamList;
    }

    /**
     * Isolates section in song
     */
    public static void isolatePart(int startTimeInMilliSeconds, int endTimeInMilliSeconds) {
        byte[] array = list.get(0);
        byte[] newarray = new byte[48 * (endTimeInMilliSeconds - startTimeInMilliSeconds) * 4];
        for (int counter = 0; counter < newarray.length; counter++) {
            int time = counter + 48 * startTimeInMilliSeconds * 4;
            newarray[counter] = array[time];
        }
        long frameLength = 48 * (endTimeInMilliSeconds - startTimeInMilliSeconds); // Framelength = samplesize * seconds
        write(audioInputStreamList.get(0).getFormat(), newarray, frameLength);
    }

    /**
     * Used if trying to create an audio file with a different length, but the same format as the original (in this case, a WAV song file)
     */
    public static void createNewWavAudioInputStream(long lengthinseconds, byte[] array) {
        AudioFormat format = new AudioFormat(48000, 16, 2, true, false); // Try changing true to false if nessecary (only unknown constant for me)
        write(format, array, lengthinseconds * 48000);
    }

    /**
     * Trying to create an entirely new format (Adjust speed, sample size, speed, etc.)
     */
    public static void createNewFormat(float SampleRate, int SampleSizeinBits, int Channels, long frameLength, byte[] array) {
        AudioFormat format = new AudioFormat(SampleRate, SampleSizeinBits, Channels, true, false);
        write(format, array, frameLength);
        
    }

    /**
     * If used directly, you can use it to alter a wav input stream, but preserve the format (i.e the length of it)
     */
    public static void write(AudioFormat format, byte[] array, long frameLength) {
        ByteArrayInputStream base = new ByteArrayInputStream(array);
        AudioInputStream inputStream = new AudioInputStream(base, format, frameLength);
        String totaloutput = "SoundOutput/" + output.get(0) + ".wav";
        fileout = new File(totaloutput);
        try {
            AudioSystem.write(inputStream, AudioFileFormat.Type.WAVE, fileout);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
