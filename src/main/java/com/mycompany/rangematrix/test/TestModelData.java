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
    //List<M[]> columnHeaderData;
    
    
    public TestModelData(int column, int row) {
        this.column = column;
        this.row = row;
        //columnHeaderData = new ArrayList();
    }

    M[][] columnHeaderData = new M[][] {sourceModule, destinationModule};
    
       
    public static M[] sourceModule = new M[]{
        new M("M6-CPU-A1 [Плата №1]",
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
        new M("M6-8RO-16DI220 [Плата №3]", 
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
        new M("M6-xVT-zCT [Плата №4]", 
                new G[] {
                    new G("Логика"),
                    new G("CAN2 (из другого модуля)")
                }
        )
    };
    
    public static M[] destinationModule = new M[]{
        new M("M6-CPU-A1 [Плата №1]",
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
        new M("M6-8RO-16DI220 [Плата №3]", 
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
        new M("M6-xVT-zCT [Плата №4]", 
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
