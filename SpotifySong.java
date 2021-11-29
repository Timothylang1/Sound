import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;

import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;
import javax.sound.midi.Synthesizer;
import javax.sound.midi.Instrument;
import javax.sound.midi.MidiChannel;

public class SpotifySong {

    // Holds all of the possible notes that we can play
    private static final int speed = 150; // Time between notes in milliseconds, essentially how fast do we want to play the song
    private static final int closingTime = 5000 / speed; // Song will wrap up five seconds after the last note was played
    
    private static Scanner myReader;
    private static List<Integer> notesBeingPlayed = List.of(); // Tracks which notes are currently being played
    private static ArrayList<Integer> notesToPlay = new ArrayList<>();

    private static MidiChannel[] channels;
	private static int INSTRUMENT = 0; // 0 is a piano, 9 is percussion, other channels are for other instruments
	private static int VOLUME = 80; // between 0 et 127
    private static Synthesizer synth;
    
    private static int closingCounter = 0;

    private static int beatCounter = 1; // Set at one so the beats don't start immediately. Sounds weird if it's set at 0;

    private static final int barLength = 8; // Try 6 for jazzy, 8 for swing, 4 fast paced, 12 slow paced

    private static final String songName = "song"; // Change this to access a specific song saved

    public static void main(String[] args) {
        
        // Load files
        File myObj = new File("songs/" + songName + ".txt");
        try {
            myReader = new Scanner(myObj);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        
        // Opens synthesizer to start playing notes
        try {
			// * Open a synthesizer
			synth = MidiSystem.getSynthesizer();
			synth.open();
            
            // Everything after is for testing new instruments. Comment out if you want to just play the song.

            channels = synth.getChannels();

            Instrument[] instruments = synth.getAvailableInstruments();
            Arrays.stream(instruments).forEach(System.out::println); // Prints all of the possible instruments

            // Scanner used to keep track of good sounds
            // Scanner scan = new Scanner(System.in);

            // ArrayList<Integer> coolSounds = new ArrayList<>();

            // channels[0].programChange(0, 114); // We can load any instrument into the sound bank, currently replaces the piano in channel 0

            List<Integer> coolPercussion = List.of(27, 28, 31, 34, 35, 36, 37, 38, 39, 42, 46, 51, 52, 55, 59, 67, 80, 83, 86, 87);

            coolPercussion.forEach(x -> {
                System.out.println("Percussion note number = " + x);

                for (int counter = 0; counter < 3; counter++) {
                    channels[9].noteOn(x, 80);
                    try {
                        Thread.sleep(600); 
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });

            // for (int counter = 0; counter <= 127; counter++) {
            //     System.out.println("Percussion note number = " + counter);
            //     for (int counter2 = 0; counter2 < 3; counter2++) {
            //         channels[9].noteOn(counter, 80);
            //         Thread.sleep(600);                    
            //     }
            //     // System.out.println(synth.getDefaultSoundbank().getInstruments()[counter].getName()); // Gets name of instrument being played
            //     String saved = scan.nextLine();
            //     if (saved.equalsIgnoreCase("y")) {
            //         coolSounds.add(counter);
            //     }
            // }

            // scan.close();
            // // Prints all of the cool recorded soundsn
            // coolSounds.forEach(System.out::println);
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        // Creates a timer so it sends output is roughly FRAMERATE times a second
        Timer timer = new Timer();
        TimerTask task = new TimerTask() {
            public void run() {

                // If the current note being played is not in the notes to play, then stop playing that note
                notesBeingPlayed.forEach(x -> {
                    if (!notesToPlay.contains(x)) {
                        // Stops playing a note
                        channels[INSTRUMENT].noteOff(x + 20); // Middle C in synth is 60 while my program treats it as 40, so we need to add 20 to have the synth play the right note
                    }
                    else {
                        // Attempt to put bars into music to make it sound more rythmic. Every 6 counts, it replays all of the notes being played. The next section adds in all of the new notes
                        if (beatCounter % barLength == 0) {
                            channels[INSTRUMENT].noteOn(x + 20, VOLUME);
                        }
                    }
                });

                // If the note to play is not in the notes being played right now, then begin playing that note
                notesToPlay.forEach(x -> {
                    if (!notesBeingPlayed.contains(x)) {
                        // Starts playing a note
			            channels[INSTRUMENT].noteOn(x + 20, VOLUME);
                    }
                });

                // All of the notes to play are now the notes currently being played
                notesBeingPlayed = notesToPlay.stream().toList();


                // Clears notes to be played for the next batch of notes
                notesToPlay.clear();

                // After playing notes, then it reads which notes to play next
                if (myReader.hasNextLine()) {
                    // Adds in a beat
                    if (beatCounter % barLength == 0) {
                        channels[9].noteOn(34, VOLUME); // Channel 9 is percussion
                    }
                    if (beatCounter % (barLength / 2) == 0) {
                        channels[9].noteOn(87, VOLUME); // 27 is the lowest percussion instrument, 87 is the max
                    }
                    beatCounter++;

                    // Then anaylze data
                    String data = myReader.nextLine();
                    String[] data1 = data.split(" ");
                    for (int closingCounter = 0; closingCounter < data1.length; closingCounter++) {
                        int note = Integer.parseInt(data1[closingCounter]);
                        if (note != 0) { // Rest
                            notesToPlay.add(note);
                        }
                    }
                }

                else {
                    if (closingCounter == 0) { // If this is the first time reaching this point, then start shutting down notes
                        notesBeingPlayed.forEach(x -> {
                            channels[INSTRUMENT].noteOff(x + 20);
                        });
                    }

                    else if (closingCounter == closingTime) { // After a certain amount of time, then closes everything
                        // Closes synthesizer
                        
                        synth.close();

                        // Stops the timer
                        timer.cancel();

                        System.out.println("Song completed");
                    }

                    closingCounter++;
                }
            }
        };
        timer.schedule(task, 0, speed); // Set delay after everyone is connected, change the 0 to number of milliseconds
        System.out.println("Beginning song");
    }
}
