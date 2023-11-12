package org.example.model;

import lombok.Data;
import lombok.ToString;

@Data
@ToString(callSuper = true)
public class CancelPackage extends NetworkPackage {
    public CancelPackage() {
        this.setPackageId(2);
    }
}
