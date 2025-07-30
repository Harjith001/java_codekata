grammar FTPCommand;

start
    : command EOF
    ;

command
    : LIST
    | GET filename
    | PUT filename length
    | LOGIN username password
    | QUIT
    ;

filename: WORD;
username: WORD;
password: WORD;
length: NUMBER;

LIST: 'LIST';
GET: 'GET';
PUT: 'PUT';
LOGIN: 'LOGIN';
QUIT: 'QUIT';

NUMBER: [0-9]+;
WORD: [a-zA-Z0-9_.-]+;
WS: [ \t\r\n]+ -> skip;
