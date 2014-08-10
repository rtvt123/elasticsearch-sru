package org.xbib.query.cql;

interface CQLTokens {
    int ENDINPUT = 0;
    int AND = 1;
    int EQ = 2;
    int EXACT = 3;
    int FLOAT = 4;
    int GE = 5;
    int GT = 6;
    int INTEGER = 7;
    int LE = 8;
    int LPAR = 9;
    int LT = 10;
    int NAMEDCOMPARITORS = 11;
    int NE = 12;
    int NL = 13;
    int NOT = 14;
    int OR = 15;
    int PROX = 16;
    int QUOTEDSTRING = 17;
    int RPAR = 18;
    int SIMPLESTRING = 19;
    int SLASH = 20;
    int SORTBY = 21;
    int error = 22;
}
