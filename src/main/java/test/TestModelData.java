/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package test;

import java.util.Arrays;

/**
 *
 * @author daniil_pozdeev
 */
public class TestModelData {
    
    public M[] columnHeaderDataSource;
    
    public TestModelData() {
        columnHeaderDataSource = new M[]{new M("ИС", new FN("Источник сигнала"), fillSourceModuleGroup(1)), new M("НС", new FN("Назначение сигнала"), fillDestinationModule(1))};
    }
    
    public static final M[] SOURCE_MODULE_TYPES = new M[] {
        new M("M6-CPU-A1",
                new M[] {
                    new M("ФК", new FN("Функциональные клавиши"),
                            new M[] {
                                new M("F1", "BOOL"),
                                new M("F2", "BOOL"),
                                new M("F3", "BOOL"),
                                new M("F4", "BOOL"),
                                new M("F5", "BOOL"),
                                new M("F6", "BOOL", 
                                    new M[] {
                                        new M("1"),
                                        new M("2_asfgesdgdshdfjhg",
                                            new M[] {
                                                new M("1"),
                                                new M("2"),
                                                new M("3")
                                            }
                                        ),
                                        new M("3",
                                            new M[] {
                                                new M("1"),
                                                new M("2"),
                                                new M("3")
                                            }
                                        )
                                    }
                                ),
                                new M("F7", "BOOL"),
                                new M("F8", "BOOL"),
                                new M("F9", "BOOL")
                            }
                    ),
                    new M("Логика"),
                    new M("61850 MMS"),
                    new M("61850 GOOSE",
                        new M[] {
                            new M("1"),
                            new M("2"),
                            new M("3")
                        }
                    ),
                    new M("Дисплей"),
                    new M("CAN2",new FN("CAN2 (из другого модуля)"))
                }
        ),
        new M("M6-8RO-16DI220", 
                new M[] {
                    new M("ДВ", new FN("Дискретные входы"),
                            new M[]{
                                new M("IN1", "BOOL"),
                                new M("IN2", "BOOL"),
                                new M("IN3", "BOOL"),
                                new M("IN4", "BOOL"),
                                new M("IN5", "BOOL"),
                                new M("IN6", "BOOL"),
                                new M("IN7", "BOOL"),
                                new M("IN8", "BOOL"),
                                new M("IN9", "BOOL"),
                                new M("IN10", "BOOL"),
                                new M("IN11", "BOOL"),
                                new M("IN12", "BOOL"),
                                new M("IN13", "BOOL"),
                                new M("IN14", "BOOL"),
                                new M("IN15", "BOOL"),
                                new M("IN16", "BOOL")
                            }
                    ),
                    new M("Логика"),
                    new M("CAN2",new FN("CAN2 (из другого модуля)"))
                }
        ),
        new M("M6-xVT-zCT", 
                new M[] {
                    new M("Логика"),
                    new M("CAN2",new FN("CAN2 (из другого модуля)"))
                }
        )
    };
    
    public static final M[] DESTINATION_MODULE_TYPES = new M[]{
        new M("M6-CPU-A1",
                new M[] {
                    new M("Логика"),
                    new M("61850 MMS"),
                    new M("61850 GOOSE"),
                    new M("Дисплей"),
                    new M("CAN2", new FN("CAN2 (в другой модуль)")),
                    new M("С", new FN("Светодиоды"),
                            new M[] {
                                new M("1", "BOOL"),
                                new M("2", "BOOL"),
                                new M("3", "BOOL"),
                                new M("4", "BOOL"),
                                new M("5", "BOOL"),
                                new M("6", "BOOL"),
                                new M("7", "BOOL"),
                                new M("8", "BOOL"),
                                new M("9", "BOOL"),
                                new M("10", "BOOL"),
                                new M("11", "BOOL"),
                                new M("12", "BOOL"),
                                new M("13", "BOOL"),
                                new M("14", "BOOL"),
                                new M("15", "BOOL"),
                                new M("16", "BOOL")
                            }
                    )
                }
        ),
        new M("M6-8RO-16DI220", 
                new M[] {
                    new M("ДВ", new FN("Дискретные входы"),
                            new M[]{
                                new M("OUT1", "BOOL"),
                                new M("OUT2", "BOOL"),
                                new M("OUT3", "BOOL"),
                                new M("OUT4", "BOOL"),
                                new M("OUT5", "BOOL"),
                                new M("OUT6", "BOOL"),
                                new M("OUT7", "BOOL"),
                                new M("OUT8", "BOOL")
                            }
                    ),
                    new M("Логика"),
                    new M("CAN2", new FN("CAN2 (в другой модуль)"))
                }
        ),
        new M("M6-xVT-zCT", 
                new M[] {
                    new M("Логика"),
                    new M("CAN2", new FN("CAN2 (в другой модуль)"))
                }
        )
    };
    
    //public M[] columnHeaderDataSource = new M[]{new M("Источник сигнала", fillSourceModuleGroup(2)), new M("Назначение сигнала", fillDestinationModule(2))};
    
    public M[] rowHeaderDataSource = new M[] {
        new M("PTOC1",
                new M[] {
                    new M("ENA", "BOOL"),
                    new M("BLOCK", "BOOL"),
                    new M("ENA_INT", "INT"),
                    new M("STR_INT", "INT"),
                    new M("OP_INT", "INT"),
                    new M("STR", "BOOL"),
                    new M("OP", "BOOL")
                }
        ),
        new M("PROTO",
                new M[] {
                    new M("I1", ""),
                    new M("I2", ""),
                    new M("I3", ""),
                    new M("I4", "")
                }
        ),
        new M("PROTO",
                new M[] {
                    new M("U1", ""),
                    new M("U2", ""),
                    new M("U3", ""),
                    new M("U4", ""),
                    new M("U5", "")
                }
        ),
        new M("PROTO",
                new M[] {
                    new M("I5", ""),
                    new M("I6", ""),
                    new M("I7", ""),
                    new M("I8", "")
                }
        )
    };
    
    public M[] fillSourceModuleGroup(int count) {
        M[] sourceModuleGroup = new M[count * SOURCE_MODULE_TYPES.length];
        for (int i = 0; i < count; i++) {
            for (int j = 0; j < SOURCE_MODULE_TYPES.length; j++) {
                sourceModuleGroup[i*SOURCE_MODULE_TYPES.length + j] = new M(SOURCE_MODULE_TYPES[j]);
                sourceModuleGroup[i*SOURCE_MODULE_TYPES.length + j].name += " [Плата №" + (i*SOURCE_MODULE_TYPES.length + j) + "]";
            }
        }
        return sourceModuleGroup;
    }
    
    public M[] fillDestinationModule(int count) {
        M[] destinationModuleGroup = new M[count * DESTINATION_MODULE_TYPES.length];
        for (int i = 0; i < count; i++) {
            for (int j = 0; j < DESTINATION_MODULE_TYPES.length; j++) {
                destinationModuleGroup[i*DESTINATION_MODULE_TYPES.length + j] = new M(DESTINATION_MODULE_TYPES[j]);
                destinationModuleGroup[i*DESTINATION_MODULE_TYPES.length + j].name += " [Плата №" + (i*DESTINATION_MODULE_TYPES.length + j) + "]";
            }
        }
        return destinationModuleGroup;
    }
    
    public static class M {

        public String name;
        public String fullName;
        public String type;
        public M[] groups;

        //Without ToolTip
        
        public M(String name, M[] groups) {
            this.name = name;
            this.groups = groups;
        }
        
        public M(String name) {
            this.name = name;
        }
        
        public M(String name, String type) {
            this.name = name;
            this.type = type;
        }
        
        public M(String name, String type, M[] groups) {
            this.name = name;
            this.type = type;
            this.groups = groups;
        }
        
        //With ToolTip
        
        public M(String name, FN fullName, M[] groups) {
            this.name = name;
            this.fullName = fullName.getFullName();
            this.groups = groups;
        }
        
        public M(String name, FN fullName) {
            this.name = name;
            this.fullName = fullName.getFullName();
        }
        
        public M(String name, FN fullName, String type) {
            this.name = name;
            this.fullName = fullName.getFullName();
            this.type = type;
        }
        
        public M(String name, FN fullName, String type, M[] groups) {
            this.name = name;
            this.fullName = fullName.getFullName();
            this.type = type;
            this.groups = groups;
        }
        
        public M(M m) {
            this.name = m.name;
            this.type = m.type;
            this.groups = m.groups;
            this.fullName = m.fullName;
        }
    }
    
    private static class FN {
        private String fullName;
        
        FN(String fullName) {
            this.fullName = fullName;
        }

        public String getFullName() {
            return fullName;
        }

        public void setFullName(String fullName) {
            this.fullName = fullName;
        }
    }
}
