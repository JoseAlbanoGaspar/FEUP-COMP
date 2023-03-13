grammar Javamm;

@header {
    package pt.up.fe.comp2023;
}

INT : [0-9]+ ;
ID : [a-zA-Z_][a-zA-Z_0-9]* ;

WS : [ \t\n\r\f]+ -> skip ;

program : (importDeclaration)* classDeclaration EOF
        ;

importDeclaration : 'import' packageNames+=ID ( '.' packageNames+=ID )* ';'
                  ;

classDeclaration : 'class' name=ID ( 'extends' superName=ID )? '{' ( varDeclaration )* ( instanceMethodDeclaration )* (mainMethodDeclaration)? (instanceMethodDeclaration)*'}'
                 ;

varDeclaration : t=type var=ID ';'
               ;

mainMethodDeclaration : ('public')? 'static' 'void' 'main' '(' type '[' ']' arg=ID ')' '{' ( varDeclaration)* ( statement )* '}'                     #MainMethod
                      ;

instanceMethodDeclaration :  ('public')? type name=ID '(' (arguments)? ')' '{' ( varDeclaration)* ( statement )* ('return' expression ';')? '}'  #Method
                          ;
arguments : types+=type args+=ID (',' types+=type args+=ID)*  #MethodArgs
          ;


type locals[boolean isArray=false] : 'int' ('[' ']'{$isArray = true;})?  #IntType
     | 'boolean'      #BoolType
     | typeName=ID    #IDType
     ;

statement : '{' ( statement )* '}'  #BlockCode
          | 'if' '(' expression ')' statement 'else' statement  #If
          | 'while' '(' expression ')' statement  #While
          | expression ';'  #StatementExpression
          | var=ID '=' expression ';'  #Assignment
          | var=ID '[' expression ']' '=' expression ';'  #Array
          ;

expression : '!' expression  #Not
           | '(' expression ')'  #Parenthesis
           | expression op=('*' | '/') expression #BinaryOp
           | expression op=('+' | '-') expression #BinaryOp
           | expression  '<' expression #Compare
           | expression '&&' expression #LogicalAnd
           | expression '[' expression ']' #SquareBrackets
           | expression '.' 'length' #Length
           | expression '.' methodName=ID '(' ( expression ( ',' expression )* )? ')' #FunctionCall
           | 'new' 'int' '[' expression ']'  #NewArray
           | 'new' className=ID '(' ')' #NewClass
           | value=INT   #Integer
           | value=('true' | 'false')  #BoolLiteral
           | value=ID      #Identifier
           | 'this'  #This
           ;
