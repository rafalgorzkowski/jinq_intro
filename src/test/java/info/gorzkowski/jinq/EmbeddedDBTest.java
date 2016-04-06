/*
 * This document set is the property of GTECH Corporation, West Greenwich,
 * Rhode Island, and contains confidential and trade secret information.
 * It cannot be transferred from the custody or control of GTECH except as
 * authorized in writing by an officer of GTECH. Neither this item nor
 * the information it contains can be used, transferred, reproduced, published,
 * or disclosed, in whole or in part, directly or indirectly, except as
 * expressly authorized by an officer of GTECH, pursuant to written agreement.
 *
 * Copyright 2016 GTECH Corporation. All Rights Reserved.
 */

package info.gorzkowski.jinq;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import info.gorzkowksi.embedded.model.User;
import org.assertj.core.api.Assertions;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;

/**
 * //TODO javadocs
 */
public class EmbeddedDBTest {

    private EmbeddedDatabase db;

    @Before
    public void setUp() {
        //db = new EmbeddedDatabaseBuilder().addDefaultScripts().build();
        db = new EmbeddedDatabaseBuilder()
                .setType(EmbeddedDatabaseType.H2)
                .addScript("db/sql/create-db.sql")
                .addScript("db/sql/insert-data.sql")
                .build();
    }

    @Test
    public void shouldFindUserWithValidEmail() {
        //given
        NamedParameterJdbcTemplate template = new NamedParameterJdbcTemplate(db);
        Map<String, String> params = new HashMap<String, String>();
        params.put("name", "rgorzkow");

        //when
        User user = template.queryForObject("SELECT * FROM USERS WHERE name=:name", params, new UserMapper());

        //then
        Assertions.assertThat(user.getEmail()).isEqualTo("rafalgorzkowski@gmail.com");
    }

    @After
    public void tearDown() {
        db.shutdown();
    }

    private static final class UserMapper implements RowMapper<User> {

        public User mapRow(ResultSet rs, int rowNum) throws SQLException {
            User user = new User();
            user.setId(rs.getInt("id"));
            user.setName(rs.getString("name"));
            user.setEmail(rs.getString("email"));
            return user;
        }
    }

}
