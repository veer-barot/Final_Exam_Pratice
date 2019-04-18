package models;

import java.math.BigDecimal;
import javax.json.Json;
import javax.json.JsonObject;


/**
 *
 * @author <Veer Barot>
 */
public class Author {
    
    //Input for the Author
    private int id;
    private String name;
    private String nationality;

    //Empty Contructor
    public Author() {
    }

    //Author Constructor taking ID, NAME and NATIONALITY
    public Author(int id, String name, String nationality) {
        this.id = id;
        this.name = name;
        this.nationality = nationality;
    }
    
    //Author Constructor taking JSON Object
    public Author(JsonObject json) {
        this.id = json.getInt("id",0);
        this.name = json.getString("name", "");
        this.nationality = json.getString("nationality", "");
    }
    
    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNationality() {
        return nationality;
    }

    public void setNationality(String nationality) {
        this.nationality = nationality;
    }
    
    //toJson() method producing JSON Object
    public JsonObject toJson() {
        return Json.createObjectBuilder()
                .add("id", id)
                .add("name", name)
                .add("nationality", nationality)
                .build();
    }
    
}
