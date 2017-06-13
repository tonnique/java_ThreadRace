package ch15;

import java.awt.*;
import javax.swing.*;

public class Racer extends Canvas implements Runnable {
	
	private short position = 0;
	private String name;
	private static final short numberOfSteps = 1000;
	Suspendable owner;
	
	public Racer(String theName, Suspendable theOwner) {
		name = theName;
		owner = theOwner;
	}
	
	public Racer(String theName) {
		name = theName;
		owner = null;
	}
	
	public synchronized void paint(Graphics g) {
		g.setColor(Color.BLACK);
		g.drawLine(0,  getSize().height/2, getSize().width, getSize().height/2);
		g.setColor(Color.red);		
		g.fillRect(position * getSize().width/numberOfSteps, getSize().height/3, 10, getSize().height/3);
		
	}
	
	@Override
	public void run() {
		while (position < numberOfSteps) {
			position++;
			repaint();
			try {
				Thread.currentThread().sleep(10);
				if (owner != null) {
					synchronized (owner) {
						if (owner.isSuspended()) {
							owner.wait();
						}
					}
				}
			} catch (InterruptedException e) {
				System.err.println("Поток " + name + " прерван");
				System.exit(1);
			}
		}
		System.out.println("Поток " + name + " завершил свое выполнение");
	} 
	
	public static void main(String[] agrs) {
		JFrame frame = new JFrame("One racer");
		Racer aRacer = new Racer("Тест");
		frame.setSize(400, 200);
		frame.add(aRacer, BorderLayout.CENTER);
		frame.setVisible(true);
		aRacer.paint(frame.getGraphics());
		frame.pack();
		aRacer.run();
		System.exit(0);
	}

}
