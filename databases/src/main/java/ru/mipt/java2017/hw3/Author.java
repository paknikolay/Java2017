package ru.mipt.java2017.hw3;

import javax.persistence.*;
import org.hibernate.annotations.GenericGenerator;

@Entity
@Table(name = "Authors")
public class Author {

  @Id
  @GeneratedValue(generator = "author_gen")
  @GenericGenerator(name = "author_gen", strategy = "increment")
  private int id;

  @Column(name = "name")
  private String name;

  public Author(){}

  public Author(String name){
    this.name = name;
  }

  public int getId(){
    return id;
  }

  public String getName() {
    return name;
  }
}
