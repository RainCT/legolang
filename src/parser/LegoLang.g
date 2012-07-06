/**
 * Copyright © 2012 Siegfried-A. Gevatter Pujals <siegfried@gevatter.com>
 * Copyright © 2012 Gerard Canal Camprodon <grar.knal@gmail.com>
 *
 * Permission to use, copy, modify, and/or distribute this software for
 * any purpose with or without fee is hereby granted, provided that the
 * above copyright notice and this permission notice appear in all copies.
 *
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
 * ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN ACTION
 * OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR IN
 * CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
*/

grammar LegoLang;

options {
	output = AST;
	//k = 2;
}

// Imaginary tokens
tokens {
	BEGIN;		// Signals entry into a new indentation level / scope
	END;	 	// Signals exit of a previous block
	DEFAULTS;	// List of default values
	GLOBALS;	//List of global variables
	LIST_INSTR;	// List of instructions
	DECL;		// Variable declaration
	PARAMS;		// Function parameters
	PARAM;		// Function parameter
	FUNCALL;	// Function call
	ARGS;		// Arguments for a function call
	ARRAY;		// An access to a value in an array
	ARRAY_INIT;	// An array initializer (eg. [1, 2, 3])
	LITERAL;	// A fixed value in source code
	PREINCR;	// Pre-increment
	PREDECR;	// Pre-decrement
	POSTINCR;	// Post-increment
	POSTDECR;	// Post-decrement
	PLUSEQ; 	// +=
	MINUSEQ;	// -=
	MULEQ;		// *=
	DIVEQ;		// /=
	MODEQ;		// %=
}

@header {
	package parser;
	import interp.CustomTree;
}

@lexer::header {
	package parser;
}

@lexer::members {
	private int indentationLevel = 0;
	private int indentationType = -1;

	// Fix so a symbol can emit more than one tokens:

	java.util.Queue<Token> tokens = new java.util.LinkedList<Token>();

	@Override
	public void emit(Token token) {
		state.token = token;
		tokens.offer(token);
	}

	@Override
	public Token nextToken() {
		super.nextToken();
		return tokens.isEmpty() ? Token.EOF_TOKEN : tokens.poll();
	}

	// Add lexer error tracking

	private int numLexerErrors = 0;

	@Override
	public void reportError(RecognitionException e) {
		super.reportError(e);
		numLexerErrors++;
	}

	@Override
	public int getNumberOfSyntaxErrors() {
		return numLexerErrors;
	}
}

@members {
	private int inAloop = 0;
	private boolean inAsignal = false;
}

prog		:	defaults globals block_instr EOF!
			;

defaults	:	(NEWLINE? DEFAULT SENSOR_TYPE SENSOR_PORT)*
				-> ^(DEFAULTS ^(DEFAULT SENSOR_TYPE SENSOR_PORT)*)
			;

globals		:	(NEWLINE? GLOBAL declarevar)* -> ^(GLOBALS ^(GLOBAL declarevar)*)
			;

block_instr	:	instruction (NEWLINE instruction)*
				-> ^(LIST_INSTR instruction+)
			;

subscope	:	NEWLINE! BEGIN! block_instr END!
			|	instruction -> ^(LIST_INSTR instruction)
			;

instruction	:	declarevar
			|	fundef
			|	signaldef
			|	(identifier (PLUS|MINUS|MUL|DIV|MOD)? ASSIGN) => assign
			|	conditional
			|	while_stmt
			|	foreach_stmt
			|	for_stmt
			| 	break_stmt
			|	PASS
			|	(identifier args) => funcall
			|	print
			|	delete
			|	return_stmt
			| 	(identifier INCR | INCR) => increment
			|	(identifier DECR | DECR) => decrement
			|	// Nothing
			;

declarevar	:	type ID (ASSIGN expr)? -> ^(DECL type ID expr?) //^(ASSIGN ID expr)?
			;

parameter	:	declarevar -> ^(PARAM declarevar)
			;

assign		:	(identifier ASSIGN) => identifier ASSIGN^ expr
			|	(identifier PLUS ASSIGN) => identifier p=PLUS ASSIGN expr -> ^(PLUSEQ[$p] identifier expr)
			|	(identifier MINUS ASSIGN) => identifier m=MINUS ASSIGN expr -> ^(MINUSEQ[$m] identifier expr)
			|	(identifier MUL ASSIGN) => identifier m=MUL ASSIGN expr -> ^(MULEQ[$m] identifier expr)
			|	(identifier DIV ASSIGN) => identifier d=DIV ASSIGN expr -> ^(DIVEQ[$d] identifier expr)
			|	(identifier MOD ASSIGN) => identifier m=MOD ASSIGN expr -> ^(MODEQ[$m] identifier expr)
			;

increment	:	INCR identifier -> ^(PREINCR identifier) 
			|	identifier p=INCR -> ^(POSTINCR identifier)
			;

decrement	:	DECR identifier -> ^(PREDECR identifier)
			|	identifier DECR -> ^(POSTDECR identifier)
			;

fundef		:	DEF ID paramlist (RETURNS type)? COLON subscope
				-> ^(DEF ID paramlist subscope type?)
			;

signaldef	:	ON_SIGNAL ID (LPAREN SENSOR_PORT RPAREN)? COLON {inAsignal = true;}subscope {inAsignal = false;}
				-> ^(ON_SIGNAL ID subscope SENSOR_PORT?)
		;

paramlist	:	LPAREN (parameter (COMMA parameter)*)? RPAREN -> ^(PARAMS (parameter+)?);

conditional	:	IF^ expr COLON! subscope
				((NEWLINE ELSE) => NEWLINE! ELSE! cond_else)?
			;

cond_else	:	conditional -> ^(LIST_INSTR conditional)
			|	COLON! subscope
			;

while_stmt	:	WHILE^ expr COLON! {++inAloop;} subscope {--inAloop;}
			;

foreach_stmt:	FOREACH ID IN ID COLON
				{++inAloop;} subscope {--inAloop;}
				-> ^(FOREACH ID ID subscope)
			;
		
for_stmt	:	FOR ID IN expr RANGE expr COLON
				{++inAloop;} subscope {--inAloop;}
				-> ^(FOR ID expr expr subscope)
			;		
			
break_stmt	:	BREAK {if (inAloop == 0) throw new RecognitionException(input);}
			;
			catch [RecognitionException re] {
				reportError(re);
				emitErrorMessage("Syntax error: break statement out of a loop");
			}	

funcall		:	ID args -> ^(FUNCALL ID args)
			;

args		:	LPAREN (expr (COMMA expr)*)? RPAREN -> ^(ARGS (expr+)?)
			;

print		:	PRINT expr (AT numexpr (COMMA numexpr)?)? -> ^(PRINT expr ^(AT numexpr+)?)
			;

delete		:	DELETE^ identifier	// for arrays
			;

return_stmt	:	RETURN^ expr?
			;

type		:	basic_type
			|	basic_type T_ARRAY^
			;

basic_type	:	T_INT | T_BOOL | T_FLOAT | T_STRING | T_COLOR | T_SENSOR | T_MOTOR | T_BUTTON
			;

expr		:	boolexpr (OR^ boolexpr)*;

boolexpr	:	boolfact (AND^ boolfact)*;

boolfact 	:	numexpr ((LT^ | LE^ | GT^ | GE^ | EQ^ | NEQ^) numexpr)?;

numexpr 	:	term (PLUS^ term | MINUS^ term)*;

term		:	factor ((MUL^ | DIV^ | MOD^) factor)*;

factor		:	(NOT^ | PLUS^ | MINUS^)? atom;

atom 		:	literal -> ^(LITERAL literal)
			|	named_atom
			| 	LPAREN! expr RPAREN!
			|	funcall
			;

named_atom	:	(identifier INCR | INCR) => increment
			|	(identifier DECR | DECR) => decrement
			|	LENGTH identifier -> ^(LENGTH ^(ARRAY identifier))
			|	identifier
			|	array_init
			|	signal_data
			;

identifier	:	ID
			|	ID LARRAY expr RARRAY -> ^(ARRAY["ARRAY[?]"] ID expr) // name[x]
			|	ID T_ARRAY -> ^(ARRAY["ARRAY[]"] ID) // name[] - ie. append
			;
	
signal_data	:	SIGNAL_DATA {if (!inAsignal) throw new RecognitionException(input);}
			;
			catch [RecognitionException re] {
				reportError(re);
				emitErrorMessage("Syntax error: 'signal_data' variable used out of a signal.");
			}

array_init	:	basic_type LARRAY
					(NEWLINE* expr (COMMA NEWLINE* expr | NEWLINE)* (COMMA NEWLINE*)?)? RARRAY
					-> ^(ARRAY_INIT basic_type expr*)
			|	basic_type T_ARRAY -> ^(ARRAY_INIT basic_type) // fix for [] without spaces
			;

literal		:	INT
			|	FLOAT
			|	STRING
			|	BOOL
			|   COLOR
			|	BUTTON
			|	MOTOR
			|	SENSOR_PORT	
//			|	array_init
			;

// Basic tokens
GLOBAL		:	'global';
ASSIGN 		: 	'=' ;
BOOL		:	'true' | 'false';
LPAREN		:	'(';
RPAREN		:	')';
LARRAY		:	'[';
RARRAY		:	']';
COMMA		:	',';
RANGE		:	'..';
INT			:	NUMBER ('e' '-'? NUMBER)?;
FLOAT		:	(NUMBER '.' NUMBER /*| NUMBER '.' | '.' NUMBER*/)('e' '-'? NUMBER)?;
fragment 
NUMBER		:	'0'..'9'+ ;
STRING 		:	'\"'(ESC_SEQ | ~('\"' | '\\') )* '\"' {setText(getText().substring(1,getText().length()-1));} ;
fragment
ESC_SEQ
			:	'\\' ('b'|'t'|'n'|'f'|'r'|'\"'|'\''|'\\');	// " (<- fix vi highlighting)
fragment
HEXCHAR		:	('0'..'9'|'a'..'f'|'A'..'F');
COLOR		:	('#' HEXCHAR HEXCHAR HEXCHAR (HEXCHAR HEXCHAR HEXCHAR)?
			|	RED | GREEN | BLUE | WHITE | BLACK);
DEF			:	'def';
ON_SIGNAL	:	'on signal';
SIGNAL_DATA	:	'signal_data';
COLON		:	':';

// Operators
PLUS	 	:	'+';
MINUS		:	'-';
MUL			:	'*';
DIV			:	'/';
MOD			: 	'%';
OR			:	('or' | '||') {setText("or");};
AND			:	('and' | '&&') {setText("and");};
INCR		:	'\u2191'; // up arrow
DECR		:	'\u2193'; // down arrow

// Comparators
NOT			:	('!' | 'not') {setText("not");};
LT			:	'<';
LE			:	'<=';
GT			:	'>';
GE			:	'>=';
EQ			:	'==';
NEQ			:	'!=';

// Statements
IF			:	'if' ;
ELSE 		:	'else';	
WHILE		:	'while';
FOREACH		:	'foreach';
FOR			:	'for';
IN			:	'in';
BREAK		:	'break';
PASS		:	'pass';
RETURN		:	'return';
RETURNS		:	'returns';
PRINT		:	'print';
DELETE		:	'delete';
AT			:	'at';
T_ARRAY		:	LARRAY RARRAY;

// A future version could support native unit transformations like those:
//SPEEDUNIT	:	('km/h' | 'm/s' | 'rpm' | 'mph');
//TIMEUNIT	:	('min' | 'sec' | 'ms');
//DISTUNIT	:	('cm' | 'mm');

// Type descriptor tokens
T_INT 		:	'int';
T_BOOL		:	'bool';
T_FLOAT		:	'float';
T_STRING	:	'string';
T_COLOR		:	'color';
T_SENSOR	:	'sensor';
T_MOTOR		:	'motor';
T_BUTTON	:	'button';

// Lego I/O constants
DEFAULT		:	'default';
SENSOR_TYPE	:	('ultrasonic' | 'sound' | 'touch' | 'light');
SENSOR_PORT	:	('S1' | 'S2' | 'S3' | 'S4');
MOTOR		:	('motorA' | 'motorB' | 'motorC'); 
BUTTON		:	('ENTER' | 'LEFT' | 'RIGHT' | 'ESCAPE');

// Color constants
fragment
RED 		:	'RED' {setText("#FF0000");};
fragment
GREEN		:	'GREEN' {setText("#00FF00");};
fragment
BLUE		:	'BLUE' {setText("#0000FF");};
fragment
WHITE		:	'WHITE' {setText("#FFFFFF");};
fragment
BLACK		:	'BLACK' {setText("#000000");};

ID 			:	('a'..'z'|'A'..'Z'|'_') ('a'..'z'|'A'..'Z'|'0'..'9'|'_')* ;

LENGTH		:	'#' text=ID
				{
					emit(new CommonToken(LENGTH, "LENGTH"));
					emit(new CommonToken(ID, text.getText()));
				}
			;

fragment
COMMENT_PRE :	(('\r'? '\n') (' ' | '\t')*)*
			;

COMMENT		:	COMMENT_PRE '//' ~('\n'|'\r')* { $channel=HIDDEN; }
			| 	COMMENT_PRE '/*' ( options {greedy=false;} : .)* '*/' {$channel=HIDDEN;}
			;

INDENT		:	'\r'? '\n' ((' ' | '\t')* '\r'? '\n')* ('\t'* | '    '+)
				{
					String text = getText().substring(getText().lastIndexOf("\n")+1);

					// Check indentation type
					int type = -1;
					if (text.length() > 0)
						type = (text.charAt(0) == '\t') ? 0 : 1;

					if (indentationType == -1) {
						indentationType = type;
					} else if (type != -1 && type != indentationType) {
						emitErrorMessage("Style error: mixing tabs and spaces");
					}

					// Indentation level
					int i = (type == 0) ? text.length() : text.length() / 4;

					if (i > indentationLevel) {
						// Emit the NEWLINE we have stolen and increase the
						// indentation level
						emit(new CommonToken(NEWLINE));
						for (; indentationLevel < i; ++indentationLevel) {
							emit(new CommonToken(BEGIN/*, "BEGIN "+(indentationLevel+1)*/));
						}
					} else if (i < indentationLevel) {
						// Decrease indentation level and emit the NEWLINE
						for (; indentationLevel > i; --indentationLevel) {
							emit(new CommonToken(END/*, "END "+(indentationLevel+1)*/));
						}
						emit(new CommonToken(NEWLINE));
					} else {
						// No indentation changes -- just emit the stolen NEWLINE
						emit(new CommonToken(NEWLINE));
					}
				}
			;

// Note: The '\n'* already got intercepted by INDENT, we are just handling
// the semi-colon here...
NEWLINE		:	('\n'* | ';');

WS			:	( ' ' | '\t' | '\r' ) { $channel=HIDDEN; } ;

// This does nothing but hides some annoying antlr warnings...
BEGIN		:	{1 == 2}? => 'fuh';
END			:	{1 == 2}? => 'fuh';

// vim: noexpandtab ts=4 sw=4
