package net.zdremann;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.DocErrorReporter;
import com.sun.javadoc.Doclet;
import com.sun.javadoc.LanguageVersion;
import com.sun.javadoc.PackageDoc;
import com.sun.javadoc.RootDoc;

public class Main extends Doclet {

	private static File outputDir = null;

	public static void main(String[] args) {
		String source = null;
		String packages = null;
		String output = null;
		for(int i=0; i<args.length; i++)
		{
			if(args[i].equals("-source"))
			{
				if(i > args.length - 2)
				{
					throw new IllegalArgumentException("-source requires a source directory to be specified");
				}
				source = args[++i];
			}
			else if(args[i].equals("-output"))
			{
				if(i > args.length - 2)
				{
					throw new IllegalArgumentException("-output requires an ouput directory to be specified");
				}
				output = args[++i];
			}
			else if(args[i].equals("-packages"))
			{
				if(i > args.length -2)
				{
					throw new IllegalArgumentException("-packages requires a list of packages, seperated by colons");
				}
				packages = args[++i];
			}
		}
		if(source == null || packages == null || output == null)
		{
			throw new IllegalArgumentException("Usage: -source (source-dir) -output (output-dir) -packages (package-list)");
		}
		com.sun.tools.javadoc.Main.execute(new String[]{"-doclet",  Main.class.getName(), "-sourcepath", source, "-subpackages", packages, "-output", output});
		System.exit(0);
	}
	

	public static boolean start(RootDoc root)
	{
		readOptions(root.options());
		ClassDoc[] classes = root.classes();
		List<Future<Void>> todo = new ArrayList<>();
		
		OutputStream importHx = null;
		try
		{
			outputDir.mkdirs();
			importHx = new BufferedOutputStream(new FileOutputStream(new File(outputDir, "AllThings.hx")));
			importHx.write(String.format("package foobarsky.best.deals;%n%n").getBytes());
		}
		catch (FileNotFoundException fnfe)
		{
			fnfe.printStackTrace();
		}
		catch (IOException ioe)
		{
			ioe.printStackTrace();
		}
		
		for(ClassDoc clazz : classes)
		{
			PackageDoc pack = clazz.containingPackage();
			
			OutputStream outputStream;
			
			if(outputDir == null)
			{
				outputStream = System.out;
			}
			else
			{
				String packs = pack.name().replace('.', File.separatorChar);
				File output = new File(outputDir, packs);
				output.mkdirs();
				if(!output.exists())
				{
					throw new RuntimeException("Could not create required directory in output directory");
				}
				output = new File(output, clazz.name().replace('.', '_')+".hx");
				//System.out.println("Generating class: " + output.getAbsolutePath() + " (" + clazz.methods().length + ")");
				try
				{
					outputStream = new BufferedOutputStream(new FileOutputStream(output));
				}
				catch (Exception e)
				{
					throw new RuntimeException(e.getMessage());
				}
			}
			
			ClassWriterCallable cwc = new ClassWriterCallable(clazz, outputStream);
			todo.add(ClassWriterCallable.writerService.submit(cwc));
			try
			{
				String importString = String.format("import %s.%s;%n", pack.name(), clazz.name().replace('.', '_'));
				importHx.write(importString.getBytes());
				
			}
			catch (Exception e)
			{
				
			}
		}
		try
		{
			importHx.write("class AllThings{}".getBytes());
			importHx.flush();
			importHx.close();
		}
		catch(Exception e)
		{
			
		}

		for(Future<Void> future : todo)
		{
			try{
				future.get();
			}
			catch(Exception e){
				e.printStackTrace();
			}
		}

		System.out.println("Finished");
		return true;
	}

	private static void readOptions(String[][] options) {
		for (int i = 0; i < options.length; i++) {
			String[] opt = options[i];
			if (opt[0].equals("-output")) {
				outputDir = new File(opt[1]);
			}
		}
	}

	public static boolean validOptions(String options[][], DocErrorReporter reporter) {
		boolean foundOutputOption = false;
		for (int i = 0; i < options.length; i++) {
			String[] opt = options[i];
			if (opt[0].equals("-output")) {
				if (foundOutputOption) {
					reporter.printError("Only one -output option allowed.");
					return false;
				} else {
					foundOutputOption = true;
				}
			}
		}

		return foundOutputOption;
	}

	public static int optionLength(String option) {
		if (option.equals("-output")) {
			return 2;
		}
		return 0;
	}
	public static LanguageVersion languageVersion() {
		return LanguageVersion.JAVA_1_5;
	}
}
