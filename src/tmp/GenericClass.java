package tmp;

import java.util.ArrayList;
import java.util.List;

/**
 * Hi
 * @author Zach
 *
 * @param <Type1> The first Param
 * @param <Type2> The second param
 */
public class GenericClass<Type1, Type2 extends List<Integer>> implements Runnable {
	
	public static final int SUPER_IMPORTANT_VALUE = 3;
	public static final List<String> bob = new SecondGenericClass<List<ArrayList<String>>, List<ArrayList<String>>, String>(3);
	
	public Type1 x;
	public Type2 list;
	
	
	public <T> int foo(T o, T oo)
	{
		return 3;
	}
	public <T,B, J> int foo(T o, B oo, J ooo)
	{
		return 4;
	}
	public Type1 foo(Type2 bar)
	{
		return null;
	}
	
	public Type1 foo(int bobs)
	{
		return null;
	}
	Type1 foo(float fl)
	{
		return null;
	}
	private <T> String foo(T dd)
	{
		return "";
	}
	public void foo()
	{
		
	}
	
	public static class Kool
	{
		public int hello;
		/**
		 * This method does stuff
		 */
		public void doStuff()
		{
			
		}
		/**
		 * This method does other stuff
		 * @param option1 the number of stuff to do
		 */
		public void doStuff(float option1)
		{
			
		}
	}
	
	public static enum Bob
	{
		/**
		 * Represents a positive answer
		 */
		YES,
		/**
		 * Represents a negitive answer
		 */
		NO
	}
	
	public static interface IJoeThing
	{
		public void doThing();
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		
	}

}
