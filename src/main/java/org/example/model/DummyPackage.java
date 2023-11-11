package org.example.model;

import lombok.Data;
import lombok.ToString;

@Data
@ToString(callSuper = true)
public class DummyPackage extends NetworkPackage {
    private int delay;
}
