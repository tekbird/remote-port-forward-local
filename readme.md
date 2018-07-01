there are two components

1. client
2. server


1. functions of client
1.1 start a local tcp listener
1.2 connect to remote websocket server and wait for a ready command
1.3 buffer any data received from local tcp client until ready command
1.4 write all buffered data and any subsequent data to websocket
1.5 write any incoming data from websocket to local client


2. functions of server
2.1 accept a websocket connection and send write ready command
2.2 connect to the specified port in the remote machine 
2.3 write data from connected port to websocket
2.4 write data from websocket to the connected port
