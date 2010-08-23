grammar SecurityPolicy;

options {
	output = AST;
	backtrack=true;
}

tokens {
	GROUP;
	ROLE;
	LIST;
	ALLOW;
	REFERENCE;
	FQN;
	WORLD;
	VERSION;
}

@header {
	package org.opencraft.server.security;
}

@lexer::header {
	package org.opencraft.server.security;
}

policy:	version block* EOF!;

version:	'VERSION' INTEGER -> ^(VERSION INTEGER);

block	:	(policyBlock | world);

policyBlock
	:	(collectionBlock | grant);

collectionBlock
	:	(group | role );

group:	'GROUP' name=ID groupList -> ^(GROUP $name groupList?);

groupList
	:	LBRACE! player* RBRACE!;

role	:	'ROLE' ID roleList -> ^(ROLE ID roleList?);

roleList	:	LBRACE! permission* RBRACE!;

grant	:	(allow );

permission:	(fqn | reference);

player	:	ID^ | STRING^ | reference;

allow	:	'ALLOW' permission 'to' player -> ^(ALLOW permission player);

world	:	'WORLD' worldName LBRACE policyBlock+ RBRACE -> ^(WORLD worldName policyBlock+);

worldName
	:	(ID^ | STRING^);

reference
	:	'@' ID -> ^(REFERENCE ID);

fqn	:	(e+=ID ('.' (e+=ID | e+='*'))* | e+='*') -> ^(FQN $e);

ID  :	('a'..'z'|'A'..'Z'|'_'|'-') ('a'..'z'|'A'..'Z'|'0'..'9'|'_'|'-')*
    ;

COMMENT
    :   '//' ~('\n'|'\r')* '\r'? '\n' {$channel=HIDDEN;}
    |   '/*' ( options {greedy=false;} : . )* '*/' {$channel=HIDDEN;}
    ;

WS  :   ( ' '
        | '\t'
        | '\r'
        | '\n'
        ) {$channel=HIDDEN;}
    ;
    
LBRACE	:	'{';
RBRACE	:	'}';

STRING
    :  '"' ( ESC_SEQ | ~('\\\\'|'"') )* '"'
    ;

INTEGER	:	('0'..'9')+;

fragment
HEX_DIGIT : ('0'..'9'|'a'..'f'|'A'..'F') ;

fragment
ESC_SEQ
    :   '\\\\' ('b'|'t'|'n'|'f'|'r'|'\"'|'\''|'\\\\')
    |   UNICODE_ESC
    |   OCTAL_ESC
    ;

fragment
OCTAL_ESC
    :   '\\\\' ('0'..'3') ('0'..'7') ('0'..'7')
    |   '\\\\' ('0'..'7') ('0'..'7')
    |   '\\\\' ('0'..'7')
    ;

fragment
UNICODE_ESC
    :   '\\\\' 'u' HEX_DIGIT HEX_DIGIT HEX_DIGIT HEX_DIGIT
    ;
