package ru.mipt.java2017.hw3;

import javax.persistence.*;
import org.hibernate.annotations.GenericGenerator;

@Entity
@Table(name = "Books")
public class Book implements Comparable {

  @Id
  @GeneratedValue(generator = "book_gen")
  @GenericGenerator(name = "book_gen", strategy = "increment")
  private int id;

  @Column(name = "ISBN")
  private long isbn;

  @Column(name = "Title")
  private String title;

  @Column(name = "Cover")
  private String cover;

  public Book() {
  }

  public Book(long isbn, String title, String cover) {
    this.isbn = isbn;
    this.title = title;
    this.cover = cover;
  }

  public Book setIsbn(long isbn) {
    this.isbn = isbn;
    return this;
  }

  public Book setTitle(String title) {
    this.title = title;
    return this;
  }

  public Book setCover(String cover) {
    this.cover = cover;
    return this;
  }

  public int getId() {
    return id;
  }

  public long getIsbn() {
    return isbn;
  }

  public String getTitle() {
    return title;
  }

  public String getCover() {
    return cover;
  }

  @Override
  public int compareTo(Object o) {
    if (this.isbn > ((Book) o).isbn) {
      return 1;
    } else {
      return -1; //isbn is unique
    }
  }
}
