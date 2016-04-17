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

import java.util.Optional;

import info.gorzkowski.jinq.jpa.model.Customer;
import info.gorzkowski.jinq.jpa.model.Lineorder;
import info.gorzkowski.jinq.jpa.model.Sale;
import org.jinq.jpa.JPAJinqStream;
import org.jinq.jpa.JPAQueryLogger;
import org.jinq.jpa.JinqJPAStreamProvider;
import org.jinq.orm.stream.JinqStream;
import org.jinq.tuples.Pair;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
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
        SampleDbCreator.createDatabase(entityManagerFactory);
        em = entityManagerFactory.createEntityManager();

        streams = new JinqJPAStreamProvider(entityManagerFactory);

        streams.registerAssociationAttribute(Lineorder.class.getMethod("getItem"), "item", false);
        streams.registerAssociationAttribute(Lineorder.class.getMethod("getSale"), "sale", false);
        streams.registerAssociationAttribute(Sale.class.getMethod("getCustomer"), "customer", false);

        // Configure Jinq to output the queries it executes
        streams.setHint("queryLogger", (JPAQueryLogger) (query, positionParameters, namedParameters) -> System.out.println("  " + query));

    }

    @Test
    public void shouldListCustomers() {
        //given
        JPAJinqStream<Customer> customers = streams.streamAll(em, Customer.class);

        //when, then
        customers.forEach(System.out::println);
    }

    @Test
    public void shouldTestSelectAllList() {
        //given
        JPAJinqStream<Customer> customersStream = streams.streamAll(em, Customer.class);

        //when
        customersStream.where(c -> c.getName().equals("Alice"))
                .selectAllList(c -> c.getSales())
                .where(s -> JinqStream.from(s.getLineorders()).count() > 1)
                .forEach(System.out::println);
    }

    @Test
    public void testIsNull() {
        //given
        JPAJinqStream<Customer> customersStream = streams.streamAll(em, Customer.class);

        //when
        customersStream.where(c -> c.getName() != null)
                .forEach(System.out::println);
    }

    @Test
    public void shouldTestSelect() {
        //given
        JPAJinqStream<Customer> customersStream = streams.streamAll(em, Customer.class);

        //when
        customersStream.select(c -> c.getSalary())
                .forEach(System.out::println);
    }

    @Test
    public void shouldTestSelectPair() {
        //given
        JPAJinqStream<Customer> customersStream = streams.streamAll(em, Customer.class);

        //when
        customersStream.select((c, source) -> new Pair<String, Object>(c.getName(),
                source.stream(Customer.class).
                        where(c2 -> c2.getSalary() > c.getSalary()).count()))
                .forEach(System.out::println);
    }

    @Test
    public void shouldTestWhereInSource() {
        //given
        JPAJinqStream<Customer> customersStream = streams.streamAll(em, Customer.class);

        //when
        customersStream.where((val, source) -> source.stream(Customer.class).max(cc -> cc.getCustomerid()) == val.getCustomerid())
                .forEach(System.out::println);
    }

    @Test
    public void shouldTestDistinctClause() {

        streams.streamAll(em, Sale.class)
                .joinFetchList(s -> s.getLineorders())
                .where(s -> s.getCustomer().getName().equals("Alice"))
                .distinct()
                .forEach(System.out::println);
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

    @Test
    public void shouldFindCustomerWithGivenName() {
        //given
        JPAJinqStream<Customer> customers = streams.streamAll(em, Customer.class);

        //when, then
        customers
                .where(c -> c.getName().equals("Alice"))
                .forEach(System.out::println);

    }

    @Test
    public void aggregateExample() {
        //given
        JPAJinqStream<Customer> customers = streams.streamAll(em, Customer.class);

        //when,then
        Optional.of(customers.aggregate(
                stream -> stream.max(c -> c.getSalary()),
                stream -> stream.avg(c -> c.getSalary())
        )).ifPresent(System.out::println);
    }

    @Test
    public void filter_vs_where() {
        //given
        JPAJinqStream<Customer> customers = streams.streamAll(em, Customer.class);
        int salary = 300;

        //when, then
        long count = customers.filter(c -> c.getSalary() > salary).count();
        System.out.println("customers.filter(c -> c.getSalary() > 300).count() ---> " + count);

        count = customers.where(c -> c.getSalary() > salary).count();
        System.out.println("customers.where(c -> c.getSalary() > 300).count() ---> " + count);
    }

    @Test
    public void where_with_not_null() {
        //given
        JPAJinqStream<Customer> customers = streams.streamAll(em, Customer.class);

        //when,then
        customers.where(c -> c.getName() != null).forEach(System.out::println);
    }

    @Test
    public void concatenation() {
        //given
        JPAJinqStream<Customer> customers = streams.streamAll(em, Customer.class);

        //when, then
        customers.select(c -> "Name -> " + c.getName()).forEach(System.out::println);
//        customers.select(c -> c.getName() + null).forEach(System.out::println);
//        customers.select(c -> c.getName() + 1).forEach(System.out::println);
    }

    @Test
    @Ignore
    public void testWithMethod() {
        //given
        JPAJinqStream<Customer> customers = streams.streamAll(em, Customer.class);

        //when, then
        customers
                .where(c -> isAlice(c))
                .forEach(System.out::println);

    }

    private boolean isAlice(Customer c) {
        return c.getName().equals("Alice");
    }

    @Test
    public void sortAndLimit() {
        //given
        JPAJinqStream<Customer> customers = streams.streamAll(em, Customer.class);

        //when, then
        customers.sortedBy(c -> c.getName() ).limit(2).forEach(System.out::println);
    }

    @Test
    public void association() {
        //given
        JPAJinqStream<Sale> sales = streams.streamAll(em, Sale.class);

        //when, then
        sales.where( s -> s.getCustomer().getName().equals("Alice")).forEach(System.out::println);
    }

    @After
    public void tearDown() {
        em.close();
    }
}
