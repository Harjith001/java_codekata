package org.example.playground;

import java.util.ArrayList;

public class ProducerConsumer {
    public static void main(String[] args) {
        Worker worker = new Worker(7, 0);

        Thread producer = new Thread(() -> {
            try {
                worker.produce();
            } catch (InterruptedException e) {
                    throw new RuntimeException();
            }
        }
        );

        Thread consumer = new Thread(()->{
            try {
                worker.consume();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });

        producer.start();
        consumer.start();
    }
}

class Worker {
    private final int top;
    private final int bottom;
    private final Object lock = new Object();
    private ArrayList<Integer> box;
    private int seq;

    public Worker(int top, int bottom) {
        this.top = top;
        this.bottom = bottom;
        box = new ArrayList<>();
        seq = 0;
    }

    public void produce() throws InterruptedException {
        synchronized (lock) {
            while (true) {
                if (box.size() == top) {
                    System.out.println("Box is full, waiting for consumer to consume");
                    lock.wait();
                } else {
                    System.out.println(seq + " Sequence added");
                    box.add(seq++);
                    lock.notify();
                }
                Thread.sleep(500);
            }
        }
    }

    public void consume() throws InterruptedException {
        synchronized (lock) {
            while (true) {
                if (box.size() == bottom) {
                    System.out.println("Box is empty, waiting for producer to put something in box");
                    lock.wait();
                } else {
                    System.out.println(box.removeFirst() + " is consumed from the box");
                    lock.notify();
                }
                Thread.sleep(500);
            }
        }
    }

}
