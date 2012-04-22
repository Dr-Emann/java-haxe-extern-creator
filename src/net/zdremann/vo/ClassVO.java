package net.zdremann.vo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.freeinternals.classfile.core.AccessFlags;
import org.freeinternals.classfile.core.ClassFile;

public class ClassVO {
	public int accessFlags;
	public String[] classParameterNames;
	public String[] classParameterTypes;
	public String className;
	public String classPackage;
	public String superClass;
	public String[] interfaces;
	public Method[] methods;
	public Field[] fields;
	
	public boolean isPublic()
	{
		return (accessFlags & AccessFlags.ACC_PUBLIC) != 0;
	}
	public boolean isInterface()
	{
		return (accessFlags & AccessFlags.ACC_INTERFACE) != 0;
	}
	
	public void classNameAndPackage(String inStr)
	{
		int searchStartPos = inStr.indexOf('<'); // should always equal -1. 
		if(searchStartPos == -1)
			searchStartPos = inStr.length()-1;
		
		int packageNameDivider = inStr.lastIndexOf('.', searchStartPos);
		
		this.className = inStr.substring(packageNameDivider+1);
		if(packageNameDivider == -1)
			this.classPackage = "";
		else
			this.classPackage = inStr.substring(0, packageNameDivider).toLowerCase();
	}

	public void fromSignature(String inStr)
	{
		int thisPos = 0;
		if(inStr.charAt(0) == '<')
		{
			// Has Generic Parameters;
			int pos = 0;
			int numOpens = 1;
			final int inStrLen = inStr.length();
			while(pos<inStrLen-1)
			{
				pos++;
				char currentChar = inStr.charAt(pos);
				if(currentChar== '<')
					numOpens++;
				else if(currentChar == '>')
					numOpens--;

				if(numOpens == 0)
					break;
			}
			thisPos = pos + 1;

			String genericParameters = inStr.substring(1, pos);
			Pattern p = Pattern.compile("(\\w+)::?([LT][\\w/<>;]+;)");
			Matcher m = p.matcher(genericParameters);
			
			ArrayList<String> names = new ArrayList<>();
			ArrayList<String> types = new ArrayList<>();
			
			if(m.find())
			{
				do
				{
					//System.out.println(m.group(1));
					//System.out.println(m.group(2));
					String paramName = m.group(1);
					String paramType = typeString(m.group(2))[0];
					
					names.add(paramName);
					types.add(paramType);
				}
				while(m.find());
			}
			else
				throw new RuntimeException("Unable to parse '" + genericParameters + "' into parameters");
			
			this.classParameterNames = new String[names.size()];
			this.classParameterTypes = new String[types.size()];
			
			names.toArray(classParameterNames);
			types.toArray(classParameterTypes);
			
		}
		
		String descriptionString = inStr.substring(thisPos); 
		
		final String[] description = typeString(descriptionString);
		
		this.superClass = description[0];
		this.interfaces = Arrays.copyOfRange(description, 1, description.length);
	}
	/**
	 * Deals only with typeStrings that are fully qualified classes, possibly with generic parameters;
	 * @param inStr
	 * @return
	 */
	public static String[] typeString(String inStr)
	{
		ArrayList<String> list = new ArrayList<>();
		
		int pos = 0;
		
		while(pos<inStr.length())
		{
			String currentType = getOneType(inStr, pos);
			final int length = (currentType.charAt(0)<<16) + (currentType.charAt(1));
			pos += length;
			list.add(currentType.substring(2));
		}

		String[] returnArr = new String[list.size()];

		for(int i=0; i<returnArr.length; i++)
		{
			returnArr[i] = list.get(i);
		}

		return returnArr;
	}

	/**
	 * 
	 * @param inStr
	 * @return A string with the first 2 chars being an integer describing the number of characters that should be skipped 
	 */
	public static String getOneType(final String inStr)
	{
		return getOneType(inStr, 0);
	}
	public static String getOneType(final String inStr, final int startIndex)
	{
		int pos = startIndex;
		
		String returnStr;

		char currentChar = inStr.charAt(pos);
		switch(currentChar)
		{
		case 'J':
		case 'Z':
		case 'B':
		case 'I':
		case 'C':
		case 'S':
		case 'F':
		case 'D':
		case 'V':
			returnStr = String.valueOf(currentChar);
			break;
		case 'L':
			pos++;
			StringBuilder s = new StringBuilder();
			while((currentChar = inStr.charAt(pos)) != ';')
			{
				if(currentChar == '<')
				{
					int unMatchedLTs = 1;
					pos++;
					currentChar = inStr.charAt(pos);
					StringBuilder genericParams = new StringBuilder();
					while(unMatchedLTs > 0)
					{
						genericParams.append(currentChar);
						pos++;
						currentChar = inStr.charAt(pos);
						if(currentChar == '>')
							unMatchedLTs--;
						else if(currentChar == '<')
							unMatchedLTs++;
					}

					String[] genericParamList = typeString(genericParams.toString());
					s.append('<');
					for(int i=0; i<genericParamList.length; i++)
					{
						s.append(genericParamList[i]);
						if(i!= genericParamList.length-1)
						{
							s.append(", ");
						}
					}
					s.append('>');

				}
				else
					s.append(currentChar);

				pos++;
			}
			returnStr = s.toString();
			returnStr = returnStr.replace('/', '.');
			break;
		case '*':
			returnStr = "java.lang.Object";
			break;
		case '+':
		case '-':
			returnStr = getOneType(inStr, pos+1);
			pos += (returnStr.charAt(0)<<16) + (returnStr.charAt(1));
			returnStr = returnStr.substring(2);
			break;
		case '[':
			returnStr = getOneType(inStr, pos+1);
			pos += (returnStr.charAt(0)<<16) + (returnStr.charAt(1));
			returnStr = "java.NativeArray<"+returnStr.substring(2)+ ">";
			break;
		case 'T':
			pos++;
			StringBuilder typeName = new StringBuilder();
			while((currentChar = inStr.charAt(pos))!= ';')
			{
				typeName.append(currentChar);
				pos++;
			}
			returnStr = typeName.toString();
			break;
		default:
			returnStr = "";
			throw new RuntimeException("Unknown type: '" + currentChar + "' in '"+ inStr+"'");
		}
		pos++;
		
		final int length = pos - startIndex;
		final String lengthString = "" + ((char)((length>>16)&0xffff)) + ((char)(length & 0xffff));
		
		return lengthString + returnStr;
		
	}
}
