/**
 * (c) 2014 dmulloy2
 */
package net.dmulloy2.swornpermissions.types;

import net.dmulloy2.util.MaterialUtil;

import org.bukkit.Material;
import org.bukkit.material.MaterialData;

/**
 * @author dmulloy2
 */

public class MyMaterial
{
	private Material mat;
	private MaterialData dat;

	public MyMaterial(Material mat, MaterialData dat)
	{
		this.mat = mat;
		this.dat = dat;
	}

	public MyMaterial(Material mat)
	{
		this(mat, null);
	}

	/**
	 * @deprecated Magic value
	 */
	@Deprecated
	public MyMaterial(Material mat, byte dat)
	{
		this.mat = mat;
		this.dat = new MaterialData(mat, dat);
	}

	/**
	 * @deprecated Magic value
	 */
	@Deprecated
	public MyMaterial(int id, byte dat)
	{
		this(MaterialUtil.getMaterial(id), dat);
	}

	/**
	 * @deprecated Magic value
	 */
	@Deprecated
	public MyMaterial(int id)
	{
		this(MaterialUtil.getMaterial(id));
	}

	public Material getMaterial()
	{
		return mat;
	}

	public MaterialData getData()
	{
		return dat;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (obj == this)
			return true;

		if (obj == null)
			return false;

		if (obj instanceof MyMaterial)
		{
			MyMaterial other = (MyMaterial) obj;
			return mat == other.mat && dat == null ? other.dat == null : dat.equals(other.dat);
		}

		return false;
	}

	@Override
	public int hashCode()
	{
		int prime = 31;

		int result = 1;
		result = prime * result + dat.hashCode();
		result = prime * result + (mat == null ? 0 : mat.hashCode());

		return result;
	}

	@Override
	@SuppressWarnings("deprecation")
	public String toString()
	{
		return mat.toString().toLowerCase() + (dat.getData() != 0 ? ":" + dat.getData() : "");
	}

	@SuppressWarnings("deprecation") // Material Data
	public static MyMaterial fromString(String string)
	{
		if (string.contains(":"))
		{
			String[] split = string.split(":");
			Material mat = MaterialUtil.getMaterial(split[0]);
			MaterialData dat = new MaterialData(mat, Byte.parseByte(split[1]));
			return new MyMaterial(mat, dat);
		}

		Material mat = MaterialUtil.getMaterial(string);
		return new MyMaterial(mat);
	}
}