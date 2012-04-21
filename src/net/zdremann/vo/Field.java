package net.zdremann.vo;

import org.freeinternals.classfile.core.FieldInfo;

public class Field implements Accessable {
	private int accessFlags;
	private String name;
	private String type;


	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getType() {
		return type;
	}
	public void setType(String descriptor) {
		this.type = ClassVO.typeString(descriptor)[0];
	}

	@Override
	public int getAccessFlags() {
		return accessFlags;
	}
	public void setAccessFlags(int accessFlags) {
		this.accessFlags = accessFlags;
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

	public boolean isPublic()
	{
		return (accessFlags & FieldInfo.ACC_PUBLIC)!=0;
	}
	
	public boolean isStatic()
	{
		return (accessFlags & FieldInfo.ACC_STATIC) !=0;
	}

	@Override
	public String toString() {
		return (isPublic()?"public":"private")+" "+getName() + " " + getType();
	}
}
