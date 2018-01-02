package ru.mipt.java2017.hw2;

import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import java.io.IOException;
import java.util.LinkedList;
import java.util.concurrent.Semaphore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Server {

  private static final Logger logger = LoggerFactory.getLogger("Server");
  private int port;
  private long coreNumber;
  private io.grpc.Server server;

  public Server(int port, long coreNumber) {
    this.port = port;
    this.coreNumber = coreNumber;
  }

  private void start() throws IOException {
    server = ServerBuilder.forPort(port)
        .addService(new SenderImpl(coreNumber))
        .build()
        .start();
    logger.info("Server started, listening on " + port);
    Runtime.getRuntime().addShutdownHook(new Thread() {
      @Override
      public void run() {
        // Use stderr here since the logger may have been reset by its JVM shutdown hook.
        System.err.print("*** shutting down gRPC server since JVM is shutting down");
        Server.this.stop();
        System.err.print("*** server shut down");
      }
    });
  }

  private void stop() {
    if (server != null) {
      server.shutdown();
    }
  }

  /**
   * Await termination on the main thread since the grpc library uses daemon threads.
   */
  private void blockUntilShutdown() throws InterruptedException {
    if (server != null) {
      server.awaitTermination();
    }
  }

  public static void main(String[] args) throws IOException, InterruptedException {
    if (args.length == 2) {
      long amountOfCores = Long.valueOf(args[0]);
      int port = Integer.valueOf(args[1]);

      final Server server = new Server(port, amountOfCores);
      server.start();
      server.blockUntilShutdown();
    }
  }


  static class SenderImpl extends SenderGrpc.SenderImplBase {

    private long coreNumber;

    public SenderImpl(long coreNumber) {
      this.coreNumber = coreNumber;
    }

    //distributes calculation among cores
    @Override
    public void send(Request request, StreamObserver<Reply> responseObserver) {
      //getting request
      long number = request.getNumber();
      long start = request.getStart();
      long end = request.getEnd();
      if (end - start < 0) {
        logger.error("Wrong parameters: end < start");
        return;
      }

      //splitting interval to calculate at cores
      long interval = (end - start) / coreNumber;

      while (end != start && interval == 0) {//not to waste cores with zero request
        --coreNumber;
        interval = (end - start) / coreNumber;
      }

      logger.info("Got request from client's thread number : {}", number);

      LinkedList<Thread> threads = new LinkedList<>();
      LinkedList<DistributorServer> distributorServers = new LinkedList<>();
      Semaphore semaphore = new Semaphore(0);

      //adding interval to thread to calculate
      for (long i = 0; i < coreNumber; ++i) {
        distributorServers.add(new DistributorServer(i, start + i * interval,
            i == coreNumber - 1 ? end : start + (i + 1) * interval - 1, semaphore, logger));
        threads.add(new Thread(distributorServers.getLast()));
      }

      //starting calculation
      threads.forEach(Thread::start);

      for (long i = 0; i < coreNumber; ++i) {//waiting threads to end calculation
        try {
          semaphore.acquire();
        } catch (InterruptedException e) {
          logger.error("Error: InterruptedException");
          e.printStackTrace();
        }
      }

      //collecting whole sum
      long sumOfPrimaryNumbers = 0;
      for (DistributorServer i : distributorServers) {
        sumOfPrimaryNumbers += i.getSumOfPrimary();
      }

      //sending reply
      Reply reply = Reply.newBuilder().setSumOfPrimaryNumbers(sumOfPrimaryNumbers).build();
      responseObserver.onNext(reply);
      responseObserver.onCompleted();
      logger.info("Send request to client's thread number : {} sum : {}", number,
          sumOfPrimaryNumbers);
    }
  }
}
