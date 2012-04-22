package net.zdremann;

import java.io.File;
import java.io.FileFilter;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {

	static ExecutorService exec = Executors.newFixedThreadPool(6);
	static ExternWriter writer;
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		File source = null;
		File destination = null;
		if(args.length == 2)
		{
			source = new File(args[0]);
			destination = new File(args[1]);
		}
		else
		{
			System.err.println("Please pass source and destination as parameters");
			System.exit(1);
		}
		
		writer = new ExternWriter(destination);
		
		if(!source.exists())
		{
			System.err.println("Source location does not exist");
			System.exit(1);
		}
		if(destination.isFile())
		{
			System.err.println("Destination is a file, not a directory");
			System.exit(1);
		}
		if(!source.isDirectory())
		{
			if(!source.getName().endsWith(".class"))
			{
				System.err.println("Source is not a directory, or a class file");
				System.exit(1);
			}
			exec.execute(new ClassParserRunnable(source, writer));
		}
		else
		{
			recurse_files(source);
		}
	}
	
	public static void recurse_files(File source)
	{
		File[] classFiles = source.listFiles(new FileFilter() {
			@Override
			public boolean accept(File pathName) {
				if(pathName.isFile() && pathName.getName().endsWith(".class"))
					return true;
				else
					return false;
			}
		});
		
		File[] innerDirectories = source.listFiles(new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				if(pathname.isDirectory())
					return true;
				else
					return false;
			}
		});
		
		for(File file : classFiles)
		{
			exec.execute(new ClassParserRunnable(file, writer));
		}
		
		for(File direc : innerDirectories)
		{
			final File directory = direc;
			exec.execute(new Runnable() {
				
				@Override
				public void run() {
					recurse_files(directory);
				}
			});
		}
	}
}
