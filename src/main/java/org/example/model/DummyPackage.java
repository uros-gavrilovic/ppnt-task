package org.example.model;

import lombok.Data;
import lombok.ToString;

import java.time.LocalTime;

@Data
@ToString(callSuper = true)
public class DummyPackage extends NetworkPackage {
    private int delay;
    private LocalTime validUntil;
    private boolean completed;

    public DummyPackage() {
        this.setPackageId(1);
    }
}
