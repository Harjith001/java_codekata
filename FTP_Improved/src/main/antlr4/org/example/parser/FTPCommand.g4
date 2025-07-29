grammar FTPCommand;

start
    : command EOF
    ;

command
    : LIST
    | GET filename
    | PUT filename content
    | LOGIN username password
    | QUIT
    ;

filename: WORD;
username: WORD;
password: WORD;
content: STRING;

LIST: 'LIST';
GET: 'GET';
PUT: 'PUT';
LOGIN: 'LOGIN';
QUIT: 'QUIT';
STRING: '"' ( ~["\\] | '\\' . )* '"';

WORD: [a-zA-Z0-9_.-]+;
WS: [ \t\r\n]+ -> skip;
