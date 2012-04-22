package net.zdremann;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ClassFinderRunnable implements Runnable {
	final File source;
	final ExternWriter writer;
	
	public ClassFinderRunnable(File source, ExternWriter writer)
	{
		this.source = source;
		this.writer = writer;
	}
	
	@Override
	public void run() {
		
	}

}
