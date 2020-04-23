package com.raad.converter.model.enums;

public enum Role {

    SUPER_ADMIN(0),
    ADMIN(1),
    USER(2);

    public Integer value; // int value of each status

    Role(Integer value) { this.value = value; }

    public Role getRole(Integer v) {
        switch (v) {
            case 0:
                return Role.SUPER_ADMIN;
            case 1:
                return Role.ADMIN;
            case 2:
                return Role.USER;
        }
        return null;
    }

    public Integer getRole(String v) {
        switch (v) {
            case "SUPER_ADMIN":
                return 0;
            case "ADMIN":
                return 1;
            case "USER":
                return 2;
        }
        return -1;
    }

    public Integer getValue() { return value; }
    public void setValue(Integer value) { this.value = value; }

}
