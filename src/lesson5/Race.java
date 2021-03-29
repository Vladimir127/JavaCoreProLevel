package lesson5;

import java.util.ArrayList;
import java.util.Arrays;

/** Гонка (объект, который содержит этапы) */
public class Race {

    /** Список этапов гонки */
    private ArrayList<Stage> stages;

    public ArrayList<Stage> getStages() { return stages; }

    public Race(Stage... stages) {
        this.stages = new ArrayList<>(Arrays.asList(stages));
    }
}
