grammar Janus;

@header {
package janus2pisa;
}

// ------------------- PARSER RULES -------------------

prog  : dec* proc+ EOF ;

proc  : 'procedure' ID stmBlk;

dec   : simpleDec
      | arrayDec
      ;

simpleDec : ID ;

arrayDec  : ID '[' INT ']' ;

stmBlk : stm+ ;

stm  : ID ROP_ASS expr
     | ID '[' expr ']' ROP_ASS expr
     | ID SWAP ID
     | 'if' expr 'then' stmBlk 'else' stmBlk 'fi' expr
     | 'from' expr 'do' stmBlk 'loop' stmBlk 'until' expr
     | 'call' ID
     | 'uncall' ID
     | 'skip'
     ;

expr : expr OP_GEN expr
     | INT
     | ID
     | ID '[' expr ']'
     ;

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
