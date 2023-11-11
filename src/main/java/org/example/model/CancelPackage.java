package org.example.model;

import lombok.Data;

@Data
public class CancelPackage extends NetworkPackage {
    public CancelPackage() {
        this.setPackageId(2);
    }
}
