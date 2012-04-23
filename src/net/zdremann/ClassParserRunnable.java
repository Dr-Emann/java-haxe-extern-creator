package net.zdremann;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import net.zdremann.vo.ClassVO;

import org.freeinternals.classfile.core.ClassFormatException;

public class ClassParserRunnable implements Runnable {
	
	final File source;
	final byte[] sourceArray;
	final ExternWriter writer;
	
	public ClassParserRunnable(final File source, final File destination)
	{
		this(source, new ExternWriter(destination));
	}
	public ClassParserRunnable(final File source, final ExternWriter writer)
	{
		this.source = source;
		this.writer = writer;
		sourceArray = null;
	}
	
	public ClassParserRunnable(final byte[] sourceArray, final ExternWriter writer)
	{
		this.source = null;
		this.writer = writer;
		this.sourceArray = sourceArray;
	}
	
	@Override
	public void run() {
		ClassFileParser parser = new ClassFileParser();
		try
		{
			if(sourceArray== null)
				parser.readFile(source);
			else
				parser.readFile(sourceArray);
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
		
		if(!data.isPublic())
		{
			//Do not write private classes
			return;
		}
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
