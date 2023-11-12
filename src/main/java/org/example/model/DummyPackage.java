package org.example.model;

import lombok.Data;
import lombok.ToString;

import java.time.LocalTime;

@Data
@ToString(callSuper = true)
public class DummyPackage extends NetworkPackage {
    private int delay;
    /**
     * The time when the package expires.
     * Calculated by adding the delay to the current time at the moment of receiving the package.
     */
    private LocalTime validUntil;
    /**
     * Whether the package has been processed and sent back to the server.
     */
    private boolean completed;

    public DummyPackage() {
        this.setPackageId(1);
    }
}
