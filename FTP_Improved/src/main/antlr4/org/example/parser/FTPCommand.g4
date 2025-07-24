grammar FTPCommand;

start
    : command EOF
    ;

command
    : LIST
    | GET filename
    | PUT filename
    | LOGIN username password
    ;

filename: WORD;
username: WORD;
password: WORD;

LIST: 'LIST';
GET: 'GET';
PUT: 'PUT';
LOGIN: 'LOGIN';

WORD: [a-zA-Z0-9_.-]+;
WS: [ \t\r\n]+ -> skip;
