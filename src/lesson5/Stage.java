package lesson5;

/** Абстрактный класс, представляющий этап гонки */
public abstract class Stage {
    protected int length;
    protected String description;
    public String getDescription() {
        return description;
    }

    /**
     * Метод, имитирующий прохождение машиной этого этапа гонки
     * @param c Машина
     */
    public abstract void go(Car c);
}
