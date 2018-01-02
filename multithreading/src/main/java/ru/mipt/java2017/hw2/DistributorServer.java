package ru.mipt.java2017.hw2;

import java.util.concurrent.Semaphore;
import org.slf4j.Logger;

//amount of DistributorServers is equal to amount of cores
public class DistributorServer implements Runnable {

  private long number;
  private long start, end;
  private Semaphore semaphore;
  private long sumOfPrimary;
  private Logger logger;


  public DistributorServer(long number, long start, long end, Semaphore semaphore, Logger logger) {
    this.end = end;
    this.number = number;
    this.start = start;
    this.semaphore = semaphore;
    this.sumOfPrimary = 0;
    this.logger = logger;
  }

  //check if prime
  private boolean isPrimary(long number) {
    if (number == 1)
      return false;
    if (number == 2)
      return true;
    for (long i = 2; i < Math.sqrt(number) + 1; ++i) {
      if (number % i == 0)
        return false;
    }
    return true;
  }

  //find sum of prime numbers from start to end
  private long findSumOfPrimaryNumbers() {
    long tmpSumOfPrimaryNumbers = 0;
    for (long i = start; i <= end; ++i) {
      if (isPrimary(i)) {
        tmpSumOfPrimaryNumbers += i;
      }
    }
    return tmpSumOfPrimaryNumbers;
  }

  //sum getter
  public long getSumOfPrimary() {
    return sumOfPrimary;
  }

  @Override
  public void run() {
    synchronized (logger) {
      logger.info("start finding sum of primary numbers in [{}, {}] in thread {}", start, end,
          number);
    }
    sumOfPrimary += findSumOfPrimaryNumbers();

    synchronized (logger) {
      logger.info(
          "finish finding sum of primary numbers in [{}, {}] in thread {}, sum of prime numbers: {}",
          start, end, number, sumOfPrimary);
    }
    semaphore.release();
  }
}

