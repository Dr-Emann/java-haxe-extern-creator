package net.zdremann;

import java.io.OutputStream;
import java.io.PipedOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import net.zdremann.compare.MethodComparator;

import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.ConstructorDoc;
import com.sun.javadoc.FieldDoc;
import com.sun.javadoc.MethodDoc;
import com.sun.javadoc.PackageDoc;
import com.sun.javadoc.Parameter;
import com.sun.javadoc.Type;
import com.sun.javadoc.TypeVariable;

public class ClassFormaterCallable implements Callable<Void> {
	
	private final ClassDoc clazz;
	public final PipedOutputStream outputStream;
	
	public ClassFormaterCallable(ClassDoc clazz)
	{
		this.clazz = clazz;
		this.outputStream = new PipedOutputStream();
	}
	
	@Override
	public Void call() throws Exception {
		try
		{
			writeClassToStream(clazz, outputStream);
		}
		finally
		{
			outputStream.flush();
			outputStream.close();
		}
		return null;
	}
	
	private void writeClassToStream(ClassDoc clazz, OutputStream outputStream) throws Exception
	{
		if(clazz.containingClass() == null)
		{
			PackageDoc pack = clazz.containingPackage();
			outputStream.write(String.format("package %s;%n%n", pack.name()).getBytes());
		}
		
		outputStream.write(String.format("@:native(\"%s\")%n",clazz.qualifiedName()).getBytes());
		
		if(clazz.isOrdinaryClass())
		{
			outputStream.write(Stringifier.makeComment(clazz.getRawCommentText()).getBytes());
			
			String extendsAndImplements = "";
			boolean doesExtend = false;
			if(clazz.superclass() != null)
			{
				if(!Stringifier.typeToString(clazz.superclass()).equals(Stringifier.DYNAMIC_NAME))
				{
					extendsAndImplements += String.format("extends %s ", Stringifier.typeToString(clazz.superclass()));
					doesExtend = true;
				}
			}
			
			if(clazz.interfaces().length > 0)
			{
				if(doesExtend)
					extendsAndImplements += ", ";
				for(ClassDoc currentInterface : clazz.interfaces())
				{
					extendsAndImplements += String.format("implements %s,", Stringifier.typeToString(currentInterface));
				}
				extendsAndImplements = extendsAndImplements.substring(0, extendsAndImplements.length()-1);
				
			}
			
			outputStream.write(String.format("extern class %s %s", clazz.name().replace('.', '_'), extendsAndImplements).getBytes());

			if(clazz.typeParameters().length > 0)
			{
				String parameterTypes = "";
				for(TypeVariable type : clazz.typeParameters())
				{
					String extendedString = "";
					if(type.bounds().length > 0)
					{
						extendedString = ":(";
						for(Type bound : type.bounds())
							extendedString += Stringifier.typeToString(bound) + ",";
						extendedString = extendedString.substring(0, extendedString.length()-1) + ")";
					}
					parameterTypes += type.qualifiedTypeName()+ extendedString+",";
				}
				parameterTypes = parameterTypes.substring(0, parameterTypes.length()-1);
				outputStream.write(String.format("<%s>", parameterTypes).getBytes());
			}
			outputStream.write(String.format("%n{%n").getBytes());
			
			// Fields
			outputStream.write(buildFields(clazz.fields()).toString().getBytes());
			
			// Constructor
			outputStream.write(buildConstructors(clazz.constructors()).toString().getBytes());
			
			// Methods
			outputStream.write(buildMethods(clazz.methods()).toString().getBytes());
			
			outputStream.write(String.format("%n}%n").getBytes());
			
			for (ClassDoc innerClass : clazz.innerClasses())
			{
				writeClassToStream(innerClass, outputStream);
			}
		}
		else if(clazz.isEnum())
		{
			outputStream.write(Stringifier.makeComment(clazz.getRawCommentText()).getBytes());
			outputStream.write(String.format("extern enum %s", clazz.name().replace('.', '_')).getBytes());
			outputStream.write(String.format("%n{%n").getBytes());
			String consts = "";
			for(FieldDoc enumConst : clazz.enumConstants())
			{
				consts += Stringifier.makeComment(enumConst.getRawCommentText());
				consts += "\t" + enumConst.name() + ",\n";
			}
			consts = consts.substring(0, consts.length()-2);
			outputStream.write(consts.getBytes());
			outputStream.write(String.format("%n}%n").getBytes());
		}
		else if(clazz.isInterface())
		{
			outputStream.write(Stringifier.makeComment(clazz.getRawCommentText()).getBytes());
			outputStream.write(String.format("extern interface %s", clazz.name().replace('.', '_')).getBytes());

			if(clazz.typeParameters().length > 0)
			{
				String parameterTypes = "";
				for(TypeVariable type : clazz.typeParameters())
				{
					String extendedString = "";
					if(type.bounds().length > 0)
					{
						extendedString = ":(";
						for(Type bound : type.bounds())
							extendedString += Stringifier.typeToString(bound) + ",";
						extendedString = extendedString.substring(0, extendedString.length()-1) + ")";
					}
					parameterTypes += type.qualifiedTypeName()+ extendedString+",";
				}
				parameterTypes = parameterTypes.substring(0, parameterTypes.length()-1);
				outputStream.write(String.format("<%s>", parameterTypes).getBytes());
			}
			outputStream.write(String.format("%n{%n").getBytes());
			
			// Methods
			outputStream.write(buildMethods(clazz.methods()).toString().getBytes());
			
			outputStream.write(String.format("%n}%n").getBytes());
		}
		else
		{
			throw new RuntimeException("Unknown Class Type");
		}
	}
	
	private StringBuilder buildFields(FieldDoc[] fields) {
		StringBuilder s = new StringBuilder();
		Collection<String> usedFieldNames = new ArrayList<>();
		
		for(FieldDoc field : fields)
		{
			s.append(Stringifier.makeComment(field.getRawCommentText()));
			if(usedFieldNames.contains(field.name()))
			{
				s.append("\t//");
			}
			else 
				s.append("\t");
			
			s.append(String.format("%s%n", fieldString(field)));
			
			usedFieldNames.add(field.name());
		}
		
		return s;
	}

	private StringBuilder buildConstructors(ConstructorDoc[] constructors_arry)
	{
		StringBuilder s = new StringBuilder();
		
		if(constructors_arry.length == 0)
			return s;
		
		List<ConstructorDoc> constructors = Arrays.asList(constructors_arry);
		Collections.sort(constructors, new MethodComparator());
		
		List<ConstructorDoc> goodConstructors = new ArrayList<>();
		List<ConstructorDoc> badConstructors = new ArrayList<>();
		
		boolean firstPublic = constructors.get(0).isPublic();
		for(ConstructorDoc constructor : constructors)
		{
			if(constructor.isPublic() == firstPublic)
				goodConstructors.add(constructor);
			else
				badConstructors.add(constructor);
		}
		
		for(ConstructorDoc constructor : badConstructors)
		{
			s.append(Stringifier.makeComment(constructor.getRawCommentText()));
			s.append(String.format("\t//%s%n", constructorString(constructor, MethodStringType.OVERLOADEDv3)));
		}
		for(int i=0;i<goodConstructors.size();i++)
		{
			ConstructorDoc constructor = goodConstructors.get(i);
			s.append(Stringifier.makeComment(constructor.getRawCommentText()));
			if(i < goodConstructors.size()-1)
				s.append(String.format("\t%s%n", constructorString(constructor, MethodStringType.OVERLOADEDv2)));
			else
				s.append(String.format("\t%s%n", constructorString(constructor, MethodStringType.NORMAL)));
		}
		return s;
	}
	
	private StringBuilder buildMethods(MethodDoc[] methods)
	{
		StringBuilder s = new StringBuilder();
		
		Map<String, List<MethodDoc>> methodsByName = new HashMap<String, List<MethodDoc>>();
		
		for(MethodDoc method : methods)
		{
			if(method.isSynthetic())
			{
				continue;
			}
			List<MethodDoc> methodsWithSameName;
			if(methodsByName.containsKey(method.name()))
			{
				methodsWithSameName = methodsByName.get(method.name());
			}
			else
			{
				methodsWithSameName = new ArrayList<>();
				methodsByName.put(method.name(), methodsWithSameName);
			}
			methodsWithSameName.add(method);
		}
		
		for(List<MethodDoc> list : methodsByName.values())
		{
			List<MethodDoc> goodMethods = new ArrayList<>();
			List<MethodDoc> badMethods = new ArrayList<>();
			Collections.sort(list, new MethodComparator());
			boolean firstPublic = list.get(0).isPublic();
			int typeParamNum = list.get(0).typeParameters().length;
			for(MethodDoc method : list)
			{
				if(firstPublic != method.isPublic())
				{
					badMethods.add(method);
				}
				else if(typeParamNum < method.typeParameters().length)
				{
					badMethods.add(method);
				}
				else
				{
					goodMethods.add(method);
				}
			}
			for(MethodDoc method : badMethods)
			{
				s.append(Stringifier.makeComment(method.getRawCommentText()));
				s.append(String.format("\t//%s%n", methodString(method, MethodStringType.OVERLOADEDv3)));
			}
			for(int i=0;i<goodMethods.size();i++)
			{
				MethodDoc method = goodMethods.get(i);
				s.append(Stringifier.makeComment(method.getRawCommentText()));
				if(i < goodMethods.size()-1)
					s.append(String.format("\t%s%n", methodString(method, MethodStringType.OVERLOADEDv2)));
				else
					s.append(String.format("\t%s%n", methodString(method, MethodStringType.NORMAL)));
			}
			
		}
		
		return s;
	}
	
	private static enum MethodStringType
	{
		NORMAL, OVERLOADEDv2, OVERLOADEDv3
	}
	
	private String fieldString(FieldDoc field) {
		String s;
		Object constValue = field.constantValue();
		
		String modifierString = "";
		
		if(constValue != null)
			modifierString += "inline ";
		
		if(field.isStatic())
			modifierString += "static ";
		
		if(field.isPublic())
			modifierString += "public ";
		else
			modifierString += "private ";
		
		if(constValue == null)
			s = String.format("%svar %s:%s;", modifierString, field.name(), Stringifier.typeToString(field.type()));
		else
			s = String.format("%svar %s:%s = %s;", modifierString, field.name(), Stringifier.typeToString(field.type()), constValue.toString());
			
		
		return s;
	}
	
	private String constructorString(ConstructorDoc constructor, MethodStringType type)
	{
		String s = "";
		
		String parameterString = "";
		for(Parameter param : constructor.parameters())
		{
			parameterString += String.format("%s:%s,", param.name(), Stringifier.typeToString(param.type()));
		}
		if(constructor.parameters().length>0)
			parameterString = parameterString.substring(0, parameterString.length()-1);
		
		switch(type)
		{
		case NORMAL:
			String modifierString = "";
			if(constructor.isPublic())
				modifierString += "public ";
			else
				modifierString += "private ";
			
			s = String.format("%sfunction new(%s);", modifierString, parameterString);
			break;
		case OVERLOADEDv3:
		case OVERLOADEDv2:
			s = String.format("@:overload(function (%s) {})", parameterString);
			break;
		}
		return s;
	}
	
	private String methodString(MethodDoc method, MethodStringType type)
	{
		String s = "";
		
		String returnString = Stringifier.typeToString(method.returnType());
		
		String parameterString = "";
		for(Parameter param : method.parameters())
		{
			parameterString += String.format("%s:%s,", param.name(), Stringifier.typeToString(param.type()));
		}
		if(method.parameters().length>0)
			parameterString = parameterString.substring(0, parameterString.length()-1);
		
		switch(type)
		{
		case NORMAL:
			String modifierString = "";
			if(method.overriddenMethod() != null)
				modifierString += "override ";
			
			if(method.isStatic())
				modifierString += "static ";
			
			if(method.isPublic())
				modifierString += "public ";
			else
				modifierString += "private ";
			
			String functionName = method.name();
			if(method.typeParameters().length > 0)
			{
				functionName += "<";
				for(TypeVariable typeVar : method.typeParameters())
				{
					functionName += typeVar.simpleTypeName() + ",";
				}
				functionName = functionName.substring(0, functionName.length()-1) + ">";
			}
			
			s = String.format("%sfunction %s(%s):%s;", modifierString, functionName, parameterString, returnString);
			break;
		case OVERLOADEDv3:
		case OVERLOADEDv2:
			s = String.format("@:overload(function (%s):%s {})", parameterString, returnString);
			break;
		}
		return s;
	}

}
