package lesson4.online;

public class SynchCounter {
    int c;

    public int value() {return c;}

    public SynchCounter() {
        c = 0;
    }

    public void inc(){
        synchronized (this){
            c++;
        }
    }

    public synchronized void dec(){
        c--;
    }
}
