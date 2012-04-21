package net.zdremann;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import net.zdremann.vo.ClassVO;
import net.zdremann.vo.Field;
import net.zdremann.vo.Method;

import org.freeinternals.classfile.core.AbstractCPInfo;
import org.freeinternals.classfile.core.AttributeExtended;
import org.freeinternals.classfile.core.AttributeInfo;
import org.freeinternals.classfile.core.ClassFile;
import org.freeinternals.classfile.core.ClassFormatException;
import org.freeinternals.classfile.core.ConstantClassInfo;
import org.freeinternals.classfile.core.ConstantUtf8Info;
import org.freeinternals.classfile.core.FieldInfo;
import org.freeinternals.classfile.core.Interface;
import org.freeinternals.classfile.core.MethodInfo;

public class Main {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		String fileName = (args.length > 1)?args[1]:".\\bin\\tmp\\SecondGenericClass.class";
		
		parseClassFile(fileName);
	}
	
	public static void parseClassFile(String fileName)
	{
		File file = new File(fileName);
		
		ClassFileParser parser = new ClassFileParser();
		
		try
		{
			parser.readFile(file);
		}
		catch(FileNotFoundException fnfe)
		{
			fnfe.printStackTrace();
			System.exit(1);
		}
		catch(IOException ioe)
		{
			ioe.printStackTrace();
			System.exit(1);
		}
		catch(ClassFormatException cfe)
		{
			cfe.printStackTrace();
			System.exit(1);
		}
		
		ClassVO vo = parser.parseClass();
		ExternWriter writer = new ExternWriter("C:\\Users\\Zach\\Desktop\\tmp\\Files");
		try
		{
			writer.writeClass(vo);
		}
		catch (IOException ioe)
		{
			ioe.printStackTrace();
			System.exit(1);
		}
	}

}
