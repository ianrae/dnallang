package org.dval;

import org.dval.oldbuilder.XStructValueBuilder;
import org.dval.repository.MyWorld;

public class PersonHelper extends BaseDValTest {
	
	public PersonHelper(DTypeRegistry registry, MyWorld world) {
		this.registry = registry;
		this.world = world;
	}
	
	//-----
	public DStructType buildAddressType(DTypeRegistry registry) {
		DType eltype = registry.getType(BuiltInTypes.STRING_SHAPE);
        OrderedMap fieldMap = new OrderedMap();
		fieldMap.add("code", eltype, false, false);
		fieldMap.add("field2", eltype, false, false);
		DStructType type = new DStructType(Shape.STRUCT, "Address", null, fieldMap);
		registry.add("Address", type);
		world.typeRegistered(type);
		return type;
	}
	public DStructType buildPersonType(DTypeRegistry registry, DType refType) {
		DType eltype = registry.getType(BuiltInTypes.STRING_SHAPE);
        OrderedMap fieldMap = new OrderedMap();
		fieldMap.add("field1", eltype, false, false);
		fieldMap.add("field2", eltype, false, false);
		fieldMap.add("address", refType, false, false);
		DStructType type = new DStructType(Shape.STRUCT, "Person", null, fieldMap);
		registry.add("Person", type);
		world.typeRegistered(type);
		return type;
	}
	
	public DValue buildAddress(DTypeRegistry registry, DStructType addrType) {
		XStructValueBuilder builder = new XStructValueBuilder(addrType);
		builder.addField("code", buildStringVal(registry, "101"));
		builder.addField("field2", buildStringVal(registry, "abc"));
		builder.finish();
		chkErrors(builder, 0);
		DValue addr = builder.getDValue();
		return addr;
	}
	public DValue buildPerson(DTypeRegistry registry, DStructType personType, DValue dref) {
		XStructValueBuilder builder = new XStructValueBuilder(personType);
		builder.addField("field1", buildStringVal(registry, "bom"));
		builder.addField("field2", buildStringVal(registry, "smith"));
		builder.addField("address", dref);
		builder.finish();
		chkErrors(builder, 0);
		DValue person = builder.getDValue();
		return person;
	}
}
