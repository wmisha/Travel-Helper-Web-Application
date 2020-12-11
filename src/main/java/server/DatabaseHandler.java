package server;

import hotelapp.Hotel;
import hotelapp.Review;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.util.JsonUtils;
import org.w3c.dom.ls.LSOutput;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Random;

/**
 * Handles all database-related actions. Uses singleton design pattern.
 * Example of Prof. Engle
 *
 * @see BackEndServer
 */
public class DatabaseHandler {

    private static Logger log = LogManager.getLogger();

    /**
     * Makes sure only one database handler is instantiated.
     */
    private static DatabaseHandler singleton = new DatabaseHandler();

    /**
     * Used to create necessary tables for this example.
     */
    private static final String CREATE_users =
            "CREATE TABLE IF NOT EXISTS users (" +
                    "userId INTEGER AUTO_INCREMENT PRIMARY KEY, " +
                    "username VARCHAR(32) NOT NULL UNIQUE, " +
                    "password CHAR(64) NOT NULL, " +
                    "userSalt CHAR(32) NOT NULL," +
                    "lastLogin DATETIME);";

    private static final String CREATE_hotels =
            "CREATE TABLE IF NOT EXISTS hotels (" +
                    "hotelId VARCHAR(55) PRIMARY KEY," +
                    "name VARCHAR(255)," +
                    "address VARCHAR(255)," +
                    "city VARCHAR(255)," +
                    "lat VARCHAR(255)," +
                    "lng VARCHAR(255)," +
                    "link VARCHAR(255));";

    private static final String CREATE_reviews =
            "CREATE TABLE IF NOT EXISTS reviews (" +
                    "reviewId VARCHAR(255) PRIMARY KEY, " +
                    "hotelId VARCHAR(255)," +
                    "rating INTEGER," +
                    "title VARCHAR(500)," +
                    "reviewText  VARCHAR(2000)," +
                    "customer VARCHAR(255)," +
                    "date        DATE ," +
                    "userId INTEGER);";

    private static final String CREATE_saved_hotels =
            "CREATE TABLE IF NOT EXISTS saved_hotels (" +
                    "userId INTEGER," +
                    "hotelId VARCHAR(255)," +
                    "UNIQUE (userId, hotelId));";

    private static final String CREATE_liked_reviews =
            "CREATE TABLE IF NOT EXISTS liked_reviews (" +
                    "userId INTEGER," +
                    "reviewId VARCHAR(255)," +
                    "UNIQUE (userId, reviewId));";

    private static final String CREATE_visited_links =
            "CREATE TABLE IF NOT EXISTS visited_links (" +
                    "userId INTEGER," +
                    "link VARCHAR(255)," +
                    "UNIQUE (userId, link));";

    /**
     * Used to insert a new user into the database.
     */
    private static final String REGISTER_SQL =
            "INSERT INTO users (username, password, userSalt, lastLogin) " +
                    "VALUES (?, ?, ?, ?);";
    private static final String Insert_hotels =
            "Insert ignore into hotels (hotelId,name,address,city,lat,lng,link)" +
                    "VALUES(?,?,?,?,?,?,?);";
    private static final String Insert_reviews =
            "Insert ignore into reviews(reviewId, hotelId,rating,title,reviewText,customer,date,userId)" +
                    "VALUES(?,?,?,?,?,?,?,?);";

    /**
     * Used to determine if a username already exists.
     */
    private static final String USER_SQL =
            "SELECT username FROM users WHERE username = ?";

    /**
     * Used to retrieve the salt associated with a specific user.
     */
    private static final String SALT_SQL =
            "SELECT userSalt FROM users WHERE username = ?";

    /**
     * Used to authenticate a user.
     */
    private static final String AUTH_SQL =
            "SELECT userId, username FROM users " +
                    "WHERE username = ? AND password = ?";

    /**
     * Used to remove a user from the database.
     */
    private static final String DELETE_SQL =
            "DELETE FROM users WHERE username = ?";

    /**
     * Used to configure connection to database.
     */
    private DatabaseConnector db;

    public DatabaseConnector getDb() {
        return db;
    }

    /**
     * Used to generate password hash salt for user.
     */
    private Random random;

    /**
     * Initializes a database handler for the Login example. Private constructor
     * forces all other classes to use singleton.
     */
    private DatabaseHandler() {
        Status status = Status.OK;
        random = new Random(System.currentTimeMillis());

        try {
            // Change to "database.properties" or whatever your file is called
            db = new DatabaseConnector("database.properties");
            status = db.testConnection() ? setupTables() : Status.CONNECTION_FAILED;


        } catch (FileNotFoundException e) {
            status = Status.MISSING_CONFIG;
        } catch (IOException e) {
            status = Status.MISSING_VALUES;
        }

        if (status != Status.OK) {
            log.fatal(status.message());
        }
    }

    /**
     * Gets the single instance of the database handler.
     *
     * @return instance of the database handler
     */
    public static DatabaseHandler getInstance() {
        return singleton;
    }

    /**
     * Checks to see if a String is null or empty.
     *
     * @param text - String to check
     * @return true if non-null and non-empty
     */
    public static boolean isBlank(String text) {
        return (text == null) || text.trim().isEmpty();
    }

    /**
     * Checks if necessary table exists in database, and if not tries to
     * create it.
     */
    private Status setupTables() {
        Status status = Status.ERROR;

        try (
                Connection connection = db.getConnection();
                Statement statement = connection.createStatement();
        ) {
            // Drop all tables and start fresh
            //  statement.executeUpdate("DROP TABLE IF EXISTS users;");
            //  statement.executeUpdate("DROP TABLE IF EXISTS hotels;");
            //  statement.executeUpdate("DROP TABLE IF EXISTS reviews;");
            //  statement.executeUpdate("DROP TABLE IF EXISTS saved_hotels;");
            //  statement.executeUpdate("DROP TABLE IF EXISTS liked_reviews;");
            //  statement.executeUpdate("DROP TABLE IF EXISTS visited_links;");

            // In case table missing, must create
            statement.executeUpdate(CREATE_users);
            statement.executeUpdate(CREATE_hotels);
            statement.executeUpdate(CREATE_reviews);
            statement.executeUpdate(CREATE_saved_hotels);
            statement.executeUpdate(CREATE_liked_reviews);
            statement.executeUpdate(CREATE_visited_links);

            // Check if create was successful
            if (!(statement.executeQuery("SHOW TABLES LIKE 'users';").next()
                    && statement.executeQuery("SHOW TABLES LIKE 'hotels';").next()
                    && statement.executeQuery("SHOW TABLES LIKE 'reviews';").next()
                    && statement.executeQuery("SHOW TABLES LIKE 'saved_hotels';").next()
                    && statement.executeQuery("SHOW TABLES LIKE 'liked_reviews';").next()
                    && statement.executeQuery("SHOW TABLES LIKE 'visited_links';").next()
            )) {
                status = Status.CREATE_FAILED;
            } else {
                log.debug("Tables exist.");
                status = Status.OK;
            }

        } catch (Exception ex) {
            System.out.println(ex);
            ex.printStackTrace();
            status = Status.CREATE_FAILED;
            log.debug(status, ex);
        }

        return status;
    }

    public void populateTables(String hotelFile, String reviewDir) {
        LoadJsonToTables loadJsonToTables = new LoadJsonToTables(hotelFile, reviewDir);
        Path path = loadJsonToTables.getPath();
        loadJsonToTables.parseHotels();
        System.out.println("finish parse hotel.");
        loadJsonToTables.traverseReviews(path);
        System.out.println("finish parsing review!");
    }

    /**
     * Tests if a user already exists in the database. Requires an active
     * database connection.
     *
     * @param connection - active database connection
     * @param user       - username to check
     * @return server.Status.OK if user does not exist in database
     * @throws SQLException
     */
    private Status duplicateUser(Connection connection, String user) {

        assert connection != null;
        assert user != null;

        Status status = Status.ERROR;

        try (
                PreparedStatement statement = connection.prepareStatement(USER_SQL);
        ) {
            statement.setString(1, user);

            ResultSet results = statement.executeQuery();
            status = results.next() ? Status.DUPLICATE_USER : Status.OK;
        } catch (SQLException e) {
            log.debug(e.getMessage(), e);
            status = Status.SQL_EXCEPTION;
        }

        return status;
    }

    /**
     * Tests if a user already exists in the database.
     *
     * @param user - username to check
     * @return server.Status.OK if user does not exist in database
     * @see #duplicateUser(Connection, String)
     */
    public Status duplicateUser(String user) {
        Status status = Status.ERROR;

        try (
                Connection connection = db.getConnection();
        ) {
            status = duplicateUser(connection, user);
        } catch (SQLException e) {
            status = Status.CONNECTION_FAILED;
            log.debug(e.getMessage(), e);
        }

        return status;
    }

    /**
     * Returns the hex encoding of a byte array.
     *
     * @param bytes  - byte array to encode
     * @param length - desired length of encoding
     * @return hex encoded byte array
     */
    public static String encodeHex(byte[] bytes, int length) {
        BigInteger bigint = new BigInteger(1, bytes);
        String hex = String.format("%0" + length + "X", bigint);

        assert hex.length() == length;
        return hex;
    }

    /**
     * Calculates the hash of a password and salt using SHA-256.
     *
     * @param password - password to hash
     * @param salt     - salt associated with user
     * @return hashed password
     */
    public static String getHash(String password, String salt) {
        String salted = salt + password;
        String hashed = salted;

        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(salted.getBytes());
            hashed = encodeHex(md.digest(), 64);
        } catch (Exception ex) {
            log.debug("Unable to properly hash password.", ex);
        }

        return hashed;
    }

    /**
     * Registers a new user, placing the username, password hash, and
     * salt into the database if the username does not already exist.
     *
     * @param newuser - username of new user
     * @param newpass - password of new user
     * @return status ok if registration successful
     */
    private Status registerUser(Connection connection, String newuser, String newpass) {

        Status status = Status.ERROR;

        byte[] saltBytes = new byte[16];
        random.nextBytes(saltBytes);

        String usersalt = encodeHex(saltBytes, 32);
        String passhash = getHash(newpass, usersalt);
        String lastLogin = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        try (
                PreparedStatement sql = connection.prepareStatement(REGISTER_SQL);
        ) {
            sql.setString(1, newuser);
            sql.setString(2, passhash);
            sql.setString(3, usersalt);
            sql.setString(4, lastLogin);
            sql.executeUpdate();

            status = Status.OK;
        } catch (SQLException ex) {
            status = Status.SQL_EXCEPTION;
            log.debug(ex.getMessage(), ex);
        }

        return status;
    }

    /**
     * Registers a new user, placing the username, password hash, and
     * salt into the database if the username does not already exist.
     *
     * @param newuser - username of new user
     * @param newpass - password of new user
     * @return status.ok if registration successful
     */
    public Status registerUser(String newuser, String newpass) {
        Status status = Status.ERROR;
        log.debug("Registering " + newuser + ".");

        // make sure we have non-null and non-emtpy values for login
        if (isBlank(newuser)) {
            status = Status.INVALID_USERNAME;
            log.debug(status);
            return status;
        }
        // validate the password at backend!
        System.out.println("password: " + newpass);
        if (!validatePassword(newpass)) {
            System.out.println("the password is invalid!");
            status = Status.INVALID_PASSWORD;
            return status;
        }

        // try to connect to database and test for duplicate user
        System.out.println(db);

        try (
                Connection connection = db.getConnection();
        ) {
            status = duplicateUser(connection, newuser);

            // if okay so far, try to insert new user
            if (status == Status.OK) {
                status = registerUser(connection, newuser, newpass);
            }
        } catch (SQLException ex) {
            status = Status.CONNECTION_FAILED;
            log.debug(status, ex);
        }

        return status;
    }

    /**
     * This method is used for validate the password that user input;
     * the password has to be between 5 to 10 characters, contains at least  one number,
     * one letter and one special character.
     *
     * @param password
     * @return
     */
    private boolean validatePassword(String password) {
        return password.matches("^(?=.*[0-9])(?=.*[a-zA-Z])(?=.*[!@#$%^&+=])(?=\\S+$).{5,10}$");
    }

    /**
     * Gets the salt for a specific user.
     *
     * @param connection - active database connection
     * @param user       - which user to retrieve salt for
     * @return salt for the specified user or null if user does not exist
     * @throws SQLException if any issues with database connection
     */
    private String getSalt(Connection connection, String user) throws SQLException {
        assert connection != null;
        assert user != null;

        String salt = null;

        try (
                PreparedStatement statement = connection.prepareStatement(SALT_SQL);
        ) {
            statement.setString(1, user);

            ResultSet results = statement.executeQuery();

            if (results.next()) {
                salt = results.getString("usersalt");
            }
        }

        return salt;
    }

    /**
     * Checks if the provided username and password match what is stored
     * in the database. Must retrieve the salt and hash the password to
     * do the comparison.
     *
     * @param username - username to authenticate
     * @param password - password to authenticate
     * @return status.ok if authentication successful
     */
    public int authenticateUser(String username, String password) {
        log.debug("Authenticating user " + username + ".");
        int userId = -1;
        try (
                Connection connection = db.getConnection();
                PreparedStatement statement = connection.prepareStatement(AUTH_SQL);
        ) {
            String usersalt = getSalt(connection, username);
            String passhash = getHash(password, usersalt);

            statement.setString(1, username);
            statement.setString(2, passhash);

            ResultSet results = statement.executeQuery();
            if (results.next()) {
                userId = results.getInt("userId");
            }

        } catch (SQLException ex) {
            System.out.println(ex);
            ex.printStackTrace();
        }

        return userId;
    }

    /**
     * Retrives previous login time, and sets current time
     *
     * @param userId - id of user
     */
    public String updateLoginTime(int userId) {
        String prevLogin = "";
        try (
                Connection connection = db.getConnection();
                PreparedStatement statement = connection.prepareStatement(
                        "SELECT lastLogin FROM users WHERE userId=?;"
                );
        ) {
            statement.setInt(1, userId);
            ResultSet results = statement.executeQuery();

            if (results.next()) {
                prevLogin = results.getString("lastLogin");

                String lastLogin = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

                PreparedStatement statement2 = connection.prepareStatement(
                        "UPDATE users SET lastLogin=? WHERE userId=?;"
                );
                statement2.setString(1, lastLogin);
                statement2.setInt(2, userId);
            }
        } catch (SQLException ex) {
            System.out.println(ex);
            ex.printStackTrace();
        }
        return prevLogin;
    }

    /**
     * Removes a user from the database if the username and password are
     * provided correctly.
     *
     * @param username - username to remove
     * @param password - password of user
     * @return status.OK if removal successful
     */
    private Status removeUser(Connection connection, String username, String password) {
        Status status = Status.ERROR;

        try (
                PreparedStatement statement = connection.prepareStatement(DELETE_SQL);
        ) {
            statement.setString(1, username);

            int count = statement.executeUpdate();
            status = (count == 1) ? Status.OK : Status.INVALID_USERNAME;
        } catch (SQLException ex) {
            status = Status.SQL_EXCEPTION;
            log.debug(status, ex);
        }

        return status;
    }

    public void insertValueToHotels(String hotelId, String name, String address, String city, String lat, String lng, String link) {

        Connection connection = null;
        try {

            connection = db.getConnection();
            PreparedStatement sql = connection.prepareStatement(Insert_hotels);
            sql.setString(1, hotelId);
            sql.setString(2, name);
            sql.setString(3, address);
            sql.setString(4, city);
            sql.setString(5, lat);
            sql.setString(6, lng);
            sql.setString(7, link);
            sql.executeUpdate();
            connection.close();

        } catch (SQLException throwable) {
            throwable.printStackTrace();
        }
    }

    public Status insertValuesToReviews(String reviewId, String hotelId, int rating, String title,
                                        String reviewText, String customer, String date, int userId) {
        Status status;
        log.debug("Inserting " + reviewId + ".");

        // make sure we have non-null and non-emtpy values for login
        if (isBlank(rating + "") || isBlank(reviewText) || isBlank(title)) {
            status = Status.INVALID_LOGIN;
            log.debug(status);
            return status;
        }

        // try to connect to database
        //  System.out.println(db);

        try (
                Connection connection = db.getConnection();
        ) {
            try (
                    PreparedStatement sql = connection.prepareStatement(Insert_reviews);
            ) {
                sql.setString(1, reviewId);
                sql.setString(2, hotelId);
                sql.setInt(3, rating);
                sql.setString(4, title);
                sql.setString(5, reviewText);
                sql.setString(6, customer);
                sql.setString(7, date);
                sql.setInt(8, userId);
                sql.executeUpdate();
                connection.close();

                status = Status.OK;
            } catch (SQLException ex) {
                status = Status.SQL_EXCEPTION;
                log.debug(ex.getMessage(), ex);
            }
        } catch (SQLException ex) {
            status = Status.CONNECTION_FAILED;
            log.debug(status, ex);
        }
        return status;
    }

    public ArrayList<Hotel> findHotels(String city, String keyword) {
        ArrayList<Hotel> hotels = new ArrayList<>();
        try (
                Connection connection = db.getConnection();
                PreparedStatement sql = connection.prepareStatement(
                        "select hotelId, name, AVG(rating) as rating, link " +
                                "from hotels natural join reviews " +
                                "where city=? and name like ? group by hotelId;");
        ) {
            sql.setString(1, city);
            sql.setString(2, "%" + keyword + "%");
            ResultSet resultSet = sql.executeQuery();

            while (resultSet.next()) {
                hotels.add(new Hotel(
                        resultSet.getString("hotelId"),
                        resultSet.getString("name"),
                        resultSet.getFloat("rating"),
                        resultSet.getString("link")
                ));
            }
        } catch (SQLException ex) {
            System.out.println(ex);
            ex.printStackTrace();
            log.debug(Status.SQL_EXCEPTION, ex);
        }
        return hotels;
    }

    public ArrayList<Review> findReviews(String keyword) {
        ArrayList<Review> reviews = new ArrayList<>();
        try (
                Connection connection = db.getConnection();
                PreparedStatement sql = connection.prepareStatement(
                        "select hotelId, name, title, reviewText, customer, date " +
                                "from hotels natural join reviews " +
                                "where reviewText like ?;");
        ) {

            sql.setString(1, "% " + keyword + " %");
            ResultSet resultSet = sql.executeQuery();

            while (resultSet.next()) {
                reviews.add(new Review(
                        resultSet.getString("hotelId"),
                        resultSet.getString("name"),
                        resultSet.getString("title"),
                        resultSet.getString("reviewText"),
                        resultSet.getString("customer"),
                        resultSet.getString("date")
                ));
            }
        } catch (SQLException ex) {
            System.out.println(ex);
            ex.printStackTrace();
            log.debug(Status.SQL_EXCEPTION, ex);
        }
        return reviews;

    }

    public Hotel findOneHotelByHotelId(String hotelId) {
        Hotel hotel = new Hotel();
        try (
                Connection connection = db.getConnection();
                PreparedStatement sql = connection.prepareStatement(
                        "select name, address " +
                                "from hotels " +
                                "where hotelId=?;");
        ) {

            sql.setString(1, hotelId);
            ResultSet resultSet = sql.executeQuery();

            while (resultSet.next()) {
                hotel.setF(resultSet.getString("name"));
                hotel.setAd(resultSet.getString("address"));
            }
        } catch (SQLException ex) {
            System.out.println(ex);
            ex.printStackTrace();
            log.debug(Status.SQL_EXCEPTION, ex);
        }
        return hotel;

    }

    public ArrayList<Review> findReviewsByHotelId(String hotelId) {
        ArrayList<Review> reviews = new ArrayList<>();
        try (
                Connection connection = db.getConnection();
                PreparedStatement sql = connection.prepareStatement(
                        "select reviewId, rating, title, reviewText, customer, date, count(liked_reviews.userId) as likes " +
                                "from reviews left join liked_reviews using (reviewId) " +
                                "where hotelId= ? group by reviewId;");
        ) {

            sql.setString(1, hotelId);
            ResultSet resultSet = sql.executeQuery();

            while (resultSet.next()) {
                reviews.add(new Review(
                        resultSet.getString("reviewId"),
                        resultSet.getInt("rating"),
                        resultSet.getString("title"),
                        resultSet.getString("reviewText"),
                        resultSet.getString("customer"),
                        resultSet.getInt("likes"),
                        resultSet.getString("date")
                ));
            }
        } catch (SQLException ex) {
            System.out.println(ex);
            ex.printStackTrace();
            log.debug(Status.SQL_EXCEPTION, ex);
        }
        return reviews;

    }

    public int findUerIdByUsername(String name) {
        int userId = 0;
        try (
                Connection connection = db.getConnection();
                PreparedStatement sql = connection.prepareStatement(
                        "select userId " +
                                "from users " +
                                "where username= ?;");
        ) {
            sql.setString(1, name);
            ResultSet resultSet = sql.executeQuery();

            while (resultSet.next()) {
                userId = resultSet.getInt("userId");
            }
        } catch (SQLException ex) {
            System.out.println(ex);
            ex.printStackTrace();
            log.debug(Status.SQL_EXCEPTION, ex);
        }
        return userId;
    }

    public String getAlphaNumericString(int n) {

        // chose a Character random from this String
        String AlphaNumericString = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
                + "0123456789"
                + "abcdefghijklmnopqrstuvxyz";

        // create StringBuffer size of AlphaNumericString
        StringBuilder sb = new StringBuilder(n);

        for (int i = 0; i < n; i++) {

            // generate a random number between
            // 0 to AlphaNumericString variable length
            int index
                    = (int) (AlphaNumericString.length()
                    * Math.random());

            // add Character one by one in end of sb
            sb.append(AlphaNumericString
                    .charAt(index));
        }

        return sb.toString();
    }


    public ArrayList<Review> findReviewByUserId(int userId) {
        ArrayList<Review> reviews = new ArrayList<>();
        try (
                Connection connection = db.getConnection();
                PreparedStatement sql = connection.prepareStatement(
                        "select reviewId,rating,title, reviewText, customer, date, userId " +
                                "from reviews " +
                                "where userId= ?;");
        ) {

            sql.setInt(1, userId);
            ResultSet resultSet = sql.executeQuery();

            while (resultSet.next()) {
                reviews.add(new Review(
                        resultSet.getString("reviewId"),
                        resultSet.getInt("rating"),
                        resultSet.getString("title"),
                        resultSet.getString("reviewText"),
                        resultSet.getString("customer"),
                        resultSet.getString("date"),
                        resultSet.getInt("userId")

                ));
            }
        } catch (SQLException ex) {
            System.out.println(ex);
            ex.printStackTrace();
            log.debug(Status.SQL_EXCEPTION, ex);
        }
        return reviews;
    }

    public void deleteAReviewByReviewId(String reviewId) {
        try (
                Connection connection = db.getConnection();

                PreparedStatement sql = connection.prepareStatement(
                        "delete " +
                                "from reviews " +
                                "where reviewId= ?;");
        ) {
            sql.setString(1, reviewId);
            sql.executeUpdate();

        } catch (SQLException ex) {
            System.out.println(ex);
            ex.printStackTrace();
            log.debug(Status.SQL_EXCEPTION, ex);
        }
    }

    public Status updateReview(String reviewId, int rating, String title, String text, String date) {
        Status status = Status.ERROR;
        log.debug("Updating " + reviewId + ".");
        try (
                Connection connection = db.getConnection();
                PreparedStatement sql = connection.prepareStatement(
                        "UPDATE reviews SET rating=?, title=?, reviewText=?, date=? WHERE reviewId=?;"
                );
        ) {
            sql.setInt(1, rating);
            sql.setString(2, title);
            sql.setString(3, text);
            sql.setString(4, date);
            sql.setString(5, reviewId);
            sql.executeUpdate();
            status = Status.OK;
        } catch (SQLException ex) {
            System.out.println(ex);
            ex.printStackTrace();
            log.debug(Status.SQL_EXCEPTION, ex);
        }
        return status;
    }

    public void saveHotel(int userId, String hotelId) {
        try (
                Connection connection = db.getConnection();
                PreparedStatement sql = connection.prepareStatement(
                        "insert into saved_hotels (userId, hotelId) VALUES (?,?);"
                );
        ) {
            sql.setInt(1, userId);
            sql.setString(2, hotelId);
            sql.executeUpdate();
        } catch (SQLException ex) {
            System.out.println(ex);
            ex.printStackTrace();
            log.debug(Status.SQL_EXCEPTION, ex);
        }
    }

    public void likeReview(int userId, String reviewId) {
        try (
                Connection connection = db.getConnection();
                PreparedStatement sql = connection.prepareStatement(
                        "insert into liked_reviews (userId, reviewId) VALUES (?,?);"
                );
        ) {
            sql.setInt(1, userId);
            sql.setString(2, reviewId);
            sql.executeUpdate();
        } catch (SQLException ex) {
            System.out.println(ex);
            ex.printStackTrace();
            log.debug(Status.SQL_EXCEPTION, ex);
        }
    }

    public void visitLink(int userId, String link) {
        try (
                Connection connection = db.getConnection();
                PreparedStatement sql = connection.prepareStatement(
                        "insert into visited_links (userId, link) VALUES (?,?);"
                );
        ) {
            sql.setInt(1, userId);
            sql.setString(2, link);
            sql.executeUpdate();
        } catch (SQLException ex) {
            System.out.println(ex);
            ex.printStackTrace();
            log.debug(Status.SQL_EXCEPTION, ex);
        }
    }

    public void clearSavedHotels(int userId) {
        try (
                Connection connection = db.getConnection();
                PreparedStatement sql = connection.prepareStatement(
                        "delete from saved_hotels where userId=?;"
                );
        ) {
            sql.setInt(1, userId);
            sql.executeUpdate();
        } catch (SQLException ex) {
            System.out.println(ex);
            ex.printStackTrace();
            log.debug(Status.SQL_EXCEPTION, ex);
        }
    }

    public void clearVisitedLinks(int userId) {
        try (
                Connection connection = db.getConnection();
                PreparedStatement sql = connection.prepareStatement(
                        "delete from visited_links where userId=?;"
                );
        ) {
            sql.setInt(1, userId);
            sql.executeUpdate();
        } catch (SQLException ex) {
            System.out.println(ex);
            ex.printStackTrace();
            log.debug(Status.SQL_EXCEPTION, ex);
        }
    }

    public ArrayList<Hotel> findSavedHotels(int userId) {
        ArrayList<Hotel> hotels = new ArrayList<>();
        try (
                Connection connection = db.getConnection();
                PreparedStatement sql = connection.prepareStatement(
                        "select hotelId, name, AVG(rating) as rating, link " +
                                "from hotels left join reviews using (hotelId) " +
                                "join saved_hotels using (hotelId) " +
                                "where saved_hotels.userId=? group by hotelId;");
        ) {
            sql.setInt(1, userId);
            ResultSet resultSet = sql.executeQuery();

            while (resultSet.next()) {
                hotels.add(new Hotel(
                        resultSet.getString("hotelId"),
                        resultSet.getString("name"),
                        resultSet.getFloat("rating"),
                        resultSet.getString("link")
                ));
            }
        } catch (SQLException ex) {
            System.out.println(ex);
            ex.printStackTrace();
            log.debug(Status.SQL_EXCEPTION, ex);
        }
        return hotels;
    }

    public ArrayList<String> findVisitedLinks(int userId) {
        ArrayList<String> links = new ArrayList<>();
        try (
                Connection connection = db.getConnection();
                PreparedStatement sql = connection.prepareStatement(
                        "select link from visited_links where userId=?;"
                );
        ) {
            sql.setInt(1, userId);
            ResultSet resultSet = sql.executeQuery();

            while (resultSet.next()) {
                links.add(resultSet.getString("link"));
            }
        } catch (SQLException ex) {
            System.out.println(ex);
            ex.printStackTrace();
            log.debug(Status.SQL_EXCEPTION, ex);
        }
        return links;
    }
}
