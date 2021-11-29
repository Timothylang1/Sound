import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;
import javax.sound.midi.MidiEvent;

public class recordingSong {

    // Link: http://www.java2s.com/Code/JavaAPI/javax.sound.midi/MidiSystemwriteSequenceininttypeFileout.htm

    // Holds all of the possible notes that we can play
    
    private static Scanner myReader;
    private static List<Integer> notesBeingPlayed = List.of(); // Tracks which notes are currently being played
    private static ArrayList<Integer> notesToPlay = new ArrayList<>();
    
    private static int beatCounter = 1; // Set at one so the beats don't start immediately. Sounds weird if it's set at 0;

    private static final int barLength = 8; // Try 6 for jazzy, 8 for swing, 4 fast paced

    private static final int VOLUME = 100;

    private static Track track; // Track for all sounds to be written too. Can hold up to 16 channels, starting from 0

    private static Sequence sequence;

    // Instruments
    private static final int instrument1 = 11; // Vibraphone

    // Input and output files
    private static final String songName = "song"; // Change this to access a specific song saved

    private static final String outputName = "testChannels"; // FIle to be written too

    // Speed
    private static int ticksPerQuarter = 500; // Plays two notes per second converted to milliseconds

    private static final int speed = 150; // Time between successive quarter notes in milliseconds (CHANGE SPEED HERE)

    public static void main(String[] args) {

        // Load files
        File myObj = new File("songs/" + songName + ".txt");
        try {
            myReader = new Scanner(myObj);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        
        // This is what the song will be recorded to
        try {
            sequence = new Sequence(Sequence.PPQ, ticksPerQuarter); // Ticks per quarter note
            track = sequence.createTrack();
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        // Adds instruments
        instrumentSetup(instrument1, 0); // Piano goes on channel 0
        // instrumentSetup(instrument2, 1); // Percussion IS on channel 9 unless we change it

        while (myReader.hasNextLine()) {
            // Analyze data
            String data = myReader.nextLine();
            String[] data1 = data.split(" ");
            for (int counter = 0; counter < data1.length; counter++) {
                int note = Integer.parseInt(data1[counter]);
                if (note != 0) { // Rest
                    notesToPlay.add(note);
                }
            }

            // If the current note being played is not in the notes to play, then stop playing that note
            notesBeingPlayed.forEach(x -> {
                if (!notesToPlay.contains(x)) {
                    // Stops playing a note
                    noteOff(beatCounter, x + 20, 0); // Middle C in synth is 60 while my program treats it as 40, so we need to add 20 to have the synth play the right note
                }
                else {
                    // Attempt to put bars into music to make it sound more rythmic. Every 6 counts, it replays all of the notes being played. The next section adds in all of the new notes
                    if (beatCounter % barLength == 0) {
                        noteOn(beatCounter, x + 20, 0);
                    }
                }
            });

            // If the note to play is not in the notes being played right now, then begin playing that note
            notesToPlay.forEach(x -> {
                if (!notesBeingPlayed.contains(x)) {
                    // Starts playing a note
                    noteOn(beatCounter, x + 20, 0);
                }
            });

            // All of the notes to play are now the notes currently being played
            notesBeingPlayed = notesToPlay.stream().toList();


            // Clears notes to be played for the next batch of notes
            notesToPlay.clear();

            // Adds in a beat
            if (beatCounter % barLength == 0) {
                noteOn(beatCounter, 34, 9); // Channel 9 is percussion
            }
            if (beatCounter % (barLength / 2) == 0) {
                noteOn(beatCounter, 87, 9); // 27 is the lowest percussion instrument, 87 is the max
            }
            beatCounter++;
        }

        // Add in closing part to song (notes die off) -------------------------


        // Saves recording as MIDI file
        int[] allowedTypes = MidiSystem.getMidiFileTypes(sequence);
        if (allowedTypes.length == 0) {
            System.err.println("No supported MIDI file types.");
        }
        else {
            try {
                MidiSystem.write(sequence, allowedTypes[0], new File("SavedSongs/" + outputName + ".midi"));
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        System.out.println("Finished");
    }

    private static void instrumentSetup(int instrument, int channel) {
        ShortMessage sm = new ShortMessage();
        try {
            sm.setMessage(ShortMessage.PROGRAM_CHANGE, channel, instrument, 0);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        track.add(new MidiEvent(sm, 0));
    }

    private static void noteOn(int Tick, int key, int channel) {
        try {
            ShortMessage on = new ShortMessage();
            on.setMessage(ShortMessage.NOTE_ON, channel, key, VOLUME);
            track.add(new MidiEvent(on, Tick * speed)); // Might need to change the timing for both on and off
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void noteOff(int Tick, int key, int channel) {
        try {
            ShortMessage off = new ShortMessage();
            off.setMessage(ShortMessage.NOTE_OFF, channel, key, VOLUME); // Sets velocity to default
            track.add(new MidiEvent(off, Tick * speed));
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
