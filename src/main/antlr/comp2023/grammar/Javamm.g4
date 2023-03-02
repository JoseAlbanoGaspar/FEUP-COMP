grammar Javamm;

@header {
    package pt.up.fe.comp2023;
}

INT : [0-9]+ ;
ID : [a-zA-Z_][a-zA-Z_0-9]* ;

WS : [ \t\n\r\f]+ -> skip ;

/*program
    : statement+ EOF
    ;

statement
    : expression ';'
    | ID '=' INTEGER ';'
    ;

expression
    : expression op=('*' | '/') expression #BinaryOp
    | expression op=('+' | '-') expression #BinaryOp
    | value=INTEGER #Integer
    | value=ID #Identifier
    ;*/

program : (importDeclaration)* classDeclaration EOF
        ;

importDeclaration : 'import' ID ( '.' ID )* ';'
                  ;

classDeclaration : 'class' ID ( 'extends' ID )? '{' ( varDeclaration )* ( methodDeclaration )*'}'
                 ;

varDeclaration : type ID ';'
               ;

methodDeclaration : ('public')? type ID '(' ( type ID ( ',' type ID )* )? ')' '{' ( varDeclaration)* ( statement )* 'return' expression ';' '}'  #Method
                  | ('public')? 'static' 'void' 'main' '(' 'String' '[' ']' ID ')' '{' ( varDeclaration)* ( statement )* '}'                     #MainMethod
                  ;

type : 'int' '[' ']'  #ArrayType
     | 'boolean'      #BoolType
     | 'int'          #IntType
     | 'String'       #StringType
     | ID             #IDType
     ;

statement : '{' ( statement )* '}'  #BlockCode
          | 'if' '(' expression ')' statement 'else' statement  #If
          | 'while' '(' expression ')' statement  #While
          | expression ';'  #StatementExpression
          | ID '=' expression ';'  #Assignment
          | ID '[' expression ']' '=' expression ';'  #Array
          ;

expression : '!' expression  #Negation
           | '(' expression ')'  #Parenthesis
           | expression op=('*' | '/') expression #Multiplicative
           | expression op=('+' | '-') expression #Additive
           | expression  '<' expression #Compare
           | expression '&&' expression #LogicalAnd
           | expression '[' expression ']' #SquareBrackets
           | expression '.' 'length' #Length
           | expression '.' ID '(' ( expression ( ',' expression )* )? ')' #FunctionCall
           | 'new' 'int' '[' expression ']'  #NewArray
           | 'new' ID '(' ')' #NewClass
           | INT   #Value
           | 'true'  #Value
           | 'false' #Value
           | ID      #Value
           | 'this'  #Value
           ;
