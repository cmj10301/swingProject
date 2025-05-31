package packages.Classes;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class User {
    private int userId;           // user_id (int, PK)
    private String username;      // username (varchar)
    private String password;      // password (varchar)
    private String email;         // email (varchar)
    private String name;          // name (varchar)
    private int birthYear;        // birth_year (int)
    private char gender;          // gender (char)
}
