package org.example.model;

import lombok.Data;

import java.io.Serializable;

@Data
public abstract class NetworkPackage implements Serializable {
    private int packageId;
    private int length;
    private int id;
}
