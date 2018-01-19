package revolhope.splanes.com.keystore.model;

import java.io.Serializable;

import revolhope.splanes.com.keystore.model.enums.EnumLength;
import revolhope.splanes.com.keystore.model.enums.EnumType;
import revolhope.splanes.com.keystore.model.interfaces.HeaderVisitor;

/**
 * Created by splanes on 15/1/18.
 **/

public class Content extends Header implements Serializable{

    private String plainUser;
    private String plainBrief;
    private String plainKey;
    private EnumType enumType;
    private EnumLength enumLength;

    public String getPlainUser() {
        return plainUser;
    }

    public void setPlainUser(String plainUser) {
        this.plainUser = plainUser;
    }

    public String getPlainBrief() {
        return plainBrief;
    }

    public void setPlainBrief(String plainBrief) {
        this.plainBrief = plainBrief;
    }

    public String getPlainKey() {
        return plainKey;
    }

    public void setPlainKey(String plainKey) {
        this.plainKey = plainKey;
    }

    public EnumType getEnumType() {
        return enumType;
    }

    public void setEnumType(EnumType enumType) {
        this.enumType = enumType;
    }

    public EnumLength getEnumLength() {
        return enumLength;
    }

    public void setEnumLength(EnumLength enumLength) {
        this.enumLength = enumLength;
    }

    @Override
    public <T> T checkClass(HeaderVisitor<T> visitor) {
        return visitor.isContent(this);
    }

    @Override
    public String toString() {

        return    "NAME.........-> " + plainName + "\n"
                + "USER.........-> " + plainUser + "\n"
                + "BRIEF........-> " + plainBrief + "\n"
                + "KEY..........-> " + plainKey + "\n"
                + "TYPE.........-> " + enumType + "\n"
                + "LENGTH.......-> " + enumLength + "\n"
                + "PARENT ID....-> " + parentId;
    }
}
