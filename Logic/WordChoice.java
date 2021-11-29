package Logic;

import java.util.*;

/**
 * Holds a collection of words to choose from, and allows clients to select one at random.
 */
final class WordChoice {
    private static final Random rand = new Random();

    private final List<String> choices = new ArrayList<>();

    /**
     * Returns all the words available to choose from.
     */
    public List<String> getChoices() {
        return Collections.unmodifiableList(choices);
    }

    /**
     * Chooses one of the available words at random.
     */
    public String getRandomChoice() {
        return choices.get(rand.nextInt(choices.size()));
    }

    /**
     * Adds a new possible choice.
     */
    public void addChoice(String choice) {
        choices.add(choice);
    }

    @Override
    public String toString() {
        return "WordChoice{"
            + "choices=" + choices
            + '}';
    }
}
