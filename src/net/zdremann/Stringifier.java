package net.zdremann;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.ParameterizedType;
import com.sun.javadoc.Type;

public class Stringifier {
	public static final String DYNAMIC_NAME = "Dynamic";
	
	public static final Map<String, String> RESERVED_NAMES = _BUILD_RESERVED_NAMES();
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
		map.put("double", "Float");
		map.put("long", "haxe.Int64");
		map.put("int", "Int");
		map.put("boolean", "Bool");
		map.put("void", "Void");
		
		return map;
	}
	
	private static Map<String, String> _BUILD_RESERVED_NAMES() {
		Map<String, String> map = new HashMap<>();
		
		map.put("callback", "callback_");
		map.put("in", "_in");
		map.put("cast", "cast_");
		
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
	
	public static String reservedNameAvoid(String name)
	{
		if(RESERVED_NAMES.containsKey(name))
		{
			return RESERVED_NAMES.get(name);
		}
		else
		{
			return name.replace('$', '_');
		}
	}
	
	public static String typeToString(Type type)
	{
		String typeString = "%s";
		String name;
		if(type.dimension()!="")
		{
			int depth = type.dimension().length()/2;
			for(int i=0;i<depth;i++)
			{
				typeString = String.format(typeString, "java.NativeArray<%s>");
			}
		}
		if(TYPE_MAP.containsKey(type.qualifiedTypeName()))
		{
			name = TYPE_MAP.get(type.qualifiedTypeName());
		}
		else
		{
			String packs = type.qualifiedTypeName();
			packs = packs.substring(0, packs.length()-type.typeName().length());
			name = packs + type.typeName().replace('.', '_');
		}
		
		if(type.asParameterizedType() != null)
		{
			name += "<";
			ParameterizedType t = type.asParameterizedType();
			for(Type paramType : t.typeArguments())
			{
				name += typeToString(paramType) + ",";
			}
			if(t.typeArguments().length == 0)
			{
				ClassDoc clazz = type.asClassDoc();
				if(clazz == null)
					name += DYNAMIC_NAME + ",";
				else
				{
					int numParams = clazz.typeParameters().length;
					for(int i=0;i<numParams;i++)
					{
						name += DYNAMIC_NAME + ",";
					}
				}
			}
			name = name.substring(0, name.length()-1) + ">";
		}
		
		typeString = String.format(typeString, name);
		
		return typeString;
	}
}
