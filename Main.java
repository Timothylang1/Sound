import java.util.List;
import java.util.Scanner;

import Graphics.*;

import javax.sound.sampled.AudioInputStream;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

import Logic.MarkVShaney;

public class Main {
    private static List<String> inputPathWay = List.of("CMajorChord", "CMajorChord"); // "ComeToBrazilStart" = First three seconds
    private static String outputPathWay = "CMajorChord20Millisecond";
    private static List<byte[]>list;
    private static List<AudioInputStream> audioInputStreamList;
    private static CanvasWindow canvas;
    private static byte[] newarray;
    private static FileWriter myWriter;

    private static String message = "";
    private static int combiningMessagesCounter = 0;

    public static void setup() {
        new Testing(inputPathWay, outputPathWay, true);
        list = Testing.getArrayInfo();
        audioInputStreamList = Testing.getAudioInputStreamList();
    }
    public static void main(String[] args) {
        setup();
        // isolate(25000, 25020);
        // graphics();
        // occurencesOfPart(88);
        frequenciesTest();
        // readFile();
    }

    // DIFFERENT TEST CASES

    // TESTING GRAPHICS Display the entire song bytes by averaging out all of the points so that they fit on the screen
    private static void graphics() {
        final int CANVAS_WIDTH = 1600;
        final int CANVAS_HEIGHT = 800;
        canvas = new CanvasWindow("Bytes mapped out for " + inputPathWay.get(1), CANVAS_WIDTH, CANVAS_HEIGHT);
        byte[] array = list.get(1);

        byte[] evenoddarray = new byte[array.length / 2];
        byte[] averageevenoddarray = new byte[evenoddarray.length / 2];
        // Gets all of the odd numbered parts
        for (int counter5 = 0; counter5 < evenoddarray.length; counter5++) {
            evenoddarray[counter5] = array[counter5 * 2 + 1];
        }
        for (int counter6 = 0; counter6 < averageevenoddarray.length; counter6++) {
            averageevenoddarray[counter6] = (byte) ((evenoddarray[counter6 * 2] + evenoddarray[counter6 * 2 + 1]) / 2);
        }
        
        byte[] newarray = new byte[array.length];

        // List that keeps track of all the points to plot
        ArrayList<Integer> y_coor = new ArrayList<>();

        // Takes the data points, averages them enough so that it fits on single canvas screen
        int NumberOfPointsToAverage = evenoddarray.length / CANVAS_WIDTH;
        if (NumberOfPointsToAverage != 0) {
            // First, averages data points
            for (int counter1 = 0; counter1 < CANVAS_WIDTH; counter1++) {
                int total = 0;
                for (int counter = 0; counter < NumberOfPointsToAverage; counter++) {
                    total += evenoddarray[counter1 * NumberOfPointsToAverage + counter];
                }
                total = total / NumberOfPointsToAverage;
                y_coor.add(total);

                // Then add them to the testing arraylist
                for (int counter3 = 0; counter3 < NumberOfPointsToAverage; counter3++) {
                    newarray[counter1 * NumberOfPointsToAverage + counter3] = (byte) total;
                }
            }
            // Then plot them all
            for (int counter2 = 0; counter2 < y_coor.size() - 1; counter2++) {
                // We know that the maximum amplitude of a byte is 127, min is -128
                Line line = new Line(counter2, CANVAS_HEIGHT / 2 - y_coor.get(counter2) * 400 / 128, counter2 + 1, CANVAS_HEIGHT / 2 - y_coor.get(counter2 + 1) * 400 / 128);
                canvas.add(line);
            }
        }
        // Occurs if the array length is shorter than the CANVAS_WIDTH
        else {
            for (int counter4 = 0; counter4 < evenoddarray.length; counter4++) {
                y_coor.add((int) evenoddarray[counter4]);
            }
            for (int counter2 = 0; counter2 < y_coor.size() - 1; counter2++) {
                // We know that the maximum amplitude of a byte is 127, min is -128
                Line line = new Line(counter2 * CANVAS_WIDTH / evenoddarray.length, CANVAS_HEIGHT / 2 - y_coor.get(counter2) * 400 / 128, (counter2 + 1) * CANVAS_WIDTH / evenoddarray.length, CANVAS_HEIGHT / 2 - y_coor.get(counter2 + 1) * 400 / 128);
                canvas.add(line);
            }
        }

        // Adds a line that represents 0
        Line line = new Line(0, CANVAS_HEIGHT / 2, CANVAS_WIDTH, CANVAS_HEIGHT / 2);
        canvas.add(line);

        // Using newarray, create a new sound file to test what we can do with this info
        // Testing.write(audioInputStreamList.get(0).getFormat(), newarray, audioInputStreamList.get(0).getFrameLength());
    }

    // TEST 1: Copying the contents from one file to another, no alterations
    private static void copy() {
        Testing.write(audioInputStreamList.get(0).getFormat(), list.get(0), audioInputStreamList.get(0).getFrameLength());
    }

    // TEST 2: Copy the part of the contents from one file to another, no alterations
    private static void copy(long lengthinseconds) {
        Testing.createNewWavAudioInputStream(lengthinseconds, list.get(0));
    }

    // TEST 3: Can we cancel a certain sound? Yes, by subracting it from itself
    private static void cancel() {
        byte[] array = list.get(0);
        byte[] newarray = new byte[array.length];
        for (int counter = 0; counter < array.length; counter++) {
            newarray[counter] = (byte) (array[counter] - array[counter]);
        }
        Testing.write(audioInputStreamList.get(0).getFormat(), newarray, audioInputStreamList.get(0).getFrameLength());
    }

    // TEST 4: Can we find a general pattern?
    private static void pattern() {
        byte[] array = list.get(0);
        ArrayList<Byte> counts = new ArrayList<>();
        counts.add((byte) 0);
        for (int counter = 0; counter < array.length; counter++) {
            if (array[counter] == (byte) 0) {

            }
        }
    }
    

    // TEST 6: Condensing song to just one channel
    private static void twoToOneChannel() {
        Testing.createNewFormat(48000, 16, 1, audioInputStreamList.get(0).getFrameLength(), list.get(0));
    }

    // TEST 7: Can we isolate one part of a song (Just the first song in the list)
    private static void isolate(int startTimeInMilliSeconds, int endTimeInMilliSeconds) {
        Testing.isolatePart(startTimeInMilliSeconds, endTimeInMilliSeconds);
    }

    // TEST 8: With an isolated part, how many times does it occur in a song?
    private static void occurencesOfPart(double percentageMatch) {
        // Keeps track of where the match occured (in seconds)
        System.out.println("Start Timer");
        ArrayList<Double> tracker = new ArrayList<>();
        byte[] notearray = list.get(1);
        byte[] songarray = list.get(0);
        for (int counter = 0; counter < songarray.length - notearray.length; counter++) {
            double currentTotal = 0;
            for (int counter2 = 0; counter2 < notearray.length; counter2++) {
                currentTotal += Math.abs(notearray[counter2] - songarray[counter2 + counter]);
            }
            currentTotal = currentTotal / 255; // The max difference between the values (-128 and 127), so the max difference is 1
            // Sees how close the sum total of points matches with the note, and if they match above the percentageMatch, then it's a match!
            if (currentTotal / notearray.length * 100 < 100 - percentageMatch) {
                tracker.add(counter / 4 / 48000.0);
                System.out.println("Total matches: " + tracker.size());
                System.out.println("Places with " + percentageMatch + "% match: " + secondsToMinutes(tracker.get(tracker.size() - 1)));
                counter += notearray.length; // Skips over the next set because we have already identified the note there
                // Upper C occurs at:
                /**
                 * What is should produce:
                 * 11.5 (original)
                 * 23
                 * 29 // Skipped
                 * 37 // Skipped
                 * 38
                 * 48
                 * Actual results:
                 * Start Timer

                 */
            }
        }
    }
    // Converts the seconds into a string that represents minutes, seconds, milliseconds
    private static String secondsToMinutes(double seconds) {
        int Seconds = (int) (seconds * 100);
        int Minutes = Seconds / 6000;
        Seconds = Seconds - Minutes * 6000;
        int ActualSeconds = Seconds / 100;
        Seconds = Seconds - ActualSeconds * 100;
        int Milliseconds = Seconds;
        return Integer.toString(Minutes) + ":" + Integer.toString(ActualSeconds) + "." + Integer.toString(Milliseconds);
    }

    // TEST 9: We know that if we get all of the odd parts of the array, we get sinusodial patterns that match the frequencies of a piano
    // Can we use that knowledge to effectively determine the piano notes?

    private static void frequenciesTest() {
        byte[] array = list.get(0); // Orginal array
        newarray = new byte[array.length / 2]; // Modified array with the only part we need (suspect it's an isolated channel)
        for (int counter = 0; counter < newarray.length; counter++) {
            newarray[counter] = array[counter * 2 + 1];
        }

        // Creates text file that puts data so that python file can analyze further
        // Open File 
        try {
            myWriter = new FileWriter("transfer.txt");
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        // Analysis
        int lastSavedPlace = 0;
        boolean belowZero = true;
        System.out.println("Beginning analysis...");
        for (int counter2 = 0; counter2 < newarray.length - 2; counter2 += 1) {
            if (belowZero) {
                while (counter2 + 1 < newarray.length && newarray[counter2] < newarray[counter2 + 1]) {
                    counter2++;
                }
                if (newarray[counter2 + 1] > 0) {
                    belowZero = false;
                    CompletedWave(lastSavedPlace, counter2);
                    lastSavedPlace = counter2;
                }
            }
            else {
                while (counter2 + 1 < newarray.length && newarray[counter2] > newarray[counter2 + 1]) {
                    counter2++;
                }
                if (newarray[counter2 + 1] < 0) {
                    belowZero = true;
                }
            }
        }
        try {
            myWriter.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void CompletedWave(int begin, int end) {
        for (int counter = begin; counter < end; counter++) {
            message += newarray[counter] + " ";
        }
        combiningMessagesCounter++;
        if (combiningMessagesCounter > 0) { // combines up to n sets of bytes (check if more info yields more accurate fft results)
            try {
                myWriter.write(message + "\n"); // We want it to include the end
                message = ""; // Resets message and counter back to nothing
                combiningMessagesCounter = 0;
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        // Formula from https://en.wikipedia.org/wiki/Piano_key_frequencies
        // To calculate frequency with the given period, we know that the period is measured in bytes
        // Song runs at 48000 frames per second, with 4 bytes per frame
        // Since we're only analyzing half of it, it's 2 bytes per frame
        // 48000 frames per second * 2 bytes per frame = 96000 bytes per second
        int period = end - begin;
        int pianoNote = (int) (12.0 * Math.log10(2400.0/(11.0 * period)) / Math.log10(2.0) + 49.0 + 0.5); // The 0.5 is for truncating the double, then casting it to an integer
        // if (pianoNote == 52) { // Upper A Note
        //     if (placement / 96000.0 - lastC / 96000.0 > 1) { // To prevent SPAM, create an if statement that stops anything less than a second difference form being printed
        //         System.out.println("Upper C note occured at " + secondsToMinutes(placement / 96000.0));
        //         lastC = placement;
        //     }
        // }
        System.out.print(period + " ");
    }

    // TEST 10: if we use python's FFT, how accurate is it to the actual results?
    private static void readFile() {
        try {
            File myObj = new File("transfer.txt");
            Scanner myReader = new Scanner(myObj);
            while (myReader.hasNextLine()) {
                String data = myReader.nextLine();
                String[] data1 = data.split(" ");
                double frequency1 = Double.parseDouble(data1[0].split(":")[0]);
                double frequency2 = Double.parseDouble(data1[1].split(":")[0]);
                double frequency3 = Double.parseDouble(data1[2].split(":")[0]);
                int pianoNote1 = (int) (12.0 * Math.log10(2400.0 * frequency1 /11.0) / Math.log10(2.0) + 49.0 + 0.5); // The 0.5 is for truncating the double, then casting it to an integer
                int pianoNote2 = (int) (12.0 * Math.log10(2400.0 * frequency2 /11.0) / Math.log10(2.0) + 49.0 + 0.5); // The 0.5 is for truncating the double, then casting it to an integer
                int pianoNote3 = (int) (12.0 * Math.log10(2400.0 * frequency3 /11.0) / Math.log10(2.0) + 49.0 + 0.5); // The 0.5 is for truncating the double, then casting it to an integer
                System.out.println("Note 1 = " + pianoNote1 + " Note 2 = " + pianoNote2 + " Note 3 = " + pianoNote3);
            }
            myReader.close();
        } catch (Exception e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
          }
    }
}
