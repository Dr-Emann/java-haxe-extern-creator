package net.zdremann;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sun.javadoc.*;

public class Stringifier {
	public static final String DYNAMIC_NAME = "Dynamic";
	
	
	public static final Map<String, String> TYPE_MAP= _BUILD_MAP();
	
	private static Map<String,String> _BUILD_MAP()
	{
		Map<String,String> map = new HashMap<String, String>(16);
		map.put("java.lang.String", "String");
		map.put("java.lang.Void", "Void");
		map.put("?", DYNAMIC_NAME);
		map.put("java.lang.Object", DYNAMIC_NAME);
		map.put("char", "Char16");
		map.put("short", "Int16");
		map.put("byte", "Int8");
		map.put("float", "Float");
		map.put("double", "Double");
		map.put("long", "haxe.Int64");
		map.put("int", "Int");
		map.put("boolean", "Bool");
		map.put("void", "Void");
		
		return map;
	}
	
	public static String makeComment(String rawComment)
	{
		String[] lines = rawComment.split("\r\n|\n");
		StringBuilder s = new StringBuilder();
		if(rawComment.length() == 0)
		{
			return "";
		}
		else if(lines.length == 1)
		{
			s.append(String.format("/** %s */%n", lines[0]));
		}
		else
		{
			s.append(String.format("/**%n"));
			for(String line : lines)
			{
				s.append(String.format(" *%s%n", line));
			}
			s.append(String.format(" */%n"));
		}
		
		return s.toString();
	}
	
	public static String typeToString(Type type)
	{
		String typeString = "%s";
		if(type.dimension()!="")
		{
			int depth = type.dimension().length()/2;
			for(int i=0;i<depth;i++)
			{
				typeString = String.format(typeString, "Array<%s>");
			}
		}
		if(TYPE_MAP.containsKey(type.qualifiedTypeName()))
			typeString = String.format(typeString, TYPE_MAP.get(type.qualifiedTypeName()));
		else
			typeString = String.format(typeString, type.qualifiedTypeName());
		
		Pattern p = Pattern.compile("\\.[A-Z]");
		Matcher m = p.matcher(typeString);
		
		m.find();
		while(m.find())
		{
			typeString = typeString.substring(0, m.start()) + "_" + typeString.substring(m.start()+1);
		}
		
		if(type.asParameterizedType() != null)
		{
			typeString += "<";
			ParameterizedType t = type.asParameterizedType();
			for(Type paramType : t.typeArguments())
			{
				typeString += typeToString(paramType) + ",";
			}
			typeString = typeString.substring(0, typeString.length()-1) + ">";
		}
		
		return typeString;
	}
}
