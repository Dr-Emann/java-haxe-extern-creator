package net.zdremann.compare;

import java.util.Comparator;

import net.zdremann.Stringifier;

import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.ExecutableMemberDoc;
import com.sun.javadoc.Parameter;

public class MethodComparator implements Comparator<ExecutableMemberDoc> {

	@Override
	public int compare(ExecutableMemberDoc method1, ExecutableMemberDoc method2) {
		final int before = -1;
		final int after = 1;
		
		if(method1.isPublic() && !method2.isPublic())
			return before;
		else if(!method1.isPublic() && method2.isPublic())
			return after;
		
		if(!method1.isStatic() && method2.isStatic())
			return before;
		else if(method1.isStatic() && !method2.isStatic())
			return after;
		
		
		if(method1.typeParameters().length < method2.typeParameters().length)
		{
			return before;
		}
		else if(method1.typeParameters().length > method2.typeParameters().length)
		{
			return after;
		}
		
		if(method1.parameters().length < method2.parameters().length)
		{
			return before;
		}
		else if(method1.parameters().length > method2.parameters().length)
		{
			return after;
		}
		
		for(int i=0; i<method1.parameters().length; i++)
		{
			Parameter param1 = method1.parameters()[i];
			Parameter param2 = method2.parameters()[i];
			
			String param1Type = Stringifier.typeToString(param1.type());
			String param2Type = Stringifier.typeToString(param2.type());
			
			if(param1Type != Stringifier.DYNAMIC_NAME && param2Type == Stringifier.DYNAMIC_NAME)
			{
				return before;
			}
			else if(param1Type == Stringifier.DYNAMIC_NAME && param2Type != Stringifier.DYNAMIC_NAME)
			{
				return after;
			}
			
			ClassDoc param1Class = param1.type().asClassDoc();
			ClassDoc param2Class = param2.type().asClassDoc();
			
			if(param1Class != null && param2Class != null)
			{
				if(param1Class.subclassOf(param2Class))
				{
					return before;
				}
				else if(param2Class.subclassOf(param1Class))
				{
					return after;
				}
			}
			
			if(param1Type.compareTo(param2Type) != 0)
			{
				return param1Type.compareTo(param2Type);
			}
		}
		return 0;
	}

}
