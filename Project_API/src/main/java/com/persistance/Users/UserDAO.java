///////////////////////////////////////////////////////////////////////////////////////////////////////
package com.persistance.Users;
//  FILE : UserDAO.java
//  AUTHOR : Pranav Sehgal <PranavSehgalSJSU>
//  DESCRIPTION: Is the userDAO.java INTERFACE file, used to DECLARE functions
//               DEFINITION of these functions are in the UserFileDAO.java file with @override
//
///////////////////////////////////////////////////////////////////////////////////////////////////////
import com.model.User;

public interface UserDAO {
    /**
     * Used to create user and persist them in database
     * @param user Object with new user data
     * @return used userobject
     */
    User createUser(User user);

    /**
     * Searches the DB for user by username
     * @param username name of user to search for
     * @return User object if found, else null
     */
    User getUserByUsername(String username);

    /**
     * Searches the DB for user by email
     * @param email email of user to search for
     * @return True object if found, else False
     */
    Boolean emailIsInUse(String email);

    /**
     * Updates exising user based on updated user object
     * @param user userobject w/ data
     */
    void updateUser(User user);

    /**
     * Sets a default mode for user if not specified
     * @
     */
    String chooseMode(User user,String mode);
}