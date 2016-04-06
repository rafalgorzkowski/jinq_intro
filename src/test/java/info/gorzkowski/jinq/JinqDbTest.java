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

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import info.gorzkowski.jinq.jpa.model.Customer;
import info.gorzkowski.jinq.jpa.model.Lineorder;
import org.jinq.jpa.JPAJinqStream;
import org.jinq.jpa.JPAQueryLogger;
import org.jinq.jpa.JinqJPAStreamProvider;
import org.jinq.orm.stream.JinqStream;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;

public class JinqDbTest {

    private EmbeddedDatabase db;

    private static EntityManagerFactory entityManagerFactory;
    private static JinqJPAStreamProvider streams;

    private EntityManager em;


    @Before
    public void setUp() throws NoSuchMethodException {
        entityManagerFactory = Persistence.createEntityManagerFactory("JPATest");
        streams = new JinqJPAStreamProvider(entityManagerFactory);
        SampleDbCreator.createDatabase(entityManagerFactory);
        streams = new JinqJPAStreamProvider(entityManagerFactory);

        streams.registerAssociationAttribute(Lineorder.class.getMethod("getItem"), "item", false);
        streams.registerAssociationAttribute(Lineorder.class.getMethod("getSale"), "sale", false);

        // Configure Jinq to output the queries it executes
        streams.setHint("queryLogger", (JPAQueryLogger) (query, positionParameters, namedParameters) -> System.out.println("  " + query));

        em = entityManagerFactory.createEntityManager();
    }

    @Test
    public void shouldListCustomers() {
        //given
        JPAJinqStream<Customer> customers = streams.streamAll(em, Customer.class);

        //when, then
        customers.forEach(System.out::println);
    }

    @Test
    public void shouldListCustomersWithSales() {
        //given
        JPAJinqStream<Customer> customers = streams.streamAll(em, Customer.class);

        //when, then
        customers
                .join(c -> JinqStream.from(c.getSales()))
                .forEach(System.out::println);
    }

    @After
    public void tearDown() {
        em.close();
    }
}
