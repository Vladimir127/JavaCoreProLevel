package lesson5;

import java.util.concurrent.CountDownLatch;

/** Машина */
public class Car implements Runnable {
    private static int CARS_COUNT;
    static {
        CARS_COUNT = 0;
    }
    private Race race;
    private int speed;
    private String name;
    private CountDownLatch cdlStart;
    private CountDownLatch cdlFinish;

    public String getName() {
        return name;
    }
    public int getSpeed() {
        return speed;
    }
    public Car(Race race, int speed, CountDownLatch cdlStart, CountDownLatch cdlFinish) {
        this.race = race;
        this.speed = speed;
        this.cdlStart = cdlStart;
        this.cdlFinish = cdlFinish;
        CARS_COUNT++;
        this.name = "Участник #" + CARS_COUNT;
    }
    @Override
    public void run() {
        try {
            System.out.println(this.name + " готовится");
            Thread.sleep(500 + (int)(Math.random() * 800));
            System.out.println(this.name + " готов");

            cdlStart.countDown();
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            cdlStart.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        for (int i = 0; i < race.getStages().size(); i++) {
            race.getStages().get(i).go(this);
        }

        cdlFinish.countDown();
    }
}
