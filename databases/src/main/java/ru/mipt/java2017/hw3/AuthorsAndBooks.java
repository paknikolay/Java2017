package ru.mipt.java2017.hw3;
import javax.persistence.*;
import org.hibernate.annotations.GenericGenerator;

@Entity
@Table(name = "Books_Authors")
public class AuthorsAndBooks {
  @Id
  @GeneratedValue(generator = "books_author_gen")
  @GenericGenerator(name = "books_author_gen", strategy = "increment")
  private int id;

  @ManyToOne
  @JoinColumn(name = "books_id")
  private Book book;

  @ManyToOne
  @JoinColumn(name = "authors_id")
  private Author author;

  @Column(name = "num")
  private int order;

  public AuthorsAndBooks(){}

  public AuthorsAndBooks(Book book, Author author, int order){
    this.book = book;
    this.author = author;
    this.order = order;
  }

  public int getId() {
    return id;
  }

  public Author getAuthor() {
    return author;
  }

  public Book getBook() {
    return book;
  }

  public int getOrder() {
    return order;
  }
}
