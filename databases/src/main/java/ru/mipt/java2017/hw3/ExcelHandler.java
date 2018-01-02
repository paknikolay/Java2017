package ru.mipt.java2017.hw3;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExcelHandler {

  private int titleRowNum;
  private int authorRowNum;
  private int isbnRowNum;
  private int currentRowIndex;
  private int lastRowIndex;
  private String filePathIn;
  private String filePathOut;
  private static Logger logger;

  public ExcelHandler(String filePathIn, String filePathOut) {
    logger = LoggerFactory.getLogger("excelHandler");

    this.filePathIn = filePathIn;
    currentRowIndex = 0;
    this.filePathOut = filePathOut;
    //determines which column refers to author, title and isbn
    try (FileInputStream fis = new FileInputStream(filePathIn)) {
      try (Workbook workbook = new XSSFWorkbook(fis)) {
        Sheet sheet = workbook.getSheetAt(0);
        lastRowIndex = sheet.getLastRowNum();
        Row currentRow = sheet.getRow(currentRowIndex);
        if (currentRow.getCell(0).getStringCellValue().equals("Title")) {
          titleRowNum = 0;
          if (currentRow.getCell(1).getStringCellValue().equals("Authors")) {
            authorRowNum = 1;
            isbnRowNum = 2;
          } else {
            authorRowNum = 2;
            isbnRowNum = 1;
          }
        } else {
          if (currentRow.getCell(0).getStringCellValue().equals("Authors")) {
            authorRowNum = 0;
            if (currentRow.getCell(1).getStringCellValue().equals("Title")) {
              titleRowNum = 1;
              isbnRowNum = 2;
            } else {
              titleRowNum = 2;
              isbnRowNum = 1;
            }
          } else {
            isbnRowNum = 0;
            if (currentRow.getCell(1).getStringCellValue().equals("Authors")) {
              authorRowNum = 1;
              titleRowNum = 2;
            } else {
              authorRowNum = 2;
              titleRowNum = 1;
            }
          }
        }
      }
    } catch (FileNotFoundException e) {
      logger.error("File % wasn't found", filePathIn);
    } catch (IOException e) {
      e.printStackTrace();
    }

    ++currentRowIndex;
  }

  //returns false if no rows available, true otherwise in BookInformation
  public BookInformation getNextRow() {
    if (currentRowIndex > lastRowIndex) {
      return new BookInformation();
    }
    String bookTitle;
    Set<String> authorsNames;
    Long isbn;
    try (FileInputStream fis = new FileInputStream(filePathIn)) {
      try (Workbook workbook = new XSSFWorkbook(fis)) {
        Sheet sheet = workbook.getSheetAt(0);
        Row currentRow = sheet.getRow(currentRowIndex);
        bookTitle = currentRow.getCell(titleRowNum).getStringCellValue();
        authorsNames = getAuthors(currentRow.getCell(authorRowNum).getStringCellValue());
        isbn = getIsbn(currentRow.getCell(isbnRowNum).getStringCellValue());
      }
    } catch (FileNotFoundException e)
    {
      logger.error("File % wasn't found", filePathIn);
      e.printStackTrace();
      return new BookInformation();
    } catch (IOException e) {
      e.printStackTrace();
      return new BookInformation();
    }

    ++currentRowIndex;
    return new BookInformation(authorsNames, bookTitle, isbn);
  }

  //writes database to filePathOut
  public void writeDatabase(List<Book> books, List<Author> authors,
      List<AuthorsAndBooks> authorsAndBooks) {

    logger.debug("writing database");

    try (Workbook workbook = new XSSFWorkbook()) {
      logger.debug("writing books");
      writeDatabaseBook(books, workbook);
      logger.debug("writing autors");
      writeDatabaseAuthors(authors, workbook);
      logger.debug("writing authors and books");
      writeDatabaseAuthorAndBook(authorsAndBooks, workbook);
      try (FileOutputStream fos = new FileOutputStream(filePathOut)) {
        workbook.write(fos);
      } catch (FileNotFoundException e) {
        logger.error("File % wasn't found", filePathOut);
        e.printStackTrace();
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private Set<String> getAuthors(String stringWithAuthors) {
    Set<String> authors = new HashSet<>();
    StringBuffer buffer = new StringBuffer();
    for (int i = 0; i < stringWithAuthors.length(); ++i) {

      if (stringWithAuthors.charAt(i) == ',') {
        authors.add(buffer.toString());
        //removing spaces in end
        while (buffer.charAt(buffer.length() - 1) == ' ') {
          buffer.delete(buffer.length() - 1, buffer.length() - 1);
        }

        buffer.setLength(0);
        //skipping spaces in beginning
        while (stringWithAuthors.charAt(i + 1) == ' ') {
          ++i;
        }

      } else {
        buffer.append(stringWithAuthors.charAt(i));
      }
    }
    authors.add(buffer.toString());
    return authors;
  }

  private Long getIsbn(String stringWithIsbn) {
    int i = 0;
    while (i < stringWithIsbn.length() && stringWithIsbn.charAt(i) != ':') {
      ++i;
    }

    while (!(stringWithIsbn.charAt(i) >= '0' && stringWithIsbn.charAt(i) <= '9'))
      ++i;



    Long isbn = new Long(0);

    while (i < stringWithIsbn.length() && (stringWithIsbn.charAt(i) >= '0'
        && stringWithIsbn.charAt(i) <= '9')) {
      isbn = isbn * 10 + stringWithIsbn.charAt(i) - '0';
      ++i;
    }
    return isbn;
  }

  private void writeDatabaseBook(List<Book> books, Workbook workbook) {
    Sheet sheetBook = workbook.createSheet("Books");
    int bookIndex = 0;
    sheetBook.createRow(bookIndex).createCell(0).setCellValue("ID");
    sheetBook.getRow(bookIndex).createCell(1).setCellValue("ISBN");
    sheetBook.getRow(bookIndex).createCell(2).setCellValue("Title");
    sheetBook.getRow(bookIndex).createCell(3).setCellValue("Cover");

    for (Book book : books) {
      ++bookIndex;
      logger.debug("book % % % %", book.getId(), book.getIsbn(), book.getTitle(), book.getCover());
      sheetBook.createRow(bookIndex).createCell(0).setCellValue(book.getId());
      sheetBook.getRow(bookIndex).createCell(1).setCellValue(book.getIsbn());
      sheetBook.getRow(bookIndex).createCell(2).setCellValue(book.getTitle());
      sheetBook.getRow(bookIndex).createCell(3).setCellValue(book.getCover());
    }
  }

  private void writeDatabaseAuthors(List<Author> authors, Workbook workbook) {
    Sheet sheetBook = workbook.createSheet("Authors");
    int bookIndex = 0;
    sheetBook.createRow(bookIndex).createCell(0).setCellValue("ID");
    sheetBook.getRow(bookIndex).createCell(1).setCellValue("Name");

    for (Author author : authors) {
      ++bookIndex;
      logger.debug("author % %", author.getId(), author.getName());
      sheetBook.createRow(bookIndex).createCell(0).setCellValue(author.getId());
      sheetBook.getRow(bookIndex).createCell(1).setCellValue(author.getName());
    }
  }

  private void writeDatabaseAuthorAndBook(List<AuthorsAndBooks> authorsAndBooks,
                                          Workbook workbook) {
    Sheet sheetBook = workbook.createSheet("Books_Authors");
    int bookIndex = 0;
    sheetBook.createRow(bookIndex).createCell(0).setCellValue("ID");
    sheetBook.getRow(bookIndex).createCell(1).setCellValue("books_id");
    sheetBook.getRow(bookIndex).createCell(2).setCellValue("authors_id");
    sheetBook.getRow(bookIndex).createCell(3).setCellValue("order");

    for (AuthorsAndBooks authorsAndBook : authorsAndBooks) {
      ++bookIndex;
      logger.debug("books and author % % % %", authorsAndBook.getId(), authorsAndBook.getBook(),
              authorsAndBook.getAuthor(), authorsAndBook.getOrder());
      sheetBook.createRow(bookIndex).createCell(0).setCellValue(authorsAndBook.getId());
      sheetBook.getRow(bookIndex).createCell(1).setCellValue(authorsAndBook.getBook().getId());
      sheetBook.getRow(bookIndex).createCell(2).setCellValue(authorsAndBook.getAuthor().getId());
      sheetBook.getRow(bookIndex).createCell(3).setCellValue(authorsAndBook.getOrder());
    }
  }

}
