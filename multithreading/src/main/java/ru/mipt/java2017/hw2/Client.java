package ru.mipt.java2017.hw2;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import java.util.ArrayList;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Client {

  private static final Logger logger = LoggerFactory.getLogger("Client");
  private final ManagedChannel channel;
  private final SenderGrpc.SenderBlockingStub blockingStub;

  public Client(String host, int port) {
    this(ManagedChannelBuilder.forAddress(host, port)
        .usePlaintext(true)
        .build());
  }

  Client(ManagedChannel channel) {
    this.channel = channel;
    blockingStub = SenderGrpc.newBlockingStub(channel);
  }

  public void shutdown() throws InterruptedException {
    channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
  }


  //return sum of primary numbers or -1 if can not calculate
  public long calculateSumOfPrimaryNumbers(int number, long start, long end) {
    String name = Integer.toString(number);
    logger.info("Connecting to server, thread number {}", name);
    Request request = Request.newBuilder().setNumber(number).setStart(start).setEnd(end).build();

    Reply response = null;
    try {
      response = blockingStub.send(request);
      return response.getSumOfPrimaryNumbers();
    } catch (StatusRuntimeException e) {
      logger.error("RPC failed, thread number {}", number);
      return -1;
    }
  }

  public static void main(String[] args) {
    //getting parameters
    long start = Long.valueOf(args[0]);
    long end = Long.valueOf(args[1]);
    int amount = (args.length - 2) / 2;

    if (args.length > 3 && args.length % 2 == 0) {//there is at least 1 server
      int previousAmount = amount;
      if (end - start < 0) {
        logger.error("Wrong parameters: end < start");
        return;
      }

      logger.info("Need to count sum from {} to {}", start, end);

      ArrayList<Integer> availableServers = new ArrayList<>(); // 0 - free; 1 - busy; 2 - is not available
      ArrayList<Pair<String, Integer>> servers = new ArrayList<>();

      //server information: host, port
      for (int i = 0; i < amount; ++i) {
        servers.add(new Pair<String, Integer>(args[2 + 2 * i], Integer.valueOf(args[3 + 2 * i])));
        availableServers.add(0); // 0 is free
      }

      //calculating interval
      long interval = (end - start) / amount;
      while (end != start && interval == 0) {//not to waste servers
        --amount;
        interval = (end - start) / amount;
      }

      ArrayList<Thread> threads = new ArrayList<>();
      ArrayList<DistributorClient> distributorClients = new ArrayList<>();
      Semaphore semaphore = new Semaphore(0);


      MyLong amountOfFreeServers = new MyLong(previousAmount - amount);
      MyLong amountOfBusyServers = new MyLong(amount);
      Semaphore serverSemaphore = new Semaphore(0);

      //adding interval to server to calculate
      for (int i = 0; i < amount; ++i) {
        distributorClients
            .add(new DistributorClient(i, servers.get(i).getKey(), servers.get(i).getValue(),
                start + i * interval, i == amount - 1 ? end : start + (i + 1) * interval - 1,
                semaphore, logger, servers, availableServers, amountOfFreeServers,
                amountOfBusyServers, serverSemaphore));
        threads.add(new Thread(distributorClients.get(i)));
        availableServers.set(i, 1);//1 is busy
      }

      //starting calculation: sending requests to servers
      threads.forEach(Thread::start);

      for (int i = 0; i < amount; ++i) {//waiting for calculation
        try {
          semaphore.acquire();
        } catch (InterruptedException e) {
          logger.error("InterruptedException");
          e.printStackTrace();
        }
      }


      long sumOfPrimaryNumbers = 0;
      //counting the whole sum
      for (DistributorClient i : distributorClients) {
        sumOfPrimaryNumbers += i.getSumOfPrimary();
      }

      logger.info("sum found : {}", sumOfPrimaryNumbers);

      System.out.println(sumOfPrimaryNumbers);

    } else {
      logger.error("Wrong number of arguments");
    }
  }
}
