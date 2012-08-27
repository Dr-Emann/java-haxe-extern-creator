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
import java.util.Collections;
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
		HashMap<String, String> value = new HashMap<String, String>();
		value.put("java.lang.String", "String");
		value.put("java.lang.Object", "Dynamic");
		value.put("C", "Char16");
		value.put("I", "Int");
		value.put("S", "Int16");
		value.put("B", "Int8");
		value.put("F", "Float");
		value.put("D", "java.lang.Number.Double");
		value.put("J", "haxe.Int64");
		value.put("V", "Void");
		value.put("Z", "Bool");
		return value;
	}

    private static HashMap<String, String> NATIVE_MAPPING = new HashMap<String, String>();
	
	private final File baseDestination;
	
	public ExternWriter(String baseDestinationLocation)
	{
		baseDestination = new File(baseDestinationLocation);
	}
	public ExternWriter(File baseDestination)
	{
		this.baseDestination = baseDestination;
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
		
		destination = new File(destination, theClass.haxeClassName + ".hx");
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
		String classOrInterface = (theClass.isInterface())?"interface":"class";
        if(!theClass.className.equals(theClass.haxeClassName)) builder.append(buildNativeMetadata(theClass));
		if(theClass.classParameterNames != null && theClass.classParameterNames.length > 0)
		{
			for(int i=0;i<theClass.classParameterNames.length; i++)
			{
				String name = theClass.classParameterNames[i];
				String type = theClass.classParameterTypes[i];
				type = getType(type);
				classParameters.append(String.format("%s:%s,", name, type));
			}
			classParameters.deleteCharAt(classParameters.length()-1);
			
			builder.append(String.format("extern %s %s<%s> %s%n", classOrInterface, theClass.haxeClassName, classParameters.toString(), buildExtensions(theClass)));
		}
		else
		{
			builder.append(String.format("extern %s %s %s%n", classOrInterface, theClass.haxeClassName, buildExtensions(theClass)));
		}
		
		builder.append(String.format("{%n"));
		
		builder.append(buildFields(theClass));
		
		
		
		builder.append(String.format("%n"));
		
		builder.append(buildMethods(theClass));
		
		builder.append(String.format("%n}"));
		return builder.toString();
	}

    private String buildNativeMetadata(ClassVO theClass)
    {
        String nativePackageAndClass = String.format("%s.%s", theClass.classPackage, theClass.className);
        String haxePackageAndClass = String.format("%s.%s", theClass.classPackage, theClass.haxeClassName);
        NATIVE_MAPPING.put(nativePackageAndClass, haxePackageAndClass);
        return String.format("@native('%s') ", nativePackageAndClass);
    }
	
	public StringBuilder buildFields(final ClassVO theClass)
	{
		StringBuilder builder = new StringBuilder();
		
		for(Field field : theClass.fields)
		{
			String accessString = field.accessString();
			String name = field.getName();
			String type = getType(field.getType());
            if(name.startsWith("class$")) continue;
			builder.append(String.format("\t%s var %s:%s;%n", accessString, name, type));
		}
		
		return builder;
		
	}
	
	public StringBuilder buildExtensions(final ClassVO theClass)
	{
		final StringBuilder builder = new StringBuilder();
		final String superClass = getType(theClass.superClass);
        boolean extended = false;
		if( !superClass.equals("Dynamic"))
		{
			builder.append(" extends " + superClass);
            extended = true;
		}
		if(theClass.interfaces != null && theClass.interfaces.length > 0)
		{
			if(extended) builder.append(",");
            builder.append(" implements ");
			for(String curInterface : theClass.interfaces)
			{
				String interfaceStr = getType(curInterface);
				builder.append(interfaceStr);
				builder.append(", ");
			}
			builder.setLength(builder.length()-2);
		}
		return builder;
	}
	
	public StringBuilder buildMethods(final ClassVO theClass)
	{
		StringBuilder builder = new StringBuilder();
		HashMap<String, List<Method>> existingMethods = new HashMap<String, List<Method>>();
		for(int i=0;i<theClass.methods.length;i++)
		{
			Method method = theClass.methods[i];
			if(!method.isPublic())
				continue;
			if(method.getName().equals("<init>"))
				method.setName("new");
			
			List<Method> methodsNamed;
			
			if(!existingMethods.containsKey(method.getName()))
			{
				methodsNamed = new ArrayList<Method>();
				existingMethods.put(method.getName(), methodsNamed);
			}
			else
			{
				methodsNamed = existingMethods.get(method.getName());
			}
			
			methodsNamed.add(method);
		}
		
		for(List<Method> value : existingMethods.values())
		{
			Collections.sort(value);
			for(int i=0; i<value.size(); i++)
			{
				Method method = value.get(i);
				String methodString;
				String methodReturnType = getType(method.returnType);
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
				
				if(i!= value.size()-1)
				{
					methodString = String.format("@:overload(function(%s):%s{})", methodArgumentString, methodReturnType);
					if(method.typeParameterNames != null && method.typeParameterNames.length != 0)
					{
						System.out.println("overloading a function with parameters "+methodString);//TODO: Replace parameters with Dynamic
					}
				}
				else //last
				{
					if(method.typeParameterNames!=null && method.typeParameterNames.length >0)
					{
						methodString = String.format("%s function %s<%s>(%s):%s;", method.accessString(), method.getName(), typeMethodString, methodArgumentString, methodReturnType);
					}
					else
					{
						methodString = String.format("%s function %s(%s):%s;", method.accessString(), method.getName(), methodArgumentString, methodReturnType);
					}
				}
				builder.append(String.format("\t%s%n", methodString));
			}
			builder.append(String.format("%n"));
		}
		
		return builder;
	}


    // TODO this does not handle the case where a type name that needs to be rempapped has not yet
    // been encountered by the the ClassVO which has some code for handling remapping illegal type names
    // e.g. Java classes beginning with lowercase.
	private static String getType(String typeStr)
	{
        StringBuilder returnStr;

		if(TYPE_EQUIVALENTS.containsKey(typeStr))
		{
			returnStr = new StringBuilder(TYPE_EQUIVALENTS.get(typeStr));
			return returnStr.toString();
		}
        else if(NATIVE_MAPPING.containsKey(typeStr))
        {
            returnStr = new StringBuilder(NATIVE_MAPPING.get(typeStr));
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
            else if(NATIVE_MAPPING.containsKey(m.group(1)))
            {
                returnStr.replace(m.start(1), m.end(1), NATIVE_MAPPING.get(m.group(1)));
            }
		}
		return returnStr.toString();
	}
    /*
    private static String fixIllegalType(String typeName)
    {
        String originalClassName;
        String legalClassName;
        String classPackage;

        int searchStartPos = typeName.indexOf('<'); // should always equal -1.
        if(searchStartPos == -1)
            searchStartPos = typeName.length()-1;

        int packageNameDivider = typeName.lastIndexOf('.', searchStartPos);

        originalClassName = typeName.substring(packageNameDivider+1,searchStartPos);
        String firstChar = originalClassName.substring(0,1);

        if(packageNameDivider == -1)
            classPackage = "";
        else
            classPackage = typeName.substring(0, packageNameDivider).toLowerCase();

        String haxePackageAndClass;
        String nativePackageAndClass;

        if(firstChar.toUpperCase() != originalClassName.substring(0,1))
        {

            legalClassName = firstChar.toUpperCase() + originalClassName.substring(1);
            nativePackageAndClass = String.format("%s.%s", classPackage, originalClassName);
            haxePackageAndClass = String.format("%s.%s", classPackage, legalClassName);
            NATIVE_MAPPING.put(nativePackageAndClass, haxePackageAndClass);
        }
        else
        {
            return typeName;
        }
        return legalClassName;
    }
	*/
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
