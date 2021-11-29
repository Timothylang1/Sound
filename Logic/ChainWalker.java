package Logic;

import java.util.ArrayList;
import java.util.List;

/**
 * A helper class for both building the Markov chain and generating text with it. Clients can either
 * repeatedly choose random words or pass in new words. As they do, ChainWalker keeps track of the
 * current context (i.e the recently added/generated words).
 */
class ChainWalker {
    private final MarkVShaney mvs;
    private final List<String> context = new ArrayList<>();

    ChainWalker(MarkVShaney mvs) {
        this.mvs = mvs;
    }

    /**
     * Updates the recent context with the new word, e.g. “one fish” → “fish two” → “two fish.”
     */
    private void shiftIntoContext(String word) {
        if (context.size() == mvs.getContextSize()) {
            context.remove(0);
        }
        context.add(word);
    }

    /**
     * Adds a new word choice for the current context to the Markov chain, and updates the context.
     */
    void addNext(String word) {
        mvs.addChoice(context, word);
        shiftIntoContext(word);
    }

    /**
     * Chooses a word appropriate to the current context at random from the Markov chain, and
     * updates the context.
     */
    String chooseNext() {
        String result = mvs.chooseNextWord(context);
        shiftIntoContext(result);
        return result;
    }
}
