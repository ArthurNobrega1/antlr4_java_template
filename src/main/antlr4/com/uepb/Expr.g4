grammar Expr;

program : statement* EOF ;

statement
    : varDeclaration
    | assignment
    | ifStatement
    | whileStatement
    | printStatement
    | inputStatement
    | block
    ;

varDeclaration : 'var' ID (ASSIGN expr)? SEMI ;

assignment : ID ASSIGN expr SEMI ;

ifStatement : 'if' LPAREN condition RPAREN block (ELSE block)? ;

whileStatement : 'while' LPAREN condition RPAREN block ;

printStatement : 'print' LPAREN (expr | STRING) RPAREN SEMI ;

inputStatement : 'input' LPAREN ID RPAREN SEMI ;

block : LBRACE statement* RBRACE ;

expr: <assoc=right> expr POW expr     # powerExpr
    | expr (MUL | DIV) expr           # mulDivExpr
    | expr (ADD | SUB) expr           # addSubExpr
    | atom                            # atomExpr
    ;

condition
    : NOT condition                   # notCond
    | condition AND condition         # andCond
    | condition OR condition          # orCond
    | expr (LT | GT | EQ | LE | GE) expr # comparisonCond
    | 'true'                          # trueCond
    | 'false'                         # falseCond
    | LPAREN condition RPAREN         # parenCond
    ;

atom
    : NUMBER
    | ID
    | LPAREN expr RPAREN
    | 'input' LPAREN ID RPAREN
    ;

POW    : '^' ;
MUL    : '*' ;
DIV    : '/' ;
ADD    : '+' ;
SUB    : '-' ;
EQ     : '==' ;
LT     : '<' ;
GT     : '>' ;
LE     : '<=' ;
GE     : '>=' ;
ASSIGN : '=' ;

AND    : 'and' ;
OR     : 'or' ;
NOT    : 'not' ;
ELSE   : 'else' ;

ID     : [a-zA-Z_][a-zA-Z0-9_]* ;
NUMBER : [0-9]+ ('.' [0-9]+)? ; 
STRING : '"' ( '""' | ~('"'|'\r'|'\n') )* '"' ;

LPAREN : '(' ;
RPAREN : ')' ;
LBRACE : '{' ;
RBRACE : '}' ;
SEMI   : ';' ;

WS     : [ \t\r\n]+ -> skip ;
