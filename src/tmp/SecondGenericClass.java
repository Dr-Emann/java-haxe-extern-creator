package tmp;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class SecondGenericClass<Param1 extends List<ArrayList<String>>, Param2 extends Param1, Param3> implements List<String> { //extends GenericClass<Param1, ArrayList<String>> implements List<String>{
	
	public SecondGenericClass(int param1)
	{
		
	}
	
	public GenericClass.Kool bob;
	
	public int foobar(String inStr, long[] arrs)
	{
		return 1;
	}
	public String foobar()
	{
		return "hi";
	}
	
	public String bobby(List<? super String> bob)
	{
		return "yo";
	}
	
	
	public List<String> bar;
	//@Override
	public Param1 foo(ArrayList<String> bar) {
		return null;
	}
	@Override
	public boolean add(String e) {
		// TODO Auto-generated method stub
		return false;
	}
	@Override
	public void add(int index, String element) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public boolean addAll(Collection<? extends String> c) {
		// TODO Auto-generated method stub
		return false;
	}
	@Override
	public boolean addAll(int index, Collection<? extends String> c) {
		// TODO Auto-generated method stub
		return false;
	}
	@Override
	public void clear() {
		// TODO Auto-generated method stub
		
	}
	@Override
	public boolean contains(Object o) {
		// TODO Auto-generated method stub
		return false;
	}
	@Override
	public boolean containsAll(Collection<?> c) {
		// TODO Auto-generated method stub
		return false;
	}
	@Override
	public String get(int index) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public int indexOf(Object o) {
		// TODO Auto-generated method stub
		return 0;
	}
	@Override
	public boolean isEmpty() {
		// TODO Auto-generated method stub
		return false;
	}
	@Override
	public Iterator<String> iterator() {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public int lastIndexOf(Object o) {
		// TODO Auto-generated method stub
		return 0;
	}
	@Override
	public ListIterator<String> listIterator() {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public ListIterator<String> listIterator(int index) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public boolean remove(Object o) {
		// TODO Auto-generated method stub
		return false;
	}
	@Override
	public String remove(int index) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public boolean removeAll(Collection<?> c) {
		// TODO Auto-generated method stub
		return false;
	}
	@Override
	public boolean retainAll(Collection<?> c) {
		// TODO Auto-generated method stub
		return false;
	}
	@Override
	public String set(int index, String element) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public int size() {
		// TODO Auto-generated method stub
		return 0;
	}
	@Override
	public List<String> subList(int fromIndex, int toIndex) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public Object[] toArray() {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public <T> T[] toArray(T[] a) {
		// TODO Auto-generated method stub
		return null;
	}
}
