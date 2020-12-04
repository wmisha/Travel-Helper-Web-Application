package hotelapp;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;

/** The driver class for project 4
 * The main function should take the following command line arguments:
 * -hotels hotelFile -reviews reviewsDirectory -threads numThreads -output filepath
 * (order may be different)
 * and read general information about the hotels from the hotelFile (a JSON file),
 * as read hotel reviews from the json files in reviewsDirectory (can be multithreaded or
 * single-threaded).
 * The data should be loaded into data structures that allow efficient search.
 * If the -output flag is provided, hotel information (about all hotels and reviews)
 * should be output into the given file.
 * Then in the main method, you should start an HttpServer that responds to
 * requests about hotels and reviews.
 * See pdf description of the project for the requirements.
 * You are expected to add other classes and methods from project 3 to this project,
 * and take instructor's / TA's feedback from a code review into account.
 * Please download the input folder from Canvas.
 */
public class HotelSearch {

    public static Map<String, String> map;
    ThreadSafeHotelDatabase db;
    ThreadSafeParseFiles passFiles;

    public HotelSearch(ThreadSafeHotelDatabase db, ThreadSafeParseFiles passFiles) {
        this.db = db;
        this.passFiles = passFiles;

    }

    public static void commandLineArguments(String[] args){
        map = new HashMap<>();
        for (int i = 0; i < args.length; i = i + 2) {
            map.putIfAbsent(args[i], args[i + 1]);
        }
        String hotelsFile = map.get("-hotels");
        String reviewsDir = map.get("-reviews");
        String numOfThread = map.get("-threads");
        String outputFile = map.get("-output");

        //System.out.println("Using hotel file: " + hotelsFile);
        //System.out.println("Using reviews directory: " + reviewsDir);
        //System.out.println("numOfThread: " + numOfThread);
        //System.out.println("outputFile: " + outputFile);

        if(hotelsFile == null) {
            System.out.println("Please provide valid arguments");
            System.exit(-1);
        }
        if(numOfThread == null)
            map.put("-threads", "1");
    }

    public void getDataReady(String hotelsFile, String reviewsDir){
        System.out.println("Starting parseData");
        long start = System.currentTimeMillis();

        passFiles.parseData(hotelsFile, reviewsDir, db); // load data into objects from files.

        long finish = System.currentTimeMillis();
        long timeElapsed = finish - start;
        System.out.println("parseData finished: took " + timeElapsed + " milliseconds");

        db.sortData(); //sort all objects based on projects requirements.

    }


    public void operations(){
        // Get user commands: find (id), find (word), or quit
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.print(">");
            String input = scanner.nextLine();  // Read user input

            String command = input.split(" ")[0];
            String arg = input.split(" ").length < 2 ? "" : input.split(" ")[1];

            if (command.equals("q")) {
                System.out.println("Goodbye");
                System.exit(0);;
            } else if (command.equals("find")) {
                db.find(arg);
            } else if (command.equals("findReviews")) {
                db.findReviews(arg);
            } else if (command.equals("findWord")) {
                db.findWord(arg);
            }else if(command.equals("hotelInfo")){
                  db.hotelInfo(arg);
            }else if(command.equals("reviews")){
                //db.reviews(arg);
            }else if(command.equals("index")){
               // db.index(arg);
            }
             else {
                System.out.println("Invalid command '" + command + "'");
            }
        }

    }
    public void writeToFile(String outputFile){
        long start, finish;
        //System.out.println("Starting write to file");
        start = System.currentTimeMillis();
        try (PrintWriter writer = new PrintWriter(new FileWriter(outputFile))) {
            TreeMap<String, Hotel> hotelMap = db.getHotelMap();
            writer.println();
            for(Hotel hotel: hotelMap.values()) {
                //logger.debug("writing one hotel to file");
                writer.println(hotel);
                writer.println(hotel.reviewsToString());
            }
            writer.flush();
        } catch (IOException e) {
            System.out.println("IOException occurred" + e);
        }
        finish = System.currentTimeMillis();
        long timeElapsed = finish - start;
        //System.out.println("Writing to file finished: took " + timeElapsed + " milliseconds");

    }

    public static void main(String[] args) {

        commandLineArguments(args);
        String hotelsFile = map.get("-hotels");
        String reviewsDir = map.get("-reviews");
        String numOfThread = map.get("-threads");
        String outputFile = map.get("-output");

        ThreadSafeHotelDatabase db = new ThreadSafeHotelDatabase();
        ThreadSafeParseFiles passFiles = new ThreadSafeParseFiles(Integer.parseInt(numOfThread));

        HotelSearch hotelSearch = new HotelSearch(db,passFiles);

        hotelSearch.getDataReady(hotelsFile,reviewsDir);

        if(outputFile == null){
            hotelSearch.operations();

        }else {
            hotelSearch.writeToFile(outputFile);
        }
    }
}

