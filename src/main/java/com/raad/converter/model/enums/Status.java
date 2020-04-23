package com.raad.converter.model.enums;

public enum Status {

    Inactive(0),
    Active(1),
    Pending(2),
    Delete(3);

    public Integer value; // int value of each status

    Status(Integer value) {
        this.value = value;
    }

    public Status getStatus(Integer v) {
        switch (v) {
            case 0:
                return Status.Inactive;
            case 1:
                return Status.Active;
            case 2:
                return Status.Pending;
            case 3:
                return Status.Delete;
        }
        return null;
    }

    public Integer getStatus(String v) {
        switch (v) {
            case "inactive":
                return 0;
            case "active":
                return 1;
            case "pending":
                return 2;
            case "delete":
                return 3;
        }
        return -1;
    }
}
