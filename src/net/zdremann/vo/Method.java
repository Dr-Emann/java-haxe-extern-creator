package net.zdremann.vo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.freeinternals.classfile.core.MethodInfo;

public class Method implements Accessable, Comparable<Method> {
	private int accessFlags;
	private String name;
	public String[] parameters;
	public String[] typeParameterNames;
	public String[] typeParameterTypes;
	public String returnType;
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public int getAccessFlags() {
		return accessFlags;
	}

	public void setAccessFlags(int accessFlags) {
		this.accessFlags = accessFlags;
	}
	
	
	public void setDescriptor(String descriptor)
	{
		int namePos = 0;
		if(descriptor.charAt(0) == '<')
		{
			// Has Generic Parameters;
			int pos = 0;
			int numOpens = 1;
			final int inStrLen = descriptor.length();
			while(pos<inStrLen-1)
			{
				pos++;
				char currentChar = descriptor.charAt(pos);
				if(currentChar== '<')
					numOpens++;
				else if(currentChar == '>')
					numOpens--;

				if(numOpens == 0)
					break;
			}
			namePos = pos + 1;

			String genericParameters = descriptor.substring(1, pos);
			Pattern p = Pattern.compile("(\\w+)::?([LT][\\w/<>;]+;)");
			Matcher m = p.matcher(genericParameters);
			
			ArrayList<String> names = new ArrayList<>();
			ArrayList<String> types = new ArrayList<>();
			
			if(m.find())
			{
				do
				{
					String paramName = m.group(1);
					String paramType = ClassVO.typeString(m.group(2))[0];
					
					names.add(paramName);
					types.add(paramType);
				}
				while(m.find());
			}
			
			this.typeParameterNames = new String[names.size()];
			this.typeParameterTypes = new String[names.size()];
			for(int i = 0; i<typeParameterNames.length; i++)
			{
				typeParameterNames[i] = names.get(i);
				typeParameterTypes[i] = types.get(i);
			}
		}
		
		
		if(descriptor.charAt(namePos)!='(')
			throw new IllegalArgumentException();
		final int endingParen = descriptor.lastIndexOf(')');
		final String parameterString = descriptor.substring(namePos+1,endingParen);
		final String returnValueString = descriptor.substring(endingParen+1);
		
		this.parameters = ClassVO.typeString(parameterString);
		this.returnType = ClassVO.typeString(returnValueString)[0];
	}
	
	public String accessString()
	{
		String accessStr = "";
		if(isStatic())
			accessStr += "static ";
		if(isPublic())
			accessStr += "public";
		
		return accessStr;
	}
	
	public boolean isStatic()
	{
		return (this.accessFlags & MethodInfo.ACC_STATIC)!=0;
	}

	@Override
	public String toString() {
		return (isPublic()?"public":"private")+ " function " + name + "(" + Arrays.toString(parameters) + "):"+ returnType;
	}
	
	@Override
	public boolean isPublic() {
		return (this.accessFlags & MethodInfo.ACC_PUBLIC)!=0;
	}

	@Override
	public int compareTo(Method other) {
		int nameCompare = this.getName().compareTo(other.getName());
		if(nameCompare != 0)
			return nameCompare;
		
		if(this.parameters.length != other.parameters.length)
			return this.parameters.length - other.parameters.length;
		
		if(this == other)
			return 0;
		
		for(int i=0;i<this.parameters.length;i++)
		{
			String thisParam = this.parameters[i];
			String otherParam = other.parameters[i];
			
			boolean thisIsDynamic = thisParam.equals("Dynamc")||thisParam.equals("java.lang.Object");
			boolean otherIsDynamic = otherParam.equals("Dynamic")||otherParam.equals("java.lang.Object");
			
			if(thisIsDynamic && !otherIsDynamic) return 1;
			if(!thisIsDynamic && otherIsDynamic) return -1;
		}
		
		return 0;
	}
}
