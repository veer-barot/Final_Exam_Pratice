package controllers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.context.ApplicationScoped;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import models.Author;
import models.Book;
import utilities.DBUtils;

/**
 * The Logic Controller for the Books
 *
 * @author <Veer Barot>
 */
@ApplicationScoped
public class BookController {

    // Used to tie Authors to Books
    AuthorController authors = new AuthorController();
    
    //Connetion to the Book List
    List<Book> books = new ArrayList<>();

    /**
     * A Constructor to build the basic controller and load data
     */
    public BookController() {
         // Refresh the List of Books from the Database
        refreshFromDB();
    }

    /**
     * A method for refreshing the list of Authors from the Database
     */
    private void refreshFromDB() {
        // refreshFromDB() Method to Refresh the List of Books from the Database
        try {
            Connection conn = DBUtils.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM books");
            books.clear();
            while (rs.next()) {
                Book b = new Book(
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getString("synopsis"),
                        authors.getById(rs.getInt("author_id")));
                books.add(b);
            }
        } catch (SQLException ex) {
            Logger.getLogger(BookController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Persists a Single Book (new or existing) to the Database
     *
     * @param b the Book object
     * @return was the database change successful
     */
    private boolean persistToDB(Book b) {
        /*
         *  - Determine if the change is an INSERT or an UPDATE by checking the ID
         *  - Build the appropriate SQL statement and execute it
         *  - If it was an INSERT, make sure to retrieve the generated key to update the List
         *  - If all the above are successful, return true, otherwise, return false.
         */
        
        // persitToDB() Method to Update or Insert records into the Database
        int results = 0;
        try {
            String sql = "";
            Connection conn = DBUtils.getConnection();
            if (b.getId() <= 0) {
                sql = "INSERT INTO books (title, synopsis, author_id) VALUES (?, ?, ?)";
            } else {
                sql = "UPDATE books SET title = ?, synopsis = ?, author_id = ? WHERE id = ?";
            }
            PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            pstmt.setString(1, b.getTitle());
            pstmt.setString(2, b.getSynopsis());
            pstmt.setInt(3, b.getAuthor().getId());
            if (b.getId() > 0) {
                pstmt.setInt(4, b.getId());
            }
            results = pstmt.executeUpdate();
            if (b.getId() <= 0) {
                ResultSet rs = pstmt.getGeneratedKeys();
                rs.next();
                int id = rs.getInt(1);
                b.setId(id);
            }
            conn.close();
        } catch (SQLException ex) {
            Logger.getLogger(AuthorController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return results > 0;
    }

    /**
     * Removes a Single Book from the Database
     *
     * @param id the ID of the Book
     * @return was the database change successful
     */
    private boolean removeFromDB(int id) {
        // removeFromDB(id) Method to Remove the identified Book from the Database
        try {
            String sql ="";
            Connection conn = DBUtils.getConnection();
            sql = "DELETE FROM books WHERE id = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
            conn.close();
            return true; 
        } catch (SQLException ex) {
            Logger.getLogger(AuthorController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    /**
     * Retrieves a JSON representation of all Books
     *
     * @return the JSON array of all Books
     */
    public JsonArray getAll() {
        // getAll() Method to Create a JSON Array that contains all of the books
        JsonArrayBuilder arr = Json.createArrayBuilder();
        for (Book b : books) {
            arr.add(b.toJson());
        }
        return arr.build();
    }

    /**
     * Retrieves a single Book by its ID
     *
     * @param id the Book's ID
     * @return the Book object
     */
    public Book getById(int id) {
        // getById(id) Method to Retrieve a Book object from the list based on the ID
        for (Book b : books) {
            if (b.getId() == id)
                return b;
        }
        return null;
    }

    /**
     * Adds a new Book based on a JSON object
     *
     * @param json the JSON object of the Book
     * @return the JSON object of the Book with its added ID
     */
    public JsonObject add(JsonObject json) {
        // add(json) Method to Add the Book to the List of Books AND the Database
        Book b = new Book(json);
        persistToDB(b);
        books.add(b);
        return b.toJson();
    }

    /**
     * Edits an existing Book by its ID based on a JSON object
     *
     * @param id the ID of the existing Book
     * @param json the JSON object of the changed Book
     * @return the JSON object of the stored Book after the change
     */
    public JsonObject edit(int id, JsonObject json) {
        // edit(id) Method to Change the existing Author details from the database to different Book
        Book b = getById(id);
        if (b != null) {
            b.setTitle(json.getString("title"));
            b.setSynopsis(json.getString("synopsis"));
            b.setAuthor(new Author(json.getJsonObject("author")));
            persistToDB(b);
            return b.toJson();
        } else {
            return null;
        }
    }

    /**
     * Removes a Book from the List and the Database based on its ID
     *
     * @param id the ID of the existing Book
     * @return whether or not the removal was successful
     */
    public boolean delete(int id) {
        // delte(id) Method to Remove the Book from the database and the list, and report on success
        Book b = getById(id);
        books.remove(b);
        return removeFromDB(id);
    }

}
