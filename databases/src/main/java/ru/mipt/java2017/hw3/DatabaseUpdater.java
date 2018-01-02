package ru.mipt.java2017.hw3;


import java.util.List;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DatabaseUpdater {
  private static Logger logger;
  public static void main(String args[]){
    logger = LoggerFactory.getLogger("databaseUpdater");

    if(args.length != 3){
      logger.error("Wrong number of parameters: expected 3 found %", args.length);
      return;
    }
    ExcelHandler excelHandler = new ExcelHandler(args[1], args[2]);

    Configuration configuration = getConfiguration(args[0]);
    try (SessionFactory sessionFactory = configuration.buildSessionFactory()) {
      try (Session session = sessionFactory.openSession()) {
        session.getTransaction().begin();
        BookInformation bookInformation = excelHandler.getNextRow();

        while (bookInformation.isValid()) {
          Book book = new Book(bookInformation.getIsbn(), bookInformation.getBookTitle(), "");
          book = checkIfExist(session, getList(session, book), book);
          book.setTitle(bookInformation.getBookTitle());

          int order = 0;
          for (String name : bookInformation.getAuthorsNames()) {
            ++order;
            Author author = new Author(name);
            author = checkIfExist(session, getList(session, author), author);

            AuthorsAndBooks authorsAndBooks = new AuthorsAndBooks(book, author, order);
            authorsAndBooks = checkIfExist(session, getList(session, book, author),
                authorsAndBooks);

          }
          bookInformation = excelHandler.getNextRow();
        }
        session.getTransaction().commit();
      }

      try (Session session = sessionFactory.openSession()) {
        List<Book> bookList = getList(session, new Book(), true);
        List<Author> authorList = getList(session, new Author(), true);
        List<AuthorsAndBooks> authorAndBookList = getList(session, null, null, true);

        excelHandler.writeDatabase(bookList, authorList, authorAndBookList);
      }
    }
  }


  private static Configuration getConfiguration(String url) {
    Configuration configuration = new Configuration();

    configuration = configuration
        .setProperty("hibernate.dialect", "org.hibernate.dialect.SQLiteDialect");

    configuration = configuration
        .setProperty("hibernate.dialect", "org.hibernate.dialect.MySQLDialect");
    configuration = configuration
        .setProperty("hibernate.dialect", "org.hibernate.dialect.PostgreSQL82Dialect");

    configuration = configuration
        .setProperty("hibernate.connection.url", url);
    configuration = configuration.addAnnotatedClass(Author.class);
    configuration = configuration.addAnnotatedClass(Book.class);
    configuration = configuration.addAnnotatedClass(AuthorsAndBooks.class);
    return configuration;
  }


  private static List<Book> getList(Session session, Book book) {
    return getList(session, book, null, false, Book.class);
  }

  private static List<Author> getList(Session session, Author author) {
    return getList(session, null, author, false, Author.class);
  }

  private static List<AuthorsAndBooks> getList(Session session, Book book, Author author) {
    return getList(session, book, author, false, AuthorsAndBooks.class);
  }

  private static List<Book> getList(Session session, Book book, boolean getAll) {
    return getList(session, book, null, getAll, Book.class);
  }

  private static List<Author> getList(Session session, Author author, boolean getAll) {
    return getList(session, null, author, getAll, Author.class);
  }

  private static List<AuthorsAndBooks> getList(Session session, Book book, Author author,
      boolean getAll) {
    return getList(session, book, author, getAll, AuthorsAndBooks.class);
  }

  private static <T> List<T> getList(Session session, Book book, Author author, boolean getAll,
      Class cl) {
    CriteriaBuilder builder = session.getCriteriaBuilder();

    CriteriaQuery<T> query = builder.createQuery(cl);
    Root<T> root = query.from(cl);
    query.select(root);

    if (!getAll) {
      if (cl.equals(Book.class)) {
        query.where(builder.equal(root.get("isbn"), book.getIsbn()));
      } else {
        if (cl.equals(Author.class)) {
          query.where(builder.equal(root.get("name"), author.getName()));
        } else {
          query.where(builder.equal(root.get("book"), book),
              builder.equal(root.get("author"), author));
        }
      }
    }
    return session.createQuery(query).list();
  }


  private static <T> T checkIfExist(Session session, List<T> list, T t) {
    if (list.size() == 1) {//exists
      t = list.get(0);
    } else {
      session.save(t);
    }
    return t;
  }
}
