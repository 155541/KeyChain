package revolhope.splanes.com.keystore.model;


import revolhope.splanes.com.keystore.model.enums.EnumLength;
import revolhope.splanes.com.keystore.model.enums.EnumType;

public class KeyMetadata{
    
    private EnumLength length;
    private EnumType type;
    
    public void setType(EnumType type) { this.type = type; }
    public EnumType getType() { return this.type; }
    public void setLength(EnumLength length) { this.length = length; }
    public EnumLength getLength() { return this.length; }
}