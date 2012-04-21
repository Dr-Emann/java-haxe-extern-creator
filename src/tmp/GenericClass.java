package tmp;

import java.util.List;

public class GenericClass<Param1, Param2 extends List<String>> {
	
	public Param1 x;
	public Param2 list;
	
	public Param1 foo(Param2 bar)
	{
		return null;
	}
	
	public static class Kool
	{
		public int hello;
	}

}
