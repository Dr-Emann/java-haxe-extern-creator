package net.zdremann;

import java.io.OutputStream;
import java.io.PipedInputStream;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.sun.javadoc.ClassDoc;

public class ClassWriterCallable implements Callable<Void> {

	public static final ExecutorService writerService = Executors.newFixedThreadPool(16);
	public static final ExecutorService formaterService = Executors.newFixedThreadPool(16);

	public final PipedInputStream inputStream;
	private final ClassFormaterCallable formater;
	public final OutputStream outputStream;

	public ClassWriterCallable(ClassDoc clazz, OutputStream outputStream)
	{
		formater = new ClassFormaterCallable(clazz);
		inputStream = new PipedInputStream(2048);
		this.outputStream = outputStream;
	}

	@Override
	public Void call() throws Exception {
		try
		{
			inputStream.connect(formater.outputStream);
			formaterService.submit(formater);
			byte[] buffer = new byte[512];
			int length;
			while((length = inputStream.read(buffer))>0)
			{
				outputStream.write(buffer, 0, length);
			}
		}
		finally
		{
			outputStream.flush();
			outputStream.close();
			inputStream.close();
		}
		return null;
	}

}
