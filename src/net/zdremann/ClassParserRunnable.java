package net.zdremann;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import net.zdremann.vo.ClassVO;

import org.freeinternals.classfile.core.ClassFormatException;

public class ClassParserRunnable implements Runnable {
	
	final File source;
	final ExternWriter writer;
	
	public ClassParserRunnable(final File source, final File destination)
	{
		this(source, new ExternWriter(destination));
	}
	public ClassParserRunnable(final File source, final ExternWriter writer)
	{
		this.source = source;
		this.writer = writer;
	}
	
	@Override
	public void run() {
		ClassFileParser parser = new ClassFileParser();
		try
		{
			parser.readFile(source);
		}
		catch(FileNotFoundException fnfe)
		{
			System.err.println("Source file was not found at '"+source.getAbsolutePath()+"'");
			return;
		}
		catch(IOException ioe)
		{
			System.err.println("Error accessing file");
			return;
		}
		catch(ClassFormatException cfe)
		{
			System.out.println("Class file is not valid");
			return;
		}
		
		final ClassVO data = parser.parseClass();
		try
		{
			writer.writeClass(data);
		}
		catch(IOException ioe)
		{
			System.err.println("Could not write file");
			return;
		}
	}

}
