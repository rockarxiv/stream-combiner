package com.homework.streamcombiner.busobj;

import lombok.*;

import javax.xml.bind.annotation.XmlRootElement;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@XmlRootElement(name = "data")
@EqualsAndHashCode
public class Data {
    private long timestamp;
    private float amount;

    /**
     * merge amount of current data with passed data
     * @param data
     */
    public void mergeData(Data data) {
        this.amount += data.amount;
    }
}
