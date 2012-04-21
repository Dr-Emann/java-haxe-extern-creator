package net.zdremann;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.zdremann.vo.ClassVO;
import net.zdremann.vo.Field;
import net.zdremann.vo.Method;

public class ExternWriter {
	private final static HashMap<String, String> TYPE_EQUIVALENTS = initTypeEquivalents();
	private static HashMap<String, String> initTypeEquivalents() {
		HashMap<String, String> value = new HashMap<>();
		value.put("java.lang.String", "String");
		value.put("java.lang.Object", "Dynamic");
		value.put("I", "Int");
		value.put("S", "Int16");
		value.put("B", "Int8");
		value.put("F", "Float");
		value.put("D", "Double");
		value.put("J", "haxe.Int64");
		value.put("V", "Void");
		value.put("Z", "Bool");
		return value;
	}
	
	private final File baseDestination;
	
	public ExternWriter(String baseDestinationLocation)
	{
		baseDestination = new File(baseDestinationLocation);
	}
	
	public void writeClass(ClassVO theClass) throws IOException
	{
		String[] packageParts = theClass.classPackage.split("\\.");
		
		File destination = baseDestination;
		for(String part : packageParts)
		{
			destination = new File(destination, part);
		}
		if(!destination.exists())
			if(destination.mkdirs() == false)
				throw new IOException("Could not create directory for class at " + destination.getCanonicalPath());
		
		destination = new File(destination, theClass.className + ".hx");
		String outputString = buildOutputString(theClass);
		Writer writer = null;
		try
		{
			writer = new BufferedWriter(new FileWriter(destination));
			writer.write(outputString);
		}
		finally
		{
			try{
				writer.close();
			}
			catch(Exception e)
			{
				
			}
		}
	}

	private String buildOutputString(ClassVO theClass) {
		StringBuilder builder = new StringBuilder();
		builder.append(String.format("package %s;%n%n",theClass.classPackage));
		builder.append(String.format("import java.StdTypes;%n"));
		
		StringBuilder classParameters = new StringBuilder();
		for(int i=0;i<theClass.classParameterNames.length; i++)
		{
			String name = theClass.classParameterNames[i];
			String type = theClass.classParameterTypes[i];
			type = getType(type);
			classParameters.append(String.format("%s:%s,", name, type));
		}
		classParameters.deleteCharAt(classParameters.length()-1);
		
		builder.append(String.format("extern class %s<%s>%n", theClass.className, classParameters.toString()));
		builder.append(String.format("{%n"));
		
		builder.append(buildFields(theClass));
		
		
		
		builder.append(String.format("%n"));
		
		builder.append(buildMethods(theClass));
		
		builder.append(String.format("%n}"));
		return builder.toString();
	}
	
	public StringBuilder buildFields(final ClassVO theClass)
	{
		StringBuilder builder = new StringBuilder();
		
		for(Field field : theClass.fields)
		{
			String accessString = field.accessString();
			String name = field.getName();
			String type = getType(field.getType());
			builder.append(String.format("\t%s %s:%s;%n",accessString, name, type));
		}
		
		return builder;
		
	}
	public StringBuilder buildMethods(final ClassVO theClass)
	{
		StringBuilder builder = new StringBuilder();
		HashMap<String, List<String>> existingMethods = new HashMap<>();
		String methodString = "";
		for(int i=0;i<theClass.methods.length;i++)
		{
			Method method = theClass.methods[i];
			if(!method.isPublic())
				continue;
			if(method.getName().equals("<init>"))
				method.setName("new");
			
			StringBuilder methodArgsStringBuilder = new StringBuilder();
			
			for(int j=0;j<method.parameters.length; j++)
			{
				String paramName = "arg"+j;
				String paramType = method.parameters[j];
				paramType = getType(paramType);
				methodArgsStringBuilder.append(String.format("%s:%s,", paramName, paramType));
			}
			
			String methodArgumentString;
			if(method.parameters.length != 0)
				methodArgumentString = methodArgsStringBuilder.substring(0, methodArgsStringBuilder.length()-1);
			else
				methodArgumentString = methodArgsStringBuilder.toString();
			
			String typeMethodString = "";
			if(method.typeParameterNames!= null)
			{
				for(int j=0;j<method.typeParameterNames.length; j++)
				{
					String paramName = method.typeParameterNames[j];
					String paramType = getType(method.typeParameterTypes[j]);

					typeMethodString += String.format("%s:%s,", paramName, paramType);
				}
			}

			if(typeMethodString.length() != 0)
			{
				typeMethodString = typeMethodString.substring(0, typeMethodString.length()-1);
			}
			
			List<String> methodsNamed;
			
			String methodReturnType = getType(method.returnType);
			
			if(!existingMethods.containsKey(method.getName()))
			{
				if(method.typeParameterNames!=null && method.typeParameterNames.length >0)
				{
					methodString = String.format("%s %s<%s>(%s):%s;", method.accessString(), method.getName(), typeMethodString, methodArgumentString, methodReturnType);
				}
				else
				{
					methodString = String.format("%s %s(%s):%s;", method.accessString(), method.getName(), methodArgumentString, methodReturnType);
				}
				methodsNamed = new ArrayList<>();
				existingMethods.put(method.getName(), methodsNamed);
			}
			else
			{
				methodsNamed = existingMethods.get(method.getName());
				if(method.typeParameterNames == null || method.typeParameterNames.length == 0)
				{
					methodString = String.format("@:overload(function(%s):%s{})", methodArgumentString, methodReturnType);
				}
				else
				{
					methodString = String.format("@:overload(function(%s):%s{})", methodArgumentString, methodReturnType); //TODO: Replace parameters with Dynamic
					System.out.println("overloading a function with parameters "+methodString);
				}
			}
			
			methodsNamed.add(methodString);
		}
		
		for(List<String> value : existingMethods.values())
		{
			for(int i=value.size()-1; i>=0; i--)
			{
				builder.append(String.format("\t%s%n", value.get(i)));
			}
			builder.append(String.format("%n"));
		}
		
		return builder;
	}
	
	private static String getType(String typeStr)
	{
		StringBuilder returnStr;
		if(TYPE_EQUIVALENTS.containsKey(typeStr))
		{
			returnStr = new StringBuilder(TYPE_EQUIVALENTS.get(typeStr));
			return returnStr.toString();
		}
		else
		{
			returnStr = new StringBuilder(typeStr);
		}
		
		Pattern p = Pattern.compile("<([^<>]+)>");
		Matcher m = p.matcher(returnStr);
		
		while(m.find())
		{
			if(TYPE_EQUIVALENTS.containsKey(m.group(1)))
			{
				returnStr.replace(m.start(1), m.end(1), TYPE_EQUIVALENTS.get(m.group(1)));
			}
		}
		return returnStr.toString();
	}
	
	private static String join(Object[] arr, String delimeter)
	{
		if(arr==null || arr.length<1)
			return "";
		StringBuilder s = new StringBuilder();
		for(Object o : arr)
		{
			s.append(o);
			s.append(delimeter);
		}
		return s.substring(0, s.length()-delimeter.length());
	}
}
