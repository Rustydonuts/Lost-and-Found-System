import java.util.Scannner;
class thread1 extends Thread
{
    synchronized  void run()
    {
            for(int i=10;i>=0;i--)
            {
                System.out.println(i);
                Thread.sleep(10*1000);
            }
    }
    public static void main(String args[])
    {
        thread1 t1=new thread1();
        thread1 t2=new thread1();
        thread1 t3=new thread1();
        t1.start();
        t2.start();
        t3.start();
    }
}