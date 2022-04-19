package org.jumpmind.pos.util.model;

public class EntryMethodCode extends AbstractTypeCode {
    private static final long serialVersionUID = 1L;
    private static final String[] DESERIALIZE_SEARCH_CLASSES = {
            "org.jumpmind.pos.item.model.EntryMethodCode",
            EntryMethodCode.class.getName()
    };

    public static final EntryMethodCode CUSTOMER_DEVICE =     new EntryMethodCode("CUSTOMER_DEVICE");
    public static final EntryMethodCode SCANNED =     new EntryMethodCode("SCANNED");
    public static final EntryMethodCode KEYED =       new EntryMethodCode("KEYED");
    public static final EntryMethodCode INQUIRY =     new EntryMethodCode("INQUIRY");
    public static final EntryMethodCode TRANSACTION = new EntryMethodCode("TRANSACTION");
    public static final EntryMethodCode OTHER =       new EntryMethodCode("OTHER");
    public static final EntryMethodCode SYSTEM =      new EntryMethodCode("SYSTEM");

    public static EntryMethodCode of(String value) {
        return AbstractTypeCode.of(EntryMethodCode.class, value);
    }
    
    private EntryMethodCode(String value) {
        super(value);
    }

    @Override
    public String[] getDeserializationSearchClasses() {
        return DESERIALIZE_SEARCH_CLASSES;
    }

}

