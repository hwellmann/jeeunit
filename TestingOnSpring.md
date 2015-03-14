## Spring vs. Embedded EJB Container ##

The `jeeunit` library provides some glue code for testing Java EE components in an embedded EJB container. Up to jeeunit 0.5.0, there was no easy way to run multiple test classes directly from the IDE, which is no problem with the Spring Test Context. With jeeunit 0.6.0, this restriction is gone, so this page mainly serves as an example how to write portable entity and service components that work both in Java EE 6 and Spring 3.

Using the Spring Test Context, you can easily run single integration tests from your IDE, and the startup overhead is minimal. The drawback is that Spring's runtime behaviour is obviously different from Java EE (in particular, transactional Spring beans are not pooled and thread-safe like EJBs), and you will probably to have to add a few more annotations to your EJBs to make Spring happy. However, these extra annotations are all EJB-compliant. You also need to add some Spring annotations to your test classes and provide a Spring application context. Apart from that, your tests will not depend on any Spring APIs.

For this approach, you do not require the `jeeunit` library. The `jeeunit` source repository contains some [example code](http://code.google.com/p/jeeunit/source/browse/#hg/jeeunit-example/jeeunit-example-test-spring) with Spring tests which you may use as a starting point.

## Writing a Test ##

```
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/META-INF/test-context.xml")
public class AuthorTest {

    @Inject
    private LibraryService libraryService;

    @Before
    public void setUp() {
        libraryService.fillLibrary();
    }

    @Test
    public void byAuthor() {
        List<Book> books = libraryService.findBooksByAuthor("Mann");
        assertEquals(1, books.size());

        Book book = books.get(0);
        assertEquals("Buddenbrooks", book.getTitle());
    }
}
```

This test class is almost the same as for the embedded case. However, the `@RunWith` annotation is required to tell JUnit to run the test on Spring, and the `@ContextConfiguration` annotation tells Spring where to find its application context. The argument is optional and defaults to `AuthorTest-context.xml` in this case.

## Adapting your EJBs ##

```
@Stateless
public class LibraryService implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @PersistenceContext
    private EntityManager em;
    
    @TransactionAttribute
    public List<Book> findBooksByAuthor(String lastName)
    {
        String jpql = "select b from Book b where b.author.lastName = :lastName";
        TypedQuery<Book> query = em.createQuery(jpql, Book.class);
        query.setParameter("lastName", lastName);
        List<Book> books = query.getResultList();
        return books;       
    }
}    
```

In the no-interface view of a stateless session bean, all public methods are by default transactional business methods with a `REQUIRED` transaction. Spring does not honor the `@Stateless` annotation with these semantics, but it does understand `@javax.ejb.TransactionAttribute`, similar to its native `@Transactional` annotation.

So by adding a redundant `@TransactionAttribute` annotation to all unannotated public methods, you can make transactions work automatically under Spring.

## The Application Context ##

This may be a matter of personal taste, but I never understood what's so cool about writing XML to create a couple of beans and wire them up. Fortunately, Spring 3.0 lets us use plain old Java to do the job. Well, almost - there's still a couple of configuration settings supported in XML syntax only.

So here is a minimal Spring XML application context that delegates most of the work to a Spring Java configuration class called `SpringTestConfig`:

```
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xmlns:context="http://www.springframework.org/schema/context" xmlns:tx="http://www.springframework.org/schema/tx"
  xsi:schemaLocation="http://www.springframework.org/schema/beans
         http://www.springframework.org/schema/beans/spring-beans-3.0.xsd
         http://www.springframework.org/schema/tx
         http://www.springframework.org/schema/tx/spring-tx-3.0.xsd
         http://www.springframework.org/schema/context
         http://www.springframework.org/schema/context/spring-context-3.0.xsd">

  <context:annotation-config/>
  <bean class="com.googlecode.jeeunit.example.test.spring.SpringTestConfig"/>
  <tx:annotation-driven proxy-target-class="true"/>

</beans>
```

And here is the `SpringTestConfig` class where we create the beans under test and some Spring beans required to mock a JPA plus JTA container.

```
@Configuration
public class SpringTestConfig {

    @Bean
    public LibraryService libraryService() {
        return new LibraryService();
    }

    @Bean
    public DataSource dataSource() {
        SimpleDriverDataSource bean = new SimpleDriverDataSource(new org.apache.derby.jdbc.ClientDriver(),
                "jdbc:derby://localhost/sun-appserv-samples;create=true");

        return bean;
    }

    @Bean
    public PlatformTransactionManager transactionManager() {
      JpaTransactionManager tm = new JpaTransactionManager(entityManagerFactory().getObject());
      return tm;
    }
    

    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
        LocalContainerEntityManagerFactoryBean bean = new LocalContainerEntityManagerFactoryBean();
        bean.setDataSource(dataSource());
        bean.setPersistenceProvider(new HibernatePersistence());
        bean.setPersistenceXmlLocation("classpath:META-INF/test-persistence.xml");
        return bean;
    }
}

```

Note that we need to override the default `persistence.xml` by a dedicated `test-persistence.xml` where we set the transaction type to `RESOURCE_LOCAL`.

See the [example code](http://code.google.com/p/jeeunit/source/browse/#hg/jeeunit-example/jeeunit-example-test-spring) for more details.

## The Dependencies ##

Take a look at the [example POM](http://code.google.com/p/jeeunit/source/browse/jeeunit-example/jeeunit-example-test-spring/pom.xml) to see the dependencies you need to have on your classpath for running the tests.

Other than JUnit and Spring, you need a JPA provider (Hibernate in this case), CGLIB for the proxies created by Spring and some logging support.

Spring still uses Apache Commons Logging, which is a [bad idea](http://articles.qos.ch/classloader.html), while Hibernate has migrated to [SLF4J](http://www.slf4j.org), which is what I prefer myself. For this reason, I added `jcl-over-slf4j` and [Logback](http://logback.qos.ch) to the example project, which makes Spring think it is using Commons Logging while in fact is using an adapter to the SLF4J API with the Logback backend.

Using Eclipse with the [m2eclipse](http://m2eclipse.sonatype.org) Maven integration, you can simply import the `jeeunit` source repository as a Maven project, select `AuthorTest` from the project explorer and then run it as a JUnit test.