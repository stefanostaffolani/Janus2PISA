grammar RIL;

@header {
package janus2pisa;
}

// ------------------- PARSER RULES -------------------

prog : block* EOF ;

block : entry body exit ;

entry : LABEL '<-' | LABEL ';' LABEL '<-' condition | 'begin' LABEL ;

exit : '->' LABEL | condition '->' LABEL ';' LABEL | 'end' LABEL ;

body : stmt* | call ;

call : 'call' LABEL | 'uncall' LABEL ;

stmt : LVAL ROP_ASS RVAL (OP_GEN RVAL)? | LVAL '<->' LVAL ;

condition : LVAL OP_COM RVAL ;

LVAL : ID | 'M[' ID ']' ;

RVAL : LVAL | INT ;

// ------------------- LEXER RULES -------------------

ROP_ASS : ('+' | '-' | '^') '=' ;

OP_GEN  : (OP_MUL | OP_ADD | OP_COM);

OP_MUL  : '*'
        | '/'
        | '*/'
        ;

OP_ADD  : '+'
        | '-'
        | '^'
        ;

SWAP    : '<=>' ;

OP_COM  : '<='
        | '>='
        | '<'
        | '>'
        | '='
        | '!='
        | '&&'
        | '||'
        | '&'
        | '|'
        ;

ID      : [a-zA-Z_][a-zA-Z0-9_]* ;

INT     : [0-9]+ ;

WS      : [ \t\r\n]+ -> skip ;
