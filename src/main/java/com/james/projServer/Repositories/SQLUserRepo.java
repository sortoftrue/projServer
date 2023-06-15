package com.james.projServer.Repositories;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import org.apache.commons.codec.cli.Digest;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Repository;

import com.james.projServer.Models.Post;
import com.james.projServer.Services.callGoogle;

@Repository
public class SQLUserRepo {

    @Autowired
    JdbcTemplate template;

    private final String GET_HASHED_PW = """
            SELECT hash from users where username = ?;
                """;

    private final String GET_USER_ID = """
            SELECT user_id from users where username = ?;
                """;

    private final String CREATE_USER= """
            insert into users (username, hash) values
            (?,?)
                """;

    public Boolean createUser(String user, String password){

        final SqlRowSet rs = template.queryForRowSet(GET_USER_ID, user);
        if(rs.first()){
            return false;
        }
        
        // Create GeneratedKeyHolder object
        GeneratedKeyHolder generatedKeyHolder = new GeneratedKeyHolder();

        // To insert data, you need to pre-compile the SQL and set up the data yourself.
        int rowsAffected = template.update(conn -> {

            // Pre-compiling SQL
            PreparedStatement preparedStatement = conn.prepareStatement(CREATE_USER, Statement.RETURN_GENERATED_KEYS);

            // Set parameters
            preparedStatement.setString(1,user);
            preparedStatement.setString(2, DigestUtils.md5Hex(password));

            return preparedStatement;

        }, generatedKeyHolder);

        // Get auto-incremented ID
        Integer id = generatedKeyHolder.getKey().intValue();

        System.out.println("Created user_id" + id);

        return true;
    }
            

    public Boolean verifyLogin(String user, String password){
       
        String hashedPass;

        final SqlRowSet rs = template.queryForRowSet(GET_HASHED_PW, user);
        if(rs.first()){
            System.out.println("hashed pass found!" + rs.getString(1));
            hashedPass = rs.getString(1);
            System.out.println(DigestUtils.md5Hex(password));
        } else return false;

        if(DigestUtils.md5Hex(password).equals(hashedPass)){
            return true;
        } else return false;

    }

}
