/**
 * (c) 2014 dmulloy2
 */
package net.dmulloy2.swornpermissions.types;

import java.util.AbstractSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

/**
 * An implementation of {@link Set} that keeps values in the order in which they are added
 *
 * @author dmulloy2
 */

public class UniformSet<E> extends AbstractSet<E> implements Set<E>, Cloneable
{
	// Backing map
	private final LinkedHashMap<E, Object> map;

	// Dummy object
	private static final Object PRESENT = new Object();

	/**
	 * Constructs an empty UniformSet
	 */
	public UniformSet()
	{
		map = new LinkedHashMap<>();
	}

	/**
	 * Constructs a UniformSet using an existing Set
	 * <p>
	 * If the Set is a UniformSet, the order will be preserved
	 */
	public UniformSet(Set<E> set)
	{
		if (set instanceof UniformSet)
		{
			map = new LinkedHashMap<>(((UniformSet<E>) set).map);
		}
		else
		{
			map = new LinkedHashMap<>();
			addAll(set);
		}
	}

	/**
	 * Constructs a UniformSet from an existing List
	 */
	public UniformSet(List<E> list)
	{
		this();
		addAll(list);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int size()
	{
		return map.size();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isEmpty()
	{
		return map.isEmpty();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean contains(Object o)
	{
		return map.containsKey(o);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Iterator<E> iterator()
	{
		return map.keySet().iterator();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object[] toArray()
	{
		return map.keySet().toArray();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <T> T[] toArray(T[] a)
	{
		return map.keySet().toArray(a);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean add(E e)
	{
		return map.put(e, PRESENT) == null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean remove(Object o)
	{
		return map.remove(o) == PRESENT;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void clear()
	{
		map.clear();
	}
}