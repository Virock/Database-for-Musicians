/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cs4222.project;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Scanner;

/**
 *
 * @author viroc
 */
public class CS4222Project {

    /*
    private static final String PORT = "5433";
    private static final String NAME_OF_DATABASE = "CS4222Project";
    private static final String USERNAME = "postgres";
    private static final String PASSWORD = "california";
     */
    private static final String PORT = "5432";
    private static final String NAME_OF_DATABASE = "cs4222s01";
    private static final String USERNAME = "cs4222s01";
    private static final String PASSWORD = "california";

    /**
     * @param args the command line arguments
     */
    public static void errorHandler(Exception e) {
        System.err.println(e.getClass().getName() + ": " + e.getMessage());
    }

    public static void createTables(Connection c) {
        try {
            Statement stmt = c.createStatement();
            String sql = "CREATE TABLE IF NOT EXISTS ALBUM "
                    + "(UID             SERIAL  NOT NULL PRIMARY KEY,"
                    + " TITLE           TEXT,"
                    + " AID             TEXT UNIQUE,"
                    + " COPYDATE        TEXT,"
                    + " FORMAT          TEXT)";
            stmt.executeUpdate(sql);
            stmt.close();

            stmt = c.createStatement();
            sql = "CREATE TABLE IF NOT EXISTS MUSICIAN "
                    + "(SSN    CHAR(9)  NOT NULL PRIMARY KEY,"
                    + " NAME   TEXT)";
            stmt.executeUpdate(sql);
            stmt.close();

            stmt = c.createStatement();
            sql = "CREATE TABLE IF NOT EXISTS SONG "
                    + "(TITLE   TEXT NOT NULL,"
                    + " AUTHOR  CHAR(9) NOT NULL,"
                    + " Primary Key (TITLE,AUTHOR))";
            stmt.executeUpdate(sql);
            stmt.close();

            stmt = c.createStatement();
            sql = "CREATE TABLE IF NOT EXISTS INSTRUMENT "
                    + "(ID    SERIAL NOT NULL PRIMARY KEY,"
                    + " KEY   TEXT,"
                    + " NAME  TEXT)";
            stmt.executeUpdate(sql);
            stmt.close();

            stmt = c.createStatement();
            sql = "CREATE TABLE IF NOT EXISTS ADDRESS "
                    + "(ADDRESS TEXT NOT NULL PRIMARY KEY)";
            stmt.executeUpdate(sql);
            stmt.close();

            stmt = c.createStatement();
            sql = "CREATE TABLE IF NOT EXISTS PHONE "
                    + "(PHONENO CHAR(10) NOT NULL PRIMARY KEY)";
            stmt.executeUpdate(sql);
            stmt.close();

            //Relationships
            stmt = c.createStatement();
            sql = "CREATE TABLE IF NOT EXISTS PRODUCES "
                    + "(UID INTEGER PRIMARY KEY NOT NULL REFERENCES ALBUM"
                    + "             ON DELETE CASCADE"
                    + "             ON UPDATE CASCADE,"
                    + " SSN CHAR(9) REFERENCES MUSICIAN"
                    + "             ON DELETE CASCADE"
                    + "             ON UPDATE CASCADE)";
            stmt.executeUpdate(sql);
            stmt.close();

            stmt = c.createStatement();
            sql = "CREATE TABLE IF NOT EXISTS CONTAINS "
                    + "(UID     INTEGER REFERENCES ALBUM"
                    + "             ON DELETE CASCADE"
                    + "             ON UPDATE CASCADE,"
                    + " TITLE   TEXT NOT NULL,"
                    + " AUTHOR  CHAR(9) NOT NULL,"
                    + " Primary Key (TITLE,AUTHOR),"
                    + " FOREIGN Key (TITLE, AUTHOR) REFERENCES SONG (TITLE, AUTHOR)"
                    + "             ON DELETE CASCADE"
                    + "             ON UPDATE CASCADE)";
            stmt.executeUpdate(sql);
            stmt.close();

            stmt = c.createStatement();
            sql = "CREATE TABLE IF NOT EXISTS PERFORMED_BY "
                    + "(SSN     CHAR(9) REFERENCES MUSICIAN"
                    + "             ON DELETE CASCADE"
                    + "             ON UPDATE CASCADE,"
                    + " TITLE   TEXT NOT NULL,"
                    + " AUTHOR  CHAR(9) NOT NULL, "
                    + " FOREIGN Key (TITLE, AUTHOR) REFERENCES SONG (TITLE, AUTHOR)"
                    + "             ON DELETE CASCADE"
                    + "             ON UPDATE CASCADE)";
            stmt.executeUpdate(sql);
            stmt.close();

            stmt = c.createStatement();
            sql = "CREATE TABLE IF NOT EXISTS USED_IN "
                    + "(ID     INTEGER REFERENCES INSTRUMENT"
                    + "             ON DELETE CASCADE"
                    + "             ON UPDATE CASCADE,"
                    + " TITLE   TEXT NOT NULL,"
                    + " AUTHOR  CHAR(9) NOT NULL, "
                    + " FOREIGN Key (TITLE,AUTHOR) REFERENCES SONG (TITLE, AUTHOR)"
                    + "             ON DELETE CASCADE"
                    + "             ON UPDATE CASCADE)";
            stmt.executeUpdate(sql);
            stmt.close();

            stmt = c.createStatement();
            sql = "CREATE TABLE IF NOT EXISTS PLAYS "
                    + "(ID     INTEGER REFERENCES INSTRUMENT"
                    + "             ON DELETE CASCADE"
                    + "             ON UPDATE CASCADE,"
                    + " SSN   CHAR(9) REFERENCES MUSICIAN"
                    + "             ON DELETE CASCADE"
                    + "             ON UPDATE CASCADE,"
                    + " Primary Key (ID,SSN))";
            stmt.executeUpdate(sql);
            stmt.close();

            stmt = c.createStatement();
            sql = "CREATE TABLE IF NOT EXISTS LIVES_IN "
                    + "(SSN     CHAR(9) PRIMARY KEY NOT NULL REFERENCES MUSICIAN"
                    + "             ON DELETE CASCADE"
                    + "             ON UPDATE CASCADE,"
                    + " ADDRESS   TEXT REFERENCES ADDRESS"
                    + "             ON DELETE CASCADE"
                    + "             ON UPDATE CASCADE)";
            stmt.executeUpdate(sql);
            stmt.close();

            stmt = c.createStatement();
            sql = "CREATE TABLE IF NOT EXISTS HAS "
                    + "(ADDRESS     TEXT PRIMARY KEY NOT NULL REFERENCES ADDRESS"
                    + "             ON DELETE CASCADE"
                    + "             ON UPDATE CASCADE,"
                    + " PHONENO   CHAR(10) REFERENCES PHONE"
                    + "             ON DELETE CASCADE"
                    + "             ON UPDATE CASCADE)";
            stmt.executeUpdate(sql);
            stmt.close();

            //Stored procedures
            stmt = c.createStatement();
            sql = "CREATE OR REPLACE FUNCTION TOTAL_SONGS() "
                    + "RETURNS INT AS $$ "
                    + "DECLARE COUNT_VAR INTEGER; "
                    + "BEGIN "
                    + "     SELECT INTO COUNT_VAR COUNT(*) FROM SONG; "
                    + "     RETURN COUNT_VAR; "
                    + "END; "
                    + "$$ LANGUAGE plpgsql;";
            stmt.executeUpdate(sql);
            stmt.close();

            /*
            stmt = c.createStatement();
            sql = "DROP FUNCTION IF EXISTS CD_ALBUM()";
            stmt.executeUpdate(sql);
            stmt.close();
             */
            stmt = c.createStatement();
            sql = "CREATE OR REPLACE FUNCTION CD_ALBUM() "
                    + "RETURNS DECIMAL AS $$ "
                    + "DECLARE COUNT_VAR_CD DECIMAL; "
                    + "DECLARE COUNT_VAR_TOTAL DECIMAL; "
                    + "BEGIN "
                    + "     SELECT INTO COUNT_VAR_CD COUNT(*) FROM ALBUM WHERE FORMAT = 'CD' OR FORMAT = 'cd'; "
                    + "     SELECT INTO COUNT_VAR_TOTAL COUNT(*) FROM ALBUM; "
                    + "     IF COUNT_VAR_TOTAL = 0 THEN "
                    + "         RETURN 0; "
                    + "     ELSE "
                    + "         RETURN CAST((COUNT_VAR_CD / COUNT_VAR_TOTAL) * 100 AS DECIMAL); "
                    + "     END IF; "
                    + "END; "
                    + "$$ LANGUAGE plpgsql;";
            stmt.executeUpdate(sql);
            stmt.close();

            stmt = c.createStatement();
            sql = "DROP FUNCTION IF EXISTS REMOVE_ALBUM() CASCADE";
            stmt.executeUpdate(sql);
            stmt.close();

            //Triggers
            //When all songs are deleted in an album, delete the album
            stmt = c.createStatement();
            sql = "CREATE OR REPLACE FUNCTION REMOVE_ALBUM() "
                    + "RETURNS TRIGGER AS $$ "
                    + "DECLARE COUNT_VAR INTEGER; "
                    + "BEGIN "
                    + "     SELECT INTO COUNT_VAR COUNT(*) FROM CONTAINS WHERE UID = OLD.UID; "
                    + "     IF COUNT_VAR = 0 THEN "
                    + "         DELETE FROM ALBUM WHERE UID = OLD.UID; " //Delete album with the same UID
                    + "     END IF; "
                    + "     RETURN NULL; "
                    + "END; "
                    + "$$ LANGUAGE plpgsql;";
            stmt.executeUpdate(sql);
            stmt.close();

            stmt = c.createStatement();
            sql = "CREATE TRIGGER REMOVE_ALBUM AFTER DELETE ON CONTAINS "
                    + "FOR EACH ROW EXECUTE PROCEDURE REMOVE_ALBUM()";
            stmt.executeUpdate(sql);
            stmt.close();

            stmt = c.createStatement();
            sql = "DROP FUNCTION IF EXISTS SONG_RESTRICT() CASCADE";
            stmt.executeUpdate(sql);
            stmt.close();

            //When the number of songs in an album wants to pass 15, don't allow insert
            stmt = c.createStatement();
            sql = "CREATE OR REPLACE FUNCTION SONG_RESTRICT() "
                    + "RETURNS TRIGGER AS $$ "
                    + "DECLARE COUNT_VAR INTEGER; "
                    + "BEGIN "
                    + "     SELECT INTO COUNT_VAR COUNT(*) FROM CONTAINS WHERE UID = NEW.UID; "
                    + "     IF COUNT_VAR > 15 THEN "
                    + "         DELETE FROM SONG WHERE TITLE = NEW.TITLE AND AUTHOR = NEW.AUTHOR; " //Delete the song
                    + "     END IF; "
                    + "     RETURN NULL; "
                    + "END; "
                    + "$$ LANGUAGE plpgsql;";
            stmt.executeUpdate(sql);
            stmt.close();

            stmt = c.createStatement();
            sql = "CREATE TRIGGER SONG_RESTRICT AFTER INSERT ON CONTAINS "
                    + "FOR EACH ROW EXECUTE PROCEDURE SONG_RESTRICT()";
            stmt.executeUpdate(sql);
            stmt.close();

        } catch (Exception e) {
            errorHandler(e);
        }
    }

    public static void deleteIndivTable(Connection c, String tableName) {
        try {
            Statement stmt = c.createStatement();
            String sql = "DROP TABLE IF EXISTS " + tableName + " CASCADE";
            stmt.executeUpdate(sql);
            stmt.close();
        } catch (Exception e) {
            errorHandler(e);
        }
    }

    public static void deleteAllTables(Connection c) {
        deleteIndivTable(c, "ALBUM");
        deleteIndivTable(c, "ADDRESS");
        deleteIndivTable(c, "CONTAINS");
        deleteIndivTable(c, "HAS");
        deleteIndivTable(c, "INSTRUMENT");
        deleteIndivTable(c, "LIVES_IN");
        deleteIndivTable(c, "MUSICIAN");
        deleteIndivTable(c, "PERFORMED_BY");
        deleteIndivTable(c, "PHONE");
        deleteIndivTable(c, "PLAYS");
        deleteIndivTable(c, "PRODUCES");
        deleteIndivTable(c, "SONG");
        deleteIndivTable(c, "USED_IN");
    }

    public static void insertThreeValuesIntoTable(Connection c, String tableName, String columnName1, String columnName2, String columnName3, String value1, String value2, String value3) {
        try {
            Statement stmt = c.createStatement();
            String sql = "INSERT INTO " + tableName + " (" + columnName1 + ", " + columnName2 + ", " + columnName3 + ") "
                    + "VALUES ('" + value1 + "', '" + value2 + "', '" + value3 + "');";
            stmt.executeUpdate(sql);
            stmt.close();
        } catch (Exception e) {
            errorHandler(e);
        }
    }

    public static void insertTwoValuesIntoTable(Connection c, String tableName, String columnName1, String columnName2, String value1, String value2) {
        try {
            Statement stmt = c.createStatement();
            String sql = "INSERT INTO " + tableName + " (" + columnName1 + ", " + columnName2 + ") "
                    + "VALUES ('" + value1 + "', '" + value2 + "');";
            stmt.executeUpdate(sql);
            stmt.close();
        } catch (Exception e) {
            errorHandler(e);
        }
    }

    public static void insertOneValueIntoTable(Connection c, String tableName, String columnName, String value) {
        try {
            Statement stmt = c.createStatement();
            String sql = "INSERT INTO " + tableName + " (" + columnName + ") "
                    + "VALUES ('" + value + "');";
            stmt.executeUpdate(sql);
            stmt.close();
        } catch (Exception e) {
            errorHandler(e);
        }
    }

    public static void removeRowFromTable(Connection c, String tableName, String columnName, String columnValue) {
        try {
            Statement stmt = c.createStatement();
            String sql = "DELETE FROM " + tableName + " WHERE " + columnName + " = '" + columnValue + "';";
            stmt.executeUpdate(sql);
            stmt.close();
        } catch (Exception e) {
            errorHandler(e);
        }
    }

    public static void removeRowFromTable(Connection c, String tableName, String columnName1, String columnName2, String columnValue1, String columnValue2) {
        try {
            Statement stmt = c.createStatement();
            String sql = "DELETE FROM " + tableName + " WHERE " + columnName1 + " = '" + columnValue1 + "' AND " + columnName2 + " = '" + columnValue2 + "';";
            stmt.executeUpdate(sql);
            stmt.close();
        } catch (Exception e) {
            errorHandler(e);
        }
    }

    public static boolean checkIfExists(Connection c, String tableName, String columnName, String value) {
        try {
            Statement stmt = c.createStatement();
            String sql = "SELECT * FROM " + tableName + " WHERE " + columnName + " = '" + value + "'";
            ResultSet result = stmt.executeQuery(sql);
            if (result.next() == false) {
                //System.err.println("The musician does not exist");
                return false;
            }
            //result.close();
        } catch (Exception e) {
            errorHandler(e);
        }
        return true;
    }

    public static boolean checkIfExists(Connection c, String tableName, String columnName1, String columnName2, String columnValue1, String columnValue2) {
        try {
            Statement stmt = c.createStatement();
            String sql = "SELECT * FROM " + tableName + " WHERE " + columnName1 + " = '" + columnValue1 + "' AND " + columnName2 + " = '" + columnValue2 + "';";
            ResultSet result = stmt.executeQuery(sql);
            if (result.next() == false) {
                //System.err.println("The musician does not exist");
                return false;
            }
            //result.close();
        } catch (Exception e) {
            errorHandler(e);
        }
        return true;
    }

    public static boolean checkIfExists(Connection c, String tableName, String columnName1, String columnName2, String columnName3, String columnValue1, String columnValue2, String columnValue3) {
        try {
            Statement stmt = c.createStatement();
            String sql = "SELECT * FROM " + tableName + " WHERE " + columnName1 + " = '" + columnValue1 + "' AND " + columnName2 + " = '" + columnValue2 + "' AND " + columnName3 + " = '" + columnValue3 + "';";
            ResultSet result = stmt.executeQuery(sql);
            if (result.next() == false) {
                //System.err.println("The musician does not exist");
                return false;
            }
            //result.close();
        } catch (Exception e) {
            errorHandler(e);
        }
        return true;
    }

    public static void listTwoColumnTableContents(Connection c, String tableName, String columnName1, String columnName2) {
        System.out.println(tableName + ": ");
        try {
            Statement stmt = c.createStatement();
            String sql = "SELECT * FROM " + tableName;
            ResultSet result = stmt.executeQuery(sql);
            while (result.next() != false) {
                System.out.println(columnName1 + ": " + result.getString(columnName1) + " " + columnName2 + ": " + result.getString(columnName2));
            }
            result.close();
            stmt.close();
        } catch (Exception e) {
            errorHandler(e);
        }
    }

    public static void listSingleColumnTableContents(Connection c, String tableName, String columnName) {
        System.out.println(tableName + ": ");
        try {
            Statement stmt = c.createStatement();
            String sql = "SELECT * FROM " + tableName;
            ResultSet result = stmt.executeQuery(sql);
            while (result.next() != false) {
                System.out.println(columnName + ": " + result.getString(columnName));
            }
            result.close();
            stmt.close();
        } catch (Exception e) {
            errorHandler(e);
        }
    }

    public static void listThreeColumnTableContents(Connection c, String tableName, String columnName1, String columnName2, String columnName3) {
        System.out.println(tableName + ": ");
        try {
            Statement stmt = c.createStatement();
            String sql = "SELECT * FROM " + tableName;
            ResultSet result = stmt.executeQuery(sql);
            while (result.next() != false) {
                System.out.println(columnName1 + ": " + result.getString(columnName1) + " " + columnName2 + ": " + result.getString(columnName2) + " " + columnName3 + ": " + result.getString(columnName3));
            }
            result.close();
            stmt.close();
        } catch (Exception e) {
            errorHandler(e);
        }
    }

    public static ArrayList<Instrument> collectInstruments(String songOrMusician) {
        Scanner scanner = new Scanner(System.in);
        scanner.useDelimiter("\n");
        ArrayList<Instrument> instruments = new ArrayList<>();
        String instrumentName, instrumentKey;
        String question = "";
        if (songOrMusician.equals("musician")) {
            question = "Enter the instruments the musician plays (Enter '0' to stop adding instruments): ";
        } else if (songOrMusician.equals("song")) {
            question = "Enter the instruments used in this song (Enter '0' to stop adding instruments): ";
        }
        boolean instrumentDuplicate = false;
        do {
            System.out.println(question);
            instrumentName = scanner.next();
            if (instrumentName.equals("0")) {
                break;
            }
            System.out.println("Enter the key of the instrument: ");
            instrumentKey = scanner.next();

            //Search through the instruments
            //If the instrument has been added before
            for (Instrument instrument : instruments) {
                if (instrument.equals(new Instrument(instrumentKey, instrumentName))) {
                    //Show an error
                    if (songOrMusician.equals("musician")) {
                        System.err.println("You have entered this instrument before for this musician");
                    } else if (songOrMusician.equals("song")) {
                        System.err.println("You have entered this instrument before for this song");
                    }
                    instrumentDuplicate = true;
                    break;
                }
            }
            if (!instrumentDuplicate) {
                //Input the instrument only if a duplicate is not found in the list of instruments
                instruments.add(new Instrument(instrumentKey, instrumentName));
            }
            instrumentDuplicate = false;

        } while (!instrumentName.equals("0"));
        return instruments;
    }

    public static void addInstrumentToDatabaseForSong(Connection c, String title, String author, ArrayList<Instrument> instruments) {
        instruments.forEach((instrument) -> {
            //Check if the instrument exists already in the database
            try {
                Statement stmt = c.createStatement();
                String sql = "SELECT * FROM INSTRUMENT WHERE KEY = '" + instrument.getKey() + "' AND NAME = '" + instrument.getName() + "'";
                ResultSet result = stmt.executeQuery(sql);
                //If the instrument exists in the database already
                if (result.next() != false) {
                    int ID = result.getInt("ID");
                    //If it does, just add the instrument to the list of instruments used in the song
                    if (!checkIfExists(c, "USED_IN", "ID", "TITLE", "AUTHOR", String.valueOf(ID), title, author)) {
                        insertThreeValuesIntoTable(c, "USED_IN", "ID", "TITLE", "AUTHOR", String.valueOf(ID), title, author);
                    }
                } else {
                    //If it doesn't exist in the database, add it to the database
                    stmt = c.createStatement();
                    sql = "INSERT INTO INSTRUMENT (KEY, NAME) "
                            + "VALUES ('" + instrument.getKey() + "', '" + instrument.getName() + "') RETURNING ID;";
                    result = stmt.executeQuery(sql);
                    result.next();
                    int ID = result.getInt("ID");
                    stmt.close();
                    insertThreeValuesIntoTable(c, "USED_IN", "ID", "TITLE", "AUTHOR", String.valueOf(ID), title, author);
                }
                result.close();
                stmt.close();
            } catch (Exception e) {
                errorHandler(e);
            }
        });
    }

    public static void addInstrumentToDatabaseForMusician(Connection c, String SSN, ArrayList<Instrument> instruments) {
        instruments.forEach((instrument) -> {
            //Check if the instrument exists already in the database
            try {
                Statement stmt = c.createStatement();
                String sql = "SELECT * FROM INSTRUMENT WHERE KEY = '" + instrument.getKey() + "' AND NAME = '" + instrument.getName() + "'";
                ResultSet result = stmt.executeQuery(sql);
                //If the instrument exists in the database already
                if (result.next() != false) {
                    int ID = result.getInt("ID");
                    //If it does, just add the musician to the list of people who use the instrument
                    if (!checkIfExists(c, "PLAYS", "ID", "SSN", String.valueOf(ID), SSN)) {
                        insertTwoValuesIntoTable(c, "PLAYS", "ID", "SSN", String.valueOf(ID), SSN);
                    }
                } else {
                    //If it doesn't exist in the database, add it to the database
                    stmt = c.createStatement();
                    sql = "INSERT INTO INSTRUMENT (KEY, NAME) "
                            + "VALUES ('" + instrument.getKey() + "', '" + instrument.getName() + "') RETURNING ID;";
                    result = stmt.executeQuery(sql);
                    result.next();
                    int ID = result.getInt("ID");
                    stmt.close();
                    insertTwoValuesIntoTable(c, "PLAYS", "ID", "SSN", String.valueOf(ID), SSN);
                }
                result.close();
                stmt.close();
            } catch (Exception e) {
                errorHandler(e);
            }
        });
    }

    public static String ensureCorrectValuesAreEntered(String question, boolean acceptZero) {
        boolean atleastOneAlpha = false;
        String SSN;
        Scanner scanner = new Scanner(System.in);
        do {

            System.out.println(question);
            SSN = scanner.next();
            if (acceptZero) {
                if (SSN.equals("0")) {
                    break;
                }
            }
            //SSN must be exactly 9 characters long
            if (SSN.length() != 9) {
                System.err.println("SSN must be 9 digits long");
            }
            atleastOneAlpha = SSN.matches(".*[a-zA-Z]+.*");
            //SSN cannot contain letters
            if (atleastOneAlpha) {
                System.err.println("SSN cannot contain letters");
            }
        } while (atleastOneAlpha || SSN.length() != 9);
        return SSN;
    }

    public static void addMusician(Connection c, String SSN) {
        String name, address, phoneNumber;
        phoneNumber = "";
        Scanner scanner = new Scanner(System.in);
        if (SSN == null) {
            boolean exists;
            do {
                SSN = ensureCorrectValuesAreEntered("Enter the musician's SSN: ", false);
                //Check to make sure the musician is not already in the database
                exists = checkIfExists(c, "MUSICIAN", "SSN", SSN);
                if (exists) {
                    System.err.println("This musician already exists in the database");
                }
            } while (exists);
        }
        System.out.print("Enter the musician's name: ");
        scanner.useDelimiter("\n");
        name = scanner.next();

        ArrayList<Instrument> instruments = collectInstruments("musician");

        System.out.print("Enter the musician's address: ");
        address = scanner.next();
        //Search for this address in the database
        boolean addressExists = false;
        try {
            Statement stmt = c.createStatement();
            String sql = "SELECT * FROM ADDRESS WHERE ADDRESS = '" + address + "'";
            ResultSet result = stmt.executeQuery(sql);
            if (result.next() != false) {
                addressExists = true;
                //If it exists, don't ask for phone number
                //and use the phone number for that address
                result.close();
                stmt.close();
                stmt = c.createStatement();
                sql = "SELECT * FROM HAS WHERE ADDRESS = '" + address + "'";
                result = stmt.executeQuery(sql);
                result.next();
                phoneNumber = result.getString("PHONENO");
                stmt.close();
                result.close();
            }
        } catch (Exception e) {
            errorHandler(e);
        }

        if (!addressExists) {
            boolean atleastOneAlpha = false;
            do {
                //Phone number cannot contain letters
                if (atleastOneAlpha) {
                    System.err.println("Phone number cannot contain letters");
                }
                System.out.print("Enter the phoneNumber for that address (Enter '0' if the address doesn't have a phone): ");
                scanner = null;
                scanner = new Scanner(System.in);
                phoneNumber = scanner.next();
                if (phoneNumber.equals("0")) {
                    break;
                }
                atleastOneAlpha = phoneNumber.matches(".*[a-zA-Z]+.*");
                //Phone number must be exactly 10 characters long or '0' if no phone number exists
                if (phoneNumber.length() != 10) {
                    System.err.println("Phone number must be 10 digits long");
                }
            } while (atleastOneAlpha || phoneNumber.length() != 10);
        }
        insertTwoValuesIntoTable(c, "MUSICIAN", "SSN", "NAME", SSN, name);
        //If address exists, don't insert it again
        if (!addressExists) {
            insertOneValueIntoTable(c, "ADDRESS", "ADDRESS", address);
            if (!phoneNumber.equals("0")) {
                insertOneValueIntoTable(c, "PHONE", "PHONENO", phoneNumber);
                insertTwoValuesIntoTable(c, "HAS", "ADDRESS", "PHONENO", address, phoneNumber);
            }
        }
        insertTwoValuesIntoTable(c, "LIVES_IN", "SSN", "ADDRESS", SSN, address);

        //Iterate through instruments and add instruments into database
        addInstrumentToDatabaseForMusician(c, SSN, instruments);
    }

    public static int addAlbum(Connection c, String AID) {
        Scanner scanner = new Scanner(System.in);
        String title, copyRightDate, format, producerSSN;
        int UID = 0;
        scanner.useDelimiter("\n");
        System.out.print("Enter the album's title: ");
        title = scanner.next();
        System.out.print("Enter the copyright date: ");

        copyRightDate = scanner.next();
        System.out.print("Enter the format: ");
        format = scanner.next();
        if (AID == null) {
            boolean AIDExists = false;
            do {
                System.out.print("Enter the album's ID: ");
                AID = scanner.next();
                //If AID exists in database
                AIDExists = checkIfExists(c, "ALBUM", "AID", AID);
                if (AIDExists) {
                    System.err.println("This album already exists in the database.");
                }
                //Inform the user of this and ask for AID again
            } while (AIDExists);
        }
        producerSSN = ensureCorrectValuesAreEntered("Enter the producer's SSN: ", false);
        try {
            if (!checkIfExists(c, "MUSICIAN", "SSN", producerSSN)) {
                addMusician(c, producerSSN);
            }

            Statement stmt = c.createStatement();
            String sql = "INSERT INTO ALBUM (TITLE, AID, COPYDATE, FORMAT) "
                    + "VALUES ('" + title + "', '" + AID + "', '" + copyRightDate + "', '" + format + "') RETURNING UID;";
            ResultSet result = stmt.executeQuery(sql);
            result.next();
            UID = result.getInt("UID");
            insertTwoValuesIntoTable(c, "PRODUCES", "UID", "SSN", String.valueOf(UID), producerSSN);

            result.close();
            stmt.close();
        } catch (Exception e) {
            errorHandler(e);

        }
        return UID;
    }

    public static void main(String[] args) {
        Connection c = null;
        try {
            Class.forName("org.postgresql.Driver");
            //c = DriverManager.getConnection("jdbc:postgresql://localhost:" + PORT + "/" + NAME_OF_DATABASE, USERNAME, PASSWORD);
            c = DriverManager.getConnection("jdbc:postgresql://cs1.calstatela.edu:" + PORT + "/" + NAME_OF_DATABASE, USERNAME, PASSWORD);
            c.setAutoCommit(true);
            Statement stmt = null;

            //deleteAllTables(c);
            createTables(c);

        } catch (Exception e) {
            errorHandler(e);
        }
        int choice = 0;
        do {

            //Retrieve total songs
            /*try {
                Statement stmt = c.createStatement();
                String sql = "SELECT TOTAL_SONGS()";
                ResultSet result = stmt.executeQuery(sql);
                result.next();
                System.out.println(result.getInt(1));
            } catch (Exception e) {
                errorHandler(e);
            }*/
            //Retrieve percentage of albums in CD format
            /*try {
                Statement stmt = c.createStatement();
                String sql = "SELECT CD_ALBUM()";
                ResultSet result = stmt.executeQuery(sql);
                result.next();
                System.out.println(result.getFloat(1));
            } catch (Exception e) {
                errorHandler(e);
            }*/
            System.out.println("What operation would you like to carry out");
            System.out.println("(1) Add a musician");
            System.out.println("(2) Remove a musician");
            System.out.println("(3) Add an album");
            System.out.println("(4) Remove an album");
            System.out.println("(5) Add a song");
            System.out.println("(6) Remove a song");
            System.out.println("(7) Display the information of all tables in the database");
            System.out.println("Enter anything else to quit");
            System.out.print("Choose one: ");
            Scanner scanner = new Scanner(System.in);

            try {
                choice = scanner.nextInt();
            } catch (java.util.InputMismatchException e) {
                scanner.next();
                return;
            }

            switch (choice) {
                case 1: {
                    addMusician(c, null);
                    System.out.println("Entry added successfully");
                    break;
                }
                case 2: {
                    String SSN = ensureCorrectValuesAreEntered("Enter the musician's SSN: ", false);
                    //Check if the musician exists
                    if (!checkIfExists(c, "MUSICIAN", "SSN", SSN)) {
                        System.err.println("No musician with SSN: " + SSN + " exists in the database");
                        break;
                    }
                    removeRowFromTable(c, "MUSICIAN", "SSN", SSN);
                    break;
                }
                case 3: {
                    addAlbum(c, null);
                    System.out.println("Entry added successfully");
                    break;
                }
                case 4: {
                    System.out.print("Enter the albums's unique id number: ");
                    int UID = scanner.nextInt();
                    if (!checkIfExists(c, "ALBUM", "UID", String.valueOf(UID))) {
                        System.err.println("No album with UID: " + UID + " exists in the database");
                        break;
                    }
                    removeRowFromTable(c, "ALBUM", "UID", String.valueOf(UID));
                    break;
                }
                case 5: {
                    boolean exists;
                    String title, SSN, authorSSN;
                    scanner.useDelimiter("\n");
                    do {
                        System.out.print("Enter the song's title: ");
                        do {
                            title = scanner.next();
                        } while (title.equals(""));
                        SSN = ensureCorrectValuesAreEntered("Enter the author's SSN: ", false);
                        authorSSN = SSN;
                        exists = checkIfExists(c, "SONG", "TITLE", "AUTHOR", title, SSN);
                        //If the song already exists
                        //Inform the user
                        if (exists) {
                            System.err.println("This song already exists in the database");
                        }
                    } while (exists);

                    if (!checkIfExists(c, "MUSICIAN", "SSN", SSN)) {
                        addMusician(c, SSN);
                    }
                    insertTwoValuesIntoTable(c, "SONG", "TITLE", "AUTHOR", title, SSN);
                    insertThreeValuesIntoTable(c, "PERFORMED_BY", "SSN", "TITLE", "AUTHOR", SSN, title, authorSSN);
                    //Ask what instruments he played in this particular song
                    ArrayList<Instrument> instruments = collectInstruments("song");
                    addInstrumentToDatabaseForSong(c, title, authorSSN, instruments);
                    //Enter the SSN of other atrists that helped in making the song
                    do {
                        SSN = ensureCorrectValuesAreEntered("Enter the SSN of another musician who featured on this song (Enter '0' to stop adding musicians): ", true);
                        if (SSN.equals("0")) {
                            break;
                        }
                        if (!checkIfExists(c, "MUSICIAN", "SSN", SSN)) {
                            addMusician(c, SSN);
                        }
                        //Re-collect information if the same musician is added twice for the same song
                        exists = checkIfExists(c, "PERFORMED_BY", "SSN", "TITLE", "AUTHOR", SSN, title, authorSSN);
                        if (exists) {
                            System.err.println("You have already added this musician to the featured list of this song.");
                            continue;
                        }
                        insertThreeValuesIntoTable(c, "PERFORMED_BY", "SSN", "TITLE", "AUTHOR", SSN, title, authorSSN);
                        //Ask what instruments he played in this particular song
                        //instruments = collectInstruments("song");

                        //addInstrumentToDatabase(c, title, SSN, instruments);
                    } while (!SSN.equals("0") || exists);

                    //Should ask which album it belongs to
                    System.out.println("What album ID does this song belong to: ");
                    int albumID = scanner.nextInt();
                    boolean maxSongs = false;
                    try {
                        Statement stmt = c.createStatement();
                        String sql = "SELECT * FROM ALBUM WHERE AID = '" + String.valueOf(albumID) + "'";
                        ResultSet result = stmt.executeQuery(sql);
                        if (result.next() != false) {
                            //If album exists
                            //Insert into Contains table
                            int UID = result.getInt("UID");

                            //Check if the album has 15 songs
                            sql = "SELECT COUNT(*) FROM CONTAINS WHERE UID = '" + String.valueOf(UID) + "'";
                            ResultSet resultCount = stmt.executeQuery(sql);
                            resultCount.next();
                            int rowCount = resultCount.getInt(1);
                            //If it does
                            if (rowCount == 14) {
                                //Tell the person that the action cannot be completed because ofthe constraint
                                System.err.println("The maximum number of songs per album has been reached");
                                maxSongs = true;
                            }
                            resultCount.close();

                            insertThreeValuesIntoTable(c, "CONTAINS", "UID", "TITLE", "AUTHOR", String.valueOf(UID), title, authorSSN);
                        } else {
                            //if album doesn't exist
                            //Add album to database then
                            int UID = addAlbum(c, String.valueOf(albumID));
                            //Insert into Contains table
                            insertThreeValuesIntoTable(c, "CONTAINS", "UID", "TITLE", "AUTHOR", String.valueOf(UID), title, authorSSN);
                        }
                    } catch (Exception e) {
                        errorHandler(e);
                    }
                    if (!maxSongs) {
                        //Also, do not show the success message
                        System.out.println("Entry added successfully");
                    }
                    break;
                }
                case 6: {
                    System.out.print("Enter the song's title: ");
                    String title = scanner.next();
                    String SSN = ensureCorrectValuesAreEntered("Enter the author's SSN: ", false);
                    if (!checkIfExists(c, "SONG", "TITLE", "AUTHOR", title, SSN)) {
                        System.err.println("No song with the given parameters exist");
                        break;
                    }
                    removeRowFromTable(c, "SONG", "TITLE", "AUTHOR", title, SSN);
                    /*//Remove the song from the "performed_by" table as well
                    try {
                        Statement stmt = c.createStatement();
                        String sql = "SELECT * FROM PERFORMED_BY WHERE TITLE = '" + title + "'";
                        ResultSet result = stmt.executeQuery(sql);
                        if (!result.next())
                        {
                            removeRowFromTable(c, "PERFORMED_BY", "TITLE", result.getString("TITLE"));
                        }
                    } catch (Exception e) {
                        errorHandler(e);
                    }*/
                    break;
                }
                case 7:
                    System.out.println("ALBUM: ");
                    try {
                        Statement stmt = c.createStatement();
                        String sql = "SELECT * FROM ALBUM";
                        ResultSet result = stmt.executeQuery(sql);
                        while (result.next() != false) {
                            System.out.println("UID: " + result.getString("UID") + " TITLE: " + result.getString("TITLE") + " AID: " + result.getString("AID") + " COPYRIGHT DATE: " + result.getString("COPYDATE") + " FORMAT: " + result.getString("FORMAT"));
                        }
                        result.close();
                        stmt.close();
                    } catch (Exception e) {
                        errorHandler(e);
                    }
                    System.out.println();
                    listTwoColumnTableContents(c, "MUSICIAN", "SSN", "NAME");
                    System.out.println();
                    listTwoColumnTableContents(c, "SONG", "TITLE", "AUTHOR");
                    System.out.println();
                    listThreeColumnTableContents(c, "INSTRUMENT", "ID", "KEY", "NAME");
                    System.out.println();
                    listSingleColumnTableContents(c, "ADDRESS", "ADDRESS");
                    System.out.println();
                    listSingleColumnTableContents(c, "PHONE", "PHONENO");
                    System.out.println();
                    listTwoColumnTableContents(c, "PRODUCES", "UID", "SSN");
                    System.out.println();
                    listThreeColumnTableContents(c, "CONTAINS", "UID", "TITLE", "AUTHOR");
                    System.out.println();
                    listThreeColumnTableContents(c, "PERFORMED_BY", "SSN", "TITLE", "AUTHOR");
                    System.out.println();
                    listThreeColumnTableContents(c, "USED_IN", "ID", "TITLE", "AUTHOR");
                    System.out.println();
                    listTwoColumnTableContents(c, "PLAYS", "ID", "SSN");
                    System.out.println();
                    listTwoColumnTableContents(c, "LIVES_IN", "SSN", "ADDRESS");
                    System.out.println();
                    listTwoColumnTableContents(c, "HAS", "ADDRESS", "PHONENO");
                    break;
                default:
                    try {
                        c.close();
                    } catch (Exception e) {
                        errorHandler(e);
                    }
                    break;
            }
            System.out.println();
        } while (choice > 0 && choice < 8);
        try {
            c.close();
        } catch (Exception e) {
            errorHandler(e);
        }
    }
    //c.commit();
}
