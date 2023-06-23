package com.james.projServer.Repositories;

import java.sql.PreparedStatement;
import java.sql.Statement;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Repository;

@Repository
public class SQLUserRepo {

    @Autowired
    JdbcTemplate template;

    private final String GET_HASHED_PW = """
            SELECT hash,user_id from users where username = ?;
                """;

    private final String GET_USER_ID_FROM_USERNAME = """
            SELECT user_id from users where username = ?;
                """;

    private final String GET_USERNAME_FROM_ID = """
            SELECT username from users where user_id = ?;
                """;

    private final String GET_USER_ID_FROM_GMAIL = """
            SELECT user_id from users where user_gmail = ?;
                """;

    private final String CREATE_USER = """
            insert into users (username, hash) values
            (?,?)
                """;

    private final String CREATE_GOOGLE_USER = """
            insert into users (username, user_gmail) values
            (?,?)
                """;

    public Boolean createUser(String user, String password) {

        final SqlRowSet rs = template.queryForRowSet(GET_USER_ID_FROM_USERNAME, user);
        if (rs.first()) {
            return false;
        }

        // Create GeneratedKeyHolder object
        GeneratedKeyHolder generatedKeyHolder = new GeneratedKeyHolder();

        // To insert data, you need to pre-compile the SQL and set up the data yourself.
        int rowsAffected = template.update(conn -> {

            // Pre-compiling SQL
            PreparedStatement preparedStatement = conn.prepareStatement(CREATE_USER, Statement.RETURN_GENERATED_KEYS);

            // Set parameters
            preparedStatement.setString(1, user);
            preparedStatement.setString(2, DigestUtils.md5Hex(password));

            return preparedStatement;

        }, generatedKeyHolder);

        // Get auto-incremented ID
        Integer id = generatedKeyHolder.getKey().intValue();

        System.out.println("Created user_id" + id);

        return true;
    }

    public Integer createGoogleUser(String user, String gmail) {


        // Create GeneratedKeyHolder object
        GeneratedKeyHolder generatedKeyHolder = new GeneratedKeyHolder();

        // To insert data, you need to pre-compile the SQL and set up the data yourself.
        int rowsAffected = template.update(conn -> {

            // Pre-compiling SQL
            PreparedStatement preparedStatement = conn.prepareStatement(CREATE_GOOGLE_USER, Statement.RETURN_GENERATED_KEYS);

            // Set parameters
            preparedStatement.setString(1, user);
            preparedStatement.setString(2, gmail);

            return preparedStatement;

        }, generatedKeyHolder);

        // Get auto-incremented ID
        Integer id = generatedKeyHolder.getKey().intValue();

        System.out.println("Created user_id" + id);

        return id;
    }

    public Integer verifyLogin(String user, String password) {

        String hashedPass;
        System.out.println(password);
        final SqlRowSet rs = template.queryForRowSet(GET_HASHED_PW, user);
        if (rs.first()) {
            System.out.println("hashed pass found!" + rs.getString(1));
            hashedPass = rs.getString(1);
            System.out.println(DigestUtils.md5Hex(password));
        } else
            return 0;

        if (DigestUtils.md5Hex(password).equals(hashedPass)) {
            return rs.getInt(2);
        } else
            return 0;

    }

    public String getUsername(String userId) {

        final SqlRowSet rs = template.queryForRowSet(GET_USERNAME_FROM_ID, Integer.parseInt(userId));
        if (rs.first()) {
            System.out.println("username found!" + rs.getString(1));
            return rs.getString(1);
        } else
            return null;

    }

    public Integer getUserIdFromGmail(String gmail){

        final SqlRowSet rs = template.queryForRowSet(GET_USER_ID_FROM_GMAIL, gmail);
        if (rs.first()) {
            return rs.getInt(1);
        }
        return 0;
    }
}
