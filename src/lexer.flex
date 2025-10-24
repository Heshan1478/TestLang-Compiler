import java_cup.runtime.Symbol;

%%

%class Lexer
%unicode
%cup
%line
%column
%public

%{
    // Helper method for building symbols with values
    private Symbol symbol(int type, Object value) {
        return new Symbol(type, yyline + 1, yycolumn + 1, value);
    }
%}

%%

/* ---------- Whitespace & Comments ---------- */
[ \t\r\n]+                        { /* skip whitespace */ }
"//".*                            { /* skip line comments */ }

/* ---------- Keywords ---------- */
"config"                          { return symbol(sym.CONFIG, null); }
"base_url"                        { return symbol(sym.BASE_URL, null); }
"header"                          { return symbol(sym.HEADER, null); }
"let"                             { return symbol(sym.LET, null); }
"test"                            { return symbol(sym.TEST, null); }
"GET"                             { return symbol(sym.GET, null); }
"POST"                            { return symbol(sym.POST, null); }
"PUT"                             { return symbol(sym.PUT, null); }
"DELETE"                          { return symbol(sym.DELETE, null); }
"expect"                          { return symbol(sym.EXPECT, null); }
"status"                          { return symbol(sym.STATUS, null); }
"body"                            { return symbol(sym.BODY, null); }
"contains"                        { return symbol(sym.CONTAINS, null); }
"in"                              { return symbol(sym.IN, null); }

/* ---------- Symbols ---------- */
"{"                               { return symbol(sym.LBRACE, null); }
"}"                               { return symbol(sym.RBRACE, null); }
"="                               { return symbol(sym.EQUALS, null); }
";"                               { return symbol(sym.SEMICOLON, null); }
".."                              { return symbol(sym.DOTDOT, null); }

/* ---------- Literals ---------- */
// Triple-quoted string (multiline support)
\"\"\"([^\"]|\"[^\"]|\"\"[^\"])*\"\"\"  {
    String s = yytext().substring(3, yytext().length() - 3);
    return symbol(sym.TRIPLE_STRING, s);
}

// Double-quoted string with escape support for \" and \\
\"([^\"\\]|\\.)*\"                {
    String s = yytext().substring(1, yytext().length() - 1);
    s = s.replace("\\\"", "\"").replace("\\\\", "\\");
    return symbol(sym.STRING, s);
}

// Numbers (only non-negative integers)
[0-9]+                            { return symbol(sym.NUMBER, Integer.parseInt(yytext())); }

// Catch invalid identifiers starting with digit (BEFORE valid identifiers)
[0-9]+[A-Za-z_][A-Za-z0-9_]*      {
    System.err.println("\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
    System.err.println("LEXICAL ERROR:");
    System.err.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
    System.err.println("Location: Line " + (yyline + 1) + ", Column " + (yycolumn + 1));
    System.err.println("Invalid identifier: '" + yytext() + "'");
    System.err.println("\nError: Identifier cannot start with a digit");
    System.err.println("Hint: Variable names must start with a letter or underscore");
    System.err.println("      Valid examples: a2, user1, _temp");
    System.err.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
    System.exit(1);
}

// Identifiers (variable/test names)
[A-Za-z_][A-Za-z0-9_]*            { return symbol(sym.IDENT, yytext()); }

/* ---------- End of file ---------- */
<<EOF>>                           { return symbol(sym.EOF, null); }

/* ---------- Error handling ---------- */
.                                 {
    System.err.println("Lexical error at line " + (yyline + 1) +
      ", column " + (yycolumn + 1) +
      ": Unexpected character '" + yytext() + "'");
}