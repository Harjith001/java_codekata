package org.example.playground;

public class RunnableThread {
    public static void main(String[] args) throws InterruptedException {
        Thread thread1 = new Thread(new ThreadImplementation("1"));
        Thread thread2 = new Thread(new ThreadImplementation("2"));

        Thread thread3 = new Thread(()->{
            for (int i =0; i < 25;i++) {
                System.out.println("Thread 3 " + " : "+ i);
            }
        });

        thread1.start();
        thread1.join();
        thread3.start();

        System.out.println("Threads completed execution");
    }
}

class ThreadImplementation implements Runnable {
    private final String threadNumber;
    ThreadImplementation(String threadNumber) {
        this.threadNumber = threadNumber;
    }
    @Override
    public void run(){
        for (int i =0; i < 5;i++) {
            System.out.println("Thread " + threadNumber + " : "+ i);
        }
    }
}
