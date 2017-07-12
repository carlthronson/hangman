import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;

/**
 * This class contains the logic to start a game and play a letter by invoking
 * rest api calls and processing the responses
 * 
 * This class also contains the logic for any function that relates to game
 * status and/or requires an understanding of how the REST API responses are
 * structured
 * 
 * @author User
 *
 */
public class HangmanGame
{
    /**
     * Start a new game
     * 
     * @throws Exception
     */
    public void start( ) throws Exception
    {
        // START A GAME
        this.json = RestClient.post( NEW_GAME_URL, EMAIL );

        // Parse the json data into a map
        // This could be replaced by 3rd party library
        this.properties = JsonHelper.parseJson( this.json );

        // BUILD ACTIVE GAME URL FOR FUTURE POSTS
        this.activeGameUrl = NEW_GAME_URL + "/" + this.getGameId( )
                        + "/guesses";
    }

    /**
     * Play a letter
     * 
     * @param guess
     * @throws Exception
     */
    public void playLetter( String guess ) throws Exception
    {
        // Send the next guess
        this.json = RestClient.post( activeGameUrl, "char=" + guess );

        // CONVERT RESPONSE TO GAME OBJECT
        this.properties = JsonHelper.parseJson( this.json );

        // Add this next guess to the list of used letters
        this.usedLetters.add( guess );
    }

    /**
     * Determine if the game is currently active
     * 
     * @return
     */
    public boolean isActive( )
    {
        return this.getStatus( ).equals( "active" );
    }

    /**
     * Determine if the last letter played has been accepted
     * 
     * @return
     */
    public boolean wasLetterAccepted( String letter )
    {
        return getWord( ).contains( letter );
    }

    /**
     * Determine if the word has been completed
     * 
     * @return
     */
    public boolean isWordCompleted( )
    {
        return !this.getWord( ).contains( "_" );
    }

    /**
     * Get the current word
     * 
     * @return
     */
    public String getWord( )
    {
        return this.properties.getProperty( "word" );
    }

    /**
     * Get the current game status
     * 
     * @return
     */
    public String getStatus( )
    {
        return this.properties.getProperty( "status", "active" );
    }

    /**
     * Get the current game id
     * 
     * @return
     */
    public String getGameId( )
    {
        return this.properties.getProperty( "gameId" );
    }

    public String toString( )
    {
        return json;
    }

    private String activeGameUrl;
    private List<String> usedLetters = new ArrayList<String>( );
    private Properties properties;
    private String json;

    private static final String NEW_GAME_URL = "http://int-sys.usr.space/hangman/games";
    private static final String EMAIL = "email=carlthronson@gmail.com";

}

class JsonHelper
{
    /**
     * Parse json string into Java Map (could be replaced by 3rd party library)
     * 
     * @param json
     * @return
     */
    public static Properties parseJson( String json )
    {
        Properties response = new Properties( );

        StringTokenizer st = new StringTokenizer( json, "{}\":," );
        while (st.hasMoreTokens( ))
        {
            response.put( st.nextToken( ), st.nextToken( ) );
        }
        return response;
    }

}

/**
 * 
 * @author User
 *
 */
class RestClient
{
    public static String post( String urlString, String data ) throws Exception
    {
        return send( urlString, POST, data );
    }

    public static String get( String urlString, String data ) throws Exception
    {
        return send( urlString, GET, data );
    }

    /**
     * Send an http get or post request, (could be replaced by 3rd party
     * library)
     * 
     * Opens an http connection with given url If method is post, send data over
     * output stream Look at response code, error stream and content If error is
     * present, this will cause exception to be thrown Other wise content will
     * be read and returned
     * 
     * @param urlString
     * @param method
     * @param data
     * @return
     * @throws Exception
     */
    private static String send( String urlString, String method, String data )
                    throws Exception
    {
        // Rank the letters that appear in words of the same length

        // Open a connection to the server
        URL url = new URL( urlString );
        HttpURLConnection con = (HttpURLConnection) url.openConnection( );

        con.setRequestMethod( method );

        // If method is post, then we send the data on the output stream
        if( method.equalsIgnoreCase( POST ) )
        {
            con.setDoOutput( true );
            OutputStream out = con.getOutputStream( );
            con.getOutputStream( ).write( data.getBytes( ) );
            out.flush( );
            out.close( );
        }

        // Just for fun, take a look at the header fields returned to us
        // System.out.println( con.getHeaderFields( ) );

        // Just for fun, take a look at the response code
        // int responseCode = con.getResponseCode( );
        // System.out.println( "response code: " + responseCode );

        // Read the error stream
        InputStream error = con.getErrorStream( );
        if( error != null )
        {
            String result = read( error );
            error.close( );
            if( result.length( ) > 0 )
            {
                throw new Exception( result );
            }
        }

        // Get the content which will be a json data string
        InputStream in = con.getInputStream( );
        String json = read( in );

        con.disconnect( );
        return json;
    }

    /**
     * 
     * @param in
     * @return
     * @throws IOException
     * @throws Exception
     */
    private static String read( InputStream in ) throws IOException, Exception
    {
        StringBuilder builder = new StringBuilder( );
        // if( in.available( ) > 0 )
        {
            Reader reader = new InputStreamReader( in );
            LineNumberReader lineReader = new LineNumberReader( reader );
            while (true)
            {
                String line = lineReader.readLine( );
                if( line == null )
                    break;
                builder.append( line );
            }
        }
        return builder.toString( );
    }

    private static final String POST = "POST";
    private static final String GET = "GET";

}
