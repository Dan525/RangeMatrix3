package com.mycompany.rangematrix.test;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author oleg_kirienko
 */
public class TestModelData {
    
    int column;
    int row;
    M[][] columnHeaderData;
    
    
    public TestModelData(int column, int row) {
        this.column = column;
        this.row = row;
        columnHeaderData = new M[][] {fillSourceModule(column), fillDestinationModule(column)};
    }

    public M[][] getColumnHeaderData() {
        return columnHeaderData;
    }
    
    public M[] fillSourceModule(int count) {
        M[] sourceModule = new M[count * sourceModuleTypes.length];
        for (int i = 0; i < count; i++) {
            for (int j = 0; j < sourceModuleTypes.length; j++) {
                sourceModule[i*sourceModuleTypes.length + j] = sourceModuleTypes[j];
                sourceModule[i*sourceModuleTypes.length + j].name += " [Плата №" + (i*sourceModuleTypes.length + j) + "]";
            }
        }
        return sourceModule;
    }
    
    public M[] fillDestinationModule(int count) {
        M[] destinationModule = new M[count * destinationModuleTypes.length];
        for (int i = 0; i < count; i++) {
            for (int j = 0; j < destinationModuleTypes.length; j++) {
                destinationModule[i*destinationModuleTypes.length + j] = destinationModuleTypes[j];
                destinationModule[i*destinationModuleTypes.length + j].name += " [Плата №" + (i*destinationModuleTypes.length + j) + "]";
            }
        }
        return destinationModule;
    }
    
    public static M[] sourceModuleTypes = new M[]{
        new M("M6-CPU-A1",
                new G[] {
                    new G("Функциональные клавиши",
                            new S[] {
                                new S("F1", "BOOL"),
                                new S("F2", "BOOL"),
                                new S("F3", "BOOL"),
                                new S("F4", "BOOL"),
                                new S("F5", "BOOL"),
                                new S("F6", "BOOL"),
                                new S("F7", "BOOL"),
                                new S("F8", "BOOL"),
                                new S("F9", "BOOL")
                            }
                    ),
                    new G("Логика"),
                    new G("61850 MMS"),
                    new G("61850 GOOSE"),
                    new G("Дисплей"),
                    new G("CAN2 (из другого модуля)")
                }
        ),
        new M("M6-8RO-16DI220", 
                new G[] {
                    new G("Дискретные входы",
                            new S[]{
                                new S("IN1", "BOOL"),
                                new S("IN2", "BOOL"),
                                new S("IN3", "BOOL"),
                                new S("IN4", "BOOL"),
                                new S("IN5", "BOOL"),
                                new S("IN6", "BOOL"),
                                new S("IN7", "BOOL"),
                                new S("IN8", "BOOL"),
                                new S("IN9", "BOOL"),
                                new S("IN10", "BOOL"),
                                new S("IN11", "BOOL"),
                                new S("IN12", "BOOL"),
                                new S("IN13", "BOOL"),
                                new S("IN14", "BOOL"),
                                new S("IN15", "BOOL"),
                                new S("IN16", "BOOL")
                            }
                    ),
                    new G("Логика"),
                    new G("CAN2 (из другого модуля)")
                }
        ),
        new M("M6-xVT-zCT", 
                new G[] {
                    new G("Логика"),
                    new G("CAN2 (из другого модуля)")
                }
        )
    };
    
    public static M[] destinationModuleTypes = new M[]{
        new M("M6-CPU-A1",
                new G[] {
                    new G("Логика"),
                    new G("61850 MMS"),
                    new G("61850 GOOSE"),
                    new G("Дисплей"),
                    new G("CAN2 (в другой модуль)"),
                    new G("Светодиоды",
                            new S[] {
                                new S("1", "BOOL"),
                                new S("2", "BOOL"),
                                new S("3", "BOOL"),
                                new S("4", "BOOL"),
                                new S("5", "BOOL"),
                                new S("6", "BOOL"),
                                new S("7", "BOOL"),
                                new S("8", "BOOL"),
                                new S("9", "BOOL"),
                                new S("10", "BOOL"),
                                new S("11", "BOOL"),
                                new S("12", "BOOL"),
                                new S("13", "BOOL"),
                                new S("14", "BOOL"),
                                new S("15", "BOOL"),
                                new S("16", "BOOL")
                            }
                    )
                }
        ),
        new M("M6-8RO-16DI220", 
                new G[] {
                    new G("Дискретные выходы",
                            new S[]{
                                new S("OUT1", "BOOL"),
                                new S("OUT2", "BOOL"),
                                new S("OUT3", "BOOL"),
                                new S("OUT4", "BOOL"),
                                new S("OUT5", "BOOL"),
                                new S("OUT6", "BOOL"),
                                new S("OUT7", "BOOL"),
                                new S("OUT8", "BOOL")
                            }
                    ),
                    new G("Логика"),
                    new G("CAN2 (в другой модуль)")
                }
        ),
        new M("M6-xVT-zCT", 
                new G[] {
                    new G("Логика"),
                    new G("CAN2 (в другой модуль)")
                }
        )
    };

    
    private static class M {

        public String name;
        public G[] groups;

        public M(String name, G[] groups) {
            this.name = name;
            this.groups = groups;
        }

    }

    private static class G {

        public String name;
        public S[] signals;

        public G(String name, S[] signals) {
            this.name = name;
            this.signals = signals;
        }
        
        public G(String name) {
            this.name = name;
        }

    }

    private static class S {

        public String name;
        public String type;

        public S(String name, String type) {
            this.name = name;
            this.type = type;
        }
    }
}
