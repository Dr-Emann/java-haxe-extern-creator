package net.zdremann;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.sun.javadoc.*;

public class Main extends Doclet {
	
	private static File outputDir;
	
	public static boolean start(RootDoc root)
	{
		readOptions(root.options());
		ClassDoc[] classes = root.classes();
		List<Future<Void>> todo = new ArrayList<>();
		for(ClassDoc clazz : classes)
		{
			if(clazz.containingClass()!= null)
			{
				continue;
			}
			PackageDoc pack = clazz.containingPackage();
			String packs = pack.name().replace('.', File.separatorChar);
			File output = new File(outputDir, packs);
			output.mkdirs();
			if(!output.exists())
			{
				throw new RuntimeException("Could not create required directory in output directory");
			}
			output = new File(output, clazz.name()+".hx");
			OutputStream outputStream = null;
			try
			{
				outputStream = new BufferedOutputStream(new FileOutputStream(output));
			}
			catch (Exception e)
			{
				throw new RuntimeException(e.getMessage());
			}
			
			
			ClassWriterCallable cwc = new ClassWriterCallable(clazz, outputStream);
			todo.add(ClassWriterCallable.service.submit(cwc));
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
