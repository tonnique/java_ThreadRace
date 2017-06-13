package ch15;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class ThreadRace extends JFrame implements Runnable, Suspendable {
	
	private Racer[] racers;
	private Thread[] threads;
	
	private static short racerCount = 0;	
	private static Thread monitorThread;
	//private static boolean inApplet = true;
	private static boolean threadSuspended = false;
	private static JFrame frame = null;
	private WindowAdapter windowAdapter = null;
	private JPanel racerPanel = new JPanel();
	
	
	public static void main(String[] args) {
		racerCount = 5;
		
		ThreadRace frame = new ThreadRace("Racing Threads");
		frame.setSize(400, 200);	
		
		frame.setVisible(true);
		frame.pack();
		
		// Ётот оператор не будет выполнен, пока не страруют все потоки-участники
		synchronized (monitorThread) {
			try {
				monitorThread.wait();
			} catch (InterruptedException e) {
				System.err.println("Main thread interrupted while waiting for racers to start");
				System.exit(1);
			}
		}
		System.out.println("And they're off!");
		
		// ќжидание пока все участники не достигнут финиша
		for (short i=0; i<racerCount; i++) {
			try {
				frame.threads[i].join();				
			} catch (InterruptedException e) {
				System.err.println("The monitor thread was interrupted while waiting for the other threads to exit.");
				System.exit(1);
			}
			System.exit(0);
		}
		
	}
	
	public ThreadRace(String s) {
		super(s);
		
		add(racerPanel, BorderLayout.CENTER);
		
		if (racerCount <= 0) {
			racerCount = 2;			
		}
		if (racerCount > Thread.MAX_PRIORITY - Thread.MIN_PRIORITY+1)
			racerCount = (short) (Thread.MAX_PRIORITY - Thread.MIN_PRIORITY +1);
		
		this.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				frame.setVisible(false);
				frame.dispose();
				System.exit(0);
			}
		});
		
		racerPanel.setLayout(new GridLayout(racerCount, 1));
		
		// »нициализаци€ массивов racer и threads
		racers = new Racer[racerCount];
		threads = new Thread[racerCount];
		
		for (short i=0; i<racerCount; i++) {
			racers[i] = new Racer("Racer# " + i, this);
			
			// ¬ыравнивание рисунка дл€ размещени€ всех потоков-участников
			racers[i].setSize(getSize().width, getSize().height/racerCount);
			racerPanel.add(racers[i]);
		}
		
		//—оздание собственного потока монитора
		monitorThread = new Thread(null, this, "MonitorThread");
		monitorThread.start();
	}
	
	public boolean isSuspended() {
		return threadSuspended;
	}
	
	public void run() {
		if (monitorThread == Thread.currentThread()) {
			MouseAdapter mouseAdapter = new MouseAdapter() {	
				
				public synchronized void mousePressed(MouseEvent e) {
					e.consume();
					threadSuspended = !threadSuspended;
					if (!threadSuspended) {
						synchronized (monitorThread) {
							monitorThread.notifyAll();
						}
					}
				}				
			};
			
			for (short i=0; i<racerCount; i++) {
				// ƒл€ выбранной версии конструктора класса Thread
				// задаетс€ целевой объект Runnable
				threads[i] = new Thread(racers[i]);
				threads[i].setPriority(Thread.MIN_PRIORITY+i);
				threads[i].start();
				racers[i].addMouseListener(mouseAdapter);
			}
			synchronized (monitorThread) {
				monitorThread.notify();
			}
		}
		
		while(monitorThread == Thread.currentThread()) {
			try {
				monitorThread.sleep(100);
				synchronized (monitorThread) {
					while(threadSuspended) {
						monitorThread.wait();
					}
					synchronized (this) {
						notifyAll();
					}
				}
			} catch (InterruptedException e) {
				System.err.println("The monitor thread was interrupted while sleeping");
				System.exit(1);
			}
		}
	}
}
