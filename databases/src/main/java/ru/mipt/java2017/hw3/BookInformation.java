package ru.mipt.java2017.hw3;

import java.util.Set;

public class BookInformation {
  private Set<String> authorsNames;
  private String bookTitle;
  private Long isbn;
  private boolean isValid;

  BookInformation(Set<String> authorsNames, String bookTitle, Long isbn){
    this.authorsNames = authorsNames;
    this.bookTitle = bookTitle;
    this.isbn = isbn;
    isValid = true;
  }

  BookInformation(){
    isValid = false;
  }

  public Set<String> getAuthorsNames() {
    return authorsNames;
  }

  public String getBookTitle() {
    return bookTitle;
  }

  public Long getIsbn() {
    return isbn;
  }

  public boolean isValid() {
    return isValid;
  }

}
