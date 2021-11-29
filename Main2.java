import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.stream.Collectors;

import Graphics.*;
import java.awt.Color;


import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import Logic.MarkVShaney;

/**
 * READ THIS FOR CONSTRAINTS
 * Reads frequencies in 0.2 second chunks
 * Only recognizes major/minor chord progressions
 * Only takes up to 6 notes max
 * Because it always analyzes six notes, some notes that weren't played might slip through, but they will only if they're part of the chord progression
 * If there is a melody overlayed in song, it might become unpredictable (REQUIRES FURTHER TESTING)
 * 
 * READ THIS FOR ORDER OF EXECUTION:
 * 1. Get audio from youtube video as wav file. using this link: https://loader.to/en24/youtube-wav-converter.html, and put it into the soundInput folder
 * 2. Change the input pathWay to the list of audio files you want analyzed
 * 3. Run setup() and frequenciesTest() ONLY. They should write to transfer.txt file
 * 4. Run fft2.py file, but only through run and debug. Overwrites transfer.txt
 * 5. Change songName variable to the output song name you want. The final song will appear in the SheetMusic folder, under the folder titled your song name
 * 6. Run readFile(), MarkVShaney(), and sheetMusic(). 
 * readFile() converts frequencies to pianoNotes, and writes them to chords.txt file. 
 * MarkVShaney analyzes chord progressions, and writes it's own song to the songs folder
 * sheetMusic() converts notes to sheet music, then saves it in SheetMusic folder under the folder with the title songName.
 * 
 * NOTE: if any part of this process goes wrong, you can clear the textfile that went wrong using the clear method
 * While this process may be slow because each part writes to a textfile, it allows the user to visually see each step of the way, 
 * and also makes any changes made easy to identify instead of everything happening "behind your back" without visual confirmation
 */
public class Main2 {
    private static List<String> inputPathWay = List.of("RelaxingPianoMusic");
    private static List<byte[]>list;
    private static byte[] newarray;
    private static FileWriter myWriter;
    private static String message = "";

    // Used to keep track of sharps' y coordinates, so we know when to add naturals
    private static ArrayList<Integer> sharps = new ArrayList<>();

    // Graphics group used to keep track of notes
    private static GraphicsGroup notesGroup = new GraphicsGroup();

    // Constants used to set up the notes position
    private static final double noteStartX = 100;
    private static final int notesPerBar = 24;
    // The bar ends at 950
    private static final double noteIncrement = (950 - noteStartX) / (notesPerBar * 2 + 1); // There are 6 notes, but because we want to leave a little space on the ends, we add one, so we divide the span of the bars by 16 + 1
    private static final int clefSize = 90; // difference between the base line of one clef to the next
    private static final int halfStep = 4; // To get from one line to the next, you have to add 8.

    // Tracker used to track clef Number
    private static int clefNumber = clefSize * 2;

    // String that helps with saving song to file location
    private static final String songName = "Song1";

    public static void setup() {
        new Testing(inputPathWay, "", true);
        list = Testing.getArrayInfo();
    }
    public static void main(String[] args) {
        // setup();
        // frequenciesTest(); // Run to analyze frequencies
        // readFile(); // Then read the file after running python file for fft analysis, and convert frequencies to notes. Then writes notes to chords file
        new MarkVShaney(5, 3000, "song"); // Then analyzes notes, then creates song which it writes to another file. The first part is the context size. The second is the length of the song
        // sheetMusic(); // Then converts the notes to sheet music
        // clear("song.txt"); // Clears file that is given as an input parameter

        // createBlankSheet(); // Creates blank sheet of music
    }

    // DIFFERENT TEST CASES

    
    // TEST 9: We know that if we get all of the odd parts of the array, we get sinusodial patterns that match the frequencies of a piano
    // Can we use that knowledge to effectively determine the piano notes?

    private static void frequenciesTest() {
        // Creates text file that puts data so that python file can analyze further
        // Open File 
        try {
            myWriter = new FileWriter("transfer.txt");
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        for (byte[] array : list) { // array = original array. Anaylzes each sound file in the list
            newarray = new byte[array.length / 2]; // Modified array with the only part we need (suspect it's an isolated channel)
            for (int counter = 0; counter < newarray.length; counter++) {
                newarray[counter] = array[counter * 2 + 1];
            }

            // Analysis
            
            System.out.println("Beginning analysis...");
            for (int counter2 = 1; counter2 < newarray.length; counter2 += 1) {
                message += newarray[counter2] + " ";
                if (counter2 % 19200 == 0) { // 19200 was picked because that way, each sample size is 0.2 seconds long
                    try {
                        myWriter.write(message + "\n");
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                    message = "";
                    System.out.println("Percent done = " + 100.0f * counter2 / newarray.length + "%");
                }

            }
        }
        
        try {
            myWriter.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("Done with analysis");
    }

    // TEST 10: can we get rid of "incorrect data" (in this case, we filter so we only accept major or minor progressions)
    // NEXT STEP: Dissonance chords
    private static void readFile() {
        try {
            File myObj = new File("transfer.txt");
            Scanner myReader = new Scanner(myObj);
            myWriter = new FileWriter("chords.txt");

            // Creates an arraylist that we can reuse
            HashSet<Integer> notes = new HashSet<>();

            // Creates list of chord progression
            // Major / Minor: [[3, [3, 7, 8]], [4, [4, 7, 9]], [5, [5, 8, 9]], [7, [7, 3, 4]], [8, [8, 3, 5]], [9, [9, 4, 5]]]
            HashMap<Integer, Set<Integer>> chord = new HashMap<>();
            chord.put(3, Set.of(3, 7, 8));
            chord.put(4, Set.of(4, 7, 9));
            chord.put(5, Set.of(5, 8, 9));
            chord.put(7, Set.of(7, 3, 4));
            chord.put(8, Set.of(8, 3, 5));
            chord.put(9, Set.of(9, 4, 5));


            while (myReader.hasNextLine()) {
                // This section takes a line and converts it into an array of integers that are the piano notes
                String data = myReader.nextLine();
                String[] data1 = data.split(" ");
                
                // List that keeps track if we've found the second note in the chord progression
                Set<Integer> toSearchFor = chord.keySet();
                
                // Keeps track of third note in progression
                int keyThatWorked = 0;

                // Creates first note that will be used for analysis. Reason why the first note is the center of analysis is because it is the one that is the most correct out of test cases so far because it has the strongest PNG value
                int firstNote = 0; // Randomly picked note
                for (int counter = 0; counter < data1.length; counter++) {
                    double frequency = Double.parseDouble(data1[counter]);
                    // Equation from https://en.wikipedia.org/wiki/Piano_key_frequencies
                    int pianoNote = (int) (12.0 * Math.log10(2400.0 * frequency /11.0) / Math.log10(2.0) + 49.0 + 0.5); // The 0.5 is for truncating the double, then casting it to an integer

                    // Special case: if the pianoNote is less than 0, then that means that it's 0. If the first pianoNote is 0, then they're all 0, se we break and move onto the next line
                    if (pianoNote < 0) {
                        notes.add(0);
                        break;
                    }

                    // Analysis of notes
                    if (counter != 0) { // Skips the first note and adds it directly
                        // This part checks to see that I'm not accidentally letting wrong notes through, so it checks that the chord progression being created at this moment
                        // is part of a major or minor chord. Check Testing.java for link with info about this

                        // Converts note to within range
                        int currentNote = pianoNote + ((firstNote - pianoNote) / 12) * 12; // Puts them in range of each other
                        if (currentNote < firstNote) {
                            currentNote += 12; // Ensures that the currentNote isn't below the firstNote
                        }
                        int difference = currentNote - firstNote;


                        // After converting notes to range, then we start filtering out notes we don't care about
                        if (difference == 0) { // If difference is 0, then it's the same note, and we just add it
                            notes.add(pianoNote);
                        }
                        else {
                            if (toSearchFor.size() == chord.keySet().size()) { // If we haven't found the first note
                                if (toSearchFor.contains(difference)) { // Now we have
                                    toSearchFor = chord.get(difference);
                                    notes.add(pianoNote);
                                    keyThatWorked = difference;
                                }
                            }
                            else {
                                if (toSearchFor.size() == 3) { // If we haven't found the second note
                                    if (toSearchFor.contains(difference)) {
                                        notes.add(pianoNote); // Now we have
                                        if (difference != keyThatWorked) { // Now we know if it's major or minor chord
                                            toSearchFor = Set.of(keyThatWorked, difference);
                                        }
                                    }
                                }
                                else {
                                    if (toSearchFor.contains(difference)) { // After we know all three notes in progression, just add the notes directly
                                        notes.add(pianoNote);
                                    }
                                }
                            }
                        }
                    }
                    else { // We always want to add the first pianoNote
                        notes.add(pianoNote);
                        firstNote = pianoNote;
                    }
                }

                // Sorts the notes that made it past the filter, and gets rid of any that are less than 2 half steps apart
                // This part of the analysis can only occur after we've gone through all of the notes
                List<Integer> list = notes.stream().sorted().collect(Collectors.toList()); // First, sorts notes

                message = "";
                for (int note : list) {
                    message += note + " ";
                }
                myWriter.write(message + "\n");

                notes.clear();
            }
            myReader.close();
            myWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private static void clear(String file) {
        try {
            myWriter = new FileWriter(file);
            myWriter.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void sheetMusic() {
        /**
         * Info about sheet of music:
         * There are 9 total rows, each with 8 bars in them. The smallest note we'll play is an eighth
         * Starts at 48, ends at 1428
         * There are 14 lines per row
         * Anything past middle C (40) starts at line 8
         * The first line starts at 75, then 160, then 242
         * Each row has a height of 62
         * From B (39) to C (40), that is a half step (This is for every B and C)
         * From E (44) to F (45), that is a half step
         */
        // TEST 1: add notes to sheet music in correct spot

        final int CANVAS_WIDTH = 1000;
        final int CANVAS_HEIGHT = 800;
        CanvasWindow canvas = new CanvasWindow("Sheet music", CANVAS_WIDTH, CANVAS_HEIGHT);

        /**
         * There are 14 total lines
         * To calculate where the next baseline is, take clefSize and multiply by two
         */

        Image blankSheet = new Image("SheetMusic/blankSheetMusic.png");
        blankSheet.setMaxWidth(CANVAS_WIDTH);
        canvas.add(blankSheet);

        HashMap<Integer, Integer> tracker = new HashMap<>(); // Tracks all of the previous notes (helps determine if it should write a whole, an eigth note, etc.)
        canvas.add(notesGroup);

        /**
         * Chart:
         * First number: placement with respect to the lowest line in the clef G (23)
         * Second number: 0 if it's normal, 1 if it's sharp
         * If there is no second number, don't add a sharp
         * Third number: if it's a positive number, then that's how many lines to add (in case the note is not on the clef, like middle C)
         * If there is no third number, don't add lines
        */ 
        List<List<Integer>> placement = List.of(List.of(0, 0), 
        List.of(-13, 0, 6), List.of(-13, 1, 6), List.of(-12, 0, 6), List.of(-11, 0, 5), List.of(-11, 1, 5), List.of(-10, 0, 5), List.of(-10, 1, 5), List.of(-9, 0, 4), List.of(-8, 0, 4), List.of(-8, 1, 4), List.of(-7, 0, 3), List.of(-7, 1, 3),
        List.of(-6, 0, 3), List.of(-6, 1, 3), List.of(-5, 0, 2), List.of(-4, 0, 2), List.of(-4, 1, 2), List.of(-3, 0, 1), List.of(-3, 1, 1), List.of(-2, 0, 1), List.of(-1, 0), List.of(-1, 1), List.of(0, 0), List.of(0, 1), 
        List.of(1, 0), List.of(1, 1), List.of(2, 0), List.of(3, 0), List.of(3, 1), List.of(4, 0), List.of(4, 1), List.of(5, 0), List.of(6, 0), List.of(6, 1), List.of(7, 0), List.of(7, 1), 
        List.of(8, 0), List.of(8, 1), List.of(9, 0), List.of(16, 0, 1), List.of(16, 1, 1), List.of(17, 0), List.of(17, 1), List.of(18, 0), List.of(19, 0), List.of(19, 1), List.of(20, 0), List.of(20, 1),
        List.of(21, 0), List.of(21, 1), List.of(22, 0), List.of(23, 0), List.of(23, 1), List.of(24, 0), List.of(24, 1), List.of(25, 0), List.of(26, 0), List.of(26, 1), List.of(27, 0), List.of(27, 1), 
        List.of(28, 0, 1), List.of(28, 1, 1), List.of(29, 0, 1), List.of(30, 0, 2), List.of(30, 1, 2), List.of(31, 0, 2), List.of(31, 1, 2), List.of(32, 0, 3), List.of(33, 0, 3), List.of(33, 1, 3), List.of(34, 0, 4), List.of(34, 1, 4), 
        List.of(35, 0, 4), List.of(35, 1, 4), List.of(36, 0, 5), List.of(37, 0, 5), List.of(37, 1, 5), List.of(38, 0, 6), List.of(38, 1, 6), List.of(39, 0, 6), List.of(40, 0, 7), List.of(40, 1, 7), List.of(41, 0, 7), List.of(41, 1, 7),
        List.of(42, 0, 8), List.of(42, 1, 8), List.of(43, 0, 8), List.of(44, 0, 9));
        

        try { // TEST 1: ANAYLZE AND PUT THE FIRST 6 NOTES ON THE SCREEN

            // Creates a new directory file to store the song files in
            File theDir = new File("SheetMusic/" + songName);
            if (!theDir.exists()) {
                theDir.mkdirs();
            }
            else {
                throw new Exception("File already exists. Must create a unique song name, or delete previous song with the same name.");
            }

            File myObj = new File("song.txt");
            Scanner myReader = new Scanner(myObj);

            // Trackers

            // Track where on the sheet music we're currently at
            double noteNumber = noteStartX + noteIncrement;
            int pageNumber = 1;

            // Track length of rests
            boolean baseRest = true;
            boolean trebleRest = true;
            int baseRestLength = 0;
            int trebleRestLength = 0;

            // Splits the notes into base and treble to help with putting rests into place
            ArrayList<Integer> currentNotesPlayed = new ArrayList<>();

            while (myReader.hasNextLine()) {
                // This section takes a line and converts it into an array of integers that are the piano notes
                String data = myReader.nextLine();
                String[] data1 = data.split(" ");
                for (int counter = 0; counter < data1.length; counter++) {
                    int note = Integer.parseInt(data1[counter]);
                    if (note != 0) {
                        if (note < 40) { // Base note
                            baseRest = false; // Used to keep track of rest length in the base clef
                        }
                        else {
                            trebleRest = false;
                        }
                        currentNotesPlayed.add(note);
                    }
                }

                // Adds the current notes being played to the tracker, which keeps track of the duration of the notes
                for (int x : currentNotesPlayed) {
                    if (tracker.keySet().contains(x)) {
                        int newValue = tracker.get(x) + 1;
                        tracker.replace(x, newValue); // Adds one to the duration of the note
                    }
                    else {
                        tracker.put(x, 1); // Adds new category of note, and sets its duration to 1
                    }
                }

                // Adds any note whose duration has ended (we know the duration has ended because it is not currently being played)
                List<Integer> keys = tracker.keySet().stream().toList();
                for (int key : keys) {
                    if (!currentNotesPlayed.contains(key)) {
                        // x value (the noteNumber), y position, if it's sharp, duration
                        createNote(noteNumber, clefNumber - placement.get(key).get(0) * halfStep, placement.get(key).get(1), tracker.get(key));
                        // Removes the note from the hashMap because we don't need it anymore. It has already been played
                        tracker.remove(key);
                    }
                    else if (tracker.get(key) == 8) { // Maximum duration of any note will be 8
                        // Have to shift it over because the note is still going, it just ends at the current spot, not the previous spot like the other notes
                        createNote(noteNumber + noteIncrement * 2, clefNumber - placement.get(key).get(0) * halfStep, placement.get(key).get(1), tracker.get(key));
                        tracker.remove(key);
                    }
                }

                // Duration of rests for treble and base clef are calculated seperately
                if (!baseRest) {
                    List<Integer> split = duration(baseRestLength); // Divides the number into different types of rests
                    for (int restType = 0; restType < split.size(); restType++) {
                        for (int restDuration = 0; restDuration < split.get(restType); restDuration++) {
                            createRest(noteNumber - baseRestLength * noteIncrement * 2, clefNumber, restType + 1);
                            baseRestLength -= restType + 1;
                        }
                    }
                    // Resets
                    baseRest = true;
                }

                else {
                    baseRestLength += 1;
                }

                if (!trebleRest) {
                    List<Integer> split = duration(trebleRestLength); // Divides the number into different types of rests
                    for (int restType = 0; restType < split.size(); restType++) {
                        for (int restDuration = 0; restDuration < split.get(restType); restDuration++) {
                            createRest(noteNumber - trebleRestLength * noteIncrement * 2, clefNumber - 72, restType + 1);
                            trebleRestLength -= restType + 1;
                        }
                    }
                    // Resets
                    trebleRest = true;
                }

                else {
                    trebleRestLength += 1;
                }

                // Trackers

                // Note placement increases
                noteNumber += noteIncrement * 2;

                // Resets the current notes being played
                currentNotesPlayed.clear();

                // Resets position if we've reached the maximum number of notes per bar
                if (noteNumber > noteIncrement * 2 * (notesPerBar - 1) + noteStartX + noteIncrement) {
                    // Firsts, puts all of the remaining notes and rests on the screen

                    // Notes
                    for (int key : tracker.keySet()) {
                        createNote(noteNumber, clefNumber - placement.get(key).get(0) * halfStep, placement.get(key).get(1), tracker.get(key));
                    }

                    // Base rests
                    List<Integer> split = duration(baseRestLength); // Divides the number into different types of rests
                    for (int restType = 0; restType < split.size(); restType++) {
                        for (int restDuration = 0; restDuration < split.get(restType); restDuration++) {
                            createRest(noteNumber - baseRestLength * noteIncrement * 2, clefNumber, restType + 1);
                            baseRestLength -= restType + 1;
                        }
                    }
                    baseRest = true;

                    // Treble rests
                    split = duration(trebleRestLength); // Divides the number into different types of rests
                    for (int restType = 0; restType < split.size(); restType++) {
                        for (int restDuration = 0; restDuration < split.get(restType); restDuration++) {
                            createRest(noteNumber - trebleRestLength * noteIncrement * 2, clefNumber - 72, restType + 1);
                            trebleRestLength -= restType + 1;
                        }
                    }
                    trebleRest = true;

                    tracker.clear(); // Then clears the tracker
                    sharps.clear(); // Removes all of the sharps from that line
                    noteNumber = noteStartX + noteIncrement; // Resets noteNumber back to the original
                    clefNumber += clefSize * 2; // Puts the notes on the next staff down
                }

                // If we've finished the page
                if (clefNumber == clefSize * 2 * 5) { // There are four staffs per sheet
                    String filename = "SheetMusic/" + songName + "/page" + pageNumber + ".png";
                    canvas.screenShot(filename); // Screen shots the page
                    notesGroup.removeAll(); // removes all of the objects
                    pageNumber++; // Increments pageNumber

                    // Reset position counters
                    clefNumber = clefSize * 2;
                    noteNumber = noteStartX + noteIncrement;
                }

            }
            
            myReader.close();

            // At the end, empty out the rest of the notes onto the canvas
            for (int key : tracker.keySet()) {
                createNote(noteNumber, clefNumber - placement.get(key).get(0) * halfStep, placement.get(key).get(1), tracker.get(key));
            }

            // Then screenshots the last page
            canvas.screenShot("SheetMusic/" + songName + "/page" + pageNumber + ".png");
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        
        canvas.onClick(event -> {
            System.out.println(event.getPosition());
        });
    }

    private static List<Integer> duration(int beats) {
        /**
         * Returns a list of numbers, representing the biggest divisible numbers for the note
         * [1, 2, 3, 4, 5, 6]
         */
        int wholeDotted = beats / 6;
        beats = beats - wholeDotted * 6;
        int whole = beats / 4;
        beats = beats - whole * 4;
        int halfDotted = beats / 3;
        beats = beats - halfDotted * 3;
        int half = beats / 2;
        beats = beats - half * 2;
        // The remainder of the beats are just the quarters
        return List.of(beats, half, halfDotted, whole, 0, wholeDotted);
    }

    private static void createNote(double x, int y, int sharp, int duration) {
        /**
         * x and y indicate where the middle of the note should be
         * size indicates the width of the center of the note
         */
        List<Integer> types = duration(duration); // First, gets the lowest divisible numbers for the notes
        x -= duration * noteIncrement * 2; // Then sets the x starting coordinate to the correct spot

        // First, add in the sharps at the front of the note (or natural)
        if (sharp == 1) {
            if (!sharps.contains(y)) { // If there isn't a sharp in that bar that is on the same y-coor
                Image sharpImage = new Image("SheetMusic/Notes/sharp.png");
                sharpImage.setScale(0.01, 0.008);
                sharpImage.setCenter(x - 7, y);
                notesGroup.add(sharpImage);
                sharps.add(y);
            }
        }
        else {
            if (sharps.contains(y)) { // If there is a sharp in that bar on the same line, and that note is supposed to be natural, then add a natural sign on it
                Image naturalImage = new Image("SheetMusic/Notes/natural.png");
                naturalImage.setScale(0.024);
                naturalImage.setCenter(x - 6.5, y + 0.5);
                notesGroup.add(naturalImage);
                sharps.remove(Integer.valueOf(y)); // Remove the y-coor, so that if there is a sharp right after, it must be added in
            }
        }

        // Then add in the correct note types
        if (types.get(0) == 1) {
            Image image = new Image("SheetMusic/Notes/quarterNote.png");
            image.setScale(0.0470588, 0.0415);
            image.setCenter(x + 7.6, y - 11.3);
            notesGroup.add(image);
            addNoteLines(x, y); // Adds lines to note to represent which clef the note belongs to
            x += noteIncrement * 2;
        }

        if (types.get(1) == 1) {
            Image image = new Image("SheetMusic/Notes/halfNote.png");
            image.setScale(0.0672269);
            image.setCenter(x + 5.8, y - 11);
            notesGroup.add(image);
            addNoteLines(x, y); // Adds lines to note to represent which clef the note belongs to
            x += noteIncrement * 4;
        }

        if (types.get(2) == 1) { // Dotted half note
            Image image = new Image("SheetMusic/Notes/halfNote.png");
            image.setScale(0.0672269);
            image.setCenter(x + 5.8, y - 11);
            notesGroup.add(image);
            addNoteLines(x, y); // Adds lines to note to represent which clef the note belongs to
            addDot(x, y);
            x += noteIncrement * 6;
        }

        if (types.get(3) == 1) {
            Image image = new Image("SheetMusic/Notes/wholeNote.png");
            image.setScale(0.0653575);
            image.setCenter(x + 3.27, y - 0.5);
            notesGroup.add(image);
            addNoteLines(x, y); // Adds lines to note to represent which clef the note belongs to
            x += noteIncrement * 8;
        }

        if (types.get(5) == 1) {
            Image image = new Image("SheetMusic/Notes/wholeNote.png");
            image.setScale(0.0653575);
            image.setCenter(x + 3.27, y - 0.5);
            notesGroup.add(image);
            addNoteLines(x, y); // Adds lines to note to represent which clef the note belongs to
            addDot(x, y);
            x += noteIncrement * 12;
        }

        // This part adds in the ties for durations that have to represented with one or more notes
        int previousNote = -1;
        for (int note = types.size() - 1; note >= 0; note--) {
            if (types.get(note) != 0 && previousNote != -1) {
                Image image = new Image("SheetMusic/Notes/tie.png");
                image.setScale(noteIncrement * 2 * (note + 1) / (image.getImageWidth() + 200), 12 / image.getImageHeight());
                image.setCenter(x - noteIncrement * (note + 1) * 0.9, y + 7); // the 0.9 shifts over a tad bit so that it looks more centered
                notesGroup.add(image);
            }
            if (types.get(note) != 0) {
                previousNote = note;
                x -= noteIncrement * 2 * (note + 1);
            }
        }
    }

    private static void addNoteLines(double x, int y) {
        // Adds lines to help keep track of which clef the note belongs to
        
        // Calculate line placement
        int currentLinePlacement = y - clefNumber;

        // First, add lines for notes below to clef
        for (int belowBase = halfStep * 2; belowBase <= currentLinePlacement; belowBase += halfStep * 2) {
            Line line = new Line(0, 0, 16, 0);
            line.setCenter(x + 3, clefNumber + belowBase);
            notesGroup.add(line);
        }

        // Adds line specifically to middle C
        if (currentLinePlacement == -16 * halfStep) {
            Line line = new Line(0, 0, 16, 0);
            line.setCenter(x + 3, clefNumber - 16 * halfStep);
            notesGroup.add(line);
        }

        // Adds lines to notes above treble clef
        for (int aboveTreble = -28 * halfStep; aboveTreble >= currentLinePlacement; aboveTreble -= halfStep * 2) {
            Line line = new Line(0, 0, 16, 0);
            line.setCenter(x + 3, clefNumber + aboveTreble);
            notesGroup.add(line);
        }
    }

    private static void addDot(double x, int y) {
        Ellipse dot = new Ellipse(0, 0, 3, 3);
        dot.setFilled(true);
        dot.setCenter(x + 11.5, y + 3);
        notesGroup.add(dot);
    }

    private static void createRest(double x, int y, int type) {
        /**
         * NOTE: the height y is the lowest line of the clef.
         * If you want to add a rest at the base clef, y = height
         * If you want to add a rest at the treble clef, y = height - 72
         */
        if (type == 3 || type == 6) { // Means that it's dotted
            Ellipse dot = new Ellipse(0, 0, 3, 3);
            dot.setFilled(true);
            dot.setCenter(x + 11.5, y - 24);
            notesGroup.add(dot);
        }

        if (type == 1) {
            Image image = new Image("SheetMusic/Notes/quarterRest.png");
            image.setScale(0.037209);
            image.setCenter(x + 2.94, y - 16);
            notesGroup.add(image);
        }

        else if (type == 2 || type == 3) {
            Rectangle halfRest = new Rectangle(0, 0, 6, 4);
            halfRest.setFillColor(new Color(0, 0, 0));
            halfRest.setCenter(x, y - 26);
            notesGroup.add(halfRest);
        }

        else if (type == 4 || type == 6) {
            Rectangle wholeRest = new Rectangle(0, 0, 6, 4);
            wholeRest.setFillColor(new Color(0, 0, 0));
            wholeRest.setCenter(x, y - 22);
            notesGroup.add(wholeRest);
        }


    }

    // Only need to do this one time to get the right music sheet that we're looking for
    private static void createBlankSheet() {
        // Creates a blank sheet of music
        final int CANVAS_WIDTH = 1000;
        final int CANVAS_HEIGHT = 800;
        CanvasWindow canvas = new CanvasWindow("Sheet music", CANVAS_WIDTH, CANVAS_HEIGHT);

        /**
         * There are 14 total lines
         * Space of 4 lines inbetween clefs
         * Each line has a pixel width of 1
         * Space inbetween lines  = 5
         */

        int clefSize = 90; // difference between the base line of one clef to the next
        int halfStep = 4; // To get from one line to the next, you have to add 8.
        int numberOfBars = 8;
        int startX = 50;
        int noteStartX = 150;
        int spaceBetweenBars = 200;

        // Trackers
        int height = 0;
        int width = startX;

        for (int counter = 0; counter < 4; counter++) {
            height += clefSize * 2;
            // Draws horizontal lines
            for (int baseClef = 0; baseClef < 5; baseClef++) {
                Line line = new Line(startX, height - baseClef * halfStep * 2, CANVAS_WIDTH - startX, height - baseClef * halfStep * 2); // The +1 is because the line has line thickness 1
                canvas.add(line);
            }

            for (int trebleClef = 9; trebleClef < 14; trebleClef++) {
                Line line = new Line(startX, height - trebleClef * halfStep * 2, CANVAS_WIDTH - startX, height - trebleClef * halfStep * 2); // The +1 is because the line has line thickness 1
                canvas.add(line);
            }

            int clefImageSize = 60;

            Image treble = new Image("SheetMusic/Notes/trebleClef.png");
            treble.setMaxHeight(clefImageSize);
            treble.setCenter(startX + 15 , height - 9 * halfStep * 2 - 15);
            canvas.add(treble);

            Image base = new Image("SheetMusic/Notes/baseClef.png");
            base.setMaxHeight(clefImageSize * 3 / 8);
            base.setCenter(startX + 16 , height - 15);
            canvas.add(base);

            // // Draws vertical lines
            // for (int bar = 0; bar < 3; bar++) {
            //     noteStartX += spaceBetweenBars;
            //     Line line1 = new Line(noteStartX, height, noteStartX, height - 13 * halfStep * 2); // Total height of clef is the second y value
            //     canvas.add(line1);
            // }
            // Reset noteStartX
            
            // Draws first and last line
            Line line = new Line(startX, height, startX, height - 13 * halfStep * 2);
            canvas.add(line);

            line = new Line(noteStartX + spaceBetweenBars * 4, height, noteStartX + spaceBetweenBars * 4, height - 13 * halfStep * 2);
            canvas.add(line);
            
            noteStartX = 150;
        }
          
        canvas.onClick(event -> {
            System.out.println(event.getPosition());
        });
        canvas.screenShot("SheetMusic/blankSheetMusic.png");
    }
}

// Chord progressions:
// Major / Minor: [[3, [3, 7, 8]], [4, [4, 7, 9]], [5, [5, 8, 9]], [7, [7, 3, 4]], [8, [8, 3, 5]], [9, [9, 4, 5]]]
// Major: 435
// Minor: 345
// Diminished: 336
// 7th: 4332
// Minor 7th: 3432
// Maj 7th: 4341


