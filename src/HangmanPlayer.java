import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Stack;

public class HangmanPlayer
{
    public static void main( String[] args ) throws Exception
    {
        for ( int index = 0; index < 100; index++ )
        {
            try
            {
                _main( args );
            }
            catch ( Exception e )
            {
                System.out.println(  e.getMessage( ) );
            }
        }
    }

    /**
     * main public method to solve game
     * 
     * @param args
     * @throws Exception
     */
    public static void _main( String[] args ) throws Exception
    {
        // START NEW HANGMAN GAME
        HangmanGame game = new HangmanGame( );
        game.start( );

        // GET THE LIST OF POSSIBLE ANSWERS
        List<String> possibleAnswers = new ArrayList<String>( );
        Scanner scanner = new Scanner( new File( "web2.txt" ) );
        while (scanner.hasNextLine( ))
        {
            String word = scanner.nextLine( );
            if( word.length( ) == game.getWord( ).length( ) )
            {
                possibleAnswers.add( word );
            }
        }
        scanner.close( );

        // RANK LETTERS BY OCCURANCES IN THE POSSIBLE ANSWERS
        Stack<String> rankedLetters = rankLetters( possibleAnswers );

        // Keep track of the letters as we play them
        List<String> playedLetters = new ArrayList<String>( );

        // KEEP PLAYING UNTIL THE GAME IS OVER OR WE RUN OUT OF LETTERS
        while (game.isActive( ) && !rankedLetters.isEmpty( ))
        {
            // Play the next letter
            String guess = rankedLetters.pop( );
            playedLetters.add( guess );
            game.playLetter( guess );

            // Check if we won the game
            if( game.isWordCompleted( ) )
            {
                System.out.println( game );
                return;
            }

            // If letter accepted, some of the previously possible
            // answers might become impossible; hence, re-rank letters
            if( game.wasLetterAccepted( guess ) )
            {
                // Re-calculate possible answers
                String regex = game.getWord( ).replaceAll( "_", "." );

                List<String> validAnswers = new ArrayList<String>( );
                for (String possibleAnswer : possibleAnswers)
                {
                    if( possibleAnswer.matches( regex ) )
                    {
                        validAnswers.add( possibleAnswer );
                    }
                }
                possibleAnswers = validAnswers;

                // Re-rank letters
                rankedLetters = rankLetters( possibleAnswers );

                // Ranking algorithm doesn't take used letters into account
                rankedLetters.removeAll( playedLetters );
            }
        }

        System.out.println( game );
    }

    /**
     * Rank the letters of the alphabet by counting them in the possible
     * answers.
     * 
     * First we generate a map where the keys are the letters and the values are
     * the counts. Then we sort the keys using a custom comparator that uses the
     * count values to determine order. Then we push all the completely
     * uncounted letters into the stack (to be pulled out last). Then we push
     * all the sorted/ranked/counted letters into the stack (to be pulled out
     * first)
     * 
     * @param possibleAnswers
     * @return
     */
    public static Stack<String> rankLetters( List<String> possibleAnswers )
    {
        Stack<String> letters = new Stack<String>( );

        // Count how many times each letter appears in this set of words
        // Note that a better and slightly more complex algorithm would be
        // to rank by The numbers of words that each letter appears in
        Map<String, Integer> map = countLetters( possibleAnswers );

        // Put all the map keys ( i.e. letters ) into a list
        List<String> mapKeys = new ArrayList<String>( );
        mapKeys.addAll( map.keySet( ) );

        // Sort the list of map keys
        Collections.sort( mapKeys, new Comparator<String>( )
        {
            @Override
            public int compare( String key1, String key2 )
            {
                Integer count1 = map.get( key1 );
                Integer count2 = map.get( key2 );
                return count1.compareTo( count2 );
            }
        } );

        // Push all the letters from the alphabet that didn't enter into our map
        // In case the word is not in our dictionary ( which does happen )
        // We could improve results by ordering this alphabet by letters
        // frequency
        String alphabet = "abcdefghijklmnopqrstuvwxyz";
               alphabet = "etaoinsrhdlucmfywgpbvkxqjz";
        for (int index = 0; index < alphabet.length( ); index++)
        {
            char c = alphabet.charAt( index );
            String letter = String.valueOf( c );
            if( !mapKeys.contains( letter ) )
            {
                letters.push( letter );
            }
        }

        // Populate the top of the queue in sorted order
        for (String letter : mapKeys)
        {
            letters.push( letter );
        }

        return letters;
    }

    /**
     * Count the times each letter appears
     * 
     * Create a map with keys = letter and values = counts Look at each letter
     * of each word Increment the counter for each occurance of each letter in
     * each word
     * 
     * A slightly different and perhaps better counting algorithm would be Count
     * the number of words that each letter appears in
     * 
     * @param words
     * @return
     */
    public static Map<String, Integer> countLetters( List<String> words )
    {
        // Create a map with keys = letters and values = counters
        Map<String, Integer> map = new HashMap<>( );

        // Look at each word
        for (String word : words)
        {
            for (int index = 0; index < word.length( ); index++)
            {
                // Look at each character
                char c = word.charAt( index );
                String letter = String.valueOf( c );

                // Calculate the count
                int count = 1;
                if( map.containsKey( letter ) )
                {
                    count += map.remove( letter );
                }

                // Put the count in the map
                map.put( letter, count );
            }
        }

        // Return the map
        return map;
    }

}
