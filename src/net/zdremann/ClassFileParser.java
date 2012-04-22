package net.zdremann;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;

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

public class ClassFileParser {
	
	
	public ClassFile classFile;
	
	public ClassFileParser()
	{
		
	}

	public void readFile(File inFile) throws FileNotFoundException, IOException, ClassFormatException
	{
		InputStream is = null;
		
		try
		{
			is = new BufferedInputStream(new FileInputStream(inFile));
			byte[] fileByteArray = new byte[(int)inFile.length()];
			is.read(fileByteArray);
			
			classFile = new ClassFile(fileByteArray);
		}
		finally
		{
			try 
			{
				is.close();
			}
			catch(Exception e)
			{
				
			}
		}
	}
	
	public ClassVO parseClass()
	{
		ClassVO vo = new ClassVO();
		
		AbstractCPInfo[] cpool = classFile.getConstantPool();
		
		//Get this
		ConstantClassInfo thisClass = (ConstantClassInfo)cpool[classFile.getThisClass().getValue()];
		vo.classNameAndPackage(getCString(cpool, thisClass.getNameIndex()).replace('/', '.').replace('$', '_'));
		
		//Get super
		ConstantClassInfo superClass = (ConstantClassInfo) cpool[classFile.getSuperClass().getValue()];
		vo.superClass = getCString(cpool, superClass.getNameIndex());
		
		//Get interfaces
		vo.interfaces = new String[classFile.getInterfacesCount().getValue()];
		Interface[] interfaces = classFile.getInterfaces();
		for(int i=0;i<vo.interfaces.length; i++)
		{
			vo.interfaces[i] = getClassName(cpool, interfaces[i].getValue());
		}
		
		vo.accessFlags = classFile.getAccessFlags().getValue();
		
		AttributeInfo[] attrs = classFile.getAttributes();
		for(AttributeInfo info : attrs)
		{
			switch(getCString(cpool, info.getNameIndex()))
			{
			case "Signature":
				AttributeExtended extendedInfo = (AttributeExtended) info;
				byte[] sigIndexRaw = extendedInfo.getRawData();
				int sigIndex = ((sigIndexRaw[0]&0xff)<<8)+(sigIndexRaw[1]&0xff);
				vo.fromSignature(getCString(cpool, sigIndex));
			}
		}
		
		vo.fields = new Field[classFile.getFieldCount().getValue()];
		FieldInfo[] fields = classFile.getFields();
		for(int i=0;i<vo.fields.length;i++)
		{
			Field cField = new Field();
			//System.out.println(getCString(cpool, fields[i].getAttribute(0).getNameIndex()));
			cField.setName(getCString(cpool, fields[i].getNameIndex()));
			cField.setAccessFlags(fields[i].getAccessFlags());
			cField.setType(getCString(cpool, fields[i].getDescriptorIndex()));
			
			for(int j=0;j<fields[i].getAttributesCount(); j++)
			{
				switch(getCString(cpool, fields[i].getAttribute(j).getNameIndex()))
				{
				case "Signature":
					AttributeExtended extendedInfo = (AttributeExtended) fields[i].getAttribute(j);
					byte[] sigIndexRaw = extendedInfo.getRawData();
					int sigIndex = ((sigIndexRaw[0]&0xff)<<8)+(sigIndexRaw[1]&0xff);
					cField.setType(getCString(cpool, sigIndex));
					//System.out.println("FieldSig: " + getCString(cpool, sigIndex));
				}
			}
			
			vo.fields[i] = cField;
		}
		
		vo.methods = new Method[classFile.getMethodCount().getValue()];
		MethodInfo[] methods = classFile.getMethods();
		for(int i=0; i<vo.methods.length; i++)
		{
			Method cMethod = new Method();
			MethodInfo cInfo = methods[i];
			cMethod.setName(getCString(cpool, cInfo.getNameIndex()));
			cMethod.setAccessFlags(cInfo.getAccessFlags());
			cMethod.setDescriptor(getCString(cpool, cInfo.getDescriptorIndex()));
			for(int j=0; j<cInfo.getAttributesCount(); j++)
			{
				switch(getCString(cpool, cInfo.getAttribute(j).getNameIndex()))
				{
				case "Signature":
					AttributeExtended extendedInfo = (AttributeExtended) cInfo.getAttribute(j);
					byte[] sigIndexRaw = extendedInfo.getRawData();
					int sigIndex = ((sigIndexRaw[0]&0xff)<<8)+(sigIndexRaw[1]&0xff);
					cMethod.setDescriptor(getCString(cpool, sigIndex));
				case "Code":
				case "Deprecated":
				case "RuntimeVisibleAnnotations":
				case "Exceptions":
				case "AnnotationDefault":
					break;
				default:
					System.out.println("Attribute found: " + getCString(cpool, cInfo.getAttribute(j).getNameIndex()));
					break;
				}
			}
			vo.methods[i] = cMethod;
		}
		
		return vo;
	}
	
	private static String getClassName(AbstractCPInfo[] cpool, int index)
	{
		return getCString(cpool, ((ConstantClassInfo)cpool[index]).getNameIndex());
		
	}
	private static String getCString(AbstractCPInfo[] cpool, int index)
	{
		return ((ConstantUtf8Info)cpool[index]).getValue();
	}
	
	
}
