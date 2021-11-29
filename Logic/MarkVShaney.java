package Logic;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Stream;

/**
 * A Markov chain text generator. Holds a transition chain whose entries each say, “For these n
 * words, the following words might appear next in the text.” You can populate the transition
 * table with one of more calls to readText(), then use a call to generateText() to randomly
 * create new text by choosing random options from the transition chain.
 */
@SuppressWarnings({"WeakerAccess","resource"})
public class MarkVShaney {
    private final int contextSize;
    private Map<List<String>, WordChoice> transitions = new HashMap<>();

    /**
     * Starts the generator with an empty Markov chain.
     *
     * @param contextSize
     *      The number of preceding words to keep as context when looking up the next word.
     * @param includeWhitespace
     *      If true, include surrounding whitespace in the words so that the Markov chain generates
     *      paragraph breaks. If false, the chain strips all whitespace and includes only bare words.
     */
    public MarkVShaney(int contextSize, int size, String outputName) {
        this.contextSize = contextSize;
        try {
            File myObj = new File("songs/" + outputName + ".txt");
            FileWriter myWriter = new FileWriter(myObj);
            readText();
            generateText().limit(size).forEach(x -> {
                try {
                    myWriter.write(x + "\n");
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            });

            // Adds in eight blank notes to help close up the text
            for (int end = 0; end < 12; end++) {
                myWriter.write(0 + "\n");
            }

            myWriter.close();

        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * The number of previous words the chain will use for context.
     */
    public int getContextSize() {
        return contextSize;
    }

    /**
     * Registers nextWord as a possible choice for the given context.
     */
    public void addChoice(List<String> context, String nextWord) {
        WordChoice choice = transitions.get(context);
        if (choice == null) {
            choice = new WordChoice();
            transitions.put(List.copyOf(context), choice);
        }
        choice.addChoice(nextWord);
    }

    /**
     * Chooses a next word at random for the given preceding words. Returns null if the Markov chain
     * contains no options for the given context.
     */
    public String chooseNextWord(List<String> context) {
        if (transitions.containsKey(context)) {
            return transitions.get(context).getRandomChoice();
        }
        return null;

    }

    /**
     * Reads all words from the given text and adds them to the Markov chain.
     */
    public void readText() {
        try {
            File myObj = new File("chords.txt");
            Scanner myReader = new Scanner(myObj);

            ChainWalker walker = new ChainWalker(this);

            while (myReader.hasNextLine()) {
                String data = myReader.nextLine();
                walker.addNext(data);
            }
            myReader.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * Randomly generates text using the Markov chain.
     */
    public Stream<String> generateText() {
        ChainWalker walker = new ChainWalker(this);
        return Stream.generate(walker::chooseNext).takeWhile(word -> word != null);
    }

    @Override
    public String toString() {
        return "MarkVShaney{"
            + "contextSize=" + contextSize
            + ", transitions=" + transitions
            + '}';
    }
}
