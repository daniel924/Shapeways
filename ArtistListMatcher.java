                                                                     
                                                                     
                                                                     
                                             
import java.io.*;

import java.util.List;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.HashMap;
import java.util.BitSet;



/**
 * This class takes in a filename with comma-delimited lists of artists separated
 * by newlines, and produces an output file containing a list of pairs of artists
 * which appear TOGETHER in at least fifty different lists.
 *
 * @author Daniel Ladenheim
 */
public class ArtistListMatcher {


    public static void main(String[] args)  {

        long start = System.currentTimeMillis();

        /* ensure filename is passed as argument */
        String fileName = null;
        if( args.length != 1) {
            System.out.println("Usage: java ArtistListMatcher <filename>");
            return;
        }
        fileName = args[0];

        /* build map of artist to list number */
        HashMap<String, BitSet> artistToListMap = buildMap(fileName);
        if( artistToListMap == null) {
            return; /* error handled */
        }

        /* match artists list appearances */
        List<String> matches = matchArtists(artistToListMap);

        /* print and write matches */
        printAndWriteMatches(matches);

        System.out.println( "" + matches.size() + " matches found in "
                + ( System.currentTimeMillis() - start) + " milliseconds" );
    }


    /*
     * Create a mapping between artist name and a BitSet where each bit
     * represents a list, and will hold the value true if the artist
     * appears in the list and false otherwise.
     *
     * Using a BitSet makes it easy to keep track of how many lists the
     * artist is in with the cardinality function. This is an optimization
     * when filtering artists that do not appear in 50 or more lists.
     *
     * @param filename - file with comma-delimited lists of artists separated
     *                   by newline characters
     */
    private static HashMap<String, BitSet> buildMap(String fileName) {

        /* Open file containing lists of artists */
        BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader(fileName));
        } catch (FileNotFoundException ex) {
            System.out.println("Error: could not find file: " + fileName
                    + "\n" + ex.getMessage());
            return null;
        }


        /* loop through each list and create a map from the artist to the list */
        HashMap<String, BitSet> artistToListMap = new HashMap<String, BitSet>();
        int listNum = 0;  /* keep track of what number list we are on */
        String line = null;

        try {
            while ((line = reader.readLine()) != null) {

                String[] artists = line.split(","); /* artists are comma delimited */
                for (String artist : artists ) {

                    if (artistToListMap.containsKey(artist)) {
                        artistToListMap.get(artist).set(listNum);
                    } else {
                        BitSet lists = new BitSet();
                        lists.set(listNum);
                        artistToListMap.put(artist, lists);
                    }
                }
                listNum++;
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }


        /* Optimizes map by removing artists with less than 50 appearances
         * by reducing the amount of artists that qualify to match. This
         * method of removing from the map is actually quicker than
         * creating an iterator as well.
         */
        ArrayList<String> smallArtists = new ArrayList<String>();
        for ( String artist : artistToListMap.keySet() ) {
            if ( artistToListMap.get(artist).cardinality() < 50 ) {
                smallArtists.add(artist);
            }
        }
        for (String artist : smallArtists) {
            artistToListMap.remove(artist);
        }

        return artistToListMap;
    }


    /*
     * Compare each artist to each other, and see if they are in the same lists by
     * comparing BitSets.
     *
     * By using BitSets, we can use the & operator to efficiently
     * see the overlap between the lists of two artists as opposed to an array.
     * This is an optimization.
     *
     * @param artistToListMap - a map of artist name mapped to a BitSet where the index of each
     *                          bit represents a list index, and the value represents if the
     *                          artist is contained in that list
     */
    private static List<String> matchArtists(HashMap<String, BitSet> artistToListMap) {

        List<String> matches = new ArrayList<String>();

        /* store previous matches to prevent duplicates */
        HashSet<String> alreadySeenArtists = new HashSet<String>();

        /* find all artist combos */
        for (String artist1 : artistToListMap.keySet()) {
            alreadySeenArtists.add(artist1);
            for (String artist2: artistToListMap.keySet())   {

                /* ignore duplicates */
                if (alreadySeenArtists.contains(artist2)) {
                    continue;
                }

                /* see if artists are in the same lists */
                BitSet artistLists1 = (BitSet)(artistToListMap.get(artist1).clone());
                BitSet artistLists2 = (BitSet)(artistToListMap.get(artist2).clone());

                artistLists1.and(artistLists2);
                if(artistLists1.cardinality() >= 50 ) {
                    matches.add(artist1 + "," + artist2);
                }
            }
        }
        return matches;
    }


    /*
     * Print and write matches to file
     *
     * @param matches - A list of strings that each contain two comma-delimited
     *                  band names that have appeared in more than 50 lists together
     */
    private static void printAndWriteMatches(List<String> matches) {

        PrintWriter writer = null;
        try {
            writer = new PrintWriter("output.txt");

            for(String s: matches) {
                System.out.println(s);
                writer.write(s + '\n');
            }
            writer.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
