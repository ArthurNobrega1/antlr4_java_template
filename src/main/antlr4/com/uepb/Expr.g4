grammar Expr;

program : statement* EOF ;

statement
    : varDeclaration
    | assignment
    | ifStatement
    | whileStatement
    | printStatement
    | inputStatement
    ;

varDeclaration : 'var' ID (ASSIGN expr)? SEMI ;

assignment : ID ASSIGN expr SEMI ;

ifStatement : 'if' LPAREN condition RPAREN LBRACE statement* RBRACE (ELSE LBRACE statement* RBRACE)? ;

whileStatement : 'while' LPAREN condition RPAREN LBRACE statement* RBRACE ;

printStatement : 'print' LPAREN (expr | STRING) RPAREN SEMI ;

inputStatement : 'input' LPAREN ID RPAREN SEMI ;

expr: <assoc=right> expr POW expr    # powerExpr
    | expr (MUL | DIV) expr          # mulDivExpr
    | expr (ADD | SUB) expr          # addSubExpr
    | atom                           # atomExpr
    ;

condition
    : condition (AND | OR) condition
    | NOT condition
    | expr (LT | GT | EQ | LE | GE) expr
    | 'true'
    | 'false'
    | LPAREN condition RPAREN
    ;

atom
    : INT
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
INT    : [0-9]+ ('.' [0-9]+)? ; 
STRING : '"' ( '""' | ~('"'|'\r'|'\n') )* '"' ;

LPAREN : '(' ;
RPAREN : ')' ;
LBRACE : '{' ;
RBRACE : '}' ;
SEMI   : ';' ;

WS     : [ \t\r\n]+ -> skip ;
