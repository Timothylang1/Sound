import java.io.File;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.Control;
import javax.sound.sampled.FloatControl;


public class loadedSound {

    private Clip clip;
    
    public loadedSound(int noteNumber) {
        String pathname = "NoteSounds/" + noteNumber + ".aiff";
        try {
            File file = new File(pathname);
            AudioInputStream stream = AudioSystem.getAudioInputStream(file);

            // NOTE: TO GET THE TOTAL DURATION
            // int totalDuration; // total duration of song in milliseconds if you want to play the full song
            // totalDuration = Math.round(stream.getFrameLength() / stream.getFormat().getSampleRate() * 1000);

            clip = AudioSystem.getClip();
            clip.open(stream);
            // Control[] control = clip.getControls();
            // for (int counter = 0; counter < control.length; counter++) {
            //     System.out.println(control[counter]);
            // }
            System.out.println("Loaded " + noteNumber);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public float getVolume() {
        FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);        
        return (float) Math.pow(10f, gainControl.getValue() / 20f);
    }
    
    public void setVolume(float volume) {
        if (volume < 0f || volume > 1f)
            throw new IllegalArgumentException("Volume not valid: " + volume);
        FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);        
        gainControl.setValue(20f * (float) Math.log10(volume));
    }

    public void play() {
        clip.start();
    }

    public void stop() {
        clip.stop();
        clip.setMicrosecondPosition(0);
    }

    public void close() {
        clip.close();
    }
}
