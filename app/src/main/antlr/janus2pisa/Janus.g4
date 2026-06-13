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

stm  : ID ROP_ASS expr                                          # SimpleAssignStm
     | ID '[' expr ']' ROP_ASS expr                             # ArrAssignStm
     | ID SWAP ID                                               # SwapStm
     | 'if' expr 'then' stmBlk 'else' stmBlk 'fi' expr          # IfThenStm
     | 'from' expr 'do' stmBlk 'loop' stmBlk 'until' expr       # FromUntilStm
     | 'call' ID                                                # CallStm
     | 'uncall' ID                                              # UncallStm
     | 'skip'                                                   # SkipStm
     ;

expr : expr OP_GEN expr   # GenericOP
     | INT                # IntegerExpr
     | ID                 # VariableExpr
     | ID '[' expr ']'    # ArrayExpr
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
